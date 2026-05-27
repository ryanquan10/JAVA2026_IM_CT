
package org.tio.sitexxx.service.jf;

import javax.sql.DataSource;

import org.tio.jfinal.plugin.activerecord.generator.MetaBuilder;

/**
 * @author tanyw
 *
 */
public class TioSiteMetaBuilder extends MetaBuilder {

	/**
	 * 
	 */
	public TioSiteMetaBuilder(DataSource dataSource) {
		super(dataSource);
	}

	protected boolean isSkipTable(String tableName) {
		if (tableName.endsWith(org.tio.sitexxx.service.vo.Const.HistoryTable.HISTORY_TABLE_SUFFIX)) {
			System.out.println("历史表不生成代码：" + tableName);
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
