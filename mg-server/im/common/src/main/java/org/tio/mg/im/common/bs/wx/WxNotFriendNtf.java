
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;

/**
 * 你们不是好友
 * 你发消息给对方时，你并不是对方的好友，这时候前端提示当前用户发送申请好友请求
 * @author tanyaowu
 */
public class WxNotFriendNtf implements Serializable {
	private static final long	serialVersionUID	= -5602454991852578446L;
	private Integer				uid;										//对端的userid
	/**
	 * 非好友类型
	 * 1、对方在当前用户的好友列表中，这时候前端需要走完整的申请好友流程（先申请加对方为好友，然后等对方同意）
	 * 2、当前用户在对方的好友列表中，这时候前端需要提示当前用户发送"/wx/friend/addFriendDirectly"请求，直接把对方加为好友
	 * 3、双方的好友列表都没有对方（你中无我，我中无你），前端处理逻辑同type=1
	 */
	private short				type				= 1;					//

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}
}
