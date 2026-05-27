
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

import org.tio.sitexxx.service.vo.PayConst;

import cn.hutool.core.bean.BeanUtil;

/**
 * 提现查询响应
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class WithholdQuery5UResp {

	/**
	 * 充值状态
	 */
	private String orderStatus;

	/**
	 * 创建时间
	 */
	private String createDateTime;

	/**
	 * 完成时间
	 */
	private String completeDateTime;

	/**
	 * 
	 */
	private String bankCardNumber;

	/**
	 * 
	 */
	private String bankCode;

	/**
	 * 
	 */
	private String bankName;

	/**
	 * 余额
	 */
	private String amount;

	/**
	 * 实际到账
	 */
	private String arrivalAmount;

	/**
	 * 交易流水号
	 */
	private String serialNumber;

	/**
	 * 订单错误信息
	 */
	private String orderErrorMessage;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 钱包id-N
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getArrivalAmount() {
		return arrivalAmount;
	}

	public void setArrivalAmount(String arrivalAmount) {
		this.arrivalAmount = arrivalAmount;
	}

	public String getCompleteDateTime() {
		return completeDateTime;
	}

	public void setCompleteDateTime(String completeDateTime) {
		this.completeDateTime = completeDateTime;
	}

	public String getBankCardNumber() {
		return bankCardNumber;
	}

	public void setBankCardNumber(String bankCardNumber) {
		this.bankCardNumber = bankCardNumber;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getWalletId() {
		return walletId;
	}

	public void setWalletId(String walletId) {
		this.walletId = walletId;
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
		map.put("arrivalAmount", this.arrivalAmount);
		map.put("amount", this.amount);
		map.put("serialnumber", this.serialNumber);
		map.put("status", this.orderStatus);
		map.put("merchantId", this.merchantId);
		map.put("remark", this.remark);
		map.put("completeDateTime", this.completeDateTime);
		map.put("ordererrormsg", this.orderErrorMessage);
		map.put("reqid", this.requestId);
		map.put("bankcardnumber", this.bankCardNumber);
		map.put("bankcode", this.bankCode);
		map.put("bankname", this.bankName);
		map.put("walletId", this.walletId);
		return map;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:23
	 */
	public Map<String, Object> toAllMap() {
		Map<String, Object> map = BeanUtil.beanToMap(this);
		map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.WITHHOLD_QUERY);
		return map;
	}
}
