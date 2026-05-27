
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.util.StrUtil;

/**
 * 开户响应信息
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class Open5UResp {

	/**
	 * 钱包状态-Y
	 */
	private String walletStatus;

	/**
	 * 运营商认证-Y
	 */
	private String operatorRzStatus;

	/**
	 * 钱包 ID -Y
	 */
	private String walletId;

	/**
	 * 实名认证-Y
	 */
	private String idCardRzStatus;

	/**
	 * 评分-Y
	 */
	private String riskScore;

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
	 * 用户uid-N
	 */
	private String merchantUserId;

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

	public String getWalletStatus() {
		return walletStatus;
	}

	public void setWalletStatus(String walletStatus) {
		this.walletStatus = walletStatus;
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

	public String getMerchantUserId() {
		return merchantUserId;
	}

	public void setMerchantUserId(String merchantUserId) {
		this.merchantUserId = merchantUserId;
	}

	public String getWalletId() {
		return walletId;
	}

	public void setWalletId(String walletId) {
		this.walletId = walletId;
	}

	public String getIdCardRzStatus() {
		return idCardRzStatus;
	}

	public void setIdCardRzStatus(String idCardRzStatus) {
		this.idCardRzStatus = idCardRzStatus;
	}

	public String getRiskScore() {
		return riskScore;
	}

	public void setRiskScore(String riskScore) {
		this.riskScore = riskScore;
	}

	public String getHmac() {
		return hmac;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("walletId", this.walletId);
		map.put("walletStatus", this.walletStatus);
		if (StrUtil.isNotBlank(getErrorMessage())) {
			map.put("errmsg", this.errorMessage);
		}
		map.put("operatorRzStatus", this.operatorRzStatus);
		map.put("idCardRzStatus", this.idCardRzStatus);
		map.put("riskScore", this.riskScore);
		return map;
	}
}
