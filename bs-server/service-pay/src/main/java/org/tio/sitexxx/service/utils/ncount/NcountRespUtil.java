
package org.tio.sitexxx.service.utils.ncount;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.map.MapUtil;

/**
 * 新生支付vo工具类
 * @author lixinji
 * 2021年3月4日 上午9:45:49
 */
public class NcountRespUtil {

	/**
	 * 开户
	 * @param resp
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午3:31:21
	 */
	public static Map<String, Object> openUser(Map<String, Object> resp) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("walletid", MapUtil.getStr(resp, "userId"));
		return result;
	}

	/**
	 * 绑卡
	 * @param resp
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午3:36:24
	 */
	public static Map<String, Object> bindBankCard(Map<String, Object> resp) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("reqid", MapUtil.getStr(resp, "merOrderId"));
		result.put("confirmreqid", MapUtil.getStr(resp, "ncountOrderId"));
		return result;
	}

	//	result.put("", MapUtil.getStr(resp, ""));

}
