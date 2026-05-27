/*
 * vujtcck本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zwhbglnkt
 */
/*
 * vujtcck本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zwhbglnkt
 * grantinfo
 */
package org.tio.clu.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.client.CluClient;
import org.tio.clu.client.handler.base.AbsCluClientHandler;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.CluSessionContext;
import org.tio.clu.common.Command;
import org.tio.clu.common.CommandHandler;
import org.tio.clu.common.bs.HandshakeResp;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;

/**
 *
 * @author tanyaowu 2016年5月9日 上午11:46:24
 */
@CommandHandler(Command.HandshakeResp)
public class HandshakeRespHandler extends AbsCluClientHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(HandshakeRespHandler.class);

	public HandshakeRespHandler() {

	}

	/**
	 * @param packet
	 * @param clientChannelContext
	 * @param bsTioServerConfig
	 * @param bsPacketClass
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass)
	        throws Exception {
		@SuppressWarnings("unused")
		HandshakeResp handshakeResp = Clu.getBodyObj(cluPacket, HandshakeResp.class);

		CluSessionContext cluSessionContext = Clu.getCluSessionContext(clientChannelContext);
		cluSessionContext.setHandshaked(true);

		if (bsTioServerConfig != null) {
			CluClient.updateBsNode(clientChannelContext, bsTioServerConfig);
			CluClient.initBindReq(bsTioServerConfig, clientChannelContext);
		}

		return;
	}

}
