
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 解绑接口
 * 交易编码：R009
 * @author lixinji
 * 2021年3月3日 下午5:46:31
 */
public class NCardUnbindVo extends NCountBaseVo {

	private static final long serialVersionUID = 5841361238789236411L;

	public static final String[]	encryptArr	= new String[] { "oriBindCardAgrNo", "userId" };
	public static final String[]	verifyArr	= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode" };
	public static final String[]	submitArr	= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach",
	        "charset" };

	/**
	 * 原绑卡协议号
	 */
	private String oriBindCardAgrNo;

	/**
	 * 用户ID
	 */
	private String userId;

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

	public String getOriBindCardAgrNo() {
		return oriBindCardAgrNo;
	}

	public void setOriBindCardAgrNo(String oriBindCardAgrNo) {
		this.oriBindCardAgrNo = oriBindCardAgrNo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
