
package org.tio.sitexxx.service.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.Demo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * @author tanyaowu 
 * 2018年02月27日 22:03:32
 */
public class DemoService {
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(DemoService.class);
	public static final DemoService	me	= new DemoService();
	final Demo						dao	= new Demo().dao();

	/**
	 * 保存数据
	 * @param demo
	 * @return
	 */
	public boolean save(Demo demo) {
		return demo.save();
	}

	/**
	 * 修改数据
	 * @param demo
	 * @return
	 */
	public boolean update(Demo demo) {
		return demo.update();
	}

	/**
	 * 不带缓存的查询
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<Demo> page(int pageNumber, int pageSize) {
		/**
		 * 此处为了演示的简洁，把sql写在了java中，更合适的做法是把sql写在demo.sql文件中(注意要在_all.sql中引入demo.sql)
		 */
		Page<Demo> page = Demo.dao.paginate(pageNumber, pageSize, "select *", "from demo where id > ?", 0);
		return page;
	}

	/**
	 * 带缓存的查询
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<Demo> pageWithCache(int pageNumber, int pageSize) {
		ICache cache = Caches.getCache(CacheConfig.DEMO_CACHE);
		String key = pageNumber + "_" + pageSize;
		boolean putTempToCacheIfNull = false;
		Page<Demo> page = CacheUtils.get(cache, key, putTempToCacheIfNull, new FirsthandCreater<Page<Demo>>() {
			@Override
			public Page<Demo> create() {
				/**
				 * 此处为了演示的简洁，把sql写在了java中，更合适的做法是把sql写在demo.sql文件中(注意要在_all.sql中引入demo.sql)
				 */
				Page<Demo> page = Demo.dao.paginate(pageNumber, pageSize, "select *", "from demo where id > ?", 0);
				return page;
			}
		});
		return page;
	}

	/**
	 * 删除数据
	 * @param demo
	 * @return
	 */
	public boolean deleteById(Demo demo) {
		return Db.deleteById("demo", 25);
	}
}
