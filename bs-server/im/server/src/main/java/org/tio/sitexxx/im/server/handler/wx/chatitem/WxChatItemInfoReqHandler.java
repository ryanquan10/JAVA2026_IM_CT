
package org.tio.sitexxx.im.server.handler.wx.chatitem;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.chatitem.WxChatItemInfoReq;
import org.tio.sitexxx.im.common.bs.wx.chatitem.WxChatItemInfoResp;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 会话信息请求，wx_friend_msg-- Client-->Server
 * 
 * @author lixinji
 * 2020年3月10日 下午3:14:44
 */
@CommandHandler(Command.WxChatItemInfoReq)
public class WxChatItemInfoReqHandler extends AbsImServerHandler {

	private static Logger log = LoggerFactory.getLogger(WxChatItemInfoReqHandler.class);

	public static final WxChatItemInfoReqHandler me = new WxChatItemInfoReqHandler();

	public WxChatItemInfoReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxChatItemInfoReq infoReq = Json.toBean(packet.getBodyStr(), WxChatItemInfoReq.class);
		Long chatlinkid = infoReq.getChatlinkid();
		if (chatlinkid == null) {
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, "会话为空");
			return;
		}
		long oldchatlinkid = chatlinkid;
		if (chatlinkid <= 0) {
			//处理群的chatlinkid
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), -chatlinkid);
			if (groupItem == null || groupItem.getChatlinkid() == null) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), oldchatlinkid, AppCode.GroupErrorCode.SYS_ERROR, "会话为空");
				return;
			}
			chatlinkid = groupItem.getChatlinkid();
		}
		WxChatItems chatItems = ChatService.me.getBaseChatItems(chatlinkid);
		if (chatItems.getChatmode().equals((short) 2)) {
			WxGroup wxGroup = WxGroup.dao.findById(chatItems.getBizid());
			if (wxGroup != null) {
				chatItems.setJoinnum((short)(chatItems.getJoinnum()+wxGroup.getVnum()));
			}
		}
		if (chatItems == null) {
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), oldchatlinkid, AppCode.FriendErrorCode.SYS_ERROR, "会话不存在");
			return;
		}
		if (!Objects.equals(curr.getId(), chatItems.getUid())) {
			log.error("异常登录请求会话信息：登录uid：{}，请求会话的uid：{},chatlinkid:{}", curr.getId(), chatItems.getUid(), oldchatlinkid);
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), oldchatlinkid, AppCode.ForbidOper.DATA_DIFFER, "异常登录请求");
			return;
		}
		WxChatItemInfoResp infoResp = new WxChatItemInfoResp();
		if (chatItems.getChatmode().equals(Short.valueOf("1"))) {
			User user = User.dao.findById(chatItems.getBizid());
			if (user != null) {
				IpInfo ipInfo = IpInfo.dao.findById(user.getIpid());
				chatItems.setIp(ipInfo.getIp());
				chatItems.setCity(ipInfo.getCity());
			}
		}
		infoResp.setData(chatItems);
		infoResp.setChatlinkid(oldchatlinkid);

		ImPacket imPacket = new ImPacket(Command.WxChatItemInfoResp, infoResp);
		Ims.send(channelContext, imPacket);
	}
}
