
package org.tio.sitexxx.im.server.handler.wx;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.sys.WxSynRecordNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxFriend;
import org.tio.sitexxx.service.model.main.WxFriendApplyItems;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.utils.json.Json;

/**
 * 同步Api
 * @author lixinji
 * 2020年9月18日 下午1:38:51
 */
public class WxSynApi {

	private static Logger log = LoggerFactory.getLogger(WxSynApi.class);

	public static final WxSynApi me = new WxSynApi();

	/**
	 * 进入焦点记录同步（已读处理）
	 * 1、会话内部的uid消息都已读
	 * 2、会话信息修改（焦点逻辑已处理），后续可以合并
	 * @param bizdata
	 * @param uid
	 * @param syntype
	 * @author lixinji
	 * 2020年9月18日 下午1:55:19
	 */
	public static void synMsgFocusJoin(Long chatlinkid, Integer uid, FocusVo focusVo) {
		Map<String, Object> bizdata = new HashMap<String, Object>();
		bizdata.put("chatlinkid", chatlinkid);
		bizdata.put("uid", uid);
		bizdata.put("readflag", Const.YesOrNo.YES);
		bizdata.put("readtime", new Date());
		bizdata.put("readdevice", focusVo.getDevicetype());
		bizdata.put("readipid", focusVo.getIpid());
		synNtf(uid, SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.ALL_UPDATE, bizdata);
	}

	/**
	 * 会话同步
	 * @param uid
	 * @param chatItems
	 * @param synType
	 * @author lixinji
	 * 2020年9月18日 下午3:31:27
	 */
	public static void synChatSession(Integer uid, WxChatItems chatItems, Short synType) {
		if (chatItems.getChatlinkid() == null) {
			chatItems.setChatlinkid(chatItems.getId());
		}
		synNtf(chatItems, uid, SynRecordVo.BizType.CHAT_SESSION, synType);
	}

	/**
	 * 清除消息
	 * @param uid
	 * @param chatlinkid
	 * @author lixinji
	 * 2020年9月18日 下午5:54:23
	 */
	public static void synMsgClear(Integer uid, Long chatlinkid) {
		Map<String, Object> bizdata = new HashMap<String, Object>();
		bizdata.put("chatlinkid", chatlinkid);
		synNtf(uid, SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.DEL_ALL, bizdata);
	}

	/**
	 * 好友消息同步处理
	 * @param uid
	 * @param chatItems
	 * @param synType
	 * @author lixinji
	 * 2020年9月18日 下午3:56:31
	 */
	public static void synP2pMsgBak(Integer uid, Long chatlinkid, Long mid) {
		Map<String, Object> bizdata = new HashMap<String, Object>();
		bizdata.put("chatlinkid", chatlinkid);
		bizdata.put("id", mid);
		synNtf(uid, SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.DEL, bizdata);
	}

	/**
	 * 群消息同步处理
	 * @param groupid
	 * @param chatlinkid
	 * @param mid
	 * @author lixinji
	 * 2020年9月18日 下午4:12:49
	 */
	public static void synGroupMsgBak(Long groupid, Long mid) {
		Map<String, Object> bizdata = new HashMap<String, Object>();
		bizdata.put("chatlinkid", -groupid);
		bizdata.put("id", mid);
		synNtf(groupid, SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.DEL, bizdata);
	}

	/**
	 * 好友消息删除
	 * @param uid
	 * @param chatlinkid
	 * @param mids
	 * @author lixinji
	 * 2020年9月18日 下午4:50:54
	 */
	public static void synP2pMsgDel(Integer uid, Long chatlinkid, String mids, WxChatItems chatItems) {
		List<SynRecordVo> synRecordVos = new ArrayList<SynRecordVo>();
		for (String mid : mids.split(",")) {
			Map<String, Object> bizdata = new HashMap<String, Object>();
			bizdata.put("chatlinkid", chatlinkid);
			bizdata.put("id", Long.parseLong(mid));
			synRecordVos.add(new SynRecordVo(SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.DEL, bizdata));
		}
		if (chatItems != null) {
			if (chatItems.getChatlinkid() == null) {
				chatItems.setChatlinkid(chatItems.getId());
			}
			synInit(SynRecordVo.BizType.CHAT_SESSION, SynRecordVo.SynType.UPDATE, chatItems, synRecordVos);
		}
		synManyNtf(uid, synRecordVos);
	}

	/**
	 * 群消息删除处理
	 * @param groupid
	 * @param chatlinkid
	 * @param mid
	 * @author lixinji
	 * 2020年9月18日 下午4:12:49
	 */
	public static void synGroupMsgDel(Integer uid, Long groupid, String mids, WxChatItems chatItems) {
		List<SynRecordVo> synRecordVos = new ArrayList<SynRecordVo>();
		for (String mid : mids.split(",")) {
			Map<String, Object> bizdata = new HashMap<String, Object>();
			bizdata.put("chatlinkid", -groupid);
			bizdata.put("id", Long.parseLong(mid));
			synRecordVos.add(new SynRecordVo(SynRecordVo.BizType.CHAT_SESSION_MSG, SynRecordVo.SynType.DEL, bizdata));
		}
		if (chatItems != null) {
			if (chatItems.getChatlinkid() == null) {
				chatItems.setChatlinkid(-groupid);
			}
			chatItems.setId(-groupid);
			synInit(SynRecordVo.BizType.CHAT_SESSION, SynRecordVo.SynType.UPDATE, chatItems, synRecordVos);
		}
		synManyNtf(uid, synRecordVos);
	}

