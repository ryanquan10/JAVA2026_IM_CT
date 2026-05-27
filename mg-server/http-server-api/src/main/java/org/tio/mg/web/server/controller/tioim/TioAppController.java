
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.main.WxApp;
import org.tio.mg.service.service.tioim.TioAppService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 钛信用户管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/wxapp")
public class TioAppController {
	private static Logger log = LoggerFactory.getLogger(TioAppController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioAppService appService = TioAppService.me;
	
	
	/**
	 * 获取版本列表
	 * @param request
	 * @param version
	 * @param status
	 * @param type
	 * @param mode
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月29日 上午10:30:10
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,String version,Short status,Short type,Short mode,Integer pageNumber,Integer pageSize) throws Exception {
		Ret ret = appService.appList(pageNumber, pageSize, version, mode, type, status);
		if(ret.isFail()) {
			log.error("获取版本列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 新增app版本信息
	 * @param request
	 * @param app
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月29日 上午10:35:05
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,WxApp app) throws Exception {
		Ret ret = appService.add(app);
		if(ret.isFail()) {
			log.error("新增失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 修改app版本信息
	 * @param request
	 * @param app
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午5:01:51
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,WxApp app) throws Exception {
		Ret ret = appService.update(app);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 删除app版本
	 * @param request
	 * @param id
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午5:01:26
	 */
	@RequestPath(value = "/del")
	public Resp del(HttpRequest request,Integer id) throws Exception {
		Ret ret = appService.del(id);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
}
