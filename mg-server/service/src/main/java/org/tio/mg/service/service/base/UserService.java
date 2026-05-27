
package org.tio.mg.service.service.base;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.IAtom;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.Role;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.model.main.UserAddress;
import org.tio.mg.service.model.main.UserBase;
import org.tio.mg.service.model.main.UserInfoSyn;
import org.tio.mg.service.model.main.UserThird;
import org.tio.mg.service.model.main.UserThirdOsc;
import org.tio.mg.service.model.main.UserlogModifyAvatar;
import org.tio.mg.service.model.main.UserlogModifyNick;
import org.tio.mg.service.service.UserBaseService;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.conf.AvatarService;
import org.tio.mg.service.utils.CommonUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.Threads;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.cache.redis.RedisCache;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

/**
 * @author tanyaowu
 * 2016年8月10日 上午11:09:59
 */
public class UserService {
	private static Logger log = LoggerFactory.getLogger(UserService.class);

	public static final UserService ME = new UserService();

	public static final RoleService roleService = RoleService.me;

	public static final User nullUser = new User();

	//	ICache loginnameAndUserCache;

	//	ICache useridAndUserCache;

	/**
	 *
	 * @author tanyaowu
	 */
	private UserService() {
		//		loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER);
		//		useridAndUserCache = Caches.getCache(CacheConfig.USERID_USER_2);
	}

	/**
	 * 查询用户信息，主要用于展示给其它人看，所以有的信息是不允许查询出来的
	 * 会区分超管和非超管
	 * @param currUser
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public Record info1(User currUser, Integer uid) throws Exception {
		boolean isSuper = UserService.isSuper(currUser);
		ICache cache = Caches.getCache(CacheConfig.USER_INFO);

		String key = String.valueOf(isSuper) + uid;
		boolean putTempToCacheIfNull = true;
		Record record = CacheUtils.get(cache, key, putTempToCacheIfNull, () -> {
			User user = UserService.ME.getById(uid);
			if (user == null) {
				return null;
			}
			Record record1 = new Record();
			record1.set("nick", user.getNick());
			record1.set("id", user.getId());
			record1.set("avatar", user.getAvatar());
			
			
//			Kv params = Kv.by("uid", uid);
//			SqlPara sqlPara = User.dao.getSqlPara("user.searchByUid", params);
//			Record record = Db.findFirst(sqlPara);
			
			perfectUserInfo(isSuper, record1);
			UserBase userBase = UserBaseService.me.getUserBaseByUid(uid);
			if(userBase != null) {
				record1.set("sex", userBase.getSex());
				record1.set("sign", userBase.getSign());
			}
			return record1;
		
		});
		return record;
	}
	
	/**
	 * 获取用户信息，所有人得到的值是一样的，不分超管和普通用户
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public Record info(Integer uid) throws Exception {
		return info1(null, uid);
	}

	/**
	 * 搜索用户
	 * @param currUser
	 * @param nick
	 * @param uid
	 * @param loginname
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 * @throws Exception 
	 */
	public Page<Record> search(User currUser, String nick, Integer uid, String loginname, Integer pageNumber, Integer pageSize) throws Exception {
		if (pageNumber == null) {
			pageNumber = 1;
		}
		if (pageSize == null) {
			pageSize = 20;
		}
		Integer pageNumber1 = pageNumber;
		Integer pageSize1 = pageSize;

		boolean isSuper = UserService.isSuper(currUser);
		if (!isSuper) { //非超管不能用登录名查询
			loginname = null;
		}

		ICache cache = Caches.getCache(CacheConfig.SEARCH_USER);

		Page<Record> page = null;
		Kv params = Kv.by("isSuper", isSuper);
		if (uid != null) { // 根据uid查询
			params.set("uid", uid);
			SqlPara sqlPara = User.dao.getSqlPara("user.searchByUid", params);
			page = Db.paginate(pageNumber, pageSize, sqlPara);
			perfectUserInfo(isSuper, page.getList());
		} else if (StrUtil.isNotBlank(loginname)) {
			params.set("loginname", loginname);
			SqlPara sqlPara = User.dao.getSqlPara("user.searchByLoginname", params);
			page = Db.paginate(pageNumber, pageSize, sqlPara);
			perfectUserInfo(isSuper, page.getList());
		} else if (StrUtil.isNotBlank(nick)) {
			nick = nick.trim();
			if (!isSuper) {
				if (StrUtil.containsAny(nick, "%")) {
					return null;
				}
				loginname = null; //非超管，不允许使用loginname进行查询
			}

			//多个空格，换成%
			Pattern p = Pattern.compile("\\s+");
			Matcher m = p.matcher(nick);
			String searchNick = m.replaceAll("%");

			String key = isSuper + "_" + pageNumber + "_" + pageSize + "_" + searchNick;
			boolean putTempToCacheIfNull = true;
			page = CacheUtils.get(cache, key, putTempToCacheIfNull, new FirsthandCreater<Page<Record>>() {
				@Override
				public Page<Record> create() throws Exception {
					params.set("nick", "%" + searchNick + "%");
					SqlPara sqlPara = User.dao.getSqlPara("user.searchByNick", params);
					Page<Record> page = Db.paginate(pageNumber1, pageSize1, sqlPara);

					perfectUserInfo(isSuper, page.getList());
					return page;
				}
			});
		} else {
			return null;
		}
		return page;

	}

