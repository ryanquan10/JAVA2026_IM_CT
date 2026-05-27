
package org.tio.sitexxx.im.server.handler.wx.call;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.wx.webrtc.WxCall14EndNtf;
import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.model.main.WxCallLog;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.service.chat.CallItemService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.service.vo.wx.WxCallItemVo;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.ReadWriteLockHandler;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2020年2月19日 下午5:44:47
 */
public class WxCallUtils {
	private static Logger		log				= LoggerFactory.getLogger(WxCallUtils.class);
	public static final String	LOCK_KEY_PREFIX	= "wx_call_";

	/**
	 * 
	 * @author tanyaowu
	 */
	public WxCallUtils() {
	}

	/**
	 * 结束所有的通话（系统启动时调用一下本方法，用于保证所有的通话都是结束的）
	 * 
	 * @author tanyaowu
	 */
	public static void endAllCall() {
		String sql = "update wx_call_item set status = ?, hanguptype = ? where status != ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql, WxCallItem.Status.END, WxCallItem.Hanguptype.SYSTEM_RESTART, WxCallItem.Status.END);

		ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
		cache.clear();
		Caches.getCache(CacheConfig.WXCALLITEM_1).clear();
	}

	/**
	 * 把ChannelContext的callId清空
	 * @param channelContextId
	 * @author tanyaowu
	 */
	public static void clearCallId(String channelContextId) {
		if (channelContextId != null) {
			ChannelContext channelContext = Tio.getByChannelContextId(TioSiteImServerStarter.tioServerConfigApp, channelContextId);
			if (channelContext != null) {
				ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
				if (imSessionContext != null) {
					imSessionContext.setCallId(null);
				}
			}
		}
	}

	/**
	 * @param channelContext 触发本方法的channelContext
	 * @param callId id wx_call_item的id
	 * @param hangupuid 挂断一方的uid，如果是系统挂断，则是null
	 * @param hanguptype 参考WxCallItem.Hanguptype
	 * @author tanyaowu
	 */
	public static WxCallItem endCall(ChannelContext channelContext, Long callId, Integer hangupuid, Short hanguptype) {
		WxCallItem wxCallItem = CallItemService.me.getById(callId);
		if (wxCallItem == null) {
			return null;
		}

		try {
			LockUtils.runWriteOrWaitRead(WxCallUtils.LOCK_KEY_PREFIX + "end" + callId, WxCallUtils.class, new ReadWriteLockHandler() {
				@SuppressWarnings("deprecation")
				@Override
				public void write() throws Exception {

					try {
						//通话已经结束了
						if (Objects.equals(wxCallItem.getStatus(), WxCallItem.Status.END)) {
							return;
						}

						wxCallItem.setHangupuid(hangupuid);
						wxCallItem.setHanguptype(hanguptype);

						Date date = new Date();
						wxCallItem.setStatus(WxCallItem.Status.END);
						wxCallItem.setEndtime(date);
						if (wxCallItem.getConnectedtime() != null) {
							wxCallItem.setCallduration(date.getTime() - wxCallItem.getConnectedtime().getTime());
						}

						wxCallItem.update();

						clearCallId(wxCallItem.getFromcid());
						clearCallId(wxCallItem.getTocid());

						//如果是WxCallItem.Hanguptype.CANCELED，则表示双方还没走webrtc流程，此时要特别处理
						if (!Objects.equals(hanguptype, WxCallItem.Hanguptype.CANCELED)) {
							//发消息
							WxCall14EndNtf ntf = new WxCall14EndNtf();
							ntf.fill(wxCallItem);//基础数据透传填充
							ImPacket toPacket = new ImPacket(Command.WxCall14EndNtf, ntf);
							Tio.sendToId(TioSiteImServerStarter.tioServerConfigApp, ntf.getFromcid(), toPacket);
							if (!Objects.equals(hanguptype, WxCallItem.Hanguptype.OTHER_SIDE_CALLING)) {
								//tocid不一定有（譬如：用户还没接听时，发起方就取消了）
								if (StrUtil.isNotBlank(ntf.getTocid())) {
									Tio.sendToId(TioSiteImServerStarter.tioServerConfigApp, ntf.getTocid(), toPacket);
								} else {
									Tio.sendToUser(TioSiteImServerStarter.tioServerConfigApp, ntf.getTouid() + "", toPacket);
								}
							}
						}

						try {
							WxCallItemVo callItemVo = new WxCallItemVo();
							Short contenttype = Const.ContentType.CALL_VIDEO;
							if (Objects.equals(wxCallItem.getType(), WxCallItem.CallType.AUDIO)) {
								contenttype = Const.ContentType.CALL_AUDIO;
							}
							callItemVo.setCalltype(contenttype);
							callItemVo.setDuration(wxCallItem.getCallduration());
							callItemVo.setHanguptype(hanguptype);
							callItemVo.setHangupuid(hangupuid);
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
//							Caches.getCache(CacheConfig.WX_IS_CALLING_TIME).put(wxCallItem.getFromuid() + "", System.currentTimeMillis());
//							Caches.getCache(CacheConfig.WX_IS_CALLING_TIME).put(wxCallItem.getTouid() + "", System.currentTimeMillis());
						} catch (Exception e) {
							log.error("", e);
						}

					} catch (Exception e) {
						log.error("", e);
					} finally {
						ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);
						cache.remove(wxCallItem.getFromuid() + "");
						cache.remove(wxCallItem.getFromuid() + "_" + wxCallItem.getFromdevice());
						if (!Objects.equals(hanguptype, WxCallItem.Hanguptype.OTHER_SIDE_CALLING)) {
							cache.remove(wxCallItem.getTouid() + "");
							cache.remove(wxCallItem.getTouid() + "_type");//清除通话类型
							cache.remove(wxCallItem.getTouid()+"_id");//目标用户id
							cache.remove(wxCallItem.getTouid() + "_" + wxCallItem.getTodevice());
						}
						CallItemService.me.clearWxCallItem(wxCallItem.getId());
						Caches.getCache(CacheConfig.WX_IS_CALLING_EXP).remove(wxCallItem.getId() + "");
					}
				}

			}, 10L);
		} catch (Exception e) {
			log.error("", e);
		} finally {

		}
		return wxCallItem;

	}

	/**
	 * 创建WxCallLog对象
	 * @param packet
	 * @param channelContext
	 * @param isWebsocket
	 * @param curr
	 * @param req
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public static WxCallLog callLog(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, WxCallBase req) throws Exception {
		WxCallLog wxCallLog = WxCallLog.from(req);
		wxCallLog.setData(packet.getBodyStr());
		wxCallLog.setUid(curr.getId());
		return wxCallLog;
	}

	/**
	 * 保存通话日志
	 * @param packet
	 * @param channelContext
	 * @param isWebsocket
	 * @param curr
	 * @param req
	 * @param summary
	 * @return
	 * @author tanyaowu
	 */
	public static boolean saveCallLog(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, WxCallBase req, String summary) {
		try {
			WxCallLog wxCallLog = WxCallUtils.callLog(packet, channelContext, isWebsocket, curr, req);
			wxCallLog.setSummary(summary);
			return wxCallLog.save();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public static void updateItem(WxCallItem wxCallItem) {
		Long callId = wxCallItem.getId();
		Object lockobj = LockUtils.getLockObj(WxCallUtils.LOCK_KEY_PREFIX + "update" + callId, WxCallUtils.class);
		synchronized (lockobj) {
			wxCallItem.update();
		}
		CallItemService.me.clearWxCallItem(wxCallItem.getId());
	}
}
