
package org.tio.sitexxx.im.server.handler.wx.call;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall07AnswerSdpReq;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall08AnswerSdpNtf;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 消息来自b<br>
 * b向a回复Answer，需要提供 sdp
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall07AnswerSdpReq)
public class WxCall07AnswerSdpReqHandler extends AbsImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxCall07AnswerSdpReqHandler.class);

	public static final WxCall07AnswerSdpReqHandler me = new WxCall07AnswerSdpReqHandler();

	public WxCall07AnswerSdpReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall07AnswerSdpReq req = Json.toBean(packet.getBodyStr(), WxCall07AnswerSdpReq.class);

		WxCallItem wxCallItem = CallItemService.me.getById(req.getId());

		String summary = Command.WxCall07AnswerSdpReq.name();
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);

		WxCall08AnswerSdpNtf ntf = new WxCall08AnswerSdpNtf();
		ntf.fill(wxCallItem);//基础数据透传填充
		ntf.setSdp(req.getSdp());

		ImPacket toPacket = new ImPacket(Command.WxCall08AnswerSdpNtf, ntf);

		//这里是发到channelcontextid，而不是user，因为user可能在多端，而channelcontextid就一个
		Tio.sendToId(channelContext.tioConfig, ntf.getFromcid(), toPacket);
		//更为精准的逻辑可以放到ICE处理逻辑后，后续需要调整的话，需要客户端监听通道的链接状态，需要解决发起方和被呼叫方同时操作问题
		updateStatusWhenStreamFinished(wxCallItem);
	}

	/**
	 * @param wxCallItem
	 * @author lixinji
	 * 2020年6月9日 下午2:28:23
	 */
	public static void updateStatusWhenStreamFinished(WxCallItem wxCallItem) {
		if (!Objects.equals(wxCallItem.getStatus(), WxCallItem.Status.STREAM_CONNECTED)) {
			Date date = new Date();
			wxCallItem.setStatus(WxCallItem.Status.STREAM_CONNECTED);
			wxCallItem.setConnectedtime(date);
			wxCallItem.setStreamwait(date.getTime() - wxCallItem.getResptime().getTime());
			WxCallUtils.updateItem(wxCallItem);
		}
	}

}
