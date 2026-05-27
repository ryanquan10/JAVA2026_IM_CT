
package org.tio.sitexxx.service.api.user.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.kit.StrKit;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.service.api.user.intf.BaseSynUserIntf;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserAgent;
import org.tio.sitexxx.service.model.main.UserToken;
import org.tio.sitexxx.service.pay.init.PayInit;
import org.tio.sitexxx.service.service.atom.RegisterAtom;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.LoginLogService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.UserTokenService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.AvatarUtils;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.OutUserVo;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionExt;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.utils.SystemTimer;
import org.tio.utils.resp.Resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

public class StdSynUser implements BaseSynUserIntf {

	private static final Logger log = LoggerFactory.getLogger(StdSynUser.class);

	/**
	 * 初始化数据
	 * @param users
	 * @return
	 * @author lixinji
	 * 2021年4月19日 上午11:06:37
	 */
	@Override
	public Ret init(List<OutUserVo> users) {
		if (CollectionUtil.isEmpty(users)) {
			return RetUtils.failMsg("外部用户列表为空");
		}
		int successCount = 0;
		Map<String, String> failMsg = new HashMap<String, String>();
		for (int i = 0; i < users.size(); i++) {
			OutUserVo outUser = users.get(i);
			if (StrUtil.isBlank(outUser.getUnioncode())) {
				failMsg.put(outUser.getNick() + i, "外部唯一code为空");
				continue;
			}
			try {
				Ret ret = initUser(outUser);
				if (ret.isFail()) {
					failMsg.put(outUser.getUnioncode(), RetUtils.getRetMsg(ret));
				}
				successCount++;
			} catch (Exception e) {
				continue;
			}
		}
		return RetUtils.okData(successCount).set("failmsg", failMsg);

	}

	/**
	 * 批量ddl操作
	 * @param users
	 * @return
	 * @author lixinji
	 * 2021年4月19日 上午11:24:56
	 */
	@Override
	public Ret userDdl(List<OutUserVo> users) {
		if (CollectionUtil.isEmpty(users)) {
			return RetUtils.failMsg("外部用户列表为空");
		}
		int successCount = 0;
		Map<String, String> failMsg = new HashMap<String, String>();
		for (int i = 0; i < users.size(); i++) {
			OutUserVo outUser = users.get(i);
			if (outUser.getOper() == null) {
				failMsg.put(outUser.getNick() + i, "操作码为空");
				continue;
			}
			try {
				switch (outUser.getOper()) {
				case OutUserVo.Oper.create:
					Ret createRet = initUser(outUser);
					if (createRet.isFail()) {
						failMsg.put(outUser.getUnioncode() + "-创建", RetUtils.getRetMsg(createRet));
						continue;
					}
				case OutUserVo.Oper.update:
					Ret updateRet = updateUser(outUser);
					if (updateRet.isFail()) {
						failMsg.put(outUser.getUnioncode() + "-修改", RetUtils.getRetMsg(updateRet));
						continue;
					}
					break;
				case OutUserVo.Oper.del:
					Ret delRet = delUser(outUser);
					if (delRet.isFail()) {
						failMsg.put(outUser.getUnioncode() + "-删除", RetUtils.getRetMsg(delRet));
						continue;
					}
					break;
				default:
					failMsg.put(outUser.getUnioncode() + "-无效", "无效操作码");
					continue;
				}
				successCount++;
			} catch (Exception e) {
				continue;
			}
		}
		return RetUtils.okData(successCount).set("failmsg", failMsg);
	}

