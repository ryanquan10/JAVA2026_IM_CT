
package org.tio.mg.web.server.controller.tioim;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.WxGroup;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.tioim.TioGroupService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.ImManagerTopicVo;
import org.tio.utils.resp.Resp;

/**
 * 钛信群管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/group")
public class TioGroupController {
	private static Logger log = LoggerFactory.getLogger(TioGroupController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioGroupService groupService = TioGroupService.me;
	
	/**
	 * 获取群列表
	 * @param request
	 * @param searchkey
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:30:22
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,String searchkey,String groupkey,Integer pageNumber,Integer pageSize,String starttime,String endtime) throws Exception {
		Ret ret = groupService.groupList(pageNumber, pageSize, searchkey, groupkey,starttime,endtime);
		if(ret.isFail()) {
			log.error("获取群列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * @param request
	 * @param searchkey
	 * @param groupkey
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月18日 上午11:39:14
	 */
	@RequestPath(value = "/managerlist")
	public Resp managerlist(HttpRequest request,String searchkey,String groupkey,Short status,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = groupService.managerGroupList(pageNumber, pageSize, searchkey, groupkey, status);
		if(ret.isFail()) {
			log.error("获取群管理列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	
	/**
	 * 禁言用户列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月24日 下午5:32:24
	 */
	@RequestPath(value = "/forbiddenUserList")
	public Resp forbiddenUserList(HttpRequest request,Integer pageNumber,Integer pageSize,Long groupid) throws Exception {
		Ret ret = groupService.forbiddenUserList(pageNumber, pageSize, groupid);
		if(ret.isFail()) {
			log.error("获取禁用列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群官列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月24日 下午5:47:27
	 */
	@RequestPath(value = "/managerUserList")
	public Resp managerUserList(HttpRequest request,Integer pageNumber,Integer pageSize,Long groupid) throws Exception {
		Ret ret = groupService.managerUserList(pageNumber, pageSize, groupid);
		if(ret.isFail()) {
			log.error("获取群管理员列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 举报列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月24日 下午5:48:04
	 */
	@RequestPath(value = "/reportlist")
	public Resp reportlist(HttpRequest request,Integer pageNumber,Integer pageSize,Long groupid,Short status) throws Exception {
		Ret ret = groupService.reportList(pageNumber, pageSize, groupid,status);
		if(ret.isFail()) {
			log.error("获取群举报列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 封停列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月25日 下午3:51:01
	 */
	@RequestPath(value = "/inblackoperlist")
	public Resp inblackoperlist(HttpRequest request,Integer pageNumber,Integer pageSize,Long groupid) throws Exception {
		Ret ret = groupService.inblackOperlist(pageNumber, pageSize, groupid);
		if(ret.isFail()) {
			log.error("获取群举报列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 获取无效群列表
	 * @param request
	 * @param searchkey
	 * @param groupkey
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:34:50
	 */
	@RequestPath(value = "/dellist")
	public Resp delList(HttpRequest request,String searchkey,String groupkey,Integer pageNumber,Integer pageSize,String starttime,String endtime) throws Exception {
		Ret ret = groupService.delgroupList(pageNumber, pageSize, searchkey, groupkey,starttime,endtime);
		if(ret.isFail()) {
			log.error("获取群列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群列表
	 * @param request
	 * @param searchkey
	 * @param groupkey
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月8日 下午5:32:26
	 */
	@RequestPath(value = "/alllist")
	public Resp allList(HttpRequest request,String searchkey,String groupkey,Integer pageNumber,Integer pageSize,Short type,String starttime,String endtime) throws Exception {
		Ret ret = null;
		if(Objects.equals(type, Const.Status.NORMAL)) {
			ret = groupService.groupList(pageNumber, pageSize, searchkey, groupkey,starttime,endtime);
		} else {
			ret = groupService.delgroupList(pageNumber, pageSize, searchkey, groupkey,starttime,endtime);
		}
		if(ret.isFail()) {
			log.error("获取群列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 消息模型下的群列表
	 * @param request
	 * @param groupkey
	 * @param contenttype
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月8日 下午4:30:10
	 */
	@RequestPath(value = "/modegrouplist")
	public Resp modeGroupList(HttpRequest request,String groupkey,String starttime,String endtime,Integer pageNumber,Integer pageSize,Short type, String searchkey) throws Exception {
		Ret ret = groupService.modeGroupAboutMsgList(pageNumber, pageSize, groupkey, starttime, endtime,type,searchkey);
		if(ret.isFail()) {
			log.error("获取群列表(消息模型)失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}


	
	/**
	 * @param request
	 * @param groupid
	 * @param reason
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月24日 下午2:23:01
	 */
	@RequestPath(value = "/inblack")
	public Resp inblack(HttpRequest request,Long groupid,String reason,Short status) throws Exception {
		MgUser user = WebUtils.currUser(request);
		Ret ret = groupService.inblack(groupid, status, reason,user);
		if(ret.isFail()) {
			log.error("封停操作失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("groupid", groupid);
		params.put("status", status);
		RTopic topic = redisson.getTopic(Const.Topic.IM_MANAGER_OPER);
		ImManagerTopicVo topicVo = new ImManagerTopicVo();
		topicVo.setType(ImManagerTopicVo.Type.GROUP_INBLACK_OPER);
		topicVo.setParams(params);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	
	/**
	 * @param request
	 * @param ids
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年2月25日 下午4:33:38
	 */
	@RequestPath(value = "/reportdeal")
	public Resp reportdeal(HttpRequest request,String ids) throws Exception {
		MgUser user = WebUtils.currUser(request);
		Ret ret = groupService.reportDeal(ids,user);
		if(ret.isFail()) {
			log.error("举报标记失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}



	/**
	 * @param request
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xinjili
	 * 2024年4月11日 上午10:33:38
	 */
	@RequestPath(value = "/reportlistnew")
	public Resp reportlistnew(HttpRequest request, Integer pageNumber,Integer pageSize,String searchkey,Short status) throws Exception {
		Ret ret = groupService.reportListNew(pageNumber, pageSize, searchkey,status);
		if(ret.isFail()) {
			log.error("获取群举报列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}


	/**
	 * @param request
	 * @param request
	 * @param ids
	 * @param status 0 不对群组操作 1 封停群组
	 * @return
	 * @throws Exception
	 * @author xinjili
	 * 2024年4月11日 上午11:13:38
	 */
	@RequestPath(value = "/handlingReport")
	public Resp handlingReport(HttpRequest request, String ids, Integer status, String reason) throws Exception {
		MgUser user = WebUtils.currUser(request);
		if (!status.equals(0) && !status.equals(1)) {
			return Resp.fail().msg("参数异常");
		}

		Ret ret = groupService.handlingReport(ids, status, user, reason);
		if(ret.isFail()) {
			log.error("操作失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok();
	}

	@RequestPath(value = "/updateVnum")
	public Resp updateVnum(HttpRequest request, String groupid, Integer vnum) throws Exception {
		WxGroup group = WxGroup.dao.findById(groupid);
		if (group == null) {
			return Resp.fail("群组不存在");
		}
		group.setVnum(vnum);
		boolean update = group.update();
		if (!update) {
			return Resp.fail("系统异常, 请重试");
		}
		return Resp.ok();
	}

}
