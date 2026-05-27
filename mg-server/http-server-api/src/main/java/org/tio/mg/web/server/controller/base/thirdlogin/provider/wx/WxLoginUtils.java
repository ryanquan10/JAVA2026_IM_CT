
package org.tio.mg.web.server.controller.base.thirdlogin.provider.wx;

import java.text.MessageFormat;

import org.tio.utils.http.HttpUtils;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import okhttp3.Response;

public class WxLoginUtils {

	public static final String	GET_QRCONNECT_URL			= "https://open.weixin.qq.com/connect/qrconnect?appid={0}&redirect_uri={1}&response_type=code&scope=snsapi_login&state={2}#wechat_redirect";
	public static final String	GET_OAUTH_USER_AGREE_URL	= "https://open.weixin.qq.com/connect/oauth2/authorize?appid={0}&redirect_uri={1}&response_type=code&scope={2}&state={3}&connect_redirect=1#wechat_redirect";
	public static final String	GET_OAUTH_URL				= "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code";
	public static final String	GET_USER_INFO				= "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}";
	public static final String	CHECK_OAUTH_TOKEN_IS_VALID	= "https://api.weixin.qq.com/sns/auth?access_token={0}&openid={1}";
	public static final String	REFRESH_TOKEN_URL			= "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={0}&grant_type=refresh_token&refresh_token={1}";

	public static final String	SOCIAL_LOGIN_CLIENT_ID		= P.get("third.login.wechat.pc.AppID");
	public static final String	SOCIAL_LOGIN_CLIENT_SERCRET	= P.get("third.login.wechat.pc.AppSecret");
	public static final String	SOCIAL_LOGIN_OPEN_ID		= P.get("wechat.open.appid");
	public static final String	SOCIAL_LOGIN_OPEN_SECRET	= P.get("wechat.open.appsecret");

	/**
	 * wechat login request code
	 * @param redirectUri
	 * @param state
	 * @return
	 * @throws Exception
	 */
	public static String getQrconnect(String redirectUri, String state) throws Exception {
		String getQrcodeUrl = MessageFormat.format(GET_QRCONNECT_URL, SOCIAL_LOGIN_CLIENT_ID, redirectUri, state);
		return getQrcodeUrl;
	}

	/**
	 * get authorization address - scope snsapi_userinfo | snsapi_base
	 * @param redirectUri
	 * @param scope
	 * @return
	 * @throws Exception
	 */
	public static String getAuthrUrl(String redirectUri, String scope) throws Exception {
		String getCodeUrl = MessageFormat.format(GET_OAUTH_USER_AGREE_URL, SOCIAL_LOGIN_CLIENT_ID, java.net.URLEncoder.encode(redirectUri, "utf-8"), scope);
		return getCodeUrl;
	}

	/**
	 * 
	 * @param redirectUri
	 * @param scope
	 * @return
	 * @throws Exception
	 * 
	 * @date 2016年10月29日 下午3:04:42
	 */
	public static String getAuthrOpenUrl(String redirectUri, String scope, String state) throws Exception {
		String getCodeUrl = MessageFormat.format(GET_OAUTH_USER_AGREE_URL, SOCIAL_LOGIN_OPEN_ID, java.net.URLEncoder.encode(redirectUri, "utf-8"), scope, state);
		return getCodeUrl;
	}

	/**
	 * 响应
	 * {
			"access_token":"ACCESS_TOKEN",
			"expires_in":7200,
			"refresh_token":"REFRESH_TOKEN",
			"openid":"OPENID",
			"scope":"SCOPE"
		}
	 * by code in exchange for web authorization - access_token
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public static Response oauth(String code) throws Exception {
		String oauthUrl = MessageFormat.format(GET_OAUTH_URL, SOCIAL_LOGIN_CLIENT_ID, SOCIAL_LOGIN_CLIENT_SERCRET, code);
		Response response = HttpUtils.get(oauthUrl);
		return response;
	}

	/**
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 * 
	 * @date 2016年10月29日 下午3:08:12
	 */
	public static Response oauthOpen(String code) throws Exception {
		String oauthUrl = MessageFormat.format(GET_OAUTH_URL, SOCIAL_LOGIN_OPEN_ID, SOCIAL_LOGIN_OPEN_SECRET, code);
		Response response = HttpUtils.get(oauthUrl);
		return response;
	}

	/**
	 * Gets the basic information for the login user
	 * @param accessToken
	 * @param openId
	 * @return
	 * @throws Exception
	 */
	public static WxUserinfo getWxUserInfo(String accessToken, String openId) throws Exception {
		String getUserInfoUrl = MessageFormat.format(GET_USER_INFO, accessToken, openId);
		Response response = HttpUtils.get(getUserInfoUrl);
		if (response.isSuccessful()) {
			WxUserinfo wxUserinfo = Json.toBean(response.body().string(), WxUserinfo.class);//(WxUserinfo) JSONObject.toBean(JSONObject.fromObject(response), WxUserinfo.class);
			return wxUserinfo;
		} else {
			return null;
		}
	}

	/**
	 * Check whether the authorization certificate is valid accessToken
	 * @param accessToken
	 * @param openId
	 * @return
	 * @throws Exception
	 */
	public static Response checkTokenIsValid(String accessToken, String openId) throws Exception {
		String checkTokenUrl = MessageFormat.format(CHECK_OAUTH_TOKEN_IS_VALID, accessToken, openId);
		Response response = HttpUtils.get(checkTokenUrl);
		return response;
	}

	/**
	 * refresh access_token(if need) refreshToken
	 * @param refreshToken
	 * @return
	 * @throws Exception
	 */
	public static Response refreshToken(String refreshToken) throws Exception {
		String refreshTokenUrl = MessageFormat.format(REFRESH_TOKEN_URL, SOCIAL_LOGIN_CLIENT_ID, refreshToken);
		Response response = HttpUtils.get(refreshTokenUrl);
		return response;
	}
}
