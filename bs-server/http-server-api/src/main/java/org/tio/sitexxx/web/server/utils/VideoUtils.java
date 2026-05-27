
package org.tio.sitexxx.web.server.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.model.main.Video;
import org.tio.utils.hutool.BetweenFormater;
import org.tio.utils.hutool.BetweenFormater.Level;

import cn.hutool.core.io.FileUtil;

/**
 * 参考：https://blog.csdn.net/lianzhang861/article/details/82014460
 * easyCV：https://github.com/eguid/easyCV
 * @author tanyaowu 
 * 2016年12月2日 下午5:23:11
 */
public class VideoUtils {
	private static Logger log = LoggerFactory.getLogger(VideoUtils.class);

	/**
	 * 
	 * @author tanyaowu
	 */
	public VideoUtils() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * 处理视频
	 * @param videoFilePath 视频路径
	 * @param coverFilePath 封面保存路径（本地全路径）
	 * @param coverWidth 封面的宽
	 * @param coverExt 封面后缀，形如jpg, png
	 * @throws Exception
	 * @author tanyaowu
	 */
	@SuppressWarnings("resource")
	public static Video processVideo(String videoFilePath, String coverFilePath, int coverWidth, String coverExt) throws Exception {
		long start = System.currentTimeMillis();
		File coverFile = new File(coverFilePath);
		if (!coverFile.exists()) {
			FileUtil.mkParentDirs(coverFile);
		}
		FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videoFilePath);
		ff.start();
		int frameCount = ff.getLengthInFrames(); //每秒帧数
		int i = 0;
		Frame f = null;
		while (i < frameCount) {
			// 去掉前面的帧，避免出现全黑的图片，依自己情况而定
			f = ff.grabImage();
			if ((i > 5) && (f.image != null)) {
				break;
			}
			i++;
		}

		Java2DFrameConverter converter = new Java2DFrameConverter();
		BufferedImage bufferedImage = converter.getBufferedImage(f);
		int initWidth = bufferedImage.getWidth();
		int initHeight = bufferedImage.getHeight();

		// 对截取的帧进行等比例缩放
		BufferedImage newBufferedImage = null;
		int newWidth = Math.min(coverWidth, initWidth);
		if (newWidth != initWidth) {
			int newHeight = initHeight;
			newHeight = (int) (((double) newWidth / initWidth) * initHeight);
			newBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
			Image img = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
			newBufferedImage.getGraphics().drawImage(img, 0, 0, null);
			ImageIO.write(newBufferedImage, coverExt, coverFile);
		} else {
			ImageIO.write(bufferedImage, coverExt, coverFile);
			newBufferedImage = bufferedImage;
		}

		long millseconds = ff.getLengthInTime() / (1000);
		long seconds = millseconds / 1000;
		double fps = ff.getFrameRate();
		//ff.flush();
		ff.stop();

		File videoFile = new File(videoFilePath);
		Video video = new Video();
		video.setFps(fps);
		video.setFramecount(frameCount);
		video.setSeconds((int) seconds);
		BetweenFormater betweenFormater = new BetweenFormater(millseconds, Level.SECOND);
		betweenFormater.format();
		video.setFormatedseconds(betweenFormater.format());
		video.setCoverheight(newBufferedImage.getHeight());
		video.setCoverwidth(newBufferedImage.getWidth());
		video.setCoversize((int) FileUtil.size(coverFile));
		video.setHeight(initHeight);
		video.setWidth(initWidth);
		video.setSize(FileUtil.size(videoFile));

