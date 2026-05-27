/*
 * tjddxtyctipht本软件由成都量尧极知电子商务有限公司采购自杭州钛特云科技有限公司，成都量尧极知电子商务有限公司需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hpnvtw
 * grantinfo
 */
package com.anji.captcha.service.impl;

import org.tio.utils.cache.ICache;

import com.anji.captcha.service.CaptchaCacheService;

public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {
	private ICache cache = null;

	public CaptchaCacheServiceRedisImpl(ICache cache) {
		this.cache = cache;
	}

	@Override
	public void set(String key, String value, long expiresInSeconds) {
		cache.put(key, value);//.set(key, value, expiresInSeconds);
	}

	@Override
	public boolean exists(String key) {
		return cache.get(key) != null;//..exists(key);
	}

	@Override
	public void delete(String key) {
		cache.remove(key);//.delete(key);
	}

	@Override
	public String get(String key) {
		return cache.get(key, String.class);//.get(key);
	}

	@Override
	public String type() {
		return "redis";
	}
}
