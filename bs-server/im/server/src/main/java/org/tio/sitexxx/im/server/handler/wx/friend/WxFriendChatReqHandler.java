
package org.tio.sitexxx.im.server.handler.wx.friend;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendChatReq;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserRoleService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.sitexxx.service.vo.wx.WxTemplateMsgVo;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;

/**
 * 朋友间的聊天请求，wx_friend_msg-- Client-->Server
 * @author tanyaowu 

 */
@CommandHandler(Command.WxFriendChatReq)
public class WxFriendChatReqHandler extends AbsImServerHandler {

	private static Logger log = LoggerFactory.getLogger(WxFriendChatReqHandler.class);

	public static final WxFriendChatReqHandler me = new WxFriendChatReqHandler();

	public WxFriendChatReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxFriendChatReq wxFriendChatReq = Json.toBean(packet.getBodyStr(), WxFriendChatReq.class);
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
		String c = wxFriendChatReq.getC(); //聊天内容
		if (wxFriendChatReq.getCardid() == null && StrUtil.isBlank(c) && StrUtil.isBlank(wxFriendChatReq.getMapjson())) {
			return;
		}
		boolean isSuper = UserService.isSuper(curr);
		//		String text = c;//SensitiveWordsService.findAndReplace(c);
		String text = c;
		//		if (curr != null && (UserService.isSuper(curr))) {
		//			isSuper = true;
		//		}
		//		if(Const.SENSITIVE_FLAG) {放到保存处理
		//			text = SensitiveWordsService.findAndReplace(text);
		//		}
		Long chatlinkid = wxFriendChatReq.getChatlinkid();
		if (Objects.equals(curr.getMsgforbiddenflag(), Const.YesOrNo.YES)) {
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.MSG_FORBIDDEN, "你已被管理员禁言");
			return;
		}
		if (StrUtil.isNotBlank(c) && c.length() > 2048) {
			// 聊天内容太长
			ImUtils.info(channelContext, "你发的内容有点长^_^", null);
			return;
		}
		//		if (!isSuper) {
		//			if(StrUtil.isNotBlank(c)) {
		//				text = StringEscapeUtils.escapeHtml4(text);
		//			}
		//		}

		if (chatlinkid != null) {
			WxChatItems chatItems = ChatService.me.getBaseChatItems(chatlinkid);
			//新增消息前检查
			Ret check = WxChatApi.checkFriendChat(curr.getId(), chatItems);
			if (check.isFail()) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, RetUtils.getIntCode(check), RetUtils.getRetMsg(check));
				return;
			}
			Integer touid = chatItems.getBizid().intValue(); //发给谁
			User toUser = UserService.ME.getById(touid);
			if (toUser == null) {
				Tio.remove(channelContext, "提供了错误的userid[" + touid + "]");
				return;
			} else {
				if (!UserRoleService.checkUserStatus(toUser)) {
					WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.USER_ERROR, "该用户已注销");
					return;
				}
			}
			if (isSuper) {
				WxTemplateMsgVo templateMsgVo = WxChatApi.checkAdminTempMsg(text);
				if (templateMsgVo != null) {
					Ret ret = WxChatApi.SendTemplateMsg(channelContext, templateMsgVo, curr.getId(), new Long(touid), Const.ChatMode.P2P);
					if (ret.isFail()) {
						WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR,
						        RetUtils.getRetMsg(ret));
						return;
					}
					return;
				}
			}
			Short contenttype = Const.ContentType.TEXT;
			if (wxFriendChatReq.getCardid() != null) {
				Ret ret = null;
				if (Objects.equals(wxFriendChatReq.getCardtype(), Const.MsgCardType.FRIEND)) {
					ret = FriendService.me.getFdCard(curr.getId(), wxFriendChatReq.getCardid().intValue());
				} else if (Objects.equals(wxFriendChatReq.getCardtype(), Const.MsgCardType.GROUP)) {
					ret = GroupService.me.getGroupCard(curr.getId(), wxFriendChatReq.getCardid());
				} else {
					WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, "无效名片类型");
					return;
				}
				if (ret.isFail()) {
					WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
					return;
				}
				WxMsgCardVo cardVo = RetUtils.getOkTData(ret);
				cardVo.setShareToBizid(new Long(touid));
				text = Json.toJson(cardVo);
				contenttype = Const.ContentType.MSG_CARD;
			}
			if (StrUtil.isNotBlank(wxFriendChatReq.getMapjson())) {
				contenttype = Const.ContentType.POSITION;
				text = wxFriendChatReq.getMapjson();
			}
			if (StrUtil.isNotBlank(wxFriendChatReq.getQuotemid())) {
				contenttype = Const.ContentType.QUOTE_MSG;
				text = packet.getBodyStr();
			}
			//发送消息
			Ret ret = WxChatApi.sendFdMsgEach(channelContext, text, contenttype, curr.getId(), toUser.getId(), Const.YesOrNo.NO);
			if (ret.isFail()) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, RetUtils.getIntCode(ret), RetUtils.getRetMsg(ret));
				return;
			}
			//新增焦点补偿逻辑
			Map<String, Short> focusMap = SynService.me.focusDevice(curr.getId(), devicetype.getValue());
			if (focusMap == null || focusMap.get(chatlinkid + "") == null) {
				log.error("补偿焦点会话:chatlinkid:{},focusMap:{},devicetype:{}", chatlinkid, Json.toJson(focusMap), devicetype.getValue());
				IpInfo ipInfo = IpInfoService.ME.save(channelContext.getClientNode().getIp());
				WxChatQueueApi.joinFocusQueue(channelContext, curr, chatlinkid, null, Const.ChatMode.P2P, devicetype.getValue(), ipInfo.getId());
			}
		} else {
			log.error("私聊会话id为空");
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), null, AppCode.FriendErrorCode.SYS_ERROR, "会话为空");
			return;
		}
	}

}
