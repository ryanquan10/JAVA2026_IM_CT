
package org.tio.sitexxx.service.pay.impl.ncount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.pay.base.BaseCallbackPay;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.base.BaseRespFormatPay;
import org.tio.sitexxx.service.utils.ncount.NRequestUtils;
import org.tio.utils.json.Json;

public class PayNcountCallbackApi implements BaseCallbackPay<BasePayResp> {

	private static Logger log = LoggerFactory.getLogger(PayNcountCallbackApi.class);

	private BaseRespFormatPay formatPay = new PayNcountFormatApi();

	@Override
	public BasePayResp recharge(HttpRequest request, Integer uid) {
		try {
			Ret ret = NRequestUtils.callback(request, PayNcountConst.TranCode.RECHARGE_CONFIRM);
			BasePayResp resp = NRequestUtils.checkRet(ret);
			if (resp.isOk()) {
				resp.setResp(formatPay.rechargeCallback(resp.getResp(), uid));
				log.error("解析后的map:{}", Json.toJson(resp.getResp()));
			}
			return resp;
		} catch (Exception e) {
			log.error("", e);
		}
		return BasePayResp.fail("系统异常", "-1");
	}

	@Override
	public BasePayResp withhold(HttpRequest request, Integer uid) {
		try {
			Ret ret = NRequestUtils.callback(request, PayNcountConst.TranCode.WITHHOLD);
			BasePayResp resp = NRequestUtils.checkRet(ret);
			if (resp.isOk()) {
				resp.setResp(formatPay.withholdCallback(resp.getResp(), uid));
			}
			return resp;
		} catch (Exception e) {
			log.error("", e);
		}
		return BasePayResp.fail("系统异常", "-1");
	}

	@Override
	public BasePayResp sendRedpacket(HttpRequest request, Integer uid) {
		try {
			Ret ret = NRequestUtils.callback(request, PayNcountConst.TranCode.RECHARGE_CONFIRM);
			BasePayResp resp = NRequestUtils.checkRet(ret);
			if (resp.isOk()) {
				resp.setResp(formatPay.rechargeCallback(resp.getResp(), uid));
				log.error("解析后的map:{}", Json.toJson(resp.getResp()));
			}
			return resp;
		} catch (Exception e) {
			log.error("", e);
		}
		return BasePayResp.fail("系统异常", "-1");
	}

	@Override
	public BasePayResp rechargeAgainCallback(Object item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp rechargeQueryNoCheck(Object item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp redpacketAgainCallback(Object item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp withholdQueryNoCheck(Object item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp withholdAgainCallback(Object item) {
		// TODO Auto-generated method stub
		return null;
	}
}
