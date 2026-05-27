/*
 * xqjtfnsqrdvno本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fqrelep
 */
/*
 * xqjtfnsqrdvno本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fqrelep
 * grantinfo
 */
package org.tio.clu.common.utils;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.Uuid;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.jfinal.P;

/**
 * @author tanyaowu 2016年8月7日 下午1:49:05
 */
public class PropInit {
    private static Logger log = LoggerFactory.getLogger(PropInit.class);

    private static boolean inited = false;

    /**
     * 配置信息加载
     */
    public static void init(String[] files) {
	synchronized (log) {
	    if (!inited) {
		P.clear();
		for (int i = 0; i < files.length; i++) {
		    if (i == 0) {
			P.use(files[i]);
		    } else {
			P.append(files[i]);
		    }
		}

		// 设置一下，用于生成高性能的uuid，这里耦合度略高，后面再优化一下
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
	}
    }
}
