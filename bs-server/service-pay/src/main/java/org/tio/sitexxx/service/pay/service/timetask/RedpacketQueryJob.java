
package org.tio.sitexxx.service.pay.service.timetask;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.init.PayInit;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.AbstractJobWithLog;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;

/**
 * 红包查询记录
 * 1、处理超时红包处理
 * 2、处理快捷支付未处理红包
 * @author lixinji
 * 2020年7月3日 下午2:56:29
 */
public class RedpacketQueryJob extends AbstractJobWithLog {

	private static Logger log = LoggerFactory.getLogger(RedpacketQueryJob.class);

	private static boolean isRunning = false;

	private static BasePayService payService = PayInit.payService;

	private FastDateFormat format = DatePattern.CHINESE_DATE_TIME_FORMAT;

	@Override
	public void run(JobExecutionContext context) throws Exception {
		boolean allowExecute = P.getBoolean("quartz.open.flag", false);
		if (allowExecute && isStart()) {
			try {
				log.debug("{}:开始处理红包定时任务--------------->", DateUtil.format(new DateTime(), format));
				payService.redpacketJob();
				log.debug("{}:结束处理红包定时任务<---------------", DateUtil.format(new DateTime(), format));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				end();
			}
		}

	}

	/**
	 * 判断计划任务是否可以开始:true：可以运用；false：不可以
	 * @return
	 * @author lixinji
	 */
	private static boolean isStart() {
		boolean ret = false;
		synchronized (RedpacketQueryJob.class) {
			ret = isRunning ? false : (isRunning = true);
		}
		return ret;
	}

	/**
	 * 计划任务结束处理
	 * @author lixinji
	 */
	private static void end() {
		synchronized (RedpacketQueryJob.class) {
			isRunning = false;
		}
	}
}
