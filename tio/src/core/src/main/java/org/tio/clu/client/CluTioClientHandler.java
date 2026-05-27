/*
 * ajvwtex本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动avgcnkhzcwmcgm
 */
/*
 * ajvwtex本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动avgcnkhzcwmcgm
 * grantinfo
 */
package org.tio.clu.client;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.intf.TioClientHandler;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.utils.ProtocolUtils;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;

/**
 * 
 */
public class CluTioClientHandler implements TioClientHandler {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(CluTioClientHandler.class);

    public final static CluPacket heartbeatPacket = new CluPacket(Command.HeartbeatReq);

    private PacketDispatcher packetDispatcher;
    private TioServerConfig bsTioServerConfig;

    /**
     * 
     * 
     * @author: tanyaowu
     */
    public CluTioClientHandler(PacketDispatcher packetDispatcher, TioServerConfig bsTioServerConfig) {
	this.packetDispatcher = packetDispatcher;
	this.bsTioServerConfig = bsTioServerConfig;
    }

    /**
     * @param buffer
     * @param limit
     * @param position
     * @param readableLength
     * @param channelContext
     * @return
     * @throws TioDecodeException
     * @author tanyaowu
     */
    @Override
    public CluPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
	    ChannelContext channelContext) throws TioDecodeException {
	return ProtocolUtils.decode(buffer, limit, position, readableLength, channelContext);
    }

    /**
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
	return ProtocolUtils.encode(packet, tioConfig, channelContext);
    }

    /**
     * @return the bsTioServerConfig
     */
    public TioServerConfig getBsTioServerConfig() {
	return bsTioServerConfig;
    }

    /**
     * 
     * @param packet
     * @param channelContext
     * @throws Exception
     * @author: tanyaowu
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
	CluPacket cluPacket = (CluPacket) packet;
	packetDispatcher.dispatch(cluPacket, (ClientChannelContext) channelContext);
    }

    /**
     * 
     * @return
     * @author: tanyaowu
     */
    @Override
    public CluPacket heartbeatPacket(ChannelContext channelContext) {
	return heartbeatPacket;
    }

}
