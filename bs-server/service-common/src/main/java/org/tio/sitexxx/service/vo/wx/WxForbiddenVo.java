
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;
import java.util.Objects;

/**
 * 禁言vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxForbiddenVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4911544167416109434L;

	/**
	 * 群id
	 */
	private Long groupid;

	/**
	 * 用户ids
	 */
	private Integer uid;

	/**
	 * 禁言类型：1：群；2：用户
	 */
	private Short mode;

	/**
	 * 禁言时长
	 */
	private Integer duration;

	/**
	 * 操作
	 */
	private Short oper;

	/**
	 * 禁言类型
	 * @author lixinji
	 * 2021年1月5日 上午10:18:10
	 */
	public static interface Mode {
		/**
		 * 用户时长禁言
		 */
		short USER = 1;

		/**
		 * 用户长久禁言
		 */
		short USER_LONGTERM = 3;

		/**
		 * 群禁言
		 */
		short ALL = 4;
	}

	/**
	 * 操作
	 * @author lixinji
	 * 2021年1月5日 上午10:34:26
	 */
	public static interface Oper {
		/**
		 * 禁言
		 */
		short FORBIDDEN = 1;

		/**
		 * 取消禁言
		 */
		short CANCEL = 2;
	}

	public Long getGroupid() {
		return groupid;
	}

	public void setGroupid(Long groupid) {
		this.groupid = groupid;
	}

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Short getMode() {
		return mode;
	}

	public void setMode(Short mode) {
		this.mode = mode;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2021年1月5日 上午10:23:25
	 */
	public boolean checkIsNull() {
		//必填
		if (groupid == null || mode == null || oper == null) {
			return true;
		}
		if (Objects.equals(oper, Oper.FORBIDDEN)) {
			//用户操作，必现有用户
			if (!Objects.equals(mode, Mode.ALL) && uid == null) {
				return true;
			}
			//时长禁言需要有时间
			if (Objects.equals(mode, Mode.USER) && (duration == null || duration <= 0)) {
				return true;
			}
		} else {
			//用户解除禁言必现有用户
			if (!Objects.equals(mode, Mode.ALL) && uid == null) {
				return true;
			}
		}
		return false;
	}
}
