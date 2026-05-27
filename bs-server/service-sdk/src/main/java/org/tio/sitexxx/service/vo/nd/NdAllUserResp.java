
package org.tio.sitexxx.service.vo.nd;

import java.io.Serializable;
import java.util.List;

/**
 * 南大先腾用户vo
 * @author lixinji
 * 2021年4月15日 下午3:17:16
 */
public class NdAllUserResp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5422450370150174751L;

	private String code;

	private List<NdUserVo> data;

	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<NdUserVo> getData() {
		return data;
	}

	public void setData(List<NdUserVo> data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
