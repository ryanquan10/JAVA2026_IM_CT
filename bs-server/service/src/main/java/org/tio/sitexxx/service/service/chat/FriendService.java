
package org.tio.sitexxx.service.service.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.CPI;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxChatItemsMeta;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.model.main.WxFriend;
import org.tio.sitexxx.service.model.main.WxFriendApplyItems;
import org.tio.sitexxx.service.model.main.WxFriendDelLog;
import org.tio.sitexxx.service.model.main.WxFriendMeta;
import org.tio.sitexxx.service.model.main.WxFriendMsg;
import org.tio.sitexxx.service.model.main.WxFriendOperMsg;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.atom.ChatAtom;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.SensitiveWordsService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.PyUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxCallItemVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.lock.ListWithLock;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.WriteLockHandler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

/**
 * 新版好友服务
 * 
 * @author lixinji 2020年2月3日 上午9:03:11
 */
public class FriendService {
	public static final FriendService me = new FriendService();

	private static Logger log = LoggerFactory.getLogger(FriendService.class);

	/**
	 * 个人会话的私聊记录 key: chatlinkid value: ListWithLock<WxFriendMsg>
	 */
	static final ICache		WX_FRIEND_MSG_CACHE	= Caches.getCache(CacheConfig.WX_FRIEND_MSG_CHAT_6);
	/**
	 * 默认查询两个私聊的历史记录条数
	 */
	public static final int	WX_FRIEND_MSG_LIMIT	= ConfService.getInt(Const.ConfMapping.WX_FRIEND_MSG_LIMIT, 30);

