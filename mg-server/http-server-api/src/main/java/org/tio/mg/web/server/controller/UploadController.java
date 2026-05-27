
package org.tio.mg.web.server.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.jfinal.P;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/upload")
public class UploadController {
	private static Logger log = LoggerFactory.getLogger(UploadController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author tanyaowu
	 */
	public UploadController() {
	}

	@RequestPath(value = "/img")
	public List<Record> img(HttpRequest request) throws Exception {
		String sql = "select * from img where updatetime > ?";
		return xx(request, sql);
	}

	@RequestPath(value = "/video")
	public List<Record> video(HttpRequest request) throws Exception {
		String sql = "select * from video where updatetime > ?";
		return xx(request, sql);
	}

	@RequestPath(value = "/all")
	public List<Map<String, Object>> all(HttpRequest request) throws Exception {
		String rootPath = P.get("res.root");
		List<File> list = FileUtil.loopFiles(rootPath, new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				try {
					String path = pathname.getCanonicalPath().substring(rootPath.length());
					path = StrUtil.replace(path, "\\", "/");
					///avatar/
					if (path.startsWith("/avatar/")) {
						return false;
					}
					return true;
				} catch (IOException e) {
					log.error(e.toString(), e);
					return true;
				}
			}});

		List<Map<String, Object>> listMap = new ArrayList<>(list.size());
		for (File file : list) {
			try {
				Map<String, Object> map = new HashMap<>();
				listMap.add(map);

				String path = file.getCanonicalPath().substring(rootPath.length());
				path = StrUtil.replace(path, "\\", "/");
				map.put("size", FileUtil.size(file));
				map.put("path", path);
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}

		return listMap;
	}

	public List<Record> xx(HttpRequest request, String sql) throws Exception {
		long iv = 3600L * 24L * 1000L * 30L; //只取最近30天的
		Date date = new Date(System.currentTimeMillis() - iv);

		List<Record> list = Db.use(Const.Db.TIO_SITE_MAIN).find(sql, date);
		return list;
	}
}
