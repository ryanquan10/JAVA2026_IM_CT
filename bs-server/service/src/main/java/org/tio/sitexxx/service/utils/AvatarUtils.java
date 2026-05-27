
package org.tio.sitexxx.service.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.UploadFile;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.AutoAvatar;
import org.tio.sitexxx.service.model.conf.AutoAvatarPlate;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.hutool.ResourceUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;

/**
 * 头像处理工具类
 * @author lixinji
 * 2021年4月27日 下午3:51:32
 */
public class AvatarUtils {
	private static final Logger log = LoggerFactory.getLogger(AvatarUtils.class);

	/**
	 * 头像基础模板width
	 */
	private static final int AVATAR_BASE_WIDTH = 168;

	/**
	 * 头像基础模板height
	 */
	private static final int AVATAR_BASE_HEIGHT = 168;

	private static final int	ONE_IMG_WIDTH		= 40;									//九宫格中，单图的宽度
	/**
	 * 
	 */
	private static final int	BORDER_WIDTH		= 3;
	/**
	 * 
	 */
	private static final int	CANVANS_W			= ONE_IMG_WIDTH * 3 + 4 * BORDER_WIDTH;	//3张图 + 4个行距
	private static final int	CANVANS_H			= CANVANS_W;
	/**
	 * 若为一张图片
	 */
	private static final int	ONE_IMAGE_WIDTH		= CANVANS_H - (2 * BORDER_WIDTH);
	/**
	 * 若为2-4张图片
	 */
	private static final int	TWO_IMAGE_WIDTH		= (CANVANS_H - (3 * BORDER_WIDTH)) / 2;
	/**
	 * 若>=5张图片
	 */
	private static final int	FIVE_IMAGE_WIDTH	= (CANVANS_H - (4 * BORDER_WIDTH)) / 3;

	/**
	 * 基础头像底版配置
	 */
	private static List<AutoAvatarPlate> basePlateConf = null;

	/**
	 * 底版数量
	 */
	private static int plateSize = -1;

	static {
		if (basePlateConf == null) {
			basePlateConf = AutoAvatarPlate.dao.find("select id,r,g,b from auto_avatar_plate");
			if (basePlateConf != null) {
				plateSize = basePlateConf.size();
			}
		}
	}

	private AvatarUtils() {
	}

