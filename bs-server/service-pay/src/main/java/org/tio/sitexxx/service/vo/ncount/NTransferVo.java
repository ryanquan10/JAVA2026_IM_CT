
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 转账接口
 * 交易编码：T003
 * @author lixinji
 * 2021年3月4日 下午5:16:31
 */
public class NTransferVo extends NCountBaseVo {

	private static final long		serialVersionUID	= -5184487010481871113L;
	public static final String[]	encryptArr			= new String[] { "payUserId", "receiveUserId", "tranAmount", "businessType" };
	public static final String[]	verifyArr			= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode" };
	public static final String[]	submitArr			= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue",
	        "merAttach", "charset" };

	/**
	 *  付款方用户编号
	 */
	private String payUserId;

	/**
	 * 收款方用户编号
	 */
	private String receiveUserId;

	/**
	 *  转账金额
	 */
	private String tranAmount;

	public static final String ORDER_STATUS = "orderStatus";

	/**
	 * 业务类型
	 */
	private String businessType;

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

	public String getPayUserId() {
		return payUserId;
	}

	public void setPayUserId(String payUserId) {
		this.payUserId = payUserId;
	}

	public String getReceiveUserId() {
		return receiveUserId;
	}

	public void setReceiveUserId(String receiveUserId) {
		this.receiveUserId = receiveUserId;
	}

	public String getTranAmount() {
		return tranAmount;
	}

	public void setTranAmount(String tranAmount) {
		this.tranAmount = tranAmount;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
}
