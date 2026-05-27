
package org.tio.sitexxx.service.service.chat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 聊天服务
 * @author lixinji
 * 2019年12月31日 下午5:57:32
 */
public class ChatService {
	private static Logger			log	= LoggerFactory.getLogger(ChatService.class);
	public static final ChatService	me	= new ChatService();

	final WxChatUserItem userItemDao = new WxChatUserItem().dao();

	final WxChatGroupItem groupItemDao = new WxChatGroupItem().dao();

	final WxChatItems itemsDao = new WxChatItems().dao();

	/*******************************************begin-调整-**********************************************************/

	/**
	 * 会话列表请求-此处未进行缓存处理，尽量不要频繁刷新该请求-已调整
	 * @param user
	 * @return
	 * @author lixinji
	 * 2019年12月31日 下午5:59:51
	 */
	public List<Record> chatItemList(User user, Short devicetype) {
//		if (user == null) {
//			log.error("获取聊天列表：无效用户");
//			return Ret.fail("msg", "无效参数");
//		}
		Kv params = Kv.by("uid", user.getId());
		if (Objects.equals(devicetype, Devicetype.APP.getValue())) {
			params.set("limit", 5000);
		} else {
			params.set("limit", 300);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.list", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		for(Record record : records) {
			if (record.get("chatmode").toString().equals("2")) {
				WxGroup wxGroup = WxGroup.dao.findById(record.get("bizid"));
				if (wxGroup != null) {
					record.set("joinnum", Integer.valueOf(record.get("joinnum").toString()) + wxGroup.getVnum());
				}
			}
		}
		return records;
	}

	/**
	 * 基础会话信息-已调整
	 * @param id
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午3:55:10
	 */
	public WxChatItems getBaseChatItems(Long id) {
		long currTime = System.currentTimeMillis();
		if (id == null) {
			return null;
		}
		ICache cache = Caches.getCache(CacheConfig.CHAT_ITEMS_6);
		String key = id + "";
		WxChatItems chatItems = CacheUtils.get(cache, key, true, new FirsthandCreater<WxChatItems>() {
			@Override
			public WxChatItems create() {
				Kv params = Kv.by("id", id);
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.baseInfo", params);
				WxChatItems chatItems = WxChatItems.dao.findFirst(sqlPara);
				long endtime = System.currentTimeMillis();
				long useTime = endtime - currTime;
				if (useTime > 500) {
					log.error("群会话内部查询慢，条件：usetime:{},chatlinkid:{}", useTime, id);
				}
				return chatItems;
			}
		});
		long endtime = System.currentTimeMillis();
		long useTime = endtime - currTime;
		if (useTime > 500) {
			log.error("群会话查询慢，条件：usetime:{},chatlinkid:{}", useTime, id);
		}
		return chatItems;
	}

	/**
	 * 获取会话的所有信息-该方法不建议进行缓存处理
	 * @param id
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午2:16:58
	 */
	public WxChatItems getAllChatItems(Long id) {
		long currTime = System.currentTimeMillis();
		if (id == null) {
			return null;
		}
		Kv params = Kv.by("id", id);
		SqlPara sqlPara = User.dao.getSqlPara("chat.info", params);
		WxChatItems chatItems = WxChatItems.dao.findFirst(sqlPara);
		long endtime = System.currentTimeMillis();
		long useTime = endtime - currTime;
		if (useTime > 500) {
			log.error("群会话内部查询慢，条件：usetime:{},chatlinkid:{}", useTime, id);
		}
		return chatItems;
	}

