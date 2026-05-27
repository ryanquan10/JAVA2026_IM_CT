
package org.tio.sitexxx.service.pay.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxWallet;
import org.tio.sitexxx.service.model.main.WxWalletBackRedPacketItems;
import org.tio.sitexxx.service.model.main.WxWalletBankCards;
import org.tio.sitexxx.service.model.main.WxWalletCoin;
import org.tio.sitexxx.service.model.main.WxWalletCoinItem;
import org.tio.sitexxx.service.model.main.WxWalletGrabRedItem;
import org.tio.sitexxx.service.model.main.WxWalletInfo;
import org.tio.sitexxx.service.model.main.WxWalletRechargeItem;
import org.tio.sitexxx.service.model.main.WxWalletRedPacketRandom;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.model.main.WxWalletWithholdItems;
import org.tio.sitexxx.service.pay.base.BaseBizPay;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.service.WalletQueueApi;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.BindCardConfirmVo;
import org.tio.sitexxx.service.vo.BindCardVo;
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
import org.tio.sitexxx.service.vo.WalletVo;
import org.tio.sitexxx.service.vo.WithholdQueryVo;
import org.tio.sitexxx.service.vo.WithholdVo;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 标准支付业务实现
 * 1、所有api基于自己的服务
 * 2、所有的支付接口服务端发起
 * @author lixinji
 * 2021年2月3日 上午10:59:42
 */
public class PayStdBizApi implements BaseBizPay {

	private static Logger log = LoggerFactory.getLogger(PayStdBizApi.class);

	private BasePay<BasePayReq, BasePayResp> basePay;

	/**
	 * @param basePay
	 */
	public PayStdBizApi(BasePay<BasePayReq, BasePayResp> basePay) {
		this.basePay = basePay;
	}

	/**
	 * 开户
	 * @param openVo
	 * @param request
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:01:15
	 */
	@Override
	public Ret open(OpenUserVo openVo, HttpRequest request) {
		log.error("开户信息：{}", Json.toJson(openVo));
		WxWalletInfo walletInfo = new WxWalletInfo();
		walletInfo.setUid(openVo.getUid());
		walletInfo.setMobile(openVo.getMobile());
		walletInfo.setCardno(openVo.getCardno());
		walletInfo.setName(openVo.getName());
		int save = walletInfo.replaceSave();
		if (save <= 0) {
			log.error("开户保存二次提交实名认证：{}", Json.toJson(walletInfo));
		}
		openVo.setInfoid(walletInfo.getId());
		Ret mainRet = openUserSingle(openVo, request);
		if (mainRet.isFail()) {
			return mainRet;
		}
		openVo.setUid(-openVo.getUid());
		Ret subRet = openUserSingle(openVo, request);
		if (subRet.isFail()) {
			return subRet;
		}
		UserService.ME._clearCache(-openVo.getUid());
		return mainRet;
	}

	/**
	 * 单次开户
	 * @param openVo
	 * @param request
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:02:45
	 */
	private Ret openUserSingle(OpenUserVo openVo, HttpRequest request) {
		BasePayReq req = new BasePayReq(request);
		req.setParams(openVo.toMap());
		// 开户请求
		BasePayResp resp = basePay.openUser(req, openVo.getUid());
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("uid", openVo.getUid());
		infoMap.put("walletid", resp.getResp().get("walletid"));
		req.setParams(infoMap);
		// 开户结果查询
		BasePayResp infoResp = basePay.getWalletInfo(req, openVo.getUid());
		Ret ret = openUserBizDeal(resp, infoResp, openVo, true);
		return ret;
	}

