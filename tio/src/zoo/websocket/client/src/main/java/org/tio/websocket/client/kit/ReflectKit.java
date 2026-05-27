/*
 * kuzfeihfdpfj本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动biklbeyubg
 */
package org.tio.websocket.client.kit;

import java.lang.reflect.Field;

public class ReflectKit {
    public static void setField(Object target, String field, Object value)
	    throws NoSuchFieldException, IllegalAccessException {
	Field field1 = target.getClass().getDeclaredField(field);
	field1.setAccessible(true);
	field1.set(target, value);
    }
}
