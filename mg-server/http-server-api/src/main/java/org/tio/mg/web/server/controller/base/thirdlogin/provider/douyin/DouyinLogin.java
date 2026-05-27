
/**
 * 
 */
package org.tio.mg.web.server.controller.base.thirdlogin.provider.douyin;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.mg.service.model.main.UserThird;
import org.tio.mg.web.server.controller.base.thirdlogin.ThirdLoginUtils;
import org.tio.mg.web.server.controller.base.thirdlogin.auth2.AccessTokenResp;
import org.tio.mg.web.server.controller.base.thirdlogin.auth2.AccessTokenResp.Data;
import org.tio.mg.web.server.controller.base.thirdlogin.provider.Auth2Login;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.utils.http.HttpUtils;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import okhttp3.Response;

/**
 * https://connect.qq.com/intro/login/
 * @author tanyaowu
 *
 */
public class DouyinLogin extends Auth2Login {
	private static Logger		log						= LoggerFactory.getLogger(DouyinLogin.class);
	public static final String	AppID					= P.get("third.login.douyin.pc.AppID");
	public static final String	AppSecret				= P.get("third.login.douyin.pc.AppSecret");
	/**
	 * 获取授权码
	 */
	public static final String	OAUTH_CONNECT_URL		= "https://open.douyin.com/platform/oauth/connect?client_key=" + AppID + "&response_type=code&scope=user_info&state={0}&redirect_uri={1}";
	/**
	 * 获取access_token
	 */
	public static final String	OAUTH_ACCESSTOKEN_URL	= "https://open.douyin.com/oauth/access_token/?client_key=" + AppID + "&client_secret=" + AppSecret + "&code={0}&grant_type=authorization_code";
	/**
	 * 获取用户信息
	 */
	public static final String	OAUTH_USERINFO_URL		= "https://open.douyin.com/oauth/userinfo/?access_token={0}&open_id={1}";
	public static DouyinLogin	me						= new DouyinLogin();

	private DouyinLogin() {
	}

	@Override
	public String loginUrl(HttpRequest request, Integer type, String state) throws Exception {
		String redirect_uri = ThirdLoginUtils.getCallbackUrl(WebApiInit.httpConfig, type);
		String url = MessageFormat.format(OAUTH_CONNECT_URL, state, redirect_uri);
		return url;
	}

	@Override
	public UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception {
		String url = MessageFormat.format(OAUTH_ACCESSTOKEN_URL, code);
		Response response = HttpUtils.get(url);
		String respStr = response.body().string();
		AccessTokenResp accessTokenResp = Json.toBean(respStr, AccessTokenResp.class);
		if ("success".equals(accessTokenResp.getMessage())) {
			Data data = accessTokenResp.getData();
			
			String url2 = MessageFormat.format(OAUTH_USERINFO_URL, data.getAccess_token(), data.getOpen_id());
			Response response2 = HttpUtils.get(url2);
			String respStr2 = response2.body().string();
			DouyinUserinfoWrap douyinUserinfoWrap = Json.toBean(respStr2, DouyinUserinfoWrap.class);
			if ("success".equals(douyinUserinfoWrap.getMessage())) {
				DouyinUserinfoWrap.DouyinUserinfo douyinUserinfo = douyinUserinfoWrap.getData();
				if (0 == douyinUserinfo.getError_code()) {
					UserThird userThird = new UserThird();
					userThird.setOpenid(douyinUserinfo.getOpen_id());
					userThird.setAvatar(douyinUserinfo.getAvatar());
					userThird.setNick(douyinUserinfo.getNickname());
					userThird.setUnionid(douyinUserinfo.getUnion_id());
					return userThird;
				} else {
					log.error("抖音登录失败，获取userinfo响应:{}", respStr2);
					return null;
				}
				
			} else {
				log.error("抖音登录失败，获取userinfo响应:{}", respStr2);
				return null;
			}
		} else {
			return null;
		}
	}
}
