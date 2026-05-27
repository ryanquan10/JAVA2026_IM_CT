
package org.tio.sitexxx.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.model.conf.Dict;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 字典数据加载
 * 
 */
public class DictService {

	private static Logger log = LoggerFactory.getLogger(DictService.class);

	public static final DictService me = new DictService();

	final static Dict dictDao = new Dict().dao();

	/**
	 * 字典缓存 key: parentCode / code, value: List<Dict> / <Dict>
	 */
	private static Map<String, List<Dict>> pcode_dictlist_map = null;

	private static Map<String, Dict> code_dict_map = null;

	/**
	 * 清空字典缓存
	 * 
	 */
	public static void clean() {
		try {
			LockUtils.runWriteOrWaitRead(DictService.class.getName(), DictService.class, () -> {
				pcode_dictlist_map = null;
				code_dict_map = null;
			});
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private static String key(String pcode, String code) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(pcode);
		stringBuilder.append(":");
		stringBuilder.append(code);
		String ret = stringBuilder.toString();
		return ret;
	}

	/**
	 * 获取
	 * 
	 * @param pcode
	 * @param code
	 * @return
	 * @author tanyaowu
	 */
	public static Dict getDict(String pcode, String code) {
		loadAll();
		return code_dict_map.get(key(pcode, code));
	}

	/**
	 * 
	 * @param pcode
	 * @param codeStrings "im,link,oa"
	 * @return
	 * @author tanyaowu
	 */
	public static List<Dict> getDictList(String pcode, String codeStrings) {
		loadAll();
		List<Dict> children = getChildren(pcode);
		if (children == null) {
			return null;
		}

		String[] codeArray = StrUtil.splitToArray(codeStrings, ",");
		List<Dict> ret = new ArrayList<>(codeArray.length);
		for (String code : codeArray) {
			Dict dict = getDict(pcode, code);
			if (dict != null) {
				ret.add(dict);
			}
		}
		return ret;
	}

	/**
	 * 获取父节点下的子节点信息
	 * 
	 * @param pcode
	 * @return
	 * 
	 */
	public static List<Dict> getChildren(String pcode) {
		loadAll();
		return pcode_dictlist_map.get(pcode);
	}

	/**
	 * 加载所有字典
	 * 
	 * @author tanyaowu
	 */
	private static void loadAll() {
		if (pcode_dictlist_map == null) {
			try {
				LockUtils.runWriteOrWaitRead(DictService.class.getName(), DictService.class, () -> {
					if (pcode_dictlist_map == null) {
						pcode_dictlist_map = new HashMap<>();
						code_dict_map = new HashMap<>();

						List<Dict> list = dictDao.find("select id,name,code,pcode from dict where status = ? order by orderby", Const.Status.NORMAL);
						for (Dict dict : list) {
							List<Dict> list2 = pcode_dictlist_map.get(dict.getPcode());
							if (list2 == null) {
								list2 = new ArrayList<>();
								pcode_dictlist_map.put(dict.getPcode(), list2);
							}
							list2.add(dict);
							code_dict_map.put(key(dict.getPcode(), dict.getCode()), dict);
						}
					}
				});
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
