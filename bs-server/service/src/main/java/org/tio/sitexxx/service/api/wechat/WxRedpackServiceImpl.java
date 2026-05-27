
package org.tio.sitexxx.service.api.wechat;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.bean.result.WxPayRedpackQueryResult;
import com.github.binarywang.wxpay.constant.WxPayConstants.BillType;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.impl.WxPayServiceApacheHttpImpl;

/**
 * 重写查询处理
 * 
 *
 */
public class WxRedpackServiceImpl extends WxPayServiceApacheHttpImpl {

	/* 
	 * 签名处理
	 */
	public WxPayRedpackQueryResult queryRedpack(String mchBillNo) throws WxPayException {
		WxRedpackQueryRequest request = new WxRedpackQueryRequest();
		request.setMchBillNo(mchBillNo);
		request.setBillType(BillType.MCHT);
		request.checkAndSign(this.getConfig());

		String url = this.getPayBaseUrl() + "/mmpaymkttransfers/gethbinfo";
		String responseContent = this.post(url, request.toXML(), true);
		WxPayRedpackQueryResult result = BaseWxPayResult.fromXML(responseContent, WxPayRedpackQueryResult.class);
		result.checkResult(this, request.getSignType(), true);
		return result;
	}
}