	/**
	 * 
	 * @param paths
	 * @param uid
	 * @return
	 * @author tanyaowu
	 * @throws Exception 
	 */
	public static Img generateGroupAvatar(List<String> paths, int uid) throws Exception {
		List<BufferedImage> biList = new ArrayList<BufferedImage>();
		int imgCount = paths.size();//图片张数
		int imageSize = 0;
		if (imgCount <= 1) {
			//若为一张图片
			imageSize = ONE_IMAGE_WIDTH;
		} else if (imgCount > 1 && imgCount < 5) {
			//若为2-4张图片
			imageSize = TWO_IMAGE_WIDTH;
		} else {
			//若>=5张图片
			imageSize = FIVE_IMAGE_WIDTH;
		}

		for (int i = 0; i < imgCount; i++) {
			BufferedImage resize2 = AvatarUtils.resize(paths.get(i), imageSize, imageSize, true);
			biList.add(resize2);
		}
		BufferedImage outImage = new BufferedImage(CANVANS_W, CANVANS_H, BufferedImage.TYPE_INT_RGB);
		// 生成画布
		Graphics g = outImage.getGraphics();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(new Color(226, 226, 226));
		// 通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
		g2d.clearRect(0, 0, CANVANS_W, CANVANS_H);
		// 开始拼凑 根据图片的数量判断该生成那种样式的组合头像目前为九种
		//		Integer size = biList.size();
		for (int i = 1; i <= imgCount; i++) {
			int index = i - 1;
			BufferedImage bi = biList.get(index);
			switch (imgCount) {
			case 1:
				g2d.drawImage(bi, BORDER_WIDTH, BORDER_WIDTH, null);
				break;
			case 2:
				if (i == 1) {
					g2d.drawImage(bi, BORDER_WIDTH, (CANVANS_W - imageSize) / 2, null);
				} else {
					g2d.drawImage(bi, 2 * BORDER_WIDTH + imageSize, (CANVANS_W - imageSize) / 2, null);
				}
				break;
			case 3:
				if (i == 1) {
					g2d.drawImage(bi, (CANVANS_W - imageSize) / 2, BORDER_WIDTH, null);
				} else {
					g2d.drawImage(bi, (i - 1) * BORDER_WIDTH + (i - 2) * imageSize, imageSize + (2 * BORDER_WIDTH), null);
				}
				break;
			case 4:
				if (i <= 2) {
					g2d.drawImage(bi, i * BORDER_WIDTH + (i - 1) * imageSize, BORDER_WIDTH, null);

				} else {
					g2d.drawImage(bi, (i - 2) * BORDER_WIDTH + (i - 3) * imageSize, imageSize + 2 * BORDER_WIDTH, null);
				}
				break;
			case 5:
				if (i <= 2) {
					g2d.drawImage(bi, (CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2 + (i - 1) * imageSize + (i - 1) * BORDER_WIDTH, (CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2,
					        null);
				} else {
					g2d.drawImage(bi, (i - 2) * BORDER_WIDTH + (i - 3) * imageSize, ((CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2) + imageSize + BORDER_WIDTH, null);
				}
				break;
			case 6:
				if (i <= 3) {
					g2d.drawImage(bi, BORDER_WIDTH * i + imageSize * (i - 1), (CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2, null);
				} else {
					g2d.drawImage(bi, ((i - 3) * BORDER_WIDTH) + ((i - 4) * imageSize), ((CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2) + imageSize + BORDER_WIDTH, null);
				}
				break;
			case 7:
				if (i <= 1) {
					g2d.drawImage(bi, 2 * BORDER_WIDTH + imageSize, BORDER_WIDTH, null);
				}
				if (i <= 4 && i > 1) {
					g2d.drawImage(bi, ((i - 1) * BORDER_WIDTH) + ((i - 2) * imageSize), 2 * BORDER_WIDTH + imageSize, null);
				}
				if (i <= 7 && i > 4) {
					g2d.drawImage(bi, ((i - 4) * BORDER_WIDTH) + ((i - 5) * imageSize), 3 * BORDER_WIDTH + 2 * imageSize, null);
				}
				break;
			case 8:
				if (i <= 2) {
					g2d.drawImage(bi, (CANVANS_W - 2 * imageSize - BORDER_WIDTH) / 2 + (i - 1) * imageSize + (i - 1) * BORDER_WIDTH, BORDER_WIDTH, null);
				}
				if (i <= 5 && i > 2) {
					g2d.drawImage(bi, ((i - 2) * BORDER_WIDTH) + ((i - 3) * imageSize), 2 * BORDER_WIDTH + imageSize, null);
				}
				if (i <= 8 && i > 5) {
					g2d.drawImage(bi, ((i - 5) * BORDER_WIDTH) + ((i - 6) * imageSize), 3 * BORDER_WIDTH + 2 * imageSize, null);
				}
				break;
			case 9:
				if (i <= 3) {
					g2d.drawImage(bi, (i * BORDER_WIDTH) + ((i - 1) * imageSize), BORDER_WIDTH, null);
				}
				if (i <= 6 && i > 3) {
					g2d.drawImage(bi, ((i - 3) * BORDER_WIDTH) + ((i - 4) * imageSize), 2 * BORDER_WIDTH + imageSize, null);
				}
				if (i <= 9 && i > 6) {
					g2d.drawImage(bi, ((i - 6) * BORDER_WIDTH) + ((i - 7) * imageSize), 3 * BORDER_WIDTH + 2 * imageSize, null);
				}
				break;
			default:
				break;
			}
		}

		String ext = "jpg";

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(outImage, ext, output);

		UploadFile uploadFile = new UploadFile();
		uploadFile.setData(output.toByteArray());
		uploadFile.setName("wxgroup_avatar." + ext);
		uploadFile.setSize(uploadFile.getData().length);
		Img img = ImgUtils.processImg(Const.UPLOAD_DIR.WX_GROUP_AVATAR, uid, uploadFile, 1F);
		return img;
	}

	/**
	 * 自动生成头像图片
	 * @param uid
	 * @param nickFristChar
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月14日 下午3:38:33
	 */
	public static String pressUserAvatar(String nickFristChar) throws Exception {
		if (!Const.USE_AUTO_AVATAR) {
			String avatar = AvatarService.nextAvatar(Const.UserSex.MALE + "");
			return avatar;
		}
		if (StrUtil.isNotEmpty(nickFristChar) && nickFristChar.length() > 1) {
			nickFristChar = nickFristChar.substring(0, 1).toUpperCase();
		}
		ICache cache = Caches.getCache(CacheConfig.AUTO_AVATAR);
		final String key = nickFristChar;
		String path = CacheUtils.get(cache, key, true, new FirsthandCreater<String>() {
			@Override
			public String create() {
				String sql = "select path from auto_avatar where chatindex = ? limit 0,1";
				String path = Db.use(Const.Db.TIO_SITE_CONF).queryStr(sql, key);
				if (StrUtil.isNotBlank(path)) {
					return path;
				}
				Img img = null;
				try {
					img = imgPressText(key + "_avatar.png", Const.UPLOAD_DIR.USER_BASE_AVATAR, key, Const.USER_AVATAR_BASE_BACKGROUND_IMG, Const.USER_AVATAR_MEDIUM_FONT_SIZE, 1);
					if (img == null || StrUtil.isBlank(img.getCoverurl())) {
						log.error("头像生成错误");
						return null;
					}
					AutoAvatar autoAvatar = new AutoAvatar();
					autoAvatar.setPath(img.getCoverurl());
					autoAvatar.setChatindex(key);
					autoAvatar.setRemark(key);
					autoAvatar.save();
				} catch (Exception e) {
					log.error("", e);
					return "";
				}
				return img.getCoverurl();
			}
		});
		if (StrUtil.isBlank(path)) {
			path = CacheUtils.get(cache, "#", true, new FirsthandCreater<String>() {
				@Override
				public String create() {
					String sql = "select path from auto_avatar where chatindex = ? limit 0,1";
					String path = Db.use(Const.Db.TIO_SITE_CONF).queryStr(sql, "#");
					if (StrUtil.isNotBlank(path)) {
						return path;
					}
					Img img = null;
					try {
						img = imgPressText("u#" + "_avatar.png", Const.UPLOAD_DIR.USER_BASE_AVATAR, "#", Const.USER_AVATAR_BASE_BACKGROUND_IMG, Const.USER_AVATAR_MEDIUM_FONT_SIZE,
						        1);
						if (img == null || StrUtil.isBlank(img.getCoverurl())) {
							return null;
						}
						AutoAvatar autoAvatar = new AutoAvatar();
						autoAvatar.setPath(img.getCoverurl());
						autoAvatar.setChatindex("#");
						autoAvatar.setRemark("#");
						autoAvatar.save();
					} catch (Exception e) {
						log.error("", e);
						return "";
					}
					return img.getCoverurl();
				}
			});
		}
		return path;
	}

	/**
	 * 按模板生成水印图片
	 * @param name 文件名称
	 * @param dir 文件上传路径
	 * @param uid 用户
	 * @param str 水印字
	 * @param basePlatePath 模板路径
	 * @param fontsize 字号大小
	 * @param alpha 透明度
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年1月14日 下午3:39:01
	 */
	@SuppressWarnings("deprecation")
	public static Img imgPressText(String name, String dir, String str, String basePlatePath, int fontsize, int alpha) throws Exception {
		Image srcImg = null;
		if (CollectionUtil.isEmpty(basePlateConf)) {
			File srcImgFile = new File(ResourceUtil.getAbsolutePath(basePlatePath));
			srcImg = ImageIO.read(srcImgFile);
		} else {
			srcImg = new BufferedImage(AVATAR_BASE_WIDTH, AVATAR_BASE_HEIGHT, BufferedImage.TYPE_INT_RGB);
			//取得图形
			Graphics g = srcImg.getGraphics();
			AutoAvatarPlate plate = getAvatarPlate();
			//设置颜色
			g.setColor(new Color(plate.getR(), plate.getB(), plate.getG()));
			//填充
			g.fillRect(0, 0, AVATAR_BASE_WIDTH, AVATAR_BASE_HEIGHT);
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImgUtil.pressText(srcImg, output, str, Color.WHITE, getMediumFont(0, fontsize), 0, 64, alpha);
		UploadFile uploadFile = new UploadFile();
		uploadFile.setData(output.toByteArray());
		uploadFile.setName(name);
		uploadFile.setSize(uploadFile.getData().length);
		try {
			Img img = ImgUtils.processImg(dir, uploadFile);
			return img;
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public static BufferedImage resize(String path, int height, int width, boolean bb) {

		try {
			double ratio = 0; // 缩放比例
			String urlPath = UploadUtils.resUrl(path);
			//			BufferedImage bi = ImageIO.read(new URL(urlPath));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			final HttpResponse response = cn.hutool.http.HttpRequest.get(urlPath).executeAsync();
			if (!response.isOk()) {
				throw new HttpException(path+" --- Server response error with status code: [{}]", response.getStatus());
			}
			response.writeBody(out, true, null);
			byte[] bytes = out.toByteArray();
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			BufferedImage bi = ImageIO.read(input);

			Image itemp = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			// 计算比例
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				if (bi.getHeight() > bi.getWidth()) {
					ratio = (new Integer(height)).doubleValue() / bi.getHeight();
				} else {
					ratio = (new Integer(width)).doubleValue() / bi.getWidth();
				}
				AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
				itemp = op.filter(bi, null);
			}
			if (bb) {
				// copyimg(filePath, "D:\\img");
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, width, height);
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				g.dispose();
				itemp = image;
			}
			return (BufferedImage) itemp;
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	/**
	 *    阿里惠普字体 
	 * @param ft
	 * @param fs
	 * @return
	 * @author lixinji
	 * 2020年1月14日 下午3:04:59
	 */
	public static Font getMediumFont(int ft, float fs) {
		String fontUrl = ResourceUtil.getAbsolutePath(Const.MEDIUM_FONT_TTF_FILE);
		InputStream is = null;
		Font definedFont = null;
		BufferedInputStream bis = null;
		try {
			is = new FileInputStream(new File(fontUrl));
			bis = new BufferedInputStream(is);
			definedFont = Font.createFont(ft, is);
			//设置字体大小，float型
			definedFont = definedFont.deriveFont(fs);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bis) {
					bis.close();
				}
				if (null != is) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return definedFont;
	}

	/**
	 * 随机获取基础头像底版配置
	 * @return
	 * @author lixinji
	 * 2020年1月15日 上午11:48:21
	 */
	public static AutoAvatarPlate getAvatarPlate() {
		int index = RandomUtil.randomInt(0, plateSize);
		return basePlateConf.get(index);
	}

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//		String path = "https://res.local.t-io.org/user/test2.png";
		//		String text = "张";
		//		BufferedImage img = WxGroupAvatarUtil.resize(path, 168, 168, true);
		//		ByteArrayOutputStream output = new ByteArrayOutputStream();
		//		ImgUtil.pressText(img,output, text, Color.WHITE, getMediumFont(0, 80), 0, 0, 1);
		//		UploadFile uploadFile = new UploadFile();
		//		uploadFile.setData(output.toByteArray());
		//		uploadFile.setName("wxgroup_avatar.png");
		//		uploadFile.setSize(uploadFile.getData().length);
		////		
		////		 BufferedImage buffImg = new BufferedImage(168, 168, BufferedImage.TYPE_INT_RGB);
		////		 Graphics2D g2d = buffImg.createGraphics();
		////		 g2d.setColor(new Color(r, g, b));
		//		try {
		//			ImgUtils.processImg("wx/lixinjitest", 12131, uploadFile, 1F);
		//		} catch (Exception e) {
		//			log.error("", e);
		//		}
		//width 生成图宽度
		// height 生成图高度
		//创建一个width xheight ，RGB高彩图，类型可自定
		BufferedImage img = new BufferedImage(168, 168, BufferedImage.TYPE_INT_RGB);
		//取得图形
		Graphics g = img.getGraphics();
		//设置颜色
		//填充
		g.setColor(new Color(108, 168, 238));
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		OutputStream output = new FileOutputStream("D:/lixinji/11.png");
		ImgUtil.pressText(img, output, "#", Color.WHITE, getMediumFont(0, 80), 0, 0, 1);
		//		//在d盘创建个文件
		//		File file=new File("D:/job/image/top.jpg");
		//		try{
		//		//以png方式写入，可改成jpg其他图片
		//			ImageIO.write(img, "jpg", file);
		//		}catch (IOException e){
		//			e.printStackTrace();
		//		}
	}
}
