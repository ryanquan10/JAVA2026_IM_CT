
/**
 *
 */
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;

/**
 * 离群通知。当某用户被T出群，或群被删除时，用户会收到这个通知
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxLeaveGroupNtf implements Serializable {
	private static final long	serialVersionUID	= 7940209197660733242L;
	private Long				groupid;									//离开了哪个群
	private Short				type				= null;					//1：主动退群；2：被T出群；3：群被删除

	public WxLeaveGroupNtf(Long groupid, Short type) {
		super();
		this.type = type;
		this.groupid = groupid;
	}

	public Long getGroupid() {
		return groupid;
	}

	public void setGroupid(Long groupid) {
		this.groupid = groupid;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

}
