
package org.tio.sitexxx.im.server.handler.wx.call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall11AnswerIceReq;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall12AnswerIceNtf;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 消息来自b<br>
 * b向a回复Answer，需要提供 e.candidate
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall11AnswerIceReq)
public class WxCall11AnswerIceReqHandler extends AbsImServerHandler {
	private static Logger log = LoggerFactory.getLogger(WxCall11AnswerIceReqHandler.class);

	public static final WxCall11AnswerIceReqHandler me = new WxCall11AnswerIceReqHandler();

	public WxCall11AnswerIceReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall11AnswerIceReq req = Json.toBean(packet.getBodyStr(), WxCall11AnswerIceReq.class);
		if (req.getId() == null) {
			log.error("ICE信息--------->ice-answer的id为空");
		}
		if (req.getCandidate() == null) {
			log.error("ICE信息--------->ice内容为空");

		}
		WxCallItem wxCallItem = CallItemService.me.getById(req.getId());
		String summary = null;

		if (req.getCandidate() != null) {
			summary = Command.WxCall11AnswerIceReq.name();
			WxCall12AnswerIceNtf ntf = new WxCall12AnswerIceNtf();
			ntf.fill(wxCallItem);//基础数据透传填充
			ntf.setCandidate(req.getCandidate());

			ImPacket toPacket = new ImPacket(Command.WxCall12AnswerIceNtf, ntf);

			//这里是发到channelcontextid，而不是user，因为user可能在多端，而channelcontextid就一个
			Tio.sendToId(channelContext.tioConfig, ntf.getFromcid(), toPacket);
		} else {
			summary = "STREAM_CONNECTED";
			//			WxCall09OfferIceReqHandler.updateStatusWhenStreamFinished(wxCallItem);
		}
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);
	}

}
