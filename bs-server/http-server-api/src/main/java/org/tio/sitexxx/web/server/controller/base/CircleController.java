package org.tio.sitexxx.web.server.controller.base;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.model.conf.Conf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.base.CircleService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.CloudflareR2Utils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.UploadUtils;
import org.tio.sitexxx.service.vo.CircleArticleMsgVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@RequestPath(value = "/circle")
public class CircleController {
    private static Logger log = LoggerFactory.getLogger(CircleController.class);
    private UserService userService = UserService.ME;
    private CircleService circleService = CircleService.ME;

    /**
     * 申请创建圈子
     * @param request
     * @param name
     * @param avatar
     * @param describe
     * @param isOpen
     * @param isExamine
     * @param isInvite
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/createCircleApply")
    public Resp createCircleApply(HttpRequest request, String name, String avatar, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) throws Exception {
        User currUser = WebUtils.currUser(request);
        if (currUser == null) {
            return Resp.fail().msg("请登录");
        }
        if (name == null || name.isEmpty()) {
            return Resp.fail("名称不能为空");
        }
        if (avatar == null || avatar.isEmpty()) {
            return Resp.fail("头像不能为空");
        }
        if (isOpen == null) {
            isOpen = 0;
        }
        if (isExamine == null) {
            isExamine = 0;
        }
        if (isInvite == null) {
            isInvite = 0;
        }
        if (inviteNum == null) {
            inviteNum = 0;
        }

        if (isOpen.equals(0)) {
            if (isInvite.equals(0)) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
            if (inviteNum < 1) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
        }

        Ret ret = circleService.applyCircle(currUser, name, avatar, describe, isOpen, isExamine, isInvite, inviteNum);
        return ret.get("data").equals(Const.YesOrNo.YES) ? Resp.ok("后台审核中") : Resp.fail("申请失败");
    }

    /**
     * 重新提交申请
     * @param request
     * @param circleApplyId
     * @param name
     * @param avatar
     * @param describe
     * @param isOpen
     * @param isExamine
     * @param isInvite
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/resubmitCircleApply")
    public Resp resubmitCircleApply(HttpRequest request, Integer circleApplyId, String name, String avatar, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) throws Exception {
        User currUser = WebUtils.currUser(request);
        if (name == null || name.isEmpty()) {
            return Resp.fail("名称不能为空");
        }
        if (avatar == null || avatar.isEmpty()) {
            return Resp.fail("头像不能为空");
        }
        if (isOpen == null) {
            isOpen = 0;
        }
        if (isExamine == null) {
            isExamine = 0;
        }
        if (isInvite == null) {
            isInvite = 0;
        }
        if (inviteNum == null) {
            inviteNum = 0;
        }
        if (isOpen.equals(0)) {
            if (isInvite.equals(0)) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
            if (inviteNum < 1) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
        }
        Ret ret = circleService.resubmitCircleApply(currUser, circleApplyId, name, avatar, describe, isOpen, isExamine, isInvite, inviteNum);
        return ret.get("data").equals(Const.YesOrNo.YES) ? Resp.ok("后台审核中") : Resp.fail(ret.get("errorMsg").toString());
    }

    /**
     * 圈子创建申请列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/circleApplyList")
    public Resp circleApplyList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("uid",user.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.circleapplylist", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        List<Record> list = records.getList();
        for (Record record : list) {
            Circle circle = Circle.dao.findById(record.get("id"));
            if (circle != null) {
                if (circle.getStatus().equals(2)) {
                    record.set("status", 3);
                }
                record.set("show_id", circle.getShowId());
            }
        }
//        List<CircleApply> circleApplies = CircleApply.dao.find("select * from circle_apply where uid = #para{} order by id desc", user.getId());
        return Resp.ok(records);
    }


    /**
     * 推荐列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/recommendList")
    public Resp recommendList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("uid", user.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.recommendList", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return Resp.ok(records);
    }

    /**
     * 圈子搜索
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/searchCircle")
    public Resp searchCircle(HttpRequest request, String searchkey,Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (searchkey == null || searchkey.isEmpty()) {
            return Resp.fail("搜索关键字不能为空");
        }
        Ret ret = circleService.searchCircle(user, searchkey, pageNumber, pageSize);
        return Resp.ok(RetUtils.getOkPage(ret));
    }


    /**
     * 生成邀请码
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/genInviteCode")
    public Resp genInviteCode(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if (CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId) == null) {
            return Resp.fail().msg("您不是该圈子的成员，无法生成邀请码");
        }
        Ret ret = circleService.genInviteCode(user, circleId);
        if (ret.isFail()) {
            return Resp.fail(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("data"));
    }

    /**
     * 邀请码列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/inviteCodeList")
    public Resp inviteCodeList(HttpRequest request, Integer circleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.inviteCodeList(user, circleId);
        if (ret.isFail()) {
            return Resp.fail(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("data"));
    }


    /**
     * 删除邀请码
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delInviteCode")
    public Resp delInviteCode(HttpRequest request, Integer inviteCodeId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.delInviteCode(user, inviteCodeId);
        if (ret.isFail()) {
            return Resp.fail(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }



    /**
     * 申请加入圈子前验证
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addCircleApplyBeforeCheck")
    public Resp addCircleApplyBeforeCheck(HttpRequest request, Integer circleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        if (circle.getIsOpen().equals(0)) {
            return Resp.fail().msg("私有圈子只能通过名片关注");
        }
        if (circle.getStatus().equals(2)) {
            return Resp.fail().msg("该圈子被封禁");
        }
        Map<String, Boolean> data = new HashMap<>();
        data.put("needInviteNum", false);
        if (circle.getIsInvite().equals(1)) {
            List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where use_uid = ? and is_invalid = 0", user.getId());
            if (circle.getInviteNum() > circleMemberInviteCodes.size()) {
                data.put("needInviteNum", true);
            }
        }

        return Resp.ok(data);
    }

    /**
     * 加入圈子申请
     * @param request
     * @param circleId
     * @param inviteCode
     * @param contentType 1:搜索关注，2:扫描二维码关注，3:名片关注
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addCircleApply")
    public Resp genInviteCode(HttpRequest request, Integer circleId, String inviteCode, Integer contentType) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("该圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Resp.fail().msg("该圈子被封禁");
        }
        if (circle.getIsOpen().equals(0) && !contentType.equals(3)) {
            return Resp.fail().msg("该圈子为私有圈子，只允许名片关注");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        if (circleMember != null) {
            return Resp.fail().msg("已是该圈子成员");
        }
        if (circle.getIsInvite().equals(0)) {
            if (circle.getIsExamine().equals(0)) {
                Ret ret1 = circleService.addCircleApply(user, circleId, "", contentType, 1);
                if (ret1.isFail()) {
                    return Resp.fail().msg("关注圈子失败，请重新关注");
                }
                Ret ret = circleService.addCircle(user, circleId, 0);
                if (ret.isFail()) {
                    return Resp.fail().msg("关注圈子失败，请重新关注");
                }
                circleService.addCircleLog(user, user, circleId, 1, contentType);
                return Resp.ok("加入成功");
            } else {
                Ret ret = circleService.addCircleApply(user, circleId, "", contentType, 0);
                if (ret.isFail()) {
                    return Resp.fail().msg("关注申请提交失败，请重新关注申请");
                }
                return Resp.ok("审核中");
            }
        } else {
            if (circle.getInviteNum().equals(0)) {
                if (circle.getIsExamine().equals(1)) {
                    Ret ret = circleService.addCircleApply(user, circleId, "", contentType, 0);
                    if (ret.isFail()) {
                        return Resp.fail().msg("关注申请提交失败，请重新关注申请");
                    }
                    return Resp.ok("审核中");
                } else {
                    Ret ret1 = circleService.addCircleApply(user, circleId, "", contentType, 1);
                    if (ret1.isFail()) {
                        return Resp.fail().msg("关注圈子失败，请重新关注");
                    }
                    Ret ret = circleService.addCircle(user, circleId, 0);
                    if (ret.isFail()) {
                        return Resp.fail().msg("关注圈子失败，请重新关注");
                    }
                    circleService.addCircleLog(user, user, circleId, 1, contentType);
                    return Resp.ok("加入成功");
                }

            }
            Ret ret = circleService.checkInviteCode(user, inviteCode, circleId);
            if (ret.get("data").equals(Const.YesOrNo.NO)) {
                return Resp.fail().msg(ret.get("errorMsg").toString());
            }
            Ret ret1 = circleService.checkInviteCodeNum(user, circleId);
            if (ret1.get("data").equals(Const.YesOrNo.NO)) {
                return Resp.fail().msg(ret1.get("errorMsg").toString());
            }
            if (circle.getIsExamine().equals(1)) {
                Ret ret2 = circleService.addCircleApply(user, circleId, "", contentType, 0);
                if (ret2.isFail()) {
                    return Resp.fail().msg("关注申请提交失败，请重新关注申请");
                }
                return Resp.ok("审核中");
            } else {
                Ret ret3 = circleService.addCircleApply(user, circleId, "", contentType, 1);
                if (ret3.isFail()) {
                    return Resp.fail().msg("关注圈子失败，请重新关注");
                }
                Ret ret2 = circleService.addCircle(user, circleId, 0);
                circleService.addCircleLog(user, user, circleId, 1, contentType);
                if (ret2.isFail()) {
                    return Resp.fail().msg("关注圈子失败，请重新关注");
                }
                return Resp.ok("加入成功");
            }
        }
    }

    /**
     * 圈子审核列表
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/memberApplyList")
    public Resp memberApplyList(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Resp.fail().msg("圈子状态异常");
        }
        User user = WebUtils.currUser(request);
        Ret ret = circleService.memberApplyList(user, circleId);
        if (ret.get("data").equals(Const.YesOrNo.NO)) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }

        return Resp.ok(ret.get("list"));
    }

    /**
     * 审核申请
     * @param request
     * @param applyIds
     * @param circleId
     * @param status
     * @param refuseReason
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/examineApply")
    public Resp examineApply(HttpRequest request, String applyIds, Integer circleId, Integer status, String refuseReason) throws Exception {
//        Circle circle = Circle.dao.findById(circleId);
//        if (circle == null) {
//            return Resp.fail().msg("圈子不存在");
//        }
//        if (!circle.getStatus().equals(1)) {
//            return Resp.fail().msg("圈子状态异常");
//        }
//        if (status == null) {
//            return Resp.fail().msg("审核结果不能为空");
//        }

        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.examineApply(user, circleId, applyIds, status, refuseReason);
        if (ret.get("data").equals(Const.YesOrNo.NO)) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("msg"));
    }


    /**
     * 关注圈子列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/circleList")
    public Resp circleList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.circleList(user, pageNumber, pageSize);
        return Resp.ok(RetUtils.getOkPage(ret));
    }


    /**
     * 圈子加入审核列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addCircleApplyList")
    public Resp addCircleApplyList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.addCircleApplyList(user, pageNumber, pageSize);
        return Resp.ok(RetUtils.getOkPage(ret));
    }

    /**
     * 查看圈子详情
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/circleInfo")
    public Resp addCircleApplyList(HttpRequest request, Integer circleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ? order by role", circleId);
        List<CircleMemberApply> circleMemberApplyList = CircleMemberApply.dao.find("select * from circle_member_apply where circle_id = ? and status = 0", circleId);
        Ret ret = circleService.isMember(user, circleId);
        HashMap<String, Object> result = new HashMap<>();
        result.put("circle", circle);
        result.put("member", circleMembers);
        result.put("applyMember", circleMemberApplyList);
        result.put("isCircleMember", ret.get("isMember"));
        if (ret.get("isMember").equals(Short.valueOf("1"))) {
            result.put("role", ret.get("role"));
        }
        return Resp.ok(result);
    }

    /**
     * 退出圈子
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/leaveCircle")
    public Resp leaveCircle(HttpRequest request, Integer circleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        Ret ret = circleService.leaveCircle(user, circleId);
        if (ret.get("data").equals(Const.YesOrNo.NO)) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        circleService.addCircleLog(user,user,circleId,2,1);
        return Resp.ok("退出成功");
    }

    /**
     * 修改圈子信息
     * @param request
     * @param circleId
     * @param avatar
     * @param name
     * @param describe
     * @param isOpen
     * @param isExamine
     * @param isInvite
     * @param inviteNum
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/updateCircle")
    public Resp updateCircle(HttpRequest request, Integer circleId, String avatar, String name, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }

        if (circleId == null) {
            return Resp.fail().msg("请选择需要修改的圈子");
        }
        if (avatar == null && name == null && describe == null && isOpen == null && isExamine == null && isInvite == null && inviteNum == null) {
            return Resp.fail().msg("无修改内容");
        }
        if (avatar != null && avatar.isEmpty()) {
            return Resp.fail().msg("圈子头像不能为空");
        }
        if (name != null && name.isEmpty()) {
            return Resp.fail().msg("圈子名称不能为空");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        Ret ret = circleService.checkRole(user, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不可进行该操作");
        }

        if (isOpen == null) {
            isOpen = circle.getIsOpen();
        }
        if (isExamine == null) {
            isExamine = circle.getIsExamine();
        }
        if (isInvite == null) {
            isInvite = circle.getIsInvite();
        }
        if (inviteNum == null) {
            inviteNum = circle.getInviteNum();
        }
        if (isOpen.equals(0)) {
            if (isInvite.equals(0)) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
            if (inviteNum < 1) {
                return Resp.fail("私有圈子必须开启邀请码, 且邀请人数必须大于或等于1");
            }
        }
        Ret ret1 = circleService.updateCircle(user, circleId, avatar, name, describe, isOpen, isExamine, isInvite, inviteNum);
        if (ret1.isFail()) {
            return Resp.fail().msg("操作失败，请重试");
        }
        return Resp.ok("修改成功");
    }


    /**
     * 普通成员列表(可添加管理员成员列表)
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/generalMemberList")
    public Resp generalMemberList(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret1 = circleService.checkRole(user, circleId);
        if (ret1.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret1.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不可查看该信息");
        }
        Ret ret = circleService.generalMemberList(circleId);

        return Resp.ok(ret.get("generalMemberList"));
    }

    /**
     * 添加管理员
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addCircleManager")
    public Resp addCircleManager(HttpRequest request, Integer circleId, String uids) throws Exception {
        if (uids == null || uids.isEmpty()) {
            return Resp.fail().msg("未选择添加成员");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        Ret ret = circleService.checkRole(user, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不能进行该操作");
        }
        Ret ret1 = circleService.addCircleManager(circleId, uids);
        if (ret1.isFail()) {
            return Resp.fail().msg(ret1.get("errorMsg").toString());
        }
        return Resp.ok("添加成功");
    }


    /**
     * 管理员列表
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/managerMemberList")
    public Resp managerMemberList(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret1 = circleService.checkRole(user, circleId);
        if (ret1.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret1.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不可查看该信息");
        }
        Ret ret = circleService.managerMemberList(circleId);

        return Resp.ok(ret.get("managerMemberList"));
    }

    /**
     * 成员列表
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/memberList")
    public Resp memberList(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret1 = circleService.checkRole(user, circleId);
        if (ret1.isFail()) {
            return Resp.fail().msg("非圈子成员不可查看该信息");
        }
        if (ret1.get("role").equals(3)) {
            return Resp.fail().msg("普通成员不可查看该信息");
        }
        Ret ret = circleService.memberList(circleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("memberList"));
    }

    /**
     * 删除管理员
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delCircleManager")
    public Resp delCircleManager(HttpRequest request, Integer circleId, String uids) throws Exception {
        if (uids == null || uids.isEmpty()) {
            return Resp.fail().msg("未选择添加成员");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        Ret ret = circleService.checkRole(user, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不能进行该操作");
        }
        Ret ret1 = circleService.delCircleManager(circleId, uids);
        if (ret1.isFail()) {
            return Resp.fail().msg("操作失败，请重试");
        }
        return Resp.ok("删除成功");
    }


    /**
     * 转让圈主
     * @param request
     * @param circleId
     * @param uid
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/transferCircle")
    public Resp transferCircle(HttpRequest request, Integer circleId, Integer uid) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if (user.getId().equals(uid)) {
            return Resp.fail().msg("不可以将圈主转让给自己");
        }
        Ret ret1 = circleService.checkRole(user, circleId);
        if (ret1.isFail()) {
            return Resp.fail().msg("您不是该圈子成员");
        }
        if (!ret1.get("role").equals(1)) {
            return Resp.fail().msg("非圈主不可进行该操作");
        }
        Ret ret = circleService.transferCircle(user, circleId, uid);
        if (ret.isFail()) {
            return Resp.fail(ret.get("errorMsg").toString());

        }
        return Resp.ok("操作成功");
    }


    /**
     * 踢出用户
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delMember")
    public Resp delMember(HttpRequest request, Integer circleId, String uids) throws Exception {
        if (uids == null || uids.isEmpty()) {
            return Resp.fail().msg("未选择踢出用户");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.delMember(user, circleId, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("删除成功");
    }

    /**
     * 解散
     * @param request
     * @param circleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delCircle")
    public Resp delCircle(HttpRequest request, Integer circleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.delCircle(user, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("删除成功");
    }


    /**
     * 上传文件
     * @param request
     * @param file
     * @param type 1：图片 2：视频
     * @return
     */
    @RequestPath("/uploadFile")
    public Resp uploadFile(HttpRequest request, UploadFile file, Integer type) {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        if (file == null) {
            return Resp.fail("参数异常");
        }

        byte[] bs = file.getData();
        String filename = file.getName();
        String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

        // 只允许特定格式的图片
        if (!"jpg jpeg png bmp".contains(extName)) {
            return Resp.fail("仅支持 jpg/jpeg/png/bmp 格式的图片上传");
        }

        try {
            String objectKeyPrefix;
            if (type.equals(1)) {
                objectKeyPrefix = "circle/img";
            } else {
                objectKeyPrefix = "circle/video";
            }

            String objectKey = UploadUtils.dateFile(objectKeyPrefix) + "." + extName;

            // 构建 Content-Type
            String contentType;
            if ("jpg jpeg".contains(extName)) {
                contentType = "image/jpeg";
            } else if ("png".equals(extName)) {
                contentType = "image/png";
            } else if ("bmp".equals(extName)) {
                contentType = "image/bmp";
            } else {
                contentType = "application/octet-stream";
            }

            // 上传文件到 R2
            InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    objectKey,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );

            return Resp.ok(objectKey); // 返回相对路径，前端拼接 base_url 获取完整 URL

        } catch (Exception e) {
            log.error("文件上传到 R2 异常", e);
            return Resp.fail().code(500).msg("文件上传失败");
        }
    }

    /**
     * 添加圈子文章
     * @param request
     * @param circleIds
     * @param cityIds
     * @param content
     * @param videoUrl
     * @param imgUrl
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addCircleArticle")
    public Resp addCircleArticle(HttpRequest request,  String circleIds, String cityIds, String content, String videoUrl, String imgUrl) throws Exception {
        if (circleIds == null || circleIds.isEmpty()) {
            return Resp.fail().msg("请选择该文章发表的圈子");
        }
        if (cityIds == null || cityIds.isEmpty()) {
            return Resp.fail().msg("请选择该文章发表的城市");
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if ((content == null || content.isEmpty())  &&
                (videoUrl == null || videoUrl.isEmpty()) &&
                (imgUrl == null ||  imgUrl.isEmpty())) {
            return Resp.fail("请输入文章内容");
        }
        if ((videoUrl != null && !videoUrl.isEmpty()) &&
                (imgUrl !=null && !imgUrl.isEmpty())) {
            return Resp.fail("发表的文章内容不能同时包含视频和图片");
        }
        if (imgUrl != null && imgUrl.split(",").length > 9) {
            return Resp.fail("最多只能上传 9 张图片");
        }
        if (videoUrl != null && videoUrl.split(",").length > 1) {
            return Resp.fail("最多只能上传 1 个视频");
        }
        Ret ret = circleService.addCircleArticle(user, circleIds, cityIds, content, videoUrl, imgUrl);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        String[] circleIdList = circleIds.split(",");
        List<Integer> sendMsgUserList = new ArrayList<>();
        for (String circleId : circleIdList) {
            List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ?", circleId);
            if (circleMembers != null) {
                for (CircleMember circleMember : circleMembers) {
                    boolean sendMsg = false;
                    if (user.getId().equals(circleMember.getUid())) {
                        continue;
                    }
                    if (sendMsgUserList.contains(circleMember.getUid())) {
                        continue;
                    }
                    CircleUidCircleidSetting showCircle = CircleUidCircleidSetting.dao.findFirst("select * from circle_uid_circleid_setting where uid = ? and circle_id = ?", circleMember.getUid(), circleId);
                    if (showCircle == null) {
                        continue;
                    }
                    String[] cityIdList = cityIds.split(",");
                    for (String cityId : cityIdList) {
                        CircleUidCityidSetting showCity = CircleUidCityidSetting.dao.findFirst("select * from circle_uid_cityid_setting where uid = ? and city_id = ?", circleMember.getUid(), cityId);
                        if (showCity != null) {
                            sendMsg = true;
                            break;
                        }
                    }
                    if (sendMsg) {
                        WxChatApi.useSysChatNtf(request, circleMember.getUid(), Const.WxSysCode.PUBLISH_CIRCLE_ARTICLE, "新的圈子文章发布", null);
                        CircleMsg circleMsg = new CircleMsg();
                        circleMsg.setUid(user.getId());
                        circleMsg.setToUid(circleMember.getUid());
                        circleMsg.setText("新的圈子文章发布");
                        circleMsg.setResume("新的圈子文章发布");
                        circleMsg.setReadFlag(2);
                        circleMsg.setTime(new Date());
                        circleMsg.setCreateTime(new Date());
                        circleMsg.setContentType(1);
                        circleMsg.save();
                        sendMsgUserList.add(circleMember.getUid());
                    }
                }
            }
        }

        return Resp.ok("发布成功");
    }

    /**
     * 删除文章
     * @param request
     * @param circleArticleId
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delCircleArticle")
    public Resp delCircleArticle(HttpRequest request, Integer circleArticleId) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        if (circleArticleId == null) {
            return Resp.fail().msg("请选择删除文章");
        }
        Ret ret = circleService.delCircleArticle(curr, circleArticleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("删除成功");
    }

    /**
     * 圈子广场设置
     * @param request
     * @param circleIds
     * @param cityIds
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/setViewSetting")
    public Resp setViewSetting(HttpRequest request, String circleIds, String cityIds) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        if (circleIds == null || circleIds.isEmpty()) {
            return Resp.fail().msg("请选择可见的圈子");
        }
        if (cityIds == null || cityIds.isEmpty()) {
            return Resp.fail().msg("请选择可见的城市");
        }
        Ret ret = circleService.setViewSetting(curr, circleIds, cityIds);

        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("设置成功");
    }

    /**
     * 查看圈子广场设置
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/viewSetting")
    public Resp viewSetting(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.viewSetting(curr);

        return Resp.ok(ret.get("viewSetting"));
    }

    /**
     * 查询所有城市信息
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/cityList")
    public Resp cityList(HttpRequest request, String searchKey) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        List<City> cityList = new ArrayList<>();
        if (searchKey == null) {
            cityList = City.dao.find("select * from city c where c.city like '%%' ORDER BY CONVERT(c.city USING gbk) COLLATE gbk_chinese_ci ASC");
        } else {
            searchKey = "%" + searchKey + "%";
            cityList = City.dao.find("select * from city c where c.city like ? ORDER BY CONVERT(c.city USING gbk) COLLATE gbk_chinese_ci ASC", searchKey);
        }
        return Resp.ok(cityList);
    }

    /**
     * 获取推荐城市列表
     * @param request
     * @return
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/cityRecommendList")
    public Resp cityList(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        List<City> cityList = City.dao.find("select * from city c where is_recommend = 1 ORDER BY CONVERT(c.city USING gbk) COLLATE gbk_chinese_ci ASC");
        return Resp.ok(cityList);
    }


    /**
     * 新增文章评论
     * @param articleId
     * @param pid
     * @param content
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/addCircleArticleComment")
    public Resp addCircleArticleComment(HttpRequest request, Integer articleId, Integer pid,  String content) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        if (pid == null) {
            pid = 0;
        }
        Ret ret = circleService.addCircleArticleComment(user, articleId, pid, content);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        WxChatApi.useSysChatNtf(request, Integer.valueOf(ret.get("articleUid").toString()), Const.WxSysCode.PUBLISH_CIRCLE_ARTICLE_COMMENT, "新的圈子文章评论", null);

        CircleMsg circleMsg = new CircleMsg();
        circleMsg.setUid(user.getId());
        circleMsg.setToUid(Integer.valueOf(ret.get("articleUid").toString()));
        circleMsg.setText("{\"title\":\"您有一条新的圈子消息\", \"type\":\"comment\", \"id\":\""+ ret.get("commentId") + "\", \"text\":\""+content+"\", \"articleId\":\""+articleId+"\", \"pid\":\""+pid+"\"}");
        circleMsg.setResume("一条评论消息");
        circleMsg.setReadFlag(2);
        circleMsg.setTime(new Date());
        circleMsg.setCreateTime(new Date());
        circleMsg.setContentType(2);
        circleMsg.save();
        if (!pid.equals(0)) {
            WxChatApi.useSysChatNtf(request, Integer.valueOf(ret.get("pCommentUid").toString()), Const.WxSysCode.PUBLISH_CIRCLE_ARTICLE_COMMENT, "新的圈子文章评论", null);
            CircleMsg circleMsgToPUser = new CircleMsg();
            circleMsgToPUser.setUid(user.getId());
            circleMsgToPUser.setToUid(Integer.valueOf(ret.get("articleUid").toString()));
            circleMsgToPUser.setText("{\"title\":\"您有一条新的圈子消息\", \"type\":\"comment\", \"id\":\""+ ret.get("commentId") + "\", \"text\":\""+content+"\", \"articleId\":\""+articleId+"\", \"pid\":\""+pid+"\"}");
            circleMsgToPUser.setResume("一条评论消息");
            circleMsgToPUser.setReadFlag(2);
            circleMsgToPUser.setTime(new Date());
            circleMsgToPUser.setCreateTime(new Date());
            circleMsgToPUser.setContentType(2);
            circleMsgToPUser.save();
        }
        return Resp.ok(ret.get("comment"));
    }


    /**
     * 删除文章评论
     * @param commentId
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/delCircleArticleComment")
    public Resp delCircleArticleComment(HttpRequest request, Integer commentId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        Ret ret = circleService.delCircleArticleComment(user, commentId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("删除成功");
    }



    /**
     * 文章点赞
     * @param articleId
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/addCircleArticleLike")
    public Resp addCircleArticleLike(HttpRequest request, Integer articleId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        Ret ret = circleService.addCircleArticleLike(user, articleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        WxChatApi.useSysChatNtf(request, Integer.valueOf(ret.get("articleUid").toString()), Const.WxSysCode.PUBLISH_CIRCLE_ARTICLE_LIKE, "新的圈子文章点赞", null);
        CircleMsg circleMsg = new CircleMsg();
        circleMsg.setUid(user.getId());
        circleMsg.setToUid(Integer.valueOf(ret.get("articleUid").toString()));
        circleMsg.setText("{\"title\":\"您有一条新的朋友圈消息\", \"type\":\"like\", \"id\":\""+ret.get("likeId")+"\", \"text\":\"\", \"articleId\":\""+articleId+"\", \"pid\":\"\"}");
        circleMsg.setResume("一条点赞消息");
        circleMsg.setReadFlag(2);
        circleMsg.setTime(new Date());
        circleMsg.setCreateTime(new Date());
        circleMsg.setContentType(3);
        circleMsg.save();
        return Resp.ok(ret.get("like"));
    }

    /**
     * 取消文章点赞
     * @param likeId
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/cancelCircleArticleLike")
    public Resp cancelCircleArticleLike(HttpRequest request, Integer likeId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        Ret ret = circleService.cancelCircleArticleLike(user, likeId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("删除成功");
    }

    /**
     * 查询圈子文章列表
     * @param request
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/circleArticleList")
    public Resp circleArticleList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        Ret ret = circleService.circleArticleList(curr.getId(), pageNumber, pageSize);
        circleService.readNewArticleMsg(curr.getId());
        return Resp.ok(ret.get("result"));
    }

    /**
     * 查询圈子文章列表
     * @param request
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/circleArticleListByUid")
    public Resp circleArticleListByUid(HttpRequest request, Integer uid, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        Ret ret;
        if (uid == null || uid.equals(0)) {
            ret = circleService.circleArticleListByUid(curr.getId(), curr.getId(), pageNumber, pageSize);
        } else {
            ret = circleService.circleArticleListByUid(curr.getId(), uid, pageNumber, pageSize);
        }
        circleService.readNewArticleMsg(curr.getId());
        return Resp.ok(ret.get("result"));
    }

    /**
     * 获取指定文章的具体内容
     * @param request
     * @param articleId
     * @return
     * @throws Exception
     * @author xin ji
     */
    @RequestPath(value = "/getCircleArticleById")
    public Resp getCircleArticleById(HttpRequest request, Integer articleId) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.getCircleArticleById(curr, articleId);

        return Resp.ok(ret.get("article"));
    }

    /**
     * 获取评论和点赞消息列表
     * @param request
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @RequestPath(value = "/getArticleMsgList")
    public Resp getMomentsMsgList(HttpRequest request, Integer pageNumber, Integer pageSize) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }

        List<CircleArticleMsgVo> result = circleService.CircleArticleMsgVo(curr, pageNumber, pageSize);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());
        circleService.readNewArticleCommentAndLikeMsg(curr.getId());
        return Resp.ok(result);
    }



    /**
     * 获取最新一条圈子文章推送消息
     * @param request
     * @return
     */
    @RequestPath(value = "/getArticleReadFlag")
    public Resp getArticleReadFlag(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        CircleMsg circleMsg = CircleMsg.dao.findFirst("select * from circle_msg where to_uid = ? and content_type = 1 order by create_time desc", curr.getId());
        HashMap<String, Object> result = new HashMap<>();
        if (circleMsg == null) {
            return Resp.ok();
        }
        User fromUser = User.dao.findById(circleMsg.getUid());
        if (fromUser != null) {
            result.put("msg", circleMsg);
            result.put("fromUserUid", fromUser.getId());
            result.put("fromUserAvatar", fromUser.getAvatar());
            return Resp.ok(result);
        }
        return Resp.ok(circleMsg);
    }

    /**
     * 清空圈子评论和点赞消息
     * @param request
     * @return
     */
    @RequestPath(value = "/clearArticleMsgs")
    public Resp clearArticleMsgs(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        circleService.clearArticleMsgs(curr);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());

        return Resp.ok();
    }

    /**
     * 评论和点赞未读数
     * @param request
     * @return
     */
    @RequestPath(value = "/unreadCount")
    public Resp unreadCount(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        int count = circleService.unreadCount(curr);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount",count);
        return Resp.ok(result);
    }

    /**
     * 名片转发
     * @param request
     * @return
     */
    @RequestPath(value = "/circleSendCard")
    public Resp circleSendCard(HttpRequest request, String sendId, Short type, Integer circleId) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = WxChatApi.circleSendCard(request, curr, sendId, type, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 上传投诉图片
     * @param request
     * @param file
     * @return
     */
    @RequestPath(value = "/uploadComplaintFile")
    public Resp uploadComplaintFile(HttpRequest request, UploadFile file) {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        if (file == null) {
            return Resp.fail("参数异常");
        }

        byte[] bs = file.getData();
        String filename = file.getName();
        String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

        // 只允许特定格式的图片
        if (!"jpg jpeg png bmp".contains(extName)) {
            return Resp.fail("仅支持 jpg/jpeg/png/bmp 格式的图片上传");
        }

        try {
            String objectKey =  UploadUtils.dateFile("circle/img/complaint/") + "." + extName;

            // 构建 Content-Type
            String contentType;
            switch (extName) {
                case "jpg":
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "bmp":
                    contentType = "image/bmp";
                    break;
                default:
                    contentType = "application/octet-stream";
            }

            // 上传文件到 R2
            InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    objectKey,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );

            // 返回相对路径，前端拼接 base_url 获取完整 URL
            return Resp.ok(objectKey);

        } catch (Exception e) {
            log.error("文件上传到 R2 异常", e);
            return Resp.fail().code(500).msg("文件上传失败");
        }
    }

    /**
     * 投诉
     * @param request 请求 request
     * @param circleId  圈子id
     * @param reason   投诉原因
     * @param reasonImg 图片证明
     * @return
     */
    @RequestPath(value = "/addComplaint")
    public Resp addComplaint(HttpRequest request, Integer circleId, String reason, String reasonImg) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.addComplaint(curr, circleId, reason, reasonImg);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok("投诉成功，请等待审核");
    }

    /**
     * 投诉列表
     * @param request 请求 request
     * @return
     */
    @RequestPath(value = "/complaintList")
    public Resp complaintList(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.complaintList(curr);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("complaintList"));
    }

    /**
     * 进出日志列表
     * @param request 请求 request
     * @param type 请求 1:进入日志，2:离开日志
     * @param circleId 圈子id
     * @return
     */
    @RequestPath(value = "/circleLogList")
    public Resp circleLogList(HttpRequest request, Integer circleId,  Integer type) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.circleLogList(curr, type, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("circleLogList"));
    }

    /**
     * 日志查询
     * @param request 请求 request
     * @param searchKey 搜索关键字
     * @param circleId 圈子id
     * @return
     */
    @RequestPath(value = "/searchCircleLog")
    public Resp searchCircleLog(HttpRequest request, Integer circleId,  String searchKey) {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = circleService.searchCircleLog(curr, searchKey, circleId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("result"));
    }


    /**
     * 获取圈子系统配置
     * @param request 请求 request
     * @return
     */
    @RequestPath(value = "/getConf")
    public Resp getConf(HttpRequest request) {
        List<Conf> confList = Conf.dao.find("select * from conf where name like '%circle%'");
        return Resp.ok(confList);
    }
}
