
package org.tio.sitexxx.service.vo.topic;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * 群管理通知
 * @author lixinji
 * 2021年2月24日 下午2:42:40
 */
public class ImManagerTopicVo implements Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5815059526121334533L;
	public static final String	CLIENTID			= UUID.randomUUID().toString();
	private Short				type				= null;
	private Serializable		value;
	private Map<String, Object>	params;
	private String				clientId			= CLIENTID;

	public static interface Type {

		/**
		 * 群封停操作
		 */
		short GROUP_INBLACK_OPER = 12;
		
		/**
		 * 系统消息
		 */
		short SYS_MSG = 13;

	}

	/**
	 * 
	 * @author tanyaowu
	 */
	public ImManagerTopicVo() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the type
	 */
	public Short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Short type) {
		this.type = type;
	}

	public java.io.Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
