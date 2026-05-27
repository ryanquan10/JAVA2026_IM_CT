
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin;

import org.tio.http.common.HttpConfig;
import org.tio.sitexxx.service.vo.Const;

/**
 * @author tanyaowu
 *
 */
public class ThirdLoginUtils {

	/**
	 * 
	 */
	public ThirdLoginUtils() {

	}

	/**
	 * 
	 * @param httpConfig
	 * @param type
	 * @return
	 */
	public static String getCallbackUrl(HttpConfig httpConfig, Integer type) {
		return Const.SITE + httpConfig.getContextPath() + "/tlogin/cb/p/" + type + httpConfig.getSuffix();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
