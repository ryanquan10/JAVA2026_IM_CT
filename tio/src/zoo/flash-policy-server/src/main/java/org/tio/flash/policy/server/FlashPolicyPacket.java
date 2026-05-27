/*
 * obnwvcccwdot本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ttkfns
 */
package org.tio.flash.policy.server;

import org.tio.core.intf.Packet;

/**
 *
 * @author tanyaowu
 *
 */
public class FlashPolicyPacket extends Packet {
    private static final long serialVersionUID = -172060606924066412L;
    public static final int MIN_LENGHT = 22; // 消息最少的长度
    public static final int MAX_LING_LENGHT = 256; // 一行最大的长度

    public static final FlashPolicyPacket REQUEST = new FlashPolicyPacket();

    public static final FlashPolicyPacket RESPONSE = new FlashPolicyPacket();

    private FlashPolicyPacket() {
    }

}
