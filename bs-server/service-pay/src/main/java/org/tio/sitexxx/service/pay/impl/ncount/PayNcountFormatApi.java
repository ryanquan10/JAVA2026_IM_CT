
package org.tio.sitexxx.service.pay.impl.ncount;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.pay.base.BaseRespFormatPay;
import org.tio.sitexxx.service.utils.AmountUtil;
import org.tio.sitexxx.service.utils.ncount.NRequestUtils;
import org.tio.sitexxx.service.vo.PayConst;
import org.tio.utils.json.Json;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

public class PayNcountFormatApi implements BaseRespFormatPay {

	private static Logger log = LoggerFactory.getLogger(PayNcountFormatApi.class);

	private boolean isTest = true;

	@Override
	public Map<String, Object> openUser(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merid", getMerid());
		result.put("walletid", MapUtil.getStr(resp, "userId"));
		result.put("ordererrormsg", errMsg(resp));
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> updateUser(Map<String, Object> resp, Integer uid) {
		return resp;
	}

	@Override
	public Map<String, Object> bindBankCard(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> bindBankCardConfirm(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("agrno", MapUtil.getStr(resp, "bindCardAgrNo"));
		result.put("bankcode", MapUtil.getStr(resp, "bankCode"));
		result.put("cardtype", cardTypeChange(Short.parseShort(MapUtil.getStr(resp, "cardType"))));
		result.put("cardno", MapUtil.getStr(resp, "shortCardNo"));
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> removeBankCard(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> getWalletInfo(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("walletid", MapUtil.getStr(resp, "userId"));
		result.put("merid", getMerid());
		result.put("uid", uid);
		result.put("merstatus", MapUtil.getStr(resp, "userStat"));
		result.put("auditstatus", MapUtil.getStr(resp, "auditStat"));
		result.put("authstatus", MapUtil.getStr(resp, "authStat"));
		//		String banks = MapUtil.getStr(resp, "bindCardAgrNoList");
		//		List<Map<String, Object>> bankMaps = new ArrayList<Map<String,Object>>();
		//		if(StrUtil.isNotBlank(banks)) {
		//			List<HashMap> bankList = NJsonUtils.jsonArrayToList(banks, HashMap.class);
		//			if(CollectionUtil.isNotEmpty(bankList)) {
		//				for(Map<String, Object> bank : bankList) {
		//					Map<String, Object> bankMap = new HashMap<String, Object>();
		//					bankMap.put("agrno",bank.get("bindCardAgrNo"));
		//					bankMap.put("bankcode",bank.get("bankCode"));
		//					bankMap.put("cardno",bank.get("cardNo"));
		//					bankMaps.add(bank);
		//				}
		//			}
		//		}
		//		result.put("bankcards", bankMaps);
		result.put("cny", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "balAmount")));
		//		result.put("mercny", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "availableBalance")));
		result.put("ordererrormsg", errMsg(resp));
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> recharge(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		String bizcreattime = MapUtil.getStr(resp, "submitTime");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMddHHmmss"));
		}
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> rechargeCallback(Map<String, Object> req, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("merorderid", MapUtil.getStr(req, "ncountOrderId"));
		result.put("amount", AmountUtil.doubleAmountToInt(MapUtil.getStr(req, "tranAmount")));
		result.put("checkdate", MapUtil.getStr(req, "checkDate"));
		result.put("bankcode", MapUtil.getStr(req, "bankCode"));
		result.put("cardtype", cardTypeChange(Short.parseShort(MapUtil.getStr(req, "cardType"))));
		result.put("shortcardno", MapUtil.getStr(req, "shortCardNo"));
		result.put("agrno", MapUtil.getStr(req, "bindCardAgrNo"));
		result.put("merfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(req, "feeAmount")));
		String bizcreattime = MapUtil.getStr(req, "submitTime");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMddHHmmss"));
		}
		String bizcompletetime = MapUtil.getStr(req, "tranFinishTime");
		if (StrUtil.isNotBlank(bizcompletetime)) {
			result.put("bizcompletetime", DateUtil.parse(bizcompletetime, "yyyyMMddHHmmss"));
		}
		result.put("recvacctamount", AmountUtil.doubleAmountToInt(MapUtil.getStr(req, "recvAcctAmount")));
		result.put("ordererrormsg", errMsg(req));
		NRequestUtils.respFormat(req, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> rechargeConfirm(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("amount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "tranAmount")));
		result.put("checkdate", MapUtil.getStr(resp, "checkDate"));
		//		result.put("bankcode", MapUtil.getStr(resp, "bankCode"));
		//		result.put("cardtype", cardTypeChange(Short.parseShort(MapUtil.getStr(resp, "cardType"))));
		//		result.put("shortcardno", MapUtil.getStr(resp, "shortCardNo"));
		result.put("agrno", MapUtil.getStr(resp, "bindCardAgrNo"));
		String bizcreattime = MapUtil.getStr(resp, "submitTime");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMddHHmmss"));
		}
		result.put("ordererrormsg", errMsg(resp));
		//		result.put("bizcompletetime", DateUtil.parse(MapUtil.getStr(resp, "tranFinishTime"), "yyyyMMddHHmmss"));
		String completetime = MapUtil.getStr(resp, "tranFinishTime");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMddHHmmss"));
		}
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> rechargeQuery(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("reqid", MapUtil.getStr(resp, "tranMerOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("merstatus", MapUtil.getStr(resp, "orderStatus"));
		result.put("amount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "tranAmount")));
		result.put("merfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "feeAmount")));
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		result.put("querytype", MapUtil.getStr(resp, "businessType"));
		result.put("ordererrormsg", errMsg(resp));
		String completetime = MapUtil.getStr(resp, "orderFinishTm");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMddHHmmss"));
		}
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> sendRedpacket(Map<String, Object> resp, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> redpacketQuery(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("reqid", MapUtil.getStr(resp, "tranMerOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("merstatus", MapUtil.getStr(resp, "orderStatus"));
		result.put("cny", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "tranAmount")));
		result.put("merfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "feeAmount")));
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		result.put("querytype", MapUtil.getStr(resp, "businessType"));
		result.put("ordererrormsg", errMsg(resp));
		String completetime = MapUtil.getStr(resp, "orderFinishTm");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMddHHmmss"));
		}
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> grabRedpacket(Map<String, Object> resp, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> withhold(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("ordererrormsg", errMsg(resp));
		String bizcreattime = MapUtil.getStr(resp, "orderDate");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMdd"));
		}
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> withholdQuery(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("reqid", MapUtil.getStr(resp, "tranMerOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("merstatus", MapUtil.getStr(resp, "orderStatus"));
		result.put("amount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "tranAmount")));
		result.put("merfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "feeAmount")));
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		result.put("querytype", MapUtil.getStr(resp, "businessType"));
		String completetime = MapUtil.getStr(resp, "orderFinishTm");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMddHHmmss"));
		}
		result.put("ordererrormsg", errMsg(resp));
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> withholdCallback(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		String bizcreattime = MapUtil.getStr(resp, "orderDate");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMdd"));
		}
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		result.put("payacctamount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "payAcctAmount")));
		String completetime = MapUtil.getStr(resp, "tranFinishDate");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMdd"));
		}
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> transfer(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerid());
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		String bizcreattime = MapUtil.getStr(resp, "orderDate");
		if (StrUtil.isNotBlank(bizcreattime)) {
			result.put("bizcreattime", DateUtil.parse(bizcreattime, "yyyyMMdd"));
		}
		result.put("payacctamount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "payAcctAmount")));
		result.put("recvacctamount", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "recvAcctAmount")));
		result.put("ordererrormsg", errMsg(resp));
		NRequestUtils.respFormat(resp, result);
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public Map<String, Object> transferQuery(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("reqid", MapUtil.getStr(resp, "tranMerOrderId"));
		result.put("merorderid", MapUtil.getStr(resp, "ncountOrderId"));
		result.put("merstatus", MapUtil.getStr(resp, "orderStatus"));
		result.put("cny", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "tranAmount")));
		result.put("merfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "feeAmount")));
		result.put("bizfee", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "serviceAmount")));
		result.put("querytype", MapUtil.getStr(resp, "businessType"));
		String completetime = MapUtil.getStr(resp, "orderFinishTm");
		if (StrUtil.isNotBlank(completetime)) {
			result.put("bizcompletetime", DateUtil.parse(completetime, "yyyyMMddHHmmss"));
		}
		result.put("ordererrormsg", errMsg(resp));
		if (isTest) {
			log.error("响应格式化后的信息：{}", Json.toJson(result));
		}
		return result;
	}

	@Override
	public String getMerid() {
		return "300008795977";
	}

	private Short cardTypeChange(Short cardtype) {
		if (cardtype == null) {
			return PayConst.BankcardType.DEBITCARD;
		}
		if (Objects.equals(cardtype, (short) 1)) {
			return PayConst.BankcardType.DEBITCARD;
		}
		return PayConst.BankcardType.CREDITCARD;
	}

	/**
	 * @param resp
	 * @return
	 * @author lixinji
	 * 2021年3月19日 下午6:46:59
	 */
	private String errMsg(Map<String, Object> resp) {
		String errCode = MapUtil.getStr(resp, "errorCode");
		String errMsg = MapUtil.getStr(resp, "errorMsg");
		if (StrUtil.isNotBlank(errCode) && StrUtil.isNotBlank(errMsg)) {
			return errCode + ":" + errMsg;
		} else if (StrUtil.isNotBlank(errCode)) {
			return errCode;
		} else {
			return errMsg;
		}
	}
}
