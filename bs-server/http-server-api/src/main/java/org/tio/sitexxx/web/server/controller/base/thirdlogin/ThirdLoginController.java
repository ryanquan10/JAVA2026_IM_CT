
package org.tio.sitexxx.web.server.controller.base.thirdlogin;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.mvc.Routes;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.service.atom.RegisterAtom;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.UserThirdService;
import org.tio.sitexxx.service.utils.CommonUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.sitexxx.web.server.controller.base.LoginController;
import org.tio.sitexxx.web.server.controller.base.RegisterController;
import org.tio.sitexxx.web.server.utils.SessionCacheUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.ICache;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 第三方登录
 * @author tanyaowu
 *
 */
@RequestPath(value = "/tlogin")
public class ThirdLoginController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ThirdLoginController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author tanyaowu
	 */
	public ThirdLoginController() {
	}

	/**
	 * 第三方登录
	 * @param type 枚举值见：org.tio.sitexxx.web.server.controller.base.thirdlogin.UserThird.Type
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/{type}")
	public HttpResponse login(Integer type, HttpRequest request) throws Exception {
		IThirdLogin thirdLogin = ThirdLoginFactory.me.getThirdLogin(type);
		if (thirdLogin == null) {
			request.close();
			return null;
		}
		//Referer   org.tio.http.common.HttpConst.RequestHeaderKey.Referer
		String referer = request.getReferer();
		if (StrUtil.isNotBlank(referer)) {
			//			request.getHttpSession().setAttribute(SessionKey.THIRD_LOGIN_REFERER, referer, request.getHttpConfig());
			SessionCacheUtils.put(request, SessionKey.THIRD_LOGIN_REFERER, referer);
		}
		return thirdLogin.toLoginPage(request, type);
	}

	/**
	 * 第三方登录回调
	 * @param type 枚举值见：org.tio.sitexxx.web.server.controller.base.thirdlogin.UserThird.Type
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/cb/p/{type}")
	public HttpResponse callback(Integer type, HttpRequest request) throws Exception {
		IThirdLogin thirdLogin = ThirdLoginFactory.me.getThirdLogin(type);
		if (thirdLogin == null) {
			request.close();
			return null;
		}
		UserThird userThird = thirdLogin.callback(request, type);
		if (userThird == null) {
			request.close();
			return null;
		}
		UserThird userThirdInDb = null;
		String openid = userThird.getOpenid();
		String unionid = userThird.getUnionid();
		if (StrUtil.isNotBlank(unionid)) {
			userThirdInDb = UserThirdService.me.getByUnionid(type, unionid);
			if (userThirdInDb == null) {
				String typeSplit = ThirdLoginFactory.getSimilarTypesStr(type);
				userThirdInDb = UserThirdService.me.getByUnionid(typeSplit, unionid);
				if (userThirdInDb != null) {
					userThird.setUid(userThirdInDb.getUid());
					boolean thirdFlag = UserThirdService.me.save(userThird); //userThird.save();
					if (!thirdFlag) {
						if (thirdLogin.isAjax(request, type)) {
							return Resps.json(request, Resp.fail("三方同步绑定失败"));
						} else {
							return Resps.redirect(request, "/");
						}
					}
				}
			}
		} else if (StrUtil.isNotBlank(openid)) {
			userThirdInDb = UserThirdService.me.getByOpenid(type, openid);
		}
		String clientip = request.getClientIp();
		IpInfo ipInfo = IpInfoService.ME.save(clientip);

		String pwd = type + "";
		if (userThirdInDb == null) //还没有本平台建立联系，需要绑定
		{
			//			userThird = new UserThird();
			//			userThird.setOpenid(openid);
			//			userThird.setTime(new Date());
			//			userThird.setType(type);
			//			userThird.setSex(userThird.getSex());
			//			userThird.setCity(userThird.getCity());
			//			userThird.setFollowers(userThird.getFollowers());
			//			userThird.setCreatedAt(userThird.getCreatedAt());

			User user = new User();
			if (StrUtil.isNotBlank(unionid)) {
				user.setLoginname(unionid);
			} else {
				user.setLoginname(openid);
			}
			RequestExt requestExt = WebUtils.getRequestExt(request);
			short deviceType = requestExt.getDeviceType();
			user.setRegistertype(deviceType);
			user.setPwd(pwd);

			String nick = userThird.getNick();
			Resp resp = CommonUtils.checkGroupName(nick, "昵称");
			if (!resp.isOk()) {
				//第三方昵称如果不合规，则随机生成一个
				nick = RandomUtil.randomString(RandomUtil.BASE_CHAR, 6);
			}
			user.setNick(nick);
			user.setAvatar(userThird.getAvatar());
			user.setAvatarbig(userThird.getAvatar());
			user.setStatus(org.tio.sitexxx.service.model.main.User.Status.NORMAL);
			user.setIpInfo(ipInfo);
			user.setThirdbindflag(Const.YesOrNo.YES);
			user.setUserThird(userThird);
			user.setThirdstatus(Const.UserThirdStatus.INIT);
			user.setThirdtype(type);
			user.setCreatetime(new Date());
			String referer = (String) SessionCacheUtils.get(request, SessionKey.THIRD_LOGIN_REFERER);
			user.setReghref(referer);

			RegisterAtom registerUserAtom = new RegisterAtom(user);
			registerUserAtom.setThird(true);
			//			boolean relsut = Db.tx(registerUserAtom);
			boolean result = RegisterController.register(request, user, registerUserAtom);
			if (result) {
				if (StrUtil.isNotBlank(unionid)) {
					ICache openidCache = Caches.getCache(CacheConfig.OPENID_USERTHIRD);
					String key = type + "_" + unionid;
					openidCache.remove(key);
					userThirdInDb = UserThirdService.me.getByUnionid(type, unionid);//(type, inStr, openid);
				} else {
					ICache openidCache = Caches.getCache(CacheConfig.OPENID_USERTHIRD);
					String key = type + "_" + openid;
					openidCache.remove(key);
					userThirdInDb = UserThirdService.me.getByOpenid(type, openid);
				}
			} else {
				if (thirdLogin.isAjax(request, type)) {
					return Resps.json(request, Resp.fail(registerUserAtom.getMsg()));
				} else {
					return Resps.redirect(request, "/");//Resp.fail(registerUserAtom.getMsg());
				}
			}
		}
		//		else {
		//			更新数据
		//		}
		Integer uid = userThirdInDb.getUid();
		User user = UserService.ME.getById(uid);
		request.setAttribute(RequestKey.IS_THIRD_LOGIN, true);
		request.setAttribute(RequestKey.THIRD_LOGIN_USER, user);
		HttpResponse response = null;
		if (thirdLogin.isAjax(request, type)) {
			//			return response;
		} else {
			String referer = (String) SessionCacheUtils.get(request, SessionKey.THIRD_LOGIN_REFERER);
			if (StrUtil.isNotBlank(referer)) {
				response = Resps.redirect(request, referer);
			} else {
				response = Resps.redirect(request, "/tioim");
			}
		}
		request.setAttribute(RequestKey.THIRD_LOGIN_RESPONSE, response);

		LoginController loginController = Routes.getController(LoginController.class);
		return loginController.login(user.getLoginname(), null, null, request);
	}
}
