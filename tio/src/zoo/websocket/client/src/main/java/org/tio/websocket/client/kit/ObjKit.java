/*
 * gtbyxijmnuo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pafub
 */
package org.tio.websocket.client.kit;

public class ObjKit {
    public static class Box<T> {
	public T value;

	public Box(T v) {
	    value = v;
	}
    }

    public static <T> Box<T> box(T obj) {
	return new Box<>(obj);
    }
}
