/*
 * yitzkls本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gjqsjqzhndt
 */
package org.tio.flash.policy.server;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.server.TioServerConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.TioServerHandler;
import org.tio.server.intf.TioServerListener;
import org.tio.utils.SystemTimer;
import org.tio.utils.Threads;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

/**
 *
 * @author tanyaowu 2017年4月4日 下午12:22:58
 */
public class FlashPolicyServerStarter {
    public static class CheckRunnable implements Runnable {
	@Override
	public void run() {

	    while (true) {
		try {
		    Thread.sleep(10 * 1000);
		} catch (InterruptedException e1) {
		    log.error(e1.toString(), e1);
		}

		SetWithLock<ChannelContext> setWithLock = tioServerConfig.connections;
		Set<ChannelContext> set = null;
		ReadLock readLock = setWithLock.readLock();
		readLock.lock();
		try {
		    long now = SystemTimer.currTime;
		    set = setWithLock.getObj();
		    for (ChannelContext channelContext : set) {
			long interval = (now - channelContext.stat.timeFirstConnected);
			if (interval > 5000) {
			    Tio.remove(channelContext, "已经连上来有" + interval + "ms了，该断开啦");
			}
		    }
		} catch (java.lang.Throwable e) {
		    log.error("", e);
		} finally {
		    readLock.unlock();
		}
	    }
	}
    }

    private static Logger log = LoggerFactory.getLogger(FlashPolicyServerStarter.class);

    // handler, 包括编码、解码、消息处理
    public static TioServerHandler tioHandler = null;

    // 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
    public static TioServerListener tioListener = null;

    // 一组连接共用的上下文对象
    public static TioServerConfig tioServerConfig = null;

    // tioServer对象
    public static TioServer tioServer = null;

    public static int count = 1;

    /**
     * 检查所有通道
     */
    private static void checkAllChannels() {
	Thread thread = new Thread(new CheckRunnable(), "Flash-Policy-Server-" + count++);
	thread.start();

    }

    /**
     * 
     * @param ip
     * @param port
     * @author tanyaowu
     */
    public static void start(String ip, Integer port) {
	start(ip, port, Threads.getTioExecutor(), Threads.getGroupExecutor());
    }

    /**
     * 
     * @param ip            可以为null
     * @param port          如果为null，则用默认的端口
     * @param tioExecutor
     * @param groupExecutor
     * @author tanyaowu
     */
    public static void start(String ip, Integer port, SynThreadPoolExecutor tioExecutor,
	    ThreadPoolExecutor groupExecutor) {
	if (port == null) {
	    port = Const.PORT;
	}
	tioHandler = new FlashPolicyTioServerHandler();
	tioServerConfig = new TioServerConfig("tio flash policy server", tioHandler, tioListener, tioExecutor,
		groupExecutor);
	tioServerConfig.setHeartbeatTimeout(Const.HEARTBEAT_TIMEOUT);
	tioServer = new TioServer(tioServerConfig);

	try {
	    tioServer.start(ip, port);
	} catch (Throwable e) {
	    log.error("", e);
	    System.exit(1);
	}

	checkAllChannels();
    }
}