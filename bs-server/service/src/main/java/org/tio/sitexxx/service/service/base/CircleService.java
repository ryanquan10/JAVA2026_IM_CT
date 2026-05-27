package org.tio.sitexxx.service.service.base;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
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
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.utils.PyUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.CircleArticleMsgVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.WxMomentMsgVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.resp.Resp;

import java.util.*;

public class CircleService {
    private static Logger log = LoggerFactory.getLogger(CircleService.class);

    public static final CircleService ME = new CircleService();

    /**
     * 申请创建圈子
     * @param currUser
     * @param name
     * @param avatar
     * @param describe
     * @param isOpen
     * @param isExamine
     * @param isInvite
     * @param inviteNum
     * @return
     */
    public Ret applyCircle(User currUser, String name, String avatar, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) {
        CircleApply circleApply = new CircleApply();
        circleApply.setUid(currUser.getId());
        circleApply.setName(name);
        circleApply.setAvatar(avatar);
        circleApply.setDescribe(describe);
        circleApply.setIsOpen(isOpen);
        circleApply.setIsInvite(isInvite);
        circleApply.setIsExamine(isExamine);
        circleApply.setInviteNum(inviteNum);
        circleApply.setStatus(0);
        circleApply.setApplyTime(new Date());
        boolean save = circleApply.save();
        if (save) {
            return RetUtils.okData(Const.YesOrNo.YES);
        }
        return RetUtils.okData(Const.YesOrNo.NO);
    }


    /**
     * 重新提交圈子申请
     * @param currUser
     * @param circleApplyId
     * @param name
     * @param avatar
     * @param describe
     * @param isOpen
     * @param isExamine
     * @param isInvite
     * @param inviteNum
     * @return
     */
    public Ret resubmitCircleApply(User currUser, Integer circleApplyId, String name, String avatar, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) {
        CircleApply circleApply = CircleApply.dao.findById(circleApplyId);
        if (circleApply == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","该申请不存在");
        }
        circleApply.setUid(currUser.getId());
        circleApply.setName(name);
        circleApply.setAvatar(avatar);
        circleApply.setDescribe(describe);
        circleApply.setIsOpen(isOpen);
        circleApply.setIsInvite(isInvite);
        circleApply.setIsExamine(isExamine);
        circleApply.setInviteNum(inviteNum);
        circleApply.setStatus(0);
        circleApply.setApplyTime(new Date());
        boolean update = circleApply.update();
        if (update) {
            return RetUtils.okData(Const.YesOrNo.YES);
        }
        return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","重新提交失败");
    }

    public Ret searchCircle(User user, String searchkey, Integer pageNumber, Integer pageSize) {
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 5;
        }
        Kv params = Kv.create();
        if(StrUtil.isNotBlank(searchkey)) {
            params.set("id", searchkey);
            params.set("searchkey", "%" + searchkey + "%");
        }
        params.set("status", 1);
        params.set("searchuid", user.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.search", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return RetUtils.okPage(records);
    }

    public Ret genInviteCode(User user, Integer circleId) {
        CircleMemberInviteCode circleMemberInviteCode = new CircleMemberInviteCode();
        circleMemberInviteCode.setCreateUid(user.getId());
        String inviteCode = "";
        while (true) {
            Random rand = new Random();
            inviteCode = (rand.nextInt(900000) + 100000) + "";
//            inviteCode = generateRandomString();
            if (CircleMemberInviteCode.dao.findFirst("select * from circle_member_invite_code where invite_code = ?", inviteCode) == null) {
                break;
            }
        }
        if (CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and create_uid = ? and is_del = 0 and is_use = 0 and is_invalid = 0", circleId, user.getId()).size() >=5) {
            return Ret.fail().set("errorMsg", "邀请码上限为5个");
        }
        circleMemberInviteCode.setCircleId(circleId);
        circleMemberInviteCode.setInviteCode(inviteCode);
        circleMemberInviteCode.setCreateTime(new Date());
        boolean save = circleMemberInviteCode.save();
        if (!save) {
            return Ret.fail().set("errorMsg", "生成失败，请重试");
        }
        return RetUtils.okData(circleMemberInviteCode);

    }

    public static String generateRandomString() {

        Random random = new Random();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {

            int randomNum = random.nextInt(62); // 生成0到61之间的随机数

            char randomChar = (char) (randomNum + 33); // 将随机数转换为字符

            if (randomChar >= '0' && randomChar <= '9' || randomChar >= 'a' && randomChar <= 'z' || randomChar >= 'A' && randomChar <= 'Z') {

                sb.append(randomChar); // 将字符添加到StringBuilder中

            } else {

                i--; // 如果生成的字符不是数字或字母，则重新循环

            }

        }

        return sb.toString(); // 将StringBuilder转换为字符串并返回

    }

    /**
     * 加入圈子
     * @param user
     * @param circleId
     * @param inviteUid
     * @return
     */
    public Ret addCircle(User user, Integer circleId, Integer inviteUid) {
        CircleMember circleMember = new CircleMember();
        circleMember.setUid(user.getId());
        circleMember.setCircleId(circleId);
        circleMember.setAvatar(user.getAvatar());
        circleMember.setNick(user.getNick());
        circleMember.setInviteUid(inviteUid);
        circleMember.setCreateTime(new Date());
        boolean save = circleMember.save();
        if (!save) {
            return RetUtils.failOper();
        }
        return RetUtils.okOper();
    }

    public Ret addCircleApply(User user, Integer circleId, String inviteCode, Integer contentType, Integer status) {
        CircleMemberApply circleMemberApply = CircleMemberApply.dao.findFirst("select * from circle_member_apply where circle_id = ? and uid = ?", circleId, user.getId());
        if (circleMemberApply == null) {
            CircleMemberApply circleMemberApply1 = new CircleMemberApply();
            circleMemberApply1.setUid(user.getId());
            circleMemberApply1.setCircleId(circleId);
            circleMemberApply1.setInviteCode(inviteCode);
            circleMemberApply1.setCreateTime(new Date());
            circleMemberApply1.setStatus(status);
            circleMemberApply1.setContentType(contentType);
            boolean save = circleMemberApply1.save();
            if (!save) {
                return RetUtils.failOper();
            }
            return RetUtils.okOper();
        }
        circleMemberApply.setUid(user.getId());
        circleMemberApply.setCircleId(circleId);
        circleMemberApply.setInviteCode(inviteCode);
        circleMemberApply.setCreateTime(new Date());
        circleMemberApply.setStatus(status);
        circleMemberApply.setRefuseReason("");
        circleMemberApply.setContentType(contentType);
        boolean save = circleMemberApply.update();
        if (!save) {
            return RetUtils.failOper();
        }
        return RetUtils.okOper();
    }