	/**
	 * 自动登录
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月19日 上午11:25:00
	 */
	@Override
	public HttpResponse autoLogin(HttpRequest request, DefaultHttpRequestHandler requestHandler, OutUserVo outUser) {
		User user = User.dao.findFirst("select * from `user` where tiono = ? and `status` = ?", outUser.getUnioncode(), User.Status.NORMAL);
		HttpResponse httpResponse = Resps.json(request, Resp.ok());
		if (user == null) {
			return Resps.json(request, Resp.fail().msg("用户不存在"));
		}
		HttpSession httpSession = request.getHttpSession();
		HttpConfig httpConfig = request.getHttpConfig();
		String sessionId = httpSession.getId();
		RequestExt requestExt = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
		short deviceType = requestExt.getDeviceType();
		//是否是从手机端过来的请求
		boolean fromApp = requestExt.isFromApp();

		//添加登录日志
		String ip = request.getClientIp();
		IpInfo ipinfo = IpInfoService.ME.save(ip);
		LoginLog loginLog = new LoginLog();
		Date time = new Date();
		loginLog.setIp(ip);
		loginLog.setIpid(ipinfo.getId());
		loginLog.setUid(user.getId());
		loginLog.setType(Const.YesOrNo.YES);
		loginLog.setDevicetype(deviceType);
		loginLog.setTime(time);
		loginLog.setDayperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.DAY));
		loginLog.setTimeperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.TIME));
		loginLog.setHourperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.HOUR));
		if (fromApp) {
			loginLog.setDeviceinfo("api-" + StringUtils.substring(requestExt.getDeviceinfo(), 0, 128));
			loginLog.setImei(requestExt.getImei());
			loginLog.setAppversion(requestExt.getAppVersion());
		} else {
			UserAgent userAgent = requestExt.getUserAgent();
			if (userAgent != null) {
				if (userAgent.getId() != null) {
					loginLog.setUaid(userAgent.getId());
				}
				loginLog.setDeviceinfo("api-" + userAgent.getOsName() + " " + userAgent.getOsVersion() + "/" + userAgent.getAgentName() + " " + userAgent.getAgentVersionMajor());
			} else {
				loginLog.setDeviceinfo("api-" + StringUtils.substring(request.getUserAgent(), 0, 128));
			}
		}

		SessionExt oldSessionExt1 = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
		if (SystemTimer.currTime - oldSessionExt1.getCreateTime() > 1000 * 60 * 10) {
			requestHandler.updateSessionId(request, httpSession, httpResponse);
		}
		String newSeesionId = request.getHttpSession().getId();
		loginLog.setSessionid(newSeesionId);
		LoginLogService.me.add(loginLog);
		Caches.getCache(CacheConfig.WX_USER_LOGIN_TOKEN_1).put(newSeesionId, Const.YesOrNo.YES);
		//token
		UserToken userToken = UserTokenService.me.find(requestExt.getAppDevice(), user.getId());
		if (userToken == null) {
			userToken = new UserToken();
			userToken.setUid(user.getId());
			userToken.setDevicetype(requestExt.getAppDevice());
			userToken.setToken(newSeesionId);
			UserTokenService.me.add(userToken);
		} else {
			String oldToken = userToken.getToken();
			if (Objects.equals(oldToken, sessionId)) {
				//这里是自己T自己，相当于没T
				userToken.setToken(newSeesionId);
				UserTokenService.me.update(userToken);
			} else {
				//把原来别人登录的T出去
				HttpSession oldHttpSession = (HttpSession) httpConfig.getSessionStore().get(oldToken);
				if (oldHttpSession != null) {
					if (Objects.equals(ConfService.getInt("login.use.sso", 2), 1)) { //启用sso（单点登录）
						//发送被踢的信息-待扩展
						int kicktedCode = AppCode.ForbidOper.KICKTED;
						if (Objects.equals(deviceType, Devicetype.WEB.getValue())) {
							kicktedCode = AppCode.ForbidOper.KICKTED_PC;
						} else if (Objects.equals(deviceType, Devicetype.H5.getValue())) {
							kicktedCode = AppCode.ForbidOper.KICKTED_H5;
						}
						WxChatApi.sendFriendErrorMsg(requestExt.getDeviceType(), sessionId, ip, user.getId(), user.getId(), user.getId(), null, kicktedCode, "当前账号已在其他设备登录");
						//此处增加长链接断开逻辑
						SessionExt oldSessionExt = oldHttpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class, new SessionExt(), httpConfig);
						oldSessionExt.setUid(null);
						oldSessionExt.setKickedInfo(loginLog);
						oldHttpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
					} else {
						//不启用sso（单点登录）
					}
				}
				userToken.setToken(newSeesionId);
				UserTokenService.me.update(userToken);
			}
		}
		if (fromApp) {
			//删除相同设备的同步记录，没有同步数据
			SynService.me.delSynTime(requestExt.getAppDevice(), user.getId());
		}
		WxChatQueueApi.leaveFocusQueue(user, requestExt.getAppDevice(), null);
		SessionExt sessionExt = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
		sessionExt.setUid(user.getId());
		sessionExt.setLoginTime(SystemTimer.currTime);
		httpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
		return httpResponse;
	}

	/**
	 * 设置默认头像
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午1:43:54
	 */
	@Override
	public Ret defaultAvatar(OutUserVo user) {
		String path = ConfService.getString(Const.ConfMapping.OUT_USER_API_DEFAULT_AVATAR_URL, "");
		try {
			path = AvatarUtils.pressUserAvatar(user.getNick());
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		if (StrUtil.isNotBlank(path)) {
			user.setAvatar(path);
		}
		return RetUtils.okOper();
	}

	/**
	 * 初始化用户
	 * @param outUser
	 * @return
	 * @author lixinji
	 * 2021年4月19日 上午10:26:42
	 */
	private Ret initUser(OutUserVo outUser) {
		if (StrUtil.isBlank(outUser.getAvatar())) {
			defaultAvatar(outUser);
		}
		if (StrKit.isBlank(outUser.getNick())) {
			return RetUtils.failMsg("昵称不能为空");
		}
		if (StrKit.isBlank(outUser.getPhone())) {
			return RetUtils.failMsg("手机号不能为空");
		}
		String nick = StrUtil.trim(outUser.getNick());
		String phone = StrUtil.trim(outUser.getPhone());
		User user = new User();
		user.setLoginname(outUser.getPhone());
		IpInfo ipInfo = IpInfoService.ME.save(Const.MY_IP);
		user.setIpInfo(ipInfo);
		user.setPhone(phone);
		user.setNick(nick);
		user.setTiono(outUser.getUnioncode());
		user.setAvatar(outUser.getAvatar());
		user.setAvatarbig(outUser.getAvatar());
		user.setPhonebindflag(Const.YesOrNo.YES);
		if (StrUtil.isNotBlank(outUser.getEmail())) {
			user.setEmail(outUser.getEmail());
			user.setEmailbindflag(Const.YesOrNo.YES);
		}
		user.setStatus(User.Status.NORMAL);
		RegisterAtom registerUserAtom = new RegisterAtom(user);
		boolean relsut = Db.tx(registerUserAtom);
		if (!relsut) {
			return RetUtils.failMsg(registerUserAtom.getMsg());
		}
		Ret slef = ChatService.me.actFdChatItems(user.getId(), user.getId());
		if (slef.isFail()) {
			log.warn("同步用户时，发现自己加自己好友失败");
			return RetUtils.failMsg("自己加自己好友失败");
		}
		return RetUtils.okOper();
	}

	/**
	 * 修改用户
	 * @param outUser
	 * @return
	 * @author lixinji
	 * 2021年4月20日 上午11:30:15
	 */
	private Ret updateUser(OutUserVo outUser) {
		User user = User.dao.findFirst("select * from `user` where tiono = ? and `status` = ?", outUser.getUnioncode(), User.Status.NORMAL);
		if (user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		User update = new User();
		update.setId(user.getId());
		if (StrUtil.isNotBlank(outUser.getNick()) && !Objects.equals(user.getNick(), outUser.getNick())) {
			update.setNick(outUser.getNick());
		}
		if (StrUtil.isNotBlank(outUser.getPhone()) && !Objects.equals(user.getPhone(), outUser.getPhone())) {
			update.setPhone(outUser.getPhone());
		}
		if (StrUtil.isNotBlank(outUser.getAvatar()) && !Objects.equals(user.getAvatar(), outUser.getAvatar())) {
			update.setAvatar(outUser.getAvatar());
		}
		boolean commit = update.update();
		if (!commit) {
			return RetUtils.failMsg("修改失败");
		}
		UserService.ME.notifyClearCache(update.getId());
		if (StrUtil.isNotBlank(user.getAvatar())) {
			User sendUser = UserService.ME.getById(update.getId());
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						WxChatApi.synUserInfoToSelfAllInfo(sendUser.getId(), Const.UserToImSynType.USER_ALL, sendUser);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		} else if (StrUtil.isNotBlank(user.getNick())) {
			User sendUser = UserService.ME.getById(update.getId());
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						WxChatApi.synUserInfoToSelfAllInfo(sendUser.getId(), Const.UserToImSynType.NICK, sendUser);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return RetUtils.okOper();
	}

	/**
	 * 删除用户
	 * @param outUser
	 * @return
	 * @author lixinji
	 * 2021年4月20日 上午11:30:06
	 */
	private Ret delUser(OutUserVo outUser) {
		User user = User.dao.findFirst("select * from `user` where tiono = ? and `status` = ?", outUser.getUnioncode(), User.Status.NORMAL);
		if (user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if (!Objects.equals(User.Status.NORMAL, user.getStatus())) {
			return RetUtils.failMsg("注销申请失败，请确认账号状态");
		}
		if (Objects.equals(user.getOpenflag(), Const.YesOrNo.YES)) {
			boolean checkWallet = PayInit.payService.walletCheckLogout(user);
			if (!checkWallet) {
				return RetUtils.failMsg("注销申请失败，请确认账号状态");
			}
		}
		return UserService.ME.logout(user, -1);
	}
}
