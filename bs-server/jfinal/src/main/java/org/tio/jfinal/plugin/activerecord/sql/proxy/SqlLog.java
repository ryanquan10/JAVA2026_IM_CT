
/**
 * 
 */
package org.tio.jfinal.plugin.activerecord.sql.proxy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu
 *
 */
public class SqlLog {

	public static final Logger slowsqlLog = LoggerFactory.getLogger("slowsqlLog");

	public static final Logger querysqlLog = LoggerFactory.getLogger("querysqlLog");

	public static final Logger updatesqlLog = LoggerFactory.getLogger("updatesqlLog");

	public static final Logger errorsqlLog = LoggerFactory.getLogger("errorsqlLog");
	
	public static final Logger bigresultsqlLog = LoggerFactory.getLogger("bigresultsqlLog");

	static final String NULL = "null";

	static final long SLOWSQL_COST = 1000L;
	/**
	 * 超大结果集
	 */
	static final int BIG_RESULT_COUNT = 1000;  //1000
	

	/**
	 * 
	 * @param sql
	 * @param pps
	 * @param cost
	 * @param e
	 * @author tanyaowu
	 */
	public static void logUpdate(String sql, ProxyPreparedStatement pps, long cost, Object ret, Throwable e) {
		log(sql, pps, cost, ret, e, updatesqlLog);
	}

	/**
	 * 
	 * @param sql
	 * @param pps
	 * @param cost
	 * @param e
	 * @author tanyaowu
	 */
	public static void logQuery(String sql, ProxyPreparedStatement pps, long cost, Object ret, Throwable e) {
	    if (ret != null) {
		if (ret instanceof ResultSet) {
		    ResultSet rs = (ResultSet)ret;
		    
		    Integer rowCount = null;
		    try {
			rs.last(); // 将光标移动到最后一行   
			rowCount = rs.getRow();
			if (rowCount != null && rowCount > BIG_RESULT_COUNT) {
			    log(sql, pps, cost, rowCount, e, bigresultsqlLog);
			}
		    } catch (SQLException e1) {
			querysqlLog.error(sql, e1);
		    }
		    log(sql, pps, cost, rowCount, e, querysqlLog);
		    try {
			rs.beforeFirst();
		    } catch (SQLException e1) {
			querysqlLog.error(sql, e1);
		    }
		} else {
		    log(sql, pps, cost, ret, e, querysqlLog);
		}
	    } else {
		log(sql, pps, cost, ret, e, querysqlLog);
	    }
		
	}

	/**
	 * 
	 * @param sql
	 * @param pps
	 * @param cost
	 * @param e
	 * @param log
	 * @author tanyaowu
	 */
	private static void log(String sql, ProxyPreparedStatement pps, long cost, Object ret, Throwable e,
		Logger log) {
	    if (org.tio.jfinal.kit.StrKit.isBlank(sql)) {
		sql = pps.sql;
	    }
	    Map<Integer, Object> params = pps.params;
	    String realSql = realSql(sql, params);
	    StringBuilder sb = new StringBuilder(realSql.length() + 18);
	    sb.append(cost).append("ms, ret: ").append(ret).append("\r\n").append(realSql);
	    String logStr = sb.toString();
	    log.info(logStr);
	    logSlowAndError(logStr, cost, e);
	}

	/**
	 * 
	 * @param pps
	 * @param cost
	 * @param e
	 * @author tanyaowu
	 */
	public static void logBatch(ProxyPreparedStatement pps, long cost, Throwable e) {
		List<String> sqlList = pps.sqlList;
		StringBuilder sb = null;
		if (sqlList != null && sqlList.size() > 0) {
			sb = new StringBuilder(sqlList.size() * sqlList.get(0).length());
			sb.append("batch sql_list:").append(sqlList.size()).append(", ").append(cost).append("ms\r\n");
			
			for (String sql : sqlList) {
				sb.append(sql).append(";\r\n");
			}

		} else {
			List<Map<Integer, Object>> paramsList = pps.paramsList;
			if (paramsList != null && paramsList.size() > 0) {
				sb = new StringBuilder(paramsList.size() * (pps.sql.length() + 20));
				sb.append("batch params_list:").append(paramsList.size()).append(", ").append(cost).append("ms\r\n");
				
				for (Map<Integer, Object> params : paramsList) {
					String realSql = realSql(pps.sql, params);
					sb.append(realSql).append(";\r\n");
				}
			}
		}

		String logStr = sb.toString();

		updatesqlLog.info(logStr);

		logSlowAndError(logStr, cost, e);
	}

	/**
	 * 
	 * @param logStr
	 * @param cost
	 * @param e
	 * @author tanyaowu
	 */
	private static void logSlowAndError(String logStr, long cost, Throwable e) {
		if (cost >= SLOWSQL_COST) {
			slowsqlLog.info(logStr);
		}

		if (e != null) {
			errorsqlLog.error(logStr, e);
		}
		
		
	}

	/**
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @author tanyaowu
	 */
	public static String realSql(String sql, Map<Integer, Object> params) {
		if (params == null || params.size() == 0) {
			return sql;
		}

		int len = sql.length();

		StringBuilder sb = new StringBuilder(len + params.size() * 10);
		int indexOfParams = 1;
		for (int i = 0; i < len; i++) {
			char c = sql.charAt(i);
			if (c == '?') {
				Object obj = params.get(indexOfParams++);
				if (obj == null) {
					sb.append(NULL);
				} else {
					if (obj instanceof String) {
						sb.append("'").append(obj).append("'");
					} else if (obj instanceof Date) {
						SimpleDateFormat aDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sb.append("'").append(aDate.format((Date) obj)).append("'");
					} else {
						sb.append(obj);
					}
				}
				//				sb.append(obj);
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	private SqlLog() {
	}
}