	/**
	 * 完善用户信息
	 * @param isSuper
	 * @param list
	 * @throws Exception
	 * @author tanyaowu
	 */
	public void perfectUserInfo(boolean isSuper, List<Record> list) throws Exception {
		if (list != null) {
			for (Record record : list) {
				perfectUserInfo(isSuper, record);
			}
		}
	}

	/**
	 * 完善用户信息
	 * @param isSuper
	 * @param record
	 * @throws Exception
	 * @author tanyaowu
	 */
	public void perfectUserInfo(boolean isSuper, Record record) throws Exception {
		if (record == null) {
			return;
		}
		Integer uid = record.getInt("id");
		if (uid != null) {
			User user = UserService.ME.getById(uid);
			record.set("roles", user.getRoles());
			if (user.getIpid() != null) {
				IpInfo ipInfo = IpInfoService.ME.getById(user.getIpid());
				if (ipInfo != null) {
					record.set("country", ipInfo.getCountry());
					record.set("province", ipInfo.getProvince());
					record.set("city", ipInfo.getCity());
				}

				UserThirdOsc userThirdOsc = null;
				if ("5".equals(user.getPwd()) && user.getLoginname().startsWith("__osc_")) {
					UserThird userThird = UserThirdService.me.getByUid(uid);
					if (userThird != null) {
						userThirdOsc = UserThirdOscService.me.getByUserThirdId(userThird.getId());
						if (userThirdOsc != null) {
							record.set("osc_url", userThirdOsc.getUrl());
						}
					}
				}

				if (isSuper) {
					if (ipInfo != null) {
						record.set("ip", ipInfo.getIp());
					}
					record.set("createtime", user.getCreatetime());
					record.set("loginname", user.getLoginname());
					if (userThirdOsc != null) {
						record.set("osc_email", userThirdOsc.getEmail());
					}
				}
			}
		}
	}

	/**
	 * 清空用户所有的相关缓存
	 * @param uid 如果为null，则清除所有用户的缓存
	 * 
	 * @date 2016年11月20日 下午1:45:19
	 */
	public void _clearCache(Integer uid) {
		if (uid == null) {
			Caches.getCache(CacheConfig.USERID_USER_2).clear();
			Caches.getCache(CacheConfig.LOGINNAME_USER).clear();
			Caches.getCache(CacheConfig.USER_INFO).clear();
			Caches.getCache(CacheConfig.USERID_BASE).clear();
		} else {
			User user = UserService.ME.getById(uid);
			if (user != null) {
				Caches.getCache(CacheConfig.LOGINNAME_USER).remove(user.getLoginname());
			}
			
			String key = uid + "";
			Caches.getCache(CacheConfig.USERID_USER_2).remove(key);

			ICache cache = Caches.getCache(CacheConfig.USER_INFO);
			cache.remove(String.valueOf(false) + key);
			cache.remove(String.valueOf(true) + key);

			Caches.getCache(CacheConfig.USERID_BASE).remove(key);
		}
	}
	
