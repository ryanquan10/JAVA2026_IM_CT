/*
 * hcxylkysdpzc本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fcwcxrnz
 */
/**
 * 
 */
package org.tio.http.server.stat.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.handler.DefaultHttpRequestHandler;

/**
 * @author tanyaowu
 */
public class DefaultTokenGetter implements TokenGetter {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(DefaultTokenGetter.class);

    public static DefaultTokenGetter me = new DefaultTokenGetter();

    /**
     * 
     */
    protected DefaultTokenGetter() {
    }

    @Override
    public String getToken(HttpRequest request) {
	// HttpSession httpSession = request.getHttpSession();
	// if (httpSession != null) {
	// return httpSession.getId();
	// }
	// Cookie cookie = DefaultHttpRequestHandler.getSessionCookie(request,
	// request.httpConfig);
	// if (cookie != null) {
	// log.error("token from cookie: {}", cookie.getValue());
	// return cookie.getValue();
	// }
	return DefaultHttpRequestHandler.getSessionId(request);
    }

}
