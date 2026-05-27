
package org.tio.sitexxx.web.server.controller.im;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.client.CluClient;
import org.tio.clu.common.bs.BestNodeResp;
import org.tio.core.Node;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.jfinal.P;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;
/**
 * 
 * @author tanyaowu
 */
@RequestPath(value = "/im")
public class ImController {
	
	private static Logger log = LoggerFactory.getLogger(ImController.class);

	public static final String SUBDIR_VIDEO = org.tio.sitexxx.service.vo.Const.UPLOAD_DIR.IM_VIDEO;

	public static final String SUBDIR_IMG = org.tio.sitexxx.service.vo.Const.UPLOAD_DIR.IM_IMG;

	/**
	 * 获取im服务器地址
	 * @param request
	 * @param groupid
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/imserver")
	public Resp imserver(HttpRequest request) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);
		String protocol = requestExt.isFromBrowser() ? Const.Protocol.IM_WS : Const.Protocol.IM_APP;
		Integer currUid = WebUtils.currUserId(request);

		Node node = null;
		BestNodeResp bestNodeResp = null;

		if (Const.USE_TIO_CLU) {
			try {
				bestNodeResp = CluClient.bestNodeReq(TioSiteImServerStarter.cluClientStarter, currUid, protocol, 1500L);
			} catch (Exception e) {
				log.error("获取最佳服务器时异常", e);
			}
		}

		if (bestNodeResp != null && bestNodeResp.isOk()) {
			//			Map<String, Node> nodemap = bestNodeResp.getNodeMap();
			node = bestNodeResp.getNode();//nodemap.get(protocol);
		} else {
			node = new Node(Const.MY_IP, WebUtils.getImServerPort(request));
			if (!Const.IM_SSL_FLAG) {
				node.setSsl((byte) Const.YesOrNo.NO);
			}
		}

		if (StrUtil.isNotBlank(P.get("tio.clu.select.sever.force"))) {
			node.setIp(P.get("tio.clu.select.sever.force"));
		}

		node.setTimeout(Const.IM_HEARTBEAT_TIMEOUT);

		Resp resp = Resp.ok().data(node);
		return resp;
	}

	/**
	 *  获取turnserver
	 * @param request
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/turnserver")
	public Resp turnserver(HttpRequest request) throws Exception {
		TurnServer turnServer = new TurnServer();
		turnServer.setUrls(ConfService.getString("turnserver.url", "turn:112.74.183.177:3478"));  //turn:112.74.183.177:3478 stun:112.74.183.177:3478
		turnServer.setCredential(ConfService.getString("turnserver.credential", "8654269566"));
		turnServer.setUsername(ConfService.getString("turnserver.username", "tan"));
		
		List<TurnServer> list = new ArrayList<>(1);
		list.add(turnServer);
		return Resp.ok().data(list);
	}
}
