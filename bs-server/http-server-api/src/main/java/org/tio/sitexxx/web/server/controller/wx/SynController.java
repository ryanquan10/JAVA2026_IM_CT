
package org.tio.sitexxx.web.server.controller.wx;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

/**
 * 同步入口
 * @author lixinji
 * 2020年8月25日 下午7:41:13
 */
@RequestPath(value = "/syn")
public class SynController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SynController.class);

	private final static SynService syn = SynService.me;

	/**
	 * @param request
	 * @param syntime
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年8月26日 下午3:36:09
	 */
	@RequestPath(value = "/chat")
	public Resp chat(HttpRequest request, Date syntime) throws Exception {
		User curr = WebUtils.currUser(request);
		RequestExt requestExt = WebUtils.getRequestExt(request);
		short deviceType = requestExt.getAppDevice();
		Ret ret = syn.chat(curr, deviceType, syntime);
		if (ret.isFail()) {

			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 
	 * @param request
	 * @param synid
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年8月27日 下午2:54:40
	 */
	@RequestPath(value = "/ack")
	public Resp ack(HttpRequest request, Integer synid) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = syn.ack(curr, synid);
		if (ret.isFail()) {

			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
}
