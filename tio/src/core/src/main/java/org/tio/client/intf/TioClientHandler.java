/*
 * lfruswwjzcts本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动lbvjbfcxhrloq
 */
package org.tio.client.intf;

import org.tio.core.ChannelContext;
import org.tio.core.intf.TioHandler;
import org.tio.core.intf.Packet;

/**
 *
 * @author tanyaowu 2017年4月1日 上午9:14:24
 */
public interface TioClientHandler extends TioHandler {
    /**
     * 创建心跳包
     * 
     * @return
     * @author tanyaowu
     */
    Packet heartbeatPacket(ChannelContext channelContext);
}