    public Ret checkInviteCode(User user, String inviteCode, Integer circleId) {
        CircleMemberInviteCode circleMemberInviteCode = CircleMemberInviteCode.dao.findFirst("select * from circle_member_invite_code where invite_code = ? and is_del = 0 and circle_id = ? and is_invalid = 0", inviteCode, circleId);
        if (circleMemberInviteCode == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "邀请码错误");
        }
        if (circleMemberInviteCode.getIsUse().equals(1)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "该邀请码已被使用");
        }
        List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where use_uid = ? and create_uid = ? and circle_id = ? and is_invalid = 0", user.getId(), circleMemberInviteCode.getCreateUid(), circleMemberInviteCode.getCircleId());
        if (circleMemberInviteCodes != null && circleMemberInviteCodes.size() > 0) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "同一圈子只能使用同一人邀请码一次");
        }
        circleMemberInviteCode.setIsUse(1);
        circleMemberInviteCode.setUseUid(user.getId());
        circleMemberInviteCode.setUseTime(new Date());
        circleMemberInviteCode.update();
        return RetUtils.okOper();
    }

    public Ret checkInviteCodeNum(User user, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and use_uid = ? and is_invalid = 0", circleId, user.getId());
        if (circleMemberInviteCodes.size() == 0) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "还需" + circle.getInviteNum() + "个邀请码");
        }
        if (circleMemberInviteCodes.size() < circle.getInviteNum()) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "还需" + (circle.getInviteNum() - circleMemberInviteCodes.size()) + "个邀请码");
        }
        return RetUtils.okData(Const.YesOrNo.YES);
    }

    public Ret memberApplyList(User user, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        if (circleMember == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "尚未加入该圈子");
        }
        if (circleMember.getRole().equals(3)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "非管理员不可查看该列表");
        }

        List<CircleMemberApply> circleMemberApplyList = CircleMemberApply.dao.find("select * from circle_member_apply where circle_id = ? order by status, create_time desc", circleId);
        if (circleMemberApplyList != null && circleMemberApplyList.size() > 0) {
            for (CircleMemberApply circleMemberApply : circleMemberApplyList) {
                circleMemberApply.setCircleInfo(circle);
                circleMemberApply.setApplyNick(User.dao.findById(circleMemberApply.getUid()).getNick());
                List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and use_uid = ? and is_invalid = 0", circleId, circleMemberApply.getUid());
                if (circleMemberInviteCodes != null && circleMemberInviteCodes.size() > 0) {
                    for (CircleMemberInviteCode circleMemberInviteCode : circleMemberInviteCodes) {
                        circleMemberInviteCode.setInviteNick(User.dao.findById(circleMemberInviteCode.getCreateUid()).getNick());
                    }
                }
                circleMemberApply.setInviteCodeList(circleMemberInviteCodes);
            }
        }
        return RetUtils.okData(Const.YesOrNo.YES).set("list", circleMemberApplyList);


    }

    public Ret examineApply(User user, Integer circleId, String applyIds, Integer status, String refuseReason) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","圈子不存在");
        }
        if (circle.getStatus().equals(2)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","圈子已被封禁，无法进行该操作");
        }
        if (status == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","审核结果不能为空");
        }
        if (!status.equals(1) && !status.equals(2)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","参数异常");
        }
        CircleMember examineMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        if (examineMember == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","非管理员无法进行关注审核");
        }
        if (examineMember.getRole().equals(3)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","非管理员无法进行关注审核");
        }
        String[] applyIdList = applyIds.split(",");
        for (String applyId:applyIdList) {
            CircleMemberApply circleMemberApply = CircleMemberApply.dao.findById(applyId);
            if (circleMemberApply == null) {
                return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","申请id: "+applyId+" 不存在");
            }
            if (!circleId.equals(circleMemberApply.getCircleId())) {
                return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","参数异常，申请id: "+applyId+" 不存在于该圈子");
            }

            circleMemberApply.setStatus(status);
            circleMemberApply.setExamineUid(user.getId());
            circleMemberApply.setExamineNick(user.getNick());
            if (status.equals(1)) {
                User applyUser = User.dao.findById(circleMemberApply.getUid());
                addCircle(applyUser, circleId, 0);
                addCircleLog(user, applyUser, circleId, 1, circleMemberApply.getContentType());
                circleMemberApply.update();
            } else {
                circleMemberApply.setRefuseReason(refuseReason);
                circleMemberApply.update();
                inviteCodeInvalid(circleMemberApply.getUid(), circleMemberApply.getCircleId());
            }
        }
        if (status.equals(1)) {
            return RetUtils.okData(Const.YesOrNo.YES).set("msg","已通过");
        } else {
            return RetUtils.okData(Const.YesOrNo.YES).set("msg","已拒绝");
        }
    }

    public Ret inviteCodeInvalid(Integer uid, Integer circleId) {
        Kv params = Kv.create();
        params.set("circleId", circleId);
        params.set("uid", uid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.updateInviteCodeInvalid", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return Ret.ok();
    }

    public Ret circleList(User user, Integer pageNumber, Integer pageSize) {
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("status", 1);
        params.set("uid", user.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.circlelist", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        for (Record record : records.getList()) {
            Object circleId = record.get("id");
            record.set("circleNum", CircleMember.dao.find("select * from circle_member where circle_id = ?", circleId).size());
        }
        return RetUtils.okPage(records);
    }

    public Ret addCircleApplyList(User user, Integer pageNumber, Integer pageSize) {
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("uid", user.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.addcircleapplylist", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return RetUtils.okPage(records);
    }

    public Ret leaveCircle(User user, Integer circleId) {
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        if (circleMember == null) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "您不是该圈子成员");
        }
        if (circleMember.getRole().equals(1)) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "圈主需转让圈子后才可取消关注");
        }
        boolean delete = circleMember.delete();
        if (!delete) {
            return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg", "操作失败，请重试");
        }
        List<CircleMemberApply> circleMemberApplyList = CircleMemberApply.dao.find("select * from circle_member_apply where uid = ? and circle_id = ?", user.getId(), circleId);
        if (circleMemberApplyList == null) {
            return RetUtils.okData(Const.YesOrNo.YES);
        }
        for (CircleMemberApply item : circleMemberApplyList) {
            item.delete();
        }
        inviteCodeInvalid(user.getId(), circleId);
        delInviteCode(circleId, user.getId());
        return RetUtils.okData(Const.YesOrNo.YES);

    }

    public void delInviteCode(Integer circleId, Integer uid) {
        Kv params = Kv.create();
        params.set("circleid", circleId);
        params.set("uid", uid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delinvitecode", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    public Ret updateCircle(User user, Integer circleId, String avatar, String name, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) {
        Circle circle = Circle.dao.findById(circleId);
        CircleApply circleApply = CircleApply.dao.findById(circleId);
        if (avatar != null && !avatar.isEmpty()) {
            circle.setAvatar(avatar);
            circleApply.setAvatar(avatar);
        }
        if (name != null && !name.isEmpty()) {
            circle.setName(name);
            circleApply.setName(name);
        }
        if (describe != null) {
            circle.setDescribe(describe);
            circleApply.setDescribe(describe);
        }
        if (isOpen != null) {
            circle.setIsOpen(isOpen);
            circleApply.setIsOpen(isOpen);
        }
        if (isExamine != null) {
            circle.setIsExamine(isExamine);
            circleApply.setIsExamine(isExamine);
        }
        if (isInvite != null) {
            circle.setIsInvite(isInvite);
            circleApply.setIsInvite(isInvite);
        }
        if (inviteNum != null) {
            circle.setInviteNum(inviteNum);
            circleApply.setInviteNum(inviteNum);
        }
        boolean update = circle.update();
        boolean update1 = circleApply.update();
        if (!update || !update1) {
            return Ret.fail();
        }
        return Ret.ok();
    }

    public Ret checkRole(User user, Integer circleId) {
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        if (circleMember == null) {
            return Ret.fail();
        }
        return Ret.ok().set("role", circleMember.getRole());
    }

    public Ret addCircleManager(Integer circleId, String uids) {
        String[] uidList = uids.split(",");
        for (String uid : uidList) {
            CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", uid, circleId);
            if (circleMember.getRole().equals(1) || circleMember.getRole().equals(2)) {
                return Ret.fail().set("errorMsg", "圈主和管理员不能被设置为管理员");
            }
            circleMember.setRole(2);
            circleMember.update();
        }
        return Ret.ok();
    }

    public Ret generalMemberList(Integer circleId) {
        List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ? and role = 3", circleId);
        for (CircleMember circleMember : circleMembers) {
            circleMember.setPinyin(PyUtils.getAllChat(circleMember.getNick()));
            circleMember.setShoupin(PyUtils.getFristChatForCircle(circleMember.getNick()));
        }
        Collections.sort(circleMembers, new Comparator<CircleMember>() {
            @Override
            public int compare(CircleMember p1, CircleMember p2) {
                if (p1.getShoupin().equals(p2.getShoupin())) {
                    return p1.getPinyin().compareTo(p2.getPinyin());
                } else {
                    return p1.getShoupin().compareTo(p2.getShoupin());
                }
            }
        });
        return Ret.ok("generalMemberList",circleMembers);
    }

    public Ret managerMemberList(Integer circleId) {
        List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ? and role = 2", circleId);
        for (CircleMember circleMember : circleMembers) {
            circleMember.setPinyin(PyUtils.getAllChat(circleMember.getNick()));
            circleMember.setShoupin(PyUtils.getFristChatForCircle(circleMember.getNick()));
        }
        Collections.sort(circleMembers, new Comparator<CircleMember>() {
            @Override
            public int compare(CircleMember p1, CircleMember p2) {
                if (p1.getShoupin().equals(p2.getShoupin())) {
                    return p1.getPinyin().compareTo(p2.getPinyin());
                } else {
                    return p1.getShoupin().compareTo(p2.getShoupin());
                }
            }
        });
        return Ret.ok("managerMemberList",circleMembers);
    }


    public Ret delCircleManager(Integer circleId, String uids) {
        String[] uidList = uids.split(",");
        for (String uid : uidList) {
            CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", uid, circleId);
            circleMember.setRole(3);
            circleMember.update();
        }
        return Ret.ok();
    }

    public Ret transferCircle(User user, Integer circleId, Integer uid) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "圈子不存在");
        }
        CircleApply circleApply = CircleApply.dao.findById(circleId);
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", uid, circleId);
        if (circleMember == null) {
            return Ret.fail().set("errorMsg", "选择用户不是该圈子成员");
        }
        CircleMember transferMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", user.getId(), circleId);
        circleMember.setRole(1);
        transferMember.setRole(3);
        circle.setUid(uid);
        circleApply.setUid(uid);
        boolean update = circleMember.update();
        boolean update1 = transferMember.update();
        boolean update2 = circle.update();
        boolean update3 = circleApply.update();
        if (update1 && update && update2&& update3) {
            return Ret.ok();
        }
        return Ret.fail().set("errorMsg", "操作失败");

    }

    public Ret delMember(User user, Integer circleId, String uids) {
        Circle circle = Circle.dao.findById(circleId);
        CircleMember manager = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and role = 1", circleId);
        if (uids.contains(manager.getUid().toString())) {
            return Ret.fail().set("errorMsg", "不能删除圈主");
        }
        if (circle == null) {
            return Ret.fail().set("errorMsg", "圈子不存在");
        }
        CircleMember operateMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?",circleId, user.getId());
        if (operateMember.getRole().equals(3)) {
            return Ret.fail().set("errorMsg", "非管理员权限不可进行该操作");
        }
        String[] uidList = uids.split(",");
        List<CircleMember> delMemberList = new ArrayList<>();
        for (String uid : uidList) {
            CircleMember delMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, uid);
            if (operateMember.getRole() >= delMember.getRole()) {
                return Ret.fail().set("errorMsg", "权限不足");
            }
            delMemberList.add(delMember);
            inviteCodeInvalid(Integer.valueOf(uid), circleId);
            delInviteCode(circleId, Integer.valueOf(uid));
        }
        if (delMemberList.size() != 0) {
            for (CircleMember delMember : delMemberList) {
                delMember.delete();
                User operatedUser = User.dao.findById(delMember.getUid());
                addCircleLog(user,operatedUser,circleId,2,2);
            }
        }

        return Ret.ok();


    }

    public Ret addCircleArticle(User user, String circleIds, String cityIds, String content, String videoUrl, String imgUrl) {
        String[] cityIdList = cityIds.split(",");
        String[] circleIdList = circleIds.split(",");
        for (String circleId : circleIdList) {
            Circle circle = Circle.dao.findById(circleId);
            if (circle == null) {
                return Ret.fail().set("errorMsg", "选择圈子不存在");
            }
            CircleArticle circleArticle = new CircleArticle();
            circleArticle.setUid(user.getId());
            circleArticle.setAvatar(user.getAvatar());
            circleArticle.setCircleId(Integer.valueOf(circleId));
            circleArticle.setContent(content);
            circleArticle.setVideoUrl(videoUrl);
            circleArticle.setImgUrl(imgUrl);
//            circleArticle.setCityIds(cityIds);
            circleArticle.setCreateTime(new Date());
            for (String cityId : cityIdList) {
                City city = City.dao.findById(cityId);
                if (city == null) {
                    return Ret.fail().set("errorMsg", "选择城市不存在");
                }
                circleArticle.setCityId(Integer.valueOf(cityId));
                if (circleArticle.getId() != null) {
                    circleArticle.setId(circleArticle.getId() + 1);
                }
                circleArticle.save();
            }
        }
        return Ret.ok();
    }

    public Ret setViewSetting(User user, String circleIds, String cityIds) {
        String[] circleIdList = circleIds.split(",");
        String[] cityIdList = cityIds.split(",");
        for (String cityId : cityIdList) {
            City city = City.dao.findById(cityId);
            if (city == null) {
                return Ret.fail().set("errorMsg", "选择城市不存在");
            }
        }
        for (String circleId : circleIdList) {
            Circle circle = Circle.dao.findById(circleId);
            if (circle == null) {
                return Ret.fail().set("errorMsg", "选择圈子不存在");
            }
            if (!circle.getStatus().equals(1)) {
                return Ret.fail().set("errorMsg", "选择圈子状态异常");
            }
            CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, user.getId());
            if (circleMember == null) {
                return Ret.fail().set("errorMsg", "不可以选择未关注的圈子");
            }
        }
        CircleViewSetting circleViewSetting = CircleViewSetting.dao.findFirst("select * from circle_view_setting where uid = ?", user.getId());
        if (circleViewSetting == null) {
            CircleViewSetting viewSetting = new CircleViewSetting();
            viewSetting.setUid(user.getId());
            viewSetting.setCircleIds(circleIds);
            viewSetting.setCityIds(cityIds);
            viewSetting.setCreateTime(new Date());
            boolean save = viewSetting.save();
            if (!save) {
                return Ret.fail().set("errorMsg", "操作失败");
            }
        } else {
            circleViewSetting.setCircleIds(circleIds);
            circleViewSetting.setCityIds(cityIds);
            circleViewSetting.setUpdateTime(new Date());
            boolean update = circleViewSetting.update();
            if (!update) {
                return Ret.fail().set("errorMsg", "操作失败");
            }
        }
        Ret ret = clearSettingRelation(user);
        if (ret.isFail()) {
            return ret;
        }
        for (String cityId : cityIdList) {
            CircleUidCityidSetting circleUidCityidSetting = new CircleUidCityidSetting();
            circleUidCityidSetting.setUid(user.getId());
            circleUidCityidSetting.setCityId(Integer.valueOf(cityId));
            circleUidCityidSetting.save();
        }

        for (String circleId : circleIdList) {
            CircleUidCircleidSetting circleUidCircleidSetting = new CircleUidCircleidSetting();
            circleUidCircleidSetting.setUid(user.getId());
            circleUidCircleidSetting.setCircleId(Integer.valueOf(circleId));
            circleUidCircleidSetting.save();
        }
        return Ret.ok();
    }

    public Ret clearSettingRelation(User user) {
        List<CircleUidCircleidSetting> circleUidCircleidSettings = CircleUidCircleidSetting.dao.find("select * from circle_uid_circleid_setting where uid = ?", user.getId());
        List<CircleUidCityidSetting> circleUidCityidSettings = CircleUidCityidSetting.dao.find("select * from circle_uid_cityid_setting where uid = ?", user.getId());
        Kv params = Kv.by("uid", user.getId());
        if (circleUidCircleidSettings != null && circleUidCircleidSettings.size() > 0) {
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delUidCircleIdSetting", params);
            int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
            if (update <= 0) {
                return Ret.fail().set("errorMsg","操作失败，请重试");
            }
        }
        if (circleUidCityidSettings != null && circleUidCityidSettings.size() > 0) {
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delUidCityIdSetting", params);
            int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
            if (update <= 0) {
                return Ret.fail().set("errorMsg","操作失败，请重试");
            }
        }
        return Ret.ok();
    }

    public Ret viewSetting(User curr) {
        CircleViewSetting circleViewSetting = CircleViewSetting.dao.findFirst("select * from circle_view_setting where uid = ?", curr.getId());
        Map<String, Object> viewSetting = new HashMap<>();
        if (circleViewSetting == null) {
            return Ret.ok().set("viewSetting", viewSetting);
        }
        String circleIds = circleViewSetting.getCircleIds();
        int updateCircleIds = 0;
        String cityIds = circleViewSetting.getCityIds();

        List<Circle> circleList = new ArrayList<>();
        String[] circleIdList = circleIds.split(",");
        String newCircleIds = "";
        for (String circleId : circleIdList) {
            Circle circle = Circle.dao.findById(circleId);
            if (circle != null) {
                newCircleIds = newCircleIds + circleId + ",";
                circleList.add(circle);
            } else {
                updateCircleIds = 1;
            }
        }
        String[] cityIdList = cityIds.split(",");
        List<City> cityList = new ArrayList<>();
        for (String cityId : cityIdList) {
            City city = City.dao.findById(cityId);
            cityList.add(city);
        }
        viewSetting.put("circleList", circleList);
        viewSetting.put("cityList", cityList);
        if (updateCircleIds == 1) {
            circleViewSetting.setCircleIds(circleIds);
            circleViewSetting.setUpdateTime(new Date());
            circleViewSetting.update();
        }

        return Ret.ok().set("viewSetting", viewSetting);
    }

    public Ret delCircleArticle(User curr, Integer circleArticleId) {
        CircleArticle circleArticle = CircleArticle.dao.findById(circleArticleId);
        if (circleArticle == null) {
            return Ret.fail().set("errorMsg", "文章不存在");
        }
        if (!curr.getId().equals(circleArticle.getUid())) {
            CircleMember currMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", curr.getId(), circleArticle.getCircleId());
            if (currMember == null) {
                return Ret.fail().set("errorMsg", "不是该圈子成员，无法删除该内容");
            }
            if (currMember.getRole().equals(3)) {
                return Ret.fail().set("errorMsg", "仅发帖人和圈子管理员有权删除改内容");
            }
        }
        boolean delete = circleArticle.delete();
        if (!delete) {
            return Ret.fail().set("errorMsg", "删除失败，请重试");
        }
        return Ret.ok();
    }

    public Ret addCircleArticleComment(User user, Integer articleId, Integer pid, String content) {
        CircleArticle circleArticle = CircleArticle.dao.findById(articleId);
        if (circleArticle == null) {
            return Ret.fail().set("errorMsg","该文章不存在");
        }
        if (content == null || content.isEmpty()) {
            return Ret.fail().set("errorMsg","评论内容不能为空");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleArticle.getCircleId(), user.getId());
        if (circleMember == null) {
            return Ret.fail().set("errorMsg","非圈子成员不可进行评论");
        }
        if (!pid.equals(0)) {
            CircleArticleComment pCircleArticleComment = CircleArticleComment.dao.findById(pid);
            if (pCircleArticleComment == null) {
                return Ret.fail().set("errorMsg","该条评论不存在或已删除，无法对该评论进行回复");
            }
        }
        CircleArticleComment circleArticleComment = new CircleArticleComment();
        circleArticleComment.setPid(pid);
        circleArticleComment.setArticleId(articleId);
        circleArticleComment.setUid(user.getId());
        circleArticleComment.setAvatar(user.getAvatar());
        circleArticleComment.setContent(content);
        circleArticleComment.setCreateTime(new Date());
        boolean save = circleArticleComment.save();
        if (!save) {
            return Ret.fail().set("errorMsg","评论失败，请重试");
        }
        circleArticleComment.setNick(user.getNick());
        if (pid.equals(0)) {
            return Ret.ok().set("comment", circleArticleComment)
                    .set("articleUid", circleArticle.getUid())
                    .set("commentId", circleArticleComment.getId());
        }
        CircleArticleComment pCircleArticleComment = CircleArticleComment.dao.findById(pid);
        return Ret.ok().set("comment", circleArticleComment)
                .set("articleUid", circleArticle.getUid())
                .set("pCommentUid",pCircleArticleComment.getUid())
                .set("commentId", circleArticleComment.getId())
                .set("pCommentId", pCircleArticleComment.getId());
    }

    public Ret delCircleArticleComment(User user, Integer commentId) {
        CircleArticleComment circleArticleComment = CircleArticleComment.dao.findById(commentId);
        if (circleArticleComment == null) {
            return Ret.fail().set("errorMsg", "该评论已删除不存在");
        }
        CircleArticle circleArticle = CircleArticle.dao.findById(circleArticleComment.getArticleId());
        if (circleArticle == null) {
            return Ret.fail().set("errorMsg", "该文章已删除");
        }
        if (!circleArticleComment.getUid().equals(user.getId())) {
            if (!circleArticle.getUid().equals(user.getId())) {
                CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleArticle.getCircleId(), user.getId());
                if (circleMember == null || circleMember.getRole().equals(3)) {
                    return Ret.fail().set("errorMsg", "非圈子管理员和评论人不可进行该操作");
                }
            }
        }
        boolean delete = circleArticleComment.delete();
        if (!delete) {
            return Ret.fail().set("errorMsg", "删除失败，请重试");
        }
        return Ret.ok();
    }

    public Ret addCircleArticleLike(User user, Integer articleId) {
        CircleArticle circleArticle = CircleArticle.dao.findById(articleId);
        if (circleArticle == null) {
            return Ret.fail().set("errorMsg","该文章不存在");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleArticle.getCircleId(), user.getId());
        if (circleMember == null) {
            return Ret.fail().set("errorMsg","非圈子成员不可进行点赞");
        }
        CircleArticleLike checkLike = CircleArticleLike.dao.findFirst("select * from circle_article_like where article_id = ? and uid = ?", articleId, user.getId());
        if (checkLike != null) {
            return Ret.fail().set("errorMsg","不可重复点赞");
        }
        CircleArticleLike circleArticleLike = new CircleArticleLike();
        circleArticleLike.setArticleId(articleId);
        circleArticleLike.setUid(user.getId());
        circleArticleLike.setAvatar(user.getAvatar());
        circleArticleLike.setLikeTime(new Date());
        boolean save = circleArticleLike.save();
        if (!save) {
            return Ret.fail().set("errorMsg","点赞失败，请重试");
        }
        return Ret.ok().set("like", circleArticleLike).set("articleUid", circleArticle.getUid()).set("likeId", circleArticleLike.getId());
    }

    public Ret cancelCircleArticleLike(User user, Integer likeId) {
        CircleArticleLike circleArticleLike = CircleArticleLike.dao.findById(likeId);
        if (circleArticleLike == null) {
            return Ret.fail().set("errorMsg", "点赞已取消");
        }
        CircleArticle circleArticle = CircleArticle.dao.findById(circleArticleLike.getArticleId());
        if (circleArticle == null) {
            return Ret.fail().set("errorMsg", "该文章已删除");
        }
        if (!circleArticleLike.getUid().equals(user.getId())) {
            return Ret.fail().set("errorMsg", "无法取消他人点赞");
        }
        boolean delete = circleArticleLike.delete();
        if (!delete) {
            return Ret.fail().set("errorMsg", "删除失败，请重试");
        }
        return Ret.ok();
    }

    public Ret circleArticleList(Integer uid, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_ARTICLE);
        String key = "article" + "_" + uid;
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<CircleArticle>circleArticleList  = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<CircleArticle>>() {
            @Override
            public ArrayList<CircleArticle> create() throws Exception {
                ArrayList<CircleArticle> circleArticles = (ArrayList<CircleArticle>) CircleArticle.dao.find("select " +
                        "a.*,  " +
                        "u.nick " +
                        "from " +
                        "circle_article a, " +
                        "circle_uid_circleid_setting b, " +
                        "circle_uid_cityid_setting c, " +
                        "user u " +
                        "where " +
                        "b.uid = ? " +
                        "and a.circle_id = b.circle_id " +
                        "and a.city_id = c.city_id " +
                        "and b.uid = c.uid " +
                        "and a.uid = u.id " +
                        "order by a.id desc", uid);

                return circleArticles;
            }
        });
        List<CircleArticle> result = new ArrayList<CircleArticle>();
        if (circleArticleList != null && circleArticleList.size() > 0) {
            int maxIndex = pageSize * (pageNumber-1) + pageSize;
            int endIndex = Math.min(circleArticleList.size(), maxIndex);
            int startIndex = pageSize * (pageNumber-1);
            if (circleArticleList.size() < startIndex) {
                return Ret.ok("result", result);
            }

            for (int i = startIndex; i < endIndex; i++) {
                CircleArticle circleArticle = circleArticleList.get(i);
                CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleArticle.getCircleId(), uid);
                circleArticle.setIsManager(circleMember.getRole() < 3);
                circleArticle.setIsAuthor(uid.equals(circleArticle.getUid()));
                List<CircleArticleComment> comments = CircleArticleComment.dao.find(
                        "select u.nick, a.* from circle_article_comment a, user u where article_id = ? and a.uid = u.id",
                        circleArticle.getId());

                List<CircleArticleLike> likes = CircleArticleLike.dao.find("select u.nick, a.* from circle_article_like a, user u where article_id = ? and a.uid = u.id", circleArticle.getId());
                Set<Map.Entry<String, Object>> entries = circleArticle._getAttrsEntrySet();
                for (Map.Entry<String, Object> entry: entries) {
                    if (entry.getKey().equals("nick")) {
                        circleArticle.setNick(entry.getValue().toString());
                    }
                }
                for (CircleArticleComment comment : comments) {
                    Set<Map.Entry<String, Object>> commentEntry = comment._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : commentEntry) {
                        if (entry.getKey().equals("nick")) {
                            comment.setNick(entry.getValue().toString());
                        }
                    }
                }

                for (CircleArticleLike like : likes) {
                    Set<Map.Entry<String, Object>> likeEntry = like._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : likeEntry) {
                        if (entry.getKey().equals("nick")) {
                            like.setNick(entry.getValue().toString());
                        }
                    }
                }
                circleArticle.setComments(comments);
                circleArticle.setLikes(likes);
                circleArticle.setCircleName(Circle.dao.findById(circleArticle.getCircleId()).getName());
                if (circleArticle.getImgUrl() == null) {
                    circleArticle.setImgUrl("");
                }
                if (circleArticle.getVideoUrl() == null) {
                    circleArticle.setVideoUrl("");
                }
