/*
 * exnwtxlex本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jouafrch
 */
package org.tio.core.ssl.facade;

import java.nio.ByteBuffer;

class AppendableBuffer {
    private ByteBuffer b;

    public ByteBuffer append(ByteBuffer data) {

	int size = data.limit();
	if (b != null) {
	    size += b.capacity();
	}

	ByteBuffer nb = ByteBuffer.allocate(size);
	if (b != null) {
	    nb.put(b);
	    clear();
	}
	nb.put(data);
	return nb;
    }

    public void clear() {
	b = null;
    }

    public ByteBuffer get() {
	return b;
    }

    public boolean hasRemaining() {
	if (b != null) {
	    return b.hasRemaining();
	}
	return false;
    }

    /**
     * 把
     * 
     * @param byteBuffer
     */
    public void set(ByteBuffer byteBuffer) {
	if (byteBuffer.hasRemaining()) {
	    b = ByteBuffer.allocate(byteBuffer.remaining());
	    b.put(byteBuffer);
	    b.rewind();
	}
    }
}
