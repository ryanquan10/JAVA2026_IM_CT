
package org.tio.sitexxx.web.server.controller.ext;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.ext.ExtUserService;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserAgent;
import org.tio.sitexxx.service.model.main.UserToken;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.LoginLogService;
import org.tio.sitexxx.service.service.base.UserTokenService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.SessionExt;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.sitexxx.web.server.init.WebApiInit;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.sitexxx.web.server.vo.LoginResult.ErrorCode;
import org.tio.utils.SystemTimer;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 扩展登录
 * @author lixinji
 */
@RequestPath(value = "/extlogin")
public class LoginController {
	private static Logger log = LoggerFactory.getLogger(LoginController.class);

	/**
	 * @param args
	 * @author lixinji
	 */
	public static void main(String[] args) {

	}

	private ExtUserService userService = ExtUserService.ME;

	/**
	 * 
	 */
	public LoginController() {
	}

	/**
	 * 
	 * @param loginname 登录名称
	 * @param pd5 md5加密后的密码
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 */
	private Ret _login(String loginname, String pd5, HttpRequest request) throws Exception {
		if (StrUtil.isNotBlank(loginname)) {
			loginname = StrUtil.trim(loginname);
		}
		User user = null;
		Ret ret = userService.login(loginname, pd5);
		if (ret.isFail()) {
			Resp resp = Resp.fail();
			Integer code = (Integer) ret.get("code");
			if (code == 3) {
				resp.code(3);
			}
			if (code == 1) {
				resp.code(ErrorCode.USER_NO_EXIST.code).msg(ErrorCode.USER_NO_EXIST.value);
			} else {
				resp.code(ErrorCode.USER_OR_PWD_ERROR_PWD.code).msg(ErrorCode.USER_OR_PWD_ERROR_PWD.value);
			}
			HttpResponse httpResponse = Resps.json(request, resp);
			return Ret.fail().set("resp", httpResponse);//.fail(ErrorCode.USER_OR_PWD_ERROR, httpResponse);
		} else {
			user = (User) ret.get("user");
		}

		if (user != null) {
			Resp resp = checkStatus(user);
			if (resp.isOk()) {
				return Ret.ok().set("user", user);
			} else {
				HttpResponse httpResponse = Resps.json(request, resp);
				return Ret.fail().set("resp", httpResponse);
			}
		} else {
			Resp resp = Resp.fail();
			resp.code(ErrorCode.USER_OR_PWD_ERROR_PWD.code).msg(ErrorCode.USER_OR_PWD_ERROR_PWD.value);
			HttpResponse httpResponse = Resps.json(request, resp);
			return Ret.fail().set("resp", httpResponse);
		}
	}

	/**
	 * 验证用户
	 * @param user
	 * @return
	 * @author lixinji
	 */
	private static Resp checkStatus(User user) {
		if (Objects.equals(user.getStatus(), User.Status.NORMAL)) {
			return Resp.ok();
		} else {
			if (Objects.equals(user.getStatus(), org.tio.sitexxx.service.model.main.User.Status.INBLACK)) {
				return Resp.fail().code(ErrorCode.USER_INBLACK_ERROR.code).msg(ErrorCode.USER_INBLACK_ERROR.value);
			} else {
				return Resp.fail().code(ErrorCode.USER_STATUS_ERROR.code).msg(ErrorCode.USER_STATUS_ERROR.value);
			}
		}
	}

	/**
	 * @param loginname 登录名称
	 * @param pd5 md5加密后的密码
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 */
	@RequestPath(value = "")
	public HttpResponse login(String loginname, String pd5, HttpRequest request) throws Exception {
		loginname = StrUtil.trim(loginname);

		String sessionId = request.getHttpSession().getId();
		HttpSession httpSession = request.getHttpSession();
		HttpConfig httpConfig = request.getHttpConfig();


		Ret ret = this._login(loginname, pd5, request);

		if (ret.isOk()) {
			User user = (User) ret.get("user");

			RequestExt requestExt = WebUtils.getRequestExt(request);
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
				loginLog.setDeviceinfo("主-" + StringUtils.substring(requestExt.getDeviceinfo(), 0, 128));
				loginLog.setImei(requestExt.getImei());
				loginLog.setAppversion(requestExt.getAppVersion());
			} else {
				UserAgent userAgent = requestExt.getUserAgent();
				if (userAgent != null) {
					if (userAgent.getId() != null) {
						loginLog.setUaid(userAgent.getId());
					}
					loginLog.setDeviceinfo("主-" + userAgent.getOsName() + " " + userAgent.getOsVersion() + "/" + userAgent.getAgentName() + " " + userAgent.getAgentVersionMajor());
				} else {
					loginLog.setDeviceinfo("主-" + StringUtils.substring(request.getUserAgent(), 0, 128));
				}
			}

			HttpResponse httpResponse = Resps.json(request, Resp.ok());
			SessionExt oldSessionExt1 = WebUtils.getSessionExt(request);
			if (SystemTimer.currTime - oldSessionExt1.getCreateTime() > 1000 * 60 * 10) {
				WebApiInit.requestHandler.updateSessionId(request, httpSession, httpResponse);
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
							if (Objects.equals(deviceType, Devicetype.PC.getValue())) {
								kicktedCode = AppCode.ForbidOper.KICKTED_PC;
							} else if (Objects.equals(deviceType, Devicetype.H5.getValue())) {
								kicktedCode = AppCode.ForbidOper.KICKTED_H5;
							}
							WxChatApi.sendFriendErrorMsg(requestExt.getDeviceType(), sessionId, ip, user.getId(), user.getId(), user.getId(), null, kicktedCode, "当前账号已在其他设备登录");
							//此处增加长链接断开逻辑
							//							Tio.removeToken(TioSiteImServerStarter.tioServerConfigApp, oldToken, "异地登录被踢");
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
		} else {
			HttpResponse httpResponse = (HttpResponse) ret.get("resp");
			if (httpResponse == null) {
				log.error("_login(loginname, pwd, authcode, request)返回值没有包含response信息");
				return Resps.json(request, Resp.fail("服务器异常"));
			}
			return httpResponse;
		}
	}
}
