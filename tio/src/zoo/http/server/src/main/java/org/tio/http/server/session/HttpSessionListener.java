/*
 * qpzyijvfrz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动znxecns
 */
package org.tio.http.server.session;

import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.HttpSession;

/**
 * @author tanyaowu 2017年9月27日 下午1:46:20
 */
public interface HttpSessionListener {
    /**
     * 
     * @param request
     * @param session
     * @param httpConfig
     */
    public void doAfterCreated(HttpRequest request, HttpSession session, HttpConfig httpConfig);

}
