
package org.tio.sitexxx.web.server.init;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.vo.ClearHttpCache;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;
import org.tio.sitexxx.web.server.topic.TopicClearHttpCacheListener;
import org.tio.sitexxx.web.server.topic.TopicPullIpToBlackListener;

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
		RTopic topic = redisson.getTopic(Topic.CLEAR_HTTP_CACHE);
		topic.addListener(ClearHttpCache.class, TopicClearHttpCacheListener.me);

		RTopic pullIpToBlackTopic = redisson.getTopic(Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.addListener(PullIpToBlackVo.class, TopicPullIpToBlackListener.me);
		//
		//		if (org.tio.sitexxx.service.vo.Const.IS_START_IM) {
		//			RTopic webNotifyImTopic = redisson.getTopic(Topic.WEB_NTY_IM);
		//			webNotifyImTopic.addListener(TioClusterVo.class, TopicWebNotifyImListener.me);
		//		}
	}
}
