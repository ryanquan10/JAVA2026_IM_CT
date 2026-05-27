
package org.tio.sitexxx.view;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.flash.policy.server.FlashPolicyServerStarter;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.init.JFInit;
import org.tio.sitexxx.service.init.PropInit;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.ip2region.Ip2RegionInit;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LogUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.view.http.WebViewInit;
import org.tio.sitexxx.view.http.WebViewModelGenerator;
import org.tio.sitexxx.view.http.pathmodel.PathModelGenerator;
import org.tio.sitexxx.view.init.TopicInit;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;
import org.tio.webpack.model.Root;

import cn.hutool.core.io.FileUtil;

/**
 * @author tanyaowu
 * 2016年8月7日 上午10:58:03
 */
public class WebViewStarter {
	private static Logger				log			= LoggerFactory.getLogger(WebViewStarter.class);
	public static Root					model		= null;
	public static Map<Object, Object>	mapModel	= null;

	public static WebViewModelGenerator tioWebpackModelGenerator;

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws IOException
	 */

	public static void main(String[] args) throws Exception {
		try {

			PropInit.init();

			Ip2RegionInit.init();

			Caches.init();

			// jfinal 初始化
			JFInit.init();

			RedisInit.init(true);
			//
			TopicInit.init();

			initView(args);

			if (P.getBoolean("start.843", false)) {
				FlashPolicyServerStarter.start(null, null);
			}
		} catch (Throwable e) {
			log.error("", e);
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	private static void initView1() {
		model = null;
		String modelFilePath = P.get("view.model.config.file", "webpack.json");
		String modelAbsFile = FileUtil.getAbsolutePath("classpath:" + modelFilePath);
		File modelFile = new File(modelAbsFile);
		log.info("modelAbsFile:{}", modelAbsFile);
		@SuppressWarnings("deprecation")
		String content = FileUtil.readString(modelFile, "utf-8");
		model = Json.toBean(content, Root.class);

		mapModel = Json.toBean(content, Map.class);
		String title = ConfService.getString("seo.title", "t-io - 让天下没有难开发的网络通讯, 单机不仅仅是支持30万长连接");
		String keywords = ConfService.getString("seo.keywords", "t-io,tio,开源,netty,mina,rpc,jfinal,layui,hutool,osc,io,socket,tcp,nio,aio,nio2,im,游戏,java,长连接");
		String description = ConfService.getString("seo.description",
		        "t-io/tio是一个网络编程框架，也可以叫TCP长连接框架，从这一点来说是有点像netty的，但t-io为常见和网络相关的业务（如IM、消息推送、RPC、监控）提供了近乎于现成的解决方案，即丰富的编程API，极大减少业务层的编程难度");
		String jsVersion = ConfService.getString("js_version", "1");

		mapModel.put(Const.ModelKey.TITLE, title);
		mapModel.put(Const.ModelKey.KEYWORDS, keywords);
		mapModel.put(Const.ModelKey.DESCRIPTION, description);
		mapModel.put(Const.ModelKey.RES_SERVER, Const.RES_SERVER); //用户上传数据服务器
		mapModel.put(Const.ModelKey.JS_VERSION, jsVersion); //js版本号

		mapModel.put(Const.ModelKey.API_CONTEXTPATH, org.tio.sitexxx.service.vo.Const.API_CONTEXTPATH);
		mapModel.put(Const.ModelKey.API_SUFFIX, org.tio.sitexxx.service.vo.Const.API_SUFFIX);
		mapModel.put(Const.ModelKey.HTTP_SESSION_COOKIE_NAME, Const.Http.SESSION_COOKIE_NAME);
		mapModel.put(Const.ModelKey.SITE_NAME, ConfService.getString("sitename", "t-io社交IM平台"));

		String tioim_title = ConfService.getString("tioim.title", "让更多公司用上安全、可靠、自主、卓越的IM");
		String tioim_keywords = ConfService.getString("tioim.keywords", "夜猫 高并发,集群,私有部署");
		String tioim_description = ConfService.getString("tioim.description", "夜猫支持集群、语音通话、视频通话、私聊、群聊");
		mapModel.put("tioim_title", tioim_title);
		mapModel.put("tioim_keywords", tioim_keywords);
		mapModel.put("tioim_description", tioim_description);

		tioWebpackModelGenerator = new WebViewModelGenerator(mapModel, new String[] { PathModelGenerator.class.getPackage().getName() });

	}

	public static void initView(String[] args) throws Exception {
		initView1();
		LogUtils.logJvmStartTime(WebViewStarter.class.getName() + ".initView1()");

		WebViewInit.init(model, tioWebpackModelGenerator);
		LogUtils.logJvmStartTime(WebViewInit.class.getName() + ".init()");
	}

	/**
	 *
	 * @author tanyaowu
	 */
	public WebViewStarter() {
	}
}
