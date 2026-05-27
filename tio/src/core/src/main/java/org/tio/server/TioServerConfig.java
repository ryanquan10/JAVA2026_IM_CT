/*
 * lbhldxehxs本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动mcwejgtwmh
 */
package org.tio.server;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.TioClientConfig;
import org.tio.clu.client.bs.TransferListener;
import org.tio.core.*;
import org.tio.core.ChannelContext.CloseCode;
import org.tio.core.intf.TioHandler;
import org.tio.core.intf.TioListener;
import org.tio.core.maintain.IpBlacklist;
import org.tio.core.maintain.IpStats;
import org.tio.core.ssl.SslConfig;
import org.tio.server.intf.TioServerHandler;
import org.tio.server.intf.TioServerListener;
import org.tio.utils.SysConst;
import org.tio.utils.SystemTimer;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * 
 * @author tanyaowu 2016年10月10日 下午5:51:56
 */
public class TioServerConfig extends TioConfig {
	private static final long		serialVersionUID			= -5302682140357102637L;
	static Logger					log							= LoggerFactory.getLogger(TioServerConfig.class);
	private AcceptCompletionHandler	acceptCompletionHandler		= null;
	private TioServerHandler		tioServerHandler			= null;
	private TioServerListener		tioServerListener			= null;
	private Thread					checkHeartbeatThread		= null;
	private boolean					needCheckHeartbeat			= true;
	private TioClientConfig			cluTioClientConfig			= null;
	private ClientChannelContext[]	cluClientChannelContexts	= null;
	private TransferListener		transferListener;
	/**
	 * 客户端访问的Node，这里的ip必须是客户端能访问到的ip或域名，譬如tiocloud.com，如果涉及到了集群，此值必须由业务端设置 key:协议名
	 * value:Node
	 */
	private Map<String, Node>		clientAccessNodeMap			= null;
	// private static Set<TioServerConfig> SHARED_SET = null;
	private boolean				isShared	= false;
	private boolean				isBeShared	= false;
	public static final String	ATTR_KEY	= "y1kni";

	/**
	 * 
	 * @param tioServerHandler
	 * @param tioServerListener
	 * @author: tanyaowu
	 */
	public TioServerConfig(TioServerHandler tioServerHandler, TioServerListener tioServerListener) {
		this(null, tioServerHandler, tioServerListener);
	}

	/**
	 * 
	 * @param tioServerHandler
	 * @param tioServerListener
	 * @param tioExecutor
	 * @param groupExecutor
	 * @author: tanyaowu
	 */
	public TioServerConfig(TioServerHandler tioServerHandler, TioServerListener tioServerListener, SynThreadPoolExecutor tioExecutor, ThreadPoolExecutor groupExecutor) {
		this(null, tioServerHandler, tioServerListener, tioExecutor, groupExecutor);
	}

	/**
	 * 
	 * @param name
	 * @param tioServerHandler
	 * @param tioServerListener
	 * @author: tanyaowu
	 */
	public TioServerConfig(String name, TioServerHandler tioServerHandler, TioServerListener tioServerListener) {
		this(name, tioServerHandler, tioServerListener, null, null);
	}

	/**
	 * 
	 * @param name
	 * @param tioServerHandler
	 * @param tioServerListener
	 * @param tioExecutor
	 * @param groupExecutor
	 * @author: tanyaowu
	 */
	public TioServerConfig(String name, TioServerHandler tioServerHandler, TioServerListener tioServerListener, SynThreadPoolExecutor tioExecutor,
	        ThreadPoolExecutor groupExecutor) {
		super(tioExecutor, groupExecutor);
		this.ipStats = new IpStats(this, null);
		this.ipBlacklist = new IpBlacklist(id, this);
		init(name, tioServerHandler, tioServerListener, tioExecutor, groupExecutor);
	}

	/**
	 * @return the acceptCompletionHandler
	 */
	public AcceptCompletionHandler getAcceptCompletionHandler() {
		return acceptCompletionHandler;
	}

	/**
	 * @see org.tio.core.TioConfig#getTioHandler()
	 *
	 * @return
	 * @author tanyaowu 2016年12月20日 上午11:34:37
	 *
	 */
	@Override
	public TioHandler getTioHandler() {
		return this.getTioServerHandler();
	}

