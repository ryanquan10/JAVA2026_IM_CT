/*
 * tlecllnx本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ntgrqgufnp
 */
/*
 * tlecllnx本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ntgrqgufnp
 * grantinfo
 */
package org.tio.clu.client.handler.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.common.CluPacket;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;

/**
 * 
 * @author tanyaowu
 */
public abstract class AbsCluClientHandler implements CluClientHandler {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(AbsCluClientHandler.class);

    /**
     * 
     * @param cluPacket
     * @param clientChannelContext
     * @param bsTioServerConfig
     * @param bsPacketClass
     * @throws Exception
     * @author tanyaowu
     */
    @Override
    public void handler(CluPacket cluPacket, ClientChannelContext clientChannelContext,
	    TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass) throws Exception {
	myHandler(cluPacket, clientChannelContext, bsTioServerConfig, bsPacketClass);
    }

    /**
     * 
     * @param cluPacket
     * @param clientChannelContext
     * @param bsTioServerConfig
     * @param bsPacketClass
     * @throws Exception
     * @author tanyaowu
     */
    public abstract void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext,
	    TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass) throws Exception;

}
