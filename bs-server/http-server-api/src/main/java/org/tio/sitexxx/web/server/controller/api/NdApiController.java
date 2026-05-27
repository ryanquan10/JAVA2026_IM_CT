
package org.tio.sitexxx.web.server.controller.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.api.user.NdUserApiService;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxGroup;
import org.tio.sitexxx.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.model.stat.GroupStat;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.utils.AvatarUtils;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.nd.NdGroupVo;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.web.server.init.WebApiInit;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 南大api接口
 */
@RequestPath(value = "/ndapi")
public class NdApiController {

	private static Logger log = LoggerFactory.getLogger(NdApiController.class);

	private NdUserApiService apiService = NdUserApiService.me;

	/**
	 * 用户初始化
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午3:41:25
	 */
	@RequestPath(value = "/init")
	public Resp init(HttpRequest request) throws Exception {
		Ret ret = apiService.initRemoteUser();
		if (ret.isFail()) {
			log.error("用户初始化异常：{}", RetUtils.getRetMsg(ret));
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}

	/**
	 * 用户变更-post-json
	 * @param userlist
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午3:42:19
	 */
	@RequestPath(value = "/change")
	public Resp change(HttpRequest request) throws Exception {
		String userlist = request.getBodyString();
		if(StrUtil.isBlank(userlist)) {
			return Resp.fail("用户json为空");
		}
		Ret ret = apiService.ddlUser(userlist);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}

	/**
	 * 登录-?号传参
	 * @param username
	 * @param password
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午3:44:39
	 */
	@RequestPath(value = "/login")
	public HttpResponse login(HttpRequest request,String username,String password) throws Exception {
		return apiService.ndLogin(request, WebApiInit.requestHandler, username, password);
	}

	/**
	 * 自动登录-?号传参
	 * 警告：此处客户如果放公网，请post或者加密处理
	 * @param sessionid
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午3:45:08
	 */
	@RequestPath(value = "/autologin")
	public HttpResponse autologin(HttpRequest request,String sessionid) throws Exception {
		return apiService.autoLogin(request, WebApiInit.requestHandler, sessionid);
	}

	/**
	 * 外部用户创建群-post-json
	 * @param request
	 * @param wxGroup
	 * @param uidList
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午3:47:30
	 */
	@RequestPath(value = "/createGroup")
	public Resp createGroup(HttpRequest request) throws Exception {
		String createInfo = request.getBodyString();
		NdGroupVo groupVo = Json.toBean(createInfo, NdGroupVo.class);
		if(groupVo == null || StrUtil.isBlank(groupVo.getCreatecode()) || StrUtil.isBlank(groupVo.getUsercodes())) {
			return Resp.fail("外部创建群参数异常");
		}
		String createcode = groupVo.getCreatecode();
		String usercodes = groupVo.getUsercodes();
		WxGroup wxGroup = new WxGroup();
		wxGroup.setIntro(groupVo.getIntro());
		wxGroup.setName(groupVo.getName());
		User curr = UserService.ME.getUserByTiono(createcode);
		if (curr == null || Objects.equals(curr.getStatus(), User.Status.LOGOUT)) {
			return Resp.fail("用户未注册");
		}
		String name = "";
		if (StrUtil.isNotBlank(wxGroup.getName())) {
			name = wxGroup.getName();
			name = name.length() > 30 ? name.substring(0, 30) : name;
			wxGroup.setName(name);
			wxGroup.setAutoflag(Const.YesOrNo.NO);
		}
		String uidList = apiService.uidFormat(usercodes);
		String[] uidArr = StrUtil.splitToArray(uidList, ",");
		Ret checkRet = WxChatApi.checkCreateGroupRegLimit(curr);
		if (checkRet.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(checkRet));
		}
		Ret imgRet = getGroupImg(uidArr, curr, name);
		Img img = RetUtils.getOkTData(imgRet, "img");
		String nicks = RetUtils.getOkTData(imgRet, "nicks");
		if (StrUtil.isBlank(name)) {
			name = RetUtils.getOkTData(imgRet, "name");
			wxGroup.setName(name);
			wxGroup.setAutoflag(Const.YesOrNo.YES);
		}
		wxGroup.setAvatar(img.getCoverurl());
		Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
		String sessionid = request.getHttpSession().getId();
		String ip = request.getClientIp();
		short joinnum = uidArr != null ? (short) (uidArr.length + 1) : (short) 1;
		wxGroup.setCreatetime(new Date());
		Ret ret = GroupService.me.createGroup(curr, wxGroup, nicks, devicetype, sessionid, ip, joinnum, WebUtils.getRequestExt(request).getAppVersion());
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		//消息触发
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
					SysMsgVo sysMsgVo = RetUtils.getOkTData(ret, "sysmsgvo");
					//发送自己的群信息
					WxChatApi.creatGroupOwner(request, devicetype, curr, wxGroup, msg, sysMsgVo);
					//其它人的消息触发
					WxChatApi.creatGroup(request, curr, uidList, nicks, wxGroup, msg, sysMsgVo);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		//统计处理
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					String dayperiod = PeriodUtils.dateToPeriodByType(wxGroup.getCreatetime(), Const.PeriodType.DAY);
					GroupStat groupStat = GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
					if (groupStat == null) {
						groupStat = new GroupStat();
						groupStat.setAddcount(1);
						groupStat.setDayperiod(dayperiod);
						groupStat.setType(Const.Status.NORMAL);
						groupStat.ignoreSave();
					} else {
						Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return Resp.ok(wxGroup);
	}

	/**
	 * 获取群头像
	 * @param uidArr
	 * @param curr
	 * @param name
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月20日 下午4:06:35
	 */
	private static Ret getGroupImg(String[] uidArr, User curr, String name) throws Exception {
		List<String> avatarList = new ArrayList<>();
		avatarList.add(curr.getAvatar());
		boolean createName = StrUtil.isBlank(name) ? true : false;
		if (createName) {
			name = curr.getNick();
		}
		String nicks = "";
		int c = 0;
		for (String uidStr : uidArr) {
			if (StrUtil.isNotBlank(uidStr)) {
				int uid = Integer.parseInt(uidStr);
				if (Objects.equals(uid, curr.getId())) { //前面已经把当前用户加到了该群，所以此处略过
					continue;
				}
				User user = UserService.ME.getById(uid);
				if (user != null) {
					if (createName) {
						String newName = name + "、" + user.getNick();
						if (newName.length() < 30) {
							name = newName;
						}
					}
					nicks += "、" + user.getNick();
					if (c >= 8) {
						continue;
					}
					avatarList.add(user.getAvatar());
					c++;
				}
			}
		}
		Img img = AvatarUtils.generateGroupAvatar(avatarList, curr.getId());
		if (createName) {
			return Ret.ok().set("img", img).set("nicks", nicks.substring(1)).set("name", name);
		} else {
			return Ret.ok().set("img", img).set("nicks", nicks.substring(1));
		}
	}

	/**
	 * 
	 * 
	 */
	public NdApiController() {
	}
}
