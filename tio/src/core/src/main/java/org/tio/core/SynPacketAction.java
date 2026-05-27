/*
 * ykpfqfcat本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动iwvklejcodprs
 */
package org.tio.core;

/**
 * 同步消息 action
 * 
 * @author tanyaowu
 *
 */
public enum SynPacketAction {
    /**
     *
     */
    BEFORE_WAIT(1),

    /**
     *
     */
    AFTER__WAIT(2),

    /**
     *
     */
    BEFORE_DOWN(3);

    public static SynPacketAction forNumber(int value) {
	switch (value) {
	case 1:
	    return BEFORE_WAIT;
	case 2:
	    return AFTER__WAIT;
	case 3:
	    return BEFORE_DOWN;
	default:
	    return null;
	}
    }

    private final int value;

    private SynPacketAction(int value) {
	this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
	return value;
    }
}
