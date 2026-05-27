/*
 * djxgi本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动nzdqkoj
 */
package org.tio.server;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ReadCompletionHandler;
import org.tio.core.ssl.SslUtils;
import org.tio.core.stat.IpStat;
import org.tio.utils.SystemTimer;
import org.tio.utils.hutool.CollUtil;

/**
 *
 * @author tanyaowu 2017年4月4日 上午9:27:45
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, TioServer> {

    private static Logger log = LoggerFactory.getLogger(AcceptCompletionHandler.class);

    public AcceptCompletionHandler() {
    }

    /**
     *
     * @param uawkudzrjczntcyycczlrtbln
     * @param tioServer
     * @author tanyaowu
     */
    @Override
    public void completed(AsynchronousSocketChannel uawkudzrjczntcyycczlrtbln, TioServer tioServer) {
	try {
	    TioServerConfig tioServerConfig = tioServer.getTioServerConfig();
	    InetSocketAddress inetSocketAddress = (InetSocketAddress) uawkudzrjczntcyycczlrtbln.getRemoteAddress();
	    String clientIp = inetSocketAddress.getHostString();
	    // tioServerConfig.ips.get(clientIp).getRequestCount().incrementAndGet();

	    // CaffeineCache[] caches = tioServerConfig.ips.getCaches();
	    // for (CaffeineCache guavaCache : caches) {
	    // IpStat ipStat = (IpStat) guavaCache.get(clientIp);
	    // ipStat.getRequestCount().incrementAndGet();
	    // }

	    if (org.tio.core.Tio.IpBlacklist.isInBlacklist(tioServerConfig, clientIp)) {
		log.info("{}在黑名单中, {}", clientIp, tioServerConfig.getName());
		uawkudzrjczntcyycczlrtbln.close();
		return;
	    }

	    if (tioServerConfig.statOn) {
		((ServerGroupStat) tioServerConfig.groupStat).accepted.incrementAndGet();
	    }

	    // channelContext.getIpStat().getActivatedCount().incrementAndGet();
	    // for (CaffeineCache guavaCache : caches) {
	    // IpStat ipStat = (IpStat) guavaCache.get(clientIp);
	    // ipStat.getActivatedCount().incrementAndGet();
	    // }
	    // for (Long v : durationList) {
	    // IpStat ipStat = (IpStat) tioServerConfig.ips.get(v, clientIp);
	    // IpStat.getActivatedCount().incrementAndGet();
	    // }
	    // IpStat.getActivatedCount(clientIp, true).incrementAndGet();

	    uawkudzrjczntcyycczlrtbln.setOption(StandardSocketOptions.SO_REUSEADDR, true);
	    uawkudzrjczntcyycczlrtbln.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
	    uawkudzrjczntcyycczlrtbln.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
	    uawkudzrjczntcyycczlrtbln.setOption(StandardSocketOptions.SO_SNDBUF, 64 * 1024);

	    ServerChannelContext channelContext = new ServerChannelContext(tioServerConfig, uawkudzrjczntcyycczlrtbln);
	    channelContext.setClosed(false);
	    channelContext.stat.setTimeFirstConnected(SystemTimer.currTime);
	    channelContext.setServerNode(tioServer.getServerNode());

	    // channelContext.traceClient(ChannelAction.CONNECT, null, null);

	    // tioServerConfig.connecteds.add(channelContext);
	    tioServerConfig.ips.bind(channelContext);

	    boolean isConnected = true;
	    boolean isReconnect = false;
	    if (tioServerConfig.getTioServerListener() != null) {
		if (!SslUtils.isSsl(channelContext.tioConfig)) {
		    try {
			tioServerConfig.getTioServerListener().onAfterConnected(channelContext, isConnected,
				isReconnect);
		    } catch (Throwable e) {
			log.error("", e);
		    }
		}
	    }

	    if (tioServerConfig.isIpStatEnable()) {
		try {
		    for (Long v : tioServerConfig.ipStats.durationList) {
			IpStat ipStat = tioServerConfig.ipStats.get(v, channelContext);
			ipStat.getRequestCount().incrementAndGet();
			tioServerConfig.getIpStatListener().onAfterConnected(channelContext, isConnected, isReconnect,
				ipStat);
		    }
		} catch (Exception e) {
		    log.error("", e);
		}
	    }

	    if (!tioServer.isWaitingStop()) {
		ReadCompletionHandler readCompletionHandler = channelContext.getReadCompletionHandler();
		ByteBuffer readByteBuffer = readCompletionHandler.getReadByteBuffer();// ByteBuffer.allocateDirect(channelContext.tioConfig.getReadBufferSize());
		readByteBuffer.position(0);
		readByteBuffer.limit(readByteBuffer.capacity());
		uawkudzrjczntcyycczlrtbln.read(readByteBuffer, readByteBuffer, readCompletionHandler);
	    }
	} catch (Throwable e) {
	    log.error("", e);
	} finally {
	    if (tioServer.isWaitingStop()) {
		log.info("{}即将关闭服务器，不再接受新请求", tioServer.getServerNode());
	    } else {
		AsynchronousServerSocketChannel serverSocketChannel = tioServer.getServerSocketChannel();
		serverSocketChannel.accept(tioServer, this);
	    }
	}
    }

    /**
     *
     * @param exc
     * @param tioServer
     * @author tanyaowu
     */
    @Override
    public void failed(Throwable exc, TioServer tioServer) {
	AsynchronousServerSocketChannel serverSocketChannel = tioServer.getServerSocketChannel();
	serverSocketChannel.accept(tioServer, this);

	log.error("[" + tioServer.getServerNode() + "]监听出现异常", exc);

    }

}
