/*
 * kegosqa本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动kkebiwepcuahy
 */
package org.tio.websocket.server;

import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerListener;
import org.tio.websocket.common.WsSessionContext;

/**
 *
 * @author tanyaowu 2017年7月30日 上午9:16:02
 */
public class WsTioServerListener implements TioServerListener {

    public WsTioServerListener() {
    }

    // @Override
    // public void onAfterClose(ChannelContext channelContext, Throwable throwable,
    // String remark, boolean isRemove) {
    // }

    @SuppressWarnings("deprecation")
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect)
	    throws Exception {
	WsSessionContext wsSessionContext = new WsSessionContext();
	channelContext.set(wsSessionContext);
	return;
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {

    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove)
	    throws Exception {
    }

    @Override
    public boolean onHeartbeatTimeout(ChannelContext channelContext, Long interval, int heartbeatTimeoutCount) {
	return false;
    }

}