	/**
	 * 通讯录列表-无参数缓存-已调整
	 * @param curr
	 * @param mode
	 * @param searchkey
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年2月17日 下午1:53:20
	 */
	public Ret mailList(User curr, Short mode, String searchkey, Integer pageNumber) throws Exception {
		if (StrUtil.isBlank(searchkey) && (pageNumber == null || pageNumber <= 0)) {
			String key = curr.getId() + "";
			if (mode == null) {
				key += "_" + 99;
			} else {
				key += "_" + mode;
			}
			ICache cache = Caches.getCache(CacheConfig.WX_MAILLIST_2);
			HashMap<String, Object> cacheResult = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
				@Override
				public HashMap<String, Object> create() {
					try {
						HashMap<String, Object> result = new HashMap<String, Object>();
						if (mode != null) {
							switch (mode) {
							case Const.ChatMode.P2P:
								Ret fd = FriendService.me.fdList(curr, searchkey, pageNumber);
								result.put("fd", RetUtils.getOkData(fd));
								break;
							case Const.ChatMode.GROUP:
								Ret group = GroupService.me.groupList(curr.getId(), searchkey);
								result.put("group", RetUtils.getOkData(group));
								break;
							default:
								return null;
							}
						} else {
							Ret fd = FriendService.me.fdList(curr, searchkey, pageNumber);
							result.put("fd", RetUtils.getOkData(fd));
							Ret group = GroupService.me.groupList(curr.getId(), searchkey);
							result.put("group", RetUtils.getOkData(group));
						}
						return result;
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						return null;
					}
				}
			});
			return RetUtils.okData(cacheResult);
		} else {
			HashMap<String, Object> result = new HashMap<String, Object>();
			if (mode != null) {
				switch (mode) {
				case Const.ChatMode.P2P:
					Ret fd = FriendService.me.fdList(curr, searchkey, pageNumber);
					result.put("fd", RetUtils.getOkData(fd));
					break;
				case Const.ChatMode.GROUP:
					Ret group = GroupService.me.groupList(curr.getId(), searchkey);
					result.put("group", RetUtils.getOkData(group));
					break;
				default:
					return RetUtils.invalidParam();
				}
			} else {
				Ret fd = FriendService.me.fdList(curr, searchkey, pageNumber);
				result.put("fd", RetUtils.getOkData(fd));
				Ret group = GroupService.me.groupList(curr.getId(), searchkey);
				result.put("group", RetUtils.getOkData(group));
			}
			return RetUtils.okData(result);
		}
	}

	/**
	 * 修改群会话的视图显示-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年12月22日 下午10:23:35
	 */
	//	public void updateGroupChatView(long groupid) {
	//		Kv params = Kv.by("groupid", groupid).set("actflag",Const.YesOrNo.YES).set("viewflag",Const.YesOrNo.YES);
	//		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.updateChatGroupview", params);
	//		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
	//	}

	/**
	 * 获取隐藏的会话列表
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年9月16日 上午11:45:33
	 */
	@Deprecated
	public List<WxChatItems> getHideGroupItems(Long groupid) {
		Kv params = Kv.by("groupid", groupid).set("viewflag", Const.YesOrNo.NO);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.getHideGroupItems", params);
		return WxChatItems.dao.find(sqlPara);
	}

	/*********************************************end-调整-*********************************************************/

	/**************************************lixinji-user-oper-begin**********************************************************/

	/**
	 * im用户操作-已调整
	 * 最上层业务处理，请勿事务调用
	 * @param oper
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午10:10:39
	 */
	public Ret chatUserOper(Short oper, Integer uid, Integer touid, Long chatlinkid) {
		if (oper == null || uid == null) {
			return RetUtils.invalidParam();
		}
		Ret ret = null;
		switch (oper) {
		case Const.WxUserOper.BLACK:
			if (uid == null || touid == null) {
				return RetUtils.invalidParam();
			}
			ret = blackOper(uid, touid);
			break;
		case Const.WxUserOper.REMOVE_BLACK:
			if (uid == null || touid == null) {
				return RetUtils.invalidParam();
			}
			ret = romoveBlackOper(uid, touid);
			break;
		case Const.WxUserOper.DEL_ITEM_REACT:
			if (chatlinkid == null) {
				return RetUtils.invalidParam();
			}
			ret = removeChatItems(chatlinkid, uid, true);
			break;
		case Const.WxUserOper.CLEAR_CHAT_MSG:
			if (chatlinkid == null) {
				return RetUtils.invalidParam();
			}
			ret = clearMsgRecord(chatlinkid);
			break;
		case Const.WxUserOper.CHAT_TOP:
			if (chatlinkid == null) {
				return RetUtils.invalidParam();
			}
			ret = chatTop(chatlinkid, Const.YesOrNo.YES);
			break;
		case Const.WxUserOper.CHAT_CANCEL_TOP:
			if (chatlinkid == null) {
				return RetUtils.invalidParam();
			}
			ret = chatTop(chatlinkid, Const.YesOrNo.NO);
			break;
		case Const.WxUserOper.HIDE_CHAT:
			if (chatlinkid == null) {
				return RetUtils.invalidParam();
			}
			ret = hideChatItems(chatlinkid, uid);
			break;
		case Const.WxUserOper.CHAT_REPORT:
			ret = RetUtils.okOper();
			break;
		default:
			return RetUtils.failMsg("无效操作码");
		}
		return ret;
	}

	/**
	 * 拉黑处理-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 下午1:54:49
	 */
	public Ret blackOper(Integer uid, Integer touid) {
		WxUserBlackItems blackItems = getBlockItems(uid, touid);
		if (blackItems != null) {
			return RetUtils.existParam();
		}
		//如果是好友，隐藏好友的聊天会话
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				//				if(userItem != null) {
				//					if(userItem.getChatlinkid() != null) {
				//						//修改试图状态
				//						boolean chatUpdate = updateChatView(Const.YesOrNo.NO, userItem.getChatlinkmetaid(),null);
				//						if(!chatUpdate) {
				//							return failRet("修改视图状态异常");
				//						}
				//					}
				//				}
				//保存黑名单记录
				boolean block = userBlockInit(uid, touid);
				if (!block) {
					return failRet("保存黑名单异常");
				}
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			return atom.getRetObj();
		}
		//清缓存-索引缓存+会话信息缓存
		if (userItem != null) {
			ChatIndexService.removeUserCache(uid, new Long(touid), Const.ChatMode.P2P);
			if (userItem.getChatlinkid() != null) {
				ChatIndexService.removeChatItemsCache(userItem.getChatlinkid());
				return RetUtils.okData("拉黑会话：" + userItem.getChatlinkid()).set("chatlinkid", userItem.getChatlinkid());
			}
		}
		return RetUtils.okData("拉黑人员：" + touid);
	}

	/**
	 * 移除黑名单操作-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 下午2:00:40
	 */
	public Ret romoveBlackOper(Integer uid, Integer touid) {
		WxUserBlackItems blackItems = getBlockItems(uid, touid);
		if (blackItems == null) {
			return RetUtils.failMsg("黑名单不存在");
		}
		//如果是好友，恢复隐藏好友的聊天会话
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				//				if(userItem != null) {
				//					if(userItem.getChatlinkid() != null) {
				//						//修改试图状态
				//						boolean chatUpdate = updateChatView(Const.YesOrNo.YES, userItem.getChatlinkmetaid(),null);
				//						if(!chatUpdate) {
				//							return failRet("修改视图状态异常");
				//						}
				//					}
				//				}
				//移除黑名单记录
				boolean block = removeBlack(uid, touid);
				if (!block) {
					return failRet("移除黑名单异常");
				}
				return true;
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			return atom.getRetObj();
		}
		//清缓存-索引缓存+会话信息缓存
		ChatIndexService.removeBlockCache(uid, touid);
		if (userItem != null) {
			ChatIndexService.removeUserCache(uid, new Long(touid), Const.ChatMode.P2P);
			if (userItem.getChatlinkid() != null) {
				ChatIndexService.removeChatItemsCache(userItem.getChatlinkid());
				return RetUtils.okData("移除黑名单会话：" + userItem.getChatlinkid()).set("chatlinkid", userItem.getChatlinkid());
			}
		}
		return RetUtils.okData("移除黑名单用户：" + touid);
	}

	/**
	 * 激活聊天界面-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月20日 下午4:52:08
	 */
	public Ret actFdChatItems(Integer uid, Integer touid) {
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		if (!ChatService.existFriend(userItem)) {
			return RetUtils.failMsg("对方不是你好友");
		}
		if (ChatService.friendExistChat(userItem)) {
			WxChatItems chatItems = getBaseChatItems(userItem.getChatlinkid());
			return Ret.ok("chat", chatItems).set("actflag", Const.YesOrNo.NO).set("chatlinkid", userItem.getChatlinkid());
		}
		WxChatUserItem toUserItem = ChatIndexService.fdUserIndex(touid, uid);
		User touser = UserService.ME.getById(touid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxFriendMsg lastMsg = null;
				Long startMsg = userItem.getStartmsgid();
				if (startMsg != null) {
					lastMsg = ChatMsgService.me.getLastMsg(userItem.getFidkey(), userItem.getBizid().intValue());
				}
				if (Objects.equals(uid, touid)) {//自己
					WxChatItems chatItems = null;
					if (lastMsg != null) {
						chatItems = ChatService.me.chatitemsInit(userItem.getUid(), userItem.getChatmode(), userItem.getBizid(), userItem.getLinkid(), touser.getAvatar(),
						        touser.getNick(), userItem.getLinkflag(), Const.YesOrNo.YES, 0, Const.YesOrNo.YES, null, lastMsg.getNick(), lastMsg.getResume(),
						        lastMsg.getSendbysys(), lastMsg.getTime(), lastMsg.getId(), lastMsg.getUid(), lastMsg.getReadflag(), null);
					} else {
						chatItems = ChatService.me.chatitemsInit(userItem.getUid(), userItem.getChatmode(), userItem.getBizid(), userItem.getLinkid(), touser.getAvatar(),
						        touser.getNick(), userItem.getLinkflag(), Const.YesOrNo.YES, 0, Const.YesOrNo.YES, null, "", "", Const.YesOrNo.NO, null, null, null, null, null);
					}
					if (chatItems == null) {
						return failRet("初始化聊天会话失败");
					}
					//修改索引状态
					boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(uid, new Long(touid), Const.ChatMode.P2P, chatItems.getId(), chatItems.getChatlinkmetaid(),
					        chatItems.getId(), chatItems.getChatlinkmetaid(), null, null);
					if (!chatindex) {
						return failRet("修改索引状态异常");
					}
					return okRet(chatItems.getId());
				} else {
					WxFriend friend = FriendService.me.getFriendInfo(userItem.getLinkid());
					WxChatItems chatItems = null;
					if (lastMsg != null) {
						chatItems = ChatService.me.chatitemsInit(userItem.getUid(), userItem.getChatmode(), userItem.getBizid(), userItem.getLinkid(), touser.getAvatar(),
						        StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : touser.getNick(), userItem.getLinkflag(), Const.YesOrNo.YES, 0,
						        Const.YesOrNo.YES, null, lastMsg.getNick(), lastMsg.getResume(), lastMsg.getSendbysys(), lastMsg.getTime(), lastMsg.getId(), lastMsg.getUid(),
						        lastMsg.getReadflag(), friend.getMsgfreeflag());
					} else {
						chatItems = ChatService.me.chatitemsInit(userItem.getUid(), userItem.getChatmode(), userItem.getBizid(), userItem.getLinkid(), touser.getAvatar(),
						        StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : touser.getNick(), userItem.getLinkflag(), Const.YesOrNo.YES, 0,
						        Const.YesOrNo.YES, null, "", "", Const.YesOrNo.NO, null, null, null, null, friend.getMsgfreeflag());
					}
					if (chatItems == null) {
						return failRet("初始化聊天会话失败");
					}
					if (ChatService.existTwoFriend(toUserItem)) {//双方都是好友
						//修改索引状态
						boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(uid, new Long(touid), Const.ChatMode.P2P, chatItems.getId(), chatItems.getChatlinkmetaid(),
						        toUserItem.getChatlinkid(), toUserItem.getChatlinkmetaid(), null, null);
						if (!chatindex) {
							return failRet("修改索引状态异常");
						}
						//修改好友索引状态
						boolean toChatIndex = ChatIndexService.me.chatuserUpdateToChatlink(toUserItem.getUid(), toUserItem.getBizid(), Const.ChatMode.P2P, chatItems.getId(),
						        chatItems.getChatlinkmetaid(), null);
						if (!toChatIndex) {
							return failRet("修改好友索引状态异常");
						}
					} else { //单方好友-一般异常
						//修改索引状态
						boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(uid, new Long(touid), Const.ChatMode.P2P, chatItems.getId(), chatItems.getChatlinkmetaid(),
						        null, null, null, null);
						if (!chatindex) {
							return failRet("修改索引状态异常");
						}
					}
					return okRet(chatItems.getId());
				}
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			return atom.getRetObj();
		}
		Long chatlinkid = RetUtils.getOkTData(atom.getRetObj());
		//清缓存-索引缓存+会话信息缓存
		if (userItem != null) {
			if (userItem.getChatlinkid() != null) {
				ChatIndexService.removeChatItemsCache(userItem.getChatlinkid());
			}
			ChatIndexService.removeUserCache(uid, new Long(touid), Const.ChatMode.P2P);
			if (!Objects.equals(uid, touid)) {
				ChatIndexService.removeUserCache(touid, new Long(uid), Const.ChatMode.P2P);
			}
		}
		WxChatItems chatItems = getBaseChatItems(chatlinkid);
		return Ret.ok("chat", chatItems).set("actflag", Const.YesOrNo.YES).set("chatlinkid", chatlinkid);
	}

	/**
	 * 激活群会话-已调整
	 * @param groupid 群id
	 * @param user 群用户
	 * @return
	 * @author lixinji
	 * 2020年2月17日 下午5:14:46
	 */
	public Ret actGroupChatItems(Long groupid, Integer uid) {
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
		if (!ChatService.groupExistChat(groupItem)) {
			return RetUtils.failMsg("你不是群成员");
		}
		if (ChatService.groupChatAct(groupItem)) {
			WxChatItems chatItems = getAllChatItems(groupItem.getChatlinkid());
			return Ret.ok("chat", chatItems).set("actflag", Const.YesOrNo.NO);
		}
		WxGroupUser groupUser = GroupService.me.getGroupUser(groupItem.getGpulinkid());
		WxGroup group = GroupService.me.getByGroupid(groupid);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxGroupMsg lastMsg = null;
				Long startMsg = groupItem.getStartmsgid();
				if (startMsg != null) {
					lastMsg = GroupService.me.getGroupFristMsg(groupItem);
				}
				WxChatItems chatItem = new WxChatItems();
				chatItem.setUid(groupItem.getUid());
				chatItem.setBizid(groupItem.getGroupid());
				chatItem.setLinkid(groupItem.getGpulinkid());
				chatItem.setChatmode(Const.ChatMode.GROUP);
				chatItem.setBizrole(groupItem.getGrouprole());
				chatItem.setAvatar(group.getAvatar());
				chatItem.setJoinnum(group.getJoinnum());
				chatItem.setName(group.getName());
				chatItem.setMsgfreeflag(groupUser.getMsgfreeflag());
				ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
				WriteLock writeLock = rwLock.writeLock();
				writeLock.lock();
				try {
					boolean chatsave = chatItem.save();
					if (!chatsave) {
						return failRet("会话初始化异常");
					}
					WxChatItemsMeta meta = new WxChatItemsMeta();
					meta.setChatlinkid(chatItem.getId());
					meta.setChatuptime(new Date());
					meta.setUid(groupItem.getUid());
					meta.setBizid(groupItem.getGroupid());
					meta.setChatmode(Const.ChatMode.GROUP);
					if (lastMsg != null) {
						meta.setLastmsgid(lastMsg.getId());
						meta.setLastmsguid(lastMsg.getUid());
						meta.setFromnick(lastMsg.getNick());
						meta.setSysflag(lastMsg.getSendbysys());
						meta.setMsgresume(lastMsg.getResume());
						meta.setMsgtype(lastMsg.getContenttype());
						meta.setSendtime(lastMsg.getTime());
						meta.setNotreadstartmsgid(lastMsg.getId());
						meta.setNotreadcount(0);
					}
					boolean metasave = meta.save();
					if (!metasave) {
						return failRet("动态会话初始化异常");
					}
					chatItem.setMeta(meta);
					chatItem.setChatlinkmetaid(meta.getId());
				} catch (Exception e) {
					log.error("", e);
					return failRet("会话初始化异常");
				} finally {
					writeLock.unlock();
				}
				ChatIndexService.me.actGroupToUserIndex(groupid, groupItem.getUid(), chatItem.getId(), chatItem.getChatlinkmetaid(), null);
				ChatIndexService.me.actUserGroup(groupid, groupItem.getUid(), chatItem.getId(), chatItem.getChatlinkmetaid(), null);
				return okRet(chatItem.getId());
			}
		};
		boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomFlag) {
			return atom.getRetObj();
		}
		Long chatlinkid = RetUtils.getOkTData(atom.getRetObj());
		WxChatItems chatItems = getAllChatItems(chatlinkid);
		return Ret.ok("chat", chatItems).set("actflag", Const.YesOrNo.YES).set("chatlinkid", chatlinkid);
	}

	/**
	 *  移除聊天会话操作-已调整
	 *  谨慎扩展此方法
	 *  问题
	 *  1、新增如果是无效群组时/无效好友时，是否删除通讯录
	 *  2、新增通知
	 *  解决：
	 *  1、群无效时，不需要特殊通知，因为群的列表中，无效不会显示
	 *  2、私聊不做处理
	 * @param id
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午4:00:14
	 */
	public Ret removeChatItems(Long id, Integer operuid, boolean clearMsg) {
		WxChatItems items = itemsDao.findById(id);
		if (items == null) {
			return RetUtils.failMsg("会话已移除，请勿重复操作");
		}
		if (!Objects.equals(operuid, items.getUid())) {
			return RetUtils.grantError();
		}
		Integer uid = items.getUid();
		Short chatmode = items.getChatmode();
		Long bizid = items.getBizid();
		Integer touid = bizid.intValue();
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				Short oper = WxChatUserItem.UserIndexOper.REMOVE_CHAT;
				if (!clearMsg) {
					oper = WxChatUserItem.UserIndexOper.BLOCK_CHAT;
				}
				if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
					/**
					 * 1、群用户无效时，删除群相关记录
					 * 2、群有效时，修改群索引：更改会话id+判断删除起始消息
					 * 3、群有效时，修改用户索引：更改会话id+判断删除起始消息
					 */
					if (Objects.equals(items.getLinkflag(), Const.YesOrNo.NO)) {
						ChatIndexService.me.chatGroupIndexDel(uid, items.getBizid());
						ChatIndexService.me.chatUserIndexDel(uid, items.getBizid(), items.getChatmode());
						GroupService.me.delGroupUser(items.getLinkid());
					} else {
						boolean groupIndex = ChatIndexService.me.chatGroupIndexUpdate(items.getBizid(), uid, clearMsg, null, null);
						if (!groupIndex) {
							return failRet("修改群索引状态异常");
						}
						//修改索引状态
						//2020-2-3：新增起始消息逻辑
						boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(uid, bizid, chatmode, oper);
						if (!chatindex) {
							return failRet("修改用户索引状态异常");
						}
						//初始化统计数据
						WxGroupMeta meta = new WxGroupMeta();
						meta.setGroupid(items.getBizid());
						meta.setAllactflag(Const.YesOrNo.NO);
						if (clearMsg) {
							meta.setAllstartflag(Const.YesOrNo.NO);
						}
						boolean metaUpdate = GroupService.me.updateMeta(meta);
						if (!metaUpdate) {
							return failRet("群统计数据修改异常");
						}
						ChatIndexService.removeGroupCache(items.getBizid());
					}
				} else {
					/**
					 * 1、修改用户索引：同群
					 * 2、修改好友相关联的会话信息
					 */
					boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(uid, bizid, chatmode, oper);
					if (!chatindex) {
						return failRet("修改用户索引状态异常");
					}
					if (Objects.equals(items.getLinkflag(), Const.YesOrNo.YES)) {
						//修改好友索引状态
						boolean toChatIndex = ChatIndexService.me.chatuserUpdateToChatlink(touid, new Long(uid), Const.ChatMode.P2P, null, null, null);
						if (!toChatIndex) {
							return failRet("修改好友索引状态异常");
						}
					}
				}
