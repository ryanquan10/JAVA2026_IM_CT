
/**
 * 
 */
package org.tio.sitexxx.service.utils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.UploadFile;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import net.coobird.thumbnailator.Thumbnails;

/**
 * @author tanyaowu
 *
 */
public class ImgUtils {
	private static Logger log = LoggerFactory.getLogger(ImgUtils.class);

	/**
	 * 
	 */
	public ImgUtils() {
	}

	//	/**
	//	 * @deprecated 这个方法暂时执行不了
	//	 * @param srcImg
	//	 * @param desImg
	//	 * @param scale
	//	 * @author tanyaowu
	//	 */
	//	public static void scale(String srcImg, String desImg, double scale) {
	//		Mat src = Imgcodecs.imread(srcImg);
	//		Mat dst = src.clone();
	//		Imgproc.resize(src, dst, new Size(src.width() * scale, src.height() * scale));
	//		Imgcodecs.imwrite(desImg, dst);
	//	}

	public static void scale(String srcImg, String desImg, double scale, double quality) throws Exception {
		Thumbnails.of(srcImg).scale(scale) //指定图片的大小，值在0到1之间，1f就是原图大小，0.5就是原图的一半大小，这里的大小是指图片的长宽
		        .outputQuality(quality) //图片的质量，值也是在0到1，越接近于1质量越好，越接近于0质量越差
		        .toFile(desImg);
	}

