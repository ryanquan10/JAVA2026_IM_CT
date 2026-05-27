
package org.tio.sitexxx.service.pay.impl.pay5u.resp;

import java.util.HashMap;
import java.util.Map;

import org.tio.sitexxx.service.vo.PayConst;

import cn.hutool.core.bean.BeanUtil;

/**
 * 钱包信息响应
 * @author lixinji
 * 2020年11月3日 下午6:31:35
 */
public class Wallet5UResp {

	/**
	 * 钱包状态
	 */
	private String walletStatus;

	/**
	 * 创建时间
	 */
	private String createDateTime;

	/**
	 * 钱包余额
	 */
	private String balance;

	/**
	 * 认证次数
	 */
	private String authTimes;

	/**
	 * 账户类型
	 */
	private String accountType;

	/**
	 * 注册风险评分
	 */
	private String riskScore;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 职业
	 */
	private String profession;

	/**
	 * 姓名
	 */
	private String nameDesc;

	/**
	 * 注册手机
	 */
	private String mobileDesc;

	/**
	 * 身份证号码
	 */
	private String idCardNoDesc;

	/**
	 * 证件类型
	 */
	private String idCardType;

	/**
	 * 设置密码状态-Y
	 */
	private String setUpPasswrod;

	/**
	 * 运营商认证-Y
	 */
	private String operatorRzStatus;

	/**
	 * 实名认证
	 */
	private String idCardRzStatus;

	/**
	 * 人像认证
	 */
	private String personRzStatus;

	/**
	 * uid
	 */
	private String merchantUserId;

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

	public String getWalletStatus() {
		return walletStatus;
	}

	public void setWalletStatus(String walletStatus) {
		this.walletStatus = walletStatus;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getAuthTimes() {
		return authTimes;
	}

	public void setAuthTimes(String authTimes) {
		this.authTimes = authTimes;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getRiskScore() {
		return riskScore;
	}

	public void setRiskScore(String riskScore) {
		this.riskScore = riskScore;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getNameDesc() {
		return nameDesc;
	}

	public void setNameDesc(String nameDesc) {
		this.nameDesc = nameDesc;
	}

	public String getMobileDesc() {
		return mobileDesc;
	}

	public void setMobileDesc(String mobileDesc) {
		this.mobileDesc = mobileDesc;
	}

	public String getIdCardNoDesc() {
		return idCardNoDesc;
	}

	public void setIdCardNoDesc(String idCardNoDesc) {
		this.idCardNoDesc = idCardNoDesc;
	}

	public String getIdCardType() {
		return idCardType;
	}

	public void setIdCardType(String idCardType) {
		this.idCardType = idCardType;
	}

	public String getSetUpPasswrod() {
		return setUpPasswrod;
	}

	public void setSetUpPasswrod(String setUpPasswrod) {
		this.setUpPasswrod = setUpPasswrod;
	}

	public String getIdCardRzStatus() {
		return idCardRzStatus;
	}

	public void setIdCardRzStatus(String idCardRzStatus) {
		this.idCardRzStatus = idCardRzStatus;
	}

	public String getPersonRzStatus() {
		return personRzStatus;
	}

	public void setPersonRzStatus(String personRzStatus) {
		this.personRzStatus = personRzStatus;
	}

	public String getMerchantUserId() {
		return merchantUserId;
	}

	public void setMerchantUserId(String merchantUserId) {
		this.merchantUserId = merchantUserId;
	}

	public String getHmac() {
		return hmac;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:25
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("walletId", this.walletId);
		map.put("idCardRzStatus", this.idCardRzStatus);
		map.put("operatorRzStatus", this.operatorRzStatus);
		map.put("personRzStatus", this.personRzStatus);
		map.put("setUpPasswrod", this.setUpPasswrod);
		map.put("idCardNoDesc", this.idCardNoDesc);
		map.put("balance", this.balance);
		map.put("mobileDesc", this.mobileDesc);
		map.put("nameDesc", this.nameDesc);
		map.put("errmsg", this.errorMessage);
		return map;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午5:10:23
	 */
	public Map<String, Object> toAllMap() {
		Map<String, Object> map = BeanUtil.beanToMap(this);
		map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.WALLET_INFO);
		return map;
	}
}
