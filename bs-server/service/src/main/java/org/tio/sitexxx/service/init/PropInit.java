
package org.tio.sitexxx.service.init;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.Uuid;
import org.tio.utils.jfinal.P;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年8月7日 下午1:49:05
 */
public class PropInit {
	private static Logger log = LoggerFactory.getLogger(PropInit.class);

	private static boolean inited = false;

	/**
	 * 配置信息加载
	 */
	public static void init() {
		init(new String[] { "app.properties", "app-env.properties", "app-host.properties" });
	}

	public static void init(String[] props) {
		if (inited) {
			return;
		}

		try {
			LockUtils.runWriteOrWaitRead(PropInit.class.getName() + ".init", log, () -> {
				if (!inited) {
					P.clear();
					//					P.use("app.properties");
					for (int i = 0; i < props.length; i++) {
						P.append(props[i]);
					}

					//设置一下，用于生成高性能的uuid，这里耦合度略高，后面再优化一下
					Integer workerid = P.getInt("uuid.workerid");
					Integer datacenter = P.getInt("uuid.datacenter");

					if (workerid != null) {
						Uuid.setWorkid(workerid);
					}
					if (datacenter != null) {
						Uuid.setDatacenterid(datacenter);
					}

					String systemparam = "systemparam.";
					Properties properties = P.getProp().getProperties();
					Set<Entry<Object, Object>> set = properties.entrySet();
					for (Entry<Object, Object> entry : set) {
						String key = (String) entry.getKey();
						if (StrUtil.startWith(key, systemparam)) {
							String sysKey = StrUtil.subAfter(key, systemparam, false);
							String value = (String) entry.getValue();
							if (StrUtil.isNotBlank(value)) {
								System.setProperty(sysKey, (String) entry.getValue());
								System.out.println("系统属性：" + sysKey + ", 值：" + entry.getValue());
							}
						}
					}
					inited = true;
				}
			});
		} catch (Exception e) {
			log.error("", e);
		}

	}

	/**
	 * 强制初始化
	 * 
	 * @author tanyaowu
	 */
	public static void forceInit() {
		inited = false;
		init();
	}
}
