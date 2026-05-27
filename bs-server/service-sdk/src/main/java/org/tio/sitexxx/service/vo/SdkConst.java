
package org.tio.sitexxx.service.vo;

public class SdkConst {

	public static interface SynUserUrl {

		/**
		 * 获取所有用户
		 */
		String ALL_USER_URL = Const.OUT_SERVICE + "/tioadmin/api/alluser" + ".admin_x";

		/**
		 * 登录状态查询
		 */
		String LOGIN_SATA_URL = Const.OUT_SERVICE + "/tioadmin/api/loginstat" + ".admin_x?sessionid=";

		/**
		 * 单点登录
		 */
		String LOGIN_URL = Const.OUT_SERVICE + "/tioadmin/api/login" + ".admin_x";

	}

}
