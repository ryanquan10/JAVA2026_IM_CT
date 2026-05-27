
package org.tio.sitexxx.service.cache;

/**
 * @author tanyaowu
 * 2016年8月16日 上午10:22:25
 */
public enum CacheType {

	REDIS(), CAFFEINE(), CAFFEINE_REDIS(),
	/**
	 * 根据情况来决定缓存类型
	 * 如果是单机，则是CAFFEINE
	 * 如果是集群，则是REDIS
	 */
	CASE_1_2(),
	/**
	 * 根据情况来决定缓存类型
	 * 如果是单机，则是CAFFEINE
	 * 如果是集群，则是CAFFEINE_REDIS
	 */
	CASE_1_12();

	private CacheType() {
	}
}
