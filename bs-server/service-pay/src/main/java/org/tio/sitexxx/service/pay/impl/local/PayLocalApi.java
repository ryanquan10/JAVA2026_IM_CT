
package org.tio.sitexxx.service.pay.impl.local;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson15.JSONObject;
import com.upay.sdk.exception.HmacVerifyException;
import com.upay.sdk.exception.RequestException;
import com.upay.sdk.exception.ResponseException;
import com.upay.sdk.exception.UnknownException;
import com.upay.sdk.executer.ResultListenerAdpater;
import com.upay.sdk.webox.builder.*;
import com.upay.sdk.webox.executer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.*;
import org.tio.sitexxx.service.pay.service.WalletQueueApi;
import org.tio.sitexxx.service.service.atom.AbsAtom;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.BankConfService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.AmountUtil;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.ncount.NRequestUtils;
import org.tio.sitexxx.service.vo.*;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 易支付api
 * @author lixinji
 * 2020年11月10日 上午10:18:12
 */
public class PayLocalApi implements BasePay<BasePayReq, BasePayResp> {

	private static Logger log = LoggerFactory.getLogger(PayLocalApi.class);

	/**
	 * 请求服务机器随机码
	 */
	private Integer payReqIdIndex = 1;

	/**
	 * 易支付本地手续费设置-暂不设
	 */
	@Override
	public long commission(long amount) {
		Integer rate = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION, 5);
		Integer withholdconst = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION_CONST, 50);
		long commission = amount * rate / 1000 + withholdconst;
		return amount - commission;
	}

	/* 
	 * 易支付开户逻辑，未添加查询同步逻辑
	 */
	@Override
	public BasePayResp openUser(BasePayReq PayQuest, Integer uid) {
		OpenUserVo userVo = OpenUserVo.toBean(PayQuest.getParams());
		BasePayResp basePayResp = new BasePayResp();
		try {
			//TODO:lixinji-业务数据保存
			AbsAtom atom = new AbsTxAtom() {

				@Override
				public boolean noTxRun() {
					WxUserWalletLocal wallet = new WxUserWalletLocal();
					wallet.setUid(userVo.getUid());
					wallet.setReqid(getReqId());
					wallet.setBizid(getMerchantid());
					wallet.setWalletid(getUUID());
					wallet.setIp(getIp(PayQuest));
					wallet.setDevice(getDeviceType(getReqExt(PayQuest)));
					wallet.setOperatorstatus(Short.valueOf("1") );
					wallet.setRealnamestatus(Short.valueOf("1"));
					wallet.setCoinsyn(Short.valueOf("3"));
					wallet.setAppversion(getAppVersion(getReqExt(PayQuest)));
					boolean save = wallet.save();
					if (!save) {
						log.error("钱包开户异常");
						return failRet("保存钱包逻辑失败");
					}
					User user = new User();
					user.setOpenflag(Const.YesOrNo.YES);
					user.setOpenid(wallet.getId());
					user.setId(userVo.getUid());
					user.setIdcard(userVo.getCardno());
					user.setName(userVo.getName());
					user.setPhone(userVo.getMobile());
					boolean update = user.update();
					if (!update) {
						return failRet("修改用户钱包逻辑异常");
					}
					WxUserCoinLocal wxUserCoinLocal = new WxUserCoinLocal();
					wxUserCoinLocal.setCny(0L);
					wxUserCoinLocal.setCreatetime(new Date());
					wxUserCoinLocal.setUid(userVo.getUid());
					wxUserCoinLocal.setWithdrawcny(0L);
					wxUserCoinLocal.setWalletid(wallet.getWalletid());
//					wxUserCoinLocal.setCostpwd(encrypt(userVo.getPaypwd()));
					wxUserCoinLocal.setUpdatetime(new Date());
					boolean save1 = wxUserCoinLocal.save();
					if (!save1) {
						return failRet("保存钱包逻辑失败");
					}
					WxWalletLocal wxWallet = new WxWalletLocal();
					wxWallet.setUid(userVo.getUid());
					wxWallet.setReqid(wallet.getReqid());
					wxWallet.setWalletid(wallet.getWalletid());
					wxWallet.setStatus((short) 1);
					wxWallet.setAuthstatus("1");
					wxWallet.setAuditstatus("1");
					wxWallet.setMainflag((short) 1);
					wxWallet.setIp(getIp(PayQuest));
					wxWallet.setDevice(wallet.getDevice());
					wxWallet.setAppversion(wallet.getAppversion());
					wxWallet.setCreatetime(new Date());
					wxWallet.setUpdatetime(new Date());
					boolean save2 = wxWallet.save();
					if (!save2) {
						return failRet("保存钱包逻辑失败");
					}

					WxWalletCoinLocal walletCoinLocal = new WxWalletCoinLocal();
					walletCoinLocal.setWid(wxWallet.getId());
					walletCoinLocal.setWalletid(wxWallet.getWalletid());
					walletCoinLocal.setUid(wxWallet.getUid());
					walletCoinLocal.setCny(0L);
					walletCoinLocal.setUnclearcny(0L);
					walletCoinLocal.setWithdrawcny(0L);
					walletCoinLocal.setAcceptredpacket(0L);
					walletCoinLocal.setSendpacket(0L);
					walletCoinLocal.setCreatetime(new Date());
					walletCoinLocal.setUpdatetime(new Date());
					boolean save3 = walletCoinLocal.save();
					if (!save3) {
						return failRet("保存钱包逻辑失败");
					}
					WxWalletInfoLocal wxWalletInfoLocal = new WxWalletInfoLocal();
					wxWalletInfoLocal.setUid(userVo.getUid());
					wxWalletInfoLocal.setMobile(userVo.getMobile());
					wxWalletInfoLocal.setName(userVo.getName());
					wxWalletInfoLocal.setCardno(userVo.getCardno());
					wxWalletInfoLocal.setStatus((short) 1);
					wxWalletInfoLocal.setCreatetime(new Date());
					wxWalletInfoLocal.setUpdatetime(new Date());
					boolean save4 = wxWalletInfoLocal.save();
					if (!save4) {
						return failRet("保存钱包逻辑失败");
					}
					return true;
				}
			};
			boolean tx = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
			if (!tx) {
				basePayResp.setOk(false);
				basePayResp.setMsg(RetUtils.getRetMsg(atom.getRetObj()));
			} else {
				UserService.ME._clearCache(userVo.getUid());
				basePayResp.setOk(true);
			}
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	@Override
	public BasePayResp bindBankCard(BasePayReq PayQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp removeBankCard(BasePayReq PayQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * 钱包信息
	 */
	@Override
	public BasePayResp getWalletInfo(BasePayReq PayQuest, Integer uid) {
		WalletVo walletVo = WalletVo.toBean(PayQuest.getParams());
		BasePayResp basePayResp = new BasePayResp();
		String merchantId = getMerchantid();
		String walletId = walletVo.getWalletid();
		try {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where walletid = ?", walletId);
			Wallet5UResp resp = new Wallet5UResp();
			resp.setWalletId(walletId);
			resp.setMerchantId(merchantId);
			resp.setBalance(userCoin.getCny().toString());
			resp.setWalletStatus("ACTIVATE");


			if (resp.getWalletStatus().equals(PayLocalConst.WalletStatus.ACTIVATE)) {
				basePayResp.setOk(true);
				WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
			} else {
				basePayResp.setOk(false);
				basePayResp.setMsg("钱包异常：状态-" + resp.getWalletStatus());
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 发红包
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp sendRedpacket(BasePayReq PayQuest, Integer uid) {
		SendRedpacketLocalVo redpacketVo = SendRedpacketLocalVo.toBean(PayQuest.getParams());
		log.error("basePay sendRedpacket debugger --> basePay sendRedpacket begin. redpacketVo: {}, uid: {}", redpacketVo.toString(), uid);
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
		String packetType = "";
		if (Objects.equals(redpacketVo.getMode(), PayConst.RedPackMode.LUCK)) {
			packetType = PayLocalConst.RedPacketType.GROUP_LUCK;
		} else if (redpacketVo.getNum() > 1) {
			packetType = PayLocalConst.RedPacketType.GROUP_NORMAL;
		} else {
			packetType = PayLocalConst.RedPacketType.ONE_TO_ONE;
		}


		BasePayResp basePayResp = new BasePayResp();
		try {

			Map<String, Object> map = redpacketVo.toMap();
			WxUserSendRedItemLocal redItem = new WxUserSendRedItemLocal();
			redItem.setUid(uid);
			redItem.setChatbizid(redpacketVo.getBizid());
			redItem.setMode(redpacketVo.getMode());
			redItem.setChatmode(redpacketVo.getChatmode());
			redItem.setReqid(getReqId());
			redItem.setBizid(getMerchantid());
			redItem.setSerialnumber(getUUID());
			redItem.setPacketcount(redpacketVo.getNum());
			redItem.setWalletid(redpacketVo.getWalletid());
			redItem.setCurrency(redpacketVo.getCurrency());
			redItem.setAmount(Integer.parseInt(redpacketVo.getCny()));
//						redItem.setToken(resp.getToken());
			redItem.setTimeout(timeout);
			redItem.setRemark(redpacketVo.getRemark());
			redItem.setBizcreattime(new Date().toString());
			redItem.setStatus("SEND");
			redItem.setIp(getIp(PayQuest));
			redItem.setDevice(getDeviceType(getReqExt(PayQuest)));
			redItem.setAppversion(getAppVersion(getReqExt(PayQuest)));
			boolean save = redItem.save();
			log.error("basePay sendRedpacket debugger --> basePay sendRedpacket. WxUserSendRedItemLocal save: {}, redItem: {}", save, redItem.toString());
			if (!save) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化发红包订单数据失败");
				log.error("初始化发红包订单数据失败");
			} else {
				WxWalletSendRedPacketLocal wxWalletSendRedPacketLocal = new WxWalletSendRedPacketLocal();
				wxWalletSendRedPacketLocal.setUid(redpacketVo.getUid());
				wxWalletSendRedPacketLocal.setChatmode(redpacketVo.getChatmode());
				wxWalletSendRedPacketLocal.setChatbizid(redpacketVo.getChatlinkid());
				wxWalletSendRedPacketLocal.setWalletid(redpacketVo.getWalletid());
				wxWalletSendRedPacketLocal.setMerorderid(redItem.getSerialnumber());
				wxWalletSendRedPacketLocal.setCny(Long.valueOf(redItem.getAmount()));
				wxWalletSendRedPacketLocal.setMode(redpacketVo.getMode());
				wxWalletSendRedPacketLocal.setStatus((short) 2);
				wxWalletSendRedPacketLocal.setNum(redItem.getPacketcount());
				wxWalletSendRedPacketLocal.setAcceptnum((short) 0);
				wxWalletSendRedPacketLocal.setPaytype((short) 2);
				wxWalletSendRedPacketLocal.setStarttime(new Date());
				wxWalletSendRedPacketLocal.setUpdatetime(new Date());
				boolean save1 = wxWalletSendRedPacketLocal.save();
				if (!save1) {
					basePayResp.setOk(false);
					basePayResp.setMsg("初始化发红包订单数据失败");
					log.error("初始化发红包订单数据失败");
				} else {
					basePayResp.setOk(true);
					RedpacketCallback5UResp resp = new RedpacketCallback5UResp();
					resp.setCompleteDateTime(new Date().toString());  // 2
					resp.setDebitDateTime(new Date().toString());   // 3
					resp.setOrderStatus(redItem.getStatus());   // 1
					resp.setOrderErrorMessage("SUCCESS"); // 6
//					if (redpacketVo.getPaytype() == 3) {
//						resp.setPaymentType("WX_PAY");  // 5
//					} else if (redpacketVo.getPaytype() == 4) {
//						resp.setPaymentType("ZFB_PAY");  // 5
//					} else if (redpacketVo.getPaytype() == 1) {
					resp.setPaymentType("BALANCE");  // 5
//					}
					resp.setSerialNumber(redItem.getSerialnumber());  // 4
					map.put("serialnumber", redItem.getSerialnumber());
					map.put("reqid", redItem.getReqid());
					map.put("bizid", redItem.getBizid());
					map.put("bizcreattime", redItem.getBizcreattime());
					map.put("orderStatus", redItem.getStatus());
					map.put("packetType", packetType);
					map.put("redpacketVo", redpacketVo);
					map.put("walletId", redItem.getWalletid());
					map.put("amount", redItem.getAmount());
					map.put("token", redItem.getSerialnumber());
//					map.put("apiclassname", "redpacketcallback");
					log.error("basePay sendRedpacket debugger --> basePay sendRedpacket. map: {}", map.toString());
					WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
					basePayResp.setResp(map);
				}
			}



		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 红包查询
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp redpacketQuery(BasePayReq PayQuest, Integer uid) {
		RedpacketQueryLocalVo redpacketVo = RedpacketQueryLocalVo.toBean(PayQuest.getParams());
		String queryType = "SIMPLE";
		if (StrUtil.isNotBlank(redpacketVo.getQueryType())) {
			queryType = redpacketVo.getQueryType();
		}
		BasePayResp basePayResp = new BasePayResp();
		RedPacketQueryBuilder builder = new RedPacketQueryBuilder(getMerchantid());
		builder.setRequestId(redpacketVo.getReqid()).setQueryType(queryType);
		RedPacketExecuter executer = new RedPacketExecuter();
		try {
//			String msg = redpacketVo.toJSONString();
			RedpacketQuery5UResp resp = Json.toBean("", RedpacketQuery5UResp.class);
			if (resp.getOrderStatus().equals(PayLocalConst.RedPacketStatus.SUCCESS) || resp.getOrderStatus().equals(PayLocalConst.RedPacketStatus.TIMEOUT)) {
				log.error("查询的红包已完成：{}", Json.toJson(redpacketVo.getSend()));
			}
			basePayResp.setResp(resp.toMap());

		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 抢红包
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp grabRedpacket(BasePayReq PayQuest, Integer uid) {
		GrabRedpacketLocalVo grabVo = GrabRedpacketLocalVo.toBean(PayQuest.getParams());
		WxUserSendRedItemLocal item = WxUserSendRedItemLocal.dao.findFirst("select * from wx_user_send_red_item_local where serialnumber = ?", grabVo.getSerialnumber());
		BasePayResp basePayResp = new BasePayResp();
		String checkStr = checkRedpacketLocal(item);
		if (StrUtil.isNotBlank(checkStr)) {
			basePayResp.setOk(false);
			basePayResp.setMsg(checkStr);
			return basePayResp;
		}
		String reqId = item.getSerialnumber() + payReqIdIndex + RandomUtil.randomNumbers(12);
		try {
			GrabRedpacket5UResp resp = new GrabRedpacket5UResp();
			WxUserGrabRedItemLocal grabRedItem = new WxUserGrabRedItemLocal();
			grabRedItem.setUid(uid);   //
			grabRedItem.setSendid(item.getId());    //
			grabRedItem.setSenduid(item.getUid());
			grabRedItem.setSendwalletid(item.getWalletid());
			grabRedItem.setSendserialnumber(item.getSerialnumber());
			grabRedItem.setReqid(getReqId());
			grabRedItem.setBizid(getMerchantid());
			grabRedItem.setChatbizid(grabVo.getBizid());    //
			grabRedItem.setChatmode(grabVo.getChatmode());    //
			grabRedItem.setSerialnumber(getUUID());
			grabRedItem.setWalletid(item.getReceivewalletid());
			grabRedItem.setAmount(item.getAmount());
			grabRedItem.setBizcompletetime(new Date().toString());
			grabRedItem.setStatus("SUCCESS");    //
			grabRedItem.setIp(getIp(PayQuest));
			grabRedItem.setCoinsyn(PayConst.CoinSyn.NO);
			grabRedItem.setDevice(getDeviceType(getReqExt(PayQuest)));
			grabRedItem.setAppversion(getAppVersion(getReqExt(PayQuest)));
			grabRedItem.setCreatetime(new Date());
			boolean save = grabRedItem.save();
			if (!save) {
				basePayResp.setOk(false);
				basePayResp.setMsg("抢红包订单数据失败");
				log.error("抢红包订单数据失败");
			} else {
				//此处进入红包用户得队列中，以避免资源死锁
				WalletQueueApi.joinWalletQueue(grabRedItem.toAllMap(), item.getUid());
				basePayResp.setOk(true);
			}
			resp.setAmount(grabRedItem.getAmount().toString());
			resp.setCompleteDateTime(grabRedItem.getCreatetime().toString());
			resp.setOrderStatus("SUCCESS");
			resp.setSerialNumber(grabRedItem.getSerialnumber());
			resp.setErrorMessage("");
			resp.setReceiveWalletId(grabRedItem.getWalletid());
			resp.setRequestId(grabRedItem.getReqid());
			resp.setStatus("SUCCESS");
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	@Override
	public Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = true;
		}
		Integer rid = grabVo.getRid();
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GRAB + "." + rid, WxWalletGrabRedItemLocal.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		log.error("grabRedpacket logger ==> rid : {} ", rid);
		try {
			WxWalletSendRedPacketLocal redPacket = WxWalletSendRedPacketLocal.dao.findById(rid);
			log.error("grabRedpacket logger ==> redPacket : {} ", redPacket.toString());
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
			WxWalletRedPacketRandomLocal exist = WxWalletRedPacketRandomLocal.dao.findFirst("select * from wx_wallet_red_packet_random_local where rid = ? and  walletid = ?", rid,
					user.getWalletid());
			if (exist != null && Objects.equals(exist.getStatus(), PayConst.RedRandomStatus.SUCCESS)) {
				return RetUtils.failMsg("您已抢过该红包");
			}
			if (exist != null && Objects.equals(exist.getStatus(), PayConst.RedRandomStatus.RANDOM)) {
				return RetUtils.okData(exist).set("redpacket", redPacket);
			}
			WxWalletRedPacketRandomLocal random = getRandom(redPacket);
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
					WxWalletRedPacketRandomLocal position = new WxWalletRedPacketRandomLocal();
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

	private WxWalletRedPacketRandomLocal getRandom(WxWalletSendRedPacketLocal redpacket) {
		WxWalletRedPacketRandomLocal random = WxWalletRedPacketRandomLocal.dao.findFirst("select * from wx_wallet_red_packet_random_local where rid = ? and status = ? order by redindex",
				redpacket.getId(), PayConst.RedRandomStatus.INIT);
		if (random == null) {
			log.error("抢红包时出现空：没有红包了", Json.toJson(redpacket));
			return null;
		}
		return random;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2021年6月10日 上午10:19:15
	 */
	@Override
	public Map<String, Object> getConfParam() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put("merchantid", Const.WALLET_MERCHANTID);
		return conf;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午5:05:47
	 */
	private String getMerchantid() {
		return (String) getConfParam().get("merchantid");
	}

	/**
	 * 请求编号
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午5:20:10
	 */
	private synchronized String getReqId() {
		return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + payReqIdIndex + RandomUtil.randomNumbers(14);
	}

	/**
	 * yes或者No的状态转换
	 * @param status
	 * @return
	 * @author lixinji
	 * 2020年11月12日 下午2:29:47
	 */
	private Short StatusToYesOrNo(String status) {
		switch (status) {
		case "SUCCESS":
			return Const.YesOrNo.YES;
		default:
			return Const.YesOrNo.NO;
		}
	}

	/** 
	 * 修改开户信息：昵称和手机号
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp updateUser(BasePayReq PayQuest, Integer uid) {
		UpdateOpenVo userVo = UpdateOpenVo.toBean(PayQuest.getParams());
		User user = UserService.ME.getById(userVo.getUid());
		BasePayResp basePayResp = new BasePayResp();
		if (user == null || Objects.equals(user.getOpenflag(), Const.YesOrNo.NO)) {
			basePayResp.setOk(false);
			basePayResp.setMsg("用户信息为空或者未开户");
			return basePayResp;
		}
		if (!user.getWalletid().equals(userVo.getWalletid())) {
			basePayResp.setOk(false);
			basePayResp.setMsg("钱包id不一致");
			return basePayResp;
		}
		String reqid = getReqId();
		try {
			UpdateOpen5UResp resp = Json.toBean("", UpdateOpen5UResp.class);
			if (resp.getModifyStatus().equals(PayLocalConst.Status.SUCCESS)) {
				//TODO:lixinji-业务数据保存
				WxUserWalletLocal wallet = new WxUserWalletLocal();
				wallet.setId(user.getOpenid());
				wallet.setOperatorstatus((short) 1);
				boolean update = wallet.update();
				if (!update) {
					basePayResp.setOk(false);
					basePayResp.setMsg("修改本地数据异常");
				} else {
					basePayResp.setOk(true);
				}
			} else {

				basePayResp.setOk(false);
				basePayResp.setMsg("修改本地数据异常");
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 获取客户端token
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp clientToken(BasePayReq PayQuest, Integer uid) {
		ClientTokenVo tokenVo = ClientTokenVo.toBean(PayQuest.getParams());
		BasePayResp basePayResp = new BasePayResp();
		try {
			ClientToken5UResp resp = Json.toBean("", ClientToken5UResp.class);
			resp.setRequestId(getReqId());
			resp.setWalletId(tokenVo.getWalletid());
			if (resp.getCreateStatus().equals(PayLocalConst.Status.SUCCESS)) {
				//TODO:lixinji-此处记录日志，但只有记录作用，因为token只能用一次
				basePayResp.setOk(true);
			} else {
				basePayResp.setOk(false);
				basePayResp.setMsg(resp.getErrorMessage());
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 充值接口
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp recharge(BasePayReq PayQuest, Integer uid) {
		log.error("basePay recharge debugger --> basePay recharge method. PayQuest: {}, uid: {}", PayQuest.toString(), uid);
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5);
		BasePayResp basePayResp = new BasePayResp();

		try {
			WxUserRechargeItemLocal recharge = new WxUserRechargeItemLocal();
			recharge.setUid(uid);
			recharge.setReqid(getReqId());
			recharge.setBizid(getMerchantid());
			recharge.setSerialnumber(getUUID());
			recharge.setWalletid(PayQuest.getParams().get("walletid").toString());
			recharge.setCurrency("CNY");
			recharge.setTimeout(timeout);
			recharge.setAmount(Integer.parseInt(PayQuest.getParams().get("amount").toString()));
			recharge.setRemark(PayQuest.getParams().get("remark").toString());
			recharge.setBizcompletetime(new Date().toString());
			recharge.setStatus("INIT");
			recharge.setIp(getIp(PayQuest));
			recharge.setDevice(getDeviceType(getReqExt(PayQuest)));
			recharge.setAppversion(getAppVersion(getReqExt(PayQuest)));
			log.error("basePay recharge debugger --> basePay recharge method WxUserRechargeItemLocal info. recharge: {}", recharge.toString());
			boolean save = recharge.save();
			log.error("basePay recharge debugger --> basePay recharge method WxUserRechargeItemLocal save. save: {}", save);

			WxWalletCoinItemLocal walletCoinItem = new WxWalletCoinItemLocal();
			walletCoinItem.setUid(recharge.getUid());
			walletCoinItem.setCny(Long.parseLong(recharge.getAmount()+""));
			walletCoinItem.setStatus((short) 2);
			walletCoinItem.setMode((short)1);
			walletCoinItem.setCoinflag((short) 1);
			walletCoinItem.setBizid(Integer.valueOf(getMerchantid()));
			walletCoinItem.setReqid(getReqId());
			walletCoinItem.setMerorderid(recharge.getSerialnumber());
			walletCoinItem.setRemark("充值");
			walletCoinItem.setBizcompletetime(new Date().toString());
			walletCoinItem.setBizcreattime(new Date().toString());
			walletCoinItem.setCreatetime(new Date());
			walletCoinItem.setUpdatetime(new Date());
			boolean save1 = walletCoinItem.save();
			WxWalletRechargeItemLocal rechargeItem = new WxWalletRechargeItemLocal();
			rechargeItem.setUid(recharge.getUid());
			rechargeItem.setWalletid(recharge.getWalletid());
			rechargeItem.setMerid(getMerchantid());
			rechargeItem.setReqid(recharge.getReqid());
			rechargeItem.setMerorderid(recharge.getSerialnumber());
			rechargeItem.setAmount(Long.parseLong(PayQuest.getParams().get("amount").toString()));
			rechargeItem.setRemark("充值");
			rechargeItem.setStatus((short) -1);
			rechargeItem.setMerstatus("-1");
			rechargeItem.setCoinsyn((short)1);
			rechargeItem.setCreatetime(new Date());
			rechargeItem.setUpdatetime(new Date());
			boolean save2 = rechargeItem.save();
			if (!save || !save1 || !save2) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化提现订单数据失败");
			} else {
				basePayResp.setOk(true);
			}
			basePayResp.setResp((Map<String, Object>) recharge.toAllMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}


	/**
	 * 充值接口
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	public BasePayResp rechargeLocal(BasePayReq PayQuest, Integer uid) {
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5);
		BasePayResp basePayResp = new BasePayResp();

		try {
			Recharge5UResp resp = Json.toBean("", Recharge5UResp.class);
			if (resp.getOrderStatus().equals(PayLocalConst.RechargeStatus.INIT)) {
				WxUserRechargeItem recharge = new WxUserRechargeItem();
				recharge.setUid(uid);
				recharge.setReqid(resp.getRequestId());
				recharge.setBizid(resp.getMerchantId());
				recharge.setSerialnumber(resp.getSerialNumber());
				recharge.setWalletid(resp.getWalletId());
				recharge.setCurrency(resp.getCurrency());
				recharge.setTimeout(timeout);
				recharge.setAmount(Integer.parseInt(resp.getAmount()));
				recharge.setToken(resp.getToken());
				recharge.setRemark(resp.getRemark());
				recharge.setBizcompletetime(resp.getCreateDateTime());
				recharge.setStatus(resp.getOrderStatus());
				recharge.setIp(getIp(PayQuest));
				recharge.setDevice(getDeviceType(getReqExt(PayQuest)));
				recharge.setAppversion(getAppVersion(getReqExt(PayQuest)));
				boolean save = recharge.save();
				if (!save) {
					basePayResp.setOk(false);
					basePayResp.setMsg("初始化充值订单数据失败");
					log.error("初始化充值订单数据失败");
				} else {
					basePayResp.setOk(true);
					log.error("充值同步保存成功：{}" + recharge.getSerialnumber());
				}
			} else {
				basePayResp.setOk(true);
				basePayResp.setMsg("错误状态：" + resp.getOrderStatus());
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 充值查询
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年6月10日 上午10:19:51
	 */
	@Override
	public BasePayResp rechargeQuery(BasePayReq PayQuest, Integer uid) {
		RechargeQueryVo rechargeQuery = RechargeQueryVo.toBean(PayQuest.getParams());
		WxUserRechargeItemLocal item = WxUserRechargeItemLocal.dao.findFirst("select * from wx_user_recharge_item where serialnumber = ?", rechargeQuery.getSerialnumber());
		BasePayResp basePayResp = new BasePayResp();
		if (item == null) {
			basePayResp.setOk(false);
			log.error("充值查询接口中，发现订单不存在：{}", Json.toJson(rechargeQuery));
			basePayResp.setMsg("订单不存在");
			return basePayResp;
		}

		return rechargeQueryNoCheck(item);
	}

	/**
	 * 提现
	 */
	@Override
	public BasePayResp withhold(BasePayReq PayQuest, Integer uid) {
		log.error("basePay withhold debugger --> basePay withhold method begin. PayQuest: {}, uid: {}", PayQuest.toString(), uid);
		WxUserWithholdCountLocal count = initWithCountLocal(uid, "");
		log.error("basePay withhold debugger --> basePay withhold method. count: {}, count is null: {}", count.toString(), count == null);
		if (count == null) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("系统初始化提现次数异常为空,{}", Json.toJson(PayQuest));
			basePayResp.setMsg("系统异常");
			return basePayResp;
		}
		Integer maxCount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_COUNT, 100);
		log.error("basePay withhold debugger --> basePay withhold method. count: {}, maxCount: {}, count > maxCount: {}", count.getCount(), maxCount, count.getCount() >maxCount);
		if (count.getCount() > maxCount) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("提现次数超限：{}", Json.toJson(count));
			basePayResp.setMsg("提现次数已超上限");
			return basePayResp;
		}
		WithholdVo withholdVo = WithholdVo.toBean(PayQuest.getParams());
		log.error("basePay withhold debugger --> basePay withhold method. withholdVo: {}", withholdVo.toString());
		Integer _amount = Integer.parseInt(withholdVo.getAmount());
		Integer minAmount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MIN_AMOUT, 10000);
		log.error("basePay withhold debugger --> basePay withhold method. _amount: {}, minAmount: {}", _amount, minAmount);
		if (_amount < minAmount) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("提现金额太小：{}", Json.toJson(count));
			basePayResp.setMsg("单次提现金额不低于" + minAmount / 100 + "元");
			return basePayResp;
		}
		UserPaymentImg userPaymentInfo = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", withholdVo.getUid(), withholdVo.getType());
		WxUserBankCard userBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", withholdVo.getUid());
		BasePayResp basePayResp = new BasePayResp();


		long commission = commission(new Long(_amount));
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_WITHHOLD_TIMEOUT, (short) 5);
		try {
			Withhold5UResp resp = new Withhold5UResp();
			WxUserWithholdItemLocal withhold = new WxUserWithholdItemLocal();
			withhold.setUid(uid);
			withhold.setReqid(getReqId());
			withhold.setBizid(getMerchantid());
			withhold.setSerialnumber(getUUID());
			withhold.setWalletid(withholdVo.getWalletid());
			withhold.setCurrency(withholdVo.getCurrency());
			withhold.setAmount(Integer.parseInt(withholdVo.getAmount()));
			withhold.setArrivalamount(Integer.parseInt(commission + ""));
			withhold.setWithholdType(withholdVo.getType());
			if (withholdVo.getType().equals(4)) {
				withhold.setWithholdAccount(userBankCard.getCardno());
				withhold.setBankname(userBankCard.getBankname());
			} else {
				withhold.setWithholdAccount(userPaymentInfo.getPaymentAccount());

			}
			withhold.setRemark(withholdVo.getRemark());
			withhold.setTimeout(timeout);
			withhold.setBizcompletetime(new Date().toString());
			withhold.setStatus("INIT");
			withhold.setIp(getIp(PayQuest));
			withhold.setDevice(getDeviceType(getReqExt(PayQuest)));
			withhold.setAppversion(getAppVersion(getReqExt(PayQuest)));
			withhold.setCreatetime(new Date());
			withhold.setUpdatetime(new Date());
			boolean save = withhold.save();
			if (!save) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化提现订单数据失败");
				withhold.setStatus("FAIL");
				withhold.setRemark("添加提现记录失败");
				withhold.update();
				return basePayResp;
			}
			log.error("basePay withhold debugger --> basePay withhold method. WxUserWithholdItemLocal save: {}, withhold: {}", save, withhold);
			WxWalletCoinItemLocal walletCoinItem = new WxWalletCoinItemLocal();
			walletCoinItem.setUid(withhold.getUid());
			walletCoinItem.setCny(Long.parseLong(withhold.getAmount()+""));
			walletCoinItem.setStatus((short) 2);
			walletCoinItem.setMode((short)2);
			walletCoinItem.setCoinflag((short) 2);
			walletCoinItem.setBizid(withhold.getUid());
			walletCoinItem.setReqid(getReqId());
			walletCoinItem.setMerorderid(withhold.getSerialnumber());
			walletCoinItem.setRemark("提现");
			walletCoinItem.setBizcompletetime(new Date().toString());
			walletCoinItem.setBizcreattime(new Date().toString());
			walletCoinItem.setCreatetime(new Date());
			walletCoinItem.setUpdatetime(new Date());
			boolean save1 = walletCoinItem.save();
			if (!save1) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化提现订单数据失败");
				return basePayResp;
			}
			WxWalletWithholdItemsLocal withholdItems = new WxWalletWithholdItemsLocal();
			withholdItems.setUid(withhold.getUid());
			withholdItems.setWalletid(withhold.getWalletid());
			withholdItems.setMerid(getMerchantid());
			withholdItems.setReqid(withhold.getReqid());
			withholdItems.setMerorderid(withhold.getSerialnumber());
			withholdItems.setAmount(Long.parseLong(withholdVo.getAmount()));
			withholdItems.setArrivalamount(Long.parseLong(commission + ""));
			withholdItems.setMerfee(0);
			withholdItems.setBizfee(0);
			withholdItems.setRemark("提现");
			withholdItems.setStatus((short)-1);
			withholdItems.setMerstatus("1");
			withholdItems.setCoinsyn((short) 1);
			withholdItems.setDevice(withhold.getDevice());
			withholdItems.setAppversion(withhold.getAppversion());
			withholdItems.setCreatetime(new Date());
			withholdItems.setUpdatetime(new Date());
			boolean save2 = withholdItems.save();
			if (!save2) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化提现订单数据失败");
				return basePayResp;
			}
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where walletid = ?", withholdVo.getWalletid());
			userCoin.setCny(userCoin.getCny() - Long.parseLong(withholdVo.getAmount()));
			userCoin.setUpdatetime(new Date());
			boolean update = userCoin.update();
			if (!update) {
				withhold.setStatus("FAIL");
				withhold.setRemark("提现申请失败");
				withhold.update();
				walletCoinItem.setStatus((short) 4);
				walletCoinItem.setRemark("提现申请失败");
				walletCoinItem.update();
				withholdItems.setStatus((short)3);
				withholdItems.update();
				basePayResp.setOk(false);
				basePayResp.setMsg("余额扣除失败");
				return basePayResp;
			}
			resp.setAmount(withhold.getAmount().toString());
			resp.setArrivalAmount(withhold.getArrivalamount()!=null ? withhold.getArrivalamount().toString() : "0");
			resp.setCurrency(withhold.getCurrency());
			resp.setCreateDateTime(withhold.getCreatetime().toString());
			resp.setErrorMessage(withhold.getOrdererrormsg());
			resp.setMerchantId(withhold.getBizid());
			resp.setOrderStatus("INIT");
			resp.setRemark(withhold.getRemark());
			resp.setRequestId(withhold.getReqid());
			resp.setStatus(withhold.getStatus());
			resp.setSerialNumber(withhold.getSerialnumber());
			resp.setToken("");
			resp.setWalletId(withhold.getWalletid());
			log.error("basePay withhold debugger --> basePay withhold method. resp: {}", resp.toString());

			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(true);
			log.error("basePay withhold debugger --> basePay withhold method. basePayResp: {}", basePayResp.toString());

		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 提现查询
	 */
	@Override
	public BasePayResp withholdQuery(BasePayReq PayQuest, Integer uid) {
		WithholdQueryVo withholdQueryVo = WithholdQueryVo.toBean(PayQuest.getParams());
		WxUserWithholdItemLocal item = WxUserWithholdItemLocal.dao.findFirst("select * from wx_user_withhold_item_local where serialnumber = ?", withholdQueryVo.getSerialnumber());
		BasePayResp basePayResp = new BasePayResp();
		if (item == null) {
			basePayResp.setOk(false);
			log.error("提现查询接口中，发现订单不存在：{}", Json.toJson(withholdQueryVo));
			basePayResp.setMsg("订单不存在");
			return basePayResp;
		}
		return withholdQueryNoCheckLocal(item);
	}

	@Override
	public BasePayResp rechargeConfirm(BasePayReq payQuest, Integer uid) {
		return null;
	}

	@Override
	public BasePayResp transfer(BasePayReq PayQuest, Integer uid) {
		SendRedpacketLocalVo redpacketVo = SendRedpacketLocalVo.toBean(PayQuest.getParams());
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
		String packetType = "";
		if (Objects.equals(redpacketVo.getMode(), PayConst.RedPackMode.LUCK)) {
			packetType = PayLocalConst.RedPacketType.GROUP_LUCK;
		} else if (redpacketVo.getNum() > 1) {
			packetType = PayLocalConst.RedPacketType.GROUP_NORMAL;
		} else {
			packetType = PayLocalConst.RedPacketType.ONE_TO_ONE;
		}


		BasePayResp basePayResp = new BasePayResp();
		try {
			Map<String, Object> map = redpacketVo.toMap();
			WxUserSendRedItemLocal redItem = new WxUserSendRedItemLocal();
			redItem.setUid(uid);
			redItem.setChatbizid(redpacketVo.getBizid());
			redItem.setMode(redpacketVo.getMode());
			redItem.setChatmode(redpacketVo.getChatmode());
			redItem.setReqid(getReqId());
			redItem.setBizid(getMerchantid());
			redItem.setSerialnumber(getUUID());
			redItem.setPacketcount(redpacketVo.getNum());
			redItem.setWalletid(redpacketVo.getWalletid());
			redItem.setCurrency(redpacketVo.getCurrency());
			redItem.setAmount(Integer.parseInt(redpacketVo.getCny()));
//						redItem.setToken(resp.getToken());
			redItem.setTimeout(timeout);
			redItem.setRemark(redpacketVo.getRemark());
			redItem.setBizcreattime(new Date().toString());
			redItem.setStatus("SEND");
			redItem.setIp(getIp(PayQuest));
			redItem.setDevice(getDeviceType(getReqExt(PayQuest)));
			redItem.setAppversion(getAppVersion(getReqExt(PayQuest)));
			boolean save = redItem.save();
			log.error("basePay sendRedpacket debugger --> basePay sendRedpacket. WxUserSendRedItemLocal save: {}, redItem: {}", save, redItem.toString());
			if (!save) {
				basePayResp.setOk(false);
				basePayResp.setMsg("初始化发红包订单数据失败");
				log.error("初始化发红包订单数据失败");
			} else {
				WxWalletSendRedPacketLocal wxWalletSendRedPacketLocal = new WxWalletSendRedPacketLocal();
				wxWalletSendRedPacketLocal.setUid(redpacketVo.getUid());
				wxWalletSendRedPacketLocal.setChatmode(redpacketVo.getChatmode());
				wxWalletSendRedPacketLocal.setChatbizid(redpacketVo.getChatlinkid());
				wxWalletSendRedPacketLocal.setWalletid(redpacketVo.getWalletid());
				wxWalletSendRedPacketLocal.setMerorderid(redItem.getSerialnumber());
				wxWalletSendRedPacketLocal.setCny(Long.valueOf(redItem.getAmount()));
				wxWalletSendRedPacketLocal.setMode(redpacketVo.getMode());
				wxWalletSendRedPacketLocal.setStatus((short) 2);
				wxWalletSendRedPacketLocal.setNum(redItem.getPacketcount());
				wxWalletSendRedPacketLocal.setAcceptnum((short) 0);
				wxWalletSendRedPacketLocal.setPaytype((short) 2);
				wxWalletSendRedPacketLocal.setStarttime(new Date());
				wxWalletSendRedPacketLocal.setUpdatetime(new Date());
				boolean save1 = wxWalletSendRedPacketLocal.save();
				if (!save1) {
					basePayResp.setOk(false);
					basePayResp.setMsg("初始化发红包订单数据失败");
					log.error("初始化发红包订单数据失败");
				} else {
					basePayResp.setOk(true);
					RedpacketCallback5UResp resp = new RedpacketCallback5UResp();
					resp.setCompleteDateTime(new Date().toString());  // 2
					resp.setDebitDateTime(new Date().toString());   // 3
					resp.setOrderStatus(redItem.getStatus());   // 1
					resp.setOrderErrorMessage("SUCCESS"); // 6
//					if (redpacketVo.getPaytype() == 3) {
//						resp.setPaymentType("WX_PAY");  // 5
//					} else if (redpacketVo.getPaytype() == 4) {
//						resp.setPaymentType("ZFB_PAY");  // 5
//					} else if (redpacketVo.getPaytype() == 1) {
					resp.setPaymentType("BALANCE");  // 5
//					}
					resp.setSerialNumber(redItem.getSerialnumber());  // 4
					map.put("serialnumber", redItem.getSerialnumber());
					map.put("reqid", redItem.getReqid());
					map.put("bizid", redItem.getBizid());
					map.put("bizcreattime", redItem.getBizcreattime());
					map.put("orderStatus", redItem.getStatus());
					map.put("packetType", packetType);
					map.put("redpacketVo", redpacketVo);
					map.put("walletId", redItem.getWalletid());
					map.put("amount", redItem.getAmount());
					map.put("token", redItem.getSerialnumber());
					map.put("apiclassname", "redpacketcallback");
					log.error("basePay sendRedpacket debugger --> basePay sendRedpacket. map: {}", map.toString());
					WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
					basePayResp.setResp(map);
				}
			}



		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * @param redItem
	 * @return
	 * @author lixinji
	 * 2020年11月23日 下午6:03:34
	 */
	private String checkRedpacket(WxUserSendRedItem redItem) {
		if (redItem == null) {
			return "红包不存在";
		}
		if (Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.SUCCESS)) {
			return "红包已抢完";
		}
		if (Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.TIMEOUT)) {
			return "红包已超时";
		}
		if (!Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.SEND)) {
			return "红包异常";
		}
		return "";
	}

	private String checkRedpacketLocal(WxUserSendRedItemLocal redItem) {
		if (redItem == null) {
			return "红包不存在";
		}
		if (Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.SUCCESS)) {
			return "红包已抢完";
		}
		if (Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.TIMEOUT)) {
			return "红包已超时";
		}
		if (!Objects.equals(redItem.getStatus(), PayLocalConst.RedPacketStatus.SEND)) {
			return "红包异常";
		}
		return "";
	}

	/**
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年11月22日 下午9:19:59
	 */
	private RequestExt getReqExt(BasePayReq PayQuest) {
		HttpRequest request = PayQuest.getRequest();
		if (request == null) {
			return null;
		}
		return (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
	}

	/**
	 * 设备
	 * @param ext
	 * @return
	 * @author lixinji
	 * 2020年11月22日 下午9:19:56
	 */
	private Short getDeviceType(RequestExt ext) {
		if (ext == null) {
			return Devicetype.SYS_TASK.getValue();
		}
		return ext.getDeviceType();
	}

	/**
	 * 客户端ip
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年11月22日 下午9:22:42
	 */
	private String getIp(BasePayReq PayQuest) {
		HttpRequest request = PayQuest.getRequest();
		if (request == null) {
			return "0.0.0.0";
		}
		return request.getClientIp();
	}

	/**
	 * app版本号
	 * @param ext
	 * @return
	 * @author lixinji
	 * 2020年11月22日 下午9:23:47
	 */
	private String getAppVersion(RequestExt ext) {
		if (ext == null) {
			return "0.0.0";
		}
		return ext.getAppVersion();
	}

	/**
	 * @param item
	 * @return
	 * @author lixinji
	 * 2020年11月26日 下午2:54:36
	 */
	public BasePayResp rechargeQueryNoCheck(WxUserRechargeItem item) {
		BasePayResp basePayResp = new BasePayResp();
		try {
			Map<String, Object> map = item.toAllMap();
			map.put("bankicon", "localRecharge");
			map.put("bankcode", "localRecharge");
			map.put("bankname", "localRecharge");
			map.put("bankcardnumber", "localRecharge");
			map.put("bizcreattime",new Date());
			map.put("ordererrormsg", "");
			map.put("status", "");
			map.put("bizcompletetime",new Date());
			WalletQueueApi.joinWalletQueue(item.toAllMap(), item.getUid());
			basePayResp.setOk(true);
			Map<String, Object> retmap = map;
			retmap.put("bankicon", "localRecharge");
			basePayResp.setResp(retmap);
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	public BasePayResp rechargeQueryNoCheck(WxUserRechargeItemLocal item) {
		BasePayResp basePayResp = new BasePayResp();
		try {
			Map<String, Object> map = item.toAllMap();
			map.put("bankicon", "本地充值");
			map.put("bankcode", "本地充值");
			map.put("bankname", "本地充值");
			map.put("bankcardnumber", "本地充值");
			map.put("bizcreattime",new Date());
			map.put("ordererrormsg", item.getOrdererrormsg());
			map.put("status", item.getStatus());
			map.put("bizcompletetime",new Date());
			WalletQueueApi.joinWalletQueue(map, item.getUid());
			basePayResp.setOk(true);
			Map<String, Object> retmap = map;
			retmap.put("bankicon", "localRecharge");
			basePayResp.setResp(retmap);
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 充值再次补偿回调
	 * @param item
	 * @return
	 * @author lixinji
	 * 2020年12月3日 上午11:07:56
	 */
	public BasePayResp rechargeAgainCallback(WxUserRechargeItem item) {
		BasePayResp basePayResp = new BasePayResp();
		RechargeQueryBuilder builder = new RechargeQueryBuilder(getMerchantid());
		builder.setRequestId(item.getReqid());
		RechargeExecuter executer = new RechargeExecuter();
		try {
			executer.bothRechargeQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					RechargeQuery5UResp resp = Json.toBean(msg, RechargeQuery5UResp.class);
					basePayResp.setOk(true);
					Map<String, Object> map = resp.toAllMap();
					map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.RECHARGE_CALLBACK);
					map.put("again", "again");
					WalletQueueApi.joinWalletQueue(map, item.getUid());
					basePayResp.setResp(map);
				}

				@Override
				public void failure(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					log.error(msg);
					basePayResp.setOk(false);
					basePayResp.setMsg("响应失败");
				}

				@Override
				public void pending(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					log.error(msg);
					basePayResp.setOk(false);
					basePayResp.setMsg("待处理");
				}
			});
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 提现查询
	 * @param item
	 * @return
	 * @author lixinji
	 * 2020年11月26日 下午3:53:30
	 */
	public BasePayResp withholdQueryNoCheck(WxUserWithholdItem item) {
		BasePayResp basePayResp = new BasePayResp();
		WithholdingQueryBuilder builder = new WithholdingQueryBuilder(getMerchantid());
		builder.setRequestId(item.getReqid());
		WithholdingExecuter executer = new WithholdingExecuter();
		try {
			executer.bothWithholdingQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					WithholdQuery5UResp resp = Json.toBean(msg, WithholdQuery5UResp.class);
					basePayResp.setOk(true);
					Map<String, Object> map = item.toAllMap();
					map.put("bankicon", BankConfService.getString(resp.getBankCode()));
					map.put("bankcode", resp.getBankCode());
					map.put("bankname", resp.getBankName());
					map.put("bankcardnumber", resp.getBankCardNumber());
					map.put("bizcreattime", resp.getCreateDateTime());
					map.put("ordererrormsg", resp.getOrderErrorMessage());
					map.put("status", resp.getOrderStatus());
					map.put("bizcompletetime", resp.getCompleteDateTime());
					WalletQueueApi.joinWalletQueue(map, item.getUid());
					Map<String, Object> retmap = resp.toMap();
					retmap.put("bankicon", BankConfService.getString(resp.getBankCode()));
					basePayResp.setResp(retmap);
				}

				@Override
				public void failure(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					log.error(msg);
					basePayResp.setOk(false);
					basePayResp.setMsg("响应失败");
				}

				@Override
				public void pending(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					log.error(msg);
					basePayResp.setOk(false);
					basePayResp.setMsg("待处理");
				}
			});
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	/**
	 * 本地提现查询
	 * @param item
	 * @return
	 * @author lixinji
	 * 2020年11月26日 下午3:53:30
	 */
	public BasePayResp withholdQueryNoCheckLocal(WxUserWithholdItemLocal item) {
		BasePayResp basePayResp = new BasePayResp();
		WithholdingQueryBuilder builder = new WithholdingQueryBuilder(getMerchantid());
		builder.setRequestId(item.getReqid());
		WithholdingExecuter executer = new WithholdingExecuter();
		try {
//			WithholdQuery5UResp resp = Json.toBean("", WithholdQuery5UResp.class);
			basePayResp.setOk(true);
			Map<String, Object> map = item.toAllMap();
			map.put("bankicon", "本地钱包");
			map.put("bankcode", "本地钱包");
			map.put("bankname", "本地钱包");
			map.put("bankcardnumber", "本地钱包");
			map.put("bizcreattime",item.getBizcreattime());
			map.put("ordererrormsg", item.getOrdererrormsg());
			map.put("status", item.getStatus());
			map.put("bizcompletetime", item.getBizcompletetime());
			WalletQueueApi.joinWalletQueue(map, item.getUid());
//			Map<String, Object> retmap = resp.toMap();
//			retmap.put("bankicon", "本地钱包");
			basePayResp.setResp(map);

		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (RequestException e) {
			log.error("请求异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		}
		return basePayResp;
	}

	@Override
	public BasePayResp bindBankCardConfirm(BasePayReq payQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp rechargeConfirm(RechargeConfirmVo rechargeVo, Integer uid, String cny) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerchantid());
		result.put("merorderid", getUUID());
		result.put("amount", cny);
		result.put("checkdate", new Date());
		result.put("ordererrormsg", "");

		BasePayResp resp = new BasePayResp();
		resp.setOk(true);
		resp.setResp(rechargeConfirm(result, uid));
		return resp;
	}

	@Override
	public BasePayResp transfer(BasePayReq payQuest, Integer uid, String cny) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerchantid());
		result.put("reqid", getReqId());
		result.put("merorderid", getUUID());
		result.put("payacctamount", cny);
		result.put("recvacctamount", 0);
		result.put("ordererrormsg", "");
		BasePayResp resp = new BasePayResp();
		resp.setResp(result);
		resp.setOk(true);
		return resp;
	}

	public Map<String, Object> transfer(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerchantid());
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
		log.error("响应格式化后的信息：{}", Json.toJson(result));

		return result;
	}

	@Override
	public Ret payRedpacket(HttpRequest request, BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		Short timeout_ = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);

		Integer offset = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_TIMEOUT, 1440);
		WxWalletSendRedPacketLocal last = null;
		if (Objects.equals(redpacketVo.getPaytype(), PayConst.RedPayType.CNY)) {
			WxWalletSendRedPacketLocal transfer = WxWalletSendRedPacketLocal.mapToBean(resp.getResp());
			transfer.setId(redpacketVo.getRid());
			transfer.setStatus(PayConst.RedPacketStatus.PROCESS);
			transfer.setPaytype(PayConst.RedPayType.CNY);
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + redpacketVo.getUid(), WxWalletCoinLocal.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				last = WxWalletSendRedPacketLocal.dao.findFirst("select * from wx_wallet_send_red_packet_local where  id = ?", redpacketVo.getRid());
				if (last == null) {
					return RetUtils.failMsg("操作已超时，请重试");
				}
				if (Objects.equals(last.getStatus(), PayConst.RedPacketStatus.PROCESS)) {
					log.error("发红包数据发生变更:last:{}", Json.toJson(last));
					return RetUtils.okData(last);
				}
				final WxWalletSendRedPacketLocal finalLast = last;
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

				WxWalletSendRedPacketLocal sendRedPacket = WxWalletSendRedPacketLocal.dao.findById(redpacketVo.getRid());

				WxUserSendRedItemLocal redItem = new WxUserSendRedItemLocal();
				redItem.setUid(sendRedPacket.getUid());
				redItem.setChatbizid(sendRedPacket.getChatbizid());
				redItem.setMode(sendRedPacket.getMode());
				redItem.setChatmode(sendRedPacket.getChatmode());
				redItem.setReqid(getReqId());
				redItem.setBizid(getMerchantid());
				redItem.setSerialnumber(getUUID());
				redItem.setPacketcount(sendRedPacket.getNum());
				redItem.setWalletid(sendRedPacket.getWalletid());
				redItem.setCurrency("CNY");
				redItem.setAmount(Integer.parseInt(sendRedPacket.getCny().toString()));
//						redItem.setToken(resp.getToken());
				redItem.setTimeout(timeout_);
				redItem.setRemark(sendRedPacket.getRemark());
				redItem.setBizcreattime(new Date().toString());
				redItem.setStatus("SEND");
				redItem.setIp(getIp(req));
				redItem.setDevice(getDeviceType(getReqExt(req)));
				redItem.setAppversion(getAppVersion(getReqExt(req)));
				redItem.save();
				//同步钱包信息
				BasePayResp walletResp = getWalletInfo(walletVo, walletVo.getUid());
				Map data = (Map<String, Object>) walletResp.getResp();
				data.put("sendCny", redItem.getAmount());
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
						Ret synRet = synWallet(data, walletVo, false, 1);
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
				last = WxWalletSendRedPacketLocal.dao.findFirst("select * from wx_wallet_send_red_packet_local where  id = ?", last.getId());
				if (Objects.equals(last.getSendmode(), PayConst.RedSendMode.RESP)) {
					WalletQueueApi.sendRedpacketCallbackLocal(last);
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} else {
			last = WxWalletSendRedPacketLocal.dao.findFirst("select * from wx_wallet_send_red_packet_local where  id = ?", redpacketVo.getRid());
		}



		return RetUtils.okData(last);
	}
	private BasePayResp getWalletInfo(WalletVo walletVo, Integer uid) {
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", uid);
//		WxWalletSendRedPacketLocal.dao.findById()
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("walletid", walletVo.getWalletid());
		result.put("merid", getMerchantid());
		result.put("uid", uid);
		result.put("merstatus", "");
		result.put("auditstatus", "1");
		result.put("authstatus", "1");
		result.put("cny", userCoin.getCny() + "");
		//		result.put("mercny", AmountUtil.doubleAmountToInt(MapUtil.getStr(resp, "availableBalance")));
		result.put("ordererrormsg", "");
		BasePayResp resp = new BasePayResp();
		resp.setResp(result);
		resp.setOk(true);
		return resp;
	}
	public Ret coinAdd(Long cny, Integer uid, Short mode, Short coinflag, String remark, Integer bizid, Short status, Map<String, Object> resultMap, String starttime, String endtime,
					   Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletCoinItemLocal coinItem = new WxWalletCoinItemLocal();
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
	public Ret synWallet(Map<String, Object> resultMap, WalletVo walletVo, Boolean isAtom, int mode) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletCoinLocal coin = WxWalletCoinLocal.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin_local where uid = ? and walletid = ?", walletVo.getUid(), walletVo.getWalletid());
		if (coin == null) {
			return RetUtils.failMsg("钱包不存在");
		}
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where walletid = ?", walletVo.getWalletid());
		if (userCoin == null) {
			return RetUtils.failMsg("请先认证钱包信息");
		}
		WxWalletCoinLocal syn = new WxWalletCoinLocal();
		syn.setId(coin.getId());
		syn.setCny(MapUtil.getLong(resultMap, "cny"));
		boolean update = syn.update();
		if (!update) {
			return RetUtils.failMsg("同步钱包错误");
		}
		if (mode == 1) {
			userCoin.setCny(userCoin.getCny() - MapUtil.getLong(resultMap, "sendCny"));
			userCoin.setSendpacket(userCoin.getSendpacket() + 1);
			userCoin.setUpdatetime(new Date());
			boolean update1 = userCoin.update();
			if(!update1) {
				return RetUtils.failMsg("同步钱包错误");
			}
		}

		if (!isAtom) {
			return RetUtils.okData(coin);
		}

		WxWalletCoinLocal last = WxWalletCoinLocal.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin_local where id = ?", coin.getId());
		return RetUtils.okData(last);
	}

	public Ret synWallet(Map<String, Object> resultMap, WalletVo walletVo, Boolean isAtom) {
		if (isAtom == null) {
			isAtom = false;
		}
		WxWalletCoinLocal coin = WxWalletCoinLocal.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin_local where uid = ? and walletid = ?", walletVo.getUid(), walletVo.getWalletid());
		if (coin == null) {
			return RetUtils.failMsg("钱包不存在");
		}
		WxWalletCoinLocal syn = new WxWalletCoinLocal();
		syn.setId(coin.getId());
		syn.setCny(MapUtil.getLong(resultMap, "cny"));
		boolean update = syn.update();
		if (!update) {
			return RetUtils.failMsg("同步钱包错误");
		}
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where walletid = ?", walletVo.getWalletid());
		userCoin.setCny(userCoin.getCny() - MapUtil.getLong(resultMap, "cny"));
		userCoin.setSendpacket(userCoin.getSendpacket() + 1);
		userCoin.setUpdatetime(new Date());
		boolean update1 = userCoin.update();
		if(!update1) {
			return RetUtils.failMsg("同步钱包错误");
		}
		if (!isAtom) {
			return RetUtils.okData(coin);
		}

		WxWalletCoinLocal last = WxWalletCoinLocal.dao.findFirst("select id,cny,unclearcny from wx_wallet_coin_local where id = ?", coin.getId());
		return RetUtils.okData(last);
	}
	@Override
	public BasePayResp transferQuery(BasePayReq payQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 初始化提现次数
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji
	 * 2020年11月30日 下午5:35:17
	 */
	public static WxUserWithholdCount initWithCount(Integer uid, String period) {
		if (StrUtil.isBlank(period)) {
			period = PeriodUtils.dateToPeriodByType(new DateTime(), Const.PeriodType.DAY);
		}
		log.error("withhold initWithCount debugger --> uid : {}, period: {}", uid, period);
		WxUserWithholdCount count = WxUserWithholdCount.dao.findFirst("select * from wx_user_withhold_count where uid = ? and period = ?", uid, period);
		log.error("withhold initWithCount debugger --> count : {}", count);
		if (count == null) {
			log.error("withhold initWithCount debugger --> count is null");
			count = new WxUserWithholdCount();
			count.setUid(uid);
			count.setPeriod(period);
			count.setCount((short) 0);
			int i = count.ignoreSave();
			log.error("withhold initWithCount debugger --> count is null --> i : {}", i);
			if (i <= 0) {
				log.error("withhold initWithCount debugger --> count is null --> i < 0 count : {}", count);
				count = WxUserWithholdCount.dao.findFirst("select * from wx_user_withhold_count where uid = ? and period = ?", uid, period);
				log.error("withhold initWithCount debugger --> count is null --> i < 0 count : {}", count);
				if (count == null) {
					log.error("withhold initWithCount debugger --> count always is null");
					return null;
				}
			}
		}
		return count;
	}

	/**
	 * 本地钱包初始化提现次数
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji
	 * 2020年11月30日 下午5:35:17
	 */
	public static WxUserWithholdCountLocal initWithCountLocal(Integer uid, String period) {
		if (StrUtil.isBlank(period)) {
			period = PeriodUtils.dateToPeriodByType(new DateTime(), Const.PeriodType.DAY);
		}
		log.error("initWithCountLocal ==> uid : {}", uid);
		WxUserWithholdCountLocal count = WxUserWithholdCountLocal.dao.findFirst("select * from wx_user_withhold_count_local where uid = ? and period = ?", uid, period);
		if (count == null) {
			count = new WxUserWithholdCountLocal();
			count.setUid(uid);
			count.setPeriod(period);
			count.setCount((short) 0);
			int i = count.ignoreSave();
			if (i <= 0) {
				count = WxUserWithholdCountLocal.dao.findFirst("select * from wx_user_withhold_count_local where uid = ? and period = ?", uid, period);
				if (count == null) {
					return null;
				}
			}
		}
		return count;
	}

	/**
	 * 修改提现次数
	 * @param id
	 * @return
	 * @author lixinji
	 * 2020年11月30日 下午5:42:02
	 */
	public static boolean updateWithholdCount(Integer id) {
		Integer maxCount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_COUNT, 100);
		if (Const.PAY_TYPE.equals("3")) {
			maxCount = Integer.MAX_VALUE;
		}
		log.error("withhold updateWithholdCount debugger --> id : {}, maxCount: {}", id, maxCount);
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_WITHHOLD + "." + id, WxUserWithholdCount.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxUserWithholdCount count = WxUserWithholdCount.dao.findById(id);
			log.error("withhold updateWithholdCount debugger --> count : {}", count.toString());
			if (count.getCount() > maxCount) {
				return false;
			}
			Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_user_withhold_count set count = count + 1 where id = ?", id);
			WxUserWithholdCount newCount = WxUserWithholdCount.dao.findById(id);
			log.error("withhold updateWithholdCount debugger --> newCount : {}", newCount.toString());
			if (newCount.getCount() - (maxCount+1) > 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("提现次数修改失败");
		} finally {
			writeLock.unlock();
		}
		return false;
	}


	public static String getUUID(){

		UUID uuid=UUID.randomUUID();

		String str = uuid.toString();

		String uuidStr=str.replace("-", "");

		return uuidStr;

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
				WxWalletSendRedPacketLocal redPacket = new WxWalletSendRedPacketLocal();
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
		WxWalletRedPacketRandomLocal random = new WxWalletRedPacketRandomLocal();
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

//	public static String encrypt(String strSrc) {
//		try {
//			char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
//					'9', 'a', 'b', 'c', 'd', 'e', 'f' };
//			byte[] bytes = strSrc.getBytes();
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			md.update(bytes);
//			bytes = md.digest();
//			int j = bytes.length;
//			char[] chars = new char[j * 2];
//			int k = 0;
//			for (int i = 0; i < bytes.length; i++) {
//				byte b = bytes[i];
//				chars[k++] = hexChars[b >>> 4 & 0xf];
//				chars[k++] = hexChars[b & 0xf];
//			}
//			return new String(chars);
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//			throw new RuntimeException("MD5加密出错！！+" + e);
//		}
//	}

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
	public WxWalletSendRedPacketLocal getRedPacketLockLocal(Integer rid, Boolean lock) {
		if (lock == null) {
			lock = false;
		}
		if (lock) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GET_LOCK + "." + rid, WxWalletSendRedPacketLocal.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacketLocal sendRed = WxWalletSendRedPacketLocal.dao.findById(rid);
				return sendRed;
			} catch (Exception e) {
				log.error("", e);
			} finally {
				writeLock.unlock();
			}
		} else {
			WxWalletSendRedPacketLocal sendRed = WxWalletSendRedPacketLocal.dao.findById(rid);
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
	public Ret updateRedPacketLock(WxWalletSendRedPacketLocal redPacket, Short status, boolean lock) {
		if (lock) {
			ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_REDPACKET_GET_LOCK + "." + redPacket.getId(), WxWalletSendRedPacketLocal.class);
			WriteLock writeLock = rwLock.writeLock();
			writeLock.lock();
			try {
				WxWalletSendRedPacketLocal sendRed = WxWalletSendRedPacketLocal.dao.findById(redPacket.getId());
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

	public Map<String, Object> rechargeConfirm(Map<String, Object> resp, Integer uid) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("uid", uid);
		result.put("merid", getMerchantid());
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
		log.error("响应格式化后的信息：{}", Json.toJson(result));
		return result;
	}

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

	/**
	 * 本地钱包抢红包回调
	 * @param request
	 * @param resp
	 * @param grabVo
	 * @param isAtom
	 * @return
	 */
	@Override
	public Ret grabRedpacketCallback(HttpRequest request, BasePayResp resp, GrabRedpacketVo grabVo, Boolean isAtom) {
		if (!resp.isBizOk()) {
			return RetUtils.failMsg(resp.getMerMsg());
		}
		WxWalletRedPacketRandomLocal random = grabVo.getRandomLocal();
		WxWalletSendRedPacketLocal redPacket = grabVo.getRedPacketLocal();
		if (isAtom == null) {
			isAtom = true;
		}
		DateTime time = new DateTime();
		WxWalletGrabRedItemLocal redItem = WxWalletGrabRedItemLocal.mapToBean(resp.getResp());
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
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_COIN + "." + grabVo.getUid(), WxWalletCoinLocal.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxWalletRedPacketRandomLocal lastRandom = WxWalletRedPacketRandomLocal.dao.findById(random.getId());
			if (lastRandom != null && !Objects.equals(lastRandom.getStatus(), PayConst.RedPacketStatus.SUCCESS)) {
				WalletVo walletVo = new WalletVo();
				walletVo.setUid(redItem.getUid());
				walletVo.setWalletid(redItem.getWalletid());
				BasePayReq req = new BasePayReq(request);
				req.setParams(walletVo.toMap());
				BasePayResp walletResp = getWalletInfo(walletVo, walletVo.getUid());
				Map data = (Map<String, Object>) walletResp.getResp();
				data.put("acceptCny", redItem.getCny());

				WalletVo subWalletVo = new WalletVo();
				subWalletVo.setUid(redItem.getUid());
				subWalletVo.setWalletid(redItem.getWalletid());
				BasePayReq subReq = new BasePayReq(request);
				subReq.setParams(subWalletVo.toMap());
				BasePayResp subWalletResp = getWalletInfo(subWalletVo, subWalletVo.getUid());
				Map data2 = (Map<String, Object>) subWalletResp.getResp();
				data2.put("acceptCny", redItem.getCny());

				AbsTxAtom absTxAtom = new AbsTxAtom() {

					@Override
					public boolean noTxRun() {
						boolean save = redItem.save();
						;
						if (!save) {
							log.error("红包转账保存信息失败：{}", Json.toJson(redItem));
							return failRet("抢红包信息保存失败");
						}
						WxWalletRedPacketRandomLocal position = new WxWalletRedPacketRandomLocal();
						position.setId(random.getId());
						position.setStatus(PayConst.RedRandomStatus.SUCCESS);
						position.setAccounttime(time);
						boolean update = position.update();
						if (!update) {
							return failRet("随机红包确认失败");
						}
						Db.use(Const.Db.TIO_SITE_MAIN).update(
								"UPDATE wx_wallet_send_red_packet_local set acceptnum = (select count(1) from wx_wallet_red_packet_random_local where rid = ? and `status` = ? ),`status` = IF(acceptnum = num,?,`status`),endtime = IF(`status` = ?,now(),NULL) where id = ?",
								redPacket.getId(), PayConst.RedRandomStatus.SUCCESS, PayConst.RedPacketStatus.SUCCESS, PayConst.RedPacketStatus.SUCCESS, redPacket.getId());
						Ret synRet = synWallet(data, walletVo, false, 2);
						if (synRet.isFail()) {
							return failRet(synRet);
						}
						Ret synSubRet = synWallet(data2, subWalletVo, false, 2);
						if (synSubRet.isFail()) {
							return failRet(synSubRet);
						}
						String redTimeStr = DateUtil.format(redItem.getGrabtime(), "yyyy-MM-dd HH:mm:ss");
						Ret coinRet = coinAdd(redItem.getCny(), redItem.getUid(), PayConst.WalletMode.REDPACKET, Const.CoinFlag.INCOME, "抢红包", redItem.getId(),
								PayConst.WalletChangeStatus.SUCCESS, resp.getResp(), redTimeStr, redTimeStr, false);
						if (coinRet.isFail()) {
							return failRet(coinRet);
						}
						WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where walletid = ?", walletVo.getWalletid());
						userCoin.setCny(userCoin.getCny() + MapUtil.getLong(data, "acceptCny"));
						userCoin.setAcceptredpacket(userCoin.getAcceptredpacket() + 1);
						userCoin.setUpdatetime(new Date());
						boolean update1 = userCoin.update();
						if(!update1) {
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
			WxWalletSendRedPacketLocal last = WxWalletSendRedPacketLocal.dao.findById(redPacket.getId());
			try {
				WalletQueueApi.grabRedpacketLocal(redItem);
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
}
