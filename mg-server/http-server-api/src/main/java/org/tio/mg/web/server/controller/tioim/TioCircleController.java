
package org.tio.mg.web.server.controller.tioim;

import net.sourceforge.pinyin4j.PinyinHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.main.*;
import org.tio.mg.service.service.tioim.TioCircleService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.resp.Resp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 圈子管理
 * @author xinji
 * 2024年1月2日
 */
@RequestPath(value = "/circle")
public class TioCircleController {
	private static Logger log = LoggerFactory.getLogger(TioCircleController.class);

	/**
	 * @param args
	 * @author xinji
	 * 2024年1月2日
	 */
	public static void main(String[] args) {

	}

	private TioCircleService tioCircleService = TioCircleService.me;

	// 圈子管理
	/**
	 * 圈子申请创建列表
	 * @param request
	 * @param searchKey
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/applyList")
	public Resp applyList(HttpRequest request,String searchKey, Short status,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = tioCircleService.applyList(pageNumber, pageSize, searchKey, status);
		if(ret.isFail()) {
			log.error("获取圈子申请列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 审核创建圈子申请
	 * @param request
	 * @param circleApplyId
	 * @param status
	 * @param refuseReason
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/updateApplyStatus")
	public Resp updateApplyStatus(HttpRequest request, Integer circleApplyId, Integer status, String refuseReason) throws Exception {
		if (circleApplyId == null) {
			return Resp.fail("参数异常，申请id为空");
		}
		if (status == null) {
			return Resp.fail("参数异常，修改状态为空");
		}
		if (!status.equals(0) && !status.equals(2) && !status.equals(1)) {
			return Resp.fail("参数异常，该修改状态不存在");
		}
		Ret ret = tioCircleService.updateApplyStatus(circleApplyId, status, refuseReason);
		if (ret.get("data").equals(Const.YesOrNo.NO)) {
			return Resp.fail().msg(ret.get("errorMsg").toString());
		}
		return Resp.ok("操作成功");
	}

	/**
	 * 圈子列表
	 * @param request 请求
	 * @param searchkey 搜索关键字
	 * @param status  查询所有圈子传空。1:正常状态的圈子 2:被封的圈子
	 * @param pageNumber 页码
	 * @param pageSize 大小
	 * @return Page<Record>
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/circleList")
	public Resp circleList(HttpRequest request,String searchkey, Short status, Integer pageNumber, Integer pageSize) throws Exception {
		Ret ret = tioCircleService.circleList(pageNumber, pageSize, searchkey, status);
		if(ret.isFail()) {
			return Resp.fail(ret.get("errorMsg").toString());
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

    /**
     * 修改圈子状态
     * @param request
     * @param circleId
     * @param status
     * @return
     * @throws Exception
     * @author xinji
     */
    @RequestPath(value = "/updateCircleStatus")
    public Resp updateCircleStatus(HttpRequest request, Integer circleId, Integer status) throws Exception {
        if (status == null) {
            return Resp.fail("请选择修改状态");
        }
        Ret ret = tioCircleService.updateCircleStatus(circleId, status);
        if(ret.isFail()) {
            return Resp.fail(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 查看圈子成员列表
     * @param request
     * @param circleId
     * @return
     * @throws Exception
     * @author xinji
     */
    @RequestPath(value = "/circleMemberList")
    public Resp updateCircleStatus(HttpRequest request, Integer circleId) throws Exception {
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail("圈子不存在");
        }
        List<CircleMember> circleMembers = CircleMember.dao.find("select * from circle_member where circle_id = ?", circleId);
        return Resp.ok(circleMembers);
    }

	/**
	 * 修改圈子信息
	 * @param request 请求request
	 * @param circleId 圈子id
	 * @param avatar 头像url
	 * @param name 圈子名称
	 * @param describe 圈子描述
	 * @param isOpen 是否公开，1公开 2私有
	 * @param isExamine 是否需要审核入圈
	 * @param isInvite 是否需要圈子成员邀请
	 * @param inviteNum 需要邀请的人数
	 * @return
	 * @author xinji
	 * @throws Exception
	 */
	@RequestPath(value = "/updateCircle")
	public Resp updateCircle(HttpRequest request, Integer circleId, Integer showId, String avatar, String name, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum) throws Exception {
		if (circleId == null) {
			return Resp.fail().msg("请选择需要修改的圈子");
		}
		if (avatar == null && name == null && describe == null && isOpen == null && isExamine == null && isInvite == null && inviteNum == null && showId == null) {
			return Resp.fail().msg("无修改内容");
		}
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Resp.fail().msg("圈子不存在");
		}
		if (isOpen != null && !isOpen.equals(1)) {
			if (inviteNum != null && inviteNum < 1) {
				return Resp.fail().msg("私有圈子至少需要一个成员的邀请");
			}
			if (circle.getInviteNum() < 1 && inviteNum == null) {
				return Resp.fail().msg("私有圈子至少需要一个成员的邀请");
			}
		}
		CircleApply circleApply = CircleApply.dao.findById(circleId);

		if (avatar != null && !circle.getAvatar().equals(avatar)) {
			circle.setAvatar(avatar);
			circleApply.setAvatar(avatar);
		}
		if (showId != null && !showId.equals(circle.getShowId())) {
            Circle checkCircle = Circle.dao.findFirst("select * from circle where show_id = ? and id != ?", showId, circleId);
            if (checkCircle != null) {
                return Resp.fail().msg("id已被使用");
            }
            circle.setShowId(showId);
		}
		if (name != null && !circle.getName().equals(name)) {
			circle.setName(name);
			circleApply.setName(name);
		}
		if (describe != null && !circle.getDescribe().equals(describe)) {
			circle.setDescribe(describe);
			circleApply.setDescribe(describe);
		}
		if (isOpen != null && !circle.getIsOpen().equals(isOpen)) {
			circle.setIsOpen(isOpen);
			circleApply.setIsOpen(isOpen);
		}
		if (isExamine != null && !circle.getIsExamine().equals(isExamine)) {
			circle.setIsExamine(isExamine);
			circleApply.setIsExamine(isExamine);
		}
		if (isInvite != null && !circle.getIsInvite().equals(isInvite)) {
			circle.setIsInvite(isInvite);
			circleApply.setIsInvite(isInvite);
		}
		if (inviteNum != null && !circle.getInviteNum().equals(inviteNum)) {
			circle.setInviteNum(inviteNum);
			circleApply.setInviteNum(inviteNum);
		}
		boolean update = circle.update();
		boolean update1 = circleApply.update();
		if (!update || !update1) {
			return Resp.fail().msg("修改失败或没有修改内容，请重试");
		}

		return Resp.ok("修改成功");
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
        Circle circle = Circle.dao.findById(circleId);
        if (circle == null) {
            return Resp.fail().msg("圈子不存在");
        }
        Ret ret = tioCircleService.addCircleManager(circleId, uids);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
		if (ret.get("errorList") != null) {
			return Resp.ok().msg("以下成员添加失败" + ret.get("errorList"));
		}
        return Resp.ok("添加成功");
    }


	/**
	 * 设置用户角色
	 * @param request
	 * @param circleId
	 * @return
	 * @author xinji
	 * @throws Exception
	 */
	@RequestPath(value = "/setMemberRole")
	public Resp setMemberRole(HttpRequest request, Integer circleId, Integer uid, Integer role) throws Exception {
		if (uid == null) {
			return Resp.fail().msg("请选择用户");
		}
		if (!role.equals(1) && !role.equals(2) && !role.equals(3)) {
			return Resp.fail().msg("角色不存在");
		}
		Circle circle = Circle.dao.findById(circleId);
		if (circle == null) {
			return Resp.fail().msg("圈子不存在");
		}
		User user = User.dao.findById(uid);
		if (user == null) {
			return Resp.fail().msg("选择用户不存在");
		}
		Ret ret = tioCircleService.setMemberRole(circle, user, role);
		if (ret.isFail()) {
			return Resp.fail().msg(ret.get("errorMsg").toString());
		}
		return Resp.ok("添加成功");
	}


	/**
	 * 移除圈子用户
	 * @param request
	 * @param circleId
	 * @return
	 * @author xinji
	 * @throws Exception
	 */
	@RequestPath(value = "/delMember")
	public Resp delMember(HttpRequest request, Integer circleId, String uids) throws Exception {
		Ret ret = tioCircleService.delMember(circleId, uids);
		if (ret.isFail()) {
			return Resp.fail().msg(ret.get("errorMsg").toString());
		}
		return Resp.ok("删除成功");
	}

	/**
	 * 删除圈子
	 * @param request
	 * @param circleId
	 * @return
	 * @author xinji
	 * @throws Exception
	 */
	@RequestPath(value = "/delCircle")
	public Resp delCircle(HttpRequest request, Integer circleId) throws Exception {
		Ret ret = tioCircleService.delCircle(circleId);
		if (ret.isFail()) {
			return Resp.fail().msg(ret.get("errorMsg").toString());
		}
		return Resp.ok("删除成功");
	}

    // 设置推荐
	/**
	 * 设置推荐圈子
	 * @param request 请求request
	 * @param circleId 圈子id
	 * @param status 0:不推荐 1:推荐
	 * @return
	 * @throws Exception
	 * @author xinji
	 */
	@RequestPath(value = "/updateCircleRecommend")
	public Resp setCircleRecommend(HttpRequest request, Integer circleId, Integer status) throws Exception {
		if (status == null) {
			return Resp.fail("请选择修改状态");
		}
		Ret ret = tioCircleService.setCircleRecommend(circleId, status);
		if(ret.isFail()) {
			return Resp.fail(ret.get("errorMsg").toString());
		}
		return Resp.ok();
	}


    /**
     * 设置推荐城市
     * @param request 请求request
     * @param cityId 城市id
     * @param status 1:推荐，0:不推荐
     * @return
     * @throws Exception
     * @author xinji
     */
    @RequestPath(value = "/setCityRecommend")
    public Resp setCityRecommend(HttpRequest request, Integer cityId, Integer status) throws Exception {
        City city = City.dao.findById(cityId);
        if (city == null) {
            return Resp.fail().msg("城市不存在");
        }
        city.setIsRecommend(status);
        city.update();
        return Resp.ok();
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
	 * 获取热门城市列表
	 * @param request
	 * @return
	 * @author xinji
	 * @throws Exception
	 */
	@RequestPath(value = "/recommendCityList")
	public Resp recommendCityList(HttpRequest request) throws Exception {
		List<City> cityList = City.dao.find("select * from city where is_recommend = 1");
		return Resp.ok(cityList);
	}

	// 投诉管理
	/**
	 * 查看投诉列表
	 * @param request 请求request
	 * @param status null: 查询所有投诉信息，0：未审核的投诉列表，1：已通过的投诉列表，2：已拒绝的投诉列表
	 * @return
	 * @throws Exception
	 * @author xinji
	 */
	@RequestPath(value = "/circleComplaintList")
	public Resp circleComplaintList(HttpRequest request, Integer status) throws Exception {
		if (status == null) {
			return Resp.ok(CircleComplaint.dao.findAll());
		} else {
			return Resp.ok(CircleComplaint.dao.find("select * from circle_complaint where status = ?", status));
		}
	}


	/**
	 * 投诉详情
	 * @param request 请求request
	 * @param complaintId 投诉id
	 * @return
	 * @throws Exception
	 * @author xinji
	 */
	@RequestPath(value = "/circleComplaintInfo")
	public Resp circleComplaintInfo(HttpRequest request, Integer complaintId) throws Exception {
		CircleComplaint circleComplaint = CircleComplaint.dao.findById(complaintId);
		if (circleComplaint == null) {
			return Resp.fail().msg("该投诉不存在");
		}
		Map<String, Object> result = new HashMap<>();
		result.put("complaint", circleComplaint);
		Circle circle = Circle.dao.findById(circleComplaint.getCircleId());
		if (circle == null) {
			return Resp.fail().msg("该圈子已解散");
		}
		result.put("circle", circle);
		return Resp.ok(result);
	}


	/**
	 * 审核投诉
	 * @param request 请求request
	 * @param complaintId 投诉id
	 * @param complaintId 1 通过 2拒绝
	 * @return
	 * @throws Exception
	 * @author xinji
	 */
	@RequestPath(value = "/examineComplaint")
	public Resp examineComplaint(HttpRequest request, Integer complaintId, Integer status) throws Exception {
		if (status == null) {
			return Resp.fail().msg("审核结果不能为空");
		}
		Ret ret = tioCircleService.examineComplaint(complaintId, status);
		if (ret.isFail()) {
			return Resp.fail().msg(ret.get("errorMsg").toString());
		}
		return Resp.ok();
	}



	// 文章管理
	/**
	 * 圈子文章列表
	 * @param request 请求
	 * @param searchkey 搜索关键字
	 * @param pageNumber 页码
	 * @param pageSize 大小
	 * @return Page<Record>
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/circleArticleList")
	public Resp circleArticleList(HttpRequest request, String searchkey, Integer pageNumber, Integer pageSize) throws Exception {
		Ret ret = tioCircleService.circleArticleList(pageNumber, pageSize, searchkey);
		if(ret.isFail()) {
			return Resp.fail(ret.get("errorMsg").toString());
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 删除圈子文章
	 * @param request 请求
	 * @param articleIds 文章id 使用 , 分隔开
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/circleArticleDel")
	public Resp circleArticleDel(HttpRequest request, String articleIds) throws Exception {
		if (articleIds == null || articleIds.isEmpty()) {
			return Resp.fail().msg("请选择需要删除的文章");
		}
		String[] ids = articleIds.split(",");
		List<String> delFail = new ArrayList<>();
		for (String id : ids) {
			CircleArticle circleArticle = CircleArticle.dao.findById(id);
			if (circleArticle == null) {
				delFail.add(id);
				continue;
			}
			boolean delete = circleArticle.delete();
			if (!delete) {
				delFail.add(id);
			}
		}
		if (delFail.size() > 0) {
			return Resp.fail().msg(delFail + " 以上id删除失败，请重试");
		}
		return Resp.ok();
	}

	/**
	 * 查看文章评论点赞列表
	 * @param request 请求
	 * @param articleId 文章id 使用
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/articleDetails")
	public Resp articleDetails(HttpRequest request, Integer articleId) throws Exception {
		if (articleId == null) {
			return Resp.fail().msg("请选择需要查看详情的文章");
		}
		Map<String, Object> data = new HashMap<>();
		CircleArticle article = CircleArticle.dao.findById(articleId);
		if (article == null) {
			return Resp.fail().msg("文章不存在");
		}
		data.put("article", article);

//		List<CircleArticleComment> circleArticleComments = CircleArticleComment.dao.find("select * from circle_article_comment where article_id = ?", articleId);
		Ret circleArticleComments = tioCircleService.commentList(articleId);
		data.put("comments", RetUtils.getOkPage(circleArticleComments));

//		List<CircleArticleLike> circleArticleLikes = CircleArticleLike.dao.find("select * from circle_article_like where article_id = ?", articleId);
		Ret circleArticleLikes = tioCircleService.likeList(articleId);
		data.put("likes", RetUtils.getOkPage(circleArticleLikes));
		return Resp.ok(data);
	}

	/**
	 * 删除圈子文章评论
	 * @param request 请求
	 * @param commentId 文章id
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年1月4日
	 */
	@RequestPath(value = "/articleCommentDel")
	public Resp circleArticleDel(HttpRequest request, Integer commentId) throws Exception {
		if (commentId == null) {
			return Resp.fail().msg("请选择需要删除的评论");
		}
		CircleArticleComment comment = CircleArticleComment.dao.findById(commentId);
		if (comment == null) {
			return Resp.fail().msg("删除的评论不存在或已被删除");
		}
		boolean delete = comment.delete();
		return Resp.ok();
	}
}
