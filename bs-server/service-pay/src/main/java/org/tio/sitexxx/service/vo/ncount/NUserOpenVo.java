
package org.tio.sitexxx.service.vo.ncount;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * 
 * 新生支付开户Vo
 * 交易编码：R001
 * @author lixinji
 * 2021年3月3日 下午4:18:45
 */
public class NUserOpenVo extends NCountBaseVo {

	private static final long		serialVersionUID	= 1L;
	public static final String[]	encryptArr			= new String[] { "merUserId", "mobile", "userName", "certNo" };
	public static final String[]	verifyArr			= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "userId" };
	public static final String[]	submitArr			= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue",
	        "merAttach", "charset" };

	private String	merUserId;	// 商户用户唯一标识
	private String	mobile;		// 用户手机号
	private String	userName;	//真实姓名
	private String	certNo;		//身份证号码

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

	public String getMerUserId() {
		return merUserId;
	}

	public void setMerUserId(String merUserId) {
		this.merUserId = merUserId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCertNo() {
		return certNo;
	}

	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}
}
