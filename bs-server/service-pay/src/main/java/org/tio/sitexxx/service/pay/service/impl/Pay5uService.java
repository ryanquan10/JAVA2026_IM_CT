
package org.tio.sitexxx.service.pay.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.tio.sitexxx.service.model.main.WxUserGrabRedItem;
import org.tio.sitexxx.service.model.main.WxUserRechargeItem;
import org.tio.sitexxx.service.model.main.WxUserSendRedItem;
import org.tio.sitexxx.service.model.main.WxUserWithholdItem;
import org.tio.sitexxx.service.pay.base.BaseCallbackPay;
import org.tio.sitexxx.service.pay.base.BasePay;
import org.tio.sitexxx.service.pay.base.BasePayReq;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5UConst;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5uApi;
import org.tio.sitexxx.service.pay.impl.pay5u.Pay5uCallBackApi;
import org.tio.sitexxx.service.pay.impl.pay5u.resp.RedpacketQuery5UResp;
import org.tio.sitexxx.service.service.conf.ConfService;
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
import org.tio.sitexxx.service.vo.UnBindCardVo;
import org.tio.sitexxx.service.vo.UpdateOpenVo;
import org.tio.sitexxx.service.vo.WalletVo;
import org.tio.sitexxx.service.vo.WithholdQueryVo;
import org.tio.sitexxx.service.vo.WithholdVo;
import org.tio.utils.json.Json;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 支付服务
 * 
 * @author lixinji 2020年09月25日 下午5:57:32
 */
public class Pay5uService implements BasePayService {

	private static Logger log = LoggerFactory.getLogger(Pay5uService.class);

	public static BasePay<BasePayReq, BasePayResp> basePay;

	public static BaseCallbackPay<BasePayResp> baseCallbackPay;

	public static void initPay() {
		basePay = new Pay5uApi();
		baseCallbackPay = new Pay5uCallBackApi();
	}

	public Pay5uService() {
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
		throw new RuntimeException("易支付暂不支持本地实名信息");
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
		throw new RuntimeException("易支付暂不支持查询本地银行卡列表信息");
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
		throw new RuntimeException("易支付暂不支持本地绑定银行卡");
	}

