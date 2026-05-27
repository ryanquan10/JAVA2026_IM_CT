
/**
 * 
 */
package org.tio.sitexxx.view.http.pathmodel;

import java.util.Map;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;

/**
 * @author tanyaowu
 *
 */
public interface PathModelGenerator {

	/**
	 * 生成model，如果返回值不为null，则直接将返回值作为响应发给前端
	 * @param request
	 * @param path
	 * @param baseModel
	 * @return
	 * @throws Exception 
	 */
	public HttpResponse generate(HttpRequest request, String path, Map<Object, Object> baseModel) throws Exception;
}
