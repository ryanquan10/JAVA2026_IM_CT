/*
 * qupvt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ckhmqqwgs
 */
/*
 * qupvt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ckhmqqwgs
 * grantinfo
 */
package org.tio.clu.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.ReadWriteLockHandler;

/**
 *
 * @author tanyaowu
 *
 */
public class CommandStat {

    private static Logger log = LoggerFactory.getLogger(CommandStat.class);
    public final static Map<Command, CommandStat> COMMAND_STAT_MAP = new HashMap<>();

    public static CommandStat getCommandStat(Command command) {
	if (command == null) {
	    return null;
	}
	CommandStat ret = COMMAND_STAT_MAP.get(command);
	if (ret != null) {
	    return ret;
	}

	try {
	    LockUtils.runWriteOrWaitRead(CommandStat.class.getName(), CommandStat.class, new ReadWriteLockHandler() {
		@Override
		public void write() throws Exception {
		    CommandStat ret = COMMAND_STAT_MAP.get(command);
		    if (ret == null) {
			ret = new CommandStat();
			COMMAND_STAT_MAP.put(command, ret);
		    }
		}
	    });
	} catch (Exception e) {
	    log.error("", e);
	}
	ret = COMMAND_STAT_MAP.get(command);

	// synchronized (COMMAND_STAT_MAP) {
	// ret = COMMAND_STAT_MAP.get(command);
	// if (ret != null) {
	// return ret;
	// }
	// ret = new CommandStat();
	// COMMAND_STAT_MAP.put(command, ret);
	// }
	return ret;
    }

    /**
     * @param args
     *
     * @author tanyaowu 2016年12月6日 下午5:32:31
     *
     */
    public static void main(String[] args) {
    }

    public final AtomicLong received = new AtomicLong();

    public final AtomicLong handled = new AtomicLong();

    public final AtomicLong sent = new AtomicLong();

    /**
     *
     *
     * @author tanyaowu 2016年12月6日 下午5:32:31
     *
     */
    public CommandStat() {
    }

    /**
     * @return the handledCount
     */
    public AtomicLong getHandled() {
	return handled;
    }

    /**
     * @return the receivedCount
     */
    public AtomicLong getReceived() {
	return received;
    }

    /**
     * @return the sentCount
     */
    public AtomicLong getSent() {
	return sent;
    }

}
