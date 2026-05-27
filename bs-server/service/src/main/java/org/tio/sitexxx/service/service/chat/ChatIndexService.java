
package org.tio.sitexxx.service.service.chat;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxChatItemsMeta;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.model.main.WxGroup;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.lock.LockUtils;

/**
 * 聊天索引服务
 * @author lixinji
 * 2019年12月31日 下午5:57:32
 */
public class ChatIndexService {

	private static Logger log = LoggerFactory.getLogger(ChatIndexService.class);

	public static final ChatIndexService me = new ChatIndexService();

	final WxChatUserItem userItemDao = new WxChatUserItem().dao();

	final WxChatGroupItem groupItemDao = new WxChatGroupItem().dao();

	/*******************************************begin-调整-**********************************************************/

	/*********************************************end-调整-*********************************************************/

	/**
	 * 获取未激活的群会话列表-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月17日 上午10:32:00
	 */
	public List<WxChatGroupItem> getNoActGroupIndex(Long groupid) {
		if (groupid == null) {
			log.error("获取未激活会话群id为空");
			return null;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.notActGroupList", params);
		return WxChatGroupItem.dao.find(sqlPara);
	}

	/**
	 * 获取群的第一个用户（新群主）-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月27日 上午10:24:08
	 */
	public WxChatGroupItem getFristGroupUserIndex(Long groupid) {
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.getFristGroupUser", params);
		return WxChatGroupItem.dao.findFirst(sqlPara);
	}

	/**
	 * 获取已激活的群会话缓存-已调整
	 * @param id
	 * @param link
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午1:22:31
	 */
	public List<WxChatGroupItem> getLinkActGroupIndex(Long groupid) {
		if (groupid == null) {
			return null;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.actGroupList", params);
		return WxChatGroupItem.dao.find(sqlPara);
	}

	/**
	 * 群管理员列表
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2021年1月13日 下午5:13:17
	 */
	public List<WxChatGroupItem> getManagerGroupIndex(Long groupid) {
		if (groupid == null) {
			return null;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES).set("grouprole", Const.GroupRole.MANAGER);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.groupMangerList", params);
		return WxChatGroupItem.dao.find(sqlPara);
	}

	/**
	 * 不存在开始消息的已激活群组列表-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月17日 上午11:24:43
	 */
	public List<WxChatGroupItem> getNoStartMsgGroupIndex(Long groupid) {
		if (groupid == null) {
			return null;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.noStartMsgList", params);
		return WxChatGroupItem.dao.find(sqlPara);
	}

	/**
	 * 有效的群组索引-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月24日 上午11:45:08
	 */
	public List<WxChatGroupItem> getGroupLinkItems(Long groupid) {
		if (groupid == null) {
			return null;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.groupLinkList", params);
		return WxChatGroupItem.dao.find(sqlPara);
	}

	/**
	 * 获取激活会话的用户的用户索引-用户维度-已调整
	 * @param uid
	 * @param chatmode
	 * @param link
	 * @param act
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午4:43:24
	 */
	public List<WxChatUserItem> getLinkActUserIndex(Integer uid, Short chatmode) {
		if (uid == null) {
			return null;
		}
		Kv params = Kv.by("uid", uid).set("linkflag", Const.YesOrNo.YES).set("chatmode", chatmode);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.linkActUserList", params);
		return WxChatUserItem.dao.find(sqlPara);
	}

	/**
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年12月25日 下午12:26:55
	 */
	public List<WxChatUserItem> getLinkUserIndex(Integer uid) {
		if (uid == null) {
			return null;
		}
		Kv params = Kv.by("uid", uid).set("linkflag", Const.YesOrNo.YES);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.linkUserList", params);
		return WxChatUserItem.dao.find(sqlPara);
	}

	/**************************************lixinji-update-oper-begin**********************************************************/

	/**
	 * 更新群聊天索引-不处理缓存-上层业务层清除缓存-已调整
	 * 
	 * @param groupid
	 * @param uid
	 * @param clearmsg:true:清除起始消息
	 * @param chatlinkid
	 * @param tochatlinkmetaid
	 * @return
	 * @author lixinji
	 * 2020年12月24日 下午2:54:22
	 */
	public boolean chatGroupIndexUpdate(Long groupid, Integer uid, boolean clearmsg, Long chatlinkid, Long tochatlinkmetaid) {
		if (groupid == null || uid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid).set("uid", uid);
		if (chatlinkid != null) {
			params.set("chatlinkid", chatlinkid);
			params.set("tochatlinkmetaid", tochatlinkmetaid);
		} else {
			params.set("setnull", Const.YesOrNo.YES).set("resetflag", Const.YesOrNo.YES);
		}
		if (clearmsg) {
			params.set("clearmsg", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.updateChatGroupIndex", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 删除未激活的群索引-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月24日 下午12:48:38
	 */
	public boolean chatGroupIndexDelNoAct(Long groupid) {
		if (groupid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.delGroupNoAct", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 修改群所有的链接为无效-已调整
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月24日 下午12:54:22
	 */
	public boolean chatGroupIndexUpdateAllLink(Long groupid) {
		if (groupid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.NO);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.updateGroupLink", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 更新群聊索引-返回值-已调整
	 * @param groupid
	 * @param uid
	 * @param chatlinkid
	 * @param isNew true:返回最新索引；false：返回旧的索引
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午4:48:26
	 */
	public WxChatGroupItem chatGroupIndexUpdate(Long groupid, Integer uid, Long chatlinkid, Long chatlinkmetaid, boolean isNew) {
		WxChatGroupItem item = chatGroupIndex(uid, groupid);
		chatGroupIndexUpdate(groupid, uid, chatlinkid == null ? true : false, chatlinkid, chatlinkmetaid);
		if (isNew) {
			return chatGroupIndex(uid, groupid);
		}
		return item;
	}

	/**
	 * 单纯的修改群索引的数据-缓存上层处理-无法设置空值-已调整
	 * 传空值为不修改
	 * @param uid
	 * @param groupid
	 * @param linkflag
	 * @param role
	 * @return
	 * @author lixinji
	 * 2020年2月24日 下午2:54:59
	 */
	public boolean chatGroupIndexUpdate(Integer uid, Long groupid, Short linkflag, Short role, Long gpulinkid, Short resetflag) {
		WxChatGroupItem update = new WxChatGroupItem();
		update.setUid(uid);
		update.setGroupid(groupid);
		if (resetflag != null) {
			update.setResetflag(resetflag);
		}
		if (linkflag != null) {
			update.setLinkflag(linkflag);
		}
		if (gpulinkid != null) {
			update.setGpulinkid(gpulinkid);
		}
		if (role != null) {
			update.setGrouprole(role);
		}
		if (groupid == null) {
			return false;
		}
		boolean updateflag = update.update();
		if (!updateflag) {
			return false;
		}
		return true;
	}

	/**
	 * 激活所有的群索引-已调整
	 * 注意：次方法的startmsgid只会在不存在时进行赋值，请扩展者小心对待
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月17日 上午11:07:20
	 */
	public boolean actUserGroup(Long groupid, Integer uid, Long chatlinkid, Long chatlinkmetaid, Long startmsgid) {
		if (groupid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid).set("chatlinkid", chatlinkid).set("chatlinkmetaid", chatlinkmetaid).set("uid", uid);
		if (startmsgid != null) {
			params.set("startmsgid", startmsgid);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.actGroupIndex", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		removeChatGroupCache(groupid, uid);
		return true;
	}

	/**
	 * 激活群组的用户索引-已调整
	 * 注意：次方法的startmsgid只会在不存在时进行赋值，请扩展者小心对待
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月17日 上午11:09:45
	 */
	public boolean actGroupToUserIndex(Long groupid, Integer uid, Long chatlinkid, Long chatlinkmetaid, Long startmsgid) {
		if (groupid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid).set("chatlinkid", chatlinkid).set("chatlinkmetaid", chatlinkmetaid).set("uid", uid).set("chatmode", Const.ChatMode.GROUP);
		if (startmsgid != null) {
			params.set("startmsgid", startmsgid);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.actGroupToUserIndex", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		removeUserCache(uid, groupid, Const.ChatMode.GROUP);
		return true;
	}

	/**
	 * 更新群组的起始消息-群索引-已调整
	 * 更新没有起始消息的索引-多人
	 * @param groupid
	 * @param startmsgid
	 * @return
	 * @author lixinji
	 * 2020年2月17日 上午11:28:35
	 */
	public boolean chatGroupStartMsgUpdate(Long groupid, Long startmsgid) {
		if (groupid == null || startmsgid == null) {
			return false;
		}
		Kv params = Kv.by("groupid", groupid).set("linkflag", Const.YesOrNo.YES).set("startmsgid", startmsgid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.chatGroupStartMsgUpdate", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 激活，隐藏会话数据库操作-已调整
	 * @param uid
	 * @param bizid
	 * @param mode
	 * @param chatlinkid
	 * @param tochatlinkid
	 * @param startMsgId
	 * @return
	 * @author lixinji
	 * 2020年2月24日 下午12:34:07
	 */
	public boolean chatUserIndexUpdate(Integer uid, Long bizid, Short mode, Long chatlinkid, Long chatlinkmetaid, Long tochatlinkid, Long tochatlinkmetaid, Long startMsgId,
	        Short linkflag) {
		if (uid == null || bizid == null || mode == null) {
			return false;
		}
		Kv params = Kv.by("uid", uid).set("chatmode", mode).set("bizid", bizid);
		if (chatlinkid != null) {
			params.set("chatlinkid", chatlinkid);
			params.set("chatlinkmetaid", chatlinkmetaid);
		} else {
			params.set("setnull", Const.YesOrNo.YES);
		}
		if (linkflag != null) {
			params.set("linkflag", Const.YesOrNo.YES);
		}
		if (startMsgId != null) {
			params.set("startmsgid", startMsgId);
		}
		if (tochatlinkid != null) {
			params.set("tochatlinkid", tochatlinkid);
			params.set("tochatlinkmetaid", tochatlinkmetaid);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.actChatUserIndex", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 修改私聊的对方的聊天会话id-已调整
	 * @param uid
	 * @param bizid
	 * @param mode
	 * @param tochatlinkid
	 * @param startMsgId
	 * @return
	 * @author lixinji
	 * 2020年2月24日 下午12:34:19
	 */
	public boolean chatuserUpdateToChatlink(Integer uid, Long bizid, Short mode, Long tochatlinkid, Long tochatlinkmetaid, Long startMsgId) {
		if (uid == null || bizid == null || mode == null) {
			return false;
		}
		Kv params = Kv.by("uid", uid).set("chatmode", mode).set("bizid", bizid);
		if (startMsgId != null) {
			params.set("startmsgid", startMsgId);
		}
		if (tochatlinkid != null) {
			params.set("tochatlinkid", tochatlinkid);
			params.set("tochatlinkmetaid", tochatlinkmetaid);
		} else {
			params.set("setnull", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.updateToChatlinkId", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 修改索引的起始消息-已调整
	 * 单独处理起始消息的方法-用户索引
	 * @param uid
	 * @param bizid
	 * @param mode
	 * @param startMsgId
	 * @return
	 * @author lixinji
	 * 2020年2月3日 下午12:07:31
	 */
	public boolean chatuserStartMsg(Integer uid, Long bizid, Short mode, Long startMsgId) {
		if (uid == null || bizid == null || mode == null) {
			return false;
		}
		Kv params = Kv.by("uid", uid).set("chatmode", mode).set("bizid", bizid);
		if (startMsgId != null) {
			params.set("startmsgid", startMsgId);
		} else {
			params.set("setnull", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.chatuserStartMsg", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 修改群聊天索引的起始消息逻辑-已调整
	 * 单独处理起始消息的方法-群索引
	 * @param uid
	 * @param groupid
	 * @param startMsgId
	 * @return
	 * @author lixinji
	 * 2020年3月20日 上午10:21:33
	 */
	public boolean chatGroupStartMsg(Integer uid, Long groupid, Long startMsgId) {
		if (uid == null || groupid == null) {
			return false;
		}
		Kv params = Kv.by("uid", uid).set("groupid", groupid);
		if (startMsgId != null) {
			params.set("startmsgid", startMsgId);
		} else {
			params.set("setnull", Const.YesOrNo.YES).set("resetflag", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.chatgroupStartMsg", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 更新用户的聊天索引-不处理缓存-上层业务层清除缓存-已调整
	 * 20-2-3：新增起始消息处理逻辑
	 * @param bizid
	 * @param uid
	 * @param mode
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午4:40:53
	 */
	public boolean chatUserIndexUpdate(Integer uid, Long bizid, Short mode, Short oper) {
		Kv params = Kv.by("uid", uid).set("chatmode", mode).set("bizid", bizid);
		switch (oper) {
		case WxChatUserItem.UserIndexOper.DEL_FRIEND:
			params.set("setfidnull", Const.YesOrNo.YES).set("linkflag", Const.YesOrNo.NO);
			break;
		case WxChatUserItem.UserIndexOper.TO_DEL_FRIEND:
			params.set("tofidnull", Const.YesOrNo.YES).set("linkflag", Const.YesOrNo.NO);
			break;
		case WxChatUserItem.UserIndexOper.REMOVE_CHAT:
			params.set("setnull", Const.YesOrNo.YES);
			break;
		case WxChatUserItem.UserIndexOper.BLOCK_CHAT:
			params.set("sethide", Const.YesOrNo.YES);
			break;
		default:
			break;
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.updateChatUserIndex", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		if (update <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 删除群组的索引-不处理缓存-上层业务层清除缓存-已调整
	 * @TODO:lixinji-存在并发问题
	 * @param uid
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年2月24日 上午9:54:20
	 */
	public boolean chatGroupIndexDel(Integer uid, Long groupid) {
		WxChatGroupItem delItem = new WxChatGroupItem();
		delItem.setUid(uid);
		delItem.setGroupid(groupid);
		boolean del = delItem.delete();
		if (del) {
			return true;
		}
		return false;
	}

	/**
	 * 删除用户的索引-不处理缓存-上层业务层清除缓存-已调整
	 * @param uid
	 * @param mode
	 * @param bizid
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午11:23:59
	 */
	public boolean chatUserIndexDel(Integer uid, Long bizid, Short mode) {
		WxChatUserItem delItem = new WxChatUserItem();
		delItem.setUid(uid);
		delItem.setChatmode(mode);
		delItem.setBizid(bizid);
		boolean del = delItem.delete();
		if (del) {
			return true;
		}
		return false;
	}

	/**
	 * 修改状态-不处理缓存-上层业务层清除缓存-不能修改成为空值-已调整
	 * 传空值为不修改
	 * @param bizid
	 * @param uid
	 * @param mode
	 * @param viewFlag
	 * @param actflag
	 * @param linkflag
	 * @author lixinji
	 * 2020年1月9日 下午3:42:31
	 */
	public boolean chatUserIndexUpdate(Integer uid, Long bizid, Short mode, Short linkflag, Long linkid, Long chatlinkid, Long chatlinkmetaid, Long tochatlinkid,
	        Long tochatlinkmetaid) {
		WxChatUserItem update = new WxChatUserItem();
		update.setUid(uid);
		update.setChatmode(mode);
		update.setBizid(bizid);
		if (tochatlinkid != null) {
			update.setTochatlinkid(tochatlinkid);
			update.setTochatlinkmetaid(tochatlinkmetaid);
		}
		if (chatlinkid != null) {
			update.setChatlinkid(chatlinkid);
			update.setChatlinkmetaid(chatlinkmetaid);
		}
		if (linkflag != null) {
			update.setLinkflag(linkflag);
		}
		if (linkid != null) {
			update.setLinkid(linkid);
		}
		boolean updateflag = update.update();
		if (!updateflag) {
			return false;
		}
		return true;
	}

	/**
	 * 更新用户聊天索引-返回值-已调整
	 * @param bizid
	 * @param uid
	 * @param mode
	 * @param isNew
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午4:50:56
	 */
	public WxChatUserItem chatUserIndexUpdate(Integer uid, Long bizid, Short mode, Short oper, boolean isNew) {
		WxChatUserItem item = chatUserIndex(uid, bizid, mode);
		chatUserIndexUpdate(uid, bizid, mode, oper);
		if (isNew) {
			return chatUserIndex(uid, bizid, mode);
		}
		return item;
	}

	/**
	 * 焦点处理-待优化-已调整
	 * 调整到会话动态信息中
	 * @param chatlinkid
	 * @param focusFlag
	 * @author lixinjiR
	 * 2020年2月17日 下午12:24:49
	 */
	@Deprecated
	public void updateFocus(Long groupid, Integer uid, Short focusFlag) {
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			Kv params = Kv.by("uid", uid).set("groupid", groupid).set("focusflag", focusFlag);
			SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.focus", params);
			Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * 更新会话的焦点
	 * @param chatlinkmetaid
	 * @param focusFlag
	 * @author lixinji
	 * 2021年1月29日 下午3:05:42
	 */
	public void updateFocus(Long chatlinkmetaid, Short focusFlag) {
		WxChatItemsMeta meta = new WxChatItemsMeta();
		meta.setId(chatlinkmetaid);
		meta.setFocusflag(focusFlag);
		meta.update();
	}

	/**************************************lixinji-update-oper--end-**********************************************************/

	/**************************************lixinji-init-oper-begin**********************************************************/
	/**
	 * 初始化群组索引记录-已调整
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param gpulinkid
	 * @param view
	 * @param act
	 * @param link
	 * @param grouprole
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午6:04:33
	 */
	public int chatGroupInit(Integer uid, Long groupid, Long chatlinkid, Long chatlinkmetaid, Long gpulinkid, Short link, Short grouprole, Long startmsgid, Short resetflag) {
		WxChatGroupItem item = new WxChatGroupItem();
		item.setUid(uid);
		item.setGroupid(groupid);
		item.setGpulinkid(gpulinkid);
		;
		item.setChatlinkid(chatlinkid);
		item.setChatlinkmetaid(chatlinkmetaid);
		item.setLinkflag(link);
		item.setGrouprole(grouprole);
		if (startmsgid != null) {
			item.setStartmsgid(startmsgid);
		}
		if (resetflag != null) {
			item.setResetflag(resetflag);
		}
		int init = item.ignoreSave();
		if (init > 0) {
			setGroupIndexCache(groupid + "_" + uid, item);
		}
		return init;
	}

	/**
	 * 初始化群组索引记录-已调整
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param gpulinkid
	 * @param grouprole
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午6:05:50
	 */
	public int chatGroupInit(Integer uid, Long groupid, Long chatlinkid, Long chatlinkmetaid, Long gpulinkid, Short grouprole, Long startmsgid, Short retsetFlag) {
		return chatGroupInit(uid, groupid, chatlinkid, chatlinkmetaid, gpulinkid, Const.YesOrNo.YES, grouprole, startmsgid, retsetFlag);
	}

	/**
	 * 初始化群组索引记录-已调整
	 * @param uid
	 * @param groupid
	 * @param chatlinkid
	 * @param gpulinkid
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午6:06:12
	 */
	public int chatGroupInit(Integer uid, Long groupid, Long chatlinkid, Long chatlinkmetaid, Long gpulinkid, Long startmsgid, Short retsetFlag) {
		return chatGroupInit(uid, groupid, chatlinkid, chatlinkmetaid, gpulinkid, Const.YesOrNo.YES, Const.GroupRole.MEMBER, startmsgid, retsetFlag);
	}

	/**
	 * 初始化群组索引记录-已调整
	 * @param uid
	 * @param groupid
	 * @param gpulinkid
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午12:55:52
	 */
	public int chatGroupInit(Integer uid, Long groupid, Long gpulinkid) {
		return chatGroupInit(uid, groupid, null, null, gpulinkid, Const.YesOrNo.YES, Const.GroupRole.MEMBER, null, null);
	}

	/**
	 * 初始化用户索引记录-已调整
	 * @param uid 用户id
	 * @param chatmode 聊天模型
	 * @param bizid 业务id
	 * @param chatlinkid 聊天会话
	 * @param tochatlinkid 对方的聊天会话
	 * @param linkid 好友id/群用户id
	 * @param viewFlag 视图状态
	 * @param actfByte 激活状态
	 * @param linkfByte 有效连接状态
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午3:34:20
	 */
	public int chatUserInit(Integer uid, Short chatmode, Long bizid, Long chatlinkid, Long chatlinkmetaid, Long tochatlinkid, Long tochatlinkmetaid, Long linkid, Short linkflag,
	        Long startmsgid) {
		WxChatUserItem item = new WxChatUserItem();
		item.setUid(uid);
		item.setChatmode(chatmode);
		item.setBizid(bizid);
		item.setLinkid(linkid);
		item.setChatlinkid(chatlinkid);
		item.setChatlinkmetaid(chatlinkmetaid);
		item.setTochatlinkid(tochatlinkid);
		item.setTochatlinkmetaid(tochatlinkmetaid);
		item.setLinkflag(linkflag);
		if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
			item.setFidkey(UserService.twoUid(uid, bizid.intValue()));
		}
		if (startmsgid != null) {
			item.setStartmsgid(startmsgid);
		}
		int init = item.ignoreSave();
		if (init > 0) {
			setUserIndexCache(uid + "_" + chatmode + "_" + bizid, item);
		}
		return init;
	}

	/**
	 * 初始化用户索引记录-好友-已调整
	 * @param fid
	 * @param uid
	 * @param chatmode
	 * @param bizid
	 * @param chatlinkid
	 * @param tochatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午3:34:08
	 */
	public int chatUserInit(Long fid, Integer uid, Short chatmode, Long bizid, Long chatlinkid, Long chatlinkmetaid, Long tochatlinkid, Long tochatlinkmetaid) {
		return chatUserInit(uid, chatmode, bizid, chatlinkid, chatlinkmetaid, tochatlinkid, tochatlinkmetaid, fid, Const.YesOrNo.YES, null);
	}

	/**
	 * 初始化用户索引记录-好友-已调整
	 * @param fid
	 * @param uid
	 * @param chatmode
	 * @param bizid
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午3:33:57
	 */
	public int chatUserInit(Long fid, Integer uid, Short chatmode, Long bizid) {
		return chatUserInit(uid, chatmode, bizid, null, null, null, null, fid, Const.YesOrNo.YES, null);
	}

	/**
	 * 初始化用户索引记录-群组-已调整
	 * @param uid
	 * @param chatmode
	 * @param bizid
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午3:33:42
	 */
	public int chatUserInit(Integer uid, Short chatmode, Long bizid, Long chatlinkid, Long chatlinkmetaid, Long linkid, Long startmsgid) {
		return chatUserInit(uid, chatmode, bizid, chatlinkid, chatlinkmetaid, null, null, linkid, Const.YesOrNo.YES, startmsgid);
	}

	/**
	 * 初始化用户索引记录-已调整
	 * @param uid
	 * @param chatmode
	 * @param bizid
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午3:33:43
	 */
	public int chatUserInit(Integer uid, Short chatmode, Long bizid) {
		return chatUserInit(uid, chatmode, bizid, null, null, null, null, null, Const.YesOrNo.YES, null);
	}

	/**************************************lixinji-init-oper--end-**********************************************************/

	/**************************************lixinji-cache-oper-begin**********************************************************/

	/**
	 * 获取用户聊天索引-已调整
	 * @param bizid
	 * @param uid
	 * @param mode
	 * @return
	 * @author lixinji
	 * 2020年1月6日 上午11:06:50
	 */
	public static WxChatUserItem chatUserIndex(Integer uid, Long bizid, Short mode) {
		if (bizid == null || uid == null || mode == null) {
			return null;
		}
		ICache cache = Caches.getCache(CacheConfig.CHAT_USER_INDEX_2);
		String key = uid + "_" + mode + "_" + bizid;
		WxChatUserItem userIndex = CacheUtils.get(cache, key, false, new FirsthandCreater<WxChatUserItem>() {
			@Override
			public WxChatUserItem create() {
				Kv params = Kv.by("uid", uid).set("chatmode", mode).set("bizid", bizid);
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.getuserindex", params);
				WxChatUserItem userItem = WxChatUserItem.dao.findFirst(sqlPara);
				return userItem;
			}
		});
		return userIndex;
	}

	/**
	 * 获取用户聊天索引-已调整
	 * @param uid
	 * @param bizid
	 * @param mode
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午4:05:01
	 */
	public static WxChatUserItem chatUserIndex(Integer uid, Integer bizid, Short mode) {
		return chatUserIndex(uid, new Long(bizid), mode);
	}

	/**
	 * 根据会话获取用户索引-已调整
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月3日 下午1:37:22
	 */
	public static WxChatUserItem chatUserIndex(Long chatlinkid) {
		if (chatlinkid == null) {
			return null;
		}
		WxChatItems items = ChatService.me.getBaseChatItems(chatlinkid);
		if (items == null) {
			return null;
		}
		return chatUserIndex(items.getUid(), items.getBizid(), items.getChatmode());
	}

	/**
	 * 获取用户索引-已调整
	 * @param items
	 * @return
	 * @author lixinji
	 * 2020年11月3日 上午10:36:30
	 */
	public static WxChatUserItem chatUserIndex(WxChatItems items) {
		if (items == null) {
			return null;
		}
		return chatUserIndex(items.getUid(), items.getBizid(), items.getChatmode());
	}

	/**
	 * 获取群会话索引-建议上层不要进行chatitem的查询-已调整
	 * @param chatlinkid
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午8:05:18
	 */
	public static WxChatGroupItem chatGroupIndex(Long chatlinkid) {
		if (chatlinkid == null) {
			return null;
		}
		WxChatItems items = ChatService.me.getBaseChatItems(chatlinkid);
		if (items == null) {
			return null;
		}
		return chatGroupIndex(items.getUid(), items.getBizid());
	}

	/**
	 * 获取群索引-已调整
	 * @param items
	 * @return
	 * @author lixinji
	 * 2020年11月3日 上午10:30:46
	 */
	public static WxChatGroupItem chatGroupIndex(WxChatItems items) {
		if (items == null) {
			return null;
		}
		return chatGroupIndex(items.getUid(), items.getBizid());
	}

	/**
	 * 私聊用户索引-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午10:34:21
	 */
	public static WxChatUserItem fdUserIndex(Integer uid, Integer touid) {
		return chatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
	}

	/**
	 * 群聊用户索引-已调整
	 * @param uid
	 * @param groupid
	 * @return
	 * @author lixinji
	 * 2020年1月19日 上午10:34:20
	 */
	public static WxChatUserItem groupUserIndex(Integer uid, Long groupid) {
		return chatUserIndex(uid, groupid, Const.ChatMode.GROUP);
	}

	/**
	 * 获取群组聊天索引-已调整
	 * @param groupid
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年1月6日 上午11:04:51
	 */
	public static WxChatGroupItem chatGroupIndex(Integer uid, Long groupid) {
		if (groupid == null || uid == null) {
			return null;
		}
		ICache cache = Caches.getCache(CacheConfig.CHAT_GROUP_INDEX_4);
		String key = groupid + "_" + uid;
		WxChatGroupItem groupindex = CacheUtils.get(cache, key, false, new FirsthandCreater<WxChatGroupItem>() {
			@Override
			public WxChatGroupItem create() {
				Kv params = Kv.by("groupid", groupid).set("uid", uid);
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatindex.getgroupindex", params);
				WxChatGroupItem groupItem = WxChatGroupItem.dao.findFirst(sqlPara);
				return groupItem;
			}
		});
		return groupindex;
	}

	/**
	 * 移除黑名单缓存-已调整
	 * @param uid
	 * @param touid
	 * @author lixinji
	 * 2020年1月8日 上午10:19:13
	 */
	public static void removeBlockCache(Integer uid, Integer touid) {
		if (uid == null || touid == null) {
			return;
		}
		Caches.getCache(CacheConfig.CHAT_USER_BLOCK_1).remove(uid + "_" + touid);
	}

	/**
	 * 清除用户聊天索引-全局-已调整
	 * @param bizid 业务touid-groupid
	 * @param uid
	 * @param mode
	 * @author lixinji
	 * 2020年1月6日 下午3:43:19
	 */
	public static void clearChatUserIndex(Integer uid, Long bizid, Short mode) {
		if (bizid == null || uid == null || mode == null) {
			return;
		}
		removeUserCache(uid, bizid, mode);
		if (Objects.equals(mode, Const.ChatMode.GROUP)) {
			removeChatGroupCache(bizid, uid);
		}
	}

	/**
	 * 通讯录缓存清理-已调整
	 * @param uid
	 * @author lixinji
	 * 2020年3月10日 下午4:39:29
	 */
	public static void clearMailListCache(Integer uid) {
		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_99");
		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_" + Const.ChatMode.P2P);
		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_" + Const.ChatMode.GROUP);
	}

	/**
	 * 清除好友会话索引缓存-已调整
	 * @param fid
	 * @param uid
	 * @author lixinji
	 * 2020年12月23日 下午2:00:21
	 */
	public static void clearFriendInfoCache(Long fid, Integer uid) {
		Caches.getCache(CacheConfig.WX_MY_FRIEND).remove(fid + "");
	}

	/**
	 * 清除私聊索引-已调整
	 * @param uid
	 * @param toUid
	 * @author lixinji
	 * 2020年1月6日 下午3:46:25
	 */
	public static void clearChatP2pIndex(Integer uid, Integer toUid) {
		removeUserCache(uid, new Long(toUid), Const.ChatMode.P2P);
	}

	/**
	 * 清除群聊索引-已调整
	 * @param groupid
	 * @param uid
	 * @author lixinji
	 * 2020年1月6日 下午3:45:23
	 */
	public static void clearChatGroupIndex(Long groupid, Integer uid) {
		removeChatGroupCache(groupid, uid);
		removeUserCache(uid, groupid, Const.ChatMode.GROUP);

	}

	/**
	 * 移除用户缓存-已调整
	 * @param uid
	 * @param mode
	 * @param bizid
	 * @author lixinji
	 * 2020年1月19日 下午1:54:24
	 */
	public static void removeUserCache(Integer uid, Long bizid, Short mode) {
		String key = uid + "_" + mode + "_" + bizid;
		Caches.getCache(CacheConfig.CHAT_USER_INDEX_2).remove(key);
	}

	/**
	 * 移除群缓存-已调整
	 * @param groupid
	 * @author lixinji
	 * 2020年2月24日 上午10:17:02
	 */
	public static void removeGroupCache(Long groupid) {
		String key = groupid + "";
		Caches.getCache(CacheConfig.WX_GROUP_4).remove(key);
	}

	/**
	 * 清除群用户列表-已调整
	 * @param groupid
	 * @author lixinji
	 * 2020年2月26日 下午1:40:00
	 */
	public static void clearGroupUserListCache(Long groupid) {
		WxGroup group = GroupService.me.getByGroupid(groupid);
		if (group == null) {
			return;
		}
		double loop = Math.ceil((double) group.getJoinnum() / 100);
		for (int i = 0; i < loop; i++) {
			if (i > 3) {
				break;
			}
			String key = groupid + "_" + (i + 1);
			Caches.getCache(CacheConfig.CHAT_GROUP_USER_LIST_3).remove(key);
		}
	}

	/**
	 * 清除群用户缓存-已调整
	 * @param linkid
	 * @author lixinji
	 * 2020年9月29日 上午11:00:46
	 */
	public static void clearGroupUserCache(Long linkid) {
		String key = linkid + "";
		Caches.getCache(CacheConfig.WX_GROUP_USER_2).remove(key);
	}

	/**
	 * 清除群消息缓存-已调整
	 * @param groupid
	 * @author lixinji
	 * 2020年6月18日 下午2:53:12
	 */
	public static void clearGroupMsgCache(Long groupid) {
		String key = groupid + "";
		Caches.getCache(CacheConfig.WX_GROUP_CHAT_6).remove(key);
	}

	/**
	 * 移除群聊缓存-已调整
	 * @param groupid
	 * @param uid
	 * @author lixinji
	 * 2020年1月8日 上午10:25:23
	 */
	public static void removeChatGroupCache(Long groupid, Integer uid) {
		String groupKey = groupid + "_" + uid;
		Caches.getCache(CacheConfig.CHAT_GROUP_INDEX_4).remove(groupKey);
	}

	/**
	 * 设置用户聊天索引-已调整
	 * @param key
	 * @param userItem
	 * @author lixinji
	 * 2020年1月8日 上午10:21:52
	 */
	public static void setUserIndexCache(String key, WxChatUserItem userItem) {
		Caches.getCache(CacheConfig.CHAT_USER_INDEX_2).put(key, userItem);
	}

	/**
	 * 设置群聊聊天索引-已调整
	 * @param key
	 * @param groupItem
	 * @author lixinji
	 * 2020年1月8日 上午10:28:08
	 */
	public static void setGroupIndexCache(String key, WxChatGroupItem groupItem) {
		Caches.getCache(CacheConfig.CHAT_GROUP_INDEX_4).put(key, groupItem);

	}

	/**
	 * 移除聊天会话缓存-已调整
	 * @param chatlinkid
	 * @author lixinji
	 * 2020年1月19日 下午1:53:12
	 */
	public static void removeChatItemsCache(Long chatlinkid) {
		String key = chatlinkid + "";
		Caches.getCache(CacheConfig.CHAT_ITEMS_6).remove(key);
	}
	/**************************************lixinji-cache-oper--end-**********************************************************/

}
