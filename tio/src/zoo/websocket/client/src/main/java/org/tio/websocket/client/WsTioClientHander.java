/*
 * ofdfofmd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rcncifz
 */
package org.tio.websocket.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.intf.TioClientHandler;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.client.httpclient.HttpRequestEncoder;
import org.tio.websocket.client.httpclient.HttpResponseDecoder;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsClientDecoder;
import org.tio.websocket.common.WsClientEncoder;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.common.WsSessionContext;

import io.reactivex.subjects.Subject;

public class WsTioClientHander implements TioClientHandler {
    private static final Logger log = LoggerFactory.getLogger(WsTioClientHander.class);

    private static final String NOT_FINAL_WEBSOCKET_PACKET_PARTS = "TIO_N_F_W_P_P";

    @SuppressWarnings("unchecked")
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext ctx)
	    throws TioDecodeException {
	WsSessionContext session = (WsSessionContext) ctx.get();
	if (!session.isHandshaked()) {
	    HttpResponse response = HttpResponseDecoder.decode(buffer, limit, position, readableLength, ctx);
	    session.setHandshakeResponse(response);
	    return response;
	}
	WsResponse packet = WsClientDecoder.decode(buffer, ctx);
	if (packet != null) {
	    if (!packet.isWsEof()) { // 数据包尚未完成
		List<WsResponse> parts = (List<WsResponse>) ctx.getAttribute(NOT_FINAL_WEBSOCKET_PACKET_PARTS);
		if (parts == null) {
		    parts = new ArrayList<>();
		    ctx.setAttribute(NOT_FINAL_WEBSOCKET_PACKET_PARTS, parts);
		}
		parts.add(packet);
	    } else {
		List<WsResponse> parts = (List<WsResponse>) ctx.getAttribute(NOT_FINAL_WEBSOCKET_PACKET_PARTS);
		if (parts != null) {
		    ctx.setAttribute(NOT_FINAL_WEBSOCKET_PACKET_PARTS, null);

		    parts.add(packet);
		    WsResponse first = parts.get(0);
		    packet.setWsOpcode(first.getWsOpcode());

		    int allBodyLength = 0;
		    for (WsResponse wsRequest : parts) {
			allBodyLength += wsRequest.getBody().length;
		    }

		    byte[] allBody = new byte[allBodyLength];
		    Integer index = 0;
		    for (WsResponse wsRequest : parts) {
			System.arraycopy(wsRequest.getBody(), 0, allBody, index, wsRequest.getBody().length);
			index += wsRequest.getBody().length;
		    }
		    packet.setBody(allBody);
		}

		HttpRequest handshakeRequest = session.getHandshakeRequest();
		if (packet.getWsOpcode() != Opcode.BINARY) {
		    try {
			String text = new String(packet.getBody(), handshakeRequest.getCharset());
			packet.setWsBodyText(text);
		    } catch (UnsupportedEncodingException e) {
			log.error("", e);
		    }
		}
	    }
	}
	return packet;
    }

    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext ctx) {
	WsSessionContext session = (WsSessionContext) ctx.get();
	if (!session.isHandshaked() && packet instanceof HttpRequest) {
	    try {
		return HttpRequestEncoder.encode((HttpRequest) packet, tioConfig, ctx);
	    } catch (UnsupportedEncodingException e) {
		log.error("");
		return null;
	    }
	}
	try {
	    return WsClientEncoder.encode((WsPacket) packet, tioConfig, ctx);
	} catch (Exception e) {
	    log.error("");
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handler(Packet packet, ChannelContext ctx) throws Exception {
	if (packet instanceof WsPacket) {
	    WsPacket wsPacket = (WsPacket) packet;
	    if (!wsPacket.isWsEof()) {
		return;
	    }
	}
	Subject<Packet> packetPublisher = (Subject<Packet>) ctx.getAttribute(WebSocketImpl.packetPublisherKey);
	packetPublisher.onNext(packet);
    }

    @Override
    public Packet heartbeatPacket(ChannelContext ctx) {
	return null;
    }
}
