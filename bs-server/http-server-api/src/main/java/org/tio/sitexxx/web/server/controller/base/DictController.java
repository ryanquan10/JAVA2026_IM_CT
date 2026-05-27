
package org.tio.sitexxx.web.server.controller.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.model.conf.Dict;
import org.tio.sitexxx.service.service.conf.DictService;
import org.tio.utils.resp.Resp;

/**
 *
 * 
 */
@RequestPath(value = "/dict")
public class DictController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(DictController.class);

	/**
	 * 
	 * 
	 */
	public DictController() {
	}

	/**
	 * 
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * 
	 */
	@RequestPath(value = "/child")
	public Resp child(String code) throws Exception {
		List<Dict> child = DictService.getChildren(code);
		return Resp.ok().data(child);
	}

}
