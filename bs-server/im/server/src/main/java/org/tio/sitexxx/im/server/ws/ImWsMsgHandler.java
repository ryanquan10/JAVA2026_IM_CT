
package org.tio.sitexxx.im.server.ws;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.http.common.HttpConst;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.HandshakeReq;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.PacketDispatcher;
import org.tio.sitexxx.im.server.handler.wx.WxHandshakeReqHandler;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年6月28日 下午5:32:38
 */
public class ImWsMsgHandler implements IWsMsgHandler {
	private static Logger log = LoggerFactory.getLogger(ImWsMsgHandler.class);

	private static final int COMMAND_LENGTH = 5;

	private PacketDispatcher packetDispatcher = null;

	/**
	 *
	 * @author tanyaowu
	 */
	public ImWsMsgHandler(PacketDispatcher packetDispatcher) {
		this.packetDispatcher = packetDispatcher;
	}

	@Override
	public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
		boolean allowAccess = ImUtils.isAllowAccessWs(request, httpResponse, channelContext);
		if (!allowAccess) {
			return null;
		}
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		String is_wx = request.getParam("wx");
		if ("1".equals(is_wx)) {
			imSessionContext.setWx(true);
		}

		String token = request.getParam(Const.Http.SESSION_COOKIE_NAME); // session id

		MobileInfo mobileInfo = new MobileInfo();
		mobileInfo.setCid("pc");
		mobileInfo.setImei("1");
		mobileInfo.setDeviceinfo(request.getHeader(HttpConst.RequestHeaderKey.User_Agent));
		mobileInfo.setDevicetype(Devicetype.WEB.getValue());

		HandshakeReq handshakeReq = new HandshakeReq();
		handshakeReq.setToken(token);
		handshakeReq.setSign("");
		handshakeReq.setMobileInfo(mobileInfo);

		if (imSessionContext.isWx()) {
			ImPacket imPacket = new ImPacket(Command.WxHandshakeReq, handshakeReq);
			WxHandshakeReqHandler.me.handler(imPacket, channelContext, true);
		} else {
			Tio.remove(channelContext, "不支持非wx握手");
			return null;
		}

		return httpResponse;
	}

	@Override
	public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		String text = ByteBufferUtils.getUtf8(bytes);
		//		log.info("收到byte消息:{},{}", bytes, text);
		return onText(wsRequest, text, channelContext);
	}

	@Override
	public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		Tio.remove(channelContext, "receive close flag");
		return null;
	}

	@Override
	public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
		ImPacket imPacket = toImPacket(wsRequest, channelContext);
		if (imPacket != null) {
			packetDispatcher.dispatch(imPacket, channelContext, true);
		}
		return null;
	}

	/**
	 * 把websocket请求包转换成ImPacket
	 * @param wsRequest
	 * @param channelContext
	 * @return
	 * @author: tanyaowu
	 */
	public static ImPacket toImPacket(WsRequest wsRequest, ChannelContext channelContext) {
		if (wsRequest.isHandShake()) {
//			ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
//			if (imSessionContext.isWx()) {
			ImPacket imPacket = new ImPacket(Command.WxHandshakeReq);
			return imPacket;
//			} else {
//				Tio.remove(channelContext, "不支持非wx握手");
//				return null;
//			}
		}

		String text = null;
		Opcode opcode = wsRequest.getWsOpcode();
		if (opcode == Opcode.PONG) {
			ImPacket imPacket = new ImPacket(Command.HeartbeatReq);
			return imPacket;
		}

		if (wsRequest.getWsBodyText() == null) {
			try {
				text = new String(wsRequest.getBody(), "utf-8");
				wsRequest.setWsBodyText(text);
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
			}
		} else {
			text = wsRequest.getWsBodyText();
		}
		if (StrUtil.isBlank(text)) {
			Tio.remove(channelContext, "消息长度小于" + COMMAND_LENGTH);
			return null;
		}
		int len = text.length();
		if (len < COMMAND_LENGTH) {
			Tio.remove(channelContext, "消息长度小于" + COMMAND_LENGTH);
			return null;
		}

		String commandValueStr = StrUtil.sub(text, 0, COMMAND_LENGTH);
		short commandValue = 0;
		try {
			commandValue = Short.parseShort(commandValueStr);
		} catch (NumberFormatException e) {
			log.error("", e);
			Tio.remove(channelContext, "消息前" + COMMAND_LENGTH + "位不能转成short");
			return null;
		}

		Command command = Command.from(commandValue);
		if (command == null) {
			Tio.remove(channelContext, "消息命令码【" + commandValue + "】不正确");
			return null;
		}

		ImPacket imPacket = new ImPacket(command);
		String bodyStr = null;
		if (len > COMMAND_LENGTH) {
			bodyStr = StrUtil.subSuf(text, COMMAND_LENGTH);
			imPacket.setBodyStr(bodyStr);
		}

		return imPacket;
	}

	@Override
	public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
		Ims.sendHandshake(channelContext);
	}
}
