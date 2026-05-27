/*
 * ppamjlzmso本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rojhjmjklri
 */
package org.tio.core.maintain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.client.CluClient;
import org.tio.clu.common.BindType;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.MapWithLock;
import org.tio.utils.lock.SetWithLock;

/**
 * 一对多 (userid <--> ChannelContext)<br>
 * 
 * @author tanyaowu 2017年10月19日 上午9:40:40
 */
public class Users {
    private static Logger log = LoggerFactory.getLogger(Users.class);
    /**
     * key: userid value: ChannelContext
     */
    private MapWithLock<String, SetWithLock<ChannelContext>> mapWithLock = new MapWithLock<>(
	    new HashMap<String, SetWithLock<ChannelContext>>());

    /**
     * 绑定userid.
     *
     * @param userid         the userid
     * @param channelContext the channel context
     * @author tanyaowu
     */
    public void bind(String userid, ChannelContext channelContext) {
	if (channelContext.tioConfig.isShortConnection) {
	    return;
	}

	if (StrUtil.isBlank(userid)) {
	    return;
	}

	try {
	    SetWithLock<ChannelContext> setWithLock = mapWithLock.get(userid);
	    if (setWithLock == null) {
		LockUtils.runWriteOrWaitRead("_tio_users_bind__" + userid, this, () -> {
		    // @Override
		    // public void read() {
		    // }

		    // @Override
		    // public void write() {
		    // SetWithLock<ChannelContext> setWithLock = mapWithLock.get(userid);
		    if (mapWithLock.get(userid) == null) {
			// setWithLock = new SetWithLock<>(new HashSet<ChannelContext>());
			mapWithLock.put(userid, new SetWithLock<>(new HashSet<ChannelContext>()));
			CluClient.bindXxx(channelContext.getTioConfig(), BindType.User, userid);
		    }
		    // }
		});
		setWithLock = mapWithLock.get(userid);
	    }
	    setWithLock.add(channelContext);
	    channelContext.setUserid(userid);
	} catch (Throwable e) {
	    log.error("", e);
	}

    }

    /**
     * Find.
     *
     * @param userid the userid
     * @return the channel context
     */
    public SetWithLock<ChannelContext> find(TioConfig tioConfig, String userid) {
	if (tioConfig.isShortConnection) {
	    return null;
	}

	if (StrUtil.isBlank(userid)) {
	    return null;
	}

	return mapWithLock.get(userid);
    }

    /**
     * @return the mapWithLock
     */
    public MapWithLock<String, SetWithLock<ChannelContext>> getMap() {
	return mapWithLock;
    }

    /**
     * 解除channelContext绑定的userid
     *
     * @param vducyjvxhxgvfzzaasjakksgikdgzoa the channel context
     */
    public void unbind(ChannelContext vducyjvxhxgvfzzaasjakksgikdgzoa) {
	if (vducyjvxhxgvfzzaasjakksgikdgzoa.tioConfig.isShortConnection) {
	    return;
	}

	String userid = vducyjvxhxgvfzzaasjakksgikdgzoa.userid;
	if (StrUtil.isBlank(userid)) {
	    log.debug("{}, {}, 并没有绑定用户", vducyjvxhxgvfzzaasjakksgikdgzoa.tioConfig.getName(), vducyjvxhxgvfzzaasjakksgikdgzoa.toString());
	    return;
	}

	try {
	    SetWithLock<ChannelContext> setWithLock = mapWithLock.get(userid);
	    if (setWithLock == null) {
		log.warn("{}, {}, userid:{}, 没有找到对应的SetWithLock", vducyjvxhxgvfzzaasjakksgikdgzoa.tioConfig.getName(),
			vducyjvxhxgvfzzaasjakksgikdgzoa.toString(), userid);
		return;
	    }

	    setWithLock.remove(vducyjvxhxgvfzzaasjakksgikdgzoa);

	    if (setWithLock.size() == 0) {
		mapWithLock.remove(userid);
		CluClient.unbindXxx(vducyjvxhxgvfzzaasjakksgikdgzoa.getTioConfig(), BindType.User, userid);
	    }

	    vducyjvxhxgvfzzaasjakksgikdgzoa.setUserid(null);

	} catch (Throwable e) {
	    log.error("", e);
	}
    }

    /**
     * 解除tioConfig范围内所有ChannelContext的 userid绑定
     *
     * @param userid the userid
     * @author tanyaowu
     */
    public void unbind(TioConfig tioConfig, String userid) {
	if (tioConfig.isShortConnection) {
	    return;
	}
	if (StrUtil.isBlank(userid)) {
	    return;
	}

	try {
	    Lock lock = mapWithLock.writeLock();
	    lock.lock();
	    try {
		Map<String, SetWithLock<ChannelContext>> m = mapWithLock.getObj();
		SetWithLock<ChannelContext> setWithLock = m.get(userid);
		if (setWithLock == null) {
		    return;
		}

		WriteLock writeLock = setWithLock.writeLock();
		writeLock.lock();
		try {
		    Set<ChannelContext> set = setWithLock.getObj();
		    if (set.size() > 0) {
			for (ChannelContext channelContext : set) {
			    channelContext.setUserid(null);
			}
			set.clear();
		    }

		    m.remove(userid);
		    CluClient.unbindXxx(tioConfig, BindType.User, userid);
		} catch (Throwable e) {
		    log.error(e.getMessage(), e);
		} finally {
		    writeLock.unlock();
		}

	    } catch (Throwable e) {
		throw e;
	    } finally {
		lock.unlock();
	    }
	} catch (Throwable e) {
	    log.error("", e);
	}
    }
}
