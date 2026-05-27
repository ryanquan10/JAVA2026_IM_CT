
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 同步记录Vo
 * @author lixinji
 * 2020年9月8日 下午5:15:57
 */
public class SynRecordVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 841445948608235787L;

	/**
	 * 记录业务类型
	 */
	private Short biztype;

	public static interface BizType {

		/**
		 * 聊天会话
		 */
		short CHAT_SESSION = 1;

		/**
		 * 聊天会话的消息
		 */
		short CHAT_SESSION_MSG = 2;

		/**
		 * 好友信息
		 */
		short FRIEND = 3;

		/**
		 * 好友申请
		 */
		short FRIEND_APPLY = 4;
	}

	/**
	 * 记录同步类型：1：新增；2：修改；3：删除;4:所有记录
	 */
	private Short syntype;

	/**
	 * 同步类型
	 * @author lixinji
	 * 2020年9月18日 下午1:44:05
	 */
	public static interface SynType {

		/**
		 * 新增
		 */
		short ADD = 1;

		/**
		 * 更新
		 */
		short UPDATE = 2;

		/**
		 * 所有记录修改(状态修改)
		 */
		short ALL_UPDATE = 3;

		/**
		 * 删除
		 */
		short DEL = 4;

		/**
		 * 删除所有
		 */
		short DEL_ALL = 5;
	}

	/**
	 * 同步数据
	 */
	private Map<String, Object> bizdata;

	/**
	 * @param biztype
	 * @param syntype
	 * @param bizdata
	 */
	public SynRecordVo(Short biztype, Short syntype, Map<String, Object> bizdata) {
		this.bizdata = bizdata;
		this.biztype = biztype;
		this.syntype = syntype;
	}

	public SynRecordVo(Object bizdata, Short biztype, Short syntype) {
		this.bizdata = BeanUtil.beanToMap(bizdata);
		this.biztype = biztype;
		this.syntype = syntype;
	}

	/**
	 * 
	 */
	public SynRecordVo() {

	}

	public Short getBiztype() {
		return biztype;
	}

	public void setBiztype(Short biztype) {
		this.biztype = biztype;
	}

	public Short getSyntype() {
		return syntype;
	}

	public void setSyntype(Short syntype) {
		this.syntype = syntype;
	}

	public Map<String, Object> getBizdata() {
		return bizdata;
	}

	public void setBizdata(Map<String, Object> bizdata) {
		this.bizdata = bizdata;
	}

	/**
	 * @param key
	 * @param value
	 * @author lixinji
	 * 2020年9月15日 下午6:03:38
	 */
	public void put(String key, Object value) {
		if (this.bizdata == null) {
			this.bizdata = new HashMap<String, Object>();
		}
		bizdata.put(key, value);
	}
}
