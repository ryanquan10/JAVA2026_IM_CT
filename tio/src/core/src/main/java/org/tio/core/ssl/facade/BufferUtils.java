/*
 * qlbss本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rhjpdic
 */
package org.tio.core.ssl.facade;

import java.nio.ByteBuffer;

public class BufferUtils {
    static void copy(ByteBuffer from, ByteBuffer to) {
	to.put(from);
	to.flip();
	to.limit(to.capacity()); // added by tanyaowu
    }

    public static ByteBuffer slice(ByteBuffer data) {
	if (data.hasRemaining()) {
	    byte[] slice = new byte[data.remaining()];
	    data.get(slice, 0, data.remaining());
	    return ByteBuffer.wrap(slice);
	}
	return null;
    }
}