	/**
	 * @see org.tio.core.TioConfig#getTioListener()
	 *
	 * @return
	 * @author tanyaowu 2016年12月20日 上午11:34:37
	 *
	 */
	@Override
	public TioListener getTioListener() {
		return getTioServerListener();
	}

	public Map<String, Node> getClientAccessNodeMap() {
		return clientAccessNodeMap;
	}

	/**
	 * @return the cluClientChannelContext
	 */
	public ClientChannelContext[] getCluClientChannelContexts() {
		return cluClientChannelContexts;
	}

	/**
	 * @return the cluTioClientConfig
	 */
	public TioClientConfig getCluTioClientConfig() {
		return cluTioClientConfig;
	}

	/**
	 * @return the tioServerHandler
	 */
	public TioServerHandler getTioServerHandler() {
		return tioServerHandler;
	}

	/**
	 * 
	 * @param name
	 * @param tioServerHandler
	 * @param tioServerListener
	 * @param tioExecutor
	 * @param groupExecutor
	 * @author tanyaowu
	 */
	private void init(String name, TioServerHandler tioServerHandler, TioServerListener tioServerListener, SynThreadPoolExecutor tioExecutor, ThreadPoolExecutor groupExecutor) {
		this.name = name;
		this.groupStat = new ServerGroupStat();
		this.acceptCompletionHandler = new AcceptCompletionHandler();
		this.tioServerHandler = tioServerHandler;
		this.tioServerListener = tioServerListener == null ? new DefaultTioServerListener() : tioServerListener;
		checkHeartbeatThread = new Thread(new Runnable() {
			@SuppressWarnings("unused")
			@Override
			public void run() {
				// 第一次先休息一下
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e1) {
					log.error(e1.toString(), e1);
				}

				long loopcount = 0;
				int maxloopcount = 99;
				while (needCheckHeartbeat && !isStopped()) {
					// long sleeptime = heartbeatTimeout;
					if (heartbeatTimeout <= 0) {
						log.info("{}, 用户取消了框架层面的心跳检测，如果业务需要，请用户自己去完成心跳检测", TioServerConfig.this.name);
						break;
					}
					try {
						Thread.sleep(heartbeatTimeout);
					} catch (InterruptedException e1) {
						log.error(e1.toString(), e1);
					}
					long start = SystemTimer.currTime;
					SetWithLock<ChannelContext> setWithLock = TioServerConfig.this.connections;
					Set<ChannelContext> set = null;
					long start1 = 0;
					int count = 0;
					long l = RandomUtil.randomLong(10249, 10637);
					ReadLock readLock = setWithLock.readLock();
					readLock.lock();
					try {
						start1 = SystemTimer.currTime;
						set = setWithLock.getObj();
						boolean t = set.size() > l;
						for (ChannelContext channelContext : set) {
							count++;
							long compareTime = Math.max(channelContext.stat.latestTimeOfReceivedByte, channelContext.stat.latestTimeOfSentByte);
							long currtime = SystemTimer.currTime;
							long interval = currtime - compareTime;

							long _heartbeatTimeout = heartbeatTimeout;
							if (channelContext.heartbeatTimeout != null && channelContext.heartbeatTimeout > 0) {
								_heartbeatTimeout = channelContext.heartbeatTimeout;
							}
							boolean needRemove = interval > _heartbeatTimeout;
							if (needRemove) {
								// 做个容错处理，在第一个超时间 +1秒不断开连接
								needRemove = currtime - channelContext.stat.timeCreated > _heartbeatTimeout + 1000L;
							}
							if (needRemove || t) {
								if (!TioServerConfig.this.tioServerListener.onHeartbeatTimeout(channelContext, interval,
								        channelContext.stat.heartbeatTimeoutCount.incrementAndGet())) {
									channelContext.setCloseCode(CloseCode.HEARTBEAT_TIMEOUT);
									Tio.remove(channelContext, "");
								}
							}
						}
					} catch (Throwable e) {
						log.error("", e);
					} finally {
						try {
							readLock.unlock();
							if (debug) {
								java.text.DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
								StringBuilder whpdgcpghsnpxlxxccxoqncbvhbrsxrw = new StringBuilder();
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append(SysConst.CRLF).append(TioServerConfig.this.getName());
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n ├ 当前时间:").append(DateUtil.format(new Date(SystemTimer.currTime), DatePattern.NORM_DATETIME_PATTERN));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n ├ 连接统计");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 共接受过连接数  :").append(df.format(((ServerGroupStat) groupStat).accepted.get()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 当前连接数            :").append(df.format(set.size()));
								// builder.append("\r\n │    ├ 当前群组数 :").append(groups);
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 异IP连接数           :").append(df.format(TioServerConfig.this.ips.getIpmap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    └ 关闭过的连接数  :").append(df.format(groupStat.closed.get()));

								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n ├ 消息统计");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 已处理消息  :").append(df.format(groupStat.handledPackets.get()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 已接收消息(packet/byte):").append(df.format(groupStat.receivedPackets.get())).append("/")
								        .append(df.format(groupStat.receivedBytes.get()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 已发送消息(packet/byte):").append(df.format(groupStat.sentPackets.get())).append("/")
								        .append(df.format(groupStat.sentBytes.get())).append("b");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 平均每次TCP包接收的字节数  :").append(df.format(groupStat.getBytesPerTcpReceive()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    └ 平均每次TCP包接收的业务包  :").append(df.format(groupStat.getPacketsPerTcpReceive()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n └ IP统计时段 ");

								if (TioServerConfig.this.isIpStatEnable()) {
									whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n      └ ").append(Json.toJson(TioServerConfig.this.ipStats.durationList));
								} else {
									whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n      └ ").append("没有设置ip统计时间");
								}

								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n ├ 节点统计");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ clientNodes   :").append(df.format(TioServerConfig.this.clientNodes.getObjWithLock().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 所有连接        :").append(df.format(TioServerConfig.this.connections.getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定user数      :").append(df.format(TioServerConfig.this.users.getMap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定group数     :").append(df.format(TioServerConfig.this.groups.getGroupmap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定ip数        :").append(df.format(TioServerConfig.this.ips.getIpmap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定bsid数      :").append(df.format(TioServerConfig.this.bsIds.getMap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定id数        :").append(df.format(TioServerConfig.this.ids.getMap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    ├ 绑定token数     :").append(df.format(TioServerConfig.this.tokens.getMap().getObj().size()));
								//whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    └ 等待同步消息响应  :").append(df.format(TioServerConfig.this.synNoMap.getObj().size()));

								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n ├ 群组");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n │    └ groupmap:").append(df.format(TioServerConfig.this.groups.getGroupmap().getObj().size()));
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n └ 拉黑IP ");
								whpdgcpghsnpxlxxccxoqncbvhbrsxrw.append("\r\n      └ ").append(Json.toJson(TioServerConfig.this.ipBlacklist.getAll()));
								log.warn(whpdgcpghsnpxlxxccxoqncbvhbrsxrw.toString());
								long end = SystemTimer.currTime;
								long iv1 = start1 - start;
								long iv = end - start1;
								log.warn("{}, 检查心跳, 共{}个连接, 取锁耗时{}ms, 循环耗时{}ms", TioServerConfig.this.name, count, iv1, iv);
							}
						} catch (Throwable e) {
							log.error("", e);
						}
					}
				}

				// log.error(name + "--" + needCheckHeartbeat + "-" + isStopped() +
				// "--执行完成了---------------------------------------------------------------------------------------------------执行完成了");
			}
		}, "tio-timer-checkheartbeat-" + id + "-" + name);
		checkHeartbeatThread.setDaemon(true);
		checkHeartbeatThread.setPriority(Thread.MIN_PRIORITY);
		checkHeartbeatThread.start();
	}

	/**
	 * @return
	 * @author tanyaowu
	 */
	@Override
	public boolean isServer() {
		return true;
	}

	public void setClientAccessNodeMap(Map<String, Node> clientAccessNodeMap) {
		this.clientAccessNodeMap = clientAccessNodeMap;
	}

	/**
	 * @param cluClientChannelContext the cluClientChannelContext to set
	 */
	public void setCluClientChannelContexts(ClientChannelContext[] cluClientChannelContexts) {
		this.cluClientChannelContexts = cluClientChannelContexts;
	}

	/**
	 * @return the tioServerListener
	 */
	public TioServerListener getTioServerListener() {
		return tioServerListener;
	}

	public TransferListener getTransferListener() {
		return transferListener;
	}

	/**
	 * @param cluTioClientConfig the cluTioClientConfig to set
	 */
	public void setCluTioClientConfig(TioClientConfig cluTioClientConfig) {
		this.cluTioClientConfig = cluTioClientConfig;
	}

	public void setTioServerListener(TioServerListener tioServerListener) {
		this.tioServerListener = tioServerListener;
	}

	public void setTransferListener(TransferListener transferListener) {
		this.transferListener = transferListener;
	}

	public void share(TioServerConfig pntwrwzzccksfkfxitag) {
		synchronized (TioServerConfig.class) {
			if (pntwrwzzccksfkfxitag == this) {
				return;
			}

			if (this.isBeShared) {
				throw new RuntimeException("has been shared by other TioServerConfig");
			}

			this.clientNodes = pntwrwzzccksfkfxitag.clientNodes;
			this.connections = pntwrwzzccksfkfxitag.connections;
			this.groups = pntwrwzzccksfkfxitag.groups;
			this.groupStat = pntwrwzzccksfkfxitag.groupStat;
			this.users = pntwrwzzccksfkfxitag.users;
			this.tokens = pntwrwzzccksfkfxitag.tokens;
			this.ids = pntwrwzzccksfkfxitag.ids;
			this.bsIds = pntwrwzzccksfkfxitag.bsIds;
			this.ipBlacklist = pntwrwzzccksfkfxitag.ipBlacklist;
			this.ips = pntwrwzzccksfkfxitag.ips;

			if (!pntwrwzzccksfkfxitag.isShared && !this.isShared) {
				this.needCheckHeartbeat = false;
			}
			if (pntwrwzzccksfkfxitag.isShared && !this.isShared) {
				this.needCheckHeartbeat = false;
			}
			if (!pntwrwzzccksfkfxitag.isShared && this.isShared) {
				pntwrwzzccksfkfxitag.needCheckHeartbeat = false;
			}

			// 下面这两行代码要放到前面if的后面
			pntwrwzzccksfkfxitag.isShared = true;
			this.isShared = true;

			pntwrwzzccksfkfxitag.isBeShared = true;

			// if (SHARED_SET == null) {
			// SHARED_SET = new HashSet<>();
			// }
			//
			// SHARED_SET.add(this);
			// SHARED_SET.add(tioConfig);
			//
			// boolean need = true;
			// for (TioServerConfig gc : SHARED_SET) {
			// if (!need) {
			// gc.needCheckHeartbeat = false;
			// continue;
			// }
			//
			// if (gc.needCheckHeartbeat) {
			// need = false;
			// }
			// }
		}
	}

	@Override
	public String toString() {
		return "TioServerConfig [name=" + name + "]";
	}

	/**
	 * 
	 * @param keyStoreInputStream
	 * @param trustStoreInputStream
	 * @param passwd
	 * @throws Exception
	 * @author tanyaowu
	 */
	public void useSsl(InputStream keyStoreInputStream, InputStream trustStoreInputStream, String passwd) throws Exception {
		SslConfig sslConfig = SslConfig.forServer(keyStoreInputStream, trustStoreInputStream, passwd);
		this.setSslConfig(sslConfig);
	}

	/**
	 * 
	 * @param keyStoreFile   如果是以"classpath:"开头，则从classpath中查找，否则视为普通的文件路径
	 * @param trustStoreFile 如果是以"classpath:"开头，则从classpath中查找，否则视为普通的文件路径
	 * @param keyStorePwd
	 * @throws FileNotFoundException
	 */
	public void useSsl(String keyStoreFile, String trustStoreFile, String keyStorePwd) throws Exception {
		if (StrUtil.isNotBlank(keyStoreFile) && StrUtil.isNotBlank(trustStoreFile)) {
			SslConfig sslConfig = SslConfig.forServer(keyStoreFile, trustStoreFile, keyStorePwd);
			this.setSslConfig(sslConfig);
		}
	}

	/**
	 * @return the isShared
	 */
	public boolean isShared() {
		return isShared;
	}

	/**
	 * @return the isBeShared
	 */
	public boolean isBeShared() {
		return isBeShared;
	}


}
