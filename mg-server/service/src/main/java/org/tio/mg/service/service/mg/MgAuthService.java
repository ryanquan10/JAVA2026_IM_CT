
package org.tio.mg.service.service.mg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.mg.MgAuth;
import org.tio.mg.service.model.mg.MgRecentPath;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgAuthVo;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.service.vo.MgUserAuthInfoVo;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.Threads;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 后台权限接口
 * @author xufei
 * 2020年5月25日 下午4:46:25
 */
public class MgAuthService {
	private static Logger log = LoggerFactory.getLogger(MgAuthService.class);

	public static final MgAuthService ME = new MgAuthService();
	
	
	/**
	 * 根据菜单类型获取菜单权限树
	 * @param name
	 * @param status
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年6月15日 下午2:39:36
	 */
	public Ret authlist(String name,Short status,Short type) {
		Kv params = Kv.create();
		if(type != null) {
			params.set("type", type);
		} else {
			params.set("defaulttype", MgConst.AuthType.OPER);
		}
		if(StrUtil.isNotBlank(name)) {
			params.set("name","%" + name.trim() + "%");
		}
		if(status != null) {
			params.set("status",status);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.authlist", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		if(CollectionUtil.isEmpty(authlist)) {
			return RetUtils.okData(new ArrayList<MgAuth>());
		}
		short deep = authlist.get(0).getDeep();
		List<MgAuth> treeList = new ArrayList<MgAuth>();
		Map<String, MgAuth> treeMap = new HashMap<String, MgAuth>();
		for(MgAuth auth : authlist) {
			if(Objects.equals(deep, auth.getDeep())) {//最高级
				treeList.add(auth);
				if(Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
					treeMap.put(auth.getId() + "", auth);
				}
			} else {
				MgAuth pAuth = treeMap.get(auth.getPid() + "");
				if(pAuth == null) {
					log.error("获取菜单列表时，发现父节点不存在，pid:{},aid:{}",auth.getPid(),auth.getId());
					continue;
				} else {
					List<MgAuth> childs = pAuth.getChilds();
					if(childs == null) {
						childs = new ArrayList<MgAuth>();
						pAuth.setChilds(childs);
					}
					childs.add(auth);
				}
				if(Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
					treeMap.put(auth.getId() + "", auth);
				}
			}
		}
		return RetUtils.okData(treeList);
	}
	
	
	/**
	 * 获取纯菜单树
	 * @param name
	 * @param status
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年5月28日 下午7:16:23
	 */
	public Ret menulist() {
		Kv params = Kv.by("type", MgConst.AuthType.MENU);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.authlist", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		if(CollectionUtil.isEmpty(authlist)) {
			return RetUtils.okData(new ArrayList<MgAuth>());
		}
		short deep = 1;
		List<MgAuth> treeList = new ArrayList<MgAuth>();
		Map<String, MgAuth> treeMap = new HashMap<String, MgAuth>();
		for(MgAuth auth : authlist) {
			if(Objects.equals(deep, auth.getDeep())) {//最高级
				treeList.add(auth);
				treeMap.put(auth.getId() + "", auth);
			} else {
				MgAuth pAuth = treeMap.get(auth.getPid() + "");
				if(pAuth == null) {
					log.error("获取纯菜单列表时，发现父节点不存在，pid:{},aid:{}",auth.getPid(),auth.getId());
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
			}
		}
		return RetUtils.okData(treeList);
	}
	
	/**
	 * 获取菜单/页面下的子节点列表
	 * @param pid
	 * @return
	 * @author xufei
	 * 2020年6月4日 下午5:06:30
	 */
	public List<MgAuth> childList(Integer pid) {
		return MgAuth.dao.find("select * from mg_auth where pid = ? order by aindex" ,pid);
	}
	
	/**
	 * 删除角色的权限列表
	 * @param rid
	 * @return
	 * @author xufei
	 * 2020年6月1日 下午3:01:13
	 */
	public void delRidGrantList(Integer rid) {
		Db.use(MgConst.Db.TIO_MG).update("delete from mg_role_auth where rid = ?",rid);
	}
	
