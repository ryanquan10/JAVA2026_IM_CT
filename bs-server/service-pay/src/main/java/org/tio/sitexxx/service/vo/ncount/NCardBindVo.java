
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 绑卡接口
 * 交易编码：R007
 * @author lixinji
 * 2021年3月3日 下午4:24:15
 */
public class NCardBindVo extends NCountBaseVo {

	private static final long serialVersionUID = 6899873421021563958L;

	/**
	 * 银行卡卡号
	 */
	private String cardNo;

	/**
	 * 持卡人姓名
	 */
	private String holderName;

	/**
	 * 信用卡有效期
	 */
	private String cardAvailableDate;

	/**
	 * 信用卡CVV2
	 */
	private String cvv2;

	/**
	 * 银行签约手机号
	 */
	private String mobileNo;

	/**
	 * 证件类型
	 */
	private String identityType;

	/**
	 * 证件号码
	 */
	private String identityCode;

	/**
	 * 商户用户ID
	 */
	private String userId;

	/**
	 * 商户用户IP
	 */
	private String merUserIp;

	public static final String[]	encryptArr	= new String[] { "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType", "identityCode", "userId",
	        "merUserIp" };
	public static final String[]	verifyArr	= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "ncountOrderId" };
	public static final String[]	submitArr	= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach",
	        "charset" };

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

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public String getCardAvailableDate() {
		return cardAvailableDate;
	}

	public void setCardAvailableDate(String cardAvailableDate) {
		this.cardAvailableDate = cardAvailableDate;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getIdentityType() {
		return identityType;
	}

	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}

	public String getIdentityCode() {
		return identityCode;
	}

	public void setIdentityCode(String identityCode) {
		this.identityCode = identityCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMerUserIp() {
		return merUserIp;
	}

	public void setMerUserIp(String merUserIp) {
		this.merUserIp = merUserIp;
	}
}
