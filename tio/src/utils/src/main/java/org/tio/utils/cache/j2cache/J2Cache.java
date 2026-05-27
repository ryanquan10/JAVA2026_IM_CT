/*
 * lcvsfbhowsbv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qmaucs
 */
/**
 * 
 */
package org.tio.utils.cache.j2cache;

import java.io.Serializable;
import java.util.Collection;

import org.tio.utils.cache.AbsCache;

import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;

/**
 * 红薯家的j2cache
 * 
 * @author tanyaowu
 *
 */
public class J2Cache extends AbsCache {

    private static CacheChannel getChannel() {
	CacheChannel cache = net.oschina.j2cache.J2Cache.getChannel();
	return cache;
    }

    /**
     * 
     */
    public J2Cache(String cacheName) {
	super(cacheName);
    }

    @Override
    public Serializable _get(String key) {
	CacheChannel cache = getChannel();
	CacheObject cacheObject = cache.get(cacheName, key);
	if (cacheObject != null) {
	    return (Serializable) cacheObject.getValue();
	}
	return null;
    }

    @Override
    public void clear() {
	CacheChannel cache = getChannel();
	cache.clear(cacheName);
    }

    @Override
    public Collection<String> keys() {
	CacheChannel cache = getChannel();
	return cache.keys(cacheName);
    }

    @Override
    public void put(String key, Serializable value) {
	CacheChannel cache = getChannel();
	cache.set(cacheName, key, value);
    }

    @Override
    public void putTemporary(String key, Serializable value) {
	throw new RuntimeException("不支持防缓存穿透");
    }

    @Override
    public void remove(String key) {
	CacheChannel cache = getChannel();
	cache.evict(cacheName, key);
    }

    @Override
    public long ttl(String key) {
	throw new RuntimeException("不支持ttl");
    }

}
