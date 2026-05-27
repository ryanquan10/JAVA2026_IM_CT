
package org.tio.mg.im.server.ws;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.intf.TioUuid;
import org.tio.server.TioServerConfig;
import org.tio.mg.im.server.PacketDispatcher;
import org.tio.utils.Threads;
import org.tio.websocket.server.WsServerConfig;
import org.tio.websocket.server.WsServerStarter;

/**
 * @author tanyaowu
 * 2016年6月28日 下午5:34:04
 */
public class ImWsStarter {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImWsStarter.class);

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//		ImWsStarter imWsStarter = new ImWsStarter(9321);
		//		imWsStarter.start();
	}

	private WsServerStarter wsServerStarter;
	private TioServerConfig tioServerConfig;

	/**
	 *
	 * @author tanyaowu
	 */
	public ImWsStarter(int port, TioUuid tioUuid, PacketDispatcher packetDispatcher) throws IOException {
		WsServerConfig wsServerConfig = new WsServerConfig(port);
		wsServerStarter = new WsServerStarter(wsServerConfig, new ImWsMsgHandler(packetDispatcher), tioUuid, Threads.getTioExecutor(), Threads.getGroupExecutor());
		tioServerConfig = wsServerStarter.getTioServerConfig();

		ImWsGroupListener imWsGroupListener = ImWsGroupListener.me;
		tioServerConfig.setGroupListener(imWsGroupListener);
		tioServerConfig.setTioServerListener(ImWsTioServerListener.me);

	}

	public WsServerStarter getWsServerStarter() {
		return wsServerStarter;
	}

	public void start() throws IOException {
		wsServerStarter.start();
	}

	/**
	 * @return the tioServerConfig
	 */
	public TioServerConfig getTioServerConfig() {
		return tioServerConfig;
	}

}
