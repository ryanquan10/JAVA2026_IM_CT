/*
 * gaaubvdekdxprn本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动bddxm
 */
package org.tio.core.udp;

import org.tio.core.Node;

/**
 * @author tanyaowu 2017年7月5日 下午3:53:20
 */
public class UdpClientConf extends UdpConf {
    /**
     *
     * @author tanyaowu
     */
    public UdpClientConf(String serverip, int serverport, int timeout) {
	super(timeout);
	Node node = new Node(serverip, serverport);
	this.setServerNode(node);
	this.setTimeout(timeout);
    }

}
