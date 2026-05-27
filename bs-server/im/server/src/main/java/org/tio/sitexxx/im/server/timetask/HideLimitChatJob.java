
package org.tio.sitexxx.im.server.timetask;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.utils.jfinal.P;
import org.tio.utils.quartz.AbstractJobWithLog;

import cn.hutool.core.collection.CollectionUtil;

/**
 * 处理超过会话列表最长的用户列表
 * @author lixinji
 * 2020年7月3日 下午2:56:29
 */
public class HideLimitChatJob extends AbstractJobWithLog {

	private static Logger log = LoggerFactory.getLogger(HideLimitChatJob.class);

	private static boolean isRunning = false;

	@Override
	public void run(JobExecutionContext context) throws Exception {
		boolean allowExecute = P.getBoolean("quartz.open.flag", false);
		if (allowExecute && isStart()) {
			try {
				Integer limit = 4000;
				List<Record> uidsList = ChatService.me.hidItemJobUid(limit);
				//获取处理用户
				if(CollectionUtil.isNotEmpty(uidsList)) {
					for(Record record : uidsList) {
						Integer uid = record.getInt("uid");
						Integer num = record.getInt("num");
						double pagetotal = Math.ceil(new Double(num)/limit);
						if(pagetotal > 1) {
							for(Integer i = 1; i < pagetotal; i++) {
								List<Record> hideChats = ChatService.me.hidItemJob(uid, limit, limit);
								if(CollectionUtil.isNotEmpty(hideChats)) {
									for(Record chat : hideChats) {
										Long id = chat.getLong("id");
										ChatService.me.hideChatItems(id, uid);
									}
									
								} else {
									break;
								}
							}
						}
					}
				}
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
		synchronized (HideLimitChatJob.class) {
			ret = isRunning ? false : (isRunning = true);
		}
		return ret;
	}

	/**
	 * 计划任务结束处理
	 * @author lixinji
	 */
	private static void end() {
		synchronized (HideLimitChatJob.class) {
			isRunning = false;
		}
	}

}
