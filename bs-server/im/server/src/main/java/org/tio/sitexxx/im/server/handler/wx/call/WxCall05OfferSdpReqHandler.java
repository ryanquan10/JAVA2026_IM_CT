
package org.tio.sitexxx.im.server.handler.wx.call;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall05OfferSdpReq;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall06OfferSdpNtf;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 消息来自a<br>
 * a向b提供offer，需要提供 sdp
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall05OfferSdpReq)
public class WxCall05OfferSdpReqHandler extends AbsImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxCall05OfferSdpReqHandler.class);

	public static final WxCall05OfferSdpReqHandler me = new WxCall05OfferSdpReqHandler();

	public WxCall05OfferSdpReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall05OfferSdpReq req = Json.toBean(packet.getBodyStr(), WxCall05OfferSdpReq.class);

		WxCallItem wxCallItem = CallItemService.me.getById(req.getId());
		String summary = Command.WxCall05OfferSdpReq.name();
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);

		WxCall06OfferSdpNtf ntf = new WxCall06OfferSdpNtf();
		ntf.fill(wxCallItem);//基础数据透传填充
		ntf.setSdp(req.getSdp());

		ImPacket toPacket = new ImPacket(Command.WxCall06OfferSdpNtf, ntf);

		//这里是发到channelcontextid，而不是user，因为user可能在多端，而channelcontextid就一个
		Tio.sendToId(channelContext.tioConfig, ntf.getTocid(), toPacket);
		if (Objects.equals(wxCallItem.getFromcid(), wxCallItem.getTocid())) { //自己呼叫自己时：offerdep时，即为建立媒体流
			WxCall07AnswerSdpReqHandler.updateStatusWhenStreamFinished(wxCallItem);
		}

	}

}
