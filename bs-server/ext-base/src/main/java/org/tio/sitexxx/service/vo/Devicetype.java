
package org.tio.sitexxx.service.vo;

import java.util.Objects;

/**
 * 设备类型, pc, android, ios
 * @author tanyaowu 
 * 2016年11月1日 下午3:01:43
 */
public enum Devicetype {

	/**
	 * WS
	 */
	WEB((short) 1),

	/**
	 * 安卓
	 */
	ANDROID((short) 2),

	/**
	 * IOS
	 */
	IOS((short) 3),
	/**
	 * H5
	 */
	H5((short) 4),

	/**
	 * APP，安卓或IOS都是APP
	 */
	APP((short) 5),
	
	/**
	 * 电脑版client(注：不是web，是用java或者c写的程序)
	 */
	PC((short) 6),

	/**
	 * 系统任务自动发送类型
	 */
	SYS_TASK((short) 99);

	public static Devicetype from(Short value) {
		Devicetype[] values = Devicetype.values();
		for (Devicetype v : values) {
			if (Objects.equals(v.value, value)) {
				return v;
			}
		}
		return null;
	}

	Short value;

	private Devicetype(Short value) {
		this.value = value;
	}

	public Short getValue() {
		return value;
	}

	public void setValue(Short value) {
		this.value = value;
	}
}
