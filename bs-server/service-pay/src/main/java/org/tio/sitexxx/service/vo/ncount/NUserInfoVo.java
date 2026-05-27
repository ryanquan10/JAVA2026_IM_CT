
package org.tio.sitexxx.service.vo.ncount;

import java.util.Map;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 用户查询接口
 * 交易编码：Q001
 * @author lixinji
 * 2021年3月3日 下午4:23:15
 */
public class NUserInfoVo extends NCountBaseVo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6588424889444341475L;

	public static String USER_ID = "userId";

	public static final String[]	encryptArr	= new String[] { "userId" };
	public static final String[]	verifyArr	= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "userId",
	        "outUserId", "userStat", "auditStat", "balAmount", "bindCardAgrNoList" };
	public static final String[]	submitArr	= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach",
	        "charset" };

	/**
	 * 平台用户Id
	 */
	private String userId;

	@Override
	public Map<String, Object> getCommonRespParams() {
		Map<String, Object> map = super.getCommonRespParams();
		map.put(USER_ID, this.userId);
		return map;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

}
