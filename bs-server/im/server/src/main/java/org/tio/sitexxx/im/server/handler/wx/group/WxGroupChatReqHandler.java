
package org.tio.sitexxx.im.server.handler.wx.group;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupChatReq;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.sitexxx.service.vo.wx.WxTemplateMsgVo;
import org.tio.utils.SystemTimer;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;

/**
 * Wx群聊处理者
 * @author tanyaowu 

 */
@CommandHandler(Command.WxGroupChatReq)
public class WxGroupChatReqHandler extends AbsImServerHandler {

	private static Logger log = LoggerFactory.getLogger(WxGroupChatReqHandler.class);

	public static final WxGroupChatReqHandler me = new WxGroupChatReqHandler();

	public WxGroupChatReqHandler() {
	}

	@SuppressWarnings("unused")
	private static UserService userService = UserService.ME;

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		boolean isSuper = UserService.isSuper(curr);
		WxGroupChatReq wxGroupChatReq = Json.toBean(packet.getBodyStr(), WxGroupChatReq.class);
		Long chatlinkid = wxGroupChatReq.getChatlinkid();
		if (Objects.equals(curr.getMsgforbiddenflag(), Const.YesOrNo.YES)) {
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.MSG_FORBIDDEN, "你已被管理员禁言");
			return;
		}
		String c = StrUtil.trim(wxGroupChatReq.getC()); //聊天内容
		if (wxGroupChatReq.getCardid() == null && StrUtil.isBlank(c) && StrUtil.isBlank(wxGroupChatReq.getMapjson())) {
			// 聊天内容是空
			return;
		}
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
//		if(Const.SENSITIVE_FLAG) {
//			c = SensitiveWordsService.findAndReplace(c);
//		}
		Long groupid = wxGroupChatReq.getG(); //
		if (StrUtil.isNotBlank(c) && c.length() > 2048) {
			// 聊天内容太长
			ImUtils.info(channelContext, "你发的内容有点长^_^", null);
			return;
		}
		if (chatlinkid == null) {
			log.error("会话id为空");
			WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), null, AppCode.FriendErrorCode.SYS_ERROR, "会话为空");
			return;
		} else {
			groupid = Math.abs(chatlinkid);
			Ret check = GroupService.checkGroupMsg(groupid, curr.getId());
			if (check.isFail()) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(check));
				return;
			}
			Short limitopen = ConfService.getShort(Const.ConfMapping.WX_GROUP_MSG_SEND_LIMIT_OPEN, Const.YesOrNo.YES);
			if (Objects.equals(limitopen, Const.YesOrNo.YES) && !isSuper) {
//				String ip = channelContext.getClientNode().getIp();
//				if (!IpWhiteListService.isWhiteIp(ip)) {
				WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
//					if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
				ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
				long lastGroupChatTime = imSessionContext.getLastGroupChatTime();
				int interval = ConfService.getInt("im.group.chat.time.interval", 2000);
				long currtime = SystemTimer.currTime;
				if ((currtime - lastGroupChatTime) <= interval) {
					//发言过快
					ImUtils.info(channelContext, "发言过快", null);
					return;
				}
				imSessionContext.setLastGroupChatTime(currtime);
//					}
//				}
			}
			chatlinkid = RetUtils.getOkTData(check);
			if (isSuper) {
				WxTemplateMsgVo templateMsgVo = WxChatApi.checkAdminTempMsg(c);
				if (templateMsgVo != null) {
					Ret ret = WxChatApi.SendTemplateMsg(channelContext, templateMsgVo, curr.getId(), groupid, Const.ChatMode.GROUP);
					if (ret.isFail()) {
						WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
						return;
					}
					return;
				}
			}
			Short contenttype = Const.ContentType.TEXT;
			if (wxGroupChatReq.getCardid() != null) {
				Ret ret = null;
				if (Objects.equals(wxGroupChatReq.getCardtype(), Const.MsgCardType.FRIEND)) {
					ret = FriendService.me.getFdCard(curr.getId(), wxGroupChatReq.getCardid().intValue());
				} else if (Objects.equals(wxGroupChatReq.getCardtype(), Const.MsgCardType.GROUP)) {
					ret = GroupService.me.getGroupCard(curr.getId(), wxGroupChatReq.getCardid());
				} else {
					WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), -groupid, AppCode.GroupErrorCode.SYS_ERROR, "无效名片类型");
					return;
				}
				if (ret.isFail()) {
					WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
					return;
				}
				WxMsgCardVo cardVo = RetUtils.getOkTData(ret);
				cardVo.setShareToBizid(groupid);
				c = Json.toJson(cardVo);
				contenttype = Const.ContentType.MSG_CARD;
			}
			if (StrUtil.isNotBlank(wxGroupChatReq.getMapjson())) {
				contenttype = Const.ContentType.POSITION;
				c = wxGroupChatReq.getMapjson();
			}
			if (StrUtil.isNotBlank(wxGroupChatReq.getQuotemid())) {
				contenttype = Const.ContentType.QUOTE_MSG;
				c = packet.getBodyStr();
			}
			//			//处理群的chatlinkid
			//			chatlinkid = groupItem.getChatlinkid();
			Ret ret = WxChatApi.sendGroupMsgEach(channelContext, c, contenttype, curr.getId(), groupid, null, null, wxGroupChatReq.getAt());
			if (ret.isFail()) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
				return;
			}
			//新增焦点补偿逻辑

			Map<String, Short> focusMap = SynService.me.focusDevice(curr.getId(), devicetype.getValue());
			if (focusMap == null || focusMap.get(-groupid + "") == null) {
				log.error("补偿焦点会话:chatlinkid:{},focusMap:{}", -groupid, Json.toJson(focusMap));
				IpInfo ipInfo = IpInfoService.ME.save(channelContext.getClientNode().getIp());
				WxChatQueueApi.joinFocusQueue(channelContext, curr, chatlinkid, groupid, Const.ChatMode.GROUP, devicetype.getValue(), ipInfo.getId());
			}
		}
	}
}
