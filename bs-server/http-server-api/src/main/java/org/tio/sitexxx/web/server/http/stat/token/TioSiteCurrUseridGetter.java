
/**
 * 
 */
package org.tio.sitexxx.web.server.http.stat.token;

import org.tio.http.common.HttpRequest;
import org.tio.http.server.intf.CurrUseridGetter;
import org.tio.sitexxx.web.server.utils.WebUtils;

/**
 * @author tanyaowu
 *
 */
public class TioSiteCurrUseridGetter implements CurrUseridGetter {

	public static TioSiteCurrUseridGetter me = new TioSiteCurrUseridGetter();

	/**
	 * 
	 */
	private TioSiteCurrUseridGetter() {
	}

	@Override
	public String getUserid(HttpRequest request) {
		Integer ret = WebUtils.currUserId(request);
		if (ret != null) {
			return ret + "";
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
