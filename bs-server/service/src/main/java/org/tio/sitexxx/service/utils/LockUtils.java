
package org.tio.sitexxx.service.utils;

import java.io.Serializable;

import org.tio.sitexxx.service.cache.CacheConfig;

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

		/**
		 * 钱包开户
		 */
		String CHAT_WALLET_OPEN = "user.wallet.";

		/**
		 * 钱包提现
		 */
		String CHAT_WALLET_WITHHOLD = "user.wallet.withhold";

		/**
		 * 聊天会话-暂时未使用
		 */
		String CHAT_GROUP_CHATITEM_KEY_PREFIX = "group.chatitem.lock.";

		/**
		 * 聊天动态会话
		 */
		String CHAT_CHATITEM_META_KEY_PREFIX = "chatitem.meta.lock.";

		/**
		 * 聊天群信息-暂时未使用
		 */
		String CHAT_GROUP_INFO_KEY_PREFIX = "group.info.lock.";

		/**
		 * 聊天会话用户合并队列
		 */
		String CHAT_USER_QUEUE_KEY_PREFIX = "user.queue.lock.";

		/**
		 * 聊天索引
		 */
		String CHAT_INDEX_KEY_PREFIX = "index.lock.";

		/**
		 * 抢红包
		 */
		String CHAT_REDPACKET_GRAB = "redpacket.lock.grab.";

		/**
		 * 钱包同步
		 */
		String CHAT_WALLET_COIN = "wallet.coin.lock.";

		/**
		 * 获取红包信息的锁
		 */
		String CHAT_REDPACKET_GET_LOCK = "wallet.redpackt.get.lock.";

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
