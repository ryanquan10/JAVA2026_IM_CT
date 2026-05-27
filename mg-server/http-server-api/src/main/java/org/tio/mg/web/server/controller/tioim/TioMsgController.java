
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioMsgService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 消息服务
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/tiomsg")
public class TioMsgController {
	private static Logger log = LoggerFactory.getLogger(TioMsgController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioMsgService msgService = TioMsgService.me;
	
	/**
	 * 模型下私聊消息列表
	 * @param request
	 * @param fidkey
	 * @param contenttype
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月14日 下午4:48:51
	 */
	@RequestPath(value = "/p2plist")
	public Resp p2pList(HttpRequest request,String fidkey,String contenttype,String starttime,String endtime,Integer pageNumber,Integer pageSize,Short type,String searchkey) throws Exception {
		Ret ret = msgService.p2pList(pageNumber, pageSize, fidkey, contenttype, starttime, endtime, type, searchkey);
		if(ret.isFail()) {
			log.error("获取私聊消息列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	
	/**
	 * 模型下的群消息列表
	 * @param request
	 * @param groupid
	 * @param type
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月8日 下午4:49:17
	 */
	@RequestPath(value = "/grouplist")
	public Resp groupList(HttpRequest request,Long groupid,Short type,Integer pageNumber,Integer pageSize, String searchkey) throws Exception {
		Ret ret = msgService.groupList(pageNumber, pageSize, groupid, type, searchkey);
		if(ret.isFail()) {
			log.error("获取群聊消息列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
}
