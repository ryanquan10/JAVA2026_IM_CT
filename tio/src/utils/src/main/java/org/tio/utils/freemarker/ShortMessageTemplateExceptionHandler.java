/*
 * rveckrafbat本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tfkbdbbzbe
 */
package org.tio.utils.freemarker;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 *
 * ShortMessageTemplateExceptionHandler
 * 
 */
public class ShortMessageTemplateExceptionHandler implements TemplateExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(ShortMessageTemplateExceptionHandler.class);
    public final static ShortMessageTemplateExceptionHandler me = new ShortMessageTemplateExceptionHandler();

    @Override
    public void handleTemplateException(TemplateException templateexception, Environment environment, Writer writer)
	    throws TemplateException {
	try {
	    String code = templateexception.getFTLInstructionStack();
	    if (null != code && code.indexOf("Failed at: ") > 0 && code.indexOf("  [") > 0) {
		String xx = code.substring(code.indexOf("Failed at: ") + 11, code.indexOf("  ["));
		writer.write(xx);
		// log.error("freemarker error :{}", xx);
	    } else {
		writer.write("[some errors occurred!]");
	    }
	} catch (IOException e) {
	    log.error(environment.getCurrentTemplate().getSourceName(), e);
	}
    }

}
