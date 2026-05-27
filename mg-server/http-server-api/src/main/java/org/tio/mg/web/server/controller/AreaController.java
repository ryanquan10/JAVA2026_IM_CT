
package org.tio.mg.web.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.server.annotation.RequestPath;
import org.tio.mg.service.model.conf.Area;
import org.tio.mg.service.service.conf.AreaService;
import org.tio.utils.resp.Resp;

/**
 * 区域请求处理类
 * 
 */
@RequestPath(value = "/area")
public class AreaController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AreaController.class);

	/**
	 * 
	 * 
	 */
	public AreaController() {
	}

	/**
	 * 获取所有区域-区域树
	 * @param mediaCode
	 * @return
	 * @throws Exception
	 * 
	 */
	@RequestPath(value = "/all")
	public Resp all() throws Exception {
		List<Area> allArea = AreaService.getAreaTree();
		return Resp.ok().data(allArea);
	}

	/**
	 * 单个区域的子列表
	 * @param code
	 * @return
	 * @throws Exception
	 * 
	 */
	@RequestPath(value = "/child")
	public Resp child(String code) throws Exception {
		Area area = AreaService.getChild(code);
		return Resp.ok().data(area);
	}

	/**
	 * 区域的父列表
	 * @param code
	 * @return
	 * @throws Exception
	 * 
	 */
	@RequestPath(value = "/parent")
	public Resp parent(String code) throws Exception {
		Area area = AreaService.getParent(code);
		return Resp.ok().data(area);
	}

}
