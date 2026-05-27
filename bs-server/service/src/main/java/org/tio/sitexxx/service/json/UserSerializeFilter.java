
package org.tio.sitexxx.service.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * @author tanyaowu
 * 2016年8月17日 上午10:49:33
 */
public class UserSerializeFilter implements PropertyFilter {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserSerializeFilter.class);

	public static final UserSerializeFilter me = new UserSerializeFilter();

	private UserSerializeFilter() {
	}

	@Override
	public boolean apply(Object object, String name, Object value) {
		if (object == null) {
			return false;
		}

		if ("pwd".equals(name)) {
			return false;
		}
		if ("emailpwd".equals(name)) {
			return false;
		}
		if ("paypwd".equals(name)) {
			return false;
		}
		if ("phonepwd".equals(name)) {
			return false;
		}
		return true;
	}
}
