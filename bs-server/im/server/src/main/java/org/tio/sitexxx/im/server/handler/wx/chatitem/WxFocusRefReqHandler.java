
package org.tio.sitexxx.im.server.handler.wx.chatitem;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.chatitem.WxFocusRefReq;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * 焦点刷新请求，Client-->Server
 * 
 * @author lixinji
 * 2020年8月27日 下午3:14:44
 */
@CommandHandler(Command.WxFocusRefReq)
public class WxFocusRefReqHandler extends AbsImServerHandler {

	private static Logger log = LoggerFactory.getLogger(WxFocusRefReqHandler.class);

	public static final WxFocusRefReqHandler me = new WxFocusRefReqHandler();

	public WxFocusRefReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		if (curr != null) {
			WxFocusRefReq refReq = Json.toBean(packet.getBodyStr(), WxFocusRefReq.class);
			Long chatlinkid = refReq.getChatlinkid();
			if(chatlinkid != null) {
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
			} else {
				Devicetype devicetype = ImUtils.getDevicetype(channelContext);
				try {
					if (Objects.equals(devicetype.getValue(), Devicetype.WEB.getValue())) {
						String key = curr.getId() + "_" + devicetype + "_" + channelContext.getId();
						Long refreshtime = (Long) Caches.getCache(CacheConfig.CHAT_FOCUS_REFRESH_TIME_1).get(key);
						if (refreshtime == null || refreshtime <= 0) {
							refreshtime = DateUtil.offsetMillisecond(new DateTime(), Const.FOCUS_REFRESH_TIMEOUT).getTime();
							Caches.getCache(CacheConfig.CHAT_FOCUS_REFRESH_TIME_1).put(key, refreshtime);
						}
						WxChatQueueApi.refreshFocusQueue(curr, devicetype.getValue(), channelContext.getId());
					} 
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
		}
	}
}
