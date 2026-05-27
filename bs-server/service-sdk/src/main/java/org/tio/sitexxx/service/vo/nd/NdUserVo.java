
package org.tio.sitexxx.service.vo.nd;

import java.io.Serializable;

/**
 * 南大先腾用户vo
 * @author lixinji
 * 2021年4月15日 下午3:17:16
 */
public class NdUserVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5119551005631855236L;

	/**
	 * 唯一code/外部用户商户唯一标识
	 */
	private String userCode;

	/**
	 * 昵称
	 */
	private String userName;

	/**
	 * 手机号
	 */
	private String regCellPhone;

	/**
	 * 是否有效：T：有效；F:无效
	 */
	private String isValid;

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRegCellPhone() {
		return regCellPhone;
	}

	public void setRegCellPhone(String regCellPhone) {
		this.regCellPhone = regCellPhone;
	}

	public String getIsValid() {
		return isValid;
	}

	public void setIsValid(String isValid) {
		this.isValid = isValid;
	}
}
