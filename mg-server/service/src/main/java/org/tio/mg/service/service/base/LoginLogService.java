
package org.tio.mg.service.service.base;

import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.LoginLog;
import org.tio.mg.service.model.main.User;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.hutool.StrUtil;

/**
 * @author tanyaowu 
 * 2016年9月15日 下午1:42:50
 */
public class LoginLogService {
	public static final LoginLogService	me	= new LoginLogService();
	public static final LoginLog		dao	= new LoginLog().dao();

	/**
	 * 
	 * @author: tanyaowu
	 */
	public LoginLogService() {
	}

	public void add(LoginLog loginLog) {
		loginLog.save();
	}

	/**
	 * 根据 sessionid获取最近一次登录日志信息
	 * @param sessionid
	 * @return
	 * @author tanyaowu
	 */
	public LoginLog selectLastLoginLog(String sessionid) {
		if (StrUtil.isBlank(sessionid)) {
			return null;
		}

		String cacheKey = "login_log.sessionid." + sessionid;
		return CacheUtils.get(Const.CacheTime.SECOND_60, null, cacheKey, true, new FirsthandCreater<LoginLog>() {
			@Override
			public LoginLog create() {
				String sql = "SELECT * FROM `login_log` where sessionid = ? order by time desc LIMIT 0, 1";
				LoginLog ret = dao.findFirst(sql, sessionid);
				return ret;
			}
		});
	}

	/**
	 * 根据 sessionid获取最近一次登录的用户
	 * @param sessionid
	 * @return
	 * @author tanyaowu
	 */
	public User selectLastLoginUser(String sessionid) {
		LoginLog loginLog = selectLastLoginLog(sessionid);
		if (loginLog == null) {
			return null;
		}

		User user = UserService.ME.getById(loginLog.getUid());
		return user;
	}
	
	/**
	 * 分页查询某用户的登录日志
	 * @param curr
	 * @param uid 查询谁的登录日志
	 * @param pageNumber
	 * @return
	 * @author tanyaowu
	 */
	public Page<Record> page(User curr, Integer uid, Integer pageNumber) {
		boolean isSuper = UserService.isSuper(curr);
		if (!isSuper) { //非超管不能查别人的登录日志
			uid = curr.getId();
		}
		Integer pageSize = 10;
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = User.dao.getSqlPara("user.pageLoginLog", params);
		Page<Record> page = Db.paginate(pageNumber, pageSize, sqlPara);
		return page;
	}

}
