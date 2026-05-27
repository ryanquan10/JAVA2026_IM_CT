
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 
 * 充值接口
 * 交易编码：T007
 * @author lixinji
 * 2021年3月6日 上午10:35:42
 */
public class NRechargeVo extends NCountBaseVo {

	private static final long		serialVersionUID	= 8911458857618988529L;
	public static final String[]	encryptArr			= new String[] { "tranAmount", "payType", "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType",
	        "identityCode", "bindCardAgrNo", "notifyUrl", "orderExpireTime", "userId", "receiveUserId", "merUserIp", "riskExpand", "goodsInfo", "subMerchantId", "divideFlag",
	        "divideDetail" };
	public static final String[]	verifyArr			= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode",
	        "ncountOrderId", "submitTime" };
	public static final String[]	submitArr			= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue",
	        "merAttach", "charset" };

	/**
	 * 支付金额
	 */
	private String tranAmount;

	/**
	 * 支付方式
	 */
	private String payType;

	/**
	 * 支付银行卡卡号
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
	 * 绑卡协议号
	 */
	private String bindCardAgrNo;

	/**
	 * 商户前台跳转地址
	 */
	private String frontUrl;

	/**
	 * 商户异步通知地址
	 */
	private String notifyUrl;

	/**
	 * 订单过期时长
	 */
	private String orderExpireTime;

	/**
	 * 商户用户ID
	 */
	private String userId;

	/**
	 * 商户用户IP
	 */
	private String merUserIp;

	/**
	 * 风控扩展信息
	 */
	private String riskExpand;

	/**
	 * 商品信息
	 */
	private String goodsInfo;

	/**
	 * 商户渠道进件ID
	 */
	private String subMerchantId;

	/**
	 * 是否分账
	 */
	private String divideFlag;

	/**
	 * 分账明细信息
	 */
	private String divideDetail;

	/**
	 * 收款方ID
	 */
	private String receiveUserId;

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

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
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

	public String getBindCardAgrNo() {
		return bindCardAgrNo;
	}

	public void setBindCardAgrNo(String bindCardAgrNo) {
		this.bindCardAgrNo = bindCardAgrNo;
	}

	public String getFrontUrl() {
		return frontUrl;
	}

	public void setFrontUrl(String frontUrl) {
		this.frontUrl = frontUrl;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getOrderExpireTime() {
		return orderExpireTime;
	}

	public void setOrderExpireTime(String orderExpireTime) {
		this.orderExpireTime = orderExpireTime;
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

	public String getRiskExpand() {
		return riskExpand;
	}

	public void setRiskExpand(String riskExpand) {
		this.riskExpand = riskExpand;
	}

	public String getGoodsInfo() {
		return goodsInfo;
	}

	public void setGoodsInfo(String goodsInfo) {
		this.goodsInfo = goodsInfo;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getDivideFlag() {
		return divideFlag;
	}

	public void setDivideFlag(String divideFlag) {
		this.divideFlag = divideFlag;
	}

	public String getDivideDetail() {
		return divideDetail;
	}

	public void setDivideDetail(String divideDetail) {
		this.divideDetail = divideDetail;
	}

	public String getReceiveUserId() {
		return receiveUserId;
	}

	public void setReceiveUserId(String receiveUserId) {
		this.receiveUserId = receiveUserId;
	}

}
