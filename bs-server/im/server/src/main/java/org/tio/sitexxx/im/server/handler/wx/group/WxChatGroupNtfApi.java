
package org.tio.sitexxx.im.server.handler.wx.group;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupChatNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 群消息通知api
 * @author lixinji
 * 2020年12月22日 下午6:06:01
 */
public class WxChatGroupNtfApi {

	private static Logger log = LoggerFactory.getLogger(WxChatGroupNtfApi.class);

	public static final WxChatGroupNtfApi me = new WxChatGroupNtfApi();

	/**
	 * @param msg
	 * @param sysMsgVo
	 * @param ats
	 * @param group
	 * @param hideItem
	 * @param chatItemList
	 * @param noActGroupChatsFinal
	 * @param noStartMsgGroupChatsFinal
	 * @param user
	 * @author lixinji
	 * 2020年12月22日 下午6:12:35
	 */
	public void sendGroupMsg(WxGroupMsg msg, SysMsgVo sysMsgVo, String ats, WxGroup group, List<WxChatItems> chatItemList, List<WxChatGroupItem> noActGroupChatsFinal,
	        List<WxChatGroupItem> noStartMsgGroupChatsFinal, User user) {
		if (group.getJoinnum().equals(Short.valueOf("2"))) {
			WxGroupUser wxGroupUser = WxGroupUser.dao.findFirst("select * from wx_group_user where groupid = ? and grouprole = 2", group.getId());
			if (wxGroupUser != null) {
				ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
				String key = "tempIM_" + wxGroupUser.getUid();
				Serializable serializable = cache.get(key);
				if (serializable != null) {
					HashMap<String, Object> result = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
						@Override
						public HashMap<String, Object> create() {
							return null;
						}
					});
					cache.remove(key);
					Map<String, Object> result1 = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
						@Override
						public HashMap<String, Object> create() {
							return result;
						}
					});
				}
			}

		}
		int notActSize = chatItemList != null ? chatItemList.size() : 0;
		long groupid = group.getId();
		WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(msg, sysMsgVo);
		if (StrUtil.isNotBlank(ats)) {
			if (ats.equals("all")) {
				wxGroupChatNtf.setAt(ats);
			} else {
				wxGroupChatNtf.setAt("," + ats + ",");
			}
		}
		ImPacket imPacket = new ImPacket(Command.WxGroupChatNtf, wxGroupChatNtf);
		Ims.sendToGroup(groupid, imPacket);
		if (notActSize < 1000) {
			if (CollectionUtil.isNotEmpty(chatItemList)) {
				WxGroupChatNtf actNtf = WxGroupChatNtf.from(msg, sysMsgVo);
				actNtf.setActflag(Const.YesOrNo.YES);
				if (StrUtil.isNotBlank(ats)) {
					if (ats.equals("all")) {
						actNtf.setAt(ats);
					} else {
						actNtf.setAt("," + ats + ",");
					}
				}
				actNtf.setActname(group.getName());
				actNtf.setJoinnum(group.getJoinnum());
				actNtf.setActavatar(group.getAvatar());
				for (WxChatItems chatItems : chatItemList) {
					String otheruid = chatItems.getUid() + "";
					actNtf.setGrouprole(chatItems.getBizrole());
					actNtf.setMsgfreeflag(chatItems.getMsgfreeflag());
					ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(actNtf));
					Ims.sendToUser(chatItems.getUid(), otherPacket);
					Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
				}
			}
		} else {
			//消息处理-超过1000个会话未激活，一般都是脚本执行的，情况很少，不做事务处理，未来如果出现此业务，可以进行逻辑优化
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						//首次接受或者发送消息处理
						if (CollectionUtil.isNotEmpty(noStartMsgGroupChatsFinal)) {
							ChatIndexService.me.chatGroupStartMsgUpdate(groupid, msg.getId());
							for (WxChatGroupItem groupItem : noStartMsgGroupChatsFinal) {
								ChatIndexService.removeChatGroupCache(groupid, groupItem.getUid());
							}
						}
						//激活消息处理
						Ret ret = WxChatApi.dealGroupAct(noActGroupChatsFinal, group, msg, user);
						if (ret.isFail()) {
							log.error(ret.getStr("msg"));
						}
						List<WxChatItems> chatItemList = RetUtils.getOkTData(ret);
						if (CollectionUtil.isNotEmpty(chatItemList)) {
							WxGroupChatNtf actNtf = WxGroupChatNtf.from(msg, sysMsgVo);
							actNtf.setActflag(Const.YesOrNo.YES);
							actNtf.setActname(group.getName());
							actNtf.setJoinnum(group.getJoinnum());
							if (StrUtil.isNotBlank(ats)) {
								actNtf.setAt("," + ats + ",");
							}
							actNtf.setActavatar(group.getAvatar());
							for (WxChatItems chatItems : chatItemList) {
								String otheruid = chatItems.getUid() + "";
								actNtf.setGrouprole(chatItems.getBizrole());
								actNtf.setMsgfreeflag(chatItems.getMsgfreeflag());
								ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(actNtf));
								Ims.sendToUser(chatItems.getUid(), otherPacket);
								Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
							}
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
	}
	public void sendGroupMsg(WxGroupMsg msg, SysMsgVo sysMsgVo, String ats, WxGroup group, List<WxChatItems> chatItemList, List<WxChatGroupItem> noActGroupChatsFinal,
							 List<WxChatGroupItem> noStartMsgGroupChatsFinal, User user, Short updatelist, Short openforbidden, Integer touid) {
		if (group.getJoinnum().equals(Short.valueOf("2"))) {
			WxGroupUser wxGroupUser = WxGroupUser.dao.findFirst("select * from wx_group_user where groupid = ? and grouprole = 2", group.getId());
			if (wxGroupUser != null) {
				ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
				String key = "tempIM_" + wxGroupUser.getUid();
				Serializable serializable = cache.get(key);
				if (serializable != null) {
					HashMap<String, Object> result = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
						@Override
						public HashMap<String, Object> create() {
							return null;
						}
					});
					cache.remove(key);
					Map<String, Object> result1 = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
						@Override
						public HashMap<String, Object> create() {
							return result;
						}
					});
				}
			}

		}
		int notActSize = chatItemList != null ? chatItemList.size() : 0;
		long groupid = group.getId();
		WxGroupChatNtf wxGroupChatNtf = WxGroupChatNtf.from(msg, sysMsgVo);
		wxGroupChatNtf.setUpdatelist(updatelist != null ? updatelist : (short) 2);
		wxGroupChatNtf.setOpenforbidden(openforbidden);
		if (touid != null) {
			wxGroupChatNtf.setTouid(touid.toString());
		}
		if (StrUtil.isNotBlank(ats)) {
			if (ats.equals("all")) {
				wxGroupChatNtf.setAt(ats);
			} else {
				wxGroupChatNtf.setAt("," + ats + ",");
			}
		}
		ImPacket imPacket = new ImPacket(Command.ForbiddenNotify, wxGroupChatNtf);
		Ims.sendToGroup(groupid, imPacket);
		if (notActSize < 1000) {
			if (CollectionUtil.isNotEmpty(chatItemList)) {
				WxGroupChatNtf actNtf = WxGroupChatNtf.from(msg, sysMsgVo);
				actNtf.setActflag(Const.YesOrNo.YES);
				if (StrUtil.isNotBlank(ats)) {
					if (ats.equals("all")) {
						actNtf.setAt(ats);
					} else {
						actNtf.setAt("," + ats + ",");
					}
				}
				actNtf.setActname(group.getName());
				actNtf.setJoinnum(group.getJoinnum());
				actNtf.setActavatar(group.getAvatar());
				for (WxChatItems chatItems : chatItemList) {
					String otheruid = chatItems.getUid() + "";
					actNtf.setGrouprole(chatItems.getBizrole());
					actNtf.setMsgfreeflag(chatItems.getMsgfreeflag());
					ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(actNtf));
					Ims.sendToUser(chatItems.getUid(), otherPacket);
					Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
				}
			}
		} else {
			//消息处理-超过1000个会话未激活，一般都是脚本执行的，情况很少，不做事务处理，未来如果出现此业务，可以进行逻辑优化
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						//首次接受或者发送消息处理
						if (CollectionUtil.isNotEmpty(noStartMsgGroupChatsFinal)) {
							ChatIndexService.me.chatGroupStartMsgUpdate(groupid, msg.getId());
							for (WxChatGroupItem groupItem : noStartMsgGroupChatsFinal) {
								ChatIndexService.removeChatGroupCache(groupid, groupItem.getUid());
							}
						}
						//激活消息处理
						Ret ret = WxChatApi.dealGroupAct(noActGroupChatsFinal, group, msg, user);
						if (ret.isFail()) {
							log.error(ret.getStr("msg"));
						}
						List<WxChatItems> chatItemList = RetUtils.getOkTData(ret);
						if (CollectionUtil.isNotEmpty(chatItemList)) {
							WxGroupChatNtf actNtf = WxGroupChatNtf.from(msg, sysMsgVo);
							actNtf.setActflag(Const.YesOrNo.YES);
							actNtf.setActname(group.getName());
							actNtf.setJoinnum(group.getJoinnum());
							if (StrUtil.isNotBlank(ats)) {
								actNtf.setAt("," + ats + ",");
							}
							actNtf.setActavatar(group.getAvatar());
							for (WxChatItems chatItems : chatItemList) {
								String otheruid = chatItems.getUid() + "";
								actNtf.setGrouprole(chatItems.getBizrole());
								actNtf.setMsgfreeflag(chatItems.getMsgfreeflag());
								ImPacket otherPacket = new ImPacket(Command.WxGroupChatNtf, Json.toJson(actNtf));
								Ims.sendToUser(chatItems.getUid(), otherPacket);
								Tio.bindGroup(TioSiteImServerStarter.tioServerConfigWs, otheruid, groupid + "");
							}
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
	}
}
