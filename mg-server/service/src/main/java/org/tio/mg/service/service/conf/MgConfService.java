
package org.tio.mg.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.conf.Conf;
import org.tio.mg.service.model.conf.ConfDev;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 配置信息加载
 * 
 * @author xufei
 * 2020年5月29日 下午3:45:28
 */
public class MgConfService {
	private static Logger				log			= LoggerFactory.getLogger(MgConfService.class);
	public static final MgConfService		me			= new MgConfService();
	final static Conf					confDao		= new Conf().dao();
	final static ConfDev				confDevDao	= new ConfDev().dao();
	/**
	 * key: name, value: value
	 */
	private static Map<String, String>	cacheData	= null;

	/**
	 * 
	 */
	public static void clearCache() {
		synchronized (MgConfService.class) {
			cacheData = null;
		}
	}

	private static void loadData() {
		//		clearCache();

		Map<String, String> tempCache = new HashMap<>();
		List<Conf> list = null;
		try {
			list = confDao.find("select * from conf");
			if (list == null) {
				list = new ArrayList<>();
			}
			List<ConfDev> list2 = confDevDao.find("select * from conf_dev");

			if (list2 != null) {
				for (ConfDev item : list2) {
					Conf conf = new Conf();
					conf.setName(item.getName());
					conf.setValue(item.getValue());
					conf.setRemark(item.getRemark());
					list.add(conf);
				}
			}

			if (list.size() == 0) {
				return;
			}

			for (Conf conf : list) {
				tempCache.put(conf.getName(), conf.getValue());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			cacheData = tempCache;
		}
	}

	/**
	 * 根据参数名字获取参数值
	 * @param name
	 * @param defaultValue 默认值
	 * @return
	 */
	public static String getString(String name, String defaultValue) {
		if (cacheData == null) {
			try {
				LockUtils.runWriteOrWaitRead(MgConfService.class.getName(), MgConfService.class, () -> {
//					@Override
//					public void read() {
//					}

//					@Override
//					public void write() {
						if (cacheData == null) {
							loadData();
						}
//					}
				});
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
		String value = cacheData.get(name);
		if (!StrUtil.isBlank(value)) {
			return value;
		} else {
			return defaultValue;
		}
	}

	/**
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static Integer getInt(String name, Integer defaultValue) {
		String value = getString(name, null);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}

	/**
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static Short getShort(String name, Short defaultValue) {
		String value = getString(name, null);
		if (value == null) {
			return defaultValue;
		}
		return Short.parseShort(value);
	}

	/**
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 * 
	 */
	public static Float getFloat(String name, Float defaultValue) {
		String value = getString(name, null);
		if (value == null) {
			return defaultValue;
		}
		return Float.parseFloat(value);
	}

	/**
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 * 
	 */
	public static Double getDouble(String name, Double defaultValue) {
		String value = getString(name, null);
		if (value == null) {
			return defaultValue;
		}
		return Double.parseDouble(value);
	}

	/**
	 * 
	 * @param name
	 * @param defaultValue
	 * @param trueValue
	 * @return
	 */
	public static Boolean getBoolean(String name, String defaultValue, String trueValue) {
		String value = getString(name, defaultValue);
		return value.equalsIgnoreCase(trueValue);
	}

	/**
	 * 值为true、TRUE的返回true
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static Boolean getBoolean(String name, String defaultValue) {
		String value = getString(name, defaultValue);
		return value.equalsIgnoreCase("true");
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @return
	 * @author tanyaowu
	 */
	public static Long getLong(String name, Long defaultValue) {
		String value = getString(name, null);
		if (value == null) {
			return defaultValue;
		}
		return Long.parseLong(value);
	}
	
	
	/**
	 * 配置列表
	 * @param searchkey
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午4:21:19
	 */
	public Ret list(String searchkey,Short type) {
		if(StrUtil.isBlank(searchkey) && type == null) {
			return RetUtils.okList(confDao.find("select * from conf order by name"));
		}
		Kv params = Kv.create();
		if(type != null) {
			params.set("type",type);
		}
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey","%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.conflist", params);
		return RetUtils.okList(confDao.find(sqlPara));
	}
	
	/**
	 * 新增配置项
	 * @param conf
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午4:32:00
	 */
	public Ret add(Conf conf) {
		if(StrUtil.isBlank(conf.getName())) {
			return RetUtils.failMsg("参数名不能为空");
		}
		if(StrUtil.isBlank(conf.getValue())) {
			return RetUtils.failMsg("参数值不能为空");
		}
		if(StrUtil.isBlank(conf.getTitle())) {
			return RetUtils.failMsg("配置项名称不能为空");
		}
		String exist = getString(conf.getName(), null);
		if(StrUtil.isNotBlank(exist)) {
			return RetUtils.failMsg("参数名已存在");
		}
		boolean save = conf.save();
		if(!save) {
			return RetUtils.failOper();
		}
		clearCache();
		return RetUtils.okOper();
	}
	
	/**
	 * 修改配置项
	 * @param dict
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午2:44:18
	 */
	public Ret update(Conf conf) {
		if(StrUtil.isBlank(conf.getName())) {
			return RetUtils.failMsg("参数名不能为空");
		}
		boolean update = conf.update();
		if(!update) {
			return RetUtils.failOper();
		}
		clearCache();
		return RetUtils.okOper();
	}
}
