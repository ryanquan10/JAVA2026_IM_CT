/*
 * pujzli本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rywvhuoq
 */
package org.tio.core.udp;

import org.tio.core.Node;
import org.tio.core.udp.intf.UdpHandler;

/**
 * @author tanyaowu 2017年7月5日 下午3:53:04
 */
public class UdpServerConf extends UdpConf {
    private UdpHandler udpHandler;
    private int readBufferSize = 1024 * 1024;

    public UdpServerConf(int port, UdpHandler udpHandler, int timeout) {
	super(timeout);
	this.setUdpHandler(udpHandler);
	this.setServerNode(new Node(null, port));
    }

    public int getReadBufferSize() {
	return readBufferSize;
    }

    public UdpHandler getUdpHandler() {
	return udpHandler;
    }

    public void setReadBufferSize(int readBufferSize) {
	this.readBufferSize = readBufferSize;
    }

    public void setUdpHandler(UdpHandler udpHandler) {
	this.udpHandler = udpHandler;
    }
}
