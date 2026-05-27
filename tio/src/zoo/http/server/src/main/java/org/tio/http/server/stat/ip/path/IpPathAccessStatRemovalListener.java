/*
 * mzfcsedspf本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动aifdjvi
 */
package org.tio.http.server.stat.ip.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.TioConfig;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * @author tanyaowu 2017年8月21日 下午1:32:32
 */
@SuppressWarnings("rawtypes")
public class IpPathAccessStatRemovalListener implements RemovalListener {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(IpPathAccessStatRemovalListener.class);

    private IpPathAccessStatListener ipPathAccessStatListener;

    private TioConfig tioConfig = null;

    /**
     * 
     * @author: tanyaowu
     */
    public IpPathAccessStatRemovalListener(TioConfig tioConfig, IpPathAccessStatListener ipPathAccessStatListener) {
	this.tioConfig = tioConfig;
	this.ipPathAccessStatListener = ipPathAccessStatListener;
    }

    // @Override
    // public void onRemoval(RemovalNotification notification) {
    // String ip = (String) notification.getKey();
    // IpAccessStat ipAccessStat = (IpAccessStat) notification.getValue();
    //
    // if (ipPathAccessStatListener != null) {
    // ipPathAccessStatListener.onExpired(tioConfig, ip, ipAccessStat);
    // }
    //
    // // log.info("ip数据统计[{}]\r\n{}", ip, Json.toFormatedJson(ipAccesspathStat));
    // }

    @Override
    public void onRemoval(Object key, Object value, RemovalCause cause) {
	String ip = (String) key;
	IpAccessStat ipAccessStat = (IpAccessStat) value;

	if (ipPathAccessStatListener != null) {
	    ipPathAccessStatListener.onExpired(tioConfig, ip, ipAccessStat);
	}

    }
}
