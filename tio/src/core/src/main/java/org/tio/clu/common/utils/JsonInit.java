/*
 * qhwwvand本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tmyyhxpaummnyx
 */
/*
 * qhwwvand本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tmyyhxpaummnyx
 * grantinfo
 */
package org.tio.clu.common.utils;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.json.Json;

import com.alibaba.fastjson.serializer.ToStringSerializer;

/**
 * @author tanyaowu 2016年8月18日 下午4:52:28
 */
public class JsonInit {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(JsonInit.class);

    public static void init() {

	Json.put(BigInteger.class, ToStringSerializer.instance);
	Json.put(Long.class, ToStringSerializer.instance);
	Json.put(Long.TYPE, ToStringSerializer.instance);
    }
}
