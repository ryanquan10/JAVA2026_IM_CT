
package org.tio.sitexxx.service.vo.nd;

import java.io.Serializable;

/**
 * 南大先腾用户vo
 * @author lixinji
 * 2021年4月15日 下午3:17:16
 */
public class NdLoginInfoResp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7009706367926600531L;

	private String code;

	private NdUserVo data;

	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public NdUserVo getData() {
		return data;
	}

	public void setData(NdUserVo data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
