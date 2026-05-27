
/**
 * 
 */
package org.tio.sitexxx.im.server.init;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.tio.sitexxx.im.server.topic.TopicImManagerListener;
import org.tio.sitexxx.im.server.topic.TopicPullIpToBlackListener;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.ImManagerTopicVo;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;

/**
 * @author tanyaowu
 *
 */
public class TopicInit {

	public static void init() {
		RedissonClient redisson = RedisInit.get();

		RTopic pullIpToBlackTopic = redisson.getTopic(Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.addListener(PullIpToBlackVo.class, TopicPullIpToBlackListener.me);

		RTopic groupmanagerTopic = redisson.getTopic(Topic.IM_MANAGER_OPER);
		groupmanagerTopic.addListener(ImManagerTopicVo.class, TopicImManagerListener.ME);

	}

}
