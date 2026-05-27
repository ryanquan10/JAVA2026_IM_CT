
package org.tio.sitexxx.service.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.json.Json;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

/**
 * @author tanyaowu
 */
public class MakePomFromJars {
	private static Logger log = LoggerFactory.getLogger(MakePomFromJars.class);

	static File rootFile = new File("d:/maked");

	static File			errorFile	= new File(rootFile, "error_pom.xml");
	static StringBuffer	errorsb		= new StringBuffer(2048);

	static File			pomFile	= new File(rootFile, "pom.xml");
	static StringBuffer	pomsb	= new StringBuffer(2048);

	static File			allFile	= new File(rootFile, "all_pom.xml");
	static StringBuffer	allsb	= new StringBuffer(2048);

	static File			notMatchingFile	= new File(rootFile, "not_matching_pom.xml");
	static StringBuffer	notmatchingsb	= new StringBuffer(2048);

	static File			allMsgFile	= new File(rootFile, "all_msg.json");
	static StringBuffer	allmsgsb	= new StringBuffer(2048);

	static File			timeInvalidFile	= new File(rootFile, "time_invalid.json");
	static StringBuffer	timeInvalidSb	= new StringBuffer(2048);

	static ExecutorService executors = Executors.newFixedThreadPool(4);

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (!rootFile.exists()) {
			FileUtil.mkdir(rootFile);
		}

		Element dependencys = new DOMElement("dependencys");

		File dir = new File("G:\\svn_qianyi\\SourceCodes\\Backend\\Java\\1080-fencang-agent\\trunk\\src\\main\\webapp\\WEB-INF\\lib"); //需生成pom.xml 文件的 lib路径
		File errorLib = new File("G:\\svn_qianyi\\SourceCodes\\Backend\\Java\\1080-fencang-agent\\trunk\\src\\main\\webapp\\WEB-INF\\lib-error2");
		File timeInvalidDir = new File("G:\\svn_qianyi\\SourceCodes\\Backend\\Java\\1080-fencang-agent\\trunk\\src\\main\\webapp\\WEB-INF\\lib-timeInvalid");

		FileUtil.del(timeInvalidDir);

		File[] files = dir.listFiles();

		CountDownLatch cdl = new CountDownLatch(files.length);
		for (File jar : files) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						JarInputStream jis = new JarInputStream(new FileInputStream(jar));
						Manifest mainmanifest = jis.getManifest();
						jis.close();
						if (mainmanifest == null) {
							return;
						}
						String bundleName = mainmanifest.getMainAttributes().getValue("Bundle-Name");
						String bundleVersion = mainmanifest.getMainAttributes().getValue("Bundle-Version");
						Element dependency = null;
						if (bundleName != null) {
							bundleName = bundleName.toLowerCase().replace(" ", "-");
							dependency = getDependency(bundleName, bundleVersion, jar);

						}
						if (dependency == null || dependency.elements().size() == 0) {
							bundleName = "";
							bundleVersion = "";
							String[] ns = jar.getName().replace(".jar", "").split("-");
							boolean startVersion = false;
							for (String s : ns) {
								if (startVersion || Character.isDigit(s.charAt(0))) {
									bundleVersion += s + "-";
									startVersion = true;
								} else {
									bundleName += s + "-";//if-crypto-sdk-3.3.2.jar
								}
							}
							if (bundleVersion.endsWith("-")) { //1.1-
								bundleVersion = bundleVersion.substring(0, bundleVersion.length() - 1); // 1.1
							}
							if (bundleName.endsWith("-")) {//activation-
								bundleName = bundleName.substring(0, bundleName.length() - 1);//activation
							}
							dependency = getDependency(bundleName, bundleVersion, jar);

						}

						if (dependency.elements().size() == 0) {
							dependency.add(new DOMElement("groupId").addText("not find"));
							dependency.add(new DOMElement("artifactId").addText(bundleName));
							dependency.add(new DOMElement("version").addText(bundleVersion));
							errorsb.append(jar.getPath()).append("\r\n");

							FileUtil.copy(jar, new File(errorLib, jar.getName()), true);
						} else {
							pomsb.append(dependency.asXML()).append("\r\n");
							dependencys.add(dependency);
						}

