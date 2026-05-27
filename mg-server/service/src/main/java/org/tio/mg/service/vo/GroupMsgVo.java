
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 */package org.tio.mg.service.vo;

import java.io.Serializable;
import java.util.List;

import org.tio.jfinal.plugin.activerecord.Record;

/**
 * 群模型消息展示vo
 * @author xufei
 * 2020年5月27日 下午1:48:56
 */
public class GroupMsgVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4128547144370124806L;


	/**
	 * 群信息
	 */
	private Record groupInfo;
	
	
	/**
	 * 群消息
	 */
	private List<Record> groupMsg;
	
	/**
	 * 消息条数
	 */
	private Integer msgSize;

	public Record getGroupInfo() {
		return groupInfo;
	}

	public void setGroupInfo(Record groupInfo) {
		this.groupInfo = groupInfo;
	}

	public List<Record> getGroupMsg() {
		return groupMsg;
	}

	public void setGroupMsg(List<Record> groupMsg) {
		this.groupMsg = groupMsg;
	}

	public Integer getMsgSize() {
		return msgSize;
	}

	public void setMsgSize(Integer msgSize) {
		this.msgSize = msgSize;
	}
}