//				moment.setNick(moment.getNick());
//				moment.setRemarkName(moment.getRemarkName());
//				moment.setAvatar(moment.getAvatar());
                if (likes.size() > 0) {
                    circleArticle.setLikesCount(likes.size());
                } else {
                    circleArticle.setLikesCount(0);
                }
                result.add(circleArticle);
            }
        }
        return Ret.ok().set("result", result);
    }

    public Ret getCircleArticleById(User curr, Integer articleId) {
        CircleArticle article = CircleArticle.dao.findById(articleId);
        if (article == null) {
            return Ret.fail().set("errorMsg", "文章不存在");
        }
        Circle circle = Circle.dao.findById(article.getCircleId());
        if (circle == null || !circle.getStatus().equals(1)) {
            return Ret.fail().set("errorMsg", "圈子状态异常，无法查看文章详情");
        }
        User user = User.dao.findById(article.getUid());
        List<CircleArticleLike> likes = CircleArticleLike.dao.find("select * from circle_article_like where article_id = ?", articleId);
        List<CircleArticleComment> comments = CircleArticleComment.dao.find("select * from circle_article_comment where article_id = ?", articleId);
        if (likes != null) {
            article.setLikes(likes);
            article.setLikesCount(likes.size());
        } else {
            article.setLikesCount(0);
        }
        if (comments != null) {
            for (CircleArticleComment comment : comments) {
                User commentUser = User.dao.findById(comment.getUid());
                comment.setNick(commentUser.getNick());
            }
        }
        article.setComments(comments);
        article.setNick(user.getNick());
        article.setAvatar(user.getAvatar());
        article.setCircleName(circle.getName());
        return Ret.ok().set("article", article);
    }

    public void readNewArticleMsg(Integer uid) {
        Kv params = Kv.by("uid", uid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.readNewArticleMsg", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    public List<CircleArticleMsgVo> CircleArticleMsgVo(User curr, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_ARTICLE_MSG);
        String key = "articlemsgs" + "_" + curr.getId();
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<CircleMsg> circleMsgs  = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<CircleMsg>>() {
            @Override
            public ArrayList<CircleMsg> create() throws Exception {
                ArrayList<CircleMsg> list = (ArrayList<CircleMsg>) CircleMsg.dao.find(
                        "select * from circle_msg where to_uid = ? and (content_type = 2 or content_type = 3)  " +
                                "order by create_time desc",
                        curr.getId());
                return list;
            }
        });


//		List<WxFriendMsg> wxFriendMsgs = WxFriendMsg.dao.find(
//				"select * from wx_friend_msg where touid = ? and (contenttype = 16 or contenttype = 17)  " +
//						"order by createtime desc limit ? offset ?",
//				 curr.getId(), pageSize, (pageNumber - 1) * pageSize);

        if (circleMsgs == null) {
            return new ArrayList<CircleArticleMsgVo>();
        }

        int maxIndex = pageSize * (pageNumber-1) + pageSize;
        int endIndex = Math.min(circleMsgs.size(), maxIndex);
        int startIndex = pageSize * (pageNumber-1);
        List<CircleArticleMsgVo> result = new ArrayList<CircleArticleMsgVo>();
        for (int i = startIndex; i < endIndex; i++) {
            User fromUser = User.dao.findById(circleMsgs.get(i).getUid());
            CircleArticleMsgVo articleMsgVo = new CircleArticleMsgVo();
            articleMsgVo.setFromNickname(fromUser.getNick());
            articleMsgVo.setTime(circleMsgs.get(i).getCreateTime());
            articleMsgVo.setCircleMsg(circleMsgs.get(i));
            articleMsgVo.setAvatar(fromUser.getAvatar());
            JSONObject jsonObject = JSONObject.parseObject(circleMsgs.get(i).getText());
            Object text = jsonObject.get("text").toString();
            String id = jsonObject.get("id").toString();

            CircleArticle article = CircleArticle.dao.findById(jsonObject.get("articleId"));
            if (article == null) {
                articleMsgVo.setContent("此文章已删除");
                articleMsgVo.setDelArticle(Const.YesOrNo.YES);
            } else {
                articleMsgVo.setArticleContent(article.getContent());
                articleMsgVo.setVideoUrl(article.getVideoUrl());
                articleMsgVo.setImgUrl(article.getImgUrl());
                articleMsgVo.setArticle(article);
                articleMsgVo.setDelArticle(Const.YesOrNo.NO);
                if (circleMsgs.get(i).getContentType().equals(3)) {
                    CircleArticleLike articleLike = CircleArticleLike.dao.findById(id);
                    if (articleLike == null) {
                        articleMsgVo.setLikeCancel(Const.YesOrNo.YES);
                        articleMsgVo.setContent("点赞已取消");
                    } else {
                        articleMsgVo.setLikeCancel(Const.YesOrNo.NO);
                        articleMsgVo.setArticleLike(articleLike);
                        articleMsgVo.setContent("您有一条新的点赞消息");
                    }
                } else {
                    CircleArticleComment comment = CircleArticleComment.dao.findById(id);
                    if (comment == null) {
                        articleMsgVo.setCommentCancel(Const.YesOrNo.YES);
                        articleMsgVo.setContent("评论已删除");
                    } else {
                        articleMsgVo.setCommentCancel(Const.YesOrNo.NO);
                        articleMsgVo.setComment(comment);
                        articleMsgVo.setContent(text.toString());
                    }
                }
            }
            result.add(articleMsgVo);
        }
        result.sort(Comparator.comparing(CircleArticleMsgVo::getTime).reversed());
        return result;
    }

    public void readNewArticleCommentAndLikeMsg(Integer uid) {
        Kv params = Kv.by("uid", uid);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.readNewArticleCommentAndLikeMsg", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    public void clearArticleMsgs(User curr) {
        Kv params = Kv.by("touid", curr.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.clearArticleMsgs", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    public int unreadCount(User curr) {
        List<CircleMsg> circleMsgs = CircleMsg.dao.find("select * from circle_msg where to_uid = ? and read_flag = 2", curr.getId());
        if (circleMsgs != null) {
            return circleMsgs.size();
        }
        return 0;
    }

    public Ret addComplaint(User curr, Integer circleId, String reason, String reasonImg) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "该圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Ret.fail().set("errorMsg", "该圈子状态异常");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ? ", circleId, curr.getId());
        if (circleMember == null) {
            return Ret.fail().set("errorMsg", "非圈子成员无法投诉");
        }
        CircleComplaint circleComplaint = new CircleComplaint();
        circleComplaint.setUid(curr.getId());
        circleComplaint.setNick(curr.getNick());
        circleComplaint.setAvatar(curr.getAvatar());
        circleComplaint.setCircleId(circleId);
        circleComplaint.setReason(reason);
        circleComplaint.setImgs(reasonImg);
        circleComplaint.setStatus(0);
        circleComplaint.setCreateTime(new Date());
        boolean save = circleComplaint.save();
        if (!save) {
            return Ret.fail().set("errorMsg", "投诉失败，请重试");
        }
        return Ret.ok();

    }

    public Ret complaintList(User curr) {
        List<CircleComplaint> circleComplaints = CircleComplaint.dao.find("selet * from circle_complaint where uid = ? order by create_time desc", curr.getId());
        return Ret.ok().set("complaintList",circleComplaints);
    }

    public Ret addCircleLog(User operateUser, User operatedUser, Integer circleId, Integer type, Integer contentType) {
        CircleLog circleLog = new CircleLog();
        if (type.equals(1)) {
            List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and use_uid = ? and is_invalid = 0", circleId, operatedUser.getId());
            String inviteUserList = "";
            if (circleMemberInviteCodes != null && circleMemberInviteCodes.size() > 0) {
                for (CircleMemberInviteCode inviteCode : circleMemberInviteCodes) {
                    inviteUserList += (inviteCode.getCreateUid() + ",");
                }
            }
            if (!inviteUserList.isEmpty()) {
                circleLog.setInviteUserList(inviteUserList);
            }
        }
        circleLog.setOperateUid(operateUser.getId());
        circleLog.setOperatedUid(operatedUser.getId());
        circleLog.setNick(operatedUser.getNick());
        circleLog.setAvatar(operatedUser.getAvatar());
        circleLog.setCircleId(circleId);
        circleLog.setType(type);
        circleLog.setContentType(contentType);
        circleLog.setRecordTime(new Date());
        if (type.equals(1)) {
            if (contentType.equals(1)) {
                circleLog.setDescribe("通过搜索 关注");
            } else if (contentType.equals(2)) {
                circleLog.setDescribe("通过扫描二维码 关注");
            } else if (contentType.equals(3)) {
                circleLog.setDescribe("通过名片分享 关注");
            }
        } else {
            if (contentType.equals(1)) {
                circleLog.setDescribe("取消了关注");
            } else if (contentType.equals(2)) {
                circleLog.setDescribe("被管理员 " + operateUser.getNick() + "移除关注");
            }
        }
        boolean save = circleLog.save();
        if (!save) {
            log.error("日志生成失败：{}", circleLog);
            return Ret.fail().set("errorMsg", "日志生成失败");
        }
        return Ret.ok();
    }

    public Ret circleLogList(User curr, Integer type, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "该圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Ret.fail().set("errorMsg", "该圈子状态异常");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, curr.getId());
        if (circleMember == null) {
            return Ret.fail().set("errorMsg", "非圈主和管理员不可查看改内容");
        }
        if (circleMember.getRole().equals(3)) {
            return Ret.fail().set("errorMsg", "非圈主和管理员不可查看改内容");
        }
        List<CircleLog> circleLogs = CircleLog.dao.find("select * from circle_log where type = ? and circle_id = ? order by record_time desc", type, circleId);
        if (type.equals(1)) {
            if (circleLogs != null) {
//                    List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and use_uid = ? and is_invalid = 0", circleId, circleLog.getOperatedUid());
//                    if (circleMemberInviteCodes == null) {
//                        circleLog.setInviteUsers(null);
//                    } else {
//                        List<User> inviteUserList = new ArrayList<>();
//                        for (CircleMemberInviteCode inviteRecord : circleMemberInviteCodes) {
//                            inviteUserList.add(User.dao.findById(inviteRecord.getCreateUid()));
//                        }
//                        circleLog.setInviteUsers(inviteUserList);
//                    }
                for (CircleLog circleLog : circleLogs) {
                    String inviteUserList = circleLog.getInviteUserList();
                    if (inviteUserList != null && !inviteUserList.isEmpty()) {
                        String[] inviteUidList = inviteUserList.split(",");
                        List<User> inviteUsers = new ArrayList<>();
                        for (String uid : inviteUidList) {
                            inviteUsers.add(User.dao.findById(uid));
                        }
                        circleLog.setInviteUsers(inviteUsers);
                    } else {
                        circleLog.setInviteUsers(new ArrayList<>());
                    }
                }
            }
        }
        return Ret.ok().set("circleLogList", circleLogs);
    }

    public Ret searchCircleLog(User curr, String searchKey, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "该圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Ret.fail().set("errorMsg", "该圈子状态异常");
        }
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, curr.getId());
        if (circleMember == null) {
            return Ret.fail().set("errorMsg", "非圈主和管理员不可查看改内容");
        }
        if (circleMember.getRole().equals(3)) {
            return Ret.fail().set("errorMsg", "非圈主和管理员不可查看改内容");
        }
        if (searchKey == null || searchKey.isEmpty()) {
            return Ret.fail().set("errorMsg", "搜索关键字不能为空");
        }
        searchKey = "%"+searchKey+"%";
        List<CircleLog> circleLogs = CircleLog.dao.find("select * from circle_log where circle_id = ? and (id like ? or nick like ? or operate_uid like ? or operated like ?)", circleId, searchKey, searchKey, searchKey, searchKey);
        if (circleLogs != null) {
            for (CircleLog circleLog : circleLogs) {
                if (circleLog.getType().equals(1)) {
                    List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where circle_id = ? and use_uid = ?", circleId, circleLog.getOperatedUid());
                    if (circleMemberInviteCodes == null) {
                        circleLog.setInviteUsers(null);
                    } else {
                        List<User> inviteUserList = new ArrayList<>();
                        for (CircleMemberInviteCode inviteRecord : circleMemberInviteCodes) {
                            inviteUserList.add(User.dao.findById(inviteRecord.getCreateUid()));
                        }
                        circleLog.setInviteUsers(inviteUserList);
                    }
                }
            }
        }
        return Ret.ok().set("result", circleLogs);
    }

    public Ret isMember(User user, Integer circleId) {
        CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, user.getId());
        if (circleMember == null) {
            return Ret.ok().set("isMember", Const.YesOrNo.NO);
        }
        return Ret.ok().set("isMember", Const.YesOrNo.YES).set("role", circleMember.getRole());
    }

    public Ret inviteCodeList(User user, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "圈子不存在");
        }
        if (!circle.getStatus().equals(1)) {
            return Ret.fail().set("errorMsg", "圈子状态异常");
        }
        List<CircleMemberInviteCode> circleMemberInviteCodes = CircleMemberInviteCode.dao.find("select * from circle_member_invite_code where create_uid = ? and circle_id = ? and is_invalid = 0 and is_del = 0 and is_use = 0", user.getId(), circleId);
        return Ret.ok().set("data", circleMemberInviteCodes);
    }

    public Ret delInviteCode(User user, Integer inviteCodeId) {
        CircleMemberInviteCode inviteCode = CircleMemberInviteCode.dao.findById(inviteCodeId);
        if (inviteCode == null) {
            return Ret.fail().set("errorMsg","邀请码不存在");
        }
        if (!user.getId().equals(inviteCode.getCreateUid())) {
            return Ret.fail().set("errorMsg","没有权限删除该邀请码");
        }
        inviteCode.setIsDel(1);
        inviteCode.update();
        return Ret.ok();

    }

    public Ret memberList(Integer circleId) {
        List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ?", circleId);
        if (circleMembers == null || circleMembers.size() == 0) {
            return Ret.fail().set("errorMsg", "成员为空");
        }
        for (CircleMember circleMember : circleMembers) {
            circleMember.setPinyin(PyUtils.getAllChat(circleMember.getNick()));
            circleMember.setShoupin(PyUtils.getFristChatForCircle(circleMember.getNick()));
        }
        Collections.sort(circleMembers, new Comparator<CircleMember>() {
            @Override
            public int compare(CircleMember p1, CircleMember p2) {
                if (p1.getShoupin().equals(p2.getShoupin())) {
                    return p1.getPinyin().compareTo(p2.getPinyin());
                } else {
                    return p1.getShoupin().compareTo(p2.getShoupin());
                }
            }
        });
        return Ret.ok().set("memberList", circleMembers);
    }

    public Ret circleArticleListByUid(Integer id, Integer uid, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_ARTICLE);
        String key = "article" + "_" + uid;
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<CircleArticle>circleArticleList  = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<CircleArticle>>() {
            @Override
            public ArrayList<CircleArticle> create() throws Exception {
                ArrayList<CircleArticle> circleArticles = (ArrayList<CircleArticle>) CircleArticle.dao.find("select " +
                        "a.*,  " +
                        "u.nick " +
                        "from " +
                        "circle_article a, " +
                        "user u " +
                        "where " +
                        "a.uid = ? " +
                        "and a.uid = u.id " +
                        "order by a.id desc", uid);

                return circleArticles;
            }
        });
        List<CircleArticle> result = new ArrayList<CircleArticle>();
        if (circleArticleList != null && circleArticleList.size() > 0) {
            int maxIndex = pageSize * (pageNumber-1) + pageSize;
            int endIndex = Math.min(circleArticleList.size(), maxIndex);
            int startIndex = pageSize * (pageNumber-1);
            if (circleArticleList.size() < startIndex) {
                return Ret.ok("result", result);
            }

            for (int i = startIndex; i < endIndex; i++) {
                CircleArticle circleArticle = circleArticleList.get(i);
                CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleArticle.getCircleId(), uid);
                circleArticle.setIsManager(circleMember.getRole() < 3);
                circleArticle.setIsAuthor(uid.equals(circleArticle.getUid()));
                List<CircleArticleComment> comments = CircleArticleComment.dao.find(
                        "select u.nick, a.* from circle_article_comment a, user u where article_id = ? and a.uid = u.id",
                        circleArticle.getId());

                List<CircleArticleLike> likes = CircleArticleLike.dao.find("select u.nick, a.* from circle_article_like a, user u where article_id = ? and a.uid = u.id", circleArticle.getId());
                Set<Map.Entry<String, Object>> entries = circleArticle._getAttrsEntrySet();
                for (Map.Entry<String, Object> entry: entries) {
                    if (entry.getKey().equals("nick")) {
                        circleArticle.setNick(entry.getValue().toString());
                    }
                }
                for (CircleArticleComment comment : comments) {
                    Set<Map.Entry<String, Object>> commentEntry = comment._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : commentEntry) {
                        if (entry.getKey().equals("nick")) {
                            comment.setNick(entry.getValue().toString());
                        }
                    }
                }

                for (CircleArticleLike like : likes) {
                    Set<Map.Entry<String, Object>> likeEntry = like._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : likeEntry) {
                        if (entry.getKey().equals("nick")) {
                            like.setNick(entry.getValue().toString());
                        }
                    }
                }
                circleArticle.setComments(comments);
                circleArticle.setLikes(likes);
                circleArticle.setCircleName(Circle.dao.findById(circleArticle.getCircleId()).getName());
                if (circleArticle.getImgUrl() == null) {
                    circleArticle.setImgUrl("");
                }
                if (circleArticle.getVideoUrl() == null) {
                    circleArticle.setVideoUrl("");
                }
