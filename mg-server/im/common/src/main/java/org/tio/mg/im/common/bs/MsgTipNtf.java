
/**
 *
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;

/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class MsgTipNtf implements Serializable {

	private static final long serialVersionUID = -2529865883086901580L;

	public static interface Level {
		/**
		 * 友情提醒
		 */
		short INFO = 1;

		/**
		 * 警告
		 */
		short WARN = 2;
	}

	public static interface Code {
		/**
		 * 请登录
		 */
		short PLEASE_LOGIN = 1;

		/**
		 * 没权限
		 */
		short NO_PERMISSION = 2;

		/**
		 * 其它
		 */
		short OTHER = 99;
	}

	private String msg;

	private short level = Level.INFO;

	private Short code = null;

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public short getLevel() {
		return level;
	}

	public void setLevel(short level) {
		this.level = level;
	}

	public Short getCode() {
		return code;
	}

	public void setCode(Short code) {
		this.code = code;
	}
}
