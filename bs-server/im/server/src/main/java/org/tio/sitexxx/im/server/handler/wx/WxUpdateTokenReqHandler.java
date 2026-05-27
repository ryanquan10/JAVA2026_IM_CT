
package org.tio.sitexxx.im.server.handler.wx;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.HandshakeReq;
import org.tio.sitexxx.im.common.bs.wx.WxUpdateTokenReq;
import org.tio.sitexxx.im.common.bs.wx.WxUpdateTokenResp;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.hutool.CollUtil;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;

/**
 * 更新token
 * @author tanyaowu
 */
@CommandHandler(Command.WxUpdateTokenReq)
public class WxUpdateTokenReqHandler extends AbsImServerHandler {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxUpdateTokenReqHandler.class);

	public WxUpdateTokenReqHandler() {
	}

	public boolean needLogin() {
		return false;
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		WxUpdateTokenReq updateTokenReq = Json.toBean(packet.getBodyStr(), WxUpdateTokenReq.class);

		String newToken = updateTokenReq.getT(); //客户端通过http登录后，服务器返回给客户端的token值，没登录则为空串

		//		String oldparams = updateTokenReq.getO();
		HandshakeReq handshakeReq = ImUtils.getHandshakeReq(channelContext);
		String oldToken = handshakeReq.getToken();

		handshakeReq.setToken(newToken);

		User user = null;
		if (!Objects.equals(newToken, oldToken)) {
			Tio.unbindToken(channelContext);
			Tio.unbindUser(channelContext);
		}

		if (StrUtil.isNotBlank(newToken)) {
			Tio.unbindGroup(channelContext);
			Tio.bindToken(channelContext, newToken);
			Integer userid = null;
			user = ImUtils.getUser(newToken);

			if (user != null) {
				userid = user.getId();
			} else {
				org.tio.sitexxx.im.server.utils.ImUtils.pleaseLogin(channelContext, "重新绑定未登录token，请断开连接");
				Tio.close(channelContext, "重新绑定未登录token，请断开连接");
				return;
			}

			if (userid != null) {
				Tio.bindUser(channelContext, userid + "");
				//这个是用来统计的，被T后此处的值仍然保留，所以不要用这个值来获取当前通道的用户
				imSessionContext.setUid(userid);
				//查询用户所在的群组，逐个绑定，以便后面的群发
				List<WxChatUserItem> userItems = ChatIndexService.me.getLinkActUserIndex(userid, Const.ChatMode.GROUP);
				if (CollUtil.isNotEmpty(userItems)) {
					for (WxChatUserItem index : userItems) {
						Tio.bindGroup(channelContext, index.getBizid() + "");
					}
				}
			}
		}

		ImUtils.rebindGroups(channelContext);
		WxUpdateTokenResp updateTokenResp = new WxUpdateTokenResp();
		ImPacket imPacket = new ImPacket(Command.WxUpdateTokenResp, updateTokenResp);
		Ims.send(channelContext, imPacket);
	}
}
