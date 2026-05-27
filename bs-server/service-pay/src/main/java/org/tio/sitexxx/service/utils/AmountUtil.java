
package org.tio.sitexxx.service.utils;

import java.text.DecimalFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.StrUtil;

public class AmountUtil {

	private static Logger log = LoggerFactory.getLogger(AmountUtil.class);

	/**
	 * @param amount
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午1:48:13
	 */
	public static Double intAmountToDouble(String amount) {
		if (StrUtil.isBlank(amount)) {
			System.out.println("转化amount时为空-intAmountToDouble");
			log.error("转化amount时为空-intAmountToDouble");
			return 0d;
		}
		Double change = Double.parseDouble(amount) / 100;
		DecimalFormat df = new DecimalFormat("0.00");
		String s = df.format(change);
		Double formatChage = Double.parseDouble(s);
		if (!Objects.equals(change, formatChage)) {
			log.error("double转化有溢出：str:{},change:{},formatchage:{}", amount, change, formatChage);
		}
		return change;
	}

	/**
	 * @param amount
	 * @return
	 * @author lixinji
	 * 2021年3月10日 上午10:28:48
	 */
	public static int doubleAmountToInt(String amount) {
		if (StrUtil.isBlank(amount)) {
			System.out.println("转化amount时为空-doubleAmountToInt");
			log.error("转化amount时为空-doubleAmountToInt");
			return 0;
		}
		Double change = Double.parseDouble(amount) * 100;
		return change.intValue();
	}

}
