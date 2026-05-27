
package org.tio.mg.im.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.flash.policy.server.FlashPolicyServerStarter;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.im.common.ImTioUuid;
import org.tio.mg.im.server.handler.ImServerHandler;
import org.tio.mg.im.server.init.TopicInit;
import org.tio.mg.im.server.ws.ImWsStarter;
import org.tio.mg.service.init.CacheInit;
import org.tio.mg.service.init.JFInit;
import org.tio.mg.service.init.JsonInit;
import org.tio.mg.service.init.PropInit;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.ip2region.Ip2RegionInit;
import org.tio.mg.service.service.base.SensitiveWordsService;
import org.tio.mg.service.tio.TioSiteIpStatListener;
import org.tio.mg.service.utils.LogUtils;
import org.tio.server.TioServerConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.TioServerHandler;
import org.tio.server.intf.TioServerListener;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.Threads;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.QuartzUtils;

/**
 * 
 * @author tanyaowu 
 * 2016年9月8日 上午11:39:30
 */
public class TioSiteImServerStarter {
	private static Logger log = LoggerFactory.getLogger(TioSiteImServerStarter.class);

	public static ImTioUuid imTioUuid;

	public static PacketDispatcher packetDispatcher = null;

	public static TioServerHandler			tioSiteImTioServerHandler	= null;
	public static TioServerListener			tioSiteImTioServerListener	= null;
	public static TioSiteImGroupListener	tioSiteImGroupListener		= null;

	//绑定服务ip，一般用null
	public static String bindIp = null;//"127.0.0.1";

	//app
	public static TioServerConfig	tioServerConfigApp	= null;
	public static TioServer			tioServerApp		= null;

	//WS
	public static TioServerConfig	tioServerConfigWs	= null;
	public static ImWsStarter		imWsStarter			= null;

	/**
	 * @param args
	 *
	 * @author tanyaowu
	 * @throws IOException
	 * 2016年11月17日 下午5:59:24
	 *
	 */
	public static void main(String[] args) throws Exception {
		try {
			// 属性初始化
			PropInit.init();

			// ip2region初始化
			Ip2RegionInit.init();

			// 敏感词初始化
			SensitiveWordsService.init();

			// Json配置初始化
			JsonInit.init();

			// jfinal 初始化
			JFInit.init();

			// 缓存初始化
			CacheInit.init(true);

			// redis初始化，里面会有topic等的初始化
			RedisInit.init(true);

			TopicInit.init();

			// 阿里直播初始化
			// AliLiveUtils.init();

			//先启动聊天服务器，再启动zk
			initImServer();

			FlashPolicyServerStarter.start(null, null, Threads.getTioExecutor(), Threads.getGroupExecutor());

			//启动定时任务，配置文件在：config/tio-quartz.properties
			QuartzUtils.start();

		} catch (Throwable e) {
			log.error(e.toString(), e);
			System.exit(1);
		}

	}

	public static void initImServer() throws Exception {
		// 启动时更新chatroom_join_leave表的数据
		String sql1 = "update chatroom_join_leave set leavetime=DATE_ADD(jointime, INTERVAL 5 second), cost=5000, status = 2 where status = 9 and server = ?";
		String sql2 = "update chatroom_join_leave set status = 2 where status = 3 and server = ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql1, Const.MY_IP);
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql2, Const.MY_IP);

		packetDispatcher = new PacketDispatcher(new String[] { ImServerHandler.class.getPackage().getName() });

		tioSiteImTioServerHandler = new TioSiteImTioServerHandler(packetDispatcher);
		tioSiteImTioServerListener = TioSiteImTioServerListener.me;
		tioSiteImGroupListener = TioSiteImGroupListener.me;
		tioServerConfigApp = new TioServerConfig("Tio Site App Im Server", tioSiteImTioServerHandler, tioSiteImTioServerListener, Threads.getTioExecutor(),
		        Threads.getGroupExecutor());
		tioServerConfigApp.setReadBufferSize(P.getInt("im.socket.readbuffersize", 1024));
		tioServerConfigApp.logWhenDecodeError = P.getBoolean("im.logWhenDecodeError", false);

		tioServerConfigApp.groups.setChannelContextComparator(ChannelContextComparator.me);
		long workerId = P.getLong("uuid.workerid");
		long datacenterId = P.getLong("uuid.datacenter");
		imTioUuid = new ImTioUuid(workerId, datacenterId);

		//启动给App用的服务器
		tioServerConfigApp.setTioUuid(imTioUuid);
		tioServerConfigApp.setGroupListener(tioSiteImGroupListener);
		tioServerConfigApp.setIpStatListener(TioSiteIpStatListener.app);
		tioServerConfigApp.ipStats.addDurations(Const.IpStatDuration.IPSTAT_DURATIONS);
		useSsl(tioServerConfigApp);
		tioServerApp = new TioServer(tioServerConfigApp);
		tioServerApp.setCheckLastVersion(P.getBoolean("tio.setCheckLastVersion", false));
		tioServerApp.start(bindIp, Const.ImPort.APP);

		//启动websocket服务器
		imWsStarter = new ImWsStarter(Const.ImPort.WS, imTioUuid, packetDispatcher);
		imWsStarter.getWsServerStarter().getTioServer().setCheckLastVersion(P.getBoolean("tio.setCheckLastVersion", false));
		tioServerConfigWs = imWsStarter.getTioServerConfig();
		tioServerConfigWs.setReadBufferSize(P.getInt("im.ws.readbuffersize", 1024));
		tioServerConfigWs.logWhenDecodeError = P.getBoolean("im.ws.logWhenDecodeError", false);
		tioServerConfigWs.groups.setChannelContextComparator(ChannelContextComparator.me);
		tioServerConfigWs.setName("Tio Site WS Im Server");
		tioServerConfigWs.setIpStatListener(TioSiteIpStatListener.ws);
		tioServerConfigWs.ipStats.addDurations(Const.IpStatDuration.IPSTAT_DURATIONS);
		useSsl(tioServerConfigWs);
		tioServerConfigWs.share(tioServerConfigApp);
		tioServerConfigWs.packetConverter = ImToWsPacketConverter.me;
		imWsStarter.start();
		LogUtils.logJvmStartTime("imWsStarter.start()");

	}

	private static void useSsl(TioServerConfig tioServerConfig) throws Exception {
		String keyStoreFile = P.get("ssl.keystore", null);
		String trustStoreFile = P.get("ssl.truststore", null);
		String keyStorePwd = P.get("ssl.pwd", null);
		tioServerConfig.useSsl(keyStoreFile, trustStoreFile, keyStorePwd);
	}

	public TioSiteImServerStarter() {

	}
}
