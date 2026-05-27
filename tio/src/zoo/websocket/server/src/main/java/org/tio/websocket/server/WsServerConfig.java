/*
 * vmfbrrh本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fjpvdbed
 */
package org.tio.websocket.server;

import org.tio.http.common.HttpConfig;

/**
 * @author tanyaowu 2017年6月28日 下午2:42:59
 */
public class WsServerConfig extends HttpConfig {

    /**
     *
     * @author tanyaowu
     */
    public WsServerConfig(Integer bindPort) {
	super(bindPort, true);
    }

    public WsServerConfig(Integer bindPort, boolean useSession) {
	super(bindPort, useSession);
    }

}
