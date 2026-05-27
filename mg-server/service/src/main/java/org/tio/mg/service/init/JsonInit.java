
package org.tio.mg.service.init;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.JfinalRecordSerializer;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.mg.service.json.IpInfoSerializeFilter;
import org.tio.mg.service.json.UserSerializeFilter;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.User;
import org.tio.utils.json.Json;

import com.alibaba.fastjson.serializer.ToStringSerializer;

/**
 * @author tanyaowu
 * 2016年8月18日 下午4:52:28
 */
public class JsonInit {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(JsonInit.class);

	public static void init() {
		Json.put(User.class, UserSerializeFilter.me);
		Json.put(IpInfo.class, IpInfoSerializeFilter.ME);
		Json.put(Record.class, JfinalRecordSerializer.me);
		
		
		Json.put(BigInteger.class, ToStringSerializer.instance);
		Json.put(Long.class, ToStringSerializer.instance);
		Json.put(Long.TYPE, ToStringSerializer.instance);
	}
}
