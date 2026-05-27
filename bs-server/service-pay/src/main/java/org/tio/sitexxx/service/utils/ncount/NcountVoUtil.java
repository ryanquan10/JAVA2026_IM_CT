
package org.tio.sitexxx.service.utils.ncount;

import java.util.Map;

import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.pay.impl.ncount.PayNcountConst;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.AmountUtil;
import org.tio.sitexxx.service.vo.BindCardConfirmVo;
import org.tio.sitexxx.service.vo.BindCardVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.OpenUserVo;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.RechargeVo;
import org.tio.sitexxx.service.vo.TransferVo;
import org.tio.sitexxx.service.vo.UnBindCardVo;
import org.tio.sitexxx.service.vo.WithholdVo;
import org.tio.sitexxx.service.vo.ncount.NCardBindConfirmVo;
import org.tio.sitexxx.service.vo.ncount.NCardBindVo;
import org.tio.sitexxx.service.vo.ncount.NCardUnbindVo;
import org.tio.sitexxx.service.vo.ncount.NRechargeConfirmVo;
import org.tio.sitexxx.service.vo.ncount.NRechargeVo;
import org.tio.sitexxx.service.vo.ncount.NTradeVo;
import org.tio.sitexxx.service.vo.ncount.NTransferVo;
import org.tio.sitexxx.service.vo.ncount.NUserInfoVo;
import org.tio.sitexxx.service.vo.ncount.NUserOpenVo;
import org.tio.sitexxx.service.vo.ncount.NWithholdVoVo;
import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

import cn.hutool.core.map.MapUtil;

/**
 * 新生支付vo工具类
 * @author lixinji
 * 2021年3月4日 上午9:45:49
 */
public class NcountVoUtil {

	/**
	 *开户 
	 */
	public static final String R010_SUBMIT_URL = "https://ncount.hnapay.com/api/r010.htm";

	/**
	 * 开户信息查询接口
	 */
	public static final String Q001_SUBMIT_URL = "https://ncount.hnapay.com/api/q001.htm";

	/**
	 * 交易订单查询
	 */
	public static final String Q002_SUBMIT_URL = "https://ncount.hnapay.com/api/q002.htm";

	/**
	 * 绑卡
	 */
	public static final String	R007_SUBMIT_URL	= "https://ncount.hnapay.com/api/r007.htm";
	/**
	 * 绑卡确认接口
	 */
	public static final String	R008_SUBMIT_URL	= "https://ncount.hnapay.com/api/r008.htm";

	/**
	 * 解绑卡接口
	 */
	public static final String R009_SUBMIT_URL = "https://ncount.hnapay.com/api/r009.htm";

	/**
	 * 充值接口
	 */
	public static final String	T007_SUBMIT_URL	= "https://ncount.hnapay.com/api/t007.htm";
	/**
	 * 充值确认接口
	 */
	public static final String	T008_SUBMIT_URL	= "https://ncount.hnapay.com/api/t008.htm";

	/**
	 * 转账接口
	 */
	public static final String T003_SUBMIT_URL = "https://ncount.hnapay.com/api/t003.htm";

	/**
	 * 提现
	 */
	public static final String T002_SUBMIT_URL = "https://ncount.hnapay.com/api/t002.htm";

	public static NCountBaseVo getBaseVoByTranCode(String tranCode, Map<String, Object> params, Integer uid) {

		NCountBaseVo baseVo = null;
		switch (tranCode) {
		case PayNcountConst.TranCode.OPEN_USER:
			baseVo = getR010OpenVo(params, uid);
			break;
		case PayNcountConst.TranCode.BIND_CARD:
			baseVo = getR007CardBindVo(params, uid);
			break;
		case PayNcountConst.TranCode.BIND_CARD_CONFIRM:
			baseVo = getR008CardBindConfirmVo(params, uid);
			break;
		case PayNcountConst.TranCode.UNBIND_CARD:
			baseVo = getR009UnbindVo(params, uid);
			break;
		case PayNcountConst.TranCode.WITHHOLD:
			baseVo = getT002WithholdVo(params, uid);
			break;
		case PayNcountConst.TranCode.TRANSFER:
			baseVo = getT003TransferVo(params, uid);
			break;
		case PayNcountConst.TranCode.RECHARGE:
			baseVo = getT007RechargeVo(params, uid);
			break;
		case PayNcountConst.TranCode.RECHARGE_CONFIRM:
			baseVo = getT008RechargeConfirmVo(params, uid);
			break;
		case PayNcountConst.TranCode.QUERY_USER_INFO:
			baseVo = getQ001UserInfoVo(params, uid);
			break;
		case PayNcountConst.TranCode.QUERY_TRADE_INFO:
			baseVo = getQ002TradeVo(params, uid);
			break;
		default:
			break;
		}
		return baseVo;
	}

