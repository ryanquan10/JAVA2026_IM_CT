
package org.tio.mg.service.service.base;

import java.util.Date;

import org.lionsoul.ip2region.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.ip2region.Ip2Region;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.mg.MgIpInfo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年10月25日 下午5:59:49
 */
public class IpInfoService {
	public static final IpInfoService ME = new IpInfoService();
	private static Logger log = LoggerFactory.getLogger(IpInfoService.class);

	/**
	 * 
	 * @author tanyaowu
	 */
	public IpInfoService() {
	}

	public IpInfo getById(Integer id) {
		if (id == null) {
			return null;
		}
		ICache cache = Caches.getCache(CacheConfig.ID_IPINFO);
		String key = id + "";

		IpInfo ipInfo1 = CacheUtils.get(cache, key, false, new FirsthandCreater<IpInfo>() {
			@Override
			public IpInfo create() {
				return IpInfo.dao.findFirst("select * from ip_info where id=?", id);
			}
		});

		return ipInfo1;
	}

	/**
	 * 会先检查缓存和数据库中是不是已经有了各字段值都一样的ip，如果有，则用原来的记录，省得插入冗余的记录
	 * @param ipInfo
	 * @return
	 * @author tanyaowu
	 */
	public IpInfo save(IpInfo ipInfo) {
		ICache cache = Caches.getCache(CacheConfig.IP_IPINFO);

		String key = ipInfo.getIp();

		IpInfo ipInfo1 = CacheUtils.get(cache, key, false, new FirsthandCreater<IpInfo>() {
			@Override
			public IpInfo create() {
				return IpInfo.dao.findFirst("select * from ip_info where ip=? and country=? and area=? and province=? and city=? and operator=?", key, ipInfo.getCountry(),
				        ipInfo.getArea(), ipInfo.getProvince(), ipInfo.getCity(), ipInfo.getOperator());
			}
		});

		if (ipInfo1 == null || ipInfo1.getId() == null) {
			ipInfo.save();
			cache.put(key, ipInfo);
			return ipInfo;
		} else {
			return ipInfo1;
		}
	}
	
	/**
	 * @param ipInfo
	 * @return
	 * @author xufei
	 * 2020年7月23日 上午10:47:56
	 */
	public MgIpInfo mgSave(MgIpInfo ipInfo) {
		String key = ipInfo.getIp();

		MgIpInfo old = MgIpInfo.dao.findFirst("select * from mg_ip_info where ip=? and country=? and area=? and province=? and city=? and operator=?", key, ipInfo.getCountry(),
		        ipInfo.getArea(), ipInfo.getProvince(), ipInfo.getCity(), ipInfo.getOperator());
		if(old != null) {
			return old;
		}
		ipInfo.save();
		return ipInfo;
	}

	/**
	 * 
	 * @param ip
	 * @return
	 * @author tanyaowu
	 */
	public IpInfo save(String ip) {
		IpInfo ipInfo = from(ip);
		return save(ipInfo);
	}
	
	
	/**
	 * @param ip
	 * @return
	 * @author xufei
	 * 2020年7月23日 上午10:48:27
	 */
	public MgIpInfo mgSave(String ip) {
		MgIpInfo ipInfo = fromMg(ip);
		return mgSave(ipInfo);
	}
	
	/**
	 * 根据ip创建Ip对象，可能会返回null
	 * @param ip
	 * @return
	 * @author: tanyaowu
	 */
	private static IpInfo from(String ip) {
		if (StrUtil.isBlank(ip)) {
			return null;
		}

		DataBlock dataBlock = Ip2Region.getDataBlock(ip);
		if (dataBlock == null) {
			log.warn("can not found DataBlock by ip {}", ip);
			return null;
		}
		String region = dataBlock.getRegion(); //中国|华北|北京市|北京市|联通 or 新加坡|0|0|0|0
		String[] fs = StrUtil.splitToArray(region, "|");
		if (fs.length != 5) {
			log.warn("ip[{}]'region[{}] is invalid", ip, region);
			return null;
		}

		int i = 0;
		IpInfo ipInfo = new IpInfo();
		ipInfo.setCountry("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setArea("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setProvince("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setCity("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setOperator("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setTime(new Date());
		ipInfo.setIp(ip);
		
		return ipInfo;
	}
	
	
	/**
	 * @param ip
	 * @return
	 * @author xufei
	 * 2020年7月23日 上午10:44:58
	 */
	private static MgIpInfo fromMg(String ip) {
		if (StrUtil.isBlank(ip)) {
			return null;
		}

		DataBlock dataBlock = Ip2Region.getDataBlock(ip);
		if (dataBlock == null) {
			log.warn("can not found DataBlock by ip {}", ip);
			return null;
		}
		String region = dataBlock.getRegion(); //中国|华北|北京市|北京市|联通 or 新加坡|0|0|0|0
		String[] fs = StrUtil.splitToArray(region, "|");
		if (fs.length != 5) {
			log.warn("ip[{}]'region[{}] is invalid", ip, region);
			return null;
		}

		int i = 0;
		MgIpInfo ipInfo = new MgIpInfo();
		ipInfo.setCountry("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setArea("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setProvince("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setCity("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setOperator("0".equals(fs[i++]) ? "" : fs[i - 1]);
		ipInfo.setTime(new Date());
		ipInfo.setIp(ip);
		
		return ipInfo;
	}

}
