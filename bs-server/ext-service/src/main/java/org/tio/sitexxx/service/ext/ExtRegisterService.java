

package org.tio.sitexxx.service.ext;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.StrKit;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.atom.RegisterAtom;
import org.tio.sitexxx.service.service.base.RegisterService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.sitexxx.service.utils.CommonUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年8月10日 上午11:09:59
 */
public class ExtRegisterService {
	private static Logger log = LoggerFactory.getLogger(ExtRegisterService.class);

	public static final ExtRegisterService me = new ExtRegisterService();

	public static final UserService userService = UserService.ME;

	/**
	 * @param user
	 * @param clientIp
	 * @param sessionid
	 * @param requestExt
	 * @return
	 * @throws Exception
	 * @author lixinji
	 */
	public Resp register(User user, String clientIp, String sessionid, RequestExt requestExt) throws Exception {
		Resp resp = regCheckAndSet(user);
		if (resp.isOk()) {
			RegisterAtom registerUserAtom = new RegisterAtom(user);
			boolean relsut = Db.tx(registerUserAtom);
			if (relsut) {
				userService.notifyClearCache(user.getId());
				return Resp.ok(registerUserAtom.getMsg()).data(Kv.by("loginname", user.getLoginname()).set("id", user.getId()).set("user", user));
			} else {
				return Resp.fail(registerUserAtom.getMsg());
			}
		} else {
			return resp;
		}
	}
	
	/**
	 * @param user
	 * @return
	 * @throws Exception
	 * @author lixinji
	 */
	public Resp regCheckAndSet(User user) throws Exception {
		String loginname = StrUtil.trim(user.getLoginname());
		String pwd = StrUtil.trim(user.getPwd());
		String nick = StrUtil.trim(user.getNick());
		if (StrKit.isBlank(loginname)) {
			return Resp.fail("账号不能为空");
		}
		if(StrKit.isBlank(pwd)) {
			log.error("注册密码为空");
			return Resp.fail("密码不能为空");
		}
		Resp resp = CommonUtils.checkGroupName(nick, "昵称");
		if (!resp.isOk()) {
			return resp;
		}
//		if (RegisterService.me.isNickExists(nick)) {
//			return Resp.fail("昵称已被注册，请换一个昵称");
//		}

		if (RegisterService.me.isLoginnameExists(loginname)) {
			return Resp.fail("该账号已注册，如忘记密码，请找回");
		}
		user.setLoginname(loginname);
		user.setNick(nick);
		user.setStatus(Const.Status.NORMAL);
		user.setCreatetime(new Date());
		//此处假绑定
		user.setPhonebindflag(Const.YesOrNo.YES);
		user.setPwd(pwd);
		user.setThirdstatus(Const.UserThirdStatus.NORMAL);
		if (StrUtil.isBlank(user.getAvatar())) {
			String avatar = AvatarService.nextAvatar();
			user.setAvatar(avatar);
			user.setAvatarbig(avatar);
		}
		return Resp.ok();
	}

}
