
package org.tio.mg.web.server.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.mg.MgAuth;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.mg.MgAuthService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

/**
 * 菜单/权限请求入口
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mgauth")
public class MgAuthController {
	private static Logger log = LoggerFactory.getLogger(MgAuthController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private MgAuthService authService = MgAuthService.ME;

	/**
	 *
	 * @author tanyaowu
	 */
	public MgAuthController() {
	}
	
	/**
	 * 获取权限列表-页面
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月27日 下午2:15:44
	 */
	@RequestPath(value = "/authlist")
	public Resp authlist(HttpRequest request,String name,Short type,Short status) throws Exception {
		Ret ret = authService.authlist(name,status,type);
		if(ret.isFail()) {
			log.error("获取权限列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 纯菜单树
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月28日 下午7:16:06
	 */
	@RequestPath(value = "/menulist")
	public Resp menulist(HttpRequest request) throws Exception {
		Ret ret = authService.menulist();
		if(ret.isFail()) {
			log.error("获取菜单失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 新增
	 * @param request
	 * @param auth
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月28日 下午6:34:35
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,MgAuth auth) throws Exception {
		Ret ret = authService.add(auth);
		if(ret.isFail()) {
			log.error("保存失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 菜单/页面修改
	 * @param request
	 * @param auth
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月28日 下午6:45:37
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,MgAuth auth) throws Exception {
		Ret ret = authService.update(auth);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 菜单/页面删除
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:17:49
	 */
	@RequestPath(value = "/del")
	public Resp del(HttpRequest request,Integer aid) throws Exception {
		Ret ret = authService.del(aid);
		if(ret.isFail()) {
			log.error("删除失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 页面的操作权限列表-所有页面使用
	 * @param request
	 * @param path
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月11日 上午10:03:00
	 */
	@RequestPath(value = "/pageAuthList")
	public Resp pageAuthList(HttpRequest request,String path) throws Exception {
		if(Objects.equals(path,"index")) {
			return Resp.ok();		
		}
		MgUser user = WebUtils.currUser(request);
		if (user == null) {
			return Resp.fail("会话失效");
		}
		Ret ret = authService.pageoperlist(user.getId(),path);
		if(ret.isFail()) {
			log.error("获取失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret)).code(RetUtils.getIntCode(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	
	/**
	 * 重排序
	 * @param request
	 * @param aid
	 * @param toaid
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月3日 上午10:32:10
	 */
	@RequestPath(value = "/index")
	public Resp index(HttpRequest request,Integer aid,Integer toaid,Short topflag) throws Exception {
		Ret ret = authService.index(aid,toaid,topflag);
		if(ret.isFail()) {
			log.error("调整失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 菜单/页面的禁用-启用操作
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:17:47
	 */
	@RequestPath(value = "/disable")
	public Resp disable(HttpRequest request,Integer aid,Short status) throws Exception {
		Ret ret = authService.disable(aid,status);
		if(ret.isFail()) {
			log.error("禁用失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	
	/**
	 * 页面的操作权限列表-菜单使用
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:19:38
	 */
	@RequestPath(value = "/operlist")
	public Resp operlist(HttpRequest request,Integer aid) throws Exception {
		Ret ret = authService.pageoperlist(aid);
		if(ret.isFail()) {
			log.error("禁用失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	/**
	 * 操作新增
	 * @param request
	 * @param auth
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:26:49
	 */
	@RequestPath(value = "/operadd")
	public Resp operadd(HttpRequest request,MgAuth auth) throws Exception {
		Ret ret = authService.operadd(auth);
		if(ret.isFail()) {
			log.error("保存失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 操作修改
	 * @param request
	 * @param auth
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月28日 下午6:45:37
	 */
	@RequestPath(value = "/operupdate")
	public Resp operupdate(HttpRequest request,MgAuth auth) throws Exception {
		Ret ret = authService.operupdate(auth);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 操作删除
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:17:49
	 */
	@RequestPath(value = "/operdel")
	public Resp operdel(HttpRequest request,Integer aid) throws Exception {
		Ret ret = authService.operdel(aid);
		if(ret.isFail()) {
			log.error("删除失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 操作的禁用-启用
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:17:47
	 */
	@RequestPath(value = "/operdisable")
	public Resp operdisable(HttpRequest request,Integer aid,Short status) throws Exception {
		Ret ret = authService.operdisable(aid,status);
		if(ret.isFail()) {
			log.error("禁用失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
}
