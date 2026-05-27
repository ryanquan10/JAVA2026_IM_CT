/*
 * cjtrm本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qzytvfbmpwqjs
 */
/**
 * 
 */
package org.tio.core.ssl;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author tanyaowu
 *
 */
public class SslVo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2582637215518609443L;

    private ByteBuffer byteBuffer = null;
    /**
     * List<Packet> or Packet
     */
    private Object obj = null;

    public SslVo() {
    }

    /**
     * 
     * @param byteBuffer
     * @param obj        List<Packet> or Packet
     */
    public SslVo(ByteBuffer byteBuffer, Object obj) {
	this.byteBuffer = byteBuffer;
	this.obj = obj;
    }

    public ByteBuffer getByteBuffer() {
	return byteBuffer;
    }

    public Object getObj() {
	return obj;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
	this.byteBuffer = byteBuffer;
    }

    @Override
    public String toString() {
	return "SslVo [byteBuffer=" + byteBuffer + ", obj=" + obj + "]";
    }

}
