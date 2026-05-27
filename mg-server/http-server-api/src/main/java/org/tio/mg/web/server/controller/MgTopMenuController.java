
package org.tio.mg.web.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.mg.MgFavoritePath;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.mg.MgTopMenuService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

/**
 * 头部接口
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/topmenu")
public class MgTopMenuController {
	private static Logger log = LoggerFactory.getLogger(MgTopMenuController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private MgTopMenuService topService = MgTopMenuService.ME;

	/**
	 *
	 * @author tanyaowu
	 */
	public MgTopMenuController() {
	}
	
	
	
	/**
	 * 调整顺序
	 * @param request
	 * @param did
	 * @param topdid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月10日 上午11:39:52
	 */
	@RequestPath(value = "/index")
	public Resp index(HttpRequest request,Integer id,Integer topid) throws Exception {
		MgUser user = WebUtils.currUser(request);
		Ret ret = topService.index(id, topid, user.getId());
		if(ret.isFail()) {
			log.error("调整失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 新增
	 * @param request
	 * @param path
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月7日 下午4:50:10
	 */
	@RequestPath(value = "/addfavorite")
	public Resp addfavorite(HttpRequest request,MgFavoritePath path) throws Exception {
		MgUser user = WebUtils.currUser(request);
		path.setMguid(user.getId());
		Ret ret = topService.addfavorite(path);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 删除
	 * @param request
	 * @param id
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月7日 下午4:50:34
	 */
	@RequestPath(value = "/delfavorite")
	public Resp delfavorite(HttpRequest request,Integer id) throws Exception {
		Ret ret = topService.delfavorite(id);
		if(ret.isFail()) {
			log.error("删除失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 收藏列表
	 * @param request
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月7日 下午4:48:57
	 */
	@RequestPath(value = "/favoritelist")
	public Resp favoritelist(HttpRequest request,Integer pageSize) throws Exception {
		MgUser user = WebUtils.currUser(request);
		Ret ret = topService.favoritePage(pageSize, user.getId());
		if(ret.isFail()) {
			log.error("获取收藏列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	
	/**
	 * 最近打开列表
	 * @param request
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月7日 下午4:49:35
	 */
	@RequestPath(value = "/recentpage")
	public Resp recentpage(HttpRequest request,Integer pageSize) throws Exception {
		MgUser user = WebUtils.currUser(request);
		Ret ret = topService.recentPage(pageSize, user.getId());
		if(ret.isFail()) {
			log.error("获取最近打开列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
}
