
package org.tio.sitexxx.service.pay.service.timetask;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.pay.base.BasePayService;
import org.tio.sitexxx.service.pay.init.PayInit;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.AbstractJobWithLog;

/**
 * 充值查询记录
 * @author lixinji
 * 2020年7月3日 下午2:56:29
 */
public class RechargeQueryJob extends AbstractJobWithLog {

	private static Logger log = LoggerFactory.getLogger(RechargeQueryJob.class);

	private static boolean isRunning = false;

	private static BasePayService payService = PayInit.payService;

	@Override
	public void run(JobExecutionContext context) throws Exception {
		boolean allowExecute = P.getBoolean("quartz.open.flag", false);
		if (allowExecute && isStart()) {
			try {
				payService.rechargeJob();
				log.debug("充值查询定时任务处理结束");
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
		synchronized (RechargeQueryJob.class) {
			ret = isRunning ? false : (isRunning = true);
		}
		return ret;
	}

	/**
	 * 计划任务结束处理
	 * @author lixinji
	 */
	private static void end() {
		synchronized (RechargeQueryJob.class) {
			isRunning = false;
		}
	}

}
