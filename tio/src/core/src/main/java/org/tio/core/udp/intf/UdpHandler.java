/*
 * lzggsjy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zhjhpwktqhuhhb
 */
package org.tio.core.udp.intf;

import java.net.DatagramSocket;

import org.tio.core.udp.UdpPacket;

/**
 * @author tanyaowu 2017年7月5日 下午2:46:47
 */
public interface UdpHandler {

    /**
     *
     * @param udpPacket
     * @param datagramSocket
     * @author tanyaowu
     */
    public void handler(UdpPacket udpPacket, DatagramSocket datagramSocket);
}
