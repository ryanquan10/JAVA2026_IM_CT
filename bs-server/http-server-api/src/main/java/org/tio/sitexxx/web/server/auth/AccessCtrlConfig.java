
package org.tio.sitexxx.web.server.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.ConfigUtils;
import org.tio.utils.json.Json;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author tanyaowu 
 * 2016年9月14日 下午5:10:04
 */
public class AccessCtrlConfig extends PropertiesConfiguration {
	//	private static AccessCtrlConfig me = null;

	private static Logger log = LoggerFactory.getLogger(AccessCtrlConfig.class);

	//	private static final String REQUESTMAPPING_SUFFIX = ".talent";

	//	private static Map<String, String> urlMap = null;

	private Set<String> allRequestMapping = null;

	/**
	 * 忽略配置项重复
	 */
	private boolean skipRepeat = false;

	private String configFile = "";

	public AccessCtrlConfig(String configFile, Set<String> allRequestMapping, boolean skipRepeat) {
		this.skipRepeat = skipRepeat;
		if (allRequestMapping == null) {
			this.allRequestMapping = new HashSet<>();
		} else {
			this.allRequestMapping = new HashSet<>(allRequestMapping);
		}
		this.configFile = configFile;

		try {
			ConfigUtils.initConfig(configFile, "utf-8", this);
		} catch (FileNotFoundException e) {
			log.error("", e);
		}

		Iterator<String> keys = this.getKeys();
		step1(keys);

		keys = this.getKeys();

		//		Set<String> allRequestMapping = WebApiInit.routes.PATH_METHOD_MAP.keySet();
		Map<String, Object> map = step2(keys);

		step3(map);

	}

	/**
	 * 
	 * @param path
	 * @return
	 * @author tanyaowu
	 */
	public Object getNeededRolecodes(String path) {
		Object neededRolecodes = getProperty(path);
		return neededRolecodes;
	}

	/**
	 * 第一步
	 * 1、给配置的servletpath前面加"/" 
	 * 2、配置文件中有，但在allRequestMapping中没有的path添加到allRequestMapping（只添加不带星号的path）
	 *
	 */
	private void step1(Iterator<String> keys) {
		PropertiesConfiguration tem = new PropertiesConfiguration();
		String configedServletPath;
		Object rolecodes;
		while (keys.hasNext()) {
			configedServletPath = keys.next();
			rolecodes = this.getProperty(configedServletPath);

			if (!configedServletPath.startsWith("/")) {
				configedServletPath = "/" + configedServletPath;
			}

			tem.addProperty(configedServletPath, rolecodes);

			if (!StrUtil.containsAny(configedServletPath, "*")) {
				if (!this.allRequestMapping.contains(configedServletPath)) {
					System.out.println(this.configFile + "没有提供：" + configedServletPath);
					this.allRequestMapping.add(configedServletPath);
				}
			}
		}
		this.clear();
		this.append(tem);
	}

