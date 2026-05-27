
package org.tio.sitexxx.service.pay.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.pay.base.*;
import org.tio.sitexxx.service.pay.impl.local.PayLocalApi;
import org.tio.sitexxx.service.pay.impl.local.PayLocalCallBackApi;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5UConst;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5uApi;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5uCallBackApi;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.RedpacketQuery5UResp;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.*;
import org.tio.utils.json.Json;

import java.util.*;

/**
 * 支付服务
 * 
 * @author lixinji 2020年09月25日 下午5:57:32
 */
public class LocalWalletService implements BasePayService {

	private static Logger log = LoggerFactory.getLogger(LocalWalletService.class);

	public static BasePay<BasePayReq, BasePayResp> basePay;

	public static BaseCallbackPay<BasePayResp> baseCallbackPay;

	public static void initPay() {
		basePay = new PayLocalApi();
		baseCallbackPay = new PayLocalCallBackApi();
	}

	public LocalWalletService() {
		initPay();
	}

	/**
	 * 实名信息
	 * @param uid
	 * @return
	 * @author lixinji 2021年3月10日 下午5:44:39
	 */
	@Override
	public Ret realInfo(Integer uid) {
		throw new RuntimeException("本地钱包暂不支持本地实名信息");
	}

	/**
	 * 开户
	 * 
	 * @param openVo
	 * @return
	 * @author lixinji 2020年11月3日 下午5:57:12
	 */
	@Override
	public Ret openUser(OpenUserVo openVo, HttpRequest request) {
		if (openVo == null) {
			log.error("开户信息为空");
			return RetUtils.failMsg("参数异常");
		} 
		if (openVo.getMobile() == null) {
			return RetUtils.failMsg("开户手机号为空");
		}
		if (openVo.getCardno() == null) {
			return RetUtils.failMsg("开户身份证为空");
		}
		if (openVo.getName() == null) {
			return RetUtils.failMsg("开户姓名为空");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(openVo.toMap());
		BasePayResp resp = basePay.openUser(req, openVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
//		return RetUtils.failMsg("你暂无内测资格");
	}

	/**
	 * 银行卡列表
	 * 
	 * @param uid
	 * @return
	 * @author lixinji 2021年3月12日 上午11:13:29
	 */
	@Override
	public Ret bankcardList(Integer uid) {
		throw new RuntimeException("本地钱包暂不支持查询本地银行卡列表信息");
	}

	/**
	 * 绑卡
	 * 
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月10日 下午5:51:44
	 */
	@Override
	public Ret bindCard(BindCardVo cardVo, HttpRequest request) {

		WxUserBankCard wxUserBankCard = new WxUserBankCard();
		WxUserBankCard userBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", cardVo.getUid());
		if (userBankCard == null) {
			wxUserBankCard.setUid(cardVo.getUid());
			wxUserBankCard.setCardno(cardVo.getCardno());
			wxUserBankCard.setBankname(cardVo.getBankname());
			wxUserBankCard.setUsername(cardVo.getName());
			wxUserBankCard.setPhone(cardVo.getMobile());
			wxUserBankCard.setCardtype((short) 2);
			wxUserBankCard.setStatus((short) 1);
			wxUserBankCard.setCreatetime(new Date());
			boolean save = wxUserBankCard.save();
			if (!save) {
				return RetUtils.failMsg("操作失败, 请重试");
			}
		} else {
			wxUserBankCard = userBankCard;
			wxUserBankCard.setBankname(cardVo.getBankname());
			wxUserBankCard.setCardno(cardVo.getCardno());
			wxUserBankCard.setUsername(cardVo.getName());
			wxUserBankCard.setUpdatetime(new Date());
			boolean update = wxUserBankCard.update();
			if (!update) {
				return RetUtils.failMsg("操作失败, 请重试");
			}
		}

		return RetUtils.okData(wxUserBankCard);
	}

	/**
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午9:59:15
	 */
	@Override
	public Ret unBindCard(UnBindCardVo cardVo, HttpRequest request) {
		WxUserBankCard userBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", cardVo.getUid());
		boolean delete = userBankCard.delete();
		if (!delete) {
			return RetUtils.failMsg("操作失败,请重试");
		}
		return RetUtils.okData("解绑成功");
	}

	/**
	 * 绑卡确定
	 * 
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月10日 下午6:14:54
	 */
	@Override
	public Ret bindCardConfirm(BindCardConfirmVo cardVo, HttpRequest request) {
		throw new RuntimeException("本地钱包暂不支持本地解绑绑定银行卡");
	}

	/**
	 * 修改开户信息
	 * 
	 * @param openVo
	 * @return
	 * @author lixinji 2020年11月12日 下午3:28:16
	 */
	@Override
	public Ret updateOpenUser(UpdateOpenVo openVo, HttpRequest request) {
		if (openVo == null) {
			log.error("开户信息为空");
			return RetUtils.failMsg("参数异常");
		}
		log.error("开户信息：{}", Json.toJson(openVo));
		BasePayReq req = new BasePayReq(request);
		req.setParams(openVo.toMap());
		BasePayResp resp = basePay.updateUser(req, openVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 钱包信息
	 * 
	 * @param uid
	 * @param walletid
	 * @return
	 * @author lixinji 2020年11月15日 下午5:56:34
	 */
	@Override
	public Ret getWalletInfo(WalletVo walletVo, HttpRequest request) {
		if (walletVo.getUid() == null || StrUtil.isBlank(walletVo.getWalletid())) {
			log.error("获取钱包信息未空");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(walletVo.toMap());
		BasePayResp resp = basePay.getWalletInfo(req, walletVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 转账
	 *
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2020年11月18日 下午6:06:01
	 */
	@Override
	public Ret transfer(SendRedpacketVo redpacketVo, HttpRequest request) {
		if (redpacketVo.getUid() == null || StrUtil.isBlank(redpacketVo.getWalletid())) {
			log.error("转账参数为空");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(redpacketVo.toMap());
		BasePayResp resp = basePay.sendRedpacket(req, redpacketVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 发送红包
	 * 
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2020年11月18日 下午6:06:01
	 */
	@Override
	public Ret sendRedpacket(SendRedpacketVo redpacketVo, HttpRequest request) {
		log.error("payService sendRedpacket debugger --> payService sendRedpacket begin. redpacketVo: {}", redpacketVo.toString());
		if (redpacketVo.getUid() == null || StrUtil.isBlank(redpacketVo.getWalletid())) {
			log.error("发送红包参数为空");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(redpacketVo.toMap());
		log.error("payService sendRedpacket debugger --> enter basePay sendRedpacket. req: {}, uid: {}", req.toString(), redpacketVo.getUid());
		BasePayResp resp = basePay.sendRedpacket(req, redpacketVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}
//	@Override
//	public Ret sendRedpacket(SendRedpacketVo redpacketVo, HttpRequest request) {
//		if (redpacketVo.getUid() == null || StrUtil.isBlank(redpacketVo.getWalletid())) {
//			log.error("发送红包参数为空");
//			return RetUtils.failMsg("参数异常");
//		}
//
//
//		WxWalletSendRedPacketLocal wxWalletSendRedPacketLocal = new WxWalletSendRedPacketLocal();
//		WxWalletSendRedPacket wxWalletSendRedPacket = redpacketVo.getSendRed();
//		wxWalletSendRedPacketLocal.setAgrno(wxWalletSendRedPacket.getAgrno());
//		wxWalletSendRedPacketLocal.setAppversion(wxWalletSendRedPacket.getAppversion());
//		wxWalletSendRedPacketLocal.setAvatar(wxWalletSendRedPacket.getAvatar());
//		wxWalletSendRedPacketLocal.setAcceptnum(wxWalletSendRedPacket.getAcceptnum());
//		wxWalletSendRedPacketLocal.setAcceptuid(wxWalletSendRedPacket.getAcceptuid());
//		wxWalletSendRedPacketLocal.setApitime(wxWalletSendRedPacket.getApitime());
//		wxWalletSendRedPacketLocal.setBizcompletetime(wxWalletSendRedPacket.getBizcompletetime());
//		wxWalletSendRedPacketLocal.setBizcreattime(wxWalletSendRedPacket.getBizcreattime());
//		wxWalletSendRedPacketLocal.setBacktime(wxWalletSendRedPacket.getBacktime());
//		wxWalletSendRedPacketLocal.setBless(wxWalletSendRedPacket.getBless());
//		wxWalletSendRedPacketLocal.setChatbizid(wxWalletSendRedPacket.getChatbizid());
//		wxWalletSendRedPacketLocal.setChatmode(wxWalletSendRedPacket.getChatmode());
//		wxWalletSendRedPacketLocal.setCheckflag(wxWalletSendRedPacket.getCheckflag());
//		wxWalletSendRedPacketLocal.setCheckdate(wxWalletSendRedPacket.getCheckdate());
//		wxWalletSendRedPacketLocal.setCny(wxWalletSendRedPacket.getCny());
//		wxWalletSendRedPacketLocal.setCoinsyn(wxWalletSendRedPacket.getCoinsyn());
//		wxWalletSendRedPacketLocal.setCovers(wxWalletSendRedPacket.getCovers());
//		wxWalletSendRedPacketLocal.setCreatetime(wxWalletSendRedPacket.getCreatetime());
//		wxWalletSendRedPacketLocal.setDevice(wxWalletSendRedPacket.getDevice());
//		wxWalletSendRedPacketLocal.setEndtime(wxWalletSendRedPacket.getEndtime());
//		wxWalletSendRedPacketLocal.setId(wxWalletSendRedPacket.getId());
//		wxWalletSendRedPacketLocal.setIp(wxWalletSendRedPacket.getIp());
//		wxWalletSendRedPacketLocal.setMerid(wxWalletSendRedPacket.getMerid());
//		wxWalletSendRedPacketLocal.setMerorderid(wxWalletSendRedPacket.getMerorderid());
//		wxWalletSendRedPacketLocal.setMode(wxWalletSendRedPacket.getMode());
//		wxWalletSendRedPacketLocal.setMsgid(wxWalletSendRedPacket.getMsgid());
//		wxWalletSendRedPacketLocal.setNum(wxWalletSendRedPacket.getNum());
//		wxWalletSendRedPacketLocal.setNick(wxWalletSendRedPacket.getNick());
//		wxWalletSendRedPacketLocal.setOrdererrormsg(wxWalletSendRedPacket.getOrdererrormsg());
//		wxWalletSendRedPacketLocal.setPaynotifyurl(wxWalletSendRedPacket.getPaynotifyurl());
//		wxWalletSendRedPacketLocal.setPaytimeout(wxWalletSendRedPacket.getPaytimeout());
//		wxWalletSendRedPacketLocal.setPaytype(wxWalletSendRedPacket.getPaytype());
//		wxWalletSendRedPacketLocal.setQuerysyn(wxWalletSendRedPacket.getQuerysyn());
//		wxWalletSendRedPacketLocal.setRemark(wxWalletSendRedPacket.getRemark());
//		wxWalletSendRedPacketLocal.setReqid(wxWalletSendRedPacket.getReqid());
//		wxWalletSendRedPacketLocal.setSendmode(wxWalletSendRedPacket.getSendmode());
//		wxWalletSendRedPacketLocal.setStatus(wxWalletSendRedPacket.getStatus());
//		wxWalletSendRedPacketLocal.setStarttime(wxWalletSendRedPacket.getStarttime());
//		wxWalletSendRedPacketLocal.setSubwalletid(wxWalletSendRedPacket.getSubwalletid());
//		wxWalletSendRedPacketLocal.setUid(wxWalletSendRedPacket.getUid());
//		wxWalletSendRedPacketLocal.setUpdatetime(wxWalletSendRedPacket.getUpdatetime());
//		wxWalletSendRedPacketLocal.setWalletid(wxWalletSendRedPacket.getWalletid());
//
//
//
//		SendRedpacketLocalVo sendRedpacketLocalVo = new SendRedpacketLocalVo();
//		sendRedpacketLocalVo.setAmount(redpacketVo.getCny());
//		sendRedpacketLocalVo.setAgrno(redpacketVo.getAgrno());
//		sendRedpacketLocalVo.setAppversion(redpacketVo.getAppversion());
//		sendRedpacketLocalVo.setBizid(redpacketVo.getBizid());
//		sendRedpacketLocalVo.setChatmode(redpacketVo.getChatmode());
//		sendRedpacketLocalVo.setChatlinkid(redpacketVo.getChatlinkid());
//		sendRedpacketLocalVo.setCny(redpacketVo.getCny());
//		sendRedpacketLocalVo.setCurrency(redpacketVo.getCurrency());
//		sendRedpacketLocalVo.setDevicetype(redpacketVo.getDevicetype());
//		sendRedpacketLocalVo.setIp(redpacketVo.getIp());
//		sendRedpacketLocalVo.setIpInfo(redpacketVo.getIpInfo());
//		sendRedpacketLocalVo.setMerorderid(redpacketVo.getMerorderid());
//		sendRedpacketLocalVo.setMode(redpacketVo.getMode());
//		sendRedpacketLocalVo.setNotifyUrl(redpacketVo.getNotifyUrl());
//		sendRedpacketLocalVo.setNum(redpacketVo.getNum());
//		sendRedpacketLocalVo.setPacketCount(redpacketVo.getNum());
//		sendRedpacketLocalVo.setPacketType(redpacketVo.getMode());
//		sendRedpacketLocalVo.setPaypwd(redpacketVo.getPaypwd());
//		sendRedpacketLocalVo.setPaytimeout(redpacketVo.getPaytimeout());
//		sendRedpacketLocalVo.setPaytype(redpacketVo.getPaytype());
//		sendRedpacketLocalVo.setRemark(redpacketVo.getRemark());
//		sendRedpacketLocalVo.setRid(redpacketVo.getRid());
//		sendRedpacketLocalVo.setSendRed(wxWalletSendRedPacketLocal);
//		sendRedpacketLocalVo.setSingleAmount(redpacketVo.getSinglecny());
//		sendRedpacketLocalVo.setSinglecny(redpacketVo.getSinglecny());
//		sendRedpacketLocalVo.setSubwalletid(redpacketVo.getSubwalletid());
//		sendRedpacketLocalVo.setSmscode(redpacketVo.getSmscode());
//		sendRedpacketLocalVo.setUid(redpacketVo.getUid());
//		sendRedpacketLocalVo.setWalletid(redpacketVo.getWalletid());
//
//
//		BasePayReq req = new BasePayReq(request);
//		req.setParams(sendRedpacketLocalVo.toMap());
//		BasePayResp resp = basePay.sendRedpacket(req, redpacketVo.getUid());
//
//		if (!resp.isOk()) {
//			return RetUtils.failMsg(resp.getMsg());
//		}
//		return RetUtils.okData(resp.getResp());
//	}

	/**
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:30
	 */
	@Override
	public Ret initRedpacket(SendRedpacketVo redpacketVo) {
		if (redpacketVo.getNum() == null || redpacketVo.getMode() == null) {
			return RetUtils.invalidParam();
		}
		Ret ret = basePay.initRedpacket(redpacketVo, true);
		return ret;
	}

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:31
	 */
	@Override
	public Ret quickRedpacket(SendRedpacketVo redpacketVo, HttpRequest request) {
		throw new RuntimeException("本地钱包暂不支持");
	}

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:32
	 */
	@Override
	public Ret payRedpacket(SendRedpacketVo redpacketVo, HttpRequest request, User user) {
		WxWalletSendRedPacketLocal sendRed = basePay.getRedPacketLockLocal(redpacketVo.getRid(), false);
		if (sendRed == null) {
			return RetUtils.failMsg("操作已超时，请重试");
		}
		BasePayResp resp = null;
		short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
		DateTime apitime = new DateTime();
		if (Objects.equals(redpacketVo.getPaytype(), PayConst.RedPayType.BANKCARD)) {
			//银行卡支付-验证码确认-PAYING-->PAYING_CONFIRM
			if (!Objects.equals(sendRed.getStatus(), PayConst.RedPacketStatus.PAYING)) {
				return RetUtils.failMsg("请先发送短信");
			}
			WxWalletSendRedPacketLocal update = new WxWalletSendRedPacketLocal();
			update.setId(redpacketVo.getRid());
			update.setApitime(apitime);
			update.setPaytype(PayConst.RedPayType.BANKCARD);
			update.setBacktime(DateUtil.offsetMinute(apitime, timeout));
			update.setStatus(PayConst.RedPacketStatus.PAYING_CONFIRM);
			Ret ret = basePay.updateRedPacketLock(update, PayConst.RedPacketStatus.PAYING, true);
			if (ret.isFail()) {
				return ret;
			}
			RechargeConfirmVo rechargeVo = new RechargeConfirmVo();
			rechargeVo.setWalletid(redpacketVo.getWalletid());
			rechargeVo.setUid(redpacketVo.getUid());
			rechargeVo.setIp(redpacketVo.getIp());
			rechargeVo.setIpInfo(redpacketVo.getIpInfo());
			rechargeVo.setSmscode(redpacketVo.getSmscode());
			rechargeVo.setMerorderid(redpacketVo.getMerorderid());
			BasePayReq req = new BasePayReq(request);
			req.setParams(rechargeVo.toMap());
			resp = basePay.rechargeConfirm(rechargeVo, rechargeVo.getUid(), redpacketVo.getCny());
			if (!resp.isOk()) {
				WxWalletSendRedPacketLocal rollbackupdate = new WxWalletSendRedPacketLocal();
				rollbackupdate.setId(redpacketVo.getRid());
				rollbackupdate.setStatus(sendRed.getStatus());
				rollbackupdate.setOrdererrormsg(resp.getMerMsg());
				rollbackupdate.setApitime(sendRed.getApitime());
				rollbackupdate.setPaytype(sendRed.getPaytype());
				rollbackupdate.setBacktime(sendRed.getBacktime());
				Ret rollback = basePay.updateRedPacketLock(rollbackupdate, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
				if (rollback.isFail()) {
					return rollback;
				}
				return RetUtils.failMsg(resp.getMerMsg());
			}
		} else {//密码支付-INIT-->PAYING_CONFIRM
//			if (!user.getPaypwd().equals(redpacketVo.getPaypwd())) {
//				return RetUtils.failMsg("支付密码错误");
//			}
			WxWalletSendRedPacketLocal update = new WxWalletSendRedPacketLocal();
			update.setId(redpacketVo.getRid());
			update.setApitime(apitime);
			update.setPaytype(PayConst.RedPayType.CNY);
			update.setBacktime(DateUtil.offsetMinute(apitime, timeout));
			update.setStatus(PayConst.RedPacketStatus.PAYING_CONFIRM);
			if (StrUtil.isNotBlank(sendRed.getReqid())) {
				//				//此处清除可能二次选择的银行卡确认信息
				//				update.setReqid("");
				//				update.setMerorderid("");
				//				update.setAgrno("");
				Ret ret = basePay.updateRedPacketLock(update, PayConst.RedPacketStatus.PAYING, true);
				if (ret.isFail()) {
					return ret;
				}
			} else {
				Ret ret = basePay.updateRedPacketLock(update, PayConst.RedPacketStatus.INIT, true);
				if (ret.isFail()) {
					return ret;
				}
			}
			TransferVo transferVo = new TransferVo();
			transferVo.setCny(sendRed.getCny() + "");
			transferVo.setTowalletid(sendRed.getSubwalletid());
			transferVo.setWalletid(redpacketVo.getWalletid());
			transferVo.setUid(redpacketVo.getUid());
			BasePayReq req = new BasePayReq(request);
			req.setParams(transferVo.toMap());
			resp = basePay.transfer(req, transferVo.getUid(), transferVo.getCny());
			if (!resp.isOk()) {
				WxWalletSendRedPacketLocal rollbackupdate = new WxWalletSendRedPacketLocal();
				rollbackupdate.setId(redpacketVo.getRid());
				rollbackupdate.setStatus(sendRed.getStatus());
				rollbackupdate.setOrdererrormsg(resp.getMerMsg());
				rollbackupdate.setApitime(sendRed.getApitime());
				if (sendRed.getPaytype() != null) {
					rollbackupdate.setPaytype(sendRed.getPaytype());
				}
				rollbackupdate.setBacktime(sendRed.getBacktime());
				Ret rollback = basePay.updateRedPacketLock(rollbackupdate, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
				if (rollback.isFail()) {
					return rollback;
				}
				return RetUtils.failMsg(resp.getMerMsg());
			}
		}
		redpacketVo.setSendRedLocal(sendRed);
		Ret ret = basePay.payRedpacket(request, resp, redpacketVo, true);
		return ret;
	}

	/**
	 * 发送红包记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return 13805730416 郭 13905817500 张
	 * @author lixinji 2020年11月19日 上午11:32:00
	 */
	@Override
	public Ret sendRedpacketlist(Integer uid, Integer pageNumber, String period) {
		Kv params = Kv.by("uid", uid).set("statuses",
				"'" + PayConst.RedPacketStatus.SUCCESS + "','" + PayConst.RedPacketStatus.PROCESS + "','" + PayConst.RedPacketStatus.TIMEOUT + "'");
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.sendlistLocal", params);
		Page<Record> redpacketPage = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okPage(redpacketPage);
	}

	/**
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji 2020年11月27日 下午4:50:13
	 */
	@Override
	public Ret sendRedpacketStat(Integer uid, String period) {
		Kv params = Kv.by("uid", uid).set("statuses",
				"'" + PayConst.RedPacketStatus.SUCCESS + "','" + PayConst.RedPacketStatus.PROCESS + "','" + PayConst.RedPacketStatus.TIMEOUT + "'");
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.sendstatLocal", params);
		Record redpacketstat = Db.use(Const.Db.TIO_SITE_MAIN).findFirst(sqlPara);
		return RetUtils.okData(redpacketstat);
	}

	/**
	 * 抢红包
	 * 
	 * @param grabRedpacketVo
	 * @return
	 * @author lixinji 2020年11月19日 上午10:45:04
	 */
	@Override
	public Ret grabRedpacket(GrabRedpacketVo grabRedpacketVo, User user, HttpRequest request) {
//		if (grabRedpacketVo.getUid() == null || StrUtil.isBlank(grabRedpacketVo.getWalletid())) {
//			log.error("抢红包参数为空");
//			return RetUtils.failMsg("参数异常");
//		}
//		BasePayReq req = new BasePayReq(request);
//		req.setParams(grabRedpacketVo.toMap());
//		BasePayResp resp = basePay.grabRedpacket(req, grabRedpacketVo.getUid());
//		if (!resp.isOk()) {
//			return RetUtils.failMsg(resp.getMsg());
//		}
//		return RetUtils.okData(resp.getResp());

		if (grabRedpacketVo.getUid() == null || StrUtil.isBlank(grabRedpacketVo.getWalletid())) {
			log.error("抢红包参数为空");
			return RetUtils.failMsg("参数异常");
		}
		if (grabRedpacketVo.getRid() == null || grabRedpacketVo.getRid() == 0) {
			log.error("红包id为空");
			return RetUtils.failMsg("红包id为空");
		}
		Ret grabret = basePay.grabRedpacket(grabRedpacketVo, user, true);
		if (grabret.isFail()) {
			return grabret;
		}
		WxWalletRedPacketRandomLocal random = RetUtils.getOkTData(grabret);
		WxWalletSendRedPacketLocal redPacket = RetUtils.getOkTData(grabret, "redpacket");
		TransferVo transferVo = new TransferVo();
		transferVo.setCny(random.getCny() + "");
		transferVo.setTowalletid(random.getWalletid());
		transferVo.setWalletid(redPacket.getSubwalletid());
		transferVo.setUid(redPacket.getUid());
		BasePayReq req = new BasePayReq(request);
		req.setParams(transferVo.toMap());
		BasePayResp resp = basePay.transfer(req, transferVo.getUid(), random.getCny() + "");
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		grabRedpacketVo.setRandomLocal(random);
		grabRedpacketVo.setRedPacketLocal(redPacket);
		Ret ret = basePay.grabRedpacketCallback(request, resp, grabRedpacketVo, true);
		return ret;
	}

	/**
	 * 抢红包记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:33:07
	 */
	@Override
	public Ret grabRedpacketlist(Integer uid, Integer pageNumber, String period) {
		Kv params = Kv.by("uid", uid).set("status", PayConst.RedRandomStatus.SUCCESS);
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.grablistLocal", params);
		Page<Record> redpacketPage = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okPage(redpacketPage);
	}

	/**
	 * 
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji 2020年11月27日 下午4:52:51
	 */
	@Override
	public Ret grabRedpacketStat(Integer uid, String period) {
		Kv params = Kv.by("uid", uid).set("status", PayConst.RedRandomStatus.SUCCESS);
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.grabstatLocal", params);
		Record redpacketstat = Db.use(Const.Db.TIO_SITE_MAIN).findFirst(sqlPara);
		return RetUtils.okData(redpacketstat);
	}

	/**
	 * 红包状态
	 * 
	 * @param uid
	 * @param serialNumber
	 * @return
	 * @author lixinji 2020年11月22日 下午10:30:43
	 */
	@Override
	public Ret redStatus(User user, String serialNumber, Integer rid) {
		Map<String, Object> statusRet = new HashMap<String, Object>();
		statusRet.put("openflag", user.getOpenflag());
		if (Objects.equals(user.getOpenflag(), Const.YesOrNo.NO)) {
			return RetUtils.okData(statusRet);
		}
		Kv params = Kv.by("rid", rid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfoLocal", params);
		WxWalletSendRedPacketLocal redPacket = WxWalletSendRedPacketLocal.dao.findFirst(sqlPara);
		if (redPacket == null || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.CANCEL) || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		statusRet.put("redstatus", redPacket.getStatus());
		WxWalletGrabRedItemLocal grabRedItem = WxWalletGrabRedItemLocal.dao.findFirst("select id,cny,`status` from wx_wallet_grab_red_item_local where uid = ? and rid = ?", user.getId(), rid);
		if (grabRedItem == null) {
			statusRet.put("grabstatus", PayConst.RedRandomStatus.INIT);
		} else {
			statusRet.put("grabcny", grabRedItem.getCny());
			statusRet.put("grabstatus", grabRedItem.getStatus());
		}
		return RetUtils.okData(statusRet);
	}

	/**
	 * 红包信息
	 * 
	 * @param serialNumber
	 * @return
	 * @author lixinji 2020年11月22日 下午10:50:11
	 */
	@Override
	public Ret redInfo(HttpRequest request, String serialNumber, User user, Integer rid) {
		Kv params = Kv.by("rid", rid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfoLocal", params);
		WxWalletSendRedPacketLocal redPacket = WxWalletSendRedPacketLocal.dao.findFirst(sqlPara);
		if (redPacket == null || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.CANCEL) || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		Map<String, Object> infoRet = new HashMap<String, Object>();
		infoRet.put("info", redPacket);
		sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfoGrablistLocal", params);
		List<WxWalletGrabRedItemLocal> grabRedItem = WxWalletGrabRedItemLocal.dao.find(sqlPara);
		if (CollectionUtil.isNotEmpty(grabRedItem)) {
			infoRet.put("grablist", grabRedItem);
		} else {
			infoRet.put("grablist", new ArrayList<WxWalletGrabRedItemLocal>());
		}
		return RetUtils.okData(infoRet);
	}

	/**
	 * 支付列表信息
	 * 
	 * @param user
	 * @param request
	 * @return
	 * @author lixinji 2021年3月17日 下午4:09:46
	 */
	@Override
	public Ret payListInfo(User user, HttpRequest request) {
		throw new RuntimeException("本地钱包暂不支持暂不支持支付列表");
	}

	/**
	 * 客户端token
	 * 
	 * @param tokenVo
	 * @return
	 * @author lixinji 2020年11月15日 下午6:51:59
	 */
	@Override
	public Ret getClientToken(ClientTokenVo tokenVo, HttpRequest request) {
		if (tokenVo == null || StrUtil.isBlank(tokenVo.getWalletid()) || StrUtil.isBlank(tokenVo.getBizType())) {
			log.error("获取token参数异常");
			return RetUtils.failMsg("参数异常");
		}
		String bizType = tokenVo.getBizType();
		if (!Pay5UConst.ClientBizType.ACCESS_CARDlIST.equals(bizType) && !Pay5UConst.ClientBizType.ACCESS_SAFETY.equals(bizType)) {
			log.error("获取token参数异常:{}", bizType);
			return RetUtils.failMsg("无效页面");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(tokenVo.toMap());
		BasePayResp resp = basePay.clientToken(req, tokenVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 充值
	 * 
	 * @param rechargeVo
	 * @return
	 * @author lixinji 2020年11月15日 下午7:57:54
	 */
	@Override
	public Ret recharge(RechargeVo rechargeVo, HttpRequest request) {
		log.error("payService recharge debugger --> ");
		if (rechargeVo == null || StrUtil.isBlank(rechargeVo.getWalletid()) || rechargeVo.getUid() == null) {
			log.error("充值参数异常");
			return RetUtils.failMsg("充值参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		log.error("payService recharge debugger --> enter basePay recharge method. req: {}, uid: {}", req.toString(), rechargeVo.getUid());
		BasePayResp resp = basePay.recharge(req, rechargeVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午11:57:31
	 */
	@Override
	public Ret rechargeConfirm(RechargeConfirmVo rechargeVo, HttpRequest request) {
		throw new RuntimeException("本地钱包暂不支持暂不支持充值确认接口");
	}

	/**
	 * 充值查询
	 * 
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2020年11月25日 下午1:59:12
	 */
	@Override
	public Ret rechargeQuery(RechargeQueryVo rechargeVo, HttpRequest request) {
		if (rechargeVo == null || StrUtil.isBlank(rechargeVo.getSerialnumber())) {
			log.error("充值查询参数异常");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		BasePayResp resp = basePay.rechargeQuery(req, rechargeVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * @param queryVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月18日 上午11:03:38
	 */
	@Override
	public Ret redpacketPayQuery(RedpacketQueryVo queryVo, HttpRequest request) {
		throw new RuntimeException("本地钱包暂不支持暂不支持红包支付查询接口");
	}

	/**
	 * 充值记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:17:04
	 */
	@Override
	public Ret rechargelist(Integer uid, Integer pageNumber) {
		if (pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		Kv params = Kv.by("uid", uid).set("status", Pay5UConst.Status.SUCCESS);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("localwallet.list", params);
		Page<Record> rechargePage = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okPage(rechargePage);
	}

	/**
	 * @param uid
	 * @param pageNumber
	 * @param mode
	 * @return
	 * @author lixinji 2020年11月26日 上午10:43:38
	 */
	@Override
	public Ret getWalletItems(Integer uid, Integer pageNumber, Short mode) {
		if (pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		Kv params = Kv.by("uid", uid);
		if (mode != null) {
			params.set("mode", mode);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("walletLocal.items", params);
		Page<Record> rechargePage = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okPage(rechargePage);
	}

	/**
	 * 提现
	 * 
	 * @param withholdVo
	 * @return
	 * @author lixinji 2020年11月16日 下午2:09:17
	 */
	@Override
	public Ret withhold(WithholdVo withholdVo, HttpRequest request) {
		log.error("payService withhold debugger --> payService method begin withholdVo: {}", withholdVo.toString());
		if (withholdVo == null || StrUtil.isBlank(withholdVo.getWalletid()) || withholdVo.getUid() == null) {
			log.error("提现参数异常");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(withholdVo.toMap());
		log.error("payService withhold debugger --> enter basePay method req: {}, uid: {}", req, withholdVo.getUid());
		BasePayResp resp = basePay.withhold(req, withholdVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 提现查询
	 * 
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2020年11月25日 下午1:59:12
	 */
	@Override
	public Ret withholdQuery(WithholdQueryVo withholdQueryVo, HttpRequest request) {
		if (withholdQueryVo == null || StrUtil.isBlank(withholdQueryVo.getSerialnumber())) {
			log.error("充值查询参数异常");
			return RetUtils.failMsg("参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(withholdQueryVo.toMap());
		BasePayResp resp = basePay.withholdQuery(req, withholdQueryVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
	}

	/**
	 * 提现记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:16:33
	 */
	@Override
	public Ret withholdlist(Integer uid, Integer pageNumber) {
		if (pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("localwalletwithhold.list", params);
		Page<Record> withholdPage = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
		return RetUtils.okPage(withholdPage);
	}

	/**
	 * 充值回调
	 * 
	 * @param request
	 * @return
	 * @author lixinji 2020年11月15日 下午7:59:04
	 */
	@Override
	public Ret rechargeCallback(HttpRequest request, Integer uid) {
		BasePayResp resp = baseCallbackPay.recharge(request, uid);
		if (!resp.isOk()) {
			log.error(resp.getMsg());
		}
		return RetUtils.okMsg(Pay5UConst.Status.SUCCESS);
	}

	/**
	 * 提现回调
	 * 
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji 2020年11月16日 下午2:15:22
	 */
	@Override
	public Ret withholdCallback(HttpRequest request, Integer uid) {
		BasePayResp resp = baseCallbackPay.withhold(request, uid);
		if (!resp.isOk()) {
			log.error(resp.getMsg());
		}
		return RetUtils.okMsg(Pay5UConst.Status.SUCCESS);
	}

	/**
	 * 发送红包回调
	 * 
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji 2020年11月18日 下午6:08:40
	 */
	@Override
	public Ret redpacketCallback(HttpRequest request, Integer uid) {
		log.error("发红包回调 service...");
		BasePayResp resp = baseCallbackPay.sendRedpacket(request, uid);
		if (!resp.isOk()) {
			log.error(resp.getMsg());
		}
		return RetUtils.okMsg(Pay5UConst.Status.SUCCESS);
	}

	/**
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月9日 下午2:20:10
	 */
	@Override
	public Ret rechargeJob() throws Exception {
		try {
			Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5);
			String initTimeDeal = DateUtil.format(DateUtil.offsetMinute(new DateTime(), -timeout - 1), DatePattern.NORM_DATETIME_PATTERN);
			log.info("充值查询定时任务处理开始:{}", initTimeDeal);
			List<WxUserRechargeItem> initDeals = WxUserRechargeItem.dao.find("select  * from wx_user_recharge_item where querysyn = ? and bizcreattime <= ?", PayConst.QuerySyn.NO,
			        initTimeDeal);
			if (CollectionUtil.isNotEmpty(initDeals)) {
				for (WxUserRechargeItem item : initDeals) {
					BasePayResp basePayResp = baseCallbackPay.rechargeAgainCallback(item);
					if (!basePayResp.isOk()) {
						log.error("定时处理充值未回调订单异常：{}", basePayResp.getMsg());
					}
				}
			}
			List<WxUserRechargeItem> items = WxUserRechargeItem.dao.find("select  * from wx_user_recharge_item where coinsyn != ? or querysyn = ?", PayConst.CoinSyn.SUCCESS,
			        PayConst.QuerySyn.CALLBACK);
			if (CollectionUtil.isNotEmpty(items)) {
				for (WxUserRechargeItem item : items) {
					BasePayResp basePayResp = baseCallbackPay.rechargeQueryNoCheck(item);
					if (!basePayResp.isOk()) {
						log.error("定时处理充值订单异常：{}", basePayResp.getMsg());
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return RetUtils.okOper();
	}

	/**
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月9日 上午10:46:03
	 */
	@Override
	public Ret redpacketJob() throws Exception {
		try {
			Short paytimeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
			String initPayTimeDeal = DateUtil.format(DateUtil.offsetMinute(new DateTime(), -paytimeout - 1), DatePattern.NORM_DATETIME_PATTERN);
			log.info("红包查询未支付定时任务处理开始:{}", initPayTimeDeal);
			List<WxUserSendRedItem> payDeals = WxUserSendRedItem.dao.find("select  * from wx_user_send_red_item where querysyn = ? and bizcreattime <= ?", PayConst.QuerySyn.NO,
			        initPayTimeDeal);
			if (CollectionUtil.isNotEmpty(payDeals)) {
				for (WxUserSendRedItem item : payDeals) {
					BasePayResp basePayResp = baseCallbackPay.redpacketAgainCallback(item);
					if (!basePayResp.isOk()) {
						log.error("定时处理红包查询未支付订单异常：{}", basePayResp.getMsg());
					}
				}
			}
			Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, (short) 1440);
			String initTimeDeal = DateUtil.format(DateUtil.offsetMinute(new DateTime(), -timeout - 1), DatePattern.NORM_DATETIME_PATTERN);
			log.info("红包查询超时任务处理开始:{}", initTimeDeal);
			List<WxUserSendRedItem> initDeals = WxUserSendRedItem.dao.find("select  * from wx_user_send_red_item where bizcreattime <= ? and querysyn != ? ", initTimeDeal,
			        PayConst.QuerySyn.SUCCESS);
			if (CollectionUtil.isNotEmpty(initDeals)) {
				for (WxUserSendRedItem item : initDeals) {
					BasePayResp basePayResp = baseCallbackPay.redpacketAgainCallback(item);
					if (!basePayResp.isOk()) {
						log.error("红包查询超时订单异常：{}", basePayResp.getMsg());
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return RetUtils.okOper();
	}

	/**
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月9日 下午2:31:22
	 */
	@Override
	public Ret withholdJob() throws Exception {
		try {
			Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_WITHHOLD_TIMEOUT, (short) 5);
			String initTimeDeal = DateUtil.format(DateUtil.offsetMinute(new DateTime(), -timeout - 1), DatePattern.NORM_DATETIME_PATTERN);
			log.info("提现查询定时任务处理开始:{}", initTimeDeal);
			List<WxUserWithholdItem> initDeals = WxUserWithholdItem.dao.find("select  * from wx_user_withhold_item where querysyn = ? and bizcreattime <= ?", PayConst.QuerySyn.NO,
			        initTimeDeal);
			if (CollectionUtil.isNotEmpty(initDeals)) {
				for (WxUserWithholdItem item : initDeals) {
					BasePayResp basePayResp = baseCallbackPay.withholdAgainCallback(item);
					if (!basePayResp.isOk()) {
						log.error("定时处理提现未回调订单异常：{}", basePayResp.getMsg());
					}
				}
			}
			List<WxUserWithholdItem> items = WxUserWithholdItem.dao.find("select  * from wx_user_withhold_item where coinsyn != ? or querysyn = ?", PayConst.CoinSyn.SUCCESS,
			        PayConst.QuerySyn.CALLBACK);
			if (CollectionUtil.isNotEmpty(items)) {
				for (WxUserWithholdItem item : items) {
					BasePayResp basePayResp = baseCallbackPay.withholdQueryNoCheck(item);
					if (!basePayResp.isOk()) {
						log.error("定时处理提现订单异常：{}", basePayResp.getMsg());
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return RetUtils.okOper();
	}

	/**
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午3:52:42
	 */
	@Override
	public boolean walletCheckLogout(User user) {
		if (Objects.equals(user.getOpenflag(), Const.YesOrNo.NO)) {
			return true;
		}
		WalletVo walletVo = new WalletVo();
		walletVo.setUid(user.getId());
		walletVo.setWalletid(user.getWalletid());
		BasePayReq req = new BasePayReq(null);
		req.setParams(walletVo.toMap());
		BasePayResp resp = basePay.getWalletInfo(req, walletVo.getUid());
		if (!resp.isOk()) {
			//失败了
			return false;
		}
		Map<String, Object> map = resp.getResp();
		Long cny = MapUtil.getLong(map, "balance");
		if (cny == null || cny > 0) {
			return false;
		}
		WxUserSendRedItemLocal redPacket = WxUserSendRedItemLocal.dao.findFirst("SELECT id from wx_user_send_red_item_local where uid = ? and `status` = ?", user.getId(),
		        Pay5UConst.RedPacketStatus.SEND);
		if (redPacket != null) {
			return false;
		}
		WxUserWithholdItemLocal initDeals = WxUserWithholdItemLocal.dao.findFirst("select  * from wx_user_withhold_item_local where querysyn = ? and uid = ?", PayConst.QuerySyn.NO, user.getId());
		if (initDeals != null) {
			return false;
		}
		WxUserWithholdItemLocal items = WxUserWithholdItemLocal.dao.findFirst("select  * from wx_user_withhold_item_local where uid = ? and (coinsyn != ? or querysyn = ?)", user.getId(),
		        PayConst.CoinSyn.SUCCESS, PayConst.QuerySyn.CALLBACK);
		if (items != null) {
			return false;
		}
		return true;
	}

	/**
	 * @param str
	 * @return
	 * @author lixinji
	 * 2020年12月1日 下午2:13:11
	 */
	private Integer strToInteger(String str) {
		if (StrUtil.isBlank(str)) {
			return 0;
		}
		return Integer.parseInt(str);
	}

	/**
	 * @param str
	 * @return
	 * @author lixinji
	 * 2020年12月1日 下午2:14:10
	 */
	private Short strToShort(String str) {
		if (StrUtil.isBlank(str)) {
			return 0;
		}
		return Short.parseShort(str);
	}
}
