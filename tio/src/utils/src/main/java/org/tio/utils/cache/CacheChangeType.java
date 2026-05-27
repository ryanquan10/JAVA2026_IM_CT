/*
 * qalrtbpj本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动agpia
 */
package org.tio.utils.cache;

import java.util.Objects;

/**
 *
 * @author tanyaowu 2017年8月12日 下午9:33:02
 */
public enum CacheChangeType {
    /**
     * key级别清空本地缓存
     */
    REMOVE(1),
    /**
     * key级别清空本地缓存
     */
    UPDATE(2),
    /**
     * key级别清空本地缓存
     */
    PUT(3),
    /**
     * cacheName级别清空本地缓存
     */
    CLEAR(4);

    public static CacheChangeType from(Integer method) {
	CacheChangeType[] values = CacheChangeType.values();
	for (CacheChangeType v : values) {
	    if (Objects.equals(v.value, method)) {
		return v;
	    }
	}
	return null;
    }

    Integer value;

    private CacheChangeType(Integer value) {
	this.value = value;
    }
}
