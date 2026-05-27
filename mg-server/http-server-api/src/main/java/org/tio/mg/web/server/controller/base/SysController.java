
package org.tio.mg.web.server.controller.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.PageSqlKit;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.mg.MgAuth;
import org.tio.mg.service.model.mg.MgRole;
import org.tio.mg.service.model.mg.MgRoleAuth;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.utils.OkHttpUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.service.vo.SysnMenuVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import okhttp3.Response;

/**
 * 系统请求
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/sys")
public class SysController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SysController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:17:46
	 */
	public static void main(String[] args) {

	}

	/**
	 * 
	 */
	public SysController() {
	}
	
	
	/**
	 * 页面系统参数
	 * @param request
	 * @param aid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:17:47
	 */
	@RequestPath(value = "/params")
	public Resp operdisable(HttpRequest request) throws Exception {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("res.server",Const.RES_SERVER);
		param.put("site",Const.SITE);
		param.put("imsite",Const.IM_SERVER);
		param.put("resetpwd","888888");
		return Resp.ok(param);
	}
	
	/**
	 * 数据库查询
	 * @param request
	 * @param sql
	 * @param db
	 * @param pageNumber
	 * @param pageSize
	 * @param groupby
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午2:07:26
	 */
	@RequestPath(value = "/query")
	public Resp query(HttpRequest request,String sql,String db,Integer pageNumber, Integer pageSize,Short groupby) throws Exception {
		if(StrUtil.isBlank(sql) || sql.trim().toLowerCase().indexOf("select") != 0) {
			return Resp.fail("无效查询");
		}
		sql = sql.trim().replaceAll(";", "");
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 100;
		}
		if(StrUtil.isBlank(db)) {
			db = MgConst.Db.TIO_SITE_MAIN;
		}
		String[] sqls = PageSqlKit.parsePageSql(sql);
		Page<Record> rePage = null;
		Boolean isGroupBySql = false;
		if(groupby != null && Objects.equals(groupby, Const.YesOrNo.YES)) {
			isGroupBySql = true;
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			rePage = Db.use(db).paginate(pageNumber, pageSize, isGroupBySql, sqls[0], sqls[1]);
			if(rePage != null && CollectionUtil.isNotEmpty(rePage.getList())) {
				List<String> couList = new ArrayList<String>();
				Map<String, Object>  rsMap = rePage.getList().get(0).getColumns();
				for(String columns : rsMap.keySet()) {
					couList.add(columns);
				}
				Collections.sort(couList);
				ret.put("columns", couList);
			}
			ret.put("page", rePage);
		} catch (Exception e) {
			return Resp.fail("查询失败");
		}
		return Resp.ok(ret);
	}
	
	/**
	 * 同步菜单列表
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年8月3日 上午10:39:35
	 */
	@RequestPath(value = "/synmenu")
	public Resp synmenu(HttpRequest request) throws Exception {
		List<MgAuth> mgAuths = MgAuth.dao.find("select * from mg_auth order by id");
		return Resp.ok(mgAuths);
	}
	
	
	/**
	 * 初始化菜单
	 * @param request
	 * @param site
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年8月3日 上午10:46:54
	 */
	@RequestPath(value = "/initmenu")
	public Resp initmenu(HttpRequest request,Integer rid) throws Exception {
		if(rid == null) {
			return Resp.fail("角色id为空");
		}
		MgRole role = MgRole.dao.findById(rid);
		if(role == null) {
			return Resp.fail("角色不存在");
		}
		String site = MgConfService.getString(MgConst.ConfMapping.MG_MENU_SYSN_SITE, "http://129.211.52.247:9292");
		Response resp = OkHttpUtils.get(site + Const.API_CONTEXTPATH + "/sys/synmenu" + Const.API_SUFFIX);
		if(resp == null) {
			return Resp.fail("同步失败,获取响应为空");
		}
		if(resp.isSuccessful()) {
			if(resp.code() != 200) {
				return Resp.fail("同步失败,状态码：" + resp.code());
			}
			String body = resp.body().string();
			SysnMenuVo sysnMenuVo = Json.toBean(body, SysnMenuVo.class);
			if(sysnMenuVo.isOk()) {
				Db.use(MgConst.Db.TIO_MG).update("create table mg_auth_temp select * from mg_auth");
				Db.use(MgConst.Db.TIO_MG).update("truncate table mg_auth");
				Db.use(MgConst.Db.TIO_MG).update("create table mg_role_auth_temp select * from mg_role_auth");
				Db.use(MgConst.Db.TIO_MG).update("truncate table mg_role_auth");
				AbsTxAtom atom = new AbsTxAtom() {
					
					@Override
					public boolean noTxRun() {
						ArrayList<MgAuth> mgAuths = sysnMenuVo.getData();
						boolean init = true;
						if(CollectionUtil.isNotEmpty(mgAuths)) {
							for(MgAuth mgAuth : mgAuths) {
								boolean save = mgAuth.save();
								if(!save) {
									init = false;
									break;
								}
								MgRoleAuth roleAuth = new MgRoleAuth();
								roleAuth.setRid(rid);
								roleAuth.setAid(mgAuth.getId());
								boolean rsave = roleAuth.save();
								if(!rsave) {
									init = false;
									break;
								}
							}
						}
						return init;
					}
				};
				boolean tx = Db.use(MgConst.Db.TIO_MG).tx(atom);
				if(!tx) {
					Db.use(MgConst.Db.TIO_MG).update("truncate table mg_auth");
					Db.use(MgConst.Db.TIO_MG).update("truncate table mg_role_auth");
					Db.use(MgConst.Db.TIO_MG).update("insert into mg_auth select * from mg_auth_temp");
					Db.use(MgConst.Db.TIO_MG).update("insert into mg_role_auth select * from mg_role_auth_temp");
				}
				Db.use(MgConst.Db.TIO_MG).update("drop table mg_auth_temp");
				Db.use(MgConst.Db.TIO_MG).update("drop table mg_role_auth_temp");
				Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
				if(!tx) {
					return Resp.ok("同步失败,数据已回滚");
				}
			}
			return Resp.ok(sysnMenuVo);
		} else {
			return Resp.fail("同步失败,响应失败");
		}
		
	}
}
