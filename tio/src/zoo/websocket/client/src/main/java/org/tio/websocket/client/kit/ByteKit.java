/*
 * xgvsjvvyufyz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动dzqbxtso
 */
package org.tio.websocket.client.kit;

import java.util.Arrays;

public class ByteKit {
    public static byte[][] split(byte[] raw, int partSize) {
	int length = raw.length % partSize == 0 ? raw.length / partSize : (raw.length / partSize) + 1;
	byte[][] parts = new byte[length][];
	int start = 0;
	for (int i = 0; i < length; i++) {
	    int end = Integer.min(raw.length, start + partSize);
	    parts[i] = Arrays.copyOfRange(raw, start, end);
	    start = end;
	}
	return parts;
    }
}
