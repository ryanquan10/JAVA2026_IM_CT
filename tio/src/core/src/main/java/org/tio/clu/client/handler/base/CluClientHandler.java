/*
 * aggssyqfxd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动wwikhn
 */
/*
 * aggssyqfxd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动wwikhn
 * grantinfo
 */
package org.tio.clu.client.handler.base;

import org.tio.client.ClientChannelContext;
import org.tio.clu.common.CluPacket;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;

/**
 *
 * @author tanyaowu
 *
 */
public interface CluClientHandler {

    /**
     * 
     * @param packet
     * @param clientChannelContext
     * @param bsTioServerConfig
     * @param bsPacketClass
     * @throws Exception
     * @author tanyaowu
     */
    public void handler(CluPacket packet, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig,
	    Class<? extends Packet> bsPacketClass) throws Exception;
}
