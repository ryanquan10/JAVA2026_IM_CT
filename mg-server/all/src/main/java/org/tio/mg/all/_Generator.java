
/**
 * 本类基于JF俱乐部代码改造
 */
package org.tio.mg.all;

import java.io.File;

import javax.sql.DataSource;

import org.tio.jfinal.plugin.activerecord.dialect.MysqlDialect;
import org.tio.jfinal.plugin.activerecord.generator.Generator;
import org.tio.jfinal.plugin.activerecord.generator.MetaBuilder;
import org.tio.jfinal.template.Engine;
import org.tio.mg.service.TioSiteBsServiceInit;
import org.tio.mg.service.init.JFInit;
import org.tio.mg.service.jf.TioSiteMetaBuilder;

import cn.hutool.core.util.ReUtil;

/**
 * Model、BaseModel、_MappingKit 生成器
 */
public class _Generator {

	/**
	 * 部分功能使用 Db + Record 模式实现，无需生成 model 的 table 在此配置
	 */
	private static String[] excludedTable = {"certificate"};

	/**
	 * 重用 JFinalClubConfig 中的数据源配置，避免冗余配置
	 */
	public static DataSource[] getDataSource() {
		int size = JFInit.plugins.length;
		DataSource[] dataSources = new DataSource[size];
		for (int i = 0; i < size; i++) {
			dataSources[i] = JFInit.plugins[i].getDataSource();
		}
		return dataSources;
	}

	public static void main(String[] args) throws Exception {
		JFInit.init();

		String modelPackageName = TioSiteBsServiceInit.class.getPackage().getName() + ".model";

		for (int i = 0; i < JFInit.dbCount; i++) {
			// base model 所使用的包名
			String[] xx = JFInit.dbNames[i].split("_");

			String basePackageName = modelPackageName + "." + xx[xx.length - 1];
			//			String basePackageName = modelPackageName + "." + JFInit.dbNames[i].split("_")[2];
			String baseModelPackageName = basePackageName + ".base";
			// base model 文件保存路径

			String path = _Generator.class.getResource("/").toURI().getPath();
			String basePath = new File(path).getParentFile().getParentFile().getCanonicalPath();

			String baseModelOutputDir = basePath + "/../service/src/main/java/" + ReUtil.replaceAll(baseModelPackageName, "\\.", "/");

			System.out.println("输出路径：" + baseModelOutputDir);

			// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
			String modelOutputDir = baseModelOutputDir + "/..";

			// 创建生成器
			Generator gen = new Generator(getDataSource()[i], baseModelPackageName, baseModelOutputDir, basePackageName, modelOutputDir);
			gen.setBaseModelTemplate("tio_base_model_template.jf");
			DataSource dataSource = getDataSource()[i];
			MetaBuilder metaBuilder = new TioSiteMetaBuilder(dataSource);
			metaBuilder.setGenerateRemarks(true);  //// 在 getter、setter 方法上生成字段备注内容
			gen.setMetaBuilder(metaBuilder);

//			// 在 getter、setter 方法上生成字段备注内容，这行代码要在gen.setMetaBuilder(metaBuilder);之后
//			gen.setGenerateRemarks(true);

			// 设置数据库方言
			gen.setDialect(new MysqlDialect().setKeepByteAndShort(true));
			// 添加不需要生成的表名
			for (String table : excludedTable) {
				gen.addExcludedTable(table);
			}
			// 设置是否在 Model 中生成 dao 对象
			gen.setGenerateDaoInModel(true);
			// 设置是否生成字典文件
			gen.setGenerateDataDictionary(true);
			// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
			// gernerator.setRemovedTableNamePrefixes("t_");
			// 生成
			gen.generate();
			Engine.remove("forBaseModel");
			Engine.remove("forModel");
			Engine.remove("forMappingKit");
		}

	}
}
