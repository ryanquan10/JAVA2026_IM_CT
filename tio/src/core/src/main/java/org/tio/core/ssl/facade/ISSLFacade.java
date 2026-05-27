/*
 * glosyirfzbwvjo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动cjcworlqvastu
 */
package org.tio.core.ssl.facade;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLException;

import org.tio.core.ssl.SslVo;

public interface ISSLFacade {
    /**
     * 开始握手
     * 
     * @throws IOException
     */
    void beginHandshake() throws IOException;

    void close();

    /**
     * 解密
     * 
     * @param byteBuffer
     * @throws SSLException
     */
    void decrypt(ByteBuffer byteBuffer) throws SSLException;

    /**
     * 加密
     * 
     * @param sslVo
     * @throws SSLException
     */
    void encrypt(SslVo sslVo) throws SSLException;

    boolean isClientMode();

    boolean isCloseCompleted();

    /**
     * SSL握手是否已经完成
     * 
     * @return
     */
    boolean isHandshakeCompleted();

    void setCloseListener(ISessionClosedListener l);

    void setHandshakeCompletedListener(IHandshakeCompletedListener hcl);

    void setSSLListener(ISSLListener l);

    void terminate();
}
