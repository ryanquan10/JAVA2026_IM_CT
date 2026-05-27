
package org.tio.sitexxx.service.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.template.stat.ast.Switch;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.DatePattern;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 上传工具处理类
 * @author lixinji
 * 2021年4月27日 下午3:50:46
 */
public class UploadUtils {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UploadUtils.class);

	/**
	 * 根据用户id获取其im-data路径(位于上传资源服务器)
	 * @param subDir
	 * @param uid
	 * @return
	 * @author tanyaowu
	 */
	public static String dataRootPath(String subDir, int uid) {
		long uid1 = uid + 74541287548L;
		long base = 107;
		long dir1 = uid1 % base;
		long dir2 = uid1 % (base * base);
		long dir3 = uid1 % (base * base * base);
		long dir4 = uid1 % (base * base * base * base);

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(DateUtil.format(new Date(), "yyyyMM"));
		sb.append("/");
		sb.append(subDir);
		sb.append("/");
		sb.append(dir1);
		sb.append("/");
		sb.append(dir2);
		sb.append("/");
		sb.append(dir3);
		sb.append("/");
		sb.append(dir4);
		sb.append("/");
		sb.append(uid1);
		String path = sb.toString();
		return path;
	}

	/**
	 * 新上传的文件，不含后缀。形如："/im/upload/img/1/1/1/2/44/20/121212"
	 * @param subDir
	 * @param uid
	 * @param filename 可以为null
	 * @return
	 */
	public static String newFile(String subDir, int uid, String filename) {
		String path = dataRootPath(subDir, uid);
		Date date = new Date();
		path = path + "/" + RandomUtil.randomInt(1, 120);

		String resRootDir = Const.RES_ROOT;
		File dir = new File(resRootDir, path);
		FileUtil.mkdir(dir);

		String newFileName = DateUtil.format(date, "HHmmss");
		if (filename != null) {
			//			fileName += "/" + FileUtil.mainName(filename);
			newFileName += "/" + SnowflakeUtils.nextId();
		} else {
			newFileName += "/" + SnowflakeUtils.nextId();
		}
		return path + "/" + newFileName;
	}

	/**
	 * 简历路径
	 * @param subDir
	 * @param filename
	 * @param cmpname
	 * @param postname
	 * @param city
	 * @param name
	 * @return
	 * @author lixinji
	 * 2020年5月19日 上午10:53:46
	 */
	public static String resume(String subDir, String cmpname, String postname, String city, String name, String phone) {
		String path = subDir;
		if (StrUtil.isBlank(name)) {
			name = "缺省名称";
		}
		if (StrUtil.isBlank(phone)) {
			phone = "缺省电话";
		}
		Date date = new Date();
		path = path + "/" + cmpname + "/" + postname + "/" + city;

		String resRootDir = Const.RES_ROOT;
		File dir = new File(resRootDir, path);
		FileUtil.mkdir(dir);

		String fileName = cmpname + "_" + postname + "_" + city + "_" + name + "_" + phone + "_" + DateUtil.format(date, "yyyyMMddHHmmss");
		return path + "/" + fileName;
	}

	/**
	 *  按时间产生路径
	 * @param subDir
	 * @param filename
	 * @return
	 * @author lixinji
	 * 2020年1月14日 下午4:12:11
	 */
	public static String dateFile(String subDir) {
		String path = "/" + subDir + "/";
		Date date = new Date();
		path += DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
		path = path + RandomUtil.randomInt(1, 120);
		String resRootDir = Const.RES_ROOT;
		File dir = new File(resRootDir, path);
		FileUtil.mkdir(dir);
		return path + "/" + DateUtil.format(date, "HHmmss") + SnowflakeUtils.nextId();
	}
	
	/**
	 * 初始化文件夹路径
	 * @param subDir
	 * @return
	 * @author lixinji
	 * 2021年7月20日 下午3:39:35
	 */
	public static String newFolder(String subDir) {
		String path = "/" + subDir + "/";
		path += DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
		path = path + RandomUtil.randomInt(1, 120);
		String resRootDir = Const.RES_ROOT;
		File dir = new File(resRootDir, path);
		FileUtil.mkdir(dir);
		return path + "/";
	}

    public static void unificationUpload(String objectKey, InputStream inputStream, long size, String contentType){
        if (Const.OSS_TYPE.equals("tencentoss")){
            TencentOssUtils.upload(objectKey, inputStream, size, contentType);
        } else if (Const.OSS_TYPE.equals("cloudflare")) {
            String bucketName = Const.CloudflareR2.R2_BUCKET_NAME;
            CloudflareR2Utils.uploadFilePublic(bucketName, objectKey, inputStream, size, contentType);
        }
    }


	/**
	 * 
	 * @author tanyaowu
	 */
	public UploadUtils() {
	}

	
	/**
	 * 返回资源路径
	 * @param path
	 * @return
	 */
	public static String resUrl(String path) {
		if (StrUtil.startWithIgnoreCase(path, "http://") || StrUtil.startWithIgnoreCase(path, "https://") || StrUtil.startWithIgnoreCase(path, "//")) {
			return path;
		}
		return Const.RES_SERVER + path;
	}
	
	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
}
