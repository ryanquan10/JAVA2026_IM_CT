
package org.tio.sitexxx.service.pay.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxWalletBankCards;
import org.tio.sitexxx.service.model.main.WxWalletCoin;
import org.tio.sitexxx.service.model.main.WxWalletGrabRedItem;
import org.tio.sitexxx.service.model.main.WxWalletRechargeItem;
import org.tio.sitexxx.service.model.main.WxWalletRedPacketRandom;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.model.main.WxWalletWithholdCount;
import org.tio.sitexxx.service.model.main.WxWalletWithholdItems;
import org.tio.sitexxx.service.pay.base.BaseBizPay;
import org.tio.sitexxx.service.pay.base.BaseCallbackPay;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.impl.PayStdBizApi;
import org.tio.sitexxx.service.pay.impl.ncount.PayNcountApi;
import org.tio.sitexxx.service.pay.impl.ncount.PayNcountCallbackApi;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.BindCardConfirmVo;
import org.tio.sitexxx.service.vo.BindCardVo;
import org.tio.sitexxx.service.vo.ClientTokenVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.GrabRedpacketVo;
import org.tio.sitexxx.service.vo.OpenUserVo;
import org.tio.sitexxx.service.vo.PayConst;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.RechargeQueryVo;
import org.tio.sitexxx.service.vo.RechargeVo;
import org.tio.sitexxx.service.vo.RedpacketQueryVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;
import org.tio.sitexxx.service.vo.TransferVo;
import org.tio.sitexxx.service.vo.UnBindCardVo;
import org.tio.sitexxx.service.vo.UpdateOpenVo;
import org.tio.sitexxx.service.vo.WalletVo;
import org.tio.sitexxx.service.vo.WithholdQueryVo;
import org.tio.sitexxx.service.vo.WithholdVo;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.util.StrUtil;

/**
 * 支付服务
 * 
 * @author lixinji 2020年09月25日 下午5:57:32
 */
public class PayStdService implements BasePayService {

	private static Logger log = LoggerFactory.getLogger(PayStdService.class);

	public static BasePay<BasePayReq, BasePayResp> basePay;

	public static BaseCallbackPay<BasePayResp> baseCallbackPay;

	private boolean isTest = true;

	private FastDateFormat format = DatePattern.CHINESE_DATE_TIME_FORMAT;

	public static BaseBizPay bizPay;

	public static void initPay() {
		basePay = new PayNcountApi();
		baseCallbackPay = new PayNcountCallbackApi();
		bizPay = new PayStdBizApi(basePay);
	}

	public PayStdService() {
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
		Record record = Db.use(Const.Db.TIO_SITE_MAIN)
		        .findFirst("select id,mobile,`name`," + "CONCAT(SUBSTR(cardno,1,6),'********',SUBSTR(cardno,15)) cardno," + "`status` " + "from wx_wallet_info where uid = ?", uid);
		return RetUtils.okData(record);
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
		return bizPay.open(openVo, request);
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
		List<Record> banklist = bankcardBeanList(uid);
		return RetUtils.okList(banklist);
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
		BasePayReq req = new BasePayReq(request);
		req.setParams(cardVo.toMap());
		// 开户请求
		BasePayResp resp = basePay.bindBankCard(req, cardVo.getUid());
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Ret ret = bizPay.bindBankCard(resp, cardVo, true);
		return ret;
	}

