
/**
 * 
 */
package org.tio.mg.view.init;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.view.topic.TopicCleanViewCacheListener;
import org.tio.mg.view.topic.TopicPullIpToBlackListener;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.CleanViewCacheVo;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;

/**
 * @author tanyaowu
 *
 */
public class TopicInit {

	/**
	 * 
	 */
	public TopicInit() {
	}

	public static void init() {
		RedissonClient redisson = RedisInit.get();

		RTopic pullIpToBlackTopic = redisson.getTopic(Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.addListener(PullIpToBlackVo.class, TopicPullIpToBlackListener.me);

		RTopic cleanViewCacheTopic = redisson.getTopic(Topic.CLEAN_VIEW_CACHE);
		cleanViewCacheTopic.addListener(CleanViewCacheVo.class, TopicCleanViewCacheListener.me);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
