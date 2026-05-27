
package org.tio.sitexxx.service.service.chat;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.atom.ChatAtom;
import org.tio.sitexxx.service.service.base.SensitiveWordsService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatMsgService.MsgTemplate;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.CommonUtils;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxForbiddenVo;
import org.tio.sitexxx.service.vo.wx.WxGroupApplyVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 新版群聊服务
 *
 * @author lixinji
 * 2020年2月13日 下午7:01:05
 */
public class GroupService {
    private static Logger log = LoggerFactory.getLogger(GroupService.class);
    public static final GroupService me = new GroupService();

    static final ICache GROUP_MSG_CACHE = Caches.getCache(CacheConfig.WX_GROUP_CHAT_6);

    /**
     * 默认查询群聊的的历史记录条数
     */
    public static final int GROUP_MSG_LIMIT = ConfService.getInt(Const.ConfMapping.WX_GROUP_MSG_LIMIT, 30);

    /**
     * 缩略消息默认长度
     */
    static final int GROUP_MSG_RESUME_MAXSIZE = ConfService.getInt(Const.ConfMapping.WX_GROUP_MSG_RESUME_MAXSIZE, 50);

    /**
     * 群用户默认显示多少
     */
    static final int GROUP_USER_VIEW_DEFAULT = ConfService.getInt(Const.ConfMapping.WX_GROUP_USER_VIEW_DEFAULT, 11);

    /*******************************************begin-调整-**********************************************************/
    /**
     * 通讯录-群列表
     *
     * @param uid
     * @param searchkey
     * @return
     * @author lixinji
     * 2020年2月17日 下午1:43:11
     */
    public Ret groupList(Integer uid, String searchkey) {
        Kv params = Kv.by("uid", uid).set("chatmode", Const.ChatMode.GROUP).set("linkflag", Const.YesOrNo.YES).set("status", Const.Status.NORMAL);
        if (StrUtil.isNotBlank(searchkey)) {
            params.set("nick", "%" + searchkey + "%");
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.grouplist", params);
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
        for (Record record : records) {
            record.set("joinnum", record.getInt("joinnum") + record.getInt("vnum"));
        }
        return RetUtils.okData(records);
    }

    /**
     * 群信息-缓存-已调整
     *
     * @param groupid
     * @param uid
     * @return
     * @author lixinji
     * 2020年2月26日 下午4:24:08
     */
    public Ret groupInfo(Long groupid, Integer uid) {
        Ret ret = Ret.ok();
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return Ret.fail().set("errorMsg", "该群组不存在");
        }
        if (group.getGroupleaderlabel() == null || group.getGroupleaderlabel().isEmpty()) {
            group.setGroupleaderlabel("群主");
        }
        if (group.getGroupmanagerlabel() == null || group.getGroupmanagerlabel().isEmpty()) {
            group.setGroupmanagerlabel("管理员");
        }
        if (group.getGroupmemberlabel() == null || group.getGroupmemberlabel().isEmpty()) {
            group.setGroupmemberlabel("成员");
        }
        group.setGroupNotices(WxGroupNotice.dao.find("select * from wx_group_notice where groupid = ? order by istop desc, toptime desc, createtime desc", groupid));
        if (uid != null) {
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
            if (ChatService.groupChatLink(groupItem)) {
                WxGroupUser groupUser = getGroupUser(groupItem);
                ret.set("groupuser", groupUser);
            }
        }
        return ret.set("group", group);
    }

    /**
     * 获取群信息：包含群人员和群的消息开关
     *
     * @param groupid
     * @return
     * @author lixinji
     * 2020年2月17日 上午11:00:53
     */
    public WxGroup getByGroupid(Long groupid) {
        String key = groupid + "";
        ICache cache = Caches.getCache(CacheConfig.WX_GROUP_4);
        WxGroup group = CacheUtils.get(cache, key, true, new FirsthandCreater<WxGroup>() {
            @Override
            public WxGroup create() {
                Kv params = Kv.by("groupid", groupid);
                SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.groupInfo", params);
                WxGroup wxGroup = WxGroup.dao.findFirst(sqlPara);
                Short joinnum = wxGroup.getJoinnum() == null ? 0 : wxGroup.getJoinnum();
                Integer vnum = wxGroup.getVnum() == null ? 0 : wxGroup.getVnum();
                wxGroup.setJoinnum((short) (joinnum + vnum));
                return wxGroup;
            }
        });
        return group;
    }

    /**
     * 群用户列表-已调整
     * 1、有条件时为无缓存
     * 2、无条件缓存
     *
     * @param groupid
     * @param pageNumber
     * @return
     * @author lixinji
     * 2020年4月7日 下午3:29:21
     */
    public Ret groupUserList(Long groupid, Integer pageNumber, String searchkey) {
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if (StrUtil.isNotBlank(searchkey)) {
            Kv params = Kv.by("groupid", groupid).set("searchkey", "%" + searchkey + "%").set("yes", Const.YesOrNo.YES).set("no", Const.YesOrNo.NO);
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.groupUserlist", params);
            Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 100, sqlPara);
            return RetUtils.okPage(records);
        } else {
            final Integer page = pageNumber;
            String key = groupid + "_" + pageNumber;
            ICache cache = Caches.getCache(CacheConfig.CHAT_GROUP_USER_LIST_3);
            Page<Record> records = CacheUtils.get(cache, key, true, new FirsthandCreater<Page<Record>>() {
                @Override
                public Page<Record> create() {
                    Kv params = Kv.by("groupid", groupid);
                    SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.groupUserlist", params);
                    Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(page, 100, sqlPara);
                    return records;
                }
            });
            return RetUtils.okPage(records);
        }
    }

    /**
     * 禁言用户
     *
     * @param groupid
     * @param pageNumber
     * @param searchkey
     * @return
     * @author lixinji
     * 2021年1月5日 上午11:47:52
     */
    public Ret forbiddenUserList(Long groupid, Integer pageNumber, String searchkey) {
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        Kv params = Kv.by("groupid", groupid).set("yes", Const.YesOrNo.YES).set("no", Const.YesOrNo.NO).set("noflag", Const.Forbiddenflag.NO);
        if (StrUtil.isNotBlank(searchkey)) {
            params.set("searchkey", "%" + searchkey + "%");
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.forbiddenUserList", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 100, sqlPara);
        return RetUtils.okPage(records);
    }

    /**
     * at群的用户列表-已调整
     * TODO:lixinji-此处可加活跃算法
     *
     * @param groupid
     * @param searchkey
     * @return
     * @author lixinji
     * 2020年4月7日 下午5:54:24
     */
    public Ret atGroupUserList(Long groupid, String searchkey, Integer uid) {
        Kv params = Kv.by("groupid", groupid).set("uid", uid);
        if (StrUtil.isNotBlank(searchkey)) {
            params.set("searchkey", "%" + searchkey + "%").set("yes", Const.YesOrNo.YES).set("no", Const.YesOrNo.NO);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.atGroupUserlist", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(1, 100, sqlPara);
        return RetUtils.okList(records.getList());
    }

    /**
     * 获取起始消息列表
     * 1、如果会话的起始消息为空，返回空
     * 2、如果会话为无效会话，进行非缓存消息处理
     * 3、如果获取的缓存消息的最小消息小于会话的起始消息，进行非缓存处理
     * 4、如果缓存消息中，全是无效的用户消息，进行非缓存处理
     *
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年2月13日 下午8:02:19
     */
    public ArrayList<WxGroupMsg> getGroupCacheMsgList(Long chatlinkid) {
        Short msgtype = 1;
        long currTime = System.currentTimeMillis();
        if (chatlinkid == null) {
            return null;
        }
        WxChatItems chatItems = ChatService.me.getBaseChatItems(chatlinkid);
        if (chatItems == null) {
            return null;
        }
        WxChatGroupItem userItem = ChatIndexService.chatGroupIndex(chatItems);
        if (userItem == null) {
            return null;
        }
        Long startMsg = userItem.getStartmsgid();
        if (startMsg == null) {
            return null;
        }
        if (Objects.equals(chatItems.getLinkflag(), Const.YesOrNo.NO)) {
            return (ArrayList<WxGroupMsg>) getStartGroupMsgList(userItem, chatItems, startMsg);
        }
        String key = chatItems.getBizid() + "";
        ArrayList<WxGroupMsg> msgList = CacheUtils.get(GROUP_MSG_CACHE, key, true, new FirsthandCreater<ArrayList<WxGroupMsg>>() {
            @Override
            public ArrayList<WxGroupMsg> create() {
                int historyCount = ConfService.getInt("im.history.chat.count.group", GROUP_MSG_LIMIT);
                Kv params = Kv.by("groupid", chatItems.getBizid()).set("status", Const.Status.NORMAL).set("limit", historyCount);
                SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.groupMsgList", params);
                ArrayList<WxGroupMsg> list = (ArrayList<WxGroupMsg>) WxGroupMsg.dao.find(sqlPara);
                return list;
            }
        });
        if (CollectionUtil.isNotEmpty(msgList) && msgList.get(msgList.size() - 1).getMid() > startMsg) {
            for (WxGroupMsg msg : msgList) {//判断是否全是无效消息
                if (Objects.equals(msg.getWhereflag(), Const.YesOrNo.NO) || msg.getWhereuid().indexOf("," + userItem.getUid() + ",") < 0) {
                    msgtype = 1;
                    long endtime = System.currentTimeMillis();
                    long useTime = endtime - currTime;
                    if (useTime > 500) {
                        log.error("群内部消息查询慢，条件：type:{},usetime:{},chatlinkid:{}", msgtype, useTime, chatlinkid);
                    }
                    return msgList;
                }
            }
        }
        //非缓存处理
        List<WxGroupMsg> list = getStartGroupMsgList(userItem, chatItems, startMsg);
        msgtype = 2;
        long endtime = System.currentTimeMillis();
        long useTime = endtime - currTime;
        if (useTime > 500) {
            log.error("群内部消息查询慢，条件：type:{},usetime:{},chatlinkid:{}", msgtype, useTime, chatlinkid);
        }
        return (ArrayList<WxGroupMsg>) list;
    }

    /**
     * 非缓存起始消息处理
     *
     * @param chatlinkid
     * @param startMid
     * @return
     * @author lixinji
     * 2020年12月22日 下午3:56:09
     */
    public List<WxGroupMsg> getStartGroupMsgList(WxChatGroupItem userItem, WxChatItems chatItems, Long startMid) {
        int historyCount = ConfService.getInt("im.history.chat.count.group", GROUP_MSG_LIMIT);
        Kv params = Kv.by("groupid", userItem.getGroupid()).set("status", Const.Status.NORMAL).set("uidstr", "," + userItem.getUid() + ",").set("uid", userItem.getUid())
                .set("limit", historyCount);
        if (startMid != null) {
            params.set("startmsgid", startMid);
        } else {
            params.set("startmsgid", WxFriendMsg.maxid);
        }
        if (Objects.equals(chatItems.getLinkflag(), Const.YesOrNo.NO)) {
            chatItems = ChatService.me.getAllChatItems(userItem.getChatlinkid());
            params.set("kickmsgid", chatItems.getLastmsgid());
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.groupMsgList", params);
        List<WxGroupMsg> list = WxGroupMsg.dao.find(sqlPara);
        return list;
    }

    /**
     * 获取其他消息
     *
     * @param chatlinkid
     * @param startMid
     * @return
     * @author lixinji
     * 2020年2月13日 下午8:35:26
     */
    public List<WxGroupMsg> getOtherGroupMsgList(Long chatlinkid, Long startMid) {
        if (chatlinkid == null) {
            return null;
        }
        if (startMid == null) {
            List<WxGroupMsg> first = getGroupCacheMsgList(chatlinkid);
            return first;
        }
        WxChatGroupItem userItem = ChatIndexService.chatGroupIndex(chatlinkid);
        if (userItem == null) {
            return null;
        }
        Long startMsg = userItem.getStartmsgid();
        if (startMsg == null) {
            return null;
        }
        int historyCount = ConfService.getInt("im.history.chat.count.group", GROUP_MSG_LIMIT);
        Kv params = Kv.by("groupid", userItem.getGroupid()).set("status", Const.Status.NORMAL).set("uidstr", "," + userItem.getUid() + ",").set("uid", userItem.getUid())
                .set("limit", historyCount).set("limitmsgid", startMid);
        if (startMsg != null) {
            params.set("startmsgid", startMsg);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.groupMsgList", params);
        List<WxGroupMsg> list = WxGroupMsg.dao.find(sqlPara);
        return list;
    }

    /**
     * 同步群消息
     *
     * @param chatlinkid
     * @param endmid
     * @return
     * @author lixinji
     * 2020年3月10日 下午3:47:34
     */
    public List<WxGroupMsg> getSynGroupMsgList(Long chatlinkid, Long endmid) {
        if (chatlinkid == null) {
            return null;
        }
        WxChatGroupItem userItem = ChatIndexService.chatGroupIndex(chatlinkid);
        if (userItem == null) {
            return null;
        }
        Long startMsg = userItem.getStartmsgid();
        if (startMsg == null) {
            return null;
        }
        int historyCount = ConfService.getInt("im.history.chat.count.group", GROUP_MSG_LIMIT);
        Kv params = Kv.by("groupid", userItem.getGroupid()).set("status", Const.Status.NORMAL).set("uidstr", "," + userItem.getUid() + ",").set("uid", userItem.getUid())
                .set("limit", historyCount);
        if (startMsg != null) {
            if (startMsg < endmid) {
                params.set("endmid", endmid);
            } else {
                params.set("startmsgid", startMsg);
            }
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.groupMsgList", params);
        List<WxGroupMsg> list = WxGroupMsg.dao.find(sqlPara);
        return list;
    }

    /**
     * 进群检测-已调整:joinflag:1-代表已在群众;2:代表可以进群；错误代表无法进群
     *
     * @param operuid  操作人
     * @param groupid  群
     * @param uid      被邀请的人
     * @param sendtime
     * @return
     * @author lixinji
     * 2020年12月22日 下午4:13:43
     */
    public Ret checkJoinGroup(Integer operuid, Long groupid, Integer uid, Date sendtime, boolean apply, Integer num) {
        if (num == null) {
            num = 1;
        }
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("群已解散");
        }
        if (Objects.equals(group.getStatus(), Const.Status.DISABLED)) {
            return RetUtils.failMsg("本群已封停");
        }
        if (num > 0 && (group.getJoinnum() + num) > group.getMaximum()) {
            return RetUtils.failMsg("群人数已达上限");
        }
        WxChatGroupItem joinGroupItem = null;
        if (uid != null) {
            joinGroupItem = ChatIndexService.chatGroupIndex(uid, groupid);
            if (ChatService.groupChatLink(joinGroupItem)) {
                return Ret.ok().set("joinflag", Const.YesOrNo.YES);
            }
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(operuid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("邀请人已退群，邀请失效");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.OWNER) || Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MANAGER)) {
            return Ret.ok().set("joinflag", Const.YesOrNo.NO);
        }
        if (!apply && Objects.equals(group.getJoinmode(), Const.GroupJoinMode.REVIEW)) {
            return RetUtils.failMsg("本群已开启入群审核，禁止邀请加入");
        }
        if (sendtime != null && DateUtil.betweenMs(sendtime, new Date()) > 3 * 24 * 60 * 60 * 1000) {
            return RetUtils.failMsg("邀请链接已失效");
        }
        if (!apply && Objects.equals(group.getApplyflag(), Const.YesOrNo.NO)) {
            return RetUtils.failMsg("该群聊未开放邀请");
        }
        return Ret.ok().set("joinflag", Const.YesOrNo.NO);
    }

    /**
     * 申请信息
     *
     * @param aid
     * @return
     * @author lixinji
     * 2021年1月19日 上午10:19:21
     */
    public Ret groupApplyInfo(Integer aid) {
        if (aid == null) {
            return RetUtils.invalidParam();
        }
        Kv params = Kv.by("aid", aid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.applyinfo", params);
        Record applyinfo = Db.use(Const.Db.TIO_SITE_MAIN).findFirst(sqlPara);
        if (applyinfo == null) {
            return RetUtils.failMsg("申请记录不存在");
        }
        SqlPara listSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.applylist", params);
        List<Record> items = Db.use(Const.Db.TIO_SITE_MAIN).find(listSqlPara);
        if (CollectionUtil.isEmpty(items)) {
            return RetUtils.failMsg("邀请成员为空");
        }
        Map<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("apply", applyinfo);
        retMap.put("items", items);
        return RetUtils.okData(retMap);
    }

    /**
     * @param curr
     * @param aid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月13日 下午5:44:09
     */
    public Ret dealGroupApply(User curr, Integer aid, Long mid) throws Exception {
        if (aid == null || mid == null) {
            return RetUtils.invalidParam();
        }
        WxGroupMsg msg = WxGroupMsg.dao.findById(mid);
        if (msg == null) {
            return RetUtils.failMsg("消息不存在");
        }
        WxGroupApply apply = WxGroupApply.dao.findById(aid);
        if (apply == null) {
            return RetUtils.noExistParam();
        }
        if (!Objects.equals(apply.getStatus(), Const.ApplyStatus.APPLY)) {
            log.error("群入群申请处理：重复处理，aid:{}", aid);
            WxGroupApplyVo old = Json.toBean(msg.getText(), WxGroupApplyVo.class);
            if (Objects.equals(old.getStatus(), Const.ApplyStatus.APPLY)) {
                WxGroupApplyVo applyVo = new WxGroupApplyVo();
                applyVo.setId(apply.getId());
                applyVo.setApplymsg(apply.getApplymsg());
                applyVo.setGroupid(apply.getGroupid());
                applyVo.setOperuid(apply.getOperuid());
                applyVo.setStatus(Const.ApplyStatus.PASS);
                String text = Json.toJson(applyVo);
                WxGroupMsg updateMsg = new WxGroupMsg();
                updateMsg.setText(text);
                updateMsg.setId(mid);
                updateMsg.update();
                ChatIndexService.clearGroupMsgCache(apply.getGroupid());
            }
            return RetUtils.okData(Const.YesOrNo.YES);
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), apply.getGroupid());
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("你已被移出群聊");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        List<WxGroupApplyItems> items = WxGroupApplyItems.dao.find("select * from wx_group_apply_items where aid =?", aid);
        Ret checkRet = checkJoinGroup(apply.getOperuid(), apply.getGroupid(), null, null, true, items.size());
        if (checkRet.isFail()) {
            return checkRet;
        }
        WxGroupApply update = new WxGroupApply();
        update.setId(aid);
        update.setStatus(Const.ApplyStatus.PASS);
        update.update();
        if (CollectionUtil.isEmpty(items)) {
            log.error("群入群申请处理：明细为空，aid:{}", aid);
            return RetUtils.okData(Const.YesOrNo.YES);
        }
        String uids = "";
        for (WxGroupApplyItems applyItem : items) {
            uids += "," + applyItem.getUid();
        }
        WxGroupApplyVo applyVo = new WxGroupApplyVo();
        applyVo.setId(apply.getId());
        applyVo.setApplymsg(apply.getApplymsg());
        applyVo.setGroupid(apply.getGroupid());
        applyVo.setOperuid(apply.getOperuid());
        applyVo.setStatus(Const.ApplyStatus.PASS);
        String text = Json.toJson(applyVo);
        WxGroupMsg updateMsg = new WxGroupMsg();
        updateMsg.setText(text);
        updateMsg.setId(mid);
        updateMsg.update();
        ChatIndexService.clearGroupMsgCache(apply.getGroupid());
        return RetUtils.okData(Const.YesOrNo.NO).set("uids", uids).set("apply", apply);
    }

    /**
     * @param curr
     * @param groupid
     * @param uids
     * @param applymsg
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月13日 下午4:15:57
     */
//	public Ret joinGroupApply(User curr, Long groupid, String uids, String applymsg) throws Exception {
//		if (groupid == null || StrUtil.isBlank(uids)) {
//			return RetUtils.invalidParam();
//		}
//		if (StrUtil.isBlank(applymsg)) {
//			return RetUtils.failMsg("申请信息不能为空");
//		}
//		WxGroup group = getByGroupid(groupid);
//		if (group == null) {
//			return RetUtils.failMsg("群已解散");
//		}
//		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
//		if (!ChatService.groupChatLink(groupItem)) {
//			return RetUtils.failMsg("你已被移出群聊");
//		}
//
//		AbsTxAtom atom = new AbsTxAtom() {
//
//			@Override
//			public boolean noTxRun() {
//				WxGroupApply apply = new WxGroupApply();
//				apply.setOperuid(curr.getId());
//				apply.setApplymsg(applymsg);
//				apply.setGroupid(groupid);
//				apply.setStatus(Const.ApplyStatus.APPLY);
//				boolean save = apply.save();
//				if (!save) {
//					return failRet("申请记录保存失败");
//				}
//				String[] uidArr = StrUtil.splitToArray(uids, ",");
//				List<String> joinTemp = new ArrayList<>();
//				List<String> onJoinTemp = new ArrayList<>();
//
//				for (String uid : uidArr) {
//					RealNameCertification userReal = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", uid);
//					if (userReal != null && userReal.getStatus().equals(1)) {
//						joinTemp.add(uid);
//					} else {
//						onJoinTemp.add(uid);
//					}
//				}
//				if (joinTemp.size() == 0) {
//					return failRet("邀请的成员均为进行实名认证，无法加入群聊");
//				}
//				String[] joinUids = new String[joinTemp.size()];
//				for (int i = 0; i < joinTemp.size(); i++) {
//					joinUids[i] = joinTemp.get(i);
//				}
//				String[] onJoinUids = new String[onJoinTemp.size()];
//				for (int i = 0; i < onJoinTemp.size(); i++) {
//					onJoinUids[i] = onJoinTemp.get(i);
//				}
//				int count = 0;
//				for (String uidStr : joinUids) {
//					int otheruid = Integer.parseInt(uidStr);
//					WxGroupApplyItems items = new WxGroupApplyItems();
//					items.setAid(apply.getId());
//					items.setUid(otheruid);
//					items.setGroupid(groupid);
//					boolean othersave = items.save();
//					if (!othersave) {
//						return failRet("申请明细记录保存失败");
//					}
//					count++;
//				}
//				return okRet(RetUtils.okData(count).set("apply", apply));
//			}
//		};
//		Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
//		return atom.getRetObj().set("group", group);
//	}
    public Ret joinGroupApply(User curr, Long groupid, String uids, String applymsg) throws Exception {
        if (groupid == null || StrUtil.isBlank(uids)) {
            return RetUtils.invalidParam();
        }
        if (StrUtil.isBlank(applymsg)) {
            return RetUtils.failMsg("申请信息不能为空");
        }
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("你已被移出群聊");
        }
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                WxGroupApply apply = new WxGroupApply();
                apply.setOperuid(curr.getId());
                apply.setApplymsg(applymsg);
                apply.setGroupid(groupid);
                apply.setStatus(Const.ApplyStatus.APPLY);
                boolean save = apply.save();
                if (!save) {
                    return failRet("申请记录保存失败");
                }
                String[] uidArr = StrUtil.splitToArray(uids, ",");
                int count = 0;
                for (String uidStr : uidArr) {
                    int otheruid = Integer.parseInt(uidStr);
                    WxGroupApplyItems items = new WxGroupApplyItems();
                    items.setAid(apply.getId());
                    items.setUid(otheruid);
                    items.setGroupid(groupid);
                    boolean othersave = items.save();
                    if (!othersave) {
                        return failRet("申请明细记录保存失败");
                    }
                    count++;
                }
                return okRet(RetUtils.okData(count).set("apply", apply));
            }
        };
        Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        return atom.getRetObj().set("group", group);
    }

    /**
     * 进群业务逻辑-已调整
     *
     * @param curr
     * @param groupid
     * @param uids
     * @param applyuid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月22日 下午4:12:08
     */
