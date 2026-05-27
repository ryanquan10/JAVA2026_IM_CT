
package org.tio.sitexxx.service.vo;

public class PayConst {

	public static interface CallBackUrl {

		/**
		 * 充值回调
		 */
		String RECHARGE = Const.SITE + "/mytio/paycallback/recharge" + ".tio_x?uid=";

		/**
		 * 提现回调
		 */
		String WITHHOLD = Const.SITE + "/mytio/paycallback/withhold" + ".tio_x?uid=";

		/**
		 * 红包
		 */
		String REDPACKET = Const.SITE + "/mytio/paycallback/redpacket" + ".tio_x?uid=";
	}

	public static interface RedPackMode {
		/**
		 * 普通红包
		 */
		short NORMAL = 1;

		/**
		 * 手气红包
		 */
		short LUCK = 2;
	}

	/**
	 * 支付版本类型
	 * @author lixinji
	 * 2021年4月9日 下午2:51:25
	 */
	public static interface PayVersionType {
		/**
		 * 新生支付
		 */
		String PAY_NCOUNT = "1";

		/**
		 * 易支付
		 */
		String PAY_5U = "2";

		String LOCAL_WALLET = "3";
	}


	/**
	 * 红包标志状态
	 * @author lixinji
	 * 2021年3月15日 下午6:10:26
	 */
	public static interface RedPacketStatus {
		/**
		 * 处理中-发送中-SEND
		 */
		short	PROCESS	= 1;
		/**
		 * 初始化
		 */
		short	INIT	= 2;

		/**
		 * 支付中
		 */
		short PAYING = 3;

		/**
		 * 支付确认中
		 */
		short PAYING_CONFIRM = 4;

		/**
		 * 正常结束
		 */
		short SUCCESS = 5;

		/**
		 * 超时结束
		 */
		short TIMEOUT = 6;

		/**
		 * 取消结束
		 */
		short CANCEL = 7;

		/**
		 * 失败
		 */
		short FAIL = 8;

	}

	/**
	 * 红包分配状态
	 * @author lixinji
	 * 2021年3月17日 上午10:43:39
	 */
	public static interface RedRandomStatus {
		/**
		 * 已转账
		 */
		short SUCCESS = 1;

		/**
		 * 初始化
		 */
		short INIT = 2;

		/**
		 * 分配中
		 */
		short RANDOM = 3;

		/**
		 * 超时
		 */
		short TIMECOUT = 4;
	}

	/**
	 * 红包发送模型
	 * @author lixinji
	 * 2021年3月17日 上午10:43:39
	 */
	public static interface RedSendMode {
		/**
		 * 回调
		 */
		short CALLBACK = 1;

		/**
		 * 查询
		 */
		short QUERY = 2;

		/**
		 * 计划补偿
		 */
		short JOB = 3;

		/**
		 * 响应
		 */
		short RESP = 4;

	}

	/**
	 * 红包支付类型
	 * @author lixinji
	 * 2021年3月15日 下午4:11:26
	 */
	public static interface RedPayType {
		/**
		 * 余额
		 */
		short CNY = 1;

		/**
		 * 银行卡
		 */
		short BANKCARD = 2;

		/**
		 * 微信
		 */
		short WX_PAY = 3;

		/**
		 * 支付宝
		 */
		short ZFB_PAY = 4;
	}

	/**
	 * 钱包模型
	 * @author lixinji
	 * 2020年11月26日 下午1:47:44
	 */
	public static interface WalletMode {
		/**
		 * 充值
		 */
		short RECHARGE = 1;

		/**
		 * 提现
		 */
		short WIHTHOLD = 2;

		/**
		 * 红包
		 */
		short REDPACKET = 3;
	}

	/**
	 * 明细同步
	 * @author lixinji
	 * 2020年11月26日 下午2:34:01
	 */
	public static interface CoinSyn {
		/**
		 * 初始化
		 */
		short INIT = 1;

		/**
		 * 未处理
		 */
		short NO = 2;

		/**
		 * 成功
		 */
		short SUCCESS = 3;
	}

	/**
	 * 充值状态
	 * @author lixinji
	 * 2021年3月11日 上午11:52:16
	 */
	public static interface WalletChangeStatus {
		/**
		 * 初始化
		 */
		Short LOCAL = -1;

		/**
		 * 成功
		 */
		Short SUCCESS = 1;

		/**
		 * 未处理
		 */
		Short CONFIRM = 2;

		/**
		 * 
		 */
		Short FAIL = 3;
	}

	/**
	 * 查询同步
	 * @author lixinji
	 * 2020年11月26日 下午5:00:00
	 */
	public static interface QuerySyn {
		/**
		 * 回调成功
		 */
		short CALLBACK = 1;

		/**
		 * 否
		 */
		short NO = 2;

		/**
		 * 成功
		 */
		short SUCCESS = 3;
	}

	public static interface BankcardType {
		/**
		 * 借记卡
		 */
		short DEBITCARD = 1;

		/**
		 * 信用卡
		 */
		short CREDITCARD = 2;
	}

	/**
	 * api class名称
	 * @author lixinji
	 * 2020年11月3日 下午6:34:03
	 */
	public static interface ApiClassName {

		/**
		 * map的key
		 */
		String API_MAP_KEY = "apiclassname";

		/**
		 * 钱包信息
		 */
		String WALLET_INFO = "walletinfo";

		/**
		 * 充值
		 */
		String RECHARGE = "recharge";

		/**
		 * 充值查询
		 */
		String RECHARGE_QUERY = "rechargequery";

		/**
		 * 提现
		 */
		String WITHHOLD = "withhold";

		/**
		 * 提现查询
		 */
		String WITHHOLD_QUERY = "withholdquery";

		/**
		 * 发红包
		 */
		String REDPACKET = "redpacket";

		/**
		 * 抢红包
		 */
		String GRAB_REDPACKET = "grabredpacket";

		/**
		 * 充值回调
		 */
		String RECHARGE_CALLBACK = "rechargecallback";

		/**
		 * 提现回调
		 */
		String WITHHOLD_CALLBACK = "withholdcallback";

		/**
		 * 发红包回调
		 */
		String REDPACKET_CALLBACK = "redpacketcallback";

		/**
		 * 发红包查询
		 */
		String REDPACKET_QUERY = "redpacketquery";

	}
}
