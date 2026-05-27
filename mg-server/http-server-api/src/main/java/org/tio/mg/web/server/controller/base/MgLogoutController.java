
package org.tio.mg.web.server.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.annotation.RequestPath;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.model.mg.MgUserToken;
import org.tio.mg.service.service.mg.MgUserTokenService;
import org.tio.mg.service.vo.RequestExt;
import org.tio.mg.service.vo.SessionExt;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.utils.resp.Resp;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/mglogout")
public class MgLogoutController {
	private static Logger log = LoggerFactory.getLogger(MgLogoutController.class);

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
	public MgLogoutController() {
	}

	/**
	 * 退出登录
	 * @param request
	 * @return
	 * @throws Exception
	 * @author: tanyaowu
	 */
	@RequestPath(value = "")
	public Resp logout(HttpRequest request) throws Exception {
		HttpConfig httpConfig = request.getHttpConfig();
		HttpSession httpSession = request.getHttpSession();
		String sessionId = httpSession.getId();
		MgUser user = WebUtils.currUser(request);
		if (user != null) {

			SessionExt sessionExt = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
			sessionExt.setUid(null);
			sessionExt.setLoginTime(null);
			sessionExt.setKickedInfo(null);
			httpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中

			RequestExt requestExt = WebUtils.getRequestExt(request);
			int deviceType = requestExt.getDeviceType();

			int c = MgUserTokenService.me.delete(deviceType, user.getId(), sessionId);
			if (c <= 0) {
				log.warn("can find usertoken by devicetype【{}】 and uid【{}】 and token:【{}】", deviceType, user.getId(), sessionId);

				MgUserToken userToken = MgUserTokenService.me.find(deviceType, user.getId());
				if (userToken != null) {
					String tokenInDb = userToken.getToken();
					HttpSession otherHttpSession = httpConfig.getHttpSession(tokenInDb);
					if (otherHttpSession != null) {
						Integer useridInOtherSession = WebUtils.getUserIdBySession(otherHttpSession);
						if (useridInOtherSession != null) {
							SessionExt sessionExtInOtherSession = otherHttpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
							if (sessionExtInOtherSession != null) {
								sessionExtInOtherSession.setUid(null);
								otherHttpSession.update(httpConfig);
							}
						}
					}

				}
			}
			Resp resp = Resp.ok();
			return resp;
		} else {
			Resp resp = Resp.fail("你并未登录");
			return resp;
		}
	}
}
