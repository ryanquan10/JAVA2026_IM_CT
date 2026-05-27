
/**
 * 
 */
package org.tio.sitexxx.service.vo;

import java.util.Map;

/**
 * @author tanyaowu
 *
 */
public class ClearHttpCache implements java.io.Serializable {

	public static interface ClearType {
		/**
		 * 删除某些key的cache
		 */
		int	REMOVE	= 1;
		/**
		 * 清空cache
		 */
		int	CLEAR	= 2;
	}

	private static final long	serialVersionUID	= -6457851103454668529L;
	private String				path				= null;
	private Integer				userid				= null;
	private Map<String, Object>	param				= null;
	/**
	 * org.tio.sitexxx.service.vo.ClearHttpCache.ClearType
	 */
	private int					clearType			= 1;					//

	public Integer getUserid() {
		return userid;
	}

	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	public Map<String, Object> getParam() {
		return param;
	}

	public void setParam(Map<String, Object> param) {
		this.param = param;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getClearType() {
		return clearType;
	}

	public void setClearType(int clearType) {
		this.clearType = clearType;
	}

}
