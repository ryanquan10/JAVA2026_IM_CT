
package org.tio.mg.service.timetask;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.service.mg.MgLoginStatService;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.AbstractJobWithLog;

import cn.hutool.core.date.DateTime;

/**
 * 后台登录统计
 * @author xufei
 * 2020年7月3日 下午2:56:29
 */
public class MgLoginStatJob extends AbstractJobWithLog {

	private static Logger log = LoggerFactory.getLogger(MgLoginStatJob.class);

	private static boolean isRunning = false;
	
	@Override
	public void run(JobExecutionContext context) throws Exception {
		boolean allowExecute = P.getBoolean("quartz.open.flag",false);
		if(allowExecute && isStart()) {
			try {
				log.info("开始-登录统计定时任务");
				DateTime dateTime = new DateTime();
				MgLoginStatService.ME.loginTimeStat(dateTime);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			} finally {
				end();
			}
		}
		
	}
	

	/**
	 * 判断计划任务是否可以开始:true：可以运用；false：不可以
	 * @return
	 * @author xufei
	 */
	private static boolean isStart() {
		boolean ret = false;
		synchronized (MgLoginStatJob.class) {
			ret = isRunning ? false : (isRunning = true); 
		}
		return ret;
	}
	
	/**
	 * 计划任务结束处理
	 * @author xufei
	 */
	private static void end() {
		synchronized (MgLoginStatJob.class) {
			isRunning = false;
		}
	}

}
