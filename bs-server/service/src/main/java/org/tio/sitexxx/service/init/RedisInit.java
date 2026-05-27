
package org.tio.sitexxx.service.init;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.FstCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.service.MyRandomLoadBalancer;
import org.tio.sitexxx.service.topic.TopicCommonListener;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年11月8日 下午3:48:02
 */
public class RedisInit {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(RedisInit.class);

	private static RedissonClient redisson;

	public static void init() {
		init(true);
	}

	/**
	 * 消息监听：前后台配置信息通道
	 * @author tanyaowu
	 */
	public static void init(boolean isFrontend) {

		boolean clusterFlag = isFrontend ? P.getBoolean("redis.cluster.flag", false) : false;
		Config config = new Config();
		if (clusterFlag) {
			String[] clusterAddress = P.get("redis.cluster.address").split(",");

			ClusterServersConfig clusterServersConfig = config.useClusterServers().setScanInterval(5000).addNodeAddress(clusterAddress).setLoadBalancer(new MyRandomLoadBalancer())
			        .setReadMode(ReadMode.MASTER_SLAVE);
			//				for(String address : clusterAddress) {
			//					clusterConfig.addNodeAddress(address);
			//				}
			String pwd = P.get("redis.password");
			if (StrUtil.isNotBlank(pwd)) {
				clusterServersConfig.setPassword(pwd);
			}
		} else {
			String address = "redis://" + P.get("redis.ip") + ":" + P.get("redis.port");
			SingleServerConfig singleServerConfig = config.useSingleServer().setAddress(address).setConnectionPoolSize(32).setConnectionMinimumIdleSize(16);

			String pwd = P.get("redis.password");
			if (StrUtil.isNotBlank(pwd)) {
				singleServerConfig.setPassword(pwd);
			}

		}
		config.setCodec(new FstCodec());
		//		config.setCodec(new Kryo5Codec());
		config.setLockWatchdogTimeout(1000 * 30); //默认情况下，看门狗的检查锁的超时时间是30秒钟

		config.setNettyThreads(0);
		config.setThreads(0);

		redisson = Redisson.create(config);
		if (isFrontend) {
			RTopic topic = redisson.getTopic(Topic.COMMON_TOPIC);
			topic.addListener(TopicVo.class, TopicCommonListener.ME);
		}
	}

	public static RedissonClient get() {
		return redisson;
	}

}
