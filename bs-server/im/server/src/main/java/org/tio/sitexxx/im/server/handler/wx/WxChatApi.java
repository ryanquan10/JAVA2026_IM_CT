
package org.tio.sitexxx.im.server.handler.wx;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendChatNtf;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendErrorNtf;
import org.tio.sitexxx.im.common.bs.wx.friend.WxUserOperNtf;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupChatNtf;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupOperNtf;
import org.tio.sitexxx.im.common.bs.wx.sys.WxFocusNtf;
import org.tio.sitexxx.im.common.bs.wx.sys.WxUserSysNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.group.WxChatGroupNtfApi;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.service.chat.ChatMsgService.MsgTemplate;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxGroupApplyVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.sitexxx.service.vo.wx.WxTemplateMsgVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.cache.redis.RedisCache;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.SetWithLock;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import org.tio.utils.resp.Resp;

/**
 * 业务调用wx的API
 * @author lixinji
 * 2020年1月17日 下午2:25:27
 */
/**
 * 
 * @author lixinji
 * 2020年8月18日 下午4:39:15
 */
public class WxChatApi {

	private static Logger log = LoggerFactory.getLogger(WxChatApi.class);

	public static final WxChatApi me = new WxChatApi();

	/**
	 * TODO-lixinji-750-撤回消息synGroupMsgBakOrDel()/synP2pMsgBakOrDel
	 * 1、撤回消息该为--synGroupMsgBakOrDel
	 * 撤回消息
	 * 消息数量会少 
	 * TODO:lixinji-该逻辑可进行优化，消息不做新增处理，进行修改处理，可与客户端进行同步处理
	 * @param request
	 * @param devicetype
	 * @param chatlinkid
	 * @param chatmode
	 * @param msg
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月11日 下午5:36:40
	 */
	public static boolean backMsg(HttpRequest request, User user, Long chatlinkid, Short chatmode, Object msg) throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenBackMsgNotify'");
		if (Objects.equals(Const.ChatMode.GROUP, chatmode)) {
			WxGroupMsg groupMsg = (WxGroupMsg) msg;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(chatlinkid);
			SysMsgVo sysMsgVo = new SysMsgVo(user.getNick(), MsgTemplate.msgback1, "", "msgback");
//			if (!Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER) && !Objects.equals(groupMsg.getUid(), user.getId())) {
//				sysMsgVo = new SysMsgVo(user.getNick(), MsgTemplate.managermsgback, "", "managermsgback");
//			}
			sysMsgVo.setOpercode(Const.WxGroupOper.BACK_MSG);
			sysMsgVo.setSrctext(groupMsg.getSrctext());
			sysMsgVo.setSrcmsgtype(groupMsg.getContenttype());
			if (WxSynApi.isSynVersion()) {
				WxSynApi.synGroupMsgBak(groupItem.getGroupid(), groupMsg.getId());
			} else {
				WxGroupOperNtf groupOper = new WxGroupOperNtf();
				groupOper.setOper(Const.WxGroupOper.BACK_MSG);
				groupOper.setChatlinkid(-groupItem.getGroupid());
				groupOper.setG(groupItem.getGroupid());
				groupOper.setBizdata(groupMsg.getId() + "");
				//发送操作消息
				ImPacket operPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
				Ims.sendToGroup(groupItem.getGroupid(), operPacket);
			}
			//发送消息
			if (clientConf.getValue().equals(1)) {
				sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, user.getId(), groupItem.getGroupid(), Const.YesOrNo.YES, sysMsgVo, null, null,
						groupMsg.getId() + "");

//                WxGroup group = GroupService.me.getByGroupid(groupItem.getGroupid());
//                //群主发送消息
//                WxChatGroupItem operGroupItem = ChatIndexService.chatGroupIndex(group.getUid(), groupItem.getGroupid());
//                if (operGroupItem == null) {
//                    log.error("群主群索引为空，uid:{},groupid:{}", operGroupItem.getUid(), operGroupItem.getGroupid());
//                    return false;
//                }
//                Long chatlinkidop = operGroupItem.getChatlinkid();
//                if (chatlinkidop == null) {
//                    Ret actRet = ChatService.me.actGroupChatItems(operGroupItem.getGroupid(), operGroupItem.getUid());
//                    if (actRet.isFail()) {
//                        log.error("会话激活失败-群聊，uid:{},groupid:{}", operGroupItem.getUid(), operGroupItem.getGroupid());
//                        return false;
//                    } else {
//                        if (WxSynApi.isSynVersion()) {
//                            WxSynApi.synChatSession(operGroupItem.getUid(), RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
//                        } else {
//                            WxChatApi.userActOper(operGroupItem.getUid(), RetUtils.getOkTData(actRet, "chat"));
//                        }
//                    }
//                    chatlinkidop = RetUtils.getOkTData(actRet, "chatlinkid");
//                }

//                sendGroupMsgOnly(request, sysMsgVo.toText(), Const.ContentType.TEXT, user.getId(), group.getUid(), groupItem.getGroupid(), chatlinkidop, Const.YesOrNo.NO, null);
//                List<WxChatGroupItem> managerList = ChatIndexService.me.getManagerGroupIndex(groupItem.getGroupid());
//                if (CollectionUtil.isNotEmpty(managerList)) {
//                    for (WxChatGroupItem manger : managerList) {
//                        Long mangerChatlinkId = manger.getChatlinkid();
//                        if (mangerChatlinkId == null) {
//                            Ret actRet = ChatService.me.actGroupChatItems(groupItem.getGroupid(), manger.getUid());
//                            if (actRet.isFail()) {
//                                log.error("会话激活失败-群聊，uid:{},groupid:{}", manger.getUid(), groupItem.getGroupid());
//                                return false;
//                            } else {
//                                if (WxSynApi.isSynVersion()) {
//                                    WxSynApi.synChatSession(manger.getUid(), RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
//                                } else {
//                                    WxChatApi.userActOper(manger.getUid(), RetUtils.getOkTData(actRet, "chat"));
//                                }
//                            }
//                            mangerChatlinkId = RetUtils.getOkTData(actRet, "chatlinkid");
//                        }
//                        //管理员发送消息
//                        sendGroupMsgOnly(request, sysMsgVo.toText(), Const.ContentType.TEXT, user.getId(), manger.getUid(), groupItem.getGroupid(), mangerChatlinkId, Const.YesOrNo.NO, null);
//                    }
//                }
			}
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			SysMsgVo sysMsgVo = new SysMsgVo(user.getNick(), MsgTemplate.msgback, "", "msgback");
			WxFriendMsg friendMsg = (WxFriendMsg) msg;
			sysMsgVo.setOpercode(Const.WxMsgOper.BACK);
			sysMsgVo.setSrcmsgtype(friendMsg.getContenttype());
			sysMsgVo.setSrctext(friendMsg.getSrctext());
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			if (WxSynApi.isSynVersion()) {
				WxSynApi.synP2pMsgBak(user.getId(), chatlinkid, friendMsg.getId());
				if (userItem.getTochatlinkid() != null) {
					WxSynApi.synP2pMsgBak(userItem.getBizid().intValue(), userItem.getTochatlinkid(), friendMsg.getId());
				}
			} else {
				userChatOper(request, user.getId(), chatlinkid, Const.WxUserOper.BACK_MSG, "你撤回消息", friendMsg.getId() + "", null);
				if (userItem.getTochatlinkid() != null) {
					userChatOper(request, userItem.getBizid().intValue(), userItem.getTochatlinkid(), Const.WxUserOper.BACK_MSG, "好友撤回消息", friendMsg.getId() + "", null);
				}
			}
			if (clientConf.getValue().equals(1)) {
				sendFdMsgEach(devicetype, sessionid, ip, sysMsgVo.toText(), Const.ContentType.TEXT, user.getId(), userItem.getBizid().intValue(), Const.YesOrNo.YES,
						friendMsg.getId() + "", null, null, appversion, sysMsgVo);
			}
		}

		return true;
	}

	/**
	 * TODO-lixinji-750-删除消息通知-synGroupMsgDel
	 * 删除消息通知
	 * @param request
	 * @param devicetype
	 * @param user
	 * @param chatlinkid
	 * @param chatmode
	 * @param msg
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年4月1日 上午10:42:53
	 */
	public static boolean delMsg(HttpRequest request, User user, Long chatlinkid, Short chatmode, String mids, WxChatItems chatItems) throws Exception {
		if (WxSynApi.isSynVersion()) {
			if (Objects.equals(Const.ChatMode.GROUP, chatmode)) {
				WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
				WxSynApi.synGroupMsgDel(user.getId(), userItem.getBizid(), mids, chatItems);
			} else {
				WxSynApi.synP2pMsgDel(user.getId(), chatlinkid, mids, chatItems);
			}
			return true;
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			if (Objects.equals(Const.ChatMode.GROUP, chatmode)) {
				WxGroupOperNtf groupOper = new WxGroupOperNtf();
				groupOper.setOper(Const.WxGroupOper.DEL_MSG);
				groupOper.setChatlinkid(-userItem.getBizid());
				groupOper.setG(userItem.getBizid());
				groupOper.setBizdata(mids);
				if (chatItems != null) {
					chatItems.setId(-userItem.getBizid());
					chatItems.setChatlinkid(-userItem.getBizid());
					groupOper.setChatItems(chatItems);
				}
				//发送操作消息
				ImPacket operPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
				Ims.sendToUser(userItem.getUid(), operPacket);
			} else {
				if (chatItems != null) {
					chatItems.setChatlinkid(chatItems.getId());
				}
				userChatOper(request, user.getId(), chatlinkid, Const.WxUserOper.DEL_MSG, "你删除了消息", mids, chatItems);
			}
			return true;
		}
	}

	/**
	 * TODO-lixinji-750-修改群信息通知
	 * 修改群信息通知
	 * @param request
	 * @param devicetype
	 * @param user
	 * @param groupid
	 * @param group
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月13日 下午1:55:12
	 */
	public static Ret updateGroupInfo(HttpRequest request, User user, Long groupid, String name) throws Exception {
		SysMsgVo sysMsgVo = new SysMsgVo(user.getNick(), MsgTemplate.updatename, name, "updatename");
		//发送操作通知
		WxGroupOperNtf ret = new WxGroupOperNtf();
		ret.setC(sysMsgVo.toText());
		ret.setMid(null);
		ret.setT(System.currentTimeMillis());
		ret.setUid(user.getId());
		ret.setG(groupid);
		ret.setChatlinkid(-groupid);
		ret.setBizdata(name);
		WxChatItems chatItems = new WxChatItems();
		chatItems.setChatlinkid(-groupid);
		chatItems.setId(-groupid);
		chatItems.setName(name);
		ret.setChatItems(chatItems);
		ret.setOper(Const.WxGroupOper.UPDATE_GROUP_NAME);
		ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
		Ims.sendToGroup(groupid, imPacket);
		Ret sendRet = sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, user.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
		List<WxChatGroupItem> linkGroupItems = ChatIndexService.me.getGroupLinkItems(groupid);
		if (CollectionUtil.isNotEmpty(linkGroupItems)) {
			//清除缓存
			for (WxChatGroupItem groupItem : linkGroupItems) {
				ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
				ChatIndexService.clearMailListCache(groupItem.getUid());
			}
		}
		return sendRet;
	}

	/**
	 * @param request
	 * @param user
	 * @param groupid
	 * @param avatar
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年1月13日 下午7:07:37
	 */
	public static Ret updateGroupAvatar(HttpRequest request, User user, Long groupid, String avatar) throws Exception {
		//发送操作通知
		WxGroupOperNtf ret = new WxGroupOperNtf();
		ret.setC("修改群头像");
		ret.setMid(null);
		ret.setT(System.currentTimeMillis());
		ret.setUid(user.getId());
		ret.setG(groupid);
		ret.setChatlinkid(-groupid);
		ret.setBizdata(avatar);
		WxChatItems chatItems = new WxChatItems();
		chatItems.setChatlinkid(-groupid);
		chatItems.setId(-groupid);
		chatItems.setAvatar(avatar);
		ret.setChatItems(chatItems);
		ret.setOper(Const.WxGroupOper.UPDATE_GROUP_AVATAR);
		ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
		Ims.sendToGroup(groupid, imPacket);
		List<WxChatGroupItem> linkGroupItems = ChatIndexService.me.getGroupLinkItems(groupid);
		if (CollectionUtil.isNotEmpty(linkGroupItems)) {
			//清除缓存
			for (WxChatGroupItem groupItem : linkGroupItems) {
				ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
				ChatIndexService.clearMailListCache(groupItem.getUid());
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * 自动群操作通知
	 * TODO:lixinji-750-群信息变更
	 * @param request
	 * @param devicetype
	 * @param user
	 * @param groupid
	 * @param name
	 * @param avatar
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月13日 下午5:03:58
	 */
	public static void autoUpdateGroupInfo(HttpRequest request, Short devicetype, User user, Long groupid, String name, String avatar, boolean cacheClearFlag) throws Exception {
		String text = user.getNick();
		Map<String, String> map = new HashMap<String, String>();
		WxChatItems chatItems = new WxChatItems();
		chatItems.setChatlinkid(-groupid);
		chatItems.setId(-groupid);
		if (StrUtil.isNotBlank(name)) {
			text = text + " 自动变更群名为\"" + name + "\"";
			map.put("name", name);
			chatItems.setName(name);
		}
		if (StrUtil.isNotBlank(name) && StrUtil.isNotBlank(avatar)) {
			map.put("avatar", avatar);
			text = text + ",自动变更群头像为\"" + avatar + "\"";
			chatItems.setAvatar(avatar);
		} else if (StrUtil.isNotBlank(avatar)) {
			map.put("avatar", avatar);
			text = text + " 自动变更群头像为\"" + avatar + "\"";
			chatItems.setAvatar(avatar);
		}
		//发送操作通知
		WxGroupOperNtf ret = new WxGroupOperNtf();
		ret.setC(text);
		ret.setMid(null);
		ret.setT(System.currentTimeMillis());
		ret.setUid(user.getId());
		ret.setG(groupid);
		ret.setChatlinkid(-groupid);
		ret.setBizdata(Json.toJson(map));
		ret.setOper(Const.WxGroupOper.AUTO_UPDATE_INFO);
		ret.setChatItems(chatItems);
		ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
		Ims.sendToGroup(groupid, imPacket);
		if (cacheClearFlag) {
			List<WxChatGroupItem> linkGroupItems = ChatIndexService.me.getGroupLinkItems(groupid);
			if (CollectionUtil.isNotEmpty(linkGroupItems)) {
				//清除缓存
				for (WxChatGroupItem groupItem : linkGroupItems) {
					ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
					ChatIndexService.clearMailListCache(groupItem.getUid());
				}
			}
		}
	}

	/**
	 * 消息转发通知-已调整
	 * @param request
	 * @param devicetype
	 * @param user
	 * @param chatlinkid
	 * @param chatmode
	 * @param mids
	 * @param groupids
	 * @param uids
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月12日 下午5:57:57
	 */
	public static Ret msgForward(HttpRequest request, User user, Short chatmode, String mids, String groupids, String uids) throws Exception {
		if (StrUtil.isBlank(mids)) {
			return RetUtils.failMsg("转发消息为空");
		}
		if (StrUtil.isBlank(uids) && StrUtil.isBlank(groupids)) {
			return RetUtils.failMsg("转发目标为空");
		}
		Integer uid = user.getId();
		String[] midArr = mids.split(",");
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
						p2pMsgForward(request, sessionid, ip, uid, chatmode, midArr, groupids, uids);
					} else {
						groupMsgForward(request, sessionid, ip, uid, chatmode, midArr, groupids, uids);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return RetUtils.okOper();
	}

	/**
	 * 共享群名片通知-已调整
	 * @param request
	 * @param devicetype
	 * @param user
	 * @param cardid
	 * @param chatmode
	 * @param groupids
	 * @param groupids
	 * @param uids
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年4月1日 下午2:46:35
	 */
	public static Ret sharChard(HttpRequest request, User user, Long cardid, Short chatmode, String groupids, String uids) throws Exception {
		if (cardid == null || chatmode == null) {
			return RetUtils.invalidParam();
		}
		if (StrUtil.isBlank(uids) && StrUtil.isBlank(groupids)) {
			return RetUtils.failMsg("分享目标为空");
		}
		Integer uid = user.getId();
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					Ret cardRet = null;
					if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
						cardRet = FriendService.me.getFdCard(uid, cardid.intValue());
					} else {
						cardRet = GroupService.me.getGroupCard(uid, cardid);
					}
					if (cardRet.isFail()) {
						WxChatApi.sendFriendErrorMsg(request, uid, uid, uid, null, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(cardRet));
						return;
					}
					WxMsgCardVo cardVo = RetUtils.getOkTData(cardRet);
					Short contenttype = Const.ContentType.MSG_CARD;
					if (StrUtil.isNotBlank(uids)) {//分享到好友
						String[] uidArr = uids.split(",");
						for (String uidStr : uidArr) {
							Integer otherUid = Integer.parseInt(uidStr);
							WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, otherUid);
							if (ChatService.existTwoFriend(userItem)) {//判断是否互相为好友
								if (userItem.getChatlinkid() == null) {
									Ret actRet = ChatService.me.actFdChatItems(uid, otherUid);
									if (actRet.isFail()) {//激活失败
										WxChatApi.sendFriendErrorMsg(request, uid, uid, uid, null, AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(actRet));
										continue;
									}
									WxChatItems chatItems = RetUtils.getOkTData(actRet, "chat");
									if (WxSynApi.isSynVersion()) {
										WxSynApi.synChatSession(uid, chatItems, SynRecordVo.SynType.ADD);
									} else {
										WxChatApi.userActOper(request, uid, chatItems);
									}
								}
								//发送消息
								cardVo.setShareToBizid(new Long(otherUid));
								String text = Json.toJson(cardVo);
								Ret ret = WxChatApi.sendFdMsgEach(request, text, contenttype, uid, otherUid, Const.YesOrNo.NO);
								if (ret.isFail()) {
									WxChatApi.sendFriendErrorMsg(request, uid, uid, uid, null, RetUtils.getIntCode(ret), RetUtils.getRetMsg(ret));
								}
							}
						}
					}
					if (StrUtil.isNotBlank(groupids)) {
						String[] groupArr = groupids.split(",");
						for (String groupStr : groupArr) {
							Long groupid = Long.parseLong(groupStr);
							WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
							if (ChatService.groupChatLink(groupItem)) {
								cardVo.setShareToBizid(groupid);
								String c = Json.toJson(cardVo);
								Ret ret = WxChatApi.sendGroupMsgEach(request, c, contenttype, uid, groupid, Const.YesOrNo.NO, null, null, null);
								if (ret.isFail()) {
									WxChatApi.sendFriendErrorMsg(request, uid, uid, uid, null, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
								}
							}
						}
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return RetUtils.okOper();
	}

	/**
	 * 私聊消息转发通知-已调整
	 * @param request
	 * @param sessionid
	 * @param ip
	 * @param devicetype
	 * @param uid
	 * @param chatmode
	 * @param midArr
	 * @param groupids
	 * @param uids
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月12日 下午7:18:45
	 */
	public static void p2pMsgForward(HttpRequest request, String sessionid, String ip, Integer uid, Short chatmode, String[] midArr, String groupids, String uids) throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		for (String midStr : midArr) {
			Long mid = Long.parseLong(midStr);
			WxFriendMsg msg = WxFriendMsg.dao.findById(mid);
			if (msg == null) {
				log.error("分享消息异常：消息不存在，uid:{},mid:{},chatmode:{}", uid, mid, chatmode);
				continue;
			}
			//分享到好友
			if (StrUtil.isNotBlank(uids)) {
				String[] uidArr = uids.split(",");
				for (String uidStr : uidArr) {
					Integer otherUid = Integer.parseInt(uidStr);
					WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, otherUid);
					if (!ChatService.existTwoFriend(userItem)) {
						log.error("分享好友消息异常：好友不存在，uid:{},touid:{}", uid, otherUid);
						continue;
					}
					if (userItem.getChatlinkid() == null) {
						Ret ret = ChatService.me.actFdChatItems(uid, otherUid);
						if (ret.isFail()) {
							log.error("分享好友消息异常：自己的会话激活失败，uid:{},touid:{}", uid, otherUid);
							continue;
						}
						if (WxSynApi.isSynVersion()) {
							WxSynApi.synChatSession(uid, RetUtils.getOkTData(ret, "chat"), SynRecordVo.SynType.ADD);
						} else {
							WxChatApi.userActOper(request, uid, RetUtils.getOkTData(ret, "chat"));
						}
					}
					sendFdMsgEach(devicetype, sessionid, ip, msg.getText(), msg.getContenttype(), uid, otherUid, msg.getSendbysys(), "", msg.getId(), Const.ChatMode.P2P,
					        appversion,null);
				}
			}
			if (StrUtil.isNotBlank(groupids)) {
				String[] groupArr = groupids.split(",");
				for (String groupStr : groupArr) {
					Long groupid = Long.parseLong(groupStr);
					Ret check = GroupService.checkGroupMsg(groupid, uid);
					if (check.isFail()) {
						log.error("分享到群消息异常：{}，uid:{},groupid:{}",RetUtils.getRetMsg(check), uid, groupid);
						continue;
					}
					sendGroupMsgEach(request, msg.getText(), msg.getContenttype(), uid, groupid, msg.getSendbysys(), null, msg.getId(), Const.ChatMode.P2P, "");
				}
			}
		}
	}

	/**
	 * 收藏转发
	 * @param request
	 * @param sessionid
	 * @param ip
	 * @param uid
	 * @param fidArr
	 * @param groupids
	 * @param uids
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年1月26日 下午5:41:15
	 */
	public static Ret favoriteForward(HttpRequest request, String sessionid, String ip, Integer uid, String[] fidArr, String groupids, String uids) throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		for (String fidstr : fidArr) {
			Integer fid = Integer.parseInt(fidstr);
			WxUserFavorites favorite = WxUserFavorites.dao.findById(fid);
			if (favorite == null) {
				log.error("分享收藏信息异常：收藏不存在，uid:{},fid:{}", uid, fid);
				continue;
			}
			//分享到好友
			if (StrUtil.isNotBlank(uids)) {
				String[] uidArr = uids.split(",");
				for (String uidStr : uidArr) {
					Integer otherUid = Integer.parseInt(uidStr);
					WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, otherUid);
					if (!ChatService.existTwoFriend(userItem)) {
						log.error("收藏转发消息异常：好友不存在，uid:{},touid:{}", uid, otherUid);
						continue;
					}
					if (userItem.getChatlinkid() == null) {
						Ret ret = ChatService.me.actFdChatItems(uid, otherUid);
						if (ret.isFail()) {
							log.error("收藏转发消息异常：自己的会话激活失败，uid:{},touid:{}", uid, otherUid);
							continue;
						}
						if (WxSynApi.isSynVersion()) {
							WxSynApi.synChatSession(uid, RetUtils.getOkTData(ret, "chat"), SynRecordVo.SynType.ADD);
						} else {
							WxChatApi.userActOper(request, uid, RetUtils.getOkTData(ret, "chat"));
						}
					}
					sendFdMsgEach(devicetype, sessionid, ip, favorite.getMsgtext(), favorite.getType(), uid, otherUid, Const.YesOrNo.NO, "", null, null, appversion,null);
				}
			}
			if (StrUtil.isNotBlank(groupids)) {
				String[] groupArr = groupids.split(",");
				for (String groupStr : groupArr) {
					Long groupid = Long.parseLong(groupStr);
					Ret check = GroupService.checkGroupMsg(groupid, uid);
					if (check.isFail()) {
						log.error("分享到群消息异常：{}，uid:{},groupid:{}",RetUtils.getRetMsg(check), uid, groupid);
						continue;
					}
					sendGroupMsgEach(request, favorite.getMsgtext(), favorite.getType(), uid, groupid, Const.YesOrNo.NO, null, null, null, "");
				}
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * 群聊消息转发通知-已调整
	 * @param request
	 * @param sessionid
	 * @param ip
	 * @param devicetype
	 * @param uid
	 * @param chatmode
	 * @param midArr
	 * @param groupids
	 * @param uids
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月12日 下午7:18:49
	 */
	public static void groupMsgForward(HttpRequest request, String sessionid, String ip, Integer uid, Short chatmode, String[] midArr, String groupids, String uids)
	        throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		for (String midStr : midArr) {
			Long mid = Long.parseLong(midStr);
			WxGroupMsg msg = WxGroupMsg.dao.findById(mid);
			if (msg == null) {
				log.error("分享消息异常：消息不存在，uid:{},mid:{},chatmode:{}", uid, mid, chatmode);
				continue;
			}
			//分享到好友
			if (StrUtil.isNotBlank(uids)) {
				String[] uidArr = uids.split(",");
				for (String uidStr : uidArr) {
					Integer otherUid = Integer.parseInt(uidStr);
					WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, otherUid);
					if (!ChatService.existTwoFriend(userItem)) {
						log.error("分享好友消息异常：好友不存在，uid:{},touid:{}", uid, otherUid);
						continue;
					}
					if (userItem.getChatlinkid() == null) {
						Ret ret = ChatService.me.actFdChatItems(uid, otherUid);
						if (ret.isFail()) {
							log.error("分享好友消息异常：自己的会话激活失败，uid:{},touid:{}", uid, otherUid);
							continue;
						}
						if (WxSynApi.isSynVersion()) {
							WxSynApi.synChatSession(uid, RetUtils.getOkTData(ret, "chat"), SynRecordVo.SynType.ADD);
						} else {
							WxChatApi.userActOper(request, uid, RetUtils.getOkTData(ret, "chat"));
						}
					}
					sendFdMsgEach(devicetype, sessionid, ip, msg.getText(), msg.getContenttype(), uid, otherUid, msg.getSendbysys(), "", msg.getId(), Const.ChatMode.P2P,
					        appversion,null);
				}
			}
			if (StrUtil.isNotBlank(groupids)) {
				String[] groupArr = groupids.split(",");
				for (String groupStr : groupArr) {
					Long groupid = Long.parseLong(groupStr);
					Ret check = GroupService.checkGroupMsg(groupid, uid);
					if (check.isFail()) {
						log.error("分享到群消息异常：{}，uid:{},groupid:{}",RetUtils.getRetMsg(check), uid, groupid);
						continue;
					}
					sendGroupMsgEach(request, msg.getText(), msg.getContenttype(), uid, groupid, msg.getSendbysys(), null, msg.getId(), Const.ChatMode.P2P, "");
				}
			}
		}
	}

	/**
	 * 好友通讯录发生变更通知-已调整
	 * @param request
	 * @param devicetype
	 * @param friend
	 * @param toFriend
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午10:20:06
	 */
	public static void friendChangeAddNtf(HttpRequest request, WxFriend friend, WxFriend toFriend) {
		if (WxSynApi.isSynVersion()) {
			if (friend != null) {
				WxSynApi.synFriend(friend.getUid(), friend, SynRecordVo.SynType.ADD);
			}
			if (toFriend != null) {
				WxSynApi.synFriend(toFriend.getUid(), toFriend, SynRecordVo.SynType.ADD);
			}
		} else {
			if (friend != null) {
				WxChatApi.useSysChatNtf(request, friend.getUid(), Const.WxSysCode.FRIEND_CHANGE_ADD, "好友发生变更-新增，好友关系id：" + friend.getId(), Json.toJson(friend));
			}
			if (toFriend != null) {
				WxChatApi.useSysChatNtf(request, toFriend.getUid(), Const.WxSysCode.FRIEND_CHANGE_ADD, "好友发生变更-新增，好友关系id：" + toFriend.getId(), Json.toJson(toFriend));
			}
		}
	}

	/**
	 * 好像删除变更通知
	 * @param request
	 * @param devicetype
	 * @param friend
	 * @author lixinji
	 * 2020年3月2日 下午9:50:44
	 */
	@Deprecated
	public static void friendChangeDelNtf(HttpRequest request, Integer uid, Long fid) {
		//		WxFriend friend = FriendService.me.getFriendInfo(fid);
		//		if(friend == null) {
		//			return;
		//		}
		WxChatApi.useSysChatNtf(request, uid, Const.WxSysCode.FRIEND_CHANGE_DEL, "好友发生变更-删除，好友关系id：" + fid, fid + "");
	}

	/**
	 * 信息修改
	 * @param request
	 * @param devicetype
	 * @param uid
	 * @param fid
	 * @author lixinji
	 * 2020年3月10日 下午1:47:29
	 */
	@Deprecated
	public static void friendInfoChangeNtf(HttpRequest request, Integer uid, Long fid) {
		WxFriend friend = FriendService.me.getFriendInfo(fid);
		WxChatApi.useSysChatNtf(request, uid, Const.WxSysCode.FRIEND_INFO_UPDATE, "好友发生变更-信息修改，好友关系id：" + fid, Json.toJson(friend));
	}

	/**
	 * 审核通过后的互相加好友-已调整
	 * -上层处理逻辑-调用请勿开启事务处理
	 * @param request 审核request
	 * @param devicetype
	 * @param uid 邀请者
	 * @param touid 被邀请者
	 * @param chatlinkid 邀请的聊天会话
	 * @param toChatLinkid 被邀请的聊天会话
	 * @param greet 招呼语
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月17日 下午1:32:12
	 */
	public static boolean addFriendEachOfPassApply(HttpRequest request, Integer uid, Integer touid, Long chatlinkid, Long toChatLinkid, String greet, boolean isExist)
	        throws Exception {
		User touser = UserService.ME.getById(touid);
		String sessionid = request.getHttpSession().getId();
		User user = UserService.ME.getById(uid);
		String ip = request.getClientIp();
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				//1、发送验证通过单通道消息
				String text = "我通过了你的朋友验证请求，现在我们可以开始聊天了";
				WxFriendMsg passMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, touid, uid, uid, null, appversion);
				if (passMsg == null) {
					failRet("保存验证消息失败");
					return false;
				}
				passMsg.setNick(touser.getNick());
				passMsg.setNick(touser.getAvatar());
				retObj = Ret.ok();
				WxFriendChatNtf passNtf = WxFriendChatNtf.from(passMsg);
				passNtf.setChatlinkid(chatlinkid);
				if (!isExist) {
					passNtf.setActflag(Const.YesOrNo.YES);
				}
				passNtf.setNick(touser.getNick());
				passNtf.setAvatar(touser.getAvatar());
				passNtf.setActavatar(touser.getAvatar());
				passNtf.setActname(touser.getNick());
				retObj.set("pass", passNtf);
				Ret itemRet = ChatMsgService.me.afterSendFriendChatMsg(passMsg, touser, userItem.getChatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
				if (itemRet.isFail()) {
					retObj = itemRet;
					msg = RetUtils.getRetMsg(retObj);
					return false;
				}
				//2、发送申请的打招呼的聊天内容
				WxFriendMsg applyMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.YES, StrUtil.isBlank(greet) ? "你好,我是" + user.getNick() : greet, uid,
				        touid, touid, null, appversion);
				if (applyMsg == null) {
					return failRet("保存打招呼消息失败");
				}
				applyMsg.setNick(user.getNick());
				applyMsg.setNick(user.getAvatar());
				WxFriendChatNtf applyNtf = WxFriendChatNtf.from(applyMsg);
				applyNtf.setChatlinkid(toChatLinkid);
				applyNtf.setActflag(Const.YesOrNo.YES);
				applyNtf.setNick(touser.getNick());
				applyNtf.setAvatar(touser.getAvatar());
				applyNtf.setActavatar(user.getAvatar());
				applyNtf.setActname(user.getNick());
				retObj.set("apply", applyNtf);

				text = "你已添加了" + user.getNick() + "，现在可以开始聊天了";
				//3、发送自己的系统内容
				WxFriendMsg sysMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, touid, text, uid, null, null, appversion);
				if (sysMsg == null) {
					return failRet("保存系统消息失败");
				}
				sysMsg.setNick(user.getNick());
				sysMsg.setNick(user.getAvatar());
				WxFriendChatNtf sysNtf = WxFriendChatNtf.from(sysMsg);
				sysNtf.setChatlinkid(toChatLinkid);
				retObj.set("sys", sysNtf);

				Ret toItemRet = ChatMsgService.me.afterSendFriendChatMsg(sysMsg, touser, userItem.getTochatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
				if (toItemRet.isFail()) {
					retObj = toItemRet;
					msg = RetUtils.getRetMsg(retObj);
					return false;
				}
				if (!isExist) {
					boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, new Long(touid), Const.ChatMode.P2P, passMsg.getId());
					if (!startTx) {
						return failRet("修改起始消息异常");
					}
				}
				boolean toStartTx = ChatIndexService.me.chatuserStartMsg(touid, new Long(uid), Const.ChatMode.P2P, passMsg.getId());
				if (!toStartTx) {
					return failRet("修改好友起始消息异常");
				}
				if (isExist) {
					//互相新增好友，不存在缓存
					FriendService.me.putToP2pCache(passMsg, chatlinkid, toChatLinkid);
					FriendService.me.putToP2pCache(applyMsg, chatlinkid, toChatLinkid);
					FriendService.me.putToP2pCache(sysMsg, chatlinkid, toChatLinkid);
				}
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, touid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return false;
		}
		Ret atomRet = atom.getRetObj();
		WxFriendChatNtf passNtf = RetUtils.getOkTData(atomRet, "pass");
		ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(passNtf));
		Ims.sendToUser(uid, imPacket);
		WxFriendChatNtf applyNtf = RetUtils.getOkTData(atomRet, "apply");
		imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(applyNtf));
		Ims.sendToUser(touid, imPacket);
		WxFriendChatNtf sysNtf = RetUtils.getOkTData(atomRet, "sys");
		imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(sysNtf));
		Ims.sendToUser(touid, imPacket);
		//清除缓存
		ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
		ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		ChatIndexService.removeChatItemsCache(chatlinkid);
		ChatIndexService.removeChatItemsCache(toChatLinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(passNtf.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(uid, passNtf.getChatlinkid(), user, passNtf.getC());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return true;
	}

	/**
	 * 审核通过后的被邀请加好友-已调整
	 * @param request 审核request
	 * @param devicetype
	 * @param uid 邀请者
	 * @param touid 被邀请者
	 * @param chatlinkid 被邀请的聊天会话
	 * @param greet 招呼语
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月17日 下午3:16:43
	 */
	public static boolean addFriendSigleOfPassApply(HttpRequest request, Integer uid, Integer touid, Long chatlinkid, String greet) throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		User touser = UserService.ME.getById(touid);
		User user = UserService.ME.getById(uid);
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				//1、发送申请的验证的聊天内容
				WxFriendMsg applyMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.YES, greet, uid, touid, touid, null, appversion);
				if (applyMsg == null) {
					failRet("保存打招呼消息失败");
					return false;
				}
				retObj = Ret.ok();
				WxFriendChatNtf applyNtf = WxFriendChatNtf.from(applyMsg);
				applyNtf.setChatlinkid(chatlinkid);
				applyNtf.setActflag(Const.YesOrNo.YES);
				applyNtf.setNick(touser.getNick());
				applyNtf.setAvatar(touser.getAvatar());
				applyNtf.setActavatar(user.getAvatar());
				applyNtf.setActname(user.getNick());
				retObj.set("apply", applyNtf);

				//2、发送自己的系统内容
				String text = "你已添加了" + user.getNick() + "，现在可以开始聊天了";
				WxFriendMsg sysMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, touid, text, uid, null, null, appversion);
				if (sysMsg == null) {
					failRet("保存系统消息失败");
					return false;
				}
				WxFriendChatNtf sysNtf = WxFriendChatNtf.from(sysMsg);
				sysNtf.setChatlinkid(chatlinkid);
				retObj.set("sys", sysNtf);
				//更新touid的消息会话
				Ret toItemRet = ChatMsgService.me.afterSendFriendChatMsg(sysMsg, touser, userItem.getChatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
				if (toItemRet.isFail()) {
					retObj = toItemRet;
					msg = RetUtils.getRetMsg(retObj);
					return false;
				}
				boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, new Long(uid), Const.ChatMode.P2P, applyMsg.getId());
				if (!startTx) {
					return failRet("修改起始消息异常");
				}
				//自己新增好友也不需要
				//				FriendService.me.putToP2pCache(applyMsg, chatlinkid, null);
				//				FriendService.me.putToP2pCache(sysMsg, chatlinkid, null);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, touid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return false;
		}
		Ret atomRet = atom.getRetObj();
		WxFriendChatNtf applyNtf = RetUtils.getOkTData(atomRet, "apply");
		ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(applyNtf));
		Ims.sendToUser(touid, imPacket);
		WxFriendChatNtf sysNtf = RetUtils.getOkTData(atomRet, "sys");
		imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(sysNtf));
		Ims.sendToUser(touid, imPacket);
		//清除缓存
		ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		ChatIndexService.removeChatItemsCache(chatlinkid);
		return true;
	}

	/**
	 * TODO-lixinji-700-用户操作通知 many
	 * 1、删除好友改为--synFriendDel()
	 * 2、撤回消息改为--synP2pMsgBakOrDel
	 * 3、删除消息改为--synP2pMsgDel
	 * 4、oper操作：switch处理
	 * 用户的默认系统操作，默认保存到用户好友消息中，等价于自己默认是自己的好友，以便好理解
	 * @param request
	 * @param devicetype
	 * @param uid
	 * @param chatlinkid 聊天会话
	 * @param oper 操作码：见 Const.WxUserOper
	 * @return
	 * @author lixinji
	 * 2020年1月20日 下午3:15:55
	 */
	public static boolean userChatOper(HttpRequest request, Integer uid, Long chatlinkid, Short oper, String text, String operBizData, WxChatItems chatItems) {
		if (chatlinkid == null) {
			//不进行操作消息转发
			return false;
		}
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		if (StrUtil.isBlank(text)) {
			text = "你自己操作了：" + oper;
		}
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, oper, operBizData, null, appversion);
		WxUserOperNtf operNtf = WxUserOperNtf.from(sysOperMsg);
		operNtf.setOperbizdata(operBizData);
		operNtf.setChatlinkid(chatlinkid);
		if (Objects.equals(oper, Const.WxUserOper.ACT)) {
			operNtf.setActflag(Const.YesOrNo.YES);
		}
		if (chatItems != null) {
			operNtf.setChatItems(chatItems);
		}
		operNtf.setOper(oper);
		ImPacket imPacket = new ImPacket(Command.WxUserOperNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * TODO-lixinji-700-操作通知-synMsgFocusJoin
	 * 用户操作通知-待优化抽象处理TODO:lixinji
	 * @param channelContext
	 * @param uid
	 * @param chatlinkid
	 * @param oper
	 * @param text
	 * @return
	 * @author lixinji
	 * 2020年3月5日 下午9:28:04
	 */
	@Deprecated
	public static boolean userChatOper(ChannelContext channelContext, Integer uid, Long chatlinkid, Short oper, String text, String channelid) {
		Devicetype devicetype = Devicetype.SYS_TASK;
		String appversion = "0.0.0";
		String sessionid = "";
		String ip = "";
		if (channelContext != null) {
			devicetype = ImUtils.getDevicetype(channelContext);
			MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
			if (mobileInfo != null) {
				appversion = mobileInfo.getAppversion();
			}
			sessionid = ImUtils.getToken(channelContext);
			ip = channelContext.getClientNode().getIp();
		} else {
			log.error("用户操作,通道为空,channelid:{}", channelid);
		}
		if (chatlinkid == null) {
			//不进行操作消息转发
			return false;
		}
		if (StrUtil.isBlank(text)) {
			text = "你自己操作了：" + oper;
		}
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype.getValue(), sessionid, ip, text, uid, oper, "", null, appversion);
		WxUserOperNtf operNtf = WxUserOperNtf.from(sysOperMsg);
		operNtf.setChatlinkid(chatlinkid);
		if (Objects.equals(oper, Const.WxUserOper.ACT)) {
			operNtf.setActflag(Const.YesOrNo.YES);
		}
		operNtf.setOper(oper);
		ImPacket imPacket = new ImPacket(Command.WxUserOperNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param appversion
	 * @param uid
	 * @param chatlinkid
	 * @param oper
	 * @param text
	 * @param channelid
	 * @return
	 * @author lixinji
	 * 2020年11月19日 下午5:01:35
	 */
	public static boolean userChatOper(Short devicetype, String sessionid, String ip, String appversion, Integer uid, Long chatlinkid, Short oper, String text, String channelid) {
		if (chatlinkid == null) {
			//不进行操作消息转发
			return false;
		}
		if (StrUtil.isBlank(text)) {
			text = "你自己操作了：" + oper;
		}
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, oper, "", null, appversion);
		WxUserOperNtf operNtf = WxUserOperNtf.from(sysOperMsg);
		operNtf.setChatlinkid(chatlinkid);
		if (Objects.equals(oper, Const.WxUserOper.ACT)) {
			operNtf.setActflag(Const.YesOrNo.YES);
		}
		operNtf.setOper(oper);
		ImPacket imPacket = new ImPacket(Command.WxUserOperNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * TODO-lixinji-738-系统同步通知-many
	 * 1、好友新增：synFriend
	 * 2、好友删除：synFriendDel
	 * 3、好友修改：synFdRemarkName
	 * 4、好友申请：synFriendApply
	 * 用户的通知请求
	 * @param request
	 * @param devicetype
	 * @param uid
	 * @param code
	 * @param text
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午9:35:09
	 */
	public static boolean useSysChatNtf(HttpRequest request, Integer uid, Short code, String text, String bizdata) {
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		if (StrUtil.isBlank(text)) {
			text = "通知码：" + code;
		}
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, code, "", null, appversion);
		WxUserSysNtf operNtf = WxUserSysNtf.from(sysOperMsg);
		operNtf.setCode(code);
		operNtf.setBizdata(bizdata);
		switch (code) {
		case Const.WxSysCode.ERROR_CODE:
			break;
		case Const.WxSysCode.FRIEND_CHANGE_ADD:
			operNtf.setFiddata(bizdata);
			break;
		case Const.WxSysCode.FRIEND_CHANGE_DEL:
			operNtf.setFiddata(bizdata);
			break;
		case Const.WxSysCode.FRIEND_INFO_UPDATE:
			operNtf.setFiddata(bizdata);
			break;
		default:
			break;
		}
		ImPacket imPacket = new ImPacket(Command.WxUserSysNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * TODO-lixinji-738-自动系统通知
	 * 下层处理
	 * 用户的系统通知
	 * @param uid
	 * @param code
	 * @param text
	 * @param bizdata
	 * @return
	 * @author lixinji
	 * 2020年3月16日 上午11:23:16
	 */
	public static boolean autoUseSysChatNtf(Integer uid, Short code, String text, String bizdata, WxChatItems chatItems) {
		if (StrUtil.isBlank(text)) {
			text = "通知码：" + code;
		}
		String ip = Const.MY_IP;
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(Devicetype.SYS_TASK.getValue(), "系统发送", ip, text, uid, code, "", null, "0.0.0");
		WxUserSysNtf operNtf = WxUserSysNtf.from(sysOperMsg);
		operNtf.setCode(code);
		operNtf.setBizdata(bizdata);
		if (chatItems != null) {
			operNtf.setChatItems(chatItems);
		}
		ImPacket imPacket = new ImPacket(Command.WxUserSysNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * 已调整
	 * TODO-lixinji-700-激活通知-synChatSession(ADD)
	 * 用户会话激活通知
	 * @param request
	 * @param devicetype
	 * @param uid
	 * @param chatlinkid
	 * @param oper
	 * @param name
	 * @param avatar
	 * @param chatmode
	 * @return
	 * @author lixinji
	 * 2020年2月17日 下午2:26:37
	 */
	@Deprecated
	public static boolean userActOper(HttpRequest request, Integer uid, WxChatItems chatItems) {
		if (chatItems == null) {
			return false;
		}
		Long chatlinkid = chatItems.getId();
		if (chatlinkid == null) {
			//不进行操作消息转发
			return false;
		}
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		String text = "激活聊天会话：" + chatlinkid;
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, Const.WxUserOper.ACT, "", null, appversion);
		WxUserOperNtf operNtf = WxUserOperNtf.from(sysOperMsg);
		operNtf.setChatlinkid(chatlinkid);
		operNtf.setActflag(Const.YesOrNo.YES);
		operNtf.setActavatar(chatItems.getAvatar());
		operNtf.setActname(chatItems.getName());
		operNtf.setChatmode(chatItems.getChatmode());
		if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP)) {
			WxGroup group = GroupService.me.getByGroupid(chatItems.getBizid());
			operNtf.setJoinnum(group.getJoinnum());
			operNtf.setG(group.getId());
			operNtf.setChatlinkid(-group.getId());
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, group.getId());
			operNtf.setGrouprole(groupItem.getGrouprole());
			Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, uid + "", group.getId() + "");
		}
		chatItems.setChatlinkid(chatItems.getId());
		operNtf.setChatItems(chatItems);
		operNtf.setOper(Const.WxUserOper.ACT);
		ImPacket imPacket = new ImPacket(Command.WxUserOperNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	@Deprecated
	public static boolean userActOper(Integer uid, WxChatItems chatItems) {
		if (chatItems == null) {
			return false;
		}
		Long chatlinkid = chatItems.getId();
		if (chatlinkid == null) {
			//不进行操作消息转发
			return false;
		}
		String appversion = "0.0.0";
		Short devicetype = Devicetype.SYS_TASK.getValue();
		String text = "激活聊天会话：" + chatlinkid;
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, "", "0.0.0.0", text, uid, Const.WxUserOper.ACT, "", null, appversion);
		WxUserOperNtf operNtf = WxUserOperNtf.from(sysOperMsg);
		operNtf.setChatlinkid(chatlinkid);
		operNtf.setActflag(Const.YesOrNo.YES);
		operNtf.setActavatar(chatItems.getAvatar());
		operNtf.setActname(chatItems.getName());
		operNtf.setChatmode(chatItems.getChatmode());
		if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP)) {
			WxGroup group = GroupService.me.getByGroupid(chatItems.getBizid());
			operNtf.setJoinnum(group.getJoinnum());
			operNtf.setG(group.getId());
			operNtf.setChatlinkid(-group.getId());
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, group.getId());
			operNtf.setGrouprole(groupItem.getGrouprole());
			Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, uid + "", group.getId() + "");
		}
		chatItems.setChatlinkid(chatItems.getId());
		operNtf.setChatItems(chatItems);
		operNtf.setOper(Const.WxUserOper.ACT);
		ImPacket imPacket = new ImPacket(Command.WxUserOperNtf, Json.toJson(operNtf));
		Ims.sendToUser(uid, imPacket);
		return true;
	}

	/**
	 * 发送私聊消息-无转发
	 * @param channelContext
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param toChatlinkid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月20日 下午2:06:06
	 */
	public static Ret sendFdMsgEach(ChannelContext channelContext, String text, Short contenttype, Integer uid, Integer touid, Short sysflag) throws Exception {
		if (channelContext == null) {
			return sendFdMsgEach(text, contenttype, uid, touid, sysflag, "");
		}
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
		String sessionid = ImUtils.getToken(channelContext);
		String ip = channelContext.getClientNode().getIp();
		MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
		String appversion = "0.0.0";
		if (mobileInfo != null) {
			appversion = mobileInfo.getAppversion();
		}
		Ret ret = sendFdMsgEach(devicetype.getValue(), sessionid, ip, text, contenttype, uid, touid, sysflag, "", null, null, appversion,null);
		return ret;
	}

	/**
	 * 发送私聊消息通知
	 * @param channelContext
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param sysflag
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年5月26日 下午4:36:44
	 */
	public static Ret sendFdMsgEach(String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operbizdata) throws Exception {
		Devicetype devicetype = Devicetype.SYS_TASK;
		String sessionid = "sys";
		String ip = Const.MY_IP;
		return sendFdMsgEach(devicetype.getValue(), sessionid, ip, text, contenttype, uid, touid, sysflag, operbizdata, null, null, "0.0.0",null);
	}

	/**
	 * 发送私聊消息-无转发
	 * @param request
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param sysflag
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public static Ret sendFdMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Integer touid, Short sysflag) throws Exception {
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		return sendFdMsgEach(devicetype, sessionid, ip, text, contenttype, uid, touid, sysflag, "", null, null, appversion,null);
	}

	/**
	 * 发送私聊消息
	 * @param channelContext
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param toChatlinkid
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月20日 下午2:06:06
	 */
	public static Ret sendFdMsgEach(ChannelContext channelContext, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, Long frommsgid, Short fromchatmode)
	        throws Exception {
		if (channelContext == null) {
			return RetUtils.failMsg("通道为空");
		}
		Devicetype devicetype = ImUtils.getDevicetype(channelContext);
		String sessionid = ImUtils.getToken(channelContext);
		String ip = channelContext.getClientNode().getIp();
		MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
		String appversion = "0.0.0";
		if (mobileInfo != null) {
			appversion = mobileInfo.getAppversion();
		}
		return sendFdMsgEach(devicetype.getValue(), sessionid, ip, text, contenttype, uid, touid, sysflag, "", frommsgid, fromchatmode, appversion,null);
	}

	/**
	 * 业务消息-发红包
	 * @param devicetType
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param sysflag
	 * @param operbizdata
	 * @param appversion
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月22日 下午9:41:02
	 */
	public static Ret sendFdMsgFroSendRed(Short devicetType, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operbizdata,
	        String appversion) throws Exception {
		String sessionid = "";
		return sendFdMsgEach(devicetType, sessionid, ip, text, contenttype, uid, touid, sysflag, operbizdata, null, null, appversion,null);
	}

	/**
	 * 发送私聊消息
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendFdMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
	        Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
				        appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
					        user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
					        readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
					        friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
					        tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
					        startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
					        readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}
				//存在更新相关会话
				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
				if (ret.isFail()) {
					return failRet(ret);
				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	/**
	 * 新的朋友圈消息提醒
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendMomentMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
									Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} /*else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}*/
				//存在更新相关会话
