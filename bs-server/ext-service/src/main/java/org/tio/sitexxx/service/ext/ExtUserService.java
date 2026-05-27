
package org.tio.sitexxx.service.ext;

import java.util.Objects;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserlogModifyNick;
import org.tio.sitexxx.service.service.base.SensitiveWordsService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.CommonUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author lixinji
 * 2022年2月16日 上午8:36:51
 */
public class ExtUserService {
	private static Logger log = LoggerFactory.getLogger(ExtUserService.class);

	public static final ExtUserService ME = new ExtUserService();
	
	public static final UserService userService = UserService.ME;
	
	public static final User nullUser = new User();

	/**
	 * @param loginname
	 * @param pd5
	 * @param isThirdLogin
	 * @return
	 * @author lixinji
	 * 2022年2月16日 上午8:40:05
	 */
	public Ret login(String loginname, String pd5) {
		String code = "code";
		if (StrUtil.isBlank(loginname)/** || ( StrUtil.isBlank(pwd) &&  !isThirdLogin) */
		) {
			return Ret.fail(code, 1);
		}
		User user = userService.getByLoginname(loginname, null);
		if (user == null) {
			log.info("can find user by loginname:【{}】", loginname);
			return Ret.fail(code, 1); //loginname不存在
		}

		if (!Objects.equals(pd5, user.getPwd())) {
			log.info("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getPwd());
			return Ret.fail(code, 2); //密码不正确 
		}
		return Ret.ok("user", user);
	}
	
	
	/**
	 * @param user
	 * @param initPwd
	 * @param newPwd
	 * @return
	 * @author lixinji
	 * 2022年2月16日 上午8:56:11
	 */
	public Resp updatePwd(User user, String initPwd, String newPwd) {
		String md5pwd = UserService.getMd5Pwd(user.getLoginname(), initPwd);
		if (!Objects.equals(md5pwd, user.getPwd())) {
			return Resp.fail("原密码不正确");
		}
		String sql = "update user set pwd = ? where id = ?";
		int c = Db.update(sql,newPwd, user.getId());
		if (c <= 0) {
			log.error("修改无变动：" + "修改密码");
		}
		userService.notifyClearCache(user.getId());
		return Resp.ok();
	}
	
	
	/**
	 * @param user
	 * @param newNick
	 * @param avatarPath
	 * @return
	 * @author lixinji
	 * 2022年2月16日 下午6:13:53
	 */
	public Resp updateNick(User user, String newNick, String avatarPath) {
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
			String filterContent = newNick;
			filterContent = SensitiveWordsService.findAndReplace(filterContent);
			filterContent = StringEscapeUtils.escapeHtml4(filterContent);
			user.setNick(filterContent);
		}
		int c = -1;
		if (StrUtil.isBlank(avatarPath)) {
			String sql = "update user set nick = ? where id = ?";
			c = Db.update(sql, newNick, user.getId());
		} else {
			String sql = "update user set nick = ?,avatar = ?,avatarbig = ? where id = ?";
			c = Db.update(sql, newNick, avatarPath, avatarPath, user.getId());
		}
		if (c < 1) {
			log.error("修改无变动：" + "用户昵称修改失败");
		}
		userService.notifyClearCache(user.getId());
		userlogModifyNick.save();
		return Resp.ok().msg("用户昵称修改成功");
	}

	/**
	 * 校验密码
	 * @param user
	 * @param initPwd
	 * @return
	 */
	public Resp checkPwd(User user, String initPwd) {
		if (!Objects.equals(initPwd, user.getPwd()) && !Objects.equals(initPwd, user.getPhonepwd())) {
			return Resp.fail("密码错误");
		}
		return Resp.ok("成功");
	}
	
}
