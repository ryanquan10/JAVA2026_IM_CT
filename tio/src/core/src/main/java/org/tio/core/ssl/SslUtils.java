/*
 * brajl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动lmjlgf
 */
/**
 * 
 */
package org.tio.core.ssl;

import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;

import cn.hutool.core.util.BooleanUtil;

/**
 * @author tanyaowu
 *
 */
public class SslUtils {

    /**
     * 是否是SSL连接
     * 
     * @param tioConfig
     * @return
     */
    public static boolean isSsl(TioConfig tioConfig) {
	return tioConfig.isSsl();
    }

    /**
     * 是否需要对这个packet进行SSL加密
     * 
     * @param packet
     * @param tioConfig
     * @return
     */
    public static boolean needSslEncrypt(Packet packet, TioConfig tioConfig) {
	if (!BooleanUtil.isTrue(packet.isSslEncrypted()) && tioConfig.sslConfig != null) {
	    return true;
	}
	return false;
    }

    /**
     * 
     */
    private SslUtils() {

    }

}
