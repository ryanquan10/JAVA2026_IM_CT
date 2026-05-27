
package org.tio.sitexxx.service.pay.impl.ncount;

import org.tio.sitexxx.service.vo.PayConst;

public class PayNcountConst extends PayConst {

	/**
	 * 新生接口code
	 * @author lixinji
	 * 2021年3月8日 下午3:08:23
	 */
	public static interface TranCode {

		/**
		 * 开户
		 */
		String OPEN_USER = "R010";

		/**
		 * 开户信息查询
		 */
		String QUERY_USER_INFO = "Q001";

		/**
		 * 绑卡
		 */
		String BIND_CARD = "R007";

		/**
		 * 绑卡确认
		 */
		String BIND_CARD_CONFIRM = "R008";

		/**
		 * 解绑
		 */
		String UNBIND_CARD = "R009";

		/**
		 * 充值
		 */
		String RECHARGE = "T007";

		/**
		 * 充值确定
		 */
		String RECHARGE_CONFIRM = "T008";

		/**
		 * 转账
		 */
		String	TRANSFER	= "T003";
		/**
		 * 提现
		 */
		String	WITHHOLD	= "T002";

		/**
		 * 交易查询
		 */
		String QUERY_TRADE_INFO = "Q002";

	}
}
