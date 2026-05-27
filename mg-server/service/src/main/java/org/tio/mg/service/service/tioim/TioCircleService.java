
package org.tio.mg.service.service.tioim;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.*;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.Const;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * im好友管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioCircleService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioCircleService.class);
	
	public static final TioCircleService me	= new TioCircleService();


	/**
	 * 查看圈子申请列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 */
	public Ret applyList(Integer pageNumber, Integer pageSize, String searchkey, Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		if(status != null) {
			params.set("status", status);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("circle.applylist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);

	}

	/**
	 * 修改申请状态
	 * @param circleApplyId
	 * @param status
	 * @param refuseReason
	 * @return
	 */
	public Ret updateApplyStatus(Integer circleApplyId, Integer status, String refuseReason) {
		CircleApply circleApply = CircleApply.dao.findById(circleApplyId);
		if (circleApply == null) {
			return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","该圈子申请不存在");
		}
		if (status.equals(circleApply.getStatus())) {
			return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","修改状态和当前申请状态一致");
		}
//		if (status.equals(2) && refuseReason == null) {
//			return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","请填写拒绝该申请的理由");
//		}

		circleApply.setStatus(status);
		if (status.equals(2)) {
			circleApply.setRefuseReason(refuseReason);
			boolean update = circleApply.update();
			if (!update) {
				return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","状态修改失败");
			}
		}


		// 通过申请，创建圈子
		if (status.equals(1)) {
			Integer showId = 0;
			while (true) {
				Random rand = new Random();
				int min = 100000;
				int max = 200000;
				showId = rand.nextInt((max - min) + 1) + min;
				Circle notExist = Circle.dao.findFirst("select * from circle where id = ? or show_id = ?", showId, showId);
				if (notExist == null) {
					break;
				}
			}
			Circle circle = new Circle();
			circle.setId(circleApplyId);
			circle.setShowId(showId);
			circle.setName(circleApply.getName());
			circle.setUid(circleApply.getUid());
			circle.setAvatar(circleApply.getAvatar());
			circle.setDescribe(circleApply.getDescribe());
			circle.setIsExamine(circleApply.getIsExamine());
			circle.setIsInvite(circleApply.getIsInvite());
			circle.setIsOpen(circleApply.getIsOpen());
			circle.setInviteNum(circleApply.getInviteNum());
			circle.setStatus(1);
			circle.setCreateTime(new Date());
			boolean save = circle.save();
			if (!save) {
				return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","圈子创建失败");
			}
			circleApply.setCircleId(circle.getId());
            boolean update = circleApply.update();
            if (!update) {
                return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","状态修改失败");
            }
			CircleMemberApply circleMemberApply = new CircleMemberApply();
			circleMemberApply.setUid(circle.getUid());
			circleMemberApply.setCircleId(circle.getId());
			circleMemberApply.setStatus(1);
			circleMemberApply.setContentType(0);
			circleMemberApply.setCreateTime(new Date());
			boolean save1 = circleMemberApply.save();
			if (!save1) {
				return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","圈子成员初始化失败");
			}

			CircleMember circleMember = new CircleMember();
			User user = User.dao.findById(circle.getUid());
			circleMember.setUid(user.getId());
			circleMember.setCircleId(circle.getId());
			circleMember.setAvatar(user.getAvatar());
			circleMember.setNick(user.getNick());
			circleMember.setRole(1);
			circleMember.setCreateTime(new Date());
			boolean save2 = circleMember.save();
			if (!save2) {
				return RetUtils.okData(Const.YesOrNo.NO).set("errorMsg","圈子成员初始化失败");
			}
		}

		return RetUtils.okData(Const.YesOrNo.YES);
	}

	/**
	 * 获取圈子列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 */
	public Ret circleList(Integer pageNumber, Integer pageSize, String searchkey, Short status) {

		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		if(status != null) {
			params.set("status", status);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("circle.list", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		List<Record> list = records.getList();
		for (Record record : list) {
			int memberNum = CircleMember.dao.find("select * from circle_member where circle_id = ?", record.get("id").toString()).size();
			record.set("memberNum", memberNum);
		}
		return RetUtils.okPage(records);
	}

	public Ret updateCircleStatus(Integer circleId, Integer status) {
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子不存在");
		}
		if (status.equals(circle.getStatus())) {
			return Ret.fail().set("errorMsg", "未修改圈子状态");
		}
		circle.setStatus(status);
		circle.setUpdateTime(new Date());
		boolean update = circle.update();
		if (!update) {
			return Ret.fail().set("errorMsg", "修改失败，请重试");
		}
		return Ret.ok();
	}

	public Ret examineComplaint(Integer complaintId, Integer status) {
		CircleComplaint complaint = CircleComplaint.dao.findById(complaintId);
		if (complaint == null) {
			return Ret.fail().set("errorMsg", "投诉不存在");
		}
		if (!complaint.getStatus().equals(0)) {
			return Ret.fail().set("errorMsg", "该投诉已审核");
		}
		Circle circle = Circle.dao.findById(complaint.getCircleId());
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子已解散");
		}
		if (status.equals(1)) {
			circle.setStatus(-1);
			boolean update1 = circle.update();
			if (!update1) {
				return Ret.fail().set("errorMsg", "审核失败，请重试");
			}
		}
		complaint.setStatus(status);
		boolean update = complaint.update();
		if (!update) {
			return Ret.fail().set("errorMsg", "审核失败，请重试");
		}
		return Ret.ok();
	}

	public Ret setCircleRecommend(Integer circleId, Integer status) {
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子已解散");
		}
		circle.setIsRecommend(status);
		boolean update = circle.update();
		if (!update) {
			return Ret.fail().set("errorMsg", "修改失败，请重试");
		}
		return Ret.ok();
	}

    public Ret circleArticleList(Integer pageNumber, Integer pageSize, String searchkey) {

		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("circle.articlelist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
    }

	public Ret commentList(Integer articleId) {
		Kv params = Kv.create().set("articleId", articleId);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("circle.commentlist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(1, 1000, sqlPara);
		return RetUtils.okPage(records);
	}
	public Ret likeList(Integer articleId) {
		Kv params = Kv.create().set("articleId", articleId);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("circle.likelist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(1, 1000, sqlPara);
		return RetUtils.okPage(records);
	}

	public Ret addCircleManager(Integer circleId, String uids) {
		String[] uidList = uids.split(",");
		List<String> errorList = new ArrayList<>();
		for (String uid : uidList) {
			CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where uid = ? and circle_id = ?", uid, circleId);
			if (circleMember == null) {
				errorList.add(uid);
				continue;
			}
			if (circleMember.getRole().equals(1) || circleMember.getRole().equals(2)) {
				return Ret.fail().set("errorMsg", "圈主和管理员不能被设置为管理员");
			}
			circleMember.setRole(2);
			circleMember.update();
		}
		if (errorList.size() > 0) {
			return Ret.ok().set("errorList", errorList);
		}
		return Ret.ok();
	}

	public Ret setMemberRole(Circle circle, User user, Integer role) {
		CircleMember circleMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circle.getId(), user.getId());
		if (circleMember == null) {
			return Ret.fail().set("errorMsg", "选择用户不是该圈子成员");
		}
		if (circleMember.getRole().equals(role)) {
			return Ret.fail().set("errorMsg", "该用户已是该角色，无需修改");
		}
		if (circleMember.getRole().equals(1)) {
			return Ret.fail().set("errorMsg", "无法直接修改圈主的角色");
		}

		circleMember.setRole(role);
		circleMember.setUpdateTime(new Date());
		if (role.equals(1)) {
			CircleMember circleMember1 = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and role = 1", circle.getId());
			circleMember1.setRole(3);
			circleMember1.setUpdateTime(new Date());
			circleMember1.update();

			circle.setUid(user.getId());
			circle.setUpdateTime(new Date());

			CircleApply circleApply = CircleApply.dao.findById(circle.getId());
			circleApply.setUid(user.getId());
			circleApply.setUpdateTime(new Date());

			boolean update = circleApply.update();
			boolean update1 = circle.update();
			if (!update || !update1) {
				return Ret.fail().set("errorMsg", "服务器异常，请重试");
			}
		}
		boolean update = circleMember.update();
		if (!update) {
			return Ret.fail().set("errorMsg", "服务器异常，请重试");
		}
		return Ret.ok();

	}

	public Ret delMember(Integer circleId, String uids) {
		Circle circle = Circle.dao.findById(circleId);
		CircleMember manager = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and role = 1", circleId);
		if (uids.contains(manager.getUid().toString())) {
			return Ret.fail().set("errorMsg", "不能删除圈主");
		}
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子不存在");
		}
		String[] uidList = uids.split(",");
		List<CircleMember> delMemberList = new ArrayList<>();
		for (String uid : uidList) {
			CircleMember delMember = CircleMember.dao.findFirst("select * from circle_member where circle_id = ? and uid = ?", circleId, uid);
			if (delMember.getRole().equals(1)) {
				return Ret.fail().set("errorMsg", "无法直接移除圈主，请先转让圈主权限");
			}
			delMemberList.add(delMember);
			inviteCodeInvalid(Integer.valueOf(uid), circleId);
		}
		if (delMemberList.size() != 0) {
			for (CircleMember delMember : delMemberList) {
				delMember.delete();
				User operatedUser = User.dao.findById(delMember.getUid());
				addCircleLog(null,operatedUser,circleId,2,2);
			}
		}

		return Ret.ok();


	}

	public Ret inviteCodeInvalid(Integer uid, Integer circleId) {
		Kv params = Kv.create();
		params.set("circleId", circleId);
		params.set("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("circle.updateInviteCodeInvalid", params);
		int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
		return Ret.ok();
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
				circleLog.setDescribe("被 后台管理员 移除关注");
			}
		}
		boolean save = circleLog.save();
		if (!save) {
			log.error("日志生成失败：{}", circleLog);
			return Ret.fail().set("errorMsg", "日志生成失败");
		}
		return Ret.ok();
	}

	public Ret delCircle(Integer circleId) {
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Ret.fail().set("errorMsg", "圈子不存在");
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
