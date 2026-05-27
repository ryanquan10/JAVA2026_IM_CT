/*
 * jyjjdpeztd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动swnlkirqd
 */
/**
 * 
 */
package org.tio.core;

import org.tio.core.intf.Packet;

/**
 * @author tanyaowu
 *
 */
public interface PacketConverter {
    /**
     * 
     * @param packet
     * @param channelContext 要发往的channelContext
     * @return
     * @author tanyaowu
     */
    public Packet convert(Packet packet, ChannelContext channelContext);
}
