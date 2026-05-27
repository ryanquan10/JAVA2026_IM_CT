
package org.tio.sitexxx.service.init;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.utils.jfinal.P;

import com.anji.captcha.service.impl.CaptchaCacheServiceRedisImpl;
import com.anji.captcha.service.impl.CaptchaServiceFactory;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

/**
 * @author tanyaowu
 * 2016年8月10日 下午1:43:50
 */
public class CacheInit {

	private static Logger log = LoggerFactory.getLogger(CacheInit.class);

	public static void init(boolean isFrontend) {
		Caches.init();

		CaptchaCacheServiceRedisImpl captchaCacheServiceRedisImpl = (CaptchaCacheServiceRedisImpl) CaptchaServiceFactory.cacheService.get("redis");
		if (captchaCacheServiceRedisImpl == null) {
			captchaCacheServiceRedisImpl = new CaptchaCacheServiceRedisImpl(Caches.getCache(CacheConfig.ANJI_CAPTCHA));
			CaptchaServiceFactory.cacheService.put("redis", captchaCacheServiceRedisImpl);
		}

		if (isFrontend) {
			clearCache();
		}

	}

	/**
	 * 
	 * @author lixinji
	 */
	public static void init() {
		init(true);
	}

	/**
	 * 开发模式下，是否清除其他缓存，非必需清除
	 * 
	 */
	private static void clearCache() {
		boolean devFlag = P.getBoolean("clear.cache.flag", false);

		if (devFlag) {
			log.info("{}:系统启动时，进行了缓存清除操作.", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
			Caches.getCache(CacheConfig.LOGINNAME_USER_1).clear();
			Caches.getCache(CacheConfig.USERID_USER_7).clear();
			Caches.getCache(CacheConfig.USERID_BASE).clear();

			Caches.getCache(CacheConfig.GROUP_CHAT_LOG).clear();
			Caches.getCache(CacheConfig.WX_GROUP_MSG).clear();
			Caches.getCache(CacheConfig.WX_FRIEND_MSG_CHAT_6).clear();

			Caches.getCache(CacheConfig.CHAT_GROUP_INDEX_4).clear();
			Caches.getCache(CacheConfig.CHAT_ITEMS_6).clear();
			Caches.getCache(CacheConfig.CHAT_USER_BLOCK_1).clear();
			Caches.getCache(CacheConfig.CHAT_USER_INDEX_2).clear();
			Caches.getCache(CacheConfig.WX_GROUP_CHAT_6).clear();
			Caches.getCache(CacheConfig.CHAT_GROUP_USER_LIST_3).clear();
			Caches.getCache(CacheConfig.WX_MAILLIST_2).clear();
			Caches.getCache(CacheConfig.WX_GROUP_USER_2).clear();

			Caches.getCache(CacheConfig.OPENID_USERTHIRD).clear();
			Caches.getCache(CacheConfig.UID_USERTHIRD).clear();
			Caches.getCache(CacheConfig.IP_IPINFO).clear();
		}
	}
}