	/* 
	 * 开户业务逻辑：
	 * 1、初始化开户信息
	 * 2、初始化钱包信息
	 * 3、修改用户钱包状态
	 */
	private Ret openUserBizDeal(BasePayResp openResp, BasePayResp queryResp, OpenUserVo openVo, Boolean isAtom) {
		if (!queryResp.isBizOk()) {
			return RetUtils.failMsg(queryResp.getMerMsg());
		}
		if (isAtom == null) {
			isAtom = false;
		}
		Map<String, Object> openMap = openResp.getResp();
		Map<String, Object> queryMap = queryResp.getResp();
		Integer uid = openVo.getUid() > 0 ? openVo.getUid() : -openVo.getUid();
		WxWallet wallet = WxWallet.mapToBean(queryMap);
		wallet.setReqid(openMap.get("reqid") + "");
		wallet.setReqinfoid(openVo.getInfoid());
		if (openVo.getUid() > 0) {
			wallet.setMainflag(Const.YesOrNo.YES);
		} else {
			wallet.setMainflag(Const.YesOrNo.NO);
		}
		if (Objects.equals("00", wallet.getAuthstatus()) || Objects.equals("03", wallet.getAuthstatus())) {
			wallet.setAuthstatus(Const.YesOrNo.YES + "");
		} else {
			wallet.setAuthstatus(Const.YesOrNo.NO_FLAG + "");
		}
		if (Objects.equals("00", wallet.getAuditstatus()) || Objects.equals("03", wallet.getAuditstatus())) {
			wallet.setAuditstatus(Const.YesOrNo.YES + "");
		} else {
			wallet.setAuditstatus(Const.YesOrNo.NO_FLAG + "");
		}
		wallet.setIp(openVo.getIp());
		wallet.setDevice(openVo.getDevicetype());
		wallet.setAppversion(openVo.getAppversion());
		wallet.setCoinsyn(Const.YesOrNo.YES);
		wallet.setQuerysyn(Const.YesOrNo.YES);
		User user = UserService.ME.getById(uid);
		Ret checkRet = User.checkUser(user);
		if (checkRet.isFail()) {
			return checkRet;
		}
		if (Objects.equals(user.getOpenflag(), Const.YesOrNo.YES)) {
			return RetUtils.failMsg("重复提交-用户已开户");
		}
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				if (openVo.getUid() > 0) {
					int init = wallet.replaceSave();
					if (init <= 0) {
						log.error("开户主信息已存在：{}", Json.toJson(queryMap));
						System.out.println("开户主信息已存在");
					}
					WxWalletCoin coin = new WxWalletCoin();
					coin.setUid(openVo.getUid());
					coin.setWid(wallet.getId());
					coin.setWalletid(wallet.getWalletid());
					coin.setCny(Long.parseLong(queryMap.get("cny").toString()));
					int coininit = coin.replaceSave();
					if (coininit <= 0) {
						System.out.println("开户主信息已存在");
						log.error("开户余额主信息已存在：{}", Json.toJson(queryMap));
					}
				} else {
					boolean init = wallet.save();
					if (!init) {
						log.error("开户信息异常：{}", Json.toJson(queryMap));
						return failRet("保存开户信息失败");
					}
					WxWalletCoin coin = new WxWalletCoin();
					coin.setUid(openVo.getUid());
					coin.setWid(wallet.getId());
					coin.setWalletid(wallet.getWalletid());
					coin.setCny(Long.parseLong(queryMap.get("cny").toString()));
					boolean coininit = coin.save();
					if (!coininit) {
						log.error("开户余额信息异常：{}", Json.toJson(queryMap));
						return failRet("保存开户余额信息失败");
					}
				}
				User user = new User();
				user.setId(uid);
				if (openVo.getUid() > 0) {
					user.setOpenid(wallet.getId());
				} else {
					user.setSubopenid(wallet.getId());
					user.setOpenflag(Const.YesOrNo.YES);
					if (Objects.equals(Const.YesOrNo.YES + "", wallet.getAuthstatus())) {
						WxWalletInfo update = new WxWalletInfo();
						update.setId(openVo.getInfoid());
						update.setStatus(Const.YesOrNo.YES);
						boolean auth = update.update();
						if (!auth) {
							return failRet("认证保存失败");
						}
					}
				}
				boolean update = user.update();
				if (!update) {
					return failRet("修改用户开户状态失败");
				}
				return true;
			}
		};
		boolean commit = false;
		if (isAtom) {
			commit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		} else {
			commit = atom.noTxRun();
		}
		if (!commit) {
			return atom.getRetObj();
		}
		return RetUtils.okData(wallet);
	}

	/* 
	 * 修改开户信息
	 */
	@Override
	public Ret updateUser(Map<String, Object> resp, Boolean isAtom) {

		return RetUtils.okOper();
	}

	/* 
	 * 实名认证
	 * 1：已认证，数据不存在保存，存在修改
	 * 2、未认证，数据替换保存，并修改用户实名状态
	 */
	@Override
	public Ret authRealname(Map<String, Object> resp, Boolean isAtom) {

		return RetUtils.okOper();
	}

	/**
	 * 初始化银行卡
	 */
	@Override
	public Ret bindBankCard(BasePayResp resp, BindCardVo cardVo, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		User user = UserService.ME.getById(cardVo.getUid());
		Ret checkRet = User.checkUser(user);
		if (checkRet.isFail()) {
			return checkRet;
		}
		WxWalletBankCards bankCards = WxWalletBankCards.mapToBean(resp.getResp());
		bankCards.setWalletid(cardVo.getWalletid());
		bankCards.setPhone(cardVo.getMobile());
		bankCards.setUsername(cardVo.getName());
		bankCards.setStatus(Const.Status.DISABLED);
		bankCards.setCardno(dealBankCode(cardVo.getBankcardno()));
		int init = bankCards.replaceSave();
		if (init <= 0) {
			log.error("银行卡初始化重复");
		}
		return RetUtils.okData(bankCards);
	}

	/**
	 * 确认绑卡
	 */
	@Override
	public Ret bindBankCardConfirm(BasePayResp resp, BindCardConfirmVo cardVo, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		User user = UserService.ME.getById(cardVo.getUid());
		Ret checkRet = User.checkUser(user);
		if (checkRet.isFail()) {
			return checkRet;
		}
		WxWalletBankCards initcard = cardVo.getInitCards();
		WxWalletBankCards allInfo = WxWalletBankCards.mapToBean(resp.getResp());
		WxWalletBankCards update = new WxWalletBankCards();
		update.setId(initcard.getId());
		update.setBankcode(allInfo.getBankcode());
		update.setCardtype(allInfo.getCardtype());
		//		update.setCardno(allInfo.getCardno());
		update.setAgrno(allInfo.getAgrno());
		update.setStatus(Const.YesOrNo.YES);
		boolean atom = update.update();
		if (!atom) {
			return RetUtils.failMsg("保存失败");
		}
		WxWalletBankCards lastCard = WxWalletBankCards.dao.findById(initcard.getId());
		return RetUtils.okData(lastCard);
	}

	/* 
	 * 移除银行卡：
	 * 1、卡必现存在
	 * 2、卡用户一致
	 * 3、注意:未进行用户密码验证
	 */
	@Override
	public Ret removeBankCard(BasePayResp resp, UnBindCardVo cardVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		WxWalletBankCards removeCard = cardVo.getRemoveCard();
		WxWalletBankCards update = new WxWalletBankCards();
		update.setId(removeCard.getId());
		update.setDelreqid(resp.getResp().get("reqid") + "");
		update.setDeltime(new Date());
		update.setDelid(removeCard.getId());
		boolean atom = update.update();
		if (!atom) {
			return RetUtils.failMsg("移除银行卡信息保存失败");
		}
		return RetUtils.okData(removeCard);
	}

	@Override
	public Ret getBankCards(Map<String, Object> resp) {
		return RetUtils.okOper();
	}

	@Override
	public Ret getWalletInfo(BasePayResp resp) {
		// TODO Auto-generated method stub
		return RetUtils.okOper();
	}

	@Override
	public Ret synWallet(Map<String, Object> resultMap, WalletVo walletVo, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletCoin coin = WxWalletCoin.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin where uid = ? and walletid = ?", walletVo.getUid(), walletVo.getWalletid());
		if (coin == null) {
			return RetUtils.failMsg("钱包不存在");
		}
		WxWalletCoin syn = new WxWalletCoin();
		syn.setId(coin.getId());
		syn.setCny(MapUtil.getLong(resultMap, "cny"));
		boolean update = syn.update();
		if (!update) {
			return RetUtils.failMsg("同步钱包错误");
		}
		if (!isAtom) {
			return RetUtils.okData(coin);
		}
		WxWalletCoin last = WxWalletCoin.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin where id = ?", coin.getId());
		return RetUtils.okData(last);
	}

	@Override
	public Ret coinAdd(Long cny, Integer uid, Short mode, Short coinflag, String remark, Integer bizid, Short status, Map<String, Object> resultMap, String starttime, String endtime,
	        Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletCoinItem coinItem = new WxWalletCoinItem();
		coinItem.setUid(uid);
		coinItem.setMode(mode);
		coinItem.setCoinflag(coinflag);
		coinItem.setStatus(status);
		coinItem.setCny(cny);
		coinItem.setOrderstatus(MapUtil.getStr(resultMap, "merstatus"));
		coinItem.setBizid(bizid);
		coinItem.setReqid(MapUtil.getStr(resultMap, "reqid"));
		coinItem.setMerorderid(MapUtil.getStr(resultMap, "merorderid"));
		coinItem.setBizcompletetime(endtime);
		coinItem.setBizcreattime(starttime);
		coinItem.setRemark(remark);
		boolean save = coinItem.save();
		if (!save) {
			log.error("钱包明细失败：{}", Json.toJson(coinItem));
			return RetUtils.failMsg("保存明细失败");
		}
		return RetUtils.okData(coinItem);
	}

	@Override
	public Ret getCoinItems(Map<String, Object> resp) {
		return RetUtils.okOper();
	}

	@Override
	public Ret getCoinItemInfo(Map<String, Object> resp) {

		return RetUtils.okOper();
	}

	/* 
	 * 
	 */
	@Override
	public Ret recharge(BasePayResp resp, RechargeVo rechargeVo, Boolean isAtom) {
		String respCheck = resp.getMerMsg();
		if (StrUtil.isNotBlank(respCheck)) {
			WxWalletRechargeItem rechargeItem = WxWalletRechargeItem.mapToBean(resp.getResp());
			rechargeItem.setAmount(Long.parseLong(rechargeVo.getAmount()));
			rechargeItem.setWalletid(rechargeVo.getWalletid());
			rechargeItem.setTimeout(ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5));
			rechargeItem.setAgrno(rechargeVo.getAgrno());
			rechargeItem.setIp(rechargeVo.getIp());
			rechargeItem.setStatus(PayConst.WalletChangeStatus.FAIL);
			rechargeItem.setAppversion(rechargeVo.getAppversion());
			rechargeItem.setNotifyurl(rechargeVo.getNotifyUrl());
			rechargeItem.setDevice(rechargeVo.getDevicetype());
			rechargeItem.setRemark(rechargeVo.getRemark());
			if (StrUtil.isBlank(rechargeItem.getBizcompletetime())) {
				rechargeItem.setBizcompletetime(DateUtil.format(new DateTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			boolean save = rechargeItem.save();
			if (!save) {
				log.error("保存错误预下单充值信息失败");
			}
			return RetUtils.failMsg(respCheck);
		}
		WxWalletRechargeItem rechargeItem = WxWalletRechargeItem.mapToBean(resp.getResp());
		rechargeItem.setAmount(Long.parseLong(rechargeVo.getAmount()));
		rechargeItem.setWalletid(rechargeVo.getWalletid());
		rechargeItem.setTimeout(ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5));
		rechargeItem.setAgrno(rechargeVo.getAgrno());
		rechargeItem.setIp(rechargeVo.getIp());
		rechargeItem.setMerstatus("-1");
		rechargeItem.setStatus(PayConst.WalletChangeStatus.LOCAL);
		rechargeItem.setAppversion(rechargeVo.getAppversion());
		rechargeItem.setNotifyurl(rechargeVo.getNotifyUrl());
		rechargeItem.setDevice(rechargeVo.getDevicetype());
		rechargeItem.setRemark(rechargeVo.getRemark());
		boolean save = rechargeItem.save();
		if (!save) {
			return RetUtils.failMsg("保存预下单充值信息失败");
		}
		return RetUtils.okData(rechargeItem);
	}

	/**
	 * 
	 */
	@Override
	public Ret rechargeConfirm(BasePayResp resp, RechargeConfirmVo rechargeVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		WxWalletRechargeItem order = rechargeVo.getOrder();
		WxWalletRechargeItem last = WxWalletRechargeItem.dao.findById(order.getId());
		return RetUtils.okData(last);
	}

	@Override
	public Ret rechargeCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletRechargeItem rechargeItem = WxWalletRechargeItem.mapToBean(resp.getResp());
		WxWalletRechargeItem local = WxWalletRechargeItem.dao.findFirst("select * from wx_wallet_recharge_item where merorderid = ?", rechargeItem.getMerorderid());
		if (local == null) {
			log.error("本地记录不存在");
			return RetUtils.failMsg("本地记录不存在");
		}
		WxWalletRechargeItem update = new WxWalletRechargeItem();
		update.setMerfee(rechargeItem.getMerfee());
		update.setRecvacctamount(rechargeItem.getRecvacctamount());
		update.setCheckdate(rechargeItem.getCheckdate());
		update.setBankcode(rechargeItem.getBankcode());
		update.setMerstatus(rechargeItem.getMerstatus());
		update.setOrdererrormsg(rechargeItem.getOrdererrormsg());
		update.setBizcompletetime(rechargeItem.getBizcompletetime());
		update.setId(local.getId());
		if (!result) {
			update.setOrdererrormsg(resp.getMsg());
			update.setStatus(PayConst.WalletChangeStatus.FAIL);
		} else {
			if (resp.isBizOk()) {
				update.setStatus(PayConst.WalletChangeStatus.SUCCESS);
			} else {
				update.setOrdererrormsg(resp.getMerMsg());
				update.setStatus(PayConst.WalletChangeStatus.FAIL);
			}
		}
		boolean atom = false;
		if (resp.isBizOk()) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + local.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletRechargeItem last = WxWalletRechargeItem.dao.findFirst("select * from wx_wallet_recharge_item where id = ?", local.getId());
				if (Objects.equals(last.getStatus(), PayConst.WalletChangeStatus.SUCCESS)) {
					atom = update.update();
				} else {
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							boolean callback = update.update();
							if (!callback) {
								return failRet("充值记录保存失败");
							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", last.getReqid());
							Ret coinRet = coinAdd(last.getAmount(), last.getUid(), PayConst.WalletMode.RECHARGE, Const.CoinFlag.INCOME, "充值", last.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), last.getBizcreattime(), update.getBizcompletetime(), false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}

		} else {
			atom = update.update();
		}
		if (!atom) {
			return RetUtils.failMsg("充值失败");
		}
		return RetUtils.okMsg("200");
	}

	//	ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + rechargeVo.getUid(), WxWalletCoin.class);
	//	WriteLock writeLock = rwLock.writeLock();
	//	writeLock.lock();
	//	try {
	//		
	//	} catch (Exception e) {
	//		log.error("", e);
	//	} finally {
	//		writeLock.unlock();
	//	}
	//	

	/**
	 * 充值查询
	 */
	@Override
	public Ret rechargeQuery(HttpRequest request, BasePayResp resp, RechargeQueryVo rechargeVo, Boolean isAtom) {
		Map<String, Object> respMap = resp.getResp();
		Object objectStatus = respMap.get("merstatus");
		String merstatus = "";
		if (objectStatus != null) {
			merstatus = objectStatus.toString();
		}
		if (Objects.equals(merstatus, "1")) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + rechargeVo.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletRechargeItem last = WxWalletRechargeItem.dao.findFirst("select * from wx_wallet_recharge_item where id = ?", rechargeVo.getRid());
				if (!Objects.equals(last.getStatus(), PayConst.WalletChangeStatus.SUCCESS)) {
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							WxWalletRechargeItem update = new WxWalletRechargeItem();
							update.setId(rechargeVo.getRid());
							update.setStatus(PayConst.WalletChangeStatus.SUCCESS);
							update.setMerstatus(MapUtil.getStr(respMap, "merstatus"));
							update.setOrdererrormsg(resp.getMerMsg());
							update.setQuerysyn(Const.YesOrNo.YES);
							update.setQueuetime(new DateTime());
							update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
							boolean callback = update.update();
							if (!callback) {
								return failRet("充值记录保存失败");
							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", last.getReqid());
							Ret coinRet = coinAdd(last.getAmount(), last.getUid(), PayConst.WalletMode.RECHARGE, Const.CoinFlag.INCOME, "充值", last.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), last.getBizcreattime(), update.getBizcompletetime(), false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					boolean atom = false;
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}

		} else if (Objects.equals(merstatus, "0")) {
		} else if (Objects.equals(merstatus, "3")) {
		} else {
			WxWalletRechargeItem update = new WxWalletRechargeItem();
			update.setId(rechargeVo.getRid());
			update.setStatus(PayConst.WalletChangeStatus.FAIL);
			update.setMerstatus(merstatus);
			update.setOrdererrormsg(resp.getMerMsg());
			update.setQuerysyn(Const.YesOrNo.YES);
			update.setQueuetime(new DateTime());
			update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
			update.update();
		}
		WxWalletRechargeItem item = WxWalletRechargeItem.dao.findById(rechargeVo.getRid());
		return RetUtils.okData(item);
	}

	@Override
	public Ret cost(Map<String, Object> resp) {
		// TODO Auto-generated method stub
		return RetUtils.okOper();
	}

	@Override
	public Ret back(Map<String, Object> resp) {
		// TODO Auto-generated method stub
		return RetUtils.okOper();
	}

	@Override
	public Ret initRedpacket(SendRedpacketVo redpacketVo, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = true;
		}
		short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_INT_TIMEOUT, (short) 20);
		Integer uid = redpacketVo.getUid();
		Short mode = redpacketVo.getMode();
		Short totalNum = redpacketVo.getNum();
		Short chatmode = redpacketVo.getChatmode();
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				//初始化红包主表
				WxWalletSendRedPacket redPacket = new WxWalletSendRedPacket();
				DateTime createtime = new DateTime();
				redPacket.setChatmode(redPacket.getChatmode());
				redPacket.setChatbizid(redpacketVo.getBizid());
				redPacket.setUid(uid);
				redPacket.setWalletid(redpacketVo.getWalletid());
				redPacket.setSubwalletid(redpacketVo.getSubwalletid());
				redPacket.setMode(mode);
				redPacket.setChatmode(redpacketVo.getChatmode());
				redPacket.setBless(redpacketVo.getRemark());
				redPacket.setIp(redpacketVo.getIp());
				redPacket.setDevice(redpacketVo.getDevicetype());
				redPacket.setAppversion(redpacketVo.getAppversion());
				redPacket.setCny(Long.parseLong(redpacketVo.getCny()));
				redPacket.setNum(totalNum);
				redPacket.setStatus(PayConst.RedPacketStatus.INIT);
				redPacket.setBacktime(DateUtil.offsetMinute(createtime, timeout));
				redPacket.setCreatetime(createtime);
				redPacket.setUpdatetime(createtime);
				if (Objects.equals(Const.ChatMode.P2P, redpacketVo.getChatmode())) {
					redPacket.setAcceptuid(redpacketVo.getBizid().intValue());
				}
				boolean init = redPacket.save();
				if (!init) {
					return failRet("红包初始化异常");
				}
				Integer rid = redPacket.getId();
				Long totalCny = redPacket.getCny();
				if (Objects.equals(totalNum, (short) 1)) {
					Integer senduid = null;
					if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
						senduid = redpacketVo.getBizid().intValue();
					}
					boolean childinit = initRedRandom(rid, senduid, totalCny, mode, (short) 1);
					if (!childinit) {
						return failRet("子红包初始化异常");
					}
				} else {
					if (Objects.equals(PayConst.RedPackMode.NORMAL, mode)) {
						Long singleCny = Long.parseLong(redpacketVo.getSinglecny());
						for (short i = 1; i <= totalNum; i++) {
							boolean childinit = initRedRandom(rid, null, singleCny, mode, i);
							if (!childinit) {
								return failRet("子红包初始化异常");
							}
						}
					} else {
						Long minRed = (long) 1;
						Random random = new Random();
						for (short i = 1; i < totalNum; i++) {
							Long safe = (long) Math.ceil(((double) (totalCny - (totalNum - i) * minRed)) / (totalNum - i));
							int randnum = (int) (safe - minRed);
							Long cny = minRed;
							if (randnum > 0) {
								cny = random.nextInt(randnum) + minRed;
							}
							totalCny = totalCny - cny;
							boolean childinit = initRedRandom(rid, null, cny, mode, i);
							if (!childinit) {
								return failRet("子红包初始化异常");
							}
						}
						boolean childinit = initRedRandom(rid, null, totalCny, mode, totalNum);
						if (!childinit) {
							return failRet("子红包初始化异常");
						}
					}
				}
				return okRet(rid);
			}
		};
		if (isAtom) {
			Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		} else {
			atom.noTxRun();
		}
		return atom.getRetObj();
	}

	@Override
	public Ret quickRedpacket(BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		short timeout = redpacketVo.getPaytimeout();
		WxWalletSendRedPacket redPacket = WxWalletSendRedPacket.mapToBean(resp.getResp());
		redPacket.setAgrno(redpacketVo.getAgrno());
		redPacket.setPaynotifyurl(redpacketVo.getNotifyUrl());
		redPacket.setPaytimeout(timeout);
		redPacket.setStatus(PayConst.RedPacketStatus.PAYING);
		redPacket.setId(redpacketVo.getRid());
		redPacket.setPaytype(PayConst.RedPayType.BANKCARD);
		redPacket.setBacktime(DateUtil.offsetMinute(new DateTime(), timeout));
		Ret ret = updateRedPacketLock(redPacket, PayConst.RedPacketStatus.INIT, true);
		if (ret.isFail()) {
			return ret;
		}
		return RetUtils.okData(redPacket);
	}

	@Override
	public Ret payRedpacket(HttpRequest request, BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Integer offset = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, 1440);
		WxWalletSendRedPacket last = null;
		if (Objects.equals(redpacketVo.getPaytype(), PayConst.RedPayType.CNY)) {
			WxWalletSendRedPacket transfer = WxWalletSendRedPacket.mapToBean(resp.getResp());
			transfer.setId(redpacketVo.getRid());
			transfer.setStatus(PayConst.RedPacketStatus.PROCESS);
			transfer.setPaytype(PayConst.RedPayType.CNY);
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + redpacketVo.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", redpacketVo.getRid());
				if (last == null) {
					return RetUtils.failMsg("操作已超时，请重试");
				}
				if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PROCESS)) {
					log.error("发红包数据发生变更:last:{}", Json.toJson(last));
					return RetUtils.okData(last);
				}
				final WxWalletSendRedPacket finalLast = last;
				DateTime starttime = new DateTime();
				DateTime timeout = DateUtil.offsetMinute(starttime, offset);
				transfer.setStarttime(starttime);
				transfer.setBacktime(timeout);
				transfer.setSendmode(PayConst.RedSendMode.RESP);
				WalletVo walletVo = new WalletVo();
				walletVo.setUid(redpacketVo.getUid());
				walletVo.setWalletid(redpacketVo.getWalletid());
				BasePayReq req = new BasePayReq(request);
				req.setParams(walletVo.toMap());
				//同步钱包信息
				BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());
				AbsTxAtom absTxAtom = new AbsTxAtom() {

					@Override
					public boolean noTxRun() {
						Ret ret = updateRedPacketLock(transfer, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
						if (ret.isFail()) {//此处发生数据变更
							log.error("{}", RetUtils.getRetMsg(ret));
							return true;
						}
						//						boolean update = transfer.update();
						//						if(!update) {
						//							return failRet("充值记录保存失败");
						//						}
						Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
						if (synRet.isFail()) {
							return failRet(synRet);
						}
						Ret coinRet = coinAdd(finalLast.getCny(), finalLast.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.PAY, "发红包", finalLast.getId(),
						        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), DateUtil.format(transfer.getStarttime(), "yyyy-MM-dd HH:mm:ss"),
						        DateUtil.format(transfer.getStarttime(), "yyyy-MM-dd HH:mm:ss"), false);
						if (coinRet.isFail()) {
							return failRet(coinRet);
						}
						return true;
					}
				};
				boolean atom = false;
				if (isAtom) {
					atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
				} else {
					atom = absTxAtom.noTxRun();
				}
				if (!atom) {
					return absTxAtom.getRetObj();
				}
				//更新红包
				last.setStatus(PayConst.RedPacketStatus.PROCESS);
				last.setStarttime(transfer.getStarttime());
				last.setBacktime(transfer.getBacktime());
				last.setPaytype(PayConst.RedPayType.CNY);
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
			//此处发送红包
			try {
				last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", last.getId());
				if (Objects.equals(last.getSendmode(), PayConst.RedSendMode.RESP)) {
					WalletQueueApi.sendRedpacketCallback(last);
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} else {
			last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", redpacketVo.getRid());
		}
		return RetUtils.okData(last);
	}

	@Override
	public Ret redpacketCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom) {
		WxWalletSendRedPacket sendRedPacket = WxWalletSendRedPacket.mapToBean(resp.getResp());
		WxWalletSendRedPacket local = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  merorderid = ?", sendRedPacket.getMerorderid());
		if (local == null) {
			log.error("本地红包记录不存在");
			return RetUtils.failMsg("本地红包记录不存在");
		}
		DateTime starttime = new DateTime();
		WxWalletSendRedPacket update = new WxWalletSendRedPacket();
		update.setOrdererrormsg(sendRedPacket.getOrdererrormsg());
		update.setCheckdate(sendRedPacket.getCheckdate());
		update.setBizcompletetime(sendRedPacket.getBizcompletetime());
		update.setId(local.getId());
		update.setPaytype(PayConst.RedPayType.BANKCARD);
		update.setStarttime(starttime);
		update.setSendmode(PayConst.RedSendMode.CALLBACK);
		if (!result) {
			update.setStatus(PayConst.RedPacketStatus.FAIL);
			update.setOrdererrormsg(resp.getMerMsg());
		} else {
			if (resp.isBizOk()) {
				update.setStatus(PayConst.RedPacketStatus.PROCESS);
				Integer offset = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, 1440);
				DateTime timeout = DateUtil.offsetMinute(starttime, offset);
				update.setBacktime(timeout);
			} else {
				update.setStatus(PayConst.RedPacketStatus.FAIL);
				update.setOrdererrormsg(resp.getMerMsg());
			}
		}
		boolean atom = false;
		if (resp.isBizOk()) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + local.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", local.getId());
				if (last == null) {
					return RetUtils.failMsg("操作已超时，请重试");
				}
				if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PROCESS)) {
					log.error("发红包回调时，数据发生变更:last:{}", Json.toJson(last));
					//					return RetUtils.failMsg("红包数据发生变更");
				} else {
					final WxWalletSendRedPacket finalLast = last;
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							//							boolean callback = update.update();
							//							if(!callback) {
							//								return failRet("红包记录保存失败");
							//							}
							Ret ret = updateRedPacketLock(update, PayConst.RedPacketStatus.PAYING_CONFIRM, true);
							if (ret.isFail()) {//此处发生数据变更
								log.error("{}", RetUtils.getRetMsg(ret));
								return true;
							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", finalLast.getReqid());
							String redTimeStr = DateUtil.format(update.getStarttime(), "yyyy-MM-dd HH:mm:ss");
							Ret coinRet = coinAdd(finalLast.getCny(), finalLast.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.PAY, "发红包", finalLast.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), redTimeStr, redTimeStr, false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", local.getId());
			try {
				if (Objects.equals(last.getSendmode(), PayConst.RedSendMode.CALLBACK)) {
					WalletQueueApi.sendRedpacketCallback(last);
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} else {
			atom = update.update();
		}
		if (!atom) {
			return RetUtils.failMsg("红包回调失败");
		}
		return RetUtils.okMsg("200");
	}

	@Override
	public Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = true;
		}
		Integer rid = grabVo.getRid();
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GRAB + "." + rid, WxWalletGrabRedItem.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxWalletSendRedPacket redPacket = WxWalletSendRedPacket.dao.findById(rid);
			if (redPacket == null) {
				return RetUtils.failMsg("红包不存在");
			}
			if (Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.SUCCESS)) {
				return RetUtils.failMsg("红包已抢完");
			}
			if (Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.TIMEOUT)) {
				return RetUtils.failMsg("红包已超时退回");
			}
			if (Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.CANCEL)) {
				return RetUtils.failMsg("红包已取消");
			}
			if (!Objects.equals(redPacket.getStatus(), PayConst.RedPacketStatus.PROCESS)) {
				return RetUtils.failMsg("红包未知状态：" + redPacket.getStatus());
			}
			WxWalletRedPacketRandom exist = WxWalletRedPacketRandom.dao.findFirst("select * from wx_wallet_red_packet_random where rid = ? and  walletid = ?", rid,
			        user.getWalletid());
			if (exist != null && Objects.equals(exist.getStatus(), PayConst.RedRandomStatus.SUCCESS)) {
				return RetUtils.failMsg("您已抢过该红包");
			}
			if (exist != null && Objects.equals(exist.getStatus(), PayConst.RedRandomStatus.RANDOM)) {
				return RetUtils.okData(exist).set("redpacket", redPacket);
			}
			WxWalletRedPacketRandom random = getRandom(redPacket);
			if (random == null) {
				log.error("红包出现已抢完，但是没有结束：red:{}", Json.toJson(redPacket));
				return RetUtils.failMsg("红包已抢完");
			}
			if (Objects.equals(redPacket.getChatmode(), Const.ChatMode.P2P) && !Objects.equals(random.getUid(), user.getId())) {
				log.error("抢红包时出现空：私聊抢的人不等于分配的人：red:{},random:{}", Json.toJson(redPacket), Json.toJson(random));
				return RetUtils.failMsg("你不能抢自己的红包");
			}
			AbsTxAtom atom = new AbsTxAtom() {

				@Override
				public boolean noTxRun() {
					WxWalletRedPacketRandom position = new WxWalletRedPacketRandom();
					position.setId(random.getId());
					position.setWalletid(user.getWalletid());
					position.setStatus(PayConst.RedRandomStatus.RANDOM);
					position.setUid(user.getId());
					position.setGrabtime(new DateTime());
					boolean save = position.update();
					if (!save) {
						return failRet("随机红包失败");
					}
					random.setWalletid(user.getWalletid());
					random.setStatus(PayConst.RedRandomStatus.RANDOM);
					random.setUid(user.getId());
					return okRet(random);
				}
			};
			if (isAtom) {
				Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
			} else {
				atom.noTxRun();
			}
			return atom.getRetObj().set("redpacket", redPacket);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			writeLock.unlock();
		}
		return RetUtils.failMsg("系统错误");
	}

	@Override
	public Ret grabRedpacketCallback(HttpRequest request, BasePayResp resp, GrabRedpacketVo grabVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		WxWalletRedPacketRandom random = grabVo.getRandom();
		WxWalletSendRedPacket redPacket = grabVo.getRedPacket();
		if (isAtom == null) {
			isAtom = true;
		}
		DateTime time = new DateTime();
		WxWalletGrabRedItem redItem = WxWalletGrabRedItem.mapToBean(resp.getResp());
		redItem.setCny(random.getCny());
		redItem.setUid(random.getUid());
		redItem.setWalletid(random.getWalletid());
		redItem.setChatbizid(grabVo.getBizid());
		redItem.setChatmode(grabVo.getChatmode());
		redItem.setRid(redPacket.getId());
		redItem.setSenduid(redPacket.getUid());
		redItem.setStatus(PayConst.RedRandomStatus.SUCCESS);
		redItem.setSendwalletid(redPacket.getSubwalletid());
		redItem.setRemark(redPacket.getRemark());
		redItem.setIp(grabVo.getIp());
		redItem.setGrabtime(time);
		redItem.setAppversion(grabVo.getAppversion());
		redItem.setDevice(grabVo.getDevicetype());
		redItem.setRandomid(random.getId());
		boolean atom = false;
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + grabVo.getUid(), WxWalletCoin.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxWalletRedPacketRandom lastRandom = WxWalletRedPacketRandom.dao.findById(random.getId());
			if (lastRandom != null && !Objects.equals(lastRandom.getStatus(), PayConst.RedPacketStatus.SUCCESS)) {
				WalletVo walletVo = new WalletVo();
				walletVo.setUid(redItem.getUid());
				walletVo.setWalletid(redItem.getWalletid());
				BasePayReq req = new BasePayReq(request);
				req.setParams(walletVo.toMap());
				BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

				WalletVo subWalletVo = new WalletVo();
				subWalletVo.setUid(redItem.getUid());
				subWalletVo.setWalletid(redItem.getWalletid());
				BasePayReq subReq = new BasePayReq(request);
				subReq.setParams(subWalletVo.toMap());
				BasePayResp subWalletResp = basePay.getWalletInfo(subReq, subWalletVo.getUid());

				AbsTxAtom absTxAtom = new AbsTxAtom() {

					@Override
					public boolean noTxRun() {
						boolean save = redItem.save();
						;
						if (!save) {
							log.error("红包转账保存信息失败：{}", Json.toJson(redItem));
							return failRet("抢红包信息保存失败");
						}
						WxWalletRedPacketRandom position = new WxWalletRedPacketRandom();
						position.setId(random.getId());
						position.setStatus(PayConst.RedRandomStatus.SUCCESS);
						position.setAccounttime(time);
						boolean update = position.update();
						if (!update) {
							return failRet("随机红包确认失败");
						}
						Db.use(Const.Db.TIO_SITE_MAIN).update(
						        "UPDATE wx_wallet_send_red_packet set acceptnum = (select count(1) from wx_wallet_red_packet_random where rid = ? and `status` = ? ),`status` = IF(acceptnum = num,?,`status`),endtime = IF(`status` = ?,now(),NULL) where id = ?",
						        redPacket.getId(), PayConst.RedRandomStatus.SUCCESS, PayConst.RedPacketStatus.SUCCESS, PayConst.RedPacketStatus.SUCCESS, redPacket.getId());
						Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
						if (synRet.isFail()) {
							return failRet(synRet);
						}
						Ret synSubRet = synWallet(subWalletResp.getResp(), subWalletVo, false);
						if (synSubRet.isFail()) {
							return failRet(synSubRet);
						}
						String redTimeStr = DateUtil.format(redItem.getGrabtime(), "yyyy-MM-dd HH:mm:ss");
						Ret coinRet = coinAdd(redItem.getCny(), redItem.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.INCOME, "抢红包", redItem.getId(),
						        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), redTimeStr, redTimeStr, false);
						if (coinRet.isFail()) {
							return failRet(coinRet);
						}
						return okRet(redItem);
					}
				};
				if (isAtom) {
					atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
				} else {
					atom = absTxAtom.noTxRun();
				}
				if (!atom) {
					return absTxAtom.getRetObj();
				}
			} else {
				log.error("抢红包数据发生变更:last:{}", Json.toJson(lastRandom));
				return RetUtils.failMsg("抢红包数据发生变更");
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			writeLock.unlock();
		}
		if (atom) {
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findById(redPacket.getId());
			try {
				WalletQueueApi.grabRedpacket(redItem);
				if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.SUCCESS)) {
					log.error("发送红包已抢完通知：{}", Json.toJson(last));
					WalletQueueApi.sendRedpacketCallback(last);
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
		return RetUtils.okData(redItem);
	}

	@Override
	public Ret sendRedpacket(Map<String, Object> resp) {
		// TODO Auto-generated method stub
		return RetUtils.okOper();
	}

	@Override
	public Ret redpacketQuery(HttpRequest request, BasePayResp resp, RedpacketQueryVo queryVo, Boolean isAtom) {
		Map<String, Object> respMap = resp.getResp();
		Object objectStatus = respMap.get("merstatus");
		String merstatus = "";
		if (objectStatus != null) {
			merstatus = objectStatus.toString();
		}
		if (Objects.equals(merstatus, "1")) {
			Integer offset = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, 1440);
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + queryVo.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
				if (last != null && !Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PROCESS)) {
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							DateTime starttime = new DateTime();
							WxWalletSendRedPacket update = new WxWalletSendRedPacket();
							update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
							update.setId(last.getId());
							update.setPaytype(PayConst.RedPayType.BANKCARD);
							update.setStarttime(starttime);
							DateTime timeout = DateUtil.offsetMinute(starttime, offset);
							update.setBacktime(timeout);
							update.setStatus(PayConst.RedPacketStatus.PROCESS);
							update.setSendmode(PayConst.RedSendMode.QUERY);
							update.setQuerysyn(Const.YesOrNo.YES);
							Ret ret = updateRedPacketLock(update, last.getStatus(), true);
							if (ret.isFail()) {//此处发生数据变更
								log.error("{}", RetUtils.getRetMsg(ret));
								return true;
							}
							//							boolean callback = update.update();
							//							if(!callback) {
							//								return failRet("发红包记录保存失败");
							//							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", last.getReqid());
							String redTimeStr = DateUtil.format(update.getStarttime(), "yyyy-MM-dd HH:mm:ss");
							Ret coinRet = coinAdd(last.getCny(), last.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.PAY, "发红包", last.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), redTimeStr, redTimeStr, false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					boolean atom = false;
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", queryVo.getRid());
			try {
				if (Objects.equals(last.getSendmode(), PayConst.RedSendMode.QUERY)) {
					WalletQueueApi.sendRedpacketCallback(last);
				}
				return RetUtils.okData(last);
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} else if (Objects.equals(merstatus, "0")) {
		} else if (Objects.equals(merstatus, "3")) {
		} else {
			WxWalletSendRedPacket update = new WxWalletSendRedPacket();
			update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
			update.setId(queryVo.getRid());
			update.setPaytype(PayConst.RedPayType.BANKCARD);
			update.setStatus(PayConst.RedPacketStatus.FAIL);
			update.setQuerysyn(Const.YesOrNo.YES);
			update.setOrdererrormsg(resp.getMerMsg());
			update.update();
		}
		WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
		return RetUtils.okData(last);
	}

	/**
	 * @param resp
	 * @param queryVo
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月24日 下午4:53:48
	 */
	@Override
	public Ret redpacketJobQuery(BasePayResp resp, RedpacketQueryVo queryVo, Boolean isAtom) {
		Map<String, Object> respMap = resp.getResp();
		Object objectStatus = respMap.get("merstatus");
		String merstatus = "";
		if (objectStatus != null) {
			merstatus = objectStatus.toString();
		}
		if (Objects.equals(merstatus, "1")) {
			Integer offset = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, 1440);
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + queryVo.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
				if (last != null) {
					if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PAYING) || Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PAYING_CONFIRM)) {
						WalletVo walletVo = new WalletVo();
						walletVo.setUid(last.getUid());
						walletVo.setWalletid(last.getWalletid());
						BasePayReq req = new BasePayReq(null);
						req.setParams(walletVo.toMap());
						BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

						AbsTxAtom absTxAtom = new AbsTxAtom() {

							@Override
							public boolean noTxRun() {
								DateTime starttime = new DateTime();
								WxWalletSendRedPacket update = new WxWalletSendRedPacket();
								update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
								update.setId(last.getId());
								//								update.setPaytype(PayConst.RedPayType.BANKCARD);
								update.setStarttime(starttime);
								update.setSendmode(PayConst.RedSendMode.JOB);
								DateTime timeout = DateUtil.offsetMinute(starttime, offset);
								update.setBacktime(timeout);
								update.setStatus(PayConst.RedPacketStatus.PROCESS);
								update.setQuerysyn(Const.YesOrNo.YES);
								//								boolean callback = update.update();
								//								if(!callback) {
								//									log.error("发红包补偿记录保存失败：{}",Json.toJson(last));
								//									return failRet("发红包补偿记录保存失败");
								//								}
								Ret ret = updateRedPacketLock(update, last.getStatus(), true);
								if (ret.isFail()) {//此处发生数据变更
									log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
									return true;
								}
								Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
								if (synRet.isFail()) {
									return failRet(synRet);
								}
								resp.getResp().put("reqid", last.getReqid());
								String redTimeStr = DateUtil.format(update.getStarttime(), "yyyy-MM-dd HH:mm:ss");
								Ret coinRet = coinAdd(last.getCny(), last.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.PAY, "发红包", last.getId(),
								        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), redTimeStr, redTimeStr, false);
								if (coinRet.isFail()) {
									return failRet(coinRet);
								}
								return true;
							}
						};
						boolean atom = false;
						if (isAtom) {
							atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
						} else {
							atom = absTxAtom.noTxRun();
						}
						if (!atom) {
							return absTxAtom.getRetObj();
						}
					}
				} else {
					return RetUtils.failMsg("红包不存在");
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where  id = ?", queryVo.getRid());
			try {
				if (Objects.equals(last.getSendmode(), PayConst.RedSendMode.JOB)) {
					WalletQueueApi.sendRedpacketCallback(last);
				}
				return RetUtils.okOper();
			} catch (Exception e) {
				log.error(e.toString(), e);
			}

		} else if (Objects.equals(merstatus, "0")) {
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
			if (last != null) {
				if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PAYING)) {
					WxWalletSendRedPacket update = new WxWalletSendRedPacket();
					update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
					update.setId(last.getId());
					//						update.setPaytype(PayConst.RedPayType.BANKCARD);
					update.setStatus(PayConst.RedPacketStatus.CANCEL);
					update.setRemark("快捷支付发起-进行中异常处理为取消");
					update.setQuerysyn(Const.YesOrNo.YES);
					//						boolean callback = update.update();
					//						if(!callback) {
					//							log.error("快捷支付发起-进行中异常处理为取消-保存异常：{}",Json.toJson(last));
					//							return RetUtils.failMsg("定时处理异常订单错误");
					//						}
					Ret ret = updateRedPacketLock(update, last.getStatus(), true);
					if (ret.isFail()) {//此处发生数据变更
						log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
						return ret;
					}

				} else {
					WxWalletSendRedPacket update = new WxWalletSendRedPacket();
					update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
					update.setId(last.getId());
					//						update.setPaytype(PayConst.RedPayType.BANKCARD);
					update.setStatus(PayConst.RedPacketStatus.FAIL);
					update.setRemark("支付确认-进行中异常处理为错误");
					update.setQuerysyn(Const.YesOrNo.YES);
					//						boolean callback = update.update();
					//						if(!callback) {
					//							log.error("支付确认-进行中异常处理为错误-保存异常：{}",Json.toJson(last));
					//							return RetUtils.failMsg("定时处理异常订单错误");
					//						}
					Ret ret = updateRedPacketLock(update, last.getStatus(), true);
					if (ret.isFail()) {//此处发生数据变更
						log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
						return ret;
					}
				}
				return RetUtils.okOper();
			}
		} else if (Objects.equals(merstatus, "3")) {
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
			WxWalletSendRedPacket update = new WxWalletSendRedPacket();
			update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
			update.setId(last.getId());
			update.setStatus(PayConst.RedPacketStatus.CANCEL);
			update.setRemark("订单处理为取消-原始订单状态：" + last.getStatus());
			update.setQuerysyn(Const.YesOrNo.YES);
			//			boolean callback = update.update();
			//			if(!callback) {
			//				log.error("订单处理为取消-保存异常：{}",Json.toJson(last));
			//				return RetUtils.failMsg("定时处理异常订单错误");
			//			}
			Ret ret = updateRedPacketLock(update, last.getStatus(), true);
			if (ret.isFail()) {//此处发生数据变更
				log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
				return ret;
			}
			return RetUtils.okOper();
		} else {
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", queryVo.getRid());
			if (last != null) {
				WxWalletSendRedPacket update = new WxWalletSendRedPacket();
				update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
				update.setId(last.getId());
				update.setStatus(PayConst.RedPacketStatus.FAIL);
				update.setRemark("订单处理为失败-原始订单状态：" + last.getStatus());
				update.setQuerysyn(Const.YesOrNo.YES);
				//					boolean callback = update.update();
				//					if(!callback) {
				//						log.error("订单处理为失败-保存异常：{}",Json.toJson(last));
				//						return RetUtils.failMsg("定时处理异常订单错误");
				//					}
				Ret ret = updateRedPacketLock(update, last.getStatus(), true);
				if (ret.isFail()) {//此处发生数据变更
					log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
					return ret;
				}
				return RetUtils.okOper();
			}
		}
		return RetUtils.failMsg("系统异常");
	}

	@Override
	public Ret redpacketTimeOut(WxWalletSendRedPacket redPacket, Boolean isAtom) {
		ReentrantReadWriteLock grabRwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GRAB + "." + redPacket.getId(), WxWalletGrabRedItem.class);
		WriteLock grabWriteLock = grabRwLock.writeLock();
		grabWriteLock.lock();
		try {
			//超时状态处理
			WxWalletSendRedPacket last = WxWalletSendRedPacket.dao.findFirst("select * from wx_wallet_send_red_packet where id = ?", redPacket.getId());
			if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.SUCCESS)) {
				return RetUtils.okOper();
			}
			WxWalletSendRedPacket update = new WxWalletSendRedPacket();
			update.setId(redPacket.getId());
			update.setStatus(PayConst.RedPacketStatus.TIMEOUT);
			Ret ret = updateRedPacketLock(update, redPacket.getStatus(), true);
			if (ret.isFail()) {//此处发生数据变更-期待下次处理
				log.error("数据发生变更:{}", RetUtils.getRetMsg(ret));
				return ret;
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			grabWriteLock.unlock();
		}
		Record record = Db.use(Const.Db.TIO_SITE_MAIN).findFirst("select count(id) count,IFNULL(sum(cny),0) cny from wx_wallet_red_packet_random WHERE rid = ? and `status` = ?",
		        redPacket.getId(), PayConst.RedRandomStatus.INIT);
		long cny = record.getLong("cny");
		int count = record.getInt("count");
		if (cny <= 0 || count <= 0) {
			return RetUtils.failMsg("超时的红包数异常：" + cny);
		}
		Integer uid = redPacket.getUid();
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + uid, WxWalletCoin.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxWalletBackRedPacketItems backitem = new WxWalletBackRedPacketItems();
			backitem.setUid(uid);
			backitem.setWalletid(redPacket.getWalletid());
			backitem.setSubwalletid(redPacket.getSubwalletid());
			backitem.setCny(cny);
			backitem.setMode(redPacket.getMode());
			backitem.setNum((short) count);
			backitem.setStatus(PayConst.WalletChangeStatus.SUCCESS);
			backitem.setRid(redPacket.getId());
			backitem.setDealtime(new DateTime());
			//转账
			TransferVo transferVo = new TransferVo();
			transferVo.setCny(cny + "");
			transferVo.setTowalletid(redPacket.getWalletid());
			transferVo.setWalletid(redPacket.getSubwalletid());
			transferVo.setUid(uid);
			BasePayReq transferReq = new BasePayReq(null);
			transferReq.setParams(transferVo.toMap());
			BasePayResp resp = basePay.transfer(transferReq, transferVo.getUid());
			if (!resp.isBizOk()) {
				log.error("出现了异常：{}", resp.getMerMsg());
				//				return RetUtils.failMsg(resp.getMerMsg());
			}
			backitem.setReqid(MapUtil.getStr(resp.getResp(), "reqid"));
			backitem.setMerid(MapUtil.getStr(resp.getResp(), "merid"));
			backitem.setMerorderid(MapUtil.getStr(resp.getResp(), "merorderid"));
			backitem.setBacktime(new DateTime());
			//查询两个账号的钱
			WalletVo subWalletVo = new WalletVo();
			subWalletVo.setUid(-uid);
			subWalletVo.setWalletid(redPacket.getSubwalletid());
			BasePayReq subReq = new BasePayReq(null);
			subReq.setParams(subWalletVo.toMap());
			BasePayResp subWalletResp = basePay.getWalletInfo(subReq, subWalletVo.getUid());
			WalletVo walletVo = new WalletVo();
			walletVo.setUid(uid);
			walletVo.setWalletid(redPacket.getWalletid());
			BasePayReq req = new BasePayReq(null);
			req.setParams(walletVo.toMap());
			BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

			AbsTxAtom absTxAtom = new AbsTxAtom() {

				@Override
				public boolean noTxRun() {
					//红包最后处理
					WxWalletSendRedPacket timeout = new WxWalletSendRedPacket();
					timeout.setId(redPacket.getId());
					timeout.setRemark("超时处理成功");
					timeout.setEndtime(new DateTime());
					boolean update = timeout.update();
					if (!update) {
						log.error("红包超时保存信息失败：{}", Json.toJson(timeout));
						return failRet("红包超时信息保存失败");
					}
					//两个账户同步数据
					Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
					if (synRet.isFail()) {
						return failRet(synRet);
					}
					Ret synSubRet = synWallet(subWalletResp.getResp(), subWalletVo, false);
					if (synSubRet.isFail()) {
						return failRet(synSubRet);
					}
					//红包退回记录保存
					backitem.setCompletetime(new DateTime());
					backitem.replaceSave();
					String startTimeStr = DateUtil.format(backitem.getBacktime(), "yyyy-MM-dd HH:mm:ss");
					String endTimeStr = DateUtil.format(backitem.getCompletetime(), "yyyy-MM-dd HH:mm:ss");
					//明细保存
					Ret coinRet = coinAdd(cny, uid, PayConst.WalletMode.REDPACKET, Const.CoinFlag.INCOME, "红包退回", backitem.getId(), PayConst.WalletChangeStatus.SUCCESS,
					        resp.getResp(), startTimeStr, endTimeStr, false);
					if (coinRet.isFail()) {
						return failRet(coinRet);
					}
					return true;
				}
			};
			boolean atom = false;
			if (isAtom) {
				atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
			} else {
				atom = absTxAtom.noTxRun();
			}
			if (!atom) {
				return absTxAtom.getRetObj();
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			writeLock.unlock();
		}
		return RetUtils.okOper();
	}

	@Override
	public Ret withhold(BasePayResp resp, WithholdVo withholdVo, Boolean isAtom) {
		String respCheck = resp.getMerMsg();
		if (StrUtil.isNotBlank(respCheck)) {
			WxWalletWithholdItems holditems = WxWalletWithholdItems.mapToBean(resp.getResp());
			holditems.setWalletid(withholdVo.getWalletid());
			holditems.setAmount(Long.parseLong(withholdVo.getAmount()));
			holditems.setAgrno(withholdVo.getAgrno());
			holditems.setNotifyurl(withholdVo.getNotifyUrl());
			holditems.setArrivalamount(holditems.getAmount() - holditems.getBizfee());
			holditems.setIp(withholdVo.getIpInfo().getIp());
			holditems.setDevice(withholdVo.getDevicetype());
			holditems.setAppversion(withholdVo.getAppversion());
			holditems.setStatus(PayConst.WalletChangeStatus.FAIL);
			if (StrUtil.isBlank(holditems.getBizcompletetime())) {
				holditems.setBizcompletetime(DateUtil.format(new DateTime(), "yyyy-MM-dd HH:mm:ss"));
			}
			holditems.setResptime(new DateTime());
			boolean save = holditems.save();
			if (!save) {
				log.error("保存错误提现信息异常:{}", Json.toJson(holditems));
			}
			return RetUtils.failMsg(respCheck);
		}
		WxWalletWithholdItems holditems = WxWalletWithholdItems.mapToBean(resp.getResp());
		holditems.setWalletid(withholdVo.getWalletid());
		holditems.setAmount(Long.parseLong(withholdVo.getAmount()));
		holditems.setAgrno(withholdVo.getAgrno());
		holditems.setNotifyurl(withholdVo.getNotifyUrl());
		holditems.setArrivalamount(holditems.getAmount() - holditems.getBizfee());
		holditems.setIp(withholdVo.getIpInfo().getIp());
		holditems.setDevice(withholdVo.getDevicetype());
		holditems.setMerstatus("-1");
		holditems.setAppversion(withholdVo.getAppversion());
		holditems.setStatus(PayConst.WalletChangeStatus.LOCAL);
		holditems.setResptime(new DateTime());
		boolean save = holditems.save();
		if (!save) {
			return RetUtils.failMsg("保存提现信息异常");
		}
		return RetUtils.okData(holditems);
	}

	@Override
	public Ret withholdCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom) {
		WxWalletWithholdItems holditems = WxWalletWithholdItems.mapToBean(resp.getResp());
		WxWalletWithholdItems local = WxWalletWithholdItems.dao.findFirst("select * from wx_wallet_withhold_items where merorderid = ?", holditems.getMerorderid());
		if (local == null) {
			log.error("本地记录不存在");
			return RetUtils.failMsg("本地记录不存在");
		}
		WxWalletWithholdItems update = new WxWalletWithholdItems();
		update.setPayacctamount(holditems.getPayacctamount());
		update.setBizcompletetime(holditems.getBizcompletetime());
		update.setBizfee(holditems.getBizfee());
		update.setCallbacktime(new DateTime());
		update.setMerstatus(holditems.getMerstatus());
		update.setOrdererrormsg(holditems.getOrdererrormsg());
		update.setId(local.getId());
		if (!result) {
			update.setStatus(PayConst.WalletChangeStatus.FAIL);
			update.setOrdererrormsg(resp.getMsg());
		} else {
			if (resp.isBizOk()) {
				update.setStatus(PayConst.WalletChangeStatus.SUCCESS);
			} else {
				update.setStatus(PayConst.WalletChangeStatus.FAIL);
				update.setOrdererrormsg(resp.getMerMsg());
			}
		}
		boolean atom = false;
		if (resp.isBizOk()) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + local.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletWithholdItems last = WxWalletWithholdItems.dao.findFirst("select * from wx_wallet_withhold_items where id = ?", local.getId());
				if (Objects.equals(last.getStatus(), PayConst.WalletChangeStatus.SUCCESS)) {
					atom = update.update();
				} else {
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							boolean callback = update.update();
							if (!callback) {
								return failRet("提现记录保存失败");
							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", last.getReqid());
							Ret coinRet = coinAdd(last.getAmount(), last.getUid(), PayConst.WalletMode.WIHTHOLD, Const.CoinFlag.PAY, "提现", last.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), DateUtil.format(last.getResptime(), "yyyy-MM-dd HH:mm:ss"),
							        DateUtil.format(update.getCallbacktime(), "yyyy-MM-dd HH:mm:ss"), false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}

		} else {
			atom = update.update();
		}
		if (!atom) {
			return RetUtils.failMsg("提现失败");
		}
		return RetUtils.okMsg("200");
	}

	@Override
	public Ret withholdQuery(HttpRequest request, BasePayResp resp, WithholdQueryVo withholdVo, Boolean isAtom) {
		Map<String, Object> respMap = resp.getResp();
		Object objectStatus = respMap.get("merstatus");
		String merstatus = "";
		if (objectStatus != null) {
			merstatus = objectStatus.toString();
		}
		if (Objects.equals(merstatus, "1")) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + withholdVo.getUid(), WxWalletCoin.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletWithholdItems last = WxWalletWithholdItems.dao.findFirst("select * from wx_wallet_withhold_items where id = ?", withholdVo.getWid());
				if (!Objects.equals(last.getStatus(), PayConst.WalletChangeStatus.SUCCESS)) {
					WalletVo walletVo = new WalletVo();
					walletVo.setUid(last.getUid());
					walletVo.setWalletid(last.getWalletid());
					BasePayReq req = new BasePayReq(request);
					req.setParams(walletVo.toMap());
					BasePayResp walletResp = basePay.getWalletInfo(req, walletVo.getUid());

					AbsTxAtom absTxAtom = new AbsTxAtom() {

						@Override
						public boolean noTxRun() {
							WxWalletWithholdItems update = new WxWalletWithholdItems();
							update.setId(withholdVo.getWid());
							update.setStatus(PayConst.WalletChangeStatus.SUCCESS);
							update.setMerstatus(MapUtil.getStr(respMap, "merstatus"));
							update.setOrdererrormsg(resp.getMerMsg());
							update.setQuerysyn(Const.YesOrNo.YES);
							update.setQueuetime(new DateTime());
							update.setBizcompletetime(MapUtil.getStr(respMap, "bizcompletetime"));
							boolean callback = update.update();
							if (!callback) {
								return failRet("充值记录保存失败");
							}
							Ret synRet = synWallet(walletResp.getResp(), walletVo, false);
							if (synRet.isFail()) {
								return failRet(synRet);
							}
							resp.getResp().put("reqid", last.getReqid());
							Ret coinRet = coinAdd(last.getAmount(), last.getUid(), PayConst.WalletMode.WIHTHOLD, Const.CoinFlag.PAY, "提现", last.getId(),
							        PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), DateUtil.format(last.getResptime(), "yyyy-MM-dd HH:mm:ss"),
							        DateUtil.format(update.getQueuetime(), "yyyy-MM-dd HH:mm:ss"), false);
							if (coinRet.isFail()) {
								return failRet(coinRet);
							}
							return true;
						}
					};
					boolean atom = false;
					if (isAtom) {
						atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
					} else {
						atom = absTxAtom.noTxRun();
					}
					if (!atom) {
						return absTxAtom.getRetObj();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else if (Objects.equals(merstatus, "0")) {
		} else if (Objects.equals(merstatus, "3")) {
		} else {
			WxWalletWithholdItems update = new WxWalletWithholdItems();
			update.setId(withholdVo.getWid());
			update.setStatus(PayConst.WalletChangeStatus.FAIL);
			update.setMerstatus(merstatus);
			update.setQuerysyn(Const.YesOrNo.YES);
			update.setQueuetime(new DateTime());
			update.setOrdererrormsg(resp.getMerMsg());
			update.update();
		}
		WxWalletWithholdItems item = getWithholdInfo(withholdVo.getWid());
		return RetUtils.okData(item);
	}

	/**
	 * @param bankcode
	 * @return
	 * @author lixinji
	 * 2021年3月11日 上午9:57:07
	 */
	public static String dealBankCode(String bankcode) {
		int leng = bankcode.length();
		String per = bankcode.substring(0, 6);
		String last = bankcode.substring(leng - 4);
		String replace = "*******************";
		return per + replace.substring(10) + last;
	}

	/**
	 * 初始化子红包
	 * @param rid
	 * @param uid
	 * @param cny
	 * @param packetType
	 * @param index
	 * @return
	 * @author lixinji
	 * 2021年3月16日 下午1:56:57
	 */
	private static boolean initRedRandom(Integer rid, Integer touid, Long cny, Short packetType, Short index) {
		WxWalletRedPacketRandom random = new WxWalletRedPacketRandom();
		random.setRid(rid);
		if (touid != null) {
			random.setUid(touid);
		}
		random.setCny(cny);
		random.setMode(packetType);
		random.setRedindex(index);
		if (!random.save()) {
			log.error("子红包初始化异常：{}", Json.toJson(random));
			return false;
		}
		return true;
	}

	/**
	 * @param redpacket
	 * @return
	 * @author lixinji
	 * 2021年3月17日 上午10:48:19
	 */
	private WxWalletRedPacketRandom getRandom(WxWalletSendRedPacket redpacket) {
		WxWalletRedPacketRandom random = WxWalletRedPacketRandom.dao.findFirst("select * from wx_wallet_red_packet_random where rid = ? and status = ? order by redindex",
		        redpacket.getId(), PayConst.RedRandomStatus.INIT);
		if (random == null) {
			log.error("抢红包时出现空：没有红包了", Json.toJson(redpacket));
			return null;
		}
		return random;
	}

	/**
	 * 获取红包时是否加锁
	 * 场景：一般针对红包初始化到预支付过程中的超时处理
	 * @param rid
	 * @param lock
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月23日 上午11:09:16
	 */
	@Override
	public WxWalletSendRedPacket getRedPacketLock(Integer rid, Boolean lock) {
		if (lock == null) {
			lock = false;
		}
		if (lock) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GET_LOCK + "." + rid, WxWalletSendRedPacket.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket sendRed = WxWalletSendRedPacket.dao.findById(rid);
				return sendRed;
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			WxWalletSendRedPacket sendRed = WxWalletSendRedPacket.dao.findById(rid);
			return sendRed;
		}
		return null;
	}

	/**
	 * 修改红包主体是否加锁
	 * @param redPacket
	 * @param lock
	 * @return
	 * @author lixinji
	 * 2021年3月23日 上午11:12:24
	 */
	@Override
	public Ret updateRedPacketLock(WxWalletSendRedPacket redPacket, Short status, boolean lock) {
		if (lock) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GET_LOCK + "." + redPacket.getId(), WxWalletSendRedPacket.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacket sendRed = WxWalletSendRedPacket.dao.findById(redPacket.getId());
				if (sendRed == null) {
					return RetUtils.failMsg("操作已超时，请重试");
				}
				if (status != null && !Objects.equals(sendRed.getStatus(), status)) {
					log.error("修改红包时，发现状态发生变更，red:{},updatestatus:{}", Json.toJson(redPacket), status);
					return RetUtils.failMsg("红包状态已发生变更");
				}
				boolean update = redPacket.update();
				if (!update) {
					return RetUtils.failMsg("修改红包数据异常");
				}
				return RetUtils.okOper();
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			boolean update = redPacket.update();
			if (!update) {
				return RetUtils.failMsg("修改红包数据异常");
			}
			return RetUtils.okOper();
		}
		return RetUtils.failMsg("系统异常");
	}

	/**
	 * @param id
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:42:05
	 */
	public static WxWalletWithholdItems getWithholdInfo(Integer id) {
		Kv params = Kv.by("id", id);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("withhold.info", params);
		return WxWalletWithholdItems.dao.findFirst(sqlPara);
	}

}
