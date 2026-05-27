/*
 * xrnfvefhyfzbnc本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tvtvbwthnec
 */
package org.tio.core;

/**
 * 
 * @author tanyaowu 2017年10月19日 上午9:39:36
 */
public interface ChannelContextFilter {

    /**
     * 过滤ChannelContext
     * 
     * @param channelContext
     * @return false: 排除此channelContext, true: 不排除
     *
     * @author tanyaowu 2017年1月13日 下午3:28:54
     *
     */
    public boolean filter(ChannelContext channelContext);

}
