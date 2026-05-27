
package org.tio.sitexxx.service.vo;

/**
 * 本类定义的是全局Code，用户在各自业务中不要定义与本类相同的Code
 * @author tanyaowu 
 * 2016年9月15日 上午10:11:15
 */
public interface AppCode {
	/**
	 * 禁止用户操作的错误码
	 */
	interface ForbidOper {

		public static final int	START				= 1000;
		/**
		 * 没有登录
		 */
		public static final int	NOTLOGIN			= START + 1;
		/**
		 * 登录超时
		 */
		public static final int	TIMEOUT				= START + 2;
		/**
		 * 帐号在其它地方登录
		 */
		public static final int	KICKTED				= START + 3;
		/**
		 * 登录了，但是没有权限操作
		 */
		public static final int	NOTPERMISSION		= START + 4;
		/**
		 * 拒绝访问
		 */
		public static final int	REFUSE				= START + 5;
		/**
		 * 需要提供正确的access_token
		 */
		public static final int	NEED_ACCESS_TOKEN	= START + 6;
		/**
		 * 图形验证异常
		 */
		public static final int	CAPTCHA_ERROR		= START + 7;

		/**
		 * 数据不一致
		 */
		public static final int DATA_DIFFER = START + 8;

		/**
		 * pc帐号在其它地方登录
		 */
		public static final int KICKTED_PC = START + 9;

		/**
		 * H5帐号在其它地方登录
		 */
		public static final int KICKTED_H5 = START + 10;

		/**
		 * 试用账号
		 */
		public static final int TRIAL_ACCOUNT = START + 11;
		
		/**
		 * 未授权或者授权到期
		 */
		public static final int	NO_GRANT		= START + 99;

	}

	/**
	 * 通用错误
	 * @author lixinji
	 * 2020年7月7日 下午2:16:57
	 */
	interface GeneralCode {

		public static final int START = 2000;

		/**
		 * 系统错误
		 */
		public static final int SYS_ERROR = 1 + START;

		/**
		 * 参数错误
		 */
		public static final int PARAM_ERROR = 2 + START;
	}

	/**
	 * 数据库操作的错误码
	 */
	interface Db {
		public static final int	START			= 10000;
		/**
		 * 记录重复
		 */
		public static final int	RECORD_REPEAT	= 1 + START;
	}

	/**
	 * 好友相关错误码
	 * @author lixinji
	 * 2020年1月19日 下午6:27:46
	 */
	interface FriendErrorCode {

		public static final int START = 20000;

		/**
		 * 系统异常
		 */
		public static final int SYS_ERROR = START + 1;

		/**
		 * 拉黑状态
		 */
		public static final int	BLACK	= START + 2;
		/**
		 * 未关联-不是好友
		 */
		public static final int	NO_LINK	= START + 3;

		/**
		 * 好友异常
		 */
		public static final int USER_ERROR = START + 4;

		/**
		 * 全局禁言
		 */
		public static final int MSG_FORBIDDEN = START + 5;
	}

	interface GroupErrorCode {

		public static final int START = 30000;

		/**
		 * 系统异常
		 */
		public static final int SYS_ERROR = START + 1;

		/**
		 * 未关联-不在群组
		 */
		public static final int NO_LINK = START + 2;

		/**
		 * 群禁言
		 */
		public static final int MSG_FORBIDDEN = START + 5;
	}
}
