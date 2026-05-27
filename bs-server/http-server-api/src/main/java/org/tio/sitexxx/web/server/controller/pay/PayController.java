
package org.tio.sitexxx.web.server.controller.pay;

import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.init.PayInit;
import org.tio.sitexxx.service.pay.service.impl.LocalWalletService;
import org.tio.sitexxx.service.pay.service.impl.Pay5uService;
import org.tio.sitexxx.service.pay.service.impl.PayStdService;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.CloudflareR2Utils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.UploadUtils;
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
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 支付
 *
 */
@RequestPath(value = "/pay")
public class PayController {

	private static Logger log = LoggerFactory.getLogger(PayController.class);

	/**
	 *
	 *
	 */
	public PayController() {
	}

	private static BasePayService payService = PayInit.payService;

	/**
	 * 开户-标准版修改
	 * @param mediaCode
	 * @return
	 * @throws Exception
	 *
	 */
	@RequestPath(value = "/open")
	public Resp open(HttpRequest request, OpenUserVo open) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.YES)) {
			return Resp.fail("用户已开户");
		}
//		if ("".equals(open.getPaypwd()) || open.getPaypwd() == null) {
//			return Resp.fail("支付密码为必填项");
//		}
		String ip = request.getClientIp();
		if (StrUtil.isBlank(open.getIp())) {
			open.setIp(ip);
		}
		open.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		open.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		open.setUid(curr.getId());
		Ret ret = payService.openUser(open, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 开户状态
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月12日 下午3:03:21
	 */
	@RequestPath(value = "/openflag")
	public Resp openflag(HttpRequest request) throws Exception {
		User curr = WebUtils.currUser(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.YES)) {
			WxUserCoinLocal wxUserCoinLocal = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
			if (curr.getWalletid() != null && !"".equals(curr.getWalletid())) {
				ret.put("walletid", curr.getWalletid());
				ret.put("cny", wxUserCoinLocal.getCny());
			} else {
				ret.put("walletid", wxUserCoinLocal.getWalletid());
				ret.put("cny", wxUserCoinLocal.getCny());
			}
			ret.put("openid", curr.getOpenid());
		}
		ret.put("uid", curr.getId());
		ret.put("openflag", curr.getOpenflag());
		ret.put("paypwdflag", curr.getPaypwdflag());
		RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
		if (realNameCertification == null) {
			ret.put("status", -99);
			ret.put("msg", "未上传实名信息");
			return Resp.ok(ret);
		}
		ret.put("status", realNameCertification.getStatus());
		ret.put("mark", realNameCertification.getMark());
		if (realNameCertification.getStatus().equals(-1)) {
			ret.put("msg", "实名审核已被拒绝");
		}
		if (realNameCertification.getStatus().equals(0)) {
			ret.put("msg", "实名审核中");
		}
		return Resp.ok(ret);
	}

	/**
	 * 修改开户信息-标准版无
	 * @param request
	 * @param open
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:30:57
	 */
	@RequestPath(value = "/updateOpenInfo")
	public Resp updateOpenInfo(HttpRequest request, UpdateOpenVo update) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}

		if (update == null) {
			return Resp.fail("参数为空");
		}
		//		if(!Objects.equals(update.getUid(), curr.getId()) || !update.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (update.getUid() == null) {
			update.setUid(curr.getId());
		}
		if (update.getWalletid() == null) {
			update.setWalletid(curr.getWalletid());
		}
		Ret ret = payService.updateOpenUser(update, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	@RequestPath(value = "/redpacketMaxAmount")
	public  Resp redpacketMaxAmount(HttpRequest request) throws Exception {
		Integer sendRedpacketLimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_MAX_AMOUNT, 10);
		Map data = new HashMap();
		data.put("redpacketMaxAmount", sendRedpacketLimit);
		return Resp.ok(data);
	}

	/**
	 * 充值接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:25:37
	 */
	@RequestPath(value = "/recharge")
	public Resp recharge(HttpRequest request, RechargeVo rechargeVo) throws Exception {
		log.error("recharge debugger --> rechargeVo: {}", rechargeVo.toString());
		UserPaymentImg item = new UserPaymentImg();
		User curr = WebUtils.currUser(request);
		WxUserBankCard wxUserBankCard = new WxUserBankCard();
		if (Const.PAY_TYPE.equals("3")) {
//			if (rechargeVo.getPaypwd() == null || "".equals(rechargeVo.getPaypwd())) {
//				return Resp.fail("请输入支付密码");
//			}
//			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
//			if (!userCoin.getCostpwd().equals(encrypt(rechargeVo.getPaypwd()))) {
//				return Resp.fail("支付密码错误");
//			}
			if (!rechargeVo.getType().equals(4)) {
				item = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", curr.getId(), rechargeVo.getType());
				if (item == null || item.getName() == null || "".equals(item.getName()) || item.getPaymentAccount() == null || "".equals(item.getPaymentAccount())) {
					return Resp.fail("请先将个人收款码信息补充完整");
				}
			} else {
				wxUserBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", curr.getId());
				if (wxUserBankCard == null) {
					return Resp.fail("请先上传银行卡信息");
				}
			}
		}
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (rechargeVo == null) {
			return Resp.fail("充值参数为空");
		}
		if (StrUtil.isBlank(rechargeVo.getAmount())) {
			return Resp.fail("充值金额为空");
		}
		Double _amount = Double.parseDouble(rechargeVo.getAmount());
		if (_amount <= 0) {
			return Resp.fail("充值金额为负");
		}
		Integer rechargeLimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_RECHARGE_MAX_AMOUNT, 100);
		if (_amount > rechargeLimit) {
			return Resp.fail("充值金额最大为" + new Double(rechargeLimit) / 100 + "元");
		}
		//		if(!Objects.equals(rechargeVo.getUid(), curr.getId()) || !rechargeVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (rechargeVo.getUid() == null) {
			rechargeVo.setUid(curr.getId());
		}
		if (rechargeVo.getWalletid() == null) {
			rechargeVo.setWalletid(curr.getWalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", rechargeVo.getUid());
			if (rechargeVo.getWalletid() == null) {
				rechargeVo.setWalletid(userCoin.getWalletid());
			}
		}
		rechargeVo.setIp(request.getClientIp());
		rechargeVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		rechargeVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		log.error("recharge debugger --> enter payService recharge method. rechargeVo: {}", rechargeVo.toString());
		Ret ret = payService.recharge(rechargeVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		if (Const.PAY_TYPE.equals("3")){
			WalletItemLocal walletItem = new WalletItemLocal();
			walletItem.setType(rechargeVo.getType());
			walletItem.setRechargeOrWithhold(1);
			walletItem.setUid(rechargeVo.getUid());
			walletItem.setAmount(Double.valueOf(rechargeVo.getAmount()));
			walletItem.setStatus(0);
			Map data = (HashMap) ret.get("data");
			walletItem.setSerialnumber(data.get("serialnumber").toString());
			walletItem.setCreateTime(new Date());
			walletItem.setUpdateTime(new Date());
			walletItem.setToken(request.getHttpSession().getId());
			if (rechargeVo.getType().equals(4)) {

				walletItem.setName(wxUserBankCard.getUsername());
				walletItem.setPaymentAccount(wxUserBankCard.getCardno());
			} else {

				walletItem.setName(item.getName());
				walletItem.setPaymentAccount(item.getPaymentAccount());
			}
			boolean save = walletItem.save();
			if (!save) {
				return Resp.fail("充值记录提交到后台失败，请联系客服");
			}
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 标准版新增
	 * @param request
	 * @param rechargeVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月11日 上午11:57:54
	 */
	@RequestPath(value = "/rechargeconfirm")
	public Resp rechargeConfirm(HttpRequest request, RechargeConfirmVo rechargeVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (rechargeVo == null) {
			return Resp.fail("充值参数为空");
		}
		if (rechargeVo.getUid() == null) {
			rechargeVo.setUid(curr.getId());
		}
		if (rechargeVo.getWalletid() == null) {
			rechargeVo.setWalletid(curr.getWalletid());
		}
		rechargeVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		Ret ret = payService.rechargeConfirm(rechargeVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 充值查询
	 * @param request
	 * @param queryVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月25日 下午2:07:39
	 */
	@RequestPath(value = "/rechargeQuery")
	public Resp rechargeQuery(HttpRequest request, RechargeQueryVo queryVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (queryVo == null || (StrUtil.isBlank(queryVo.getSerialnumber()) && StrUtil.isBlank(queryVo.getReqid()))) {
			return Resp.fail("充值参数为空");
		}
		if (queryVo.getUid() == null) {
			queryVo.setUid(curr.getId());
		}
		if (queryVo.getWalletid() == null) {
			queryVo.setWalletid(curr.getWalletid());
		}
		Ret ret = payService.rechargeQuery(queryVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 充值记录
	 * @param request
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月19日 上午11:20:27
	 */
	@RequestPath(value = "/rechargelist")
	public Resp rechargelist(HttpRequest request, Integer pageNumber) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.rechargelist(curr.getId(), pageNumber);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 提现接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:26:20
	 */
	@RequestPath(value = "/withhold")
	public Resp withhold(HttpRequest request, WithholdVo withholdVo) throws Exception {
		log.error("withhold debugger --> withholdVo: {}", withholdVo.toString());
		User curr = WebUtils.currUser(request);
		UserPaymentImg item = new UserPaymentImg();
		WxUserBankCard wxUserBankCard = new WxUserBankCard();
		if (Const.PAY_TYPE.equals("3")) {
			if (Objects.equals(curr.getPaypwdflag(), Const.YesOrNo.NO)) {
				return Resp.fail("未设置支付密码，请先设置支付密码");
			}
			if (withholdVo.getPaypwd() == null || "".equals(withholdVo.getPaypwd())) {
				return Resp.fail("请输入支付密码");
			}
			if (!Objects.equals(withholdVo.getPaypwd(), curr.getPaypwd())) {
				return Resp.fail("支付密码错误");
			}
			if (!withholdVo.getType().equals(4)) {
				item = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", curr.getId(), withholdVo.getType());
				if (item == null || item.getName() == null || "".equals(item.getName()) || item.getPaymentAccount() == null || "".equals(item.getPaymentAccount())) {
					return Resp.fail("请先将个人收款码信息补充完整");
				}
			} else {
				wxUserBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", curr.getId());
				if (wxUserBankCard == null) {
					return Resp.fail("请先上传银行卡信息");
				}
			}

		}
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (withholdVo == null) {
			return Resp.fail("充值参数为空");
		}
		withholdVo.setUser(curr);
		if (StrUtil.isBlank(withholdVo.getAmount())) {
			return Resp.fail("提现金额为空");
		}
		Double _amount = Double.parseDouble(withholdVo.getAmount());
		if (_amount <= 0) {
			return Resp.fail("提现金额为负");
		}
		Integer withholdlimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_AMOUNT, 10);
		if (_amount > withholdlimit) {
			return Resp.fail("提现金额最大为" + new Double(withholdlimit) / 100 + "元");
		}
		Integer minAmount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MIN_AMOUT, 10000);
		if (_amount < minAmount) {
			return Resp.fail("单次提现金额不低于" + ((double) minAmount) / 100 + "元");
		}

		//		if(!Objects.equals(withholdVo.getUid(), curr.getId()) || !withholdVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (withholdVo.getUid() == null) {
			withholdVo.setUid(curr.getId());
		}
		if (withholdVo.getWalletid() == null) {
			withholdVo.setWalletid(curr.getWalletid());
		}

		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", withholdVo.getUid());
			if (userCoin.getCny() - Long.valueOf(withholdVo.getAmount()) < 0.0) {
				return Resp.fail("余额不足");
			}
//			UserPaymentImg userPaymentInfo = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", withholdVo.getUid(), withholdVo.getType());
//			if(userPaymentInfo == null) {
//				return Resp.fail("请先设置您的收款码");
//			}
			if (withholdVo.getWalletid() == null) {
				withholdVo.setWalletid(userCoin.getWalletid());
			}
		}
		String ip = request.getClientIp();
		IpInfo ipinfo = IpInfoService.ME.save(ip);
		withholdVo.setIpInfo(ipinfo);
		withholdVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		withholdVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		log.error("withhold debugger --> enter payService method withholdVo: {}", withholdVo.toString());
		Ret ret = payService.withhold(withholdVo, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}

		if (Const.PAY_TYPE.equals("3")){
			WalletItemLocal walletItem = new WalletItemLocal();
			walletItem.setType(withholdVo.getType());
			walletItem.setUid(withholdVo.getUid());
			walletItem.setAmount(Double.valueOf(withholdVo.getAmount()));
			walletItem.setStatus(0);
			walletItem.setRechargeOrWithhold(2);
			Map data = (HashMap) ret.get("data");
			walletItem.setSerialnumber(data.get("serialnumber").toString());
			walletItem.setCreateTime(new Date());
			walletItem.setUpdateTime(new Date());
			walletItem.setToken(request.getHttpSession().getId());
			if (withholdVo.getType().equals(4)) {
				walletItem.setName(wxUserBankCard.getUsername());
				walletItem.setBankname(wxUserBankCard.getBankname());
				walletItem.setPaymentAccount(wxUserBankCard.getCardno());
			} else {
				walletItem.setName(item.getName());
				walletItem.setPaymentAccount(item.getPaymentAccount());
			}
//			walletItem.setUlink(withholdVo.getUlink());
//			walletItem.setUlinkimg(withholdVo.getUlinkimg());
			boolean save = walletItem.save();
			if (!save) {
				Resp.fail("提现记录提交到后台数据失败");
			}
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * @param request
	 * @param queryVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月25日 下午2:24:31
	 */
	@RequestPath(value = "/withholdQuery")
	public Resp withholdQuery(HttpRequest request, WithholdQueryVo queryVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (queryVo == null || (StrUtil.isBlank(queryVo.getSerialnumber()) && StrUtil.isBlank(queryVo.getReqid()))) {
			return Resp.fail("提现查询参数为空");
		}
		if (queryVo.getUid() == null) {
			queryVo.setUid(curr.getId());
		}
		if (queryVo.getWalletid() == null) {
			queryVo.setWalletid(curr.getWalletid());
		}
		Ret ret = payService.withholdQuery(queryVo, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 提现记录
	 * @param request
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月19日 上午11:19:56
	 */
	@RequestPath(value = "/withholdlist")
	public Resp withholdlist(HttpRequest request, Integer pageNumber) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.withholdlist(curr.getId(), pageNumber);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 发红包接口
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:27:04
	 */
	@RequestPath(value = "/sendRedpacket")
	public Resp sendRedpacket(HttpRequest request, SendRedpacketVo redpacketVo) throws Exception {
		log.error("sendRedpacket debugger --> redpacketVo: {}", redpacketVo.toString());
		String walletMerchantid = Const.WALLET_MERCHANTID;
		User curr = WebUtils.currUser(request);
		if (Const.PAY_TYPE.equals("3")) {
			if (redpacketVo.getPaypwd() == null || "".equals(redpacketVo.getPaypwd())) {
				return Resp.fail("请输入支付密码");
			}
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
//			if (!userCoin.getCostpwd().equals(encrypt(redpacketVo.getPaypwd()))) {
//				return Resp.fail("支付密码错误");
//			}
		}
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (redpacketVo == null || redpacketVo.getChatlinkid() == null) {
			return Resp.fail("钱包参数为空");
		}

		Double _amount = Double.parseDouble(redpacketVo.getCny());
		if (_amount <= 0) {
			return Resp.fail("红包金额最小为0.01元");
		}
		Integer sendRedpacketLimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_MAX_AMOUNT, 10);
		if (_amount > sendRedpacketLimit) {
			return Resp.fail("红包金额最大为" + new Double(sendRedpacketLimit) / 100 + "元");
		}

		//		if(!Objects.equals(redpacketVo.getUid(), curr.getId()) || !redpacketVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (redpacketVo.getUid() == null) {
			redpacketVo.setUid(curr.getId());
		}
		if (redpacketVo.getWalletid() == null) {
			redpacketVo.setWalletid(curr.getWalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", redpacketVo.getUid());
			if (userCoin.getCny() - _amount < 0.0) {
				return Resp.fail("余额不足");
			}
			redpacketVo.setWalletid(userCoin.getWalletid());
		}
		Long chatlinkid = redpacketVo.getChatlinkid();
		Short chatmode = Const.ChatMode.P2P;
		Long groupid = null;
		if (chatlinkid <= 0) {
			chatmode = Const.ChatMode.GROUP;
			groupid = -chatlinkid;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
			if (!ChatService.groupExistChat(groupItem)) {
				return Resp.fail("不是群成员");
			}
			chatlinkid = groupItem.getChatlinkid();
			redpacketVo.setBizid(groupid);
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			if (!ChatService.existTwoFriend(userItem)) {
				return Resp.fail("你们不是互相不是好友");
			}
			redpacketVo.setBizid(userItem.getBizid());
		}
		redpacketVo.setChatmode(chatmode);
		log.error("sendRedpacket debugger --> enter payService sendRedpacket redpacketVo: {}", redpacketVo.toString());
		Ret ret = payService.sendRedpacket(redpacketVo, request);

		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}


	/**
	 * 转账
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午4:27:04
	 */
	@RequestPath(value = "/transfer")
	public Resp transfer(HttpRequest request, SendRedpacketVo redpacketVo) throws Exception {
//		String walletMerchantid = Const.WALLET_MERCHANTID;
		User curr = WebUtils.currUser(request);
		if (!Const.PAY_TYPE.equals("3")) {
			return Resp.fail("暂不支持转账功能");
		}
		if (redpacketVo.getPaypwd() == null || "".equals(redpacketVo.getPaypwd())) {
			return Resp.fail("请输入支付密码");
		}
//		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
//			if (!userCoin.getCostpwd().equals(encrypt(redpacketVo.getPaypwd()))) {
//				return Resp.fail("支付密码错误");
//			}
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (redpacketVo == null || redpacketVo.getChatlinkid() == null) {
			return Resp.fail("钱包参数为空");
		}

		Double _amount = Double.parseDouble(redpacketVo.getCny());
//		if (_amount <= 0) {
//			return Resp.fail("转账金额最小为0.01元");
//		}
//		Integer sendRedpacketLimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_MAX_AMOUNT, 10);
//		if (_amount > sendRedpacketLimit) {
//			return Resp.fail("转账金额最大为" + new Double(sendRedpacketLimit) / 100 + "元");
//		}

		//		if(!Objects.equals(redpacketVo.getUid(), curr.getId()) || !redpacketVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (redpacketVo.getUid() == null) {
			redpacketVo.setUid(curr.getId());
		}
		if (redpacketVo.getWalletid() == null) {
			redpacketVo.setWalletid(curr.getWalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", redpacketVo.getUid());
			if (userCoin.getCny() - _amount < 0.0) {
				return Resp.fail("余额不足");
			}
			redpacketVo.setWalletid(userCoin.getWalletid());
		}
		Long chatlinkid = redpacketVo.getChatlinkid();
		Short chatmode = Const.ChatMode.P2P;
		Long groupid = null;
		if (chatlinkid <= 0) {
			chatmode = Const.ChatMode.GROUP;
			groupid = -chatlinkid;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
			if (!ChatService.groupExistChat(groupItem)) {
				return Resp.fail("不是群成员");
			}
			chatlinkid = groupItem.getChatlinkid();
			redpacketVo.setBizid(groupid);
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			if (!ChatService.existTwoFriend(userItem)) {
				return Resp.fail("你们不是互相不是好友");
			}
			redpacketVo.setBizid(userItem.getBizid());
		}
		redpacketVo.setChatmode(chatmode);
		Ret ret = payService.transfer(redpacketVo, request);

		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 *
	 * 标准版新增
	 * 初始化红包
	 * @param request
	 * @param redpacketVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月15日 下午4:19:50
	 */
	@RequestPath(value = "/initredpacket")
	public Resp initRedpacket(HttpRequest request, SendRedpacketVo redpacketVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
		if (realNameCertification == null) {
			return Resp.fail().msg("未进行实名认证，提交实名认证，并在审核通过后，设置支付密码才可抢红包");
		}
		if (realNameCertification.getStatus().equals(-1)) {
			return Resp.fail().msg("实名认证失败，请重新提交实名信息。");
		}
		if (realNameCertification.getStatus().equals(0)) {
			return Resp.fail().msg("实名认证审核中，实名通过后才可抢红包。");
		}
		if (Objects.equals(curr.getPaypwdflag(), Const.YesOrNo.NO)) {
			return Resp.fail("支付密码未设置");
		}
		if (redpacketVo == null || redpacketVo.getChatlinkid() == null) {
			return Resp.fail("钱包参数为空");
		}
		Double _amount = Double.parseDouble(redpacketVo.getCny());
		if (_amount <= 0) {
			return Resp.fail("红包金额最小为0.01元");
		}

		Integer sendRedpacketLimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_SENDREDPACKET_MAX_AMOUNT, 10);
		if (_amount > sendRedpacketLimit) {
			return Resp.fail("红包金额最大为" + new Double(sendRedpacketLimit) / 100 + "元");
		}
		if (redpacketVo.getUid() == null) {
			redpacketVo.setUid(curr.getId());
		}
		if (redpacketVo.getWalletid() == null) {
			redpacketVo.setWalletid(curr.getWalletid());
		}
		if (redpacketVo.getSubwalletid() == null) {
			redpacketVo.setSubwalletid(curr.getSubwalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", redpacketVo.getUid());
			if (userCoin.getCny() - _amount < 0.0) {
				return Resp.fail("余额不足");
			}
			redpacketVo.setWalletid(userCoin.getWalletid());
		}
		Long chatlinkid = redpacketVo.getChatlinkid();
		Short chatmode = Const.ChatMode.P2P;
		Long groupid = null;
		if (chatlinkid <= 0) {
			chatmode = Const.ChatMode.GROUP;
			groupid = -chatlinkid;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
			if (!ChatService.groupExistChat(groupItem)) {
				return Resp.fail("不是群成员");
			}
			chatlinkid = groupItem.getChatlinkid();
			redpacketVo.setBizid(groupid);
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			if (!ChatService.existTwoFriend(userItem)) {
				return Resp.fail("你们不是互相不是好友");
			}
			redpacketVo.setBizid(userItem.getBizid());
		}
		redpacketVo.setIp(request.getClientIp());
		redpacketVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		redpacketVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		redpacketVo.setChatmode(chatmode);
		Ret ret = payService.initRedpacket(redpacketVo);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 标准版新增
	 * 快捷支付红包-预下单-发短信
	 * @param request
	 * @param redpacketVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月15日 下午4:19:46
	 */
	@RequestPath(value = "/quickredpacket")
	public Resp quickRedpacket(HttpRequest request, SendRedpacketVo redpacketVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (redpacketVo == null || redpacketVo.getRid() == null || StrUtil.isBlank(redpacketVo.getAgrno())) {
			return Resp.fail("钱包参数为空");
		}
		if (redpacketVo.getUid() == null) {
			redpacketVo.setUid(curr.getId());
		}
		if (redpacketVo.getWalletid() == null) {
			redpacketVo.setWalletid(curr.getWalletid());
		}
		redpacketVo.setIp(request.getClientIp());
		redpacketVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		redpacketVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		Ret ret = payService.quickRedpacket(redpacketVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 标准版新增
	 * 红包支付
	 * @param request
	 * @param redpacketVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月15日 下午4:19:43
	 */
	@RequestPath(value = "/payredpacket")
	public Resp payRedpacket(HttpRequest request, SendRedpacketVo redpacketVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (redpacketVo == null || redpacketVo.getPaytype() == null || redpacketVo.getRid() == null) {
			return Resp.fail("钱包参数为空");
		}
		if (redpacketVo.getUid() == null) {
			redpacketVo.setUid(curr.getId());
		}
		if (redpacketVo.getWalletid() == null) {
			redpacketVo.setWalletid(curr.getWalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			if (StrUtil.isBlank(redpacketVo.getPaypwd())) {
				return Resp.fail("未输入支付密码");
			}
			if (!Objects.equals(redpacketVo.getPaypwd(), curr.getPaypwd())) {
				return Resp.fail("支付密码错误");
			}
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", redpacketVo.getUid());
			redpacketVo.setWalletid(userCoin.getWalletid());
		}
		if (Objects.equals(redpacketVo.getPaytype(), PayConst.RedPayType.CNY)) {
//			if (StrUtil.isBlank(redpacketVo.getPaypwd())) {
//				return Resp.fail("支付密码为空");
//			}
//			if (!Objects.equals(curr.getPaypwd(), redpacketVo.getPaypwd())) {
//				return Resp.fail("支付密码错误");
//			}
		} else {
			if (StrUtil.isBlank(redpacketVo.getMerorderid()) || StrUtil.isBlank(redpacketVo.getSmscode())) {
				return Resp.fail("快捷支付参数为空");
			}
		}
		String ip = request.getClientIp();
		IpInfo ipinfo = IpInfoService.ME.save(ip);
		redpacketVo.setIp(ip);
		redpacketVo.setIpInfo(ipinfo);
		redpacketVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		redpacketVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		Ret ret = payService.payRedpacket(redpacketVo, request, curr);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * @param request
	 * @param queryVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月18日 上午10:56:51
	 */
	@RequestPath(value = "/redpacketpayquery")
	public Resp redpacketPayQuery(HttpRequest request, RedpacketQueryVo queryVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (queryVo == null || (StrUtil.isBlank(queryVo.getSerialnumber()) && StrUtil.isBlank(queryVo.getReqid()))) {
			return Resp.fail("红包参数为空");
		}
		if (queryVo.getUid() == null) {
			queryVo.setUid(curr.getId());
		}
		if (queryVo.getWalletid() == null) {
			queryVo.setWalletid(curr.getWalletid());
		}
		Ret ret = payService.redpacketPayQuery(queryVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 发红包列表
	 * @param request
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月19日 上午11:37:50
	 */
	@RequestPath(value = "/sendRedpacketlist")
	public Resp sendRedpacketlist(HttpRequest request, Integer pageNumber, String period) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.sendRedpacketlist(curr.getId(), pageNumber, period);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月27日 下午4:50:26
	 */
	@RequestPath(value = "/sendredpacketstat")
	public Resp sendRedpacketStat(HttpRequest request, String period) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.sendRedpacketStat(curr.getId(), period);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		Record record = RetUtils.getOkTData(ret);
		if (record != null) {
			record.set("nick", curr.getNick());
			record.set("avatar", curr.getAvatar());
		}
		return Resp.ok(record);
	}

	/**
	 * 抢红包
	 * @param request
	 * @param grabRedpacketVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月19日 上午10:44:03
	 */
	@RequestPath(value = "/grabRedpacket")
	public Resp grabRedpacket(HttpRequest request, GrabRedpacketVo grabRedpacketVo) throws Exception {
//		if (Const.PAY_TYPE.equals("3")) {
//			WxWalletSendRedPacketLocal redPacketLocal = WxWalletSendRedPacketLocal.dao.findFirst("select * from wx_wallet_send_red_packet_local where merorderid = ?", grabRedpacketVo.getSerialnumber());
//			if (redPacketLocal != null) {
//				if(redPacketLocal.getNum() - redPacketLocal.getAcceptnum() <= 0 && redPacketLocal.getStatus() == 4) {
//					return Resp.fail("红包已抢完.");
//				}
//			} else {
//				return Resp.fail("红包记录不存在");
//			}
//		}
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (grabRedpacketVo == null) {
			return Resp.fail("钱包参数为空");
		}
		//		if(!Objects.equals(grabRedpacketVo.getUid(), curr.getId()) || !grabRedpacketVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (grabRedpacketVo.getUid() == null) {
			grabRedpacketVo.setUid(curr.getId());
		}
		if (grabRedpacketVo.getWalletid() == null) {
			grabRedpacketVo.setWalletid(curr.getWalletid());
		}
		RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
		if (realNameCertification == null) {
			return Resp.fail().msg("未进行实名认证，提交实名认证，并在审核通过后，设置支付密码才可抢红包");
		}
		if (realNameCertification.getStatus().equals(-1)) {
			return Resp.fail().msg("实名认证失败，请重新提交实名信息。");
		}
		if (realNameCertification.getStatus().equals(0)) {
			return Resp.fail().msg("实名认证审核中，实名通过后才可抢红包。");
		}
		if (Objects.equals(curr.getPaypwdflag(), Const.YesOrNo.NO)) {
			return Resp.fail("支付密码未设置");
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", grabRedpacketVo.getUid());
			grabRedpacketVo.setWalletid(userCoin.getWalletid());
			curr.setWalletid(userCoin.getWalletid());
		}
		Long chatlinkid = grabRedpacketVo.getChatlinkid();
		Short chatmode = Const.ChatMode.P2P;
		Long groupid = null;
		log.error("抢红包的chatlinkid:{}", chatlinkid);
		if (chatlinkid <= 0) {
			chatmode = Const.ChatMode.GROUP;
			groupid = -chatlinkid;
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(curr.getId(), groupid);
			if (!ChatService.groupExistChat(groupItem)) {
				return Resp.fail("不是群成员");
			}
			chatlinkid = groupItem.getChatlinkid();
			grabRedpacketVo.setBizid(groupid);
		} else {
			WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
			if (!ChatService.existTwoFriend(userItem)) {
				return Resp.fail("你们不是互相不是好友");
			}
			grabRedpacketVo.setBizid(userItem.getBizid());
		}
		grabRedpacketVo.setChatmode(chatmode);

		String ip = request.getClientIp();
		grabRedpacketVo.setIp(ip);
		grabRedpacketVo.setDevicetype(WebUtils.getRequestExt(request).getDeviceType());
		grabRedpacketVo.setAppversion(WebUtils.getRequestExt(request).getAppVersion());
		log.error("grabRedpacket logger ==> grabRedpacketVo : {} ", grabRedpacketVo.toString());
		Ret ret = payService.grabRedpacket(grabRedpacketVo, curr, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 抢红包列表
	 * @param request
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月19日 上午11:38:12
	 */
	@RequestPath(value = "/grabRedpacketlist")
	public Resp grabRedpacketlist(HttpRequest request, Integer pageNumber, String period) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.grabRedpacketlist(curr.getId(), pageNumber, period);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * @param request
	 * @param period
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月27日 下午4:52:29
	 */
	@RequestPath(value = "/grabredpacketstat")
	public Resp grabRedpacketStat(HttpRequest request, String period) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.grabRedpacketStat(curr.getId(), period);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		Record record = RetUtils.getOkTData(ret);
		if (record != null) {
			record.set("nick", curr.getNick());
			record.set("avatar", curr.getAvatar());
		}
		return Resp.ok(record);
	}

	/**
	 * @param request
	 * @param serialNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月22日 下午10:20:30
	 */
	@RequestPath(value = "/redStatus")
	public Resp redStatus(HttpRequest request, Integer rid, String serialnumber) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.redStatus(curr, serialnumber, rid);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 红包信息
	 * @param request
	 * @param serialNumber
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月22日 下午10:51:00
	 */
	@RequestPath(value = "/redInfo")
	public Resp redInfo(HttpRequest request, String serialnumber, Integer rid) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.redInfo(request, serialnumber, curr, rid);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 标准版新增
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年4月9日 上午9:57:36
	 */
	@RequestPath(value = "/paylistinfo")
	public Resp payListInfo(HttpRequest request) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.payListInfo(curr, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 获取钱包信息
	 * @param request
	 * @param open
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午3:42:15
	 */
	@RequestPath(value = "/getWalletInfo")
	public Resp getWalletInfo(HttpRequest request, WalletVo walletVo) throws Exception {
		log.error("/pay/getWalletInfo param: {}", walletVo);
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (walletVo == null) {
			return Resp.fail("钱包参数为空");
		}
		if (walletVo.getUid() == null) {
			walletVo.setUid(curr.getId());
		}
		if (walletVo.getWalletid() == null) {
			walletVo.setWalletid(curr.getWalletid());
		}
		if (Const.PAY_TYPE.equals("3")) {
			WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", walletVo.getUid());
			walletVo.setWalletid(userCoin.getWalletid());
		}
		Ret ret = payService.getWalletInfo(walletVo, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 获取钱包明细
	 * @param request
	 * @param pageNumber
	 * @param mode
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月26日 上午10:42:13
	 */
	@RequestPath(value = "/getWalletItems")
	public Resp getWalletItems(HttpRequest request, Integer pageNumber, Short mode) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.getWalletItems(curr.getId(), pageNumber, mode);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 获取客户端native的token
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年11月6日 下午3:42:57
	 */
	@RequestPath(value = "/getClientToken")
	public Resp getClientToken(HttpRequest request, ClientTokenVo tokenVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (tokenVo == null) {
			return Resp.fail("token参数为空");
		}
		//		if(!Objects.equals(tokenVo.getUid(), curr.getId()) || !tokenVo.getWalletid().equals(curr.getWalletid())) {
		//			return Resp.fail("权限不足");
		//		}
		if (tokenVo.getUid() == null) {
			tokenVo.setUid(curr.getId());
		}
		if (tokenVo.getWalletid() == null) {
			tokenVo.setWalletid(curr.getWalletid());
		}
		tokenVo.setUid(curr.getId());
		tokenVo.setWalletid(curr.getWalletid());
		Ret ret = payService.getClientToken(tokenVo, request);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/*******************************标准版新增*******************************************/

	/**
	 * 实名信息
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月10日 下午5:44:46
	 */
	@RequestPath(value = "/realinfo")
	public Resp realinfo(HttpRequest request) throws Exception {
		User curr = WebUtils.currUser(request);
		Ret ret = payService.realInfo(curr.getId());
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 银行卡列表
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月12日 上午11:15:03
	 */
	@RequestPath(value = "/bankcardlist")
	public Resp bankcardList(HttpRequest request) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		Ret ret = payService.bankcardList(curr.getId());
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkList(ret));
	}

	/**
	 * @param request
	 * @param cardVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月10日 下午5:47:48
	 */
	@RequestPath(value = "/bindcard")
	public Resp bindcard(HttpRequest request, BindCardVo cardVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (cardVo == null) {
			return Resp.fail("参数为空");
		}
		if (cardVo.getUid() == null) {
			cardVo.setUid(curr.getId());
		}
		if (cardVo.getWalletid() == null) {
			cardVo.setWalletid(curr.getWalletid());
		}
		if (cardVo.getName() == null || cardVo.getName().isEmpty()) {
			cardVo.setName(curr.getName());
		}
		if (cardVo.getMobile() == null || cardVo.getMobile().isEmpty()) {
			cardVo.setMobile(curr.getPhone());
		}
		Ret ret = payService.bindCard(cardVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * @param request
	 * @param cardVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月10日 下午6:14:06
	 */
	@RequestPath(value = "/bindcardconfirm")
	public Resp bindcardconfirm(HttpRequest request, BindCardConfirmVo cardVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (cardVo == null || cardVo.getBankcardid() == null || StrUtil.isBlank(cardVo.getSmscode())) {
			return Resp.fail("参数为空");
		}
		if (cardVo.getUid() == null) {
			cardVo.setUid(curr.getId());
		}
		Ret ret = payService.bindCardConfirm(cardVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * @param request
	 * @param cardVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月10日 下午5:47:48
	 */
	@RequestPath(value = "/unbindcard")
	public Resp unbindcard(HttpRequest request, UnBindCardVo cardVo) throws Exception {
		User curr = WebUtils.currUser(request);
		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}
		if (cardVo == null) {
			return Resp.fail("参数为空");
		}
		if (!Objects.equals(curr.getPaypwd(), cardVo.getPaypwd())) {
			return Resp.fail("支付密码错误");
		}
		if (cardVo.getUid() == null) {
			cardVo.setUid(curr.getId());
		}
		if (cardVo.getWalletid() == null) {
			cardVo.setWalletid(curr.getWalletid());
		}
		Ret ret = payService.unBindCard(cardVo, request);
		if (ret.isFail()) {
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 手续费
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2021年3月18日 下午2:46:50
	 */
	@RequestPath(value = "/commission")
	public Resp commission(HttpRequest request, Long amount) throws Exception {
		Integer rate = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION, 5);
		Integer withholdconst = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_COMMISSION_CONST, 50);
		Map<String, Object> commission = new HashMap<String, Object>();
		commission.put("rate", rate);
		commission.put("withholdconst", withholdconst);
		Integer withholdlimit = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MAX_AMOUNT, 10);
		Integer minAmount = ConfService.getInt(Const.ConfMapping.WX_WALLET_WITHHOLD_MIN_AMOUT, 10000);
		commission.put("max", withholdlimit);
		commission.put("min", minAmount);
		if (amount != null) {
			if (Const.PAY_TYPE.equals("1")) {
				commission.put("commission", PayStdService.basePay.commission(amount));
			} else if (Const.PAY_TYPE.equals("3")) {
				commission.put("commission", LocalWalletService.basePay.commission(amount));
			} else {
				commission.put("commission", Pay5uService.basePay.commission(amount));
			}
		}
		return Resp.ok(commission);
	}

	@RequestPath(value = "/getPayImg")
	public Resp getPaymentImg(HttpRequest request) {
		List<PaymentImg> all = PaymentImg.dao.findAll();
		if (all != null && all.size() > 0)
			return Resp.ok(all.get(0));
		return Resp.ok("后台管理员未上传收款码");
	}
	@RequestPath(value = "/getPayImgNew")
	public Resp getPaymentImgNew(HttpRequest request, Integer type) {
		PaymentImg paymentImg = PaymentImg.dao.findFirst("select * from payment_img where type = ?", type);
		if (paymentImg != null)
			return Resp.ok(paymentImg);

		if (type.equals(1))
			return Resp.fail("后台管理员未上传微信收款码");
		if (type.equals(2))
			return Resp.fail("后台管理员未上传支付宝收款码");
		if (type.equals(3))
			return Resp.fail("后台管理员未上传USDT收款码");
		return Resp.fail("后台管理员未上传收款码");

	}

	@RequestPath("/uploadPaymentCode")
	public Resp uploadPaymentImg(HttpRequest request, UploadFile logo) {
		if (logo == null) {
			return Resp.fail("参数异常");
		}

		byte[] bs = logo.getData();
		String filename = logo.getName();
		String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

		// 只允许特定格式的图片
		if (!"jpg jpeg png bmp gif".contains(extName)) {
			return Resp.fail("仅支持 jpg/jpeg/png/bmp/gif 格式的图片上传");
		}

		try {
			String objectKey = "userPayment/" + getUUID() + "." + extName;

			// 构建 Content-Type
			String contentType;
			switch (extName) {
				case "jpg":
				case "jpeg":
					contentType = "image/jpeg";
					break;
				case "png":
					contentType = "image/png";
					break;
				case "bmp":
					contentType = "image/bmp";
					break;
				case "gif":
					contentType = "image/gif";
					break;
				default:
					contentType = "application/octet-stream";
			}

			// 上传文件到 R2
			InputStream inputStream = new ByteArrayInputStream(bs);

            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//			CloudflareR2Utils.uploadFilePublic(
//					Const.CloudflareR2.R2_BUCKET_NAME,
//					objectKey,
//					inputStream,
//					bs.length,
//					contentType
//			);

			// 返回相对路径，前端拼接 base_url 获取完整 URL
			return Resp.ok("/" + objectKey);

		} catch (Exception e) {
			log.error("文件上传到 R2 异常", e);
			return Resp.fail().code(500).msg("文件上传失败");
		}
	}


	@RequestPath(value = "/setUserPayImg")
	public Resp setUserPayImg(HttpRequest request, Integer uid, String url, Integer type, String name, String paymentAccount) {
		if(uid == null || url == null || type == null) {
			return Resp.fail("参数异常");
		}

		User user = User.dao.findById(uid);
		if (user == null) {
			return Resp.fail("该uid不存在");
		}
		UserPaymentImg payInfo = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", uid, type);
		if (payInfo != null) {
			if (type.equals(1))
				return Resp.fail("已上传微信收款码信息");
			if (type.equals(2))
				return Resp.fail("已上传支付宝收款码信息");
			if (type.equals(3))
				return Resp.fail("已上传USDT收款码信息");
		}

		try {
			UserPaymentImg paymentImg = new UserPaymentImg();
			paymentImg.setUId(uid);
			paymentImg.setUserPaymentUrl(url);
			paymentImg.setType(type);
			paymentImg.setName(name);
			paymentImg.setPaymentAccount(paymentAccount);
			paymentImg.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("添加收款码失败");
		}
		return Resp.fail().code(500);
	}

	@RequestPath(value = "/getUserPayInfo")
	public Resp getUserPaymentUrlInfo(HttpRequest request, Integer uid, Integer type) {
		HashMap<String, Object> data = new HashMap<>();
		if (type.equals(4)) {
			WxUserBankCard userBankCard = WxUserBankCard.dao.findFirst("select * from wx_user_bank_card where uid = ?", uid);
			if (userBankCard == null) {
				return Resp.ok();
			}
			data.put("id", userBankCard.getId());
			data.put("uid", userBankCard.getUid());
//			data.put("userPaymentUrl", userBankCard.getUserPaymentUrl());
			data.put("type", 4);
			data.put("name", userBankCard.getUsername());
			data.put("paymentAccount", userBankCard.getCardno());
			data.put("bankname", userBankCard.getBankname());
		} else {
			UserPaymentImg userPaymentImg = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", uid, type);
			if (userPaymentImg == null) {
				return Resp.ok();
			}
			data.put("id", userPaymentImg.getId());
			data.put("uid", userPaymentImg.getUId());
			data.put("userPaymentUrl", userPaymentImg.getUserPaymentUrl());
			data.put("type", userPaymentImg.getType());
			data.put("name", userPaymentImg.getName());
			data.put("paymentAccount", userPaymentImg.getPaymentAccount());
		}

		return Resp.ok(data);
	}

	@RequestPath(value = "/delUserPayImg")
	public Resp delPaymentImg(HttpRequest request, String id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		UserPaymentImg paymentImg = UserPaymentImg.dao.findById(id);
		if(paymentImg == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = paymentImg.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	@RequestPath(value = "/updatePayImg")
	public Resp updatePayImg(HttpRequest request, Integer id, String url, String name, String paymentAccount) {
		if (id == null) {
			return Resp.fail("id不能为空");
		}

		UserPaymentImg payImg = UserPaymentImg.dao.findById(id);
		if(payImg == null) {
			return Resp.fail("该id不存在");
		}

		try {
			UserPaymentImg paymentImg = new UserPaymentImg();
			paymentImg.setId(id);
			paymentImg.setUId(payImg.getUId());
			paymentImg.setType(payImg.getType());
			paymentImg.setUserPaymentUrl(url);
			paymentImg.setName(name);
			paymentImg.setPaymentAccount(paymentAccount);
			paymentImg.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("更新收款码失败异常");
		}
		return Resp.fail().code(500);
	}

	public static String getUUID(){

		UUID uuid=UUID.randomUUID();

		String str = uuid.toString();

		String uuidStr=str.replace("-", "");

		return uuidStr;

	}

	@RequestPath(value = "/getPayType")
	public Resp getPayType(HttpRequest request) {

		String payType = Const.PAY_TYPE;
		Map data = new HashMap();
		data.put("payType", payType);
		return Resp.ok(data);
	}

	@RequestPath(value = "/setPayPwd")
	public Resp setPayPwd(HttpRequest request, String pwd) {
		User curr = WebUtils.currUser(request);


		if (pwd.replaceAll("\\s*", "").length() != 6) {
			return Resp.fail("请输入六位数密码");
		}

		WxWalletCoinLocal walletCoin = WxWalletCoinLocal.dao.findFirst("select * from wx_wallet_coin_local where uid = ?", curr.getId());
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
		if (walletCoin == null || userCoin == null) {
			return Resp.fail("钱包信息不存在，请先开户。");
		}

		walletCoin.setCostpwd(encrypt(pwd));
		log.error("walletCoin's pwd: {}", walletCoin.getCostpwd());
		boolean update = walletCoin.update();
		userCoin.setCostpwd(encrypt(pwd));
		log.error("userCoin's pwd: {}", userCoin.getCostpwd());
		boolean update1 = userCoin.update();
		if (!update || !update1) {
			return Resp.fail("支付密码设置失败");
		}

		return Resp.ok();
	}

	@RequestPath(value = "/authPayPwd")
	public Resp authPayPwd(HttpRequest request, String pwd) {
		User curr = WebUtils.currUser(request);


		if (pwd.replaceAll("\\s*", "").length() != 6) {
			return Resp.fail("请输入六位数密码");
		}

		WxWalletCoinLocal walletCoin = WxWalletCoinLocal.dao.findFirst("select * from wx_wallet_coin_local where uid = ?", curr.getId());
		log.error("密码验证: walletCoin's pwd: {}", walletCoin.getCostpwd());
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());
		log.error("密码验证: userCoin's pwd: {}", userCoin.getCostpwd());
		log.error("密码验证: encrypt(pwd): {}", encrypt(pwd));

		if (!encrypt(pwd).equals(walletCoin.getCostpwd()) || !encrypt(pwd).equals(userCoin.getCostpwd())) {
			return Resp.fail("密码错误");
		}

		return Resp.ok();
	}

	public static String encrypt(String strSrc) {
		try {
			char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
					'9', 'a', 'b', 'c', 'd', 'e', 'f' };
			byte[] bytes = strSrc.getBytes();
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			bytes = md.digest();
			int j = bytes.length;
			char[] chars = new char[j * 2];
			int k = 0;
			for (int i = 0; i < bytes.length; i++) {
				byte b = bytes[i];
				chars[k++] = hexChars[b >>> 4 & 0xf];
				chars[k++] = hexChars[b & 0xf];
			}
			return new String(chars);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("MD5加密出错！！+" + e);
		}
	}
}
