
package org.tio.mg.service.utils;

import org.tio.mg.service.service.base.SensitiveWordsService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 */
public class CommonUtils {

	/**
	 * 
	 */
	public CommonUtils() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * 
	 * @param name
	 * @param prefix
	 * @return
	 */
	public static Resp checkGroupName(String name, String prefix) {
		if (StrUtil.isBlank(name)) {
			return Resp.fail(prefix + "不允许为空");
		}
		if (name.length() > 32) {
			return Resp.fail(prefix + "太长了");
		}

		if (name.contains("@") || name.contains("＠")) {
			return Resp.fail(prefix + "不能包含 \"@\" 字符");
		}

		if (name.contains(" ") || name.contains("　")) {
			return Resp.fail(prefix + "不能包含空格");
		}

		if (StrUtil.containsAny(name, Const.SPECIAL_CHARACTER)) {
			return Resp.fail(prefix + "不能包含如下特殊字符：" + Json.toJson(Const.SPECIAL_CHARACTER));
		}

		if (SensitiveWordsService.isMatch(name)) {
			return Resp.fail(prefix + "存在敏感信息，请重新输入");
		}

		return Resp.ok();
	}
}
