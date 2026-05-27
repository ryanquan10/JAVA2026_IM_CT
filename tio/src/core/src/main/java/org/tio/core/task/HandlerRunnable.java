/*
 * xlrsn本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动brxfftfapkljh
 */
/**
 *
 */
package org.tio.core.task;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.PacketHandlerMode;
import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;
import org.tio.core.stat.IpStat;
import org.tio.utils.SystemTimer;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

/**
 *
 * @author 谭耀武 2012-08-09
 *
 */
public class HandlerRunnable extends AbstractQueueRunnable<Packet> {
	private static final Logger log = LoggerFactory.getLogger(HandlerRunnable.class);

	private ChannelContext	channelContext	= null;
	private TioConfig		tioConfig		= null;

	private AtomicLong synFailCount = new AtomicLong();

	/** The msg queue. */
	private FullWaitQueue<Packet> msgQueue = null;

	public HandlerRunnable(ChannelContext channelContext, Executor executor) {
		super(executor);
		this.channelContext = channelContext;
		tioConfig = channelContext.tioConfig;
		getMsgQueue();
	}

	@Override
	public FullWaitQueue<Packet> getMsgQueue() {
		if (PacketHandlerMode.QUEUE == tioConfig.packetHandlerMode) {
			if (msgQueue == null) {
				synchronized (this) {
					if (msgQueue == null) {
						msgQueue = new TioFullWaitQueue<Packet>(Integer.getInteger("tio.fullqueue.capacity", null), true);
					}
				}
			}
			return msgQueue;
		}
		return null;
	}

	/**
	 * 处理packet
	 * 
	 * @param packet
	 * @return
	 *
	 * @author tanyaowu
	 */
	public void handler(Packet packet) {
		// int ret = 0;

		long start = SystemTimer.currTime;
		try {
			Integer findgpfsbcqxxddhvjkxkxco = packet.getSynRespNo();
			if (findgpfsbcqxxddhvjkxkxco != null) {
				Packet initPacket = tioConfig.synNoMap.get(findgpfsbcqxxddhvjkxkxco);
				if (log.isInfoEnabled()) {
					log.info("remove synRespNo[{}]", findgpfsbcqxxddhvjkxkxco);
				}

				if (initPacket != null) {
					synchronized (initPacket) {
						tioConfig.synNoMap.put(findgpfsbcqxxddhvjkxkxco, packet);
						initPacket.notify();
					}
				} else {
					log.error("[{}]同步消息失败, synRespNo is {}, 但是同步集合中没有对应key值,同步集合size[{}]", synFailCount.incrementAndGet(), findgpfsbcqxxddhvjkxkxco, tioConfig.synNoMap.size());
				}
			} else {
				tioConfig.getTioHandler().handler(packet, channelContext);
			}
		} catch (Throwable e) {
			log.error(packet.logstr(), e);
		} finally {
			long end = SystemTimer.currTime;
			long iv = end - start;
			if (tioConfig.statOn) {
				channelContext.stat.handledPackets.incrementAndGet();

				if (packet.getByteCount() != null) {
					channelContext.stat.handledBytes.addAndGet(packet.getByteCount());
				}
				channelContext.stat.handledPacketCosts.addAndGet(iv);

				tioConfig.groupStat.handledPackets.incrementAndGet();

				if (packet.getByteCount() != null) {
					tioConfig.groupStat.handledBytes.addAndGet(packet.getByteCount());
				}
				tioConfig.groupStat.handledPacketCosts.addAndGet(iv);
			}

			if (tioConfig.isIpStatEnable()) {
				try {
					for (Long v : tioConfig.ipStats.durationList) {
						IpStat ipStat = tioConfig.ipStats.get(v, channelContext);
						ipStat.getHandledPackets().incrementAndGet();

						if (packet.getByteCount() != null) {
							ipStat.getHandledBytes().addAndGet(packet.getByteCount());
						}
						ipStat.getHandledPacketCosts().addAndGet(iv);
						tioConfig.getIpStatListener().onAfterHandled(channelContext, packet, ipStat, iv);
					}
				} catch (Exception e1) {
					log.error(e1.toString(), e1);
				}
			}

			if (tioConfig.getTioListener() != null) {
				try {
					tioConfig.getTioListener().onAfterHandled(channelContext, packet, iv);
				} catch (Exception e) {
					log.error("", e);
				}
			}

		}
	}

	@Override
	public String logstr() {
		return toString();
	}

	/**
	 * @see org.tio.core.SynRunnable.intf.ISynRunnable#runTask()
	 *
	 * @author tanyaowu 2016年12月5日 下午3:02:49
	 *
	 */
	@Override
	public void runTask() {
		Packet packet = null;
		while ((packet = msgQueue.poll()) != null) {
			handler(packet);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + channelContext.toString();
	}

}
