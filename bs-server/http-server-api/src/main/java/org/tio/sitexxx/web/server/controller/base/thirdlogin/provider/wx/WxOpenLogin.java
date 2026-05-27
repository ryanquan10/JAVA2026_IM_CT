
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wx;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.model.main.UserThirdWx;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.ThirdLoginUtils;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.Auth2Login;
import org.tio.sitexxx.web.server.init.WebApiInit;
import org.tio.utils.json.Json;

import okhttp3.Response;

/**
 *
 * 
 * @date 2016年10月29日 下午3:13:55
 */
public class WxOpenLogin extends Auth2Login {
	@SuppressWarnings("unused")
	private static Logger		log	= LoggerFactory.getLogger(WxOpenLogin.class);
	public static WxOpenLogin	me	= new WxOpenLogin();

	/**
	 * 
	 */
	private WxOpenLogin() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public String loginUrl(HttpRequest request, Integer type, String state) throws Exception {
		String url = WxLoginUtils.getAuthrOpenUrl(ThirdLoginUtils.getCallbackUrl(WebApiInit.httpConfig, type), "snsapi_userinfo", state);
		return url;
	}

	@Override
	public UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception {
		Response result = WxLoginUtils.oauthOpen(code);
		if (result.isSuccessful()) {
			String bodyString = result.body().string();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = Json.toBean(bodyString, Map.class);
			String openId = (String) map.get("openid");
			String access_token = (String) map.get("access_token");

			WxUserinfo wxUserinfo = WxLoginUtils.getWxUserInfo(access_token, openId);
			if (wxUserinfo == null) {
				return null;
			}

			UserThird userThird = new UserThird();
			userThird.setOpenid(openId);

			userThird.setAvatar(wxUserinfo.getHeadimgurl());
			userThird.setNick(wxUserinfo.getNickname());
			userThird.setSex(wxUserinfo.getSex());
			userThird.setUnionid(wxUserinfo.getUnionid());
			UserThirdWx userThirdWx = new UserThirdWx();
			userThirdWx.setCity(wxUserinfo.getCity());
			userThirdWx.setCountry(wxUserinfo.getCountry());
			userThirdWx.setProvince(wxUserinfo.getProvince());
			userThird.setSubTable(userThirdWx);

			return userThird;
		}

		return null;
	}
}
