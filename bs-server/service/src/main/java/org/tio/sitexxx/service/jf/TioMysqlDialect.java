
package org.tio.sitexxx.service.jf;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.tio.jfinal.plugin.activerecord.Table;
import org.tio.jfinal.plugin.activerecord.dialect.MysqlDialect;

/**
 * 
 * @author lixinji
 * 2020年1月9日 下午1:46:11
 */
public class TioMysqlDialect extends MysqlDialect {

	@Override
	public void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		//		sql.append("insert into `").append(table.getName()).append("`(");
		//此处重写父类保存sql起始语句，以实现replace into,insert ignore into
		if (sql.length() <= 0) {
			sql.append("insert into ");
		}
		sql.append(" `").append(table.getName()).append("`(");
		StringBuilder temp = new StringBuilder(") values(");
		for (Entry<String, Object> e : attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
				sql.append('`').append(colName).append('`');
				temp.append('?');
				paras.add(e.getValue());
			}
		}
		sql.append(temp.toString()).append(')');
	}

}
