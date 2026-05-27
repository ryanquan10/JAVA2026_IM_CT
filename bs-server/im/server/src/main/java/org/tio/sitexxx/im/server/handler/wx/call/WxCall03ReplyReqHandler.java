
package org.tio.sitexxx.im.server.handler.wx.call;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall03ReplyReq;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall04ReplyNtf;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCallRespNtf;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.cache.ICache;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.ReadWriteLockHandler;

/**
 * 消息来自b<br>
 * b回复s：同意通话，或拒绝通话（1、同意通话、2、拒接）
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall03ReplyReq)
public class WxCall03ReplyReqHandler extends AbsImServerHandler {
	private static Logger log = LoggerFactory.getLogger(WxCall03ReplyReqHandler.class);

	public static final WxCall03ReplyReqHandler me = new WxCall03ReplyReqHandler();

	public WxCall03ReplyReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		WxCall03ReplyReq req = Json.toBean(packet.getBodyStr(), WxCall03ReplyReq.class);
		WxCallItem wxCallItem = CallItemService.me.getById(req.getId());
		if (wxCallItem == null) {
			return;
		}
		Long callId = wxCallItem.getId();
		try {
			LockUtils.runWriteOrWaitRead(WxCallUtils.LOCK_KEY_PREFIX + "reply" + callId, WxCallUtils.class, new ReadWriteLockHandler() {
				@Override
				public void write() throws Exception {
					//拿到锁后，再获取一遍WxCallItem，防止并发带来的状态不对
					WxCallItem wxCallItem = CallItemService.me.getById(req.getId());
					Short devicetype = ImUtils.getDevicetype(channelContext).getValue();
					if (wxCallItem == null) {
						return;
					}
					if (!Objects.equals(wxCallItem.getStatus(), WxCallItem.Status.BEGIN_CALLING)) {
						WxCallUtils.endCall(channelContext, req.getId(), curr.getId(),WxCallItem.Hanguptype.NORMAL);
						return;
					}
					if (Objects.equals(req.getResult(), (short) 3) && WxChatApi.isManyOnline(curr.getId(), devicetype)) {//对方没有通话的设备
						return;
					}
					ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
					imSessionContext.setCallId(wxCallItem.getId());

					Date date = new Date();
					wxCallItem.setTocid(channelContext.getId());
					wxCallItem.setTodevice(devicetype);
					wxCallItem.setResptime(date);
					wxCallItem.setStatus(WxCallItem.Status.TCP_CONNECTED);
					wxCallItem.setRespwait(date.getTime() - wxCallItem.getCalltime().getTime());
					wxCallItem.setToipid(IpInfoService.ME.save(channelContext.getClientNode().getIp()).getId());
					wxCallItem.update();
					CallItemService.me.clearWxCallItem(wxCallItem.getId());
					String summary = Command.WxCall03ReplyReq.name();
					WxCallUtils.saveCallLog(packet, channelContext, isWebsocket, curr, req, summary);

					String fromcid = wxCallItem.getFromcid();

					WxCall04ReplyNtf ntf = new WxCall04ReplyNtf();
					ntf.fill(wxCallItem);//基础数据透传填充
					ntf.setResult(req.getResult());
					WxCallItem endCallItem = null;
					if (!Objects.equals(req.getResult(), (short) 1)) {//对方不同意通话
						if (StrUtil.isNotBlank(req.getReason())) {
							ntf.setReason(req.getReason());
						} else {
							if (Objects.equals(req.getResult(), (short) 2)) {
								ntf.setReason("对方暂时不方便接听");
							} else if (Objects.equals(req.getResult(), (short) 3)) {
								ntf.setReason("对方没有通话的设备");
								//通用挂断状态：TODO:lixinji-后续扩展状态码
								ntf.setResult((short) 2);
							}
						}

						endCallItem = WxCallUtils.endCall(channelContext, req.getId(), curr.getId(), WxCallItem.Hanguptype.REJECT);
					}
					ImPacket toPacket = new ImPacket(Command.WxCall04ReplyNtf, ntf);
					//这里是发到channelcontextid，而不是user，因为user可能在多端，而channelcontextid就一个
					Tio.sendToId(channelContext.tioConfig, fromcid, toPacket);
					WxCallRespNtf resp = new WxCallRespNtf();
					if (endCallItem != null) {
						resp.fill(endCallItem);
					} else {
						resp.fill(wxCallItem);//基础数据透传填充
					}
					resp.setContextid(channelContext.getId());
					resp.setResult(req.getResult());
					//告诉接收方：我已经拒绝或者接听，其它端都需要进行处理
					resp.setSelf(Const.YesOrNo.YES);
					ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
					cache.put(curr.getId() + "_" + devicetype, req.getId());
					ImPacket selfPacket = new ImPacket(Command.WxCallRespNtf, resp);
					Tio.sendToUser(channelContext.tioConfig, curr.getId() + "", selfPacket);
				}
			});
		} catch (Exception e) {
			log.error("", e);
		}

	}
}
