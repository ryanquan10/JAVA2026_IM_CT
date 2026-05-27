
package org.tio.sitexxx.service.vo.ncount.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.tio.sitexxx.service.vo.Const;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

/**
 * 新生支付交易报文
 * 
 * @author lixinji
 * 2021年3月1日 下午4:22:12
 */
public abstract class NCountBaseVo implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String	VERSION			= "version";
	public static final String	TRAN_CODE		= "tranCode";
	public static final String	MER_ID			= "merId";
	public static final String	MER_ORDER_ID	= "merOrderId";
	public static final String	CHARSET			= "charset";
	public static final String	SIGN_TYPE		= "signType";
	public static final String	SIGN_VALUE		= "signValue";
	public static final String	MER_ATTACH		= "merAttach";
	public static final String	SUBMIT_TIME		= "submitTime";
	public static final String	MSG_CIPHER_TEXT	= "msgCiphertext";
	public static final String	RESULT_CODE		= "resultCode";
	public static final String	ERROR_CODE		= "errorCode";
	public static final String	ERROR_MSG		= "errorMsg";

	// 暂定公共的报文字段
	protected String	tranCode;
	protected String	version;
	protected String	merId;
	protected String	merOrderId;
	protected String	signType;
	protected String	signValue;
	protected String	msgCiphertext;
	protected String	charset;
	protected String	merAttach;
	protected String	submitTime;

	//提交地址
	protected String submitUrl;

	/**
	 * 获取公共返回参数<br>
	 * Map中的key必须与返回XML文件中的field的name值一致<br>
	 * 子类可选择性重写
	 * @return
	 */
	@SuppressWarnings("static-access")
	public Map<String, Object> getCommonRespParams() {
		Map<String, Object> commonParams = new HashMap<String, Object>();
		commonParams.put(this.VERSION, this.version);
		commonParams.put(this.TRAN_CODE, this.tranCode);
		commonParams.put(this.MER_ORDER_ID, this.merOrderId);
		commonParams.put(this.MER_ID, this.merId);
		commonParams.put(this.MER_ATTACH, this.merAttach);
		commonParams.put(this.CHARSET, this.charset);
		commonParams.put(this.SIGN_TYPE, this.signType);
		return commonParams;
	}

	/**
	 * 获取共用请求的签名字段
	 * @author wgy
	 * @date 2018年8月10日 上午10:26:54
	 * @description 一句话描述功能
	 * @return Map<String,Object>
	 */
	@SuppressWarnings("static-access")
	public Map<String, Object> getCommonReqSignParams() {
		Map<String, Object> commonParams = new HashMap<String, Object>();
		commonParams.put(this.TRAN_CODE, this.tranCode);
		commonParams.put(this.VERSION, this.version);
		commonParams.put(this.MER_ID, this.merId);
		commonParams.put(this.MER_ORDER_ID, this.merOrderId);
		commonParams.put(this.SUBMIT_TIME, this.submitTime);
		commonParams.put(this.MSG_CIPHER_TEXT, this.msgCiphertext);
		commonParams.put(this.SIGN_TYPE, this.signType);
		return commonParams;
	}

	/**
	 * 获取加密json串明文
	 * @author wgy
	 * @date 2018年8月27日 下午6:08:46
	 * @description 一句话描述功能
	 * @return String
	 */
	public abstract String getEncryptJsonStr();

	/**
	 * 获取验签字段
	 * @return
	 */
	public abstract String getVerifyJsonStr();

	/**
	 * 获取提交字段
	 * @return
	 */
	public abstract String getSubmitJsonStr();

	public void initCommonParams(String tranCode, Integer uid) {
		this.version = "1.0";
		this.tranCode = tranCode;
		this.merId = Const.WALLET_MERCHANTID;
		this.merOrderId = tranCode + "_" + uid + "_" + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");
		this.submitTime = DateUtil.format(new Date(), "yyyyMMddHHmmss");
		this.signType = "1";
		this.merAttach = "";
		this.charset = "UTF-8";
	}

	public String getCommonSignStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(VERSION);
		sb.append("=[");
		sb.append(getVersion());
		sb.append("]");

		sb.append(TRAN_CODE);
		sb.append("=[");
		sb.append(getTranCode());
		sb.append("]");

		sb.append(MER_ID);
		sb.append("=[");
		sb.append(getMerId());
		sb.append("]");

		sb.append(MER_ORDER_ID);
		sb.append("=[");
		sb.append(getMerOrderId());
		sb.append("]");

		sb.append(SUBMIT_TIME);
		sb.append("=[");
		sb.append(getSubmitTime());
		sb.append("]");

		sb.append(MSG_CIPHER_TEXT);
		sb.append("=[");
		sb.append(getMsgCiphertext());
		sb.append("]");

		sb.append(SIGN_TYPE);
		sb.append("=[");
		sb.append(getSignType());
		sb.append("]");

		return sb.toString();
	}

	public String getValueByReflet(Object model, String paraName) {
		// 返回值
		String value = "";
		try {
			// 获取属性值
			Field[] fields = model.getClass().getDeclaredFields();

			for (Field field : fields) {
				field.setAccessible(true);

				if (field.getName().equals(paraName)) {
					value = (String) field.get(model);

					break;
				}
			}
		} catch (Exception e) {

		}
		if (paraName.equals("userName")) {
			System.out.println(value);
		}
		return value;
	}

	public String getJsonStr(Object model, String[] strArr) {
		JSONObject json = new JSONObject(true);
		for (String file : strArr) {
			json.put(file, getValueByReflet(model, file));
		}
		return json.toJSONString();
	}

	public String getTranCode() {
		return tranCode;
	}

	public void setTranCode(String tranCode) {
		this.tranCode = tranCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getMerOrderId() {
		return merOrderId;
	}

	public void setMerOrderId(String merOrderId) {
		this.merOrderId = merOrderId;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getSignValue() {
		return signValue;
	}

	public void setSignValue(String signValue) {
		this.signValue = signValue;
	}

	public String getMsgCiphertext() {
		return msgCiphertext;
	}

	public void setMsgCiphertext(String msgCiphertext) {
		this.msgCiphertext = msgCiphertext;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getMerAttach() {
		return merAttach;
	}

	public void setMerAttach(String merAttach) {
		this.merAttach = merAttach;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public String getSubmitUrl() {
		return submitUrl;
	}

	public void setSubmitUrl(String submitUrl) {
		this.submitUrl = submitUrl;
	}
}
