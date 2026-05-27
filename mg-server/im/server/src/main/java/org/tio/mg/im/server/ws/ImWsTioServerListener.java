
package org.tio.mg.im.server.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.WsResponseExt;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.im.server.TioSiteImTioServerListener;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsTioServerListener;

/**
 * @author tanyaowu 
 * 2016年9月8日 上午10:51:37
 */
public class ImWsTioServerListener extends WsTioServerListener {
	private static Logger log = LoggerFactory.getLogger(ImWsTioServerListener.class);

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	public static final ImWsTioServerListener me = new ImWsTioServerListener();

	private ImWsTioServerListener() {
		super();
	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
		super.onAfterConnected(channelContext, isConnected, isReconnect);
		WsSessionContext wsSessionContext = (WsSessionContext) channelContext.get();

		TioSiteImTioServerListener.me.onAfterConnected(channelContext, isConnected, isReconnect);

		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		imSessionContext.setWebsocket(true);
		imSessionContext.setWsSessionContext(wsSessionContext);
	}

	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
		super.onAfterDecoded(channelContext, packet, packetSize);
		WsRequest wsRequest = (WsRequest) packet;
		Opcode opcode = wsRequest.getWsOpcode();
		if (opcode == Opcode.PONG) {
			return;
		}

		ImPacket imPacket = ImWsMsgHandler.toImPacket(wsRequest, channelContext);
		if (imPacket != null) {
			TioSiteImTioServerListener.me.onAfterDecoded(channelContext, imPacket, packetSize);
		}
	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
		super.onAfterSent(channelContext, packet, isSentSuccess);
		if (packet instanceof WsResponseExt) {
			WsResponseExt wsResponse = (WsResponseExt) packet;
			ImPacket imPacket = wsResponse.getInitPacket();
			TioSiteImTioServerListener.me.onAfterSent(channelContext, imPacket, isSentSuccess);
		} else {
			log.debug("握手包");
		}
	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
		super.onBeforeClose(channelContext, throwable, remark, isRemove);
		TioSiteImTioServerListener.me.onBeforeClose(channelContext, throwable, remark, isRemove);
	}
}
