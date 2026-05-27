
package org.tio.mg.service.json;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * @author tanyaowu
 * 2016年8月17日 上午10:49:33
 */
public class IpInfoSerializeFilter implements PropertyFilter {
	public static final IpInfoSerializeFilter ME = new IpInfoSerializeFilter();

	private IpInfoSerializeFilter() {
	}

	@Override
	public boolean apply(Object object, String name, Object value) {
		if (object == null) {
			return false;
		}

		if ("id".equals(name)) {
			return false;
		}
		if ("time".equals(name)) {
			return false;
		}

		if ("ip".equals(name)) {
			return false;
		}

		return true;
	}
}
