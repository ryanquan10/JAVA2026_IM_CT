
package org.tio.sitexxx.web.server.controller.pay;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.service.impl.LocalWalletService;
import org.tio.sitexxx.service.pay.service.impl.Pay5uService;
import org.tio.sitexxx.service.pay.service.impl.PayStdService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.PayConst;

/**
 * 支付回调
 * 
 */
@RequestPath(value = "/paycallback")
public class PayCallbackController {

	private static Logger log = LoggerFactory.getLogger(PayCallbackController.class);

	private static BasePayService payService;

	static {
		if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.PAY_5U)) {
			payService = new Pay5uService();
		} else if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.PAY_NCOUNT)) {
			payService = new PayStdService();
		} else if (Objects.equals(Const.PAY_TYPE, PayConst.PayVersionType.LOCAL_WALLET)) {
			payService = new LocalWalletService();
		}  else {
			log.error("支付未配置版本类型：默认新生支付");
			payService = new PayStdService();
		}
	}

	/**
	 * 
	 * 
	 */
	public PayCallbackController() {
	}

	/**
	 * 充值接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:25:37
	 */
	@RequestPath(value = "/recharge")
	public String recharge(HttpRequest request, Integer uid) throws Exception {
		Ret ret = payService.rechargeCallback(request, uid);
		return RetUtils.getRetMsg(ret);
	}

	/**
	 * 提现接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:26:20
	 */
	@RequestPath(value = "/withhold")
	public String withhold(HttpRequest request, Integer uid) throws Exception {
		Ret ret = payService.withholdCallback(request, uid);
		return RetUtils.getRetMsg(ret);
	}

	/**
	 * 发红包接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:27:04
	 */
	@RequestPath(value = "/redpacket")
	public String redpacket(HttpRequest request, Integer uid) throws Exception {
		log.error("发红包回调 begin...");
		Ret ret = payService.redpacketCallback(request, uid);
		return RetUtils.getRetMsg(ret);
	}
}
