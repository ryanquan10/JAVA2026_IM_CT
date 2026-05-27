
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
import org.tio.mg.im.common.bs.UpdateTokenReq;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.im.server.Ims;
import org.tio.mg.service.model.main.User;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;

/**
 * 更新token
 * @author tanyaowu
 */
@CommandHandler(Command.UpdateTokenReq)
public class UpdateTokenReqHandler implements ImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UpdateTokenReqHandler.class);

	public UpdateTokenReqHandler() {
	}

	private static final ImPacket updateTokenRespPacket = new ImPacket(Command.UpdateTokenResp);

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		UpdateTokenReq updateTokenReq = Json.toBean(packet.getBodyStr(), UpdateTokenReq.class);

		String newToken = updateTokenReq.getT(); //客户端通过http登录后，服务器返回给客户端的token值，没登录则为空串

		HandshakeReq handshakeReq = ImUtils.getHandshakeReq(channelContext);
		String oldToken = handshakeReq.getToken();

		handshakeReq.setToken(newToken);

		User user = null;
		if (!Objects.equals(newToken, oldToken)) {
			Tio.unbindToken(channelContext);
			Tio.unbindUser(channelContext);
		}

		if (StrUtil.isNotBlank(newToken)) {
			Tio.bindToken(channelContext, newToken);

			Integer userid = null;
			user = ImUtils.getUser(newToken);

			if (user != null) {
				userid = user.getId();
			}

			if (userid != null) {
				Tio.bindUser(channelContext, userid + "");
				//这个是用来统计的，被T后此处的值仍然保留，所以不要用这个值来获取当前通道的用户
				imSessionContext.setUid(userid);
			}
		}

		ImUtils.rebindGroups(channelContext);

		//		UpdateTokenResp updateTokenResp = new UpdateTokenResp();
		//		ImPacket imPacket = new ImPacket(Command.UpdateTokenResp, updateTokenResp);
		Ims.send(channelContext, updateTokenRespPacket);
	}
}
