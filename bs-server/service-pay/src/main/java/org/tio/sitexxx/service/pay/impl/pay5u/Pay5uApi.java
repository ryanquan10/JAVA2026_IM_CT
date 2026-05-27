
package org.tio.sitexxx.service.pay.impl.pay5u;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.ClientToken5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Excep5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.GrabRedpacket5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Open5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Recharge5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.RechargeQuery5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Redpacket5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.RedpacketQuery5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.UpdateOpen5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Wallet5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.Withhold5UResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.WithholdQuery5UResp;
import org.tio.sitexxx.service.pay.service.WalletQueueApi;
import org.tio.sitexxx.service.service.atom.AbsAtom;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.BankConfService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.*;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;

import com.alibaba.fastjson15.JSONObject;
import com.upay.sdk.exception.HmacVerifyException;
import com.upay.sdk.exception.RequestException;
import com.upay.sdk.exception.ResponseException;
import com.upay.sdk.exception.UnknownException;
import com.upay.sdk.executer.ResultListenerAdpater;
import com.upay.sdk.webox.builder.ClientTokenBuilder;
import com.upay.sdk.webox.builder.RechargeBuilder;
import com.upay.sdk.webox.builder.RechargeQueryBuilder;
import com.upay.sdk.webox.builder.RedPacketBuilder;
import com.upay.sdk.webox.builder.RedPacketGrabBuilder;
import com.upay.sdk.webox.builder.RedPacketQueryBuilder;
import com.upay.sdk.webox.builder.WalletCreateBuilder;
import com.upay.sdk.webox.builder.WalletQueryBuilder;
import com.upay.sdk.webox.builder.WalletUpdateWalletInfoBuilder;
import com.upay.sdk.webox.builder.WithholdingBuilder;
import com.upay.sdk.webox.builder.WithholdingQueryBuilder;
import com.upay.sdk.webox.executer.ClientTokenExecuter;
import com.upay.sdk.webox.executer.RechargeExecuter;
import com.upay.sdk.webox.executer.RedPacketExecuter;
import com.upay.sdk.webox.executer.WalletExecuter;
import com.upay.sdk.webox.executer.WithholdingExecuter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 易支付api
 * @author lixinji
 * 2020年11月10日 上午10:18:12
 */
public class Pay5uApi implements BasePay<BasePayReq, BasePayResp> {

	private static Logger log = LoggerFactory.getLogger(Pay5uApi.class);

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

