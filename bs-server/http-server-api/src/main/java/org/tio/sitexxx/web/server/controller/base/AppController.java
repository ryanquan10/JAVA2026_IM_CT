
package org.tio.sitexxx.web.server.controller.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.MgDiscoveryPage;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

/**
 * 单独提供给app的接口
 * @author tanyaowu
 */
@RequestPath(value = "/app")
public class AppController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AppController.class);

	/**
	 *
	 * @author tanyaowu
	 */
	public AppController() {
	}

	@SuppressWarnings("unused")
	@RequestPath(value = "/conf")
	public Resp conf(HttpRequest request) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);

		Map<String, Object> map = new HashMap<>();
		map.put("res_server", Const.RES_SERVER);
		return Resp.ok(map);
	}

	@RequestPath(value = "/getDiscoveryPageInfo")
	public Resp getDiscoveryPageInfo(HttpRequest request) {
		List<MgDiscoveryPage> all = MgDiscoveryPage.dao.findAll();
		return Resp.ok(all);
	}

	/**
	 *  查询指定客户端配置
	 * @param request
	 * @param name
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/getClientConfByName")
	public Resp updateClientConf(HttpRequest request, String name) {
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", name);
		if (clientConf == null) {
			return Resp.fail().msg("该配置不存在");
		}
		return Resp.ok(clientConf);
	}

	/**
	 *  查询所有客户端配置
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/getClientConf")
	public Resp getClientConf(HttpRequest request) {
		List<ClientConf> clientConfs = ClientConf.dao.find("select * from client_conf");
		return Resp.ok(clientConfs);
	}

	/**
	 *  查询所有客户端配置 改
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.10.11 14:43:37
	 */
	@RequestPath(value = "/getClientConfNew")
	public Resp getClientConfNew(HttpRequest request) {
		List<ClientConf> data = ClientConf.dao.find("select * from client_conf");
		List<Map> clientConfs = new ArrayList<>();

		for (ClientConf temp : data) {
			HashMap clientConf = new HashMap();
			clientConf.put("name", temp.getName());
			clientConf.put("isOpen", temp.getValue());
			clientConfs.add(clientConf);
		}
		return Resp.ok(clientConfs);
	}

}
