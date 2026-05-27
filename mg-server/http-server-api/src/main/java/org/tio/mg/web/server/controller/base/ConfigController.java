
package org.tio.mg.web.server.controller.base;

import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.mg.service.init.PropInit;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.jfinal.P;
import org.tio.utils.resp.Resp;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/config")
public class ConfigController {
	@SuppressWarnings("unused")
	private static Logger				log			= LoggerFactory.getLogger(ConfigController.class);
	public static Map<String, Object>	CONFIG_MAP	= new HashMap<>();
	public static Map<String, Object>	VIEW_MODEL	= new HashMap<>();
	static {
		String title = MgConfService.getString("seo.title", "t-io - 解决其它网络框架没有解决的痛点");
		String keywords = MgConfService.getString("seo.keywords", "t-io,tio,开源,netty,mina,rpc,jfinal,layui,hutool,osc,io,socket,tcp,nio,aio,nio2,im,游戏,java,长连接");
		String description = MgConfService.getString("seo.description",
		        "t-io - 解决其它网络框架没有解决的痛点");
		String jsVersion = MgConfService.getString("js_version", "1");
		//后台特殊处理
		CONFIG_MAP.put(Const.ModelKey.RES_SERVER, Const.RES_SERVER); //用户上传数据服务器
		CONFIG_MAP.put(Const.ModelKey.HTTP_SESSION_COOKIE_NAME, Const.Http.SESSION_COOKIE_NAME);
		CONFIG_MAP.put(Const.ModelKey.API_CONTEXTPATH, Const.API_CONTEXTPATH);
		CONFIG_MAP.put(Const.ModelKey.API_SUFFIX, Const.API_SUFFIX);
		CONFIG_MAP.put(Const.ModelKey.JS_VERSION, jsVersion); //js版本号
		CONFIG_MAP.put(Const.ModelKey.SITE_NAME, MgConfService.getString("sitename", "t-io社交IM平台"));
		
		VIEW_MODEL.put(Const.ModelKey.TITLE, title);
		VIEW_MODEL.put(Const.ModelKey.KEYWORDS, keywords);
		VIEW_MODEL.put(Const.ModelKey.DESCRIPTION, description);
		
		VIEW_MODEL.putAll(CONFIG_MAP);
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author tanyaowu
	 */
	public ConfigController() {
	}

	/**
	 * 更新app-xxx.properties文件
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request) throws Exception {
		P.clear();
		PropInit.init();
		return Resp.ok();
	}

	/**
	 * 清空conf缓存
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/clearConf")
	public Resp clearConf(HttpRequest request) throws Exception {
		MgConfService.clearCache();

		RedissonClient redisson = RedisInit.get();
		RTopic topic1 = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_CONF);
		topic1.publish(topicVo);

		return Resp.ok();
	}

	/**
	 * 清空所有配置项：包括conf、conf_dev表、app.properties、app-env.properties、app-host.properties
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/clearAll")
	public Resp clearAll(HttpRequest request) throws Exception {
		clearConf(request);
		update(request);
		return Resp.ok();
	}

	/**
	 * 获取view model数据，主要用于vue
	 * @param request
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/viewmodel")
	public Resp viewmodel(HttpRequest request) throws Exception {
		return Resp.ok(VIEW_MODEL);
	}
	
	/**
	 * 获取基础配置信息
	 * @param request
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/base")
	public Resp config(HttpRequest request) throws Exception {
		return Resp.ok(CONFIG_MAP);
	}
}