	/**
	 * 调整-开户信息
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午3:13:21
	 */
	private static NUserOpenVo getR010OpenVo(Map<String, Object> params, Integer uid) {
		OpenUserVo userVo = OpenUserVo.toBean(params);
		NUserOpenVo r001Vo = new NUserOpenVo();
		r001Vo.initCommonParams(PayNcountConst.TranCode.OPEN_USER, uid);
		r001Vo.setSubmitUrl(R010_SUBMIT_URL);
		r001Vo.setMerUserId(uid + "");
		r001Vo.setMobile(userVo.getMobile());
		r001Vo.setUserName(userVo.getName());
		r001Vo.setCertNo(userVo.getCardno());
		return r001Vo;
	}

	/**
	 * 调整-绑卡
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午5:38:48
	 */
	private static NCardBindVo getR007CardBindVo(Map<String, Object> params, Integer uid) {
		BindCardVo bindCardVo = BindCardVo.toBean(params);
		NCardBindVo r007Vo = new NCardBindVo();
		r007Vo.initCommonParams(PayNcountConst.TranCode.BIND_CARD, uid);
		r007Vo.setSubmitUrl(R007_SUBMIT_URL);
		r007Vo.setCardNo(bindCardVo.getBankcardno());
		r007Vo.setHolderName(bindCardVo.getName());
		r007Vo.setCardAvailableDate(bindCardVo.getAvailabledate());
		r007Vo.setCvv2(bindCardVo.getCvv2());
		r007Vo.setMobileNo(bindCardVo.getMobile());
		r007Vo.setIdentityType("1");
		r007Vo.setIdentityCode(bindCardVo.getCardno());
		r007Vo.setUserId(bindCardVo.getWalletid());
		r007Vo.setMerUserIp(bindCardVo.getIp());
		return r007Vo;
	}

	/**
	 * 确认绑卡
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午5:50:26
	 */
	private static NCardBindConfirmVo getR008CardBindConfirmVo(Map<String, Object> params, Integer uid) {
		BindCardConfirmVo bindCardConfirmVo = BindCardConfirmVo.toBean(params);
		NCardBindConfirmVo r008Vo = new NCardBindConfirmVo();
		r008Vo.initCommonParams(PayNcountConst.TranCode.BIND_CARD_CONFIRM, uid);
		r008Vo.setSubmitUrl(R008_SUBMIT_URL);
		r008Vo.setNcountOrderId(bindCardConfirmVo.getMerorderid());
		r008Vo.setSmsCode(bindCardConfirmVo.getSmscode());
		r008Vo.setMerUserIp(bindCardConfirmVo.getIp());
		return r008Vo;
	}

	/**
	 * 解绑
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午5:56:20
	 */
	private static NCardUnbindVo getR009UnbindVo(Map<String, Object> params, Integer uid) {
		UnBindCardVo cardVo = UnBindCardVo.toBean(params);
		NCardUnbindVo r009Vo = new NCardUnbindVo();
		r009Vo.initCommonParams(PayNcountConst.TranCode.UNBIND_CARD, uid);
		r009Vo.setSubmitUrl(R009_SUBMIT_URL);
		r009Vo.setOriBindCardAgrNo(cardVo.getAgrno());
		r009Vo.setUserId(cardVo.getWalletid());
		return r009Vo;
	}

