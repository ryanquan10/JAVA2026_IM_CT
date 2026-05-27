
/**
 * 
 */
package org.tio.sitexxx.service.topic;

import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.vo.ClearHttpCache;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.cache.CacheChangeType;
import org.tio.utils.cache.CacheChangedVo;
import org.tio.utils.json.Json;

/**
 * @author tanyaowu
 *
 */
public class Topics {
	private static Logger log = LoggerFactory.getLogger(Topics.class);

	public Topics() {
	}

	/**
	 * 通知清空或删除http缓存
	 * @param path 形如：/user/get
	 * @param uid 对应缓存中的currUserid
	 * @param param 参数值(不用关心顺序)
	 * @param clearType org.tio.sitexxx.service.vo.ClearHttpCache.ClearType
	 */
	public static void notifyRemoveHttpCache(String path, Integer uid, Map<String, Object> param, int clearType) {
		//通知清除httpcache
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Topic.CLEAR_HTTP_CACHE);
		ClearHttpCache clearHttpCache = new ClearHttpCache();
		clearHttpCache.setPath(path);
		clearHttpCache.setParam(param);
		clearHttpCache.setUserid(uid);
		clearHttpCache.setClearType(clearType);
		topic.publish(clearHttpCache);
		if (log.isInfoEnabled()) {
			log.info("发出通知，准备清除/删除httpcache[{}], param:{}, uid:{}", path, Json.toJson(param), uid);
		}
	}

	/**
	 * 通知删除key为指定值的本地缓存
	 * @param cacheName
	 * @param key
	 * @author tanyaowu
	 */
	public static void notifyRemoveLocalCache(String cacheName, String key) {
		CacheChangedVo cacheChangedVo = new CacheChangedVo(cacheName, key, CacheChangeType.REMOVE);
		org.tio.utils.cache.caffeineredis.CaffeineRedisCache.topic.publish(cacheChangedVo);
	}

	/**
	 * 通知清除本地缓存
	 * @param cacheName
	 * @author tanyaowu
	 */
	public static void notifyClearLocalCache(String cacheName) {
		CacheChangedVo cacheChangedVo = new CacheChangedVo(cacheName, CacheChangeType.CLEAR);
		org.tio.utils.cache.caffeineredis.CaffeineRedisCache.topic.publish(cacheChangedVo);
	}

	/**
	 * 通知各节点设置系统参数
	 */
	public static void notifySetSystemParam(String key, String value) {
		Map<String, Object> params = new HashMap<>();
		params.put(key, value);

		RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setParams(params);
		topicVo.setType(TopicVo.Type.SET_SYSTEM_PARAM);
		topic.publishAsync(topicVo);
	}

}
