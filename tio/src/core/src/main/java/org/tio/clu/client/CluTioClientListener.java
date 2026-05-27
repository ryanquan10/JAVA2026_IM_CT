/*
 * ctphptbyli本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ncokuhvwmolv
 */
/*
 * ctphptbyli本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ncokuhvwmolv
 * grantinfo
 */
package org.tio.clu.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.intf.TioClientListener;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluConst;
import org.tio.clu.common.Command;
import org.tio.clu.common.bs.HandshakeReq;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.utils.crypto.Md5;

/**
 * @author tanyaowu
 *
 */
public class CluTioClientListener implements TioClientListener {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(CluTioClientListener.class);

    public static void main(String[] args) {
    }

    private CluClientStarter cluClientStarter = null;

    /**
     * @author tanyaowu 2016年12月16日 下午5:52:06
     *
     */
    public CluTioClientListener(CluClientStarter cluClientStarter) {
	this.cluClientStarter = cluClientStarter;
    }

    @SuppressWarnings("unused")
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
	if (!isConnected) {
	    // 没连上
	    return;
	}

	CluClientSessionContext cluSessionContext = new CluClientSessionContext();
	cluSessionContext.setCgId(cluClientStarter.getCgId());
	channelContext.setAttribute(CluConst.TIO_CLU_SESSION_KEY, cluSessionContext);
	ClientChannelContext clientChannelContext = (ClientChannelContext) channelContext;

	String token = channelContext.getId();
	int serverPort = clientChannelContext.getServerNode().getPort();
	String sign = Md5.getMD5(token + System.getProperty(CluConst.HANDSHAKE_KEY_KEY, org.tio.core.TioConfig.CLU_KEY));

	HandshakeReq handshakeReq = new HandshakeReq();
	handshakeReq.setToken(token);
	handshakeReq.setSign(sign);
	handshakeReq.setCgId(cluClientStarter.getCgId());

	Clu.send(clientChannelContext, Command.HandshakeReq, handshakeReq);

	return;
    }

    /**
     * 
     * @param channelContext
     * @param packet
     * @param packetSize
     * @author tanyaowu
     */
    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) {

    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

    }

    /**
     * @see org.tio.core.intf.TioListener#onBeforeSent(org.tio.core.ChannelContext,
     *      org.tio.core.intf.Packet, int)
     *
     * @param channelContext
     * @param packet
     * @author tanyaowu 2016年12月20日 上午11:41:27
     *
     */
    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) {

    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
	CluClientSessionContext cluClientSessionContext = CluClient.getCluClientSessionContext(channelContext);
	if (cluClientSessionContext != null) {
	    // cluClientSessionContext.setHandshaked(false);
	    cluClientSessionContext.clean();
	}
    }
}
