
package org.tio.mg.web.server.controller.tioim;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.CleanViewCacheVo;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.resp.Resp;

/**
 * 会话管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/tio")
public class TioController {

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	/**
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年8月18日 下午4:01:27
	 */
	@RequestPath(value = "/clearuser")
	public Resp chatlist(HttpRequest request) throws Exception {
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_ALL_USER);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.OPER_RIGHT);
	}
	
	/**
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年8月18日 下午4:02:18
	 */
	@RequestPath(value = "/clearconf")
	public Resp clearconf(HttpRequest request) throws Exception {
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_ALL_CONF);
		topicVo.setForceRun(true);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.OPER_RIGHT);
	}
	
	/**
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年8月18日 下午4:18:01
	 */
	@RequestPath(value = "/clearstaticres")
	public Resp clearstaticres(HttpRequest request) throws Exception {
		CleanViewCacheVo cleanViewCacheVo = new CleanViewCacheVo();
		RTopic topic = RedisInit.get().getTopic(Topic.CLEAN_VIEW_CACHE);
		topic.publish(cleanViewCacheVo);
		return Resp.ok(RetUtils.OPER_RIGHT);
	}
}
