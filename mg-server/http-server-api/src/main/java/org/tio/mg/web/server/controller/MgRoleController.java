
package org.tio.mg.web.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.mg.MgRole;
import org.tio.mg.service.service.mg.MgRoleService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 后台角色请求入口
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mgrole")
public class MgRoleController {
	private static Logger log = LoggerFactory.getLogger(MgRoleController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private MgRoleService roleService = MgRoleService.ME;

	/**
	 *
	 * @author tanyaowu
	 */
	public MgRoleController() {
	}
	
	/**
	 * 获取角色列表
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月27日 下午2:15:44
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,Short status) throws Exception {
		Ret ret = roleService.list(status);
		if(ret.isFail()) {
			log.error("获取角色失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	/**
	 * 纯角色字典列表
	 * @param request
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:03:53
	 */
	@RequestPath(value = "/dictlist")
	public Resp dictlist(HttpRequest request,Short status) throws Exception {
		Ret ret = roleService.dictlist();
		if(ret.isFail()) {
			log.error("获取角色字典失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	
	/**
	 * 角色新增
	 * @param request
	 * @param role
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 上午10:13:48
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,MgRole role) throws Exception {
		Ret ret = roleService.add(role);
		if(ret.isFail()) {
			log.error("保存失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 授权
	 * @param request
	 * @param role
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午5:11:05
	 */
	@RequestPath(value = "/grant")
	public Resp grant(HttpRequest request,Integer rid,String aids,String closeoperaids) throws Exception {
		Ret ret = roleService.grant(rid, aids,closeoperaids);
		if(ret.isFail()) {
			log.error("保存失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 角色权限树:
	 * 
	 * @param request
	 * @param rid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午5:14:48
	 */
	@RequestPath(value = "/roleAuthTree")
	public Resp roleAuthTree(HttpRequest request,Integer rid) throws Exception {
		Ret ret = roleService.roleAuthTree(rid);
		if(ret.isFail()) {
			log.error("保存失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 角色修改
	 * @param request
	 * @param role
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 上午10:13:50
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,MgRole role) throws Exception {
		Ret ret = roleService.update(role);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 角色删除-深度删除
	 * @param request
	 * @param rid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:37:05
	 */
	@RequestPath(value = "/del")
	public Resp del(HttpRequest request,Integer rid) throws Exception {
		Ret ret = roleService.del(rid);
		if(ret.isFail()) {
			log.error("删除失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
}
