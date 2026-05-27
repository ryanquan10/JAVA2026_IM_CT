
package org.tio.mg.service.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.IAtom;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.SystemTimer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public class MysqlTool {
	private static Logger log = LoggerFactory.getLogger(MysqlTool.class);

	/**
	 * 将表移入历史表
	 * @param dbName
	 * @param tableName
	 * @return
	 */
	public static void toHistory(String dbName, String tableName) {
		Db.use(dbName).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				String createSql = showCreateSql(dbName, tableName);
				String tempTableName = tableName + "_temp";
				String tempCreatSql = StrUtil.replace(createSql, "CREATE TABLE `" + tableName + "`", "CREATE TABLE `" + tempTableName + "`");

				log.warn("开始创建临时表\r\n{}", tempCreatSql);
				Db.use(dbName).update(tempCreatSql);

				String newTableName = tableName + "_" + DateUtil.format(new Date(SystemTimer.currTime), "yyyyMMddHHmm") + Const.HistoryTable.HISTORY_TABLE_SUFFIX;
				String renameSql = "RENAME TABLE " + tableName + " to " + newTableName + ", " + tempTableName + " to " + tableName;
				log.warn("开始重命名表\r\n{}", renameSql);

				Db.use(dbName).update(renameSql);
				return true;
			}
		});
	}

	/**
	 * 显示创建表结构的SQL脚本
	 * @param dbName
	 * @param tableName
	 * @return
	 */
	public static String showCreateSql(String dbName, String tableName) {
		String sql = "show create table " + tableName;
		Record record = Db.use(dbName).findFirst(sql);
		String ct = record.getStr("Create Table");
		return ct;
	}

	/**
	 * @param host 数据库服务器主机地址，可以是ip，也可以是域名
	 * @param port 数据库服务器端口
	 * @param dbName 数据库名字
	 * @param username 数据库用户名
	 * @param password 数据库密码（明文）
	 * @param filePath 存到哪个文件，形如："d:/dbbackup/2019-08-03_00_00_00.sql"
	 * @return
	 */
	public static File backup(String host, int port, String dbName, String username, String password, String filePath) {
		Long starttime = System.currentTimeMillis();
		try {
			File file = new File(filePath);
			String[] commands = new String[3];
			String os = System.getProperties().getProperty("os.name");
			if (os.startsWith("Win")) {
				commands[0] = "cmd.exe";
				commands[1] = "/c";
			} else {
				commands[0] = "/bin/sh";
				commands[1] = "-c";
			}

			StringBuilder mysqldump = new StringBuilder();
			mysqldump.append("mysqldump");
			mysqldump.append(" --opt");

			mysqldump.append(" --user=").append(username);
			mysqldump.append(" --password=").append("#####");

			mysqldump.append(" --host=").append(host);
			mysqldump.append(" --protocol=tcp");
			mysqldump.append(" --port=").append(port);

			mysqldump.append(" --default-character-set=utf8");
			mysqldump.append(" --single-transaction=TRUE");

			mysqldump.append(" --routines");
			mysqldump.append(" --events");

			mysqldump.append(" ").append(dbName);
			mysqldump.append(" > ");
			mysqldump.append("").append(filePath).append("");

			String command = mysqldump.toString();
			log.error(command);
			command = command.replaceAll("#####", password);
			commands[2] = command;
			
			
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(commands);
			if (process.waitFor() == 0) {
				Long endtime = System.currentTimeMillis();
				Long distance = endtime - starttime;
				log.error("【" + dbName + "】备份成功，耗时：" + distance + "ms");
				return file;
			} else {
				InputStream is = process.getErrorStream();
				if (is != null) {
					@SuppressWarnings("resource")
					BufferedReader in = new BufferedReader(new InputStreamReader(is, "utf-8"));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = in.readLine()) != null) {
						sb.append(line);
					}
					log.error("数据库【" + dbName + "】备份失败\r\n" + sb.toString());
				}
			}
		} catch (Exception e) {
			log.error("数据库【" + dbName + "】备份失败。eror: " + e.getMessage(), e);
			return null;
		}
		return null;
	}
}
