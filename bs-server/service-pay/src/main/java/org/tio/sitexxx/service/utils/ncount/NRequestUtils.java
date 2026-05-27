
package org.tio.sitexxx.service.utils.ncount;

import java.security.PrivateKey;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.pay.base.BasePayResp;
import org.tio.sitexxx.service.pay.impl.ncount.PayNcountFormatApi;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.ncount.base.NCountBaseVo;
import org.tio.utils.json.Json;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 新账通请求工具
 * 客户可以根据自己的业务需要进行相关逻辑调整
 * @author lixinji
 * 2021年3月2日 上午9:35:05
 */
public class NRequestUtils {

	private static Logger log = LoggerFactory.getLogger(PayNcountFormatApi.class);

	/**
	 * @param baseVo 根据交易码获取不同的提交表单信息，需要完善不同接口的参数值（需客户自己完善具体的参数值）
	 * @author lixinji
	 * 2021年3月6日 上午5:10:32
	 */
	public static Ret nRequest(NCountBaseVo base) {
		try {

			//测试商户ID:
			//1，加载商户私钥
			PrivateKey privateKey = NcountUtils.loadPrivateKey();

			//2，不同接口交易码不同
			String tranCode = base.getTranCode();

			//4，获取加密字段的json串（明文）
			String encryptStr = base.getEncryptJsonStr();

			//5，使用平台公钥进行rsa加密
			String msgCipherText = Base64Util.encode(NcountSign.encryptByPublicKey(encryptStr.getBytes("utf-8"), NcountKey.NCOUNT_PUBLIC_KEY)).replace("\n", "").replace("\r", "");

			base.setMsgCiphertext(msgCipherText);

			//6，使用商户私钥进行签名
			String signValue = Base64Util.encode(NcountSign.sign(privateKey, base.getCommonSignStr())).replace("\n", "").replace("\r", "");

			base.setSignValue(signValue);

			//7，构建提交至平台的请求参数
			Map<String, Object> reqMap = base.getCommonReqSignParams();
			reqMap.put("signValue", base.getSignValue());
			reqMap.put("charset", "1");
			log.debug("{}-提交参数:{}", tranCode, Json.toJson(reqMap));
			String response = null;
			if (base.getSubmitUrl().startsWith("https://")) {
				NHttpsTransport httpsTransport = new NHttpsTransport();
				httpsTransport.setSendEncoding("UTF-8");
				httpsTransport.setUrl(base.getSubmitUrl());
				response = (String) httpsTransport.submit(reqMap);
			} else {
				NHttpClientUtils httpClientUtils = new NHttpClientUtils();
				response = httpClientUtils.submit(reqMap, base.getSubmitUrl());
			}
			//8，返回参数转map
			Map<String, Object> responseMap = NJsonUtils.jsonToMap(response);
			log.debug("{}-响应参数:{}", tranCode, Json.toJson(responseMap));
			//9，根据交易码构建不同接口验签明文串
			String verifyFieldStr = NcountVoUtil.getVerifyDataByTranCode(tranCode, responseMap);

			//10，使用平台公钥进行验签
			boolean verifyResult = NcountSign.verify(verifyFieldStr, responseMap.get("signValue").toString());
			if (!verifyResult) {
				log.error("{}-验签不正确", tranCode);
				return RetUtils.failMsg("验签不正确");
			}
			return RetUtils.okData(responseMap);
		} catch (Exception e) {
			log.error("", e);
		}
		return RetUtils.failMsg("请求异常");
	}