	/**
	 * 好友删除同步
	 * @param uid
	 * @param friend
	 * @author lixinji
	 * 2020年9月18日 下午4:21:34
	 */
	public static void synFriendDel(Integer uid, Integer touid, Long chatlinkid, Long tochatlinkid, Long fid) {
		//自己的同步
		List<SynRecordVo> uidSynVos = new ArrayList<SynRecordVo>();
		if (chatlinkid != null) {
			Map<String, Object> uidMap = new HashMap<String, Object>();
			uidMap.put("chatlinkid", chatlinkid);
			synInit(SynRecordVo.BizType.CHAT_SESSION, SynRecordVo.SynType.DEL, uidSynVos, uidMap);
		}
		Map<String, Object> fidMap = new HashMap<String, Object>();
		fidMap.put("id", fid);
		synInit(SynRecordVo.BizType.FRIEND, SynRecordVo.SynType.DEL, uidSynVos, fidMap);
		synManyNtf(uid, uidSynVos);
		//对方好友的同步:只同步对方的链接信息
		if (tochatlinkid != null) {
			Map<String, Object> bizdata = new HashMap<String, Object>();
			bizdata.put("chatlinkid", tochatlinkid);
			bizdata.put("linkflag", Const.YesOrNo.NO);
			synNtf(touid, SynRecordVo.BizType.CHAT_SESSION, SynRecordVo.SynType.UPDATE, bizdata);
		}
	}

	/**
	 * 同步好友关系信息
	 * @param uid
	 * @param friend
	 * @param synType
	 * @author lixinji
	 * 2020年9月18日 下午6:21:08
	 */
	public static void synFriend(Integer uid, WxFriend friend, Short synType) {
		synNtf(friend, uid, SynRecordVo.BizType.FRIEND, synType);
	}

	/**
	 * 好友申请
	 * @param uid
	 * @param apply
	 * @param synType
	 * @author lixinji
	 * 2020年9月18日 下午6:49:30
	 */
	public static void synFriendApply(Integer uid, WxFriendApplyItems apply, Short synType) {
		synNtf(apply, uid, SynRecordVo.BizType.FRIEND_APPLY, synType);
	}

	/**
	 * 同步修改好友备注名
	 * @param uid
	 * @param fid
	 * @param remarkName
	 * @param chatlinkid
	 * @author lixinji
	 * 2020年9月18日 下午6:26:59
	 */
	public static void synFdRemarkName(Integer uid, Long fid, String remarkName, Long chatlinkid) {
		List<SynRecordVo> uidSynVos = new ArrayList<SynRecordVo>();
		if (chatlinkid != null) {
			Map<String, Object> uidMap = new HashMap<String, Object>();
			uidMap.put("chatlinkid", chatlinkid);
			uidMap.put("name", remarkName);
			synInit(SynRecordVo.BizType.CHAT_SESSION, SynRecordVo.SynType.UPDATE, uidSynVos, uidMap);
		}
		Map<String, Object> fidMap = new HashMap<String, Object>();
		fidMap.put("id", fid);
		fidMap.put("remarkname", remarkName);
		synInit(SynRecordVo.BizType.FRIEND, SynRecordVo.SynType.UPDATE, uidSynVos, fidMap);
		synManyNtf(uid, uidSynVos);
	}

