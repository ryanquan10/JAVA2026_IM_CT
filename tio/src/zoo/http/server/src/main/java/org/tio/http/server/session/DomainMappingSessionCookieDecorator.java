/*
 * cebmzqrt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动cydfrdmwkvrob
 */
package org.tio.http.server.session;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.Cookie;
import org.tio.http.common.HttpRequest;
import org.tio.utils.hutool.ReUtil;
import org.tio.utils.hutool.StrUtil;

/**
 * @author tanyaowu 2017年10月11日 下午2:59:10
 */
public class DomainMappingSessionCookieDecorator implements SessionCookieDecorator {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(DomainMappingSessionCookieDecorator.class);

    public static void main(String[] args) {
	boolean ss = ReUtil.isMatch("(.)*(.tiocloud.com){1}", ".tiocloud.com");
	System.out.println(ss);

	ss = ReUtil.isMatch("(.)*(.tiocloud.com){1}", "www.tiocloud.com");
	System.out.println(ss);

	ss = ReUtil.isMatch("(.)*(.tiocloud.com){1}", "www.xx.tiocloud.com");
	System.out.println(ss);
    }

    /**
     * key: (.)*(.tiocloud.com){1} value : 替换原始domain的domain，譬如.tiocloud.com
     * 
     * 结果会把域名为www.tiocloud.com的cookie的域名替换成.tiocloud.com
     */
    private Map<String, String> domainMap = null;

    protected DomainMappingSessionCookieDecorator() {

    }

    /**
     * 
     * @author: tanyaowu
     */
    public DomainMappingSessionCookieDecorator(Map<String, String> domainMap) {
	this.domainMap = domainMap;
    }

    public void addMapping(String key, String value) {
	domainMap.put(key, value);
    }

    /**
     * @param sessionCookie
     * @author: tanyaowu
     */
    @Override
    public void decorate(Cookie sessionCookie, HttpRequest request, String domain) {
	Set<Entry<String, String>> set = domainMap.entrySet();
	String initDomain = sessionCookie.getDomain();
	for (Entry<String, String> entry : set) {
	    String key = entry.getKey();
	    String value = entry.getValue();
	    if (StrUtil.equalsIgnoreCase(key, initDomain) || ReUtil.isMatch(key, initDomain)) {
		sessionCookie.setDomain(value);
	    }
	}
    }

    public void removeMapping(String key) {
	domainMap.remove(key);
    }

}
