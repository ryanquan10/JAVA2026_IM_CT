
package org.tio.mg.service;

import java.io.IOException;

import org.tio.mg.service.init.CacheInit;
import org.tio.mg.service.init.JFInit;
import org.tio.mg.service.init.JsonInit;
import org.tio.mg.service.init.PropInit;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.ip2region.Ip2RegionInit;
import org.tio.mg.service.service.base.SensitiveWordsService;

/**
 * 
 * @author tanyaowu
 */
public class TioSiteBsServiceInit {

	/**
	 * @author tanyaowu
	 * @throws Exception 
	 * @throws IOException
	 */
	public static void init() throws Exception {
		// 属性初始化
		PropInit.init();

		// ip2region初始化
		Ip2RegionInit.init();

		// 敏感词初始化
		SensitiveWordsService.init();

		// Json配置初始化
		JsonInit.init();

		// jfinal 初始化
		JFInit.init();

		// redis初始化，里面会有topic等的初始化
		RedisInit.init(true);

		// 缓存初始化
		CacheInit.init(true);


	}

	/**
	 * @author tanyaowu
	 */
	public TioSiteBsServiceInit() {
	}
}
