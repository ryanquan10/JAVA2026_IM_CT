/*
 * axujbjtalxf本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ljoflad
 */
package org.tio.core;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.TioClientConfig;
import org.tio.client.ReconnConf;
import org.tio.clu.client.CluClient;
import org.tio.clu.common.BindType;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.bs.BindUserToGroup;
import org.tio.core.ChannelContext.CloseCode;
import org.tio.core.intf.Packet;
import org.tio.core.intf.PacketMeta;
import org.tio.core.intf.SynSendHandler;
import org.tio.server.TioServerConfig;
import org.tio.utils.SystemTimer;
import org.tio.utils.Threads;
import org.tio.utils.convert.Converter;
import org.tio.utils.hutool.CollUtil;
import org.tio.utils.json.Json;
import org.tio.utils.lock.ReadLockHandler;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.page.Page;
import org.tio.utils.page.PageUtils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

/**
 * The Class Tio. t-io用户关心的API几乎全在这
 *
 * @author tanyaowu
 */
public class Tio {
	public static class IpBlacklist {
		/**
		 * 添加全局ip黑名单
		 * 
		 * @param ip
		 * @return
		 * @author tanyaowu
		 */
		public static boolean add(String ip) {
			return org.tio.core.maintain.IpBlacklist.GLOBAL.add(ip);
		}

		/**
		 * 把ip添加到黑名单，此黑名单只针对tioConfig有效，其它tioConfig不会把这个ip视为黑名单
		 * 
		 * @param tioConfig
		 * @param ip
		 * @author tanyaowu
		 */
		public static boolean add(TioConfig tioConfig, String ip) {
			return tioConfig.ipBlacklist.add(ip);
		}

		/**
		 * 清空全局黑名单
		 * 
		 * @author tanyaowu
		 */
		public static void clear() {
			org.tio.core.maintain.IpBlacklist.GLOBAL.clear();
		}

		/**
		 * 清空黑名单，只针对tioConfig有效
		 * 
		 * @param tioConfig
		 * @author tanyaowu
		 */
		public static void clear(TioConfig tioConfig) {
			tioConfig.ipBlacklist.clear();
		}

		/**
		 * 获取全局黑名单
		 * 
		 * @return
		 * @author tanyaowu
		 */
		public static Collection<String> getAll() {
			return org.tio.core.maintain.IpBlacklist.GLOBAL.getAll();
		}

		/**
		 * 获取ip黑名单列表
		 * 
		 * @param tioConfig
		 * @return
		 * @author tanyaowu
		 */
		public static Collection<String> getAll(TioConfig tioConfig) {
			return tioConfig.ipBlacklist.getAll();
		}

		/**
		 * 是否在黑名单中
		 * 
		 * @param tioConfig
		 * @param ip
		 * @return
		 * @author tanyaowu
		 */
		public static boolean isInBlacklist(TioConfig tioConfig, String ip) {
			return tioConfig.ipBlacklist.isInBlacklist(ip) || org.tio.core.maintain.IpBlacklist.GLOBAL.isInBlacklist(ip);
		}

		/**
		 * 删除全局黑名单
		 * 
		 * @param ip
		 * @author tanyaowu
		 */
		public static void remove(String ip) {
			org.tio.core.maintain.IpBlacklist.GLOBAL.remove(ip);
		}

		/**
		 * 把ip从黑名单中删除
		 * 
		 * @param tioConfig
		 * @param ip
		 * @author tanyaowu
		 */
		public static void remove(TioConfig tioConfig, String ip) {
			tioConfig.ipBlacklist.remove(ip);
		}
	}

	private static class WaitSynSendHandler<T extends Packet> implements SynSendHandler<T> {
		private T respPacket = null;

		public T getRespPacket() {
			return respPacket;
		}

		@Override
		public void onResp(ChannelContext channelContext, T initPacket, T respPacket, long timeout) {
			synchronized (this) {
				this.respPacket = respPacket;
				this.notify();
			}
		}

		@Override
		public void onTimeout(ChannelContext channelContext, T initPacket, long timeout) {
			synchronized (this) {
				this.notify();
			}
		}
	}

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(Tio.class);

	/**
	 * 绑定业务id
	 * 
	 * @param channelContext
	 * @param bsId
	 * @author tanyaowu
	 */
	public static void bindBsId(ChannelContext channelContext, String bsId) {
		channelContext.tioConfig.bsIds.bind(channelContext, bsId);
	}

	/**
	 * 绑定群组
	 * 
	 * @param channelContext
	 * @param group
	 * @author tanyaowu
	 */
	public static void bindGroup(ChannelContext channelContext, String group) {
		channelContext.tioConfig.groups.bind(group, channelContext);
	}

	/**
	 * 将用户绑定到群组
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param group
	 */
	public static void bindGroup(TioConfig tioConfig, String userid, String group) {
		bindGroup(tioConfig, userid, group, true);
	}
	
