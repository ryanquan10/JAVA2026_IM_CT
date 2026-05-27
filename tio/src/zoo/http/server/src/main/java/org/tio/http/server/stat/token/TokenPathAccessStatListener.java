/*
 * hskraohzxtl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qrgfheozufalzn
 */
package org.tio.http.server.stat.token;

import org.tio.core.TioConfig;
import org.tio.http.common.HttpRequest;

public interface TokenPathAccessStatListener {

    /**
     * 
     * @param httpRequest
     * @param token
     * @param path
     * @param tokenAccessStat
     * @param tokenPathAccessStat
     * @author tanyaowu
     */
    public boolean onChanged(HttpRequest httpRequest, String token, String path, TokenAccessStat tokenAccessStat,
	    TokenPathAccessStat tokenPathAccessStat);

    /**
     * 
     * @param tioConfig
     * @param token
     * @param tokenAccessStat
     * @author tanyaowu
     */
    public void onExpired(TioConfig tioConfig, String token, TokenAccessStat tokenAccessStat);

}
