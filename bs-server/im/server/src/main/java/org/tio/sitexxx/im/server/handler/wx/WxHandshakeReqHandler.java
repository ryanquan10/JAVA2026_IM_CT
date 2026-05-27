
package org.tio.sitexxx.im.server.handler.wx;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.HandshakeReq;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.call.WxCallUtils;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.ChatroomJoinLeave;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserAgent;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.model.main.WxJpushUser;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.LoginLogService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.cache.ICache;
import org.tio.utils.crypto.Md5;
import org.tio.utils.json.Json;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 握手包处理者
 * @author tanyaowu 
 * 2016年9月8日 下午3:59:14
 */
@CommandHandler(Command.WxHandshakeReq)
public class WxHandshakeReqHandler extends AbsImServerHandler {
	private static Logger log = LoggerFactory.getLogger(WxHandshakeReqHandler.class);

	public static final WxHandshakeReqHandler me = new WxHandshakeReqHandler();

	public WxHandshakeReqHandler() {
	}

	@Override
	public boolean needLogin() {
		return false;
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		imSessionContext.setWx(true);
		HandshakeReq handshakeReq = null;
		if (isWebsocket) {
			handshakeReq = (HandshakeReq) packet.getBodyObj();
		} else {
			handshakeReq = Json.toBean(packet.getBodyStr(), HandshakeReq.class);
		}

		String token = handshakeReq.getToken(); //客户端通过http登录后，服务器返回给客户端的token值，没登录则为空串
		MobileInfo mobileInfo = handshakeReq.getMobileInfo();
		Devicetype devicetype = null;
		if (token == null) {
			token = "";
		}
		//		Node serverNode = channelContext.getServerNode();
		if (isWebsocket) {
			devicetype = Devicetype.WEB;
			//			imSessionContext.setHandshaked(true);
		} else {
			mobileInfo = handshakeReq.getMobileInfo();
			@SuppressWarnings("unused")
			String version = mobileInfo.getAppversion(); //协议版本号
			String imei = mobileInfo.getImei(); //设备id，对于手机就是imei
			String deviceinfo = mobileInfo.getDeviceinfo(); //设备型号
			String cid = mobileInfo.getCid(); //渠道号
			String sign = handshakeReq.getSign(); //签名
			Short devicetypeValue = handshakeReq.getDevicetype();

			String mysign = null;
			boolean signOk = false;

			devicetype = Devicetype.from(devicetypeValue);
			mysign = Md5.getMD5(token + imei + deviceinfo + devicetype.getValue() + cid + Const.HANDSHAKE_KEY);
			if (Objects.equals(sign, mysign)) {
				signOk = true;
				mobileInfo.setDevicetype(devicetypeValue);
				if (Objects.equals(devicetypeValue, Devicetype.IOS.getValue()) || Objects.equals(devicetypeValue, Devicetype.ANDROID.getValue())) {
					mobileInfo.setFromApp(true);
				}
			}

			if (!signOk) {//验签不通过
				Tio.remove(channelContext, "握手过程中，验签失败");
				return;
			}
		}

		User user = null;
		if (StrUtil.isNotBlank(token)) {
			Tio.unbindToken(channelContext);
			Tio.bindToken(channelContext, token);

			user = ImUtils.getUser(token);

			if (user != null) {
				Integer userid = user.getId();
				//				SynService.me.online(userid, devicetype.getValue(), null);
				//				Map<String, OnlineVo> onlineMap = SynService.me.onlineMap(userid);
				if (WxChatApi.isOutline(userid)) {//之前没有有效链接
					SynService.me.clearCluChatCache(userid);
				}
				//				boolean isSuper = UserService.isSuper(user);
				//				imSessionContext.setSupper(isSuper);
				Tio.bindUser(channelContext, userid + "");
				//这个是用来统计的，被T后此处的值仍然保留，所以不要用这个值来获取当前通道的用户
				imSessionContext.setUid(userid);

				//查询用户所在的群组，逐个绑定，以便后面的群发
				List<WxChatUserItem> userItems = ChatIndexService.me.getLinkActUserIndex(userid, Const.ChatMode.GROUP);
				if (CollUtil.isNotEmpty(userItems)) {
					for (WxChatUserItem index : userItems) {
						//TODO:lixinji-此处隐藏会话是否绑定-待处理
						Tio.bindGroup(channelContext, index.getBizid() + "");
					}
				}
				ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
				Long callId = cache.get(user.getId() + "_" + devicetype, Long.class);
				if (callId != null) {
					WxCallUtils.endCall(channelContext, callId, user.getId(), WxCallItem.Hanguptype.SYSTEM_RESTART);
					log.error("长链接握手发现未断开视频链接通话：uid:{},callid：{}", userid, callId);
				}
				//极光推送
				if (Const.JPushConfig.OPENFLAG && !isWebsocket && StrUtil.isNotBlank(handshakeReq.getJpushinfo())) {
					WxJpushUser jpushUser = new WxJpushUser();
					jpushUser.setUid(userid);
					jpushUser.setRegid(handshakeReq.getJpushinfo());
					jpushUser.replaceSave();
				}
			}
		}

		imSessionContext.setHandshakeReq(handshakeReq);

		if (imSessionContext.isHandshaked()) {
			ImUtils.rebindGroups(channelContext);
		} else {
			// 检查可能的用户()
			SimpleUser may = null;
			if (user == null) {
				User user1 = LoginLogService.me.selectLastLoginUser(token);
				if (user1 != null) {
					may = SimpleUser.fromUser(user1);
					ImUtils.completeSimpleUser(may, channelContext);
				}
			}
			ImUtils.setHandshakeUser(channelContext, user, may);
		}

		imSessionContext.setHandshaked(true);

		/**
		 * 注意：ws不允许在这里发packet，ws只能通过ImWsMsgHandler.onAfterHandshaked()来发送im的握手
		 */
		if (!isWebsocket) {
			Ims.sendHandshake(channelContext);
		} else {
			//			Devicetype devicetype = ImUtils.getDevicetype(channelContext);
			IpInfo ipinfo = IpInfoService.ME.save(channelContext.getClientNode().getIp());
			ChatroomJoinLeave chatroomJoinLeave = new ChatroomJoinLeave();
			chatroomJoinLeave.setDevice(devicetype.getValue());
			HttpRequest httpRequest = ImUtils.getImSessionContext(channelContext).getWsSessionContext().getHandshakeRequest();
			chatroomJoinLeave.setGroupid(httpRequest.getParam("frompath"));
			if (StrUtil.isBlank(chatroomJoinLeave.getGroupid())) {
				chatroomJoinLeave.setGroupid("/h5/wx/index.html");
			}
			chatroomJoinLeave.setChat(true);
			chatroomJoinLeave.setIpid(ipinfo.getId());
			chatroomJoinLeave.setJointime(new Date());
			chatroomJoinLeave.setSession(ImUtils.getToken(channelContext));
			chatroomJoinLeave.setUid(ImUtils.getUid(channelContext));
			chatroomJoinLeave.setStatus((short) 9);
			chatroomJoinLeave.setServer(Const.MY_IP);
			chatroomJoinLeave.save();
			imSessionContext.setChatroomJoinLeave(chatroomJoinLeave);
		}

		if (user != null) {
			Short exist = (Short) Caches.getCache(CacheConfig.WX_USER_LOGIN_TOKEN_1).get(token);
			if (exist == null) {
				Caches.getCache(CacheConfig.WX_USER_LOGIN_TOKEN_1).put(token, Const.YesOrNo.YES);
				LoginLog loginLog = new LoginLog();
				Date time = new Date();
				IpInfo ipinfo = IpInfoService.ME.save(channelContext.getClientNode().getIp());
				loginLog.setIp(ipinfo.getIp());
				loginLog.setIpid(ipinfo.getId());
				loginLog.setSessionid(token);
				loginLog.setUid(user.getId());
				loginLog.setDevicetype(devicetype.getValue());
				loginLog.setTime(time);
				loginLog.setType(Const.YesOrNo.NO);
				loginLog.setDayperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.DAY));
				loginLog.setTimeperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.TIME));
				loginLog.setHourperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.HOUR));
				if (mobileInfo != null) {
					loginLog.setDeviceinfo("自-" + StringUtils.substring(mobileInfo.getDeviceinfo(), 0, 128));
					loginLog.setImei(mobileInfo.getImei());
					loginLog.setAppversion(mobileInfo.getAppversion());
				} else {
					UserAgent userAgent = currSimpleUser.getUserAgent();
					if (userAgent != null) {
						if (userAgent.getId() != null) {
							loginLog.setUaid(userAgent.getId());
						}
						loginLog.setDeviceinfo(
						        "自-" + userAgent.getOsName() + " " + userAgent.getOsVersion() + "/" + userAgent.getAgentName() + " " + userAgent.getAgentVersionMajor());
					} else {
						loginLog.setDeviceinfo("自-userAgent默认空");
					}
				}
				LoginLogService.me.add(loginLog);
			}
		}
	}
}
