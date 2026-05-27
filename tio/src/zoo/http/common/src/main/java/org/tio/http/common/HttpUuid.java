/*
 * qpnzksqm本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gxskvsdznzmjcj
 */
package org.tio.http.common;

import java.util.concurrent.atomic.AtomicLong;

import org.tio.core.intf.TioUuid;

/**
 * @author tanyaowu 2017年6月5日 上午10:44:26
 */
public class HttpUuid implements TioUuid {
    // private static Logger log = LoggerFactory.getLogger(HttpUuid.class);

    private static java.util.concurrent.atomic.AtomicLong seq = new AtomicLong();

    /**
     *
     * @author tanyaowu
     */
    public HttpUuid() {
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public String uuid() {
	return seq.incrementAndGet() + "";
    }
}
