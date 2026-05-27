
package org.tio.mg.web.server.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.mg.MgLoginStatService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * 钛信登录统计管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mgloginstat")
public class MgLoginStatController {
	private static Logger log = LoggerFactory.getLogger(MgLoginStatController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private MgLoginStatService loginStatService = MgLoginStatService.ME;
	
	/**
	 * 登录日志
	 * @param request
	 * @param ip
	 * @param searchkey
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:44:25
	 */
	@RequestPath(value = "/loginlist")
	public Resp loginlist(HttpRequest request,String ip,String searchkey,Short rid,String starttime,String endtime,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginStatService.loginList(pageNumber, pageSize, searchkey, ip, starttime, endtime,rid);
		if(ret.isFail()) {
			log.error("获取日志列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 
	 * @param request
	 * @param off
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 上午11:48:00
	 */
	@RequestPath(value = "/stat")
	public Resp stat(HttpRequest request,Integer off) throws Exception {
		loginStatService.loginTimeStat(DateUtil.offsetDay(new DateTime(), off));
		return Resp.ok(RetUtils.OPER_RIGHT);
	}
	
	/**
	 * 登录-时间-统计
	 * @param request
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:34:53
	 */
	@RequestPath(value = "/timelist")
	public Resp timeList(HttpRequest request,String starttime,String endtime,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginStatService.statTimeList(pageNumber, pageSize, starttime, endtime);
		if(ret.isFail()) {
			log.error("获取登录-时间-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-用户-统计
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:36:05
	 */
	@RequestPath(value = "/timeuserlist")
	public Resp timeUserList(HttpRequest request,String period,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginStatService.statTimeUserList(pageNumber, pageSize,period);
		if(ret.isFail()) {
			log.error("获取登录-时间-用户-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-时间-用户-日志-统计
	 * @param request
	 * @param period
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:36:40
	 */
	@RequestPath(value = "/logininfolist")
	public Resp logininfolist(HttpRequest request,String period,Integer mguid) throws Exception {
		Ret ret = loginStatService.statLoginList(period, mguid);
		if(ret.isFail()) {
			log.error("获取登录-时间-用户-日志-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	
	
	/**
	 * 登录-用户-统计
	 * @param request
	 * @param ip
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:39:41
	 */
	@RequestPath(value = "/userList")
	public Resp userList(HttpRequest request,String searchkey,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginStatService.statUserList(pageNumber, pageSize, searchkey);
		if(ret.isFail()) {
			log.error("获取登录-用户-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-用户-天-统计
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:36:05
	 */
	@RequestPath(value = "/userdaylist")
	public Resp userdaylist(HttpRequest request,Integer mguid,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginStatService.statUserDayList(pageNumber, pageSize, mguid);
		if(ret.isFail()) {
			log.error("获取登录-IP-天-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
}
