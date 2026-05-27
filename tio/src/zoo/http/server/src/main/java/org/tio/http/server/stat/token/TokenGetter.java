/*
 * hutrbgbywkpacd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动exdoxc
 */
/**
 * 
 */
package org.tio.http.server.stat.token;

import org.tio.http.common.HttpRequest;

/**
 * @author tanyaowu
 *
 */
public interface TokenGetter {

    /**
     * 根据HttpRequest对象获取业务上的token
     * 
     * @param request
     * @return
     */
    public String getToken(HttpRequest request);
}