	/* 
	 * 易支付开户逻辑，未添加查询同步逻辑
	 */
	@Override
	public BasePayResp openUser(BasePayReq PayQuest, Integer uid) {
		OpenUserVo userVo = OpenUserVo.toBean(PayQuest.getParams());
		WalletCreateBuilder builder = new WalletCreateBuilder(getMerchantid());
		builder.setRequestId(getReqId()).setMerchantId(getMerchantid()).setMerchantUserId(userVo.getUid() + "").setName(userVo.getName()).setIdCardType(userVo.getCardtype())
		        .setIdCardNo(userVo.getCardno()).setMobile(userVo.getMobile()).setProfession(userVo.getProfession()).setIp(userVo.getIp()).setMac(userVo.getMac())
		        .setNickName(userVo.getName());
		BasePayResp basePayResp = new BasePayResp();
		WalletExecuter executer = new WalletExecuter();
		try {
			executer.bothCreate(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					Open5UResp resp = Json.toBean(msg, Open5UResp.class);
					if (resp.getWalletStatus().equals(Pay5UConst.WalletStatus.ACTIVATE)) {
						//TODO:lixinji-业务数据保存
						AbsAtom atom = new AbsTxAtom() {

							@Override
							public boolean noTxRun() {
								WxUserWallet wallet = new WxUserWallet();
								wallet.setUid(userVo.getUid());
								wallet.setReqid(resp.getRequestId());
								wallet.setBizid(resp.getMerchantId());
								wallet.setWalletid(resp.getWalletId());
								wallet.setOperatorstatus(StatusToYesOrNo(resp.getOperatorRzStatus()));
								wallet.setRealnamestatus(StatusToYesOrNo(resp.getIdCardRzStatus()));
								wallet.setIp(getIp(PayQuest));
								wallet.setDevice(getDeviceType(getReqExt(PayQuest)));
								wallet.setAppversion(getAppVersion(getReqExt(PayQuest)));
								boolean save = wallet.save();
								if (!save) {
									log.error("钱包开户异常：{}", Json.toJson(resp));
									return failRet("保存钱包逻辑失败");
								}
								User user = new User();
								user.setOpenflag(Const.YesOrNo.YES);
								user.setOpenid(wallet.getId());
								user.setId(userVo.getUid());
								boolean update = user.update();
								if (!update) {
									return failRet("修改用户钱包逻辑异常");
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
					} else {
						String walletid = resp.getWalletId();
						if (StrUtil.isNotBlank(walletid)) {
							ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_OPEN + "." + userVo.getUid(), WxUserWallet.class);
							WriteLock writeLock = rwLock.writeLock();
							writeLock.lock();
							try {
								User user = UserService.ME.getById(userVo.getUid());
								if (user != null && Objects.equals(user.getOpenflag(), Const.YesOrNo.NO)) {
									AbsAtom atom = new AbsTxAtom() {

										@Override
										public boolean noTxRun() {
											WxUserWallet wallet = new WxUserWallet();
											wallet.setUid(userVo.getUid());
											wallet.setReqid(resp.getRequestId());
											wallet.setBizid(resp.getMerchantId());
											wallet.setWalletid(resp.getWalletId());
											wallet.setExcepsyn(Const.YesOrNo.NO);
											boolean save = wallet.save();
											if (!save) {
												log.error("钱包补偿开户异常：{}", Json.toJson(resp));
												return failRet("保存钱包逻辑失败");
											}
											User user = new User();
											user.setOpenflag(Const.YesOrNo.YES);
											user.setOpenid(wallet.getId());
											user.setId(userVo.getUid());
											boolean update = user.update();
											if (!update) {
												return failRet("修改用户钱包逻辑异常");
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
										basePayResp.setOk(false);
										basePayResp.setMsg(resp.getErrorMessage() + ",但已补偿开户");
									}
								} else {
									basePayResp.setOk(false);
									basePayResp.setMsg(resp.getErrorMessage());
								}
							} catch (Exception e) {
								log.error("", e);
							} finally {
								writeLock.unlock();
							}
						} else {
							basePayResp.setOk(false);
							basePayResp.setMsg(resp.getErrorMessage());
						}
					}
					basePayResp.setResp(resp.toMap());
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
		WalletQueryBuilder builder = new WalletQueryBuilder(merchantId);
		builder.setMerchantId(merchantId).setWalletId(walletId);
		WalletExecuter executer = new WalletExecuter();
		try {
			executer.bothQUery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					Wallet5UResp resp = Json.toBean(msg, Wallet5UResp.class);
					if (resp.getWalletStatus().equals(Pay5UConst.WalletStatus.ACTIVATE)) {
						basePayResp.setOk(true);
						WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
					} else {
						basePayResp.setOk(false);
						basePayResp.setMsg("钱包异常：状态-" + resp.getWalletStatus());
					}
					basePayResp.setResp(resp.toMap());
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
		SendRedpacketVo redpacketVo = SendRedpacketVo.toBean(PayQuest.getParams());
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_REDPACKET_PAY_TIMEOUT, (short) 5);
		String notifyUrl = Pay5UConst.CallBackUrl.REDPACKET + redpacketVo.getUid();
		if (StrUtil.isNotBlank(redpacketVo.getNotifyUrl())) {
			notifyUrl = redpacketVo.getNotifyUrl();
		}
		String packetType = "";
		if (Objects.equals(redpacketVo.getMode(), PayConst.RedPackMode.LUCK)) {
			packetType = Pay5UConst.RedPacketType.GROUP_LUCK;
		} else if (redpacketVo.getNum() > 1) {
			packetType = Pay5UConst.RedPacketType.GROUP_NORMAL;
		} else {
			packetType = Pay5UConst.RedPacketType.ONE_TO_ONE;
		}
		BasePayResp basePayResp = new BasePayResp();
		RedPacketBuilder builder = new RedPacketBuilder(getMerchantid());
		builder.setRequestId(getReqId()).setWalletId(redpacketVo.getWalletid()).setAmount(redpacketVo.getCny()).setSingleAmount(redpacketVo.getSinglecny())
		        .setCurrency(redpacketVo.getCurrency()).setPacketType(packetType).setPacketCount(redpacketVo.getNum() + "").setNotifyUrl(notifyUrl)
		        .setRemark(redpacketVo.getRemark()).setTimeout(timeout + "");
		RedPacketExecuter executer = new RedPacketExecuter();
		try {
			executer.bothCreate(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					Redpacket5UResp resp = Json.toBean(msg, Redpacket5UResp.class);
					if (resp.getOrderStatus().equals(Pay5UConst.RechargeStatus.INIT)) {
						WxUserSendRedItem redItem = new WxUserSendRedItem();
						redItem.setUid(uid);
						redItem.setChatbizid(redpacketVo.getBizid());
						redItem.setMode(redpacketVo.getMode());
						redItem.setChatmode(redpacketVo.getChatmode());
						redItem.setReqid(resp.getRequestId());
						redItem.setBizid(resp.getMerchantId());
						redItem.setSerialnumber(resp.getSerialNumber());
						redItem.setPacketcount(redpacketVo.getNum());
						redItem.setWalletid(resp.getWalletId());
						redItem.setCurrency(resp.getCurrency());
						redItem.setAmount(Integer.parseInt(resp.getAmount()));
						redItem.setToken(resp.getToken());
						redItem.setTimeout(timeout);
						redItem.setRemark(resp.getRemark());
						redItem.setBizcreattime(resp.getCreateDateTime());
						redItem.setStatus(resp.getOrderStatus());
						redItem.setIp(getIp(PayQuest));
						redItem.setDevice(getDeviceType(getReqExt(PayQuest)));
						redItem.setAppversion(getAppVersion(getReqExt(PayQuest)));
						boolean save = redItem.save();
						if (!save) {
							basePayResp.setOk(false);
							basePayResp.setMsg("初始化发红包订单数据失败");
							log.error("初始化发红包订单数据失败");
						} else {
							basePayResp.setOk(true);
						}
					} else {
						basePayResp.setOk(false);
						basePayResp.setMsg("错误状态：" + resp.getOrderStatus());
						log.error("初始化发红包订单数据失败:{}", resp.getOrderStatus());
					}
					Map<String, Object> map = resp.toMap();
					map.put("packetCount", redpacketVo.getNum());
					map.put("packetType", redpacketVo.getMode());
					map.put("singleAmount", redpacketVo.getSinglecny());
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
	 * 红包查询
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp redpacketQuery(BasePayReq PayQuest, Integer uid) {
		RedpacketQueryVo redpacketVo = RedpacketQueryVo.toBean(PayQuest.getParams());
		String queryType = "SIMPLE";
		if (StrUtil.isNotBlank(redpacketVo.getQueryType())) {
			queryType = redpacketVo.getQueryType();
		}
		BasePayResp basePayResp = new BasePayResp();
		RedPacketQueryBuilder builder = new RedPacketQueryBuilder(getMerchantid());
		builder.setRequestId(redpacketVo.getReqid()).setQueryType(queryType);
		RedPacketExecuter executer = new RedPacketExecuter();
		try {
			executer.bothQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					RedpacketQuery5UResp resp = Json.toBean(msg, RedpacketQuery5UResp.class);
					if (resp.getOrderStatus().equals(Pay5UConst.RedPacketStatus.SUCCESS) || resp.getOrderStatus().equals(Pay5UConst.RedPacketStatus.TIMEOUT)) {
						log.error("查询的红包已完成：{}", Json.toJson(redpacketVo.getSend()));
					}
					basePayResp.setResp(resp.toMap());
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
	 * 抢红包
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp grabRedpacket(BasePayReq PayQuest, Integer uid) {
		GrabRedpacketVo grabVo = GrabRedpacketVo.toBean(PayQuest.getParams());
		WxUserSendRedItem item = WxUserSendRedItem.dao.findFirst("select * from wx_user_send_red_item where serialnumber = ?", grabVo.getSerialnumber());
		BasePayResp basePayResp = new BasePayResp();
		String checkStr = checkRedpacket(item);
		if (StrUtil.isNotBlank(checkStr)) {
			basePayResp.setOk(false);
			basePayResp.setMsg(checkStr);
			return basePayResp;
		}
		String reqId = item.getSerialnumber() + payReqIdIndex + RandomUtil.randomNumbers(12);
		RedPacketGrabBuilder builder = new RedPacketGrabBuilder(getMerchantid());
		builder.setRequestId(reqId).setSerialNumber(grabVo.getSerialnumber()).setWalletId(grabVo.getWalletid());
		RedPacketExecuter executer = new RedPacketExecuter();
		try {
			executer.bothGrab(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					GrabRedpacket5UResp resp = Json.toBean(msg, GrabRedpacket5UResp.class);
					if (resp.getOrderStatus().equals(Pay5UConst.RechargeStatus.SUCCESS)) {
						WxUserGrabRedItem grabRedItem = new WxUserGrabRedItem();
						grabRedItem.setUid(uid);
						grabRedItem.setSendid(item.getId());
						grabRedItem.setSenduid(item.getUid());
						grabRedItem.setSendwalletid(item.getWalletid());
						grabRedItem.setSendserialnumber(item.getSerialnumber());
						grabRedItem.setReqid(resp.getRequestId());
						grabRedItem.setBizid(resp.getMerchantId());
						grabRedItem.setChatbizid(grabVo.getBizid());
						grabRedItem.setChatmode(grabVo.getChatmode());
						grabRedItem.setSerialnumber(resp.getSerialNumber());
						grabRedItem.setWalletid(resp.getReceiveWalletId());
						grabRedItem.setAmount(Integer.parseInt(resp.getAmount()));
						grabRedItem.setBizcompletetime(resp.getCompleteDateTime());
						grabRedItem.setStatus(resp.getOrderStatus());
						grabRedItem.setIp(getIp(PayQuest));
						grabRedItem.setCoinsyn(PayConst.CoinSyn.NO);
						grabRedItem.setDevice(getDeviceType(getReqExt(PayQuest)));
						grabRedItem.setAppversion(getAppVersion(getReqExt(PayQuest)));
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
					} else {
						basePayResp.setOk(false);
						basePayResp.setMsg("错误状态：" + resp.getOrderStatus());
						log.error("初始化发红包订单数据失败:{}", resp.getOrderStatus());
					}
					basePayResp.setResp(resp.toMap());
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

	@Override
	public Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom) {
		return null;
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
		WalletUpdateWalletInfoBuilder builder = new WalletUpdateWalletInfoBuilder(getMerchantid());
		builder.setRequestId(reqid).setMerchantId(getMerchantid()).setWalletId(userVo.getWalletid()).setMobile(userVo.getMobile()).setNickName(userVo.getNickName());
		WalletExecuter executer = new WalletExecuter();
		try {
			executer.bothUpdateWalletInfo(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					UpdateOpen5UResp resp = Json.toBean(msg, UpdateOpen5UResp.class);
					if (resp.getModifyStatus().equals(Pay5UConst.Status.SUCCESS)) {
						//TODO:lixinji-业务数据保存
						WxUserWallet wallet = new WxUserWallet();
						wallet.setId(user.getOpenid());
						wallet.setOperatorstatus(StatusToYesOrNo(resp.getOperatorRzStatus()));
						boolean update = wallet.update();
						if (!update) {
							basePayResp.setOk(false);
							basePayResp.setMsg("修改本地数据异常");
						} else {
							basePayResp.setOk(true);
						}
					} else {

						basePayResp.setOk(false);
						basePayResp.setMsg(resp.getErrorMessage());
					}
					basePayResp.setResp(resp.toMap());
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
		ClientTokenBuilder builder = new ClientTokenBuilder(getMerchantid());
		builder.setRequestId(getReqId()).setWalletId(tokenVo.getWalletid()).setBusinessType(tokenVo.getBizType());
		ClientTokenExecuter executer = new ClientTokenExecuter();
		try {
			executer.bothCreate(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					ClientToken5UResp resp = Json.toBean(msg, ClientToken5UResp.class);
					if (resp.getCreateStatus().equals(Pay5UConst.Status.SUCCESS)) {
						//TODO:lixinji-此处记录日志，但只有记录作用，因为token只能用一次
						basePayResp.setOk(true);
					} else {
						basePayResp.setOk(false);
						basePayResp.setMsg(resp.getErrorMessage());
					}
					basePayResp.setResp(resp.toMap());
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
	 * 充值接口
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp recharge(BasePayReq PayQuest, Integer uid) {
		RechargeVo rechargeVo = RechargeVo.toBean(PayQuest.getParams());
		String notifyUrl = Pay5UConst.CallBackUrl.RECHARGE + rechargeVo.getUid();
		if (StrUtil.isNotBlank(rechargeVo.getNotifyUrl())) {
			notifyUrl = rechargeVo.getNotifyUrl();
		}
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_RECHARGE_TIMEOUT, (short) 5);
		BasePayResp basePayResp = new BasePayResp();
		RechargeBuilder builder = new RechargeBuilder(getMerchantid());
		builder.setRequestId(getReqId()).setWalletId(rechargeVo.getWalletid()).setAmount(rechargeVo.getAmount()).setRemark(rechargeVo.getRemark()).setTimeout(timeout + "")
		        .setCurrency(rechargeVo.getCurrency()).setNotifyUrl(notifyUrl);
		RechargeExecuter executer = new RechargeExecuter();
		try {
			executer.bothRecharge(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					Recharge5UResp resp = Json.toBean(msg, Recharge5UResp.class);
					if (resp.getOrderStatus().equals(Pay5UConst.RechargeStatus.INIT)) {
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

	@Override
	public BasePayResp rechargeConfirm(RechargeConfirmVo rechargeVo, Integer uid, String cny) {
		return null;
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
		WxUserRechargeItem item = WxUserRechargeItem.dao.findFirst("select * from wx_user_recharge_item where serialnumber = ?", rechargeQuery.getSerialnumber());
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
		WxUserWithholdCount count = initWithCount(uid, "");
		if (count == null) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("系统初始化提现次数异常为空,{}", Json.toJson(PayQuest));
			basePayResp.setMsg("系统异常");
			return basePayResp;
		}
		Integer maxCount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_COUNT, 100);
		if (count.getCount() > maxCount) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("提现次数超限：{}", Json.toJson(count));
			basePayResp.setMsg("提现次数已超上限");
			return basePayResp;
		}
		WithholdVo withholdVo = WithholdVo.toBean(PayQuest.getParams());
		String notifyUrl = Pay5UConst.CallBackUrl.WITHHOLD + withholdVo.getUid();
		if (StrUtil.isNotBlank(withholdVo.getNotifyUrl())) {
			notifyUrl = withholdVo.getNotifyUrl();
		}
		Integer _amount = Integer.parseInt(withholdVo.getAmount());
		Integer minAmount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MIN_AMOUT, 10000);
		if (_amount < minAmount) {
			BasePayResp basePayResp = new BasePayResp();
			basePayResp.setOk(false);
			log.error("提现金额太小：{}", Json.toJson(count));
			basePayResp.setMsg("单次提现金额不低于" + minAmount / 100 + "元");
			return basePayResp;
		}
		BasePayResp basePayResp = new BasePayResp();

		WithholdingBuilder builder = new WithholdingBuilder(getMerchantid());
		long commission = commission(new Long(_amount));
		Short timeout = ConfService.getShort(Const.ConfMapping.WX_WALLET_WITHHOLD_TIMEOUT, (short) 5);
		builder.setRequestId(getReqId()).setWalletId(withholdVo.getWalletid()).setAmount(withholdVo.getAmount()).setTimeout(timeout + "").setArrivalAmount(commission + "")
		        .setCurrency(withholdVo.getCurrency()).setRemark(withholdVo.getRemark()).setNotifyUrl(notifyUrl);
		WithholdingExecuter executer = new WithholdingExecuter();
		try {
			executer.bothWithholding(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					Withhold5UResp resp = Json.toBean(msg, Withhold5UResp.class);
					if (resp.getOrderStatus().equals(Pay5UConst.WithholdStatus.INIT)) {
						WxUserWithholdItem withhold = new WxUserWithholdItem();
						withhold.setUid(uid);
						withhold.setReqid(resp.getRequestId());
						withhold.setBizid(resp.getMerchantId());
						withhold.setSerialnumber(resp.getSerialNumber());
						withhold.setWalletid(resp.getWalletId());
						withhold.setCurrency(resp.getCurrency());
						withhold.setAmount(Integer.parseInt(resp.getAmount()));
						withhold.setArrivalamount(Integer.parseInt(resp.getArrivalAmount()));
						withhold.setToken(resp.getToken());
						withhold.setRemark(resp.getRemark());
						withhold.setTimeout(timeout);
						withhold.setBizcompletetime(resp.getCreateDateTime());
						withhold.setStatus(resp.getOrderStatus());
						withhold.setIp(getIp(PayQuest));
						withhold.setDevice(getDeviceType(getReqExt(PayQuest)));
						withhold.setAppversion(getAppVersion(getReqExt(PayQuest)));
						boolean save = withhold.save();
						if (!save) {
							basePayResp.setOk(false);
							basePayResp.setMsg("初始化提现订单数据失败");
						} else {
							basePayResp.setOk(true);
						}
					} else {
						basePayResp.setOk(true);
						basePayResp.setMsg("错误状态：" + resp.getOrderStatus());
					}
					basePayResp.setResp(resp.toMap());
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
	 */
	@Override
	public BasePayResp withholdQuery(BasePayReq PayQuest, Integer uid) {
		WithholdQueryVo withholdQueryVo = WithholdQueryVo.toBean(PayQuest.getParams());
		WxUserWithholdItem item = WxUserWithholdItem.dao.findFirst("select * from wx_user_withhold_item where serialnumber = ?", withholdQueryVo.getSerialnumber());
		BasePayResp basePayResp = new BasePayResp();
		if (item == null) {
			basePayResp.setOk(false);
			log.error("提现查询接口中，发现订单不存在：{}", Json.toJson(withholdQueryVo));
			basePayResp.setMsg("订单不存在");
			return basePayResp;
		}
		return withholdQueryNoCheck(item);
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
		if (Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.SUCCESS)) {
			return "红包已抢完";
		}
		if (Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.TIMEOUT)) {
			return "红包已超时";
		}
		if (!Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.SEND)) {
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
		RechargeQueryBuilder builder = new RechargeQueryBuilder(getMerchantid());
		builder.setRequestId(item.getReqid());
		RechargeExecuter executer = new RechargeExecuter();
		try {
			executer.bothRechargeQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					RechargeQuery5UResp resp = Json.toBean(msg, RechargeQuery5UResp.class);
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
					basePayResp.setOk(true);
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

	@Override
	public BasePayResp bindBankCardConfirm(BasePayReq payQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp rechargeConfirm(BasePayReq payQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePayResp transfer(BasePayReq payQuest, Integer uid) {
		// TODO Auto-generated method stub
		return null;
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
		WxUserWithholdCount count = WxUserWithholdCount.dao.findFirst("select * from wx_user_withhold_count where uid = ? and period = ?", uid, period);
		if (count == null) {
			count = new WxUserWithholdCount();
			count.setUid(uid);
			count.setPeriod(period);
			count.setCount((short) 0);
			int i = count.ignoreSave();
			if (i <= 0) {
				count = WxUserWithholdCount.dao.findFirst("select * from wx_user_withhold_count where uid = ? and period = ?", uid, period);
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
		ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_WALLET_WITHHOLD + "." + id, WxUserWithholdCount.class);
		WriteLock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			WxUserWithholdCount count = WxUserWithholdCount.dao.findById(id);
			if (count.getCount() > maxCount) {
				return false;
			}
			Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_user_withhold_count set count = count + 1 where id = ?", id);
			WxUserWithholdCount newCount = WxUserWithholdCount.dao.findById(id);
			if (newCount.getCount() > (maxCount + 1)) {
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

}
