
package org.tio.sitexxx.service.utils;

import java.util.Date;
import java.util.Objects;

import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * 周期工具类
 * @author lixinji
 */
public class PeriodUtils {

	/**
	 * 周期转换
	 * @param date
	 * @param periodType
	 * @return
	 * @author lixinji
	 */
	public static String dateToPeriodByType(Date date, short periodType) {
		if (periodType == Const.PeriodType.TOTAL) {
			return Const.PeriodType.TOTAL_PERIOD;
		}
		String periodFormat = "yyyyMMdd";
		if (Objects.equals(periodType, Const.PeriodType.YEAR)) {
			periodFormat = periodFormat.substring(0, 4);
		} else if (Objects.equals(periodType, Const.PeriodType.MONTH)) {
			periodFormat = periodFormat.substring(0, 6);
		} else if (Objects.equals(periodType, Const.PeriodType.WEEK)) {
			date = DateUtil.beginOfWeek(date);
		} else if (Objects.equals(periodType, Const.PeriodType.QUARTER)) {
			date = DateUtil.beginOfQuarter(date);
		} else if (Objects.equals(periodType, Const.PeriodType.HOUR)) {
			periodFormat = "HH";
		} else if (Objects.equals(periodType, Const.PeriodType.TIME)) {
			periodFormat = "HH:mm";
		}
		String period = DateUtil.format(date, periodFormat);
		if (Objects.equals(periodType, Const.PeriodType.WEEK)) {
			period += "W";
		} else if (Objects.equals(periodType, Const.PeriodType.QUARTER)) {
			period += "Q";
		}

		return period;
	}

	/**
	 * 根据周期转换时间
	 * @param period
	 * @return
	 * @author lixinji
	 */
	public static DateTime getDateByPeriod(String period) {
		int length = period.length();
		if (length == 5) {
			return new DateTime();
		}
		if (period.indexOf("W") >= 0) {
			period = period.substring(0, period.length() - 1);
		}
		if (period.indexOf("Q") >= 0) {
			period = period.substring(0, period.length() - 1);
		}
		if (period.length() == 4) {
			period = period + "0101";
		}
		if (period.length() == 6) {
			period = period + "01";
		}

		if (period.length() == 10) {
			period = period + "0000";
		}
		return DateUtil.parse(period);
	}

	/**
	 * 获取天数
	 * @param periodType
	 * @param num
	 * @return
	 */
	public static int getDayByPeriodNum(Short periodType, Integer num) {
		int result = num;
		switch (periodType) {
		case Const.PeriodType.DAY:
			break;
		case Const.PeriodType.WEEK:
			result = num * 7;
			break;
		case Const.PeriodType.MONTH:
			result = num * 30;
			break;
		case Const.PeriodType.QUARTER:
			result = num * 90;
			break;
		case Const.PeriodType.YEAR:
			result = num * 365;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 卡劵周期创建
	 * @param periodType
	 * @param num
	 * @param date
	 * @return
	 */
	/**
	 * @param periodType
	 * @param num
	 * @param date
	 * @return
	 */
	public static Date getCouponPeriod(Short periodType, Integer num, Date date) {
		if (num <= 0) {
			num = 1;
		}
		if (date == null) {
			date = new DateTime();
		}
		Date result = null;
		switch (periodType) {
		case Const.PeriodType.DAY:
			result = DateUtil.offsetDay(date, num);
			break;
		case Const.PeriodType.WEEK:
			result = DateUtil.offsetWeek(date, num);
			break;
		case Const.PeriodType.MONTH:
			result = DateUtil.offsetMonth(date, num);
			break;
		case Const.PeriodType.QUARTER:
			result = DateUtil.offsetMonth(date, num * 3);
			break;
		case Const.PeriodType.YEAR:
			result = DateUtil.offsetMonth(date, num * 12);
			break;
		default:
			result = DateUtil.offsetDay(date, num);
			break;
		}
		return result;
	}

	public static void main(String[] args) {
	}

}
