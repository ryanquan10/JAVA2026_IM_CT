/*
 * dnfhldo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ilglwyyirqouch
 */
/*
 * dnfhldo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ilglwyyirqouch
 * grantinfo
 */
package org.tio.clu.common;

import java.util.Objects;

public enum BindType {

    /**
     * Group
     */
    Group((byte) 1),
    /**
     * User
     */
    User((byte) 2),
	BindUserToGroup((byte) 12),
    /**
     * Token
     */
    Token((byte) 3),
    /**
     * Ip
     */
    Ip((byte) 4),
    /**
     * BsId
     */
    BsId((byte) 5),

    /**
     * ChannelId
     */
    ChannelId((byte) 6),
    /**
     * All
     */
    All((byte) 7),

    xxxxx((byte) 99999);

    public static BindType from(Byte value) {
	BindType[] values = BindType.values();
	for (BindType v : values) {
	    if (Objects.equals(v.value, value)) {
		return v;
	    }
	}
	return null;
    }

    Byte value;

    private BindType(Byte value) {
	this.value = value;
    }

    public Byte getValue() {
	return value;
    }

    public void setValue(Byte value) {
	this.value = value;
    }
}
