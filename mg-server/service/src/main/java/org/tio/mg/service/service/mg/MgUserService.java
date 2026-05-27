
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
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.mg.MgAuth;
import org.tio.mg.service.model.mg.MgRole;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.model.mg.MgUserBase;
import org.tio.mg.service.model.mg.MgUserRole;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgAuthVo;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.service.vo.MgMenuVo;
import org.tio.mg.service.vo.MgUserAuthInfoVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

/**
 * 后台用户接口
 * @author xufei
 * 2020年5月25日 下午4:46:25
 */
public class MgUserService {
	private static Logger log = LoggerFactory.getLogger(MgUserService.class);

	public static final MgUserService ME = new MgUserService();


	/**
	 * 登录
	 * @param loginname
	 * @param pd5
	 * @return
	 * @author xufei
	 * 2020年5月26日 下午3:09:03
	 */
	public Ret login(String loginname, String pd5, IpInfo ipInfo) {
		String code = "code";
		if (StrUtil.isBlank(loginname)) {
			log.error("无效参数");
			return RetUtils.invalidParam().set(code, 1);
		}
		MgUser user = getByLoginname(loginname, null);
		if (user == null) {
			log.info("can find user by loginname:【{}】", loginname);
			return Ret.fail(code, 1); 
		}
		if (user.getBindip() != null && !user.getBindip().equals(ipInfo.getIp())) {
			log.info("user ip error:【{}】", ipInfo.getIp());
			return Ret.fail(code, 5);
		}
		if (!Objects.equals(pd5, user.getPwd())) {
			log.info("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getPwd());
			return Ret.fail(code, 2); //密码不正确 
		}
		return RetUtils.okData(user);
	}
	
	/**
	 * 获取后台用户
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年5月26日 下午3:35:36
	 */
	public MgUser getById(Integer id) {
		if (id == null) {
			return null;
		}
		MgUser user = MgUser.dao.findById(id);
		if (user == null || !Objects.equals(user.getStatus(), Const.Status.NORMAL)) {
			return null;
		}
		Record role = MgRoleService.ME.userRoles(id);
		if(role != null) {
			user.setRids(role.getStr("rids"));
			user.setRolename(role.getStr("rolename"));
		}
		return user;
	}
	
	/**
	 * 通过登录名获取后台用户
	 * @param loginname
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年5月26日 下午3:08:38
	 */
	public MgUser getByLoginname(String loginname, Short status) {
		MgUser user = MgUser.dao.findFirst("select * from mg_user where loginname = ? and `status` <> ?", loginname, Const.Status.DELETE);
		return user;
	}
	
	
	
	/**
	 * 获取用户权限信息
	 * 1、用户的菜单/页面权限
	 * 2、用户所有的权限map
	 * 3、用户的操作权限map
	 * 4、用户的纯菜单权限map
	 * 5、用户的纯页面权限map
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年5月27日 下午2:11:49
	 */
	public MgUserAuthInfoVo getMgUserAuth(Integer mguid) {
		String rootPath = MgConfService.getString(MgConst.ConfMapping.MG_VUE_ROOT_PATH, "/");
		String rootComponent = MgConfService.getString(MgConst.ConfMapping.MG_VUE_ROOT_COMPONENT, "SystemPub");
		ICache cache = Caches.getCache(CacheConfig.MG_USER_MENU_AUTH);
		String key = mguid + "_";
		MgUserAuthInfoVo authInfoVo = CacheUtils.get(cache, key, false, new FirsthandCreater<MgUserAuthInfoVo>() {
			@Override
			public MgUserAuthInfoVo create() {
				Kv params = Kv.by("mguid", mguid).set("status",Const.Status.NORMAL);
				SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("mguser.mguserauth", params);
				List<MgAuth> auth = MgAuth.dao.find(sqlPara);
				if(CollectionUtil.isEmpty(auth)) {
					return null;
				}
				MgUserAuthInfoVo authInfoVo = new MgUserAuthInfoVo(mguid);
				List<MgMenuVo> userMenuList = authInfoVo.getMenuList();
				Map<String, MgMenuVo> menuMap = new HashMap<String, MgMenuVo>();//菜单/页面权限map
				Map<String, MgAuthVo> userallAuth = authInfoVo.getAllAuth();//用户所有的权限map
				Map<String, MgAuthVo> useroperAuth = authInfoVo.getOperAuth();//用户操作权限map
				Map<String, MgAuthVo> usermenuAuth = authInfoVo.getMenuAuth();//用户的菜单权限map
				Map<String, MgAuthVo> userpageAuth = authInfoVo.getPageAuth();//用户的页面权限map
				short topDeep = 1;
				for(MgAuth record : auth) {
					String routekey = record.getRoutekey();//页面路由
					String authurl = record.getAuthurl();//html路径
					MgAuthVo mgAuthVo = new MgAuthVo();//权限Vo
					mgAuthVo.setName(record.getName());
					mgAuthVo.setType(record.getType());
					mgAuthVo.setComponent(authurl);
					mgAuthVo.setIcon(record.getIcon());
					mgAuthVo.setPath(routekey);
					if(Objects.equals(record.getDeep(), topDeep)) {//第一层
						if(Objects.equals(record.getType(), MgConst.AuthType.PAGE)){//第一层页面
							MgMenuVo virMenu = new MgMenuVo();//虚拟菜单
							virMenu.setDeep(record.getDeep());
							virMenu.setName("");
							virMenu.setType(record.getType());
							virMenu.setVirtualmenuflag(Const.YesOrNo.YES);
							virMenu.setComponent(rootComponent);
							virMenu.setPath(rootPath);
							userMenuList.add(virMenu);
							List<MgMenuVo> virChilds = new ArrayList<MgMenuVo>();
							virMenu.setChilds(virChilds);
							MgMenuVo menuVo = new MgMenuVo();
							menuVo.setId(record.getId());
							menuVo.setDeep((short)(record.getDeep() + 1));
							menuVo.setName(record.getName());
							menuVo.setType(record.getType());
							menuVo.setComponent(authurl);
							menuVo.setIcon(record.getIcon());
							menuVo.setPath(routekey);
							menuVo.setLevelname(record.getName());
							virChilds.add(menuVo);
							menuMap.put(record.getId() + "", menuVo);
						} else {//第一层是菜单
							MgMenuVo menuVo = new MgMenuVo();
							menuVo.setId(record.getId());
							menuVo.setDeep(record.getDeep());
							menuVo.setName(record.getName());
							menuVo.setIcon(record.getIcon());
							menuVo.setType(record.getType());
							menuVo.setComponent(rootComponent);
							menuVo.setLevelname(record.getName());
							menuVo.setPath(rootPath);
							userMenuList.add(menuVo);
							menuMap.put(record.getId() + "", menuVo);
						}
					} else {//第二层以下
						MgMenuVo pMenuVo = menuMap.get(record.getPid() + "");
						if(pMenuVo == null) {
							log.error("菜单配置异常：出现父节点不存在的配置，已自动忽略：id:{},pid:{},mguid:{}",record.getId(),record.getPid(),mguid);
							continue;
						}
						if(Objects.equals(record.getType(), MgConst.AuthType.OPER)) {
							useroperAuth.put(authurl,mgAuthVo);
							//菜单或者页面操作
							Map<String, MgAuthVo> menuOperAuth = pMenuVo.getOperAuth();
							if(menuOperAuth == null) {
								menuOperAuth = new HashMap<String, MgAuthVo>();
								pMenuVo.setOperAuth(menuOperAuth);
							}
							menuOperAuth.put(authurl, mgAuthVo);
							userallAuth.put(authurl,mgAuthVo);
						} else {
							List<MgMenuVo> childs = pMenuVo.getChilds();
							if(childs == null) {
								childs = new ArrayList<MgMenuVo>();
								pMenuVo.setChilds(childs);
							}
							MgMenuVo menuVo = new MgMenuVo();
							menuVo.setId(record.getId());
							menuVo.setDeep(record.getDeep());
							menuVo.setName(record.getName());
							menuVo.setIcon(record.getIcon());
							menuVo.setComponent(authurl);
							menuVo.setPath(routekey);
							menuVo.setLevelname(pMenuVo.getLevelname() + "," + record.getName());
							menuVo.setType(record.getType());
							childs.add(menuVo);
							menuMap.put(record.getId() + "", menuVo);
						}
					}
					if(Objects.equals(record.getType(), MgConst.AuthType.MENU)) {
						usermenuAuth.put(record.getId() + "",mgAuthVo);
					} else if(Objects.equals(record.getType(), MgConst.AuthType.PAGE)){
						userallAuth.put(authurl,mgAuthVo);
						userpageAuth.put(authurl,mgAuthVo);
					}
				}
				//补充操作禁用权限列表
				List<MgAuth> operList = MgAuthService.ME.operlist("");
				if(CollectionUtil.isNotEmpty(operList)) {
					for(MgAuth oper : operList) {
						MgMenuVo pMenuVo = menuMap.get(oper.getPid() + "");
						if(pMenuVo == null) {
							//log.error("菜单配置异常：出现页面节点不存在的配置，已自动忽略：id:{},pid:{},mguid:{}",oper.getId(),oper.getPid(),mguid);
							continue;
						}
						String routekey = oper.getRoutekey();//操作key
						String authurl = oper.getAuthurl();//axjx操作请求
						if(useroperAuth.get(authurl) != null) {
							continue;
						}
						MgAuthVo mgAuthVo = new MgAuthVo();//权限Vo
						mgAuthVo.setName(oper.getName());
						mgAuthVo.setType(oper.getType());
						mgAuthVo.setComponent(authurl);
						mgAuthVo.setIcon(oper.getIcon());
						mgAuthVo.setPath(routekey);
						mgAuthVo.setOperstatus(Const.Status.DISABLED);
						useroperAuth.put(authurl,mgAuthVo);
						//菜单或者页面操作
						Map<String, MgAuthVo> menuOperAuth = pMenuVo.getOperAuth();
						if(menuOperAuth == null) {
							menuOperAuth = new HashMap<String, MgAuthVo>();
							pMenuVo.setOperAuth(menuOperAuth);
						}
						menuOperAuth.put(authurl, mgAuthVo);
						userallAuth.put(authurl,mgAuthVo);
					}
				}
				return authInfoVo;
			}
		});
		return authInfoVo;
	}
	
	
	/**
	 * 获取菜单列表
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年5月27日 下午2:11:49
	 */
	public Ret getMenu(Integer mguid) {
		MgUserAuthInfoVo authInfoVo = getMgUserAuth(mguid);
		if(authInfoVo == null || CollectionUtil.isEmpty(authInfoVo.getMenuList())) {
			return RetUtils.failMsg("没有分配权限");
		}
		return RetUtils.okData(authInfoVo.getMenuList());
	}
	
	/**
	 * 用户列表
	 * @param searchKey
	 * @param rid
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年6月1日 下午4:07:29
	 */
	public Ret userList(String searchKey,Integer rid,Short status,Integer pageNumber,Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(status != null) {
			params.set("status", status);
		} else {
			params.set("defaultstatus", Const.Status.DELETE);
		}
		if(rid != null) {
			params.set("rid",rid);
		}
		if(StrUtil.isNotBlank(searchKey)) {
			params.set("searchkey", searchKey);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("mguser.mguserlist", params);
		Page<Record> userPage = Db.use(MgConst.Db.TIO_MG).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(userPage);
	}
	
	/**
	 * 重置密码
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午3:14:48
	 */
	public Ret resetPwd(Integer mguid) {
		if(mguid == null) {
			return RetUtils.invalidParam();
		}
		MgUser user = MgUser.dao.findById(mguid);
		if(user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		String resetPwd = getMd5Pwd(user.getLoginname(),MgConst.MG_USER_DEFAULT_PWD);
		MgUser update = new MgUser();
		update.setId(user.getId());
		update.setPwd(resetPwd);
		boolean ret = update.update();
		if(!ret) {
			return RetUtils.failOper();
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 修改密码
	 * @param mguid
	 * @param pwd
	 * @param newpwd
	 * @return
	 * @author xufei
	 * 2020年6月8日 下午3:47:49
	 */
	public Ret updatePwd(Integer mguid,String pwd,String newpwd) {
		MgUser user = MgUser.dao.findById(mguid);
		if(user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if(!user.getPwd().equals(pwd)) {
			return RetUtils.failMsg("原密码不正确");
		}
		MgUser update = new MgUser();
		update.setId(user.getId());
		update.setPwd(newpwd);
		boolean ret = update.update();
		if(!ret) {
			return RetUtils.failOper();
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 保存
	 * @param user
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午3:22:13
	 */
	public Ret add(MgUser user) {
		if(user == null) {
			return RetUtils.invalidParam();
		}
		if(StrUtil.isBlank(user.getRids())) {
			return RetUtils.failMsg("角色不能为空");
		}
		AbsTxAtom add = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				boolean init = user.save();
				if(!init) {
					return failRet("保存失败");
				}
				MgUserBase base = new MgUserBase();
				base.setMguid(user.getId());
				base.setRealname(user.getRealname());
				base.setPhone(user.getPhone());
				base.setDeptname(user.getDeptname());
				base.setPosition(user.getPosition());
				boolean baseInit = base.save();
				if(!baseInit) {
					return failRet("附属信息保存异常");
				}
				String[] ridStrs = user.getRids().split(",");
				for(String ridStr : ridStrs) {
					Integer rid = Integer.parseInt(ridStr);
					MgRole role = MgRole.dao.findById(rid);
					if(role == null) {
						log.error("创建用户时，选择的角色不存在");
						continue;
					}
					MgUserRole userRole = new MgUserRole();
					userRole.setMguid(user.getId());
					userRole.setRid(rid);
					userRole.replaceSave();
				}
				return true;
			}
		};
		boolean commit = Db.use(MgConst.Db.TIO_MG).tx(add);
		if(!commit) {
			return add.getRetObj();
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午3:22:15
	 */
	public Ret update(MgUser user) {
		if(user.getId() == null) {
			return RetUtils.invalidParam();
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				boolean update = user.update();
				if(!update) {
					return failRet("修改失败");
				}
				MgUserBase userBase = getBase(user.getId());
				MgUserBase baseUpdate = new MgUserBase();
				baseUpdate.setId(userBase.getId());
				baseUpdate.setPhone(strNull(user.getPhone()));
				baseUpdate.setRealname(strNull(user.getRealname()));
				baseUpdate.setDeptname(strNull(user.getDeptname()));
				baseUpdate.setPosition(strNull(user.getPosition()));
				boolean baseflag = baseUpdate.update();
				if(!baseflag) {
					return failRet("附属信息修改失败");
				}
				if(StrUtil.isNotBlank(user.getRids())) {
					delUserRole(user.getId());
					String[] ridStrs = user.getRids().split(",");
					for(String ridStr : ridStrs) {
						Integer rid = Integer.parseInt(ridStr);
						MgRole role = MgRole.dao.findById(rid);
						if(role == null) {
							log.error("修改用户时，选择的角色不存在");
							continue;
						}
						MgUserRole userRole = new MgUserRole();
						userRole.setMguid(user.getId());
						userRole.setRid(rid);
						userRole.replaceSave();
					}
				}
				return true;
			}
		};
		boolean commit = Db.use(MgConst.Db.TIO_MG).tx(atom);
		if(!commit) {
			return atom.getRetObj();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).remove(user.getId() + "");
		return RetUtils.okOper();
	}
	
	
	
	/**
	 * 删除用户的角色
	 * @param mguid
	 * @author xufei
	 * 2020年6月2日 下午3:54:18
	 */
	public void delUserRole(Integer mguid) {
		Db.use(MgConst.Db.TIO_MG).delete("delete from mg_user_role where mguid = ?",mguid);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午3:49:34
	 */
	public String strNull(String str) {
		return StrUtil.isBlank(str) ? "" : str;
	}
	
	
	/**
	 * 获取用户的基础数据
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年6月18日 下午3:24:58
	 */
	public MgUserBase getBase(Integer mguid) {
		return MgUserBase.dao.findFirst("select * from mg_user_base where mguid = ?",mguid);
	}
	
	/**
	 * 删除用户-深度删除
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午3:22:42
	 */
	public Ret del(Integer mguid) {
		MgUser user = MgUser.dao.findById(mguid);
		if(user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if(Objects.equals(user.getStatus(), Const.Status.DELETE)) {
			return RetUtils.failMsg("用户已删除");
		}
		MgUser update = new MgUser();
		update.setId(mguid);
		update.setStatus(Const.Status.DELETE);
		boolean del = update.update();
		if(!del) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).remove(mguid + "");
		return RetUtils.okOper();
	}
	
	/**
	 * 用户密码加密
	 * @param loginname
	 * @param plainpwd
	 * @return
	 * @author xufei
	 * 2020年6月18日 下午3:25:18
	 */
	public static String getMd5Pwd(String loginname, String plainpwd) {
		String pwd = SecureUtil.md5("${" + StrUtil.trim(loginname) + "}" + StrUtil.trim(plainpwd));
		return pwd;
	}

	public Ret bindIp(Integer id, String ip) {
		MgUser user = MgUser.dao.findById(id);
		if (user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if(Objects.equals(user.getStatus(), Const.Status.DELETE)) {
			return RetUtils.failMsg("用户已删除");
		}
		if (!isValidIPv4(ip)) {
			return RetUtils.failMsg("ip格式不正确");
		}
		MgUser update = new MgUser();
		update.setId(id);
		update.setBindip(ip);
		boolean del = update.update();
		if(!del) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_USER_MENU_AUTH).remove(id + "");
		return RetUtils.okOper();
	}
	public static boolean isValidIPv4(String ip) {
		String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
		return ip.matches(regex);
	}
}
