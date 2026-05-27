/*
 * wvawvnvy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动elwhurfgepfte
 */
/**
 * 
 */
package org.tio.http.server.intf;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.RequestLine;

/**
 * @author tanyaowu
 */
public interface ThrowableHandler {

    /**
     * 
     * @param request
     * @param requestLine
     * @param throwable
     * @return
     * @throws Exception
     */
    public HttpResponse handler(HttpRequest request, RequestLine requestLine, Throwable throwable) throws Exception;
}