	/*******************************************begin-调整-**********************************************************/
	/**
	 * 好友列表-无分页
	 * 
	 * @param curr
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年2月4日 下午2:06:19
	 */
	public Ret fdList(User curr, String searchkey) throws Exception {
		Kv params = Kv.by("uid", curr.getId()).set("chatmode", Const.ChatMode.P2P).set("userstatus", User.Status.NORMAL);
		if (StrUtil.isNotBlank(searchkey)) {
			params.set("nick", "%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.myFriends", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return RetUtils.okData(records);
	}

	/**
	 * 好友列表-分页
	 * @param curr
	 * @param searchkey
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年2月20日 下午11:16:26
	 */
	public Ret fdList(User curr, String searchkey, Integer pageNumber) throws Exception {
		if (pageNumber == null || pageNumber <= 0) {
			return fdList(curr, searchkey);
		}
		Kv params = Kv.by("uid", curr.getId()).set("chatmode", Const.ChatMode.P2P).set("userstatus", User.Status.NORMAL);
		if (StrUtil.isNotBlank(searchkey)) {
			params.set("nick", "%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.myFriends", params);
		Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okData(records);
	}

	/**
	 * 是否是好友-已调整
	 * 
	 * @param curr
	 * @param touid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年2月5日 上午10:49:21
	 */
	public Ret isFriend(User curr, Integer touid) throws Exception {
		return ChatService.existFriend(curr.getId(), touid) ? RetUtils.okData(Const.YesOrNo.YES) : RetUtils.okData(Const.YesOrNo.NO);
	}

	/**
	 * 获取群的可邀请成员-已调整
	 * 
	 * @param curr
	 * @param searchkey
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年2月20日 下午11:31:43
	 */
	public Ret getOutGroupFdList(User curr, String searchkey, Long groupid) throws Exception {
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(curr.getId(), groupid, Const.ChatMode.GROUP);
		if (userItem == null) {
			return RetUtils.grantError();
		}
		Kv params = Kv.by("uid", curr.getId()).set("groupid", groupid).set("linkflag", Const.YesOrNo.YES).set("chatmode", Const.ChatMode.P2P).set("userstatus", User.Status.NORMAL);
		if (StrUtil.isNotBlank(searchkey)) {
			params.set("nick", "%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.getOutGroupFd", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return RetUtils.okData(records);
	}

	/**
	 * 获取俩人最近的私聊记录
	 * 
	 * @param chatlinkid
	 * @return
	 * @author lixinji 2020年2月3日 下午1:38:34
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ListWithLock<WxFriendMsg> getP2PMsgList(Long chatlinkid) {
		if (chatlinkid == null) {
			return null;
		}
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
		if (userItem == null) {
			return null;
		}
		String key = chatlinkid + "";
		ListWithLock<WxFriendMsg> listWithLock = CacheUtils.get(WX_FRIEND_MSG_CACHE, key, true, new FirsthandCreater<ListWithLock<WxFriendMsg>>() {
			@Override
			public ListWithLock<WxFriendMsg> create() {
				int historyCount = ConfService.getInt("im.history.chat.count.p2p", WX_FRIEND_MSG_LIMIT);
				Long startMsg = userItem.getStartmsgid();
				Kv params = Kv.by("fidkey", userItem.getFidkey()).set("status", Const.Status.NORMAL).set("limit", historyCount);
				if (startMsg != null) {
					params.set("startmsgid", startMsg);
				} else {
					params.set("startmsgid", WxFriendMsg.maxid);
				}
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.p2pMsgList", params);
				ArrayList<WxFriendMsg> list = (ArrayList<WxFriendMsg>) WxFriendMsg.dao.find(sqlPara);
				return new ListWithLock(list);
			}
		});
		return listWithLock;
	}

	/**
	 * 获取其他消息
	 * 
	 * @param chatlinkid
	 * @param startMid
	 * @return
	 * @author lixinji 2020年2月3日 下午2:15:15
	 */
	public List<WxFriendMsg> getOtherP2PMsgList(Long chatlinkid, Long startMid) {
		if (chatlinkid == null) {
			return null;
		}
		if (startMid == null) {
			ListWithLock<WxFriendMsg> listWithLock = getP2PMsgList(chatlinkid);
			if (listWithLock != null) {
				return listWithLock.getObj();
			}
			return null;
		}
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
		if (userItem == null) {
			return null;
		}
		Long startMsg = userItem.getStartmsgid();
		int historyCount = ConfService.getInt("im.history.chat.count.p2p", WX_FRIEND_MSG_LIMIT);
		Kv params = Kv.by("fidkey", userItem.getFidkey()).set("status", Const.Status.NORMAL).set("limit", historyCount).set("limitmsgid", startMid);
		if (startMsg != null) {
			params.set("startmsgid", startMsg);
		} else {
			params.set("startmsgid", WxFriendMsg.maxid);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.p2pMsgList", params);
		List<WxFriendMsg> list = WxFriendMsg.dao.find(sqlPara);
		return list;
	}

	/**
	 * 同步消息
	 * 
	 * @param chatlinkid
	 * @param endmid
	 * @return
	 * @author lixinji 2020年3月10日 下午3:26:34
	 */
	public List<WxFriendMsg> getSynP2PMsgList(Long chatlinkid, Long endmid) {
		if (chatlinkid == null) {
			return null;
		}
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
		if (userItem == null) {
			return null;
		}
		Long startMsg = userItem.getStartmsgid();
		int historyCount = ConfService.getInt("im.history.chat.count.p2p", WX_FRIEND_MSG_LIMIT);
		Kv params = Kv.by("fidkey", userItem.getFidkey()).set("status", Const.Status.NORMAL).set("limit", historyCount);
		if (startMsg != null) {
			if (startMsg < endmid) {
				params.set("endmid", endmid);
			} else {
				params.set("startmsgid", startMsg);
			}
		} else {
			params.set("startmsgid", WxFriendMsg.maxid);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.p2pMsgList", params);
		List<WxFriendMsg> list = WxFriendMsg.dao.find(sqlPara);
		return list;
	}

	/*********************************************end-调整-*********************************************************/

	/**
	 * 清空记录-已调整
	 * 
	 * @param chatlinkid
	 * @author lixinji 2020年2月3日 下午2:07:03
	 */
	public void clearP2pChatCache(Long chatlinkid) {
		String lockKey = WX_FRIEND_MSG_CACHE.getCacheName() + chatlinkid;
		try {
			LockUtils.runWriteOrWaitRead(lockKey, WX_FRIEND_MSG_CACHE, () -> {
				WX_FRIEND_MSG_CACHE.remove(chatlinkid + "");
			}, 5L);
		} catch (Exception e1) {
			log.error(e1.toString(), e1);
		}

	}

	/**
	 * 验证添加好友-已调整
	 * 条件 code：1：无条件；2：需要验证
	 * 
	 * @param curr
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji 2020年1月9日 下午5:37:55
	 */
	public Ret checkAddFriend(User curr, Integer touid) {
		Integer uid = curr.getId();
		if (touid == null) {
			return RetUtils.invalidParam();
		}
		User toUser = UserService.ME.getById(touid);
		if (toUser == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if (Objects.equals(toUser.getStatus(), User.Status.LOGOUT)) {
			RetUtils.failMsg("用户已注销");
		}
		if (!ChatService.checkBlack(curr.getId(), touid, uid)) {
			return RetUtils.failMsg("申请好友失败，对方把你加入了黑名单");
		}
		WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (ChatService.existFriend(oldItem)) {
			return RetUtils.failMsg("对方已经是您的好友啦!");
		}
		WxChatUserItem toUserItem = ChatIndexService.chatUserIndex(touid, uid, Const.ChatMode.P2P);
		if (ChatService.existFriend(toUserItem)) {// 对方已加我为好友
			return RetUtils.setCode(WxFriend.AddRespCode.NO_WHERE);
		}
		if (Objects.equals(toUser.getFdvalidtype(), Const.FdValidType.VALID)) {
			return RetUtils.setCode(WxFriend.AddRespCode.WHERE);
		} else {
			return RetUtils.setCode(WxFriend.AddRespCode.NO_WHERE);
		}
	}

	/**
	 * 新增加好友申请-已调整
	 * 
	 * @param curr
	 * @param uid
	 * @param touid
	 * @param greet
	 * @return
	 * @author lixinji 2020年1月9日 下午5:40:22
	 */
	public Ret addApply(User curr, Integer touid, String greet) {
		Integer uid = curr.getId();
		if (touid == null) {
			return RetUtils.invalidParam();
		}
		User toUser = UserService.ME.getById(touid);
		if (toUser == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if (Objects.equals(toUser.getStatus(), User.Status.LOGOUT)) {
			RetUtils.failMsg("用户已注销");
		}
		if (!ChatService.checkBlack(curr.getId(), touid, uid)) {
			return RetUtils.failMsg("申请好友失败，对方把你加入了黑名单");
		}
		WxChatUserItem oldItem = ChatIndexService.chatUserIndex(touid, uid, Const.ChatMode.P2P);
		if (ChatService.existFriend(oldItem)) {
			return RetUtils.failMsg("请不要重复申请，对方已加你为好友.");
		}
		WxFriendApplyItems applyItems = new WxFriendApplyItems();
		int count = FriendApplyService.me.applyInit(uid, touid, greet, applyItems);
		if (count <= 0) {
			return RetUtils.failMsg("申请失败");
		}
		return RetUtils.okData(applyItems);
	}

	/**
	 * 主动添加好友-已调整
	 * （通过验证后，不进行二次验证）
	 * 
	 * @param curr
	 * @param uid
	 * @param touid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年1月8日 下午3:08:34
	 */
	public Ret addFriend(User curr, Integer touid) throws Exception {
		if (touid == null) {
			return RetUtils.invalidParam();
		}
		Integer uid = curr.getId();
		User toUser = UserService.ME.getById(touid);
		if (toUser == null) {
			RetUtils.failMsg("用户不存在");
		}
		if (Objects.equals(toUser.getStatus(), User.Status.LOGOUT)) {
			RetUtils.failMsg("用户已注销");
		}
		WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (ChatService.existFriend(oldItem)) {
			return RetUtils.failMsg("请不要重复申请，对方已是你好友.");
		}
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(touid, uid, Const.ChatMode.P2P);
		if (ChatService.existFriend(userItem)) {// 对方已加我为好友
			return selfAddFriendExistToFriend(uid, touid, userItem);
		} else {
			return eachAddFriend(curr, toUser);
		}
	}

	/**
	 * 自己加自己好友的逻辑-已调整
	 * @param curr
	 * @return
	 * @author lixinji 2020年2月29日 下午3:28:17
	 */
	public Ret addSelfFriend(User curr) {
		Integer uid = curr.getId();
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				// 保存好友信息
				WxFriend friend = friendInit(uid, uid, null, "", true);
				if (friend == null) {
					return failRet("自己加自己好友失败");
				}
				WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, uid, Const.ChatMode.P2P);
				if (oldItem != null) {
					return failRet("自己的索引存在");
				} else {
					// 创建用户索引
					int count = ChatIndexService.me.chatUserInit(uid, Const.ChatMode.P2P, new Long(uid), null, null, null, null, friend.getId(), Const.YesOrNo.YES, null);
					if (count <= 0) {
						failRet("创建索引冲突");
						return false;
					}
				}
				return okRet(friend);
			}
		};
		Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		return atom.getRetObj();
	}

	/**
	 * 获取好友信息-已调整
	 * 
	 * @param fid
	 * @return
	 * @author lixinji 2020年3月10日 上午10:12:08
	 */
	public WxFriend getFriendInfo(Long fid) {
		ICache cache = Caches.getCache(CacheConfig.WX_MY_FRIEND);
		String cacheKey = fid + "";
		WxFriend ret = CacheUtils.get(cache, cacheKey, true, () -> {
			return WxFriend.dao.findById(fid);
		});
		return ret;
	}

	/**
	 * 修改好友的备注名称-已调整
	 * @param uid
	 * @param touid
	 * @param name
	 * @return
	 * @author lixinji 2020年3月10日 下午1:42:16
	 */
	public Ret updateRemarkName(Integer uid, Integer touid, String name) {
		WxChatUserItem chatUserItem = ChatIndexService.fdUserIndex(uid, touid);
		if (!ChatService.existFriend(chatUserItem)) {
			return RetUtils.failMsg("好友不存在");
		}
		User user = UserService.ME.getById(chatUserItem.getBizid().intValue());
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxFriend friend = new WxFriend();
				friend.setId(chatUserItem.getLinkid());
				if (StrUtil.isNotBlank(name)) {
					friend.setChatindex(PyUtils.getFristChat(name));
					friend.setRemarkname(name);
				} else {
					friend.setChatindex(PyUtils.getFristChat(user.getNick()));
					friend.setRemarkname("");
				}
				boolean fUpdate = friend.update();
				if (!fUpdate) {
					return failRet("修改失败");
				}
				if (ChatService.friendExistChat(chatUserItem)) {
					WxChatItems chatItems = new WxChatItems();
					if (StrUtil.isBlank(name)) {
						chatItems.setName(user.getNick());
					} else {
						chatItems.setName(name);
					}
					chatItems.setId(chatUserItem.getChatlinkid());
					boolean chatupdate = chatItems.update();
					if (!chatupdate) {
						return failRet("会话同步失败");
					}
				}
				return true;
			}
		};
		boolean atomCommit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!atomCommit) {
			return atom.getRetObj();
		}
		if (chatUserItem.getChatlinkid() != null) {
			ChatIndexService.removeChatItemsCache(chatUserItem.getChatlinkid());
		}
		ChatIndexService.clearFriendInfoCache(chatUserItem.getLinkid(), chatUserItem.getUid());
		ChatIndexService.clearMailListCache(uid);
		return RetUtils.okData(chatUserItem.getLinkid()).set("chatlinkid", chatUserItem.getChatlinkid());
	}

	/**
	 * 好友名片-已调整
	 * 
	 * @param uid
	 * @param touid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年3月9日 下午2:38:47
	 */
	public Ret getFdCard(Integer uid, Integer touid) throws Exception {
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		if (!ChatService.existFriend(userItem)) {
			return RetUtils.failMsg("对方不是你的好友");
		}
		User user = UserService.ME.getById(touid);
		if (user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if (Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
			RetUtils.failMsg("用户已注销");
		}
		WxMsgCardVo wsMsgCardVo = new WxMsgCardVo();
		wsMsgCardVo.setCardtype(Const.MsgCardType.FRIEND);
		wsMsgCardVo.setBizavatar(user.getAvatar());
		wsMsgCardVo.setBizname(user.getNick());
		wsMsgCardVo.setBizid(new Long(user.getId()));
		wsMsgCardVo.setShareFromUid(uid);
		return RetUtils.okData(wsMsgCardVo);
	}

	/**
	 * 处理好友申请-已调整
	 * 
	 * @param curr
	 * @param uid
	 * @param touid
	 * @param remarkname
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年1月8日 下午5:54:56
	 */
	public Ret dealApply(User curr, WxFriendApplyItems items, String remarkname) throws Exception {
		if (items == null) {
			return RetUtils.failMsg("申请记录不存在");
		}
		Integer applyid = items.getId();
		Integer uid = items.getFromuid();
		Integer touid = items.getTouid();
		if (uid == null || touid == null) {
			return RetUtils.invalidParam();
		}
		if (!Objects.equals(touid, curr.getId())) {
			return RetUtils.grantError();
		}
		User user = null;
		if (Objects.equals(curr.getId(), uid)) {
			user = curr;
		} else {
			user = UserService.ME.getById(uid);
			if (user == null) {
				RetUtils.failMsg("操作的用户不存在");
			}
		}
		User toUser = UserService.ME.getById(touid);
		if (toUser == null) {
			RetUtils.failMsg("用户不存在");
		}
		if (Objects.equals(toUser.getStatus(), User.Status.LOGOUT)) {
			RetUtils.failMsg("用户已注销");
		}
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		if (ChatService.existFriend(userItem)) {
			return selfAddFriendExistToFriendByApply(touid, uid, userItem, applyid, remarkname).set("greet", items.getGreet()).set("applysigle", true);
		}
		boolean addTo = true;
		if (!ChatService.checkBlack(curr.getId(), uid, touid)) {
			log.warn("好友申请处理警告：申请人把好友加入了黑名单，uid:{},touid:{},applyid:{}", uid, touid, applyid);
			addTo = false;
		}
		if (addTo) {
			return eachAddFriendByApply(user, toUser, items, remarkname).set("greet", items.getGreet()).set("applysigle", false);
		} else {
			// 此处为审核方加邀请方好友，但主动加好友的人把被邀请人拉黑，无权限家人
			return selfAddFriendBlockToFriend(user, toUser, items, remarkname).set("greet", items.getGreet());
		}
	}

	/**
	 * @param curr
	 * @param items
	 * @param remarkname
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年2月4日 下午3:08:05
	 */
	public Ret ignoreApply(User curr, WxFriendApplyItems items) throws Exception {
		if (items == null) {
			return RetUtils.failMsg("申请记录不存在");
		}
		boolean apply = FriendApplyService.me.update(items.getId(), Const.ApplyStatus.REJECT);
		if (!apply) {
			return RetUtils.failMsg("申请记录修改失败");
		}
		return RetUtils.okOper();
	}

	/**
	 * 主动加好友-对方已加自己 -已调整
	 * 业务场景：A加B好友，但是B已经加了A好友 处理流程：1、A的索引可能存在，如果存在进行绑定操作
	 * 
	 * @param uid
	 * @param touid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年1月9日 下午5:03:05
	 */
	public Ret selfAddFriendExistToFriend(Integer uid, Integer touid, WxChatUserItem touserItem) {
		WxFriendApplyItems items = FriendApplyService.me.getApply(touid, uid);
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				// 保存好友信息
				WxFriend friend = friendInit(uid, touid, null, "", false);
				if (friend == null) {
					failRet("重复加好友");
					return false;
				}
				WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
				if (oldItem != null) {
					WxChatUserItem update = new WxChatUserItem();
					update.setUid(uid);
					update.setChatmode(Const.ChatMode.P2P);
					update.setBizid(new Long(touid));
					update.setLinkflag(Const.YesOrNo.YES);
					update.setLinkid(friend.getId());
					if (touserItem.getChatlinkid() != null) {// 如果对方的会话存在
						update.setTochatlinkid(touserItem.getChatlinkid());
						update.setTochatlinkmetaid(touserItem.getChatlinkmetaid());
					}
					boolean updateflag = update.update();
					if (!updateflag) {
						return false;
					}
				} else {
					// 创建用户索引
					int count = ChatIndexService.me.chatUserInit(uid, Const.ChatMode.P2P, new Long(touid), null, null, touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(),
					        friend.getId(), Const.YesOrNo.YES, null);
					if (count <= 0) {
						failRet("创建索引冲突");
						return false;
					}
				}
				// 更新对方用户索引
				boolean update = ChatIndexService.me.chatUserIndexUpdate(touid, new Long(uid), Const.ChatMode.P2P, Const.YesOrNo.YES, null, null, null, null, null);
				if (!update) {
					failRet("更新索引失败");
					return false;
				}
				if (touserItem.getChatlinkid() != null) {
					boolean toUpdateChat = ChatService.me.updateChatLink(Const.YesOrNo.YES, touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(), null);
					if (!toUpdateChat) {
						failRet("更新好友会话失败");
						return false;
					}
				}
				if (items != null && !Objects.equals(items.getStatus(), Const.ApplyStatus.PASS)) {
					boolean isupdate = FriendApplyService.me.update(items.getId(), Const.ApplyStatus.PASS);
					if (!isupdate) {
						failRet("更新申请记录失败");
						return false;
					}
				}
				retObj = RetUtils.okData(friend).set("friend", friend);
				return true;
			}
		};
		boolean add = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (add) {
			ChatIndexService.clearChatP2pIndex(uid, touid);
			ChatIndexService.clearChatP2pIndex(touid, uid);
			if (touserItem.getChatlinkid() != null) {
				ChatIndexService.removeChatItemsCache(touserItem.getChatlinkid());
			}
		}
		return atom.getRetObj();
	}

	/**
	 * 好友申请-一方已加好友-已调整
	 * @param uid
	 * @param touid
	 * @param touserItem
	 * @return
	 * @author lixinji 2020年3月9日 上午10:23:44
	 */
	public Ret selfAddFriendExistToFriendByApply(Integer uid, Integer touid, WxChatUserItem touserItem, Integer applyid, String remarkname) {
		WxFriendApplyItems items = FriendApplyService.me.getById(applyid);
		User user = UserService.ME.getById(touid);
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				// 保存好友信息
				WxFriend friend = friendInit(uid, touid, null, remarkname, false);
				if (friend == null) {
					return failRet("重复加好友");
				}
				// 初始化会话
				WxChatItems chatItems = new WxChatItems();
				chatItems.setUid(uid);
				chatItems.setChatmode(Const.ChatMode.P2P);
				chatItems.setBizid(new Long(touid));
				chatItems.setAvatar(user.getAvatar());
				chatItems.setLinkflag(Const.YesOrNo.YES);
				chatItems.setName(StrUtil.isNotBlank(remarkname) ? remarkname : user.getNick());
				chatItems.setLinkid(friend.getId());
				chatItems.setFidkey(UserService.twoUid(uid, touid));
				boolean chatinit = chatItems.save();
				if (!chatinit) {
					return failRet("聊天会话保存失败");
				}
				WxChatItemsMeta meta = new WxChatItemsMeta();
				meta.setChatlinkid(chatItems.getId());
				meta.setViewflag(Const.YesOrNo.YES);
				meta.setChatuptime(new Date());
				boolean metainit = meta.save();
				if (!metainit) {
					return failRet("聊天动态会话保存失败");
				}
				chatItems.setChatlinkmetaid(meta.getId());
				chatItems.setMeta(meta);
				boolean toInit = false;
				// 初始化自己的会话
				if (!ChatService.friendExistChat(touserItem)) {
					User touser = UserService.ME.getById(touid);
					WxFriend myfriend = FriendService.me.getFriendInfo(touserItem.getLinkid());
					WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
					        touser.getAvatar(), StrUtil.isNotBlank(myfriend.getRemarkname()) ? myfriend.getRemarkname() : touser.getNick(), Const.YesOrNo.YES, Const.YesOrNo.YES, 0,
					        Const.YesOrNo.YES, null, "", "", Const.YesOrNo.NO, null, null, null, null, myfriend.getMsgfreeflag());
					if (toChatItems == null) {
						return failRet("初始化对方聊天会话失败");
					}
					touserItem.setChatlinkid(toChatItems.getId());
					touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
					toInit = true;
				}
				// 初始化索引
				WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
				if (oldItem != null) {
					WxChatUserItem update = new WxChatUserItem();
					update.setUid(uid);
					update.setChatmode(Const.ChatMode.P2P);
					update.setBizid(new Long(touid));
					update.setLinkflag(Const.YesOrNo.YES);
					update.setLinkid(friend.getId());
					update.setChatlinkid(chatItems.getId());
					update.setChatlinkmetaid(chatItems.getMeta().getId());
					if (touserItem.getChatlinkid() != null) {// 如果对方的会话存在
						update.setTochatlinkid(touserItem.getChatlinkid());
						update.setTochatlinkmetaid(touserItem.getChatlinkmetaid());
					}
					boolean updateflag = update.update();
					if (!updateflag) {
						return failRet("已存在的索引修改失败");
					}
				} else {
					// 创建用户索引
					int count = ChatIndexService.me.chatUserInit(uid, Const.ChatMode.P2P, new Long(touid), chatItems.getId(), chatItems.getChatlinkmetaid(),
					        touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(), friend.getId(), Const.YesOrNo.YES, null);
					if (count <= 0) {
						return failRet("创建索引冲突");
					}
				}
				if (toInit) {
					// 修改索引状态
					boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(touid, new Long(uid), Const.ChatMode.P2P, touserItem.getChatlinkid(),
					        touserItem.getChatlinkmetaid(), chatItems.getId(), chatItems.getChatlinkmetaid(), null, Const.YesOrNo.YES);
					if (!chatindex) {
						return failRet("修改索引状态异常");
					}
				} else {
					// 更新对方用户索引
					boolean update = ChatIndexService.me.chatUserIndexUpdate(touid, new Long(uid), Const.YesOrNo.YES, Const.YesOrNo.YES, null, touserItem.getChatlinkid(),
					        touserItem.getChatlinkmetaid(), chatItems.getId(), chatItems.getChatlinkmetaid());
					if (!update) {
						failRet("更新索引失败");
						return false;
					}
					boolean toUpdateChat = ChatService.me.updateChatLink(Const.YesOrNo.YES, touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(), null);
					if (!toUpdateChat) {
						failRet("更新好友会话失败");
						return false;
					}
				}
				if (items != null && !Objects.equals(items.getStatus(), Const.ApplyStatus.PASS)) {
					boolean isupdate = FriendApplyService.me.update(items.getId(), Const.ApplyStatus.PASS);
					if (!isupdate) {
						failRet("更新申请记录失败");
						return false;
					}
				}
				retObj = RetUtils.okData(friend).set("send", "double").set("tochatlinkid", chatItems.getId()).set("chatlinkid", touserItem.getChatlinkid()).set("friend", friend);
				return true;
			}
		};
		boolean add = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (add) {
			ChatIndexService.clearChatP2pIndex(uid, touid);
			ChatIndexService.clearChatP2pIndex(touid, uid);
			if (touserItem.getChatlinkid() != null) {
				ChatIndexService.removeChatItemsCache(touserItem.getChatlinkid());
			}
		}
		return atom.getRetObj();
	}

	/**
	 * 主动添加好友-已调整
	 * 双方互相加 主动添加好友的都不发送通知
	 * 
	 * @param user
	 * @param toUser
	 * @return
	 * @author lixinji 2020年1月9日 下午5:50:24
	 */
	public Ret eachAddFriend(User user, User toUser) {
		Integer uid = user.getId();
		Integer touid = toUser.getId();
		WxFriendApplyItems items = FriendApplyService.me.getApply(touid, uid);
		WxFriendApplyItems toitems = FriendApplyService.me.getApply(uid, touid);

		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				int apply = FriendApplyService.me.applyInit(uid, touid, "默认加为好友", Const.YesOrNo.YES, Const.YesOrNo.YES);
				if (apply <= 0) {
					return failRet("自动申请记录保存失败");
				}
				WxFriend friend = friendInit(uid, touid, null, "", true);
				if (friend == null) {
					return failRet("好友记录保存失败");
				}
				WxFriend tofriend = friendInit(touid, uid, null, user.getNick());
				if (tofriend == null) {
					return failRet("to好友记录保存失败");
				}
				// 关联用户索引
				int indexinit = ChatIndexService.me.chatUserInit(friend.getId(), uid, Const.ChatMode.P2P, new Long(touid));
				if (indexinit <= 0) {
					return failRet("好友索引保存失败");
				}
				int toindexinit = ChatIndexService.me.chatUserInit(tofriend.getId(), touid, Const.ChatMode.P2P, new Long(uid));
				if (toindexinit <= 0) {
					return failRet("to好友索引保存失败");
				}
				if (items != null && !Objects.equals(items.getStatus(), Const.ApplyStatus.PASS)) {
					boolean isupdate = FriendApplyService.me.update(items.getId(), Const.ApplyStatus.PASS);
					if (!isupdate) {
						return failRet("更新申请记录失败");
					}
				}
				if (toitems != null && !Objects.equals(toitems.getStatus(), Const.ApplyStatus.PASS)) {
					boolean isupdate = FriendApplyService.me.update(toitems.getId(), Const.ApplyStatus.PASS);
					if (!isupdate) {
						return failRet("to更新申请记录失败");
					}
				}
				retObj = RetUtils.okData(friend).set("friend", friend).set("tofriend", tofriend);
				return true;
			}
		};
		Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		// 不做缓存处理
		return atom.getRetObj();

	}

	/**
	 * 申请通过后，互相加好友-已调整
	 * 
	 * @param user    申请方
	 * @param toUser  被加好友-此时应该是操作者
	 * @param applyid
	 * @return
	 * @author lixinji 2020年1月9日 下午7:10:58
	 */
	public Ret eachAddFriendByApply(User user, User toUser, WxFriendApplyItems items, String remarkName) {
		Integer uid = user.getId();
		Integer touid = toUser.getId();
		Integer applyid = items.getId();
		if (!Objects.equals(Const.ApplyStatus.APPLY, items.getStatus())) {
			return RetUtils.failMsg("审核记录不存在");
		}
		WxFriendApplyItems toItems = FriendApplyService.me.getApply(touid, uid);
		// 创建双方聊天会话
		WxChatItems chatItems = new WxChatItems();
		chatItems.setUid(uid);
		chatItems.setChatmode(Const.ChatMode.P2P);
		chatItems.setBizid(new Long(touid));
		chatItems.setAvatar(toUser.getAvatar());
		chatItems.setLinkflag(Const.YesOrNo.YES);
		chatItems.setName(toUser.getNick());
		chatItems.setFidkey(UserService.twoUid(uid, touid));
		WxChatItemsMeta meta = new WxChatItemsMeta();
		meta.setViewflag(Const.YesOrNo.YES);
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				boolean apply = FriendApplyService.me.update(applyid, Const.ApplyStatus.PASS);
				if (!apply) {
					return failRet("申请记录修改失败");
				}
				WxFriend friend = friendInit(uid, touid, null, "", true);
				if (friend == null) {
					return failRet("好友记录保存失败");
				}
				WxFriend tofriend = friendInit(touid, uid, null, remarkName);
				if (tofriend == null) {
					return failRet("to好友记录保存失败");
				}
				chatItems.setLinkid(friend.getId());
				meta.setChatuptime(new Date());
				boolean chatinit = chatItems.save();
				if (!chatinit) {
					return failRet("聊天会话保存失败");
				}
				meta.setChatlinkid(chatItems.getId());
				boolean metainit = meta.save();
				if (!metainit) {
					return failRet("聊天动态会话保存失败");
				}
				chatItems.setChatlinkmetaid(meta.getId());
				chatItems.setMeta(meta);
				WxChatItems tochatItems = ChatService.me.chatitemsInitToItem(chatItems, tofriend, user);
				boolean toChatinit = tochatItems.save();
				if (!toChatinit) {
					return failRet("to聊天会话保存失败");
				}
				tochatItems.getMeta().setChatlinkid(tochatItems.getId());
				boolean tometainit = tochatItems.getMeta().save();
				if (!tometainit) {
					return failRet("to聊天动态会话保存失败");
				}
				tochatItems.setChatlinkmetaid(tochatItems.getMeta().getId());
				// 关联用户索引
				int indexinit = ChatIndexService.me.chatUserInit(friend.getId(), uid, Const.ChatMode.P2P, new Long(touid), chatItems.getId(), chatItems.getChatlinkmetaid(),
				        tochatItems.getId(), tochatItems.getChatlinkmetaid());
				if (indexinit <= 0) {
					return failRet("好友索引保存失败");
				}
				int toindexinit = ChatIndexService.me.chatUserInit(tofriend.getId(), touid, Const.ChatMode.P2P, new Long(uid), tochatItems.getId(), tochatItems.getChatlinkmetaid(),
				        chatItems.getId(), chatItems.getChatlinkmetaid());
				if (toindexinit <= 0) {
					return failRet("to好友索引保存失败");
				}
				if (toItems != null && !Objects.equals(toItems.getStatus(), Const.ApplyStatus.PASS)) {
					boolean isupdate = FriendApplyService.me.update(toItems.getId(), Const.ApplyStatus.PASS);
					if (!isupdate) {
						return failRet("to更新申请记录失败");
					}
				}
				retObj = RetUtils.okData(tofriend).set("send", "double").set("chatlinkid", chatItems.getId()).set("tochatlinkid", tochatItems.getId()).set("friend", friend)
				        .set("tofriend", tofriend);
				return true;
			}
		};
		Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		// 不做缓存处理
		return atom.getRetObj();
	}

	/**
	 * 被动加好友（申请通过后）-已调整
	 * 对方拉黑自己-此方法调整了参数方向，请扩展业务者注意uid和touid的归属
	 * 
	 * @param uid   申请方
	 * @param touid 被邀请方-业务应该是操作者
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年1月9日 下午5:03:05
	 */
	public Ret selfAddFriendBlockToFriend(User user, User toUser, WxFriendApplyItems items, String remarkName) {
		Integer uid = user.getId();
		Integer touid = toUser.getId();
		Integer applyid = items.getId();
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				boolean apply = FriendApplyService.me.update(applyid, Const.ApplyStatus.PASS);
				if (!apply) {
					failRet("申请记录修改失败");
					return false;
				}
				// 保存好友信息
				WxFriend friend = friendInit(touid, uid, null, remarkName, false);
				if (friend == null) {
					return failRet("重复加好友");
				}
				WxChatItems chatItems = new WxChatItems();
				chatItems.setUid(touid);
				chatItems.setChatmode(Const.ChatMode.P2P);
				chatItems.setBizid(new Long(uid));
				chatItems.setAvatar(user.getAvatar());
				chatItems.setLinkflag(Const.YesOrNo.NO);
				chatItems.setName(StrUtil.isNotBlank(remarkName) ? remarkName : user.getNick());
				chatItems.setLinkid(friend.getId());
				chatItems.setFidkey(UserService.twoUid(uid, touid));
				boolean chatinit = chatItems.save();
				if (!chatinit) {
					return failRet("聊天记录保存失败");
				}
				WxChatItemsMeta meta = new WxChatItemsMeta();
				meta.setViewflag(Const.YesOrNo.YES);
				meta.setChatuptime(new Date());
				boolean metainit = meta.save();
				if (!metainit) {
					return failRet("聊天记录保存失败");
				}
				chatItems.setMeta(meta);
				chatItems.setChatlinkmetaid(meta.getId());
				// 创建用户索引
				int count = ChatIndexService.me.chatUserInit(touid, Const.ChatMode.P2P, new Long(uid), chatItems.getId(), chatItems.getChatlinkmetaid(), null, null, friend.getId(),
				        Const.YesOrNo.NO, null);
				if (count <= 0) {
					return failRet("创建索引冲突");
				}
				// 此处返回的是操作者方向的数据，所有chatlinkid的方向是反的
				retObj = RetUtils.okData(friend).set("send", "one").set("tochatlinkid", chatItems.getId()).set("friend", friend);
				return true;
			}
		};
		Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		// 不需要做缓存处理
		return atom.getRetObj();
	}

	/**
	 * 删除好友记录-已调整
	 * 
	 * @param fid
	 * @return
	 * @author lixinji 2020年1月10日 下午3:03:26
	 */
	public boolean delFriend(Long fid) {
		return WxFriend.dao.deleteById(fid);
	}

	/**
	 * 删除好友-已调整
	 * @param curr
	 * @param frienduid
	 * @param ipid
	 * @return
	 * @throws Exception
	 * @author lixinji 2020年1月9日 下午5:55:20
	 */
	public Ret delFriend(User curr, Integer touid, Integer ipid) throws Exception {
		if (touid == null) {
			return RetUtils.invalidParam();
		}
		Integer uid = curr.getId();
		WxChatUserItem userItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
		if (!ChatService.existFriend(userItem)) {
			return RetUtils.failMsg("好友不存在");
		}
		WxChatUserItem toUserItem = ChatIndexService.chatUserIndex(touid, uid, Const.ChatMode.P2P);
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				// 删除好友记录
				boolean del = delFriend(userItem.getLinkid());
				if (!del) {
					return failRet("删除好友失败");
				}
				// 清空申请记录
				FriendApplyService.me.removeApply(touid, uid);
				// 删除聊天会话
				if (ChatService.friendExistChat(uid, touid)) {
					boolean remove = ChatService.me.deleteChatItem(userItem.getChatlinkid());
					if (!remove) {
						return failRet("清除聊天列表失败");
					}
				}
				// 判断对方是否删除
				if (ChatService.existFriend(toUserItem)) {
					boolean updateIndex = ChatIndexService.me.chatUserIndexUpdate(uid, userItem.getBizid(), userItem.getChatmode(), WxChatUserItem.UserIndexOper.DEL_FRIEND);
					if (!updateIndex) {
						return failRet("修改好友索引失败");
					}
					boolean toUpdateIndex = ChatIndexService.me.chatUserIndexUpdate(touid, toUserItem.getBizid(), toUserItem.getChatmode(),
					        WxChatUserItem.UserIndexOper.TO_DEL_FRIEND);
					if (!toUpdateIndex) {
						return failRet("修改对方好友索引失败");
					}
					if (toUserItem.getChatlinkid() != null) {
						boolean toUpdateChat = ChatService.me.updateChatLink(Const.YesOrNo.NO, toUserItem.getChatlinkid(), toUserItem.getChatlinkmetaid(), null);
						if (!toUpdateChat) {
							return failRet("修改对方好友会话失败");
						}
					}
				} else {
					boolean delIndex = ChatIndexService.me.chatUserIndexDel(uid, userItem.getBizid(), userItem.getChatmode());
					if (!delIndex) {
						return failRet("删除好友索引失败");
					}
					if (toUserItem != null) {
						boolean toDelIndex = ChatIndexService.me.chatUserIndexDel(touid, toUserItem.getBizid(), toUserItem.getChatmode());
						if (!toDelIndex) {
							return failRet("删除对方好友索引失败");
						}
						//删除好友消息
						ChatMsgService.me.delFriendMsg(toUserItem.getFidkey());
					}
				}
				// 保存操作日志
				saveDelFriendOper(uid, touid, ipid);
				return true;
			}
		};
		boolean delFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (delFlag) {
			Ret ret = RetUtils.okOper();
			ChatIndexService.clearChatP2pIndex(uid, touid);
			ChatIndexService.clearChatP2pIndex(touid, uid);
			if (userItem.getChatlinkid() != null) {
				ret.set("chatlinkid", userItem.getChatlinkid());
				ChatIndexService.removeChatItemsCache(userItem.getChatlinkid());
				// 清除聊天记录
				clearP2pChatCache(userItem.getChatlinkid());
			}
			if (ChatService.friendExistChat(toUserItem)) {
				ret.set("tochatlinkid", toUserItem.getChatlinkid());
				ChatIndexService.removeChatItemsCache(toUserItem.getChatlinkid());
			}
			ChatIndexService.clearMailListCache(uid);
			return ret.set("fid", userItem.getLinkid());
		} else {
			return atom.getRetObj();
		}
	}

	/**
	 * 好友初始化-有统计-已调整
	 * 此方法只用在两个好友初始化中，第一个创建者使用，请注意
	 * @param uid
	 * @param touid
	 * @return boolean isEach
	 * @author lixinji 2020年1月9日 下午4:28:30
	 */
	public WxFriend friendInit(Integer uid, Integer touid, Long msgid, String remark, boolean isEach) {
		WxFriend friend = new WxFriend();
		friend.setUid(uid);
		friend.setStartmsgid(msgid);
		friend.setFrienduid(touid);
		if (StrUtil.isBlank(remark)) {
			User user = UserService.ME.getById(touid);
			friend.setChatindex(PyUtils.getFristChat(user.getNick()));
		} else {
			//放在这么，避免remarkname存在两个默认值：null 和空字符串
			friend.setRemarkname(remark);
			friend.setChatindex(PyUtils.getFristChat(remark));
		}
		int count = friend.ignoreSave();
		if (count < 1) {
			return null;
		}
		WxFriendMeta meta = new WxFriendMeta();
		meta.setUid(uid);
		meta.setTouid(touid);
		meta.setFidkey(UserService.twoUid(uid, touid));
		if (isEach) {
			meta.replaceSave();
		} else {
			meta.ignoreSave();
		}
		ChatIndexService.clearMailListCache(uid);
		return friend;
	}

	/**
	 * 好友初始化-无统计-已调整
	 * @param uid
	 * @param touid
	 * @param msgid
	 * @param remark
	 * @return
	 * @author lixinji
	 * 2020年7月14日 下午3:27:07
	 */
	public WxFriend friendInit(Integer uid, Integer touid, Long msgid, String remark) {
		WxFriend friend = new WxFriend();
		friend.setUid(uid);
		friend.setStartmsgid(msgid);
		friend.setFrienduid(touid);
		if (StrUtil.isBlank(remark)) {
			User user = UserService.ME.getById(touid);
			friend.setChatindex(PyUtils.getFristChat(user.getNick()));
		} else {
			friend.setChatindex(PyUtils.getFristChat(remark));
			friend.setRemarkname(remark);
		}

		int count = friend.ignoreSave();
		if (count < 1) {
			return null;
		}
		ChatIndexService.clearMailListCache(uid);
		return friend;
	}

	/**
	 * @param groupid
	 * @param freeflag
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年1月26日 上午11:07:07
	 */
	public Ret modifyFdPush(Integer touid, Short freeflag, Integer uid) {
		WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
		if (!ChatService.existTwoFriend(userItem)) {
			return RetUtils.failMsg("你们已不是有效的好友");
		}

		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxFriend update = new WxFriend();
				update.setId(userItem.getLinkid());
				update.setMsgfreeflag(freeflag);
				boolean up = update.update();
				if (!up) {
					return failRet("操作失败");
				}
				if (userItem.getChatlinkid() != null) {
					ChatService.me.updateChatMsgFreeFlag(freeflag, userItem.getChatlinkid());
					return okRet(RetUtils.okData(Const.YesOrNo.YES).set("chatlinkid", userItem.getChatlinkid()));
				}
				return okRet(Const.YesOrNo.NO);
			}
		};
		boolean oper = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!oper) {
			return atom.getRetObj();
		}
		ChatIndexService.clearFriendInfoCache(userItem.getLinkid(), uid);
		return atom.getRetObj();
	}

	/**
	 * 删除好友记录保存-已调整
	 * @param friend
	 * @param ipid
	 * @author lixinji 2020年1月10日 下午3:09:14
	 */
	public void saveDelFriendOper(Integer uid, Integer touid, Integer ipid) {
		WxFriendDelLog wxFriendDelLog = new WxFriendDelLog();
		wxFriendDelLog.setIpid(ipid);
		wxFriendDelLog.setFrienduid(uid);
		wxFriendDelLog.setUid(touid);
		wxFriendDelLog.save();
	}

	/**
	 * 删除消息-已调整
	 * 
	 * @param chatUserItem
	 * @param msg
	 * @return
	 * @author lixinji 2020年2月11日 下午1:08:29
	 */
	public Ret delMsg(WxChatUserItem chatUserItem, String mids) {
		Integer uid = chatUserItem.getUid();
		WxChatItems chatItems = ChatService.me.getAllChatItems(chatUserItem.getChatlinkid());
		AbsTxAtom atom = new AbsTxAtom() {
			@Override
			public boolean noTxRun() {
				String[] midArr = mids.split(",");
				boolean lastFlag = false;
				for (String midStr : midArr) {
					Long mid = Long.parseLong(midStr);
					WxFriendMsg delmsg = WxFriendMsg.dao.findById(mid);
					if (delmsg == null) {
						continue;
					}
					if (Objects.equals(delmsg.getSigleflag(), Const.YesOrNo.YES) || Objects.equals(delmsg.getUid(), delmsg.getTouid())) {// 单通道消息处理
						boolean del = delmsg.delete();
						if (!del) {
							return failRet(RetUtils.sysError());
						}
					} else {
						WxFriendMsg updateMsg = new WxFriendMsg();
						updateMsg.setId(delmsg.getId());
						updateMsg.setSigleflag(Const.YesOrNo.YES);
						updateMsg.setSigleuid(chatUserItem.getBizid().intValue());
						boolean update = updateMsg.update();
						if (!update) {
							return failRet(RetUtils.sysError());
						}
					}
					if (Objects.equals(chatItems.getLastmsgid(), delmsg.getId())) {
						lastFlag = true;
					}
				}
				Short clear = Const.YesOrNo.NO;
				Short chat = Const.YesOrNo.NO;
				if (lastFlag) {
					WxFriendMsg lastmsg = ChatMsgService.me.getLastMsg(chatUserItem.getFidkey(), chatUserItem.getBizid().intValue());
					if (lastmsg == null) {
						boolean startTx = ChatIndexService.me.chatuserStartMsg(chatItems.getUid(), chatItems.getBizid(), chatItems.getChatmode(), null);
						if (!startTx) {
							return failRet("修改起始消息异常");
						}
						boolean clearMsg = ChatService.me.clearChatItemMsg(chatUserItem.getChatlinkmetaid(), null);
						if (!clearMsg) {
							return failRet("清空消息异常");
						}
						clear = Const.YesOrNo.YES;
					} else {
						Short toread = null;
						if (Objects.equals(lastmsg.getUid(), uid)) {
							toread = lastmsg.getReadflag();
						}
						Ret itemRet = ChatMsgService.me.afterSendFriendChatMsg(lastmsg, null, chatUserItem.getChatlinkmetaid(), null, toread, null);
						if (itemRet.isFail()) {
							return failRet(itemRet);
						}
						chat = Const.YesOrNo.YES;
					}
				}
				return okRet(Ret.ok().set("clear", clear).set("chat", chat));
			}
		};
		boolean del = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!del) {
			return atom.getRetObj();
		}
		Short clear = RetUtils.getOkTData(atom.getRetObj(), "clear");
		Short chat = RetUtils.getOkTData(atom.getRetObj(), "chat");
		Short send = Const.YesOrNo.NO;
		WxChatItems updateChatItems = null;
		if (Objects.equals(clear, Const.YesOrNo.YES)) {
			// 清除索引
			ChatIndexService.clearChatUserIndex(chatItems.getUid(), chatItems.getBizid(), chatItems.getChatmode());
			// 移除会话缓存
			ChatIndexService.removeChatItemsCache(chatUserItem.getChatlinkid());
			send = Const.YesOrNo.YES;
			updateChatItems = ChatService.me.getAllChatItems(chatUserItem.getChatlinkid());
		} else if (Objects.equals(chat, Const.YesOrNo.YES)) {
			// 移除会话缓存
			ChatIndexService.removeChatItemsCache(chatUserItem.getChatlinkid());
			send = Const.YesOrNo.YES;
			updateChatItems = ChatService.me.getAllChatItems(chatUserItem.getChatlinkid());
		}
		// 清空聊天记录
		FriendService.me.clearP2pChatCache(chatUserItem.getChatlinkid());
		return Ret.ok().set("chatindex", chatUserItem).set("msg", null).set("send", send).set("chatItems", updateChatItems);
	}

	/**
	 * 撤回消息-已调整
	 * 可优化处理
	 * 
	 * @param chatUserItem
	 * @param msg
	 * @return
	 * @author lixinji 2020年2月11日 下午1:07:14
	 */
	public Ret backMsg(WxChatUserItem chatUserItem, String midStr) {
		Long mid = Long.parseLong(midStr);
		WxFriendMsg msg = WxFriendMsg.dao.findById(mid);
		if (msg == null) {
			return RetUtils.failMsg("消息不存在");
		}
		long offset = DateUtil.betweenMs(msg.getTime(), new Date());
//		if (offset > ChatMsgService.MSG_BACK_MAX_TIME) {
//			return RetUtils.failMsg("超过2分钟,无法撤回");
//		}
		boolean del = delP2PMsg(msg.getId());
		if (!del) {
			return RetUtils.sysError();
		}
		// 清空聊天记录
		FriendService.me.clearP2pChatCache(chatUserItem.getChatlinkid());
		if (chatUserItem.getTochatlinkid() != null) {
			FriendService.me.clearP2pChatCache(chatUserItem.getTochatlinkid());
		}
		return Ret.ok().set("chatindex", chatUserItem).set("msg", msg);
	}
	
	/**
	 * @param id
	 * @return
	 * @author lixinji
	 * 2022年3月4日 下午7:10:14
	 */
	public boolean delP2PMsg(Long id) {
		Kv params = Kv.by("id", id);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.delP2pMsgBak", params);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		return WxFriendMsg.dao.deleteById(id);
	}

	/**
	 * 新增聊天消息-正常文本聊天
	 * 
	 * @param devicetype 触发发送的设备类型
	 * @param sessionid  触发发送方的session
	 * @param ip         发送方的ip
	 * @param text       消息内容
	 * @param uid        触发发送用户uid
	 * @param touid      接受方的uid
	 * @return
	 * @author lixinji 2020年1月17日 下午2:35:50
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, String text, Integer uid, Integer touid, FocusVo isread, String appversion) {
		return addChatMsg(devicetype, sessionid, ip, text, uid, touid, Const.ContentType.TEXT, isread, appversion);
	}

	/**
	 * 新增聊天消息-正常聊天（含消息内容类型）
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param contenttype 消息类型
	 * @return
	 * @author lixinji 2020年1月17日 下午2:56:10
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, String text, Integer uid, Integer touid, Short contenttype, FocusVo isread, String appversion) {
		return addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, uid, touid, contenttype, Const.YesOrNo.NO, null, isread, appversion);
	}

	/**
	 * 新增聊天消息-转发聊天消息
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param contenttype 消息类型
	 * @param frommsgid   转发来源消息id
	 * @param sysflag
	 * @return
	 * @author lixinji 2020年1月17日 下午3:04:02
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, String text, Integer uid, Integer touid, Short contenttype, Long frommsgid, Short fromchatmode,
	        Short sysflag, String operBizData, FocusVo isread, String appversion,SysMsgVo msgVo) {
		return addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, uid, touid, contenttype, sysflag, null, WxFriendMsg.MsgType.NORMAL, null, isread, operBizData,
		        frommsgid, fromchatmode, appversion,msgVo);
	}

	/**
	 * 新增聊天消息-正常消息（含系统消息）
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param reverseflag 反向发送标识
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param contenttype 消息类型
	 * @param sendbysys   是否是系统消息标识
	 * @param sigleuid    消息归属uid
	 * @return
	 * @author lixinji 2020年1月17日 下午3:45:41
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, Short reverseflag, String text, Integer uid, Integer touid, Short contenttype, Short sendbysys,
	        Integer sigleuid, FocusVo isread, String appversion) {
		return addChatMsg(devicetype, sessionid, ip, reverseflag, text, uid, touid, contenttype, sendbysys, sigleuid, WxFriendMsg.MsgType.NORMAL, null, isread, "", null, null,
		        appversion,null);
	}

	/**
	 * 自己发送给自己的系统消息-单通道
	 * 
	 * @param devicetype 触发发送的设备类型
	 * @param sessionid  触发发送方的session
	 * @param ip         发送方的ip
	 * @param uid        触发发送用户uid
	 * @param text       消息内容
	 * @param touid      另外的用户id
	 * @return
	 * @author lixinji 2020年2月4日 下午11:15:14
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, Integer uid, String text, Integer touid, Short oper, FocusVo isread, String appversion) {
		return addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, uid, touid, Const.ContentType.TEXT, Const.YesOrNo.YES, uid, WxFriendMsg.MsgType.NORMAL, oper, isread,
		        "", null, null, appversion,null);
	}

	/**
	 * 自己发送给自己的系统消息-单通道(含操作)
	 * 
	 * @param devicetype 触发发送的设备类型
	 * @param sessionid  触发发送方的session
	 * @param ip         发送方的ip
	 * @param text       消息内容
	 * @param uid        触发发送用户uid
	 * @param oper       操作码
	 * @return
	 * @author lixinji 2020年1月19日 下午2:45:14
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, String text, Integer uid, Short oper, String operbizdata, FocusVo isread, String appversion) {
		Short msgtype = WxFriendMsg.MsgType.NORMAL;
		if (oper != null) {
			msgtype = WxFriendMsg.MsgType.oper;
		}
		return addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, uid, uid, Const.ContentType.TEXT, Const.YesOrNo.YES, uid, msgtype, oper, isread, operbizdata, null,
		        null, appversion,null);
	}

	/**
	 * 新增聊天消息-正常文本聊天-单通道
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param reverseflag 反向发送标识
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param sigleuid    消息归属者
	 * @return
	 * @author lixinji 2020年1月17日 下午2:59:33
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, Short reverseflag, String text, Integer uid, Integer touid, Integer sigleuid, FocusVo isread,
	        String appversion) {
		return addChatMsg(devicetype, sessionid, ip, reverseflag, text, uid, touid, Const.ContentType.TEXT, Const.YesOrNo.NO, sigleuid, WxFriendMsg.MsgType.NORMAL, null, isread,
		        "", null, null, appversion,null);
	}

	/**
	 * 新增聊天消息-操作消息-单通道
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param reverseflag 反向发送标识
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param oper        操作码
	 * @param sigleuid    消息归属者
	 * @return
	 * @author lixinji 2020年1月17日 下午3:02:20
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, Short reverseflag, String text, Integer uid, Integer touid, Short oper, Integer sigleuid,
	        FocusVo isread, String appversion) {
		Short msgtype = WxFriendMsg.MsgType.NORMAL;
		if (oper != null) {
			msgtype = WxFriendMsg.MsgType.oper;
		}
		return addChatMsg(devicetype, sessionid, ip, reverseflag, text, uid, touid, Const.ContentType.TEXT, Const.YesOrNo.YES, sigleuid, msgtype, oper, isread, "", null, null,
		        appversion,null);
	}

	/**
	 * 新增聊天消息-全参数
	 * 
	 * @param devicetype  触发发送的设备类型
	 * @param sessionid   触发发送方的session
	 * @param ip          发送方的ip
	 * @param reverseflag 反向发送标识
	 * @param text        消息内容
	 * @param uid         触发发送用户uid
	 * @param touid       接受方的uid
	 * @param contenttype 消息类型
	 * @param sendbysys   是否是系统消息标识
	 * @param sigleuid    单通道消息拥有者uid
	 * @param msgtype     消息类型
	 * @param oper        操作消息的操作码
	 * @param frommsgid   转发消息来源id
	 * @return
	 * @author lixinji 2020年1月17日 下午2:26:06
	 */
	public WxFriendMsg addChatMsg(Short devicetype, String sessionid, String ip, Short reverseflag, String text, Integer uid, Integer touid, Short contenttype, Short sendbysys,
	        Integer sigleuid, Short msgtype, Short oper, FocusVo focusVo, String operBizData, Long frommsgid, Short fromchatmode, String appversion,SysMsgVo msgVo) {
		// 存聊天日志
		IpInfo ipInfo = IpInfoService.ME.save(ip);
		WxFriendMsg wxFriendMsg = new WxFriendMsg();
		wxFriendMsg.setFrommode(fromchatmode);
		String resume = "";
		if (StrUtil.isNotBlank(appversion)) {
			wxFriendMsg.setAppversion(appversion);
		}
		WxCallItemVo call = null;
		switch (contenttype) {
		case Const.ContentType.QUOTE_MSG:
			if (Const.SENSITIVE_FLAG) {
				if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
					text = SensitiveWordsService.findAndReplace(text);
				}
			}
			ChatAtom chatAtom = Json.toBean(text, ChatAtom.class);
			text = chatAtom.getC();
			wxFriendMsg.setQuotemid(Long.valueOf(chatAtom.getQuotemid()));
			wxFriendMsg.setQuotemsgcontent(chatAtom.getQuotemsgcontent());
			wxFriendMsg.setQuotesrcnick(chatAtom.getQuotesrcnick());
			wxFriendMsg.setQuotemsgtype(Short.valueOf(chatAtom.getQuotemsgtype()));
			// 保存缩略文字
			resume = StrUtil.trim(HtmlUtil.cleanHtmlTag(text));
			resume = StrUtil.subWithLength(resume, 0, ConfService.getInt(Const.ConfMapping.WX_FRIEND_MSG_RESUME_MAXSIZE, 50));
			break;
		case Const.ContentType.TEXT:
			if (Const.SENSITIVE_FLAG) {
				if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
					text = SensitiveWordsService.findAndReplace(text);
				}
			}
			// 保存缩略文字
			resume = StrUtil.trim(HtmlUtil.cleanHtmlTag(text));
			resume = StrUtil.subWithLength(resume, 0, ConfService.getInt(Const.ConfMapping.WX_FRIEND_MSG_RESUME_MAXSIZE, 50));
			break;
		case Const.ContentType.AUDIO:
			// 保存音频
			resume = "[语音消息]";
			wxFriendMsg.setAc(text);
			break;
		case Const.ContentType.BLOG:
			// 保存博客
			resume = "发一条博客";
			wxFriendMsg.setBc(text);
			break;
		case Const.ContentType.FILE:
			// 保存文件
			resume = "分享一个文件";
			wxFriendMsg.setFc(text);
			break;
		case Const.ContentType.IMG:
			// 保存图片
			resume = "分享一个图片";
			wxFriendMsg.setIc(text);
			break;
		case Const.ContentType.VIDEO:
			// 保存视频
			resume = "分享一个视频";
			wxFriendMsg.setVc(text);
			break;
		case Const.ContentType.MSG_CARD:
			// 保存名片
			resume = "分享一个名片";
			wxFriendMsg.setCardc(text);
			break;
		case Const.ContentType.CALL_AUDIO:
			// 音频通话
			resume = "[语音通话]";
			wxFriendMsg.setCall(text);
			call = Json.toBean(text, WxCallItemVo.class);
			break;
		case Const.ContentType.CALL_VIDEO:
			// 视频通话
			resume = "[视频通话]";
			call = Json.toBean(text, WxCallItemVo.class);
			wxFriendMsg.setCall(text);
			break;
		case Const.ContentType.REDPACKET:
			//保存视频
			resume = "发送一个红包";
			wxFriendMsg.setRed(text);
			break;
		case Const.ContentType.TEMPLATE:
			//分享一个链接
			resume = "分享一个链接";
			wxFriendMsg.setTemp(text);
			break;
		case Const.ContentType.POSITION:
			//分享一个位置
			resume = "分享一个位置";
			wxFriendMsg.setPosition(text);
			break;
		case Const.ContentType.MOMENT:
            //发布了一条朋友圈
            resume = "发布了一条朋友圈";
            wxFriendMsg.setMoment(text);
            break;
		case Const.ContentType.LIKES:
			//发布了一条朋友圈
			resume = "一条新的点赞消息";
			wxFriendMsg.setMoment(text);
			break;
		case Const.ContentType.COMMENT:
			//发布了一条朋友圈
			resume = "一条新的评论消息";
			wxFriendMsg.setMoment(text);
			break;
		case Const.ContentType.MERGE:
			resume = "转发了一条消息";
			break;
		default:
			break;
		}
		if(msgVo != null) {
			wxFriendMsg.setOper(msgVo.getOpercode());
			wxFriendMsg.setOpersrcmsg(msgVo.getSrctext());
			wxFriendMsg.setOpersrcmsgtype(msgVo.getSrcmsgtype());
		} else {
			// 设置消息操作码，前提消息类型为操作消息
			wxFriendMsg.setOper(oper);
		}
		wxFriendMsg.setSrctext(text);
		wxFriendMsg.setText(text);
		wxFriendMsg.setResume(resume);
		wxFriendMsg.setOperbizdata(operBizData);
		wxFriendMsg.setIpid(ipInfo.getId());
		wxFriendMsg.setDevice(devicetype);
		wxFriendMsg.setTime(new Date());
		wxFriendMsg.setTouid(touid);
		wxFriendMsg.setUid(uid);
		if (Objects.equals(reverseflag, Const.YesOrNo.YES)) {
			wxFriendMsg.setTosession(sessionid);
		} else {
			wxFriendMsg.setSession(sessionid);
		}
		wxFriendMsg.setContenttype(contenttype);
		wxFriendMsg.setStatus(Const.Status.NORMAL);
		if (sendbysys != null) {
			wxFriendMsg.setSendbysys(sendbysys);
		}
		// 设置消息单通道方向
		if (sigleuid != null) {
			wxFriendMsg.setSigleflag(Const.YesOrNo.YES);
			wxFriendMsg.setSigleuid(sigleuid);
		} else {
			wxFriendMsg.setSigleuid(Const.MSG_DEFAULT_UID);
		}
		// 设置消息类型：1：正常消息；2：操作消息
		wxFriendMsg.setMsgtype(msgtype);
		// 设置转发路径
		wxFriendMsg.setFrommsgid(frommsgid);
		if (focusVo != null
				&& !wxFriendMsg.getContenttype().equals(Const.ContentType.MOMENT)
				&& !wxFriendMsg.getContenttype().equals(Const.ContentType.LIKES)
				&& !wxFriendMsg.getContenttype().equals(Const.ContentType.COMMENT)) {
			wxFriendMsg.setReaddevice(focusVo.getDevicetype());
			wxFriendMsg.setReadflag(Const.YesOrNo.YES);
			wxFriendMsg.setReadipid(focusVo.getIpid());
			wxFriendMsg.setReadtime(new Date());
		} else {
			if (call != null && (Objects.equals(call.getHanguptype(), WxCallItem.Hanguptype.CANCELED) || Objects.equals(call.getHanguptype(), WxCallItem.Hanguptype.NORMAL)
			        || Objects.equals(call.getHanguptype(), WxCallItem.Hanguptype.REJECT))) {
				wxFriendMsg.setReaddevice(call.getDevicetype());
				wxFriendMsg.setReadflag(Const.YesOrNo.YES);
				wxFriendMsg.setReadtime(new Date());
			} else {
				wxFriendMsg.setReadflag(Const.YesOrNo.NO);
			}
		}
		if (oper != null) {
			WxFriendOperMsg operMsg = new WxFriendOperMsg();
			BeanUtil.copyProperties(CPI.getAttrs(wxFriendMsg), CPI.getAttrs(operMsg));
			operMsg.setTwouid(UserService.twoUid(wxFriendMsg.getUid(), wxFriendMsg.getTouid()));
			boolean add = operMsg.save();
			if (!add) {
				return null;
			}
			wxFriendMsg.setId(operMsg.getId());
		} else {
			boolean add = msgAdd(wxFriendMsg);
			if (!add) {
				return null;
			}
		}
		return wxFriendMsg;
	}

	/**
	 * 消息数据库保存并处理缓存
	 * 
	 * @param wxFriendMsg
	 * @return
	 * @author lixinji 2020年1月17日 下午2:24:10
	 */
	public boolean msgAdd(WxFriendMsg wxFriendMsg) {
		wxFriendMsg.setTwouid(UserService.twoUid(wxFriendMsg.getUid(), wxFriendMsg.getTouid()));
		boolean save = wxFriendMsg.save();
		ChatMsgService.me.friendMsgMetaStat(wxFriendMsg.getTwouid(), wxFriendMsg.getId());
		return save;
	}

	/**
	 * 添加到p2p cache
	 * 
	 * @param chatRecord
	 * @param chatlinkid
	 * @param tochatlinkid
	 * @author lixinji 2020年2月3日 下午2:05:15
	 */
	@SuppressWarnings("unchecked")
	public void putToP2pCache(WxFriendMsg chatRecord, Long chatlinkid, Long tochatlinkid) {
		if (Const.ContentType.RUN_JS == chatRecord.getContenttype()) {
			return;
		}
		int historyCount = ConfService.getInt("im.history.chat.count.p2p", WX_FRIEND_MSG_LIMIT);
		if (tochatlinkid != null) {
			String tokey = tochatlinkid + "";
			ListWithLock<WxFriendMsg> listWithLock = WX_FRIEND_MSG_CACHE.get(tokey, ListWithLock.class);
			if (listWithLock != null) {
				listWithLock.handle(new WriteLockHandler<List<WxFriendMsg>>() {
					@Override
					public void handler(List<WxFriendMsg> list) {
						list.add(0, chatRecord);
						while (list.size() > historyCount) {
							list.remove(historyCount);
						}
					}
				});
			}
		}
		if (chatlinkid != null && !Objects.equals(chatlinkid, tochatlinkid)) {
			String key = chatlinkid + "";
			ListWithLock<WxFriendMsg> listWithLock = WX_FRIEND_MSG_CACHE.get(key, ListWithLock.class);
			if (listWithLock != null) {
				listWithLock.handle(new WriteLockHandler<List<WxFriendMsg>>() {
					@Override
					public void handler(List<WxFriendMsg> list) {
						list.add(0, chatRecord);
						while (list.size() > historyCount) {
							list.remove(historyCount);
						}
					}
				});
			}
		}
	}
}
