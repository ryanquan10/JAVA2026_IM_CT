/*
 * gbfpqfceheknf本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ogvzc
 */
package org.tio.utils.hutool;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ResourceUtil} 单元测试
 * 
 * @author looly
 *
 */
public class ResourceUtilTest {

    @Test
    public void getResourceAsStreamTest() {
	InputStream resourceAsStream = ResourceUtil.getResourceAsStream("classpath:config/tio-quartz.properties");
	Assert.assertNotNull(resourceAsStream);
	try {
	    resourceAsStream.close();
	} catch (IOException e) {
	    // ignore
	}
    }
}