	/**
	 * 同步记录-单业务-对象
	 * @param bizdata
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @author lixinji
	 * 2020年9月15日 下午6:29:09
	 */
	public static void synNtf(Object object, Integer uid, Short biztype, Short syntype) {
		if (uid == null || biztype == null || syntype == null || object == null) {
			log.error("记录同步触发异常：参数存在空值,uid:{},biztype:{},syntype:{},bizdata:{}", uid, biztype, syntype, object);
			return;
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(new SynRecordVo(object, biztype, syntype));
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToUser(uid, imPacket);
	}

	/**
	 * 群发同步-单业务-对象
	 * @param bizdata
	 * @param groupid
	 * @param biztype
	 * @param syntype
	 * @author lixinji
	 * 2020年9月18日 下午4:11:57
	 */
	public static void synNtf(Object object, Long groupid, Short biztype, Short syntype) {
		if (groupid == null || biztype == null || syntype == null || object == null) {
			log.error("记录同步触发异常（群）：参数存在空值,uid:{},biztype:{},syntype:{},bizdata:{}", groupid, biztype, syntype, object);
			return;
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(new SynRecordVo(object, biztype, syntype));
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToGroup(groupid, imPacket);
	}

	/**
	 * 同步记录-单业务-Map
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @param bizdata
	 * @author lixinji
	 * 2020年9月15日 下午6:29:15
	 */
	public static void synNtf(Integer uid, Short biztype, Short syntype, Map<String, Object> bizdata) {
		if (uid == null || biztype == null || syntype == null || bizdata == null) {
			log.error("记录同步触发异常：参数存在空值,uid:{},biztype:{},syntype:{},bizdata:{}", uid, biztype, syntype, bizdata);
			return;
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(new SynRecordVo(biztype, syntype, bizdata));
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToUser(uid, imPacket);
	}

	/**
	 * 群发同步-单业务-Map
	 * @param groupid
	 * @param biztype
	 * @param syntype
	 * @param bizdata
	 * @author lixinji
	 * 2020年9月18日 下午5:56:46
	 */
	public static void synNtf(Long groupid, Short biztype, Short syntype, Map<String, Object> bizdata) {
		if (groupid == null || biztype == null || syntype == null || bizdata == null) {
			log.error("记录同步触发异常：参数存在空值,groupid:{},biztype:{},syntype:{},bizdata:{}", groupid, biztype, syntype, bizdata);
			return;
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(new SynRecordVo(biztype, syntype, bizdata));
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToGroup(groupid, imPacket);
	}

	/**
	 * 相同用户业务同步-多条-Map
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @param bizdatas
	 * @author lixinji
	 * 2020年9月15日 下午6:32:17
	 */
	public static void synEqualNtf(Integer uid, Short biztype, Short syntype, List<Map<String, Object>> bizdatas) {
		if (uid == null || biztype == null || syntype == null || bizdatas == null) {
			log.error("记录同步触发异常：参数存在空值,uid:{},biztype:{},syntype:{},bizdatas:{}", uid, biztype, syntype, bizdatas);
			return;
		}
		List<SynRecordVo> synRecordVos = new ArrayList<SynRecordVo>();
		for (Map<String, Object> object : bizdatas) {
			synRecordVos.add(new SynRecordVo(biztype, syntype, object));
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(synRecordVos);
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToUser(uid, imPacket);
	}

	/**
	 * 相同群业务同步-多条-Map
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @param bizdatas
	 * @author lixinji
	 * 2020年9月15日 下午6:32:17
	 */
	public static void synEqualNtf(Long groupid, Short biztype, Short syntype, List<Map<String, Object>> bizdatas) {
		if (groupid == null || biztype == null || syntype == null || bizdatas == null) {
			log.error("记录同步触发异常：参数存在空值,groupid:{},biztype:{},syntype:{},bizdatas:{}", groupid, biztype, syntype, bizdatas);
			return;
		}
		List<SynRecordVo> synRecordVos = new ArrayList<SynRecordVo>();
		for (Map<String, Object> object : bizdatas) {
			synRecordVos.add(new SynRecordVo(biztype, syntype, object));
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(synRecordVos);
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToGroup(groupid, imPacket);
	}

	/**
	 * 多业务同步
	 * @param uid
	 * @param synRecordVos
	 * @author lixinji
	 * 2020年9月15日 下午6:35:04
	 */
	public static void synManyNtf(Integer uid, List<SynRecordVo> synRecordVos) {
		if (uid == null || synRecordVos == null) {
			log.error("记录同步触发异常：参数存在空值,uid:{},synRecordVos:{}", uid, synRecordVos);
			return;
		}
		WxSynRecordNtf ntf = new WxSynRecordNtf(synRecordVos);
		ImPacket imPacket = new ImPacket(Command.WxSynRecordNtf, Json.toJson(ntf));
		Ims.sendToUser(uid, imPacket);
	}

	/**
	 * 初始化-对象-业务合并
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @param object
	 * @param synRecordVos
	 * @author lixinji
	 * 2020年9月15日 下午6:33:28
	 */
	public static void synInit(Short biztype, Short syntype, Object object, List<SynRecordVo> synRecordVos) {
		if (synRecordVos == null) {
			return;
		}
		synRecordVos.add(new SynRecordVo(object, biztype, syntype));
	}

	/**
	 * 初始化-Map-业务合并
	 * @param uid
	 * @param biztype
	 * @param syntype
	 * @param synRecordVos
	 * @param bizdata
	 * @author lixinji
	 * 2020年9月18日 下午4:36:15
	 */
	public static void synInit(Short biztype, Short syntype, List<SynRecordVo> synRecordVos, Map<String, Object> bizdata) {
		if (synRecordVos == null) {
			return;
		}
		synRecordVos.add(new SynRecordVo(biztype, syntype, bizdata));
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年9月18日 下午2:06:42
	 */
	public static boolean isSynVersion() {
		return isSynVersion(null);
	}

	/**
	 * 新版标识
	 * @return
	 * @author lixinji
	 * 2020年9月18日 下午1:37:11
	 */
	public static boolean isSynVersion(Short deviceType) {
		return Const.SYN_NEW_VERSION;
	}
}
