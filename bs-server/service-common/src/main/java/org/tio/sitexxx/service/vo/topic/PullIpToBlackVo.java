
/**
 * 
 */
package org.tio.sitexxx.service.vo.topic;

import java.io.Serializable;

/**
 * 将ip拉黑的vo
 * @author tanyaowu
 *
 */
public class PullIpToBlackVo implements Serializable {

	public static interface Type {
		/**
		 * 添加ip到黑名单
		 */
		Short ADD_BLACK_IP = 1;

		/**
		 * 
		 */
		Short DELETE_IP_FROM_BLACK = 2;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1993070969721194485L;

	/**
	 * 
	 */
	public PullIpToBlackVo() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	private String	ip;
	private String	remark;

	/**
	 * 操作类型，1：把ip加到黑名单中，2：把ip从黑名单中删除
	 */
	private Short type = PullIpToBlackVo.Type.ADD_BLACK_IP;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

}