	/**
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午9:59:15
	 */
	@Override
	public Ret unBindCard(UnBindCardVo cardVo, HttpRequest request) {
		WxWalletBankCards removeCard = WxWalletBankCards.dao.findById(cardVo.getBankcardid());
		if (removeCard == null) {
			return RetUtils.failMsg("银行卡不存在");
		}
		if (Objects.equals(Const.Status.DELETE, removeCard.getStatus())) {
			return RetUtils.failMsg("银行卡已移除");
		}
		if (!Objects.equals(cardVo.getUid(), removeCard.getUid())) {
			return RetUtils.grantError();
		}
		WxWalletBankCards update = new WxWalletBankCards();
		update.setId(removeCard.getId());
		update.setStatus(Const.Status.DELETE);
		boolean atom = update.update();
		if (!atom) {
			return RetUtils.failMsg("移除银行卡前置失败");
		}
		cardVo.setRemoveCard(removeCard);
		BasePayReq req = new BasePayReq(request);
		req.setParams(cardVo.toMap());
		// 开户请求
		BasePayResp resp = basePay.removeBankCard(req, cardVo.getUid());
		if (!resp.isBizOk()) {
			update.setId(removeCard.getId());
			update.setStatus(removeCard.getStatus());
			boolean rollback = update.update();
			if (!rollback) {
				log.error("回滚失败：{}", Json.toJson(removeCard));
			}
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Ret ret = bizPay.removeBankCard(resp, cardVo, true);
		return ret;
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
		WxWalletBankCards bankCards = WxWalletBankCards.dao.findById(cardVo.getBankcardid());
		if (bankCards == null) {
			return RetUtils.failMsg("初始化银行卡不存在");
		}
		cardVo.setInitCards(bankCards);
		BasePayReq req = new BasePayReq(request);
		req.setParams(cardVo.toMap());
		// 开户请求
		BasePayResp resp = basePay.bindBankCardConfirm(req, cardVo.getUid());
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Ret ret = bizPay.bindBankCardConfirm(resp, cardVo, true);
		return ret;
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
		return RetUtils.failMsg("标准版不支持修改开户信息");
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
		WxWalletCoin coin = WxWalletCoin.dao.findFirst("select id,cny,walletid from wx_wallet_coin where uid = ? and walletid = ?", walletVo.getUid(), walletVo.getWalletid());
		if (coin == null) {
			return RetUtils.failMsg("钱包不存在");
		}
		if (Objects.equals(coin.getCny(), 0l)) {
			log.error("钱包金额为0,进行同步：{}", Json.toJson(coin));
			BasePayReq req = new BasePayReq(request);
			req.setParams(walletVo.toMap());
			BasePayResp resp = basePay.getWalletInfo(req, walletVo.getUid());
			return bizPay.synWallet(resp.getResp(), walletVo, true);
		}
		return RetUtils.okData(coin);
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
		return RetUtils.failMsg("标准版不支持商户发红包逻辑");
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
		throw new RuntimeException("暂不支持转账功能");
	}

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
		Ret ret = bizPay.initRedpacket(redpacketVo, true);
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
		WxWalletSendRedPacket sendRed = bizPay.getRedPacketLock(redpacketVo.getRid(), false);
		if (sendRed == null) {
			return RetUtils.failMsg("操作已超时，请重试");
		}
		if (!Objects.equals(sendRed.getStatus(), PayConst.RedPacketStatus.INIT)) {
			return RetUtils.failMsg("红包状态异常");
		}
		redpacketVo.setSendRed(sendRed);
		RechargeVo rechargeVo = new RechargeVo();
		rechargeVo.setWalletid(redpacketVo.getWalletid());
		rechargeVo.setUid(redpacketVo.getUid());
		rechargeVo.setAgrno(redpacketVo.getAgrno());
		rechargeVo.setAmount(sendRed.getCny() + "");
		rechargeVo.setRemark(sendRed.getBless());
		rechargeVo.setTowalletid(sendRed.getSubwalletid());
		rechargeVo.setIp(redpacketVo.getIp());
		short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
		rechargeVo.setTimeout(timeout);
		String notifyUrl = PayConst.CallBackUrl.REDPACKET + rechargeVo.getUid();
		rechargeVo.setNotifyUrl(notifyUrl);
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		BasePayResp resp = basePay.recharge(req, rechargeVo.getUid());
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		redpacketVo.setNotifyUrl(notifyUrl);
		redpacketVo.setPaytimeout(timeout);
		Ret ret = bizPay.quickRedpacket(resp, redpacketVo, true);
		return ret;
	}

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:32
	 */
	@Override
	public Ret payRedpacket(SendRedpacketVo redpacketVo, HttpRequest request, User user) {
		WxWalletSendRedPacket sendRed = bizPay.getRedPacketLock(redpacketVo.getRid(), false);
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
			WxWalletSendRedPacket update = new WxWalletSendRedPacket();
			update.setId(redpacketVo.getRid());
			update.setApitime(apitime);
			update.setPaytype(PayConst.RedPayType.BANKCARD);
			update.setBacktime(DateUtil.offsetMinute(apitime, timeout));
			update.setStatus(PayConst.RedPacketStatus.PAYING_CONFIRM);
			Ret ret = bizPay.updateRedPacketLock(update, PayConst.RedPacketStatus.PAYING, true);
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
			resp = basePay.rechargeConfirm(req, rechargeVo.getUid());
			if (!resp.isBizOk()) {
				WxWalletSendRedPacket rollbackupdate = new WxWalletSendRedPacket();
				rollbackupdate.setId(redpacketVo.getRid());
				rollbackupdate.setStatus(sendRed.getStatus());
				rollbackupdate.setOrdererrormsg(resp.getMerMsg());
				rollbackupdate.setApitime(sendRed.getApitime());
				rollbackupdate.setPaytype(sendRed.getPaytype());
				rollbackupdate.setBacktime(sendRed.getBacktime());
				Ret rollback = bizPay.updateRedPacketLock(rollbackupdate, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
				if (rollback.isFail()) {
					return rollback;
				}
				return RetUtils.failMsg(resp.getMerMsg());
			}
		} else {//密码支付-INIT-->PAYING_CONFIRM
			if (!user.getPaypwd().equals(redpacketVo.getPaypwd())) {
				return RetUtils.failMsg("支付密码错误");
			}
			WxWalletSendRedPacket update = new WxWalletSendRedPacket();
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
				Ret ret = bizPay.updateRedPacketLock(update, PayConst.RedPacketStatus.PAYING, true);
				if (ret.isFail()) {
					return ret;
				}
			} else {
				Ret ret = bizPay.updateRedPacketLock(update, PayConst.RedPacketStatus.INIT, true);
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
			resp = basePay.transfer(req, transferVo.getUid());
			if (!resp.isBizOk()) {
				WxWalletSendRedPacket rollbackupdate = new WxWalletSendRedPacket();
				rollbackupdate.setId(redpacketVo.getRid());
				rollbackupdate.setStatus(sendRed.getStatus());
				rollbackupdate.setOrdererrormsg(resp.getMerMsg());
				rollbackupdate.setApitime(sendRed.getApitime());
				if (sendRed.getPaytype() != null) {
					rollbackupdate.setPaytype(sendRed.getPaytype());
				}
				rollbackupdate.setBacktime(sendRed.getBacktime());
				Ret rollback = bizPay.updateRedPacketLock(rollbackupdate, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
				if (rollback.isFail()) {
					return rollback;
				}
				return RetUtils.failMsg(resp.getMerMsg());
			}
		}
		redpacketVo.setSendRed(sendRed);
		Ret ret = bizPay.payRedpacket(request, resp, redpacketVo, true);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.sendlist", params);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.sendstat", params);
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
		if (grabRedpacketVo.getUid() == null || StrUtil.isBlank(grabRedpacketVo.getWalletid())) {
			log.error("抢红包参数为空");
			return RetUtils.failMsg("参数异常");
		}
		Ret grabret = bizPay.grabRedpacket(grabRedpacketVo, user, true);
		if (grabret.isFail()) {
			return grabret;
		}
		WxWalletRedPacketRandom random = RetUtils.getOkTData(grabret);
		WxWalletSendRedPacket redPacket = RetUtils.getOkTData(grabret, "redpacket");
		TransferVo transferVo = new TransferVo();
		transferVo.setCny(random.getCny() + "");
		transferVo.setTowalletid(random.getWalletid());
		transferVo.setWalletid(redPacket.getSubwalletid());
		transferVo.setUid(redPacket.getUid());
		BasePayReq req = new BasePayReq(request);
		req.setParams(transferVo.toMap());
		BasePayResp resp = basePay.transfer(req, transferVo.getUid());
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		grabRedpacketVo.setRandom(random);
		grabRedpacketVo.setRedPacket(redPacket);
		Ret ret = bizPay.grabRedpacketCallback(request, resp, grabRedpacketVo, true);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.grablist", params);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.grabstat", params);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfo", params);
		WxWalletSendRedPacket redPacket = WxWalletSendRedPacket.dao.findFirst(sqlPara);
		if (redPacket == null || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.CANCEL) || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		statusRet.put("redstatus", redPacket.getStatus());
		WxWalletGrabRedItem grabRedItem = WxWalletGrabRedItem.dao.findFirst("select id,cny,`status` from wx_wallet_grab_red_item where uid = ? and rid = ?", user.getId(), rid);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfo", params);
		WxWalletSendRedPacket redPacket = WxWalletSendRedPacket.dao.findFirst(sqlPara);
		if (redPacket == null || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.CANCEL) || Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		Map<String, Object> infoRet = new HashMap<String, Object>();
		infoRet.put("info", redPacket);
		sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket.redinfoGrablist", params);
		List<WxWalletGrabRedItem> grabRedItem = WxWalletGrabRedItem.dao.find(sqlPara);
		if (CollectionUtil.isNotEmpty(grabRedItem)) {
			infoRet.put("grablist", grabRedItem);
		} else {
			infoRet.put("grablist", new ArrayList<WxWalletGrabRedItem>());
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
		List<Record> banklist = bankcardBeanList(user.getId());
		WalletVo walletVo = new WalletVo();
		walletVo.setUid(user.getId());
		walletVo.setWalletid(user.getWalletid());
		Ret ret = getWalletInfo(walletVo, request);
		WxWalletCoin walletInfo = RetUtils.getOkTData(ret);
		Map<String, Object> infoRet = new HashMap<String, Object>();
		infoRet.put("banklist", banklist);
		infoRet.put("walletinfo", walletInfo);
		return RetUtils.okData(infoRet);
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
		return RetUtils.failMsg("标准版不支持客户端拉起商户api");
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
		if (rechargeVo == null || StrUtil.isBlank(rechargeVo.getWalletid()) || rechargeVo.getUid() == null) {
			log.error("充值参数异常");
			return RetUtils.failMsg("充值参数异常");
		}
		String notifyUrl = PayConst.CallBackUrl.RECHARGE + rechargeVo.getUid();
		rechargeVo.setNotifyUrl(notifyUrl);
		rechargeVo.setTowalletid(rechargeVo.getWalletid());
		short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5);
		rechargeVo.setTimeout(timeout);
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		BasePayResp resp = basePay.recharge(req, rechargeVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.recharge(resp, rechargeVo, true);
		return ret;
	}