	/**
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午9:59:15
	 */
	@Override
	public Ret unBindCard(UnBindCardVo cardVo, HttpRequest request) {
		throw new RuntimeException("易支付暂不支持本地解绑绑定银行卡");
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
		throw new RuntimeException("易支付暂不支持本地解绑绑定银行卡");
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
		throw new RuntimeException("暂不支持转账功能");
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
		if (redpacketVo.getUid() == null || StrUtil.isBlank(redpacketVo.getWalletid())) {
			log.error("发送红包参数为空");
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
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:30
	 */
	@Override
	public Ret initRedpacket(SendRedpacketVo redpacketVo) {
		throw new RuntimeException("易支付暂不支持本地红包");
	}

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:31
	 */
	@Override
	public Ret quickRedpacket(SendRedpacketVo redpacketVo, HttpRequest request) {
		throw new RuntimeException("易支付暂不支持本地红包");
	}

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:32
	 */
	@Override
	public Ret payRedpacket(SendRedpacketVo redpacketVo, HttpRequest request, User user) {
		throw new RuntimeException("易支付暂不支持本地红包");
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
		        "'" + Pay5UConst.RedPacketStatus.SUCCESS + "','" + Pay5UConst.RedPacketStatus.SEND + "','" + Pay5UConst.RedPacketStatus.TIMEOUT + "'");
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket5u.sendlist", params);
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
		        "'" + Pay5UConst.RedPacketStatus.SUCCESS + "','" + Pay5UConst.RedPacketStatus.SEND + "','" + Pay5UConst.RedPacketStatus.TIMEOUT + "'");
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket5u.sendstat", params);
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
		BasePayReq req = new BasePayReq(request);
		req.setParams(grabRedpacketVo.toMap());
		BasePayResp resp = basePay.grabRedpacket(req, grabRedpacketVo.getUid());
		if (!resp.isOk()) {
			return RetUtils.failMsg(resp.getMsg());
		}
		return RetUtils.okData(resp.getResp());
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
		Kv params = Kv.by("uid", uid).set("status", Pay5UConst.Status.SUCCESS);
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket5u.grablist", params);
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
		Kv params = Kv.by("uid", uid).set("status", Pay5UConst.Status.SUCCESS);
		DateTime dateTime = new DateTime();
		if (StrUtil.isNotBlank(period)) {
			dateTime = PeriodUtils.getDateByPeriod(period);
		}
		String starttime = DateUtil.format(DateUtil.beginOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		String endtime = DateUtil.format(DateUtil.endOfYear(dateTime), DatePattern.NORM_DATETIME_PATTERN);
		params.set("starttime", starttime).set("endtime", endtime);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("redpacket5u.grabstat", params);
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
		WxUserSendRedItem redItem = WxUserSendRedItem.dao.findFirst("select * from wx_user_send_red_item where serialnumber = ?", serialNumber);
		if (redItem == null || Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.CANCEL) || Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		statusRet.put("redstatus", redItem.getStatus());
		WxUserGrabRedItem grabRedItem = WxUserGrabRedItem.dao.findFirst("select * from wx_user_grab_red_item where uid = ? and sendid = ?", user.getId(), redItem.getId());
		if (grabRedItem == null) {
			statusRet.put("grabstatus", Pay5UConst.Status.INIT);
		} else {
			statusRet.put("grabamount", grabRedItem.getAmount());
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
		WxUserSendRedItem redItem = WxUserSendRedItem.dao.findFirst(
		        "select red.chatmode,red.uid,red.id,red.reqid,red.amount,red.serialnumber,red.remark,red.packetcount,red.bizcompletetime,red.bizcreattime,red.receivedamount,red.receivedcount,u.nick,u.avatar,red.`status`,red.mode from wx_user_send_red_item red inner join `user` u on u.id = red.uid where red.serialnumber = ?",
		        serialNumber);
		if (redItem == null || Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.CANCEL) || Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.FAIL)) {
			return RetUtils.failMsg("红包不存在");
		}
		if (Objects.equals(redItem.getStatus(), Pay5UConst.RedPacketStatus.SEND)) {
			BasePayReq req = new BasePayReq(request);
			RedpacketQueryVo queryVo = new RedpacketQueryVo();
			queryVo.setSend(redItem);
			queryVo.setReqid(redItem.getReqid());
			queryVo.setUid(user.getId());
			queryVo.setWalletid(user.getWalletid());
			queryVo.setSerialnumber(serialNumber);
			req.setParams(queryVo.toMap());
			BasePayResp resp = basePay.redpacketQuery(req, queryVo.getUid());
			RedpacketQuery5UResp queryRet = RedpacketQuery5UResp.toBean(resp.getResp());
			if (queryRet != null) {
				redItem.setReceivedamount(strToInteger(queryRet.getReceivedAmount()));
				redItem.setReceivedcount(strToShort(queryRet.getReceivedCount()));
				redItem.setRefundamount(strToInteger(queryRet.getRefundAmount()));
				redItem.setReceivewalletid(queryRet.getReceiveWalletId());
			}
		}
		redItem.setReqid("");
		Map<String, Object> infoRet = new HashMap<String, Object>();
		infoRet.put("info", redItem);
		List<WxUserGrabRedItem> grabRedItem = WxUserGrabRedItem.dao.find(
		        "select grab.uid,grab.serialnumber,grab.id,grab.amount,grab.bizcompletetime,grab.walletid,u.nick,u.avatar from wx_user_grab_red_item grab inner join `user` u on u.id = grab.uid  where grab.sendid = ? order by grab.id desc",
		        redItem.getId());
		if (CollectionUtil.isNotEmpty(grabRedItem)) {
			infoRet.put("grablist", grabRedItem);
		} else {
			infoRet.put("grablist", new ArrayList<WxUserGrabRedItem>());
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
		throw new RuntimeException("易支付暂不支持支付列表");
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
		if (rechargeVo == null || StrUtil.isBlank(rechargeVo.getWalletid()) || rechargeVo.getUid() == null) {
			log.error("充值参数异常");
			return RetUtils.failMsg("充值参数异常");
		}
		BasePayReq req = new BasePayReq(request);
		req.setParams(rechargeVo.toMap());
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
		throw new RuntimeException("易支付暂不支持充值确认接口");
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
		throw new RuntimeException("易支付暂不支持红包支付查询接口");
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("recharge5u.list", params);
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
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("wallet5u.items", params);
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
		BasePayReq req = new BasePayReq(request);
		req.setParams(withholdVo.toMap());
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
		Kv params = Kv.by("uid", uid).set("status", Pay5UConst.Status.SUCCESS);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("withhold5u.list", params);
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
		WxUserSendRedItem redPacket = WxUserSendRedItem.dao.findFirst("SELECT id from wx_user_send_red_item where uid = ? and `status` = ?", user.getId(),
		        Pay5UConst.RedPacketStatus.SEND);
		if (redPacket != null) {
			return false;
		}
		WxUserWithholdItem initDeals = WxUserWithholdItem.dao.findFirst("select  * from wx_user_withhold_item where querysyn = ? and uid = ?", PayConst.QuerySyn.NO, user.getId());
		if (initDeals != null) {
			return false;
		}
		WxUserWithholdItem items = WxUserWithholdItem.dao.findFirst("select  * from wx_user_withhold_item where uid = ? and (coinsyn != ? or querysyn = ?)", user.getId(),
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
