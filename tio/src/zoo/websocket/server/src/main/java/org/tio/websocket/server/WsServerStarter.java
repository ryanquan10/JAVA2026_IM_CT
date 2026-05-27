/*
 * djzeieoxioe本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动swcmhd
 */
package org.tio.websocket.server;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.intf.TioUuid;
import org.tio.server.TioServerConfig;
import org.tio.server.TioServer;
import org.tio.utils.Threads;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;
import org.tio.websocket.common.WsTioUuid;
import org.tio.websocket.server.handler.IWsMsgHandler;

/**
 *
 * @author tanyaowu 2017年7月30日 上午9:45:54
 */
public class WsServerStarter {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(WsServerStarter.class);
    private WsServerConfig wsServerConfig = null;
    private IWsMsgHandler wsMsgHandler = null;
    private WsTioServerHandler wsTioServerHandler = null;
    private WsTioServerListener wsTioServerListener = null;
    private TioServerConfig tioServerConfig = null;
    private TioServer tioServer = null;

    public WsServerStarter(int port, IWsMsgHandler wsMsgHandler) throws IOException {
	this(port, wsMsgHandler, null, null);
    }

    public WsServerStarter(int port, IWsMsgHandler wsMsgHandler, SynThreadPoolExecutor tioExecutor,
	    ThreadPoolExecutor groupExecutor) throws IOException {
	this(new WsServerConfig(port), wsMsgHandler, tioExecutor, groupExecutor);
    }

    public WsServerStarter(WsServerConfig wsServerConfig, IWsMsgHandler wsMsgHandler) throws IOException {
	this(wsServerConfig, wsMsgHandler, null, null);
    }

    public WsServerStarter(WsServerConfig wsServerConfig, IWsMsgHandler wsMsgHandler, SynThreadPoolExecutor tioExecutor,
	    ThreadPoolExecutor groupExecutor) throws IOException {
	this(wsServerConfig, wsMsgHandler, new WsTioUuid(), tioExecutor, groupExecutor);
    }

    public WsServerStarter(WsServerConfig wsServerConfig, IWsMsgHandler wsMsgHandler, TioUuid tioUuid,
	    SynThreadPoolExecutor tioExecutor, ThreadPoolExecutor groupExecutor) throws IOException {
	if (tioExecutor == null) {
	    tioExecutor = Threads.getTioExecutor();
	}

	if (groupExecutor == null) {
	    groupExecutor = Threads.getGroupExecutor();
	}

	this.wsServerConfig = wsServerConfig;
	this.wsMsgHandler = wsMsgHandler;
	wsTioServerHandler = new WsTioServerHandler(wsServerConfig, wsMsgHandler);
	wsTioServerListener = new WsTioServerListener();
	tioServerConfig = new TioServerConfig("Tio Websocket Server", wsTioServerHandler, wsTioServerListener,
		tioExecutor, groupExecutor);
	tioServerConfig.setHeartbeatTimeout(0);
	tioServerConfig.setTioUuid(tioUuid);
	tioServerConfig.setReadBufferSize(1024 * 30);
	tioServer = new TioServer(tioServerConfig);
    }

    /**
     * @return the tioServerConfig
     */
    public TioServerConfig getTioServerConfig() {
	return tioServerConfig;
    }

    public TioServer getTioServer() {
	return tioServer;
    }

    /**
     * @return the wsMsgHandler
     */
    public IWsMsgHandler getWsMsgHandler() {
	return wsMsgHandler;
    }

    /**
     * @return the wsTioServerHandler
     */
    public WsTioServerHandler getWsTioServerHandler() {
	return wsTioServerHandler;
    }

    /**
     * @return the wsTioServerListener
     */
    public WsTioServerListener getWsTioServerListener() {
	return wsTioServerListener;
    }

    /**
     * @return the wsServerConfig
     */
    public WsServerConfig getWsServerConfig() {
	return wsServerConfig;
    }

    public void start() throws IOException {
	tioServer.start(wsServerConfig.getBindIp(), wsServerConfig.getBindPort());

    }
}
