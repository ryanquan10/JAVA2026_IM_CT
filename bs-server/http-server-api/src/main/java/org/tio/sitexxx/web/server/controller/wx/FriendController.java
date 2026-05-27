
package org.tio.sitexxx.web.server.controller.wx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendApplyService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RetCode.CommonCode;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友相关接口
 * @author lixinji
 * 2021年1月7日 下午6:26:37
 */
@RequestPath(value = "/friend")
public class FriendController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(FriendController.class);

	/**
	 * 修改好友备注名-已调整
	 * @param request
	 * @param frienduid
	 * @param remarkname
	 * @return
	 */
	@RequestPath(value = "/modifyRemarkname")
	public Resp modifyRemarkname(HttpRequest request, Integer frienduid, String remarkname) throws Exception {
		User curr = WebUtils.currUser(request);
		if (StrUtil.isNotBlank(remarkname)) {
			remarkname = EscapeUtil.escapeHtml4(remarkname);
		} else {
			remarkname = "";
		}
		Ret friendRet = FriendService.me.isFriend(curr, frienduid);
		if (friendRet.get("data").equals(Short.valueOf("2"))) {
			StrangerRemarkName strangerRemarkName = StrangerRemarkName.dao.findFirst("select * from stranger_remark_name where uid = ? and relation_uid = ?", curr.getId(), frienduid);
			if (strangerRemarkName == null) {
				StrangerRemarkName strangerRemarkName1 = new StrangerRemarkName();
				strangerRemarkName1.setUid(curr.getId());
				strangerRemarkName1.setRelationUid(frienduid);
				strangerRemarkName1.setRemarkName(remarkname);
				strangerRemarkName1.setCreateTime(new Date());
				strangerRemarkName1.setUpdateTime(new Date());
				strangerRemarkName1.save();
			} else {
				strangerRemarkName.setRemarkName(remarkname);
				strangerRemarkName.setUpdateTime(new Date());
				strangerRemarkName.update();

			}

			return Resp.ok(RetUtils.OPER_RIGHT);
		}

		Ret ret = FriendService.me.updateRemarkName(curr.getId(), frienduid, remarkname);
		if (ret.isFail()) {

			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		Long fid = RetUtils.getOkTData(ret);
		if (WxSynApi.isSynVersion()) {
			WxSynApi.synFdRemarkName(curr.getId(), fid, remarkname, RetUtils.getOkTData(ret, "chatlinkid"));
		} else {
			WxFriend sendFreind = FriendService.me.getFriendInfo(fid);
			WxChatItems chatItems = ChatService.me.getAllChatItems(RetUtils.getOkTData(ret, "chatlinkid"));
			WxChatApi.autoUseSysChatNtf(curr.getId(), Const.WxSysCode.FRIEND_INFO_UPDATE, "好友信息发生变更", Json.toJson(sendFreind), chatItems);
		}

		ICache cache = Caches.getCache(CacheConfig.REMARK);
		String key = "remark_" + curr.getId();
		if (cache.get(key)!=null) {
			cache.remove(key);
		}

		WxChatApi.useSysChatNtf(request, curr.getId(), Const.WxSysCode.CLEAR_REMARK_NAME_CACHE, "修改好友和陌生人备注缓存", null);


//		log.error("token: {}", request.getHttpSession().getId());
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 忽略申请
	 * @param request
	 * @param applyid
	 * @param remarkname
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年2月4日 下午3:11:47
	 */
	@RequestPath(value = "/ignoreApply")
	public Resp ignoreApply(HttpRequest request, Integer applyid) throws Exception {
		User curr = WebUtils.currUser(request);
		WxFriendApplyItems items = FriendApplyService.me.getById(applyid);
		if (items == null) {
			return RetUtils.getFailResp(CommonCode.BIZ_NOT_EXIST.value);
		}
		Ret ret = FriendService.me.ignoreApply(curr, items);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}



	/**
	 * 指定好友是否在线
	 * @param request
	 * @param applyid
	 * @param remarkname
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年2月4日 下午3:11:47
	 */
	@RequestPath(value = "/isOnline")
	public Resp isOnline(HttpRequest request, Integer uid) throws Exception {
		User toUser = User.dao.findById(uid);
		if (toUser == null) {
			return Resp.fail("该用户不存在");
		}
		boolean online = !WxChatApi.isOutline(uid);
		UserLastLoginTime lastLoginTime = UserLastLoginTime.dao.findFirst("select * from user_last_login_time where uid = ?", uid);
		Map data = new HashMap();
		data.put("online", online);
		data.put("lastLoginTime",lastLoginTime!=null ? lastLoginTime.getCreatetime() : null);
		return Resp.ok(data);
	}


	/**
	 * 添加标签
	 * @param request
	 * @param labelname
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/addLabel")
	public Resp addLabel(HttpRequest request, String labelname) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}

		if (labelname == null||labelname.isEmpty()) {
			return Resp.fail("标签名不能为空");

		}
		Label label = Label.dao.findFirst("select * from label where uid = ? and labelname = ?", curr.getId(), labelname);
		if (label != null) {
			return Resp.fail("标签名称已存在");
		}

		Label label_ = new Label();
		label_.setUid(curr.getId());
		label_.setLabelname(labelname);
		label_.setCreatetime(new Date());
		boolean save = label_.save();
		if (save) {
			return Resp.ok(RetUtils.OPER_RIGHT);
		}
		return Resp.fail(RetUtils.OPER_ERROR);
	}


	/**
	 * 修改标签名称
	 * @param request
	 * @param labelname
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/updateLabel")
	public Resp updateLabel(HttpRequest request, Integer labelid, String labelname) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		if (labelid == null) {
			return Resp.fail("请选择修改的标签");
		}

		if (labelname == null||labelname.isEmpty()) {
			return Resp.fail("标签名不能为空");
		}
		Label label = Label.dao.findFirst("select * from label where uid = ? and id = ?", curr.getId(), labelid);
		if (label == null) {
			return Resp.fail("标签不存在");
		}
		if (labelname.equals(label.getLabelname())) {
			return Resp.fail("标签名称相同");
		}

		label.setLabelname(labelname);
		boolean update = label.update();
		if (update) {
			return Resp.ok(RetUtils.OPER_RIGHT);
		}
		return Resp.fail(RetUtils.OPER_ERROR);
	}



	/**
	 * 删除标签
	 * @param request
	 * @param labelids
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/delLabel")
	public Resp delLabel(HttpRequest request, String labelids) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		String[] labelidList = labelids.split(",");
		for (String labelid:labelidList) {
			Label label = Label.dao.findById(labelid);
			if (label == null) {
				return Resp.fail("标签名称不存在");
			}
			if (!label.getUid().equals(curr.getId())) {
				return Resp.fail("标签名称不存在");
			}

			List<WxFriend> wxFriends = WxFriend.dao.find("select * from wx_friend where uid = ?", curr.getId());
			for (WxFriend friend:wxFriends) {
				if (friend.getLabelids() == null) {
					continue;
				}
				String[] splits = friend.getLabelids().split(",");
				String flabelids = "";
				for (String split : splits) {
					if (split.trim().equals(labelid)) {
						continue;
					}
					if (split.trim().isEmpty()) {
						continue;
					}
					flabelids += " " + split+", ";
				}
				if (!flabelids.equals(friend.getLabelids())) {
					friend.setLabelids(flabelids);
					friend.update();
				}
			}
			label.delete();
		}

		return Resp.ok(RetUtils.OPER_RIGHT);
	}


	/**
	 * 查询标签列表
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/labelList")
	public Resp labelList(HttpRequest request, String searchkey) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		List<Label> labels;
		if (searchkey != null && !searchkey.isEmpty()) {
			labels = Label.dao.find("select * from label where uid = ? and labelname like ?", curr.getId(), "%"+searchkey+"%");
		} else {
			labels = Label.dao.find("select * from label where uid = ?", curr.getId());
		}
		for (Label label:labels) {
//			List<WxFriend> wxFriends = WxFriend.dao.find("select * from wx_friend where uid = ? and labelids like ?", curr.getId(), "% " + label.getId() + ", %");
			Kv params = Kv.create();
			params.set("uid", curr.getId());
			params.set("id", label.getId());
			params.set("labelid", "% " + label.getId() + ", %");
//			if (searchkey != null && !searchkey.isEmpty()) {
//				params.set("searchkey", searchkey);
//			}
			SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.labelList", params);
			List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara/*.getSql(), params.get("uid"), params.get("id"), params.get("labelid")*/);

			if (records == null) {
				label.setNum(0);
			} else {
				label.setNum(records.size());
			}
			label.setFriendList(records);
		}
		Map<String,List<Label>> map = new HashMap<>();
		map.put("labels", labels);
		return Resp.ok(map);
	}


	/**
	 * 查询标签好友
	 * @param request
	 * @param searchkey
	 * @param labelid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/searchLabelUser")
	public Resp searchLabelUser(HttpRequest request, String searchkey, Integer labelid) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		if (labelid == null) {
			return Resp.fail("未选择标签");
		}

		Kv params = Kv.create();
		params.set("uid", curr.getId());
		params.set("id", labelid);
		params.set("labelid", "% " + labelid + ", %");
		if (searchkey != null && !searchkey.isEmpty()) {
			params.set("searchkey", "%"+searchkey+"%");
			params.set("fuid", searchkey);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.labelList", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara/*.getSql(), params.get("uid"), params.get("id"), params.get("labelid")*/);
		Map<String,List<Record>> map = new HashMap<>();
		map.put("fdList", records);
		return Resp.ok(map);
	}


	/**
	 * 获取群组中的好友列表
	 * @param request
	 * @param searchkey
	 * @param labelid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/groupLabelFriendList")
	public Resp groupLabelFriendList(HttpRequest request, String searchkey, Integer labelid) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
//		if (labelid == null) {
//			return Resp.fail("未选择标签");
//		}

		Kv params = Kv.create();
		params.set("uid", curr.getId());
//		params.set("id", labelid);
//		params.set("labelid", "% " + labelid + ", %");
		if (searchkey != null && !searchkey.isEmpty()) {
			params.set("searchkey", "%"+searchkey+"%");
			params.set("groupid", searchkey);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.searchGroup", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara/*.getSql(), params.get("uid"), params.get("id"), params.get("labelid")*/);
		for (Record record:records) {
			Kv params2 = Kv.create();
			params2.set("groupid", record.get("id"));
			params2.set("uid", curr.getId());
			if (labelid != null) {
				params2.set("labelid", "% " + labelid + ", %");
			}
			SqlPara sqlPara2 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friend.searchGroupFdList", params2);
			List<Record> fdList = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara2/*.getSql(), params.get("uid"), params.get("id"), params.get("labelid")*/);
			record.set("fdList", fdList);
			record.set("fdNum", fdList == null ? 0 : fdList.size());
		}
		Map<String,List<Record>> map = new HashMap<>();
		map.put("groups", records);
		return Resp.ok(map);
	}

	/**
	 * 添加好友标签
	 * @param request
	 * @param uids
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/addLabelForFriend")
	public Resp addLabel(HttpRequest request, String uids, Integer labelid) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		if (uids == null || uids.isEmpty()) {
			return Resp.fail("未选择好友");
		}
		if (labelid == null) {
			return Resp.fail("未选择标签");
		}
		Label label = Label.dao.findById(labelid);
		if (label == null) {
			return Resp.fail("标签不存在");
		}
		String[] uidList = uids.split(",");
		for (String uid : uidList) {
			WxFriend friend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid = ?", curr.getId(), uid);
			if (friend == null) {
				continue;
			}
			if (friend.getLabelids() != null) {
				if (friend.getLabelids().contains(" " + labelid + ", ")) {
					continue;
				}
				friend.setLabelids(friend.getLabelids() + " " + labelid + ", ");
			} else {
				friend.setLabelids(" " + labelid + ", ");
			}
			boolean update = friend.update();
			if (!update) {
				return Resp.fail(RetUtils.OPER_ERROR);
			}
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}


	/**
	 * 更新好友标签
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/updateLabelForFriend")
	public Resp updateLabelForFriend(HttpRequest request, String uid, String labelids) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		if (uid == null) {
			return Resp.fail("未选择好友");
		}
		WxFriend friend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid = ?", curr.getId(), uid);
		if (friend == null) {
			return Resp.fail("好友信息不存在");
		}
		if (labelids != null) {
			String[] labelIdList = labelids.split(",");
			for (String labelId:labelIdList) {
				if (labelId == null || labelId.isEmpty()) {
					continue;
				}
				Label label = Label.dao.findById(labelId.trim());
				if (label == null || (!label.getUid().equals(curr.getId()))) {
					return Resp.fail("标签不存在");
				}
			}
		}
		friend.setLabelids(labelids+" ");

		boolean update = friend.update();
		if (!update) {
			return Resp.fail(RetUtils.OPER_ERROR);
		}

		return Resp.ok(RetUtils.OPER_RIGHT);
	}



	/**
	 * 删除好友标签
	 * @param request
	 * @param uids
	 * @param labelid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2024年8月21日 下午4:41:47
	 */
	@RequestPath(value = "/delLabelForFriend")
	public Resp delLabelForFriend(HttpRequest request, String uids, Integer labelid) throws Exception {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		if (uids == null || uids.isEmpty()) {
			return Resp.fail("未选择好友");
		}
		if (labelid == null) {
			return Resp.fail("未选择标签");
		}
		Label label = Label.dao.findById(labelid);
		if (label == null) {
			return Resp.fail("标签不存在");
		}
		if (!label.getUid().equals(curr.getId())) {
			return Resp.fail("标签不存在");
		}
		String[] uidList = uids.split(",");
		for (String uid : uidList) {
			WxFriend friend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid = ?", curr.getId(), uid);
			if (friend == null) {
				return Resp.fail("好友不存在");
			}
			friend.setLabelids(friend.getLabelids().replace(" " + labelid + ", ", " "));
			boolean update = friend.update();
			if (!update) {
				return Resp.fail(RetUtils.OPER_ERROR);
			}
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 获取好友和陌生人备注
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2021年2月4日 下午3:11:47
	 */
	@RequestPath(value = "/getRemarkList")
	public Resp getRemarkList(HttpRequest request) throws Exception {
		User currUser = WebUtils.currUser(request);
		if(currUser==null){
			return Resp.fail("请登录");
		}
		ICache cache = Caches.getCache(CacheConfig.REMARK);
		String key = "remark_" + currUser.getId();
		HashMap<String, Object> result = CacheUtils.get(cache, key, false, new FirsthandCreater<HashMap<String, Object>>() {
			@Override
			public HashMap<String, Object> create() {
				HashMap<String, Object> results = new HashMap<>();
//				Map<Integer, String> friendRemark = new HashMap<>();
//				Map<Integer, String> strangerRemark = new HashMap<>();
				List<WxFriend> wxFriends = WxFriend.dao.find("select * from wx_friend where uid = ? and remarkname != ''", currUser.getId());
				if (wxFriends != null) {
					for (WxFriend wxFriend : wxFriends) {
						results.put(wxFriend.getFrienduid().toString(), wxFriend.getRemarkname());
					}
				}
				List<StrangerRemarkName> strangerRemarkNames = StrangerRemarkName.dao.find("select * from stranger_remark_name where uid = ?", currUser.getId());
				if (strangerRemarkNames != null) {
					for (StrangerRemarkName strangerRemarkName : strangerRemarkNames) {
						results.put(strangerRemarkName.getRelationUid().toString(), strangerRemarkName.getRemarkName());
					}
				}
//				results.put("friendRemarkName", friendRemark);
//				results.put("strangerRemarkName", strangerRemark);
				return results;
			}
		});


		return Resp.ok(result);
	}


}
