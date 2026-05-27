
package org.tio.sitexxx.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.ICache;
import org.tio.utils.cache.caffeine.CaffeineCache;
import org.tio.utils.cache.caffeineredis.CaffeineRedisCache;
import org.tio.utils.cache.redis.RedisCache;

/**
 * @author tanyaowu
 * 2016年8月16日 上午11:35:49
 */
public class Caches {
	private static Logger log = LoggerFactory.getLogger(Caches.class);

	/**
	 * 涉及到大改动时，把这个值改一下，相当于清了一遍缓存
	 */
	private static final String CACHE_PREFIX = "chat200301";

	/**
	 * 当缓存是单值时，用这个作为key值
	 */
	public static final String SINGLE_KEY = "y";

	public static ICache getCache(CacheConfig cacheConfig) {
		String cacheName = CACHE_PREFIX + cacheConfig.getCacheName();

		CacheType cacheType = cacheConfig.getCacheType();

		ICache ret = null;

		switch (cacheType) {
		case REDIS:
			ret = RedisCache.getCache(cacheName);
			break;
		case CAFFEINE_REDIS:
			ret = CaffeineRedisCache.getCache(cacheName);
			break;
		case CAFFEINE:
			ret = CaffeineCache.getCache(cacheName);
			break;
		case CASE_1_2:
			if (!Const.USE_TIO_CLU) {
				ret = CaffeineCache.getCache(cacheName);
			} else {
				ret = RedisCache.getCache(cacheName);
			}
			break;
		case CASE_1_12:
			if (!Const.USE_TIO_CLU) {
				ret = CaffeineCache.getCache(cacheName);
			} else {
				ret = CaffeineRedisCache.getCache(cacheName);
			}
			break;

		default:
			break;
		}

		if (ret == null) {
			log.error("cacheName[{}]还没注册", cacheName);
		}
		return ret;
	}

	public static void init() {
		CacheConfig[] values = CacheConfig.values();
		for (CacheConfig cacheConfig : values) {
			String cacheName = CACHE_PREFIX + cacheConfig.getCacheName();
			Long timeToLiveSeconds = cacheConfig.getTimeToLiveSeconds();
			Long timeToIdleSeconds = cacheConfig.getTimeToIdleSeconds();
			CacheType cacheType = cacheConfig.getCacheType();

			if (cacheType == CacheType.REDIS) {
				RedisCache.register(RedisInit.get(), cacheName, timeToLiveSeconds, timeToIdleSeconds);
			} else if (cacheType == CacheType.CAFFEINE_REDIS) {
				CaffeineRedisCache.register(RedisInit.get(), cacheName, timeToLiveSeconds, timeToIdleSeconds);
			} else if (cacheType == CacheType.CAFFEINE) {
				CaffeineCache.register(cacheName, timeToLiveSeconds, timeToIdleSeconds);
			} else if (cacheType == CacheType.CASE_1_2) {
				if (!Const.USE_TIO_CLU) {
					CaffeineCache.register(cacheName, timeToLiveSeconds, timeToIdleSeconds);
				} else {
					RedisCache.register(RedisInit.get(), cacheName, timeToLiveSeconds, timeToIdleSeconds);
				}
			} else if (cacheType == CacheType.CASE_1_12) {
				if (!Const.USE_TIO_CLU) {
					CaffeineCache.register(cacheName, timeToLiveSeconds, timeToIdleSeconds);
				} else {
					CaffeineRedisCache.register(RedisInit.get(), cacheName, timeToLiveSeconds, timeToIdleSeconds);
				}
			}
		}
	}
}
