
package org.tio.sitexxx.service.api.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.api.user.impl.StdSynUser;
import org.tio.sitexxx.service.api.user.intf.BaseSynUserIntf;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.OkHttpUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.OutUserVo;
import org.tio.sitexxx.service.vo.SdkConst;
import org.tio.sitexxx.service.vo.nd.NdAllUserResp;
import org.tio.sitexxx.service.vo.nd.NdLoginInfoResp;
import org.tio.sitexxx.service.vo.nd.NdUserVo;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import okhttp3.Response;

public class NdUserApiService {

	private static Logger log = LoggerFactory.getLogger(NdUserApiService.class);

	private static BaseSynUserIntf userIntf = new StdSynUser();

	public static final NdUserApiService me = new NdUserApiService();

	/**
	 * 初始化所有用户
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午2:09:51
	 */
	public Ret initRemoteUser() {
		try {
			Response resp = OkHttpUtils.get(SdkConst.SynUserUrl.ALL_USER_URL);
			if (resp == null) {
				return RetUtils.failMsg("同步失败,获取响应为空");
			}
			if (resp.isSuccessful()) {
				if (resp.code() != 200) {
					return RetUtils.failMsg("同步失败,状态码：" + resp.code());
				}
				String body = resp.body().string();
				NdAllUserResp allUserResp = Json.toBean(body, NdAllUserResp.class);
				List<OutUserVo> formatUser = userFormat(allUserResp.getData());
				if (CollectionUtil.isEmpty(formatUser)) {
					return RetUtils.failMsg("没有获取的有效的用户列表");
				}
				return userIntf.init(formatUser);
			} else {
				return RetUtils.failMsg("同步失败,响应失败");
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		return RetUtils.sysError();
	}

	/**
	 * 登录验证
	 * @param sessionid
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午2:43:13
	 */
	public HttpResponse autoLogin(HttpRequest request, DefaultHttpRequestHandler requestHandler, String sessionid) {
		try {
			Response resp = OkHttpUtils.get(SdkConst.SynUserUrl.LOGIN_SATA_URL + sessionid);
			if (resp == null) {
				return Resps.json(request, Resp.fail().msg("响应失败"));
			}
			if (resp.isSuccessful()) {
				if (resp.code() != 200) {
					return Resps.json(request, Resp.fail().msg("获取登录信息失败,状态码：" + resp.code()));
				}
				String body = resp.body().string();
				NdLoginInfoResp allUserResp = Json.toBean(body, NdLoginInfoResp.class);
				if (!allUserResp.getCode().equals("0") || allUserResp.getData() == null) {
					return Resps.json(request, Resp.fail().msg("用户未登录"));
				}
				return userIntf.autoLogin(request, requestHandler, userFormat(allUserResp.getData()));
			} else {
				return Resps.json(request, Resp.fail().msg("获取登录信息失败,响应失败"));
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		return Resps.json(request, Resp.fail().msg("系统异常"));
	}
 
	/**
	 * @param username
	 * @param pwd
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午3:23:35
	 */
	public HttpResponse ndLogin(HttpRequest request, DefaultHttpRequestHandler requestHandler, String username, String password) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);
		params.put("password", password);
		try {
			Response resp = OkHttpUtils.post(SdkConst.SynUserUrl.LOGIN_URL, params);
			if (resp == null) {
				return Resps.json(request, Resp.fail().msg("响应失败"));
			}
			if (resp.isSuccessful()) {
				if (resp.code() != 200) {
					return Resps.json(request, Resp.fail().msg("登录失败,状态码：" + resp.code()));
				}
				String body = resp.body().string();
				NdLoginInfoResp allUserResp = Json.toBean(body, NdLoginInfoResp.class);
				if (!allUserResp.getCode().equals("0")) {
					return Resps.json(request, Resp.fail().msg("登录失败"));
				}
				return userIntf.autoLogin(request, requestHandler, userFormat(allUserResp.getData()));
			} else {
				return Resps.json(request, Resp.fail().msg("登录失败,响应失败"));
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		return Resps.json(request, Resp.fail().msg("系统异常"));
	}

	/**
	 * 用户变动
	 * @param ddlJson
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午2:58:12
	 */
	public Ret ddlUser(String ddlJson) {
		List<OutUserVo> userVos = Json.toList(ddlJson, OutUserVo.class);
		if (CollectionUtil.isEmpty(userVos)) {
			return RetUtils.invalidParam();
		}
		return userIntf.userDdl(userVos);
	}

	/**
	 * @param tionoStr
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午4:01:56
	 */
	public String uidFormat(String tionoStr) {
		if (StrUtil.isBlank(tionoStr)) {
			return "";
		}
		String[] tionoArr = StrUtil.splitToArray(tionoStr, ",");
		String uidStr = "";
		for (String tiono : tionoArr) {
			User user = UserService.ME.getUserByTiono(tiono);
			if (user != null) {
				uidStr += "," + user.getId();
			}
		}
		if (StrUtil.isNotBlank(uidStr)) {
			return uidStr.substring(1);
		}
		return uidStr;
	}

	/**
	 * @param userVos
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午2:33:27
	 */
	public List<OutUserVo> userFormat(List<NdUserVo> userVos) {
		if (CollectionUtil.isEmpty(userVos)) {
			return null;
		}
		List<OutUserVo> userList = new ArrayList<OutUserVo>();
		for (NdUserVo user : userVos) {
			if (Objects.equals(user.getIsValid(), "F")) {
				continue;
			}
			OutUserVo userVo = new OutUserVo();
			userVo.setNick(user.getUserName());
			userVo.setUnioncode(user.getUserCode());
			userVo.setPhone(user.getRegCellPhone());
			userVo.setOper(OutUserVo.Oper.create);
			userList.add(userVo);
		}
		return userList;
	}

	/**
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月20日 下午2:46:31
	 */
	public OutUserVo userFormat(NdUserVo user) {
		if (user == null) {
			return null;
		}
		OutUserVo userVo = new OutUserVo();
		userVo.setNick(user.getUserName());
		userVo.setUnioncode(user.getUserCode());
		userVo.setPhone(user.getRegCellPhone());
		return userVo;
	}

}
