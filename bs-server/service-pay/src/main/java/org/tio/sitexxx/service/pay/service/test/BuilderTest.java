
package org.tio.sitexxx.service.pay.service.test;

import java.lang.reflect.Field;

import com.alibaba.fastjson15.JSONObject;
import com.upay.sdk.SignPublisher;

public class BuilderTest {

	@SuppressWarnings("rawtypes")
	protected JSONObject assembleBuild() throws IllegalAccessException {
		Class clazz = this.getClass();
		JSONObject json = this.findField(clazz);
		return json;
	}

	@SuppressWarnings("rawtypes")
	private void putField(Class clazz, JSONObject json) throws IllegalAccessException {
		Field[] arr$ = clazz.getDeclaredFields();
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			Field field = arr$[i$];
			field.setAccessible(true);
			Object obj = field.get(this);
			if (obj != null) {
				json.put(field.getName(), obj);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	private JSONObject findField(Class clazz) throws IllegalAccessException {
		JSONObject json = new JSONObject();
		for (Class parent = clazz.getSuperclass(); parent != null && parent != Object.class; parent = parent.getSuperclass()) {
			this.putField(parent, json);
		}

		this.putField(clazz, json);
		return json;
	}

	protected String orderGenerateHmac() throws IllegalAccessException {
		String hmacSource = SignPublisher.generateHmacSource(this);
		return hmacSource;
	}
}
