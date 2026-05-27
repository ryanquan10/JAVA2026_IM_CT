
package org.tio.mg.im.common.bs.wx;


import java.io.Serializable;

import org.tio.mg.service.vo.SimpleUser;

/**
 *  服务器通知用户"有人请求加你为好友啦"-- Server-->Client
 * @author tanyaowu
 */
public class WxApplyFriendNtf implements Serializable {
	private static final long serialVersionUID = -5030663203373490706L;

	private SimpleUser from;

	public SimpleUser getFrom() {
		return from;
	}

	public void setFrom(SimpleUser from) {
		this.from = from;
	}
}
