
package org.tio.sitexxx.web.server.controller.wx;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupOperNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.conf.Conf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.model.stat.GroupStat;
import org.tio.sitexxx.service.service.ImgService;
import org.tio.sitexxx.service.service.VideoService;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserRoleService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.service.chat.ChatMsgService.MsgTemplate;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendApplyService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.utils.*;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RetCode.CommonCode;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.sitexxx.web.server.vo.GroupMsgVo;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxForbiddenVo;
import org.tio.sitexxx.web.server.controller.agora.media.RtcTokenBuilder2;
import org.tio.sitexxx.web.server.utils.VideoUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 新版聊天短链接入口
 *
 * @author lixinji
 * 2020年1月6日 下午7:41:13
 */
@RequestPath(value = "/chat")
public class ChatController {

    private static Logger log = LoggerFactory.getLogger(ChatController.class);

    private final static FriendService friendService = FriendService.me;
    private final static UserService userService = UserService.ME;

    private final static ChatService chatService = ChatService.me;

    private final static GroupService groupService = GroupService.me;


    /**********************************************begin-query**************************************************/

    /**
     * 会话列表请求-此处未进行缓存处理，尽量不要频繁刷新该请求-已调整
     *
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月21日 下午5:11:45
     */
    @RequestPath(value = "/list")
    public Resp list(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        if (curr == null) {
            log.error("获取聊天列表：无效用户");
            return Resp.fail().msg(RetUtils.getRetMsg(Ret.fail("msg", "无效参数")));
        }
        List<Record> records = chatService.chatItemList(curr, devicetype);
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isShowOnlineStatus'");
        if (clientConf.getValue().equals(1)) {
            for (Record record : records) {
                if (record.get("chatmode").toString().equals("1")) {
                    User tempUser = User.dao.findById(record.get("uid"));
                    log.info("uid: {}", tempUser.getId());
                    boolean online = !WxChatApi.isOutline(record.get("uid"));
                    record.set("online", online);
                    UserLastLoginTime lastLoginTime = UserLastLoginTime.dao.findFirst("select * from user_last_login_time where uid = ?", tempUser.getId());
                    if (lastLoginTime != null) {
                        record.set("lastLoginTime", lastLoginTime.getCreatetime());
                    } else {
                        record.set("lastLoginTime", "");
                    }
                }
            }
        }
        Ret ret = RetUtils.okList(records);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkList(ret));
    }

    /**
     * 会话信息-已废弃-转换为长链接-已调整
     *
     * @param request
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午4:52:08
     */
    @Deprecated
    @RequestPath(value = "/chatInfo")
    public Resp chatInfo(HttpRequest request, Long chatlinkid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (chatlinkid == null) {
            return Resp.fail("会话id参数为空");
        }
        if (chatlinkid <= 0) {
            //处理群的chatlinkid
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), -chatlinkid);
            if (groupItem == null || groupItem.getChatlinkid() == null) {
                return Resp.fail("会话id为空");
            }
            chatlinkid = groupItem.getChatlinkid();
        }
        /**
         * 此处没有缓存,后续根据数据变更频率，可以替换成getBaseInfo
         */
        WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
        if (chatItems == null) {
            return Resp.fail("会话不存在");
        }
        if (!Objects.equals(curr.getId(), chatItems.getUid())) {
            log.error("异常登录请求会话信息：登录uid：{}，请求会话的uid：{}", curr.getId(), chatItems.getUid());
            return Resp.fail(RetUtils.GRANT_ERROR);
        }
        return Resp.ok(chatItems);
    }

    /**
     * 群信息-5分钟缓存-已调整
     *
     * @param request
     * @param groupid
     * @param userflag,用户标识-1:获取用户的群用户信息
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月22日 下午2:50:32
     */
    @RequestPath(value = "/group")
    public Resp group(HttpRequest request, Long groupid, Short userflag) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.groupInfo(groupid, Objects.equals(userflag, Const.YesOrNo.YES) ? curr.getId() : null);
        if (ret.isFail()) {

            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret);
    }

    /**
     * 是否是好友-已调整
     *
     * @param request
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 上午10:52:25
     */
    @RequestPath(value = "/isFriend")
    public Resp isFriend(HttpRequest request, Integer touid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = friendService.isFriend(curr, touid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 好友的申请列表-已调整
     *
     * @param request
     * @param uid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月4日 下午4:30:55
     */
    @RequestPath(value = "/applyList")
    public Resp applyList(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = FriendApplyService.me.applyList(curr.getId());
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkList(ret));
    }

    /**
     * 用户的被邀请好友的条数-已调整
     *
     * @param request
     * @param uid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月4日 下午4:37:08
     */
    @RequestPath(value = "/applyData")
    public Resp applyData(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = FriendApplyService.me.applyData(curr.getId());
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 申请信息-已调整
     *
     * @param request
     * @param applyid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午9:24:00
     */
    @RequestPath(value = "/applyInfo")
    public Resp applyInfo(HttpRequest request, Integer applyid) throws Exception {
        Ret ret = FriendApplyService.me.applyInfo(applyid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Record record = RetUtils.getOkTData(ret);
        if (record == null) {
            return Resp.fail().msg("申请记录不存在");
        }
        return Resp.ok(record);
    }

    /**
     * 通讯录-无参数缓存-已调整
     *
     * @param request
     * @param mode
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月4日 下午2:07:14
     */
    @RequestPath(value = "/mailList")
    public Resp mailList(HttpRequest request, Short mode, String searchkey, Integer pageNumber) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = chatService.mailList(curr, mode, searchkey, pageNumber);
        HashMap data = (HashMap) ret.get("data");
//		log.error("data: {}, data.class: {}", data, data.getClass());
//		log.error("fd: {}, fd.class: {}", data.get("fd"), data.get("fd").getClass());
        if (data.get("fd") != null) {
//			log.error("进入if fd not null");

            if (data.get("fd").getClass() == ArrayList.class) {
//				log.error("进入if  fd is arraylist");
                for (Record record : (ArrayList<Record>) data.get("fd")) {
                    Integer uid = (Integer) record.get("uid");
                    boolean isOnline = !WxChatApi.isOutline(uid);
                    record.set("isOnline", isOnline);
                }
            } else {
                Page fd = (Page) data.get("fd");
                List<Record> friendList = fd.getList();
                for (Record record : friendList) {
                    Integer uid = (Integer) record.get("uid");
                    boolean isOnline = !WxChatApi.isOutline(uid);
                    record.set("isOnline", isOnline);
                }
            }
        }

        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Object res = ((HashMap) ret.get("data")).get("fd");
//		log.error("res: {}", res);
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 获取群可邀请的好友列表-已调整
     *
     * @param request
     * @param mode
     * @param searchkey
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月20日 下午11:33:54
     */
    @RequestPath(value = "/applyGroupFdList")
    public Resp applyGroupFdList(HttpRequest request, String searchkey, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = friendService.getOutGroupFdList(curr, searchkey, groupid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @param applyid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月5日 上午10:08:58
     */
    @RequestPath(value = "/forbidden")
    public Resp forbidden(HttpRequest request, WxForbiddenVo forbiddenVo) throws Exception {
        User curr = WebUtils.currUser(request);
        if (forbiddenVo == null || forbiddenVo.checkIsNull()) {
            return RetUtils.getInvalidResp();
        }
        Ret ret = groupService.forbidden(forbiddenVo, curr.getId());
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenGroupForbiddenNotify'");
        short openforbidden = Objects.equals(forbiddenVo.getOper(), WxForbiddenVo.Oper.CANCEL) ? (short) 2 : (short) 1;
        Integer touid = forbiddenVo.getUid();
        if (clientConf.getValue().equals(1)) {
//            Const.getBsExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (Objects.equals(forbiddenVo.getMode(), WxForbiddenVo.Mode.ALL)) {
//                            String groupText = "本群已禁言";
//                            if (Objects.equals(forbiddenVo.getOper(), WxForbiddenVo.Oper.CANCEL)) {
//                                groupText = "本群已解除禁言";
//                            }
//                            WxChatApi.sendGroupMsgEach(request, groupText, Const.ContentType.TEXT, curr.getId(), forbiddenVo.getGroupid(), Const.YesOrNo.YES, null);
////							WxChatApi.sendGroupMsgEach(request, groupText, Const.ContentType.TEXT, curr.getId(), forbiddenVo.getGroupid(), Const.YesOrNo.YES, null, (short)1);
//                        } else {
//                            User user = UserService.ME.getById(forbiddenVo.getUid());
//                            SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.cancelforbidden, user.getNick(), "cancelforbidden");
//                            if (!Objects.equals(forbiddenVo.getOper(), WxForbiddenVo.Oper.CANCEL)) {
//                                sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.forbidden, user.getNick(), "forbidden");
//                            }
//                            WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), forbiddenVo.getGroupid(), Const.YesOrNo.YES, sysMsgVo);
////							WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), forbiddenVo.getGroupid(), Const.YesOrNo.YES, sysMsgVo, (short)1);
//                        }
//                    } catch (Exception e) {
//                        log.error("", e);
//                    }
//                }
//            });
        }
//        WxChatApi.sendGroupMsgEach(request, "", Const.ContentType.TEXT, curr.getId(), forbiddenVo.getGroupid(), Const.YesOrNo.YES, null, (short) 1, openforbidden, touid);

        return Resp.ok();
    }

    /**
     * 群用户针对当前用户的状态，该接口为了扩展，命名不符合规范
     * 1、grant：管理员权限：ture:是；
     * 2、rolegrant:角色管理权限
     *
     * @param request
     * @param uid
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月6日 上午11:39:44
     */
    @RequestPath(value = "/forbiddenFlag")
    public Resp forbiddenFlag(HttpRequest request, Integer uid, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Map<String, String> flagMap = new HashMap<String, String>();
        flagMap.put("grant", Const.YesOrNo.NO + "");
        flagMap.put("rolegrant", Const.YesOrNo.NO + "");
        flagMap.put("kickgrant", Const.YesOrNo.NO + "");
        flagMap.put("userstatus", Const.YesOrNo.YES + "");
        if (Objects.equals(uid, curr.getId())) {
            return Resp.ok(flagMap);
        }
        WxGroup group = GroupService.me.getByGroupid(groupid);
        if (group == null) {
            return Resp.ok(flagMap);
        }
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.ok(flagMap);
        }
        WxGroupUser groupUser = GroupService.me.getGroupUser(groupItem.getGpulinkid());
        if (Objects.equals(groupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
            return Resp.ok(flagMap);
        }
        WxChatGroupItem toGroupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(toGroupItem)) {
            flagMap.put("userstatus", Const.YesOrNo.NO + "");
            return Resp.ok(flagMap);
        }
        WxGroupUser toGroupUser = GroupService.me.getGroupUser(toGroupItem.getGpulinkid());
        if (Objects.equals(groupUser.getGrouprole(), Const.GroupRole.OWNER)) {
            flagMap.put("grouprole", toGroupUser.getGrouprole() + "");
            flagMap.put("rolegrant", Const.YesOrNo.YES + "");
        } else {
            if (!Objects.equals(toGroupUser.getGrouprole(), Const.GroupRole.MEMBER)) {
                return Resp.ok(flagMap);
            }
        }
        flagMap.put("grant", Const.YesOrNo.YES + "");
        flagMap.put("flag", toGroupUser.getForbiddenflag() + "");
        flagMap.put("kickgrant", Const.YesOrNo.YES + "");
        return Resp.ok(flagMap);
    }

    /**
     * 群用户列表-已调整
     * 1、精准查询无缓存
     * 2、无条件查询存在缓存
     *
     * @param request
     * @param groupid
     * @param pageNumber
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月7日 下午3:17:36
     */
    @RequestPath(value = "/groupUserList")
    public Resp groupUserList(HttpRequest request, Long groupid, Integer pageNumber, String searchkey) throws Exception {
        User currUser = WebUtils.currUser(request);
        Ret ret = groupService.groupUserList(groupid, pageNumber, searchkey);
        Page<Record> pages = (Page<Record>) ret.get("page");
        List<Record> recordList = pages.getList();
        for (Record record : recordList) {
            Ret friendRet = friendService.isFriend(currUser, record.get("uid"));
            if (friendRet.get("data").equals(Const.YesOrNo.NO)) {
                StrangerRemarkName strangerRemarkName = StrangerRemarkName.dao.findFirst("select * from stranger_remark_name where uid = ? and relation_uid = ?", currUser.getId(), record.get("uid"));
                if (strangerRemarkName != null) {
                    record.set("nick", strangerRemarkName.getRemarkName());
                    record.set("srcnick", strangerRemarkName.getRemarkName());
                }
            } else {
                WxFriend wxFriend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid = ?", currUser.getId(), record.get("uid"));
                if (wxFriend.getRemarkname() != null && !wxFriend.getRemarkname().isEmpty()) {
                    record.set("nick", wxFriend.getRemarkname());
                    record.set("srcnick", wxFriend.getRemarkname());
                }

            }

        }
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkPage(ret));
    }

    /**
     * 禁言用户列表
     *
     * @param request
     * @param groupid
     * @param pageNumber
     * @param searchkey
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月5日 上午11:49:07
     */
    @RequestPath(value = "/forbiddenUserList")
    public Resp forbiddenUserList(HttpRequest request, Long groupid, Integer pageNumber, String searchkey) throws Exception {
        Ret ret = groupService.forbiddenUserList(groupid, pageNumber, searchkey);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkPage(ret));
    }

    /**
     * at使用的群用户列表-暂时没有算法处理-已调整
     *
     * @param request
     * @param groupid
     * @param pageNumber
     * @param searchkey
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月7日 下午5:55:13
     */
    @RequestPath(value = "/atGroupUserList")
    public Resp atGroupUserList(HttpRequest request, Long groupid, String searchkey) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.atGroupUserList(groupid, searchkey, curr.getId());
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkList(ret));
    }

    /**
     * 私聊的消息-已转长链接处理
     *
     * @param request
     * @param uid
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月1日 下午7:30:38
     */
    @Deprecated
    @RequestPath(value = "/p2pMsgList")
    public Resp p2pMsgList(HttpRequest request, Integer uid, Long chatlinkid, Long startmid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = ChatMsgService.me.p2pMsgList(chatlinkid, curr.getId(), startmid, null);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 群聊消息-已转长链接处理
     *
     * @param request
     * @param chatlinkid
     * @param startmid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月13日 下午9:56:14
     */
    @Deprecated
    @RequestPath(value = "/groupMsgList")
    public Resp groupMsgList(HttpRequest request, Long chatlinkid, Long startmid) throws Exception {
        User curr = WebUtils.currUser(request);
        //处理群的chatlinkid
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), Math.abs(chatlinkid));
        if (groupItem == null || groupItem.getChatlinkid() == null) {
            return Resp.fail("会话为空");
        }
        chatlinkid = groupItem.getChatlinkid();
        Ret ret = ChatMsgService.me.groupMsgList(chatlinkid, curr.getId(), startmid, null);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /************************************************end-query**************************************************/

    /**********************************************begin-oper**************************************************/

    /**
     * 进群-异步触发（名片和其它申请的不可异步） - 已调整
     *
     * @param request
     * @param uids
     * @param groupid
     * @param applyuid 群名片的实际用户
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月22日 下午4:10:55
     */
    @RequestPath(value = "/joinGroup")
    public Resp joinGroup(HttpRequest request, String uids, Long groupid, Integer applyuid, Boolean dealapply) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        String[] uidList = uids.split(",");


        if (dealapply == null) {
            dealapply = false;
        }

        if (!dealapply) {
            boolean isFriend = true;
            for (String uid : uidList) {
                if (!uid.isEmpty()) {
                    Ret friend = friendService.isFriend(curr, Integer.valueOf(uid));
                    if (friend.get("data").equals(Const.YesOrNo.NO)) {
                        isFriend = false;
                        break;
                    }
                }
            }
            if (!isFriend) {
                return Resp.fail("非好友不能邀请入群");
            }
        }
        if (applyuid != null) {
            Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
            Ret ret = groupService.joinGroup(curr, groupid, uids, applyuid, dealapply);
            log.error("ret isFail : {}", ret.isFail());
            if (ret.isFail()) {
                return Resp.fail(RetUtils.getRetMsg(ret));
            }
            Short joinnum = RetUtils.getOkTData(ret, "joinnum");
            Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
            if (joinnum != 0) {
                User msgUser = UserService.ME.getById(applyuid);
                boolean auot = auotUpdateGroupInfo(request, devicetype, groupid, nameUpdate, joinnum, msgUser);
                WxChatApi.joinGroup(request, msgUser, groupid, RetUtils.getOkTData(ret), RetUtils.getOkTData(ret, "rebind"), auot);
            }
        } else {
            boolean dealapplyfinal = dealapply;
            //异步处理触发
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                        Ret ret = groupService.joinGroup(curr, groupid, uids, applyuid, dealapplyfinal);
                        log.error("else ret isFail : {}", ret.isFail());
                        if (ret.isFail()) {
                            WxChatApi.sendFriendErrorMsg(request, curr.getId(), curr.getId(), curr.getId(), -groupid, AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
                            return;
                        }
                        Short joinnum = RetUtils.getOkTData(ret, "joinnum");
                        Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
                        if (joinnum != null && joinnum != 0) {
                            User msgUser = curr;
                            boolean auot = auotUpdateGroupInfo(request, devicetype, groupid, nameUpdate, joinnum, msgUser);
                            WxChatApi.joinGroup(request, msgUser, groupid, RetUtils.getOkTData(ret), RetUtils.getOkTData(ret, "rebind"), auot);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 邀请用户进群申请
     *
     * @param request
     * @param uids
     * @param groupid
     * @param applymsg
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月13日 下午4:07:11
     */
    @RequestPath(value = "/joinGroupApply")
    public Resp joinGroupApply(HttpRequest request, String uids, Long groupid, String applymsg) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret checkRet = groupService.checkJoinGroup(curr.getId(), groupid, null, null, false, uids.split(",").length);
        if (checkRet.isOk()) {
            return joinGroup(request, uids, groupid, null, false);
        }
        Ret ret = groupService.joinGroupApply(curr, groupid, uids, applymsg);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        //消息触发
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxGroupApply apply = RetUtils.getOkTData(ret, "apply");
                    Integer count = RetUtils.getOkTData(ret);
                    WxGroup group = RetUtils.getOkTData(ret, "group");
                    Ret ret = WxChatApi.joinGroupApply(request, curr, group, count, apply);
                    if (ret.isFail()) {
                        log.error(RetUtils.getRetMsg(ret));
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 获取群申请信息
     *
     * @param request
     * @param aid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月19日 上午10:07:37
     */
    @RequestPath(value = "/groupApplyInfo")
    public Resp groupApplyInfo(HttpRequest request, Integer aid) throws Exception {
        Ret ret = groupService.groupApplyInfo(aid);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 处理邀请用户进群申请
     *
     * @param request
     * @param aid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月13日 下午5:42:10
     */
    @RequestPath(value = "/dealGroupApply")
    public Resp dealGroupApply(HttpRequest request, Integer aid, Long mid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.dealGroupApply(curr, aid, mid);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        Short dealflag = RetUtils.getOkTData(ret);
        if (Objects.equals(dealflag, Const.YesOrNo.NO)) {
            WxGroupApply apply = RetUtils.getOkTData(ret, "apply");
            String uids = RetUtils.getOkTData(ret, "uids");
            return joinGroup(request, uids, apply.getGroupid(), apply.getOperuid(), true);
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 群邀请开关修改-已调整
     *
     * @param request
     * @param groupid
     * @param mode
     * @return
     * @author lixinji
     * 2020年3月9日 下午4:32:55
     */
    @RequestPath(value = "/modifyApply")
    public Resp modifyApply(HttpRequest request, Long groupid, Short mode) {
        String text = "";
        User curr = WebUtils.currUser(request);
        SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), "", "", "");
        switch (mode) {
            case Const.YesOrNo.YES:
                text = MsgTemplate.applyopen;
                sysMsgVo.setMsgbody(text);
                sysMsgVo.setMsgkey("applyopen");
                break;
            case Const.YesOrNo.NO:
                text = MsgTemplate.applyclose;
                sysMsgVo.setMsgbody(text);
                sysMsgVo.setMsgkey("applyclose");
                break;
            default:
                return Resp.fail().msg("无效入群方式");
        }
        Ret ret = groupService.modifyApply(curr, groupid, mode);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenGroupApplyNotify'");
        if (clientConf.getValue().equals(1)) {
            //消息触发
//            Const.getBsExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
//                    } catch (Exception e) {
//                        log.error("", e);
//                    }
//                }
//            });
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 修改群昵称-已调整
     *
     * @param request
     * @param groupid
     * @param nick
     * @return
     * @author lixinji
     * 2020年4月8日 上午11:38:47
     */
    @RequestPath(value = "/modifyGroupNick")
    public Resp modifyGroupNick(HttpRequest request, Long groupid, String nick) {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.modifyGroupNick(groupid, nick, curr.getId());
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxChatApi.synGroupInfo(curr.getId(), groupid, RetUtils.getOkTData(ret));
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * TODO-lixinji-750-主动离开群通知-已调整
     * 主动离开群
     *
     * @param request
     * @param uids
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午5:49:30
     */
    @RequestPath(value = "/leaveGroup")
    public Resp leaveGroup(HttpRequest request, Integer uid, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.leaveGroup(curr, groupid);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
        Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, curr.getId() + "", groupid + "");
        //自己退群操作
        WxGroupOperNtf leaveNtf = new WxGroupOperNtf();
        leaveNtf.setC("自动退群");
        leaveNtf.setMid(null);
        leaveNtf.setT(System.currentTimeMillis());
        leaveNtf.setUid(curr.getId());
        leaveNtf.setG(groupid);
        leaveNtf.setChatlinkid(-groupid);
        leaveNtf.setOper(Const.WxGroupOper.LEAVE_GROUP);
        ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(leaveNtf));
        Ims.sendToUser(curr.getId(), imPacket);
        //消息触发
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                    WxChatGroupItem newowner = RetUtils.getOkTData(ret, "owner");
                    WxChatGroupItem leave = RetUtils.getOkTData(ret, "leave");
                    boolean auto = auotUpdateGroupInfo(request, devicetype, groupid, nameUpdate, (short) -1, curr);
                    WxChatApi.leaveGroup(request, curr, leave, newowner, auto);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 踢人-已调整
     *
     * @param request
     * @param uids
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月25日 下午5:57:34
     */
    @RequestPath(value = "/kickGroup")
    public Resp kickGroup(HttpRequest request, String uids, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.kickGroup(curr, groupid, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Short joinnum = RetUtils.getOkTData(ret, "joinnum");
        Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
        if (joinnum != 0) {
            //消息触发
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                        List<WxChatGroupItem> kickGroupItem = RetUtils.getOkTData(ret, "kick");
                        String nickStr = RetUtils.getOkTData(ret);
                        boolean auot = auotUpdateGroupInfo(request, devicetype, groupid, nameUpdate, joinnum, curr);
                        WxChatApi.kickGroup(request, curr, groupid, kickGroupItem, nickStr, auot);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 创建群-已调整
     * 1、群邀请人数过多只依赖前端控制
     * 2、群消息不是同步发送的，会出现延迟消息
     *
     * @param request
     * @param wxGroup
     * @param uidList
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月13日 下午5:24:05
     */
    @RequestPath(value = "/createGroup")
    public Resp createGroup(HttpRequest request, WxGroup wxGroup, String uidList) throws Exception {
        String name = "";
        if (StrUtil.isNotBlank(wxGroup.getName())) {
            name = wxGroup.getName();
            name = name.length() > 30 ? name.substring(0, 30) : name;
            wxGroup.setName(name);
            wxGroup.setAutoflag(Const.YesOrNo.NO);
        }
        String[] uidArr = StrUtil.splitToArray(uidList, ",");

        User curr = WebUtils.currUser(request);

        if (uidArr != null && uidArr.length > 0) {
            for (String uid : uidArr) {
                Ret ret = FriendService.me.isFriend(curr, Integer.valueOf(uid));
                if (ret.get("data").equals(Const.YesOrNo.NO)) {
                    return Resp.fail("非好友不可拉入群聊");
                }
            }
        }
//		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
//			return Resp.fail("用户未实名");
//		}
//		RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
//		if (realNameCertification == null) {
//			return Resp.fail().msg("未进行实名认证，提交实名认证，并在审核通过后，才可创建群聊");
//		}
//		if (realNameCertification.getStatus().equals(-1)) {
//			return Resp.fail().msg("实名认证失败，请重新提交实名信息");
//		}
//		if (realNameCertification.getStatus().equals(0)) {
//			return Resp.fail().msg("实名认证审核中，实名通过后才可创建群聊");
//		}
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
//		if (joinTemp.size() == 0) {
//			return Resp.fail().msg("添加的群成员均未实名认证，创建群组失败。");
//		}
//		String[] joinUids = new String[joinTemp.size()];
//		for (int i = 0; i < joinTemp.size(); i++) {
//			joinUids[i] = joinTemp.get(i);
//		}
//		String[] onJoinUids = new String[onJoinTemp.size()];
//		for (int i = 0; i < onJoinTemp.size(); i++) {
//			onJoinUids[i] = onJoinTemp.get(i);
//		}
        Ret checkRet = WxChatApi.checkCreateGroupRegLimit(curr);
        if (checkRet.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(checkRet));
        }
//		Ret imgRet = getGroupImg(joinUids, curr, name);
        Ret imgRet = getGroupImg(uidArr, curr, name); /// 创建群自动生成头像注释了
        Img img = RetUtils.getOkTData(imgRet, "img");
        String nicks = RetUtils.getOkTData(imgRet, "nicks");
        if (StrUtil.isBlank(name)) {
            name = RetUtils.getOkTData(imgRet, "name");
            wxGroup.setName(name);
            wxGroup.setAutoflag(Const.YesOrNo.YES);
        }
        wxGroup.setAvatarautoflag(Const.YesOrNo.NO);
        wxGroup.setAvatar(img.getCoverurl());
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        String sessionid = request.getHttpSession().getId();
        String ip = request.getClientIp();
//		short joinnum = joinUids != null ? (short) (joinUids.length + 1) : (short) 1;
        short joinnum = uidArr != null ? (short) (uidArr.length + 1) : (short) 1;
        wxGroup.setCreatetime(new Date());
        Ret ret = groupService.createGroup(curr, wxGroup, nicks, devicetype, sessionid, ip, joinnum, WebUtils.getRequestExt(request).getAppVersion());
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
//		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", "isOpenCreateGroupNotify");
        //消息触发
//		if (clientConf.getValue().equals(1)) {
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
                    SysMsgVo sysMsgVo = RetUtils.getOkTData(ret, "sysmsgvo");
                    //发送自己的群信息
                    WxChatApi.creatGroupOwner(request, devicetype, curr, wxGroup, msg, sysMsgVo);
                    //其它人的消息触发
                    WxChatApi.creatGroup(request, curr, uidList, nicks, wxGroup, msg, sysMsgVo);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
//		}
        //统计处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String dayperiod = PeriodUtils.dateToPeriodByType(wxGroup.getCreatetime(), Const.PeriodType.DAY);
                    GroupStat groupStat = GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                    if (groupStat == null) {
                        groupStat = new GroupStat();
                        groupStat.setAddcount(1);
                        groupStat.setDayperiod(dayperiod);
                        groupStat.setType(Const.Status.NORMAL);
                        groupStat.ignoreSave();
                    } else {
                        Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(wxGroup);
    }

    /**
     * 好友申请-已调整
     *
     * @param request
     * @param uid
     * @param touid
     * @param greet
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月15日 下午5:22:23
     */
    @RequestPath(value = "/friendApply")
    public Resp friendApply(HttpRequest request, Integer touid, String greet) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = friendService.addApply(curr, touid, greet);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        WxFriendApplyItems applyItems = RetUtils.getOkTData(ret);
        //发送申请
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (WxSynApi.isSynVersion()) {
                        WxSynApi.synFriendApply(touid, applyItems, SynRecordVo.SynType.ADD);
                    } else {
                        WxChatApi.useSysChatNtf(request, touid, Const.WxSysCode.APPLY_REQUEST, curr.getNick() + " 想要成为你的好友：" + applyItems.getGreet(), applyItems.getId() + "");
                    }

                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(applyItems);
    }

    /**
     * 主动加好友-已调整
     * 不激活聊天列表-也不发送通知
     *
     * @param request
     * @param uid
     * @param touid
     * @param greet
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月15日 下午5:48:55
     */
    @RequestPath(value = "/addFriend")
    public Resp addFriend(HttpRequest request, Integer touid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        if (!curr.getStatus().equals((short) 1)) {
            return Resp.fail("账号状态异常");
        }
        Map<String, Integer> data = checkRequestTime(curr);
        Ret ret1 = friendService.checkAddFriend(curr, touid);
        if (ret1.get("code").equals(1)) {
            Ret ret = userService.disable(curr.getId(), (short) 5);
            if (ret.isFail()) {
                log.error("禁用/启用失败：{}", RetUtils.getRetMsg(ret));
                return Resp.fail(RetUtils.getRetMsg(ret));
            }
            RedissonClient redisson = RedisInit.get();
            RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
            TopicVo topicVo = new TopicVo();
            topicVo.setType(TopicVo.Type.CLEAR_USER);
            topicVo.setValue(curr.getId());
            topic.publish(topicVo);
            return Resp.fail("账号已被禁用");
        }
        if (data != null) {
            if (Integer.parseInt(Conf.dao.findFirst("select * from conf where name = 'request.add.friend.time.min'").getValue()) > data.get("interval")) {
                Ret ret = userService.disable(curr.getId(), (short) 5);
                if (ret.isFail()) {
                    log.error("禁用/启用失败：{}", RetUtils.getRetMsg(ret));
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                RedissonClient redisson = RedisInit.get();
                RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
                TopicVo topicVo = new TopicVo();
                topicVo.setType(TopicVo.Type.CLEAR_USER);
                topicVo.setValue(curr.getId());
                topic.publish(topicVo);
                return Resp.fail("账号已被禁用");
            }
            if (Integer.parseInt(Conf.dao.findFirst("select * from conf where name = 'request.add.friend.count.max'").getValue()) <= data.get("count")) {
                Ret ret = userService.disable(curr.getId(), (short) 5);
                if (ret.isFail()) {
                    log.error("禁用/启用失败：{}", RetUtils.getRetMsg(ret));
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                RedissonClient redisson = RedisInit.get();
                RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
                TopicVo topicVo = new TopicVo();
                topicVo.setType(TopicVo.Type.CLEAR_USER);
                topicVo.setValue(curr.getId());
                topic.publish(topicVo);
                return Resp.fail("账号已被禁用");
            }
        }


        Ret ret = friendService.addFriend(curr, touid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }

        RequestAddFriendRecord requestAddFriendRecord = new RequestAddFriendRecord();
        requestAddFriendRecord.setUid(curr.getId());
        requestAddFriendRecord.setRequestTime(new Date());
        boolean save = requestAddFriendRecord.save();
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxFriend friend = RetUtils.getOkTData(ret, "friend");
                    WxFriend toFriend = RetUtils.getOkTData(ret, "tofriend");
                    WxChatApi.friendChangeAddNtf(request, friend, toFriend);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.getOkData(ret));
    }

    private Map<String, Integer> checkRequestTime(User curr) {
        List<RequestAddFriendRecord> requestAddFriendRecords = RequestAddFriendRecord.dao.find("select * from request_add_friend_record where uid = ? and date(request_time) = date(now()) order by request_time desc", curr.getId());
        if (requestAddFriendRecords == null || requestAddFriendRecords.isEmpty()) {
            return null;
        }
        Map<String, Integer> data = new HashMap<>();
        data.put("count", requestAddFriendRecords.size());
        Date now = new Date(); // 当前日期和时间
        long secondsNow = now.getTime() / 1000; // 转换为秒
        Date lastRequestTime = requestAddFriendRecords.get(0).getRequestTime();
        long secondsLastRequestTime = lastRequestTime.getTime() / 1000;
        data.put("interval", (int) (secondsNow - secondsLastRequestTime));
        return data;
    }

    /**
     * 删除好友-已调整
     *
     * @param request
     * @param uid
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月22日 下午4:23:20
     */
    @SuppressWarnings("deprecation")
    @RequestPath(value = "/delFriend")
    public Resp delFriend(HttpRequest request, Integer touid) throws Exception {
        User curr = WebUtils.currUser(request);
        Integer uid = curr.getId();
        if (Objects.equals(uid, touid)) {
            return Resp.fail("不能删除自己");
        }
        IpInfo ipInfo = IpInfoService.ME.save(request.getClientIp());
        Ret ret = friendService.delFriend(curr, touid, ipInfo.getId());
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Long chatlinkid = RetUtils.getOkTData(ret, "chatlinkid");
        Long tochatlinkid = RetUtils.getOkTData(ret, "tochatlinkid");
        if (WxSynApi.isSynVersion()) {
            WxSynApi.synFriendDel(uid, touid, chatlinkid, tochatlinkid, RetUtils.getOkTData(ret, "fid"));
        } else {
            if (chatlinkid != null) {
                WxChatApi.userChatOper(request, uid, chatlinkid, Const.WxUserOper.DEL_FRIEND, "删除好友", "", null);
            }
            if (tochatlinkid != null) {
                WxChatApi.userChatOper(request, touid, tochatlinkid, Const.WxUserOper.TO_DEL_FRIEND, "被删除好友", "", null);
            }
            WxChatApi.friendChangeDelNtf(request, uid, RetUtils.getOkTData(ret, "fid"));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 删除群-已调整
     * 未进行信息用户处理-TODO:lixinji
     *
     * @param request
     * @param uid
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月24日 上午10:28:20
     */
    @RequestPath(value = "/delGroup")
    public Resp delGroup(HttpRequest request, Integer uid, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (!Objects.equals(curr.getId(), uid)) {
            return Resp.fail(RetUtils.LOGIN_ERROR);
        }
        Ret ret = groupService.delGroup(curr, groupid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxChatGroupItem groupItem = RetUtils.getOkTData(ret, "groupitem");
                    WxGroup group = RetUtils.getOkTData(ret, "group");
                    WxGroupUser groupUser = RetUtils.getOkTData(ret, "groupuser");
                    WxChatApi.delGroup(request, curr, groupItem, group, groupUser);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        //统计处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxGroup group = RetUtils.getOkTData(ret, "group");
                    String dayperiod = PeriodUtils.dateToPeriodByType(group.getCreatetime(), Const.PeriodType.DAY);
                    GroupStat groupStat = GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                    if (groupStat == null) {
                        groupStat = new GroupStat();
                        groupStat.setAddcount(1);
                        groupStat.setDayperiod(dayperiod);
                        groupStat.setType(Const.Status.DELETE);
                        groupStat.ignoreSave();
                    } else {
                        Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                    }
                    Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount - 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.okOper());
    }

    /**
     * 转让群-已调整
     *
     * @param request
     * @param groupid
     * @param otherUid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月24日 下午2:33:22
     */
    @RequestPath(value = "/changeOwner")
    public Resp changeOwner(HttpRequest request, Long groupid, Integer otheruid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (Objects.equals(otheruid, curr.getId())) {
            return Resp.fail().msg("不能对自己操作哦");
        }
        User user = UserService.ME.getById(otheruid);
        if (user == null) {
            return Resp.fail().msg("无效转让用户");
        }
        Ret ret = groupService.changeOwner(curr.getId(), user.getId(), groupid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxChatGroupItem owner = RetUtils.getOkTData(ret, "owner");
                    WxChatGroupItem other = RetUtils.getOkTData(ret, "other");
                    Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                    boolean auot = auotUpdateGroupInfo(request, devicetype, groupid, Const.YesOrNo.YES, (short) 0, curr);
                    WxChatApi.changeOwner(request, curr, owner, other, auot);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        return Resp.ok(RetUtils.okOper());
    }

    /**
     * @param request
     * @param groupid
     * @param touid
     * @param freeflag
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月26日 上午11:12:09
     */
    @RequestPath(value = "/msgfreeflag")
    public Resp msgfreeflag(HttpRequest request, Long groupid, Integer touid, Short freeflag) throws Exception {
        User curr = WebUtils.currUser(request);
        Integer uid = curr.getId();
        Ret ret = null;
        if (groupid != null) {
            ret = groupService.modifyGroupPush(groupid, freeflag, uid);
        } else {
            ret = friendService.modifyFdPush(touid, freeflag, uid);
        }
        if (ret == null || ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Short chatsyn = RetUtils.getOkTData(ret);
        if (Objects.equals(chatsyn, Const.YesOrNo.YES)) {
            Long chatlinkid = RetUtils.getOkTData(ret, "chatlinkid");
            ChatIndexService.removeChatItemsCache(chatlinkid);
//            Const.getBsExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
//                        if (WxSynApi.isSynVersion()) {
//                            WxSynApi.synChatSession(uid, chatItems, SynRecordVo.SynType.UPDATE);
//                        } else {
//                            WxChatApi.userChatOper(request, uid, groupid != null ? -groupid : chatlinkid, Const.WxUserOper.CHAT_MSGFREE, "", null, chatItems);
//                        }
//                    } catch (Exception e) {
//                        log.error("", e);
//                    }
//                }
//            });
        }
        return Resp.ok(RetUtils.okOper());
    }

    /**
     * 上传文件
     *
     * @param request
     * @param uploadFile
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年6月7日 上午10:20:34
     */
    @SuppressWarnings("deprecation")
    @RequestPath(value = "/uploadImg")
    public Resp uploadImg(HttpRequest request, UploadFile uploadFile) throws Exception {
        if (uploadFile == null) {
            return Resp.fail("上传信息为空");
        }
        User curr = WebUtils.currUser(request);
        Resp ret = null;
        if (curr == null) {
            ret = Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }
        byte[] imageBytes = uploadFile.getData();
        if (UserService.isSuper(curr) || UserRoleService.hasRole(curr, Role.UPLOAD_VIDEO)) {
        } else {
            int maxsize = ConfService.getInt("user.upload.avatar.maxsize", 512);
            if (imageBytes.length > 1024 * maxsize) {
                ret = Resp.fail("文件尺寸不能大于" + maxsize + "KB");
            }
        }
        Img img = ImgUtils.processImg(Const.UPLOAD_DIR.USER_AVATAR, uploadFile);
        img.setComefrom(Img.ComeFrom.WX_UPLOAD);
        img.setStatus((short) 1);
        img.setSession(request.getHttpSession().getId());

        boolean f = ImgService.me.save(img);
        if (ret != null) {
            return ret;
        }
        if (f) {
            return Resp.ok(img.getUrl());
        } else {
            return Resp.fail("服务器异常");
        }
    }

    /**
     * 收藏
     *
     * @param request
     * @param chatmode
     * @param mid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月26日 下午4:48:24
     */
    @RequestPath(value = "/favorite")
    public Resp favorite(HttpRequest request, Short chatmode, Long mid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = ChatMsgService.me.msgFavorite(curr, mid, chatmode);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * @param request
     * @param type
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月26日 下午5:10:19
     */
    @RequestPath(value = "/favoritelist")
    public Resp favoritelist(HttpRequest request, Short type, Integer pageNumber) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = ChatMsgService.me.favoriteList(curr.getId(), type, pageNumber);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkPage(ret));
    }

    /**
     * 取消收藏
     *
     * @param request
     * @param chatmode
     * @param mid
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月26日 下午5:07:14
     */
    @RequestPath(value = "/cancelfavorite")
    public Resp cancelfavorite(HttpRequest request, Integer fid) throws Exception {
        Ret ret = ChatMsgService.me.cancelMsgFavorite(fid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 操作-已调整
     * 用户拉黑/删除聊天会话/恢复用户拉黑/清空聊天记录
     *
     * @param request
     * @param uid
     * @param touid
     * @param oper
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月19日 下午2:20:38
     */
    @RequestPath(value = "/oper")
    public Resp oper(HttpRequest request, Integer touid, Short oper, Long chatlinkid) throws Exception {
        User curr = WebUtils.currUser(request);
        Integer uid = curr.getId();
        //处理群的chatlinkid
        Long group = null;
        Long oldChatLinkid = chatlinkid;
        if (chatlinkid != null && chatlinkid <= 0) {
            group = -chatlinkid;
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), -chatlinkid);
            if (groupItem == null || groupItem.getChatlinkid() == null) {
                return Resp.fail("会话id为空");
            }
            chatlinkid = groupItem.getChatlinkid();
        }
        Ret ret = chatService.chatUserOper(oper, uid, touid, chatlinkid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        final Long fGroup = group;
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WxChatItems chatItems = RetUtils.getOkTData(ret, "chat");
                    if (Objects.equals(oper, Const.WxUserOper.DEL_ITEM_REACT) || Objects.equals(oper, Const.WxUserOper.HIDE_CHAT)) {
                        if (chatItems != null && Objects.equals(chatItems.getChatmode(), Const.ChatMode.GROUP)) {
                            Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, uid + "", chatItems.getBizid() + "");
                        }
                    }
                    if (WxSynApi.isSynVersion()) {
                        switch (oper) {
                            case Const.WxUserOper.BLACK:
                                break;
                            case Const.WxUserOper.REMOVE_BLACK:
                                break;
                            case Const.WxUserOper.DEL_ITEM_REACT:
                                WxChatItems delChatItems = new WxChatItems();
                                delChatItems.setChatlinkid(oldChatLinkid);
                                WxSynApi.synChatSession(uid, delChatItems, SynRecordVo.SynType.DEL);
                                break;
                            case Const.WxUserOper.CLEAR_CHAT_MSG:
                                WxSynApi.synMsgClear(uid, oldChatLinkid);
                                break;
                            case Const.WxUserOper.CHAT_TOP:
                                WxChatItems topChatItems = new WxChatItems();
                                topChatItems.setChatlinkid(oldChatLinkid);
                                topChatItems.setTopflag(Const.YesOrNo.YES);
                                topChatItems.setChatuptime(new Date());
                                WxSynApi.synChatSession(uid, topChatItems, SynRecordVo.SynType.UPDATE);
                                break;
                            case Const.WxUserOper.CHAT_CANCEL_TOP:
                                WxChatItems cancelTop = new WxChatItems();
                                cancelTop.setChatlinkid(oldChatLinkid);
                                cancelTop.setTopflag(Const.YesOrNo.NO);
                                cancelTop.setChatuptime(new Date());
                                WxSynApi.synChatSession(uid, cancelTop, SynRecordVo.SynType.UPDATE);
                                break;
                            case Const.WxUserOper.HIDE_CHAT:
                                //							WxChatItems hide = new WxChatItems();
                                //							hide.setChatlinkid(oldChatLinkid);
                                //							hide.setViewflag(Const.YesOrNo.NO);
                                //							hide.setChatuptime(new Date());
                                //							WxSynApi.synChatSession(uid, hide, SynRecordVo.SynType.UPDATE);
                                WxChatItems hidChatItems = new WxChatItems();
                                hidChatItems.setChatlinkid(oldChatLinkid);
                                WxSynApi.synChatSession(uid, hidChatItems, SynRecordVo.SynType.DEL);
                                break;
                        }
                    } else {
                        String operBizData = "";
                        Long chatlinkid = RetUtils.getOkTData(ret, "chatlinkid");
                        switch (oper) {
                            case Const.WxUserOper.BLACK:
                                break;
                            case Const.WxUserOper.REMOVE_BLACK:
                                if (chatlinkid != null) {
                                    WxChatItems removeBlack = chatService.getAllChatItems(chatlinkid);
                                    WxChatApi.userChatOper(request, uid, chatlinkid, oper, "", operBizData, removeBlack);
                                }
                                break;
                            case Const.WxUserOper.CHAT_TOP:
                                WxChatItems topChatItems = new WxChatItems();
                                topChatItems.setChatlinkid(oldChatLinkid);
                                topChatItems.setTopflag(Const.YesOrNo.YES);
                                topChatItems.setChatuptime(new Date());
                                operBizData = Json.toJson(topChatItems);
                                break;
                            case Const.WxUserOper.CHAT_CANCEL_TOP:
                                WxChatItems cancelTop = new WxChatItems();
                                cancelTop.setChatlinkid(oldChatLinkid);
                                cancelTop.setTopflag(Const.YesOrNo.NO);
                                cancelTop.setChatuptime(new Date());
                                operBizData = Json.toJson(cancelTop);
                                break;
                        }
                        if (Objects.equals(oper, Const.WxUserOper.HIDE_CHAT)) {
                            WxChatApi.userChatOper(request, uid, fGroup == null ? RetUtils.getOkTData(ret, "chatlinkid") : -fGroup, Const.WxUserOper.DEL_ITEM_REACT, "",
                                    operBizData, null);
                        } else {
                            WxChatApi.userChatOper(request, uid, fGroup == null ? RetUtils.getOkTData(ret, "chatlinkid") : -fGroup, oper, "", operBizData, null);
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 打开聊天界面
     * 如果是激活操作，发送通知
     *
     * @param request
     * @param uid
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月20日 下午4:53:34
     */
    @RequestPath(value = "/actChat")
    public Resp actChat(HttpRequest request, Integer touid, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Integer uid = curr.getId();
        Ret ret = null;
        if (groupid == null) {
            if (touid == null) {
                return RetUtils.getInvalidResp();
            }
            ret = chatService.actFdChatItems(uid, touid);
        } else {
            ret = chatService.actGroupChatItems(groupid, uid);
        }
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Short act = RetUtils.getOkTData(ret, "actflag");
        Ret sendRet = ret;
        if (act != null && Objects.equals(act, Const.YesOrNo.YES)) {//发送激活通知
            //消息处理
            Const.getBsExecutor().execute(new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    try {
                        WxChatItems chatItems = RetUtils.getOkTData(sendRet, "chat");
                        if (WxSynApi.isSynVersion()) {
                            WxSynApi.synChatSession(uid, chatItems, SynRecordVo.SynType.ADD);
                        } else {
                            WxChatApi.userActOper(request, uid, chatItems);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });

        }
        return Resp.ok(ret);
    }

    /**
     * 处理申请-已调整
     *
     * @param request
     * @param applyid
     * @param remarkname
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月15日 下午5:55:41
     */
    @RequestPath(value = "/dealApply")
    public Resp dealApply(HttpRequest request, Integer applyid, String remarkname) throws Exception {
        User curr = WebUtils.currUser(request);
        WxFriendApplyItems items = FriendApplyService.me.getById(applyid);
        if (items == null) {
            return RetUtils.getFailResp(CommonCode.BIZ_NOT_EXIST.value);
        }
        Ret ret = friendService.dealApply(curr, items, remarkname);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer uid = items.getFromuid();
                    Integer touid = items.getTouid();
                    String send = RetUtils.retKey(ret, "send");
                    if (StrUtil.isNotBlank(send)) {
                        String greet = RetUtils.retKey(ret, "greet");
                        //操作者的聊天列表
                        Long tochatlinkid = RetUtils.retLongKey(ret, "tochatlinkid");
                        if (send.equals("double")) {
                            boolean applysigle = RetUtils.getOkTData(ret, "applysigle");
                            //申请者的聊天列表
                            Long chatlinkid = RetUtils.retLongKey(ret, "chatlinkid");
                            WxChatApi.addFriendEachOfPassApply(request, uid, touid, chatlinkid, tochatlinkid, greet, applysigle);
                        } else {
                            WxChatApi.addFriendSigleOfPassApply(request, uid, touid, tochatlinkid, greet);
                        }
                    }
                    WxFriend friend = RetUtils.getOkTData(ret, "friend");
                    WxFriend toFriend = RetUtils.getOkTData(ret, "tofriend");
                    WxChatApi.friendChangeAddNtf(request, friend, toFriend);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 消息操作-已调整
     *
     * @param request
     * @param mid
     * @param oper
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月11日 下午12:03:26
     */
    @RequestPath(value = "/msgOper")
    public Resp msgOper(HttpRequest request, Long chatlinkid, String mids, Short oper) throws Exception {
        if (chatlinkid == null) {
            return Resp.fail("会话id参数为空");
        }
        User curr = WebUtils.currUser(request);
        Short chatmode = Const.ChatMode.P2P;
        if (chatlinkid <= 0) {
            //处理群的chatlinkid
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), -chatlinkid);
            if (groupItem == null || groupItem.getChatlinkid() == null) {
                return Resp.fail("会话id为空");
            }
            chatlinkid = groupItem.getChatlinkid();
            chatmode = Const.ChatMode.GROUP;
        }
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenBackMsg'");
        if (oper.equals(Const.WxMsgOper.BACK) && clientConf.getValue().equals(0)) {
            return Resp.fail("禁止消息撤回");
        }
        Ret ret = ChatMsgService.me.msgOper(curr, chatlinkid, oper, mids, chatmode);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Long sendChatLinkid = chatlinkid;
        Short sendMode = chatmode;
        WxChatItems chatItems = RetUtils.getOkTData(ret, "chatItems");
        //消息处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Object msg = RetUtils.getOkTData(ret, "msg");
                    switch (oper) {
                        case Const.WxMsgOper.DEL:
                            WxChatApi.delMsg(request, curr, sendChatLinkid, sendMode, mids, chatItems);
                            break;
                        case Const.WxMsgOper.BACK:
                            WxChatApi.backMsg(request, curr, sendChatLinkid, sendMode, msg);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 消息转发-已调整
     *
     * @param request
     * @param mids
     * @param chatlinkids
     * @param groupids
     * @param uids
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月12日 下午5:11:26
     */
    @RequestPath(value = "/msgForward")
    public Resp msgForward(HttpRequest request, Long chatlinkid, String mids, String groupids, String uids) throws Exception {
        if (chatlinkid == null) {
            return Resp.fail("会话id参数为空");
        }
        User curr = WebUtils.currUser(request);
        Short chatmode = Const.ChatMode.P2P;
        if (chatlinkid <= 0) {
            //处理群的chatlinkid
            WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), -chatlinkid);
            if (groupItem == null || groupItem.getChatlinkid() == null) {
                return Resp.fail("会话id为空");
            }
            chatlinkid = groupItem.getChatlinkid();
            chatmode = Const.ChatMode.GROUP;
        }
        Ret ret = WxChatApi.msgForward(request, curr, chatmode, mids, groupids, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 收藏转发
     *
     * @param request
     * @return
     * @author lixinji
     * 2021年1月26日 下午5:35:19
     */
    @RequestPath(value = "/favoriteForward")
    public Resp favoriteForward(HttpRequest request, String fids, String groupids, String uids) throws Exception {
        User curr = WebUtils.currUser(request);
        String sessionid = request.getHttpSession().getId();
        String ip = request.getClientIp();
        if (StrUtil.isBlank(fids)) {
            return Resp.fail("收藏信息为空");
        }
        if (StrUtil.isBlank(uids) && StrUtil.isBlank(groupids)) {
            return Resp.fail("转发目标为空");
        }
        String[] fidArr = fids.split(",");
        Ret ret = WxChatApi.favoriteForward(request, sessionid, ip, curr.getId(), fidArr, groupids, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 分享名片-已调整
     *
     * @param request
     * @param chatlinkid
     * @param mids
     * @param groupids
     * @param uids
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月1日 下午2:10:33
     */
    @RequestPath(value = "/shareCard")
    public Resp shareCard(HttpRequest request, Long cardid, String groupids, String uids, Short chatmode) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = WxChatApi.sharChard(request, curr, cardid, chatmode, groupids, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /************************************************end-oper**************************************************/

    /**********************************************begin-check**************************************************/

    /**
     * 检查是否可以加好友-已调整
     *
     * @param request
     * @param uid
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月15日 下午5:20:11
     */
    @RequestPath(value = "/checkAddFriend")
    public Resp checkAddFriend(HttpRequest request, Integer touid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = friendService.checkAddFriend(curr, touid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getCode(ret));
    }

    /**
     * 检查群名片进去条件-已调整
     *
     * @param request
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月11日 上午10:25:12
     */
    @RequestPath(value = "/checkCardJoinGroup")
    public Resp checkCardJoinGroup(HttpRequest request, Long groupid, Integer applyuid, Date sendtime) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.checkJoinGroup(applyuid, groupid, curr.getId(), sendtime, false, 1);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkTData(ret, "joinflag"));
    }

    /**
     * check发送名片-已调整
     *
     * @param request
     * @param groupid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月11日 上午10:25:11
     */
    @RequestPath(value = "/checkSendCard")
    public Resp checkCard(HttpRequest request, Long groupid) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = groupService.checkSendCard(curr.getId(), groupid);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok();
    }

    /**
     * 检查是否是有效群成员-已调整
     *
     * @param request
     * @param groupid
     * @param uid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月31日 下午6:29:22
     */
    @RequestPath(value = "/checkGroupUser")
    public Resp checkGroupUser(HttpRequest request, Long groupid, Integer uid) throws Exception {
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(uid, groupid);
        if (!ChatService.groupChatLink(groupItem)) {
            return Resp.ok(Const.YesOrNo.NO).msg("不是群成员");
        }
        return Resp.ok(Const.YesOrNo.YES);
    }

    /**
     * 已读ack
     *
     * @param request
     * @return
     * @author lixinji
     * 2021年1月4日 下午12:23:36
     */
    @RequestPath(value = "/readAck")
    public Resp readAck(HttpRequest request, Long chatlinkid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (chatlinkid == null) {
            log.error("消息已读ack异常:会话为空");
            return Resp.fail("ack参数异常");
        }
        if (chatlinkid <= 0) {
            return Resp.ok(Const.YesOrNo.YES);
        }
        WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
        if (userItem == null) {
            return Resp.fail("ack索引异常");
        }
        WxReadAck readAck = new WxReadAck();
        readAck.setUid(curr.getId());
        readAck.setTouid(userItem.getBizid().intValue());
        readAck.setChatlinkid(chatlinkid);
        readAck.setType(Const.YesOrNo.YES);
        readAck.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
        readAck.save();
        return Resp.ok(Const.YesOrNo.YES);
    }

    /************************************************end-check**************************************************/

    /**********************************************begin-upload-msg**************************************************/
    /**
     * 发送文件-已调整
     *
     * @param request
     * @param uploadFile
     * @param groupid
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:01:16
     */
    @RequestPath(value = "/file")
    public Resp file(HttpRequest request, UploadFile uploadFile, Long chatlinkid) throws Exception {
        try {
            if (uploadFile == null) {
                return Resp.fail("上传信息为空");
            }
            // 文件后缀验证
            String fileName = uploadFile.getName();
            String[] fileNameArray = fileName.split("\\.");
            String fileEnd = fileNameArray[fileNameArray.length - 1];
            String fileRole = "php,java,jsp,sh,html,css,js,vue,c,h,jar,war,exe,scr";
            String[] fileRoleArray = fileRole.split(",");
            for (String s : fileRoleArray) {
                if (fileEnd.toLowerCase().equals(s.toLowerCase())) {
                    return Resp.fail("非法文件格式无法上传");
                }
            }


            if (chatlinkid == null) {
                return Resp.fail("会话id参数为空");
            }
            User curr = WebUtils.currUser(request);
            if (chatlinkid <= 0) {
                Ret check = GroupService.checkGroupMsg(-chatlinkid, curr.getId());
                if (check.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(check));
                }
                chatlinkid = RetUtils.getOkTData(check);
            }
            WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            String sessionid = request.getHttpSession().getId();
            File dbFile = innerUploadFile(curr, uploadFile, sessionid);
            String text = Json.toJson(dbFile);
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.FILE, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            } else {
                Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.FILE, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            }
        } catch (Exception e) {
            log.error("", e);
            return RetUtils.getSysErrorResp();
        }
    }

    /**
     * 群发消息
     *
     * @param request
     * @param groupMsgVo
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:43:00
     */
    @RequestPath(value = "/sendGroupMsg")
    public Resp sendGroupMsg(HttpRequest request, GroupMsgVo groupMsgVo) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        if (groupMsgVo.getUid() == null) {
            groupMsgVo.setUid(curr.getId());
        }
        if (groupMsgVo.getUids() == null && groupMsgVo.getGroupids() == null) {
            return Resp.fail("请选择群发对象");
        }
        if (groupMsgVo.getContenttype() == null) {
            return Resp.fail("消息类型不能为空");
        }
        if (groupMsgVo.getContenttype().equals(1) && groupMsgVo.getMsg() == null) {
            return Resp.fail("消息内容不能为空");
        }
        if (!groupMsgVo.getContenttype().equals(1) && groupMsgVo.getFile() == null) {
            return Resp.fail("消息内容不能为空");
        }
        if (!groupMsgVo.getContenttype().equals(1) && groupMsgVo.getContenttype().equals(4) && groupMsgVo.getContenttype().equals(5) && groupMsgVo.getContenttype().equals(6)) {
            return Resp.fail("不支持该类型消息的转发");
        }
        String[] uids = new String[0];
        if (groupMsgVo.getUids() != null) {
            uids = groupMsgVo.getUids().split(",");
        }
        ArrayList<Long> chatlinkidList = new ArrayList<>();

        String text = "";
        Ret ret = RetUtils.okOper();
        for (String uidStr : uids) {
            WxChatUserItem wxChatUserItem = WxChatUserItem.dao.findFirst("select * from wx_chat_user_item where chatmode = 1 and uid = ? and bizid = ?", curr.getId(), uidStr);
            WxChatItems chatItems = chatService.getBaseChatItems(wxChatUserItem.getChatlinkid());
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            if (groupMsgVo.getContenttype().equals(1)) { /*文本*/
                text = groupMsgVo.getMsg();
                ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.TEXT, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);

            } else if (groupMsgVo.getContenttype().equals(4)) { /*音频*/
                Resp resp = processUploadedAudio(request, groupMsgVo.getFile(), chatItems);
                if (!resp.isOk()) {
                    return resp;
                }
                Audio audio = (Audio) resp.getData();
                text = Json.toJson(audio);
                ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.AUDIO, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
            } else if (groupMsgVo.getContenttype().equals(5)) { /*视频*/
                Resp resp = processUploadedVideo(request, groupMsgVo.getFile(), chatItems);
                if (!resp.isOk()) {
                    return resp;
                }
                Video video = (Video) resp.getData();
                text = Json.toJson(video);
                ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.VIDEO, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);

            } else if (groupMsgVo.getContenttype().equals(6)) { /*图片*/
                Resp resp = processUploadedImg(request, groupMsgVo.getFile());
                if (!resp.isOk()) {
                    return resp;
                }
                Img img = (Img) resp.getData();
                text = Json.toJson(img);
                ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.IMG, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
            }
        }

        String[] groupids = new String[0];
        if (groupMsgVo.getGroupids() != null)
            groupids = groupMsgVo.getGroupids().split(",");

        GroupMsg groupMsg = new GroupMsg();

        for (String groupid : groupids) {
            long chatlinkid = 0 - Long.parseLong(groupid);
            if (chatlinkid <= 0) {
                Ret check = GroupService.checkGroupMsg(-chatlinkid, curr.getId());
                if (check.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(check));
                }
                chatlinkid = RetUtils.getOkTData(check);
            }
            WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            if (groupMsgVo.getContenttype().equals(1)) { /*文本*/
                text = groupMsgVo.getMsg();
                ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.TEXT, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);

            } else if (groupMsgVo.getContenttype().equals(4)) { /*音频*/
                Resp resp = processUploadedAudio(request, groupMsgVo.getFile(), chatItems);
                if (!resp.isOk()) {
                    return resp;
                }
                Audio audio = (Audio) resp.getData();
                text = Json.toJson(audio);
                ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.AUDIO, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
            } else if (groupMsgVo.getContenttype().equals(5)) { /*视频*/
                Resp resp = processUploadedVideo(request, groupMsgVo.getFile(), chatItems);
                if (!resp.isOk()) {
                    return resp;
                }
                Video video = (Video) resp.getData();
                text = Json.toJson(video);
                ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.VIDEO, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);

            } else if (groupMsgVo.getContenttype().equals(6)) { /*图片*/
                Resp resp = processUploadedImg(request, groupMsgVo.getFile());
                if (!resp.isOk()) {
                    return resp;
                }
                Img img = (Img) resp.getData();
                text = Json.toJson(img);
                ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.IMG, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
            }
        }
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        groupMsg.setUid(curr.getId());
        groupMsg.setUids(groupMsgVo.getUids());
        groupMsg.setGroupids(groupMsgVo.getGroupids());
//		if (groupMsgVo.getContenttype().equals(1)) {
//			groupMsg.setMsg(text);
//		} else if (groupMsgVo.getContenttype().equals(4)) {
//			groupMsg.setMsg("发送了一条语音");
//		} else if (groupMsgVo.getContenttype().equals(5)) {
//			groupMsg.setMsg("发送了一条视频");
//		} else if (groupMsgVo.getContenttype().equals(6)) {
//			groupMsg.setMsg("发送了一张图片");
//		}
        groupMsg.setMsg(text);
        groupMsg.setContenttype(groupMsgVo.getContenttype());
        groupMsg.setCreatetime(new Date());
        if (!groupMsgVo.getContenttype().equals(1)) {
            groupMsg.setFile(text);
        }
        groupMsg.save();

        Map<String, GroupMsg> groupMsgMap = new HashMap<>();
        groupMsgMap.put("groupMsg", groupMsg);
        return Resp.ok(groupMsgMap);
    }


    /**
     * 群发消息列表
     *
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:43:00
     */
    @RequestPath(value = "/msgGroupList")
    public Resp msgGroupList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        List<GroupMsg> groupMsgs = GroupMsg.dao.find("select * from group_msg where uid = ? order by createtime desc LIMIT ? OFFSET ?;", curr.getId(), pageSize, (pageNumber - 1) * pageSize);
        for (GroupMsg groupMsg : groupMsgs) {
            List<Map<String, String>> info = new ArrayList<>();
            if (groupMsg.getUids() != null) {
                String[] uids = groupMsg.getUids().split(",");
                for (String uid : uids) {
                    User user = User.dao.findById(uid);
                    if (user != null) {
                        Map<String, String> map = new HashMap<>();
                        map.put("id", uid);
                        map.put("name", user.getNick());
                        map.put("avatar", user.getAvatar());
                        groupMsg.setNicks(groupMsg.getNicks() + user.getNick() + ",");
                        info.add(map);
                    }
                }
                if (groupMsg.getNicks() != null && groupMsg.getNicks().endsWith(",")) {
                    groupMsg.setNicks(groupMsg.getNicks().substring(0, groupMsg.getNicks().lastIndexOf(",")));
                }
                groupMsg.setNum(uids.length);
            }
            if (groupMsg.getGroupids() != null) {
                String[] groups = groupMsg.getGroupids().split(",");
                for (String groupid : groups) {
                    WxGroup group = WxGroup.dao.findById(groupid);
                    if (group != null) {
                        Map<String, String> map = new HashMap<>();
                        map.put("id", groupid);
                        map.put("name", group.getName());
                        map.put("avatar", group.getAvatar());
                        groupMsg.setGroups(groupMsg.getGroups() + group.getName() + ",");
                        info.add(map);
                    }
                }
                if (groupMsg.getGroups() != null && groupMsg.getGroups().endsWith(",")) {
                    groupMsg.setGroups(groupMsg.getGroups().substring(0, groupMsg.getGroups().lastIndexOf(",")));
                }
                groupMsg.setNum(groups.length);
            }
            groupMsg.setInfo(info);

        }

        Map<String, List<GroupMsg>> map = new HashMap<>();
        map.put("groupMsgs", groupMsgs);
        return Resp.ok(map);
    }


    /**
     * 删除群发消息
     *
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:43:00
     */
    @RequestPath(value = "/delGroupMsg")
    public Resp sendGroupMsg(HttpRequest request, String ids) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        if (ids != null && !ids.isEmpty()) {
            boolean delete = true;
            for (String id : ids.split(",")) {
                GroupMsg groupMsg = GroupMsg.dao.findById(id);
                if (!curr.getId().equals(groupMsg.getUid())) {
                    return Resp.fail("该群发消息不存在");
                }
                delete = groupMsg.delete();
            }

            if (delete) {
                return Resp.ok(RetUtils.OPER_RIGHT);
            }
        } else {
            Kv param = Kv.by("uid", curr.getId());
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.delGroupMsg", param);
            int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
            if (update > 0) {
                return Resp.ok(RetUtils.OPER_RIGHT);
            }

        }
        return Resp.fail(RetUtils.OPER_ERROR);
    }


    /**
     * 发送视频-已调整
     *
     * @param request
     * @param uploadFile
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:43:00
     */
    @RequestPath(value = "/video")
    public Resp video(HttpRequest request, UploadFile uploadFile, Long chatlinkid) throws Exception {
        try {
            if (uploadFile == null) {
                return Resp.fail("上传信息为空");
            }
            if (chatlinkid == null) {
                return Resp.fail("会话id参数为空");
            }
            User curr = WebUtils.currUser(request);
            if (chatlinkid <= 0) {
                Ret check = GroupService.checkGroupMsg(-chatlinkid, curr.getId());
                if (check.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(check));
                }
                chatlinkid = RetUtils.getOkTData(check);
            }
            WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            Resp resp = processUploadedVideo(request, uploadFile, chatItems);
            if (!resp.isOk()) {
                return resp;
            }
            Video video = (Video) resp.getData();
            String text = Json.toJson(video);
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.VIDEO, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            } else {
                Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.VIDEO, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            }
        } catch (Exception e) {
            log.error("", e);
            return RetUtils.getSysErrorResp();
        }
    }

    /**
     * 发送图片-已调整
     *
     * @param request
     * @param uploadFile
     * @param groupid
     * @param touid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:44:17
     */
    @RequestPath(value = "/img")
    public Resp img(HttpRequest request, UploadFile uploadFile, Long chatlinkid) throws Exception {
        try {
            if (uploadFile == null) {
                return Resp.fail("上传信息为空");
            }
            if (chatlinkid == null) {
                return Resp.fail("会话id参数为空");
            }
            User curr = WebUtils.currUser(request);
            if (chatlinkid <= 0) {
                Ret check = GroupService.checkGroupMsg(-chatlinkid, curr.getId());
                if (check.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(check));
                }
                chatlinkid = RetUtils.getOkTData(check);
            }
            WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            Resp resp = processUploadedImg(request, uploadFile);
            if (!resp.isOk()) {
                return resp;
            }
            Img img = (Img) resp.getData();
            String text = Json.toJson(img);

            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.IMG, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            } else {
                Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.IMG, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            }
        } catch (Exception e) {
            log.error("", e);
            return RetUtils.getSysErrorResp();
        }
    }

    /**
     * 音频上传-已调整
     *
     * @param request
     * @param uploadFile
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月13日 下午2:18:45
     */
    @RequestPath(value = "/audio")
    public Resp audio(HttpRequest request, UploadFile uploadFile, Long chatlinkid) throws Exception {
        try {
            if (uploadFile == null) {
                return Resp.fail("上传信息为空");
            }
            if (chatlinkid == null) {
                return Resp.fail("会话id参数为空");
            }
            User curr = WebUtils.currUser(request);
            if (chatlinkid <= 0) {
                Ret check = GroupService.checkGroupMsg(-chatlinkid, curr.getId());
                if (check.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(check));
                }
                chatlinkid = RetUtils.getOkTData(check);
            }
            WxChatItems chatItems = chatService.getBaseChatItems(chatlinkid);
            if (chatItems == null) {
                return RetUtils.getInvalidResp();
            }
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                if (!ChatService.existTwoFriend(touserItem)) {
                    return Resp.fail("对方不是你的好友");
                }
            }
            Resp resp = processUploadedAudio(request, uploadFile, chatItems);
            if (!resp.isOk()) {
                return resp;
            }
            Audio audio = (Audio) resp.getData();
            String text = Json.toJson(audio);
            if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.AUDIO, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            } else {
                Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.AUDIO, chatItems.getUid(), chatItems.getBizid(), Const.YesOrNo.NO, null);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            }
        } catch (Exception e) {
            log.error("", e);
            return RetUtils.getSysErrorResp();
        }
    }

    /**
     * 获取声网token
     *
     * @param request
     * @param chatlinkid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月13日 下午2:18:45
     */
    @RequestPath(value = "/getToken")
    public Resp getToken(HttpRequest request, String channelName) throws Exception {

        String agoraOpen = ConfService.getString("agora.open", "0");
        String agoraAppId = ConfService.getString("agora.app.id", "");
        String agoraCertificate = ConfService.getString("agora.app.certificate", "");
        String agoraTokenExpirationSeconds = ConfService.getString("agora.app.token.expiration.seconds", "3600");
        String agoraPrivilegeExpirationSeconds = ConfService.getString("agora.app.privilege.expiration.seconds", "3600");

        if (!"1".equals(agoraOpen)) {
            return Resp.fail("未开启声网sdk，无法调用该接口");
        }
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }

        if (StrUtil.isBlank(agoraAppId) || StrUtil.isBlank(agoraCertificate)) {
            return Resp.fail("声网sdk异常, 未配置声网服务");
        }
        // 生成 Token
        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        String result = token.buildTokenWithUid(agoraAppId, agoraCertificate, channelName, curr.getId(), RtcTokenBuilder2.Role.ROLE_SUBSCRIBER,
                Integer.parseInt(agoraTokenExpirationSeconds), Integer.parseInt(agoraPrivilegeExpirationSeconds));
        Map<Object, Object> data = new HashMap<>();
        data.put("token", result);
        return Resp.ok(data);
    }

    /**
     * 上传文件-已调整
     * 可抽象TODO:lixinji
     *
     * @param curr
     * @param uploadFile
     * @param sessionid
     * @return
     * @author lixinji
     * 2020年2月5日 下午7:25:45
     */
    private static File innerUploadFile(User curr, UploadFile uploadFile, String sessionid) throws Exception {
        byte[] bs = uploadFile.getData();
        String filename = uploadFile.getName();
        String ext = FileUtil.extName(filename);
        if (StrUtil.isBlank(ext)) {
            ext = "bin"; // 默认格式
        }

        // 生成文件路径
        String urlWithoutExt = UploadUtils.newFile(Const.UPLOAD_DIR.WX_FILE, curr.getId(), filename);
        String url = urlWithoutExt + "." + ext;

        // 构建 Content-Type
        String contentType;
        switch (ext.toLowerCase()) {
            case "jpg":
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "mp4":
                contentType = "video/mp4";
                break;
            case "pdf":
                contentType = "application/pdf";
                break;
            default:
                contentType = "application/octet-stream";
        }

        // 上传文件到 R2
        try (InputStream inputStream = new ByteArrayInputStream(bs)) {
            UploadUtils.unificationUpload( url, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    url,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );
        }

        // 构造并保存数据库记录
        File dbFile = new File();
        dbFile.setExt(ext);
        dbFile.setFilename(filename);
        dbFile.setSession(sessionid);
        dbFile.setSize((long) bs.length);
        dbFile.setUid(curr.getId());
        dbFile.setUrl(url); // 存储的是相对路径，前端拼接 base_url 即可访问
        dbFile.save();

        return dbFile;
    }

    /**
     * 用户上传的视频-已调整
     * 可抽象TODO:lixinji
     *
     * @param request
     * @param uploadFile
     * @param groupid
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    private Resp processUploadedVideo(HttpRequest request, UploadFile uploadFile, WxChatItems chatItems) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }

        byte[] bs = uploadFile.getData();
        String filename = uploadFile.getName();
        String extName = FileUtil.extName(filename).toLowerCase();
        if (StrUtil.isBlank(extName)) {
            extName = "mp4"; // 默认格式
        }

        Integer uid = curr.getId();

        try {
            // 构建视频 objectKey
            String videoUrlWithoutExt = UploadUtils.newFile(Const.UPLOAD_DIR.WX_VIDEO, uid, filename);
            String videoUrl = videoUrlWithoutExt + "." + extName;

            // 构建封面 objectKey
            String coverExt = "jpg";
            String coverUrl = UploadUtils.newFile(Const.UPLOAD_DIR.WX_VIDEO, uid, FileUtil.mainName(filename)) + "." + coverExt;

            // 提取封面图
            BufferedImage coverImage = VideoUtils.generateCoverFromVideoNew(new ByteArrayInputStream(bs));

            // 压缩封面图
            byte[] coverBytes = ImgUtils.compressImage(coverImage, 1f, 0.6d, coverExt);

            UploadUtils.unificationUpload( videoUrl, new ByteArrayInputStream(bs), bs.length, "video/mp4");
            // 上传视频主文件到 R2
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    videoUrl,
//                    new ByteArrayInputStream(bs),
//                    bs.length,
//                    "video/mp4"
//            );
            UploadUtils.unificationUpload( coverUrl, new ByteArrayInputStream(coverBytes), coverBytes.length, "image/" + coverExt);
            // 上传封面图到 R2
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    coverUrl,
//                    new ByteArrayInputStream(coverBytes),
//                    coverBytes.length,
//                    "image/" + coverExt
//            );

            // 构造 Video 对象
            Video video = new Video();
            video.setUrl(videoUrl);            // 返回的是 R2 的 objectKey 路径
            video.setCoverurl(coverUrl);      // 同上
            video.setUid(uid);
            video.setCoverwidth(coverImage.getWidth());
            video.setCoverheight(coverImage.getHeight());
            video.setWidth(coverImage.getWidth());
            video.setHeight(coverImage.getHeight());
            video.setComefrom(Video.ComeFrom.IM_UPLOAD);
            video.setStatus((short) 1);
            video.setTitle(chatItems != null
                    ? "聊天者uid:" + chatItems.getUid() + "-模式：" + chatItems.getChatmode() + "-业务id：" + chatItems.getBizid()
                    : filename);
            video.setFilename(filename);
            video.setSession(request.getHttpSession().getId());
            boolean f = VideoService.me.save(video);
            return f ? Resp.ok(video) : Resp.fail().data(video);
        } catch (Exception e) {
            log.error("视频处理或上传失败", e);
            return Resp.fail("视频上传失败").code(500);
        }
    }

    /**
     * 用户上传的音频-已调整
     * 可抽象TODO:lixinji
     *
     * @param request
     * @param uploadFile
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月13日 下午2:20:45
     */
    @SuppressWarnings("resource")
    private Resp processUploadedAudio(HttpRequest request, UploadFile uploadFile, WxChatItems chatItems) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }

        Integer uid = curr.getId();
        byte[] bs = uploadFile.getData();
        String filename = uploadFile.getName();
        String extName = FileUtil.extName(filename).toLowerCase();

        // 只允许特定格式的音频
        if (!"mp3 wav m4a".contains(extName)) {
            return Resp.fail("仅支持 mp3/wav/m4a 格式的音频上传");
        }

        try {
            String urlWithoutExt = UploadUtils.newFile(Const.UPLOAD_DIR.WX_AUDIO, uid, filename);
            String url = urlWithoutExt + "." + extName;

            // 构建 Content-Type
            String contentType;
            switch (extName) {
                case "mp3":
                    contentType = "audio/mpeg";
                    break;
                case "wav":
                    contentType = "audio/wav";
                    break;
                case "m4a":
                    contentType = "audio/mp4";
                    break;
                default:
                    contentType = "application/octet-stream";
            }

            // 上传音频到 R2
            try (InputStream inputStream = new ByteArrayInputStream(bs)) {
                UploadUtils.unificationUpload( url, inputStream, bs.length, contentType);
//                CloudflareR2Utils.uploadFilePublic(
//                        Const.CloudflareR2.R2_BUCKET_NAME,
//                        url,
//                        inputStream,
//                        bs.length,
//                        contentType
//                );
            }

            // 使用 JavaCV 提取音频时长（使用临时文件中转）
            double seconds = getAudioDurationInSeconds(bs);

            // 构造并保存 Audio 对象
            Audio audio = new Audio();
            audio.setSeconds((int) seconds);
            audio.setUid(uid);
            audio.setUrl(url);
            audio.setFilename(filename);
            audio.save();
            return Resp.ok(audio);

        } catch (Exception e) {
            log.error("音频处理失败", e);
            return Resp.fail("音频处理失败：" + e.getMessage());
        }
    }


    public static double getAudioDurationInSeconds(byte[] audioBytes) throws Exception {
        // 创建临时文件
        java.io.File tempFile = java.io.File.createTempFile("audio-", ".tmp");
        tempFile.deleteOnExit();

        // 写入字节流到临时文件
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(audioBytes);
        }

        // 使用 FFmpeg 提取时长
        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(tempFile.getAbsolutePath());
        ff.start();
        long durationInMillis = ff.getLengthInTime() / 1000; // 总毫秒数
        ff.stop();

        return Math.ceil(durationInMillis / 1000.0); // 转换为秒，并向上取整
    }

    /**
     * 用户上传的图片-已调整
     * 可抽象TODO:lixinji
     *
     * @param request
     * @param uploadFile
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月5日 下午7:44:47
     */
    private Resp processUploadedImg(HttpRequest request, UploadFile uploadFile) throws Exception {
        User curr = WebUtils.currUser(request);
        byte[] imageBytes = uploadFile.getData();
        Resp ret = null;
        if (curr == null) {
            ret = Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }
        Integer uid = null;
        if (curr != null) {
            uid = curr.getId();
        } else {
            uid = 1;
        }
//		BufferedImage read = ImageIO.read(new ByteArrayInputStream(imageBytes));

        BufferedImage bi = ImgUtil.toImage(imageBytes);

        float scale = ImgUtils.calcScaleWithWidth(400, bi);
        Img img = ImgUtils.processImg(Const.UPLOAD_DIR.WX_IMG, uid, uploadFile, scale);
        img.setComefrom(Img.ComeFrom.WX_UPLOAD);
        img.setStatus((short) 1);
        img.setSession(request.getHttpSession().getId());
        boolean f = ImgService.me.save(img);
        if (ret != null) {
            return ret;
        }
        if (f) {
            return Resp.ok(img);
        } else {
            return Resp.fail("服务器异常");
        }
    }

    /************************************************end-upload-msg**************************************************/

    /**
     * 获取群用户的的头像-已调整
     *
     * @param uidArr
     * @param curr
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月13日 下午4:06:50
     */
    private static Ret getGroupImg(String[] uidArr, User curr, String name) throws Exception {
        List<String> avatarList = new ArrayList<>();
        avatarList.add(curr.getAvatar());
        boolean createName = StrUtil.isBlank(name) ? true : false;
        if (createName) {
            name = curr.getNick();
        }
        String nicks = "";
        int c = 0;
        for (String uidStr : uidArr) {
            if (StrUtil.isNotBlank(uidStr)) {
                int uid = Integer.parseInt(uidStr);
                if (Objects.equals(uid, curr.getId())) { //前面已经把当前用户加到了该群，所以此处略过
                    continue;
                }
                User user = UserService.ME.getById(uid);
                if (user != null) {
                    if (createName) {
                        String newName = name + "、" + user.getNick();
                        if (newName.length() < 30) {
                            name = newName;
                        }
                    }
                    nicks += "、" + user.getNick();
                    if (c >= 8) {
                        continue;
                    }
                    avatarList.add(user.getAvatar());
                    c++;
                }
            }
        }
        // 取消生成头像
//        Img img = AvatarUtils.generateGroupAvatar(avatarList, curr.getId());
//		log.error("img: {}", img);
        if (createName) {
            return Ret.ok().set("img", new Img()).set("nicks", nicks.substring(1)).set("name", name);
        } else {
            return Ret.ok().set("img", new Img()).set("nicks", nicks.substring(1));
        }
    }

    /**
     * 自动修改群头像和群名称-已调整
     * 1、群名称如果指定修改后，该方法无效
     * 2、群头像生成条件：A、人员减员；B、人员新增时，未满；C、群权限操作
     * 3、群昵称生成条，排除1后，A、2触发；B、上层逻辑触发
     *
     * @param request
     * @param devicetype
     * @param groupid
     * @param nameUpdate 名称修改标识：1：修改；2：不修改
     * @param groupname  1、修改后的群名称
     * @param joinnum    人数变动数据
     * @param curr
     * @throws Exception
     * @author lixinji
     * 2020年3月13日 下午5:20:25
     */
    public static boolean auotUpdateGroupInfo(HttpRequest request, Short devicetype, Long groupid, Short nameUpdate, Short joinnum, User curr) throws Exception {
        WxGroup group = groupService.getByGroupid(groupid);
        if (group == null || group.getJoinnum() == 0) {
            log.error("群自动修改头像，本群已解散：groupid-{}", groupid);
            return false;
        }
        //群名称是否重新生成
        boolean nameInit = false;
        String name = "";
        String oldName = group.getName();
        if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES) && Objects.equals(nameUpdate, Const.YesOrNo.YES)) { //群名称修改标识
            nameInit = true;
        }
        String avatar = ""; //头像是否生成
        //1、joinnum不为空；2：joinnum > 0时，新增用户判断；3:joinnum < 0时，退出或者被踢
        if (Objects.equals(group.getAvatarautoflag(), Const.YesOrNo.YES) && joinnum != null && ((joinnum > 0 && group.getJoinnum() - joinnum < 9) //新增用户判断,新增前是否已满头像
                || joinnum <= 0 //删除人或者操作，重新更新下头像
        )) { // 头像重新生成
            List<String> avatarList = new ArrayList<>();
            Ret ret = groupService.groupUserList(groupid, null, "");
            if (ret.isFail()) {

                return false;
            }
            Page<Record> page = RetUtils.getOkTPage(ret);
            if (page == null) {
                log.error("获取用户列表为空");
                return false;
            }
            List<Record> groupUserList = page.getList();
            int avatarCount = 0;
            for (Record record : groupUserList) {
                if (avatarCount > 8) {
                    break;
                }
                Integer uid = record.getInt("uid");
                User user = UserService.ME.getById(uid);
                if (user != null) {
                    avatarList.add(user.getAvatar());
                    avatarCount++;
                }
                if (nameInit) {
                    String newName = name + "、" + user.getNick();
                    if (newName.length() >= 31) {
                        continue;
                    }
                    name = newName;
                }
            }
            try {
                Img img = AvatarUtils.generateGroupAvatar(avatarList, groupUserList.get(0).getInt("uid"));
                avatar = img.getCoverurl();
                if (!avatar.equals(group.getAvatar())) {
                    groupService.modifyAvatar(groupid, avatar, true);
                } else {
                    avatar = "";
                }
            } catch (Exception e) {
                log.error("", e);
            }
        } else if (nameInit) { //头像不生成，名称需要重新生成
            Ret ret = groupService.groupUserList(groupid, null, "");
            if (ret.isFail()) {

                return false;
            }
            Page<Record> page = RetUtils.getOkTPage(ret);
            if (page == null) {
                log.error("获取用户列表为空");
                return false;
            }
            List<Record> groupUserList = page.getList();
            for (Record record : groupUserList) {
                Integer uid = record.getInt("uid");
                User user = UserService.ME.getById(uid);
                String newName = name + "、" + user.getNick();
                if (newName.length() >= 31) {
                    continue;
                }
                name = newName;
            }
        }
        if (StrUtil.isNotBlank(name)) {
            name = name.substring(1);
        }
        if (StrUtil.isNotBlank(name) && !oldName.equals(name)) {
            groupService.modifyName(curr.getId(), groupid, name, true);
        } else {
            name = "";
        }
        if (StrUtil.isNotBlank(name) || StrUtil.isNotBlank(avatar)) {
            return true;
        }
        return false;
    }


    /**
     * 消息合并转发
     */
    @RequestPath(value = "/msgMergeForward")
    public Resp msgMergeForward(HttpRequest request, WxUserMergeData wxUserMergeData) throws Exception {
        try {

            Integer touid = wxUserMergeData.getTouid();
            Long groupid = wxUserMergeData.getGroupid();
            String data = wxUserMergeData.getData();

            User curr = WebUtils.currUser(request);
            if (curr == null) {
                return Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
            }

            if (touid == null && groupid == null) {
                return Resp.fail("目标用户表示不能为空");
            }

            if (StrUtil.isBlank(data)) {
                return Resp.fail("消息不能为空");
            }

            String msg = wxUserMergeData.getMsg();
            int uid = curr.getId();
            long saveId = IdUtil.getSnowflake().nextId();
            //保存后的有限数据
            wxUserMergeData.setUid(uid);
            wxUserMergeData.setId(saveId);
            wxUserMergeData.setCreatetime(new Date());
            wxUserMergeData.setUpdatetime(new Date());
            wxUserMergeData.replaceSave();

            JSONObject dataJson = new JSONObject();
            dataJson.put("mergeid", saveId);
            if (StrUtil.isNotBlank(msg)) {
                dataJson.put("msg", msg);
            }

            dataJson.put("type", Const.MergeType.TYPE);
            String text = dataJson.toString();
            if (touid != null) {
                WxChatUserItem chatItems = ChatIndexService.fdUserIndex(uid, touid);
                if (chatItems == null) {
                    return RetUtils.getInvalidResp();
                }
                if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                    WxChatUserItem touserItem = ChatIndexService.fdUserIndex(chatItems.getBizid().intValue(), chatItems.getUid());
                    if (!ChatService.existTwoFriend(touserItem)) {
                        return Resp.fail("对方不是你的好友");
                    }
                }
                if (Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
                    Ret ret = WxChatApi.sendFdMsgEach(request, text, Const.ContentType.MERGE, chatItems.getUid(), chatItems.getBizid().intValue(), Const.YesOrNo.NO);
                    if (ret.isFail()) {
                        return Resp.fail(RetUtils.getRetMsg(ret));
                    }
                    return Resp.ok(RetUtils.OPER_RIGHT);
                }
            }

            if (groupid != null) {
                Ret ret = WxChatApi.sendGroupMsgEach(request, text, Const.ContentType.MERGE, curr.getId(), groupid, Const.YesOrNo.NO, null);
                if (ret.isFail()) {
                    return Resp.fail(RetUtils.getRetMsg(ret));
                }
                return Resp.ok(RetUtils.OPER_RIGHT);
            }

        } catch (Exception e) {
            log.error("", e);
            return RetUtils.getSysErrorResp();
        }
        return Resp.fail(RetUtils.OPER_ERROR);
    }

    /**
     * 合并转发消息查看
     */
    @RequestPath(value = "/showMergeMessage")
    public Resp showMergeMessage(HttpRequest request, Long mergeid) throws Exception {
        WxUserMergeData byId = WxUserMergeData.dao.findById(mergeid);
        return Resp.ok(byId);
    }


    /**
     * 查询黑名单列表
     */
    @RequestPath(value = "/blackList")
    public Resp blackList(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }
        Kv params = Kv.by("uid", curr.getId()).set("status", Const.Status.NORMAL);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.userBlock", params);
        List<WxUserBlackItems> wxUserBlackItems = WxUserBlackItems.dao.find(sqlPara);
        List<SimpleUser> result = new ArrayList<>(wxUserBlackItems.size());
        wxUserBlackItems.forEach(black -> {
            User user = UserService.ME.getById(black.getTouid());
            SimpleUser simpleUser = SimpleUser.fromUser(user);
            result.add(simpleUser);
        });
        return Resp.ok(result);
    }
}
