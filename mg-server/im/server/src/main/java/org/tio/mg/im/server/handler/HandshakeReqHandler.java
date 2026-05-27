
package org.tio.mg.im.server.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.mg.im.common.Command;
import org.tio.mg.im.common.CommandHandler;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.bs.HandshakeReq;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.im.server.Ims;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.LoginLogService;
import org.tio.mg.service.service.base.UserService;
import org.tio.mg.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.utils.json.Json;
import org.tio.websocket.common.util.Md5;

import cn.hutool.core.util.StrUtil;

/**
 * 握手包处理者
 * @author tanyaowu 
 * 2016年9月8日 下午3:59:14
 */
@SuppressWarnings("deprecation")
@CommandHandler(Command.HandshakeReq)
public class HandshakeReqHandler implements ImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(HandshakeReqHandler.class);

	public static final HandshakeReqHandler me = new HandshakeReqHandler();

	public HandshakeReqHandler() {
	}
	//	private static final ImPacket handshakeRespPacket = new ImPacket(Command.HandshakeResp);

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception {

		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		HandshakeReq handshakeReq = null;
		if (isWebsocket) {
			handshakeReq = (HandshakeReq) packet.getBodyObj();
		} else {
			handshakeReq = Json.toBean(packet.getBodyStr(), HandshakeReq.class);
		}

		String token = handshakeReq.getToken(); //客户端通过http登录后，服务器返回给客户端的token值，没登录则为空串
		if (token == null) {
			token = "";
		}
		//		Node serverNode = channelContext.getServerNode();
		if (isWebsocket) {
			//			imSessionContext.setHandshaked(true);
		} else {
			MobileInfo mobileInfo = handshakeReq.getMobileInfo();
			@SuppressWarnings("unused")
			String version = mobileInfo.getAppversion(); //协议版本号
			String imei = mobileInfo.getImei(); //设备id，对于手机就是imei
			String deviceinfo = mobileInfo.getDeviceinfo(); //设备型号
			String cid = mobileInfo.getCid(); //渠道号
			String sign = handshakeReq.getSign(); //签名
			Short devicetypeValue = handshakeReq.getDevicetype();
			Devicetype devicetype = null;
			String mysign = null;
			boolean signOk = false;

			devicetype = Devicetype.from(devicetypeValue);
			mysign = Md5.getMD5(token + imei + deviceinfo + devicetype.getValue() + cid + Const.HANDSHAKE_KEY);
			if (Objects.equals(sign, mysign)) {
				signOk = true;
				mobileInfo.setDevicetype(devicetypeValue);
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

			Integer userid = null;
			user = ImUtils.getUser(token);

			if (user != null) {
				userid = user.getId();
				boolean isSuper = UserService.isSuper(user);
				imSessionContext.setSupper(isSuper);
				
				if (Const.SUPER_MANAGER_LOGINNAME.equals(user.getLoginname())) {//指定的这个超管不显示在用户列表中
					user = null;
				}
			}

			if (userid != null) {
				//				Tio.unbindUser(channelContext.tioConfig, userid + "");

				Tio.bindUser(channelContext, userid + "");

				//这个是用来统计的，被T后此处的值仍然保留，所以不要用这个值来获取当前通道的用户
				imSessionContext.setUid(userid);
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
					//					imSessionContext.setLastLoginSimpleUser(may);
					//					ImUtils.setHandshakeUser(channelContext, user1, true);
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
		}

	}
}
