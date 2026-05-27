/*
 * ziccydgkidcv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动geghqhrl
 */
package org.tio.http.server.stat.ip.path;

import org.tio.core.TioConfig;
import org.tio.http.common.HttpRequest;

public interface IpPathAccessStatListener {

    /**
     * 
     * @param httpRequest
     * @param ip
     * @param path
     * @param ipAccessStat
     * @param ipPathAccessStat
     * @author tanyaowu
     */
    public boolean onChanged(HttpRequest httpRequest, String ip, String path, IpAccessStat ipAccessStat,
	    IpPathAccessStat ipPathAccessStat);

    /**
     * 
     * @param tioConfig
     * @param ip
     * @param ipAccessStat
     * @author tanyaowu
     */
    public void onExpired(TioConfig tioConfig, String ip, IpAccessStat ipAccessStat);

}
