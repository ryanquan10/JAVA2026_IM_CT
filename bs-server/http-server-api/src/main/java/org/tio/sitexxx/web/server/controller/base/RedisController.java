
package org.tio.sitexxx.web.server.controller.base;

import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.redis.RedisCache;
import org.tio.utils.resp.Resp;

import cn.hutool.core.date.DateUtil;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/redis")
public class RedisController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(RedisController.class);

	/**
	 *
	 * @author tanyaowu
	 */
	public RedisController() {
	}

	/**
	 * 获取缓存过期时间(还有多久过期)
	 * @param request
	 * @param cacheName
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/getTtl")
	public Resp getTtl(HttpRequest request, String cacheName, String key) throws Exception {
		RedisCache redisCache = RedisCache.getCache(cacheName);
		if (redisCache == null) {
			return Resp.fail("cacheName【" + cacheName + "】不存在");
		}

		long remainTimeToLive = redisCache.ttl(key);

		if (remainTimeToLive == -1) {
			return Resp.fail("不会超时(-1)");
		} else if (remainTimeToLive == -2) {
			return Resp.fail("key不存在(-2)");
		} else {
			return Resp.ok(DateUtil.formatBetween(remainTimeToLive));
		}
	}

	@RequestPath(value = "/clean")
	public Resp clean(HttpRequest request, String cacheName) throws Exception {
		User currUser = WebUtils.currUser(request);
		if (currUser == null || !UserService.isSuper(currUser)) {
			return Resp.fail("你没资格清除redis缓存");
		}

		RedisCache redisCache = RedisCache.getCache(cacheName);
		if (redisCache == null) {
			RedissonClient redisson = RedisInit.get();
			RKeys keys = redisson.getKeys();
			keys.deleteByPatternAsync(RedisCache.keyPrefix(cacheName) + "*");
		} else {
			redisCache.clear();
		}

		return Resp.ok();
	}

	//	@RequestPath(value = "/keyCount")
	//	public Resp keyCount(HttpRequest request, String cacheName) throws Exception {
	//		User currUser = WebUtils.currUser(request);
	//		if (currUser == null || !UserService.isSuper(currUser.getLoginname())) {
	//			return Resp.fail("你没资格获取keyCount");
	//		}
	//		
	//		RedisCache distCache = RedisCache.getCache(cacheName);
	//		if (distCache == null) {
	//			return Resp.fail("cacheName【" + cacheName + "】不存在");
	//		}
	//		
	//		distCache.k
	//		distCache.clear();
	//
	//		return Resp.ok();
	//	}
}