	/**
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午11:57:31
	 */
	@Override
	public Ret rechargeConfirm(RechargeConfirmVo rechargeVo, HttpRequest request) {
		if (rechargeVo == null || StrUtil.isBlank(rechargeVo.getWalletid()) || rechargeVo.getUid() == null || StrUtil.isBlank(rechargeVo.getMerorderid())
		        || rechargeVo.getRid() == null) {
			log.error("充值参数异常");
			return RetUtils.failMsg("充值参数异常");
		}
		WxWalletRechargeItem rechargeItem = WxWalletRechargeItem.dao.findById(rechargeVo.getRid());
		if (rechargeItem == null || !Objects.equals(rechargeItem.getMerstatus(), "-1")) {
			return RetUtils.failMsg("充值订单已失效");
		}
		if (!Objects.equals(rechargeVo.getUid(), rechargeItem.getUid()) || !Objects.equals(rechargeItem.getMerorderid(), rechargeVo.getMerorderid())) {
			return RetUtils.grantError();
		}
		WxWalletRechargeItem update = new WxWalletRechargeItem();
		update.setId(rechargeVo.getRid());
		update.setStatus(PayConst.WalletChangeStatus.CONFIRM);
		boolean atom = update.update();
		if (!atom) {
			return RetUtils.failMsg("充值确认失败");
		}
		rechargeVo.setOrder(rechargeItem);
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		BasePayResp resp = basePay.rechargeConfirm(req, rechargeVo.getUid());
		if (!resp.isBizOk()) {
			WxWalletRechargeItem rollback = new WxWalletRechargeItem();
			rollback.setId(rechargeVo.getRid());
			rollback.setStatus(rechargeItem.getStatus());
			rollback.setOrdererrormsg(resp.getMerMsg());
			boolean back = rollback.update();
			if (!back) {
				log.error("充值回滚失败:{}", Json.toJson(rechargeItem));
			}
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Ret ret = bizPay.rechargeConfirm(resp, rechargeVo, true);
		return ret;
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
		if (rechargeVo == null || (StrUtil.isBlank(rechargeVo.getSerialnumber()) && StrUtil.isBlank(rechargeVo.getReqid()))) {
			log.error("充值查询参数异常");
			return RetUtils.failMsg("参数异常");
		}
		WxWalletRechargeItem item = WxWalletRechargeItem.dao.findById(rechargeVo.getRid());
		if (item == null) {
			return RetUtils.failMsg("充值订单不存在");
		}
		if (Objects.equals(item.getStatus(), PayConst.WalletChangeStatus.SUCCESS) || Objects.equals(item.getStatus(), PayConst.WalletChangeStatus.FAIL)) {
			return RetUtils.okData(item);
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
		BasePayResp resp = basePay.rechargeQuery(req, rechargeVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.rechargeQuery(request, resp, rechargeVo, true);
		return ret;
	}

	/**
	 * @param queryVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月18日 上午11:03:38
	 */
	@Override
	public Ret redpacketPayQuery(RedpacketQueryVo queryVo, HttpRequest request) {
		if (queryVo == null || (StrUtil.isBlank(queryVo.getSerialnumber()) && StrUtil.isBlank(queryVo.getReqid()))) {
			log.error("红包查询参数异常");
			return RetUtils.failMsg("参数异常");
		}
		WxWalletSendRedPacket item = WxWalletSendRedPacket.dao.findById(queryVo.getRid());
		if (item == null) {
			return RetUtils.failMsg("红包不存在");
		}
		if (!Objects.equals(item.getStatus(), PayConst.RedPacketStatus.PAYING_CONFIRM)) {
			return RetUtils.okData(item);
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(queryVo.toMap());
		BasePayResp resp = basePay.redpacketQuery(req, queryVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.redpacketQuery(request, resp, queryVo, true);
		return ret;
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
		Kv params = Kv.by("uid", uid).set("status", PayConst.WalletChangeStatus.SUCCESS);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("recharge.list", params);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("wallet.items", params);
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
		if (withholdVo == null || StrUtil.isBlank(withholdVo.getWalletid()) || withholdVo.getUid() == null) {
			log.error("提现参数异常");
			return RetUtils.failMsg("参数异常");
		}
		if (StrUtil.isBlank(withholdVo.getPaypwd())) {
			return RetUtils.failMsg("支付密码为空");
		}
		if (!Objects.equals(withholdVo.getUser().getPaypwd(), withholdVo.getPaypwd())) {
			return RetUtils.failMsg("支付密码不正确");
		}
		WxWalletWithholdCount count = initWithHoldCount(withholdVo.getUid(), "");
		if (count == null) {
			return RetUtils.failMsg("系统异常");
		}
		Integer maxCount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_COUNT, 100);
		if (count.getCount() > maxCount) {
			return RetUtils.failMsg("提现次数已超上限");
		}
		boolean update = updateWithholdCount(count.getId(), (short) 1);
		if (!update) {
			return RetUtils.failMsg("提现次数已超上限");
		}
		String notifyUrl = PayConst.CallBackUrl.WITHHOLD + withholdVo.getUid();
		withholdVo.setNotifyUrl(notifyUrl);
		BasePayReq req = new BasePayReq(request);
		req.setParams(withholdVo.toMap());
		BasePayResp resp = basePay.withhold(req, withholdVo.getUid());
		if (!resp.isBizOk()) {
			boolean callback = updateWithholdCount(count.getId(), (short) -1);
			if (!callback) {
				return RetUtils.failMsg("提现次数回滚失败");
			}
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Ret ret = bizPay.withhold(resp, withholdVo, true);
		return ret;
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
		if (withholdQueryVo == null || (StrUtil.isBlank(withholdQueryVo.getSerialnumber()) && StrUtil.isBlank(withholdQueryVo.getReqid()))) {
			log.error("提现查询参数异常");
			return RetUtils.failMsg("参数异常");
		}
		WxWalletWithholdItems item = PayStdBizApi.getWithholdInfo(withholdQueryVo.getWid());
		if (item == null) {
			return RetUtils.failMsg("提现订单不存在");
		}
		if (Objects.equals(item.getStatus(), PayConst.WalletChangeStatus.SUCCESS) || Objects.equals(item.getStatus(), PayConst.WalletChangeStatus.FAIL)) {
			return RetUtils.okData(item);
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(withholdQueryVo.toMap());
		BasePayResp resp = basePay.withholdQuery(req, withholdQueryVo.getUid());
		Ret ret = bizPay.withholdQuery(request, resp, withholdQueryVo, true);
		return ret;
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
		Kv params = Kv.by("uid", uid).set("status", PayConst.WalletChangeStatus.SUCCESS);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("withhold.list", params);
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
			log.error("充值回调异常：{}", resp.getMsg());
			bizPay.rechargeCallback(request, resp, false, true);
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.rechargeCallback(request, resp, true, true);
		return ret;
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
			log.error("提现回调异常：{}", resp.getMsg());
			bizPay.withholdCallback(request, resp, false, true);
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.withholdCallback(request, resp, true, true);
		return ret;
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
		BasePayResp resp = baseCallbackPay.sendRedpacket(request, uid);
		if (!resp.isOk()) {
			log.error("红包回调异常：{}", resp.getMsg());
			bizPay.redpacketCallback(request, resp, false, true);
			return RetUtils.failMsg(resp.getMsg());
		}
		Ret ret = bizPay.redpacketCallback(request, resp, true, true);
		return ret;
	}

	@Override
	public Ret rechargeJob() throws Exception {
		return RetUtils.failMsg("标准版不支持充值定时任务");
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
			dealInitOrder();
			dealPayOrder();
			dealPayConfirmOrder();
			dealTimeoutOrder();
		} catch (Exception e) {
			throw e;
		}
		return RetUtils.okOper();
	}

	@Override
	public Ret withholdJob() throws Exception {
		return RetUtils.failMsg("标准版不支持提现定时任务");
	}

	/**
	 * 注销判断
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午3:36:54
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
		Ret ret = bizPay.synWallet(resp.getResp(), walletVo, true);
		if (ret.isFail()) {
			//失败了
			return false;
		}
		WxWalletCoin coin = RetUtils.getOkTData(ret);
		if (coin == null || coin.getCny() > 0) {
			return false;
		}
		WxWalletSendRedPacket redPacket = WxWalletSendRedPacket.dao.findFirst("SELECT id from wx_wallet_send_red_packet where uid = ? and `status` = ?", user.getId(),
		        PayConst.RedPacketStatus.PROCESS);
		if (redPacket != null) {
			return false;
		}
		WxWalletWithholdItems withholdItems = WxWalletWithholdItems.dao.findFirst("SELECT id from wx_wallet_withhold_items where uid = ? and `status` = ?", user.getId(),
		        PayConst.WalletChangeStatus.LOCAL);
		if (withholdItems != null) {
			return false;
		}
		return true;
	}

	/**
	 * 超时处理
	 * @author lixinji
	 * 2021年3月24日 下午7:25:59
	 */
	private void dealTimeoutOrder() {
		List<WxWalletSendRedPacket> timeoutRedpackets = WxWalletSendRedPacket.dao.find("select * from wx_wallet_send_red_packet where `status` = ? and backtime < ?",
		        PayConst.RedPacketStatus.PROCESS, new DateTime());
		if (CollectionUtil.isNotEmpty(timeoutRedpackets)) {
			int dealcount = 0;
			for (WxWalletSendRedPacket item : timeoutRedpackets) {
				Ret ret = PayStdService.bizPay.redpacketTimeOut(item, true);
				if (ret.isFail()) {
					log.error("超时处理异常：{}", RetUtils.getRetMsg(ret));
					continue;
				}
				dealcount++;
			}
			if (isTest) {
				log.error("{}:超时的订单数->{}<-条,本次处理->{}<-条", DateUtil.format(new DateTime(), format), timeoutRedpackets.size(), dealcount);
			}
		}

	}

	/**
	 * 初始化红包超时处理
	 * @author lixinji
	 * 2021年3月24日 下午5:59:05
	 */
	private void dealInitOrder() {
		List<WxWalletSendRedPacket> redPackets = WxWalletSendRedPacket.dao.find("select * from wx_wallet_send_red_packet where `status`= ? and backtime < ?",
		        PayConst.RedPacketStatus.INIT, new DateTime());
		if (CollectionUtil.isNotEmpty(redPackets)) {
			int dealcount = 0;
			for (WxWalletSendRedPacket item : redPackets) {
				//判断是否有reqid，进行逻辑判断:此处转账不成功，不会留下订单信息
				Ret ret = delRedPacketLock(item.getId(), item.getStatus(), true);
				if (ret.isOk()) {
					dealcount++;
				}
			}
			if (isTest) {
				log.error("{}:红包初始化的订单数->{}<-条,本次处理->{}<-条", DateUtil.format(new DateTime(), format), redPackets.size(), dealcount);
			}
		}
	}

	/**
	 * 快捷支付预下单红包超时处理
	 * @author lixinji
	 * 2021年3月24日 下午5:59:27
	 */
	private void dealPayOrder() {
		List<WxWalletSendRedPacket> quickPackets = WxWalletSendRedPacket.dao.find("select * from wx_wallet_send_red_packet where `status`= ? and backtime < ?",
		        PayConst.RedPacketStatus.PAYING, new DateTime());
		if (CollectionUtil.isNotEmpty(quickPackets)) {
			int dealcount = 0;
			for (WxWalletSendRedPacket item : quickPackets) {
				if (StrUtil.isBlank(item.getReqid())) {
					WxWalletSendRedPacket update = new WxWalletSendRedPacket();
					update.setStatus(PayConst.RedPacketStatus.FAIL);
					update.setRemark("系统发现快捷支付发起--无reqid记录");
					update.setId(item.getId());
					Ret ret = bizPay.updateRedPacketLock(update, item.getStatus(), true);
					if (ret.isOk()) {
						dealcount++;
					}
					log.error("出现发短信状态-但是没有reqiq的记录：red:{}", Json.toJson(item));
					continue;
				}
				RedpacketQueryVo queryVo = new RedpacketQueryVo();
				queryVo.setUid(item.getUid());
				queryVo.setReqid(item.getReqid());
				queryVo.setRid(item.getId());
				BasePayReq req = new BasePayReq(null);
				req.setParams(queryVo.toMap());
				BasePayResp resp = PayStdService.basePay.redpacketQuery(req, queryVo.getUid());
				if (!resp.isOk()) {
					log.error("查询响应错误：{}", resp.getMerMsg());
				}
				dealcount++;
				Ret ret = PayStdService.bizPay.redpacketJobQuery(resp, queryVo, true);
				if (ret.isFail()) {
					log.error("查询处理错误：{}", RetUtils.getRetMsg(ret));
				}
			}
			if (isTest) {
				log.error("{}:红包预下单的订单数->{}<-条,本次处理->{}<-条", DateUtil.format(new DateTime(), format), quickPackets.size(), dealcount);
			}
		}
	}

	/**
	 * 订单确认红包超时处理
	 * @author lixinji
	 * 2021年3月24日 下午6:09:44
	 */
	private void dealPayConfirmOrder() {
		List<WxWalletSendRedPacket> confirmPackets = WxWalletSendRedPacket.dao.find("select * from wx_wallet_send_red_packet where `status`= ? and backtime < ?",
		        PayConst.RedPacketStatus.PAYING_CONFIRM, new DateTime());
		if (CollectionUtil.isNotEmpty(confirmPackets)) {
			int dealcount = 0;
			for (WxWalletSendRedPacket item : confirmPackets) {
				if (StrUtil.isBlank(item.getReqid())) {
					if (Objects.equals(item.getPaytype(), PayConst.RedPayType.BANKCARD)) {
						WxWalletSendRedPacket update = new WxWalletSendRedPacket();
						update.setStatus(PayConst.RedPacketStatus.FAIL);
						update.setRemark("系统发现支付确认--无reqid记录");
						update.setId(item.getId());
						Ret ret = bizPay.updateRedPacketLock(update, item.getStatus(), true);
						if (ret.isOk()) {
							dealcount++;
						}
						log.error("出现支付确认状态-但是没有reqiq的记录：red:{}", Json.toJson(item));
						continue;
					} else {
						Ret ret = delRedPacketLock(item.getId(), item.getStatus(), true);
						if (ret.isOk()) {
							dealcount++;
						}
						continue;
					}

				}
				RedpacketQueryVo queryVo = new RedpacketQueryVo();
				queryVo.setUid(item.getUid());
				queryVo.setReqid(item.getReqid());
				queryVo.setRid(item.getId());
				BasePayReq req = new BasePayReq(null);
				req.setParams(queryVo.toMap());
				BasePayResp resp = PayStdService.basePay.redpacketQuery(req, queryVo.getUid());
				if (!resp.isOk()) {
					log.error("查询响应错误：{}", resp.getMerMsg());
					continue;
				}
				Ret ret = PayStdService.bizPay.redpacketJobQuery(resp, queryVo, true);
				if (ret.isFail()) {
					log.error("查询处理错误：{}", RetUtils.getRetMsg(ret));
					continue;
				}
				dealcount++;
			}
			if (isTest) {
				log.error("{}:红包支付确认的订单数->{}<-条,本次处理->{}<-条", DateUtil.format(new DateTime(), format), confirmPackets.size(), dealcount);
			}
		}
	}

	/**
	 * 删除红包-加锁选择
	 * @param rid
	 * @param lock
	 * @return
	 * @author lixinji
	 * 2021年3月23日 下午3:48:06
	 */
	private Ret delRedPacketLock(Integer rid, Short status, boolean lock) {
		if (lock) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GET_LOCK + "." + rid, WxWalletSendRedPacket.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket sendRed = WxWalletSendRedPacket.dao.findById(rid);
				if (sendRed == null) {
					log.error("删除红包时，发现红包已经不存在,rid:{},status:{}", rid, status);
					return RetUtils.failMsg("删除-红包已被删除");
				}
				if (!Objects.equals(status, sendRed.getStatus())) {
					log.error("删除红包时，发现红包状态发送变更,red:{},delstatus:{}", Json.toJson(sendRed), status);
					return RetUtils.failMsg("删除-红包状态发生变更");
				}

				boolean del = WxWalletSendRedPacket.dao.deleteById(rid);
				if (!del) {
					return RetUtils.failMsg("删除红包数据异常");
				}
				return RetUtils.okOper();
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			boolean del = WxWalletSendRedPacket.dao.deleteById(rid);
			if (!del) {
				return RetUtils.failMsg("删除红包数据异常");
			}
		}
		return RetUtils.failMsg("系统异常");
	}

	/**
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji
	 * 2021年3月25日 下午3:07:12
	 */
	private WxWalletWithholdCount initWithHoldCount(Integer uid, String period) {
		if (StrUtil.isBlank(period)) {
			period = PeriodUtils.dateToPeriodByType(new DateTime(), Const.PeriodType.DAY);
		}
		WxWalletWithholdCount count = WxWalletWithholdCount.dao.findFirst("select * from wx_wallet_withhold_count where uid = ? and period = ?", uid, period);
		if (count == null) {
			count = new WxWalletWithholdCount();
			count.setUid(uid);
			count.setPeriod(period);
			count.setCount((short) 0);
			int i = count.ignoreSave();
			if (i <= 0) {
				count = WxWalletWithholdCount.dao.findFirst("select * from wx_wallet_withhold_count where uid = ? and period = ?", uid, period);
				if (count == null) {
					return null;
				}
			}
		}
		return count;
	}

	private boolean updateWithholdCount(Integer id, short addOrReduce) {
		Integer maxCount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_COUNT, 100);
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_WITHHOLD + "." + id, WxWalletWithholdCount.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxWalletWithholdCount count = WxWalletWithholdCount.dao.findById(id);
			if (count.getCount() > maxCount) {
				return false;
			}
			Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_wallet_withhold_count set count = count + ? where id = ?", addOrReduce, id);
			WxWalletWithholdCount newCount = WxWalletWithholdCount.dao.findById(id);
			if (newCount.getCount() > (maxCount + 1)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("", e);
			log.error("提现次数修改失败");
		} finally {
			writeLock.unlock();
		}
		return false;
	}

	/**
	 * @param uid
	 * @return
	 * @author lixinji 2021年3月17日 下午4:06:11
	 */
	private List<Record> bankcardBeanList(Integer uid) {
		Kv params = Kv.by("uid", uid).set("status", Const.Status.NORMAL);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("wallet.banklist", params);
		List<Record> banklist = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return banklist;
	}
}
