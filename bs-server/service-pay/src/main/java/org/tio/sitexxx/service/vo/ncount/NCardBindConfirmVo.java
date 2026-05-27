
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 绑卡接口确认接口
 * 交易编码：R008
 * @author lixinji
 * 2021年3月3日 下午4:24:55
 */
public class NCardBindConfirmVo extends NCountBaseVo {

	private static final long serialVersionUID = 8021862346094484016L;

	public static final String[]	encryptArr	= new String[] { "ncountOrderId", "smsCode", "merUserIp" };
	public static final String[]	verifyArr	= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "bindCardAgrNo",
	        "bankCode", "cardType", "shortCardNo" };
	public static final String[]	submitArr	= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach",
	        "charset" };

	/**
	 * 签约订单号
	 */
	private String ncountOrderId;

	/**
	 * 签约短信验证码
	 */
	private String smsCode;

	/**
	 * 商户用户IP
	 */
	private String merUserIp;

	@Override
	public String getEncryptJsonStr() {
		return getJsonStr(this, encryptArr);
	}

	@Override
	public String getVerifyJsonStr() {
		return getJsonStr(this, verifyArr);
	}

	@Override
	public String getSubmitJsonStr() {
		return getJsonStr(this, submitArr);
	}

	public String getNcountOrderId() {
		return ncountOrderId;
	}

	public void setNcountOrderId(String ncountOrderId) {
		this.ncountOrderId = ncountOrderId;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public String getMerUserIp() {
		return merUserIp;
	}

	public void setMerUserIp(String merUserIp) {
		this.merUserIp = merUserIp;
	}
}