//	public Ret joinGroup(User curr, Long groupid, String uids, Integer applyuid, boolean dealapply) throws Exception {
//		if (StrUtil.isBlank(uids)) {
//			return RetUtils.invalidParam();
//		}
//		Integer operUid = curr.getId();
//		if (applyuid != null) {
//			operUid = applyuid;
//		}
//		Ret checkRet = checkJoinGroup(operUid, groupid, null, null, dealapply, StrUtil.splitToArray(uids, ",").length);
//		if (checkRet.isFail()) {
//			return checkRet;
//		}
//		WxGroup group = getByGroupid(groupid);
//		String[] uidArr = StrUtil.splitToArray(uids, ",");
//		if (uidArr.length > 50) {
//			return RetUtils.failMsg("一次邀请不要超过50哦");
//		}
//
//		List<String> joinTemp = new ArrayList<>();
//		List<String> onJoinTemp = new ArrayList<>();
//
//		for (String uid : uidArr) {
//			RealNameCertification userReal = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", uid);
//			if (userReal != null && userReal.getStatus().equals(1)) {
//				joinTemp.add(uid);
//			} else {
//				onJoinTemp.add(uid);
//			}
//		}
//		log.error("joinuids: {}",joinTemp);
//		log.error("joinuids.size(): {}",joinTemp.size());
//		if (joinTemp.size() == 0) {
//			return RetUtils.failMsg(onJoinTemp.toString() + " 未进行实名认证，不可加入群聊");
//		}
//		String[] joinUids = new String[joinTemp.size()];
//		for (int i = 0; i < joinTemp.size(); i++) {
//			joinUids[i] = joinTemp.get(i);
//		}
//		String[] onJoinUids = new String[onJoinTemp.size()];
//		for (int i = 0; i < onJoinTemp.size(); i++) {
//			onJoinUids[i] = onJoinTemp.get(i);
//		}
//		if (joinUids.length == 1) {
//			int otheruid = Integer.parseInt(joinUids[0]);
//			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
//			if (ChatService.groupChatLink(groupItem)) {
//				return RetUtils.failMsg("用户已在群组中");
//			}
//		}
//
//		List<WxChatGroupItem> resetBindList = new ArrayList<WxChatGroupItem>();
//		AbsTxAtom atom = new AbsTxAtom() {
//			@Override
//			public boolean noTxRun() {
//				short joinnum = 0;
//				String nickStr = "";
//				String groupname = group.getName();
//				boolean updateGroupName = false;
//				boolean msgClear = false;
//				//初始化其它用户信息
//				for (String uidStr : joinUids) {
//					if (StrUtil.isNotBlank(uidStr)) {
//						try {
//							int otheruid = Integer.parseInt(uidStr);
//							User user = UserService.ME.getById(otheruid);
//							if (user == null || Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
//								log.error("邀请用户不存在：uid:{},groupid:{}", otheruid, groupid);
//								continue;
//							}
//							nickStr += "、" + user.getNick();
//							WxGroupUser otherUser = new WxGroupUser();
//							otherUser.setGroupid(groupid);
//							otherUser.setUid(otheruid);
//							otherUser.setSrcnick(user.getNick());
//							otherUser.setAutoflag(Const.YesOrNo.YES);
//							otherUser.setGroupnick(user.getNick());
//							otherUser.setGroupavator(user.getAvatar());
//							int otherSave = otherUser.ignoreSave();
//							if (otherSave <= 0) {
//								//其它用户保存失败
//								log.error("已存在群用户：uid:{},groupid:{}", otheruid, groupid);
//								continue;
//							}
//							WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
//							if (groupItem != null) {//重新绑定
//								//绑定索引
//								ChatIndexService.me.chatUserIndexUpdate(otheruid, groupid, Const.ChatMode.GROUP, Const.YesOrNo.YES, otherUser.getId(), null, null, null, null);
//								if (groupItem.getChatlinkid() != null) {
//									ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
//									//更新会话状态
//									ChatService.me.updateChatItemStatus(groupItem.getChatlinkid(), groupItem.getChatlinkmetaid(), Const.YesOrNo.YES, null, otherUser.getId(),
//									        group.getJoinnum(), groupid, group.getName(), group.getAvatar(), null);
//									WxChatItems chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
//									ChatMsgService.me.rebindGroupMsgDeal(chatItems);
//									msgClear = true;
//								} else {
//									ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
//								}
//								resetBindList.add(groupItem);
//							} else {
//								ChatIndexService.me.chatUserInit(otheruid, Const.ChatMode.GROUP, groupid, null, null, null, null, otherUser.getId(), Const.YesOrNo.YES, null);
//								ChatIndexService.me.chatGroupInit(otheruid, groupid, null, null, otherUser.getId(), Const.YesOrNo.YES, Const.GroupRole.MEMBER, null,
//								        Const.YesOrNo.YES);
//							}
//							joinnum++;
//							if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES)) {
//								String newName = groupname + "、" + user.getNick();
//								if (newName.length() <= 30) {
//									updateGroupName = true;
//								}
//							}
//							//更新用户的通讯录
//							ChatIndexService.clearMailListCache(otheruid);
//						} catch (Exception e) {
//							log.error("", e);
//						}
//					}
//				}
//				//初始化统计数据
//				WxGroupMeta meta = new WxGroupMeta();
//				meta.setGroupid(groupid);
//				meta.setJoinnum(joinnum);
//				meta.setAllactflag(Const.YesOrNo.NO);
//				boolean metaSave = updateMeta(meta);
//				if (!metaSave) {
//					return failRet("群统计数据初始化异常");
//				}
//				ChatService.me.updateItemJoinNum(joinnum, groupid, false);
//				retObj = RetUtils.okData(nickStr.substring(1)).set("joinnum", joinnum);
//				if (updateGroupName) {
//					retObj.set("nameupdate", Const.YesOrNo.YES);
//				}
//				retObj.set("msgclear", msgClear);
//				return true;
//			}
//		};
//		boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
//		if (!init) {
//			return atom.getRetObj();
//		}
//		if (CollectionUtil.isNotEmpty(resetBindList)) {
//			atom.getRetObj().set("rebind", resetBindList);
//		}
//		//		boolean msgclear = RetUtils.getOkTData(atom.getRetObj(), "msgclear");
//		//		if(msgclear) {
//		//			ChatIndexService.clearGroupMsgCache(groupid);
//		//		}
//		ChatIndexService.removeGroupCache(groupid);
//		ChatIndexService.clearGroupUserListCache(groupid);
//		return atom.getRetObj();
//	}
    public Ret joinGroup(User curr, Long groupid, String uids, Integer applyuid, boolean dealapply) throws Exception {

        if (StrUtil.isBlank(uids)) {
            return RetUtils.invalidParam();
        }
//		String[] uidList = StrUtil.splitToArray(uids, ",");
//		if (uidList.length > 0) {
//			for (String uid : uidList) {
//				if (uid.isEmpty()) {
//					continue;
//				}
//				Ret ret = FriendService.me.isFriend(curr, Integer.valueOf(uid));
//				if (ret.get("data").equals(Const.YesOrNo.NO)) {
//					return RetUtils.failMsg("非好友不可拉入群聊");
//				}
//			}
//		}
        Integer operUid = curr.getId();
        if (applyuid != null) {
            operUid = applyuid;
        }
        Ret checkRet = checkJoinGroup(operUid, groupid, null, null, dealapply, StrUtil.splitToArray(uids, ",").length);
        if (checkRet.isFail()) {
            return checkRet;
        }
        WxGroup group = getByGroupid(groupid);
        String[] uidArr = StrUtil.splitToArray(uids, ",");
        if (uidArr.length == 1) {
            int otheruid = Integer.parseInt(uidArr[0]);
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
            if (ChatService.groupChatLink(groupItem)) {
                return RetUtils.failMsg("用户已在群组中");
            }
        }
        if (uidArr.length > 50) {
            return RetUtils.failMsg("一次邀请不要超过50哦");
        }
        List<WxChatGroupItem> resetBindList = new ArrayList<WxChatGroupItem>();
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                short joinnum = 0;
                String nickStr = "";
                String groupname = group.getName();
                boolean updateGroupName = false;
                boolean msgClear = false;
                //初始化其它用户信息
                for (String uidStr : uidArr) {
                    if (StrUtil.isNotBlank(uidStr)) {
                        try {
                            int otheruid = Integer.parseInt(uidStr);
                            User user = UserService.ME.getById(otheruid);
                            if (user == null || Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
                                log.error("邀请用户不存在：uid:{},groupid:{}", otheruid, groupid);
                                continue;
                            }
                            nickStr += "、" + user.getNick();
                            WxGroupUser otherUser = new WxGroupUser();
                            otherUser.setGroupid(groupid);
                            otherUser.setUid(otheruid);
                            otherUser.setSrcnick(user.getNick());
                            otherUser.setAutoflag(Const.YesOrNo.YES);
                            otherUser.setGroupnick(user.getNick());
                            otherUser.setGroupavator(user.getAvatar());
                            int otherSave = otherUser.ignoreSave();
                            if (otherSave <= 0) {
                                //其它用户保存失败
                                log.error("已存在群用户：uid:{},groupid:{}", otheruid, groupid);
                                continue;
                            }
                            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
                            if (groupItem != null) {//重新绑定
                                //绑定索引
                                ChatIndexService.me.chatUserIndexUpdate(otheruid, groupid, Const.ChatMode.GROUP, Const.YesOrNo.YES, otherUser.getId(), null, null, null, null);
                                if (groupItem.getChatlinkid() != null) {
                                    ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
                                    //更新会话状态
                                    ChatService.me.updateChatItemStatus(groupItem.getChatlinkid(), groupItem.getChatlinkmetaid(), Const.YesOrNo.YES, null, otherUser.getId(),
                                            group.getJoinnum(), groupid, group.getName(), group.getAvatar(), null);
                                    WxChatItems chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
                                    ChatMsgService.me.rebindGroupMsgDeal(chatItems);
                                    msgClear = true;
                                } else {
                                    ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
                                }
                                resetBindList.add(groupItem);
                            } else {
                                ChatIndexService.me.chatUserInit(otheruid, Const.ChatMode.GROUP, groupid, null, null, null, null, otherUser.getId(), Const.YesOrNo.YES, null);
                                ChatIndexService.me.chatGroupInit(otheruid, groupid, null, null, otherUser.getId(), Const.YesOrNo.YES, Const.GroupRole.MEMBER, null,
                                        Const.YesOrNo.YES);
                            }
                            joinnum++;
                            if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES)) {
                                String newName = groupname + "、" + user.getNick();
                                if (newName.length() <= 30) {
                                    updateGroupName = true;
                                }
                            }
                            //更新用户的通讯录
                            ChatIndexService.clearMailListCache(otheruid);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
                //初始化统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setJoinnum(joinnum);
                meta.setAllactflag(Const.YesOrNo.NO);
                boolean metaSave = updateMeta(meta);
                if (!metaSave) {
                    return failRet("群统计数据初始化异常");
                }
                ChatService.me.updateItemJoinNum(joinnum, groupid, false);
                retObj = RetUtils.okData(nickStr.substring(1)).set("joinnum", joinnum);
                if (updateGroupName) {
                    retObj.set("nameupdate", Const.YesOrNo.YES);
                }
                retObj.set("msgclear", msgClear);
                return true;
            }
        };
        boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!init) {
            return atom.getRetObj();
        }
        if (CollectionUtil.isNotEmpty(resetBindList)) {
            atom.getRetObj().set("rebind", resetBindList);
        }
        //		boolean msgclear = RetUtils.getOkTData(atom.getRetObj(), "msgclear");
        //		if(msgclear) {
        //			ChatIndexService.clearGroupMsgCache(groupid);
        //		}
        ChatIndexService.removeGroupCache(groupid);
        ChatIndexService.clearGroupUserListCache(groupid);
        return atom.getRetObj();
    }


    public Ret joinGroupByRegister(User curr, Long groupid, String uids, Integer applyuid, boolean dealapply) throws Exception {

        if (StrUtil.isBlank(uids)) {
            return RetUtils.invalidParam();
        }
//		String[] uidList = StrUtil.splitToArray(uids, ",");
//		if (uidList.length > 0) {
//			for (String uid : uidList) {
//				if (uid.isEmpty()) {
//					continue;
//				}
//				Ret ret = FriendService.me.isFriend(curr, Integer.valueOf(uid));
//				if (ret.get("data").equals(Const.YesOrNo.NO)) {
//					return RetUtils.failMsg("非好友不可拉入群聊");
//				}
//			}
//		}
        Integer operUid = curr.getId();
        if (applyuid != null) {
            operUid = applyuid;
        }
//		Ret checkRet = checkJoinGroup(operUid, groupid, null, null, dealapply, StrUtil.splitToArray(uids, ",").length);
//		if (checkRet.isFail()) {
//			return checkRet;
//		}
        WxGroup group = getByGroupid(groupid);
        String[] uidArr = StrUtil.splitToArray(uids, ",");
        if (uidArr.length == 1) {
            int otheruid = Integer.parseInt(uidArr[0]);
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
            if (ChatService.groupChatLink(groupItem)) {
                return RetUtils.failMsg("用户已在群组中");
            }
        }
        if (uidArr.length > 50) {
            return RetUtils.failMsg("一次邀请不要超过50哦");
        }
        List<WxChatGroupItem> resetBindList = new ArrayList<WxChatGroupItem>();
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                short joinnum = 0;
                String nickStr = "";
                String groupname = group.getName();
                boolean updateGroupName = false;
                boolean msgClear = false;
                //初始化其它用户信息
                for (String uidStr : uidArr) {
                    if (StrUtil.isNotBlank(uidStr)) {
                        try {
                            int otheruid = Integer.parseInt(uidStr);
                            User user = UserService.ME.getById(otheruid);
                            if (user == null || Objects.equals(user.getStatus(), User.Status.LOGOUT)) {
                                log.error("邀请用户不存在：uid:{},groupid:{}", otheruid, groupid);
                                continue;
                            }
                            nickStr += "、" + user.getNick();
                            WxGroupUser otherUser = new WxGroupUser();
                            otherUser.setGroupid(groupid);
                            otherUser.setUid(otheruid);
                            otherUser.setSrcnick(user.getNick());
                            otherUser.setAutoflag(Const.YesOrNo.YES);
                            otherUser.setGroupnick(user.getNick());
                            otherUser.setGroupavator(user.getAvatar());
                            int otherSave = otherUser.ignoreSave();
                            if (otherSave <= 0) {
                                //其它用户保存失败
                                log.error("已存在群用户：uid:{},groupid:{}", otheruid, groupid);
                                continue;
                            }
                            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
                            if (groupItem != null) {//重新绑定
                                //绑定索引
                                ChatIndexService.me.chatUserIndexUpdate(otheruid, groupid, Const.ChatMode.GROUP, Const.YesOrNo.YES, otherUser.getId(), null, null, null, null);
                                if (groupItem.getChatlinkid() != null) {
                                    ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
                                    //更新会话状态
                                    ChatService.me.updateChatItemStatus(groupItem.getChatlinkid(), groupItem.getChatlinkmetaid(), Const.YesOrNo.YES, null, otherUser.getId(),
                                            group.getJoinnum(), groupid, group.getName(), group.getAvatar(), null);
                                    WxChatItems chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
                                    ChatMsgService.me.rebindGroupMsgDeal(chatItems);
                                    msgClear = true;
                                } else {
                                    ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.YES, null, otherUser.getId(), Const.YesOrNo.YES);
                                }
                                resetBindList.add(groupItem);
                            } else {
                                ChatIndexService.me.chatUserInit(otheruid, Const.ChatMode.GROUP, groupid, null, null, null, null, otherUser.getId(), Const.YesOrNo.YES, null);
                                ChatIndexService.me.chatGroupInit(otheruid, groupid, null, null, otherUser.getId(), Const.YesOrNo.YES, Const.GroupRole.MEMBER, null,
                                        Const.YesOrNo.YES);
                            }
                            joinnum++;
                            if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES)) {
                                String newName = groupname + "、" + user.getNick();
                                if (newName.length() <= 30) {
                                    updateGroupName = true;
                                }
                            }
                            //更新用户的通讯录
                            ChatIndexService.clearMailListCache(otheruid);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
                //初始化统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setJoinnum(joinnum);
                meta.setAllactflag(Const.YesOrNo.NO);
                boolean metaSave = updateMeta(meta);
                if (!metaSave) {
                    return failRet("群统计数据初始化异常");
                }
                ChatService.me.updateItemJoinNum(joinnum, groupid, false);
                retObj = RetUtils.okData(nickStr.substring(1)).set("joinnum", joinnum);
                if (updateGroupName) {
                    retObj.set("nameupdate", Const.YesOrNo.YES);
                }
                retObj.set("msgclear", msgClear);
                return true;
            }
        };
        boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!init) {
            return atom.getRetObj();
        }
        if (CollectionUtil.isNotEmpty(resetBindList)) {
            atom.getRetObj().set("rebind", resetBindList);
        }
        //		boolean msgclear = RetUtils.getOkTData(atom.getRetObj(), "msgclear");
        //		if(msgclear) {
        //			ChatIndexService.clearGroupMsgCache(groupid);
        //		}
        ChatIndexService.removeGroupCache(groupid);
        ChatIndexService.clearGroupUserListCache(groupid);
        return atom.getRetObj();
    }

    /*********************************************end-调整-*********************************************************/

    /**
     * 修改头像-已调整
     *
     * @param groupid
     * @param avatar
     * @return
     * @author lixinji
     * 2020年3月13日 下午1:45:03
     */
    public Ret modifyAvatar(Long groupid, String avatar, boolean isAuto) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }

        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                WxGroup group = new WxGroup();
                group.setId(wxGroup.getId());
                group.setAvatar(avatar);
                if (!isAuto) {
                    group.setAvatarautoflag(Const.YesOrNo.NO);
                }
                boolean update = group.update();
                if (!update) {
                    return failRet("修改失败");
                }
                ChatService.me.updateChatItemInfo(groupid, "", avatar);
                return true;

            }
        };
        boolean commit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!commit) {
            return atom.getRetObj();
        }
        ChatIndexService.removeGroupCache(groupid);
        return RetUtils.okOper();
    }

    /**
     * 修改群提醒-已调整
     *
     * @param groupid
     * @param nick
     * @param uid
     * @return
     * @author lixinji
     * 2020年4月8日 上午11:37:09
     */
    public Ret modifyGroupPush(Long groupid, Short freeflag, Integer uid) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem chatGroupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(chatGroupItem)) {
            return RetUtils.failMsg("不是群成员");
        }
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                WxGroupUser update = new WxGroupUser();
                update.setId(chatGroupItem.getGpulinkid());
                update.setMsgfreeflag(freeflag);
                boolean up = update.update();
                if (!up) {
                    return failRet("操作失败");
                }
                if (chatGroupItem.getChatlinkid() != null) {
                    ChatService.me.updateChatMsgFreeFlag(freeflag, chatGroupItem.getChatlinkid());
                    return okRet(RetUtils.okData(Const.YesOrNo.YES).set("chatlinkid", chatGroupItem.getChatlinkid()));
                }
                return okRet(Const.YesOrNo.NO);
            }
        };
        boolean oper = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!oper) {
            return atom.getRetObj();
        }
        ChatIndexService.clearGroupUserCache(chatGroupItem.getGpulinkid());
        return atom.getRetObj();
    }

    /**
     * 修改群昵称-已调整
     *
     * @param groupid
     * @param nick
     * @param uid
     * @return
     * @author lixinji
     * 2020年4月8日 上午11:37:09
     */
    public Ret modifyGroupNick(Long groupid, String nick, Integer uid) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem chatGroupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(chatGroupItem)) {
            return RetUtils.failMsg("不是群成员");
        }
        WxGroupUser update = new WxGroupUser();
        update.setId(chatGroupItem.getGpulinkid());
        if (StrUtil.isBlank(nick)) {
            User user = UserService.ME.getById(uid);
            update.setSrcnick(user.getNick());
            update.setGroupnick(user.getNick());
            update.setAutoflag(Const.YesOrNo.YES);
            nick = user.getNick();
        } else {
            update.setAutoflag(Const.YesOrNo.NO);
            update.setGroupnick(nick);
        }
        boolean up = update.update();
        if (!up) {
            return RetUtils.failOper();
        }
        ChatIndexService.clearGroupUserListCache(groupid);
        ChatIndexService.clearGroupUserCache(chatGroupItem.getGpulinkid());
        return RetUtils.okData(nick);
    }

    /**
     * 修改群名称-手动-已调整
     *
     * @param uid
     * @param groupid
     * @param name
     * @return
     * @author lixinji
     * 2020年3月13日 下午5:42:54
     */
    public Ret modifyName(Integer uid, Long groupid, String name) {
        return modifyName(uid, groupid, name, false);
    }

    /**
     * 修改上传群头像
     *
     * @param uid
     * @param groupid
     * @param name
     * @return
     * @author lixinji
     * 2021年1月13日 下午7:01:35
     */
    public Ret modifyUploadAvatar(Integer uid, Long groupid, String avatar) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改群头像");
        }
        return modifyAvatar(groupid, avatar, false);
    }

    /**
     * 修改名称-已调整
     *
     * @param groupid
     * @param name
     * @return
     * @author lixinji
     * 2020年3月13日 下午2:17:03
     */
    public Ret modifyName(Integer uid, Long groupid, String name, boolean auto) {
        if (!auto) {
            WxGroup wxGroup = this.getByGroupid(groupid);
            if (wxGroup == null) {
                return RetUtils.failMsg("本群已解散");
            }
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
            if (!ChatService.groupChatLink(groupItem)) {
                return RetUtils.failMsg("不是群用户");
            }
            if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
                return RetUtils.failMsg("只有管理员才能修改群名字");
            }
        }
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                WxGroup group = new WxGroup();
                group.setId(groupid);
                if (!auto) {
                    group.setAutoflag(Const.YesOrNo.NO);
                }
                group.setName(name);
                boolean update = group.update();
                if (!update) {
                    return failRet("修改失败");
                }
                ChatService.me.updateChatItemInfo(groupid, name, "");
                return true;

            }
        };
        boolean commit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!commit) {
            return atom.getRetObj();
        }
        ChatIndexService.removeGroupCache(groupid);
        return RetUtils.okOper();
    }

    /**
     * 撤回消息-已调整
     *
     * @param chatUserItem
     * @param msg
     * @return
     * @author lixinji
     * 2020年3月11日 下午5:28:12
     */
    public Ret backMsg(WxChatGroupItem groupItem, String midStr) {
        Long mid = Long.parseLong(midStr);
        WxGroupMsg msg = WxGroupMsg.dao.findById(mid);
        if (msg == null) {
            return RetUtils.failMsg("消息不存在");
        }
//		if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
//			long offset = DateUtil.betweenMs(msg.getTime(), new Date());
//			if (offset > ChatMsgService.MSG_BACK_MAX_TIME) {
//				return RetUtils.failMsg("超过2分钟,无法撤回");
//			}
//		} else {
        if (!Objects.equals(msg.getUid(), groupItem.getUid())) {
            WxChatGroupItem msgOwner = ChatIndexService.chatGroupIndex(msg.getUid(), groupItem.getGroupid());
            if (ChatService.groupChatLink(msgOwner) && !Objects.equals(msgOwner.getGrouprole(), Const.GroupRole.MEMBER)) {
                return RetUtils.failMsg("没有权限撤回管理员消息");
            }
        }
//		}
        boolean del = delGroupMsg(msg.getId());
        if (!del) {
            return RetUtils.sysError();
        }
        return Ret.ok().set("chatindex", groupItem).set("msg", msg);
    }

    /**
     * @param id
     * @return
     * @author lixinji
     * 2022年3月4日 下午7:10:13
     */
    public boolean delGroupMsg(Long id) {
        Kv params = Kv.by("id", id);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.delGroupMsgBak", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return WxGroupMsg.dao.deleteById(id);
    }

    /**
     * 群删除消息-已调整
     *
     * @param chatUserItem
     * @param midStr
     * @return
     * @author lixinji
     * 2020年4月1日 上午10:02:55
     */
    public Ret delMsg(WxChatGroupItem groupItem, String mids) {
        if (StrUtil.isBlank(mids)) {
            return RetUtils.failMsg("消息id为空");
        }
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                Ret ret = ChatMsgService.me.groupMsgDel(mids, groupItem.getUid(), groupItem.getGroupid());
                if (ret.isFail()) {
                    return failRet(ret);
                }
                Short clear = Const.YesOrNo.NO;
                Short chat = Const.YesOrNo.NO;
                if (groupItem.getChatlinkid() != null) {
                    ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
                    WxChatItems chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
                    if (chatItems != null && chatItems.getLastmsgid() != null) {
                        if (("," + mids + ",").indexOf("," + chatItems.getLastmsgid() + ",") >= 0) {
                            WxGroupMsg msg = getGroupFristMsg(chatItems);
                            if (msg == null) {
                                boolean startTx = ChatIndexService.me.chatGroupStartMsg(chatItems.getUid(), chatItems.getBizid(), null);
                                if (!startTx) {
                                    return failRet("修改起始消息异常");
                                }
                                ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + chatItems.getBizid(),
                                        WxChatItemsMeta.class);
                                WriteLock writeLock = rwLock.writeLock();
                                writeLock.lock();
                                try {
                                    boolean clearMsg = ChatService.me.clearChatItemMsg(groupItem.getChatlinkmetaid(), groupItem.getGroupid());
                                    if (!clearMsg) {
                                        return failRet("清空消息异常");
                                    }
                                    clear = Const.YesOrNo.YES;
                                } catch (Exception e) {
                                    log.error("", e);
                                } finally {
                                    writeLock.unlock();
                                }
                                WxGroupMeta meta = new WxGroupMeta();
                                meta.setGroupid(groupItem.getGroupid());
                                meta.setAllstartflag(Const.YesOrNo.NO);
                                boolean metaUpdate = updateMeta(meta);
                                if (!metaUpdate) {
                                    return failRet("群统计数据修改异常");
                                }
                                ChatIndexService.removeGroupCache(groupItem.getGroupid());
                            } else {
                                Ret itemRet = ChatMsgService.me.afterSendGroupById(msg, null, groupItem.getChatlinkmetaid(), null);
                                if (itemRet.isFail()) {
                                    return failRet(itemRet);
                                }
                                chat = Const.YesOrNo.YES;
                            }
                        }
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
        WxChatItems chatItems = null;
        if (Objects.equals(clear, Const.YesOrNo.YES)) {
            // 清除索引
            ChatIndexService.clearChatUserIndex(groupItem.getUid(), groupItem.getGroupid(), Const.ChatMode.GROUP);
            // 移除会话缓存
            ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
            chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
        } else if (Objects.equals(chat, Const.YesOrNo.YES)) {
            // 移除会话缓存
            ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
            chatItems = ChatService.me.getAllChatItems(groupItem.getChatlinkid());
        }
        ChatIndexService.clearGroupMsgCache(groupItem.getGroupid());
        if (chatItems != null) {
            atom.getRetObj().set("chatItems", chatItems);
        }
        return atom.getRetObj();
    }

    /**
     * 群用户-已调整
     *
     * @param uid
     * @param groupid
     * @return
     * @author lixinji
     * 2020年2月22日 上午9:41:39
     */
    public WxGroupUser getGroupUser(Integer uid, Long groupid) {
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        return getGroupUser(groupItem);
    }

    /**
     * 群用户-已调整
     *
     * @param groupItem
     * @return
     * @author lixinji
     * 2020年2月22日 上午9:42:03
     */
    public WxGroupUser getGroupUser(WxChatGroupItem groupItem) {
        if (groupItem == null || groupItem.getGpulinkid() == null) {
            return null;
        }
        return getGroupUser(groupItem.getGpulinkid());
    }

    /**
     * 修改群简介
     *
     * @param curr
     * @param groupid
     * @param intro
     * @return
     */
    public Resp modifyIntro(User curr, Long groupid, String intro) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Resp.fail("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.fail("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Resp.fail("只有管理员才能修改群简介");
        }
        if (StrUtil.isBlank(intro)) {
            wxGroup.setIntro("");
        } else {
            wxGroup.setIntro(intro);
        }
        boolean f = wxGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Resp.ok();
        } else {
            return Resp.fail("操作失败");
        }
    }

    /**
     * 修改群公告
     *
     * @param curr
     * @param groupid
     * @param notice
     * @return
     */
    public Resp modifyNotice(User curr, Long groupid, String notice, Integer isTop) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Resp.fail("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.fail("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Resp.fail("只有管理员才能修改群公告");
        }
        if (StrUtil.isBlank(notice)) {
            wxGroup.setNotice("");
            wxGroup.setNoticetime(null);
        } else {
            wxGroup.setNotice(notice);
            wxGroup.setNoticetime(new Date());
        }
        boolean f = wxGroup.update();
        if (f) {
            WxGroupNotice wxGroupNotice = new WxGroupNotice();
            wxGroupNotice.setGroupid(groupid);
            wxGroupNotice.setContent(notice);
            wxGroupNotice.setIstop(isTop);
            if (isTop.equals(1)) {
                wxGroupNotice.setToptime(new Date());
            }
            wxGroupNotice.setCreatetime(new Date());
            wxGroupNotice.save();
            ChatIndexService.removeGroupCache(groupid);
            return Resp.ok();
        } else {
            return Resp.fail("操作失败");
        }
    }

    /**
     * 修改群公告
     *
     * @param curr
     * @param groupid
     * @param notice
     * @return
     */
    public Resp updateNotice(User curr, Long groupid, Integer noticeId, String notice, Integer isTop) {
//		WxGroup wxGroup = this.getByGroupid(groupid);
//		if (wxGroup == null) {
//			return Resp.fail("本群已解散");
//		}
//		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
//		if (!ChatService.groupChatLink(groupItem)) {
//			return Resp.fail("不是群用户");
//		}
//		if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
//			return Resp.fail("只有管理员才能修改群公告");
//		}
        WxGroupNotice wxGroupNotice = WxGroupNotice.dao.findById(noticeId);
        if (wxGroupNotice == null) {
            return Resp.fail("公告不存在");
        }
        if (!wxGroupNotice.getContent().equals(notice)) {
            wxGroupNotice.setContent(notice);
        }
        if (isTop == null) {
            isTop = wxGroupNotice.getIstop();
        }
        wxGroupNotice.setIstop(isTop);
        if (isTop.equals(1)) {
            wxGroupNotice.setToptime(new Date());
        } else {
            wxGroupNotice.setToptime(null);
        }
        wxGroupNotice.setUpdatetime(new Date());
        boolean update1 = wxGroupNotice.update();
        ChatIndexService.removeGroupCache(groupid);
        return Resp.ok();

    }


    /**
     * 删除历史群公告
     *
     * @param curr
     * @param groupid
     * @return
     */
    public Resp delNotice(User curr, Long groupid, String noticeid) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Resp.fail("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.fail("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Resp.fail("只有管理员才能删除群历史公告");
        }
        String[] noticeIdList = noticeid.split(",");
        WxGroupNotice notice = new WxGroupNotice();
        for (String nid : noticeIdList) {
            notice = WxGroupNotice.dao.findById(nid);
            boolean f = WxGroupNotice.dao.deleteById(nid);
            if (!f) {
                log.error("/group/delNotice 删除群公告接口 groupid:{}, noticeid:{}, 删除失败", groupid, nid);
                return Resp.fail("删除公告失败，请重试或联系客服");
            }
        }
        Resp ok = Resp.ok();
        ok.setData(notice.getContent());
        return ok;
    }


    /**
     * 签到
     *
     * @param curr
     * @param groupid
     * @return
     */
    public Resp sign(User curr, Long groupid) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Resp.fail("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.fail("不是群用户");
        }
        WxGroupSign sign = WxGroupSign.dao.findFirst("select * from wx_group_sign where uid = ? and groupid = ? and date(signtime)=date(now())", curr.getId(), groupid);
        if (sign != null) {
            return Resp.fail("已签到");
        }
        WxGroupSign wxGroupSign = new WxGroupSign();
        wxGroupSign.setGroupid(groupid);
        wxGroupSign.setUid(curr.getId());
        wxGroupSign.setSigntime(new Date());
        boolean f = wxGroupSign.save();
        if (f) {
            return Resp.ok();
        } else {
            return Resp.fail("操作失败");
        }
    }

    public Ret userSignInfo(User curr, Long groupid, int year, int month) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Ret.fail("errormsg", "本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Ret.fail("errormsg", "不是群用户");
        }
        List<WxGroupSign> wxGroupSigns = WxGroupSign.dao.find("select * from wx_group_sign where groupid = ? and uid = ? and year(signtime) = ? and month(signtime) = ? order by signtime desc", groupid, curr.getId(), year, month);
