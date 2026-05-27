
package org.tio.mg.service.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tio.jfinal.kit.StrKit;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.UserService;

/**
 * 艾特某人的工具类，小徐同学友情提供，稍作修改
 * @author tanyaowu
 */
public class AtUtils {
	static final Pattern p = Pattern.compile("@([^@\\s:,;：，；　<&]{1,})([\\s:,;：，；　<&]{0,1})");

	/**
	 * 匹配 @xxx 生成链接，将生成过链接的账号 id 存放在 referUids，以供后续生成 remind 记录
	 */
	public static String buildAtMeLink(String content, Set<Integer> referUids) {
		if (StrKit.isBlank(content)) {
			return content;
		}

		StringBuilder ret = new StringBuilder();
		Matcher matcher = p.matcher(content);
		int pointer = 0;
		while (matcher.find()) {
			ret.append(content.substring(pointer, matcher.start()));
			String nick = matcher.group(1);

			User user = UserService.ME.getByNick(nick);
			//			User account = User.dao.findByNickname(nick, "id");
			if (user != null) {
				ret.append("<a href=\"/u/").append(user.getId()).append("\" target=\"_blank\" class=\"at-me\">").append("@").append(nick).append("</a>");
				ret.append(matcher.group(2));

				if (!referUids.contains(user.getId())) {
					referUids.add(user.getId());
				}
			} else {
				ret.append(matcher.group());
			}

			pointer = matcher.end();
		}
		ret.append(content.substring(pointer));
		return ret.toString();
	}

//	/**
//	 * 将 model 中的 attrName 属性内容创建 at me 链接
//	 */
//	public static Set<Integer> buildAtMeLink(Model<?> model, String attrName) {
//		Set<Integer> referUids = new HashSet<Integer>();
//		String content = model.getStr(attrName);
//		if (StrKit.notBlank(content)) {
//			content = buildAtMeLink(content, referUids);
//			model.set(attrName, content);
//		}
//		return referUids;
//	}
//
//	/**
//	 * 将 model 中的 content 属性内容创建 at me 链接
//	 */
//	public static Set<Integer> buildAtMeLink(Model<?> model) {
//		return buildAtMeLink(model, "content");
//	}
}
