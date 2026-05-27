
package org.tio.sitexxx.web.server.controller.ext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.model.conf.Area;
import org.tio.sitexxx.service.service.conf.AreaService;
import org.tio.utils.resp.Resp;

/**
 * 扩展Demo
 */
@RequestPath(value = "/ext/demo")
public class ExtDemoController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ExtDemoController.class);

	/**
	 * 
	 * 
	 */
	public ExtDemoController() {
	}

	/**
	 * show hello
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/hello")
	public Resp hello() throws Exception {
		List<Area> allArea = AreaService.getAreaTree();
		return Resp.ok().data(allArea);
	}

}
