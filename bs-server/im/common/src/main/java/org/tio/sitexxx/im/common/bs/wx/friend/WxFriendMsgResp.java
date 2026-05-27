
package org.tio.sitexxx.im.common.bs.wx.friend;

import java.io.Serializable;
import java.util.List;

import org.tio.sitexxx.service.model.main.WxFriendMsg;

/**
 * 获取两好友间聊天记录--响应-- Server-->Client
 * @author lixinji
 * 2020年3月10日 下午3:18:01
 */
public class WxFriendMsgResp implements Serializable {
	private static final long	serialVersionUID	= -526032926464073384L;
	private List<WxFriendMsg>	data;

	private Long chatlinkid;

	private boolean lastPage = false;

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public List<WxFriendMsg> getData() {
		return data;
	}

	public void setData(List<WxFriendMsg> data) {
		this.data = data;
	}

	public boolean isLastPage() {
		return lastPage;
	}

	public void setLastPage(boolean lastPage) {
		this.lastPage = lastPage;
	}

}