//		int currSignDay = 0;
        int signDayCount = 0;
//		boolean isContinue = true;
//		int lastDay = 0;
        if (wxGroupSigns != null) {
            signDayCount = wxGroupSigns.size();
            for (int i = 0; i < wxGroupSigns.size(); i++) {

                WxGroupSign wxGroupSign = wxGroupSigns.get(i);
                Calendar instance = Calendar.getInstance();
                instance.setTime(wxGroupSign.getSigntime());
                wxGroupSign.setDay(instance.get(Calendar.DAY_OF_MONTH));
                wxGroupSign.setMonth(instance.get(Calendar.MONTH) + 1);
                wxGroupSign.setTimeStamp(wxGroupSign.getSigntime().getTime());
//
//				if (i == 0) {
//					lastDay = instance.get(Calendar.DAY_OF_MONTH);
//					Calendar currInstance = Calendar.getInstance();
//					currInstance.setTime(new Date());
//					if (instance.get(Calendar.YEAR) == currInstance.get(Calendar.YEAR)
//							&& instance.get(Calendar.MONTH) == currInstance.get(Calendar.MONTH)
//							&& instance.get(Calendar.DAY_OF_MONTH) == currInstance.get(Calendar.DAY_OF_MONTH)) {
//						currSignDay ++;
//					}
//				} else {
//
//				}
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("signCount", signDayCount);
        data.put("signInfo", wxGroupSigns);

        return Ret.ok("data", data);
    }

    /**
     * 删除历史群公告
     *
     * @param curr
     * @param groupid
     * @return
     */
    public Ret signInfo(User curr, Long groupid, Integer uid, Integer year, Integer month, Integer day) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Ret.fail("errormsg", "本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Ret.fail("errormsg", "不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Ret.fail("errormsg", "只有管理员才能查看该信息");
        }
        Kv params = Kv.by("groupid", groupid);
        if (uid != null) {
            params.set("uid", uid);
        }
        if (year != null) {
            params.set("year", year);
        }
        if (month != null) {
            params.set("month", month);
        }
        if (day != null) {
            params.set("day", day);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.signInfo", params);
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
        for (Record record : records) {
            Object signtime = record.get("signtime");
            Calendar instance = Calendar.getInstance();
            instance.setTime((Date) signtime);
            record.set("year", instance.get(Calendar.YEAR));
            record.set("month", instance.get(Calendar.MONTH) + 1);
            record.set("day", instance.get(Calendar.DAY_OF_MONTH));
            record.set("timeStamp", instance.getTime().getTime());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("signInfo", records);
        if (uid != null) {
            data.put("signCount", records.size());
            User user = User.dao.findById(uid);
            data.put("nickname", user.getNick());
            data.put("avatar", user.getAvatar());
        }/* else {
			for (Record record:records) {
				WxGroupSign.dao.find("select * from wx_group_sign where uid = ?",record.get("uid"))
			}
		}*/
//		List<WxGroupSign> wxGroupSigns;
//		if (uid != null)
//			wxGroupSigns = WxGroupSign.dao.find("select * from wx_group_sign where groupid = ? and uid = ? and year(signtime) = ? and month(signtime) = ? and day(signtime) = ? order by signtime desc", groupid, uid, year, month, day);
//		else
//			wxGroupSigns = WxGroupSign.dao.find("select * from wx_group_sign where groupid = ? and year(signtime) = ? and month(signtime) = ? and day(signtime) = ? order by signtime desc", groupid, year, month, day);

        return Ret.ok("data", data);
    }

    public Ret signRecords(User curr, Long groupid, Integer year, Integer month) {
        if (groupid == null || year == null || month == null) {
            return Ret.fail("errormsg", "参数异常");
        }
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return Ret.fail("errormsg", "本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Ret.fail("errormsg", "不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Ret.fail("errormsg", "只有管理员才能查看该信息");
        }

        Kv params = Kv.by("groupid", groupid);

        if (year != null) {
            params.set("year", year);
        }
        if (month != null) {
            params.set("month", month);
        }

        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.signRecords", params);
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
//		for (Record record:records) {
//			Object signtime = record.get("signtime");
//			Calendar instance = Calendar.getInstance();
//			instance.setTime((Date) signtime);
//			record.set("year", instance.get(Calendar.YEAR));
//			record.set("month", instance.get(Calendar.MONTH) + 1);
//			record.set("day", instance.get(Calendar.DAY_OF_MONTH));
//			record.set("timeStamp", instance.getTime().getTime());
//		}
        Map<String, Object> data = new HashMap<>();
        data.put("signInfo", records);
/*		if (uid != null) {
			data.put("signCount", records.size());
			User user = User.dao.findById(uid);
			data.put("nickname", user.getNick());
			data.put("avatar", user.getAvatar());
		}*//* else {
			for (Record record:records) {
				WxGroupSign.dao.find("select * from wx_group_sign where uid = ?",record.get("uid"))
			}
		}*/
//		List<WxGroupSign> wxGroupSigns;
//		if (uid != null)
//			wxGroupSigns = WxGroupSign.dao.find("select * from wx_group_sign where groupid = ? and uid = ? and year(signtime) = ? and month(signtime) = ? and day(signtime) = ? order by signtime desc", groupid, uid, year, month, day);
//		else
//			wxGroupSigns = WxGroupSign.dao.find("select * from wx_group_sign where groupid = ? and year(signtime) = ? and month(signtime) = ? and day(signtime) = ? order by signtime desc", groupid, year, month, day);

        return Ret.ok("data", data);
    }

    /**
     * 群用户-已调整
     *
     * @param linkid
     * @return
     * @author lixinji
     * 2020年2月22日 上午9:42:32
     */
    public WxGroupUser getGroupUser(Long linkid) {
        String key = linkid + "";
        ICache cache = Caches.getCache(CacheConfig.WX_GROUP_USER_2);
        WxGroupUser record = CacheUtils.get(cache, key, true, new FirsthandCreater<WxGroupUser>() {
            @Override
            public WxGroupUser create() {
                return WxGroupUser.dao.findById(linkid);
            }
        });
        return record;
    }

    /**
     * 获取群用户列表-推送专用-已调整
     *
     * @param groupid
     * @param pageNumber
     * @return
     * @author lixinji
     * 2020年9月24日 上午11:42:07
     */
    public List<Record> groupUserToPush(Long groupid) {
        Kv params = Kv.by("groupid", groupid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.groupUserToPush", params);
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
        return records;
    }

    /**
     * 转让群-已调整
     *
     * @param uid
     * @param otherUid
     * @param groupid
     * @return
     * @author lixinji
     * 2020年2月24日 下午2:34:13
     */
    public Ret changeOwner(Integer uid, Integer otherUid, Long groupid) {
        WxGroup wxGroup = getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.noExistParam();
        }
        if (!Objects.equals(wxGroup.getUid(), uid)) {
            return RetUtils.grantError();
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("群主无效");
        }
        WxChatGroupItem otherGroupItem = ChatIndexService.chatGroupIndex(otherUid, groupid);
        if (!ChatService.groupChatLink(otherGroupItem)) {
            return RetUtils.failMsg("转让用户不是群有效用户");
        }
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                //基本数据处理
                WxGroup updateGroup = new WxGroup();
                updateGroup.setId(groupid);
                updateGroup.setUid(otherUid);
                boolean update = updateGroup.update();
                if (!update) {
                    return failRet("群转让失败");
                }
                ChatIndexService.me.chatGroupIndexUpdate(otherUid, groupid, null, Const.GroupRole.OWNER, null, null);
                ChatIndexService.me.chatGroupIndexUpdate(uid, groupid, null, Const.GroupRole.MEMBER, null, null);
                WxGroupUser memberUser = new WxGroupUser();
                memberUser.setGrouprole(Const.GroupRole.MEMBER);
                memberUser.setId(groupItem.getGpulinkid());
                boolean memberUpdate = updateGroupUser(memberUser);
                if (!memberUpdate) {
                    return failRet("群角色修改失败");
                }
                WxGroupUser ownerUser = new WxGroupUser();
                ownerUser.setGrouprole(Const.GroupRole.OWNER);
                ownerUser.setId(otherGroupItem.getGpulinkid());
                boolean ownerUpdate = updateGroupUser(ownerUser);
                if (!ownerUpdate) {
                    return failRet("群角色修改失败");
                }
                //修改统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setTransfercount((short) 1);
                updateMeta(meta);
                if (groupItem.getChatlinkid() != null) {
                    //更新会话
                    WxChatItems chatItems = new WxChatItems();
                    chatItems.setId(groupItem.getChatlinkid());
                    chatItems.setBizrole(Const.GroupRole.MEMBER);
                    chatItems.update();
                }
                //此处缺少会话级别的role处理
                if (otherGroupItem.getChatlinkid() != null) {
                    WxChatItems chatItems = new WxChatItems();
                    chatItems.setId(otherGroupItem.getChatlinkid());
                    chatItems.setBizrole(Const.GroupRole.OWNER);
                    chatItems.update();
                }
                return true;
            }
        };
        boolean del = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!del) {
            return atom.getRetObj();
        }
        //此处群组缓存直接进行修改，不需要进行清除
        otherGroupItem.setGrouprole(Const.GroupRole.OWNER);
        groupItem.setGrouprole(Const.GroupRole.MEMBER);
        ChatIndexService.removeUserCache(uid, groupid, Const.ChatMode.GROUP);
        ChatIndexService.removeUserCache(otherUid, groupid, Const.ChatMode.GROUP);
        ChatIndexService.removeGroupCache(groupid);
        if (otherGroupItem.getChatlinkid() != null) {
            ChatIndexService.removeChatItemsCache(otherGroupItem.getChatlinkid());
        }
        if (groupItem.getChatlinkid() != null) {
            ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
        }
        ChatIndexService.clearGroupUserListCache(groupid);
        ChatIndexService.clearGroupUserCache(otherGroupItem.getGpulinkid());
        ChatIndexService.clearGroupUserCache(groupItem.getGpulinkid());
        return Ret.ok().set("other", otherGroupItem).set("owner", groupItem);
    }

    /**
     * 解散群-已调整
     * 1、需要新增自己的操作通知
     * 2、需要新增删除会话后的用户通知-同步通讯录的群列表
     * 解决：
     * 1、删除群直接删除群
     * 2、所有用户需要发送同步群列表
     * 3、激活用户需要发送删除消息
     *
     * @param curr
     * @param group
     * @param uids
     * @param nicks
     * @param devicetype
     * @param sessionid
     * @param ip
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月24日 上午9:02:13
     */
    public Ret delGroup(User curr, Long groupid) throws Exception {
        WxGroup group = GroupService.me.getByGroupid(groupid);
        if (group == null) {
            return RetUtils.noExistParam();
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem) || !Objects.equals(groupItem.getGrouprole(), Const.GroupRole.OWNER)) {
            return RetUtils.grantError();
        }
        WxGroupUser groupUser = getGroupUser(groupItem.getGpulinkid());
        Integer uid = curr.getId();
        Short chatmode = Const.ChatMode.GROUP;
        Long chatlinkid = groupItem.getChatlinkid();
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                Ret del = delGroupInfo(groupid);
                if (del.isFail()) {
                    return failRet("群删除异常");
                }
                //群主会话删除
                if (chatlinkid != null) {
                    boolean remove = ChatService.me.deleteChatItem(chatlinkid);
                    if (!remove) {
                        return failRet("清除聊天列表失败");
                    }
                }
                delGroupUser(groupItem.getGpulinkid());
                //群主索引删除
                ChatIndexService.me.chatGroupIndexDel(uid, groupid);
                ChatIndexService.me.chatUserIndexDel(uid, groupid, chatmode);
                return true;
            }
        };
        boolean del = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!del) {
            return atom.getRetObj();
        }
        //清除缓存
        if (chatlinkid != null) {
            ChatIndexService.removeChatItemsCache(chatlinkid);
        }
        ChatIndexService.clearChatUserIndex(uid, groupid, chatmode);
        ChatIndexService.removeGroupCache(groupid);
        return Ret.ok().set("groupitem", groupItem).set("group", group).set("groupuser", groupUser);
    }

    /**
     * 初始化群-后续可优化-已调整
     * 1、初始化群主得会话
     * 2、初始化群主得索引
     * 3、创建群信息
     * 4、创建群动态信息
     *
     * @param curr
     * @param wxGroup
     * @param uidList
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月12日 下午4:08:30
     */
    public static Ret createGroup(User curr, WxGroup group, String nicks, Short devicetype, String sessionid, String ip, short joinnum, String appversion) throws Exception {
        Integer uid = curr.getId();
        String name = group.getName();
        Resp resp = CommonUtils.checkGroupName(name, "群名");
        if (!resp.isOk()) {
            return RetUtils.failMsg(resp.getMsg());
        }
        String avatar = group.getAvatar();
        if (StrUtil.isBlank(avatar)) {
            group.setAvatar(WxGroup.DEFAULT_GROUP_AVATAR);
        }
        Integer maxNum = ConfService.getInt(Const.ConfMapping.WX_GROUP_MAX_JOIN_NUM, 1000);
        if (maxNum.shortValue() < joinnum) {
            return RetUtils.failMsg("群人数最大可邀请：" + maxNum + "人");
        }
        group.setMaximum(maxNum);
        group.setUid(uid);
        SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.create, nicks, "create");
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                //初始化基础信息
                boolean save = group.save();
                if (!save) {
                    return failRet("群基础信息初始化异常");
                }
                Long groupid = group.getId();
                WxGroupUser groupUser = new WxGroupUser();
                groupUser.setGroupid(groupid);
                groupUser.setUid(uid);
                groupUser.setAutoflag(Const.YesOrNo.YES);
                groupUser.setGroupnick(curr.getNick());
                groupUser.setSrcnick(curr.getNick());
                groupUser.setGroupavator(curr.getAvatar());
                groupUser.setGrouprole(Const.GroupRole.OWNER);
                boolean userSave = groupUser.save();
                if (!userSave) {
                    return failRet("群主数据初始化异常");
                }
                //初始化群主的信息
                WxGroupMsg groupMsg = GroupService.me.addSysMsg(devicetype, sessionid, sysMsgVo.toText(), uid, ip, groupid, "", sysMsgVo, groupUser, appversion);
                if (groupMsg == null) {
                    return failRet("消息保存失败");
                }
                WxChatItems chatItem = new WxChatItems();
                chatItem.setUid(uid);
                chatItem.setBizid(groupid);
                chatItem.setLinkid(groupUser.getId());
                chatItem.setChatmode(Const.ChatMode.GROUP);
                chatItem.setBizrole(groupUser.getGrouprole());
                chatItem.setAvatar(group.getAvatar());
                chatItem.setName(group.getName());
                chatItem.setJoinnum(joinnum);
                chatItem.setStartmsgid(groupMsg.getId());
                boolean chatsave = chatItem.save();
                if (!chatsave) {
                    return failRet("会话初始化异常");
                }
                WxChatItemsMeta itemmeta = new WxChatItemsMeta();
                itemmeta.setUid(uid);
                itemmeta.setBizid(groupid);
                itemmeta.setChatmode(Const.ChatMode.GROUP);
                itemmeta.setChatlinkid(chatItem.getId());
                itemmeta.setLastmsgid(groupMsg.getId());
                itemmeta.setLastmsguid(uid);
                itemmeta.setFromnick(curr.getNick());
                itemmeta.setSysflag(groupMsg.getSendbysys());
                itemmeta.setMsgresume(groupMsg.getResume());
                itemmeta.setMsgtype(groupMsg.getContenttype());
                itemmeta.setSendtime(groupMsg.getTime());
                itemmeta.setNotreadstartmsgid(groupMsg.getId());
                itemmeta.setNotreadcount(0);
                itemmeta.setChatuptime(new Date());
                boolean metasave = itemmeta.save();
                if (!metasave) {
                    return failRet("会话动态初始化异常");
                }
                ChatIndexService.clearMailListCache(uid);
                ChatIndexService.me.chatUserInit(uid, Const.ChatMode.GROUP, groupid, chatItem.getId(), itemmeta.getId(), groupUser.getId(), groupMsg.getId());
                ChatIndexService.me.chatGroupInit(uid, groupid, chatItem.getId(), itemmeta.getId(), groupUser.getId(), Const.GroupRole.OWNER, groupMsg.getId(), null);
                //初始化统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setJoinnum(joinnum);
                boolean metaSave = meta.save();
                if (!metaSave) {
                    return failRet("群统计数据初始化异常");
                }
                group.setJoinnum(joinnum);
                retObj = Ret.ok().set("owner", chatItem).set("msg", groupMsg);
                return true;
            }
        };
        boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!init) {
            return atom.getRetObj();
        }
        return atom.getRetObj().set("sysmsgvo", sysMsgVo);
    }

    /**
     * 踢人-已调整
     *
     * @param curr
     * @param groupid
     * @param uids
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午5:58:06
     */
    public Ret kickGroup(User curr, Long groupid, String uids) throws Exception {
        if (StrUtil.isBlank(uids)) {
            return RetUtils.invalidParam();
        }
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return RetUtils.noExistParam();
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        if (("," + uids + ",").indexOf(("," + curr.getId() + ",")) >= 0) {
            return RetUtils.failMsg("不能对自己操作");
        }
        String[] uidArr = StrUtil.splitToArray(uids, ",");
        Short mode = Const.ChatMode.GROUP;
        List<WxChatGroupItem> kickGroupItem = new ArrayList<WxChatGroupItem>();
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                short joinnum = 0;
                String nickStr = "";
                String groupname = group.getName();
                boolean updateGroupName = false;
                Short sigle = Const.YesOrNo.NO;
                //初始化其它用户信息
                for (String uidStr : uidArr) {
                    if (StrUtil.isNotBlank(uidStr)) {
                        try {
                            int otheruid = Integer.parseInt(uidStr);
                            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(otheruid, groupid);
                            if (!ChatService.groupChatLink(groupItem)) {
                                continue;
                            }
                            if (!Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
                                if (uidArr.length == 1) {
                                    return failRet("不能直接移除管理员");
                                }
                                continue;
                            }
                            kickGroupItem.add(groupItem);
                            joinnum--;
                            User user = UserService.ME.getById(otheruid);
                            if (user == null) {
                                log.error("被踢用户不存在：uid:{},groupid:{}", otheruid, groupid);
                                ChatIndexService.me.chatGroupIndexDel(otheruid, groupid);
                                ChatIndexService.me.chatUserIndexDel(otheruid, groupid, mode);
                                if (groupItem.getChatlinkid() != null) {
                                    ChatService.me.deleteChatItem(groupItem.getChatlinkid());
                                }
                                continue;
                            }
                            nickStr += "、" + user.getNick();
                            delGroupUser(groupItem.getGpulinkid());
                            if (groupItem.getChatlinkid() != null) {
                                ChatIndexService.me.chatGroupIndexUpdate(otheruid, groupid, Const.YesOrNo.NO, null, null, Const.YesOrNo.YES);
                                ChatIndexService.me.chatUserIndexUpdate(otheruid, groupid, mode, Const.YesOrNo.NO, null, null, null, null, null);
                                ChatService.me.updateChatItemStatus(groupItem.getChatlinkid(), groupItem.getChatlinkmetaid(), Const.YesOrNo.NO, null, null, null, groupid, null,
                                        null, null);
                            } else {
                                ChatIndexService.me.chatGroupIndexDel(otheruid, groupid);
                                ChatIndexService.me.chatUserIndexDel(otheruid, groupid, mode);
                            }
                            if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES)) {
                                if (("、" + groupname + "、").indexOf("、" + user.getNick() + "、") >= 0) {
                                    updateGroupName = true;
                                }
                            }
                            ChatIndexService.clearMailListCache(otheruid);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
                //初始化统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setJoinnum(joinnum);
                boolean metaSave = updateMeta(meta);
                if (!metaSave) {
                    return failRet("群统计数据初始化异常");
                }
                ChatService.me.updateItemJoinNum(joinnum, groupid, false);
                retObj = RetUtils.okData(nickStr.substring(1)).set("joinnum", joinnum).set("sigle", sigle);
                if (updateGroupName) {
                    retObj.set("nameupdate", Const.YesOrNo.YES);
                }
                return true;
            }
        };
        boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!init) {
            return atom.getRetObj();
        }
        ChatIndexService.clearMailListCache(curr.getId());
        ChatIndexService.removeGroupCache(groupid);
        ChatIndexService.clearGroupUserListCache(groupid);
        return atom.getRetObj().set("kick", kickGroupItem);
    }

    /**
     * 离开群-主动 -已调整
     *
     * @param curr
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午10:24:07
     */
    public Ret leaveGroup(User curr, Long groupid) throws Exception {
        Integer uid = curr.getId();
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.grantError();
        }
        Short chatmode = Const.ChatMode.GROUP;
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                String groupname = group.getName();
                boolean change = false;
                //会话删除
                if (groupItem.getChatlinkid() != null) {
                    boolean remove = ChatService.me.deleteChatItem(groupItem.getChatlinkid());
                    if (!remove) {
                        return failRet("清除聊天列表失败");
                    }
                }
                //索引删除
                ChatIndexService.me.chatGroupIndexDel(uid, groupid);
                ChatIndexService.me.chatUserIndexDel(uid, groupid, chatmode);
                //群关系删除
                delGroupUser(groupItem.getGpulinkid());
                retObj = Ret.ok();
                if (Objects.equals(Const.GroupRole.OWNER, groupItem.getGrouprole())) {
                    WxChatGroupItem newOnwerGroupItem = ChatIndexService.me.getFristGroupUserIndex(groupid);
                    if (newOnwerGroupItem != null) {
                        Integer otherUid = newOnwerGroupItem.getUid();
                        WxGroup updateGroup = new WxGroup();
                        updateGroup.setId(groupid);
                        updateGroup.setUid(otherUid);
                        boolean update = updateGroup.update();
                        if (!update) {
                            return failRet("群转让失败");
                        }
                        WxGroupUser groupUser = new WxGroupUser();
                        groupUser.setGrouprole(Const.GroupRole.OWNER);
                        groupUser.setId(newOnwerGroupItem.getGpulinkid());
                        boolean userUpdate = updateGroupUser(groupUser);
                        if (!userUpdate) {
                            return failRet("群角色修改失败");
                        }
                        ChatIndexService.me.chatGroupIndexUpdate(otherUid, groupid, null, Const.GroupRole.OWNER, null, null);
                        //此处缺少会话级别的role处理,此处有可能引发死锁
                        if (newOnwerGroupItem.getChatlinkid() != null) {
                            //更新会话
                            WxChatItems chatItems = new WxChatItems();
                            chatItems.setId(newOnwerGroupItem.getChatlinkid());
                            chatItems.setBizrole(Const.GroupRole.OWNER);
                            chatItems.update();
                        }
                        newOnwerGroupItem.setGrouprole(Const.GroupRole.OWNER);
                        retObj.set("owner", newOnwerGroupItem);
                        change = true;
                    }
                }
                retObj.set("change", change);
                //初始化统计数据
                WxGroupMeta meta = new WxGroupMeta();
                meta.setGroupid(groupid);
                meta.setJoinnum((short) -1);
                if (change) {
                    meta.setTransfercount((short) 1);
                    meta.setLeavecount((short) 1);
                }
                boolean metaUpdate = updateMeta(meta);
                if (!metaUpdate) {
                    return failRet("群统计数据修改异常");
                }
                ChatService.me.updateItemJoinNum((short) -1, groupid, false);
                if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES)) {
                    if (("、" + groupname + "、").indexOf("、" + curr.getNick() + "、") >= 0) {
                        retObj.set("nameupdate", Const.YesOrNo.YES);
                    }
                }
                return true;
            }
        };
        boolean init = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!init) {
            return atom.getRetObj();
        }
        Ret ret = atom.getRetObj();
        boolean change = RetUtils.getOkTData(ret, "change");
        if (change) {
            WxChatGroupItem newgroupitItem = RetUtils.getOkTData(ret, "owner");
            if (newgroupitItem.getChatlinkid() != null) {
                ChatIndexService.removeChatItemsCache(newgroupitItem.getChatlinkid());
            }
            ChatIndexService.clearChatGroupIndex(groupid, newgroupitItem.getUid());
            ChatIndexService.clearGroupUserCache(newgroupitItem.getGpulinkid());
            ChatIndexService.removeUserCache(newgroupitItem.getUid(), groupid, Const.ChatMode.GROUP);
        }
        //此处删除缓存是又可能用户又被加进去了
        ChatIndexService.clearChatUserIndex(uid, groupid, Const.ChatMode.GROUP);
        if (groupItem.getChatlinkid() != null) {
            ChatIndexService.removeChatItemsCache(groupItem.getChatlinkid());
        }
        ChatIndexService.clearGroupUserListCache(groupid);
        ChatIndexService.removeGroupCache(groupid);
        ChatIndexService.clearMailListCache(uid);
        return ret.set("leave", groupItem);
    }

    /**
     * 群聊文本消息-有转发
     *
     * @param devicetype 设备类型
     * @param sessionid  sessionid
     * @param text       文本内容
     * @param uid        发送的uid
     * @param ip         ip地址
     * @param groupid    群id
     * @param frommsgid  转发id
     * @return
     * @author lixinji
     * 2020年2月13日 下午8:55:16
     */
    public WxGroupMsg addMsg(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, Long frommsgid, Short fromchatmode, WxGroupUser groupUser,
                             String appversion) {
        return addMsg(devicetype, sessionid, text, uid, ip, groupid, Const.ContentType.TEXT, Const.YesOrNo.NO, null, null, frommsgid, fromchatmode, "", null, groupUser,
                appversion);
    }

    /**
     * 群聊消息发送-正常
     *
     * @param devicetype  设备类型
     * @param sessionid   sessionid
     * @param text        文本内容
     * @param uid         发送的uid
     * @param ip          ip地址
     * @param groupid     群id
     * @param contenttype 消息类型
     * @param frommsgid   转发id
     * @param sendbysys   系统标识
     * @return
     * @author lixinji
     * 2020年3月12日 下午1:31:34
     */
    public WxGroupMsg addMsg(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, Short contenttype, Long frommsgid, Short fromchatmode,
                             Short sendbysys, String operBizData, SysMsgVo sysMsgVo, WxGroupUser groupUser, String appversion) {
        return addMsg(devicetype, sessionid, text, uid, ip, groupid, contenttype, sendbysys, null, null, frommsgid, fromchatmode, operBizData, sysMsgVo, groupUser, appversion);
    }


    /**
     * 群聊消息发送-正常
     *
     * @param devicetype  设备类型
     * @param sessionid   sessionid
     * @param text        文本内容
     * @param uid         发送的uid
     * @param ip          ip地址
     * @param groupid     群id
     * @param contenttype 消息类型
     * @param frommsgid   转发id
     * @param sendbysys   系统标识
     * @return
     * @author lixinji
     * 2020年3月12日 下午1:31:34
     */
    public WxGroupMsg addMsgNew(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, Short contenttype, Long frommsgid, Short fromchatmode,
                                Short sendbysys, String operBizData, SysMsgVo sysMsgVo, WxGroupUser groupUser, String appversion) {
        return addMsgNew(devicetype, sessionid, text, uid, ip, groupid, contenttype, sendbysys, null, null, frommsgid, fromchatmode, operBizData, sysMsgVo, groupUser, appversion);
    }

    /**
     * 新增系统消息
     *
     * @param devicetype  设备类型
     * @param sessionid   sessionid
     * @param text        文本内容
     * @param uid         发送的uid
     * @param ip          ip地址
     * @param groupid     群id
     * @param operBizData 系统消息的操作id
     * @return
     * @author lixinji
     * 2020年2月13日 下午8:57:38
     */
    public WxGroupMsg addSysMsg(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, String operBizData, SysMsgVo sysMsgVo, WxGroupUser groupUser,
                                String appversion) {
        return addMsg(devicetype, sessionid, text, uid, ip, groupid, Const.ContentType.TEXT, Const.YesOrNo.YES, null, null, null, null, operBizData, sysMsgVo, groupUser,
                appversion);
    }

    /**
     * 保存消息
     *
     * @param devicetype  设备类型
     * @param sessionid   sessionid
     * @param text        文本内容
     * @param uid         发送的uid
     * @param ip          ip地址
     * @param groupid     群id
     * @param contenttype 消息类型
     * @param sendbysys   系统消息标识
     * @param sigleuid    单通道uid
     * @param whereuid    过滤用户的uid字符串
     * @param frommsgid   转发消息id
     * @return
     * @author lixinji
     * 2020年2月13日 下午7:56:09
     */
    public WxGroupMsg addMsg(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, Short contenttype, Short sendbysys, Integer sigleuid,
                             Integer whereuid, Long frommsgid, Short fromchatmode, String operBizData, SysMsgVo sysMsgVo, WxGroupUser groupUser, String appversion) {
        //存聊天日志
        //		IpInfo ipInfo = IpInfoService.ME.save(ip);
        WxGroupMsg msg = new WxGroupMsg();
        String resume = "";
        text = StrUtil.subWithLength(text, 0, 3072);
        if (StrUtil.isNotBlank(appversion)) {
            msg.setAppversion(appversion);
        }
        switch (contenttype) {
            case Const.ContentType.QUOTE_MSG:
                if (Const.SENSITIVE_FLAG) {
                    if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
                        text = SensitiveWordsService.findAndReplace(text);
                    }
                    //				text = SensitiveWordsService.findAndReplace(text);
                }
                ChatAtom chatAtom = Json.toBean(text, ChatAtom.class);
                text = chatAtom.getC();
                msg.setQuotemid(Long.valueOf(chatAtom.getQuotemid()));
                msg.setQuotemsgcontent(chatAtom.getQuotemsgcontent());
                msg.setQuotesrcnick(chatAtom.getQuotesrcnick());
                msg.setQuotemsgtype(Short.valueOf(chatAtom.getQuotemsgtype()));
                resume = StrUtil.subWithLength(text, 0, GROUP_MSG_RESUME_MAXSIZE);
                if (Objects.equals(sendbysys, Const.YesOrNo.YES) && sysMsgVo != null) {
                    msg.setOpernick(sysMsgVo.getOpernick());
                    msg.setSysmsgkey(sysMsgVo.getMsgkey());
                    msg.setTonicks(sysMsgVo.getTonicks().length() > 1024 ? StrUtil.subWithLength(sysMsgVo.getTonicks(), 0, 1024) + "..." : sysMsgVo.getTonicks());
                    msg.setOper(sysMsgVo.getOpercode());
                    msg.setOpersrcmsg(sysMsgVo.getSrctext());
                    msg.setOpersrcmsgtype(sysMsgVo.getSrcmsgtype());
                }
                break;
            case Const.ContentType.TEXT:
                if (Const.SENSITIVE_FLAG) {
                    if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
                        text = SensitiveWordsService.findAndReplace(text);
                    }
                    //				text = SensitiveWordsService.findAndReplace(text);
                }
                ////			保存缩略文字
                //			resume = StrUtil.trim(HtmlUtil.cleanHtmlTag(text));
                //			resume = StrUtil.subWithLength(resume, 0, GROUP_MSG_RESUME_MAXSIZE);
                resume = StrUtil.subWithLength(text, 0, GROUP_MSG_RESUME_MAXSIZE);
                if (Objects.equals(sendbysys, Const.YesOrNo.YES) && sysMsgVo != null) {
                    msg.setOpernick(sysMsgVo.getOpernick());
                    msg.setSysmsgkey(sysMsgVo.getMsgkey());
                    msg.setTonicks(sysMsgVo.getTonicks().length() > 1024 ? StrUtil.subWithLength(sysMsgVo.getTonicks(), 0, 1024) + "..." : sysMsgVo.getTonicks());
                    msg.setOper(sysMsgVo.getOpercode());
                    msg.setOpersrcmsg(sysMsgVo.getSrctext());
                    msg.setOpersrcmsgtype(sysMsgVo.getSrcmsgtype());
                }
                break;
            case Const.ContentType.AUDIO:
                //保存视频
                resume = "[语音消息]";
                break;
            case Const.ContentType.BLOG:
                //保存视频
                resume = "发一条博客";
                break;
            case Const.ContentType.FILE:
                //保存视频
                resume = "分享一个文件";
                break;
            case Const.ContentType.IMG:
                //保存视频
                resume = "分享一个图片";
                break;
            case Const.ContentType.VIDEO:
                //保存视频
                resume = "分享一个视频";
                break;
            case Const.ContentType.MSG_CARD:
                //保存视频
                resume = "分享一个名片";
                break;
            case Const.ContentType.REDPACKET:
                //保存视频
                resume = "发送一个红包";
                break;
            case Const.ContentType.TEMPLATE:
                //保存视频
                resume = "分享一个链接";
                break;
            case Const.ContentType.GROUP_APPLY:
                //保存视频
                resume = "收到一条入群申请";
                break;
            case Const.ContentType.POSITION:
                //保存视频
                resume = "分享一个位置";
                break;
            case Const.ContentType.MERGE:
                //保存视频
                resume = "转发了一条消息";
                break;
            default:
                break;
        }
        msg.setSrctext(text);
        msg.setFrommode(fromchatmode);
        msg.setOperbizdata(operBizData);
        msg.setResume(resume);
        msg.setText(text);
        msg.setGroupid(groupid);
        //		msg.setIpid(ipInfo.getId());
        msg.setIp(ip);
        msg.setDevice(devicetype);
        msg.setTime(new Date());
        msg.setUid(uid);
        if (groupUser == null) {
            msg.setAutoflag(Const.YesOrNo.NO);
            User sender = UserService.ME.getById(uid);
            msg.setNick(sender.getNick());
            msg.setAvatar(sender.getAvatar());
        } else {
            msg.setNick(groupUser.getGroupnick());
            msg.setAvatar(groupUser.getGroupavator());
            msg.setAutoflag(groupUser.getAutoflag());
        }
        msg.setSession(sessionid);
        msg.setSendbysys(sendbysys);
        msg.setContenttype(contenttype);
        msg.setStatus(Const.Status.NORMAL);
        if (sigleuid != null) {
            msg.setSigleflag(Const.YesOrNo.YES);
            msg.setSigleuid(sigleuid);
        } else {
            msg.setSigleuid(Const.MSG_DEFAULT_UID);
        }
        if (whereuid != null) {
            msg.setWhereflag(Const.YesOrNo.YES);
            msg.setWhereuid(whereuid + "");
        }
        msg.setFrommsgid(frommsgid);
        boolean save = msg.save();
        if (!save) {
            log.error("消息保存失败：{}", Json.toJson(msg));
            return null;
        }
        return msg;
    }


    public WxGroupMsg addMsgNew(Short devicetype, String sessionid, String text, Integer uid, String ip, long groupid, Short contenttype, Short sendbysys, Integer sigleuid,
                                Integer whereuid, Long frommsgid, Short fromchatmode, String operBizData, SysMsgVo sysMsgVo, WxGroupUser groupUser, String appversion) {
        //存聊天日志
        //		IpInfo ipInfo = IpInfoService.ME.save(ip);
        WxGroupMsg msg = new WxGroupMsg();
        String resume = "";
        text = StrUtil.subWithLength(text, 0, 3072);
        if (StrUtil.isNotBlank(appversion)) {
            msg.setAppversion(appversion);
        }
        switch (contenttype) {
            case Const.ContentType.QUOTE_MSG:
                if (Const.SENSITIVE_FLAG) {
                    if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
                        text = SensitiveWordsService.findAndReplace(text);
                    }
                    //				text = SensitiveWordsService.findAndReplace(text);
                }
                ChatAtom chatAtom = Json.toBean(text, ChatAtom.class);
                text = chatAtom.getC();
                msg.setQuotemid(Long.valueOf(chatAtom.getQuotemid()));
                msg.setQuotemsgcontent(chatAtom.getQuotemsgcontent());
                msg.setQuotesrcnick(chatAtom.getQuotesrcnick());
                msg.setQuotemsgtype(Short.valueOf(chatAtom.getQuotemsgtype()));
                resume = StrUtil.subWithLength(text, 0, GROUP_MSG_RESUME_MAXSIZE);
                if (Objects.equals(sendbysys, Const.YesOrNo.YES) && sysMsgVo != null) {
                    msg.setOpernick(sysMsgVo.getOpernick());
                    msg.setSysmsgkey(sysMsgVo.getMsgkey());
                    msg.setTonicks(sysMsgVo.getTonicks().length() > 1024 ? StrUtil.subWithLength(sysMsgVo.getTonicks(), 0, 1024) + "..." : sysMsgVo.getTonicks());
                    msg.setOper(sysMsgVo.getOpercode());
                    msg.setOpersrcmsg(sysMsgVo.getSrctext());
                    msg.setOpersrcmsgtype(sysMsgVo.getSrcmsgtype());
                }
                break;
            case Const.ContentType.TEXT:
                if (Const.SENSITIVE_FLAG) {
                    if (!UserService.isSuper(uid)) {//不是超管则做敏感词过滤
                        text = SensitiveWordsService.findAndReplace(text);
                    }
                    //				text = SensitiveWordsService.findAndReplace(text);
                }
                ////			保存缩略文字
                //			resume = StrUtil.trim(HtmlUtil.cleanHtmlTag(text));
                //			resume = StrUtil.subWithLength(resume, 0, GROUP_MSG_RESUME_MAXSIZE);
                resume = StrUtil.subWithLength(text, 0, GROUP_MSG_RESUME_MAXSIZE);
                if (Objects.equals(sendbysys, Const.YesOrNo.YES) && sysMsgVo != null) {
                    msg.setOpernick(sysMsgVo.getOpernick());
                    msg.setSysmsgkey(sysMsgVo.getMsgkey());
                    msg.setTonicks(sysMsgVo.getTonicks().length() > 1024 ? StrUtil.subWithLength(sysMsgVo.getTonicks(), 0, 1024) + "..." : sysMsgVo.getTonicks());
                    msg.setOper(sysMsgVo.getOpercode());
                    msg.setOpersrcmsg(sysMsgVo.getSrctext());
                    msg.setOpersrcmsgtype(sysMsgVo.getSrcmsgtype());
                }
                break;
            case Const.ContentType.AUDIO:
                //保存视频
                resume = "[语音消息]";
                break;
            case Const.ContentType.BLOG:
                //保存视频
                resume = "发一条博客";
                break;
            case Const.ContentType.FILE:
                //保存视频
                resume = "分享一个文件";
                break;
            case Const.ContentType.IMG:
                //保存视频
                resume = "分享一个图片";
                break;
            case Const.ContentType.VIDEO:
                //保存视频
                resume = "分享一个视频";
                break;
            case Const.ContentType.MSG_CARD:
                //保存视频
                resume = "分享一个名片";
                break;
            case Const.ContentType.REDPACKET:
                //保存视频
                resume = "发送一个红包";
                break;
            case Const.ContentType.TEMPLATE:
                //保存视频
                resume = "分享一个链接";
                break;
            case Const.ContentType.GROUP_APPLY:
                //保存视频
                resume = "收到一条入群申请";
                break;
            case Const.ContentType.POSITION:
                //保存视频
                resume = "分享一个位置";
                break;
            default:
                break;
        }
        msg.setSrctext(text);
        msg.setFrommode(fromchatmode);
        msg.setOperbizdata(operBizData);
        msg.setResume(resume);
        msg.setText(text);
        msg.setGroupid(groupid);
        //		msg.setIpid(ipInfo.getId());
        msg.setIp(ip);
        msg.setDevice(devicetype);
        msg.setTime(new Date());
        msg.setUid(uid);
        if (groupUser == null) {
            msg.setAutoflag(Const.YesOrNo.NO);
            User sender = UserService.ME.getById(uid);
            msg.setNick(sender.getNick());
            msg.setAvatar(sender.getAvatar());
        } else {
            msg.setNick(groupUser.getGroupnick());
            msg.setAvatar(groupUser.getGroupavator());
            msg.setAutoflag(groupUser.getAutoflag());
        }
        msg.setSession(sessionid);
        msg.setSendbysys(sendbysys);
        msg.setContenttype(contenttype);
        msg.setStatus(Const.Status.NORMAL);
        if (sigleuid != null) {
            msg.setSigleflag(Const.YesOrNo.YES);
            msg.setSigleuid(sigleuid);
        } else {
            msg.setSigleuid(Const.MSG_DEFAULT_UID);
        }
        if (whereuid != null) {
            msg.setWhereflag(Const.YesOrNo.YES);
            msg.setWhereuid(whereuid + "");
        }
        msg.setFrommsgid(frommsgid);
        return msg;
    }

    /**
     * 群第一条消息-已调整
     *
     * @param chatItems
     * @return
     * @author lixinji
     * 2020年4月1日 下午5:35:55
     */
    public WxGroupMsg getGroupFristMsg(WxChatItems chatItems) {
        WxChatGroupItem userItem = ChatIndexService.chatGroupIndex(chatItems);
        if (userItem == null) {
            return null;
        }
        return getGroupFristMsg(userItem);
    }

    /**
     * 群第一条消息-已调整
     *
     * @param userItem
     * @return
     * @author lixinji
     * 2020年12月24日 下午5:56:31
     */
    public WxGroupMsg getGroupFristMsg(WxChatGroupItem userItem) {
        //非缓存处理
        Long startMsg = userItem.getStartmsgid();
        if (startMsg == null) {
            return null;
        }
        Kv params = Kv.by("groupid", userItem.getGroupid()).set("status", Const.Status.NORMAL).set("uidstr", "," + userItem.getUid() + ",").set("uid", userItem.getUid());
        if (startMsg != null) {
            params.set("startmsgid", startMsg);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.firstGroupMsg", params);
        WxGroupMsg msg = WxGroupMsg.dao.findFirst(sqlPara);
        return msg;
    }

    /**
     * 校验发送群名片条件-已调整
     *
     * @param operuid
     * @param uid
     * @param touid
     * @return
     * @author lixinji
     * 2020年3月11日 上午10:25:40
     */
    public Ret checkSendCard(Integer operuid, Long groupid) {
        WxGroup group = GroupService.me.getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("本群已解散");
        }
        if (Objects.equals(group.getStatus(), Const.Status.DISABLED)) {
            return RetUtils.failMsg("本群已封停");
        }
        if (Objects.equals(operuid, group.getUid())) {
            return RetUtils.okOper();
        }
        if (Objects.equals(group.getApplyflag(), Const.YesOrNo.NO)) {
            return RetUtils.failMsg("当前群聊未开启成员邀请");
        }
        return RetUtils.okOper();
    }

    /**
     * 群名片-已调整
     *
     * @param uid
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月9日 下午2:43:44
     */
    public Ret getGroupCard(Integer uid, Long groupid) throws Exception {
        Ret ret = checkJoinGroup(uid, groupid, null, null, false, 0);
        if (ret.isFail()) {
            return ret;
        }
        WxGroup group = getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxMsgCardVo wsMsgCardVo = new WxMsgCardVo();
        wsMsgCardVo.setCardtype(Const.MsgCardType.GROUP);
        wsMsgCardVo.setBizavatar(group.getAvatar());
        wsMsgCardVo.setBizname(group.getName());
        wsMsgCardVo.setBizid(groupid);
        wsMsgCardVo.setShareFromUid(uid);
        return RetUtils.okData(wsMsgCardVo);
    }

    /**
     * 修改群消息状态-已调整
     *
     * @param groupid
     * @param allactflag
     * @param allstartflag
     * @return
     * @author lixinji
     * 2020年12月22日 下午6:02:56
     */
    public boolean updateMeta(Long groupid, Short allactflag, Short allstartflag) {
        WxGroupMeta meta = new WxGroupMeta();
        meta.setAllactflag(allactflag);
        meta.setAllstartflag(allstartflag);
        meta.setGroupid(groupid);
        return updateMeta(meta);
    }

    /**
     * 修改群统计数据-已调整
     *
     * @param meta
     * @return
     * @author lixinji
     * 2020年2月24日 下午2:49:14
     */
    public boolean updateMeta(WxGroupMeta meta) {
        if (meta.getGroupid() == null) {
            return false;
        }
        Kv params = Kv.by("groupid", meta.getGroupid());
        if (meta.getJoinnum() != null) {
            params.set("joinnum", meta.getJoinnum());
        }
        if (meta.getKickcount() != null) {
            params.set("kickcount", meta.getKickcount());
        }
        if (meta.getLeavecount() != null) {
            params.set("leavecount", meta.getLeavecount());
        }
        if (meta.getTransfercount() != null) {
            params.set("transfercount", meta.getTransfercount());
        }
        if (meta.getAllactflag() != null) {
            params.set("allactflag", meta.getAllactflag());
        }
        if (meta.getAllstartflag() != null) {
            params.set("allstartflag", meta.getAllstartflag());
        }
        if (meta.getForbiddenflag() != null) {
            params.set("forbiddenflag", meta.getForbiddenflag());
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.updateMeta", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return update > 0;
    }

    /**
     * 群邀请开关修改-已调整
     *
     * @param curr
     * @param groupid
     * @param mode
     * @return
     * @author lixinji
     * 2020年3月9日 下午4:52:16
     */
    public Ret modifyApply(User curr, Long groupid, Short mode) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        updateGroup.setApplyflag(mode);
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 群公告滚屏开关
     *
     * @param curr
     * @param groupid
     * @param noticeRoll
     * @return
     * @author xinji
     * 2023年10月17日 下午4:52:16
     */
    public Ret isOpenNoticeRoll(User curr, Long groupid, Integer noticeRoll) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        updateGroup.setNoticeRoll(noticeRoll);
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }


    /**
     * 修改开关
     *
     * @param curr
     * @param groupid
     * @param isOpen
     * @param type    inviteCode(邀请码), inviteFriend(邀请好友), card(名片), memberNum(成员数量)
     * @return
     * @author xinji
     * 2024年7月13日 上午10:53:11
     */
    public Ret updateGroupSwitch(User curr, Long groupid, Integer isOpen, String type) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        switch (type) {
            case "inviteCode":
                updateGroup.setIsOpenInvitationCode(isOpen);
                break;
//			case "inviteFriend":
//				updateGroup.setIsOpenInviteFriend(isOpen);
//				break;
//			case "card":
//				updateGroup.setIsOpenCard(isOpen);
//				break;
            case "memberNum":
                updateGroup.setIsOpenMemberNum(isOpen);
                break;
            default:
                break;
        }
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 是否使用名片
     *
     * @param curr
     * @param groupid
     * @param useCard
     * @return
     * @author xinji
     * 2023年10月17日 下午4:52:16
     */
    public Ret updateCardSwitch(User curr, Long groupid, Integer useCard) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        updateGroup.setUseCard(useCard);
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            List<WxGroupUser> wxGroupUsers = WxGroupUser.dao.find("select * from wx_group_user where groupid=?", groupid);
            for (WxGroupUser groupUser : wxGroupUsers) {
                ChatIndexService.clearMailListCache(groupUser.getUid());
            }
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 是否使用名片
     *
     * @param curr
     * @param groupid
     * @param useCard
     * @return
     * @author xinji
     * 2023年10月17日 下午4:52:16
     */
    public Ret updateMsgBack(User curr, Long groupid, Integer msgBack) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        updateGroup.setMsgBack(msgBack);
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            List<WxGroupUser> wxGroupUsers = WxGroupUser.dao.find("select * from wx_group_user where groupid=?", groupid);
            for (WxGroupUser groupUser : wxGroupUsers) {
                ChatIndexService.clearMailListCache(groupUser.getUid());
            }
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 禁言
     *
     * @param forbiddenVo
     * @return
     * @author lixinji
     * 2021年1月5日 上午10:24:51
     */
    public Ret forbidden(WxForbiddenVo forbiddenVo, Integer operuid) {
        Long groupid = forbiddenVo.getGroupid();
        WxGroup wxGroup = getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxGroupUser groupUser = getGroupUser(operuid, groupid);
        if (Objects.equals(groupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        Ret ret = null;
        switch (forbiddenVo.getMode()) {
            case WxForbiddenVo.Mode.ALL:
                ret = forbiddenGroup(wxGroup, groupUser, forbiddenVo.getOper());
                break;
            case WxForbiddenVo.Mode.USER:
                ret = forbiddenUser(wxGroup, groupUser, forbiddenVo.getOper(), forbiddenVo.getUid(), forbiddenVo.getDuration());
                break;
            case WxForbiddenVo.Mode.USER_LONGTERM:
                ret = forbiddenUser(wxGroup, groupUser, forbiddenVo.getOper(), forbiddenVo.getUid(), null);
                break;
            default:
                return RetUtils.noImplement();
        }
        return ret;
    }

    /**
     * 管理员设置
     *
     * @param operuid
     * @param uid
     * @param groupid
     * @param grouprole
     * @return
     * @author lixinji
     * 2021年1月12日 下午6:58:58
     */
    public Ret manager(Integer operuid, Integer uid, Long groupid, Short grouprole) {
        WxGroup wxGroup = getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        if (!Objects.equals(wxGroup.getUid(), operuid)) {
            return RetUtils.grantError();
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("成员已被移出群聊");
        }
        WxGroupUser groupUser = getGroupUser(groupItem.getGpulinkid());
        if (groupUser == null) {
            return RetUtils.noExistParam();
        }
        if (Objects.equals(grouprole, groupUser.getGrouprole())) {
            return RetUtils.failMsg("重复操作");
        }
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                WxGroupUser updateGroupUser = new WxGroupUser();
                updateGroupUser.setId(groupItem.getGpulinkid());
                updateGroupUser.setGrouprole(grouprole);
                updateGroupUser.setSetroletime(new Date());
                boolean update = updateGroupUser.update();
                if (!update) {
                    log.error("修改角色异常");
                    return failRet("修改角色异常");
                }
                boolean index = ChatIndexService.me.chatGroupIndexUpdate(uid, groupid, null, grouprole, null, null);
                if (!index) {
                    log.error("修改角色异常");
                    return failRet("修改角色异常-索引更新异常");
                }
                return true;
            }
        };
        boolean commit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!commit) {
            return atom.getRetObj();
        }
        ChatIndexService.clearGroupUserCache(groupItem.getGpulinkid());
        ChatIndexService.clearGroupUserListCache(groupid);
        ChatIndexService.clearChatGroupIndex(groupid, uid);
        ChatIndexService.clearMailListCache(uid);
        return RetUtils.okOper();
    }

    /**
     * @param operuid
     * @param groupid
     * @param friendflag
     * @return
     * @author lixinji
     * 2021年1月13日 下午3:51:12
     */
    public Ret friendFlag(Integer operuid, Long groupid, Short friendflag) {
        if (groupid == null || friendflag == null) {
            return RetUtils.invalidParam();
        }
        WxGroup wxGroup = getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(operuid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        if (Objects.equals(wxGroup.getFriendflag(), friendflag)) {
            return RetUtils.failMsg("重复操作");
        }
        WxGroup update = new WxGroup();
        update.setId(groupid);
        update.setFriendflag(friendflag);
        boolean result = update.update();
        if (!result) {
            log.error("群好友显示操作异常");
        }
        ChatIndexService.removeGroupCache(groupid);
        return RetUtils.okOper();
    }

    /**
     * 群禁言
     *
     * @param operUser
     * @param groupid
     * @return
     * @author lixinji
     * 2021年1月5日 上午10:40:59
     */
    public Ret forbiddenGroup(WxGroup group, WxGroupUser operUser, Short oper) {
        if (group == null || operUser == null || oper == null) {
            return RetUtils.invalidParam();
        }
        long groupid = group.getId();
        if (Objects.equals(operUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        if (Objects.equals(group.getForbiddenflag(), oper)) {
            return RetUtils.failMsg("重复操作");
        }
        WxGroupMeta groupMeta = new WxGroupMeta();
        groupMeta.setId(group.getMetaid());
        groupMeta.setForbiddenflag(oper);
        boolean metaUpdate = groupMeta.update();
        if (!metaUpdate) {
            log.error("群禁言操作异常");
        }
        ChatIndexService.removeGroupCache(groupid);
        return RetUtils.okOper();
    }

    /**
     * @param group
     * @param operUser
     * @param oper
     * @param uid
     * @param duration
     * @return
     * @author lixinji
     * 2021年1月5日 上午11:00:27
     */
    public Ret forbiddenUser(WxGroup group, WxGroupUser operUser, Short oper, Integer uid, Integer duration) {
        if (group == null || operUser == null || oper == null || uid == null) {
            return RetUtils.invalidParam();
        }
        long groupid = group.getId();
        if (Objects.equals(operUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("用户已退群");
        }
        WxGroupUser groupUser = getGroupUser(groupItem);
        if (groupUser == null) {
            return RetUtils.failMsg("用户已退群");
        }
        if (Objects.equals(oper, WxForbiddenVo.Oper.FORBIDDEN) && !Objects.equals(groupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.grantError();
        }
        if (Objects.equals(oper, WxForbiddenVo.Oper.CANCEL)) {
            if (Objects.equals(groupUser.getForbiddenflag(), Const.Forbiddenflag.NO)) {
                return RetUtils.failMsg("重复操作");
            }
            WxGroupUser updateGroupUser = new WxGroupUser();
            updateGroupUser.setId(groupItem.getGpulinkid());
            updateGroupUser.setForbiddenflag(Const.Forbiddenflag.NO);
            updateGroupUser.setForbiddenduration(0);
            boolean update = updateGroupUser.update();
            if (!update) {
                log.error("解除禁言操作异常");
                return RetUtils.failMsg("解除禁言异常");
            }
            groupUser.setForbiddenflag(Const.Forbiddenflag.NO);
        } else {
            if (!Objects.equals(groupUser.getForbiddenflag(), Const.Forbiddenflag.NO)) {
                return RetUtils.failMsg("重复操作");
            }
            WxGroupUser updateGroupUser = new WxGroupUser();
            updateGroupUser.setId(groupItem.getGpulinkid());
            if (duration != null) {
                updateGroupUser.setForbiddenflag(Const.Forbiddenflag.DURATION);
                updateGroupUser.setForbiddenduration(duration);
                updateGroupUser.setCancelforbiddentime(DateUtil.offsetSecond(new Date(), duration));
            } else {
                updateGroupUser.setForbiddenflag(Const.Forbiddenflag.LONGTERM);
                updateGroupUser.setForbiddenduration(0);
            }
            boolean update = updateGroupUser.update();
            if (!update) {
                log.error("时长禁言操作异常");
                return RetUtils.failMsg("时长禁言异常");
            }
            groupUser.setForbiddenflag(updateGroupUser.getForbiddenflag());
            groupUser.setForbiddenduration(updateGroupUser.getForbiddenduration());
            groupUser.setCancelforbiddentime(updateGroupUser.getCancelforbiddentime());
        }
        ChatIndexService.clearGroupUserCache(groupItem.getGpulinkid());
        ChatIndexService.clearGroupUserListCache(groupid);
        return RetUtils.okOper().set("forbiddenGuser", groupUser).set("groupitem", groupItem);
    }

    public Ret updateLabel(User curr, Long groupid, Integer type, String label) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (type == null) {
            return RetUtils.failMsg("参数异常");
        }
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        if (type.equals(1)) {
            updateGroup.setGroupleaderlabel(label);
        } else if (type.equals(2)) {
            updateGroup.setGroupmemberlabel(label);
        } else {
            updateGroup.setGroupmanagerlabel(label);
        }
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 群审核开关修改-已调整
     *
     * @param curr
     * @param groupid
     * @param mode
     * @return
     * @author lixinji
     * 2020年3月9日 下午4:56:34
     */
    public Ret modifyReview(User curr, Long groupid, Short mode) {
        WxGroup wxGroup = this.getByGroupid(groupid);
        if (wxGroup == null) {
            return RetUtils.failMsg("本群已解散");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("不是群用户");
        }
        if (Objects.equals(groupItem.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("只有管理员才能修改");
        }
        WxGroup updateGroup = new WxGroup();
        updateGroup.setId(groupid);
        updateGroup.setJoinmode(mode);
        boolean f = updateGroup.update();
        if (f) {
            ChatIndexService.removeGroupCache(groupid);
            return Ret.ok();
        } else {
            return RetUtils.failMsg("操作失败");
        }
    }

    /**
     * 修改群用户信息-已调整
     *
     * @param groupUser
     * @return
     * @author lixinji
     * 2020年2月27日 上午10:40:31
     */
    public boolean updateGroupUser(WxGroupUser groupUser) {
        if (groupUser.getId() == null) {
            return false;
        }
        return groupUser.update();
    }

    /**
     * 群名称剔除-已调整
     *
     * @param groupname
     * @param nick
     * @return
     * @author lixinji
     * 2020年3月17日 下午1:47:16
     */
    public static String remorveGroupNick(String groupname, String nick) {
        groupname = ("、" + groupname + "、").replace("、" + nick + "、", "、");
        if (groupname.indexOf("、") == 0) {
            groupname = groupname.substring(1);
        }
        if (groupname.lastIndexOf("、") == (groupname.length() - 1)) {
            groupname = groupname.substring(0, groupname.length() - 1);
        }
        return groupname;
    }

    /**
     * @param groupid
     * @param uid
     * @return
     * @author lixinji
     * 2021年1月5日 下午1:41:39
     */
    public static Ret checkGroupMsg(Long groupid, Integer uid) {
        WxGroup group = GroupService.me.getByGroupid(groupid);
        if (group == null) {
            return RetUtils.failMsg("本群已解散");
        }
        if (Objects.equals(group.getStatus(), Const.Status.DISABLED)) {
            return RetUtils.failMsg("本群已封停");
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (groupItem == null || groupItem.getChatlinkid() == null) {
            return RetUtils.failMsg("当前会话已删除");
        }
        if (!ChatService.groupChatLink(groupItem)) {
            return RetUtils.failMsg("您已被移出群聊");
        }
        WxGroupUser groupUser = GroupService.me.getGroupUser(groupItem.getGpulinkid());
        if (!Objects.equals(groupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.okData(groupItem.getChatlinkid());
        }
        if (!Objects.equals(groupUser.getForbiddenflag(), Const.Forbiddenflag.NO)) {
            return RetUtils.failMsg("你已被禁言");
        }
        if (Objects.equals(group.getForbiddenflag(), Const.YesOrNo.YES) && Objects.equals(groupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return RetUtils.failMsg("本群已禁言");
        }
        return RetUtils.okData(groupItem.getChatlinkid());
    }

    /**
     * @param groupid
     * @return
     * @author lixinji
     * 2022年3月4日 下午6:56:38
     */
    public Ret delGroupInfo(Long groupid) {
        Kv params = Kv.by("id", groupid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.delGroupBak", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        SqlPara metaSqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.delGroupMetaBak", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(metaSqlPara);
        boolean del = WxGroup.dao.deleteById(groupid);
        if (!del) {
            return RetUtils.failMsg("移除群失败");
        }
        return RetUtils.okOper();
    }


    /**
     * @param id
     * @return
     * @author lixinji
     * 2022年3月4日 下午7:01:51
     */
    public boolean delGroupUser(Long id) {
        Kv params = Kv.by("id", id);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("group.delGroupUserBak", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return WxGroupUser.dao.deleteById(id);
    }


}
