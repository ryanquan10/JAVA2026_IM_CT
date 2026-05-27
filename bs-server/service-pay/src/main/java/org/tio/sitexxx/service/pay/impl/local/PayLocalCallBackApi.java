
package org.tio.sitexxx.service.pay.impl.local;

import com.alibaba.fastjson15.JSONObject;
import com.upay.sdk.exception.HmacVerifyException;
import com.upay.sdk.exception.RequestException;
import com.upay.sdk.exception.ResponseException;
import com.upay.sdk.exception.UnknownException;
import com.upay.sdk.executer.ResultListenerAdpater;
import com.upay.sdk.onlinepay.executer.OnlinePayOrderExecuter;
import com.upay.sdk.webox.builder.RechargeQueryBuilder;
import com.upay.sdk.webox.builder.RedPacketQueryBuilder;
import com.upay.sdk.webox.builder.WithholdingQueryBuilder;
import com.upay.sdk.webox.executer.RechargeExecuter;
import com.upay.sdk.webox.executer.RedPacketExecuter;
import com.upay.sdk.webox.executer.WithholdingExecuter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.pay.base.BaseCallbackPay;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.*;
import org.tio.sitexxx.service.pay.service.WalletQueueApi;
import org.tio.sitexxx.service.service.conf.BankConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.PayConst;
import org.tio.sitexxx.service.vo.SendRedpacketLocalVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;
import org.tio.utils.json.Json;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

/**
 * 回调
 * @author lixinji
 */
public class PayLocalCallBackApi implements BaseCallbackPay<BasePayResp> {

	private static Logger log = LoggerFactory.getLogger(PayLocalCallBackApi.class);

	private static final String	ENCRYPT_KEY	= "encryptKey";
	private static final String	MERCHANT_ID	= "merchantId";