	/**
	 * 提现
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午1:41:09
	 */
	private static NWithholdVoVo getT002WithholdVo(Map<String, Object> params, Integer uid) {
		WithholdVo withholdVo = WithholdVo.toBean(params);
		NWithholdVoVo t002Vo = new NWithholdVoVo();
		t002Vo.initCommonParams(PayNcountConst.TranCode.WITHHOLD, uid);
		t002Vo.setSubmitUrl(T002_SUBMIT_URL);
		t002Vo.setTranAmount(AmountUtil.intAmountToDouble(withholdVo.getAmount()) + "");
		t002Vo.setUserId(withholdVo.getWalletid());
		t002Vo.setBindCardAgrNo(withholdVo.getAgrno());
		t002Vo.setNotifyUrl(withholdVo.getNotifyUrl());
		t002Vo.setServiceAmount(AmountUtil.intAmountToDouble(withholdVo.getBizfee() + "") + "");
		t002Vo.setBusinessType("08");
		t002Vo.setCardNo("");
		t002Vo.setPaymentTerminalInfo(payDeviceInfo(withholdVo.getDevicetype(), withholdVo.getIpInfo()));
		t002Vo.setDeviceInfo(deviceInfo(withholdVo.getDevicetype(), withholdVo.getIpInfo()));
		return t002Vo;
	}

	private static NTransferVo getT003TransferVo(Map<String, Object> params, Integer uid) {
		TransferVo transferVo = TransferVo.toBean(params);
		NTransferVo t003Vo = new NTransferVo();
		t003Vo.initCommonParams(PayNcountConst.TranCode.TRANSFER, uid);
		t003Vo.setSubmitUrl(T003_SUBMIT_URL);
		t003Vo.setPayUserId(transferVo.getWalletid());
		t003Vo.setReceiveUserId(transferVo.getTowalletid());
		t003Vo.setTranAmount(AmountUtil.intAmountToDouble(transferVo.getCny()) + "");
		t003Vo.setBusinessType("02");
		return t003Vo;
	}

