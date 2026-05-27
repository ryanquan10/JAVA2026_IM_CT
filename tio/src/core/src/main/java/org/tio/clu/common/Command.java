/*
 * uotcsuzgbyxjjv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jfjwt
 */
/*
 * uotcsuzgbyxjjv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jfjwt
 * grantinfo
 */
package org.tio.clu.common;

import java.util.Objects;

public enum Command {

    HeartbeatReq((short) 1),
    HandshakeReq((short) 2),
    HandshakeResp((short) 3),
    BindReq((short) 4),
    BindNtf((short) 5),
    UnbindReq((short) 6),
    UnbindNtf((short) 7),
    TransferReq((short) 8),
    TransferNtf((short) 9),
    UpdateBsNodeReq((short) 10),
    UpdateBsNodeResp((short) 11),
    BestNodeReq((short) 12),
    BestNodeResp((short) 13),

	BindUserToGroup((short) 204),
    xxxxx((short) 99999);

    public static Command from(Short value) {
	Command[] values = Command.values();
	for (Command v : values) {
	    if (Objects.equals(v.value, value)) {
		return v;
	    }
	}
	return null;
    }

    Short value;

    private Command(Short value) {
	this.value = value;
    }

    public Short getValue() {
	return value;
    }

    public void setValue(Short value) {
	this.value = value;
    }
}
