/*
 * xmzcqrbzk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动spwazkcxkrrbn
 */
package org.tio.http.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.UploadFile;
import org.tio.utils.hutool.ClassUtil;

/**
 * @author tanyaowu 2017年7月26日 下午6:46:11
 */
public class ClassUtils {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(ClassUtils.class);

    public static boolean isSimpleTypeOrArray(Class<?> clazz) {
	return ClassUtil.isSimpleTypeOrArray(clazz) || clazz.isAssignableFrom(UploadFile.class)
		|| clazz.isAssignableFrom(UploadFile[].class);
    }

    /**
     *
     * @author tanyaowu
     */
    public ClassUtils() {
    }
}
