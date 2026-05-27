/*
 * wpsytu本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tfehljnzpelrn
 */
package org.tio.utils.cache;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.hutool.StrUtil;

/**
 * @author tanyaowu 2018年10月21日 下午3:45:26
 */
public abstract class AbsCache implements ICache {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(AbsCache.class);

    protected String cacheName = null;

    private Long timeToLiveSeconds;

    private Long timeToIdleSeconds;

    /**
     * 
     * @author tanyaowu
     */
    public AbsCache(String cacheName) {
	if (StrUtil.isBlank(cacheName)) {
	    throw new RuntimeException("cacheName不允许为空");
	}
	this.setCacheName(cacheName);
    }

    public AbsCache(String cacheName, Long timeToLiveSeconds, Long timeToIdleSeconds) {
	if (StrUtil.isBlank(cacheName)) {
	    throw new RuntimeException("cacheName不允许为空");
	}
	this.setCacheName(cacheName);
	this.setTimeToLiveSeconds(timeToLiveSeconds);
	this.setTimeToIdleSeconds(timeToIdleSeconds);
    }

    public abstract Serializable _get(String key);

    /**
     * 根据key获取value
     * 
     * @param key
     * @return
     * @author tanyaowu
     */
    @Override
    public Serializable get(String key) {
	Serializable obj = _get(key);
	if (obj instanceof ICache.NullClass) {
	    return null;
	}
	return obj;
    }

    /**
     * 根据key获取value
     * 
     * @param key
     * @param clazz
     * @return
     * @author: tanyaowu
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
	return (T) get(key);
    }

    /**
     * @return the cacheName
     */
    @Override
    public String getCacheName() {
	return cacheName;
    }

    @Override
    public Long getTimeToIdleSeconds() {
	return timeToIdleSeconds;
    }

    @Override
    public Long getTimeToLiveSeconds() {
	return timeToLiveSeconds;
    }

    /**
     * @param cacheName the cacheName to set
     */
    public void setCacheName(String cacheName) {
	this.cacheName = cacheName;
    }

    public void setTimeToIdleSeconds(Long timeToIdleSeconds) {
	this.timeToIdleSeconds = timeToIdleSeconds;
    }

    public void setTimeToLiveSeconds(Long timeToLiveSeconds) {
	this.timeToLiveSeconds = timeToLiveSeconds;
    }

}
