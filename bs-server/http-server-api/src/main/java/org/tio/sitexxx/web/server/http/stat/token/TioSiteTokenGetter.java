
package org.tio.sitexxx.web.server.http.stat.token;

import org.tio.http.server.stat.token.DefaultTokenGetter;

/**
 * @author tanyaowu
 */
public class TioSiteTokenGetter extends DefaultTokenGetter {

	public static TioSiteTokenGetter me = new TioSiteTokenGetter();

	/**
	 * 
	 */
	private TioSiteTokenGetter() {

	}

	//	@Override
	//	public String getToken(HttpRequest request) {
	//		HttpSession httpSession = request.getHttpSession();
	//		if (httpSession != null) {
	//			return httpSession.getId();
	//		}
	//		Cookie cookie = DefaultHttpRequestHandler.getSessionCookie(request, request.getHttpConfig());
	//		if (cookie != null) {
	//			return cookie.getValue();
	//		}
	//		return null;
	//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
