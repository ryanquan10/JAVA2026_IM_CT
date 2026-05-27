
/**
 * 
 */
package org.tio.sitexxx.web.server.http.cache;

import org.tio.http.common.HttpRequest;
import org.tio.sitexxx.service.model.main.User;

/**
 * @author tanyaowu
 *
 */
public interface HttpCacheMeta {

	/**
	 * 是不是自己的资源，譬如某篇博客
	 * @param request
	 * @param curr
	 * @return 如果返回null，则不用这个作为key，
	 */
	public Boolean isSelf(HttpRequest request, User curr);

	/**
	 * 需要用角色作为key
	 * @param request
	 * @param curr
	 * @return
	 */
	public String roleKey(HttpRequest request, User curr);

}
