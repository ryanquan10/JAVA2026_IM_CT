
package org.tio.sitexxx.web.server.vo;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpResponse;
import org.tio.sitexxx.service.model.main.User;

/**
 * @author tanyaowu
 * 2016年8月10日 上午10:19:22
 */
public class LoginResult {
	public enum Code {
		SUCCESS(1), FAIL(2);

		public static Code from(Integer code) {
			Code[] values = Code.values();
			for (Code v : values) {
				if (Objects.equals(v.value, code)) {
					return v;
				}
			}
			return SUCCESS;
		}

		Integer value;

		private Code(Integer value) {
			this.value = value;
		}
	}

	/**
	 * 登录失败才有的errorcode
	 */
	public enum ErrorCode {
		/**
		 * 用户名或密码不正确，一般不应告诉用户具体是密码不对还是用户不存在
		 */
		USER_OR_PWD_ERROR_PWD(1, "用户名或密码不正确"),

		/**
		 * 验证码不正确
		 */
		USER_OR_PWD_SMSCODE_PWD(1, "验证码不正确"),

		/**
		 * 用户名或验证码不正确，一般不应告诉用户具体是密码不对还是用户不存在
		 */
		USER_OR_PWD_ERROR_SMS(1, "用户名或验证码不正确"),

		/**
		 * 用户封停
		 */
		USER_INBLACK_ERROR(4, "该账号已禁用"),

		/**
		 * 无效状态用户 
		 */
		USER_STATUS_ERROR(3, "无效状态用户"),

		/**
		 * 用户不存在
		 */
		USER_NO_EXIST(5, "账号未注册");

		public static ErrorCode from(String code) {
			ErrorCode[] values = ErrorCode.values();
			for (ErrorCode v : values) {
				if (Objects.equals(v.value, code)) {
					return v;
				}
			}
			return USER_OR_PWD_ERROR_PWD;
		}

		public Integer	code;
		public String	value;

		private ErrorCode(Integer code, String value) {
			this.code = code;
			this.value = value;
		}
	}

	private static Logger log = LoggerFactory.getLogger(LoginResult.class);

	public static LoginResult fail(ErrorCode errorCode, HttpResponse httpResponse) {
		LoginResult ret = new LoginResult(LoginResult.Code.FAIL, null, httpResponse);
		ret.setErrorCode(errorCode);
		return ret;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("");
	}

	public static LoginResult success(User user, HttpResponse httpResponse) {
		LoginResult ret = new LoginResult(LoginResult.Code.SUCCESS, user, httpResponse);
		return ret;
	}

	private Code code;

	private ErrorCode errorCode;

	private User user = null;

	private HttpResponse httpResponse = null;

	private LoginResult(Code code, User user, HttpResponse httpResponse) {
		super();
		this.code = code;
		this.user = user;
		this.setHttpResponse(httpResponse);
	}

	public Code getCode() {
		return code;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public User getUser() {
		return user;
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
