
package org.tio.sitexxx.im.common.bs.wx.chatitem;

import java.io.Serializable;

import org.tio.sitexxx.service.model.main.WxChatItems;

/**
 * 获取会话信息--响应-- Server-->Client
 * @author lixinji
 * 2020年3月10日 下午3:18:01
 */
public class WxChatItemInfoResp implements Serializable {
	private static final long	serialVersionUID	= -526032926464073384L;
	private WxChatItems			data;

	private Long chatlinkid;


	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public WxChatItems getData() {
		return data;
	}

	public void setData(WxChatItems data) {
		this.data = data;
	}
}
