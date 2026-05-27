/*
 * zkyyqmpcgafynd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动secdcnul
 */
package org.tio.http.server.stat.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.TioConfig;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * @author tanyaowu 2017年8月21日 下午1:32:32
 */
@SuppressWarnings("rawtypes")
public class TokenPathAccessStatRemovalListener implements RemovalListener {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(TokenPathAccessStatRemovalListener.class);

    private TokenPathAccessStatListener tokenPathAccessStatListener;

    private TioConfig tioConfig = null;

    /**
     * 
     * @author: tanyaowu
     */
    public TokenPathAccessStatRemovalListener(TioConfig tioConfig,
	    TokenPathAccessStatListener tokenPathAccessStatListener) {
	this.tioConfig = tioConfig;
	this.tokenPathAccessStatListener = tokenPathAccessStatListener;
    }

    // @Override
    // public void onRemoval(RemovalNotification notification) {
    // String token = (String) notification.getKey();
    // TokenAccessStat tokenAccessStat = (TokenAccessStat) notification.getValue();
    //
    // if (tokenPathAccessStatListener != null) {
    // tokenPathAccessStatListener.onExpired(tioConfig, token, tokenAccessStat);
    // }
    //
    // // log.info("token数据统计[{}]\r\n{}", token,
    // Json.toFormatedJson(tokenAccesspathStat));
    // }

    @Override
    public void onRemoval(Object key, Object value, RemovalCause cause) {
	String token = (String) key;
	TokenAccessStat tokenAccessStat = (TokenAccessStat) value;

	if (tokenPathAccessStatListener != null) {
	    tokenPathAccessStatListener.onExpired(tioConfig, token, tokenAccessStat);
	}

    }
}
