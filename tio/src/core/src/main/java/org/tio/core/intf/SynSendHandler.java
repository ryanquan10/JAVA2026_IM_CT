/*
 * xwjbtyi本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动unsnkhricwpq
 */
package org.tio.core.intf;

import org.tio.core.ChannelContext;

/**
 * @author tanyaowu 2020年8月28日 下午6:36:56
 */
public interface SynSendHandler<T extends Packet> {
    /**
     * 正常收到响应消息
     * 
     * @param channelContext
     * @param initPacket
     * @param respPacket     可能是null（超时情况下是null）
     * @param timeout
     * @author tanyaowu
     */
    public void onResp(ChannelContext channelContext, T initPacket, T respPacket, long timeout);

    /**
     * 超时处理
     * 
     * @param channelContext
     * @param initPacket
     * @param timeout
     * @author tanyaowu
     */
    public void onTimeout(ChannelContext channelContext, T initPacket, long timeout);

}
