
package org.tio.sitexxx.service.vo.topic;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * @author tanyaowu 2016年11月8日 下午4:02:58
 */
public class TopicVo implements Serializable {
	private static final long	serialVersionUID	= -3043791690152475450L;
	public static final String	CLIENTID			= UUID.randomUUID().toString();
	private Short				type				= null;
	private Serializable		value;
	private Map<String, Object>	params;
	private String				clientId			= CLIENTID;
	private boolean				forceRun			= false;

	public static interface Type {
		/**
		 * 清空数据字典
		 */
		short CLEAR_DICT = 1;

		/**
		 * 清空Conf和ConfDev
		 */
		short CLEAR_CONF = 3;

		/**
		 * 移除user缓存,params为uid
		 */
		short CLEAR_USER = 4;

		/**
		 * 清空所有用户
		 */
		short CLEAR_ALL_USER = 5;

		/**
		 * 清空配置
		 */
		short CLEAR_ALL_CONF = 6;

		/**
		 * ip白名单缓存清空
		 */
		short CLEAR_IP_WHITE_LIST = 9;

		/**
		 * ip黑名单缓存清空
		 */
		short CLEAR_IP_BLACK_LIST = 10;

		/**
		 * tioim黑名单缓存清空
		 */
		short CLEAR_TIOIM_BLACK_IP = 11;

		/**
		 * 设置系统参数
		 */
		short SET_SYSTEM_PARAM = 12;
	}

	/**
	 * 
	 * @author tanyaowu
	 */
	public TopicVo() {
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

	/**
	 * @return the forceRun
	 */
	public boolean isForceRun() {
		return forceRun;
	}

	/**
	 * @param forceRun the forceRun to set
	 */
	public void setForceRun(boolean forceRun) {
		this.forceRun = forceRun;
	}
}
