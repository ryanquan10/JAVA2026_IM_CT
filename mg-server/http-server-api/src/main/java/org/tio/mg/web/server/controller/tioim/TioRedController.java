
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioRedService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 钛信红包管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/red")
public class TioRedController {
	private static Logger log = LoggerFactory.getLogger(TioRedController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioRedService redService = TioRedService.me;
	
	/**
	 * 开户列表
	 * @param request
	 * @param searchkey
	 * @param walletid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月29日 上午10:30:10
	 */
	@RequestPath(value = "/openlist")
	public Resp openList(HttpRequest request,String searchkey,String walletid,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.openList(pageNumber, pageSize, searchkey, walletid);
		if(ret.isFail()) {
			log.error("获取开户列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 红包列表
	 * @param request
	 * @param searchkey
	 * @param orderno
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月12日 下午4:58:17
	 */
	@RequestPath(value = "/redlist")
	public Resp redList(HttpRequest request,String searchkey,String orderno,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.redList(pageNumber, pageSize, searchkey, orderno);
		if(ret.isFail()) {
			log.error("获取红包列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 抢红包列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2023年9月13日 下午4:58:17
	 */
	@RequestPath(value = "/grabredlist")
	public Resp grabRedList(HttpRequest request,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.grabRedList(pageNumber, pageSize);
		if(ret.isFail()) {
			log.error("获取红包列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}


	
	/**
	 * 提现列表
	 * @param request
	 * @param searchkey
	 * @param orderno
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月12日 下午4:58:15
	 */
	@RequestPath(value = "/withholdlist")
	public Resp withholdList(HttpRequest request,String searchkey,String orderno,Short status,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.withholdList(pageNumber, pageSize, searchkey, orderno, status);
		if(ret.isFail()) {
			log.error("获取提现列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 充值列表
	 * @param request
	 * @param searchkey
	 * @param orderno
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月12日 下午4:58:14
	 */
	@RequestPath(value = "/rechargelist")
	public Resp rechargeList(HttpRequest request,String searchkey,String orderno,Short status,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.rechargeList(pageNumber, pageSize, searchkey, orderno, status);
		if(ret.isFail()) {
			log.error("获取充值列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	
	/**
	 * 钱包列表
	 * @param request
	 * @param uid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月12日 下午5:19:13
	 */
	@RequestPath(value = "/coinlist")
	public Resp coinList(HttpRequest request,Integer uid,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = redService.coinList(pageNumber, pageSize, uid);
		if(ret.isFail()) {
			log.error("获取钱包列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
}
