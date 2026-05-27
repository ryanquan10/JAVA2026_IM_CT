/*
 * brjly本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ilxqpsayt
 */
/**
 * 
 */
package org.tio.http.server.stat;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;

/**
 * @author tanyaowu
 *
 */
public interface StatPathFilter {

    /**
     * 
     * @param path
     * @param request
     * @param response
     * @return true: 表示要统计， false: 不统计
     */
    public boolean filter(String path, HttpRequest request, HttpResponse response);
}
