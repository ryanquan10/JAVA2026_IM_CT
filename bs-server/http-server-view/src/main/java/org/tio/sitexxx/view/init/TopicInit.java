
/**
 * 
 */
package org.tio.sitexxx.view.init;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.CleanViewCacheVo;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;
import org.tio.sitexxx.view.topic.TopicCleanViewCacheListener;
import org.tio.sitexxx.view.topic.TopicPullIpToBlackListener;

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
