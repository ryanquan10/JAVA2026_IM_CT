
package org.tio.mg.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.conf.IpBlackList;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author tanyaowu
 *
 */
public class IpBlackListService {
    private static Logger log = LoggerFactory.getLogger(IpBlackListService.class);
    public static final IpBlackListService me = new IpBlackListService();
    final static IpBlackList dao = new IpBlackList().dao();
    /**
     * key: name, value: IpBlackList
     */
    private static Map<String, IpBlackList> cacheData = null;

    /**
     * 
     * @return
     * @author tanyaowu
     */
    public Map<String, IpBlackList> getAll() {
	return getCacheData();
    }

    public boolean save(String ip, String remark) {
		IpBlackList ipBlackList = new IpBlackList();
		ipBlackList.setIp(ip);
		ipBlackList.setStatus((short) 1);
		ipBlackList.setRemark(remark);
		boolean ret = ipBlackList.save();

		clearCache();
		RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_IP_BLACK_LIST);
		topic.publishAsync(topicVo);
		return ret;
    }

    /**
     * 删除白名单
     * 
     * @param ip
     * @param remark
     * @return
     */
    public int delete(String ip, String remark) {
	int ret = Db.use(Const.Db.TIO_SITE_CONF).update("update ip_black_list set status = ?, remark = ? where ip = ?",
		2, remark, ip);

	clearCache();
	RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);
	TopicVo topicVo = new TopicVo();
	topicVo.setType(TopicVo.Type.CLEAR_IP_BLACK_LIST);
	topic.publishAsync(topicVo);
	return ret;
    }

    /**
     * 
     */
    public static void clearCache() {
	try {
	    LockUtils.runWriteOrWaitRead(IpBlackListService.class.getName(), IpBlackListService.class, () -> {
		cacheData = null;
	    });
	} catch (Exception e) {
	    log.error("", e);
	}

    }

    private static void loadData() {
	try {
	    LockUtils.runWriteOrWaitRead(IpBlackListService.class.getName(), IpBlackListService.class, () -> {
		if (cacheData == null) {
		    Map<String, IpBlackList> cacheData1 = new HashMap<>();
		    List<IpBlackList> list = null;
		    try {
			list = dao.find("select * from ip_black_list where status=1");
			if (list == null) {
			    list = new ArrayList<>();
			    return;
			}

			for (IpBlackList conf : list) {
			    cacheData1.put(conf.getIp(), conf);
			}
		    } catch (Exception e) {
			log.error(e.getMessage(), e);
		    } finally {
			cacheData = cacheData1;
		    }
		}
	    });
	} catch (Exception e) {
	    log.error("", e);
	}
    }

    /**
     * 是否是白名单ip
     * 
     * @param ip
     * @return true: 是白名单ip
     */
    public static boolean isBlackIp(String ip) {
	if (StrUtil.isBlank(ip)) {
	    return false;
	}
	return getCacheData().containsKey(ip);
    }

    public static Map<String, IpBlackList> getCacheData() {
	if (cacheData == null) {
	    try {
		loadData();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
	return cacheData;
    }

}
