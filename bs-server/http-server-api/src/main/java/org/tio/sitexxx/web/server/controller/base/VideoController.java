
package org.tio.sitexxx.web.server.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.service.VideoService;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

/**
 * 
 * @author tanyaowu
 */
@RequestPath(value = "/video")
public class VideoController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(VideoController.class);

	/**
	 * 
	 * @param request
	 * @param id
	 * @param status
	 * @return
	 */
	@RequestPath(value = "/updateStatus")
	public Resp updateStatus(HttpRequest request, int id, short status) {
		VideoService.me.updateStatus(id, status);
		WebUtils.clearHttpcache("/video/page1");
		WebUtils.clearHttpcache("/video/page");
		return Resp.ok();
	}

	/**
	 * 重置视频title
	 * @param request
	 * @param id
	 * @param title
	 * @return
	 * @author lixinji
	 */
	@RequestPath(value = "/reTitle")
	public Resp reTitle(HttpRequest request, int id, String title) {
		VideoService.me.reTitle(id, title);
		WebUtils.clearHttpcache("/video/page1");
		WebUtils.clearHttpcache("/video/page");
		return Resp.ok();
	}
}
