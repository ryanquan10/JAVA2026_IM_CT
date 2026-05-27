/*
 * jizslxn本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gwlics
 */
/**
 * 
 */
package org.tio.http.common.view;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;

/**
 * @author tanyaowu
 *
 */
public interface View {
    /**
     * 
     * @param path    请求的路径
     * @param request
     * @return
     */
    public HttpResponse render(String path, HttpRequest request);
}
