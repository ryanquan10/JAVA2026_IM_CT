
/**
 * 
 */
package org.tio.sitexxx.web.server.controller.base.thirdlogin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.apple.AppleLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.douyin.DouyinLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.osc.OscLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.qq.QQLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.qqmobile.QQMobileLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wb.WbLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wbmobile.WbMobileLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wx.WxLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wx.WxOpenLogin;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.wxmobile.WxMobileLogin;

import cn.hutool.core.collection.CollUtil;

/**
 * @author tanyaowu
 *
 */
public class ThirdLoginFactory {

	private static Logger log = LoggerFactory.getLogger(ThirdLoginFactory.class);

	public static final ThirdLoginFactory me = new ThirdLoginFactory();

	/**
	 * 
	 */
	private ThirdLoginFactory() {

	}

	private static final Map<Integer, List<Integer>>	SIMILAR_TYPES		= new HashMap<>();
	private static final Map<Integer, String>			SIMILAR_TYPES_STR	= new HashMap<>();
	static {
		for (int i = 1; i < 10; i++) {
			List<Integer> list = new ArrayList<>(3);
			list.add(i);
			list.add(i + i * 10);
			list.add(i + i * 10 + i * 100);
			list.add(i + i * 10 + i * 100 + i * 1000);

			for (int j = 0; j < list.size(); j++) {
				SIMILAR_TYPES.put(list.get(j), list);
				SIMILAR_TYPES_STR.put(list.get(j), CollUtil.join(list, ","));
			}
		}
	}

	/**
	 * 
	 * @param type 形如"1","2"
	 * @return 形如"[1,11,111,1111]"
	 * @author tanyaowu
	 */
	public static List<Integer> getSimilarTypes(Integer type) {
		return SIMILAR_TYPES.get(type);
	}

	/**
	 * 
	 * @param type 形如"1","2"
	 * @return 形如"1,11,111,1111"
	 * @author tanyaowu
	 */
	public static String getSimilarTypesStr(Integer type) {
		return SIMILAR_TYPES_STR.get(type);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public IThirdLogin getThirdLogin(Integer type) {
		switch (type) {
		case UserThird.Type.QQ:
			return QQLogin.me;
		case UserThird.Type.QQ_MOBILE:
			return QQMobileLogin.me;

		case UserThird.Type.WX:
			return WxLogin.me;
		case UserThird.Type.WX_MOBILE:
			return WxMobileLogin.me;
		case UserThird.Type.WX_OPEN:
			return WxOpenLogin.me;
		case UserThird.Type.WB:
			return WbLogin.me;
		case UserThird.Type.WB_MOBILE:
			return WbMobileLogin.me;

		case UserThird.Type.DOUYIN:
			return DouyinLogin.me;

		case UserThird.Type.OSC:
			return OscLogin.me;

		case UserThird.Type.APPLE:
			return AppleLogin.me;

		default:
			log.warn("找不到IThirdLogin的实现类, type:{}", type);
			return null;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(33 % 10);
		System.out.println(11 % 10);
		System.out.println(66 % 10);
		System.out.println(77 % 10);

		System.out.println(ThirdLoginFactory.getSimilarTypesStr(1));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(11));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(111));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(1111));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(2));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(22));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(222));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(2222));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(3));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(33));
		System.out.println(ThirdLoginFactory.getSimilarTypesStr(9999));
	}

}
