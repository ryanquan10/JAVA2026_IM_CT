
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

import org.tio.sitexxx.service.vo.PayConst;

import cn.hutool.core.bean.BeanUtil;

/**
 * 抢红包同步响应
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class GrabRedpacket5UResp {

	/**
	 * 充值状态
	 */
	private String orderStatus;

	/**
	 * 创建时间
	 */
	private String completeDateTime;

	/**
	 * 红包金额
	 */
	private String amount;

	/**
	 * 交易流水号
	 */
	private String serialNumber;

	/**
	 * 钱包 ID -Y
	 */
	private String receiveWalletId;

	/**
	 * 响应状态-N
	 */
	private String status;

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

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getCompleteDateTime() {
		return completeDateTime;
	}

	public void setCompleteDateTime(String completeDateTime) {
		this.completeDateTime = completeDateTime;
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

	public String getReceiveWalletId() {
		return receiveWalletId;
	}

	public void setReceiveWalletId(String receiveWalletId) {
		this.receiveWalletId = receiveWalletId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:25
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("amount", this.amount);
		map.put("serialnumber", this.serialNumber);
		map.put("receiveWalletId", this.receiveWalletId);
		map.put("status", this.orderStatus);
		map.put("merchantId", this.merchantId);
		map.put("completeDateTime", this.completeDateTime);
		map.put("errmsg", this.errorMessage);
		map.put("reqid", this.requestId);
		return map;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:23
	 */
	public Map<String, Object> toAllMap() {
		Map<String, Object> map = BeanUtil.beanToMap(this);
		map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.GRAB_REDPACKET);
		return map;
	}
}
