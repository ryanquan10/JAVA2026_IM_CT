
package org.tio.sitexxx.im.server.handler.wx.call;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall02_2CancelNtf;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall14EndNtf;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.service.vo.wx.WxCallItemVo;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.ReadWriteLockHandler;

import cn.hutool.core.util.StrUtil;

/**
 * 消息来自b<br>
 * b回复s：同意通话，或拒绝通话（1、同意通话、2、拒接）
 * @author tanyaowu 
 * 2020年1月30日 下午9:05:22
 */
@CommandHandler(Command.WxCall02_1CancelReq)
public class WxCall02_1CancelReqHandler extends AbsImServerHandler {
	private static Logger log = LoggerFactory.getLogger(WxCall02_1CancelReqHandler.class);

	public static final WxCall02_1CancelReqHandler me = new WxCall02_1CancelReqHandler();

	public WxCall02_1CancelReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {

		try {
			LockUtils.runWriteOrWaitRead(WxCallUtils.LOCK_KEY_PREFIX + "cancel" + curr.getId(), WxCallUtils.class, new ReadWriteLockHandler() {
				@SuppressWarnings("deprecation")
				@Override
				public void write() throws Exception {
					//					WxCall02_1CancelReq req = Json.toBean(packet.getBodyStr(), WxCall02_1CancelReq.class);
					WxCallItem wxCallItem = CallItemService.me.getCanceledItem(curr.getId());
					if (wxCallItem == null) {
						log.error("取消会话为空");
						return;
					}
					log.error("status-{},{}", wxCallItem.getStatus(), Json.toJson(wxCallItem));
					if (Objects.equals(WxCallItem.Status.BEGIN_CALLING, wxCallItem.getStatus())) {
						WxCallItem newCallItem = WxCallUtils.endCall(channelContext, wxCallItem.getId(), curr.getId(), WxCallItem.Hanguptype.CANCELED);

						//发消息
						WxCall02_2CancelNtf ntf = new WxCall02_2CancelNtf();
						if (newCallItem == null) {
							//此处wxCallItem不是缓存对象，如果后续需要最新的，需要进行缓存处理或者重新获取对象信息
							ntf.fill(wxCallItem);
						} else {
							ntf.fill(newCallItem);//基础数据透传填充
						}
						ImPacket toPacket = new ImPacket(Command.WxCall02_2CancelNtf, ntf);
						Tio.sendToId(TioSiteImServerStarter.tioServerConfigApp, ntf.getFromcid(), toPacket);
						Tio.sendToUser(TioSiteImServerStarter.tioServerConfigApp, ntf.getTouid() + "", toPacket);
					} else {
						try {
							//通话已经结束了
							if (Objects.equals(wxCallItem.getStatus(), WxCallItem.Status.END)) {
								return;
							}
							wxCallItem.setHangupuid(curr.getId());
							wxCallItem.setHanguptype(WxCallItem.Hanguptype.CANCELED);

							Date date = new Date();
							wxCallItem.setStatus(WxCallItem.Status.END);
							wxCallItem.setEndtime(date);
							if (wxCallItem.getConnectedtime() != null) {
								wxCallItem.setCallduration(date.getTime() - wxCallItem.getConnectedtime().getTime());
							}
							//如果是WxCallItem.Hanguptype.CANCELED，则表示双方还没走webrtc流程，此时要特别处理
							//发消息
							WxCall14EndNtf ntf = new WxCall14EndNtf();
							ntf.fill(wxCallItem);//基础数据透传填充
							ImPacket toPacket = new ImPacket(Command.WxCall14EndNtf, ntf);
							Tio.sendToId(TioSiteImServerStarter.tioServerConfigApp, ntf.getFromcid(), toPacket);
							//tocid不一定有（譬如：用户还没接听时，发起方就取消了）
							if (StrUtil.isNotBlank(ntf.getTocid())) {
								Tio.sendToId(TioSiteImServerStarter.tioServerConfigApp, ntf.getTocid(), toPacket);
							} else {
								Tio.sendToUser(TioSiteImServerStarter.tioServerConfigApp, ntf.getTouid() + "", toPacket);
							}
							wxCallItem.update();
							WxCallUtils.clearCallId(wxCallItem.getFromcid());
							WxCallUtils.clearCallId(wxCallItem.getTocid());
							try {
								WxCallItemVo callItemVo = new WxCallItemVo();
								Short contenttype = Const.ContentType.CALL_VIDEO;
								if (Objects.equals(wxCallItem.getType(), WxCallItem.CallType.AUDIO)) {
									contenttype = Const.ContentType.CALL_AUDIO;
								}
								callItemVo.setCalltype(contenttype);
								callItemVo.setDuration(wxCallItem.getCallduration());
								callItemVo.setHanguptype(WxCallItem.Hanguptype.CANCELED);
								callItemVo.setHangupuid(curr.getId());
								callItemVo.setDevicetype(wxCallItem.getTodevice());
								WxChatUserItem userItem = ChatIndexService.fdUserIndex(wxCallItem.getFromuid(), wxCallItem.getTouid());
								if (!ChatService.existTwoFriend(userItem)) {
									log.error("音视频通话异常：好友不存在，uid:{},touid:{},callid:{}", wxCallItem.getFromuid(), wxCallItem.getTouid(), wxCallItem.getId());
									return;
								}
								if (userItem.getChatlinkid() == null) {
									Ret ret = ChatService.me.actFdChatItems(wxCallItem.getFromuid(), wxCallItem.getTouid());
									if (ret.isFail()) {
										log.error("音视频通话异常,会话激活失败：uid:{},touid:{},callid:{}", wxCallItem.getFromuid(), wxCallItem.getTouid(), wxCallItem.getId());
										return;
									}
									if (WxSynApi.isSynVersion()) {
										WxSynApi.synChatSession(wxCallItem.getFromuid(), RetUtils.getOkTData(ret, "chat"), SynRecordVo.SynType.ADD);
									} else {
										WxChatApi.userActOper(wxCallItem.getFromuid(), RetUtils.getOkTData(ret, "chat"));
									}
								}
								WxChatApi.sendFdMsgEach(channelContext, Json.toJson(callItemVo), contenttype, wxCallItem.getFromuid(), wxCallItem.getTouid(), Const.YesOrNo.NO);
							} catch (Exception e) {
								log.error("", e);
							}
						} catch (Exception e) {
							log.error("", e);
						} finally {
							ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
							cache.remove(wxCallItem.getTouid()+"_type");//清除通话类型
							cache.remove(wxCallItem.getTouid()+"_id");//目标用户id
							cache.remove(wxCallItem.getFromuid() + "");
							cache.remove(wxCallItem.getTouid() + "");
							cache.remove(wxCallItem.getFromuid() + "_" + wxCallItem.getFromdevice());
							cache.remove(wxCallItem.getTouid() + "_" + wxCallItem.getTodevice());
							CallItemService.me.clearWxCallItem(wxCallItem.getId());
						}
					}

				}
			});
		} catch (Exception e) {
			log.error("", e);
		}

	}
}
