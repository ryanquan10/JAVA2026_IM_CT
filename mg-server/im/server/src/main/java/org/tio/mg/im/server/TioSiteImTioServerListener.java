
package org.tio.mg.im.server;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.lionsoul.ip2region.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.im.common.CommandStat;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.service.ip2region.Ip2Region;
import org.tio.mg.service.model.main.ChatroomJoinLeave;
import org.tio.monitor.RateLimiterWrap;
import org.tio.server.intf.TioServerListener;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.jfinal.P;

/**
 *
 * @author tanyaowu
 *
 */
public class TioSiteImTioServerListener implements TioServerListener {
	@SuppressWarnings("unused")
	private static Logger							log			= LoggerFactory.getLogger(TioSiteImTioServerListener.class);
	static AtomicLong								accessCount	= new AtomicLong();
	public static final TioSiteImTioServerListener	me			= new TioSiteImTioServerListener();

	/**
	 * @author tanyaowu
	 * 2016年12月16日 下午5:52:06
	 *
	 */
	private TioSiteImTioServerListener() {
	}

	/**
	 * @see org.tio.core.intf.TioListener#onAfterClose(org.tio.core.ChannelContext, java.lang.Throwable, java.lang.String)
	 *
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @author tanyaowu
	 * 2016年2月1日 上午11:03:11
	 *
	 */
	//	@Override
	//	public void onAfterClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
	//	}

	/**
	 * @see org.tio.server.intf.TioServerListener#onAfterAccepted(java.nio.channels.AsynchronousSocketChannel, org.tio.server.TioServer)
	 *
	 * @param asynchronousSocketChannel
	 * @param tioServer
	 * @return
	 * @author tanyaowu
	 * 2016年12月20日 上午11:03:45
	 *
	 */
	//	@Override
	//	public boolean onAfterAccepted(AsynchronousSocketChannel asynchronousSocketChannel, TioServer tioServer)
	//	{
	//		return true;
	//	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
		ImSessionContext imSessionContext = new ImSessionContext();
		channelContext.setAttribute(Const.IM_SESSION_KEY, imSessionContext);

		//		TioConfig tioConfig = channelContext.tioConfig;

		int permitsPerSecond = P.getInt("request.permitsPerSecond");
		int warnClearInterval = 1000 * P.getInt("request.warnClearInterval");
		int maxWarnCount = P.getInt("request.maxWarnCount");
		int maxAllWarnCount = P.getInt("request.maxAllWarnCount");
		RateLimiterWrap rateLimiterWrap = new RateLimiterWrap(permitsPerSecond, warnClearInterval, maxWarnCount, maxAllWarnCount);

		imSessionContext.setRequestRateLimiter(rateLimiterWrap);

		if (isConnected) {
			String ip = channelContext.getClientNode().getIp();

			DataBlock dataBlock = Ip2Region.getDataBlock(ip);
			imSessionContext.setDataBlock(dataBlock);

			//			AtomicLong ipcount = ipmap.get(ip);
			//			if (ipcount == null) {
			//				ipcount = new AtomicLong();
			//				ipmap.put(ip, ipcount);
			//			}
			//			ipcount.incrementAndGet();

			//			String region = StrUtil.fillBefore(dataBlock.getRegion(), ' ', 12);//(dataBlock.getRegion(), 12);
			//			String accessCountStr = StrUtil.fillBefore(accessCount.incrementAndGet() + "", ' ', 9);
			//			//地区，所有的访问次数，有多少个不同的ip， ip， 这个ip连接的次数
			//			iplog.info("{} {} {}", region, accessCountStr, ip);
		}

		return;
	}

	/**
	 * 
	 * @param channelContext
	 * @param packet
	 * @param packetSize
	 * @author: tanyaowu
	 */
	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) {
		if (packet instanceof ImPacket) {
			ImPacket imPacket = (ImPacket) packet;
			CommandStat.getCommandStat(imPacket.getCommand()).received.incrementAndGet();
		}
	}

	/**
	 * 
	 * @param channelContext
	 * @param packet
	 * @param isSentSuccess
	 * @author: tanyaowu
	 */
	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) {
		if (packet instanceof ImPacket) {
			ImPacket imPacket = (ImPacket) packet;
			if (isSentSuccess) {
				CommandStat.getCommandStat(imPacket.getCommand()).sent.incrementAndGet();
			}
		}
	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
		//		long groupChatCount = CommandStat.getCount(Command.GroupChatReq).received.get(); //这个人发了多少条群聊消息
		//		long p2pChatCount = CommandStat.getCount(Command.P2pChatReq).received.get(); //这个人发了多少条私聊消息
		//		log.info("{}, 此人发的群聊数:{}, 此人发的私聊数:{}", channelContext, groupChatCount, p2pChatCount);
		//
		//		long groupChatCount2 = CommandStat.getCount(Command.GroupChatNtf).sent.get(); //服务器发了多少条群聊给这个人
		//		long p2pChatCount2 = CommandStat.getCount(Command.P2pChatNtf).sent.get(); //服务器发了多少条私聊给这个人
		//		log.info("{}, 发往此人的群聊数:{}, 发往此人的私聊数:{}", channelContext, groupChatCount2, p2pChatCount2);
		//
		//		@SuppressWarnings("unused")
		//		long iv = SystemTimer.currTime - channelContext.stat.timeCreated; //这个通道的时长，单位：毫秒
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		
		if (imSessionContext != null) {
			ChatroomJoinLeave chatroomJoinLeave = imSessionContext.getChatroomJoinLeave();
			if (chatroomJoinLeave != null) {
				if (chatroomJoinLeave.isChat()) {
					long cost = System.currentTimeMillis() - chatroomJoinLeave.getJointime().getTime();
					String sql = "update chatroom_join_leave set leavetime = ?, cost = ?, status = 1 where id = ?";
					Db.use(Const.Db.TIO_SITE_MAIN).update(sql, new Date(), cost, chatroomJoinLeave.getId());
				}
			}
		}
	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

	}

	@Override
	public boolean onHeartbeatTimeout(ChannelContext channelContext, Long interval, int heartbeatTimeoutCount) {
		return false;
	}

}
