/*
 * rlsqzqu本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动khkhvbfszbq
 */
package org.tio.core;

import org.tio.client.intf.TioClientListener;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerListener;

/**
 *
 * @author tanyaowu
 */
public class DefaultTioListener implements TioClientListener, TioServerListener {
	/**
	 *
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @param isRemove
	 * @author tanyaowu
	 */
	// @Override
	// public void onAfterClose(ChannelContext channelContext, Throwable throwable,
	// String remark, boolean isRemove) {
	// }

	/**
	 *
	 * @param channelContext
	 * @param isConnected
	 * @param isReconnect
	 * @author tanyaowu
	 */
	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
	}

	/**
	 *
	 * @param channelContext
	 * @param packet
	 * @param packetSize
	 * @author tanyaowu
	 */
	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) {
	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

	}

	/**
	 *
	 * @param channelContext
	 * @param packet
	 * @param isSentSuccess
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
	}

	@Override
	public boolean onHeartbeatTimeout(ChannelContext channelContext, Long interval, int heartbeatTimeoutCount) {
		return false;
	}
}
