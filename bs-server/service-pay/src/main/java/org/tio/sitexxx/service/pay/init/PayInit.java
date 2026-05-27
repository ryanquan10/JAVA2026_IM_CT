
package org.tio.sitexxx.service.pay.init;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.service.impl.LocalWalletService;
import org.tio.sitexxx.service.pay.service.impl.Pay5uService;
import org.tio.sitexxx.service.pay.service.impl.PayStdService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.PayConst;

public class PayInit {
	private static Logger log = LoggerFactory.getLogger(PayInit.class);

	public static BasePayService payService;

	static {
		if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.PAY_5U)) {
			payService = new Pay5uService();
		} else if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.PAY_NCOUNT)) {
			payService = new PayStdService();
		} else if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.LOCAL_WALLET)) {
			payService = new LocalWalletService();
		} else {
			log.error("支付未配置版本类型：默认新生支付");
			payService = new PayStdService();
		}
	}

	/**
	 * 
	 */
	public PayInit() {
	}
}