	/**
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param group
	 * @param notifyClu
	 */
	public static void bindGroup(TioConfig tioConfig, String userid, String group, boolean notifyClu) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByUserid(tioConfig, userid);
		if (setWithLock != null && setWithLock.size() > 0) {
			setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
				@Override
				public void handler(Set<ChannelContext> set) {
					for (ChannelContext channelContext : set) {
						Tio.bindGroup(channelContext, group);
					}
				}
			});
		} else {
			if (notifyClu) {
				BindUserToGroup bindUserToGroup = new BindUserToGroup(userid, group);
				CluPacket cluPacket = CluPacket.from(Command.BindUserToGroup, bindUserToGroup);
				CluClient.transfer(tioConfig, BindType.BindUserToGroup, null, cluPacket);
			}
		}
	}

	/**
	 * 绑定token
	 * 
	 * @param channelContext
	 * @param token
	 * @author tanyaowu
	 */
	public static void bindToken(ChannelContext channelContext, String token) {
		channelContext.tioConfig.tokens.bind(token, channelContext);
	}

	/**
	 * 绑定用户
	 * 
	 * @param channelContext
	 * @param userid
	 * @author tanyaowu
	 */
	public static void bindUser(ChannelContext channelContext, String userid) {
		channelContext.tioConfig.users.bind(userid, channelContext);
	}

	/**
	 * 阻塞发送消息到指定ChannelContext
	 * 
	 * @param channelContext
	 * @param packet
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean bSend(ChannelContext channelContext, Packet packet) {
		if (channelContext == null) {
			return false;
		}
		CountDownLatch countDownLatch = new CountDownLatch(1);
		return send(channelContext, packet, countDownLatch, PacketSendMode.SINGLE_BLOCK);
	}

	/**
	 * 发送到指定的ip和port
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param port
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean bSend(TioConfig tioConfig, String ip, int port, Packet packet) {
		return send(tioConfig, ip, port, packet, true);
	}

	/**
	 * 发消息到所有连接
	 * 
	 * @param tioConfig
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static Boolean bSendToAll(TioConfig tioConfig, Packet packet, ChannelContextFilter channelContextFilter) {
		return sendToAll(tioConfig, packet, channelContextFilter, true);
	}

	/**
	 * 阻塞发消息给指定业务ID
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean bSendToBsId(TioConfig tioConfig, String bsId, Packet packet) {
		return sendToBsId(tioConfig, bsId, packet, true);
	}

	/**
	 * 发消息到组
	 * 
	 * @param tioConfig
	 * @param group
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean bSendToGroup(TioConfig tioConfig, String group, Packet packet) {
		return bSendToGroup(tioConfig, group, packet, null);
	}

	/**
	 * 发消息到组
	 * 
	 * @param tioConfig
	 * @param group
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static Boolean bSendToGroup(TioConfig tioConfig, String group, Packet packet, ChannelContextFilter channelContextFilter) {
		return sendToGroup(tioConfig, group, packet, channelContextFilter, true);
	}

	/**
	 * 发消息给指定ChannelContext id
	 * 
	 * @param channelContextId
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean bSendToId(TioConfig tioConfig, String channelContextId, Packet packet) {
		return sendToId(tioConfig, channelContextId, packet, true);
	}

	/**
	 * 阻塞发送到指定ip对应的集合
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param packet
	 * @author: tanyaowu
	 */
	public static Boolean bSendToIp(TioConfig tioConfig, String ip, Packet packet) {
		return bSendToIp(tioConfig, ip, packet, null);
	}

	/**
	 * 阻塞发送到指定ip对应的集合
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param packet
	 * @param channelContextFilter
	 * @return
	 * @author: tanyaowu
	 */
	public static Boolean bSendToIp(TioConfig tioConfig, String ip, Packet packet, ChannelContextFilter channelContextFilter) {
		return sendToIp(tioConfig, ip, packet, channelContextFilter, true);
	}

	/**
	 * 发消息到指定集合
	 * 
	 * @param tioConfig
	 * @param setWithLock
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static Boolean bSendToSet(TioConfig tioConfig, SetWithLock<ChannelContext> setWithLock, Packet packet, ChannelContextFilter channelContextFilter) {
		return sendToSet(tioConfig, setWithLock, packet, channelContextFilter, true);
	}

	/**
	 * 阻塞发消息到指定token
	 * 
	 * @param tioConfig
	 * @param token
	 * @param packet
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean bSendToToken(TioConfig tioConfig, String token, Packet packet) {
		return sendToToken(tioConfig, token, packet, true);
	}

	/**
	 * 阻塞发消息给指定用户
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param packet
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean bSendToUser(TioConfig tioConfig, String userid, Packet packet) {
		return sendToUser(tioConfig, userid, packet, true);
	}

	/**
	 * 关闭连接
	 * 
	 * @param channelContext
	 * @param remark
	 * @author tanyaowu
	 */
	public static void close(ChannelContext channelContext, String remark) {
		close(channelContext, null, remark);
	}

	/**
	 * 
	 * @param channelContext
	 * @param remark
	 * @param closeCode
	 */
	public static void close(ChannelContext channelContext, String remark, CloseCode closeCode) {
		close(channelContext, null, remark, closeCode);
	}

	/**
	 * 关闭连接
	 * 
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @author tanyaowu
	 */
	public static void close(ChannelContext channelContext, Throwable throwable, String remark) {
		close(channelContext, throwable, remark, false);
	}

	public static void close(ChannelContext channelContext, Throwable throwable, String remark, boolean isNeedRemove) {
		close(channelContext, throwable, remark, isNeedRemove, true);
	}

	public static void close(ChannelContext channelContext, Throwable throwable, String remark, boolean isNeedRemove, boolean needCloseLock) {
		close(channelContext, throwable, remark, isNeedRemove, needCloseLock, null);
	}

	/**
	 * 
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @param isNeedRemove
	 * @param needCloseLock
	 */
	public static void close(ChannelContext channelContext, Throwable throwable, String remark, boolean isNeedRemove, boolean needCloseLock, CloseCode closeCode) {
		if (channelContext == null) {
			return;
		}
		if (channelContext.isWaitingClose) {
			log.debug("{} 正在等待被关闭", channelContext);
			return;
		}

		// 先立即取消各项任务，这样可防止有新的任务被提交进来
		channelContext.decodeRunnable.setCanceled(true);
		channelContext.handlerRunnable.setCanceled(true);
		channelContext.sendRunnable.setCanceled(true);

		WriteLock writeLock = null;
		if (needCloseLock) {
			writeLock = channelContext.closeLock.writeLock();

			boolean tryLock = writeLock.tryLock();
			if (!tryLock) {
				return;
			}
			channelContext.isWaitingClose = true;
			writeLock.unlock();
		} else {
			channelContext.isWaitingClose = true;
		}

		if (closeCode == null) {
			if (channelContext.getCloseCode() == CloseCode.INIT_STATUS) {
				channelContext.setCloseCode(CloseCode.NO_CODE);
			}
		} else {
			channelContext.setCloseCode(closeCode);
		}

		if (channelContext.asynchronousSocketChannel != null) {
			try {
				channelContext.asynchronousSocketChannel.shutdownInput();
			} catch (Throwable e) {
				// log.error("", e);
			}
			try {
				channelContext.asynchronousSocketChannel.shutdownOutput();
			} catch (Throwable e) {
				// log.error("", e);
			}
			try {
				channelContext.asynchronousSocketChannel.close();
			} catch (Throwable e) {
				// log.error("", e);
			}
		}

		channelContext.closeMeta.setRemark(remark);
		channelContext.closeMeta.setThrowable(throwable);
		if (!isNeedRemove) {
			if (channelContext.isServer()) {
				isNeedRemove = true;
			} else {
				ClientChannelContext clientChannelContext = (ClientChannelContext) channelContext;
				if (!ReconnConf.isNeedReconn(clientChannelContext, false)) { // 不需要重连
					isNeedRemove = true;
				}
			}
		}
		channelContext.closeMeta.setNeedRemove(isNeedRemove);

		channelContext.tioConfig.closeRunnable.addMsg(channelContext);
		channelContext.tioConfig.closeRunnable.execute();
	}

	public static void close(ChannelContext channelContext, Throwable throwable, String remark, boolean isNeedRemove, CloseCode closeCode) {
		close(channelContext, throwable, remark, isNeedRemove, true, closeCode);
	}

	public static void close(ChannelContext channelContext, Throwable throwable, String remark, CloseCode closeCode) {
		close(channelContext, throwable, remark, false, closeCode);
	}

	/**
	 * 关闭连接
	 * 
	 * @param tioConfig
	 * @param clientIp
	 * @param clientPort
	 * @param throwable
	 * @param remark
	 * @author tanyaowu
	 */
	public static void close(TioConfig tioConfig, String clientIp, Integer clientPort, Throwable throwable, String remark) {
		ChannelContext channelContext = tioConfig.clientNodes.find(clientIp, clientPort);
		close(channelContext, throwable, remark);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param group
	 * @param remark
	 * @return
	 */
	public static void closeGroup(TioConfig tioConfig, String group, String remark) {
		closeGroup(tioConfig, group, remark, null);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param group
	 * @param remark
	 * @param closeCode
	 */
	public static void closeGroup(TioConfig tioConfig, String group, String remark, CloseCode closeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByGroup(tioConfig, group);
		closeSet(tioConfig, setWithLock, remark, closeCode);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param remark
	 * @return
	 */
	public static void closeIp(TioConfig tioConfig, String ip, String remark) {
		closeIp(tioConfig, ip, remark, null);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param remark
	 * @param closeCode
	 */
	public static void closeIp(TioConfig tioConfig, String ip, String remark, CloseCode closeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByIp(tioConfig, ip);
		closeSet(tioConfig, setWithLock, remark, closeCode);
	}

	/**
	 * 关闭集合
	 * 
	 * @param tioConfig
	 * @param setWithLock
	 * @param remark
	 * @param closeCode
	 * @author tanyaowu
	 */
	public static void closeSet(TioConfig tioConfig, SetWithLock<ChannelContext> setWithLock, String remark, CloseCode closeCode) {
		if (setWithLock != null && setWithLock.size() > 0) {
			setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
				@Override
				public void handler(Set<ChannelContext> set) {
					for (ChannelContext channelContext : set) {
						Tio.close(channelContext, remark, closeCode);
					}
				}
			});
		}
	}

	/**
	 * 关闭token的所有连接
	 * 
	 * @param tioConfig
	 * @param token
	 * @param remark
	 * @return
	 */
	public static void closeToken(TioConfig tioConfig, String token, String remark) {
		closeToken(tioConfig, token, remark, null);
	}

	/**
	 * 关闭某token的所有连接
	 * 
	 * @param tioConfig
	 * @param token
	 * @param remark
	 * @param closeCode
	 */
	public static void closeToken(TioConfig tioConfig, String token, String remark, CloseCode closeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByToken(tioConfig, token);
		closeSet(tioConfig, setWithLock, remark, closeCode);
	}

	/**
	 * 关闭用户的所有连接
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param remark
	 * @return
	 */
	public static void closeUser(TioConfig tioConfig, String userid, String remark) {
		closeUser(tioConfig, userid, remark, null);
	}

	/**
	 * 关闭某用户的所有连接
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param remark
	 * @param closeCode
	 */
	public static void closeUser(TioConfig tioConfig, String userid, String remark, CloseCode closeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByUserid(tioConfig, userid);
		closeSet(tioConfig, setWithLock, remark, closeCode);
	}

	/**
	 * 在线TCP连接数
	 * 
	 * @return
	 * @author tanyaowu
	 */
	public static int online() {
		return getAllTcpCount();
	}

	/**
	 * 获取所有连接，包括当前处于断开状态的
	 * 
	 * @param tioConfig
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getAll(TioConfig tioConfig) {
		return tioConfig.connections;
	}

	/**
	 * 获取所有连接，包括当前处于断开状态的
	 * 
	 * @param tioConfig
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getAll(TioConfig tioConfig)
	 */
	@Deprecated
	public static SetWithLock<ChannelContext> getAllChannelContexts(TioConfig tioConfig) {
		return getAll(tioConfig);
	}

	/**
	 * 此API仅供 tio client使用 获取所有处于正常连接状态的连接
	 * 
	 * @param tioClientConfig
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getConnecteds(TioClientConfig tioClientConfig)
	 */
	@Deprecated
	public static SetWithLock<ChannelContext> getAllConnectedsChannelContexts(TioClientConfig tioClientConfig) {
		return getConnecteds(tioClientConfig);
	}

	/**
	 * 
	 * @return
	 * @author tanyaowu
	 */
	public static int getAllTcpCount() {
		Set<TioServerConfig> set = TioConfig.ALL_SERVER_GROUPCONTEXTS;
		int lin = 0;
		if (CollUtil.isNotEmpty(set)) {
			for (TioServerConfig tioServerConfig : set) {
				if (tioServerConfig.isBeShared()) {
					continue;
				}

				int size = Tio.getAll(tioServerConfig).size();
				lin = lin + size;
			}
		}
		return lin;
	}

	/**
	 * 根据业务id找ChannelContext
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @return
	 * @author tanyaowu
	 */
	public static ChannelContext getByBsId(TioConfig tioConfig, String bsId) {
		return tioConfig.bsIds.find(tioConfig, bsId);
	}

	/**
	 * 根据ChannelContext.id获取ChannelContext
	 * 
	 * @param channelContextId
	 * @return
	 * @author tanyaowu
	 */
	public static ChannelContext getByChannelContextId(TioConfig tioConfig, String channelContextId) {
		return tioConfig.ids.find(tioConfig, channelContextId);
	}

	/**
	 * 根据clientip和clientport获取ChannelContext
	 * 
	 * @param tioConfig
	 * @param clientIp
	 * @param clientPort
	 * @return
	 * @author tanyaowu
	 */
	public static ChannelContext getByClientNode(TioConfig tioConfig, String clientIp, Integer clientPort) {
		return tioConfig.clientNodes.find(clientIp, clientPort);
	}

	/**
	 * 获取一个组的所有客户端
	 * 
	 * @param tioConfig
	 * @param group
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getByGroup(TioConfig tioConfig, String group) {
		return tioConfig.groups.clients(tioConfig, group);
	}

	/**
	 * 根据客户端ip获取SetWithLock<ChannelContext>
	 * 
	 * @param tioConfig
	 * @param ip
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getByIp(TioConfig tioConfig, String ip) {
		return tioConfig.ips.clients(tioConfig, ip);
	}

	/**
	 * 根据token获取SetWithLock<ChannelContext>
	 * 
	 * @param tioConfig
	 * @param token
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getByToken(TioConfig tioConfig, String token) {
		return tioConfig.tokens.find(tioConfig, token);
	}

	/**
	 * 根据userid获取SetWithLock<ChannelContext>
	 * 
	 * @param tioConfig
	 * @param userid
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getByUserid(TioConfig tioConfig, String userid) {
		return tioConfig.users.find(tioConfig, userid);
	}

	/**
	 * 根据业务id找ChannelContext
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getByBsId(TioConfig tioConfig, String bsId)
	 */
	@Deprecated
	public static ChannelContext getChannelContextByBsId(TioConfig tioConfig, String bsId) {
		return getByBsId(tioConfig, bsId);
	}

	/**
	 * 根据clientip和clientport获取ChannelContext
	 * 
	 * @param tioConfig
	 * @param clientIp
	 * @param clientPort
	 * @return
	 * @author tanyaowu
	 * @deprecated getByClientNode(tioConfig, clientIp, clientPort)
	 */
	@Deprecated
	public static ChannelContext getChannelContextByClientNode(TioConfig tioConfig, String clientIp, Integer clientPort) {
		return getByClientNode(tioConfig, clientIp, clientPort);
	}

	/**
	 * 根据ChannelContext.id获取ChannelContext
	 * 
	 * @param channelContextId
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getByChannelContextId(tioConfig, channelContextId)
	 */
	@Deprecated
	public static ChannelContext getChannelContextById(TioConfig tioConfig, String channelContextId) {
		return getByChannelContextId(tioConfig, channelContextId);
	}

	/**
	 * 获取一个组的所有客户端
	 * 
	 * @param tioConfig
	 * @param group
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getByGroup(tioConfig, group)
	 */
	@Deprecated
	public static SetWithLock<ChannelContext> getChannelContextsByGroup(TioConfig tioConfig, String group) {
		return getByGroup(tioConfig, group);
	}

	/**
	 * 根据token获取SetWithLock<ChannelContext>
	 * 
	 * @param tioConfig
	 * @param token
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getByToken(tioConfig, token)
	 */
	@Deprecated
	public static SetWithLock<ChannelContext> getChannelContextsByToken(TioConfig tioConfig, String token) {
		return getByToken(tioConfig, token);
	}

	/**
	 * 根据userid获取SetWithLock<ChannelContext>
	 * 
	 * @param tioConfig
	 * @param userid
	 * @return
	 * @author tanyaowu
	 * @deprecated 用getByUserid(tioConfig, userid)
	 */
	@Deprecated
	public static SetWithLock<ChannelContext> getChannelContextsByUserid(TioConfig tioConfig, String userid) {
		return getByUserid(tioConfig, userid);
	}

	/**
	 * 此API仅供 tio client使用 获取所有处于正常连接状态的连接
	 * 
	 * @param tioClientConfig
	 * @return
	 * @author tanyaowu
	 */
	public static SetWithLock<ChannelContext> getConnecteds(TioClientConfig tioClientConfig) {
		return tioClientConfig.connecteds;
	}

	/**
	 *
	 * @param tioConfig
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public static Page<ChannelContext> getPageOfAll(TioConfig tioConfig, Integer pageIndex, Integer pageSize) {
		return getPageOfAll(tioConfig, pageIndex, pageSize, null);
	}

	/**
	 * 
	 * @param tioConfig
	 * @param pageIndex
	 * @param pageSize
	 * @param converter
	 * @return
	 */
	public static <T> Page<T> getPageOfAll(TioConfig tioConfig, Integer pageIndex, Integer pageSize, Converter<T> converter) {
		SetWithLock<ChannelContext> setWithLock = Tio.getAllChannelContexts(tioConfig);
		return PageUtils.fromSetWithLock(setWithLock, pageIndex, pageSize, converter);
	}

	/**
	 * 这个方法是给客户器端用的
	 * 
	 * @param tioClientConfig
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public static Page<ChannelContext> getPageOfConnecteds(TioClientConfig tioClientConfig, Integer pageIndex, Integer pageSize) {
		return getPageOfConnecteds(tioClientConfig, pageIndex, pageSize, null);
	}

	/**
	 * 这个方法是给客户器端用的
	 * 
	 * @param tioClientConfig
	 * @param pageIndex
	 * @param pageSize
	 * @param converter
	 * @return
	 * @author tanyaowu
	 */
	public static <T> Page<T> getPageOfConnecteds(TioClientConfig tioClientConfig, Integer pageIndex, Integer pageSize, Converter<T> converter) {
		SetWithLock<ChannelContext> setWithLock = Tio.getAllConnectedsChannelContexts(tioClientConfig);
		return PageUtils.fromSetWithLock(setWithLock, pageIndex, pageSize, converter);
	}

	/**
	 *
	 * @param tioConfig
	 * @param group
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public static Page<ChannelContext> getPageOfGroup(TioConfig tioConfig, String group, Integer pageIndex, Integer pageSize) {
		return getPageOfGroup(tioConfig, group, pageIndex, pageSize, null);
	}

	/**
	 * 
	 * @param tioConfig
	 * @param group
	 * @param pageIndex
	 * @param pageSize
	 * @param converter
	 * @return
	 */
	public static <T> Page<T> getPageOfGroup(TioConfig tioConfig, String group, Integer pageIndex, Integer pageSize, Converter<T> converter) {
		SetWithLock<ChannelContext> setWithLock = Tio.getChannelContextsByGroup(tioConfig, group);
		return PageUtils.fromSetWithLock(setWithLock, pageIndex, pageSize, converter);
	}

	/**
	 * 群组有多少个连接
	 * 
	 * @param tioConfig
	 * @param group
	 * @return
	 */
	public static int groupCount(TioConfig tioConfig, String group) {
		SetWithLock<ChannelContext> setWithLock = tioConfig.groups.clients(tioConfig, group);
		if (setWithLock == null) {
			return 0;
		}

		return setWithLock.size();
	}

	/**
	 * 某通道是否在某群组中
	 * 
	 * @param group
	 * @param channelContext
	 * @return true：在该群组
	 * @author: tanyaowu
	 */
	public static boolean isInGroup(String group, ChannelContext channelContext) {
		SetWithLock<String> setWithLock = channelContext.getGroups();
		if (setWithLock == null || setWithLock.size() == 0) {
			return false;
		}

		return setWithLock.contains(group);
	}

	/**
	 * 
	 * @param channelContext
	 * @param remark
	 */
	public static void remove(ChannelContext channelContext, String remark) {
		remove(channelContext, remark, null);
	}

	/**
	 * 和close方法对应，只不过不再进行重连等维护性的操作
	 * 
	 * @param channelContext
	 * @param remark
	 * @param closeCode
	 */
	public static void remove(ChannelContext channelContext, String remark, CloseCode closeCode) {
		remove(channelContext, null, remark, closeCode);
	}

	/**
	 * 和close方法对应，只不过不再进行重连等维护性的操作
	 * 
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 */
	public static void remove(ChannelContext channelContext, Throwable throwable, String remark) {
		remove(channelContext, throwable, remark, (CloseCode) null);
	}

	/**
	 * 和close方法对应，只不过不再进行重连等维护性的操作
	 * 
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @param closeCode
	 */
	public static void remove(ChannelContext channelContext, Throwable throwable, String remark, CloseCode closeCode) {
		close(channelContext, throwable, remark, true, closeCode);
	}

	/**
	 * 删除clientip为指定值的所有连接
	 * 
	 * @param tioServerConfig
	 * @param ip
	 * @param remark
	 */
	public static void remove(TioServerConfig tioServerConfig, String ip, String remark) {
		remove(tioServerConfig, ip, remark, (CloseCode) null);
	}

	/**
	 * 删除clientip为指定值的所有连接
	 * 
	 * @param tioServerConfig
	 * @param ip
	 * @param remark
	 * @param closeCode
	 */
	public static void remove(TioServerConfig tioServerConfig, String ip, String remark, CloseCode closeCode) {
		SetWithLock<ChannelContext> setWithLock = tioServerConfig.ips.clients(tioServerConfig, ip);
		if (setWithLock == null || setWithLock.size() == 0) {
			return;
		}

		setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
			@Override
			public void handler(Set<ChannelContext> set) {
				for (ChannelContext channelContext : set) {
					Tio.remove(channelContext, remark, closeCode);
				}
			}
		});
	}

	/**
	 * 和close方法对应，只不过不再进行重连等维护性的操作
	 * 
	 * @param tioConfig
	 * @param clientIp
	 * @param clientPort
	 * @param throwable
	 * @param remark
	 */
	public static void remove(TioConfig tioConfig, String clientIp, Integer clientPort, Throwable throwable, String remark) {
		remove(tioConfig, clientIp, clientPort, throwable, remark, (CloseCode) null);
	}

	/**
	 * 删除clientip和clientPort为指定值的连接
	 * 
	 * @param tioConfig
	 * @param clientIp
	 * @param clientPort
	 * @param throwable
	 * @param remark
	 * @param closeCode
	 */
	public static void remove(TioConfig tioConfig, String clientIp, Integer clientPort, Throwable throwable, String remark, CloseCode closeCode) {
		ChannelContext channelContext = tioConfig.clientNodes.find(clientIp, clientPort);
		remove(channelContext, throwable, remark, closeCode);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param group
	 * @param remark
	 * @return
	 */
	public static void removeGroup(TioConfig tioConfig, String group, String remark) {
		removeGroup(tioConfig, group, remark, null);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param group
	 * @param remark
	 * @param removeCode
	 */
	public static void removeGroup(TioConfig tioConfig, String group, String remark, CloseCode removeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByGroup(tioConfig, group);
		removeSet(tioConfig, setWithLock, remark, removeCode);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param remark
	 * @return
	 */
	public static void removeIp(TioConfig tioConfig, String ip, String remark) {
		removeIp(tioConfig, ip, remark, null);
	}

	/**
	 * 关闭某群所有连接
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param remark
	 * @param removeCode
	 */
	public static void removeIp(TioConfig tioConfig, String ip, String remark, CloseCode removeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByIp(tioConfig, ip);
		removeSet(tioConfig, setWithLock, remark, removeCode);
	}

	/**
	 * 移除集合
	 * 
	 * @param tioConfig
	 * @param setWithLock
	 * @param remark
	 * @param closeCode
	 * @author tanyaowu
	 */
	public static void removeSet(TioConfig tioConfig, SetWithLock<ChannelContext> setWithLock, String remark, CloseCode closeCode) {
		if (setWithLock != null && setWithLock.size() > 0) {
			setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
				@Override
				public void handler(Set<ChannelContext> set) {
					for (ChannelContext channelContext : set) {
						Tio.remove(channelContext, remark, closeCode);
					}
				}
			});
		}
	}

	/**
	 * 关闭token的所有连接
	 * 
	 * @param tioConfig
	 * @param token
	 * @param remark
	 * @return
	 */
	public static void removeToken(TioConfig tioConfig, String token, String remark) {
		removeToken(tioConfig, token, remark, null);
	}

	/**
	 * 关闭某token的所有连接
	 * 
	 * @param tioConfig
	 * @param token
	 * @param remark
	 * @param removeCode
	 */
	public static void removeToken(TioConfig tioConfig, String token, String remark, CloseCode removeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByToken(tioConfig, token);
		removeSet(tioConfig, setWithLock, remark, removeCode);
	}

	/**
	 * 关闭用户的所有连接
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param remark
	 * @return
	 */
	public static void removeUser(TioConfig tioConfig, String userid, String remark) {
		removeUser(tioConfig, userid, remark, null);
	}

	/**
	 * 关闭某用户的所有连接
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param remark
	 * @param removeCode
	 */
	public static void removeUser(TioConfig tioConfig, String userid, String remark, CloseCode removeCode) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByUserid(tioConfig, userid);
		removeSet(tioConfig, setWithLock, remark, removeCode);
	}

	/**
	 * 发送消息到指定ChannelContext
	 * 
	 * @param channelContext
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean send(ChannelContext channelContext, Packet packet) {
		return send(channelContext, packet, null, null);
	}

	/**
	 *
	 * @param channelContext
	 * @param packet
	 * @param countDownLatch
	 * @param packetSendMode
	 * @return
	 * @author tanyaowu
	 */
	private static Boolean send(final ChannelContext channelContext, Packet packet, CountDownLatch countDownLatch, PacketSendMode packetSendMode) {
		try {
			if (packet == null || channelContext == null) {
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
				return false;
			}

			if (channelContext.isVirtual) {
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
				return true;
			}

			if (channelContext.isClosed || channelContext.isRemoved) {
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
				if (channelContext != null) {
					log.info("can't send data, {}, isClosed:{}, isRemoved:{}", channelContext, channelContext.isClosed, channelContext.isRemoved);
				}
				return false;
			}

			if (channelContext.tioConfig.packetConverter != null) {
				Packet packet1 = channelContext.tioConfig.packetConverter.convert(packet, channelContext);
				if (packet1 == null) {
					if (log.isInfoEnabled()) {
						log.info("convert后为null，表示不需要发送", channelContext, packet.logstr());
					}
					return true;
				}
				packet = packet1;
			}

			boolean isSingleBlock = countDownLatch != null && packetSendMode == PacketSendMode.SINGLE_BLOCK;

			boolean isAdded = false;
			if (countDownLatch != null) {
				PacketMeta meta = new PacketMeta();
				meta.setCountDownLatch(countDownLatch);
				packet.setMeta(meta);
			}

			if (channelContext.tioConfig.useQueueSend) {
				isAdded = channelContext.sendRunnable.addMsg(packet);
			} else {
				isAdded = channelContext.sendRunnable.sendPacket(packet);
			}

			if (!isAdded) {
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
				return false;
			}
			if (channelContext.tioConfig.useQueueSend) {
				channelContext.sendRunnable.execute();
			}

			if (isSingleBlock) {
				long timeout = 10;
				try {
					Boolean awaitFlag = countDownLatch.await(timeout, TimeUnit.SECONDS);
					if (!awaitFlag) {
						log.error("{}, 阻塞发送超时, timeout:{}s, packet:{}", channelContext, timeout, packet.logstr());
					}
				} catch (InterruptedException e) {
					log.error("", e);
				}

				Boolean isSentSuccess = packet.getMeta().getIsSentSuccess();
				return isSentSuccess;
			} else {
				return true;
			}
		} catch (Throwable e) {
			log.error(channelContext + ", " + e.toString(), e);
			return false;
		}

	}

	/**
	 * 发送到指定的ip和port
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param port
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean send(TioConfig tioConfig, String ip, int port, Packet packet) {
		return send(tioConfig, ip, port, packet, false);
	}

	/**
	 * 发送到指定的ip和port
	 * 
	 * @param xjbcpvcyyddrgmoacuoimhjtk
	 * @param ip
	 * @param port
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	private static Boolean send(TioConfig xjbcpvcyyddrgmoacuoimhjtk, String ip, int port, Packet packet, boolean isBlock) {
		ChannelContext channelContext = xjbcpvcyyddrgmoacuoimhjtk.clientNodes.find(ip, port);
		if (channelContext != null) {
			if (isBlock) {
				return bSend(channelContext, packet);
			} else {
				return send(channelContext, packet);
			}
		} else {
			log.info("{}, can find channelContext by {}:{}", xjbcpvcyyddrgmoacuoimhjtk.getName(), ip, port);
			return false;
		}
	}

	public static void sendToAll(TioConfig tioConfig, Packet packet) {
		sendToAll(tioConfig, packet, null);
	}

	/**
	 * 发消息到所有连接
	 * 
	 * @param tioConfig
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static void sendToAll(TioConfig tioConfig, Packet packet, ChannelContextFilter channelContextFilter) {
		sendToAll(tioConfig, packet, channelContextFilter, false);
	}

	/**
	 *
	 * @param tioConfig
	 * @param packet
	 * @param channelContextFilter
	 * @param isBlock
	 * @author tanyaowu
	 */
	private static Boolean sendToAll(TioConfig tioConfig, Packet packet, ChannelContextFilter channelContextFilter, boolean isBlock) {
		try {
			SetWithLock<ChannelContext> setWithLock = tioConfig.connections;
			if (setWithLock == null || setWithLock.size() == 0) {
				log.debug("{}, 没有任何连接", tioConfig.getName());
				return true;
			}
			Boolean ret = sendToSet(tioConfig, setWithLock, packet, channelContextFilter, isBlock);
			return ret;
		} finally {
			CluClient.transfer(tioConfig, BindType.All, null, packet);
		}
	}

	/**
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @param bsIds
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean sendToBsId(TioConfig tioConfig, String bsId, Collection<String> bsIds, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(bsId)) {
			sendToBsId(tioConfig, bsId, packet, isBlock);
		}

		if (bsIds != null && !bsIds.isEmpty()) {
			for (String bid : bsIds) {
				sendToBsId(tioConfig, bid, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息给指定业务ID
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @param packet
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean sendToBsId(TioConfig tioConfig, String bsId, Packet packet) {
		return sendToBsId(tioConfig, bsId, packet, false);
	}

	/**
	 * 发消息给指定业务ID
	 * 
	 * @param tioConfig
	 * @param bsId
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	private static Boolean sendToBsId(TioConfig tioConfig, String bsId, Packet packet, boolean isBlock) {
		ChannelContext channelContext = Tio.getByBsId(tioConfig, bsId);
		if (channelContext == null) {
			CluClient.transfer(tioConfig, BindType.BsId, bsId, packet);
			return true;
		}
		if (isBlock) {
			return bSend(channelContext, packet);
		} else {
			return send(channelContext, packet);
		}
	}

	public static Boolean sendToBsId(TioConfig tioConfig, String bsId, String[] bsIds, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(bsId)) {
			sendToBsId(tioConfig, bsId, packet, isBlock);
		}

		if (ArrayUtil.isNotEmpty(bsIds)) {
			for (String bid : bsIds) {
				sendToBsId(tioConfig, bid, packet, isBlock);
			}
		}
		return true;
	}

	public static Boolean sendToGroup(TioConfig tioConfig, String group, Collection<String> groups, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(group)) {
			sendToGroup(tioConfig, group, packet, null, isBlock);
		}

		if (groups != null && !groups.isEmpty()) {
			for (String g : groups) {
				sendToGroup(tioConfig, g, packet, null, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息到组
	 * 
	 * @param tioConfig
	 * @param group
	 * @param packet
	 * @author tanyaowu
	 */
	public static void sendToGroup(TioConfig tioConfig, String group, Packet packet) {
		sendToGroup(tioConfig, group, packet, null);
	}

	/**
	 * 发消息到组
	 * 
	 * @param tioConfig
	 * @param group
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static void sendToGroup(TioConfig tioConfig, String group, Packet packet, ChannelContextFilter channelContextFilter) {
		sendToGroup(tioConfig, group, packet, channelContextFilter, false);
	}

	/**
	 * 发消息到组
	 * 
	 * @param tioConfig
	 * @param group
	 * @param packet
	 * @param channelContextFilter
	 * @param isBlock
	 * @return
	 */
	private static Boolean sendToGroup(TioConfig tioConfig, String group, Packet packet, ChannelContextFilter channelContextFilter, boolean isBlock) {
		try {
			SetWithLock<ChannelContext> setWithLock = tioConfig.groups.clients(tioConfig, group);
			if (setWithLock == null || setWithLock.size() == 0) {
				log.debug("{}, 组[{}]不存在", tioConfig.getName(), group);
				return true;
			}
			Boolean ret = sendToSet(tioConfig, setWithLock, packet, channelContextFilter, isBlock);
			return ret;
		} finally {
			CluClient.transfer(tioConfig, BindType.Group, group, packet);

		}
	}

	public static Boolean sendToGroup(TioConfig tioConfig, String group, String[] groups, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(group)) {
			sendToGroup(tioConfig, group, packet, null, isBlock);
		}

		if (ArrayUtil.isNotEmpty(groups)) {
			for (String g : groups) {
				sendToGroup(tioConfig, g, packet, null, isBlock);
			}
		}
		return true;
	}

	public static Boolean sendToId(TioConfig tioConfig, String id, Collection<String> ids, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(id)) {
			sendToId(tioConfig, id, packet, isBlock);
		}

		if (ids != null && !ids.isEmpty()) {
			for (String item : ids) {
				sendToId(tioConfig, item, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息给指定ChannelContext id
	 * 
	 * @param channelContextId
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean sendToId(TioConfig tioConfig, String channelContextId, Packet packet) {
		return sendToId(tioConfig, channelContextId, packet, false);
	}

	/**
	 * 发消息给指定ChannelContext id
	 * 
	 * @param channelContextId
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	private static Boolean sendToId(TioConfig tioConfig, String channelContextId, Packet packet, boolean isBlock) {
		ChannelContext channelContext = Tio.getByChannelContextId(tioConfig, channelContextId);
		if (channelContext == null) {
			CluClient.transfer(tioConfig, BindType.ChannelId, channelContextId, packet);
			return true;
		}
		if (isBlock) {
			return bSend(channelContext, packet);
		} else {
			return send(channelContext, packet);
		}
	}

	public static Boolean sendToId(TioConfig tioConfig, String id, String[] ids, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(id)) {
			sendToId(tioConfig, id, packet, isBlock);
		}

		if (ArrayUtil.isNotEmpty(ids)) {
			for (String item : ids) {
				sendToId(tioConfig, item, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param ips
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean sendToIp(TioConfig tioConfig, String ip, Collection<String> ips, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(ip)) {
			sendToIp(tioConfig, ip, packet, null, isBlock);
		}

		if (ips != null && !ips.isEmpty()) {
			for (String item : ips) {
				sendToIp(tioConfig, item, packet, null, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发送到指定ip对应的集合
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param packet
	 * @author: tanyaowu
	 */
	public static void sendToIp(TioConfig tioConfig, String ip, Packet packet) {
		sendToIp(tioConfig, ip, packet, null);
	}

	/**
	 * 发送到指定ip对应的集合
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param packet
	 * @param channelContextFilter
	 * @author: tanyaowu
	 */
	public static void sendToIp(TioConfig tioConfig, String ip, Packet packet, ChannelContextFilter channelContextFilter) {
		sendToIp(tioConfig, ip, packet, channelContextFilter, false);
	}

	/**
	 * 发送到指定ip对应的集合
	 * 
	 * @param tioConfig
	 * @param ip
	 * @param packet
	 * @param channelContextFilter
	 * @param isBlock
	 * @return
	 * @author: tanyaowu
	 */
	private static Boolean sendToIp(TioConfig tioConfig, String ip, Packet packet, ChannelContextFilter channelContextFilter, boolean isBlock) {
		try {
			SetWithLock<ChannelContext> setWithLock = tioConfig.ips.clients(tioConfig, ip);
			if (setWithLock == null || setWithLock.size() == 0) {
				log.info("{}, 没有ip为[{}]的对端", tioConfig.getName(), ip);
				return true;
			}
			Boolean ret = sendToSet(tioConfig, setWithLock, packet, channelContextFilter, isBlock);
			return ret;
		} finally {
			CluClient.transfer(tioConfig, BindType.Ip, ip, packet);
		}
	}

	public static Boolean sendToIp(TioConfig tioConfig, String ip, String[] ips, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(ip)) {
			sendToIp(tioConfig, ip, packet, null, isBlock);
		}

		if (ArrayUtil.isNotEmpty(ips)) {
			for (String item : ips) {
				sendToIp(tioConfig, item, packet, null, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息到指定集合
	 * 
	 * @param tioConfig
	 * @param setWithLock
	 * @param packet
	 * @param channelContextFilter
	 * @author tanyaowu
	 */
	public static void sendToSet(TioConfig tioConfig, SetWithLock<ChannelContext> setWithLock, Packet packet, ChannelContextFilter channelContextFilter) {
		sendToSet(tioConfig, setWithLock, packet, channelContextFilter, false);
	}

	/**
	 * 发消息到指定集合
	 * 
	 * @param tioConfig
	 * @param setWithLock
	 * @param packet
	 * @param channelContextFilter
	 * @param isBlock
	 * @author tanyaowu
	 */
	private static Boolean sendToSet(TioConfig tioConfig, SetWithLock<ChannelContext> setWithLock, Packet packet, ChannelContextFilter channelContextFilter, boolean isBlock) {
		boolean releasedLock = false;
		Lock lock = setWithLock.readLock();
		lock.lock();
		try {
			Set<ChannelContext> set = setWithLock.getObj();
			if (set.size() == 0) {
				log.debug("{}, 集合为空", tioConfig.getName());
				return true;
			}

			CountDownLatch countDownLatch = null;
			if (isBlock) {
				countDownLatch = new CountDownLatch(set.size());
			}
			int sendCount = 0;
			for (ChannelContext channelContext : set) {
				if (channelContextFilter != null) {
					boolean isfilter = channelContextFilter.filter(channelContext);
					if (!isfilter) {
						if (isBlock) {
							countDownLatch.countDown();
						}
						continue;
					}
				}

				sendCount++;
				if (isBlock) {
					send(channelContext, packet, countDownLatch, PacketSendMode.GROUP_BLOCK);
				} else {
					send(channelContext, packet, null, null);
				}
			}
			lock.unlock();
			releasedLock = true;

			if (sendCount == 0) {
				return false;
			}

			if (isBlock) {
				try {
					long timeout = sendCount / 5;
					timeout = Math.max(timeout, 10);// timeout < 10 ? 10 : timeout;
					boolean awaitFlag = countDownLatch.await(timeout, TimeUnit.SECONDS);
					if (!awaitFlag) {
						log.error("{}, 同步群发超时, size:{}, timeout:{}, packet:{}", tioConfig.getName(), setWithLock.getObj().size(), timeout, packet.logstr());
						return false;
					} else {
						return true;
					}
				} catch (InterruptedException e) {
					log.error("", e);
					return false;
				} finally {

				}
			} else {
				return true;
			}
		} catch (Throwable e) {
			log.error("", e);
			return false;
		} finally {
			if (!releasedLock) {
				lock.unlock();
			}
		}
	}

	public static Boolean sendToToken(TioConfig tioConfig, String token, Collection<String> tokens, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(token)) {
			sendToToken(tioConfig, token, packet, isBlock);
		}

		if (tokens != null && !tokens.isEmpty()) {
			for (String t : tokens) {
				sendToToken(tioConfig, t, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息到指定token
	 * 
	 * @param tioConfig
	 * @param token
	 * @param packet
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean sendToToken(TioConfig tioConfig, String token, Packet packet) {
		return sendToToken(tioConfig, token, packet, false);
	}

	/**
	 * 发消息给指定token
	 * 
	 * @param tioConfig
	 * @param token
	 * @param packet
	 * @param isBlock
	 * @author tanyaowu
	 */
	private static Boolean sendToToken(TioConfig tioConfig, String token, Packet packet, boolean isBlock) {
		SetWithLock<ChannelContext> setWithLock = tioConfig.tokens.find(tioConfig, token);
		try {
			if (setWithLock == null || setWithLock.size() == 0) {
				return true;
			}

			ReadLock readLock = setWithLock.readLock();
			readLock.lock();
			try {
				Set<ChannelContext> set = setWithLock.getObj();
				boolean ret = false;
				for (ChannelContext channelContext : set) {
					boolean singleRet = false;
					// 不要用 a = a || b()，容易漏执行后面的函数
					if (isBlock) {
						singleRet = bSend(channelContext, packet);
					} else {
						singleRet = send(channelContext, packet);
					}
					if (singleRet) {
						ret = true;
					}
				}
				return ret;
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				readLock.unlock();
			}
			return false;
		} finally {
			CluClient.transfer(tioConfig, BindType.Token, token, packet);
		}
	}

	public static Boolean sendToToken(TioConfig tioConfig, String token, String[] tokens, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(token)) {
			sendToToken(tioConfig, token, packet, isBlock);
		}

		if (ArrayUtil.isNotEmpty(tokens)) {
			for (String t : tokens) {
				sendToToken(tioConfig, t, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param userids
	 * @param packet
	 * @param isBlock
	 * @return
	 * @author tanyaowu
	 */
	public static Boolean sendToUser(TioConfig tioConfig, String userid, Collection<String> userids, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(userid)) {
			sendToUser(tioConfig, userid, packet, isBlock);
		}

		if (userids != null && !userids.isEmpty()) {
			for (String uid : userids) {
				sendToUser(tioConfig, uid, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 发消息给指定用户
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param packet
	 * @author tanyaowu
	 */
	public static Boolean sendToUser(TioConfig tioConfig, String userid, Packet packet) {
		return sendToUser(tioConfig, userid, packet, false);
	}

	/**
	 * 发消息给指定用户
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param packet
	 * @param isBlock
	 * @author tanyaowu
	 */
	private static Boolean sendToUser(TioConfig tioConfig, String userid, Packet packet, boolean isBlock) {
		SetWithLock<ChannelContext> setWithLock = tioConfig.users.find(tioConfig, userid);
		try {
			if (setWithLock == null || setWithLock.size() == 0) {
				CluClient.transfer(tioConfig, BindType.User, userid, packet);
				return true;
			}

			ReadLock readLock = setWithLock.readLock();
			readLock.lock();
			try {
				Set<ChannelContext> set = setWithLock.getObj();
				boolean ret = false;
				for (ChannelContext channelContext : set) {
					boolean singleRet = false;
					// 不要用 a = a || b()，容易漏执行后面的函数
					if (isBlock) {
						singleRet = bSend(channelContext, packet);
					} else {
						singleRet = send(channelContext, packet);
					}
					if (singleRet) {
						ret = true;
					}
				}
				return ret;
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				readLock.unlock();
			}
			return false;
		} finally {
			// CluClient.transfer(tioConfig, BindType.User, userid, packet);
		}
	}

	public static Boolean sendToUser(TioConfig tioConfig, String userid, String[] userids, Packet packet, boolean isBlock) {
		if (StrUtil.isNotBlank(userid)) {
			sendToUser(tioConfig, userid, packet, isBlock);
		}

		if (ArrayUtil.isNotEmpty(userids)) {
			for (String uid : userids) {
				sendToUser(tioConfig, uid, packet, isBlock);
			}
		}
		return true;
	}

	/**
	 * 响应超时的时候，返回值是null
	 * 
	 * @param <T>
	 * @param channelContext
	 * @param packet
	 * @param timeout 单位：毫秒
	 * @return
	 * @author tanyaowu
	 */
	public static <T extends Packet> T synSend(ChannelContext channelContext, T packet, long timeout) {
		WaitSynSendHandler<T> waitSynSendHandler = new WaitSynSendHandler<>();
		synchronized (waitSynSendHandler) {
			synSend(channelContext, packet, timeout, waitSynSendHandler);
			try {
				waitSynSendHandler.wait(timeout);
				return waitSynSendHandler.getRespPacket();
			} catch (InterruptedException e) {
				log.error(e.toString(), e);
			} finally {

			}
			return null;
		}
	}

	/**
	 * 发送并等待响应.<br>
	 * 注意：<br>
	 * 1、对端收到此消息后，需要回一条synSeq一样的消息，所以业务需要在decode()方法中根据bytebuffer反解析出packet的synSeq值，并赋给XxPacket对象<br>
	 * 2、对于同步发送，框架层面并不会帮应用去调用handler.handler(packet,
	 * channelContext)方法，应用需要自己去处理响应的消息包。
	 * 业务侧可以手工调一下：tioConfig.getTioHandler().handler(packet, channelContext);<br>
	 *
	 * @param channelContext
	 * @param packet         业务层必须设置好synSeq字段的值，而且要保证唯一（不能重复）。可以在tioConfig范围内用AtomicInteger
	 * @param timeout        单位：毫秒
	 * @return
	 * @author tanyaowu
	 */
	public static <T extends Packet> void synSend(ChannelContext channelContext, T packet, long timeout, SynSendHandler<T> synSendHandler) {
		Threads.getGroupExecutor().execute(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Integer synSeqNo = channelContext.tioConfig.SEQNO_GEN.incrementAndGet();
				// if (synSeq == null || synSeq <= 0) {
				// throw new RuntimeException("synSeq必须大于0");
				// }
				packet.setSynReqNo(synSeqNo);

				channelContext.tioConfig.synNoMap.put(synSeqNo, packet);
				log.info("put synReqNo[{}]", synSeqNo);
				synchronized (packet) {
					send(channelContext, packet);
					try {
						if (log.isInfoEnabled()) {
							long start = SystemTimer.currTime;
							packet.wait(timeout);
							long end = SystemTimer.currTime;
							log.info("同步耗时:{}ms,\r\n", end - start, Json.toFormatedJson(packet));
						} else {
							packet.wait(timeout);
						}
					} catch (InterruptedException e) {
						log.error("", e);
					} finally {
						T fgylapegleyrbxxaaksdwmtworshc = (T) channelContext.tioConfig.synNoMap.remove(synSeqNo);
						log.info("remove synReqNo[{}]", synSeqNo);
						if (fgylapegleyrbxxaaksdwmtworshc == null) {
							log.error("respPacket == null,{}", channelContext);
							synSendHandler.onTimeout(channelContext, packet, timeout);
							// return null;
						} else {
							if (fgylapegleyrbxxaaksdwmtworshc == packet) {
								synSendHandler.onTimeout(channelContext, packet, timeout);
								log.info("{}, 同步发送超时, {}", channelContext.tioConfig.getName(), channelContext);
								// return null;
							} else {
								synSendHandler.onResp(channelContext, packet, fgylapegleyrbxxaaksdwmtworshc, timeout);
							}
						}
					}
				}

			}
		});

	}

	/**
	 * 解绑业务id
	 * 
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void unbindBsId(ChannelContext channelContext) {
		channelContext.tioConfig.bsIds.unbind(channelContext);
	}

	/**
	 * 与所有组解除解绑关系
	 * 
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void unbindGroup(ChannelContext channelContext) {
		channelContext.tioConfig.groups.unbind(channelContext);
	}

	/**
	 * 与指定组解除绑定关系
	 * 
	 * @param group
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void unbindGroup(String group, ChannelContext channelContext) {
		channelContext.tioConfig.groups.unbind(group, channelContext);
	}

	/**
	 * 将某用户从组中解除绑定
	 * 
	 * @param tioConfig
	 * @param userid
	 * @param group
	 */
	public static void unbindGroup(TioConfig tioConfig, String userid, String group) {
		SetWithLock<ChannelContext> setWithLock = Tio.getByUserid(tioConfig, userid);
		if (setWithLock != null && setWithLock.size() > 0) {
			setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
				@Override
				public void handler(Set<ChannelContext> set) {
					for (ChannelContext channelContext : set) {
						Tio.unbindGroup(group, channelContext);
					}
				}
			});
		}
	}

	/**
	 * 解除channelContext绑定的token
	 * 
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void unbindToken(ChannelContext channelContext) {
		channelContext.tioConfig.tokens.unbind(channelContext);
	}

	/**
	 * 解除token
	 * 
	 * @param tioConfig
	 * @param token
	 */
	public static void unbindToken(TioConfig tioConfig, String token) {
		tioConfig.tokens.unbind(tioConfig, token);
	}

	// org.tio.core.TioConfig.ipBlacklist

	/**
	 * 解除channelContext绑定的userid
	 * 
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void unbindUser(ChannelContext channelContext) {
		channelContext.tioConfig.users.unbind(channelContext);
	}

	/**
	 * 解除userid的绑定。一般用于多地登录，踢掉前面登录的场景
	 * 
	 * @param tioConfig
	 * @param userid
	 * @author: tanyaowu
	 */
	public static void unbindUser(TioConfig tioConfig, String userid) {
		tioConfig.users.unbind(tioConfig, userid);
	}

	private Tio() {
	}

	public static void main(String[] args) {
		System.out.println(Tio.online());
	}

}
