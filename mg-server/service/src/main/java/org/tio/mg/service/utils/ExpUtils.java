
package org.tio.mg.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpUtils {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ExpUtils.class);

	//	/**
	//	 * 缓存处理
	//	 * @param uid
	//	 * @param type
	//	 * @return
	//	 * 
	//	 */
	//	public static LevelRunnable getLevelRunnableByUidAndType(Integer uid,Short type) {
	//		ICache expRunnableCaches = Caches.getCache(CacheConfig.EXP_DEAL_RUNNABLE);
	//		String key = Math.abs(uid.hashCode() % Const.LEVEL_THREAD_MAX_SIZE) + "_" + type;
	//		LevelRunnable levelRunnable = expRunnableCaches.get(key,LevelRunnable.class);
	//		if(levelRunnable == null) {
	//			synchronized (log) {
	//				levelRunnable = expRunnableCaches.get(key,LevelRunnable.class);
	//				if(levelRunnable == null) {
	//					levelRunnable = new LevelRunnable(Threads.levelExecutor, type);
	//					expRunnableCaches.put(key, levelRunnable);
	//				}
	//			}
	//		}
	//		return levelRunnable;
	//	}

}
