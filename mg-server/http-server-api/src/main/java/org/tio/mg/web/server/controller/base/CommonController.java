
package org.tio.mg.web.server.controller.base;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.mg.service.model.main.File;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.web.server.utils.CloudflareR2Utils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.web.server.utils.UploadUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.io.FileUtil;

/**
 * 公共请求
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/common")
public class CommonController {
	
	private static Logger log = LoggerFactory.getLogger(CommonController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:17:25
	 */
	public static void main(String[] args) {

	}

	/**
	 * 
	 */
	public CommonController() {
	}
	
	
	/**
	 * 文件上传
	 * @param request
	 * @param uploadFile
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月18日 上午10:17:14
	 */
	@RequestPath(value = "/file")
	public Resp file(HttpRequest request, UploadFile uploadFile,Short type) throws Exception {
		try {
			if(uploadFile == null) {
				return Resp.fail("上传信息为空");
			}
			File dbFile = innerUploadFile(uploadFile,type);
			return Resp.ok(dbFile);
		} catch (Exception e) {
			log.error(e.toString(), e);
			log.error(e.getMessage());
			return Resp.fail(RetUtils.SYS_ERROR);
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param uploadFile
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月8日 下午1:55:57
	 */
	@RequestPath(value = "/files")
	public Resp files(HttpRequest request, UploadFile[] uploadFile,Short type) throws Exception {
		try {
			if(uploadFile == null || uploadFile.length <= 0) {
				return Resp.fail("上传信息为空");
			}
			List<File> dbFiles = new ArrayList<File>();
			for(UploadFile file : uploadFile) {
				File dbFile = innerUploadFile(file,type);
				dbFiles. add(dbFile);
			}
			return Resp.ok(dbFiles);
		} catch (Exception e) {
			log.error(e.toString(), e);
			log.error(e.getMessage());
			return Resp.fail(RetUtils.SYS_ERROR);
		}
	}
	
	/**
	 * @param uploadFile
	 * @param type
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月18日 上午10:16:13
	 */
	private static File innerUploadFile(UploadFile uploadFile, Short type) throws Exception {
		byte[] bs = uploadFile.getData();
		String filename = uploadFile.getName();
		String ext = FileUtil.extName(filename);
		if (StrUtil.isBlank(ext)) {
			throw new Exception("未识别的文件格式");
		}

		// 文件类型黑名单校验
		String fileRole = "php,java,jsp,sh,html,css,js,vue,c,h,jar,war,exe,scr,bat,vbs";
		String[] fileRoleArray = fileRole.split(",");
		for (String s : fileRoleArray) {
			if (ext.toLowerCase().equals(s.toLowerCase())) {
				throw new Exception("不允许的文件格式：" + ext);
			}
		}

		// 生成文件路径
		String urlWithoutExt = UploadUtils.newFile(UploadUtils.mgSubDir(type), -type, filename);
		String url = urlWithoutExt + "." + ext;

		// 构建 Content-Type
		String contentType;
		switch (ext.toLowerCase()) {
			case "jpg":
			case "jpeg":
				contentType = "image/jpeg";
				break;
			case "png":
				contentType = "image/png";
				break;
			case "mp4":
				contentType = "video/mp4";
				break;
			case "pdf":
				contentType = "application/pdf";
				break;
			default:
				contentType = "application/octet-stream";
		}

		// 上传文件到 R2
		try (InputStream inputStream = new ByteArrayInputStream(bs)) {
            UploadUtils.unificationUpload( url, inputStream, bs.length, contentType);
//			CloudflareR2Utils.uploadFilePublic(
//					Const.CloudflareR2.R2_BUCKET_NAME,
//					url,
//					inputStream,
//					bs.length,
//					contentType
//			);
		}

		// 构造并保存数据库记录
		File dbFile = new File();
		dbFile.setExt(ext);
		dbFile.setFilename(uploadFile.getName());
		dbFile.setSize((long) bs.length);
		dbFile.setUid(-type); // 业务逻辑保持不变
		dbFile.setUrl(url);   // 存储的是相对路径，前端拼接 base_url 即可访问
		dbFile.save();

		return dbFile;
	}
	
}
