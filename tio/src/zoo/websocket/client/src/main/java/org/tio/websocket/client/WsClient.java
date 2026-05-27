/*
 * rivhyvmaaamttj本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动apojvnige
 */
package org.tio.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.TioClientConfig;
import org.tio.client.TioClient;
import org.tio.client.intf.TioClientHandler;
import org.tio.client.intf.TioClientListener;
import org.tio.websocket.client.config.WsClientConfig;
import org.tio.websocket.client.kit.ReflectKit;
import org.tio.websocket.client.kit.UriKit;

public class WsClient {
    private static Logger log = LoggerFactory.getLogger(WsClient.class);
    static TioClientHandler tioClientHandler = new WsTioClientHander();
    static TioClientListener tioListener = new WsTioClientListener();

    /**
     * To create a WsClient.
     *
     * @param uri The uri to connect
     * @return
     * @throws IOException
     */
    public static WsClient create(String uri) throws Exception {
	return create(uri, (Map<String, String>) null);
    }

    /**
     * To create a WsClient.
     *
     * @param uri                   The uri to connect
     * @param additionalHttpHeaders Additional headers added to the http package
     *                              sent to the server during the handshake
     * @return
     * @throws IOException
     */
    public static WsClient create(String uri, Map<String, String> additionalHttpHeaders) throws Exception {
	return new WsClient(uri, additionalHttpHeaders);
    }

    /**
     * To create a WsClient.
     *
     * @param uri                   The uri to connect
     * @param additionalHttpHeaders Additional headers added to the http package
     *                              sent to the server during the handshake
     * @param config                The config of client. If you change the value
     *                              later, you need to bear the possible
     *                              consequences.
     * @return
     * @throws IOException
     */
    public static WsClient create(String uri, Map<String, String> additionalHttpHeaders, WsClientConfig config)
	    throws Exception {
	WsClient client = new WsClient(uri, additionalHttpHeaders);
	client.config = config;
	return client;
    }

    /**
     * To create a WsClient.
     *
     * @param uri    The uri to connect
     * @param config The config of client. If you change the value later, you need
     *               to bear the possible consequences.
     * @return
     * @throws IOException
     */
    public static WsClient create(String uri, WsClientConfig config) throws Exception {
	return create(uri, null, config);
    }

    URI uri;
    String rawUri;
    TioClient tioClient;
    WsClientConfig config = new WsClientConfig();
    ClientChannelContext clientChannelContext;
    Map<String, String> additionalHttpHeaders;
    WebSocketImpl ws;
    TioClientConfig tioClientConfig;

    WsClient(String rawUri) throws Exception {
	this(rawUri, null);
    }

    WsClient(String rawUri, Map<String, String> additionalHttpHeaders) throws Exception {
	rawUri = rawUri.trim();
	if (!rawUri.matches(
		"wss?\\://((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))(\\:[0-9]+)?(/.*)?(\\?.*)?")) {
	    throw new Exception("Invalid uri of " + rawUri);
	}

	this.rawUri = rawUri;
	this.additionalHttpHeaders = additionalHttpHeaders;

	construct();
    }

    public void close() {
	if (ws != null) {
	    ws.close();
	    ws = null;
	    clientChannelContext = null;
	    tioClientConfig = null;
	    tioClient = null;
	}
    }

    /**
     * connect to server
     *
     * @return WebSocket
     * @throws Exception
     */
    public synchronized WebSocket connect() throws Exception {
	ws.connect();
	return ws;
    }

    void construct() throws Exception {
	uri = UriKit.parseURI(rawUri);
	int port = uri.getPort();
	if (port == -1) {
	    if (uri.getScheme().equals("ws")) {
		port = 80;
		log.info("No port specified, use the default: {}", port);
	    } else {
		port = 443;
	    }
	    try {
		ReflectKit.setField(uri, "port", port);
	    } catch (Exception ex) {
	    }
	}
	tioClientConfig = new TioClientConfig(tioClientHandler, tioListener, null);
	tioClientConfig.setHeartbeatTimeout(0);
	if (uri.getScheme().equals("ws")) {
	    tioClient = new TioClient(tioClientConfig);
	} else {
	    tioClientConfig.useSsl();
	    tioClient = new TioClient(tioClientConfig);
	}
	ws = new WebSocketImpl(this, additionalHttpHeaders);
    }

    public ClientChannelContext getClientChannelContext() {
	return clientChannelContext;
    }

    public WsClientConfig getConfig() {
	return config;
    }

    public String getRawUri() {
	return rawUri;
    }

    public TioClient getTioClient() {
	return tioClient;
    }

    public URI getUri() {
	return uri;
    }

    public WebSocket getWs() {
	return ws;
    }
}
