
package org.tio.mg.all;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.flash.policy.server.FlashPolicyServerStarter;
import org.tio.mg.im.server.TioSiteImServerStarter;
import org.tio.mg.service.init.CacheInit;
import org.tio.mg.service.init.JFInit;
import org.tio.mg.service.init.JsonInit;
import org.tio.mg.service.init.PropInit;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.ip2region.Ip2RegionInit;
import org.tio.mg.service.model.conf.IpBlackList;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.SensitiveWordsService;
import org.tio.mg.service.service.conf.AreaService;
import org.tio.mg.service.service.conf.AvatarService;
import org.tio.mg.service.service.conf.IpBlackListService;
import org.tio.mg.service.utils.LogUtils;
import org.tio.mg.view.WebViewStarter;
import org.tio.mg.view.http.WebViewInit;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.Threads;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.QuartzUtils;

/**
 * @author tanyaowu
 *
 */
public class Starter {
	private static Logger log = LoggerFactory.getLogger(Starter.class);

	/**
	 * 
	 */
	public Starter() {
	}

	/**
	 * 基本的初始化，一般用于单元小测试
	 * @throws SQLException 
	 */
	public static void initBase() throws SQLException {
		PropInit.init();
		LogUtils.logJvmStartTime("PropInit.init()");
		
		// redis初始化，里面会有topic等的初始化
		RedisInit.init(true);
		LogUtils.logJvmStartTime("RedisInit.init()");

		// ip2region初始化
		Ip2RegionInit.init();
		LogUtils.logJvmStartTime("Ip2RegionInit.init()");

		// 敏感词初始化
		SensitiveWordsService.init();
		LogUtils.logJvmStartTime("SensitiveWordsService.init()");

		// Json配置初始化
		JsonInit.init();
		LogUtils.logJvmStartTime("JsonInit.init()");

		// jfinal 初始化
		JFInit.init();
		LogUtils.logJvmStartTime("JFInit.init()");

		// 缓存初始化
		CacheInit.init(true);
		LogUtils.logJvmStartTime("CacheInit.init()");

		AreaService.init();
		LogUtils.logJvmStartTime("AreaService.init()");

	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {
		try {
			initBase();
			LogUtils.logJvmStartTime("initBase()");

			boolean startFlashPolicyServer = P.getInt("start.flash.policy.server", 1) == 1;

			org.tio.mg.web.server.init.TopicInit.init();
			LogUtils.logJvmStartTime("org.tio.mg.web.server.init.TopicInit.init()");

			// 阿里直播初始化
			//			AliLiveUtils.init();

			// 加载头像数据
			AvatarService.loadData();
			LogUtils.logJvmStartTime("AvatarService.loadData()");

			//加载meizi图数据
			//			MeiziService.loadData();

			//爬一下美女图
			//			DownloadMeizitu.main(args);

			//爬一下头像
			//			DownloadAvatar.main(args);

			if (Const.IS_START_IM) {
				//先启动聊天服务器，再启动zk
				TioSiteImServerStarter.initImServer();
				LogUtils.logJvmStartTime("TioSiteImServerStarter.imServerInit()");
			}

			if (startFlashPolicyServer) {
				FlashPolicyServerStarter.start(null, null, Threads.getTioExecutor(), Threads.getGroupExecutor());
				LogUtils.logJvmStartTime("FlashPolicyServerStarter.start(null, null, Threads.getTioExecutor(), Threads.getGroupExecutor())");
			}

			// 启动Api服务器
			WebApiInit.init();
			LogUtils.logJvmStartTime("WebApiInit.init()");

			if (Const.IS_START_IM) {
				org.tio.mg.im.server.init.TopicInit.init();
				LogUtils.logJvmStartTime("org.tio.mg.im.server.init.TopicInit.init()");
			}

			//启动View服务器
			if (Const.IS_START_VIEW) {
				org.tio.mg.view.init.TopicInit.init();
				LogUtils.logJvmStartTime("org.tio.mg.view.init.TopicInit.init()");
				WebViewStarter.initView(args);
				WebViewInit.httpServerStarter.getTioServerConfig().share(WebApiInit.httpServerStarter.getTioServerConfig());
				LogUtils.logJvmStartTime("WebViewStarter.initView(args)");
			}

			//启动定时任务
			QuartzUtils.start();
			LogUtils.logJvmStartTime("QuartzUtils.start()");

			// 初始化ip黑名单
			initIpBlackList();
			LogUtils.logJvmStartTime("initIpBlackList()");

			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

			// 设置任务为每天00:00执行
			runEveryDayAtMidnight(executor);
//			executor.scheduleAtFixedRate(new MyRunnable(), 0, 1, TimeUnit.MINUTES);

		} catch (Throwable e) {
			log.error(e.toString(), e);
			System.exit(1);
		}
	}

	public static void runEveryDayAtMidnight(ScheduledExecutorService executor) {
		// 获取当前时间
		Calendar now = Calendar.getInstance();

		// 设置目标时间为明天的00:00
		Calendar targetTime = (Calendar) now.clone();
		targetTime.add(Calendar.DATE, 1);
		targetTime.set(Calendar.HOUR_OF_DAY, 0);
		targetTime.set(Calendar.MINUTE, 0);
		targetTime.set(Calendar.SECOND, 0);
		targetTime.set(Calendar.MILLISECOND, 0);

		// 如果当前时间已经超过了00:00，那么将目标时间设置为后天的00:00
		if (now.after(targetTime)) {
			targetTime.add(Calendar.DATE, 1);
		}

		// 计算距离下次执行的时间间隔
		long delay = targetTime.getTimeInMillis() - now.getTimeInMillis();

		// 调度任务在延迟后执行，然后设置新的调度
		executor.schedule(() -> {
			// 在这里执行你的任务
			try {
				List<User> users = User.dao.find("select * from user where is_beautiful_id = 1 and now() > beautiful_id_expire_time");
				for (User user : users) {
					user.setIsBeautifulId(0);
					user.update();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			// 重新调度任务，确保明天这个时候再次执行
			runEveryDayAtMidnight(executor);
		}, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 初始化ip黑名单
	 * 
	 * @author tanyaowu
	 */
	private static void initIpBlackList() {
		Map<String, IpBlackList> map = IpBlackListService.me.getAll();
		if (map != null) {
			map.forEach(new BiConsumer<String, IpBlackList>() {

				@Override
				public void accept(String ip, IpBlackList ipBlackList) {
					Tio.IpBlacklist.add(ip);
				}
			});
		}
	}

}
