
package org.tio.sitexxx.im.server.handler.wx.chatitem;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.chatitem.WxSessionOperReq;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 会话操作
 * @author lixinji
 * 2020年3月5日 下午9:33:19
 */
@CommandHandler(Command.WxSessionOperReq)
public class WxSessionOperReqHandler extends AbsImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxSessionOperReqHandler.class);

	public static final WxSessionOperReqHandler me = new WxSessionOperReqHandler();

	public WxSessionOperReqHandler() {
	}

	@Override
	public boolean needLogin() {
		return true;
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxSessionOperReq req = Json.toBean(packet.getBodyStr(), WxSessionOperReq.class);
		Short oper = req.getOper();
		if (Objects.equals(Const.SessionOper.LEAVE, oper)) {
			leave(channelContext, curr);
		} else if (Objects.equals(Const.SessionOper.JOIN, oper)) {
			Long chatlinkid = req.getChatlinkid();
			if (chatlinkid == null) {
				ImUtils.info(channelContext, "会话id为空", null);
				return;
			}
			join(channelContext, chatlinkid, curr);
		} else {
			ImUtils.info(channelContext, "无效操作", null);
			return;
		}
	}

	/**
	 * @param channelContext
	 * @param curr
	 * @author lixinji
	 * 2020年3月5日 下午9:41:36
	 */
	private void leave(ChannelContext channelContext, User curr) {
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
		if (Objects.equals(devicetype.getValue(), Devicetype.IOS.getValue()) || Objects.equals(devicetype.getValue(), Devicetype.ANDROID.getValue())) {
			devicetype = Devicetype.APP;
		}
		WxChatQueueApi.leaveFocusQueue(curr, devicetype.getValue(), channelContext.getId());
		//		Ret ret = ChatMsgService.me.leaveChat(curr,devicetype.getValue());
		//		if(ret.isFail()) {
		//			
		//			WxChatApi.sendFriendErrorMsg(channelContext,curr.getId(),curr.getId(), curr.getId(), null, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
		//			return;
		//		}
	}

	/**
	 * @param channelContext
	 * @param chatlinkid
	 * @param curr
	 * @author lixinji
	 * 2020年3月5日 下午9:39:13
	 */
	private void join(ChannelContext channelContext, Long chatlinkid, User curr) {
		Integer uid = curr.getId();
		Short chatmode = Const.ChatMode.P2P;
		Long groupid = null;
		if (chatlinkid <= 0) {
			chatmode = Const.ChatMode.GROUP;
			groupid = -chatlinkid;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
			if (groupItem == null || groupItem.getChatlinkid() == null) {
				ImUtils.info(channelContext, "会话id为空", null);
				return;
			}
			chatlinkid = groupItem.getChatlinkid();
		}
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
		IpInfo ipInfo = IpInfoService.ME.save(channelContext.getClientNode().getIp());
		WxChatQueueApi.joinFocusQueue(channelContext, curr, chatlinkid, groupid, chatmode, devicetype.getValue(), ipInfo.getId());
	}
}
