
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.IThirdLogin;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public abstract class Auth2Login implements IThirdLogin {
	private static Logger		log			= LoggerFactory.getLogger(Auth2Login.class);
	public static final String	STATE_KEY	= "THIRD_LOGIN_STATE_KEY";

	/**
	 * 
	 */
	public Auth2Login() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public HttpResponse toLoginPage(HttpRequest request, Integer type) throws Exception {
		HttpSession session = request.getHttpSession();
		String state = IdUtil.simpleUUID();
		String url = loginUrl(request, type, state);

		if (StrUtil.isNotBlank(url)) {
			session.setAttribute(STATE_KEY, state, request.getHttpConfig());
			return Resps.redirect(request, url);
		}
		request.close();
		return null;
	}

	/**
	 * 获取第三方登录url
	 * @param request
	 * @param type
	 * @param state
	 * @return
	 */
	public abstract String loginUrl(HttpRequest request, Integer type, String state) throws Exception;

	@Override
	public UserThird callback(HttpRequest request, Integer type) throws Exception {
		String state = request.getParam("state");
		String code = request.getParam("code");
		if (StrUtil.isBlank(state)) {
			request.close();
			return null;
		}

		HttpSession session = request.getHttpSession();
		String sessionState = session.getAttribute(STATE_KEY, String.class);
		session.removeAttribute(STATE_KEY, request.getHttpConfig());

		log.error("{}, code:{}", this.getClass().getSimpleName(), code);

		if (!Objects.equals(state, sessionState)) {
			request.close();
			return null;
		}

		UserThird userThird = getUserThird(request, type, state, code);
		userThird.setType(type);
		userThird.setTime(new Date());
		return userThird;
	}

	/**
	 * 
	 * @param request
	 * @param type
	 * @param state
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public abstract UserThird getUserThird(HttpRequest request, Integer type, String state, String code) throws Exception;

	@Override
	public boolean isAjax(HttpRequest request, Integer type) throws Exception {
		return false;
	}

}