//				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
//				if (ret.isFail()) {
//					return failRet(ret);
//				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		msg.setReadflag(Short.valueOf("2"));
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxMomentsOperNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxMomentsOperNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	/**
	 * 好友异常通知
	 * @param request
	 * @param devicetype
	 * @param senduid
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param code
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年3月16日 下午3:58:14
	 */
	public static boolean sendFriendErrorMsg(HttpRequest request, Integer senduid, Integer uid, Integer touid, Long chatlinkid, Integer code, String msg) {
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		Short devicetype = ext.getDeviceType();
		return sendFriendErrorMsg(devicetype, sessionid, ip, senduid, uid, touid, chatlinkid, code, msg);
	}

	/**
	 * @param senduid
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param code
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年11月20日 上午10:36:05
	 */
	public static boolean sendFriendErrorMsg(Integer senduid, Integer uid, Integer touid, Long chatlinkid, Integer code, String msg) {
		String sessionid = "";
		String ip = "0.0.0.0";
		Short devicetype = Devicetype.SYS_TASK.getValue();
		;
		return sendFriendErrorMsg(devicetype, sessionid, ip, senduid, uid, touid, chatlinkid, code, msg);
	}

	/**
	 * TODO-lixinji-701-错误通知
	 * 好友相关错误通知(保存消息)
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param senduid 发送者uid
	 * @param uid 好友方向发送方
	 * @param touid 好友方向接收方
	 * @param chatlinkid 发送方聊天会话
	 * @param code 错误码
	 * @param msg 错误信息
	 * @return
	 * @author lixinji
	 * 2020年1月18日 下午3:14:28
	 */
	public static boolean sendFriendErrorMsg(Short devicetype, String sessionid, String ip, Integer senduid, Integer uid, Integer touid, Long chatlinkid, Integer code, String msg) {
		WxFriendErrorNtf errorNtf = new WxFriendErrorNtf();
		errorNtf.setChatlinkid(chatlinkid);
		errorNtf.setCode(code);
		errorNtf.setMsg(msg);
		errorNtf.setTouid(touid);
		errorNtf.setUid(uid);
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, uid, msg, touid, Const.WxSysCode.ERROR_CODE, null, "0.0.0");
		errorNtf.setMid(sysOperMsg.getId());
		errorNtf.setT(sysOperMsg.getTime().getTime());
		ImPacket imPacket = new ImPacket(Command.WxFriendErrorNtf, Json.toJson(errorNtf));
		Ims.sendToUser(senduid, imPacket);
		return true;
	}

	/**
	 * TODO-lixinji-701-错误通知
	 * 好友相关错误通知(保存消息)
	 * @param channelContext
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @param code
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年1月20日 下午2:02:22
	 */
	public static boolean sendFriendErrorMsg(ChannelContext channelContext, Integer senduid, Integer uid, Integer touid, Long chatlinkid, Integer code, String msg) {
		Devicetype devicetype = Devicetype.SYS_TASK;
		String sessionid = "";
		String appversion = "0.0.0";
		String ip = "";
		if (channelContext != null) {
			devicetype = ImUtils.getDevicetype(channelContext);
			sessionid = ImUtils.getToken(channelContext);
			MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
			appversion = "0.0.0";
			if (mobileInfo != null) {
				appversion = mobileInfo.getAppversion();
			}
			ip = channelContext.getClientNode().getIp();
		}
		WxFriendErrorNtf errorNtf = new WxFriendErrorNtf();
		errorNtf.setChatlinkid(chatlinkid);
		errorNtf.setCode(code);
		errorNtf.setMsg(msg);
		errorNtf.setTouid(touid);
		errorNtf.setUid(uid);
		WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype.getValue(), sessionid, ip, uid, msg, touid, Const.WxSysCode.ERROR_CODE, null, appversion);
		errorNtf.setMid(sysOperMsg.getId());
		errorNtf.setT(sysOperMsg.getTime().getTime());
		ImPacket imPacket = new ImPacket(Command.WxFriendErrorNtf, Json.toJson(errorNtf));
		Ims.send(channelContext, imPacket);
		return true;
	}
	
	
	

	/**
	 * 检查好友聊天
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 下午1:54:44
	 */
	public static Ret checkFriendChat(Integer uid, Integer touid) {
		//验证黑名单
		WxUserBlackItems items = ChatService.getBlockItems(touid, uid);
		if (items != null) {
			return RetUtils.failMsg("黑名单", AppCode.FriendErrorCode.BLACK);
		}
		//验证相关链接是否建立
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (!ChatService.existTwoFriend(userItem)) {
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		return RetUtils.okData(userItem);
	}

	/**
	 * 验证好友会话状态
	 * @param uid
	 * @param chatItems
	 * @return
	 * @author lixinji
	 * 2020年2月27日 下午4:03:28
	 */
	public static Ret checkFriendChat(Integer uid, WxChatItems chatItems) {
		if (chatItems == null) {
			return RetUtils.failMsg("系统异常", AppCode.FriendErrorCode.SYS_ERROR);
		}
		//验证黑名单
		WxUserBlackItems items = ChatService.getBlockItems(chatItems.getBizid().intValue(), uid);
		if (items != null) {
			return RetUtils.failMsg("黑名单", AppCode.FriendErrorCode.BLACK);
		}
		//验证相关链接是否建立
		if (!ChatService.existTwoFriend(chatItems)) {
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		return Ret.ok();
	}

	/**
	 * 校验群会话状态
	 * @param uid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月14日 上午10:32:34
	 */
	public static Ret checkGroupChat(Integer uid, Long groupid, Long chatlinkid) {
		if (groupid == null) {
			return RetUtils.failMsg("群id为空", AppCode.GroupErrorCode.SYS_ERROR);
		}
		WxGroup group = GroupService.me.getByGroupid(groupid);
		if (group == null) {
			return RetUtils.failMsg("本群已解散", AppCode.GroupErrorCode.SYS_ERROR);
		}
		if (chatlinkid == null) {
			return RetUtils.failMsg("会话id为空", AppCode.GroupErrorCode.SYS_ERROR);
		}
		//验证相关链接是否建立
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(chatlinkid);
		if (!Objects.equals(groupid, groupItem.getGroupid())) {
			return RetUtils.failMsg("数据不一致", AppCode.ForbidOper.DATA_DIFFER);
		}
		if (!ChatService.groupChatLink(groupItem)) {
			return RetUtils.failMsg("你不在群组成员里", AppCode.GroupErrorCode.NO_LINK);
		}
		return Ret.ok();
	}

	/**
	 * 校验群会话状态
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月27日 下午4:11:20
	 */
	public static Ret checkGroupChat(WxChatGroupItem groupItem) {
		if (groupItem == null) {
			return RetUtils.failMsg("您已退出群聊", AppCode.GroupErrorCode.SYS_ERROR);
		}
		WxGroup group = GroupService.me.getByGroupid(groupItem.getGroupid());
		if (group == null) {
			return RetUtils.failMsg("本群已解散", AppCode.GroupErrorCode.SYS_ERROR);
		}
		if (groupItem.getChatlinkid() == null) {
			return RetUtils.failMsg("当前会话已删除", AppCode.GroupErrorCode.SYS_ERROR);
		}
		if (!ChatService.groupChatLink(groupItem)) {
			return RetUtils.failMsg("您已被移出群聊", AppCode.GroupErrorCode.NO_LINK);
		}
		return Ret.ok();
	}

	/**
	 * 创建群消息处理-已调整
	 * @param curr
	 * @param chatList
	 * @param group
	 * @param msg
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月13日 下午9:53:16
	 */
//	public static boolean creatGroup(HttpRequest request, User curr, String uids, String nicks, WxGroup group, WxGroupMsg groupMsg, SysMsgVo sysMsgVo) throws Exception {
//		Integer uid = curr.getId();
//		Long groupid = group.getId();
//		AbsTxAtom atom = new AbsTxAtom() {
//			@Override
//			public boolean noTxRun() {
//				String[] uidArr = null;
//				List<String> joinTemp = new ArrayList<>();
//				List<String> onJoinTemp = new ArrayList<>();
//				if (StrUtil.isNotBlank(uids)) {
//					uidArr = StrUtil.splitToArray(uids, ",");
//					for (String uid : uidArr) {
//						RealNameCertification userReal = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", uid);
//						if (userReal != null && userReal.getStatus().equals(1)) {
//							joinTemp.add(uid);
//						} else {
//							onJoinTemp.add(uid);
//						}
//					}
//					if (joinTemp.size() == 0) {
//						return failRet("创建群组失败，所有成员均为实名认证");
//					}
//				}
//				String[] joinUids = new String[joinTemp.size()];
//				for (int i = 0; i < joinTemp.size(); i++) {
//					joinUids[i] = joinTemp.get(i);
//				}
//				String[] onJoinUids = new String[onJoinTemp.size()];
//				for (int i = 0; i < onJoinTemp.size(); i++) {
//					onJoinUids[i] = onJoinTemp.get(i);
//				}
//				short joinnum = joinUids != null ? (short) (joinUids.length + 1) : (short) 1;
//				List<WxChatItems> chatItemList = new ArrayList<WxChatItems>();
//				if (StrUtil.isNotBlank(uids)) {
//					//初始化其它用户信息
//					for (String uidStr : joinUids) {
//						if (StrUtil.isNotBlank(uidStr)) {
//							try {
//								int otheruid = Integer.parseInt(uidStr);
//								if (Objects.equals(otheruid, uid)) { //前面已经把当前用户加到了该群，所以此处略过
//									continue;
//								}
//								User user = UserService.ME.getById(otheruid);
//								if (user == null || Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
//									log.error("邀请用户不存在：uid:{},groupid:{}", otheruid, groupid);
//									continue;
//								}
//								WxGroupUser otherUser = new WxGroupUser();
//								otherUser.setGroupid(groupid);
//								otherUser.setUid(otheruid);
//								otherUser.setSrcnick(user.getNick());
//								otherUser.setAutoflag(Const.YesOrNo.YES);
//								otherUser.setGroupnick(user.getNick());
//								otherUser.setGroupavator(user.getAvatar());
//								boolean otherSave = otherUser.save();
//								if (!otherSave) {
//									log.error("群用户保存部分失败：uid:{},groupid:{}", otheruid, groupid);
//									return failRet("群用户保存失败");
//								}
//								//TODO:lixinji-此处可以加入群聊是否激活的判断
//								WxChatItems otherItems = new WxChatItems();
//								otherItems.setUid(otheruid);
//								otherItems.setBizid(groupid);
//								otherItems.setLinkid(otherUser.getId());
//								otherItems.setChatmode(Const.ChatMode.GROUP);
//								otherItems.setBizrole(Const.GroupRole.MEMBER);
//								otherItems.setAvatar(group.getAvatar());
//								otherItems.setName(group.getName());
//								otherItems.setJoinnum(joinnum);
//								otherItems.setStartmsgid(groupMsg.getId());
//								boolean ohterItemSave = otherItems.save();
//								if (!ohterItemSave) {
//									return failRet("会话初始化异常");
//								}
//								WxChatItemsMeta otherMeta = new WxChatItemsMeta();
//								otherMeta.setUid(otheruid);
//								otherMeta.setBizid(groupid);
//								otherMeta.setChatmode(Const.ChatMode.GROUP);
//								otherMeta.setChatlinkid(otherItems.getId());
//								otherMeta.setLastmsgid(groupMsg.getId());
//								otherMeta.setLastmsguid(uid);
//								otherMeta.setFromnick(groupMsg.getNick());
//								otherMeta.setSysflag(groupMsg.getSendbysys());
//								otherMeta.setMsgresume(groupMsg.getResume());
//								otherMeta.setMsgtype(groupMsg.getContenttype());
//								otherMeta.setSendtime(groupMsg.getTime());
//								otherMeta.setNotreadstartmsgid(groupMsg.getId());
//								otherMeta.setNotreadcount(0);
//								otherMeta.setChatuptime(new Date());
//								boolean ohterMetaSave = otherMeta.save();
//								if (!ohterMetaSave) {
//									return failRet("会话动态初始化异常");
//								}
//								//TODO:lixinji-此处可以加入群聊是否激活的判断,如果创建不激活，需要修改激活状态
//								ChatIndexService.me.chatUserInit(otheruid, Const.ChatMode.GROUP, groupid, otherItems.getId(), otherMeta.getId(), otherUser.getId(),
//								        groupMsg.getId());
//								ChatIndexService.me.chatGroupInit(otheruid, groupid, otherItems.getId(), otherMeta.getId(), otherUser.getId(), groupMsg.getId(), null);
//								chatItemList.add(otherItems);
//								//更新用户的通讯录
//								ChatIndexService.clearMailListCache(otheruid);
//							} catch (Exception e) {
//								log.error("", e);
//							}
//						}
//					}
//				}
//				retObj = Ret.ok().set("list", chatItemList);
//				return true;
//			}
//		};
//		boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
//		if (init) {
//			ChatIndexService.removeGroupCache(groupid);
//			List<WxChatItems> chatList = RetUtils.getOkTData(atom.getRetObj(), "list");
//			WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(groupMsg, sysMsgVo);
//			wxGroupChatNtf.setActflag(Const.YesOrNo.YES);
//			wxGroupChatNtf.setActname(group.getName());
//			wxGroupChatNtf.setActavatar(group.getAvatar());
//			wxGroupChatNtf.setJoinnum(group.getJoinnum());
//			wxGroupChatNtf.setChatlinkid(-groupid);
//			for (WxChatItems chatItems : chatList) {
//				String otheruid = chatItems.getUid() + "";
//				Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
//			}
//			wxGroupChatNtf.setGrouprole(Const.GroupRole.MEMBER);
//			Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", group.getId() + "");
//			ImPacket imPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(wxGroupChatNtf));
//			Ims.sendToGroup(groupid, imPacket);
//			Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", group.getId() + "");
//			return true;
//		}
//		sendFriendErrorMsg(request, uid, uid, uid, null, AppCode.GroupErrorCode.SYS_ERROR, atom.getMsg());
//		return false;
//	}
	public static boolean creatGroup(HttpRequest request, User curr, String uids, String nicks, WxGroup group, WxGroupMsg groupMsg, SysMsgVo sysMsgVo) throws Exception {
		Integer uid = curr.getId();
		Long groupid = group.getId();
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				String[] uidArr = null;
				if (StrUtil.isNotBlank(uids)) {
					uidArr = StrUtil.splitToArray(uids, ",");
				}
				short joinnum = uidArr != null ? (short) (uidArr.length + 1) : (short) 1;
				List<WxChatItems> chatItemList = new ArrayList<WxChatItems>();
				if (StrUtil.isNotBlank(uids)) {
					//初始化其它用户信息
					for (String uidStr : uidArr) {
						if (StrUtil.isNotBlank(uidStr)) {
							try {
								int otheruid = Integer.parseInt(uidStr);
								if (Objects.equals(otheruid, uid)) { //前面已经把当前用户加到了该群，所以此处略过
									continue;
								}
								User user = UserService.ME.getById(otheruid);
								if (user == null || Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
									log.error("邀请用户不存在：uid:{},groupid:{}", otheruid, groupid);
									continue;
								}
								WxGroupUser otherUser = new WxGroupUser();
								otherUser.setGroupid(groupid);
								otherUser.setUid(otheruid);
								otherUser.setSrcnick(user.getNick());
								otherUser.setAutoflag(Const.YesOrNo.YES);
								otherUser.setGroupnick(user.getNick());
								otherUser.setGroupavator(user.getAvatar());
								boolean otherSave = otherUser.save();
								if (!otherSave) {
									log.error("群用户保存部分失败：uid:{},groupid:{}", otheruid, groupid);
									return failRet("群用户保存失败");
								}
								//TODO:lixinji-此处可以加入群聊是否激活的判断
								WxChatItems otherItems = new WxChatItems();
								otherItems.setUid(otheruid);
								otherItems.setBizid(groupid);
								otherItems.setLinkid(otherUser.getId());
								otherItems.setChatmode(Const.ChatMode.GROUP);
								otherItems.setBizrole(Const.GroupRole.MEMBER);
								otherItems.setAvatar(group.getAvatar());
								otherItems.setName(group.getName());
								otherItems.setJoinnum(joinnum);
								otherItems.setStartmsgid(groupMsg.getId());
								boolean ohterItemSave = otherItems.save();
								if (!ohterItemSave) {
									return failRet("会话初始化异常");
								}
								WxChatItemsMeta otherMeta = new WxChatItemsMeta();
								otherMeta.setUid(otheruid);
								otherMeta.setBizid(groupid);
								otherMeta.setChatmode(Const.ChatMode.GROUP);
								otherMeta.setChatlinkid(otherItems.getId());
								otherMeta.setLastmsgid(groupMsg.getId());
								otherMeta.setLastmsguid(uid);
								otherMeta.setFromnick(groupMsg.getNick());
								otherMeta.setSysflag(groupMsg.getSendbysys());
								otherMeta.setMsgresume(groupMsg.getResume());
								otherMeta.setMsgtype(groupMsg.getContenttype());
								otherMeta.setSendtime(groupMsg.getTime());
								otherMeta.setNotreadstartmsgid(groupMsg.getId());
								otherMeta.setNotreadcount(0);
								otherMeta.setChatuptime(new Date());
								boolean ohterMetaSave = otherMeta.save();
								if (!ohterMetaSave) {
									return failRet("会话动态初始化异常");
								}
								//TODO:lixinji-此处可以加入群聊是否激活的判断,如果创建不激活，需要修改激活状态
								ChatIndexService.me.chatUserInit(otheruid, Const.ChatMode.GROUP, groupid, otherItems.getId(), otherMeta.getId(), otherUser.getId(),
										groupMsg.getId());
								ChatIndexService.me.chatGroupInit(otheruid, groupid, otherItems.getId(), otherMeta.getId(), otherUser.getId(), groupMsg.getId(), null);
								chatItemList.add(otherItems);
								//更新用户的通讯录
								ChatIndexService.clearMailListCache(otheruid);
							} catch (Exception e) {
								log.error("", e);
							}
						}
					}
				}
				retObj = Ret.ok().set("list", chatItemList);
				return true;
			}
		};
		boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (init) {
			ChatIndexService.removeGroupCache(groupid);
			List<WxChatItems> chatList = RetUtils.getOkTData(atom.getRetObj(), "list");
			WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(groupMsg, sysMsgVo);
			wxGroupChatNtf.setActflag(Const.YesOrNo.YES);
			wxGroupChatNtf.setActname(group.getName());
			wxGroupChatNtf.setActavatar(group.getAvatar());
			wxGroupChatNtf.setJoinnum(group.getJoinnum());
			wxGroupChatNtf.setChatlinkid(-groupid);
			for (WxChatItems chatItems : chatList) {
				String otheruid = chatItems.getUid() + "";
				Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
			}
			wxGroupChatNtf.setGrouprole(Const.GroupRole.MEMBER);
			Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", group.getId() + "");
			ImPacket imPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(wxGroupChatNtf));
			Ims.sendToGroup(groupid, imPacket);
			Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", group.getId() + "");
			return true;
		}
		sendFriendErrorMsg(request, uid, uid, uid, null, AppCode.GroupErrorCode.SYS_ERROR, atom.getMsg());
		return false;
	}

	/**
	 * 创建群-群主通知
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param uids
	 * @param nicks
	 * @param group
	 * @param groupMsg
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月16日 下午6:00:32
	 */
	public static void creatGroupOwner(HttpRequest request, Short devicetype, User curr, WxGroup group, WxGroupMsg groupMsg, SysMsgVo sysMsgVo) throws Exception {
		WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(groupMsg, sysMsgVo);
		wxGroupChatNtf.setActflag(Const.YesOrNo.YES);
		wxGroupChatNtf.setActname(group.getName());
		wxGroupChatNtf.setActavatar(group.getAvatar());
		wxGroupChatNtf.setJoinnum(group.getJoinnum());
		wxGroupChatNtf.setChatlinkid(-group.getId());
		wxGroupChatNtf.setGrouprole(Const.GroupRole.OWNER);
		ImPacket imPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(wxGroupChatNtf));
		Ims.sendToUser(curr.getId(), imPacket);
		Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", group.getId() + "");
	}

	/**
	 * 标准发送群聊文本消息
	 * @param channelContext
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月14日 上午10:29:22
	 */
	public static Ret sendGroupMsgEach(ChannelContext channelContext, String text, Short contenttype, Integer uid, Long groupid, Long frommsgid, Short fromchatmode, String ats)
	        throws Exception {
		Devicetype devicetype = Devicetype.SYS_TASK;
		String sessionid = "";
		String ip = "";
		String appversion = "0.0.0";
		if (channelContext != null) {
			MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
			if (mobileInfo != null) {
				appversion = mobileInfo.getAppversion();
			}
			devicetype = ImUtils.getDevicetype(channelContext);
			sessionid = ImUtils.getToken(channelContext);
			ip = channelContext.getClientNode().getIp();
		}
		return sendGroupMsgEach(devicetype.getValue(), sessionid, ip, text, contenttype, uid, groupid, Const.YesOrNo.NO, null, "", frommsgid, fromchatmode, ats, appversion);
	}

	/**
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param frommsgid
	 * @param fromchatmode
	 * @param operbizdata
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月20日 上午10:43:14
	 */
	public static Ret sendGroupMsgEach(String text, Short contenttype, Integer uid, Long groupid, Long frommsgid, Short fromchatmode, String operbizdata) throws Exception {
		Devicetype devicetype = Devicetype.SYS_TASK;
		String sessionid = "";
		String ip = "";
		String appversion = "0.0.0";
		return sendGroupMsgEach(devicetype.getValue(), sessionid, ip, text, contenttype, uid, groupid, Const.YesOrNo.NO, null, operbizdata, frommsgid, fromchatmode, "",
		        appversion);
	}

	/**
	 * 标准请求发送含系统的群聊消息
	 * @param request
	 * @param devicetype
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param sysflag
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月25日 上午9:49:08
	 */
	public static Ret sendGroupMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo, Long frommsgid,
	        Short fromchatmode) throws Exception {
		return sendGroupMsgEach(request, text, contenttype, uid, groupid, sysflag, sysMsgVo, frommsgid, fromchatmode, "");
	}



	/**
	 * 非转发群聊消息
	 * @param request
	 * @param devicetype
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param sysflag
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月12日 下午6:37:55
	 */
	public static Ret sendGroupMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo) throws Exception {
		return sendGroupMsgEach(request, text, contenttype, uid, groupid, sysflag, sysMsgVo, null, null, "");
	}

	public static Ret sendGroupMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo, Short updatelist, Short openforbidden, Integer touid) throws Exception {
		return sendGroupMsgEach(request, text, contenttype, uid, groupid, sysflag, sysMsgVo, null, null, "", updatelist, openforbidden, touid);
	}

	/**
	 * 请求发送含系统的群聊消息-含操作码的
	 * @param request
	 * @param devicetype
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param sysflag
	 * @param frommsgid
	 * @param operbizdata
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月12日 下午1:45:22
	 */
	public static Ret sendGroupMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo, Long frommsgid,
	        Short fromchatmode, String operbizdata) throws Exception {
		if (request == null) {
			Short devicetype = Devicetype.SYS_TASK.getValue();
			String sessionid = "";
			String ip = "";
			String appversion = "0.0.0";
			return sendGroupMsgEach(devicetype, sessionid, ip, text, contenttype, uid, groupid, sysflag, sysMsgVo, operbizdata, frommsgid, fromchatmode, null, appversion);
		} else {
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
			String appversion = ext.getAppVersion();
			Short devicetype = ext.getDeviceType();
			return sendGroupMsgEach(devicetype, sessionid, ip, text, contenttype, uid, groupid, sysflag, sysMsgVo, operbizdata, frommsgid, fromchatmode, null, appversion);
		}
	}

	public static Ret sendGroupMsgEach(HttpRequest request, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo, Long frommsgid,
									   Short fromchatmode, String operbizdata, Short updatelist, Short openforbidden, Integer touid) throws Exception {
		if (request == null) {
			Short devicetype = Devicetype.SYS_TASK.getValue();
			String sessionid = "";
			String ip = "";
			String appversion = "0.0.0";
			return sendGroupMsgEach(devicetype, sessionid, ip, text, contenttype, uid, groupid, sysflag, sysMsgVo, operbizdata, frommsgid, fromchatmode, null, appversion, updatelist, openforbidden, touid);
		} else {
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
			String appversion = ext.getAppVersion();
			Short devicetype = ext.getDeviceType();
			return sendGroupMsgEach(devicetype, sessionid, ip, text, contenttype, uid, groupid, sysflag, sysMsgVo, operbizdata, frommsgid, fromchatmode, null, appversion, updatelist, openforbidden, touid);
		}
	}

	/**
	 * 业务消息-发送红包
	 * @param deviceTpe
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param frommsgid
	 * @param fromchatmode
	 * @param operbizdata
	 * @param appversion
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月22日 下午9:43:36
	 */
	public static Ret sendGroupMsgEachForSendRed(Short deviceTpe, String ip, String text, Short contenttype, Integer uid, Long groupid, Long frommsgid, Short fromchatmode,
	        String operbizdata, String appversion) throws Exception {
		String sessionid = "";
		return sendGroupMsgEach(deviceTpe, sessionid, ip, text, contenttype, uid, groupid, Const.YesOrNo.NO, null, operbizdata, frommsgid, fromchatmode, "", appversion);
	}

	/**
	 * 发送群聊消息-已调整
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月14日 上午10:30:10
	 */
	public static Ret sendGroupMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo,
	        String operdata, Long frommsgid, Short fromchatmode, String ats, String appversion) throws Exception {
		User user = UserService.ME.getById(uid);
		Ret checkRet = checkChatRegLimit(user, null);
		if (checkRet.isFail()) {
			return checkRet;
		}
		WxGroup group = GroupService.me.getByGroupid(groupid);
		//TODO:lixinji此处进行性能控制
		boolean isMeta = false;
		List<WxChatGroupItem> noStartMsgGroupChats = null;
		if (Objects.equals(group.getAllstartflag(), Const.YesOrNo.NO)) {
			noStartMsgGroupChats = ChatIndexService.me.getNoStartMsgGroupIndex(groupid);
			isMeta = true;
		}
		List<WxChatGroupItem> noActGroupChats = null;
		if (Objects.equals(group.getAllactflag(), Const.YesOrNo.NO)) {
			noActGroupChats = ChatIndexService.me.getNoActGroupIndex(groupid);
			isMeta = true;
		}
		WxGroupUser groupUser = GroupService.me.getGroupUser(uid, groupid);
		Ret ret = null;
		if (!isMeta) {
			ret = sendGroupMsgNoChatItemDeal(groupid, devicetype, sessionid, ip, text, contenttype, uid, sysflag, sysMsgVo, operdata, frommsgid, fromchatmode, appversion, ats,
			        groupUser);

		} else {
			ret = sendGroupMsgChatItemDeal(groupid, devicetype, sessionid, ip, text, contenttype, uid, sysflag, sysMsgVo, operdata, frommsgid, fromchatmode, appversion, ats,
			        groupUser, noActGroupChats, noStartMsgGroupChats, group, user);
		}
		if (ret.isFail()) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, uid, -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
			return ret;
		}
		WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
		List<WxChatItems> chatItemList = RetUtils.getOkTData(ret, "items");
		WxChatGroupNtfApi.me.sendGroupMsg(msg, sysMsgVo, ats, group, chatItemList, noActGroupChats, noStartMsgGroupChats, user);
		ChatIndexService.clearGroupMsgCache(groupid);
		if (Const.JPushConfig.OPENFLAG) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendGroupMsg(groupid, group, user, msg);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	public static Ret sendGroupMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Long groupid, Short sysflag, SysMsgVo sysMsgVo,
									   String operdata, Long frommsgid, Short fromchatmode, String ats, String appversion, Short updatelist, Short openforbidden, Integer touid) throws Exception {
		User user = UserService.ME.getById(uid);
		Ret checkRet = checkChatRegLimit(user, null);
		if (checkRet.isFail()) {
			return checkRet;
		}
		WxGroup group = GroupService.me.getByGroupid(groupid);
		//TODO:lixinji此处进行性能控制
		boolean isMeta = false;
		List<WxChatGroupItem> noStartMsgGroupChats = null;
		if (Objects.equals(group.getAllstartflag(), Const.YesOrNo.NO)) {
			noStartMsgGroupChats = ChatIndexService.me.getNoStartMsgGroupIndex(groupid);
			isMeta = true;
		}
		List<WxChatGroupItem> noActGroupChats = null;
		if (Objects.equals(group.getAllactflag(), Const.YesOrNo.NO)) {
			noActGroupChats = ChatIndexService.me.getNoActGroupIndex(groupid);
			isMeta = true;
		}
		WxGroupUser groupUser = GroupService.me.getGroupUser(uid, groupid);
		Ret ret = null;
		if (!isMeta) {
			ret = sendGroupMsgNoChatItemDealNew(groupid, devicetype, sessionid, ip, text, contenttype, uid, sysflag, sysMsgVo, operdata, frommsgid, fromchatmode, appversion, ats,
					groupUser);

		} else {
			ret = sendGroupMsgChatItemDealNew(groupid, devicetype, sessionid, ip, text, contenttype, uid, sysflag, sysMsgVo, operdata, frommsgid, fromchatmode, appversion, ats,
					groupUser, noActGroupChats, noStartMsgGroupChats, group, user);
		}
		if (ret.isFail()) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, uid, -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
			return ret;
		}
		WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
		List<WxChatItems> chatItemList = RetUtils.getOkTData(ret, "items");
		WxChatGroupNtfApi.me.sendGroupMsg(msg, sysMsgVo, ats, group, chatItemList, noActGroupChats, noStartMsgGroupChats, user, updatelist, openforbidden, touid);
		ChatIndexService.clearGroupMsgCache(groupid);
		if (Const.JPushConfig.OPENFLAG) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendGroupMsg(groupid, group, user, msg);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	/**
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param sysflag
	 * @param sysMsgVo
	 * @param operdata
	 * @param frommsgid
	 * @param fromchatmode
	 * @param appversion
	 * @param ats
	 * @param groupUser
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午6:28:12
	 */
	public static Ret sendGroupMsgNoChatItemDeal(Long groupid, Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Short sysflag,
	        SysMsgVo sysMsgVo, String operdata, Long frommsgid, Short fromchatmode, String appversion, String ats, WxGroupUser groupUser) {
		WxGroupMsg groupMsg = GroupService.me.addMsg(devicetype, sessionid, text, uid, ip, groupid, contenttype, frommsgid, fromchatmode, sysflag, operdata, sysMsgVo, groupUser,
		        appversion);
		if (groupMsg == null) {
			return RetUtils.failMsg("消息保存失败");
		}
		WxChatQueueApi.joinGroupSendMsgAfterQueue(groupid, groupMsg, (short) 1, ats);
		return Ret.ok().set("msg", groupMsg);
	}

	/**
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param sysflag
	 * @param sysMsgVo
	 * @param operdata
	 * @param frommsgid
	 * @param fromchatmode
	 * @param appversion
	 * @param ats
	 * @param groupUser
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午6:28:12
	 */
	public static Ret sendGroupMsgNoChatItemDealNew(Long groupid, Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Short sysflag,
												 SysMsgVo sysMsgVo, String operdata, Long frommsgid, Short fromchatmode, String appversion, String ats, WxGroupUser groupUser) {
		WxGroupMsg groupMsg = GroupService.me.addMsgNew(devicetype, sessionid, text, uid, ip, groupid, contenttype, frommsgid, fromchatmode, sysflag, operdata, sysMsgVo, groupUser,
				appversion);
		if (groupMsg == null) {
			return RetUtils.failMsg("消息保存失败");
		}
		WxChatQueueApi.joinGroupSendMsgAfterQueue(groupid, groupMsg, (short) 1, ats);
		return Ret.ok().set("msg", groupMsg);
	}

	/**
	 * 
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param sysflag
	 * @param sysMsgVo
	 * @param operdata
	 * @param frommsgid
	 * @param fromchatmode
	 * @param appversion
	 * @param ats
	 * @param groupUser
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午6:32:13
	 */
	public static Ret sendGroupMsgChatItemDeal(Long groupid, Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Short sysflag,
	        SysMsgVo sysMsgVo, String operdata, Long frommsgid, Short fromchatmode, String appversion, String ats, WxGroupUser groupUser, List<WxChatGroupItem> noActGroupChats,
	        List<WxChatGroupItem> noStartMsgGroupChats, WxGroup group, User user) {
		WxGroupMsg groupMsg = GroupService.me.addMsg(devicetype, sessionid, text, uid, ip, groupid, contenttype, frommsgid, fromchatmode, sysflag, operdata, sysMsgVo, groupUser,
		        appversion);
		if (groupMsg == null) {
			return RetUtils.failMsg("消息保存失败");
		}
		//首次接受或者发送消息处理
		if (CollectionUtil.isNotEmpty(noStartMsgGroupChats)) {
			//TODO:lixinji-修改群索引
			ChatIndexService.me.chatGroupStartMsgUpdate(groupid, groupMsg.getId());
			for (WxChatGroupItem groupItem : noStartMsgGroupChats) {
				ChatIndexService.removeChatGroupCache(groupid, groupItem.getUid());
			}
		}
		boolean actOk = true;
		List<WxChatItems> chatItemList = null;
		if (CollectionUtil.isNotEmpty(noActGroupChats)) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				//激活消息处理
				Ret ret = dealGroupAct(noActGroupChats, group, groupMsg, user);
				if (ret.isFail()) {
					log.error(RetUtils.getRetMsg(ret));
				} else {
					chatItemList = RetUtils.getOkTData(ret);
				}
				actOk = RetUtils.getOkTData(ret, "result");
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			chatItemList = new ArrayList<WxChatItems>();
		}

		if (actOk) {
			//			log.warn("出现群会话激活、显示、消息头处理逻辑：{}",groupid);
			//更新统计数据
			boolean metaUpdate = GroupService.me.updateMeta(groupid, Const.YesOrNo.YES, Const.YesOrNo.YES);
			if (!metaUpdate) {
				log.error("群统计数据修改异常");
			}
		} else {
			log.error("出现群会话激活、显示、消息头处理逻辑,groupid:{},但是激活失败，没有进行修改", groupid);
		}
		ChatIndexService.removeGroupCache(groupid);
		WxChatQueueApi.joinGroupSendMsgAfterQueue(groupid, groupMsg, (short) 1, null);
		return Ret.ok("msg", groupMsg).set("items", chatItemList);
	}



	/**
	 *
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param sysflag
	 * @param sysMsgVo
	 * @param operdata
	 * @param frommsgid
	 * @param fromchatmode
	 * @param appversion
	 * @param ats
	 * @param groupUser
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午6:32:13
	 */
	public static Ret sendGroupMsgChatItemDealNew(Long groupid, Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Short sysflag,
											   SysMsgVo sysMsgVo, String operdata, Long frommsgid, Short fromchatmode, String appversion, String ats, WxGroupUser groupUser, List<WxChatGroupItem> noActGroupChats,
											   List<WxChatGroupItem> noStartMsgGroupChats, WxGroup group, User user) {
		WxGroupMsg groupMsg = GroupService.me.addMsgNew(devicetype, sessionid, text, uid, ip, groupid, contenttype, frommsgid, fromchatmode, sysflag, operdata, sysMsgVo, groupUser,
				appversion);
		if (groupMsg == null) {
			return RetUtils.failMsg("消息保存失败");
		}
		//首次接受或者发送消息处理
		if (CollectionUtil.isNotEmpty(noStartMsgGroupChats)) {
			//TODO:lixinji-修改群索引
			ChatIndexService.me.chatGroupStartMsgUpdate(groupid, groupMsg.getId());
			for (WxChatGroupItem groupItem : noStartMsgGroupChats) {
				ChatIndexService.removeChatGroupCache(groupid, groupItem.getUid());
			}
		}
		boolean actOk = true;
		List<WxChatItems> chatItemList = null;
		if (CollectionUtil.isNotEmpty(noActGroupChats)) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				//激活消息处理
				Ret ret = dealGroupAct(noActGroupChats, group, groupMsg, user);
				if (ret.isFail()) {
					log.error(RetUtils.getRetMsg(ret));
				} else {
					chatItemList = RetUtils.getOkTData(ret);
				}
				actOk = RetUtils.getOkTData(ret, "result");
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			chatItemList = new ArrayList<WxChatItems>();
		}

		if (actOk) {
			//			log.warn("出现群会话激活、显示、消息头处理逻辑：{}",groupid);
			//更新统计数据
			boolean metaUpdate = GroupService.me.updateMeta(groupid, Const.YesOrNo.YES, Const.YesOrNo.YES);
			if (!metaUpdate) {
				log.error("群统计数据修改异常");
			}
		} else {
			log.error("出现群会话激活、显示、消息头处理逻辑,groupid:{},但是激活失败，没有进行修改", groupid);
		}
		ChatIndexService.removeGroupCache(groupid);
		WxChatQueueApi.joinGroupSendMsgAfterQueue(groupid, groupMsg, (short) 1, null);
		return Ret.ok("msg", groupMsg).set("items", chatItemList);
	}



	/**
	 * 群消息单通道发送
	 * 一般用在踢人场景下
	 * @param request
	 * @param devicetype
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param touid
	 * @param groupid
	 * @param tochatlinkid
	 * @param sysflag
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月2日 上午11:23:37
	 */
	public static Ret sendGroupMsgOnly(HttpRequest request, String text, Short contenttype, Integer uid, Integer touid, Long groupid, Long tochatlinkid, Short sysflag,
	        SysMsgVo sysMsgVo) throws Exception {
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		return sendGroupMsgOnly(devicetype, sessionid, ip, text, contenttype, uid, touid, groupid, tochatlinkid, sysflag, sysMsgVo, appversion);
	}

	/**
	 * 群消息单通道发送
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text
	 * @param contenttype
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param sysflag
	 * @param frommsgid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月2日 上午11:19:41
	 */
	public static Ret sendGroupMsgOnly(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Long groupid, Long tochatlinkid,
	        Short sysflag, SysMsgVo sysMsgVo, String appversion) throws Exception {
		WxGroupUser groupUser = GroupService.me.getGroupUser(uid, groupid);
		WxChatItems chatItems = ChatService.me.getAllChatItems(tochatlinkid);
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(chatItems);
		if (groupItem == null || groupItem.getChatlinkid() == null) {
			return RetUtils.failMsg("单通道-群索引不存在");
		}
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxGroupMsg groupMsg = GroupService.me.addMsg(devicetype, sessionid, text, uid, ip, groupid, contenttype, sysflag, touid, null, null, null, "", sysMsgVo, groupUser,
				        appversion);
				if (groupMsg == null) {
					return failRet("消息保存失败");
				}
				if (groupItem.getStartmsgid() == null) {
					ChatIndexService.me.chatGroupStartMsg(groupItem.getUid(), groupid, groupMsg.getId());
				}
				//修改下视图为显示，避免隐藏
				//				ChatService.me.updateChatView(Const.YesOrNo.YES, groupItem.getChatlinkmetaid(),null);
				if (Objects.equals(chatItems.getFocusflag(), Const.YesOrNo.YES)) {
					//已读处理
					ChatMsgService.me.afterSendGroupById(groupMsg, null, groupItem.getChatlinkmetaid(), Const.YesOrNo.YES);
				} else {
					ChatMsgService.me.afterSendGroupById(groupMsg, 1, groupItem.getChatlinkmetaid(), Const.YesOrNo.YES);
				}
				retObj = Ret.ok("msg", groupMsg);
				return true;
			}
		};

		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, tochatlinkid, AppCode.GroupErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxGroupMsg msg = RetUtils.getOkTData(atomRet, "msg");
		WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(msg, sysMsgVo);
		ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(wxGroupChatNtf));
		Ims.sendToUser(touid, otherPacket);
		ChatIndexService.clearGroupMsgCache(groupid);
		ChatIndexService.removeChatGroupCache(groupid, touid);
		ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
		ChatIndexService.removeUserCache(touid, groupid, Const.ChatMode.GROUP);
		return RetUtils.okData(msg);
	}

	/**
	 * 已调整
	 * 1、所有用户需要发送同步群列表
	 * 2、激活用户需要发送删除消息
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param groupItem
	 * @param group
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月24日 上午11:36:48
	 */
	public static Ret delGroup(HttpRequest request, User curr, WxChatGroupItem groupItem, WxGroup group, WxGroupUser groupUser) throws Exception {
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		Long groupid = group.getId();
		Long chatlinkid = groupItem.getChatlinkid();
		String sessionid = request.getHttpSession().getId();
		Integer uid = curr.getId();
		String ip = request.getClientIp();
		List<WxChatGroupItem> linkGroupItems = ChatIndexService.me.getGroupLinkItems(groupid);
		//		List<WxChatItems> hideItem = ChatService.me.getHideGroupItems(groupid);
		Short chatmode = Const.ChatMode.GROUP;
		List<Integer> noActUid = new ArrayList<Integer>();
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.delgroup, "", "delgroup");
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxGroupMsg groupMsg = GroupService.me.addSysMsg(devicetype, sessionid, sysMsgVo.toText(), uid, ip, groupid, groupid + "", sysMsgVo, groupUser, appversion);
				if (groupMsg == null) {
					return failRet("消息保存失败");
				}
				//				if(CollectionUtil.isNotEmpty(hideItem)) {
				//					//修改下视图为显示，避免隐藏
				//					ChatService.me.updateGroupChatView(groupid);
				//				}
				//未读处理
				ChatMsgService.me.delGroupChatMsg(groupMsg, 1, Const.YesOrNo.YES);
				//已读处理
				ChatMsgService.me.delGroupChatMsg(groupMsg, null, Const.YesOrNo.YES);
				//数据处理-此处性能需要优化-优先级比较低
				if (CollectionUtil.isNotEmpty(linkGroupItems)) {
					for (WxChatGroupItem groupItem : linkGroupItems) {
						Long otherLinkid = groupItem.getChatlinkid();
						Integer otheruid = groupItem.getUid();
						if (otherLinkid == null) {
							//群成员用户索引删除-整个群的删除整合处理
							ChatIndexService.me.chatUserIndexDel(otheruid, groupid, chatmode);
							noActUid.add(otheruid);
							//							Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid + "", groupid + "");
						} else {
							//更新用户索引，缓存刷新/会话索引缓存刷新
							ChatIndexService.me.chatUserIndexUpdate(otheruid, groupid, chatmode, Const.YesOrNo.NO, null, null, null, null, null);
							ChatIndexService.removeChatItemsCache(otherLinkid);
							ChatIndexService.clearChatUserIndex(otheruid, groupid, chatmode);
						}
						ChatIndexService.clearMailListCache(otheruid);
					}
				}
				//删除所有未激活的群索引
				ChatIndexService.me.chatGroupIndexDelNoAct(groupid);
				//修改所有激活的群所有
				ChatIndexService.me.chatGroupIndexUpdateAllLink(groupid);
				ChatService.me.updateActItemLink(groupid, Const.YesOrNo.NO);
				return okRet(groupMsg);
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, uid, chatlinkid, AppCode.GroupErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		//自己发送操作信息
		Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, uid + "", groupid + "");
		WxGroupMsg msg = RetUtils.getOkTData(atomRet);
		WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(msg, sysMsgVo);
		ChatIndexService.clearMailListCache(curr.getId());
		//发送最后一条通知消息
		ImPacket imPacket = new ImPacket(Command.WxGroupChatNtf, wxGroupChatNtf);
		Ims.sendToGroup(groupid, imPacket);
		//		if(CollectionUtil.isNotEmpty(hideItem)) {
		//			WxGroupChatNtf actNtf = WxGroupChatNtf.from(msg,sysMsgVo);
		//			actNtf.setActflag(Const.YesOrNo.YES);
		//			actNtf.setActname(group.getName());
		//			actNtf.setJoinnum(group.getJoinnum());
		//			actNtf.setActavatar(group.getAvatar());
		//			for(WxChatItems hItem : hideItem) {
		//				actNtf.setGrouprole(hItem.getBizrole());
		//				ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(actNtf));
		//				Ims.sendToUser(hItem.getUid(), otherPacket); 
		//				ChatIndexService.removeChatGroupCache(groupid, hItem.getUid());
		//				ChatIndexService.removeUserCache(hItem.getUid(), groupid, Const.ChatMode.GROUP);
		//			}
		//		}
		ChatIndexService.clearGroupMsgCache(groupid);
		//其他所有人发送操作通知：1：设置会话linkflag=2;2:同步群通讯录
		if (CollectionUtil.isNotEmpty(linkGroupItems)) {
			WxGroupOperNtf groupOper = WxGroupOperNtf.from(msg);
			groupOper.setOper(Const.WxGroupOper.MEMBER_DEL_GROUP);
			WxChatItems chatItems = new WxChatItems();
			chatItems.setId(-groupid);
			chatItems.setChatlinkid(-groupid);
			chatItems.setLinkflag(Const.YesOrNo.NO);
			groupOper.setChatItems(chatItems);
			groupOper.setGrouprole(Const.GroupRole.MEMBER);
			//发送操作消息
			ImPacket operPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
			Ims.sendToGroup(groupid, operPacket);
			for (WxChatGroupItem item : linkGroupItems) {
				Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, item.getUid() + "", groupid + "");
			}
			//自己已删除-发送操作消息
			groupOper.setGrouprole(Const.GroupRole.OWNER);
			groupOper.setOper(Const.WxGroupOper.DEL_GROUP);
			ImPacket ownOperPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
			Ims.sendToUser(uid, ownOperPacket);
		}
		return Ret.ok();
	}

	/**
	 * 变更群权限通知-已调整
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param owner
	 * @param other
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月25日 上午9:44:59
	 */
	public static Ret changeOwner(HttpRequest request, User curr, WxChatGroupItem owner, WxChatGroupItem other, boolean groupAuto) throws Exception {
		Integer otheruid = other.getUid();
		User otheruser = UserService.ME.getById(otheruid);
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.ownerchange, otheruser.getNick(), "ownerchange");
		Ret ret = sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), owner.getGroupid(), Const.YesOrNo.YES, sysMsgVo);
		WxGroupMsg msg = RetUtils.getOkTData(ret);
		WxGroupOperNtf groupOper = WxGroupOperNtf.from(msg);
		//发送操作消息-接受方
		groupOper.setC("你现在是群主");
		WxChatItems chatItems = new WxChatItems();
		chatItems.setId(-other.getGroupid());
		chatItems.setChatlinkid(-other.getGroupid());
		chatItems.setBizrole(Const.GroupRole.OWNER);
		groupOper.setChatItems(chatItems);
		groupOper.setOper(Const.WxGroupOper.CHANGE_IN_GROUP);
		ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
		Ims.sendToUser(other.getUid(), imPacket);
		//发送操作消息-转让方
		groupOper.setOper(Const.WxGroupOper.CHANGE_OUT_GROUP);
		WxChatItems chatItems1 = new WxChatItems();
		chatItems1.setId(-other.getGroupid());
		chatItems1.setChatlinkid(-other.getGroupid());
		chatItems1.setBizrole(Const.GroupRole.MANAGER);
		groupOper.setChatItems(chatItems);
		groupOper.setC("你转让了群主");
		ImPacket outImPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
		Ims.sendToUser(owner.getUid(), outImPacket);
		if (groupAuto) {
			WxGroup group = GroupService.me.getByGroupid(owner.getGroupid());
			WxChatApi.autoUpdateGroupInfo(request, Devicetype.SYS_TASK.getValue(), curr, owner.getGroupid(), group.getName(), group.getAvatar(), true);
		}
		return ret;
	}

	/**
	 * TODO-lixinji-750-进群通知（2）-已调整
	 * 进群通知
	 * 1、此处消息会有延迟
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param groupid
	 * @param nickStr
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月25日 下午1:44:02
	 */
	public static Ret joinGroup(HttpRequest request, User curr, Long groupid, String nickStr, List<WxChatGroupItem> rebindList, boolean groupauto) throws Exception {
		Integer uid = curr.getId();
		WxGroup group = GroupService.me.getByGroupid(groupid);
		Map<String, String> exist = new HashMap<String, String>();
		//一般很少重新绑定的功能，所有放在前面
		if (CollectionUtil.isNotEmpty(rebindList)) {
			for (WxChatGroupItem groupItem : rebindList) {
				//重新绑定的命令
				if (groupItem.getChatlinkid() != null) {
					ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
					WxGroupOperNtf ret = new WxGroupOperNtf();
					ret.setC("重新加入群，进行同步信息");
					ret.setMid(null);
					ret.setT(System.currentTimeMillis());
					ret.setUid(uid);
					ret.setG(groupid);
					ret.setChatlinkid(-groupid);
					ret.setBizdata(group.getJoinnum() + "");
					WxChatItems chatItems = new WxChatItems();
					chatItems.setId(-groupid);
					chatItems.setChatlinkid(-groupid);
					chatItems.setJoinnum(group.getJoinnum());
					chatItems.setLinkflag(Const.YesOrNo.YES);
					ret.setChatItems(chatItems);
					ret.setOper(Const.WxGroupOper.RE_JOIN_GROUP);
					ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
					Ims.sendToUser(groupItem.getUid(), imPacket);
					Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, groupItem.getUid() + "", groupid + "");
					exist.put(groupItem.getUid() + "", groupItem.getUid() + "");
				}
				ChatIndexService.clearChatUserIndex(groupItem.getUid(), groupItem.getGroupid(), Const.ChatMode.GROUP);
			}
		}
