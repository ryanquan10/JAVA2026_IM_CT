
/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tio.jfinal.plugin.activerecord;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SqlReporter.
 */
public class SqlReporter implements InvocationHandler {
	
	private Connection conn;
	@SuppressWarnings("unused")
	private static boolean logOn = true;
//	private static final Log log = Log.getLog(SqlReporter.class);
//	private static Logger log = LoggerFactory.getLogger(SqlReporter.class);
	
	public static final Logger slowsqlLog = LoggerFactory.getLogger("slowsqlLog");
	
	public static final Logger querysqlLog = LoggerFactory.getLogger("querysqlLog");
	
	public static final Logger updatesqlLog = LoggerFactory.getLogger("updatesqlLog");
//	
//	protected Logger getSlowSqlLog()
//	{
//		Logger logger = LoggerFactory.getLogger("slowsqlLog");
//		return logger;
//	}
//	
//	public Logger getQuerySqlLog()
//	{
//		Logger logger = LoggerFactory.getLogger("querysqlLog");
//		return logger;
//	}
//	
//	public Logger getUpdateSqlLog()
//	{
//		Logger logger = LoggerFactory.getLogger("updatesqlLog");
//        return logger;
//	}

	public SqlReporter(Connection conn) {
		this.conn = conn;
	}
	
	public static void setLog(boolean on) {
		SqlReporter.logOn = on;
	}
	
	private static final String METHODNAME_PREPARESTATEMENT = "prepareStatement";
	
	@SuppressWarnings("rawtypes")
	public Connection getConnection() {
		Class clazz = conn.getClass();
		return (Connection)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Connection.class}, this);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (method.getName().equals(METHODNAME_PREPARESTATEMENT)) {
//				String info = "Sql: " + args[0];
				String info = "" + args[0];
				if (querysqlLog.isInfoEnabled()) {
					querysqlLog.info(args.length +"     \r\n"+info);
				}
					
//				else
//					System.out.println(info);
			} else {
//				log.info("xxxxxxxxxxxxxxxx:" + method.getName());
			}
			return method.invoke(conn, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}




