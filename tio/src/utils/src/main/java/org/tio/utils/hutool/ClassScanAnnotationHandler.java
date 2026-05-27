/*
 * pmhquvxjqv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动bwlvotht
 */
package org.tio.utils.hutool;

import java.lang.annotation.Annotation;

/**
 * 本对象会帮你找到含有指定Annotation类的class
 * 
 * @author tanyaowu
 */
public abstract class ClassScanAnnotationHandler implements ClassScanHandler {

    private Class<? extends Annotation> annotationClass;

    /**
     * 
     * @param annotationClass
     */
    public ClassScanAnnotationHandler(Class<? extends Annotation> annotationClass) {
	this.annotationClass = annotationClass;
    }

    @Override
    public void handler(Class<?> clazz) {
	if (!clazz.isAnnotationPresent(annotationClass)) {
	    return;
	}
	handlerAnnotation(clazz);
    }

    /**
     * 
     * @param clazz 拥有annotationClass注解的class对象
     * @author tanyaowu
     */
    public abstract void handlerAnnotation(Class<?> clazz);

}