	static long c = 0;

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String rootPath = "F:\\work\\tio-site\\some\\技术白皮书";
		List<File> files = FileUtil.loopFiles(rootPath, new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String ext = FileUtil.extName(pathname);
				if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg")) {
					if (pathname.getName().startsWith("scaled_")) {
						return false;
					}
					return true;
				}
				return false;
			}

		});
		int size = 1080;
		for (File file : files) {
			try {
				byte[] imageBytes = FileUtil.readBytes(file);
				BufferedImage bi = ImgUtil.toImage(imageBytes);
				if (bi.getWidth() <= size) {
					//					System.out.println(c++ + "..");
					continue;
				}

				float scale = ImgUtils.calcScaleWithWidth(size, bi);
				String imgFilePath = file.getCanonicalPath();
				File desFile = new File(file.getParent(), "scaled_" + scale + "_" + file.getName());
				String smImgFilePath = desFile.getCanonicalPath();

				//				scale(imgFilePath, smImgFilePath, scale);

				//				Img img = ImgUtils.processImg(bi, imgFilePath, smImgFilePath, scale);
				//				log.error(c++ + "、新size:{}, 文件:{}", img.getSize(), imgFilePath);

				Thumbnails.of(imgFilePath).scale(scale) //指定图片的大小，值在0到1之间，1f就是原图大小，0.5就是原图的一半大小，这里的大小是指图片的长宽
				        .outputQuality(0.8f) //图片的质量，值也是在0到1，越接近于1质量越好，越接近于0质量越差
				        .toFile(smImgFilePath);

			} catch (Exception e) {
				try {
					log.error(file.getCanonicalPath(), e);
				} catch (IOException e1) {
					log.error(e1.toString(), e1);
				}
			}
		}

	}

	/**
	 * 用目标宽度计算缩放比
	 * @param maxWidth
	 * @param initBufferedImage
	 * @return
	 */
	public static float calcScaleWithWidth(int maxWidth, BufferedImage initBufferedImage) {
		int initWidth = initBufferedImage.getWidth();
		int newWidth = Math.min(maxWidth, initWidth);
		return (float) newWidth / (float) initWidth;
	}

	/**
	 * 用目标高度计算缩放比
	 * @param maxHeight
	 * @param initBufferedImage
	 * @return
	 */
	public static float calcScaleWithHeight(int maxHeight, BufferedImage initBufferedImage) {
		int initHeight = initBufferedImage.getHeight();
		int newHeight = Math.min(maxHeight, initHeight);
		return (float) newHeight / (float) initHeight;
	}

	/**
	 * 
	 * @param subDir
	 * @param uid
	 * @param uploadFile
	 * @param maxWidth
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public static Img processImgWithWidth(String subDir, Integer uid, UploadFile uploadFile, int maxWidth) throws Exception {
		byte[] imgBytes = uploadFile.getData();
		BufferedImage bi = ImgUtil.toImage(imgBytes);
		return processImg(subDir, uid, uploadFile, bi, calcScaleWithWidth(maxWidth, bi));
	}

	/**
	 * 
	 * @param subDir
	 * @param uid
	 * @param uploadFile
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public static Img processImgWithHeight(String subDir, Integer uid, UploadFile uploadFile, int maxHeight) throws Exception {
		byte[] imgBytes = uploadFile.getData();
		BufferedImage bi = ImgUtil.toImage(imgBytes);
		return processImg(subDir, uid, uploadFile, bi, calcScaleWithHeight(maxHeight, bi));
	}

	/**
	 * 
	 * @param subDir
	 * @param uid
	 * @param uploadFile
	 * @param bi
	 * @param scale
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public static Img processImg(String subDir, Integer uid, UploadFile uploadFile, BufferedImage bi, float scale) throws Exception {
		String ext = FileUtil.extName(uploadFile.getName());
		if (StrUtil.isBlank(ext)) {
			ext = "gif";
		}
		boolean needScale = !"gif".equals(ext); // gif 不压缩
		String imgUrlWithoutExt = UploadUtils.newFile(subDir, uid, uploadFile.getName()); // 不带后缀的文件路径
		String imgUrl = imgUrlWithoutExt + "." + ext;
		String smImgUrl = null;
		byte[] imgBytes = uploadFile.getData();

		if (needScale) {
			smImgUrl = imgUrlWithoutExt + "_sm." + ext;
		} else {
			smImgUrl = imgUrl;
		}

		String objectKey = imgUrl;
		String smObjectKey = smImgUrl;
		try (InputStream inputStream = new ByteArrayInputStream(imgBytes)) {
            UploadUtils.unificationUpload( objectKey, inputStream, imgBytes.length, "image/" + ext);
//			CloudflareR2Utils.uploadFilePublic(
//					Const.CloudflareR2.R2_BUCKET_NAME,
//					objectKey,
//					inputStream,
//					imgBytes.length,
//					"image/" + ext
//			);
		}

		// 如果是需要压缩的图片，则处理缩略图
		if (needScale) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				String outputFormat = "jpg";
				if ("png".equalsIgnoreCase(ext)) {
					outputFormat = "png";
				} else if ("gif".equalsIgnoreCase(ext)) {
					outputFormat = "gif";
				}
				double quality = 0.5f;
				byte[] originalBytes = compressImage(bi, 1f, quality, outputFormat);
				try (InputStream is = new ByteArrayInputStream(originalBytes)) {
                    UploadUtils.unificationUpload( smObjectKey, is, originalBytes.length, "image/" + ext);
//					CloudflareR2Utils.uploadFilePublic(
//							Const.CloudflareR2.R2_BUCKET_NAME,
//							smObjectKey,
//							is,
//							originalBytes.length,
//							"image/" + ext
//					);
				}

				if (scale < 1f) {
					try (ByteArrayOutputStream smOs = new ByteArrayOutputStream()) {
						byte[] smBytes = compressImage(bi, scale, quality, outputFormat);
						try (InputStream is = new ByteArrayInputStream(smBytes)) {
                            UploadUtils.unificationUpload( smObjectKey, is, smBytes.length, "image/" + ext);
//							CloudflareR2Utils.uploadFilePublic(
//									Const.CloudflareR2.R2_BUCKET_NAME,
//									smObjectKey,
//									is,
//									smBytes.length,
//									"image/" + ext
//							);
						}
					}
				}
			}
		}

		// 构造返回对象
		Img img = new Img();

		// 获取缩略图信息
		BufferedImage smBi = Thumbnails.of(bi).scale(needScale ? 0.5 : 1).asBufferedImage();
		img.setCoverheight(smBi.getHeight());
		img.setCoverwidth(smBi.getWidth());

		// 模拟大小估算（实际建议从上传结果中获取）
		img.setCoversize((int) (imgBytes.length * (needScale ? 0.6 : 1)));

		img.setCoverurl(smImgUrl); // 返回的是相对路径
		img.setUrl(imgUrl);         // 返回的是相对路径

		img.setUid(uid);
		String filename = uploadFile.getName();
		img.setFilename(filename);
		img.setHeight(bi.getHeight());
		img.setWidth(bi.getWidth());
		img.setSize((long) imgBytes.length);
		img.setTitle(filename);

		return img;
	}


	/**
	 * 压缩图片并返回字节数组
	 *
	 * @param image     BufferedImage 原始图片
	 * @param scale     缩放比例（1f=原尺寸，0.5f=缩小一半）
	 * @param quality   输出质量（0.0 ~ 1.0，仅对有损格式有效如 JPG）
	 * @param format    输出格式（"jpg", "png" 等）
	 * @return 压缩后的图片字节数组
	 */
	public static byte[] compressImage(BufferedImage image, float scale, double quality, String format) throws IOException {
		if (image == null) throw new IllegalArgumentException("image 不能为空");

		if (scale <= 0 || scale > 1f) scale = 1f;
		if (quality < 0 || quality > 1) quality = 0.8d;
		if (StrUtil.isBlank(format)) format = "jpg";

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			Thumbnails.of(image)
					.scale(scale)
					.outputQuality(quality)
					.outputFormat(format)
					.toOutputStream(os);
			return os.toByteArray();
		}
	}

	public static Img processImg(String subDir, Integer uid, UploadFile uploadFile, float scale) throws Exception {
		byte[] imgBytes = uploadFile.getData();
		BufferedImage bi = ImgUtil.toImage(imgBytes);
		return processImg(subDir, uid, uploadFile, bi, scale);
	}

	/**
	 * @deprecated
	 * @param subDir
	 * @param uploadFile
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月14日 下午4:21:45
	 */
	public static Img processImg(String subDir, UploadFile uploadFile) throws Exception {
		String ext = FileUtil.extName(uploadFile.getName());
		if (StrUtil.isBlank(ext)) {
			ext = "gif";
		}

		// 生成 objectKey 路径
		String imgUrlWithoutExt = UploadUtils.dateFile(subDir); // 如：avatar/img/20250410/12345678
		String imgUrl = imgUrlWithoutExt + "." + ext;

		byte[] imgBytes = uploadFile.getData();
		BufferedImage bi = ImgUtil.toImage(imgBytes);

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
			case "gif":
				contentType = "image/gif";
				break;
			case "bmp":
				contentType = "image/bmp";
				break;
			default:
				contentType = "image/*";
		}

		// 上传文件到 R2
		try (InputStream inputStream = new ByteArrayInputStream(imgBytes)) {
            UploadUtils.unificationUpload( imgUrl, inputStream, imgBytes.length, contentType);
//			CloudflareR2Utils.uploadFilePublic(
//					Const.CloudflareR2.R2_BUCKET_NAME,
//					imgUrl,
//					inputStream,
//					imgBytes.length,
//					contentType
//			);
		}

		// 构造返回对象
		Img img = new Img();
		img.setCoverheight(bi.getHeight());
		img.setCoversize(imgBytes.length);
		img.setCoverwidth(bi.getWidth());
		img.setCoverurl(imgUrl);
		img.setUrl(imgUrl);
		img.setUid(null);
		img.setFilename(uploadFile.getName());
		img.setHeight(bi.getHeight());
		img.setWidth(bi.getWidth());
		img.setSize((long)imgBytes.length);
		img.setTitle(uploadFile.getName());

		return img;
	}

}