	/**
	 * 支付
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午6:47:45
	 */
	private static NRechargeVo getT007RechargeVo(Map<String, Object> params, Integer uid) {
		RechargeVo rechargeVo = RechargeVo.toBean(params);
		String timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5) + "";
		if (rechargeVo.getTimeout() != null) {
			timeout = rechargeVo.getTimeout() + "";
		}
		NRechargeVo t007Vo = new NRechargeVo();
		t007Vo.initCommonParams(PayNcountConst.TranCode.RECHARGE, uid);
		t007Vo.setSubmitUrl(T007_SUBMIT_URL);
		t007Vo.setTranAmount(AmountUtil.intAmountToDouble(rechargeVo.getAmount()) + "");
		t007Vo.setReceiveUserId(rechargeVo.getTowalletid());
		t007Vo.setDivideDetail("");
		t007Vo.setDivideFlag("");
		t007Vo.setSubMerchantId("2012221657332944330");//此值固定不可变
		t007Vo.setGoodsInfo("");
		t007Vo.setRiskExpand("");
		t007Vo.setMerUserIp(rechargeVo.getIp());
		t007Vo.setUserId(rechargeVo.getWalletid());
		t007Vo.setOrderExpireTime(timeout);
		t007Vo.setNotifyUrl(rechargeVo.getNotifyUrl());
		t007Vo.setFrontUrl("");
		t007Vo.setBindCardAgrNo(rechargeVo.getAgrno());
		t007Vo.setIdentityCode("");
		t007Vo.setIdentityType("");
		t007Vo.setMobileNo("");
		t007Vo.setCvv2("");
		t007Vo.setCardAvailableDate("");
		t007Vo.setHolderName("");
		t007Vo.setCardNo("");
		t007Vo.setPayType("3");//写死协议支付
		return t007Vo;
	}

	private static NRechargeConfirmVo getT008RechargeConfirmVo(Map<String, Object> params, Integer uid) {
		RechargeConfirmVo confirmVo = RechargeConfirmVo.toBean(params);
		NRechargeConfirmVo t008Vo = new NRechargeConfirmVo();
		t008Vo.initCommonParams(PayNcountConst.TranCode.RECHARGE_CONFIRM, uid);
		t008Vo.setSubmitUrl(T008_SUBMIT_URL);
		t008Vo.setNcountOrderId(confirmVo.getMerorderid());
		t008Vo.setSmsCode(confirmVo.getSmscode());
		t008Vo.setMerUserIp(confirmVo.getIp());
		t008Vo.setPaymentTerminalInfo(payDeviceInfo(confirmVo.getDevicetype(), confirmVo.getIpInfo()));
		t008Vo.setReceiverTerminalInfo(receiverDeviceInfo(confirmVo.getDevicetype(), confirmVo.getIpInfo()));
		t008Vo.setDeviceInfo(deviceInfo(confirmVo.getDevicetype(), confirmVo.getIpInfo()));
		t008Vo.setBusinessType("03");
		t008Vo.setFeeType("0");//0:客户无感知-无手续费
		t008Vo.setDivideAcctDtl("");
		t008Vo.setFeeAmountUser("");
		return t008Vo;
	}

	// 查询
	private static NUserInfoVo getQ001UserInfoVo(Map<String, Object> params, Integer uid) {
		NUserInfoVo q001Vo = new NUserInfoVo();
		q001Vo.initCommonParams(PayNcountConst.TranCode.QUERY_USER_INFO, uid);
		q001Vo.setUserId(MapUtil.getStr(params, "walletid"));
		q001Vo.setSubmitUrl(Q001_SUBMIT_URL);
		return q001Vo;
	}

	/**
	 * 交易查询
	 * @param params
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午3:01:09
	 */
	private static NTradeVo getQ002TradeVo(Map<String, Object> params, Integer uid) {
		NTradeVo q002Vo = new NTradeVo();
		q002Vo.initCommonParams(PayNcountConst.TranCode.QUERY_TRADE_INFO, uid);
		q002Vo.setTranMerOrderId(MapUtil.getStr(params, "reqid"));
		q002Vo.setSubmitUrl(Q002_SUBMIT_URL);
		q002Vo.setQueryType(MapUtil.getStr(params, "querytype"));
		return q002Vo;
	}

	public static String getVerifyDataByTranCode(String tranCode, Map<String, Object> responseMap) {

		StringBuffer sb = new StringBuffer();

		String[] verifyFields = getVerifyArrByTranCode(tranCode);

		for (int i = 0; i < verifyFields.length; i++) {
			sb.append(verifyFields[i]);
			sb.append("=[");
			sb.append(responseMap.get(verifyFields[i]) == null ? "" : responseMap.get(verifyFields[i]));
			sb.append("]");
		}

		return sb.toString();

	}

	private static String[] getVerifyArrByTranCode(String tranCode) {
		switch (tranCode) {
		case PayNcountConst.TranCode.OPEN_USER:
			return NUserOpenVo.verifyArr;
		case PayNcountConst.TranCode.BIND_CARD:
			return NCardBindVo.verifyArr;
		case PayNcountConst.TranCode.BIND_CARD_CONFIRM:
			return NCardBindConfirmVo.verifyArr;
		case PayNcountConst.TranCode.UNBIND_CARD:
			return NCardUnbindVo.verifyArr;
		case PayNcountConst.TranCode.WITHHOLD:
			return NWithholdVoVo.verifyArr;
		case PayNcountConst.TranCode.TRANSFER:
			return NTransferVo.verifyArr;
		case PayNcountConst.TranCode.RECHARGE:
			return NRechargeVo.verifyArr;
		case PayNcountConst.TranCode.RECHARGE_CONFIRM:
			return NRechargeConfirmVo.verifyArr;
		case PayNcountConst.TranCode.QUERY_USER_INFO:
			return NUserInfoVo.verifyArr;
		case PayNcountConst.TranCode.QUERY_TRADE_INFO:
			return NTradeVo.verifyArr;
		default:
			return null;
		}
	}

	/**
	 * 获取付款方终端信息
	 * @param device
	 * @param ipInfo
	 * @return
	 * @author lixinji
	 * 2021年3月9日 上午11:48:03
	 */
	private static String payDeviceInfo(Short device, IpInfo ipInfo) {
		return "02|";
	}

	/**
	 * 获取收款方终端信息
	 * @param device
	 * @param ipInfo
	 * @return
	 * @author lixinji
	 * 2021年3月9日 上午11:47:47
	 */
	private static String receiverDeviceInfo(Short device, IpInfo ipInfo) {
		return "02||CN|310016";
	}

	/**
	 * 获取支付设备信息
	 * @param device
	 * @param ipInfo
	 * @return
	 * @author lixinji
	 * 2021年3月9日 上午11:47:31
	 */
	private static String deviceInfo(Short device, IpInfo ipInfo) {
		return "115.227.196.117||||||";
	}

}
