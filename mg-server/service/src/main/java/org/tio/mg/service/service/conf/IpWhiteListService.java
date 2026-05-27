
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
import org.tio.mg.service.model.conf.IpWhiteList;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author tanyaowu
 *
 */
public class IpWhiteListService {
    private static Logger log = LoggerFactory.getLogger(IpWhiteListService.class);
    public static final IpWhiteListService me = new IpWhiteListService();
    final static IpWhiteList dao = new IpWhiteList().dao();
    /**
     * key: name, value: IpWhiteList
     */
    private static Map<String, IpWhiteList> cacheData = null;

    /**
     * 
     * @return
     * @author tanyaowu
     */
    public Map<String, IpWhiteList> getAll() {
	return getCacheData();
    }

    public boolean save(String ip, String remark) {
	IpWhiteList ipWhiteList = new IpWhiteList();
	ipWhiteList.setIp(ip);
	ipWhiteList.setStatus((short) 1);
	ipWhiteList.setRemark(remark);
	boolean ret = ipWhiteList.save();

	clearCache();
	RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);
	TopicVo topicVo = new TopicVo();
	topicVo.setType(TopicVo.Type.CLEAR_IP_WHITE_LIST);
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
	int ret = Db.use(Const.Db.TIO_SITE_CONF).update("update ip_white_list set status = ?, remark = ? where ip = ?",
		2, remark, ip);

	clearCache();
	RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);
	TopicVo topicVo = new TopicVo();
	topicVo.setType(TopicVo.Type.CLEAR_IP_WHITE_LIST);
	topic.publishAsync(topicVo);
	return ret;
    }

    /**
     * 
     */
    public static void clearCache() {
	try {
	    LockUtils.runWriteOrWaitRead(IpWhiteListService.class.getName(), IpWhiteListService.class, () -> {
		cacheData = null;
	    });
	} catch (Exception e) {
	    log.error("", e);
	}

    }

    private static void loadData() {
	try {
	    LockUtils.runWriteOrWaitRead(IpWhiteListService.class.getName(), IpWhiteListService.class, () -> {
		if (cacheData == null) {
		    Map<String, IpWhiteList> cacheData1 = new HashMap<>();
		    List<IpWhiteList> list = null;
		    try {
			list = dao.find("select * from ip_white_list where status=1");
			if (list == null) {
			    list = new ArrayList<>();
			    return;
			}

			for (IpWhiteList conf : list) {
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
    public static boolean isWhiteIp(String ip) {
	if (StrUtil.isBlank(ip)) {
	    return false;
	}
	return getCacheData().containsKey(ip);
    }

    public static Map<String, IpWhiteList> getCacheData() {
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
