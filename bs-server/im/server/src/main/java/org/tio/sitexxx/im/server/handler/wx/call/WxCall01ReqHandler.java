
package org.tio.sitexxx.im.server.handler.wx.call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall01Req;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall02Ntf;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall04ReplyNtf;
import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.PushBizService;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;

/**
 * 消息来自a<br>
 * a --> s   a向b发起通话请求
 * @author tanyaowu 
 * 2020年1月23日 上午11:00:11
 */
@CommandHandler(Command.WxCall01Req)
public class WxCall01ReqHandler extends AbsImServerHandler {
	private static Logger log = LoggerFactory.getLogger(WxCall01ReqHandler.class);

	public static final WxCall01ReqHandler me = new WxCall01ReqHandler();

	public WxCall01ReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
		Long callId = cache.get(curr.getId() + "", Long.class);
		WxCall01Req req = Json.toBean(packet.getBodyStr(), WxCall01Req.class);
		Short devicetype = ImUtils.getDevicetype(channelContext).getValue();
		if(callId != null) {
			if(!devicetype.equals(Devicetype.PC.getValue()) && !devicetype.equals(Devicetype.WEB.getValue())) {
				Long exitcallId = cache.get(curr.getId() + "_" + devicetype, Long.class);
				Long tocallId = cache.get(req.getTouid() + "", Long.class);
				log.error("call:{},exitcallid:{},tocallid:{}",callId,exitcallId,tocallId);
				if(exitcallId != null && tocallId != null && exitcallId.equals(tocallId)) {
					WxCallUtils.endCall(channelContext, callId, curr.getId(), WxCallItem.Hanguptype.TCP_DROPPED);
				}
			} else {
				if(Caches.getCache(CacheConfig.WX_IS_CALLING_EXP).get(callId + "") != null) {
					WxCallUtils.endCall(channelContext, callId, curr.getId(), WxCallItem.Hanguptype.SYSTEM_RESTART);
				} else {
					//发起人正在通话
					WxCall04ReplyNtf ntf = new WxCall04ReplyNtf();
					ntf.setResult((short) 2);
					ntf.setReason("别闹，你正在通话");
					ImPacket toPacket = new ImPacket(Command.WxCall04ReplyNtf, ntf);
					Tio.sendToId(channelContext.tioConfig, channelContext.getId(), toPacket);
					return;
				}
			}
		}
		//好友逻辑 
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(curr.getId(), req.getTouid(), Const.ChatMode.P2P);
		if (!ChatService.existTwoFriend(userItem)) {
			WxCall04ReplyNtf ntf = new WxCall04ReplyNtf();
			ntf.setResult((short) 2);
			ntf.setReason("你们互相不是好友");
			ImPacket toPacket = new ImPacket(Command.WxCall04ReplyNtf, ntf);
			Tio.sendToId(channelContext.tioConfig, channelContext.getId(), toPacket);
			WxChatItems chatItems = ChatService.me.getBaseChatItems(userItem.getChatlinkid());
			//新增消息前检查
			Ret check = WxChatApi.checkFriendChat(curr.getId(), chatItems);
			if (check.isFail()) {
				WxChatApi.sendFriendErrorMsg(channelContext, curr.getId(), curr.getId(), curr.getId(), userItem.getChatlinkid(), RetUtils.getIntCode(check),
				        RetUtils.getRetMsg(check));
				return;
			}
			return;
		}
		req.setFromcid(channelContext.getId());
		req.setFromuid(curr.getId());
		req.setFromipid(IpInfoService.ME.save(channelContext.getClientNode().getIp()).getId());
		req.setFromdevice(devicetype);

		WxCallBase.save(req);
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		imSessionContext.setCallId(req.getId());

		String summary = Command.WxCall01Req.name();
		WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);

		Integer touid = req.getTouid(); //要和谁通话
		String touidStr = String.valueOf(touid);
		Short type = req.getType();//通话类型
		if (type == null) {
			log.error("需要提供通话类型");
			return;
		}


		callId = cache.get(touidStr, Long.class);
		if (callId != null) { //对方正在通话
			if(Caches.getCache(CacheConfig.WX_IS_CALLING_EXP).get(callId + "") != null) {
				WxCallUtils.endCall(channelContext, callId, curr.getId(), WxCallItem.Hanguptype.SYSTEM_RESTART);
			} else {
				WxCall04ReplyNtf ntf = new WxCall04ReplyNtf();
				ntf.setResult((short) 2);
				ntf.setReason("对方正在通话");

				ImPacket toPacket = new ImPacket(Command.WxCall04ReplyNtf, ntf);
				Tio.sendToId(channelContext.tioConfig, channelContext.getId(), toPacket);
				WxCallUtils.endCall(channelContext, req.getId(), curr.getId(), WxCallItem.Hanguptype.OTHER_SIDE_CALLING);
				return;
			}
		}
//		if(Caches.getCache(CacheConfig.WX_IS_CALLING_TIME).get(curr.getId() + "") != null || Caches.getCache(CacheConfig.WX_IS_CALLING_TIME).get(touidStr) != null) {
//			Thread.sleep(2500);
//		}
		//通知对方：有人想和你通话
		WxCall02Ntf ntf = new WxCall02Ntf();
		ntf.fill(req);//基础数据透传填充
		ntf.setFromavatar(curr.getAvatar());
		ntf.setFromnick(curr.getNick());
		ImPacket toPacket = new ImPacket(Command.WxCall02Ntf, ntf);
		Boolean f = Tio.sendToUser(channelContext.tioConfig, touidStr, toPacket);


		//后增加----发送通知
		Integer uid = curr.getId();

		User user = UserService.ME.getById(uid);
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, req.getType().shortValue()==1 ? "[语音通话]" : "[视频通话]");
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		//后增加----发送通知


		if (!f) { //对方不在线
			WxCall04ReplyNtf ntf2 = new WxCall04ReplyNtf();
			ntf2.setResult((short) 2);
			ntf2.setReason("对方不在线");

			ImPacket toPacket2 = new ImPacket(Command.WxCall04ReplyNtf, ntf2);
			Tio.sendToId(channelContext.tioConfig, channelContext.getId(), toPacket2);

			WxCallUtils.endCall(channelContext, req.getId(), curr.getId(), WxCallItem.Hanguptype.OFFLINE);

			return;
		} else {
			cache.put(touidStr+"_type", req.getType());//通话类型
			cache.put(touidStr+"_id", curr.getId());//目标用户id
			cache.put(touidStr, req.getId());
			cache.put(curr.getId() + "", req.getId());
			cache.put(curr.getId() + "_" + devicetype, req.getId());
		}
	}

}
