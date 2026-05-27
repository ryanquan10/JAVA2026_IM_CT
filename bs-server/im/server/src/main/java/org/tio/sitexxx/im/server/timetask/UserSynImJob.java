
package org.tio.sitexxx.im.server.timetask;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.AbstractJobWithLog;

/**
 * 同步用户信息
 * @author lixinji
 * 2020年7月3日 下午2:56:29
 */
public class UserSynImJob extends AbstractJobWithLog {

	private static Logger log = LoggerFactory.getLogger(UserSynImJob.class);

	private static boolean isRunning = false;

	@Override
	public void run(JobExecutionContext context) throws Exception {
		boolean allowExecute = P.getBoolean("quartz.open.flag", false);
		if (allowExecute && isStart()) {
			try {
				WxChatApi.synUserInfoToIm();
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
		synchronized (UserSynImJob.class) {
			ret = isRunning ? false : (isRunning = true);
		}
		return ret;
	}

	/**
	 * 计划任务结束处理
	 * @author lixinji
	 */
	private static void end() {
		synchronized (UserSynImJob.class) {
			isRunning = false;
		}
	}

}
