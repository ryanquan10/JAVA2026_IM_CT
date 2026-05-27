/*
 * letqv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动njxtrrfgm
 */
package org.tio.server.intf;

import org.tio.core.ChannelContext;
import org.tio.core.intf.TioListener;

/**
 *
 * @author tanyaowu
 *
 */
public interface TioServerListener extends TioListener {

    /**
     * 建立连接后触发的方法
     * 
     * @param asynchronousSocketChannel
     * @param tioServer
     * @return false: 表示拒绝这个连接, true: 表示接受这个连接
     *
     * @author tanyaowu 2016年12月20日 上午10:10:56
     *
     */
    // void onAfterAccepted(AsynchronousSocketChannel asynchronousSocketChannel,
    // TioServer tioServer);

    /**
     * 
     * 服务器检查到心跳超时时，会调用这个函数（一般场景，该方法只需要直接返回false即可）
     * 
     * @param channelContext
     * @param interval              已经多久没有收发消息了，单位：毫秒
     * @param heartbeatTimeoutCount 心跳超时次数，第一次超时此值是1，以此类推。此值被保存在：channelContext.stat.heartbeatTimeoutCount
     * @return 返回true，那么服务器则不关闭此连接；返回false，服务器将按心跳超时关闭该连接
     */
    public boolean onHeartbeatTimeout(ChannelContext channelContext, Long interval, int heartbeatTimeoutCount);
}
