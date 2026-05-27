
package org.tio.sitexxx.im.server.timetask;

/**
 * @author tanyaowu 
 * 2016年10月29日 下午8:31:08
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.tio.sitexxx.service.model.conf.Avatar;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.utils.hutool.StrUtil;

import ch.qos.logback.core.util.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;

/**
 * 头像批量下载
 * 2015-5-29
 * @author passion
 *
 */
public class DownloadAvatar {
	public static final List<String> URLS = new ArrayList<>(10000);

	/**
	 * 女性头像：https://www.woyaogexing.com/touxiang/nv/
	 * 小清新：https://www.woyaogexing.com/touxiang/z/nvxqx/
	 * 微信女：
	 * 		https://www.woyaogexing.com/touxiang/z/wxnv/
	 * 		https://www.woyaogexing.com/touxiang/z/wxnv/index_2.html
	 * 		https://www.woyaogexing.com/touxiang/z/wxnv/index_3.html
	 * 风景：https://www.woyaogexing.com/touxiang/fengjing/
	 * 微信风景：https://www.woyaogexing.com/touxiang/z/wxfengjing/
	 * 微信卡通：  https://www.woyaogexing.com/touxiang/z/wxkatong/
	 * 			https://www.woyaogexing.com/touxiang/z/wxkatong/index_2.html
	 */
	private static final String	DOWNLOAD_URL	= "https://www.woyaogexing.com/touxiang/z/wxkatong/index_2.html";
	private static final String	TYPE			= "wxkatong2";
	//图片存放路径
	private static final String savePath = "e:/download/avatar/" + TYPE + "/";

	private static final Log log = LogFactory.getLog(DownloadAvatar.class);

	public static void main(String[] args) throws Exception {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new DownloadAvatar().download();
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}).start();
	}

	public static String nextUrl() {
		if (URLS.size() == 0) {
			return "https://tiocloud.com/img/case/nb.png";
		}
		return URLS.get(RandomUtil.randomInt(0, URLS.size()));
	}

	private void download() throws Exception {
		String html = null;
		String[] imgUrls = null;
		File file = new File(savePath);
		if (!file.exists())
			file.mkdirs();
		//		for (int i = 1; i < 366; i++) {
		//			
		//		}

		html = getHtml(DOWNLOAD_URL);
		List<String> urls = parseUrl(html);

		for (String url : urls) {
			/**
			 * <li class="tx-img">
			 * 		<a href="//img2.woyaogexing.com/2018/11/06/2b52e901f4210807!480x480.jpg" class="swipebox">
			 * 			<img class="lazy" src="//img2.woyaogexing.com/2018/11/06/2b52e901f4210807!480x480.jpg" width="200" height="200">
			 * 		</a>
			 * </li>
			 */
			try {
				html = getHtml(url);
				imgUrls = parseImgUrl(html);
				new Thread(new DownloadImgThread(imgUrls)).start();
			} catch (Exception e) {
				log.error("出错了，URL：" + url, e);
			}
		}

	}

	private List<String> parseUrl(String html) {
		log.info("正在分析图像地址...");
		if (html == null)
			return null;
		Document doc = Jsoup.parse(html);
		Elements imgElements = doc.select(".txList a");
		final List<String> urls = new ArrayList<>(imgElements.size());
		for (int i = 0; i < imgElements.size(); i++) {
			String href = imgElements.get(i).attr("href");
			if (href.startsWith("javascript:")) {
				continue;
			}

			if (!href.endsWith(".html")) {
				continue;
			}
			href = "https://www.woyaogexing.com" + href;

			//			System.out.println(href);
			urls.add(href);
		}
		return urls;
	}

	/**
	 * 获得妹子图页面html字符串，供后续分析其中的妹子img地址
	 * @param url
	 * @return
	 */
	public String getHtml(String url) {
		try {
			log.info("正在获取页面:" + url);
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
				CloseableHttpResponse response = httpClient.execute(httpGet);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							return EntityUtils.toString(entity, "UTF-8");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		} catch (ParseException e) {
			log.error("出错了，URL：" + url, e);
			return null;
		}
	}

	/**
	 * 分析html字符串中的妹子图片地址
	 * @param html
	 * @return 返回的数组包含妹子图片地址
	 */
	private String[] parseImgUrl(String html) {

		/**
		 * <li class="tx-img">
		 * 		<a href="//img2.woyaogexing.com/2018/11/06/2b52e901f4210807!480x480.jpg" class="swipebox">
		 * 			<img class="lazy" src="//img2.woyaogexing.com/2018/11/06/2b52e901f4210807!480x480.jpg" width="200" height="200">
		 * 		</a>
		 * </li>
		 */

		log.info("正在分析图像地址...");
		if (html == null)
			return null;
		Document doc = Jsoup.parse(html);
		Elements imgElements = doc.select(".tx-img a img");
		final String[] img_urls = new String[imgElements.size()];
		for (int i = 0; i < imgElements.size(); i++) {
			img_urls[i] = imgElements.get(i).attr("src");
		}
		return img_urls;
	}

	//下载图片线程
	static class DownloadImgThread implements Runnable {
		private String[]												imgUrls;
		private static final java.util.concurrent.atomic.AtomicInteger	FILE_NAME_SEQ	= new AtomicInteger();

		public DownloadImgThread(String[] imgUrls) {
			this.imgUrls = imgUrls;
		}

		@Override
		public void run() {
			if (imgUrls != null) {
				for (String imgUrl : imgUrls)
					downloadImg(imgUrl);
			}
		}

		public void downloadImg(String imgUrl) {
			//			System.out.println(imgUrl);
			//			System.out.println(URLUtil.getPath(imgUrl));

			if (StrUtil.startWith(imgUrl, "//")) {
				imgUrl = "https:" + imgUrl;
			}
			//			
			String path = URLUtil.getPath(imgUrl);
			String fullpath = FilenameUtils.getFullPath(path);
			path = fullpath + FILE_NAME_SEQ.incrementAndGet() + "." + FilenameUtils.getExtension(path);
			try {
				//				if (URLS.size() > 100000) {
				//					return;
				//				}

				URLS.add(imgUrl);
				//				if (1 == 1) {
				//					return;
				//				}
			} catch (Exception e1) {
				log.error(e1.toString(), e1);
			}

			//
			log.info("正在下载头像库：" + imgUrl);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000).build();
			HttpGet httpGet = new HttpGet(imgUrl);
			httpGet.setConfig(requestConfig);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				CloseableHttpResponse response = httpClient.execute(httpGet);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							File f = new File(savePath, path);
							FileUtil.createMissingParentDirectories(f);

							entity.writeTo(new FileOutputStream(f));
							log.info("保存图片到：" + f.getPath());

							Avatar avatar = new Avatar();
							avatar.setInitUrl(imgUrl);
							avatar.setPath(path);
							avatar.setType(TYPE);
							try {
								AvatarService.me.save(avatar);
							} catch (Exception e) {
								//log.error("", e);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					response.close();
				}
				Thread.sleep(800L);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
