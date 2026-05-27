
package org.tio.mg.im.common.bs.wx.group;

import java.io.Serializable;
import java.util.List;

import org.tio.mg.service.model.main.WxGroupMsg;

/**
 * 获取群聊聊天记录--响应-- Server-->Client
 * @author xufei
 * 2020年3月10日 下午3:18:01
 */
public class WxGroupMsgResp implements Serializable {
	
	private static final long serialVersionUID = -526032926464073384L;
	
	private List<WxGroupMsg> data;
	
	private Long chatlinkid;
	
	private boolean lastPage = false; 
	
	public Long getChatlinkid() {
		return chatlinkid;
	}
	
	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public List<WxGroupMsg> getData() {
		return data;
	}

	public void setData(List<WxGroupMsg> data) {
		this.data = data;
	}

	public boolean isLastPage() {
		return lastPage;
	}

	public void setLastPage(boolean lastPage) {
		this.lastPage = lastPage;
	}
	
}
