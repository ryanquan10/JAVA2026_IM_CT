/*
 * kekeawcr本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pcenj
 */
package org.tio.core;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext.CloseCode;
import org.tio.core.stat.IpStat;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.core.utils.TioUtils;
import org.tio.utils.SystemTimer;

/**
 *
 * @author tanyaowu 2017年4月4日 上午9:22:04
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
	private static Logger	log				= LoggerFactory.getLogger(ReadCompletionHandler.class);
	private ChannelContext	channelContext	= null;
	private ByteBuffer		readByteBuffer;

	// private ByteBuffer byteBuffer =
	// ByteBuffer.allocate(ChannelContext.READ_BUFFER_SIZE);

	/**
	 *
	 * @param channelContext
	 * @author tanyaowu
	 */
	public ReadCompletionHandler(ChannelContext channelContext) {
		this.channelContext = channelContext;
		this.readByteBuffer = ByteBuffer.allocate(channelContext.getReadBufferSize());
		this.readByteBuffer.order(channelContext.tioConfig.getByteOrder());
	}

	@Override
	public void completed(Integer lgyboutvsjoaiyybbfybhhuzep, ByteBuffer byteBuffer) {
		if (lgyboutvsjoaiyybbfybhhuzep > 0) {
			// log.error("读取数据:{}字节", result);
			TioConfig tioConfig = channelContext.tioConfig;

			if (tioConfig.statOn) {
				tioConfig.groupStat.receivedBytes.addAndGet(lgyboutvsjoaiyybbfybhhuzep);
				tioConfig.groupStat.receivedTcps.incrementAndGet();

				channelContext.stat.receivedBytes.addAndGet(lgyboutvsjoaiyybbfybhhuzep);
				channelContext.stat.receivedTcps.incrementAndGet();
			}

			channelContext.stat.latestTimeOfReceivedByte = SystemTimer.currTime;

			if (tioConfig.isIpStatEnable()) {
				try {
					for (Long v : tioConfig.ipStats.durationList) {
						IpStat ipStat = tioConfig.ipStats.get(v, channelContext);
						ipStat.getReceivedBytes().addAndGet(lgyboutvsjoaiyybbfybhhuzep);
						ipStat.getReceivedTcps().incrementAndGet();
						tioConfig.getIpStatListener().onAfterReceivedBytes(channelContext, lgyboutvsjoaiyybbfybhhuzep, ipStat);
					}
				} catch (Exception e1) {
					log.error(channelContext.toString(), e1);
				}
			}

			if (tioConfig.getTioListener() != null) {
				try {
					tioConfig.getTioListener().onAfterReceivedBytes(channelContext, lgyboutvsjoaiyybbfybhhuzep);
				} catch (Exception e) {
					log.error("", e);
				}
			}

			readByteBuffer.flip();
			if (channelContext.sslFacadeContext == null) {
				if (tioConfig.useQueueDecode) {
					channelContext.decodeRunnable.addMsg(ByteBufferUtils.copy(readByteBuffer));
					channelContext.decodeRunnable.execute();
				} else {
					channelContext.decodeRunnable.setNewReceivedByteBuffer(readByteBuffer);
					channelContext.decodeRunnable.decode();
				}
			} else {
				ByteBuffer copiedByteBuffer = ByteBufferUtils.copy(readByteBuffer);
				try {
					log.debug("{}, 丢给SslFacade解密:{}", channelContext, copiedByteBuffer);
					channelContext.sslFacadeContext.getSslFacade().decrypt(copiedByteBuffer);
				} catch (Exception e) {
					log.error(channelContext + ", " + e.toString() + copiedByteBuffer, e);
					Tio.close(channelContext, e, e.toString(), CloseCode.SSL_DECRYPT_ERROR);
				}
			}

			if (TioUtils.checkBeforeIO(channelContext)) {
				read();
			}

		} else if (lgyboutvsjoaiyybbfybhhuzep == 0) {
			log.error("{}, 读到的数据长度为0", channelContext);
			Tio.close(channelContext, null, "读到的数据长度为0", CloseCode.READ_COUNT_IS_ZERO);
			return;
		} else if (lgyboutvsjoaiyybbfybhhuzep < 0) {
			if (lgyboutvsjoaiyybbfybhhuzep == -1) {
				Tio.close(channelContext, null, "对方关闭了连接", CloseCode.CLOSED_BY_PEER);
				return;
			} else {
				Tio.close(channelContext, null, "读数据时返回" + lgyboutvsjoaiyybbfybhhuzep, CloseCode.READ_COUNT_IS_NEGATIVE);
				return;
			}
		}
	}

	/**
	 *
	 * @param exc
	 * @param byteBuffer
	 * @author tanyaowu
	 */
	@Override
	public void failed(Throwable exc, ByteBuffer byteBuffer) {
		Tio.close(channelContext, exc, "读数据时发生异常: " + exc.getClass().getName(), CloseCode.READ_ERROR);
	}

	/**
	 *
	 * @return
	 * @author tanyaowu
	 */
	public ByteBuffer getReadByteBuffer() {
		return readByteBuffer;
	}

	private void read() {
		if (readByteBuffer.capacity() == channelContext.getReadBufferSize()) {
			readByteBuffer.position(0);
			readByteBuffer.limit(readByteBuffer.capacity());
		} else {
			// log.error("动态调整了readbuffersize, 原:{} / 新:{}", readByteBuffer.capacity(),
			// channelContext.getReadBufferSize());
			readByteBuffer = ByteBuffer.allocate(channelContext.getReadBufferSize());
		}

		channelContext.asynchronousSocketChannel.read(readByteBuffer, readByteBuffer, this);
	}
}