	/**
	 * 充值回调 
	 * 此方法只有记录不存在时，才进行fail输出
	 * 订单错误也是正常数据
	 */
	@Override
	public BasePayResp recharge(HttpRequest request, Integer uid) {
		BasePayResp basePayResp = new BasePayResp();
		try {
			JSONObject json = JSONObject.parseObject(new String(request.getBody(), "utf-8"));
			String msg = json.toJSONString();
			RechargeCallback5UResp resp = Json.toBean(msg, RechargeCallback5UResp.class);
			WxUserRechargeItemLocal item = WxUserRechargeItemLocal.dao.findFirst("select * from wx_user_recharge_item_local where serialnumber = ?", resp.getSerialNumber());
			if (item == null) {
				basePayResp.setOk(false);
				log.error("支付回调接口中，发现订单不存在：{}", Json.toJson(resp));
				basePayResp.setMsg("订单不存在");
			} else {
				WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
				basePayResp.setOk(true);
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		} catch (UnsupportedEncodingException e) {
			log.error("解析流失败,{}", e.getMessage());
		}
		return basePayResp;
	}



	public BasePayResp rechargeLocal(Ret ret, Integer uid) {
		BasePayResp basePayResp = new BasePayResp();
		try {
					WxUserRechargeItem item = WxUserRechargeItem.dao.findFirst("select * from wx_user_recharge_item where serialnumber = ?", ret.get("serialnumber"));
					if (item == null) {
						basePayResp.setOk(false);
						log.error("支付回调接口中，发现订单不存在：{}", Json.toJson(ret));
						basePayResp.setMsg("订单不存在");
					} else {
						WalletQueueApi.joinWalletQueue(ret, uid);
						basePayResp.setOk(true);
					}
					basePayResp.setResp(ret);
		} catch (Exception e) {
			log.error("响应异常,{}", e.getMessage());
		}
		return basePayResp;
	}

	/**
	 * 提现回调
	 */
	@Override
	public BasePayResp withhold(HttpRequest request, Integer uid) {
		BasePayResp basePayResp = new BasePayResp();
		try {
			log.error("withhold debugger --> uid : {}", uid);
			WxUserWithholdCount count = PayLocalApi.initWithCount(uid, "");
			log.error("withhold debugger --> count : {}", count);
			if (count == null) {
				basePayResp.setOk(false);
				log.error("withhold debugger --> count is null");
				log.error("系统初始化提现次数异常为空");
				basePayResp.setMsg("系统异常");
				return basePayResp;
			}
			boolean withholdupdate = PayLocalApi.updateWithholdCount(count.getId());
			if (!withholdupdate) {
				basePayResp.setOk(false);
				log.error("系统系统次数更新异常,{}");
				basePayResp.setMsg("系统次数更新异常");
				return basePayResp;
			}
			JSONObject json = JSONObject.parseObject(new String(request.getBody(), "utf-8"));
			String msg = json.toJSONString();
			WithholdCallback5UResp resp = Json.toBean(msg, WithholdCallback5UResp.class);
			WxUserWithholdItem item = WxUserWithholdItem.dao.findFirst("select * from wx_user_withhold_item_local where serialnumber = ?", resp.getSerialNumber());
			if (item == null) {
				basePayResp.setOk(false);
				log.error("提现回调接口中，发现订单不存在：{}", Json.toJson(resp));
				basePayResp.setMsg("订单不存在");
			} else {
				WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
				basePayResp.setOk(true);
			}
			basePayResp.setResp(resp.toMap());
		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return basePayResp;
	}

	/**
	 * 发红包回调
	 */
	@Override
	public BasePayResp sendRedpacket(HttpRequest request, Integer uid) {
		BasePayResp basePayResp = new BasePayResp();
		try {
			log.error("发红包回调 login begin...");
			JSONObject json = JSONObject.parseObject(new String(request.getBody(), "utf-8"));
			RedpacketCallback5UResp resp = new RedpacketCallback5UResp();
			JSONObject redpacketVo = (JSONObject)json.get("redpacketVo");
//			resp.setAmount(redpacketVo.get("cny").toString());
//			resp.setCurrency(redpacketVo.get("currency").toString());
			resp.setCompleteDateTime(new Date().toString());  // 2
//			resp.setCreateDateTime(new Date().toString());
			resp.setDebitDateTime(new Date().toString());   // 3
//			resp.setMerchantId(redpacketVo.get("merorderid").toString());
			resp.setOrderStatus(json.get("orderStatus").toString());   // 1
			resp.setOrderErrorMessage(json.get("ordererrormsg").toString()); // 6
//			resp.setPacketCount(((JSONObject)redpacketVo.get("sendRed")).get("num").toString());
//			resp.setPacketType(json.get("packetType").toString());
			resp.setPaymentType(json.get("paymentType").toString());  // 5
//			resp.setReceivedAmount();
//			resp.setReceivedCount(resp.getReceivedCount());
//			resp.setRequestId(json.get("reqid").toString());
//			resp.setRemark(redpacketVo.get("remark").toString());
//			resp.setReceiveWalletId();
//			resp.setRefundAmount(redpacketVo.getCny());
//			resp.setRefundCount(redpacketVo.getSendRed().getNum().toString());
//			resp.setRefundType("本地钱包");
			resp.setSerialNumber(json.get("serialnumber").toString());  // 4
//			resp.setWalletId(redpacketVo.get("walletid").toString());
			WxUserSendRedItemLocal item = WxUserSendRedItemLocal.dao.findFirst("select * from wx_user_send_red_item_local where serialnumber = ?", json.get("serialnumber"));
			if (item == null) {
				basePayResp.setOk(false);
				log.error("发红包回调接口中，发现订单不存在：{}", Json.toJson(resp));
				basePayResp.setMsg("订单不存在");
			} else {
				WalletQueueApi.joinWalletQueue(resp.toAllMap(), uid);
				basePayResp.setOk(true);
			}
			basePayResp.setResp(resp.toMap());
			log.error("发红包回调 login end...");

		} catch (ResponseException e) {
			log.error("响应异常,{}", e.getMessage());
		} catch (HmacVerifyException e) {
			log.error("签名验证异常,{}", e.getMessage());
		} catch (UnknownException e) {
			log.error("其它异常,{}", e.getMessage());
			String err = e.getMessage();
			Excep5UResp resp = Json.toBean(err, Excep5UResp.class);
			basePayResp.setResp(resp.toMap());
			basePayResp.setOk(false);
			basePayResp.setMsg(resp.getErrorMessage());
			return basePayResp;
		} catch (UnsupportedEncodingException e) {
			log.error("解析流失败,{}", e.getMessage());
		}
		return basePayResp;
	}

	/**
	 * 充值补偿回调
	 * @param object
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp rechargeAgainCallback(Object object) {
		WxUserRechargeItem item = (WxUserRechargeItem) object;
		BasePayResp basePayResp = new BasePayResp();
		RechargeQueryBuilder builder = new RechargeQueryBuilder(Const.WALLET_MERCHANTID);
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
	 * @param object
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp rechargeQueryNoCheck(Object object) {
		WxUserRechargeItem item = (WxUserRechargeItem) object;
		BasePayResp basePayResp = new BasePayResp();
		RechargeQueryBuilder builder = new RechargeQueryBuilder(Const.WALLET_MERCHANTID);
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
	 * 发红包补偿回调
	 * @param object
	 * @return
	 * @author lixinji
	 */
	@Override
	public BasePayResp redpacketAgainCallback(Object object) {
		WxUserSendRedItem redItem = (WxUserSendRedItem) object;
		String queryType = "SIMPLE";
		BasePayResp basePayResp = new BasePayResp();
		RedPacketQueryBuilder builder = new RedPacketQueryBuilder(Const.WALLET_MERCHANTID);
		builder.setRequestId(redItem.getReqid()).setQueryType(queryType);
		;
		RedPacketExecuter executer = new RedPacketExecuter();
		try {
			executer.bothQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					RedpacketQuery5UResp resp = Json.toBean(msg, RedpacketQuery5UResp.class);
					Map<String, Object> map = resp.toAllMap();
					map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.REDPACKET_CALLBACK);
					map.put("again", "again");
					WalletQueueApi.joinWalletQueue(map, redItem.getUid());
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
	 * @param object
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:39:50
	 */
	@Override
	public BasePayResp withholdQueryNoCheck(Object object) {
		WxUserWithholdItem item = (WxUserWithholdItem) object;
		BasePayResp basePayResp = new BasePayResp();
		WithholdingQueryBuilder builder = new WithholdingQueryBuilder(Const.WALLET_MERCHANTID);
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
	 * 提现补偿回调
	 * @param object
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午2:40:48
	 */
	@Override
	public BasePayResp withholdAgainCallback(Object object) {
		WxUserWithholdItem item = (WxUserWithholdItem) object;
		BasePayResp basePayResp = new BasePayResp();
		WithholdingQueryBuilder builder = new WithholdingQueryBuilder(Const.WALLET_MERCHANTID);
		builder.setRequestId(item.getReqid());
		WithholdingExecuter executer = new WithholdingExecuter();
		try {
			executer.bothWithholdingQuery(builder, new ResultListenerAdpater() {
				@Override
				public void success(JSONObject jsonObject) {
					String msg = jsonObject.toJSONString();
					WithholdQuery5UResp resp = Json.toBean(msg, WithholdQuery5UResp.class);
					basePayResp.setOk(true);
					Map<String, Object> map = resp.toAllMap();
					map.put(PayConst.ApiClassName.API_MAP_KEY, PayConst.ApiClassName.WITHHOLD_CALLBACK);
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
}
