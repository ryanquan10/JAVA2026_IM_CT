/*
 * vrwvzfg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tbsajun
 */
package org.tio.core.task;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.ChannelContext.CloseCode;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.core.stat.ChannelStat;
import org.tio.core.stat.IpStat;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.utils.SystemTimer;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

/**
 * 解码任务对象，一个连接对应一个本对象
 *
 * @author 谭耀武 2012-08-09
 */
public class DecodeRunnable extends AbstractQueueRunnable<ByteBuffer> {
	private static final Logger	log						= LoggerFactory.getLogger(DecodeRunnable.class);
	private ChannelContext		channelContext			= null;
	private TioConfig			tioConfig				= null;
	/**
	 * 上一次解码剩下的数据
	 */
	private ByteBuffer			lastByteBuffer			= null;
	/**
	 * 新收到的数据
	 */
	private ByteBuffer			newReceivedByteBuffer	= null;

	/** The msg queue. */
	private FullWaitQueue<ByteBuffer> msgQueue = null;

	/**
	 *
	 */
	public DecodeRunnable(ChannelContext channelContext, Executor executor) {
		super(executor);
		this.channelContext = channelContext;
		this.tioConfig = channelContext.tioConfig;
		getMsgQueue();
	}

	/**
	 * 清空处理的队列消息
	 */
	@Override
	public void clearMsgQueue() {
		super.clearMsgQueue();
		lastByteBuffer = null;
		newReceivedByteBuffer = null;
	}

	/**
	 * @see java.lang.Runnable#run()
	 *
	 * @author tanyaowu 2017年3月21日 下午4:26:39
	 *
	 */
	public void decode() {
		ByteBuffer byteBuffer = newReceivedByteBuffer;
		if (lastByteBuffer != null) {
			byteBuffer = ByteBufferUtils.composite(lastByteBuffer, byteBuffer);
			lastByteBuffer = null;
		}

		label_2: while (true) {
			try {
				int initPosition = byteBuffer.position();
				int limit = byteBuffer.limit();
				int readableLength = limit - initPosition;
				Packet packet = null;
				ChannelStat channelStat = channelContext.stat;
				if (channelContext.packetNeededLength != null) {
//					if (log.isInfoEnabled()) {
//						log.info("{}, 解码所需长度:{}, 可用长度:{}", channelContext, channelContext.packetNeededLength, readableLength);
//					}
					if (readableLength >= channelContext.packetNeededLength) {
						if (log.isInfoEnabled()) {
							log.info("[{}]达到解码长度了，此前已经连续[{}]次长度不够，解码所需长度[{}], 可用长度[{}]", channelContext, channelStat.decodeFailCount, channelContext.packetNeededLength, readableLength);
						}
						packet = tioConfig.getTioHandler().decode(byteBuffer, limit, initPosition, readableLength, channelContext);
					}
				} else {
					try {
						packet = tioConfig.getTioHandler().decode(byteBuffer, limit, initPosition, readableLength, channelContext);
					} catch (BufferUnderflowException e) {
						// log.error("", e);
						// 数据不够读
					}
				}

				if (packet == null)// 数据不够，解不了码
				{
					// lastByteBuffer = ByteBufferUtils.copy(byteBuffer, initPosition, limit);
					if (tioConfig.useQueueDecode || (byteBuffer != newReceivedByteBuffer)) {
						byteBuffer.position(initPosition);
						byteBuffer.limit(limit);
						lastByteBuffer = byteBuffer;
					} else {
						lastByteBuffer = ByteBufferUtils.copy(byteBuffer, initPosition, limit);
					}
					
					channelStat.decodeFailCount++;
					// int len = byteBuffer.limit() - initPosition;
					if (log.isInfoEnabled()) {
						log.info("[{}]连续[{}]次解码失败，解码所需长度[{}], 可用长度[{}]", channelContext, channelStat.decodeFailCount, channelContext.packetNeededLength, readableLength);
					}
//					if (channelStat.decodeFailCount > 5) {
//						if (channelContext.packetNeededLength == null) {
//							if (log.isInfoEnabled()) {
//								log.info("{} 本次解码失败, 已经连续{}次解码失败，参与解码的数据长度共{}字节", channelContext, channelStat.decodeFailCount, readableLength);
//							}
//						}

						// 检查慢包攻击
						if (channelStat.decodeFailCount > 10) {
							// int capacity = lastByteBuffer.capacity();
							int per = readableLength / channelStat.decodeFailCount;
							if (per < Math.min(channelContext.getReadBufferSize() / 2, 256)) {
								StringBuilder sb = new StringBuilder();
								sb.append("[");
								sb.append(channelContext);
								sb.append("]连续解码");
								sb.append(channelStat.decodeFailCount);
								sb.append("次都不成功，并且平均每次接收到的数据为");
								sb.append(per);
								sb.append("字节，有慢攻击的嫌疑");
								String str = sb.toString();
								throw new TioDecodeException(str);
							}
						}
//					}
					return;
				} else // 解码成功
				{
					if (channelStat.decodeFailCount > 5) {
						log.info("[{}]连续[{}]次解码后, 解码成功，解码所需长度[{}], 可用长度[{}]", channelContext, channelStat.decodeFailCount, channelContext.packetNeededLength, readableLength);
					}
					channelContext.setPacketNeededLength(null);
					channelStat.latestTimeOfReceivedPacket = SystemTimer.currTime;
					channelStat.decodeFailCount = 0;

					int packetSize = byteBuffer.position() - initPosition;
					packet.setByteCount(packetSize);

					if (tioConfig.statOn) {
						tioConfig.groupStat.receivedPackets.incrementAndGet();
						channelStat.receivedPackets.incrementAndGet();
					}

					if (tioConfig.isIpStatEnable()) {
						try {
							for (Long v : tioConfig.ipStats.durationList) {
								IpStat ipStat = tioConfig.ipStats.get(v, channelContext);
								ipStat.getReceivedPackets().incrementAndGet();
								tioConfig.getIpStatListener().onAfterDecoded(channelContext, packet, packetSize, ipStat);
							}
						} catch (Exception e1) {
							log.error(packet.logstr(), e1);
						}
					}

					if (tioConfig.getTioListener() != null) {
						try {
							tioConfig.getTioListener().onAfterDecoded(channelContext, packet, packetSize);
						} catch (Throwable e) {
							log.error("", e);
						}
					}

					if (log.isDebugEnabled()) {
						log.debug("{}, 解包获得一个packet:{}", channelContext, packet.logstr());
					}

					handler(packet, packetSize);

					if (byteBuffer.hasRemaining())// 组包后，还剩有数据
					{
						if (log.isDebugEnabled()) {
							log.debug("{},组包后，还剩有数据:{}", channelContext, byteBuffer.remaining());
						}
						continue label_2;
					} else// 组包后，数据刚好用完
					{
						lastByteBuffer = null;
						if (log.isDebugEnabled()) {
							log.debug("{},组包后，数据刚好用完", channelContext);
						}
						return;
					}
				}
			} catch (Throwable e) {
				if (channelContext.logWhenDecodeError) {
					log.error("["+channelContext+"]解码时遇到异常", e);
				}

				channelContext.setPacketNeededLength(null);

				if (e instanceof TioDecodeException && tioConfig.isIpStatEnable()) {
					try {
						for (Long v : tioConfig.ipStats.durationList) {
							IpStat ipStat = tioConfig.ipStats.get(v, channelContext);
							ipStat.getDecodeErrorCount().incrementAndGet();
							tioConfig.getIpStatListener().onDecodeError(channelContext, ipStat);
						}
					} catch (Exception e1) {
						log.error(e1.toString(), e1);
					}
				}

				Tio.close(channelContext, e, "解码异常:" + e.getMessage(), CloseCode.DECODE_ERROR);
				return;
			}
		}
	}

