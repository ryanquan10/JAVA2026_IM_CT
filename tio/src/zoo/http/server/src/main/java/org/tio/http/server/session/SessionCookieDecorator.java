/*
 * asjxijtum本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ptmonnzsttz
 */
package org.tio.http.server.session;

import org.tio.http.common.Cookie;
import org.tio.http.common.HttpRequest;

/**
 * @author tanyaowu 2017年10月11日 下午2:40:19
 */
public interface SessionCookieDecorator {
    /**
     * DefaultHttpRequestHandler根据host字段创建了用于session的cookie，用户可以通过本方法定制一下Cookie，
     * 譬如把cookie的域名由www.baidu.com改成.baidu.com
     * 
     * @param sessionCookie
     * @param request
     * @param domain
     */
    public void decorate(Cookie sessionCookie, HttpRequest request, String domain);
}
