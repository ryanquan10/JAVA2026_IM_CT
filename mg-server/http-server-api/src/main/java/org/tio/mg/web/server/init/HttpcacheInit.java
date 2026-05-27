
/**
 * 
 */
package org.tio.mg.web.server.init;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.server.mvc.Routes;
import org.tio.mg.service.model.conf.Httpcache;
import org.tio.mg.service.service.conf.HttpcacheService;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;

/**
 * @author tanyaowu
 *
 */
public class HttpcacheInit {
	private static Logger log = LoggerFactory.getLogger(HttpcacheInit.class);

	/**
	 * 
	 */
	public HttpcacheInit() {
	}

	/**
	 * 
	 */
	public static void init(Routes routes) {
		List<Httpcache> list = HttpcacheService.getAll();
		if (CollectionUtil.isNotEmpty(list)) {
			StringBuilder sb = new StringBuilder(50);
			for (Httpcache httpcache : list) {
				String path = httpcache.getPath();
				if (!routes.PATH_METHOD_MAP.containsKey(path)) {
					sb.append(path).append(System.lineSeparator());
				}
			}

			if (sb.length() > 0) {
				log.error("有些路径在httpcache表中配置了，但是是无效路径\r\n{}", sb.toString());
				try {
					String writeMappingToFile = System.getProperty("tio.mvc.route.writeMappingToFile", "true");
					if ("true".equalsIgnoreCase(writeMappingToFile)) {
						FileUtil.writeUtf8String("有些路径在httpcache表中配置了，但是是无效路径\r\n" + sb.toString(), "/tio_error_httpcache_path.txt");//.writeString("/tio_error_httpcache_path.txt", "有些路径在httpcache表中配置了，但是是无效路径\r\n" + sb.toString());
					}
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
			}
		}
	}

}
