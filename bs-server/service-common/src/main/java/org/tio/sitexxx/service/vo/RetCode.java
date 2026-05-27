
package org.tio.sitexxx.service.vo;

import java.util.Objects;

/**
 * 返回码
 * @author lixinji
 * 2021年6月17日 下午4:21:46
 */
public class RetCode {
	
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
	
	private static int commonCode = 100000;
	
	public enum CommonCode {
		
		/**
		 * 参数不正确
		 */
		PARAM_ERROR(commonCode + 1, "无效参数"),
		
		/**
		 * 业务不正确
		 */
		BIZ_ERROR(commonCode + 2, "业务不正确"),
		
		/**
		 * 业务数据不存在
		 */
		BIZ_NOT_EXIST(commonCode + 3, "记录不存在"),
		
		/**
		 * 业务数据已存在
		 */
		BIZ_EXIST(commonCode + 4, "记录已存在"),
		
		/**
		 * 业务数据已存在
		 */
		GRANT_ERROR(commonCode + 5, "业务权限不足"),
		
		/**
		 * 服务异常
		 */
		SYS_ERROR(commonCode + 0, "服务异常");

		public static CommonCode from(String code) {
			CommonCode[] values = CommonCode.values();
			for (CommonCode v : values) {
				if (Objects.equals(v.value, code)) {
					return v;
				}
			}
			return SYS_ERROR;
		}

		public Integer	code;
		public String	value;

		private CommonCode(Integer code, String value) {
			this.code = code;
			this.value = value;
		}
	}
	

	private static int loginCode = 200000;
	
	
	/**
	 * 登录失败才有的errorcode
	 */
	public enum LoginCode {
		/**
		 * 密码不正确
		 */
		PWD_ERROR(loginCode + 1, "密码不正确"),

		/**
		 * 用户名或验证码不正确，一般不应告诉用户具体是密码不对还是用户不存在
		 */
		USER_OR_PWD_ERROR(loginCode + 1, "用户名或验证码不正确"),
		
		/**
		 * 验证码不正确
		 */
		SMS_CODE_ERROR(loginCode + 2, "验证码不正确"),
		
		/**
		 * 无效状态用户 
		 */
		STATUS_ERROR(loginCode + 3, "无效状态用户"),
		
		/**
		 * 用户封停
		 */
		INBLACK_ERROR(loginCode + 4, "该账号已禁用"),

		/**
		 * 用户不存在
		 */
		USER_NO_EXIST(loginCode + 5, "账号未注册");

		public static LoginCode from(String code) {
			LoginCode[] values = LoginCode.values();
			for (LoginCode v : values) {
				if (Objects.equals(v.value, code)) {
					return v;
				}
			}
			return USER_OR_PWD_ERROR;
		}

		public Integer	code;
		public String	value;

		private LoginCode(Integer code, String value) {
			this.code = code;
			this.value = value;
		}
	}
	
	private static int userCode = 300000;
	
	/**
	 * 
	 * @author lixinji
	 * 2021年6月17日 下午4:54:39
	 */
	public enum UserCode {
		
		/**
		 * 无效状态用户
		 */
		STATUS_ERROR(userCode + 1, "无效状态用户"),
		
		/**
		 * 封停用户
		 */
		INBLACK_ERROR(userCode + 2, "封停用户"),
		
		/**
		 * 注销用户 
		 */
		LOGOUT_ERROR(userCode + 3, "注销用户");
		
		public static UserCode from(String code) {
			UserCode[] values = UserCode.values();
			for (UserCode v : values) {
				if (Objects.equals(v.value, code)) {
					return v;
				}
			}
			return STATUS_ERROR;
		}

		public Integer	code;
		public String	value;

		private UserCode(Integer code, String value) {
			this.code = code;
			this.value = value;
		}
	}

}
