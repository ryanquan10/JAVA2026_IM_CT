
package org.tio.mg.web.server.controller;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.conf.Conf;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.resp.Resp;

/**
 * 
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mgconf")
public class MgConfController {
	private static Logger log = LoggerFactory.getLogger(MgConfController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private MgConfService confService = MgConfService.me;

	/**
	 *
	 * @author tanyaowu
	 */
	public MgConfController() {
	}
	
	/**
	 * @param request
	 * @param dict
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午2:44:06
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,String searchkey,Short type) throws Exception {
		Ret ret = confService.list(searchkey, type);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	/**
	 * @param request
	 * @param conf
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午4:25:09
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,Conf conf) throws Exception {
		Ret ret = confService.add(conf);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_CONF);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param conf
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午4:25:13
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,Conf conf) throws Exception {
		Ret ret = confService.update(conf);
		if(ret.isFail()) {
			log.error(RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_CONF);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
}
