
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wxmobile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.sitexxx.service.model.main.UserThird.SubTable;
import org.tio.sitexxx.service.model.main.UserThirdWx;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.MobileLogin;

/**
 * 
 * @author tanyaowu
 */
public class WxMobileLogin extends MobileLogin {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxMobileLogin.class);

	public static WxMobileLogin me = new WxMobileLogin();

	/**
	 * 
	 */
	private WxMobileLogin() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public SubTable createSubTable(HttpRequest request, Integer type) {
		String country = request.getParam("country");
		String province = request.getParam("province");
		String city = request.getParam("city");

		UserThirdWx userThirdWx = new UserThirdWx();
		userThirdWx.setCity(city);
		userThirdWx.setCountry(country);
		userThirdWx.setProvince(province);
		return userThirdWx;
	}

}
