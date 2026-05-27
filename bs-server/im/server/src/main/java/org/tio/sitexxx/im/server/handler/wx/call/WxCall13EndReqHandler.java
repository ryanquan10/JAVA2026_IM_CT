
package org.tio.sitexxx.im.server.handler.wx.call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall13EndReq;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * a或b --> s 发起结束通话请求
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall13EndReq)
public class WxCall13EndReqHandler extends AbsImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxCall13EndReqHandler.class);

	public static final WxCall13EndReqHandler me = new WxCall13EndReqHandler();

	public WxCall13EndReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall13EndReq req = Json.toBean(packet.getBodyStr(), WxCall13EndReq.class);

		String summary = Command.WxCall13EndReq.name();
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);

		Short huangup = WxCallItem.Hanguptype.NORMAL;
		if (req.getHanguptype() != null) {
			huangup = req.getHanguptype();
		}
		WxCallUtils.endCall(channelContext, req.getId(), curr.getId(), huangup);
	}
}
