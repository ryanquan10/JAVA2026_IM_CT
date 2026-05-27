/*
 * zjdemh本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xnqjjodgon
 */
package org.tio.http.server.session;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.Cookie;
import org.tio.http.common.HttpRequest;

/**
 * @author tanyaowu 2017年10月11日 下午2:59:10
 */
public class DomainSessionCookieDecorator implements SessionCookieDecorator {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(DomainSessionCookieDecorator.class);

    /**
     * 形如:".baidu.com"
     */
    private String domain;

    private DomainMappingSessionCookieDecorator domainMappingSessionCookieDecorator;

    /**
     * 
     * @param domain 形如:".baidu.com"
     * @author: tanyaowu
     */
    public DomainSessionCookieDecorator(String domain) {
	this.domain = domain;

	Map<String, String> domainMap = new HashMap<>();
	domainMap.put("(.)*(" + domain + "){1}", domain);

	domainMappingSessionCookieDecorator = new DomainMappingSessionCookieDecorator(domainMap);
    }

    /**
     * @param sessionCookie
     * @author: tanyaowu
     */
    @Override
    public void decorate(Cookie sessionCookie, HttpRequest request, String domain) {
	domainMappingSessionCookieDecorator.decorate(sessionCookie, request, domain);
    }

    public String getDomain() {
	return domain;
    }

    public void setDomain(String domain) {
	this.domain = domain;
    }
}