	/**
	 * 头部插入元素的排序修改
	 * @param pid
	 * @author xufei
	 * 2020年6月4日 下午5:34:06
	 */
	public void insertTopOtherUpdate(Integer pid) {
		Db.use(MgConst.Db.TIO_MG).update("update mg_auth set aindex = aindex + 1 where pid = ?",pid);
	}
	
	/**
	 * 初始化权限
	 * @param rid
	 * @param aids
	 * @param status
	 * @author xufei
	 * 2020年6月1日 下午3:10:34
	 */
	public void initRidGrant(Integer rid,String aids,Short status) {
		Kv params = Kv.by("rid", rid).set("aids", aids).set("status", status);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.roleAuthInit", params);
		int count = Db.use(MgConst.Db.TIO_MG).update(sqlPara);
		if(count <= 0) {
			log.warn("分配权限修改无变化");
		}
	}
	
	
	/**
	 * 新增菜单
	 * @param auth
	 * @return
	 * @author xufei
	 * 2020年5月28日 下午6:44:54
	 */
	public Ret add(MgAuth auth) {
		if(StrUtil.isBlank(auth.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(auth.getPid() == null) {
			log.error("新增菜单错误：上级节点为空");
			return RetUtils.failMsg("父节点为空");
		}
		if(auth.getType() == null) {
			return RetUtils.failMsg("菜单类型为空");
		}
		if(!Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
			if(StrUtil.isBlank(auth.getAuthurl()) || StrUtil.isBlank(auth.getRoutekey())) {
				return RetUtils.failMsg("非菜单类型的路由和页面路径都不能为空");
			}
			if(existRoutekeyForPage(auth.getRoutekey(),null)) {
				return RetUtils.failMsg("路由已存在");
			}
		}
		if(Objects.equals(auth.getPid(), -1)) {
			auth.setDeep((short)1);
		} else {
			MgAuth pAuth = MgAuth.dao.findById(auth.getPid());
			if(pAuth == null) {
				return RetUtils.failMsg("父节点不存在");
			}
			if(!Objects.equals(pAuth.getType(), MgConst.AuthType.MENU)) {
				return RetUtils.failMsg("父节点不是菜单");
			}
			auth.setDeep((short)(pAuth.getDeep() + 1));
		}
		boolean save = auth.save(); 
		if(!save) {
			return RetUtils.failMsg("新增失败");
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 修改菜单
	 * @param auth
	 * @return
	 * @author xufei
	 * 2020年5月28日 下午6:45:50
	 */
	public Ret update(MgAuth auth) {
		if(auth.getId() == null) {
			return RetUtils.failMsg("id为空");
		}
		if(auth.getPid() == null) {
			log.error("修改菜单错误：上级节点为空");
			return RetUtils.failMsg("父节点为空");
		}
		if(auth.getType() == null) {
			return RetUtils.failMsg("菜单类型为空");
		}
		if(!Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
			if(StrUtil.isBlank(auth.getAuthurl()) || StrUtil.isBlank(auth.getRoutekey())) {
				return RetUtils.failMsg("非菜单类型的路由和页面路径都不能为空");
			}
			if(existRoutekeyForPage(auth.getRoutekey(),auth.getId())) {
				return RetUtils.failMsg("路由已存在");
			}
		}
		MgAuth old = MgAuth.dao.findById(auth.getId());
		if(old == null) {
			return RetUtils.failMsg("菜单不存在");
		}
		if(Objects.equals(old.getPid(), auth.getPid())) {
			auth.setDeep(old.getDeep());
			boolean update = auth.update(); 
			if(!update) {
				return RetUtils.failMsg("修改失败");
			}
		} else {
			if(Objects.equals(auth.getPid(), -1)) {
				auth.setDeep((short)1);
			} else {
				MgAuth pAuth = MgAuth.dao.findById(auth.getPid());
				if(pAuth == null) {
					return RetUtils.failMsg("父节点不存在");
				}
				if(!Objects.equals(pAuth.getType(), MgConst.AuthType.MENU)) {
					return RetUtils.failMsg("父节点不是菜单");
				}
				auth.setDeep((short)(pAuth.getDeep() + 1));
				if(Objects.equals(old.getDeep(), auth.getDeep()) || !Objects.equals(old.getType(), MgConst.AuthType.MENU)) {
					boolean update = auth.update(); 
					if(!update) {
						return RetUtils.failMsg("修改失败");
					}
				} else {
					AbsTxAtom tx = new AbsTxAtom() {
						
						@Override
						public boolean noTxRun() {
							return updateChildDeep((short)(auth.getDeep() + 1), auth.getId());
						}
					};
					boolean update = Db.use(MgConst.Db.TIO_MG).tx(tx);
					if(!update) {
						return RetUtils.failMsg("修改失败");
					}
				}
			}
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 删除菜单-完全深度删除-关联数据删除
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:17:04
	 */
	public Ret del(Integer aid) {
		if(aid == null) {
			return RetUtils.invalidParam();
		}
		MgAuth auth = MgAuth.dao.findById(aid);
		if(auth == null) {
			return RetUtils.noExistParam();
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				List<MgAuth> childs = authChildTree(aid);
				if(CollectionUtil.isEmpty(childs)) {
					return failRet("空权限树");
				}
				String ids = aid + "";
				for(MgAuth mgAuth : childs) {
					MgAuth.dao.deleteById(mgAuth.getId());
					ids += "," + mgAuth.getId();
				}
				//删除关联权限
				delRoleAuths("(" + ids + ")");
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
	 * 删除角色关联的权限
	 * @param rid
	 * @author xufei
	 * 2020年6月8日 下午4:15:35
	 */
	public void delRoleAuths(String aids) {
		Db.use(MgConst.Db.TIO_MG).delete("delete from mg_role_auth where aid in " + aids);
	}
	
	
	/**
	 * 判断是否存在routekey
	 * @param routekey
	 * @return
	 * @author xufei
	 * 2020年6月29日 上午10:54:17
	 */
	public boolean existRoutekeyForPage(String routekey,Integer id) {
		if(id == null) {
			return CollectionUtil.isNotEmpty(MgAuth.dao.find("select * from mg_auth where routekey = ? and type = ?", routekey,MgConst.AuthType.PAGE));	
		} else {
			return CollectionUtil.isNotEmpty(MgAuth.dao.find("select * from mg_auth where routekey = ? and type = ? and id != ?", routekey,MgConst.AuthType.PAGE,id));
		}
	}
	
	/**
	 * 根据路由获取权限
	 * @param routekey
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午2:22:32
	 */
	public MgAuth getAuthByRoutekey(String routekey) {
		return MgAuth.dao.findFirst("select * from mg_auth where routekey = ? and type = ?", routekey,MgConst.AuthType.PAGE);
	}
	
	/**
	 * 根据url获取权限
	 * @param authurl
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午2:37:04
	 */
	public MgAuth getAuthByAuthurl(String authurl) {
		return MgAuth.dao.findFirst("select * from mg_auth where authurl = ? and type = ? and `status` = ?", authurl,MgConst.AuthType.OPER,Const.Status.NORMAL);
	}
	
	/**
	 * 权限子树
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月15日 下午2:23:41
	 */
	public List<MgAuth> authChildTree(Integer aid) {
		Kv params = Kv.by("id", aid);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.authChildTree", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		return authlist;
	}
	
	/**
	 * 获取页面的操作权限列表-Map<routekey,status>
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月11日 上午10:04:37
	 */
	public Ret pageoperlist(Integer mguid,String path) {
		if(mguid == null || StrUtil.isBlank(path)) {
			return RetUtils.failMsg(RetUtils.INVALID_PARAMETER, AppCode.GeneralCode.PARAM_ERROR);
		}
		MgAuth auth = getAuthByRoutekey(path);
		if(auth == null) {
			return RetUtils.failMsg(RetUtils.NOT_EXIST + ":" + path, AppCode.GeneralCode.PARAM_ERROR);
		}
		MgUserAuthInfoVo authInfoVo = MgUserService.ME.getMgUserAuth(mguid);
		if(authInfoVo == null) {
			return RetUtils.failMsg(RetUtils.GRANT_ERROR, AppCode.ForbidOper.NOTPERMISSION);
		}
		HashMap<String, MgAuthVo> page = authInfoVo.getPageAuth();
		if(MapUtil.isEmpty(page) || page.get(auth.getAuthurl()) == null) {
			return RetUtils.failMsg(RetUtils.GRANT_ERROR, AppCode.ForbidOper.NOTPERMISSION);
		}
		Kv params = Kv.by("type", MgConst.AuthType.OPER).set("mguid",mguid).set("routekey",path).set("status",Const.Status.NORMAL);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.pageoperlist", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		Map<String, Short> retMap = new HashMap<String, Short>();
		if(CollectionUtil.isNotEmpty(authlist)) {
			for(MgAuth mgAuth : authlist) {
				retMap.put(mgAuth.getRoutekey(), mgAuth.getStatus());
			}
		}
		//补充操作禁用权限列表
		List<MgAuth> operList = MgAuthService.ME.operlist(path);
		if(CollectionUtil.isNotEmpty(operList)) {
			for(MgAuth oper : operList) {
				if(retMap.get(oper.getRoutekey()) == null) {
					retMap.put(oper.getRoutekey(), Const.Status.DISABLED);
				}
			}
		}
		//保存更新最近打开记录
		Threads.getGroupExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					MgRecentPath recentPath = MgRecentPath.dao.findFirst("select * from mg_recent_path where aid = ? and mguid = ?",auth.getId(),mguid);
					if(recentPath == null) {
						recentPath = new MgRecentPath();
						recentPath.setMguid(mguid);
						recentPath.setAid(auth.getId());
						recentPath.setOpercount(1);
						recentPath.setRoutkey(path);
						boolean save = recentPath.save();
						if(save) {
							return;
						}
					}
					Db.use(MgConst.Db.TIO_MG).update("update mg_recent_path set opercount = opercount + 1 where aid = ? and mguid = ?",auth.getId(),mguid);
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
			}
		});
		return RetUtils.okData(retMap);
	}
	
	/**
	 * 重排序
	 * @param aid
	 * @param toaid，排在插入位置前面的兄弟节点，如果topflag为1，则为父节点
	 * @param topflag：是否时置顶操作，1：是；2：否
	 * @return
	 * @author xufei
	 * 2020年6月3日 下午1:55:07
	 */
	public Ret index(Integer aid,Integer toaid,Short topflag) {
		if(Objects.equals(aid, toaid)) {
			return RetUtils.invalidParam();
		}
		MgAuth old = MgAuth.dao.findById(aid);
		if(old == null) {
			return RetUtils.failMsg("菜单存在");
		}
		Integer pid = toaid;
		MgAuth brother = null;
		if(!Objects.equals(topflag, Const.YesOrNo.YES)) {
			brother = MgAuth.dao.findById(toaid);
			if(brother == null) {
				return RetUtils.failMsg("兄弟节点不存在");
			}
			pid = brother.getPid();
		} 
		MgAuth parent = null;
		if(!Objects.equals(pid, -1)) {
			parent = MgAuth.dao.findById(pid);
			if(parent == null || !Objects.equals(parent.getType(), MgConst.AuthType.MENU)) {
				return RetUtils.failMsg("无效父节点");
			}
		}
		List<MgAuth> all = childList(pid);
		List<MgAuth> oldChild = childList(aid);
		if(CollectionUtil.isEmpty(all)) {
			final MgAuth finalparent = parent;
			final Integer fPid = pid;
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					MgAuth newAuth = new MgAuth();
					newAuth.setId(old.getId());
					newAuth.setPid(fPid);
					newAuth.setAindex((short)1);
					if(Objects.equals(toaid, -1)) {
						newAuth.setDeep((short)1);
					} else {
						newAuth.setDeep((short)(finalparent.getDeep() + 1));
					}
					boolean update = newAuth.update();
					if(!update) {
						return failRet("调整失败");
					}
					if(CollectionUtil.isNotEmpty(oldChild)) {
						Db.use(MgConst.Db.TIO_MG).update("update mg_auth set deep = ? where pid = ?",(short)(newAuth.getDeep() + 1),old.getId());
					}
					return true;
				}
			};
			boolean tx = Db.use(MgConst.Db.TIO_MG).tx(atom);
			if(!tx) {
				return atom.getRetObj();
			}
		} else {
			final MgAuth finalBrother = brother;
			final MgAuth finalParent = parent;
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					short index = 1;
					short newDeep = 1;
					if(finalBrother != null) {
						index = finalBrother.getAindex();
						index++;
						MgAuth newAuth = new MgAuth();
						newAuth.setId(old.getId());
						newAuth.setPid(finalBrother.getPid());
						newAuth.setDeep(finalBrother.getDeep());
						newAuth.setAindex(index);
						boolean newUpdate = newAuth.update();
						if(!newUpdate) {
							return failRet("调整失败");
						}
						newDeep = newAuth.getDeep();
						boolean insert = false;
						for(MgAuth auth : all) {
							if(!insert) {//未定位到
								if(Objects.equals(auth.getId(), finalBrother.getId())) {
									insert = true;
								} 
							} else {//已定位，后续的后移
								if(Objects.equals(old.getId(), auth.getId())) {
									continue;
								}
								index ++;
								MgAuth inAuth = new MgAuth();
								inAuth.setId(auth.getId());
								inAuth.setAindex(index);
								boolean update = inAuth.update();
								if(!update) {
									return failRet("调整失败");
								}
							}
						}
					} else {
						MgAuth newAuth = new MgAuth();
						newAuth.setId(old.getId());
						newAuth.setPid(toaid);
						newAuth.setAindex(index);
						if(Objects.equals(toaid, -1)) {
							newAuth.setDeep((short)1);
						} else {
							newAuth.setDeep((short)(finalParent.getDeep() + 1));
						}
						boolean newUpdate = newAuth.update();
						if(!newUpdate) {
							return failRet("调整失败");
						}
						newDeep = newAuth.getDeep();
						insertTopOtherUpdate(toaid);
					}
					if(CollectionUtil.isNotEmpty(oldChild)) {
						newDeep++;
						Db.use(MgConst.Db.TIO_MG).update("update mg_auth set deep = ? where pid = ?",newDeep,old.getId());
					}
					return true;
				}
			};
			boolean tx = Db.use(MgConst.Db.TIO_MG).tx(atom);
			if(!tx) {
				return atom.getRetObj();
			}
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	/**
	 * 禁用-启用
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:17:11
	 */
	public Ret disable(Integer aid,Short status) {
		if(aid == null || status == null) {
			return RetUtils.invalidParam();
		}
		MgAuth auth = MgAuth.dao.findById(aid);
		if(auth == null) {
			return RetUtils.noExistParam();
		}
		
		//不需要进行深层次的禁用和启用
		if(Objects.equals(auth.getStatus(), status)) {
			return RetUtils.okOper();
		}
		MgAuth update = new MgAuth();
		update.setId(aid);
		update.setStatus(status);
		boolean commit = update.update();
		if(!commit) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 修改子节点的深度-迭代
	 * @param deep
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年5月28日 下午7:07:02
	 */
	public boolean updateChildDeep(Short deep,Integer id) {
		List<MgAuth> mgAuths = MgAuth.dao.find("select id from mg_auth where pid = ?",id);
		if(CollectionUtil.isEmpty(mgAuths)) {
			return true;
		}
		short nextDeep = (short)(deep + 1);
		for(MgAuth auth : mgAuths) {
			MgAuth updateAuth = new MgAuth();
			updateAuth.setDeep(deep);
			updateAuth.setId(auth.getId());
			if(!updateAuth.update()) {
				return false;
			}
			if(Objects.equals(auth.getType(), MgConst.AuthType.MENU)) {
				boolean loopUpdate = updateChildDeep(nextDeep, auth.getId());
				if(!loopUpdate) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 页面的操作权限列表
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:19:20
	 */
	public Ret pageoperlist(Integer aid) {
		if(aid == null) {
			return RetUtils.invalidParam();
		}
		MgAuth pAuth = MgAuth.dao.findById(aid);
		if(pAuth == null) {
			return RetUtils.noExistParam();
		}
		if(!Objects.equals(pAuth.getType(),MgConst.AuthType.PAGE)) {
			return RetUtils.failMsg("父节点不是页面");
		}
		List<MgAuth> mgAuths = MgAuth.dao.find("select *  from mg_auth where pid = ?",aid);
		return RetUtils.okList(mgAuths);
	}
	
	/**
	 * 所有的操作权限列表
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月15日 下午3:15:58
	 */
	public List<MgAuth> operlist(String key) {
		Kv params = Kv.by("type", MgConst.AuthType.OPER).set("status",Const.Status.NORMAL);
		if(StrUtil.isNotBlank(key)) {
			params.set("key",key);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.authOperList", params);
		List<MgAuth> authlist = MgAuth.dao.find(sqlPara);
		return authlist;
	}
	
	/**
	 * 操作权限删除操作
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:17:04
	 */
	public Ret operdel(Integer aid) {
		if(aid == null) {
			return RetUtils.invalidParam();
		}
		MgAuth auth = MgAuth.dao.findById(aid);
		if(auth == null) {
			return RetUtils.noExistParam();
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				MgAuth.dao.deleteById(aid);
				//删除关联权限
				delRoleAuths("(" + aid + ")");
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
	 * 操作权限的禁用-启用操作
	 * @param aid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:17:11
	 */
	public Ret operdisable(Integer aid,Short status) {
		if(aid == null) {
			return RetUtils.failMsg("id为空");
		}
		MgAuth old = MgAuth.dao.findById(aid);
		if(old == null) {
			return RetUtils.failMsg("操作不存在");
		}
		MgAuth auth = new MgAuth();
		auth.setId(aid);
		auth.setStatus(status);
		boolean update = auth.update(); 
		if(!update) {
			return RetUtils.failMsg("修改失败");
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 操作权限的新增操作
	 * @param auth
	 * @return
	 * @author xufei
	 * 2020年6月12日 下午4:02:31
	 */
	public Ret operadd(MgAuth auth) {
		if(StrUtil.isBlank(auth.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(auth.getPid() == null) {
			log.error("新增操作错误：上级节点为空");
			return RetUtils.failMsg("父节点为空");
		}
		MgAuth pAuth = MgAuth.dao.findById(auth.getPid());
		if(pAuth == null) {
			return RetUtils.noExistParam();
		}
		if(!Objects.equals(pAuth.getType(),MgConst.AuthType.PAGE)) {
			return RetUtils.failMsg("父节点不是页面");
		}
		auth.setType(MgConst.AuthType.OPER);
		auth.setDeep((short)(pAuth.getDeep() + 1));
		boolean save = auth.save(); 
		if(!save) {
			return RetUtils.failMsg("新增失败");
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
	
	
	/**
	 * 操作权限的修改操作
	 * @param auth
	 * @return
	 * @author xufei
	 * 2020年5月28日 下午6:45:50
	 */
	public Ret operupdate(MgAuth auth) {
		if(auth.getId() == null) {
			return RetUtils.failMsg("id为空");
		}
		MgAuth old = MgAuth.dao.findById(auth.getId());
		if(old == null) {
			return RetUtils.failMsg("操作不存在");
		}
		if(auth.getPid() != null && !Objects.equals(auth.getPid(), old.getPid()) ) {
			return RetUtils.failMsg("不能修改父节点");
		}
		boolean update = auth.update(); 
		if(!update) {
			return RetUtils.failMsg("修改失败");
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).clear();
		return RetUtils.okOper();
	}
}