						allsb.append(dependency.asXML()).append("\r\n").append(jar.getPath()).append("\r\n\r\n");

						try {
							@SuppressWarnings("resource")
							JarFile jarFile = new JarFile(jar);
							Enumeration<JarEntry> entrys = jarFile.entries();

							Long stdTime = null;
							boolean copied = false;
							while (entrys.hasMoreElements()) {
								JarEntry jarEntry = entrys.nextElement();
								String name = jarEntry.getName();
								if (name.endsWith(".class")) {
									try {
										FileTime fileTime = jarEntry.getLastModifiedTime();
										Date date = new Date(fileTime.toMillis());
										int year = DateUtil.year(date);
										int month = DateUtil.month(date);
										if (stdTime == null) {
											stdTime = fileTime.toMillis();
										} else {
											//											long iv = Math.abs(stdTime - fileTime.toMillis());
											if (!copied && (year > 2016)) {
												DateUtil.formatDate(new Date(fileTime.toMillis()));
												timeInvalidSb.append(jar.getPath()).append("\r\n");
												FileUtil.copy(jar, new File(timeInvalidDir, year + "-" + month + jar.getName()), true);
												copied = true;
											}
										}

										//										// jar内的文件只能通过流处理
										//										InputStream in = StrUtils.class.getClassLoader().getResourceAsStream(name);
										//										File parent = new File(dir + name).getParentFile();
										//										if (!parent.exists()) {
										//											parent.mkdirs();
										//										}
										//										OutputStream out = new FileOutputStream(dir + name);
										//										IOUtils.copy(in, out);
									} catch (Exception e) {
										log.error("", e);
									}
								}
							}
						} catch (Exception e) {
							log.error("", e);
						}

					} catch (Exception e) {
						log.error("", e);
					} finally {
						cdl.countDown();
					}
				}
			};

			executors.execute(r);
		}

		try {
			cdl.await();
		} catch (InterruptedException e) {
			log.error("", e);
		}

		FileUtil.writeString(pomsb.toString(), pomFile, "utf-8");
		FileUtil.writeString(errorsb.toString(), errorFile, "utf-8");
		FileUtil.writeString(allsb.toString(), allFile, "utf-8");
		FileUtil.writeString(notmatchingsb.toString(), notMatchingFile, "utf-8");
		FileUtil.writeString(allmsgsb.toString(), allMsgFile, "utf-8");

		System.exit(0);

	}

	public static Element getDependency(String key, String ver, File jar) {
		Element dependency = new DOMElement("dependency");
		try {
			String url = "http://search.maven.org/solrsearch/select?q=a%3A%22" + key + "%22%20AND%20v%3A%22" + ver + "%22&rows=3&wt=json";
			org.jsoup.nodes.Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(30000).get();
			String elem = doc.body().text();
			JSONObject response = JSONObject.parseObject(elem).getJSONObject("response");
			if (response.containsKey("docs") && response.getJSONArray("docs").size() > 0) {
				JSONObject docJson = response.getJSONArray("docs").getJSONObject(0);
				docJson.put("jarFile", jar.getPath());

				String josnStr = Json.toFormatedJson(docJson);
				allmsgsb.append(josnStr).append("\r\n");

				String groupId = docJson.getString("g");
				String artifactId = docJson.getString("a");
				String version = docJson.getString("v");

				Element groupIdEle = new DOMElement("groupId");
				Element artifactIdEle = new DOMElement("artifactId");
				Element versionEle = new DOMElement("version");

				groupIdEle.addText(groupId);
				artifactIdEle.addText(artifactId);
				versionEle.addText(version);

				dependency.add(groupIdEle);
				dependency.add(artifactIdEle);
				dependency.add(versionEle);

				if (!Objects.equals(artifactId + "-" + version, FileUtil.mainName(jar))) {
					notmatchingsb.append(josnStr).append("\r\n");
				}
			}
		} catch (Exception e) {
			log.error(jar.getPath(), e);
		}
		return dependency;
	}

	//	public static class Dependency {
	//		private String	groupId;
	//		private String	artifactId;
	//		private String	version;
	//	}
}
