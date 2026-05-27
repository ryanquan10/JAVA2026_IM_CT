
package org.tio.sitexxx.im.server.handler.wx.call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall09OfferIceReq;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall10OfferIceNtf;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 消息来自a<br>
 * a向b提供offer，需要提供 e.candidate
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall09OfferIceReq)
public class WxCall09OfferIceReqHandler extends AbsImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxCall09OfferIceReqHandler.class);

	public static final WxCall09OfferIceReqHandler me = new WxCall09OfferIceReqHandler();

	public WxCall09OfferIceReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall09OfferIceReq req = Json.toBean(packet.getBodyStr(), WxCall09OfferIceReq.class);
		WxCallItem wxCallItem = CallItemService.me.getById(req.getId());
		if (req.getId() == null) {
			log.error("ICE信息--------->ice-offer的id为空");
		}
		String summary = null;

		if (req.getCandidate() != null) {
			summary = Command.WxCall09OfferIceReq.name();

			WxCall10OfferIceNtf ntf = new WxCall10OfferIceNtf();
			ntf.fill(wxCallItem);//基础数据透传填充
			ntf.setCandidate(req.getCandidate());

			ImPacket toPacket = new ImPacket(Command.WxCall10OfferIceNtf, ntf);

			//这里是发到channelcontextid，而不是user，因为user可能在多端，而channelcontextid就一个
			Tio.sendToId(channelContext.tioConfig, ntf.getTocid(), toPacket);
		} else {
			summary = "STREAM_CONNECTED";
			//			updateStatusWhenStreamFinished(wxCallItem);
		}
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);
	}

}