//				boolean hide = itemsDao.deleteById(id);
				Ret ret = delChat(id);
				if (ret.isFail()){
					return failRet("移除聊天失败");
				}
				return true;
			}
		};
		boolean hide = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!hide) {
			return atom.getRetObj();
		}
		//清除索引
		ChatIndexService.clearChatUserIndex(uid, items.getBizid(), items.getChatmode());
		if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
			ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
			//清空聊天记录
			FriendService.me.clearP2pChatCache(id);
		}
		//移除会话缓存
		ChatIndexService.removeChatItemsCache(id);
		return RetUtils.okOper().set("chatlinkid", id).set("chat", items);
	}

	/**
	 * 隐藏会话-已调整
	 * @param id
	 * @param operuid
	 * @return
	 * @author lixinji
	 * 2020年9月15日 下午6:40:30
	 */
	public Ret hideChatItems(Long id, Integer operuid) {
		return removeChatItems(id, operuid, false);
	}

	/**
	 * 清空消息-已调整
	 * @param uid
	 * @param touid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月3日 下午12:43:26
	 */
	public Ret clearMsgRecord(Long chatlinkid) {
		WxChatItems chatItems = ChatService.me.getAllChatItems(chatlinkid);
		if (chatItems == null) {
			return RetUtils.noExistParam();
		}
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				boolean isGroup = Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP);
				if (isGroup) {
					boolean startTx = ChatIndexService.me.chatGroupStartMsg(chatItems.getUid(), chatItems.getBizid(), null);
					if (!startTx) {
						return failRet("修改群索引起始消息异常");
					}
					ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + chatItems.getBizid(),
					        WxChatItemsMeta.class);
					WriteLock writeLock = rwLock.writeLock();
					writeLock.lock();
					try {
						boolean clearMsg = clearChatItemMsg(chatItems.getChatlinkmetaid(), chatItems.getBizid());
						if (!clearMsg) {
							return failRet("清空消息异常");
						}
					} catch (Exception e) {
						log.error("", e);
					} finally {
						writeLock.unlock();
					}
					WxGroupMeta meta = new WxGroupMeta();
					meta.setGroupid(chatItems.getBizid());
					meta.setAllstartflag(Const.YesOrNo.NO);
					boolean metaUpdate = GroupService.me.updateMeta(meta);
					if (!metaUpdate) {
						return failRet("群统计数据修改异常");
					}
					ChatIndexService.removeGroupCache(chatItems.getBizid());
				} else {
					boolean clearMsg = clearChatItemMsg(chatItems.getChatlinkmetaid(), null);
					if (!clearMsg) {
						return failRet("清空消息异常");
					}
				}
				boolean startTx = ChatIndexService.me.chatuserStartMsg(chatItems.getUid(), chatItems.getBizid(), chatItems.getChatmode(), null);
				if (!startTx) {
					return failRet("修改起始消息异常");
				}
				return true;
			}
		};
		boolean clear = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!clear) {
			return atom.getRetObj();
		}
		//清除索引
		ChatIndexService.clearChatUserIndex(chatItems.getUid(), chatItems.getBizid(), chatItems.getChatmode());
		//移除会话缓存
		ChatIndexService.removeChatItemsCache(chatlinkid);
		//清空聊天记录
		if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
			FriendService.me.clearP2pChatCache(chatlinkid);
		}
		return RetUtils.okOper().set("chatlinkid", chatlinkid);
	}

	/**
	 * 置顶操作-已调整
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年3月31日 下午3:13:16
	 */
	public Ret chatTop(Long chatlinkid, Short topflag) {
		WxChatItems chatItems = ChatService.me.getBaseChatItems(chatlinkid);
		if (chatItems == null) {
			return RetUtils.noExistParam();
		}
		boolean topUpdate = updateChatTop(topflag, chatlinkid, Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP) ? chatItems.getBizid() : null);
		if (!topUpdate) {
			return RetUtils.failMsg("置顶操作失败");
		}
		//移除会话缓存
		ChatIndexService.removeChatItemsCache(chatlinkid);
		return RetUtils.okOper().set("chatlinkid", chatlinkid);
	}

	/**************************************lixinji-user-oper--end-**********************************************************/

	/**************************************lixinji-update/delete-begin**********************************************************/

	/**
	 * 清除聊天记录-已调整
	 * TODO:此处有脏数据-可进行统计数据处理
	 * @param chatid
	 * @return
	 * @author lixinji
	 * 2020年1月10日 下午3:06:50
	 */
	public boolean deleteChatItem(Long chatid) {
		WxChatItems chatItems = getBaseChatItems(chatid);
		if (chatItems == null) {
			return false;
		}
		//		if(Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP)) {
		//			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_CHATITEM_KEY_PREFIX + "." + chatItems.getBizid(), WxChatItems.class);
		//			WriteLock writeLock = rwLock.writeLock();
		//			writeLock.lock();
		//			try {
		//				return itemsDao.deleteById(chatid);
		//			} catch (Exception e) {
		//				log.error("", e);
		//				return false;
		//			} finally {
		//				writeLock.unlock();
		//			}
		//		} else {
		Ret ret = delChat(chatid);
		return ret.isOk();
		//		}
	}

	/**
	 * 修改聊天会话显示状态-已调整
	 * TODO:lixinji-view字段
	 * @param viewflag
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午11:26:20
	 */
	@Deprecated
	public boolean updateChatView(Short viewflag, Long chatlinkmetaid, Long groupid) {
		return updateChatItemMeta(chatlinkmetaid, groupid, viewflag, null);
	}

	/**
	 * 修改聊天会话的链接状态-已调整
	 * @param linkflag
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午11:26:52
	 */
	public boolean updateChatLink(Short linkflag, Long chatlinkid, Long chatlinkmetaid, Long groupid) {
		return updateChatItemStatus(chatlinkid, chatlinkmetaid, linkflag, null, null, null, groupid, null, null, null);
	}

	/**
	 * 修改会话的置顶状态-已调整
	 * @param topflag
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午11:27:43
	 */
	public boolean updateChatTop(Short topflag, Long chatlinkid, Long groupid) {
		return updateChatItemStatus(chatlinkid, null, null, topflag, null, null, groupid, null, null, null);
	}

	/**
	 * @param msgfreeflag
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2021年1月26日 上午10:46:23
	 */
	public boolean updateChatMsgFreeFlag(Short msgfreeflag, Long chatlinkid) {
		return updateChatItemStatus(chatlinkid, null, null, null, null, null, null, null, null, msgfreeflag);
	}

	/**
	 * 修改会话的被读状态-已调整
	 * @param topflag
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午11:27:45
	 */
	public boolean updateChatToread(Long chatlinkmetaid, Long groupid) {
		return updateChatItemMeta(chatlinkmetaid, groupid, null, Const.YesOrNo.YES);
	}

	/**
	 * 聊天会话修改状态-已调整
	 * 用户自己的读状态另算，此方法不包含
	 * 不能修改自己的值为空值，传空值为不修改
	 * @param chatlinkid
	 * @param linkflag
	 * @param viewflag
	 * @param topflag
	 * @param toreadflag
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午11:23:36
	 */
	public boolean updateChatItemStatus(Long chatlinkid, Long chatlinkmetaid, Short linkflag, Short topflag, Long linkid, Short joinnum, Long groupid, String name, String avatar,
	        Short msgfreeflag) {
		WxChatItems chatItems = new WxChatItems();
		chatItems.setId(chatlinkid);
		if (linkflag != null) {
			chatItems.setLinkflag(linkflag);
			if (groupid != null) {
				ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItems.class);
				WriteLock writeLock = rwLock.writeLock();
				writeLock.lock();
				try {
					WxChatItemsMeta itemsMeta = new WxChatItemsMeta();
					itemsMeta.setId(chatlinkmetaid);
					itemsMeta.setLinkflag(linkflag);
					itemsMeta.update();
				} catch (Exception e) {
					log.error("", e);
				} finally {
					writeLock.unlock();
				}
			} else {
				WxChatItemsMeta itemsMeta = new WxChatItemsMeta();
				itemsMeta.setId(chatlinkmetaid);
				itemsMeta.setLinkflag(linkflag);
				itemsMeta.update();
			}
		}
		if (linkid != null) {
			chatItems.setLinkid(linkid);
		}
		if (topflag != null) {
			chatItems.setTopflag(topflag);
		}
		if (joinnum != null) {
			chatItems.setJoinnum(joinnum);
		}
		if (msgfreeflag != null) {
			chatItems.setMsgfreeflag(msgfreeflag);
		}
		if (StrUtil.isNotBlank(name)) {
			chatItems.setName(name);
		}
		if (StrUtil.isNotBlank(avatar)) {
			chatItems.setAvatar(avatar);
		}

		return chatItems.update();
	}

	/**
	 * 修改会话动态数据-已调整
	 * @param chatlinkmetaid
	 * @param viewflag
	 * @param toreadflag
	 * @return
	 * @author lixinji
	 * 2020年12月23日 上午11:41:02
	 */
	public boolean updateChatItemMeta(Long chatlinkmetaid, Long groupid, Short viewflag, Short toreadflag) {
		WxChatItemsMeta meta = new WxChatItemsMeta();
		meta.setId(chatlinkmetaid);
		if (viewflag != null) {
			meta.setViewflag(viewflag);
			meta.setChatuptime(new Date());
		}
		if (toreadflag != null) {
			meta.setToreadflag(toreadflag);
		}
		if (groupid != null) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				return meta.update();
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			return meta.update();
		}
		return false;
	}

	/**
	 * 群人员变更修改-已调整
	 * @param joinnum
	 * @param groupid
	 * @author lixinji
	 * 2020年2月25日 下午1:06:54
	 */
	public void updateItemJoinNum(Short joinnum, Long groupid, boolean lock) {
		//		if(lock) {
		//			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_CHATITEM_KEY_PREFIX + "." + groupid, WxChatItems.class);
		//			WriteLock writeLock = rwLock.writeLock();
		//			writeLock.lock();
		//			try {
		//				Kv params = Kv.by("groupid", groupid).set("linkflag",Const.YesOrNo.YES)
		//						.set("joinnum",joinnum);
		//				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.chatItemUpdateJoinNum", params);
		//				Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		//			} catch (Exception e) {
		//				log.error("", e);
		//			} finally {
		//				writeLock.unlock();
		//			} 
		//		} else {
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES).set("joinnum", joinnum);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.chatItemUpdateJoinNum", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		//		}
	}

	/**
	 * 修改会话信息-已调整
	 * @param groupid
	 * @param name
	 * @param avatar
	 * @author lixinji
	 * 2020年3月13日 下午2:10:50
	 */
	public void updateChatItemInfo(Long groupid, String name, String avatar) {
		//		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_CHATITEM_KEY_PREFIX + "." + groupid, WxChatItems.class);
		//		WriteLock writeLock = rwLock.writeLock();
		//		writeLock.lock();
		//		try {
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		if (StrUtil.isNotBlank(name)) {
			params.set("name", name);
		}
		if (StrUtil.isNotBlank(avatar)) {
			params.set("avatar", avatar);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.chatItemUpdateInfo", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		//		} catch (Exception e) {
		//			log.error("", e);
		//		} finally {
		//			writeLock.unlock();
		//		}
	}

	/**
	 * 根据会话id修改会话信息-此方被动触发修改会话的头像和名称-已调整
	 * @param chatlinkid
	 * @param name
	 * @param avatar
	 * @author lixinji
	 * 2020年3月16日 上午10:55:40
	 */
	public void updateChatItemById(Long chatlinkid, String name, String avatar, Long groupid) {
		//		if(groupid != null) {
		//			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_CHATITEM_KEY_PREFIX + "." + groupid, WxChatItems.class);
		//			WriteLock writeLock = rwLock.writeLock();
		//			writeLock.lock();
		//			try {
		WxChatItems items = new WxChatItems();
		items.setId(chatlinkid);
		if (StrUtil.isNotBlank(name)) {
			items.setName(name);
		}
		if (StrUtil.isNotBlank(avatar)) {
			items.setAvatar(avatar);
		}
		items.update();
		//			} catch (Exception e) {
		//				log.error("", e);
		//			} finally {
		//				writeLock.unlock();
		//			}
		//		} else {
		//			WxChatItems items = new WxChatItems();
		//			items.setId(chatlinkid);
		//			if(StrUtil.isNotBlank(name)) {
		//				items.setName(name);
		//			}
		//			if(StrUtil.isNotBlank(avatar)) {
		//				items.setAvatar(avatar);
		//			}
		//			items.update();
		//		}

	}

	/**
	 * 修改已激活的会话的link状态-已调整
	 * @param groupid
	 * @param link
	 * @author lixinji
	 * 2020年2月26日 下午5:27:39
	 */
	public void updateActItemLink(Long groupid, Short link) {
		//		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_CHATITEM_KEY_PREFIX + "." + groupid, WxChatItems.class);
		//		WriteLock writeLock = rwLock.writeLock();
		//		writeLock.lock();
		//		try {
		Kv params = Kv.by("groupid", groupid).set("linkflag", link);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.chatItemUpdateActLinK", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		//		} catch (Exception e) {
		//			log.error("", e);
		//		} finally {
		//			writeLock.unlock();
		//		}
	}

	/**
	 * 修改会话得状态
	 * @param groupid
	 * @param status
	 * @author lixinji
	 * 2021年2月24日 下午1:48:21
	 */
	public void updateItemStatus(Long groupid, Short status) {
		Kv params = Kv.by("groupid", groupid).set("status", status).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.updateItemStatus", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
	}

	/**
	 * 清空聊天会话-已调整
	 * alter:去除本方法的锁，请上层加锁
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月3日 下午12:41:13
	 */
	public boolean clearChatItemMsg(Long chatlinkmetaid, Long groupid) {
		if (chatlinkmetaid == null) {
			return false;
		}
		Kv params = Kv.by("id", chatlinkmetaid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.clearChatItemMsg", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**************************************lixinji-update/delete--end-**********************************************************/

	/**************************************lixinji-init-oper-begin**********************************************************/

	/**
	 * 初始化会话-返回动态信息-已调整
	 * @param uid
	 * @param chatmode
	 * @param bizid
	 * @param linkid
	 * @param avatar
	 * @param name
	 * @param linkflag
	 * @param viewflag
	 * @param notreadcount
	 * @param readflag
	 * @param notreadstartmsgid
	 * @param fromnick
	 * @param msgresume
	 * @param sysflag
	 * @param sendtime
	 * @param lastmsgid
	 * @param lastmsguid
	 * @return
	 * @author lixinji
	 * 2020年1月16日 上午10:20:35
	 */
	public WxChatItems chatitemsInit(Integer uid, Short chatmode, Long bizid, Long linkid, String avatar, String name, Short linkflag, Short viewflag, Integer notreadcount,
	        Short readflag, Long notreadstartmsgid, String fromnick, String msgresume, Short sysflag, Date sendtime, Long lastmsgid, Integer lastmsguid, Short toreadflag,
	        Short msgfreeflag) {
		WxChatItems items = new WxChatItems();
		items.setUid(uid);
		items.setChatmode(chatmode);
		items.setBizid(bizid);
		items.setLinkid(linkid);
		items.setAvatar(avatar);
		items.setLinkflag(linkflag);
		if (msgfreeflag != null) {
			items.setMsgfreeflag(msgfreeflag);
		}
		items.setName(name);
		if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
			items.setFidkey(UserService.twoUid(uid, bizid.intValue()));
		}
		boolean init = items.save();
		if (!init) {
			return null;
		}
		WxChatItemsMeta meta = new WxChatItemsMeta();
		meta.setUid(uid);
		meta.setChatmode(chatmode);
		meta.setBizid(bizid);
		meta.setChatlinkid(items.getId());
		meta.setViewflag(viewflag);
		meta.setReadflag(readflag);
		meta.setToreadflag(Const.YesOrNo.YES);
		meta.setNotreadcount(notreadcount);
		meta.setNotreadstartmsgid(notreadstartmsgid);
		meta.setLastmsguid(lastmsguid);
		meta.setLastmsgid(lastmsgid);
		meta.setFromnick(fromnick);
		meta.setMsgresume(msgresume);
		meta.setChatuptime(new Date());
		meta.setSysflag(sysflag);
		meta.setToreadflag(toreadflag);
		meta.setSendtime(sendtime);
		boolean metainit = meta.save();
		if (!metainit) {
			return null;
		}
		items.setMeta(meta);
		items.setChatlinkmetaid(meta.getId());
		return items;
	}

	/**
	 * 初始化好友的另一个聊天列表-已调整
	 * @param items
	 * @param friend
	 * @param user
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午4:44:33
	 */
	public WxChatItems chatitemsInitToItem(WxChatItems items, WxFriend friend, User user) {
		WxChatItems tochatItems = new WxChatItems();
		tochatItems.setUid(friend.getUid());
		tochatItems.setBizid(new Long(friend.getFrienduid()));
		tochatItems.setLinkid(friend.getId());
		tochatItems.setAvatar(user.getAvatar());
		tochatItems.setName(StrUtil.isNotBlank(friend.getRemarkname()) ? friend.getRemarkname() : user.getNick());
		tochatItems.setFidkey(items.getFidkey());
		tochatItems.setChatmode(Const.ChatMode.P2P);
		tochatItems.setLinkflag(Const.YesOrNo.YES);
		WxChatItemsMeta meta = new WxChatItemsMeta();
		meta.setViewflag(Const.YesOrNo.YES);
		meta.setChatuptime(new Date());
		meta.setUid(friend.getUid());
		meta.setBizid(new Long(friend.getFrienduid()));
		meta.setChatmode(Const.ChatMode.P2P);
		tochatItems.setMeta(meta);
		return tochatItems;
	}

	/**
	 * 初始化黑名单信息-已调整
	 * @param blackItems
	 * @return
	 * @author lixinji
	 * 2020年1月8日 上午10:49:18
	 */
	public boolean userBlockInit(WxUserBlackItems blackItems) {
		return userBlockInit(blackItems.getUid(), blackItems.getTouid());
	}

	/**
	 * 初始化黑名单信息-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 下午1:38:50
	 */
	public boolean userBlockInit(Integer uid, Integer touid) {
		Kv params = Kv.by("uid", uid).set("touid", touid);
		SqlPara sqlPara = User.dao.getSqlPara("chat.blackInit", params);
		int init = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		return init > 0;
	}

	/**
	 * 移除黑名单-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 下午1:59:31
	 */
	public boolean removeBlack(Integer uid, Integer touid) {
		WxUserBlackItems blackItems = getBlockItems(uid, touid);
		if (blackItems == null) {
			return false;
		}
		WxUserBlackItems del = new WxUserBlackItems();
		del.setId(blackItems.getId());		
		del.setStatus(Const.Status.DELETE);
		del.update();
		return del.delete();
	}

	/**************************************lixinji-init-oper--end-**********************************************************/

	/**************************************lixinji-utils-begin**********************************************************/

	/**
	 * 判断是否通过黑名单验证-已调整
	 * true:通过；false:通不过
	 * 特别提醒：此处判断uid是否将touid加入黑名单，一般调用此业务的uid和touid是相反的
	 * @param operuid
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午3:50:12
	 */
	public static boolean checkBlack(Integer operuid, Integer uid, Integer touid) {
		if (Objects.equals(operuid, uid)) {
			return true;
		}
		WxUserBlackItems items = getBlockItems(uid, touid);
		if (items != null) {
			return false;
		}
		return true;
	}

	/**
	 * 获取黑名单信息-已调整
	 * @param bizid
	 * @param uid
	 * @param mode
	 * @author lixinji
	 * 2020年1月8日 上午10:08:47
	 */
	public static WxUserBlackItems getBlockItems(Integer uid, Integer touid) {
		if (uid == null || touid == null) {
			return null;
		}
		String key = uid + "_" + touid;
		ICache cache = Caches.getCache(CacheConfig.CHAT_USER_BLOCK_1);
		WxUserBlackItems block = CacheUtils.get(cache, key, true, new FirsthandCreater<WxUserBlackItems>() {
			@Override
			public WxUserBlackItems create() {
				Kv params = Kv.by("uid", uid).set("touid", touid).set("status", Const.Status.NORMAL);
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.blockitems", params);
				WxUserBlackItems blockItems = WxUserBlackItems.dao.findFirst(sqlPara);
				return blockItems;
			}
		});
		return block;
	}


	/**
	 *   是否存在好友-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午10:21:47
	 */
	public static boolean existFriend(Integer uid, Integer touid) {
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (userItem == null || userItem.getLinkid() == null) {
			return false;
		}
		return true;
	}

	/**
	 * 是否存在好友-已调整
	 * @param userItem
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午10:39:16
	 */
	public static boolean existFriend(WxChatUserItem userItem) {
		if (userItem == null || userItem.getLinkid() == null) {
			return false;
		}
		return true;
	}

	/**
	 * 是否双方互为好友-已调整
	 * @param userItem
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午11:35:26
	 */
	public static boolean existTwoFriend(WxChatUserItem userItem) {
		if (userItem == null || userItem.getLinkid() == null || Objects.equals(userItem.getLinkflag(), Const.YesOrNo.NO)) {
			return false;
		}
		return true;
	}

	/**
	 * 会话激活后的判断-确定会话已激活-已调整
	 * @param items
	 * @return
	 * @author lixinji
	 * 2020年2月27日 下午3:58:25
	 */
	public static boolean existTwoFriend(WxChatItems items) {
		if (items == null || items.getLinkid() == null || Objects.equals(items.getLinkflag(), Const.YesOrNo.NO)) {
			return false;
		}
		return true;
	}

	/**
	 * 用户的私聊聊天是否激活创建-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午10:29:33
	 */
	public static boolean friendExistChat(Integer uid, Integer touid) {
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (userItem == null || userItem.getChatlinkid() == null) {
			return false;
		}
		return true;
	}

	/**
	 * 群聊是否激活-已调整
	 * @param groupItem
	 * @return
	 * @author lixinji
	 * 2020年2月14日 上午10:24:03
	 */
	public static boolean groupChatAct(WxChatGroupItem groupItem) {
		if (groupItem == null || groupItem.getChatlinkid() == null) {
			return false;
		}
		return true;
	}

	/**
	 * 是否存在群聊-已调整
	 * @param groupItem
	 * @return
	 * @author lixinji
	 * 2020年2月17日 下午2:05:42
	 */
	public static boolean groupExistChat(WxChatGroupItem groupItem) {
		if (groupItem == null) {
			return false;
		}
		return true;
	}

	/**
	 * 群聊是否有效-已调整
	 * @param groupItem
	 * @return
	 * @author lixinji
	 * 2020年2月14日 上午10:24:44
	 */
	public static boolean groupChatLink(WxChatGroupItem groupItem) {
		if (groupItem == null || Objects.equals(groupItem.getLinkflag(), Const.YesOrNo.NO)) {
			return false;
		}
		return true;
	}

	/**
	 * 用户的私聊聊天是否激活创建-已调整
	 * @param userItem
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午10:39:11
	 */
	public static boolean friendExistChat(WxChatUserItem userItem) {
		if (userItem == null || userItem.getChatlinkid() == null) {
			return false;
		}
		return true;
	}

	
	/**
	 * @param uid
	 * @param num
	 * @return
	 * @author lixinji
	 * 2021年5月21日 下午2:29:27
	 */
	public List<Record> hidItemJob(Integer uid,Integer limit,Integer startnum) {
		Kv params = Kv.by("uid", uid).set("limit", limit).set("num", startnum);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.hidechatjob", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return records;
	}
	
	
	/**
	 * 
	 * @param chatlinkid
	 * @author lixinji
	 * 2022年3月4日 下午6:43:51
	 */
	public Ret delChat(Long chatlinkid) {
		Kv params = Kv.by("id", chatlinkid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.delChatBak", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		boolean hide =  itemsDao.deleteById(chatlinkid);
		if (!hide) {
			return RetUtils.failMsg("移除聊天失败");
		}
		return RetUtils.okOper();
	}
	
	
	/**
	 * @param limit
	 * @return
	 * @author lixinji
	 * 2021年5月21日 下午2:37:00
	 */
	public List<Record> hidItemJobUid(Integer limit) {
		Kv params = Kv.by("limit", limit);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.hidechatuid", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return records;
	}
	
	/**************************************lixinji-utils--end-**********************************************************/

}
