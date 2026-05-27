
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

/**
 * 修改开户信息响应
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class UpdateOpen5UResp {

	/**
	 * 钱包状态-Y
	 */
	private String modifyStatus;

	/**
	 * 运营商认证-Y
	 */
	private String operatorRzStatus;

	/**
	 * 钱包 ID -Y
	 */
	private String walletId;

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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOperatorRzStatus() {
		return operatorRzStatus;
	}

	public void setOperatorRzStatus(String operatorRzStatus) {
		this.operatorRzStatus = operatorRzStatus;
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

	public String getWalletId() {
		return walletId;
	}

	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}

	public String getModifyStatus() {
		return modifyStatus;
	}

	public void setModifyStatus(String modifyStatus) {
		this.modifyStatus = modifyStatus;
	}

	public String getHmac() {
		return hmac;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("operatorRzStatus", this.operatorRzStatus);
		return map;
	}
}