//		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.join, nickStr, "join");
		//发送入群通知
//		Ret ret = sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					//新版本会话逻辑，可以进行消息发送优化-TODO:lixinji
					WxGroupOperNtf ntf = new WxGroupOperNtf();
					ntf.setC("更新群用户数量");
					ntf.setMid(null);
					ntf.setT(System.currentTimeMillis());
					ntf.setUid(uid);
					ntf.setG(groupid);
					ntf.setChatlinkid(-groupid);
					ntf.setBizdata(group.getJoinnum() + "");
					WxChatItems chatItems = new WxChatItems();
					chatItems.setId(-groupid);
					chatItems.setChatlinkid(-groupid);
					chatItems.setJoinnum(group.getJoinnum());
					ntf.setChatItems(chatItems);
					ntf.setOper(Const.WxGroupOper.UPDATE_JOINNUM);
					ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ntf));
					Ims.sendToGroup(groupid, imPacket);
					if (groupauto) {
						//此处可进行消息合并-需要前台合作
						WxChatApi.autoUpdateGroupInfo(request, Devicetype.SYS_TASK.getValue(), curr, groupid, group.getName(), group.getAvatar(), true);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return RetUtils.okOper();
	}

	/**
	 * TODO-lixinji-750-踢人通知（2）
	 * 群踢人通知
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param groupid
	 * @param kickGroupItem
	 * @param nickStr
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年3月2日 上午10:09:51
	 */
	public static Ret kickGroup(HttpRequest request, User curr, Long groupid, List<WxChatGroupItem> kickGroupItem, String nickStr, boolean groupAuto) throws Exception {
		Integer uid = curr.getId();
		WxGroup group = GroupService.me.getByGroupid(groupid);
		if (CollectionUtil.isNotEmpty(kickGroupItem)) {
			for (WxChatGroupItem groupItem : kickGroupItem) {
				//发出群数据更新操作
				ChatIndexService.clearChatUserIndex(groupItem.getUid(), groupItem.getGroupid(), Const.ChatMode.GROUP);
				WxGroupOperNtf ret = new WxGroupOperNtf();
				ret.setC("被踢出群");
				ret.setMid(null);
				ret.setT(System.currentTimeMillis());
				ret.setUid(uid);
				ret.setG(groupid);
				ret.setChatlinkid(-groupid);
				WxChatItems chatItems = new WxChatItems();
				chatItems.setId(-groupid);
				chatItems.setChatlinkid(-groupid);
				chatItems.setLinkflag(Const.YesOrNo.NO);
				ret.setChatItems(chatItems);
				ret.setOper(Const.WxGroupOper.KICK_GROUP);
				ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
				Ims.sendToUser(groupItem.getUid(), imPacket);
				if (groupItem.getChatlinkid() != null) {
					User touser = UserService.ME.getById(groupItem.getUid());
					SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.tokick, touser.getNick(), "tokick");
					//发送最后一条被踢数据
					sendGroupMsgOnly(request, sysMsgVo.toText(), Const.ContentType.TEXT, uid, groupItem.getUid(), groupid, groupItem.getChatlinkid(), Const.YesOrNo.YES, sysMsgVo);
				}
				Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, groupItem.getUid() + "", groupid + "");
			}
		}
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.operkick, nickStr, "operkick");
		Ret ret = sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
		//TODO：lixinji同步通知，需要后续进行扩展优化，可改为所有人发送，速度会快一些
		WxGroupOperNtf retntf = new WxGroupOperNtf();
		retntf.setC("更新群用户数量");
		retntf.setMid(null);
		retntf.setT(System.currentTimeMillis());
		retntf.setUid(uid);
		retntf.setG(groupid);
		retntf.setChatlinkid(-groupid);
		retntf.setBizdata(group.getJoinnum() + "");
		WxChatItems chatItems = new WxChatItems();
		chatItems.setId(-groupid);
		chatItems.setChatlinkid(-groupid);
		chatItems.setJoinnum(group.getJoinnum());
		retntf.setChatItems(chatItems);
		retntf.setOper(Const.WxGroupOper.UPDATE_JOINNUM);
		ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(retntf));
		Ims.sendToGroup(groupid, imPacket);
		if (groupAuto) {
			WxChatApi.autoUpdateGroupInfo(request, Devicetype.SYS_TASK.getValue(), curr, groupid, group.getName(), group.getAvatar(), false);
		}
		List<WxChatGroupItem> linkActGroupItems = ChatIndexService.me.getLinkActGroupIndex(groupid);
		//TODO:XUFIE-此处是否需要进行缓存更新，需要跟前端沟通，已减少服务器的压力
		if (CollectionUtil.isNotEmpty(linkActGroupItems)) {
			for (WxChatGroupItem groupItem : linkActGroupItems) {
				//通知群人员发送变更
				ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
			}
		}
		return ret;
	}

	/**
	 * @param request
	 * @param curr
	 * @param groupid
	 * @param uid
	 * @param grouprole
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年1月13日 下午3:19:31
	 */
	public static Ret manager(HttpRequest request, User curr, Long groupid, Integer uid, Short grouprole) throws Exception {
		Integer operuid = curr.getId();
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
		if (groupItem == null) {
			log.error("用户群索引为空，uid:{},groupid:{}", uid, groupid);
			return RetUtils.failMsg("用户群索引为空");
		}
		WxChatGroupItem operGroupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
		if (operGroupItem == null) {
			log.error("群主群索引为空，uid:{},groupid:{}", uid, groupid);
			return RetUtils.failMsg("群主群索引为空");
		}
		Long chatlinkid = groupItem.getChatlinkid();
		if (chatlinkid == null) {
			Ret actRet = ChatService.me.actGroupChatItems(groupid, uid);
			if (actRet.isFail()) {
				log.error("会话激活失败-群聊，uid:{},groupid:{}", uid, groupid);
				return RetUtils.failMsg("会话激活失败-群聊");
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
				}
			}
			chatlinkid = RetUtils.getOkTData(actRet, "chatlinkid");
		} else {
			//发出群数据更新操作
			WxGroupOperNtf ret = new WxGroupOperNtf();
			ret.setC("用户更新了群角色");
			ret.setMid(null);
			ret.setT(System.currentTimeMillis());
			ret.setUid(uid);
			ret.setG(groupid);
			ret.setChatlinkid(-groupid);
			WxChatItems chatItems = new WxChatItems();
			chatItems.setId(chatlinkid);
			chatItems.setBizrole(grouprole);
			chatItems.update();
			ChatIndexService.removeChatItemsCache(chatlinkid);
			chatItems.setId(-groupid);
			chatItems.setChatlinkid(-groupid);
			ret.setChatItems(chatItems);
			ret.setOper(Const.WxGroupOper.UPDATE_GROUP_ROLE);
			ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
			Ims.sendToUser(groupItem.getUid(), imPacket);
		}
		Long operChatlinkid = operGroupItem.getChatlinkid();
		if (operChatlinkid == null) {
			Ret actRet = ChatService.me.actGroupChatItems(groupid, operuid);
			if (actRet.isFail()) {
				log.error("会话激活失败-群聊，uid:{},groupid:{}", operuid, groupid);
				return RetUtils.failMsg("会话激活失败-群聊");
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(operuid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(operuid, RetUtils.getOkTData(actRet, "chat"));
				}
			}
			operChatlinkid = RetUtils.getOkTData(actRet, "chatlinkid");
		}

		User user = UserService.ME.getById(uid);
		String opertext = "你设置 " + user.getNick() + " 为管理员";
		String text = "你已被设为管理员";
		if (Objects.equals(grouprole, Const.GroupRole.MEMBER)) {
			opertext = "你取消了 " + user.getNick() + " 的管理员";
			text = "你已被取消管理员";
		}
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenGroupUpdateManagerNotify'");
		if (clientConf.getValue().equals(1)) {
			//群主发送消息
			sendGroupMsgOnly(request, opertext, Const.ContentType.TEXT, operuid, operuid, groupid, operChatlinkid, Const.YesOrNo.YES, null);
			//成员发送消息
			sendGroupMsgOnly(request, text, Const.ContentType.TEXT, operuid, uid, groupid, chatlinkid, Const.YesOrNo.YES, null);
		}
		return RetUtils.okOper();
	}

	/**
	 * @param request
	 * @param curr
	 * @param groupid
	 * @param count
	 * @param apply
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年1月13日 下午5:09:06
	 */
	public static Ret joinGroupApply(HttpRequest request, User curr, WxGroup group, Integer count, WxGroupApply apply) throws Exception {
		if (count == null || apply == null || count <= 0 || group == null) {
			return RetUtils.invalidParam();
		}
		Long groupid = group.getId();
		//操作人处理
		WxChatGroupItem operGroupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
		if (operGroupItem == null) {
			log.error("操作人群索引为空，uid:{},groupid:{}", curr.getId(), groupid);
			return RetUtils.failMsg("操作人群索引为空");
		}
		Long operChatlinkid = operGroupItem.getChatlinkid();
		if (operChatlinkid == null) {
			Ret actRet = ChatService.me.actGroupChatItems(groupid, curr.getId());
			if (actRet.isFail()) {
				log.error("会话激活失败-群聊，uid:{},groupid:{}", curr.getId(), groupid);
				return RetUtils.failMsg("会话激活失败-群聊");
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(curr.getId(), RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(curr.getId(), RetUtils.getOkTData(actRet, "chat"));
				}
			}
			operChatlinkid = RetUtils.getOkTData(actRet, "chatlinkid");
		}
		//操作人发送消息
		sendGroupMsgOnly(request, "群聊邀请已发送，请等待管理员确认", Const.ContentType.TEXT, curr.getId(), curr.getId(), groupid, operChatlinkid, Const.YesOrNo.YES, null);
		WxGroupApplyVo applyVo = new WxGroupApplyVo();
		applyVo.setId(apply.getId());
		applyVo.setApplymsg(apply.getApplymsg());
		applyVo.setGroupid(groupid);
		applyVo.setOperuid(curr.getId());
		applyVo.setStatus(apply.getStatus());
		String text = Json.toJson(applyVo);
		//群主处理
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(group.getUid(), groupid);
		if (groupItem == null) {
			log.error("群主群索引为空，uid:{},groupid:{}", group.getUid(), groupid);
			return RetUtils.failMsg("群主群索引为空");
		}
		Long chatlinkid = groupItem.getChatlinkid();
		if (chatlinkid == null) {
			Ret actRet = ChatService.me.actGroupChatItems(groupid, group.getUid());
			if (actRet.isFail()) {
				log.error("会话激活失败-群聊，uid:{},groupid:{}", group.getUid(), groupid);
				return RetUtils.failMsg("会话激活失败-群聊");
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(group.getUid(), RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(group.getUid(), RetUtils.getOkTData(actRet, "chat"));
				}
			}
			chatlinkid = RetUtils.getOkTData(actRet, "chatlinkid");
		}
		//群主发送消息
		sendGroupMsgOnly(request, text, Const.ContentType.GROUP_APPLY, curr.getId(), group.getUid(), groupid, chatlinkid, Const.YesOrNo.NO, null);
		List<WxChatGroupItem> managerList = ChatIndexService.me.getManagerGroupIndex(groupid);
		if (CollectionUtil.isNotEmpty(managerList)) {
			for (WxChatGroupItem manger : managerList) {
				Long mangerChatlinkId = manger.getChatlinkid();
				if (mangerChatlinkId == null) {
					Ret actRet = ChatService.me.actGroupChatItems(groupid, manger.getUid());
					if (actRet.isFail()) {
						log.error("会话激活失败-群聊，uid:{},groupid:{}", manger.getUid(), groupid);
						return RetUtils.failMsg("会话激活失败-群聊");
					} else {
						if (WxSynApi.isSynVersion()) {
							WxSynApi.synChatSession(manger.getUid(), RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
						} else {
							WxChatApi.userActOper(manger.getUid(), RetUtils.getOkTData(actRet, "chat"));
						}
					}
					mangerChatlinkId = RetUtils.getOkTData(actRet, "chatlinkid");
				}
				//管理员发送消息
				sendGroupMsgOnly(request, text, Const.ContentType.GROUP_APPLY, curr.getId(), manger.getUid(), groupid, mangerChatlinkId, Const.YesOrNo.NO, null);
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * TODO-lixinji-750-离开群通知-已调整
	 * 主动离开群通知
	 * @param request
	 * @param devicetype
	 * @param curr
	 * @param groupid
	 * @param nickStr
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月27日 上午11:30:29
	 */
	public static boolean leaveGroup(HttpRequest request, User curr, WxChatGroupItem leave, WxChatGroupItem newOwner, boolean groupAuto) throws Exception {
		Integer uid = curr.getId();
		Long groupid = leave.getGroupid();
		WxGroup group = GroupService.me.getByGroupid(groupid);
		SysMsgVo nomarl = new SysMsgVo(curr.getNick(), MsgTemplate.leave, "", "leave");
		boolean sysSend = false;
		if (newOwner != null) {
			User newuUser = UserService.ME.getById(newOwner.getUid());
			Integer newUid = newOwner.getUid();
			if (newuUser != null) {//群主退群，设置新的群主消息
				SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.ownerleave, newuUser.getNick(), "ownerleave");
				Ret ret = sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
				sysSend = true;
				WxGroupMsg msg = RetUtils.getOkTData(ret);
				WxGroupOperNtf groupOper = WxGroupOperNtf.from(msg);
				//新群主操作通知
				groupOper.setC("你现在是群主");
				groupOper.setOper(Const.WxGroupOper.CHANGE_IN_GROUP);
				ImPacket change = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
				Ims.sendToUser(newUid, change);
			}
		}
		if (!sysSend) {
			//非群主退群的正常 
			sendGroupMsgEach(request, nomarl.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, nomarl, null, null);
		}
		//此处少了所有会话的人数变化缓存处理，需要进行前端配合，否则数据30秒内是不准确的
		WxGroupOperNtf ret = new WxGroupOperNtf();
		ret.setC("更新群用户数量");
		ret.setMid(null);
		ret.setT(System.currentTimeMillis());
		ret.setUid(uid);
		ret.setG(groupid);
		ret.setBizdata(group.getJoinnum() + "");
		WxChatItems chatItems = new WxChatItems();
		chatItems.setId(-groupid);
		chatItems.setChatlinkid(-groupid);
		chatItems.setJoinnum(group.getJoinnum());
		ret.setChatItems(chatItems);
		ret.setOper(Const.WxGroupOper.UPDATE_JOINNUM);
		ret.setChatlinkid(-groupid);
		ImPacket updateJoinNum = new ImPacket(Command.WxGroupOperNtf, Json.toJson(ret));
		Ims.sendToGroup(groupid, updateJoinNum);
		if (groupAuto) {
			WxChatApi.autoUpdateGroupInfo(request, Devicetype.SYS_TASK.getValue(), curr, groupid, group.getName(), group.getAvatar(), false);
		}
		List<WxChatGroupItem> linkActGroupItems = ChatIndexService.me.getLinkActGroupIndex(groupid);
		//TODO:XUFIE-此处是否需要进行缓存更新，需要跟前端沟通，已减少服务器的压力
		if (CollectionUtil.isNotEmpty(linkActGroupItems)) {
			for (WxChatGroupItem groupItem : linkActGroupItems) {
				//通知群人员发送变更
				ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
			}
		}
		return true;
	}

	/**
	 * 群会话激活抽象方法-已调整
	 * @param noActGroupChats
	 * @param group
	 * @param groupMsg
	 * @param user
	 * @return
	 * @author lixinji
	 * 2020年2月25日 上午10:06:35
	 */
	public static Ret dealGroupAct(List<WxChatGroupItem> noActGroupChats, WxGroup group, WxGroupMsg groupMsg, User user) {
		boolean ok = true;
		Long groupid = group.getId();
		List<WxChatItems> chatItemList = new ArrayList<WxChatItems>();
		//激活消息处理
		if (CollectionUtil.isNotEmpty(noActGroupChats)) {
			for (WxChatGroupItem groupItem : noActGroupChats) {
				try {
					WxChatItems chatItem = new WxChatItems();
					chatItem.setUid(groupItem.getUid());
					chatItem.setBizid(groupItem.getGroupid());
					chatItem.setLinkid(groupItem.getGpulinkid());
					chatItem.setChatmode(Const.ChatMode.GROUP);
					chatItem.setBizrole(groupItem.getGrouprole());
					chatItem.setAvatar(group.getAvatar());
					chatItem.setName(group.getName());
					chatItem.setStartmsgid(groupMsg.getId());
					chatItem.setJoinnum(group.getJoinnum());
					chatItem.setMsgfreeflag(groupItem.getMsgfreeflag());
					boolean chatsave = chatItem.save();
					if (!chatsave) {
						log.error("会话初始化异常：{}", Json.toJson(chatItem));
						continue;
					}
					WxChatItemsMeta meta = new WxChatItemsMeta();
					meta.setUid(groupItem.getUid());
					meta.setBizid(groupItem.getGroupid());
					meta.setChatmode(Const.ChatMode.GROUP);
					meta.setChatlinkid(chatItem.getId());
					meta.setLastmsgid(groupMsg.getId());
					meta.setLastmsguid(groupItem.getUid());
					meta.setFromnick(groupMsg.getNick());
					meta.setSysflag(groupMsg.getSendbysys());
					meta.setMsgresume(groupMsg.getResume());
					meta.setMsgtype(groupMsg.getContenttype());
					meta.setSendtime(groupMsg.getTime());
					meta.setNotreadstartmsgid(groupMsg.getId());
					meta.setNotreadcount(1);
					meta.setChatuptime(new Date());
					boolean chatmetasave = meta.save();
					if (!chatmetasave) {
						log.error("会话动态数据初始化异常：{}", Json.toJson(meta));
						continue;
					}
					ChatIndexService.me.actGroupToUserIndex(groupid, groupItem.getUid(), chatItem.getId(), meta.getId(), groupMsg.getId());
					ChatIndexService.me.actUserGroup(groupid, groupItem.getUid(), chatItem.getId(), meta.getId(), groupMsg.getId());
					chatItemList.add(chatItem);
				} catch (Exception e) {
					log.error("", e);
					log.error("激活失败：{}", Json.toJson(groupItem));
					ok = false;
					continue;
				}
			}
		}
		return RetUtils.okData(chatItemList).set("result", ok);
	}

	/**
	 * TODO-lixinji-738-系统自动通知
	 * 自动调整方法
	 * 1、群头像
	 * 2、群昵称
	 * 3、好友头像
	 * 4、好友昵称
	 * 5、同步消息变更
	 * @return
	 * @author lixinji
	 * 2020年3月24日 下午6:17:37
	 */
	public static boolean synUserInfoToIm() {
		List<UserInfoSyn> userInfoSyns = UserInfoSyn.dao.findAll();
		if (CollectionUtil.isNotEmpty(userInfoSyns)) {
			//同步群头像和群昵称
			//修改群用户头像
			String groupUserAvatarSql = "update wx_group_user gu " + "INNER JOIN  user_info_syn us on gu.uid = us.uid and us.type = ? and us.`status` = ? "
			        + "set gu.groupavator = us.bizstr";
			//修改群用户昵称
			String groupUserNickSql = "update wx_group_user gu " + "INNER JOIN  user_info_syn us on gu.uid = us.uid and us.type = ? and us.`status` = ? "
			        + "set gu.srcnick = us.bizstr,gu.groupnick = us.bizstr where gu.autoflag = ?";
			//修改群用户非自动昵称
			String groupAutoUserNickSql = "update wx_group_user gu " + "INNER JOIN  user_info_syn us on gu.uid = us.uid and us.type = ? and us.`status` = ? "
			        + "set gu.srcnick = us.bizstr where gu.autoflag = ?";
			//同步群消息昵称和头像
			String msgNickSql = "update wx_group_msg gu " + "INNER JOIN wx_group_user guer on guer.uid = gu.uid " + "and guer.groupid = gu.groupid and guer.autoflag = ? "
			        + "INNER JOIN user_info_syn us on gu.uid = us.uid and us.type = ? and us.`status` = ?  " + "set gu.nick = us.bizstr ";
			String msgAutoNickSql = "update wx_group_msg gu " + "INNER JOIN wx_group_user guer on guer.uid = gu.uid " + "and guer.groupid = gu.groupid and guer.autoflag = ? "
			        + "INNER JOIN user_info_syn us on gu.uid = us.uid and gu.groupid = us.bizbigint and us.type = ? and us.`status` = ?  " + "set gu.nick = us.bizstr ";
			String msgAvatarSql = "update wx_group_msg gu " +
			//					"INNER JOIN wx_group_user guer on guer.uid = gu.uid and guer.groupid = gu.groupid and guer.autoflag = ? " + 
			        "INNER JOIN wx_group_user guer on guer.uid = gu.uid and guer.groupid = gu.groupid "
			        + "INNER JOIN user_info_syn us on gu.uid = us.uid and us.type = ? and us.`status` = ?  " + "set gu.avatar = us.bizstr ";
			Db.use(Const.Db.TIO_SITE_MAIN).update(groupUserAvatarSql, Const.UserToImSynType.AVATAR, Const.Status.NORMAL);
			Db.use(Const.Db.TIO_SITE_MAIN).update(groupUserNickSql, Const.UserToImSynType.NICK, Const.Status.NORMAL, Const.YesOrNo.YES);
			Db.use(Const.Db.TIO_SITE_MAIN).update(groupAutoUserNickSql, Const.UserToImSynType.NICK, Const.Status.NORMAL, Const.YesOrNo.NO);
			//修改群消息同步
			Db.use(Const.Db.TIO_SITE_MAIN).update(msgNickSql, Const.YesOrNo.YES, Const.UserToImSynType.NICK, Const.Status.NORMAL);
			Db.use(Const.Db.TIO_SITE_MAIN).update(msgAutoNickSql, Const.YesOrNo.NO, Const.UserToImSynType.GROUP_NICK, Const.Status.NORMAL);
			Db.use(Const.Db.TIO_SITE_MAIN).update(msgAvatarSql, Const.UserToImSynType.AVATAR, Const.Status.NORMAL);
			Map<String, WxChatUserItem> change = new HashMap<String, WxChatUserItem>();
			for (UserInfoSyn infoSyn : userInfoSyns) {
				if (Objects.equals(infoSyn.getStatus(), Const.Status.DISABLED)) {
					log.error("用户信息同步，二次处理，uid:{},type:{},newstr:{}", infoSyn.getUid(), infoSyn.getType(), infoSyn.getBizstr());
				}
				infoSyn.setStatus(Const.Status.DISABLED);
				infoSyn.setIp(Const.MY_IP);
				infoSyn.update();
				List<WxFriend> friends = WxFriend.dao.find("select uid,frienduid,remarkname from wx_friend where frienduid = ? and uid != frienduid", infoSyn.getUid());
				if (CollectionUtil.isNotEmpty(friends)) {
					for (WxFriend friend : friends) {
						WxChatUserItem chatUserItem = ChatIndexService.fdUserIndex(friend.getUid(), friend.getFrienduid());
						if (chatUserItem == null) {
							continue;
						}
						boolean isDeal = change.get(chatUserItem.getUid() + "_" + chatUserItem.getBizid()) != null;
						if (ChatService.existTwoFriend(chatUserItem) && chatUserItem.getChatlinkid() != null) {
							switch (infoSyn.getType()) {
							case Const.UserToImSynType.NICK:
								if (StrUtil.isBlank(friend.getRemarkname())) {
									ChatService.me.updateChatItemById(chatUserItem.getChatlinkid(), infoSyn.getBizstr(), "",
									        Objects.equals(chatUserItem.getChatmode(), Const.ChatMode.GROUP) ? chatUserItem.getBizid() : null);
								}
								break;
							case Const.UserToImSynType.AVATAR:
								ChatService.me.updateChatItemById(chatUserItem.getChatlinkid(), "", infoSyn.getBizstr(),
								        Objects.equals(chatUserItem.getChatmode(), Const.ChatMode.GROUP) ? chatUserItem.getBizid() : null);
								break;
							default:
								break;
							}
							if (!isDeal) {
								change.put(chatUserItem.getUid() + "_" + chatUserItem.getBizid(), chatUserItem);
							}
						}
					}
				}
				infoSyn.delete();
			}
			if (MapUtil.isNotEmpty(change)) {
				for (String key : change.keySet()) {
					WxChatUserItem chatUserItem = change.get(key);
					ChatIndexService.removeChatItemsCache(chatUserItem.getChatlinkid());
					ChatIndexService.clearFriendInfoCache(chatUserItem.getLinkid(), chatUserItem.getUid());
					User friendUser = UserService.ME.getById(chatUserItem.getBizid().intValue());
					ChatIndexService.clearMailListCache(chatUserItem.getUid());
					WxChatItems chatItems = ChatService.me.getAllChatItems(chatUserItem.getChatlinkid());
					autoUseSysChatNtf(chatUserItem.getUid(), Const.WxSysCode.FRIEND_INFO_UPDATE, "好友信息发生变更", Json.toJson(friendUser), chatItems);
				}
			}
		}
		return true;
	}

	/**
	 * 修改自己的同步信息
	 * 自己和自己的聊天会话信息
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年9月28日 上午10:53:41
	 */
	public static void synUserInfoToSelf(Integer uid, Short type, User user) {
		try {
			WxChatUserItem chatUserItem = ChatIndexService.fdUserIndex(uid, uid);
			ChatIndexService.removeChatItemsCache(chatUserItem.getChatlinkid());
			if (chatUserItem.getChatlinkid() != null) {
				switch (type) {
				case Const.UserToImSynType.NICK:
					ChatService.me.updateChatItemById(chatUserItem.getChatlinkid(), user.getNick(), "",
					        Objects.equals(chatUserItem.getChatmode(), Const.ChatMode.GROUP) ? chatUserItem.getBizid() : null);
					break;
				case Const.UserToImSynType.AVATAR:
					ChatService.me.updateChatItemById(chatUserItem.getChatlinkid(), "", user.getAvatar(),
					        Objects.equals(chatUserItem.getChatmode(), Const.ChatMode.GROUP) ? chatUserItem.getBizid() : null);
					break;
				case Const.UserToImSynType.USER_ALL:
					ChatService.me.updateChatItemById(chatUserItem.getChatlinkid(), user.getNick(), user.getAvatar(),
					        Objects.equals(chatUserItem.getChatmode(), Const.ChatMode.GROUP) ? chatUserItem.getBizid() : null);
					break;
				default:
					break;
				}
			}
			ChatIndexService.clearFriendInfoCache(chatUserItem.getLinkid(), chatUserItem.getUid());
			ChatIndexService.clearMailListCache(uid);
			WxChatItems chatItems = ChatService.me.getAllChatItems(chatUserItem.getChatlinkid());
			autoUseSysChatNtf(uid, Const.WxSysCode.FRIEND_INFO_UPDATE, "好友信息发生变更", Json.toJson(user), chatItems);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 清除群消息
	 * @param uid
	 * @param groupid
	 * @param groupnick
	 * @author lixinji
	 * 2020年12月25日 下午2:12:33
	 */
	public static void synGroupInfo(Integer uid, Long groupid, String groupnick) {
		try {
			Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_group_msg set nick = ? where groupid = ? and uid = ?", groupnick, groupid, uid);

			ChatIndexService.clearGroupMsgCache(groupid);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 清空自己的缓存信息
	 * @param uid
	 * @param type
	 * @param user
	 * @author lixinji
	 * 2020年12月25日 上午10:12:13
	 */
	public static void synUserInfoToSelfAllInfo(Integer uid, Short type, User user) {
		try {
			List<WxChatUserItem> chatUserItems = ChatIndexService.me.getLinkUserIndex(uid);
			if (CollectionUtil.isNotEmpty(chatUserItems)) {
				Kv params = Kv.by("uid", uid);
				switch (type) {
				case Const.UserToImSynType.NICK:
					params.set("setnick", "setnick").set("nick", user.getNick()).set("yes", Const.YesOrNo.YES);
					SqlPara nickSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.synAutoGroupMsgByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(nickSqlPara);

					SqlPara userNickSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.synAutoGroupUserByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(userNickSqlPara);
					break;
				case Const.UserToImSynType.AVATAR:
					params.clear();
					params.set("uid", uid).set("setavatar", "setavatar").set("avatar", user.getAvatar());
					SqlPara avatarSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.synAutoGroupMsgByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(avatarSqlPara);
					SqlPara userAvatarSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.synAutoGroupUserByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(userAvatarSqlPara);
					break;
				case Const.UserToImSynType.USER_ALL:
					params.clear();
					params.set("uid", uid).set("setavatar", "setavatar").set("avatar", user.getAvatar());
					SqlPara avatarAllSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.synAutoGroupMsgByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(avatarAllSqlPara);
					SqlPara userAllAvatarSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.synAutoGroupUserByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(userAllAvatarSqlPara);
					params.clear();
					params.set("uid", uid).set("setnick", "setnick").set("nick", user.getNick()).set("yes", Const.YesOrNo.YES);
					SqlPara nickAllSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.synAutoGroupMsgByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(nickAllSqlPara);
					SqlPara userAllNickSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.synAutoGroupUserByUserUpdate", params);
					Db.use(Const.Db.TIO_SITE_MAIN).update(userAllNickSqlPara);
					break;
				default:
					break;
				}
				for (WxChatUserItem userItem : chatUserItems) {
					Short chatmode = userItem.getChatmode();
					if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
						if (userItem.getTochatlinkid() == null) {
							continue;
						}
						switch (type) {
						case Const.UserToImSynType.NICK:
							ChatService.me.updateChatItemById(userItem.getTochatlinkid(), user.getNick(), "", null);
							break;
						case Const.UserToImSynType.AVATAR:
							ChatService.me.updateChatItemById(userItem.getTochatlinkid(), "", user.getAvatar(), null);
							break;
						case Const.UserToImSynType.USER_ALL:
							ChatService.me.updateChatItemById(userItem.getTochatlinkid(), user.getNick(), user.getAvatar(), null);
							break;
						default:
							break;
						}
						WxChatItems chatItems = ChatService.me.getAllChatItems(userItem.getTochatlinkid());
						if (chatItems != null) {
							//更新会话
							ChatIndexService.clearFriendInfoCache(chatItems.getLinkid(), chatItems.getUid());
							autoUseSysChatNtf(chatItems.getUid(), Const.WxSysCode.FRIEND_INFO_UPDATE, "好友信息发生变更", Json.toJson(user), chatItems);
						}
						FriendService.me.clearP2pChatCache(userItem.getTochatlinkid());
						if (userItem.getChatlinkid() != null) {
							FriendService.me.clearP2pChatCache(userItem.getChatlinkid());
						}
						//更新通讯录
						ChatIndexService.clearMailListCache(userItem.getBizid().intValue());
					} else {
						ChatIndexService.clearGroupMsgCache(userItem.getBizid());
						ChatIndexService.clearGroupUserCache(userItem.getLinkid());
						ChatIndexService.clearGroupUserListCache(userItem.getBizid());
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 焦点逻辑通知
	 * @param uid
	 * @param devicetype
	 * @param chatlinkid
	 * @param status
	 * @return
	 * @author lixinji
	 * 2020年8月27日 下午3:46:35
	 */
	public static void focusNtf(Integer uid, Long chatlinkid) {
		Map<String, Short> focusMap = SynService.me.focus(uid);
		if (focusMap == null) {
			return;
		}
		if (chatlinkid != null && focusMap.get(chatlinkid + "") != null) {
			focusMap.put(chatlinkid + "", Const.YesOrNo.YES);
		}
		WxFocusNtf ntf = new WxFocusNtf();
		ntf.setFocusMap(focusMap);
		ImPacket imPacket = new ImPacket(Command.WxFocusNtf, Json.toJson(ntf));
		Ims.sendToUser(uid, imPacket);
	}

	/**
	 * 是否在线：false:否；true:是
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年9月30日 上午10:09:33
	 */
	public static boolean isOnline(Integer uid) {
		if (uid == null) {
			return false;
		}
		SetWithLock<ChannelContext> contextSet = Tio.getByUserid(TioSiteImServerStarter.tioServerConfigApp, uid + "");
		if (contextSet != null) {
			ReadLock readLock = contextSet.readLock();
			readLock.lock();
			try {
				Set<ChannelContext> online = contextSet.getObj();
				if (online != null) {
					for (ChannelContext channelContext : online) {
						Devicetype devicetype = ImUtils.getDevicetype(channelContext);
						if (devicetype == null) {
							continue;
						}
						if (Objects.equals(Devicetype.IOS.getValue(), devicetype.getValue()) || Objects.equals(Devicetype.ANDROID.getValue(), devicetype.getValue()) || Objects.equals(Devicetype.WEB.getValue(), devicetype.getValue())) {
							return true;
						}
					}
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				readLock.unlock();
			}
		}
		return false;
	}

	/**
	 * @param uid
	 * @param devicetype
	 * @return
	 * @author lixinji
	 * 2020年10月12日 下午2:42:09
	 */
	public static boolean isManyOnline(Integer uid, Short deviceparam) {
		if (uid == null) {
			return false;
		}
		SetWithLock<ChannelContext> contextSet = Tio.getByUserid(TioSiteImServerStarter.tioServerConfigApp, uid + "");
		if (contextSet != null) {
			ReadLock readLock = contextSet.readLock();
			readLock.lock();
			try {
				Set<ChannelContext> online = contextSet.getObj();
				if (online != null) {
					for (ChannelContext channelContext : online) {
						Devicetype devicetype = ImUtils.getDevicetype(channelContext);
						if (devicetype == null) {
							continue;
						}
						if (!Objects.equals(deviceparam, devicetype.getValue())) {
							return true;
						}
					}
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				readLock.unlock();
			}
		}
		return false;
	}

	/**
	 * 是否离线：false:否；true:是
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年9月30日 上午10:28:14
	 */
	public static boolean isOutline(Integer uid) {
		if (uid == null) {
			return true;
		}
		SetWithLock<ChannelContext> contextSet = Tio.getByUserid(TioSiteImServerStarter.tioServerConfigApp, uid + "");
		if (contextSet != null) {
			ReadLock readLock = contextSet.readLock();
			readLock.lock();
			try {
				Set<ChannelContext> online = contextSet.getObj();
				if (online != null) {
					if (online.size() >= 1) {
						return false;
					}
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				readLock.unlock();
			}
		}
		return true;
	}

	public static Ret SendTemplateMsg(ChannelContext channelContext, WxTemplateMsgVo templateMsgVo, Integer uid, Long bizid, Short chatmode) throws Exception {
		if (channelContext == null) {
			return SendTemplateMsg(templateMsgVo, uid, bizid, chatmode);
		}
		if (templateMsgVo == null || templateMsgVo.checkIsNull()) {
			return RetUtils.failMsg("模板消息为空");
		}
		String text = Json.toJson(templateMsgVo);
		if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, bizid);
			if (groupItem.getChatlinkid() == null) {
				Ret actRet = ChatService.me.actGroupChatItems(bizid, uid);
				if (actRet.isFail()) {
					log.error("自己的会话激活失败-群聊，uid:{},touid:{}", uid, bizid);
					return actRet;
				} else {
					if (WxSynApi.isSynVersion()) {
						WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
					} else {
						WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
					}
				}
			}
			Ret ret = WxChatApi.sendGroupMsgEach(channelContext, text, Const.ContentType.TEMPLATE, uid, bizid, null, null, null);
			if (ret.isFail()) {
				return ret;
			}
		} else {
			Ret actRet = ChatService.me.actFdChatItems(uid, bizid.intValue());
			if (actRet.isFail()) {
				log.error("自己的会话激活失败-私聊，uid:{},touid:{}", uid, bizid.intValue());
				return actRet;
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
				}
			}
			Ret ret = WxChatApi.sendFdMsgEach(channelContext, text, Const.ContentType.TEMPLATE, uid, bizid.intValue(), Const.YesOrNo.NO);
			if (ret.isFail()) {
				return ret;
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * @param text
	 * @param uid
	 * @param bizid
	 * @param chatmode
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月29日 下午4:38:09
	 */
	public static Ret SendTemplateMsg(WxTemplateMsgVo templateMsgVo, Integer uid, Long bizid, Short chatmode) throws Exception {
		if (templateMsgVo == null || templateMsgVo.checkIsNull()) {
			return RetUtils.failMsg("模板消息为空");
		}
		String text = Json.toJson(templateMsgVo);
		if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, bizid);
			if (groupItem.getChatlinkid() == null) {
				Ret actRet = ChatService.me.actGroupChatItems(bizid, uid);
				if (actRet.isFail()) {
					log.error("自己的会话激活失败-群聊，uid:{},touid:{}", uid, bizid);
					return actRet;
				} else {
					if (WxSynApi.isSynVersion()) {
						WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
					} else {
						WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
					}
				}
			}
			Ret ret = WxChatApi.sendGroupMsgEach(text, Const.ContentType.TEMPLATE, uid, bizid, null, null, null);
			if (ret.isFail()) {
				return ret;
			}
		} else {
			Ret actRet = ChatService.me.actFdChatItems(uid, bizid.intValue());
			if (actRet.isFail()) {
				log.error("自己的会话激活失败-私聊，uid:{},touid:{}", uid, bizid.intValue());
				return actRet;
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
				}
			}
			Ret ret = WxChatApi.sendFdMsgEach(text, Const.ContentType.TEMPLATE, uid, bizid.intValue(), Const.YesOrNo.NO, null);
			if (ret.isFail()) {
				return ret;
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * 
	 * @param text
	 * @param uid
	 * @param bizid
	 * @param chatmode
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月29日 下午4:38:41
	 */
	public static Ret SendTemplateMsg(HttpRequest request, WxTemplateMsgVo templateMsgVo, Integer uid, Long bizid, Short chatmode) throws Exception {
		if (request == null) {
			return SendTemplateMsg(templateMsgVo, uid, bizid, chatmode);
		}
		if (templateMsgVo == null || templateMsgVo.checkIsNull()) {
			return RetUtils.failMsg("模板消息为空");
		}
		String text = Json.toJson(templateMsgVo);
		if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, bizid);
			if (groupItem.getChatlinkid() == null) {
				Ret actRet = ChatService.me.actGroupChatItems(bizid, uid);
				if (actRet.isFail()) {
					log.error("自己的会话激活失败-群聊，uid:{},touid:{}", uid, bizid);
					return actRet;
				} else {
					if (WxSynApi.isSynVersion()) {
						WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
					} else {
						WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
					}
				}
			}
			Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.TEMPLATE, uid, bizid, Const.YesOrNo.NO, null);
			if (ret.isFail()) {
				return ret;
			}
		} else {
			Ret actRet = ChatService.me.actFdChatItems(uid, bizid.intValue());
			if (actRet.isFail()) {
				log.error("自己的会话激活失败-私聊，uid:{},touid:{}", uid, bizid.intValue());
				return actRet;
			} else {
				if (WxSynApi.isSynVersion()) {
					WxSynApi.synChatSession(uid, RetUtils.getOkTData(actRet, "chat"), SynRecordVo.SynType.ADD);
				} else {
					WxChatApi.userActOper(uid, RetUtils.getOkTData(actRet, "chat"));
				}
			}
			Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.TEMPLATE, uid, bizid.intValue(), Const.YesOrNo.NO);
			if (ret.isFail()) {
				return ret;
			}
		}
		return RetUtils.okOper();
	}

	private static final String TEMPLATE_TIOIM_LINK = "tioim:link";

	/**
	 * @param text
	 * @return
	 * @author lixinji
	 * 2020年12月29日 下午4:56:21
	 */
	public static WxTemplateMsgVo checkAdminTempMsg(String text) {
		if (StrUtil.isBlank(text)) {
			log.error("模板消息验证内容为空");
			return null;
		}
		if (text.startsWith(TEMPLATE_TIOIM_LINK)) {
			text = StrUtil.subAfter(text, TEMPLATE_TIOIM_LINK, false);
		} else {
			return null;
		}
		try {
			WxTemplateMsgVo templateMsgVo = Json.toBean(text, WxTemplateMsgVo.class);
			if (templateMsgVo == null || templateMsgVo.checkIsNull()) {
				return null;
			}
			return templateMsgVo;
		} catch (Exception e) {
			log.error("解析模板消息异常：{}", text);
		}
		return null;
	}

	/**
	 * 封停通知
	 * @param groupid
	 * @param status
	 * @return
	 * @author lixinji
	 * 2021年2月24日 下午2:17:33
	 */
	public static void inblackNotify(Long groupid, Short status) {
		ChatIndexService.removeGroupCache(groupid);
		List<WxChatGroupItem> linkActGroupItems = ChatIndexService.me.getLinkActGroupIndex(groupid);
		if (CollectionUtil.isNotEmpty(linkActGroupItems)) {
			ChatService.me.updateItemStatus(groupid, status);
			for (WxChatGroupItem groupItem : linkActGroupItems) {
				ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
				ChatIndexService.clearMailListCache(groupItem.getUid());
			}
			WxGroupOperNtf groupOper = new WxGroupOperNtf();
			groupOper.setG(groupid);
			groupOper.setChatlinkid(-groupid);
			groupOper.setT(new Date().getTime());
			groupOper.setUid(-1);
			groupOper.setOper(Const.WxGroupOper.INBLACK_OPER);
			WxChatItems chatItems = new WxChatItems();
			chatItems.setId(-groupid);
			chatItems.setChatlinkid(-groupid);
			chatItems.setStatus(status);
			groupOper.setChatItems(chatItems);
			//发送操作消息
			ImPacket operPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(groupOper));
			Ims.sendToGroup(groupid, operPacket);
		}
	}

	/**
	 * 判断创建群是否满足用户注册时间
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月12日 上午10:21:00
	 */
	public static Ret checkCreateGroupRegLimit(User user) {
		if (UserService.isSuper(user)) {
			return RetUtils.okOper();
		}
		long limit = ConfService.getLong(Const.ConfMapping.WX_CREATE_GROUP_LIMIT_USER_REG_DAY, 0l);
		if (limit <= 0) {
			return RetUtils.okOper();
		}
		long offset = DateUtil.betweenDay(user.getCreatetime(), new Date(), false);
		if (offset < limit) {
			return RetUtils.failMsg("注册未满" + limit + "天，禁止建群", AppCode.FriendErrorCode.SYS_ERROR);
		}
		return RetUtils.okOper();
	}

	/**
	 * 判断聊天是否满足注册时间
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月12日 上午10:21:01
	 */
	public static Ret checkChatRegLimit(User user, User toUser) {
		if (UserService.isSuper(user) || UserService.isSuper(toUser)) {
			return RetUtils.okOper();
		}
		if (toUser != null && Objects.equals(user.getId(), toUser.getId())) {
			return RetUtils.okOper();
		}
		long limit = ConfService.getLong(Const.ConfMapping.WX_CHAT_LIMIT_USER_REG_DAY, 0l);
		if (limit <= 0) {
			return RetUtils.okOper();
		}
		long offset = DateUtil.betweenDay(user.getCreatetime(), new Date(), false);
		if (offset < limit) {
			return RetUtils.failMsg("注册未满" + limit + "天，禁止聊天", AppCode.FriendErrorCode.SYS_ERROR);
		}
		return RetUtils.okOper();
	}

	public static Ret circleSendCard(HttpRequest request, User curr, String sendId, Short type, Integer circleId) throws Exception {
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子不存在");
		}
		CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, curr.getId());
		if (circleMember == null) {
			return Ret.fail().set("errorMsg", "非圈子成员不可分享名片");
		}
		String[] sendIdList = sendId.split(",");
		for (String id : sendIdList) {
			WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where uid=? and bizid = ? and chatmode = ?", curr.getId(), id, type);
			if (wxChatUserItem == null) {
				return Ret.fail().set("errorMsg", "聊天不存在");
			}
			RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
			String appversion = ext.getAppVersion();
			Short devicetype = ext.getDeviceType();
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			SysMsgVo sysMsgVo = new SysMsgVo(circleId + "", MsgTemplate.circlecard, circle.getName(), "circlecard");
			sysMsgVo.setOpercode(Const.WxMsgOper.CIRCLECARDSEND);

			if (type.equals(Const.YesOrNo.YES)) {
				sendCircleCard(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", circle.getAvatar()),
						Const.ContentType.CIRCLE_CARD, curr.getId(), wxChatUserItem.getBizid().intValue(), Const.YesOrNo.YES,
						"", null, null, appversion, null);
			} else {
//				sendGroupMsgOnly(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", circle.getAvatar()), Const.ContentType.CIRCLE_CARD, curr.getId(), curr.getId(), Long.valueOf(id), wxChatUserItem.getChatlinkid(), Const.YesOrNo.YES, sysMsgVo, appversion);
				sendGroupMsgEach(request, sysMsgVo.toText().replaceAll("@@@", circle.getAvatar()), Const.ContentType.CIRCLE_CARD, curr.getId(), Long.valueOf(id), Const.YesOrNo.NO, null, null, null, "");

			}

		}

		return Ret.ok();
	}

	public static Ret sendCollectMsg(HttpRequest request, User curr, String uids, String groupids, Integer collectid) throws Exception {
		Collect collect = Collect.dao.findById(collectid);
		if (collect == null) {
			return Ret.fail().set("errorMsg", "收藏不存在");
		}
		if (!collect.getUid().equals(curr.getId())) {
			return Ret.fail().set("errorMsg", "只允许转发自己的收藏内容");
		}
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		SysMsgVo sysMsgVo = new SysMsgVo(collectid + "", MsgTemplate.sendcollectmsg, collect.getContent(), "sendcollectmsg");
		sysMsgVo.setOpercode(Const.WxMsgOper.COLLECTMSG);
		if (uids != null && !uids.isEmpty()) {
			String[] uidList = uids.split(",");
			if (uidList.length > 0) {
				for (String id : uidList) {
					WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where uid=? and bizid = ?", curr.getId(), id);
					if (wxChatUserItem == null) {
						return Ret.fail().set("errorMsg", "聊天不存在");
					}
//				sendCircleCard(devicetype, sessionid, ip, sysMsgVo.toText(), Const.ContentType.COLLECT_MSG, curr.getId(),
//						wxChatUserItem.getBizid().intValue(), Const.YesOrNo.YES, "", null, null, appversion, null);
					sendFdMsgEach(devicetype, sessionid, ip, sysMsgVo.toText(), Const.ContentType.COLLECT_MSG, curr.getId(), Integer.valueOf(id), Const.YesOrNo.NO, "", null, null, appversion,null);

				}
			}
		}
		if (groupids != null && !groupids.isEmpty()) {
			String[] groupIdList = groupids.split(",");

			if (groupIdList.length > 0) {
				for (String id : groupIdList) {
					//				sendGroupMsgOnly(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", circle.getAvatar()), Const.ContentType.CIRCLE_CARD, curr.getId(), curr.getId(), Long.valueOf(id), wxChatUserItem.getChatlinkid(), Const.YesOrNo.YES, sysMsgVo, appversion);
					sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.COLLECT_MSG, curr.getId(), Long.valueOf(id), Const.YesOrNo.NO, null, null, null, "");

				}

			}
		}


		return Ret.ok();
	}

	public static void sendFriendMomentsMsg(HttpRequest request, User user) throws Exception {
		List<WxChatUserItem> wxChatUserItems = WxChatUserItem.dao.find("select * from wx_chat_user_item where uid=? and chatmode = 1", user.getId());
		for (WxChatUserItem item : wxChatUserItems) {
			if (user.getId().equals(Integer.valueOf(item.getBizid().toString()))) {
				continue;
			}
			Ret ret = ChatMsgService.me.msgOper(user, item.getChatlinkid(), Const.WxMsgOper.MOMENT, "0", Const.ChatMode.P2P);
			Object msg = RetUtils.getOkTData(ret, "msg");
			RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
			String appversion = ext.getAppVersion();
			Short devicetype = ext.getDeviceType();
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			SysMsgVo sysMsgVo = new SysMsgVo(user.getNick(), MsgTemplate.newmoment, "", "newmoment");
			sysMsgVo.setOpercode(Const.WxMsgOper.MOMENT);
			sendMomentMsgEach(devicetype, sessionid, ip, sysMsgVo.toText(), Const.ContentType.MOMENT, user.getId(), item.getBizid().intValue(), Const.YesOrNo.YES,
					"", null, null, appversion, null);
		}
	}

	/**
	 * 发送朋友圈发送通知
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendMomentNotice(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
									Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}
				//存在更新相关会话
				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
				if (ret.isFail()) {
					return failRet(ret);
				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxMomentsOperNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxMomentsOperNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	public static void sendNewCommentMsg(HttpRequest request, User user, MomentsComments comment, Integer mUid) throws Exception {
		WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where uid=? and chatmode = 1 and bizid = ?", user.getId(), mUid);
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		SysMsgVo sysMsgVo = new SysMsgVo("comment", MsgTemplate.commentorlike, comment.getId()+"", "commentorlike");
		sysMsgVo.setOpercode(Const.WxMsgOper.COMMENTSORLIKES);
		sendCommentMsgEach(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", comment.getContent())
						.replaceAll("mmm", comment.getMid()+"")
						.replaceAll("ppp", comment.getPid() + ""), Const.ContentType.COMMENT, user.getId(), wxChatUserItem.getBizid().intValue(), Const.YesOrNo.YES,
				"", null, null, appversion, null);
	}

	public static void sendNewLikesMsg(HttpRequest request, User user, Integer mUid, MomentsLikes likes) throws Exception {

		WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where uid=? and chatmode = 1 and bizid = ?", user.getId(), mUid);
		Ret ret = ChatMsgService.me.msgOper(user, wxChatUserItem.getChatlinkid(), Const.WxMsgOper.COMMENTSORLIKES, "0", Const.ChatMode.P2P);
		RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		String appversion = ext.getAppVersion();
		Short devicetype = ext.getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		SysMsgVo sysMsgVo = new SysMsgVo("like", MsgTemplate.commentorlike, likes.getId() + "", "commentorlike");
		sysMsgVo.setOpercode(Const.WxMsgOper.COMMENTSORLIKES);
		sendLikesMsgEach(devicetype, sessionid, ip, sysMsgVo.toText()
						.replaceAll("@@@", "")
						.replaceAll("mmm", likes.getMid()+"")
						.replaceAll("ppp", ""), Const.ContentType.LIKES, user.getId(), wxChatUserItem.getBizid().intValue(), Const.YesOrNo.YES,
				"", null, null, appversion, null);
	}

	/**
	 * 新的朋友圈点赞消息提醒
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendLikesMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
										Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} /*else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}*/
				//存在更新相关会话
//				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
//				if (ret.isFail()) {
//					return failRet(ret);
//				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		msg.setReadflag(Short.valueOf("2"));
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxMomentsCommentsOrLikesOperNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxMomentsCommentsOrLikesOperNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	/**
	 * 新的朋友圈评论消息提醒
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendCommentMsgEach(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
									   Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} /*else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}*/
				//存在更新相关会话
//				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
//				if (ret.isFail()) {
//					return failRet(ret);
//				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		msg.setReadflag(Short.valueOf("2"));
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxMomentsCommentsOrLikesOperNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxMomentsCommentsOrLikesOperNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}

	public static User getTempUser() {
		List<User> users = User.dao.find("select * from user where user_type = 2");
		for (User tempUser : users) {
			boolean online = isOnline(tempUser.getId());
			if (!online) {
				ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
				String key = "tempIM" + "_" + tempUser.getId();
				Map<String, Object> result = CacheUtils.get(cache, key, false, new FirsthandCreater<HashMap<String, Object>>() {
					@Override
					public HashMap<String, Object> create() {
						return null;
					}
				});
				if (result != null) {
					continue;
				}
				return tempUser;
			}
		}
		return null;
	}



	/**
	 * 新的朋友圈评论消息提醒
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param chatlinkid 发送者聊天会话
	 * @param toChatlinkid 被发送者聊天会话
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendCircleCard(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
										 Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} /*else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}*/
				//存在更新相关会话
//				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
//				if (ret.isFail()) {
//					return failRet(ret);
//				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		msg.setReadflag(Short.valueOf("2"));
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}



	public static Ret sendNote(HttpRequest request, User curr, String sendId, Short type, Integer noteId) throws Exception {
		Collect note = Collect.dao.findById(noteId);
		if (note == null) {
			return Ret.fail().set("errorMsg", "笔记不存在");
		}
		String[] sendIdList = sendId.split(",");
		for (String id : sendIdList) {
			WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where uid=? and bizid = ? and chatmode = ?", curr.getId(), id, type);
			if (wxChatUserItem == null) {
				return Ret.fail().set("errorMsg", "聊天不存在");
			}
			RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
			String appversion = ext.getAppVersion();
			Short devicetype = ext.getDeviceType();
			String sessionid = request.getHttpSession().getId();
			String ip = request.getClientIp();
			SysMsgVo sysMsgVo = new SysMsgVo(noteId + "", MsgTemplate.note, note.getImgUrl() == null ? "":note.getImgUrl(), "note");
			sysMsgVo.setOpercode(Const.WxMsgOper.NOTE);
			if (type.equals(Const.YesOrNo.YES)) {
				sendNoteMsg(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", note.getText() == null ? "":note.getText()).replaceAll("aaa", note.getTitle() == null ? "":note.getTitle()),
						Const.ContentType.NOTE_MSG, curr.getId(), wxChatUserItem.getBizid().intValue(), Const.YesOrNo.YES,
						"", null, null, appversion, null);
			} else {
//				sendGroupMsgOnly(devicetype, sessionid, ip, sysMsgVo.toText().replaceAll("@@@", circle.getAvatar()), Const.ContentType.CIRCLE_CARD, curr.getId(), curr.getId(), Long.valueOf(id), wxChatUserItem.getChatlinkid(), Const.YesOrNo.YES, sysMsgVo, appversion);
				sendGroupMsgEach(request, sysMsgVo.toText().replaceAll("@@@", note.getImgUrl() == null ? "":note.getImgUrl()).replaceAll("aaa", note.getTitle() == null ? "":note.getTitle()), Const.ContentType.NOTE_MSG, curr.getId(), Long.valueOf(id), Const.YesOrNo.NO, null, null, null, "");

			}

		}

		return Ret.ok();
	}



	/**
	 * 发送笔记
	 * @param devicetype
	 * @param sessionid
	 * @param ip
	 * @param text 消息内容
	 * @param contenttype
	 * @param uid 发送者
	 * @param touid 被发送者
	 * @param frommsgid 消息转发id
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月18日 上午10:05:48
	 */
	public static Ret sendNoteMsg(Short devicetype, String sessionid, String ip, String text, Short contenttype, Integer uid, Integer touid, Short sysflag, String operBizData,
									 Long frommsgid, Short fromchatmode, String appVersion,SysMsgVo sysMsgVo) throws Exception {
		User user = UserService.ME.getById(uid);
		User toUser = UserService.ME.getById(touid);
		Ret checkRet = checkChatRegLimit(user, toUser);
		if (checkRet.isFail()) {
			return checkRet;
		}
		Ret check = checkFriendChat(uid, touid);
		if (check.isFail()) {
			return check;
		}
		WxChatUserItem touserItem = ChatIndexService.fdUserIndex(touid, uid);
		if (!ChatService.existTwoFriend(touserItem)) { //原则上不会存在这种情况
			return RetUtils.failMsg("对方不是你的好友", AppCode.FriendErrorCode.NO_LINK);
		}
		WxChatUserItem userItem = RetUtils.getOkTData(check);
		boolean startFlag = userItem.getStartmsgid() != null;
		WxFriend friend = FriendService.me.getFriendInfo(touserItem.getLinkid());
		boolean toStartFlag = touserItem.getStartmsgid() != null;
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				Short toAct = Const.YesOrNo.NO;
				FocusVo focusVo = null;//对方焦点
				Long tochatlinkid = touserItem.getChatlinkid();
				Long tochatlinkmetaid = touserItem.getChatlinkmetaid();
				Long chatlinkid = userItem.getChatlinkid();
				Long chatlinkmetaid = userItem.getChatlinkmetaid();
				if (tochatlinkid == null) {//不存在
					if (touserItem.getChatlinkid() == null) {
						toAct = Const.YesOrNo.YES;
					}
				} else {
					focusVo = ChatMsgService.isFocus(touid, tochatlinkid);
				}
				//保存消息
				WxFriendMsg msg = FriendService.me.addChatMsg(devicetype, sessionid, ip, text, uid, touid, contenttype, frommsgid, fromchatmode, sysflag, operBizData, focusVo,
						appVersion,sysMsgVo);
				if (msg == null) {
					return failRet("保存消息失败");
				}
				boolean readflag = Objects.equals(msg.getReadflag(), Const.YesOrNo.YES);
				msg.setNick(user.getNick());
				msg.setAvatar(user.getAvatar());
				if (Objects.equals(toAct, Const.YesOrNo.YES)) {//不存在创建相关，并更新相关索引
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
							user.getAvatar(), StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick(), touserItem.getLinkflag(), Const.YesOrNo.YES,
							readflag ? 0 : 1, Const.YesOrNo.NO, msg.getId(), user.getNick(), msg.getResume(), Const.YesOrNo.NO, msg.getTime(), msg.getId(), uid, Const.YesOrNo.NO,
							friend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("保存会话记录失败");
					}
					tochatlinkid = toChatItems.getId();
					tochatlinkmetaid = toChatItems.getChatlinkmetaid();
					touserItem.setChatlinkid(tochatlinkid);
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					//更新索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(toChatItems.getUid(), toChatItems.getBizid(), toChatItems.getChatmode(), tochatlinkid,
							tochatlinkmetaid, chatlinkid, chatlinkmetaid, toStartFlag ? null : msg.getId(), null);
					if (!update) {
						return failRet("修改索引失败");
					}
					//修改自己的索引状态
					boolean chatIndex = ChatIndexService.me.chatuserUpdateToChatlink(uid, new Long(touid), Const.ChatMode.P2P, tochatlinkid, tochatlinkmetaid,
							startFlag ? null : msg.getId());
					if (!chatIndex) {
						return failRet("修改好友索引状态异常");
					}

				} /*else {
					Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, tochatlinkmetaid, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, Const.YesOrNo.YES,
							readflag ? 0 : 1);
					if (ret.isFail()) {
						return failRet(ret);
					}
				}*/
				//存在更新相关会话
//				Ret ret = ChatMsgService.me.afterSendFriendChatMsg(msg, user, chatlinkmetaid, Const.YesOrNo.YES, readflag ? Const.YesOrNo.YES : Const.YesOrNo.NO, null);
//				if (ret.isFail()) {
//					return failRet(ret);
//				}
				if (Objects.equals(Const.YesOrNo.NO, toAct)) {
					if (!startFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, userItem.getBizid(), userItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
					}
					if (!toStartFlag) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, touserItem.getBizid(), touserItem.getChatmode(), msg.getId());
						if (!startTx) {
							return failRet("修改好友起始消息异常");
						}
					}
				}
				retObj = Ret.ok().set("toact", toAct).set("msg", msg);
				FriendService.me.putToP2pCache(msg, chatlinkid, tochatlinkid);
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		Long chatlinkid = userItem.getChatlinkid();
		Long tochatlinkid = touserItem.getChatlinkid();
		Ret atomRet = atom.getRetObj();
		if (!atomFlag) {
			sendFriendErrorMsg(devicetype, sessionid, ip, uid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
			return atomRet;
		}
		WxFriendMsg msg = RetUtils.getOkTData(atomRet, "msg");
		Short toAct = RetUtils.getOkTData(atomRet, "toact");
		//发送消息
		msg.setReadflag(Short.valueOf("2"));
		WxFriendChatNtf p2PChatNtf = WxFriendChatNtf.from(msg);
		p2PChatNtf.setChatlinkid(chatlinkid);
		p2PChatNtf.setActflag(Const.YesOrNo.NO);
		p2PChatNtf.setNick(user.getNick());
		p2PChatNtf.setAvatar(user.getAvatar());
		ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
		Ims.sendToUser(uid, imPacket);
		if (!Objects.equals(uid, touid)) {
			p2PChatNtf.setChatlinkid(tochatlinkid);
			p2PChatNtf.setActflag(toAct);
			if (Objects.equals(toAct, Const.YesOrNo.YES)) {
				p2PChatNtf.setActavatar(user.getAvatar());
				p2PChatNtf.setActname(user.getNick());
				p2PChatNtf.setMsgfreeflag(friend.getMsgfreeflag());
			}
			ImPacket toimPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(p2PChatNtf));
			Ims.sendToUser(touid, toimPacket);
		}
		if (Objects.equals(toAct, Const.YesOrNo.YES)) {
			ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
		} else {
			if (!startFlag) {
				ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
			}
			if (!toStartFlag) {
				ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		//TODO:lixinji-2021-08-27:修改不需要进行基础缓存清理
//		ChatIndexService.removeChatItemsCache(chatlinkid);
//		ChatIndexService.removeChatItemsCache(tochatlinkid);
		if (Const.JPushConfig.OPENFLAG && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						PushBizService.me.sendFdMsg(touid, touserItem.getChatlinkid(), user, msg.getResume());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okData(msg);
	}
}
