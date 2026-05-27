
package org.tio.sitexxx.view.http;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.RequestLine;
import org.tio.http.common.view.ModelGenerator;
import org.tio.sitexxx.view.annotaion.ModelGeneratorPath;
import org.tio.sitexxx.view.http.pathmodel.PathModelGenerator;
import org.tio.utils.hutool.ClassScanAnnotationHandler;
import org.tio.utils.hutool.ClassUtil;
import org.tio.utils.json.Json;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 2016年11月15日 下午1:14:20
 */
public class WebViewModelGenerator implements ModelGenerator {

	private static Logger log = LoggerFactory.getLogger(WebViewModelGenerator.class);

	// public static WebViewModelGenerator me = new WebViewModelGenerator();

	private Map<Object, Object>				mapModel;
	/**
	 * 路径和对象映射 key: path 形如：/user/index.html value: object
	 */
	public Map<String, PathModelGenerator>	generatorMap	= new HashMap<>();
	/**
	 * 路径和class映射 只是用来打印的 key: path 形如：/user/index.html value: Class
	 */
	public Map<String, Class<?>>			pathClassMap	= new TreeMap<>();

	/**
	 * 
	 * @author tanyaowu
	 */
	public WebViewModelGenerator(Map<Object, Object> mapModel, String[] scanPackages) {
		this.mapModel = mapModel;

		if (scanPackages != null) {

			for (String pkg : scanPackages) {
				try {
					ClassUtil.scanPackage(pkg, new ClassScanAnnotationHandler(ModelGeneratorPath.class) {
						@Override
						public void handlerAnnotation(Class<?> clazz) {
							try {
								Object bean = clazz.newInstance();
								if (!(bean instanceof PathModelGenerator)) {
									log.error("{}没有实现 {}", clazz.getName(), PathModelGenerator.class.getName());
									return;
								}
								PathModelGenerator pathModelGenerator = (PathModelGenerator) bean;
								ModelGeneratorPath mapping = clazz.getAnnotation(ModelGeneratorPath.class);
								String[] pathes = mapping.value();
								for (String path : pathes) {
									Object obj = generatorMap.get(path);
									if (obj != null) {
										log.error("mapping[{}] already exists in class [{}]", path, obj.getClass().getName());
									} else {
										generatorMap.put(path, pathModelGenerator);
										pathClassMap.put(path, clazz);
									}
								}
							} catch (Exception e) {
								log.error(clazz.getName(), e);
							}
						}
					});
				} catch (Exception e) {
					log.error("", e);
				}
			}
			log.warn("command mapping\r\n{}", Json.toFormatedJson(pathClassMap));
		}
	}

	/**
	 * @param request
	 * @return
	 * @author tanyaowu
	 * @throws Exception
	 */
	@Override
	public Object generate(HttpRequest request) throws Exception {
		RequestLine requestLine = request.getRequestLine();
		String path = requestLine.getPath();

		if (StrUtil.endWith(path, "/")) {
			path = path + "index.html";
		}

		String ext = FileUtil.extName(path);

		if (!"css".equalsIgnoreCase(ext) && !"js".equalsIgnoreCase(ext) && !"html".equalsIgnoreCase(ext) && StrUtil.endWith(path, "/")) { // 不是以html结尾，也不是以"/"结尾，表明不是页面文件
			return mapModel;
		}

		// 群组："/live/index.html"
		// 首 页："/"
		// log.warn("path:{}, initPath:{}", path, requestLine.getInitPath());
		// path:/res/public/layui/css/modules/code.css,
		// initPath:/res/public/layui/css/modules/code.css
		Map<Object, Object> map = new HashMap<>();
		map.putAll(mapModel);

		PathModelGenerator pathModelGenerator = generatorMap.get(path);
		if (pathModelGenerator == null) {
			return mapModel;
		}

		HttpResponse response = pathModelGenerator.generate(request, path, map);
		if (response != null) {
			return response;
		}

		return map;
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
}
