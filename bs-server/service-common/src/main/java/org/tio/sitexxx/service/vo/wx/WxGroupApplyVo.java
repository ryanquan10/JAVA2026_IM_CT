
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 群申请vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxGroupApplyVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1283024282068675588L;

	/**
	 * 群id
	 */
	private Long groupid;

	/**
	 * 申请人
	 */
	private Integer operuid;

	/**
	 * 申请文案
	 */
	private String applymsg;

	/**
	 * 申请id
	 */
	private Integer id;

	/**
	 * 申请状态
	 */
	private Short status;

	public Long getGroupid() {
		return groupid;
	}

	public void setGroupid(Long groupid) {
		this.groupid = groupid;
	}

	public Integer getOperuid() {
		return operuid;
	}

	public void setOperuid(Integer operuid) {
		this.operuid = operuid;
	}

	public String getApplymsg() {
		return applymsg;
	}

	public void setApplymsg(String applymsg) {
		this.applymsg = applymsg;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}
}
