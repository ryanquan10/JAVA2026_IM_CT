/*
 * lwwldflcigmb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jiezb
 */
package org.tio.core.intf;

import org.tio.core.ChannelContext;

/**
 * @author tanyaowu 2017年5月8日 下午1:14:08
 */
public interface PacketListener extends java.io.Serializable {
    /**
     *
     * @param channelContext
     * @param packet
     * @param isSentSuccess
     * @throws Exception
     * @author tanyaowu
     */
    void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception;

}