	/**
	 * 回调
	 * @param request
	 * @return
	 * @author lixinji
	 * 2021年3月10日 下午12:11:52
	 */
	public static Ret callback(HttpRequest request, String tranCode) {
		try {
			String body = new String(request.getBody(), "utf-8");
			log.debug("----->callback-body:{}", body);
			//8，返回参数转map
			Map<String, Object> reqMap = request.getParam();
			log.debug("解析前得map:{}", Json.toJson(reqMap));

			//9，根据交易码构建不同接口验签明文串
			String verifyFieldStr = NcountVoUtil.getVerifyDataByTranCode(tranCode, reqMap);
			log.debug("----->callback:验签明文字符串为：" + verifyFieldStr);
			//10，使用平台公钥进行验签
			boolean verifyResult = NcountSign.verify(verifyFieldStr, reqMap.get("signValue").toString());
			if (!verifyResult) {
				log.debug("----->验签不正确");
				return RetUtils.failMsg("验签不正确");
			}
			return RetUtils.okData(reqMap);
		} catch (Exception e) {
			log.error("", e);
		}
		return RetUtils.failMsg("请求解析异常");
	}

	/**
	 * 响应码
	 * @author lixinji
	 * 2021年3月10日 下午12:10:33
	 */
	public static interface ResultCode {

		/**
		 * 成功
		 */
		String SUCCESS = "0000";

		/**
		 * 失败
		 */
		String FAIL = "4444";

		/**
		 * 交易进行中
		 */
		String RUNNING = "9999";

		/**
		 * 无效交易
		 */
		String INVALID = "7777";
	}

	/**
	 * 响应处理
	 * @param respMap
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午4:57:32
	 */
	public static BasePayResp checkRet(Ret ret) {
		if (ret.isFail()) {
			BasePayResp resp = BasePayResp.fail(RetUtils.getRetMsg(ret), "-1");
			log.error("系统异常，{}", RetUtils.getRetMsg(ret));
			return resp;
		}
		Map<String, Object> respMap = RetUtils.getOkTData(ret);
		String resultCode = MapUtil.getStr(respMap, "resultCode");
		String tranCode = MapUtil.getStr(respMap, "tranCode");
		String errorCode = MapUtil.getStr(respMap, "errorCode");
		String errorMsg = MapUtil.getStr(respMap, "errorMsg");
		if (StrUtil.isBlank(tranCode)) {
			BasePayResp resp = BasePayResp.fail("业务类型为空", "-1");
			log.error("业务类型为空：{}", Json.toJson(respMap));
			return resp;
		}
		if (StrUtil.isBlank(resultCode)) {
			BasePayResp resp = BasePayResp.fail("响应码不存在", "-1");
			log.error("响应码不存在：{}", Json.toJson(respMap));
			return resp;
		}
		BasePayResp resp = new BasePayResp();
		resp.setOk(true);
		resp.setMerCode(resultCode);
		resp.setResp(respMap);
		if (Objects.equals(resultCode, ResultCode.SUCCESS)) {
			resp.setMsg("");
			resp.setMerBizCode("");
		} else if (Objects.equals("100E0240", errorCode)) {
			resp.setMsg("用户已开户");
			resp.setMerBizCode("");
		} else if (Objects.equals(resultCode, ResultCode.RUNNING)) {
			resp.setMsg("");
			resp.setMerBizCode("");
			;
		} else {
			resp.setMsg(errorMsg);
			resp.setMerBizCode(errorCode);
		}
		return resp;
	}

	/**
	 * 响应格式化处理
	 * @param resp
	 * @param result
	 * @author lixinji
	 * 2021年3月16日 下午6:01:16
	 */
	public static void respFormat(Map<String, Object> resp, Map<String, Object> result) {
		String resultCode = MapUtil.getStr(resp, "resultCode");
		String errorCode = MapUtil.getStr(resp, "errorCode");
		if (Objects.equals(resultCode, ResultCode.FAIL) || Objects.equals(resultCode, ResultCode.INVALID)) {
			if (StrUtil.isNotBlank(errorCode)) {
				if (Objects.equals("100E0240", errorCode)) { //重复开户
					result.put("merstatus", "SUCCESS");
				} else {
					result.put("merstatus", errorCode);
				}
			} else {
				result.put("merstatus", resultCode);
			}
		} else {
			result.put("merstatus", "SUCCESS");
		}
	}
}