	/**
	 * 第二步:<br>
	 * 1、把configedServletPath中带星号的具体化<br>
	 * 2、把角色和requestMapping映射好<br>
	 * 3、用没带星号的配置代替带星号的配置<br>
	 * 4、加上.talent后缀<br>
	 * @param configServletPaths
	 * @return
	 */
	private Map<String, Object> step2(Iterator<String> configServletPaths) {
		String configedServletPath;
		String initConfigedServletPath;
		Object rolecodes;
		Map<String, Object> asteriskServletPath = new HashMap<>(); //配置中带星号的servletpath
		Map<String, Object> notAsteriskServletPath = new HashMap<>(); //配置中没有带星号的servletpath
		Set<String> noMatchServletPath = new HashSet<>(); //在配置文件中配了，但是没有匹配到任何可请求的requestMapping
		Map<String, Object> tem1 = null;
		while (configServletPaths.hasNext()) {
			configedServletPath = configServletPaths.next();
			initConfigedServletPath = configedServletPath;
			rolecodes = this.getProperty(configedServletPath);

			if (configedServletPath.contains("*")) {
				tem1 = asteriskServletPath;
				configedServletPath = configedServletPath.replaceAll("\\*", ".*");//用".*"替代"*"
			} else {
				tem1 = notAsteriskServletPath;
			}

			boolean ismatched = false; //是否匹配到了
			for (String requestMapping : this.allRequestMapping) /*  /usercenter/beantocoin/addBeanToCoin  */
			{
				//requestMapping = requestMapping.substring(0); /*  usercenter/beantocoin/addBeanToCoin  */
				Pattern p = Pattern.compile(configedServletPath); // 正则表达式
				Matcher m = p.matcher(requestMapping); // 操作的字符串 
				boolean b = m.matches(); //返回是否匹配的结果

				if (b) {
					ismatched = true;
					if (rolecodes instanceof List) {
						@SuppressWarnings("unchecked")
						List<String> rolecodeList = (List<String>) rolecodes;
						for (String rolecode : rolecodeList) {
							addRoleToRequestmapping(requestMapping, rolecode, tem1);
						}
					} else {
						addRoleToRequestmapping(requestMapping, (String) rolecodes, tem1);
					}
				}
			}

			if (!ismatched) {
				noMatchServletPath.add(initConfigedServletPath);
			}
		}

		if (noMatchServletPath.size() > 0) {
			log.error("有{}个配置项没有找到映射路径，请检查是否配置有误:{}", noMatchServletPath.size(), Json.toJson(noMatchServletPath));
		}
		asteriskServletPath.putAll(notAsteriskServletPath); //这个会用没带星号的配置覆盖带星号的配置

		this.clear();

		Set<Entry<String, Object>> set = asteriskServletPath.entrySet();
		for (Entry<String, Object> entry : set) {
			this.addProperty(entry.getKey(), entry.getValue());
			//			ME.addProperty(entry.getKey() + REQUESTMAPPING_SUFFIX, entry.getValue());
		}
		return asteriskServletPath;
	}

	/**
	 *  只是简单地打印一下
	 * @param map
	 */
	private void step3(Map<String, Object> map) {
		//		Iterator<String> keys;
		//		keys = this.getKeys();
		//		Map<String, Object> allmap = new HashMap<>();
		//		while (keys.hasNext()) {
		//			String configedServletPath = keys.next();
		//			Object rolecodes = this.getProperty(configedServletPath);
		//			allmap.put(configedServletPath, rolecodes);
		//		}

		try {
			String writeMappingToFile = System.getProperty("tio.mvc.route.writeMappingToFile", "true");
			if ("true".equalsIgnoreCase(writeMappingToFile)) {
				FileUtil.writeString(Json.toFormatedJson(map), new File("/" + FileUtil.mainName(this.configFile) + ".js"), "utf-8");//.write(new File("d:/accessurlrole.js"), Json.toJson(map), "utf-8");
			}
			//			FileUtil.writeString(Json.toFormatedJson(allmap), new File("/" + FileUtil.mainName(this.configFile) + "_all.js"), "utf-8");//.write(new File("d:/accessurlrole2.js"), Json.toJson(allmap), "utf-8");
		} catch (Throwable e) {
			//			log.error(e.getMessage());
		}
	}

	/**
	 * 把角色关联到requestmapping
	 * @param requestMapping  /usercenter/agent/getSellMonthStatAjax
	 * @param rolecode
	 */
	@SuppressWarnings("unchecked")
	private void addRoleToRequestmapping(String requestMapping, String rolecode, Map<String, Object> map) {
		Object _rcs = map.get(requestMapping);
		if (_rcs == null) {
			map.put(requestMapping, rolecode);
			//			map.addProperty(requestMapping + REQUESTMAPPING_SUFFIX, rolecode);
		} else {
			if (!skipRepeat) {
				log.error("配置项重复{}", requestMapping);
			}

			if (_rcs instanceof List) {
				List<String> rclist = (List<String>) _rcs;
				if (!rclist.contains(rolecode)) {
					rclist.add(rolecode);
					map.put(requestMapping, rclist);
					//					map.addProperty(requestMapping + REQUESTMAPPING_SUFFIX, rclist);
				}
			} else {
				if (!_rcs.equals(rolecode)) {
					List<String> rclist = new ArrayList<>();
					rclist.add(rolecode);
					rclist.add((String) _rcs);
					map.put(requestMapping, rclist);
					//					map.addProperty(requestMapping + REQUESTMAPPING_SUFFIX, rclist);
				}
			}
		}
	}
}
