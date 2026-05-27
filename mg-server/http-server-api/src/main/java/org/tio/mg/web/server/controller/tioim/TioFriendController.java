
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioFriendService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 钛信群管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/friend")
public class TioFriendController {
	private static Logger log = LoggerFactory.getLogger(TioFriendController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioFriendService friendService = TioFriendService.me;
	
	/**
	 * 消息模型下的好友列表
	 * @param request
	 * @param searchkey
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月14日 下午4:54:15
	 */
	@RequestPath(value = "/fdlist")
	public Resp fdlist(HttpRequest request,String searchkey,String starttime,String endtime,Integer pageNumber,Integer pageSize,Short type, String content) throws Exception {
		Ret ret = friendService.fdlist(pageNumber, pageSize, searchkey, starttime, endtime, type, content);
		if(ret.isFail()) {
			log.error("获取好友列表(消息模型)失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
}