	@Override
	public FullWaitQueue<ByteBuffer> getMsgQueue() {
		if (tioConfig.useQueueDecode) {
			if (msgQueue == null) {
				synchronized (this) {
					if (msgQueue == null) {
						msgQueue = new TioFullWaitQueue<ByteBuffer>(Integer.getInteger("tio.fullqueue.capacity", null), true);
					}
				}
			}
			return msgQueue;
		}
		return null;
	}

	/**
	 *
	 * @param packet
	 * @param byteCount
	 * @author tanyaowu
	 */
	public void handler(Packet packet, int byteCount) {
		switch (tioConfig.packetHandlerMode) {
		case SINGLE_THREAD:
			channelContext.handlerRunnable.handler(packet);
			break;
		case QUEUE:
			channelContext.handlerRunnable.addMsg(packet);
			channelContext.handlerRunnable.execute();
			break;
		default:
			channelContext.handlerRunnable.handler(packet);
			break;
		}
	}

	@Override
	public String logstr() {
		return toString();
	}

	@Override
	public void runTask() {
		while ((newReceivedByteBuffer = msgQueue.poll()) != null) {
			decode();
		}
	}

	/**
	 * 
	 * @param newReceivedByteBuffer
	 */
	public void setNewReceivedByteBuffer(ByteBuffer newReceivedByteBuffer) {
		if (log.isInfoEnabled()) {
			log.info("[{}]收到数据[{}]", channelContext, newReceivedByteBuffer);
		}
		this.newReceivedByteBuffer = newReceivedByteBuffer;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + channelContext.toString();
	}

}
