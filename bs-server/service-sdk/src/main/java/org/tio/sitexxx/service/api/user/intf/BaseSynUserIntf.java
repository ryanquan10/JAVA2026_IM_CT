
package org.tio.sitexxx.service.api.user.intf;

import java.util.List;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.vo.OutUserVo;

/**
 * 
 * @author lixinji
 * 2021年4月16日 下午2:06:49
 */
public interface BaseSynUserIntf {

	/**
	 * 初始化用户
	 * @param users
	 * @return
	 * @author lixinji
	 * 2021年4月16日 下午2:01:04
	 */
	Ret init(List<OutUserVo> users);

	/**
	 * 同步用户
	 * @param users
	 * @return
	 * @author lixinji
	 * 2021年4月16日 下午2:01:35
	 */
	Ret userDdl(List<OutUserVo> users);

	/**
	 * 自动登录
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月16日 下午2:03:22
	 */
	HttpResponse autoLogin(HttpRequest request, DefaultHttpRequestHandler requestHandler, OutUserVo user);

	/**
	 * 默认头像处理
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月19日 上午10:36:00
	 */
	Ret defaultAvatar(OutUserVo user);
}
