/*
 * wtoibtty本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jgbpkypt
 */
package org.tio.http.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanyaowu 2017年6月29日 下午7:52:31
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestPath {

    /**
     * 这个不能用变量，譬如/user/{id}，这样的路径是不允许的
     * 
     * @return
     */
    String forward() default "";

    /**
     * 
     * @return
     */
    String value() default "";
    
    /**
     * 
     * @return
     */
    String[] valueList() default {};

}
