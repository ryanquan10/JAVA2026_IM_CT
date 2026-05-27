
package org.tio.mg.web.server.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.model.mg.MgInvoice;
import org.tio.mg.service.utils.SnowflakeUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.DatePattern;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 上传工具处理类
 * @author xufei
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
	 * 后台上传类型路径
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年6月18日 上午10:14:50
	 */
	public static String mgSubDir(Short type) {
		switch (type) {
		case MgConst.MgUploadDir.RECRUIT_CMP_LOGO:
			return MgConst.MgUploadDir.RECRUIT_CMP_LOGO_DIR;
		case MgConst.MgUploadDir.ORDER_CONTRACT:
			return MgConst.MgUploadDir.ORDER_CONTRACT_DIR;
		case MgConst.MgUploadDir.APP_FILE:
			return MgConst.MgUploadDir.APP_FILE_DIR;
		default:
			return MgConst.MgUploadDir.DEFAULT_DIR;
		}
	}

	/**
	 *  按时间产生路径
	 * @param subDir
	 * @param filename
	 * @return
	 * @author xufei
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
	 * @param subDir
	 * @param nick
	 * @param invoice
	 * @return
	 * @author xufei
	 * 2020年7月28日 下午3:06:38
	 */
	public static String invoice(String subDir,String nick,MgInvoice invoice) {
		String path = subDir;
		if(StrUtil.isBlank(nick)) {
			nick = "缺省名称";
		}
		double amonut = invoice.getAmount();
		if(amonut <= 0) {
			amonut = 0d;
		}
		path = path + "/" + nick + "/";
		String resRootDir = Const.RES_ROOT;
		File dir = new File(resRootDir, path);
		FileUtil.mkdir(dir);
		String fileName = nick;
		fileName += "-" + DateUtil.format(new Date(), "yyMMddHHmmssSSS");
		fileName += "-" + invoice.getName() + "￥" + amonut;
		Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		fileName= matcher.replaceAll("");
		return path + "/" + fileName;
	}
	
	/**
	 * 
	 * @author tanyaowu
	 */
	public UploadUtils() {
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
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
}