//				moment.setNick(moment.getNick());
//				moment.setRemarkName(moment.getRemarkName());
//				moment.setAvatar(moment.getAvatar());
                if (likes.size() > 0) {
                    circleArticle.setLikesCount(likes.size());
                } else {
                    circleArticle.setLikesCount(0);
                }
                result.add(circleArticle);
            }
        }
        return Ret.ok().set("result", result);
    }


    public Ret delCircle(User user, Integer circleId) {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Ret.fail().set("errorMsg", "圈子不存在");
        }
        if (!circle.getUid().equals(user.getId())) {
            return Ret.fail().set("errorMsg", "非圈主不可进行该操作");

        }
        List<CircleArticle> circleArticles = CircleArticle.dao.find("select * from circle_article where circle_id = ?", circleId);
        boolean b = Circle.dao.deleteById(circleId);
        boolean b1 = CircleApply.dao.deleteById(circleId);
        Kv params = Kv.by("circleid", circleId);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcirclemember", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        SqlPara sqlPara1 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcirclememberapply", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara1);
        SqlPara sqlPara2 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delarticle", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara2);
        SqlPara sqlPara3 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcomplaint", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara3);
        SqlPara sqlPara4 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.dellog", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara4);
        SqlPara sqlPara5 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcirclememberinvitecode", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara5);
        SqlPara sqlPara6 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcirclesetting", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara6);
        for (CircleArticle circleArticle : circleArticles) {
            Kv params1 = Kv.by("articleid", circleId);
            SqlPara sql1 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delcomment", params1);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sql1);
            SqlPara sql2 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.dellike", params1);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sql2);
            SqlPara sql3 = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.delarticlecomplaint", params1);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sql3);
        }
        List<CircleViewSetting> circleViewSettings = CircleViewSetting.dao.find("select * from circle_view_setting where circle_ids like ?", "%" + circleId + "%");
        if (circleViewSettings != null && circleViewSettings.size() > 0) {
            for (CircleViewSetting circleViewSetting : circleViewSettings) {
                String temp = "";
                String[] split = circleViewSetting.getCircleIds().split(",");
                for (String str : split) {
                    if (!str.equals(circleId.toString())) {
                        temp = temp + str + ",";
                    }
                }
                if (temp.isEmpty()) {
                    continue;
                }
                String replaceStr = temp.substring(0, temp.length() - 1);
                if (circleViewSetting.getCircleIds().equals(replaceStr)) {
                    continue;
                }
                circleViewSetting.setCircleIds(replaceStr);
                circleViewSetting.update();
            }
        }

        return Ret.ok();
    }
}
