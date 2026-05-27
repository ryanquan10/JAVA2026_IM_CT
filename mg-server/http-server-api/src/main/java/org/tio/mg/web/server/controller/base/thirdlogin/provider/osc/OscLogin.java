
/**
 * 
 */
package org.tio.mg.web.server.controller.base.thirdlogin.provider.osc;

import java.text.MessageFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.mg.service.model.main.UserThird;
import org.tio.mg.service.model.main.UserThirdOsc;
import org.tio.mg.web.server.controller.base.thirdlogin.ThirdLoginUtils;
import org.tio.mg.web.server.controller.base.thirdlogin.provider.Auth2Login;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.utils.http.HttpUtils;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;
import okhttp3.Response;

/**
 * @author tanyaowu
 *
 */
public class OscLogin extends Auth2Login {
	private static Logger		log						= LoggerFactory.getLogger(OscLogin.class);
	public static final String	AppID					= P.get("third.login.osc.pc.AppID");
	public static final String	AppSecret				= P.get("third.login.osc.pc.AppSecret");
	/**
	 * 获取授权码
	 * 
	 */
	public static final String	OAUTH_CONNECT_URL		= "https://www.oschina.net/action/oauth2/authorize?response_type=code&client_id=" + AppID + "&state={0}&redirect_uri={1}";
	/**
	 * 获取access_token
	 */
	public static final String	OAUTH_ACCESSTOKEN_URL	= "https://www.oschina.net/action/openapi/token?client_id=" + AppID + "&client_secret=" + AppSecret + "&code={0}&grant_type=authorization_code&dataType=json&redirect_uri={1}";
	/**
	 * 获取用户信息
	 */
	public static final String	OAUTH_USERINFO_URL		= "https://www.oschina.net/action/openapi/user?access_token={0}&dataType=json";
	public static OscLogin	me						= new OscLogin();

	private OscLogin() {
	}

	@Override
	public String loginUrl(HttpRequest request, Integer type, String state) throws Exception {
		String redirect_uri = ThirdLoginUtils.getCallbackUrl(WebApiInit.httpConfig, type);
		String url = MessageFormat.format(OAUTH_CONNECT_URL, state, redirect_uri);
		return url;
	}

	@Override
	public UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception {
		String redirect_uri = ThirdLoginUtils.getCallbackUrl(WebApiInit.httpConfig, type);
		String url = MessageFormat.format(OAUTH_ACCESSTOKEN_URL, code, redirect_uri);
		Response response = HttpUtils.get(url);
		
		/**
		 * {
			    "access_token": "8447ff97-9b8c-4224-9cec-63b97d34ba65", 
				"refresh_token": "8447ff97-9b8c-4224-9cec", 
			    "token_type": "bearer", 
			    "expires_in": 43199,
				"uid": 12
			}
		 */
		String respStr = response.body().string();
		log.error("osc access_token:" + respStr);
		OscAccessTokenResp data = Json.toBean(respStr, OscAccessTokenResp.class);
		
		if (StrUtil.isNotBlank(data.getAccess_token())) {
			String unionid = "__osc_" + data.getUid();
			String url2 = MessageFormat.format(OAUTH_USERINFO_URL, data.getAccess_token());
			Response response2 = HttpUtils.get(url2);
			String respStr2 = response2.body().string();
			log.error("osc userinfo:" + respStr2);
			OscUserInfo oscUserInfo = Json.toBean(respStr2, OscUserInfo.class);
			if (oscUserInfo.getId() != null) {
				UserThird userThird = new UserThird();
				userThird.setOpenid(unionid);
				userThird.setAvatar(oscUserInfo.getAvatar());
				userThird.setNick(oscUserInfo.getName());
				userThird.setSex(Objects.equals("male", oscUserInfo.getGender()) ? 1 : 2);
				userThird.setUnionid(unionid);
				
				UserThirdOsc userThirdOsc = new UserThirdOsc();
				userThirdOsc.setEmail(oscUserInfo.getEmail());
				userThirdOsc.setLocation(oscUserInfo.getLocation());
				userThirdOsc.setOscid(oscUserInfo.getId());
				userThirdOsc.setUrl(oscUserInfo.getUrl());
				
				userThird.setSubTable(userThirdOsc);
				return userThird;
				
			} else {
				log.error("OSC登录失败，获取userinfo响应:{}", respStr2);
				return null;
			}
		} else {
			return null;
		}
	}
}
