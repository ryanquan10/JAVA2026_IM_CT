
package org.tio.sitexxx.service.pay.impl.ncount;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacketLocal;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.base.BaseRespFormatPay;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.ncount.NRequestUtils;
import org.tio.sitexxx.service.utils.ncount.NcountVoUtil;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.GrabRedpacketVo;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;
import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;

import cn.hutool.core.map.MapUtil;

public class PayNcountApi implements BasePay<BasePayReq, BasePayResp> {

	private static Logger log = LoggerFactory.getLogger(PayNcountApi.class);

	private BaseRespFormatPay formatPay = new PayNcountFormatApi();

	@Override
	public Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom) {
		return null;
	}

	@Override
	public Map<String, Object> getConfParam() {
		return null;
	}

	/**
	 * 开户
	 */
	@Override
	public BasePayResp openUser(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.OPEN_USER;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.openUser(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp updateUser(BasePayReq payQuest, Integer uid) {
		return null;
	}

	@Override
	public BasePayResp bindBankCard(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.BIND_CARD;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.bindBankCard(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp bindBankCardConfirm(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.BIND_CARD_CONFIRM;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.bindBankCardConfirm(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp removeBankCard(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.UNBIND_CARD;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.removeBankCard(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp getWalletInfo(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.QUERY_USER_INFO;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.getWalletInfo(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp recharge(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.RECHARGE;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.recharge(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp rechargeConfirm(RechargeConfirmVo rechargeVo, Integer uid, String cny) {
		return null;
	}


	@Override
	public BasePayResp withhold(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.WITHHOLD;
		payQuest.getParams().put("bizfee", commission(MapUtil.getLong(payQuest.getParams(), "amount")));
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.withhold(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp transfer(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.TRANSFER;
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.transfer(resp.getResp(), uid));
		}
		return resp;
	}

	@Override
	public BasePayResp transfer(BasePayReq payQuest, Integer uid, String cny) {
		return null;
	}

	@Override
	public Ret payRedpacket(HttpRequest request, BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom) {
		return null;
	}

	@Override
	public BasePayResp rechargeQuery(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.QUERY_TRADE_INFO;
		payQuest.getParams().put("querytype", "RECV");
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		if (ret.isFail()) {
			BasePayResp resp = BasePayResp.fail(RetUtils.getRetMsg(ret), "-1");
			log.error("系统异常，{}", RetUtils.getRetMsg(ret));
			return resp;
		}
		BasePayResp resp = new BasePayResp();
		resp.setOk(true);
		resp.setResp(formatPay.rechargeQuery(RetUtils.getOkTData(ret), uid));
		return resp;
	}

	@Override
	public BasePayResp withholdQuery(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.QUERY_TRADE_INFO;
		payQuest.getParams().put("querytype", "PAY");
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		if (ret.isFail()) {
			BasePayResp resp = BasePayResp.fail(RetUtils.getRetMsg(ret), "-1");
			log.error("系统异常，{}", RetUtils.getRetMsg(ret));
			return resp;
		}
		BasePayResp resp = new BasePayResp();
		resp.setOk(true);
		resp.setResp(formatPay.withholdQuery(RetUtils.getOkTData(ret), uid));
		return resp;
	}

	@Override
	public BasePayResp rechargeConfirm(BasePayReq payQuest, Integer uid) {
		return null;
	}

	@Override
	public BasePayResp transferQuery(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.QUERY_TRADE_INFO;
		payQuest.getParams().put("querytype", "TRAN");
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		BasePayResp resp = NRequestUtils.checkRet(ret);
		if (resp.isOk()) {
			resp.setResp(formatPay.transferQuery(resp.getResp(), uid));
		}
		return resp;
	}

	/* 
	 * 手续费
	 */
	@Override
	public long commission(long amount) {
		if (amount <= 0) {
			return 0;
		}
		Integer rate = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION, 5);
		Integer withholdconst = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION_CONST, 50);
		long commission = amount * rate / 1000 + withholdconst;
		return commission;
	}

	@Override
	public Ret initRedpacket(SendRedpacketVo redpacketVo, Boolean isAtom) {
		return null;
	}

	@Override
	public WxWalletSendRedPacketLocal getRedPacketLockLocal(Integer rid, Boolean lock) {
		return null;
	}

	@Override
	public Ret updateRedPacketLock(WxWalletSendRedPacketLocal redPacket, Short status, boolean lock) {
		return null;
	}

	@Override
	public Ret grabRedpacketCallback(HttpRequest request, BasePayResp resp, GrabRedpacketVo grabVo, Boolean isAtom) {
		return null;
	}

	@Override
	public BasePayResp clientToken(BasePayReq payQuest, Integer uid) {
		return null;
	}

	@Override
	public BasePayResp sendRedpacket(BasePayReq payQuest, Integer uid) {
		return null;
	}

	@Override
	public BasePayResp redpacketQuery(BasePayReq payQuest, Integer uid) {
		String tranCode = PayNcountConst.TranCode.QUERY_TRADE_INFO;
		payQuest.getParams().put("querytype", "RECV");
		NCountBaseVo baseVo = NcountVoUtil.getBaseVoByTranCode(tranCode, payQuest.getParams(), uid);
		Ret ret = NRequestUtils.nRequest(baseVo);
		if (ret.isFail()) {
			BasePayResp resp = BasePayResp.fail(RetUtils.getRetMsg(ret), "-1");
			log.error("系统异常，{}", RetUtils.getRetMsg(ret));
			return resp;
		}
		BasePayResp resp = new BasePayResp();
		resp.setOk(true);
		resp.setResp(formatPay.redpacketQuery(RetUtils.getOkTData(ret), uid));
		return resp;
	}

	@Override
	public BasePayResp grabRedpacket(BasePayReq payQuest, Integer uid) {
		return null;
	}

}
