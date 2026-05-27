
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.model.main.UserThird.SubTable;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.IThirdLogin;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.ICache;
import org.tio.utils.crypto.Md5;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

/**
1.变量定义 
	type
		QQ登录:   11
		微信登录: 22
		微博登录: 33
2. 访问: /tlogin/{type}，带上如下参数 
	openid: 用户的openid
	sign:  md5(openid + token + {type})
	服务器会返回: uuid

3. 访问: /tlogin/cb/p/{type}，带上如下参数
	uuid: 		前面一步，服务器返回的uuid
	openid: 	用户的openid
	unionid: 	有该字段则传过来
	nick: 		昵称
	avatar: 	头像地址
	sex: 		整数。1: 男， 2: 女
	
	如果是QQ登录，还需要带上如下参数
		is_yellow_vip:    标识用户是否为黄钻用户（0: 不是；1: 是）。
		yellow_vip_level: 黄钻等级
	
	果是微信登录，还需要带上如下参数
		country:       国家
		province:      省
		city:          市
 * @author tanyaowu
 *
 */
public abstract class MobileLogin implements IThirdLogin {
	private static Logger log = LoggerFactory.getLogger(MobileLogin.class);

	final ICache cache = Caches.getCache(CacheConfig.TIME_TO_LIVE_SECONDS_5);

	/**
	 * 
	 */
	public MobileLogin() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public HttpResponse toLoginPage(HttpRequest request, Integer type) throws Exception {
		String openid = request.getParam("openid");
		String sign = request.getParam("sign");
		String mysign = Md5.getMD5(openid + request.getHttpSession().getId() + type);

		if (!Objects.equals(mysign, sign)) {
			RequestExt requestExt = WebUtils.getRequestExt(request);
			log.error("appversion:{}, {}, 验签没通过,mysign:{},sign:{}", requestExt.getAppVersion(), requestExt.getDeviceinfo(), mysign, sign);
			request.close();
			return null;
		}

		String uuid = IdUtil.simpleUUID();
		cache.put(uuid, openid);
		return Resps.json(request, Resp.ok(uuid));
	}

	@Override
	public UserThird callback(HttpRequest request, Integer type) throws Exception {
		String uuid = request.getParam("uuid");
		if (StrUtil.isBlank(uuid)) {
			return null;
		}
		String openid = request.getParam("openid");
		String unionid = request.getParam("unionid");
		String myopenid = cache.get(uuid, String.class);
		if (StrUtil.isBlank(openid) || StrUtil.isBlank(myopenid) || !Objects.equals(myopenid, openid)) {
			log.error("myopenid:{}, openid:{}", myopenid, openid);
			return null;
		}

		String nick = request.getParam("nick");
		String avatar = request.getParam("avatar");
		String sex = request.getParam("sex");

		UserThird userThird = new UserThird();
		userThird.setAvatar(avatar);
		userThird.setNick(nick);
		userThird.setOpenid(openid);
		userThird.setUnionid(unionid);

		if (StrUtil.isNotBlank(sex)) {
			userThird.setSex(Integer.parseInt(sex));
		}
		userThird.setType(type);
		SubTable sub = createSubTable(request, type);
		if (sub != null) {
			userThird.setSubTable(sub);
		}
		return userThird;
	}

	/**
	 * 
	 * @param request
	 * @param type
	 * @return
	 * @author tanyaowu
	 */
	public abstract SubTable createSubTable(HttpRequest request, Integer type);

	@Override
	public boolean isAjax(HttpRequest request, Integer type) throws Exception {
		return true;
	}

}
