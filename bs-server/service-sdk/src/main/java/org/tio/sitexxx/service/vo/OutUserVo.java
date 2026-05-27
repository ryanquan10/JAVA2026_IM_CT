
package org.tio.sitexxx.service.vo;

import java.io.Serializable;

/**
 * 外部用户vo
 * @author lixinji
 * 2021年4月15日 下午3:17:16
 */
public class OutUserVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1360666701266535819L;

	/**
	 * 操作码
	 * @author lixinji
	 * 2021年4月19日 上午11:22:28
	 */
	public static interface Oper {
		/**
		 * 创建
		 */
		short create = 1;

		/**
		 * 修改
		 */
		short update = 2;

		/**
		 * 删除
		 */
		short del = 3;
	}

	/**
	 * 唯一code/外部用户商户唯一标识
	 */
	private String unioncode;

	/**
	 * 昵称
	 */
	private String nick;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 操作：1：新增；2：修改；3：删除
	 */
	private Short oper;

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

	public String getUnioncode() {
		return unioncode;
	}

	public void setUnioncode(String unioncode) {
		this.unioncode = unioncode;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
