/*
 * rgqqfviypbo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动chzldolmegvcc
 */
package org.tio.core.ssl.facade;

import java.nio.ByteBuffer;

import org.tio.core.ssl.SslVo;

public interface ISSLListener {
    /**
     * 业务层通过这个方法把SSL解密后的数据进行业务解包
     * 
     * @param plainBuffer
     */
    public void onPlainData(ByteBuffer plainBuffer);

    /**
     * 业务层通过这个方法把SSL加密后的数据发出去
     * 
     * @param sslVo
     */
    public void onWrappedData(SslVo sslVo);
}
