
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.sitexxx.service.model.main.UserThird;

/**
 * @author tanyaowu
 *
 */
public interface IThirdLogin {

	/**
	 * 跳转到第三方登录
	 * @param request
	 * @param type 登录类型
	 * @return
	 * @throws Exception
	 */
	public HttpResponse toLoginPage(HttpRequest request, Integer type) throws Exception;

	/**
	 * 回调
	 * @param request
	 * @param type 登录类型
	 * @return UserThird
	 */
	public UserThird callback(HttpRequest request, Integer type) throws Exception;

	/**
	 * 
	 * @param request
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public boolean isAjax(HttpRequest request, Integer type) throws Exception;
}
