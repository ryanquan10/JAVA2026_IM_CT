/*
 * fyrqlmrgoncz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动lgrntyebeugddk
 */
package org.tio.clu.client;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.BindType;
import org.tio.clu.common.bs.UpdateBsNodeReq;
import org.tio.utils.lock.SetWithLock;

/**
 * @author tanyaowu 2020-9-7 19:10:47
 */
public class BindedData {
	private static Logger log = LoggerFactory.getLogger(BindedData.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	private UpdateBsNodeReq		lastUpdateBsNodeReq	= null;
	public SetWithLock<String>	uids				= null;
	public SetWithLock<String>	groups				= null;
	public SetWithLock<String>	tokens				= null;
	public SetWithLock<String>	bsids				= null;
	public SetWithLock<String>	ips					= null;
	public SetWithLock<String>	channelids			= null;

	/**
	 * 
	 * @author tanyaowu
	 */
	public BindedData() {
	}

	public void clean() {
		uids = null;
		groups = null;
		tokens = null;
		bsids = null;
		ips = null;
		channelids = null;

		lastUpdateBsNodeReq = null;
	}

	public boolean contains(BindType bindType, String v) {
		SetWithLock<String> set = getBindSet(bindType);
		if (set == null) {
			return false;
		}

		return set.contains(v);
	}

	public SetWithLock<String> getBindSet(BindType bindType) {
		switch (bindType) {
		case User:
			return uids;
		case Group:
			return groups;
		case Token:
			return tokens;
		case BsId:
			return bsids;
		case Ip:
			return ips;
		case ChannelId:
			return channelids;
		default:
			log.error("can not find by BindType[{}]", bindType);
			return null;
		}
	}

	/**
	 * @return the lastUpdateBsNodeReq
	 */
	public UpdateBsNodeReq getLastUpdateBsNodeReq() {
		return lastUpdateBsNodeReq;
	}

	public void init() {
		uids = new SetWithLock<>(new HashSet<String>());
		groups = new SetWithLock<>(new HashSet<String>());
		tokens = new SetWithLock<>(new HashSet<String>());
		bsids = new SetWithLock<>(new HashSet<String>());
		ips = new SetWithLock<>(new HashSet<String>());
		channelids = new SetWithLock<>(new HashSet<String>());
	}

	/**
	 * @param lastUpdateBsNodeReq the lastUpdateBsNodeReq to set
	 */
	public void setLastUpdateBsNodeReq(UpdateBsNodeReq lastUpdateBsNodeReq) {
		this.lastUpdateBsNodeReq = lastUpdateBsNodeReq;
	}
}
