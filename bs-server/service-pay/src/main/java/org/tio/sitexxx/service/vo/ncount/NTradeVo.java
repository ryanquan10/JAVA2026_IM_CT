
package org.tio.sitexxx.service.vo.ncount;

import java.util.Map;

import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

/**
 * Created by caimusong on 18/8/9.
 * Q002 - 交易查询
 */
public class NTradeVo extends NCountBaseVo {

	private static final long serialVersionUID = 1L;

	public static String TRAN_MER_ORDER_ID = "tranMerOrderId";

	public static final String[]	encryptArr	= new String[] { "tranMerOrderId", "queryType" };
	public static final String[]	verifyArr	= new String[] { "version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "tranMerOrderId",
	        "ncountOrderId", "orderStatus", "tranAmount" };
	public static final String[]	submitArr	= new String[] { "version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach",
	        "charset" };

	/**
	 * 交易商户订单号
	 */
	private String tranMerOrderId;

	/**
	 * 交易类型
	 */
	private String queryType;

	public Map<String, Object> getCommonRespParams() {
		Map<String, Object> map = super.getCommonRespParams();
		map.put(TRAN_MER_ORDER_ID, this.tranMerOrderId);
		return map;
	}

	@Override
	public String getEncryptJsonStr() {
		return getJsonStr(this, encryptArr);
	}

	@Override
	public String getVerifyJsonStr() {
		return getJsonStr(this, verifyArr);
	}

	@Override
	public String getSubmitJsonStr() {
		return getJsonStr(this, submitArr);
	}

	public String getTranMerOrderId() {
		return tranMerOrderId;
	}

	public void setTranMerOrderId(String tranMerOrderId) {
		this.tranMerOrderId = tranMerOrderId;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
}
