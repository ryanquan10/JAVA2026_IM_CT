
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioStatService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 钛信统计管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/stat")
public class TioStatController {
	private static Logger log = LoggerFactory.getLogger(TioStatController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioStatService statService = TioStatService.me;
	
	
	/**
	 * 获取用户统计列表
	 * @param request
	 * @param searchkey
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:30:22
	 */
	@RequestPath(value = "/userstatlist")
	public Resp userstatlist(HttpRequest request,String searchkey,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = statService.userStatList(pageNumber, pageSize, searchkey);
		if(ret.isFail()) {
			log.error("获取用户统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 区域字典
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午3:44:17
	 */
	@RequestPath(value = "/areadict")
	public Resp areadict(HttpRequest request) throws Exception {
		Ret ret = statService.areadict();
		if(ret.isFail()) {
			log.error("获取区域字典失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	/**
	 * 用户注册统计
	 * @param request
	 * @param start
	 * @param end
	 * @param searchip
	 * @param type
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月13日 下午3:32:07
	 */
	@RequestPath(value = "/userRegisterStat")
	public Resp userRegisterStat(HttpRequest request,String start,String end,String searchip,Short type,Integer pageNumber,Integer pageSize,String province,String city,String order) throws Exception {
		Ret ret = statService.userRegisterStat(pageNumber, pageSize, start, end, searchip, type,province,city,order);
		if(ret.isFail()) {
			log.error("获取用户统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群统计
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @param starttime
	 * @param endtime
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月27日 下午2:30:15
	 */
	@RequestPath(value = "/groupstat")
	public Resp groupstat(HttpRequest request,Integer pageNumber,Integer pageSize,Short type,String starttime,String endtime) throws Exception {
		Ret ret = statService.groupStat(starttime, endtime, pageNumber, pageSize, type);
		if(ret.isFail()) {
			log.error("获取用户统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 获取用户注册下的ip周期统计
	 * @param request
	 * @param ipid
	 * @param order
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午5:14:26
	 */
	@RequestPath(value = "/userIpTimeRegisterStat")
	public Resp userIpTimeRegisterStat(HttpRequest request,Integer ipid,String order,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = statService.userIpTimeRegisterStat(pageNumber, pageSize, ipid, order);
		if(ret.isFail()) {
			log.error("获取用户ip下的时间统计列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	
	/**
	 * 总登录次数
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午5:19:05
	 */
	@RequestPath(value = "/userlogincount")
	public Resp userLoginCount(HttpRequest request,Integer uid) throws Exception {
		Ret ret = statService.userLoginCount(uid);
		if(ret.isFail()) {
			log.error("获取总登录次数：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 
	 * @param request
	 * @param ip
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午5:19:34
	 */
	@RequestPath(value = "/ipregcount")
	public Resp ipRegisterCount(HttpRequest request,String ip) throws Exception {
		Ret ret = statService.ipRegisterCount(ip);
		if(ret.isFail()) {
			log.error("获取ip注册次数：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 区域注册人数
	 * @param request
	 * @param province
	 * @param city
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午5:24:04
	 */
	@RequestPath(value = "/arearegcount")
	public Resp areaRegisterCount(HttpRequest request,String province,String city) throws Exception {
		Ret ret = statService.areaRegisterCount(province, city);
		if(ret.isFail()) {
			log.error("获取区域注册次数：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 时间注册次数
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月23日 下午5:25:03
	 */
	@RequestPath(value = "/timeregcount")
	public Resp timeRegisterCount(HttpRequest request,String period) throws Exception {
		Ret ret = statService.timeRegisterCount(period);
		if(ret.isFail()) {
			log.error("获取时间注册次数：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
}
