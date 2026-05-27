
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 
 * 充值确认接口
 * 交易编码：T008
 * @author lixinji
 * 2021年3月5日 上午10:31:03
 */
public class NRechargeConfirmVo extends NCountBaseVo {

	private static final long		serialVersionUID	= 2688204405928575996L;
	public static final String[]	encryptArr			= new String[] { "ncountOrderId", "smsCode", "merUserIp", "paymentTerminalInfo", "receiverTerminalInfo", "deviceInfo",
	        "businessType", "feeType", "divideAcctDtl", "feeAmountUser" };
	public static final String[]	verifyArr			= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode",
	        "ncountOrderId", "tranAmount", "checkDate", "submitTime", "tranFinishTime", "bankCode", "cardType", "shortCardNo", "bindCardAgrNo" };
	public static final String[]	submitArr			= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue",
	        "merAttach", "charset" };

	public static final String	TRAN_AMOUNT			= "tranAmount";
	public static final String	CHECK_DATE			= "checkDate";
	public static final String	BANK_CODE			= "bankCode";
	public static final String	CARD_TYPE			= "cardType";
	public static final String	SHORT_CARD_NO		= "shortCardNo";
	public static final String	BIND_CARD_AGR_NO	= "bindCardAgrNo";

	/**
	 * 新账通订单号
	 */
	private String ncountOrderId;

	/**
	 * 短信验证码
	 */
	private String smsCode;

	/**
	 * 商户用户IP
	 */
	private String merUserIp;

	//新账通1.6上新增担保交易字段
	private String	businessType;	//业务类型
	private String	feeType;		//手续费内扣外扣
	private String	divideAcctDtl;	//分账明细
	private String	feeAmountUser;	//手续费承担方ID

	private String paymentTerminalInfo;

	private String receiverTerminalInfo;

	private String deviceInfo;

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

	public String getPaymentTerminalInfo() {
		return paymentTerminalInfo;
	}

	public void setPaymentTerminalInfo(String paymentTerminalInfo) {
		this.paymentTerminalInfo = paymentTerminalInfo;
	}

	public String getReceiverTerminalInfo() {
		return receiverTerminalInfo;
	}

	public void setReceiverTerminalInfo(String receiverTerminalInfo) {
		this.receiverTerminalInfo = receiverTerminalInfo;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
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

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getFeeType() {
		return feeType;
	}

	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	public String getDivideAcctDtl() {
		return divideAcctDtl;
	}

	public void setDivideAcctDtl(String divideAcctDtl) {
		this.divideAcctDtl = divideAcctDtl;
	}

	public String getFeeAmountUser() {
		return feeAmountUser;
	}

	public void setFeeAmountUser(String feeAmountUser) {
		this.feeAmountUser = feeAmountUser;
	}

}
