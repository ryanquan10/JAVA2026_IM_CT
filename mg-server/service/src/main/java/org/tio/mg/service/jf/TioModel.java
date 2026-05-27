
package org.tio.mg.service.jf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.tio.jfinal.plugin.activerecord.ActiveRecordException;
import org.tio.jfinal.plugin.activerecord.Config;
import org.tio.jfinal.plugin.activerecord.Model;
import org.tio.jfinal.plugin.activerecord.Table;

/**
 * 扩展mysql的insert ignore和replace into 方法
 * @param <M>
 * @author xufei
 * 2020年1月9日 下午1:46:05
 */
@SuppressWarnings("serial")
public abstract class TioModel<M extends TioModel<M>> extends Model<M> {
	
	/**
	 * 忽略唯一索引错误，无则新增，有则忽略(返回false)
	 * mysql专用
	 * @return 1:新增；0:忽略错误
	 * @author xufei
	 * 2020年1月9日 上午10:21:56
	 */
	public int ignoreSave() {
		filter(FILTER_BY_SAVE);
		
		Config config = _getConfig();
		Table table = _getTable();
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		//此处忽略唯一索引错误
		sql.append("insert ignore into ");
		config.getDialect().forModelSave(table, super._getAttrs(), sql, paras);
		// if (paras.size() == 0)	return false;	// The sql "insert into tableName() values()" works fine, so delete this line
		
		// --------
		Connection conn = null;
		PreparedStatement pst = null;
		int result = 0;
		try {
			conn = config.getConnection();
			if (config.getDialect().isOracle()) {
				pst = conn.prepareStatement(sql.toString(), table.getPrimaryKey());
			} else {
				pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			}
			config.getDialect().fillStatement(pst, paras);
			result = pst.executeUpdate();
			config.getDialect().getModelGeneratedKey(this, pst, table);
			_getModifyFlag().clear();
			return result;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(pst, conn);
		}
	}
	
	/**
	 * 唯一索引，无则新增，有则删除新增
	 * @return 1：新增；2：删除并新增；0：异常
	 * @author xufei
	 * 2020年1月9日 上午11:02:03
	 */
	public int replaceSave() {
		filter(FILTER_BY_SAVE);
		
		Config config = _getConfig();
		Table table = _getTable();
		
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		//此处忽略唯一索引错误
		sql.append("replace into ");
		config.getDialect().forModelSave(table, super._getAttrs(), sql, paras);
		// if (paras.size() == 0)	return false;	// The sql "insert into tableName() values()" works fine, so delete this line
		
		// --------
		Connection conn = null;
		PreparedStatement pst = null;
		int result = 0;
		try {
			conn = config.getConnection();
			if (config.getDialect().isOracle()) {
				pst = conn.prepareStatement(sql.toString(), table.getPrimaryKey());
			} else {
				pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			}
			config.getDialect().fillStatement(pst, paras);
			result = pst.executeUpdate();
			config.getDialect().getModelGeneratedKey(this, pst, table);
			_getModifyFlag().clear();
			return result;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(pst, conn);
		}
	}
}
