/*
 * rbbwcgpajnch本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动szlptezy
 */
package org.tio.http.common.session.id.impl;

import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.id.ISessionIdGenerator;
import org.tio.utils.hutool.Snowflake;

/**
 * @author tanyaowu 2017年8月15日 上午10:58:22
 */
public class SnowflakeSessionIdGenerator implements ISessionIdGenerator {

    private Snowflake snowflake;

    // /**
    // *
    // * @author tanyaowu
    // */
    // public SnowflakeSessionIdGenerator() {
    // snowflake = new Snowflake(RandomUtil.randomInt(0, 31),
    // RandomUtil.randomInt(0, 31));
    // }

    /**
     *
     * @author tanyaowu
     */
    public SnowflakeSessionIdGenerator(int workerId, int datacenterId) {
	snowflake = new Snowflake(workerId, datacenterId);
    }

    public long nextId() {
	return snowflake.nextId();
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public String sessionId(HttpConfig httpConfig, HttpRequest request) {
	return String.valueOf(snowflake.nextId());
    }
}
