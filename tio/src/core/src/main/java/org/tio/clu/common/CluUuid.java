/*
 * bqpoaaxsuiqbg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动wvkdubxrbtfxif
 */
/*
 * bqpoaaxsuiqbg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动wvkdubxrbtfxif
 * grantinfo
 */
package org.tio.clu.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.intf.TioUuid;

import cn.hutool.core.lang.Snowflake;

/**
 * @author tanyaowu 2016年6月5日 上午10:44:26
 */
public class CluUuid implements TioUuid {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(CluUuid.class);

    /**
     * @param args
     * @author tanyaowu
     */
    public static void main(String[] args) {

    }

    // private long workerId;
    // private long datacenterId;
    //
    private Snowflake snowflake;

    /**
     *
     * @author tanyaowu
     */
    public CluUuid(long workerId, long datacenterId) {
	snowflake = new Snowflake(workerId, datacenterId);
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public String uuid() {
	return snowflake.nextId() + "";
    }
}
