
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioLoginService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 钛信登录统计管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/tiologin")
public class TioLoginController {
	private static Logger log = LoggerFactory.getLogger(TioLoginController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioLoginService loginService = TioLoginService.me;
	
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
	public Resp loginlist(HttpRequest request,String ip,String searchkey,String starttime,String endtime,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginService.loginList(pageNumber, pageSize, searchkey, ip, starttime, endtime);
		if(ret.isFail()) {
			log.error("获取日志列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
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
		Ret ret = loginService.statTimeList(pageNumber, pageSize, starttime, endtime);
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
		Ret ret = loginService.statTimeUserList(pageNumber, pageSize,period);
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
	@RequestPath(value = "/timeloginlist")
	public Resp timeLoginList(HttpRequest request,String period,Integer uid) throws Exception {
		Ret ret = loginService.statTimeLoginList(period, uid);
		if(ret.isFail()) {
			log.error("获取登录-时间-用户-日志-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	
	
	/**
	 * 登录-ip-统计
	 * @param request
	 * @param ip
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:39:41
	 */
	@RequestPath(value = "/iplist")
	public Resp ipList(HttpRequest request,String ip,Integer pageNumber,Integer pageSize,String order) throws Exception {
		Ret ret = loginService.statIpList(pageNumber, pageSize, ip,order);
		if(ret.isFail()) {
			log.error("获取登录-ip-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-IP-天-统计
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:36:05
	 */
	@RequestPath(value = "/ipdaylist")
	public Resp ipDayList(HttpRequest request,String ip,String order,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginService.statIpDayList(pageNumber, pageSize,ip,order);
		if(ret.isFail()) {
			log.error("获取登录-IP-天-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-ip-用户-统计
	 * @param request
	 * @param period
	 * @param ip
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:41:57
	 */
	@RequestPath(value = "/ipuserlist")
	public Resp ipUserList(HttpRequest request,String period,String ip,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = loginService.statIpUserList(pageNumber, pageSize,period, ip);
		if(ret.isFail()) {
			log.error("获取登录-ip-用户-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 登录-ip-用户-日志-统计
	 * @param request
	 * @param period
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月16日 下午4:36:40
	 */
	@RequestPath(value = "/iploginlist")
	public Resp iploginlist(HttpRequest request,String period,Integer uid,String ip) throws Exception {
		Ret ret = loginService.statIpLoginList(period, uid, ip);
		if(ret.isFail()) {
			log.error("获取登录-ip-用户-日志-统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	
}
