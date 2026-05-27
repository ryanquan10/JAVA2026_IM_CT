
package org.tio.sitexxx.service.api.sms;

/**
 * 短信基础响应
 * @author lixinji
 * 2020年12月18日 下午1:46:07
 */
public class BaseSmsResultVo {

	/**
	 * 
	 */
	public BaseSmsResultVo() {

	}

	public static interface Code {
		/**
		 * 成功
		 */
		int SUCCESS = 1;

		/**
		 * 今天不能再发了
		 */
		int CAN_SENT_TODAY = -2;

		/**
		 * 内部异常
		 */
		int INNER_ERROR = -3;

		/**
		 * 其它业务
		 */
		int OTHER = -4;
	}

	public static interface ThreeCode {
		/**
		 * 成功
		 */
		String SUCCESS = "OK";

	}

	/**
	 * true: 发送成功
	 */
	private boolean isSuccess = false;

	/**
	 * 消息码(一般用于发送失败时表示的失败原因)
	 */
	private Integer code = Code.SUCCESS;

	/**
	 * 提供商code
	 */
	private String thirdCode = null;

	/**
	 * 提供商msg
	 */
	private String thirdMsg = null;

	/**
	 * 提供商的消费id
	 */
	private String thirdId = null;

	public BaseSmsResultVo(boolean result, Integer code) {
		super();
		this.setSuccess(result);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getThirdCode() {
		return thirdCode;
	}

	public void setThirdCode(String thirdCode) {
		this.thirdCode = thirdCode;
	}

	public String getThirdMsg() {
		return thirdMsg;
	}

	public void setThirdMsg(String thirdMsg) {
		this.thirdMsg = thirdMsg;
	}

	public String getThirdId() {
		return thirdId;
	}

	public void setThirdId(String thirdId) {
		this.thirdId = thirdId;
	}
}
