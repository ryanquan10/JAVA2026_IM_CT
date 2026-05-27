
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

import org.tio.sitexxx.service.vo.PayConst;

import cn.hutool.core.bean.BeanUtil;

/**
 * 发红包异步响应
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class RedpacketCallback5UResp {

	/**
	 * 
	 * GROUP_NORMAL 普通群红包
	 * ONE_TO_ONE 一对一
	 * GROUP_LUCK 拼手气红包
	 */
	private String packetType;

	/**
	 * 红包个数
	 */
	private String packetCount;

	/**
	 * 接收红包个数
	 */
	private String receivedCount;

	/**
	 * 接收红包金额
	 */
	private String receivedAmount;

	/**
	 * 接收红包ID
	 */
	private String receiveWalletId;

	/**
	 * 支付方式
	 * 银行卡支付：BANK_CARD
	 * 余额支付：BALANCE
	 * 支付宝支付： ZFB_PAY
	 *  微信支付：WX_PAY
	 */
	private String paymentType;

	/**
	 * 红包状态：已发送：SEND 成功：SUCCESS 取消：CANCEL 订单超时：TIMEOUT (只有已经支付过的订单有此状态) 失败：FAIL
	 */
	private String orderStatus;

	/**
	 * 创建时间
	 */
	private String createDateTime;

	/**
	 * 支付完成时间
	 */
	private String debitDateTime;

	/**
	 * 完成时间
	 */
	private String completeDateTime;

	/**
	 * 余额退回：BALANCE
	 * 银行卡退回：BANKCARD
	 */
	private String refundType;

	/**
	 * 退回金额
	 */
	private String refundAmount;

	/**
	 * 退回个数
	 */
	private String refundCount;

	/**
	 * 红包金额
	 */
	private String amount;

	/**
	 * 交易流水号
	 */
	private String serialNumber;

	/**
	 * 币种
	 */
	private String currency;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 发送者钱包 ID -Y
	 */
	private String walletId;

	/**
	 * 商户号-N
	 */
	private String merchantId;

	/**
	 * 请求id-N
	 */
	private String requestId;

	/**
	 * 参数-N
	 */
	private String hmac;

	/**
	 * 错误日志
	 */
	private String errorMessage;

	/**
	 * 订单错误信息
	 */
	private String orderErrorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWalletId() {
		return walletId;
	}

	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getHmac() {
		return hmac;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public String getPacketType() {
		return packetType;
	}

	public void setPacketType(String packetType) {
		this.packetType = packetType;
	}

	public String getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(String packetCount) {
		this.packetCount = packetCount;
	}

	public String getReceivedCount() {
		return receivedCount;
	}

	public void setReceivedCount(String receivedCount) {
		this.receivedCount = receivedCount;
	}

	public String getReceivedAmount() {
		return receivedAmount;
	}

	public void setReceivedAmount(String receivedAmount) {
		this.receivedAmount = receivedAmount;
	}

	public String getReceiveWalletId() {
		return receiveWalletId;
	}

	public void setReceiveWalletId(String receiveWalletId) {
		this.receiveWalletId = receiveWalletId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getDebitDateTime() {
		return debitDateTime;
	}

	public void setDebitDateTime(String debitDateTime) {
		this.debitDateTime = debitDateTime;
	}

	public String getCompleteDateTime() {
		return completeDateTime;
	}

	public void setCompleteDateTime(String completeDateTime) {
		this.completeDateTime = completeDateTime;
	}

	public String getRefundType() {
		return refundType;
	}

	public void setRefundType(String refundType) {
		this.refundType = refundType;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundCount() {
		return refundCount;
	}

	public void setRefundCount(String refundCount) {
		this.refundCount = refundCount;
	}

	public String getOrderErrorMessage() {
		return orderErrorMessage;
	}

	public void setOrderErrorMessage(String orderErrorMessage) {
		this.orderErrorMessage = orderErrorMessage;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:25
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("walletId", this.walletId);
		map.put("amount", this.amount);
		map.put("serialnumber", this.serialNumber);
		map.put("status", this.orderStatus);
		map.put("merchantId", this.merchantId);
		map.put("remark", this.remark);
		map.put("createDateTime", this.createDateTime);
		map.put("reqid", this.requestId);
		map.put("ordererrormsg", this.orderErrorMessage);
		return map;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:23
	 */
	public Map<String, Object> toAllMap() {
		Map<String, Object> map = BeanUtil.beanToMap(this);
		map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.REDPACKET_CALLBACK);
		return map;
	}

	public static RedpacketCallback5UResp toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new RedpacketCallback5UResp(), true);
	}
}
