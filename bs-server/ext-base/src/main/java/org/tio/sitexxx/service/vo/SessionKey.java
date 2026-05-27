
package org.tio.sitexxx.service.vo;

/**
 * @author tanyaowu
 * 2016年8月10日 上午10:58:36
 */
public interface SessionKey {
	/**
	 * SessionExt
	 */
	String SESSION_EXT = "SESSION_EXT";

	/**
	 * 微信支付扫码支付二维码的base64串
	 */
	String WX_PAY_QR_BASE64 = "WX_PAY_QR_BASE64";

	/**
	 * 微信支付扫码支付的支付金额，单位：元
	 */
	String WX_PAY_AMOUNT = "WX_PAY_AMOUNT";

	/**
	 * 微信支付扫码支付的商品名，形如：1000T币
	 */
	String WX_PAY_SUBJECT = "WX_PAY_SUBJECT";

	/**
	 * 微信支付扫码支付的订单号
	 */
	String WX_PAY_TRADENO = "WX_PAY_TRADENO";

	/**
	 * 微信公众号支付openid
	 */
	String WX_PAY_OPENID = "WX_PAY_OPENID";

	/**
	 * 第三方登录时所在的页面
	 */
	String THIRD_LOGIN_REFERER = "THIRD_LOGIN_REFER";

	/**
	 * 微信授权stat
	 */
	String STATE_OPEN_KEY = "WECHAT_OPEN_STATE_KEY";

}
