/*
 * wbokuv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动dibcdmapxsr
 */
package org.tio.websocket.common;

import java.util.concurrent.ThreadLocalRandom;

import org.tio.core.intf.TioUuid;
import org.tio.utils.hutool.Snowflake;

/**
 * @author tanyaowu 2017年6月5日 上午10:44:26
 */
public class WsTioUuid implements TioUuid {
    private Snowflake snowflake;

    public WsTioUuid() {
	snowflake = new Snowflake(ThreadLocalRandom.current().nextInt(1, 30),
		ThreadLocalRandom.current().nextInt(1, 30));
    }

    public WsTioUuid(long workerId, long datacenterId) {
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