		log.error("生成封面图耗时:{}ms, 视频[{}]，缩略图:[{}]", System.currentTimeMillis() - start, videoFilePath, coverFilePath);
		return video;

	}

	/**
	 * 处理视频
	 * @param videoFilePath 视频路径
	 * @throws Exception
	 * @author tanyaowu
	 */
	@SuppressWarnings("resource")
	public static BufferedImage generateCoverFromVideoNew(InputStream videoStream) throws Exception {
		File tempFile = File.createTempFile("video-", ".mp4");
		tempFile.deleteOnExit();
// 将 InputStream 写入临时文件
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = videoStream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}
		FFmpegFrameGrabber ff = new FFmpegFrameGrabber(tempFile);
		ff.start();
		int frameCount = ff.getLengthInFrames(); //每秒帧数
		int i = 0;
		Frame f = null;
		while (i < frameCount) {
			// 去掉前面的帧，避免出现全黑的图片，依自己情况而定
			f = ff.grabImage();
			if ((i > 5) && (f.image != null)) {
				break;
			}
			i++;
		}

		Java2DFrameConverter converter = new Java2DFrameConverter();
		BufferedImage bufferedImage = converter.getBufferedImage(f);

		// 对截取的帧进行等比例缩放
		ImageIO.write(bufferedImage, "jpg", tempFile);

		ff.stop();
		ff.release();
		return bufferedImage;

	}

	//	public static BufferedImage frameToBufferedImage(Frame frame) {
	//		//创建BufferedImage对象
	//		Java2DFrameConverter converter = new Java2DFrameConverter();
	//		BufferedImage bufferedImage = converter.getBufferedImage(frame);
	//		
	//        BufferedImage srcBi =converter.getBufferedImage(frame);
	//        int owidth = srcBi.getWidth();
	//        int oheight = srcBi.getHeight();
	//        // 对截取的帧进行等比例缩放
	//        int width = 800;
	//        int height = (int) (((double) width / owidth) * oheight);
	//        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	//        bi.getGraphics().drawImage(srcBi.getScaledInstance(width, height, Image.SCALE_SMOOTH),0, 0, null);
	//        try {
	//            ImageIO.write(bi, "jpg", targetFile);
	//        }catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//        
	//        
	//		return bi;
	//	}

	//	/**
	//	 * 获取视频总时长，单位：秒
	//	 * @param fileName
	//	 * @return
	//	 */
	//	public static float getVideoFileLength(String fileName, Video video) {
	//		File file = new File(fileName);
	//		if (!file.exists()) {
	//			return 0;
	//		}
	//
	//		float len = 0;
	//
	//		CvCapture capture = opencv_videoio.cvCreateFileCapture(fileName);
	//		try {
	//			// 获取视频总帧数
	//			long frameCount = (long) opencv_videoio.cvGetCaptureProperty(capture, org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_COUNT);
	//			// 获取视频每秒帧数
	//			long fps = (long) opencv_videoio.cvGetCaptureProperty(capture, org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FPS);
	//
	//			len = (float) frameCount / fps;
	//			
	//			if (video != null) {
	//				video.setFps(fps);
	//				video.setFramecount(frameCount);
	//				video.setSeconds((int)len);
	//				
	//				BetweenFormater betweenFormater = new BetweenFormater((int)len, Level.SECOND);
	//				betweenFormater.format();
	//				video.setFormatedseconds(betweenFormater.format());
	//			}
	//		} catch (Exception e) {
	//			log.error("", e);
	//		} finally {
	//			org.bytedeco.javacpp.opencv_videoio.cvReleaseCapture(capture);
	//		}
	//		return len;
	//	}



	/**
	 * 使用 JavaCV 从视频输入流中提取第一帧作为封面图（兼容 JavaCV 1.4.4）
	 */
	public static BufferedImage generateCoverFromVideo(InputStream videoStream) throws Exception {
		// 创建临时文件保存视频流内容
		File tempFile = File.createTempFile("video-", ".mp4");
		tempFile.deleteOnExit(); // JVM退出时自动删除

		// 将 InputStream 写入临时文件
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = videoStream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}

		FFmpegFrameGrabber grabber = null;
		Java2DFrameConverter converter = new Java2DFrameConverter();
		BufferedImage coverImage = null;

		try {
			// 使用本地文件路径初始化 FFmpegFrameGrabber
			grabber = FFmpegFrameGrabber.createDefault(tempFile.getAbsolutePath());
			grabber.start();

			// 抓取第一帧图像
			Frame frame = grabber.grab();
			if (frame == null) {
				throw new RuntimeException("无法提取视频封面，未获取到帧数据");
			}

			// 转换为 BufferedImage
			coverImage = converter.getBufferedImage(frame);

			grabber.stop();
		} finally {
			if (grabber != null) {
				grabber.release(); // 注意释放资源
			}
		}

		return coverImage;
	}

	/**
	 * 可选：将 BufferedImage 写成字节数组，便于上传 R2
	 */
	public static byte[] bufferedImageToBytes(BufferedImage image, String formatName) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, formatName, baos);
		return baos.toByteArray();
	}
}
