
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.qq;

import java.net.URLEncoder;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.model.main.UserThirdQq;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.ThirdLoginUtils;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.Auth2Login;
import org.tio.sitexxx.web.server.init.WebApiInit;
import org.tio.utils.http.HttpUtils;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import com.qq.connect.utils.QQConnectConfig;

import cn.hutool.core.util.StrUtil;
import okhttp3.Response;

/**
 * https://connect.qq.com/intro/login/
 * @author tanyaowu
 *
 */
public class QQLogin extends Auth2Login {
	private static Logger log = LoggerFactory.getLogger(QQLogin.class);

	public static QQLogin me = new QQLogin();

	/**
	 * 
	 */
	private QQLogin() {
		updateProperties();
	}

	private void updateProperties() {
		QQConnectConfig.updateProperties("app_ID", P.get("third.login.qq.pc.AppID"));
		QQConnectConfig.updateProperties("app_KEY", P.get("third.login.qq.pc.AppSecret"));
		QQConnectConfig.updateProperties("redirect_URI", ThirdLoginUtils.getCallbackUrl(WebApiInit.httpConfig, UserThird.Type.QQ));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@SuppressWarnings("deprecation")
	@Override
	public String loginUrl(HttpRequest request, Integer type, String state) throws Exception {
		String scope = QQConnectConfig.getValue("scope");
		return new Oauth().getAuthorizeURL(scope, state);
	}

	/**
	 * 文档：http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D
	 * 获取QQ登录unionid
	 * @param accessToken
	 * @return
	 * @throws Exception
	 */
	private static String getQQUnionID(String accessToken) throws Exception {
		String url = "https://graph.qq.com/oauth2.0/me?access_token=" + URLEncoder.encode(accessToken, "utf-8") + "&unionid=1";
		Response response = HttpUtils.get(url);

		if (response.isSuccessful()) {
			String string = response.body().string();
			int i = StrUtil.indexOf(string, '(');//.lastIndexOf(string, ")");//string.("(");
			int j = StringUtils.lastIndexOf(string, ")");
			string = StrUtil.trim(string.substring(i + 1, j - 1));
			UnionID unionidObj = Json.toBean(string, UnionID.class);
			//			log.error("QQ登录， AccessToken is null" + unionidObj.getError());
			if (StrUtil.isNotBlank(unionidObj.getUnionID())) {
				return unionidObj.getUnionID();
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception {
		String queryString = request.getRequestLine().getQueryString();
		try {
			@SuppressWarnings("deprecation")
			AccessToken accessTokenObj = (new Oauth()).getAccessTokenByQueryString(queryString, state);//.getAccessTokenByRequest(request);
			if (accessTokenObj == null) {
				return null;
			}
			String accessToken = null;
			String openID = null;
			@SuppressWarnings("unused")
			long tokenExpireIn = 0L;

			if (StrUtil.isBlank(accessTokenObj.getAccessToken())) {
				log.error("QQ登录， AccessToken is null");
				request.close();
				return null;
			} else {
				accessToken = accessTokenObj.getAccessToken();
				tokenExpireIn = accessTokenObj.getExpireIn();
				OpenID openIDObj = new OpenID(accessToken);
				openID = openIDObj.getUserOpenID();
				String unionID = getQQUnionID(accessToken);
				if (StrUtil.isBlank(unionID)) {
					return null;
				}

				UserInfo userinfo = new UserInfo(accessToken, openID);
				/**
				 * ret	返回码
					msg	如果ret<0，会有相应的错误信息提示，返回数据全部用UTF-8编码。
					nickname	用户在QQ空间的昵称。
					figureurl	大小为30×30像素的QQ空间头像URL。
					figureurl_1	大小为50×50像素的QQ空间头像URL。
					figureurl_2	大小为100×100像素的QQ空间头像URL。
					figureurl_qq_1	大小为40×40像素的QQ头像URL。
					figureurl_qq_2	大小为100×100像素的QQ头像URL。需要注意，不是所有的用户都拥有QQ的100x100的头像，但40x40像素则是一定会有。
					gender	性别。 如果获取不到则默认返回"男"
					is_yellow_vip	标识用户是否为黄钻用户（0：不是；1：是）。
					vip	标识用户是否为黄钻用户（0：不是；1：是）
					yellow_vip_level	黄钻等级
					level	黄钻等级
					is_yellow_year_vip	标识是否为年费黄钻用户（0：不是； 1：是）
				 */
				UserInfoBean userInfoBean = userinfo.getUserInfo();
				if (userInfoBean.getRet() == 0) {
					UserThird userThird = new UserThird();
					userThird.setOpenid(openID);
					userThird.setAvatar(userInfoBean.getAvatar().getAvatarURL100());
					userThird.setNick(userInfoBean.getNickname());
					userThird.setUnionid(unionID);
					String gender = userInfoBean.getGender();
					Integer sex = Objects.equals("男", gender) ? 1 : (Objects.equals("女", gender) ? 2 : null);
					if (sex != null) {
						userThird.setSex(sex);
					}

					UserThirdQq userThirdQq = new UserThirdQq();
					userThirdQq.setIsYellowVip(userInfoBean.isYellowYearVip() ? (short) 1 : (short) 2);
					if (userInfoBean.isYellowYearVip()) {
						userThirdQq.setYellowVipLevel(userInfoBean.getLevel() + "");
					}
					userThird.setSubTable(userThirdQq);
					return userThird;
				}
				return null;
			}
		} catch (QQConnectException e) {
			throw e;
		}
	}
}
