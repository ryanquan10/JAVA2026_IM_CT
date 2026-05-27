
package org.tio.mg.web.server.controller.tioim;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.conf.EmailServer;
import org.tio.mg.service.service.tioim.TioEmailService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 邮件服务
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/email")
public class TioEmailController {
	private static Logger log = LoggerFactory.getLogger(TioEmailController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioEmailService emailService = TioEmailService.me;
	
	/**
	 * 发送邮件
	 * @param request
	 * @param startid
	 * @param endid
	 * @param content
	 * @param title
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:43:55
	 */
	@RequestPath(value = "/submit")
	public Resp submit(HttpRequest request,Integer startid,Integer endid,String content,String title) throws Exception {
		Ret ret = emailService.sendEmail(startid, endid, title, content);
		if(ret.isFail()) {
			log.error("发送失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 获取邮件服务列表
	 * @param request
	 * @param searchkey
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:43:35
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,String searchkey) throws Exception {
		List<EmailServer> emailServers = emailService.getAll(searchkey);
		return Resp.ok(emailServers);
	}
	
	/**
	 * 新增邮件服务器
	 * @param request
	 * @param email
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午5:01:38
	 */
	@RequestPath(value = "/add")
	public Resp add(HttpRequest request,EmailServer email) throws Exception {
		Ret ret = emailService.add(email);
		if(ret.isFail()) {
			log.error("新增失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 修改邮件服务器
	 * @param request
	 * @param email
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午5:01:51
	 */
	@RequestPath(value = "/update")
	public Resp update(HttpRequest request,EmailServer email) throws Exception {
		Ret ret = emailService.update(email);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 删除邮件服务器
	 * @param request
	 * @param email
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午5:01:26
	 */
	@RequestPath(value = "/del")
	public Resp del(HttpRequest request,String email) throws Exception {
		Ret ret = emailService.del(email);
		if(ret.isFail()) {
			log.error("修改失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
}
