
package org.tio.sitexxx.service.pay.base;

import org.tio.http.common.HttpRequest;

/**
 * 支付接口
 * @param <payQuest>
 * @param <Resp>
 * @author lixinji
 * 2020年10月27日 上午11:20:10
 * @param <req>
 * @param <Resp>
 */
public interface BaseCallbackPay<Resp extends BasePayResp> {

	/**
	 * 
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午7:37:00
	 */
	Resp recharge(HttpRequest request, Integer uid);

	/**
	 * 提现回调
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:11:15
	 */
	Resp withhold(HttpRequest request, Integer uid);

	/**
	 * 发红包回调
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月18日 下午6:08:30
	 */
	Resp sendRedpacket(HttpRequest request, Integer uid);

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:12:28
	 */
	Resp rechargeAgainCallback(Object item);

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:13:17
	 */
	Resp rechargeQueryNoCheck(Object item);

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:21:55
	 */
	Resp redpacketAgainCallback(Object item);

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:23:15
	 */
	Resp withholdQueryNoCheck(Object item);

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:26:42
	 */
	Resp withholdAgainCallback(Object item);
}
