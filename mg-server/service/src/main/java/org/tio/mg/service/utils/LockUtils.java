
package org.tio.mg.service.utils;

import java.io.Serializable;

import org.tio.mg.service.cache.CacheConfig;

/**
 * 本地锁工具类
 * 
 */
public class LockUtils {

	/**
	 * 锁前缀
	 * 
	 */
	public static interface LockPrefix {

		/**
		 * 用户基础信息
		 */
		String USER_BASE = "user.base.";

		/**
		 * IM服务
		 */
		String IM_SERVICE = "im.service.";

		/**
		 * 视频列表
		 */
		String VIDEO_LIST = "video.list.";

		/**
		 * 视频信息
		 */
		String VIDEO_INFO = "video.info.";

		/**
		 * 视频封面处理
		 */
		String VIDEO_COVER = "video.cover.";

	}

	/**
	 * 获取锁对象，用于synchronized(lockObj)
	 * @param key
	 * @return
	 * 
	 */
	public static Serializable getLocalLockObj(String key) {
		return org.tio.utils.lock.LockUtils.getLockObj(key);
	}

	/**
	 * 获取锁对象，用于synchronized(lockObj)
	 * @param key
	 * @param mylock
	 * @return
	 * 
	 * @date 2016年10月15日 下午4:32:16
	 */
	public static Serializable getLocalLockObj(String key, Object mylock) {
		return org.tio.utils.lock.LockUtils.getLockObj(key, mylock);
	}

	/**
	 * 获取锁对象，用于synchronized(lockObj)
	 * @param cacheConfig
	 * @param key
	 * @return
	 */
	public static Object getLocalLockObj(CacheConfig cacheConfig, Object key) {
		String keystr = cacheConfig.name() + "." + key;
		return getLocalLockObj(keystr);
	}

}
