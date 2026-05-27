
package org.tio.mg.service.service.mg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.mg.MgAuth;
import org.tio.mg.service.model.mg.MgRole;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgConst;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 后台用户接口
 * @author xufei
 * 2020年5月25日 下午4:46:25
 */
public class MgRoleService {
	private static Logger log = LoggerFactory.getLogger(MgRoleService.class);

	public static final MgRoleService ME = new MgRoleService();
	
	/**
	 * 查询的角色列表
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年5月29日 上午10:09:53
	 */
	public Ret list(Short status) {
		Kv params = Kv.by("status", Const.Status.NORMAL);
		if(status != null) {
			params.set("rolestatus",status);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.rolelist", params);
		List<Record> list = Db.use(MgConst.Db.TIO_MG).find(sqlPara);
		return RetUtils.okList(list);
	}
	
	/**
	 * 用户的角色信息
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年6月10日 下午5:59:58
	 */
	public Record userRoles(Integer mguid) {
		Kv params = Kv.by("mguid", mguid);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("mguser.userRoles", params);
		Record record = Db.use(MgConst.Db.TIO_MG).findFirst(sqlPara);
		return record;
	}
	
	/**
	 * 角色字典列表-下拉列表使用
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:02:56
	 */
	public Ret dictlist() {
		Kv params = Kv.by("status", Const.Status.NORMAL);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.roledict", params);
		List<Record> list = Db.use(MgConst.Db.TIO_MG).find(sqlPara);
		return RetUtils.okList(list);
	}
	
	/**
	 * 新增角色
	 * @param role
	 * @return
	 * @author xufei
	 * 2020年5月29日 上午10:18:31
	 */
	public Ret add(MgRole role) {
		if(role == null || StrUtil.isBlank(role.getName())) {
			log.error("新增角色，参数为空");
			return RetUtils.invalidParam();
		}
		boolean save = role.save();
		if(!save) {
			return RetUtils.failMsg("新增失败");
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 授权
	 * @param rid
	 * @param aids
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午5:13:19
	 */
	public Ret grant(Integer rid,String aids,String closeOperaids) {
		if(rid == null) {
			return RetUtils.invalidParam();
		}
		if(StrUtil.isBlank(aids) && StrUtil.isBlank(closeOperaids)) {
			return RetUtils.failMsg("请选择权限列表");
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				MgAuthService.ME.delRidGrantList(rid);
				if(StrUtil.isNotBlank(aids)) {
					MgAuthService.ME.initRidGrant(rid, "(" + aids + ")",Const.Status.NORMAL);
				}
//				if(StrUtil.isNotBlank(closeOperaids)) {
//					MgAuthService.ME.initRidGrant(rid, "(" + closeOperaids + ")",Const.Status.DISABLED);
//				}
				return true;
			}
		};
		boolean grant = Db.use(MgConst.Db.TIO_MG).tx(atom);
		if(!grant) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	/**
	 * 角色权限列表
	 * @param rid
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午5:13:50
	 */
	public Ret roleAuthTree(Integer rid) {
		Kv params = Kv.by("status",Const.Status.NORMAL).set("rid",rid);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.roleAuthlist", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		if(CollectionUtil.isEmpty(authlist)) {
			return RetUtils.okData(new ArrayList<MgAuth>());
		}
		short deep = authlist.get(0).getDeep();
		List<MgAuth> treeList = new ArrayList<MgAuth>();
		Map<String, MgAuth> treeMap = new HashedMap<String, MgAuth>();
		Map<String, MgAuth> menuMap = new HashMap<String, MgAuth>();
		for(MgAuth auth : authlist) {
			if(Objects.equals(deep, auth.getDeep())) {//最高级
				treeList.add(auth);
				treeMap.put(auth.getId() + "", auth);
				if(Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
					menuMap.put(auth.getId() + "", auth);
				}
			} else {
				MgAuth pAuth = treeMap.get(auth.getPid() + "");
				if(pAuth == null) {
					log.warn("获取权限树警告:没有上级菜单或页面：id：{}，pid:{},name:{}",auth.getId(),auth.getPid(),auth.getName());
					continue;
				} else {
					List<MgAuth> childs = pAuth.getChilds();
					if(childs == null) {
						childs = new ArrayList<MgAuth>();
						pAuth.setChilds(childs);
					}
					childs.add(auth);
				}
				treeMap.put(auth.getId() + "", auth);
				if(Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
					menuMap.put(auth.getId() + "", auth);
				} else if(Objects.equals(auth.getType(), MgConst.AuthType.OPER)) {
					continue;
				}
				MgAuth menuAuth = menuMap.get(auth.getPid() + "");
				if(menuAuth != null) {
					menuAuth.setInchildids(menuAuth.getInchildids() + "," + auth.getId());
					while (menuAuth != null) {
						menuAuth.setChildids(menuAuth.getChildids() + "," + auth.getId());
						menuAuth = menuMap.get(menuAuth.getPid() + "");
					}
				}
			}
		}
		return RetUtils.okData(treeList);
	}
	
	
	/**
	 * 角色修改
	 * @param role
	 * @return
	 * @author xufei
	 * 2020年5月29日 上午10:18:33
	 */
	public Ret update(MgRole role) {
		if(role == null || role.getId() == null ||StrUtil.isBlank(role.getName())) {
			return RetUtils.invalidParam();
		}
		boolean update = role.update();
		if(!update) {
			return RetUtils.failMsg("修改失败");
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 角色删除-深度删除
	 * @param rid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:37:27
	 */
	public Ret del(Integer rid) {
		MgRole role = MgRole.dao.findById(rid);
		if(role == null) {
			return RetUtils.failMsg("角色不存在");
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				//删除关联用户
				delRoleUser(rid);
				//删除关联权限
				delRoleAuth(rid);
				//删除角色
				boolean del = MgRole.dao.deleteById(rid);
				if(!del) {
					return false;
				}
				return true;
			}
		};
		boolean commit = Db.use(MgConst.Db.TIO_MG).tx(atom);
		if(!commit) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 删除角色的权限列表
	 * @param rid
	 * @author xufei
	 * 2020年6月8日 下午4:15:35
	 */
	public void delRoleAuth(Integer rid) {
		Db.use(MgConst.Db.TIO_MG).delete("delete from mg_role_auth where rid = ?",rid);
	}
	
	/**
	 * 删除角色的用户
	 * @param rid
	 * @author xufei
	 * 2020年6月8日 下午4:15:36
	 */
	public void delRoleUser(Integer rid) {
		Db.use(MgConst.Db.TIO_MG).delete("delete from mg_user_role where rid = ?",rid);
	}
	
}
