
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 提现接口
 * 交易编码：T002
 * @author lixinji
 * 2021年3月4日 下午3:23:35
 */
public class NWithholdVoVo extends NCountBaseVo {

	private static final long		serialVersionUID	= -822690241127251739L;
	public static final String[]	encryptArr			= new String[] { "tranAmount", "userId", "bindCardAgrNo", "notifyUrl", "paymentTerminalInfo", "deviceInfo", "serviceAmount",
	        "businessType", "cardNo" };
	public static final String[]	verifyArr			= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode",
	        "ncountOrderId" };
	public static final String[]	submitArr			= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue",
	        "merAttach", "charset" };

	private String	tranAmount;		//金额
	private String	userId;			//用户ID
	private String	bindCardAgrNo;	//绑卡协议号
	private String	notifyUrl;		//通知地址
	/**
	 * 服务费
	 */
	private String	serviceAmount;

	/**
	 * 业务类型:08 绑定卡提现
	 */
	private String businessType;

	/**
	 * 银行卡号
	 */
	private String cardNo;

	private String paymentTerminalInfo;

	private String deviceInfo;

	public String getPaymentTerminalInfo() {
		return paymentTerminalInfo;
	}

	public void setPaymentTerminalInfo(String paymentTerminalInfo) {
		this.paymentTerminalInfo = paymentTerminalInfo;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

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

	public String getTranAmount() {
		return tranAmount;
	}

	public void setTranAmount(String tranAmount) {
		this.tranAmount = tranAmount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBindCardAgrNo() {
		return bindCardAgrNo;
	}

	public void setBindCardAgrNo(String bindCardAgrNo) {
		this.bindCardAgrNo = bindCardAgrNo;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getServiceAmount() {
		return serviceAmount;
	}

	public void setServiceAmount(String serviceAmount) {
		this.serviceAmount = serviceAmount;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

}
