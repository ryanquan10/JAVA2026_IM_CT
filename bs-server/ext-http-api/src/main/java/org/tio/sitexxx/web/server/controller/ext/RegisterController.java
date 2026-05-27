
package org.tio.sitexxx.web.server.controller.ext;

import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.ext.ExtRegisterService;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.utils.AvatarUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 扩展注册
 * @author lixinji
 */
@RequestPath(value = "/extregister")
public class RegisterController {

	private static final ExtRegisterService registerService = ExtRegisterService.me;


	/**
	 * api注册逻辑
	 * 
	 * avatar:头像-非必传<br>
	 * nick:昵称-必传<br>
	 * loginname:登录名-必传<br>
	 * pwd:密码-必传<br>
	 * 
	 * @param user
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji 2021年1月26日 下午6:35:56
	 */
	@RequestPath(value = "/register")
	public Resp register(User user, HttpRequest request) throws Exception {
		completeUser(user, request);
		RequestExt requestExt = WebUtils.getRequestExt(request);
		user.setReghref(request.getReferer());
		if (StrUtil.isBlank(user.getAvatar())) {
			String path = AvatarUtils.pressUserAvatar(user.getNick());
			if (StrUtil.isNotBlank(path)) {
				user.setAvatar(path);
				user.setAvatarbig(path);
			}
		}
		//默认同意
		user.setAgreement("1");
		Resp resp = registerService.register(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
		if (resp.isOk()) {
			Kv kv = (Kv) resp.getData();
			User reguser = (User) kv.get("user");
			p2pAfterRegister(reguser, request);
		}
		return resp;
	}
	
	/**
	 * 填充信息
	 * @param user
	 * @param request
	 * @author lixinji
	 */
	private void completeUser(User user, HttpRequest request) {
		IpInfo ipInfo = IpInfoService.ME.save(request.getClientIp());
		user.setIpInfo(ipInfo);
		RequestExt requestExt = WebUtils.getRequestExt(request);
		short deviceType = requestExt.getDeviceType();
		user.setRegistertype(deviceType);
	}
	

	/**
	 * 注册后处理
	 * @param user
	 * @param request
	 * @author lixinji
	 */
	@SuppressWarnings("deprecation")
	public static void p2pAfterRegister(User user, HttpRequest request) {
		Ret slef = ChatService.me.actFdChatItems(user.getId(), user.getId());
		if (slef.isFail()) {
			return;
		}
		WxChatItems selfChatItems = RetUtils.getOkTData(slef, "chat");
		if (WxSynApi.isSynVersion()) {
			WxSynApi.synChatSession(user.getId(), selfChatItems, SynRecordVo.SynType.ADD);
		} else {
			WxChatApi.userActOper(request, user.getId(), selfChatItems);
		}
	}

}
