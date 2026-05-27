
/**
 * 
 */
package org.tio.mg.web.server.controller.base.thirdlogin.provider.apple;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.main.UserThird;
import org.tio.mg.service.vo.RequestExt;
import org.tio.mg.web.server.controller.base.thirdlogin.provider.Auth2Login;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.utils.crypto.Md5;
import org.tio.utils.jfinal.P;
import org.tio.utils.resp.Resp;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

/**
 * 用苹果的imei登录
 * 步骤：
 * 1、苹果端APP访问：/tlogin/9999, 
 * 2、服务器会返回下一步要访问的URL和验签数据，形如：{"url":"/cb/p/9999", "state":"yyyyyyyyyyyy"}
 * 3、App访问这个url进行登录，同时带上加密参数，形如：/cb/p/9999?state=6543gfdgwee&sign=xxxxxxxxxxxx
 * 		sign签名规则：md5(state + "服务器给的私钥" + imei);
 * 4、
 *
 */
public class AppleLogin extends Auth2Login {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AppleLogin.class);

	public static AppleLogin me = new AppleLogin();

	/**
	 * 
	 */
	private AppleLogin() {

	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

	}

	@Override
	public String loginUrl(HttpRequest request, Integer type, String state) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);

		if (!requestExt.isFromAppIos()) {
			return null;
		}

		return "/tlogin/cb/p/" + type;// + "?state=" + URLEncoder.encode(state, "utf-8");//WebUtils.path("/cb/p/" + type + "?state=" + URLEncoder.encode(state, "utf-8"));
	}

	@Override
	public UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);

		if (!requestExt.isFromAppIos()) {
			return null;
		}
		String sign = request.getParam("sign");
		if (StrUtil.isBlank(sign)) {
			return null;
		}

		String imei = requestExt.getImei();

		//md5(state + "服务器给的私钥" + imei);
		String pkey = P.get("third.login.apple.app.key", "jkijhbmn");
		String str = state + pkey + imei;
		String mysign = Md5.getMD5(str);

		//验签没通过
		if (!Objects.equals(mysign, sign)) {
			return null;
		}

		UserThird userThird = new UserThird();
		userThird.setOpenid(imei);
		userThird.setNick("IOS游客" + WebApiInit.sessionIdGenerator.sessionId(request.getHttpConfig(), request));
		return userThird;
	}

	@Override
	public HttpResponse toLoginPage(HttpRequest request, Integer type) throws Exception {
		HttpSession session = request.getHttpSession();

		String state = UUID.fastUUID().toString();
		String url = loginUrl(request, type, state);

		if (StrUtil.isNotBlank(url)) {
			session.setAttribute(STATE_KEY, state, request.getHttpConfig());

			Ret ret = Ret.ok("url", url);
			ret.set("state", state);
			Resp resp = Resp.ok(ret);
			return Resps.json(request, resp);
		}
		request.close();
		return null;
	}

	@Override
	public boolean isAjax(HttpRequest request, Integer type) throws Exception {
		return true;
	}
}
