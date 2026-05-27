/*
 * akxrvzdal本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tdhvtoonebauyx
 */
package org.tio.core.intf;

/**
 *
 * @author tanyaowu 2017年4月1日 上午9:34:42
 */
public class EncodedPacket extends Packet {

    private static final long serialVersionUID = 1014364783783749718L;
    private byte[] bytes;

    /**
     *
     *
     * @author tanyaowu
     *
     */
    public EncodedPacket(byte[] bytes) {
	this.bytes = bytes;
    }

    /**
     * @return the bytes
     */
    public byte[] getBytes() {
	return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
	this.bytes = bytes;
    }

}
