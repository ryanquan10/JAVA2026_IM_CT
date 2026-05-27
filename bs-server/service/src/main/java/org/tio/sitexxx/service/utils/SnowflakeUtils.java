
package org.tio.sitexxx.service.utils;

import org.tio.sitexxx.service.init.PropInit;
import org.tio.utils.hutool.Snowflake;
import org.tio.utils.jfinal.P;

/**
 * 本类不能单独使用，需要系统初始化后才可使用
 * @author tanyaowu 
 * 2019年7月14日 下午5:48:45
 */
public class SnowflakeUtils {

	private static Snowflake snowflake;

	static {
		PropInit.init();
		int workerid = P.getInt("uuid.workerid");
		int datacenter = P.getInt("uuid.datacenter");
		snowflake = new Snowflake(workerid, datacenter);
	}

	public static long nextId() {
		return snowflake.nextId();
	}

}
