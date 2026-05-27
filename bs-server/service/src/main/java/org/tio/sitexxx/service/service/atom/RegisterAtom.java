
package org.tio.sitexxx.service.service.atom;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.service.base.RegisterService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.UserThirdService;
import org.tio.sitexxx.service.service.stat.StatService;
import org.tio.utils.BinaryUtils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

public class RegisterAtom extends AbsAtom {

	private static Logger log = LoggerFactory.getLogger(RegisterAtom.class);

	private boolean isThird = false;

	private User user;

	public RegisterAtom(User user) {
		this.user = user;
	}

	@Override
	public boolean run() throws SQLException {
		user.setInvFlag(false);
		if (RegisterService.me.isLoginnameExists(user.getLoginname())) {
			msg = "该账号已注册，如忘记密码，请找回";
			log.error(msg + ",loginname:{},nick:{},pwd:{}", user.getLoginname(), user.getNick(), user.getPwd());
			return false;
		}
		String nick = user.getNick();
		String initNick = nick;

//		while (RegisterService.me.isNickExists(nick)) {
//			nick += RandomUtil.randomString(RandomUtil.BASE_CHAR, 2);//.randomAlphanumeric(2);
//			msg = "您的昵称【" + initNick + "】已经存在，平台已为您生成新的昵称【" + nick + "】，您登录后可在个人中心进行昵称修改，谢谢！";
//		}
		user.setNick(nick);

		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenInviteShow'");
		if (clientConf.getValue().equals(1)) {
			user.setInviteshow(1);
		} else {
			user.setInviteshow(0);
		}

		//		if (isThird) {
		//			while (RegisterService.me.isNickExists(nick)) {
		//				nick += RandomUtil.randomString(RandomUtil.BASE_CHAR, 2);//.randomAlphanumeric(2);
		//			}
		//			user.setNick(nick);
		//		} else if (RegisterService.me.isNickExists(nick)) {
		//			msg = "昵称已被注册，请换一个昵称";
		//			log.error(msg + ",loginname:{},nick:{},pwd:{}", user.getLoginname(), user.getNick(), user.getPwd());
		//			return false;
		//		}

		if (UserService.ME.save(user) == null) {
			msg = "账号注册失败：账号信息有误";
			log.error(msg + ",loginname:{},nick:{},pwd:{}", user.getLoginname(), user.getNick(), user.getPwd());
			return false;
		}

		String inviteCode = BinaryUtils.encode(user.getId());
		user.setInvitecode(inviteCode);
		user.update();

		if (user.getUserThird() != null) {
			UserThird userThird = user.getUserThird();
			userThird.setUid(user.getId());
			boolean thirdFlag = UserThirdService.me.save(userThird); //userThird.save();

			if (!thirdFlag) {
				msg = "账号注册失败：三方登录关联信息保存异常";
				log.error(msg + ",openid:{},type:{}", userThird.getOpenid(), userThird.getType());
				return false;
			}
		}

		boolean baseResult = RegisterService.me.initBaseInfo(user);
		if (!baseResult) {
			msg = "账号注册失败：基础数据初始化失败";
			log.error(msg + ",loginname:{},nick:{},pwd:{}", user.getLoginname(), user.getNick(), user.getPwd());
			return false;
		}
		boolean roleResult = RegisterService.me.initRole(user);
		if (!roleResult) {
			msg = "账号注册失败：角色数据初始化失败";
			log.error(msg + ",loginname:{},nick:{},pwd:{}", user.getLoginname(), user.getNick(), user.getPwd());
			return false;
		}

		RegisterService.me.initWx(user);

		//清空当前用户数
		Caches.getCache(CacheConfig.USER_COUNT).clear();

		if (StrUtil.isBlank(msg)) {
			msg = "注册成功";
		}
		StatService.me.userRegisterStat(user);
		return true;
	}

	public boolean isThird() {
		return isThird;
	}

	public void setThird(boolean isThird) {
		this.isThird = isThird;
	}
}
