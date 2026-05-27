
package org.tio.sitexxx.im.server.handler.wx.friend;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendMsgReq;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendMsgResp;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxFriendMsg;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 朋友间的聊天消息请求，wx_friend_msg-- Client-->Server
 * 
 * @author lixinji
 * 2020年3月10日 下午3:14:44
 */
@CommandHandler(Command.WxFriendMsgReq)
public class WxFriendMsgReqHandler extends AbsImServerHandler {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxFriendMsgReqHandler.class);

	public static final WxFriendMsgReqHandler me = new WxFriendMsgReqHandler();

	public WxFriendMsgReqHandler() {
	}

	/**
	 * 消息获取三种
	 * 1、起始消息为空，起始页
	 * 2、有开始消息，中间页
	 * 3、有结束页，同步断层数据
	 */
	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxFriendMsgReq msgReq = Json.toBean(packet.getBodyStr(), WxFriendMsgReq.class);
		Long chatlinkid = msgReq.getChatlinkid();
		Long startmid = msgReq.getStartmid();
		Long endmid = msgReq.getEndmid();
		Ret ret = ChatMsgService.me.p2pMsgList(chatlinkid, curr.getId(), startmid, endmid);
		if (ret.isFail()) {
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
			return;
		}
		List<WxFriendMsg> data = RetUtils.getOkTData(ret);
		WxFriendMsgResp wxFriendMsgResp = new WxFriendMsgResp();
		wxFriendMsgResp.setData(data);
		wxFriendMsgResp.setChatlinkid(chatlinkid);
		if (data.size() < ConfService.getInt("im.history.chat.count.p2p", FriendService.WX_FRIEND_MSG_LIMIT)) {
			wxFriendMsgResp.setLastPage(true);
		}
		ImPacket imPacket = new ImPacket(Command.WxFriendMsgResp, wxFriendMsgResp);
		Ims.send(channelContext, imPacket);
	}
}
