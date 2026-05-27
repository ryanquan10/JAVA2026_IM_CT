/*
 * lezlotk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tnkeumkku
 */
package org.tio.websocket.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanyaowu 2017年6月30日 下午5:06:09
 */
public enum Opcode {

    NOT_FIN((byte) 0), TEXT((byte) 1), BINARY((byte) 2), CLOSE((byte) 8), PING((byte) 9), PONG((byte) 10);

    private static final Map<Byte, Opcode> map = new HashMap<>();

    static {
	for (Opcode command : values()) {
	    map.put(command.getCode(), command);
	}
    }

    public static Opcode valueOf(byte code) {
	return map.get(code);
    }

    private final byte code;

    private Opcode(byte code) {
	this.code = code;
    }

    public byte getCode() {
	return code;
    }

}
