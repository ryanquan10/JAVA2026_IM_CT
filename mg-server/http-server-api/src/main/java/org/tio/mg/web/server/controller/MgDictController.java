
package org.tio.mg.web.server.controller;

import java.util.List;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.conf.Dict;
import org.tio.mg.service.service.conf.MgDictService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mgdict")
public class MgDictController {
	private static Logger log = LoggerFactory.getLogger(MgDictController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private MgDictService dictService = MgDictService.me;

	/**
	 *
	 * @author tanyaowu
	 */
	public MgDictController() {
	}
	
	/**
	 * 获取顶层字典列表
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月27日 下午2:15:44
	 */
	@RequestPath(value = "/topList")
	public Resp topList(HttpRequest request,String name) throws Exception {
		List<Dict> topList = dictService.topList(name);
		return Resp.ok(topList);
	}
	
	/**
	 * 获取父节点下的所有子节点数据
	 * @param request
	 * @param parentcode
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午2:02:13
	 */
	@RequestPath(value = "/childList")
	public Resp childList(HttpRequest request,String pcode,String name,Short status) throws Exception {
		if(StrUtil.isBlank(pcode)) {
			log.error("获取子节点数据：参数为空");
			return Resp.fail(RetUtils.INVALID_PARAMETER);
		}
		List<Dict> childList = MgDictService.getChildDictByParentCode(pcode,name,status);
		return Resp.ok(childList);
	}
	
	/**
	 * 子节点字典
	 * @param request
	 * @param pcode
	 * @param name
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月22日 下午4:05:34
	 */
	@RequestPath(value = "/childdict")
	public Resp childDict(HttpRequest request,String pcode) throws Exception {
		if(StrUtil.isBlank(pcode)) {
			log.error("获取子节点数据：参数为空");
			return Resp.fail(RetUtils.INVALID_PARAMETER);
		}
		List<Dict> childList = MgDictService.getChildDictByParentCode(pcode);
		return Resp.ok(childList);
	}
	
	/**
	 * @param request
	 * @param did
	 * @param topdid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月10日 上午11:39:52
	 */
	@RequestPath(value = "/index")
	public Resp index(HttpRequest request,Integer did,Integer topdid) throws Exception {
		Ret ret = MgDictService.index(did,topdid);
		if(ret.isFail()) {
			log.error("调整失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param dict
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午2:44:06
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,Dict dict) throws Exception {
		Ret ret = dictService.add(dict);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 插入
	 * @param request
	 * @param dict
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月15日 下午4:18:10
	 */
	@RequestPath(value = "/insert")
	public Resp insert(HttpRequest request,Dict dict) throws Exception {
		Ret ret = dictService.insert(dict);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param dict
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年5月29日 下午2:44:08
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,Dict dict) throws Exception {
		Ret ret = dictService.update(dict);
		if(ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param rid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:37:05
	 */
	@RequestPath(value = "/del")
	public Resp del(HttpRequest request,Integer id) throws Exception {
		Ret ret = dictService.del(id);
		if(ret.isFail()) {
			log.error("删除失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param id
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月2日 下午4:39:17
	 */
	@RequestPath(value = "/disable")
	public Resp disable(HttpRequest request,Integer id,Short status) throws Exception {
		Ret ret = dictService.disable(id,status);
		if(ret.isFail()) {
			log.error("禁用/开启失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_DICT);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
}
