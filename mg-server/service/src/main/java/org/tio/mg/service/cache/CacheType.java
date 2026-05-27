
package org.tio.mg.service.cache;

/**
 * @author tanyaowu
 * 2016年8月16日 上午10:22:25
 */
public enum CacheType {

	REDIS(), CAFFEINE(), CAFFEINE_REDIS();

	private CacheType() {
	}
}