	/**
	 * 发topic通知清除用户缓存
	 * @param uid
	 */
	public void notifyClearCache(Integer uid) {
		_clearCache(uid);

		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_USER);
		topicVo.setValue(uid);
		RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);

		//这里设置clientid，避免过滤掉自己
		//有的消息是要过滤自己的，就不能设置此值
		//		topicVo.setClientId("3");
		topic.publish(topicVo);
	}

	/**
	 * 所有用户，包括各种状态，调用后根据业务进行状态处理
	 * @param id
	 * @return
	 * @author tanyaowu
	 */
	public User getById(Integer id) {
		if (id == null) {
			return null;
		}
		String key = id + "";

		User user1 = CacheUtils.get(Caches.getCache(CacheConfig.USERID_USER_2), key, true, new FirsthandCreater<User>() {
			@Override
			public User create() {
				User user = User.dao.findById(id);

				if (user == null) {
					return null;
				}

				if (Const.USE_ANONYMOUS) {
					if (user.getXx() == (short) 1) {
						user.setAvatar(AVATARS[id % AVATARS.length]);
					}
				}
				roleService.setRoles(user);

				IpInfo ipInfo = IpInfoService.ME.getById(user.getIpid());
				user.setIpInfo(ipInfo);

				UserThird userThird = UserThirdService.me.getByUid(user.getId());
				user.setUserThird(userThird);

				if (StrUtil.isBlank(user.getAvatar())) {
					user.setAvatar(User.DEFAULT_AVATAR);
				}

				if (StrUtil.isBlank(user.getAvatarbig())) {
					user.setAvatarbig(user.getAvatar());
				}
				UserBase userBase = UserBaseService.me.getUserBaseByUid(user.getId());
				if(userBase != null) {
					user.setPhone(userBase.getPhone());
					user.setSign(userBase.getSign());
					user.setSex(userBase.getSex());
				}
				return user;
			}
		});
		return user1;
	}

	/**
	 * 所有状态下的用户，调用后，根据业务进行处理
	 * @param id
	 * @return
	 * @author: tanyaowu
	 */
	public User getById(String id) {
		if (StrUtil.isBlank(id)) {
			return null;
		}

		return getById(Integer.parseInt(id));
	}

	/**
	 * 保存用户，会连着保存ip等信息
	 * @param user
	 * @return
	 * @author tanyaowu
	 */
	public User save(User user) {
		IpInfo ip = user.getIpInfo();
		ip = IpInfoService.ME.save(ip);

		if (ip != null) {
			user.setIpid(ip.getId());
			boolean ff = user.save();
			if (ff) {
				user.setIpInfo(ip);
				return user;
			}
		}
		return null;
	}

	/**
	 * 保存唯一用户
	 * @param user
	 * @return
	 */
	public User userUnionSave(User user) {
		return null;
	}

	/**
	 * 获取用户数
	 * @return
	 */
	public int getUserCount() {
		ICache cache = Caches.getCache(CacheConfig.USER_COUNT);
		String key = org.tio.mg.service.cache.Caches.SINGLE_KEY;

		Integer count = CacheUtils.get(cache, key, true, new FirsthandCreater<Integer>() {
			@Override
			public Integer create() {
				String sql = "select count(*) from user";
				return Db.use(Const.Db.TIO_SITE_MAIN).queryInt(sql);
			}
		});

		return count;
	}

	/**
	 * 根据loginname获取用户
	 * @param loginname
	 * @param status 如果为null则不限状态
	 * @return
	 * @author tanyaowu
	 */
	public User getByLoginname(String loginname, Short status) {
		ICache loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER);
		User user = (User) loginnameAndUserCache.get(loginname);
		if (user == null) {
			user = User.dao.findFirst("select * from user where loginname = ? and `status` <> ?", loginname, org.tio.mg.service.model.main.User.Status.LOGOUT);
			if (user != null) {
				user = getById(user.getId());
			}
			if (user != null) {
				loginnameAndUserCache.put(loginname, user);
			} else {
				loginnameAndUserCache.putTemporary(loginname, nullUser);
			}
		} else {
			//可能是 nullUser
			if (user.getId() == null) {
				return null;
			}
			if (status == null) {
				return user;
			}
			if (!Objects.equals(status, user.getStatus())) {
				return null;
			}
		}
		return user;
	}

	/**
	 * 1、loginname不存在
	 * 2、密码不正确
	 * 3、换aes登录吧
	 * @param loginname
	 * @param pd5 md5加密的密码
	 * @param isThirdLogin
	 * @return
	 */
	public Ret login(String loginname, String pd5, boolean isThirdLogin) {
		String code = "code";
		if (StrUtil.isBlank(loginname)/** || ( StrUtil.isBlank(pwd) &&  !isThirdLogin) */
		) {
			return Ret.fail();
		}
		User user = getByLoginname(loginname, null);
		if (user == null) {
			log.info("can find user by loginname:【{}】", loginname);
			return Ret.fail(code, 1); //loginname不存在
		}

		if (!isThirdLogin) {
			if (!Objects.equals(pd5, user.getPwd())) {
				log.info("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getPwd());
				return Ret.fail(code, 2); //密码不正确 
			}
		}

		return Ret.ok("user", user);
	}

	/**
	 * 修改用户昵称
	 * @param user
	 * @param newNick
	 * @return
	 * @author tanyaowu
	 */
	public Resp updateNick(User user, String newNick,String avatarPath) {
		if (newNick.equals(user.getNick())) {
			return Resp.fail("原来的昵称就是这个哦！");
		}
		Resp resp = CommonUtils.checkGroupName(newNick, "昵称");
		if (!resp.isOk()) {
			return resp;
		}

		UserlogModifyNick userlogModifyNick = new UserlogModifyNick();
		userlogModifyNick.setNewnick(newNick);
		userlogModifyNick.setOldnick(user.getNick());
		userlogModifyNick.setUid(user.getId());

		if (StrUtil.isNotBlank(newNick)) {
			Integer check = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select id from user where nick = ? and id != ? and `status` != ? limit 1", newNick, user.getId(),
			        org.tio.mg.service.model.main.User.Status.LOGOUT);
			if (check != null) {
				return Resp.fail("昵称已存在");
			}
			//			user.setSrcnick(newNick);
			String filterContent = newNick;
			filterContent = SensitiveWordsService.findAndReplace(filterContent);
			filterContent = StringEscapeUtils.escapeHtml4(filterContent);
			user.setNick(filterContent);
		}
//		String loginname = user.getLoginname();
		int c = -1;
		if(StrUtil.isBlank(avatarPath)) {
			String sql = "update user set nick = ? where id = ?";
			c = Db.update(sql, newNick, user.getId());
		} else {
			String sql = "update user set nick = ?,avatar = ?,avatarbig = ? where id = ?";
			c = Db.update(sql, newNick,avatarPath,avatarPath,user.getId());
		}
		if (c < 1) {
			return Resp.fail("用户昵称修改失败");
		}
		notifyClearCache(user.getId());
		userlogModifyNick.save();
		initSynInfo(user.getId(), Const.UserToImSynType.NICK, newNick,null);
		if(StrUtil.isNotBlank(avatarPath))  {
			initSynInfo(user.getId(), Const.UserToImSynType.AVATAR, avatarPath,null);
		}
		return Resp.ok().msg("用户昵称修改成功");
	}

	/**
	 * 更新头像
	 * @param user
	 * @param newavatar
	 * @param newavatarbig
	 * @return
	 * @author tanyaowu
	 */
	public Resp updateAvatar(User user, String newavatar, String newavatarbig) {
		UserlogModifyAvatar userlogModifyAvatar = new UserlogModifyAvatar();
		userlogModifyAvatar.setNewavatar(newavatar);
		userlogModifyAvatar.setNewavatarbig(newavatarbig);
		userlogModifyAvatar.setOldavatar(user.getAvatar());
		userlogModifyAvatar.setOldavatarbig(user.getAvatarbig());
		userlogModifyAvatar.setUid(user.getId());

//		String loginname = user.getLoginname();
		String sql = "update user set avatar = ?, avatarbig = ? where id = ?";
		int c = Db.update(sql, newavatar, newavatarbig, user.getId());

		if (c < 1) {
			return Resp.fail("用户头像修改失败");
		}
		notifyClearCache(user.getId());

		userlogModifyAvatar.save();
		initSynInfo(user.getId(), Const.UserToImSynType.AVATAR, newavatar,null);
		return Resp.ok().msg("用户头像修改成功");
	}
	
	
	/**
	 * 修改验证方式
	 * @param user
	 * @param fdvalidtype
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午5:24:48
	 */
	public Resp updateFdvalidtype(User user, Short fdvalidtype) {
		String sql = "update user set fdvalidtype = ? where id = ?";
		int c = Db.update(sql, fdvalidtype, user.getId());

		if (c < 1) {
			return Resp.fail("用户验证方式失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户验证方式成功");
	}
	
	/**
	 * 消息提醒设置
	 * @param user
	 * @param remindflag
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午6:21:30
	 */
	public Resp updateRemind(User user, Short remindflag) {
		String sql = "update user set msgremindflag = ? where id = ?";
		int c = Db.update(sql, remindflag, user.getId());

		if (c < 1) {
			return Resp.fail("用户消息提醒设置失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户消息提醒设置成功");
	}
	
	
	/**
	 * 修改电话
	 * @param user
	 * @param searchflag
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午6:24:15
	 */
	public Resp updatePhone(User user, String phone) {
		if(phone == null) {
			phone = "";
		}
		String sql = "update user_base set phone = ? where uid = ?";
		int c = Db.update(sql, phone, user.getId());
		if (c < 1) {
			return Resp.fail("用户修改电话失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户修改电话成功");
	}
	
	/**
	 * 修改签名
	 * @param user
	 * @param sign
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午6:30:39
	 */
	public Resp updateSign(User user, String sign) {
		if(sign == null) {
			sign = "";
		}
		String sql = "update user_base set sign = ? where uid = ?";
		int c = Db.update(sql, sign, user.getId());
		if (c < 1) {
			return Resp.fail("用户修改签名失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户修改签名成功");
	}
	
	/**
	 * 修改用户
	 * @param curr
	 * @param update
	 * @return
	 * @author xufei
	 * 2020年4月23日 上午11:06:07
	 */
	public Resp updateUser(User curr, User update) {
		if(update == null) {
			return Resp.fail().msg(RetUtils.INVALID_PARAMETER);
		}
		if (StrUtil.isNotBlank(update.getNick())) {
			Integer check = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select id from user where nick = ? and id != ? and `status` != ? limit 1", update.getNick(), curr.getId(),
			        org.tio.mg.service.model.main.User.Status.LOGOUT);
			if (check != null) {
				return Resp.fail("昵称已存在");
			}
			//			user.setSrcnick(newNick);
			String filterContent = update.getNick();
			filterContent = SensitiveWordsService.findAndReplace(filterContent);
			filterContent = StringEscapeUtils.escapeHtml4(filterContent);
			update.setNick(filterContent);
		}
		AbsTxAtom absTxAtom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				Kv param = Kv.by("uid", curr.getId());
				if(StrUtil.isBlank(update.getSign())) {
					param.set("sign","");
				} else {
					param.set("sign",update.getSign());
				}
				if(update.getSex() != null) {
					param.set("sex",update.getSex());
				}
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("user.updateBase", param);
				Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
				update.setId(curr.getId());
				return update.update();
			}
		};
		boolean atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
		if(!atom) {
			return Resp.fail("用户修改失败");
		}
		notifyClearCache(curr.getId());
		return Resp.ok().msg("用户修改成功");
	}
	
	
	
	/**
	 * 性别修改
	 * @param user
	 * @param sex
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午6:33:38
	 */
	public Resp updateSex(User user, Short sex) {
		if(sex == null) {
			sex = Const.UserSex.MALE;
		}
		if(!Objects.equals(user.getSex(), sex)) {
			try {
				if (user.getAvatar().trim().indexOf("/avatar/tio/") == 0) {
					String avatar = AvatarService.nextAvatar(sex + "");
					String sql = "update user set avatar = ?, avatarbig = ? where id = ?";
					Db.update(sql, avatar, avatar, user.getId());
				}
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
		String sql = "update user_base set sex = ? where uid = ?";
		int c = Db.update(sql, sex, user.getId());
		if (c < 1) {
			return Resp.fail("用户修改性别失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户修改性别成功");
	}
	
	/**
	 * @param user
	 * @param searchflag
	 * @return
	 * @author xufei
	 * 2020年3月3日 下午6:25:59
	 */
	public Resp updateSearchFlag(User user, Short searchflag) {
		String sql = "update user set searchflag = ? where id = ?";
		int c = Db.update(sql, searchflag, user.getId());

		if (c < 1) {
			return Resp.fail("用户设置别人搜索开关失败");
		}
		notifyClearCache(user.getId());
		return Resp.ok().msg("用户设置别人搜索开关成功");
	}
	

	/**
	 * 修改密码
	 * @param user
	 * @param initPwd 用户输入的原密码
	 * @param newPwd
	 * @return
	 * @author tanyaowu
	 */
	public Resp updatePwd(User user, String initPwd, String newPwd) {
		String loginname = user.getLoginname();
		String md5pwd = getMd5Pwd(loginname, initPwd);

		if (!Objects.equals(md5pwd, user.getPwd())) {
			return Resp.fail("原密码不正确");
		}

		String newmd5pwd = newPwd;//getMd5Pwd(loginname, newPwd);
		String sql = "update user set pwd = ? where id = ?";
		int c = Db.update(sql, newmd5pwd, user.getId());
		if (c > 0) {
			notifyClearCache(user.getId());
		}

		return Resp.ok();
	}

	public Resp addRoleByLoginname(String loginname, short roleid) {
		User user = getByLoginname(loginname, org.tio.mg.service.model.main.User.Status.NORMAL);
		if (user == null) {
			return Resp.fail("用户不存在或状态不正常");
		}
		return addRole(user, roleid);
	}

	public Resp addRoleByNick(String nick, short roleid) {
		User user = getByNick(nick);
		if (user == null) {
			return Resp.fail("昵称不存在");
		}

		if (!Objects.equals(user.getStatus(), org.tio.mg.service.model.main.User.Status.NORMAL)) {
			return Resp.fail("用户状态不对");
		}

		return addRole(user, roleid);
	}

	/**
	 * 给某用户添加角色
	 * @param loginname
	 * @param roleid
	 * @return
	 * @author tanyaowu
	 */
	public Resp addRole(User user, short roleid) {
		if (user == null) {
			return Resp.fail("用户为空");
		}

		boolean f = Db.use(Const.Db.TIO_SITE_MAIN).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				//先删除
				removeRole(user, roleid, false);

				//再添加
				String sql = "insert into user_role (uid, rid, status) values (?,?,?)";
				Db.use(Const.Db.TIO_SITE_MAIN).update(sql, user.getId(), roleid, 1);

				return true;
			}

		});

		if (f) {
			notifyClearCache(user.getId());
			return Resp.ok();
		} else {
			return Resp.fail("操作失败");
		}
	}

	/**
	 * 删除角色
	 * @param user
	 * @param roleid
	 * @return
	 */
	public Resp removeRole(User user, short roleid) {
		return removeRole(user, roleid, true);
	}

	public Resp removeRole(User user, short roleid, boolean clearCache) {
		String sql = "delete from user_role where uid = ? and rid = ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql, user.getId(), roleid);
		if (clearCache) {
			notifyClearCache(user.getId());
		}

		return Resp.ok();
	}

	/**
	 * 获取所有有可能侵权的头像
	 * @return
	 * @author xufei
	 * 2020年1月15日 下午1:48:42
	 */
	public List<Record> getTortAvatarUser() {
		String sql = "select u.id,nick,avatar,b.sex from `user` u INNER JOIN user_base b on u.id = b.uid where avatar not like '/user/avatar/%' and avatar not like 'http%'";
		List<Record> users = Db.use(Const.Db.TIO_SITE_MAIN).find(sql);
		return users;
	}
	
	/**
	 * 获取md5加密密码
	 * @param loginname 登录名
	 * @param plainpwd 明文密码
	 * @return
	 */
	public static String getMd5Pwd(String loginname, String plainpwd) {
		String pwd = SecureUtil.md5("${" + StrUtil.trim(loginname) + "}" + StrUtil.trim(plainpwd));
		return pwd;
	}

	/**
	 * 修改用户地址
	 * @param uid
	 * @param userAddress
	 * @return
	 * 
	 */
	public Ret updateUserAddress(Integer uid, UserAddress userAddress) {
		if (userAddress == null || uid == null) {
			return Ret.fail().set("msg", "无效参数");
		}
		userAddress.setUid(uid);
		userAddress.update();
		return Ret.ok().set("data", userAddress);
	}

	public static final String[] AVATARS = new String[] { "/img/avatar/1.png", "/img/avatar/10171119181614.jpg", "/img/avatar/1106070_jc1127.png", "/img/avatar/1168934_100.jpeg",
	        "/img/avatar/1485256_wooxz.png", "/img/avatar/2.jpg", "/img/avatar/20171118121406.png", "/img/avatar/20171118122407.jpg", "/img/avatar/20171118124108.jpg",
	        "/img/avatar/20171118124247.png", "/img/avatar/20171118124927.jpg", "/img/avatar/20171118125112.jpg", "/img/avatar/20171118125630.jpg",
	        "/img/avatar/20171118181652.jpg", "/img/avatar/20171119182050.jpg", "/img/avatar/20180416154203.jpg", "/img/avatar/20180416155040.jpg",
	        "/img/avatar/20180429093933.jpg", "/img/avatar/2232696_talent-iofan.png", "/img/avatar/302580_wu1g119.png", "/img/avatar/3196787_100.jpg",
	        "/img/avatar/3440734_100.jpeg", "/img/avatar/3802362_50.jpeg", "/img/avatar/463940_hehui082452239.jpg", "/img/avatar/512121_SJRSB.png", "/img/avatar/556878_100.gif",
	        "/img/avatar/636232_meallon.png", "/img/avatar/87d66e45edd0274fe9c29b8cb54f9258_1.jpg", "/img/avatar/a.jpg", "/img/avatar/beimi.jpeg", "/img/avatar/cc.png",
	        "/img/avatar/eee.png", "/img/avatar/fds.png", "/img/avatar/fdsse.png", "/img/avatar/gopush.jpeg", "/img/avatar/l.png", "/img/avatar/liyus.jpg",
	        "/img/avatar/luxiaolei.jpg", "/img/avatar/orpherus.jpg", "/img/avatar/qbug.png", "/img/avatar/shts.jpg", "/img/avatar/springForAll.png", "/img/avatar/xianxin.jpg",
	        "/img/avatar/zhishu.png" };

	/**
	 * 是否是超管，上帝视角
	 * @param user
	 * @return
	 */
	public static final boolean isSuper(User user) {
		if (user == null) {
			return false;
		}
		return UserRoleService.hasRole(user, Role.ADMIN_SUPER);
	}

	/**
	 * 根据用户昵称修改用户状态 (status)
	 * @param nick
	 * @param newStatus
	 * @return
	 */
	public int updateUserStatus(String nick, short newStatus) {
		if (StrUtil.isBlank(nick)) {
			return 0;
		}

		Integer uid = getUidByNick(nick);
		return updateUserStatus(uid, newStatus);
	}

	/**
	 * 根据uid修改用户状态 (status)
	 * @param uid
	 * @param newStatus
	 * @return
	 */
	public int updateUserStatus(Integer uid, short newStatus) {
		if (uid == null) {
			return 0;
		}
		String sql = "update user set status = ? where id = ?";
		int c = Db.update(sql, newStatus, uid);

		notifyClearCache(uid);

		return c;
	}


	/**
	 * 根据用户昵称将用户的状态改为正常
	 * @param nick
	 * @return
	 */
	public int normalUserByNick(String nick) {
		Integer uid = getUidByNick(nick);
		return normalUserByUid(uid);
	}

	/**
	 * 根据uid将用户的状态改为正常
	 * @param uid
	 * @return
	 */
	public int normalUserByUid(Integer uid) {
		return updateUserStatus(uid, org.tio.mg.service.model.main.User.Status.NORMAL);
	}

	/**
	 * 根据昵称找到uid
	 * @param nick
	 * @return
	 */
	public Integer getUidByNick(String nick) {
		if (StrUtil.isBlank(nick)) {
			return null;
		}
		ICache cache = Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_SECONDS_5);
		String key = "user_nick_id" + RedisCache.SPLIT_FOR_CACHENAME + nick;
		Integer uid = CacheUtils.get(cache, key, true, new FirsthandCreater<Integer>() {
			@Override
			public Integer create() {
				String sql = "select id from user where nick = ?";
				return Db.use(Const.Db.TIO_SITE_MAIN).queryInt(sql, nick);
			}
		});
		return uid;
	}

	public User getByNick(String nick) {
		Integer uid = getUidByNick(nick);
		if (uid != null) {
			return getById(uid);
		}
		return null;
	}

	/**
	 * 根据ipid获取这个用户可能是谁
	 * @param ipid
	 * @return
	 */
	public Integer getUidByIpid(Integer ipid) {
		if (ipid == null) {
			return null;
		}
		String sql = "SELECT uid FROM `tio_token_path_access_stat` where ipid=? and uid is not null order by firstAccessTime desc LIMIT 0, 1";
		return Db.use(Const.Db.TIO_SITE_STAT).queryInt(sql, ipid);
	}

	/**
	 * 完善一下评论列表/博客列表等中的个人信息
	 * @param record
	 * @param ext
	 * @param uidKey
	 */
	public static void completeUser(Record record, Map<String, Object> ext, String uidKey) {
		Integer uid = record.getInt(uidKey);
		if (uid != null) {
			String uidStr = org.tio.utils.hutool.StrUtil.int2Str(uid);
			if (ext.get(uidStr) != null) {
				return;
			}
			User user = UserService.ME.getById(uid);
			if (user != null) {
				Kv kv = Kv.by("nick", user.getNick()).set("avatar", user.getAvatar()).set("roles", user.getRoles());
				ext.put(uidStr, kv);
			}
		}
	}

	/**
	 * 完善一下评论列表/博客列表等中的个人信息
	 * @param list
	 * @param ext
	 * @param uidKey
	 */
	public static void completeUser(List<Record> list, Map<String, Object> ext, String uidKey) {
		if (list != null) {
			for (Record record : list) {
				UserService.completeUser(record, ext, uidKey);
			}
		}
	}

	/**
	 * 完善一下评论列表/博客列表等中的个人信息
	 * @param page
	 * @param uidKey
	 */
	public static void completeUser(Page<Record> page, String uidKey) {
		List<Record> list = page.getList();
		if (list != null) {
			Map<String, Object> ext = new HashMap<>();
			page.setExt(ext);
			for (Record record : list) {
				UserService.completeUser(record, ext, uidKey);
			}
		}
	}

	/**
	 * 用户是否包含某种角色
	 * @param user
	 * @param code
	 * @return
	 */
	public static boolean hasRole(User user, Short code) {
		return UserRoleService.hasRole(user, code);
	}

	/**
	 * 根据登录名或昵称获取用户
	 * @param loginname
	 * @param nick
	 * @return
	 */
	public static User getByLoginnameOrNick(String loginname, String nick) {
		User user = null;
		if (StrUtil.isNotBlank(loginname)) {
			user = UserService.ME.getByLoginname(loginname, Const.Status.NORMAL);
		} else {
			user = UserService.ME.getByNick(nick);
		}
		return user;
	}
	
	/**
	 * 获取两个userid的key
	 * @param uid1
	 * @param uid2
	 * @return
	 */
	public static String twoUid(Integer uid1, Integer uid2) {
		return Math.max(uid1, uid2) + "_" + Math.min(uid1, uid2);
	}

	public static List<Integer> robots = null;

	public void initRobot() {
		if (robots != null) {
			return;
		}
		synchronized (this) {
			int maxId = 23356;
			//			int total = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select count(*) from user where id <= ?", maxId);
			int count = 3000;
			int start = 0;

			//			if (total > count) {
			//				start = RandomUtil.randomInt(0, total - count);
			//			}

			List<Integer> list = Db.use(Const.Db.TIO_SITE_MAIN).query("select id from user where id <= ? limit ?,?", maxId, start, count);

			robots = list;
		}
	}

	/**
	 * 用户信息同步数据
	 * @param uid
	 * @param type
	 * @param bizStr
	 * @author xufei
	 * 2020年3月16日 上午10:28:08
	 */
	public void initSynInfo(Integer uid,Short type,String bizStr,Long bizbigint) {
		Threads.getGroupExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					UserInfoSyn infoSyn = new UserInfoSyn();
					switch (type) {
					case Const.UserToImSynType.NICK:
						infoSyn.setBizstr(bizStr);
						break;
					case Const.UserToImSynType.AVATAR:
						infoSyn.setBizstr(bizStr);
						break;
					case Const.UserToImSynType.GROUP_NICK:
						infoSyn.setBizstr(bizStr);
						infoSyn.setBizbigint(bizbigint);
						break;
					default:
						break;
					}
					infoSyn.setType(type);
					infoSyn.setUid(uid);
					infoSyn.replaceSave();
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
			}
		});
	}
	
	/**
	 * 
	 * @return
	 */
	public User nextRobot() {
		if (robots != null && robots.size() > 0) {
			Integer uid = RandomUtil.randomEle(robots);
			return getById(uid);
		}
		return null;
	}

}
