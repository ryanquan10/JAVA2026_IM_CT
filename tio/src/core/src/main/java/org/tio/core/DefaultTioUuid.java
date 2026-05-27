/*
 * audlusru本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动srnpknaanzbgo
 */
package org.tio.core;

import org.tio.core.intf.TioUuid;

/**
 * @author tanyaowu 2017年6月5日 上午10:31:40
 */
public class DefaultTioUuid implements TioUuid {

    public static final DefaultTioUuid me = new DefaultTioUuid();

    /**
     *
     * @author tanyaowu
     */
    public DefaultTioUuid() {
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public String uuid() {
	return java.util.UUID.randomUUID().toString();
    }
}
