/*
 * ewhyeahsvawoit本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动evbyrqo
 */
package org.tio.utils.hutool;

/**
 * 日期时间单位，每个单位都是以毫秒为基数
 * 
 * @author Looly
 *
 */
public enum DateUnit {
    /** 一毫秒 */
    MS(1),
    /** 一秒的毫秒数 */
    SECOND(1000),
    /** 一分钟的毫秒数 */
    MINUTE(SECOND.getMillis() * 60),
    /** 一小时的毫秒数 */
    HOUR(MINUTE.getMillis() * 60),
    /** 一天的毫秒数 */
    DAY(HOUR.getMillis() * 24),
    /** 一周的毫秒数 */
    WEEK(DAY.getMillis() * 7);

    private long millis;

    DateUnit(long millis) {
	this.millis = millis;
    }

    /**
     * @return 单位对应的毫秒数
     */
    public long getMillis() {
	return this.millis;
    }
}
