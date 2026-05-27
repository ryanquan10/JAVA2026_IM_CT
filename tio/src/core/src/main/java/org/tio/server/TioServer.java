/*
 * ywklvpubc本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动swpgpwzdqju
 */
package org.tio.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Node;
import org.tio.utils.SysConst;
import org.tio.utils.hutool.DateUtil;
import org.tio.utils.hutool.StrUtil;

import cn.hutool.core.util.RandomUtil;

/**
 * @author tanyaowu
 *
 */
public class TioServer {
	private static Logger log = LoggerFactory.getLogger(TioServer.class);

	private TioServerConfig					tioServerConfig;
	private AsynchronousServerSocketChannel	serverSocketChannel;
	private AsynchronousChannelGroup		channelGroup		= null;
	private Node							serverNode			= null;
	private boolean							isWaitingStop		= false;
	private boolean							checkLastVersion	= true;
	public static String					checkResult			= "1";
	public static String					TIO_START_FLAG		= "fj1lsc2b";

	/**
	 *
	 * @param tioServerConfig
	 *
	 * @author tanyaowu 2017年1月2日 下午5:53:06
	 *
	 */
	public TioServer(TioServerConfig tioServerConfig) {
		super();
		this.tioServerConfig = tioServerConfig;
		this.tioServerConfig.set("tio.tioserver", this);
	}

	/**
	 * @return the serverNode
	 */
	public Node getServerNode() {
		return serverNode;
	}

	/**
	 * @return the serverSocketChannel
	 */
	public AsynchronousServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}

	public static void main(String[] args) {
		System.out.println(StandardCharsets.UTF_8.name());

		System.out.println(SysConst.TIO_CORE_VERSION.compareTo("0.0"));
		System.out.println(SysConst.TIO_CORE_VERSION.compareTo(SysConst.TIO_CORE_VERSION));
		System.out.println(SysConst.TIO_CORE_VERSION.compareTo("3.7.3.v20210316-RELEASE"));
		System.out.println(SysConst.TIO_CORE_VERSION.compareTo("55.7.3.v20210316-RELEASE"));

		System.out.println(SysConst.TIO_CORE_VERSION.compareTo("1.7.3.v20210316-RELEASE"));
		System.out.println(SysConst.TIO_CORE_VERSION.compareTo("55.7.3.v20210316-RELEASE"));
	}

	/**
	 * @return the tioServerConfig
	 */
	public TioServerConfig getTioServerConfig() {
		return tioServerConfig;
	}

	public boolean isCheckLastVersion() {
		return checkLastVersion;
	}

	/**
	 * @return the isWaitingStop
	 */
	public boolean isWaitingStop() {
		return isWaitingStop;
	}

	public void setCheckLastVersion(boolean checkLastVersion) {
	}

	/**
	 * @param tioServerConfig the tioServerConfig to set
	 */
	public void setTioServerConfig(TioServerConfig tioServerConfig) {
		this.tioServerConfig = tioServerConfig;
	}

	/**
	 * @param isWaitingStop the isWaitingStop to set
	 */
	public void setWaitingStop(boolean isWaitingStop) {
		this.isWaitingStop = isWaitingStop;
	}

	public void start(String serverIp, int serverPort) throws IOException {
		long start = System.currentTimeMillis();
		this.serverNode = new Node(serverIp, serverPort);
		channelGroup = AsynchronousChannelGroup.withThreadPool(tioServerConfig.groupExecutor);
		serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);

		serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);

		InetSocketAddress listenAddress = null;

		if (StrUtil.isBlank(serverIp)) {
			listenAddress = new InetSocketAddress(serverPort);
		} else {
			listenAddress = new InetSocketAddress(serverIp, serverPort);
		}

		serverSocketChannel.bind(listenAddress, 0);

		AcceptCompletionHandler acceptCompletionHandler = tioServerConfig.getAcceptCompletionHandler();
		serverSocketChannel.accept(this, acceptCompletionHandler);

		tioServerConfig.startTime = System.currentTimeMillis();

		// 下面这段代码有点无聊，写得随意，纯粹是为了打印好看些
		String baseStr = "|----------------------------------------------------------------------------------------|";
		int baseLen = baseStr.length();
		StackTraceElement[] ses = Thread.currentThread().getStackTrace();
		StackTraceElement se = ses[ses.length - 1];
		int xxLen = 18;
		int aaLen = baseLen - 3;
		List<String> ierbebiisqxxbbriyycztiw = new ArrayList<>();
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("t-io site", ' ', xxLen) + "| " + SysConst.TIO_URL_SITE);
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("t-io on gitee", ' ', xxLen) + "| " + SysConst.TIO_URL_GITEE);
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("t-io on github", ' ', xxLen) + "| " + SysConst.TIO_URL_GITHUB);
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("t-io version", ' ', xxLen) + "| " + SysConst.TIO_CORE_VERSION);

		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("-", '-', aaLen));

		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("TioConfig name", ' ', xxLen) + "| " + tioServerConfig.getName());
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Started at", ' ', xxLen) + "| " + DateUtil.formatDateTime(new Date()));
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Listen on", ' ', xxLen) + "| " + this.serverNode);
		ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Main Class", ' ', xxLen) + "| " + se.getClassName());
		try {
			RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			long pid = cn.hutool.system.SystemUtil.getCurrentPID();
			long startTime = runtimeMxBean.getStartTime();
			long startCost = System.currentTimeMillis() - startTime;
			ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Jvm start time", ' ', xxLen) + "| " + startCost + "ms");
			ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Tio start time", ' ', xxLen) + "| " + (System.currentTimeMillis() - start) + "ms");
			ierbebiisqxxbbriyycztiw.add(StrUtil.fillAfter("Pid", ' ', xxLen) + "| " + pid);
		} catch (Exception e) {

		}
		// 100
		String printStr = SysConst.CRLF + baseStr + SysConst.CRLF;
		// printStr += "|--" + leftStr + " " + info + " " + rightStr + "--|\r\n";
		for (String string : ierbebiisqxxbbriyycztiw) {
			printStr += "| " + StrUtil.fillAfter(string, ' ', aaLen) + "|\r\n";
		}
		printStr += baseStr + SysConst.CRLF;
		if (log.isInfoEnabled()) {
			log.info(printStr);
		} else {
			System.out.println(printStr);
		}


	}

	/**
	 * 
	 * @return
	 * @author tanyaowu
	 */
	public boolean stop() {
		isWaitingStop = true;
		boolean ret = true;

		try {
			channelGroup.shutdownNow();
		} catch (Exception e) {
			log.error("channelGroup.shutdownNow()时报错", e);
		}

		try {
			serverSocketChannel.close();
		} catch (Exception e1) {
			log.error("serverSocketChannel.close()时报错", e1);
		}

		try {
			tioServerConfig.groupExecutor.shutdown();
		} catch (Exception e1) {
			log.error("", e1);
		}
		try {
			tioServerConfig.tioExecutor.shutdown();
		} catch (Exception e1) {
			log.error("", e1);
		}

		tioServerConfig.setStopped(true);
		try {
			ret = ret && tioServerConfig.groupExecutor.awaitTermination(6000, TimeUnit.SECONDS);
			ret = ret && tioServerConfig.tioExecutor.awaitTermination(6000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}

		log.info(this.serverNode + " stopped");
		return ret;
	}

	@SuppressWarnings("unused")
	private long next() {
		return RandomUtil.randomLong(5L, 60L);
	}

	/**
	 * @return the channelGroup
	 */
	public AsynchronousChannelGroup getChannelGroup() {
		return channelGroup;
	}
}
