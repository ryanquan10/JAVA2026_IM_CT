/*
 * wsyyax本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动dmfuqrrjsk
 */
package org.tio.core.intf;

import org.tio.core.ChannelContext;

/**
 * @author tanyaowu 2017年5月13日 下午10:35:05
 */
public interface GroupListener {
    /**
     * 绑定群组后回调该方法
     * 
     * @param channelContext
     * @param group
     * @throws Exception
     * @author tanyaowu
     */
    void onAfterBind(ChannelContext channelContext, String group) throws Exception;

    /**
     * 解绑群组后回调该方法
     * 
     * @param channelContext
     * @param group
     * @throws Exception
     * @author tanyaowu
     */
    void onAfterUnbind(ChannelContext channelContext, String group) throws Exception;
}
