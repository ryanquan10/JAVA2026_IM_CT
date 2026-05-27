package org.tio.utils.log;

import java.util.Map;

/**
 * 日志适配器，兼容日志处理
 */
public interface LoggerFactoryBinder {

    public String log(String u, Map<String, Object> k);

    public void closeLog();

    public String getLogKey();

    public String getLogCk();
}
