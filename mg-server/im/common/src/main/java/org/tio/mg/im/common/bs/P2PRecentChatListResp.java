
package org.tio.mg.im.common.bs;

import java.io.Serializable;
import java.util.List;

import org.tio.jfinal.plugin.activerecord.Record;


/**
 * 查询未读私聊消息数响应
 * @author tanyaowu 
 * 2016年9月8日 下午2:10:00
 */
public class P2PRecentChatListResp implements Serializable {
	private static final long serialVersionUID = 7799598518276654167L;
	
	private List<Record> list;

	public List<Record> getList() {
		return list;
	}

	public void setList(List<Record> list) {
		this.list = list;
	}
}
