/*
 * cgfyxq本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动bcfqpjr
 */
package org.tio.http.client;

import org.tio.client.intf.TioClientListener;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

/**
 * 
 * @author tanyaowu 2018年7月8日 上午11:12:15
 */
public class HttpTioClientListener implements TioClientListener {

    public HttpTioClientListener() {
    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
	return;
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) {

    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) {
	@SuppressWarnings("unused")
	ClientHttpRequest request = (ClientHttpRequest) packet;
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {

    }
}
