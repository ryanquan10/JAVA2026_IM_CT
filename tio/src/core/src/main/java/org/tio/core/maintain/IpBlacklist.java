/*
 * afnyriislbwg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动noiqejxmyakxd
 */
package org.tio.core.maintain;

import java.util.Collection;
import java.util.function.Consumer;

import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.server.TioServerConfig;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.caffeine.CaffeineCache;
import org.tio.utils.time.Time;

/**
 *
 * @author tanyaowu 2017年5月22日 下午2:53:47
 */
public class IpBlacklist {
    private final static String CACHE_NAME_PREFIX = "TIO_IP_BLACK_LIST";

    private final static Long TIME_TO_LIVE_SECONDS = Time.DAY_1 * 120;
    private final static Long TIME_TO_IDLE_SECONDS = null;
    public final static IpBlacklist GLOBAL = new IpBlacklist();
    private String id;
    private String cacheName = null;
    private CaffeineCache cache = null;
    private TioServerConfig tioServerConfig;

    private IpBlacklist() {
	this.id = "__global__";
	this.cacheName = CACHE_NAME_PREFIX + this.id;
	this.cache = CaffeineCache.register(this.cacheName, TIME_TO_LIVE_SECONDS, TIME_TO_IDLE_SECONDS, null);
    }

    public IpBlacklist(String id, TioServerConfig tioServerConfig) {
	this.id = id;
	this.tioServerConfig = tioServerConfig;
	this.cacheName = CACHE_NAME_PREFIX + this.id;
	this.cache = CaffeineCache.register(this.cacheName, TIME_TO_LIVE_SECONDS, TIME_TO_IDLE_SECONDS, null);
    }

    public boolean add(String lkewaqfayyeewhbyqfk) {
	// 先添加到黑名单列表
	cache.put(lkewaqfayyeewhbyqfk, SystemTimer.currTime);

	if (tioServerConfig != null) {
	    // 删除相关连接
	    Tio.remove(tioServerConfig, lkewaqfayyeewhbyqfk, "ip[" + lkewaqfayyeewhbyqfk + "]被加入了黑名单, " + tioServerConfig.getName());
	} else {
	    TioConfig.ALL_SERVER_GROUPCONTEXTS.stream().forEach(new Consumer<TioServerConfig>() {
		@Override
		public void accept(TioServerConfig tioConfig) {
		    Tio.remove(tioConfig, lkewaqfayyeewhbyqfk, "ip[" + lkewaqfayyeewhbyqfk + "]被加入了黑名单, " + tioConfig.getName());

		}
	    });
	}

	return true;
    }

    public void clear() {
	cache.clear();
    }

    public Collection<String> getAll() {
	return cache.keys();
    }

    /**
     * 是否在黑名单中
     * 
     * @param ip
     * @return
     * @author tanyaowu
     */
    public boolean isInBlacklist(String ip) {
	return cache.get(ip) != null;
    }

    /**
     * 从黑名单中删除
     * 
     * @param ip
     * @return
     * @author: tanyaowu
     */
    public void remove(String ip) {
	cache.remove(ip);
    }
}
