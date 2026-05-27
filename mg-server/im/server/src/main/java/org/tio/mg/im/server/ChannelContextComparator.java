
/**
 * 
 */
package org.tio.mg.im.server;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.mg.im.server.utils.ImUtils;
import org.tio.mg.service.model.main.Role;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.UserRoleService;

import cn.hutool.core.util.StrUtil;

/**
	1、 自反性：x，y 的比较结果和 y，x 的比较结果相反。
	2、传递性：x>y,y>z,则 x>z。
	3、对称性：x=y,则 x,z 比较结果和 y，z 比较结果相同。
 * @author tanyaowu
 *
 */
public class ChannelContextComparator implements Comparator<ChannelContext> {
	//	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ChannelContextComparator.class);

	// = null时不排序
	public static final ChannelContextComparator me = null;//new ChannelContextComparator();

	private ChannelContextComparator() {
	}

	/**
	 * o2在前面请返回1
	 * o1在前面请返回-1
	 */
	@Override
	public int compare(ChannelContext o1, ChannelContext o2) {
		if (o1 == o2) {
			//			log.error("------------------俩ChannelContext对象是同一个对象");
			return 0;
		}

		User u1 = ImUtils.getHandshakeUser(o1);
		User u2 = ImUtils.getHandshakeUser(o2);

		if (u1 == null && u2 == null) { //都为null
			return compareWhenEqual(o1, o2);
		} else if (u1 != null && u2 == null) {
			return -1;
		} else if (u1 == null && u2 != null) {
			return 1;
		} else { //u1, u2都不为null
			boolean isDoc1 = UserRoleService.hasRole(u1, Role.ALLOW_READ_DOC);
			boolean isDoc2 = UserRoleService.hasRole(u2, Role.ALLOW_READ_DOC);

			if (isDoc1 && !isDoc2) {
				return -1;
			} else if (!isDoc1 && isDoc2) {
				return 1;
			}

			Integer level1 = u1.getLevel();
			Integer level2 = u2.getLevel();
			if (level2 > level1) {
				return 1;
			} else if (level1 > level2) {
				return -1;
			} else { //level相等
				return compareWhenEqual(o1, o2);
			}
		}
	}

	/**
	 * 当都为游客或为同一个用户时
	 * o2在前面请返回1
	 * o1在前面请返回-1
	 */
	private int compareWhenEqual(ChannelContext o1, ChannelContext o2) {
		String cid1 = o1.getId();
		String cid2 = o2.getId();
		int ret = StrUtil.compare(cid1, cid2, false);
		if (ret > 0) {
			return 1;
		} else if (ret < 0) {
			return -1;
		} else {
			log.error("ChannelContext.id[{}][{}]竟然一样", cid1, cid2);

		}
		return ret;

		//		long xx = o2.stat.timeCreated - o1.stat.timeCreated;
		//
		//		System.out.println("o2.stat.timeCreated: " + o2.stat.timeCreated + ", id: " + o2.getId());
		//		System.out.println("o1.stat.timeCreated: " + o1.stat.timeCreated + ", id: " + o1.getId());
		//		System.out.println("xxxxxxxxxxxxxxxxxxx: " + xx);
		//
		//		if (xx > 0) {//后进的在前面（2在前面 ）
		//			return 1;
		//		} else if (xx < 0) {//后进的在前面（1在前面 ）
		//			return -1;
		//		} else {
		//			String cid1 = o1.getId();
		//			String cid2 = o2.getId();
		//			int ret = StrUtil.compare(cid1, cid2, false);//cid1.compareTo(cid2);
		//			if (ret > 0) {
		//				return 1;
		//			} else if (ret < 0) {
		//				return -1;
		//			} else {
		//				log.error("------------------ChannelContext.id[{}][{}]竟然一样", cid1, cid2, new RuntimeException());
		//				return ret;
		//			}
		//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//如果不想按升序进行排序，就需要使用TreeSet(Comparator<? super E> comparator)的构造器，自己重新编写排序规则  
		//set降序输出  
		Set<String> treeSet = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				System.out.println("o1:" + o1 + ", o2:" + o2);
				return Integer.parseInt(o2) - (Integer.parseInt(o1));
			}
		});
		System.out.println("add 18, " + treeSet);
		treeSet.add("18");

		System.out.println("add 4, " + treeSet);
		treeSet.add("4");

		System.out.println("add 2, " + treeSet);
		treeSet.add("2");

		System.out.println("add 16, " + treeSet);
		treeSet.add("16");

		System.out.println("add 18, " + treeSet);
		treeSet.add("18");

		System.out.println("add 23, " + treeSet);
		treeSet.add("23");

		System.out.println("add 88, " + treeSet);
		treeSet.add("88");

		System.out.println("add 1, " + treeSet);
		treeSet.add("1");

		System.out.println("add 15, " + treeSet);
		treeSet.add("15");

		int x = 77;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 45;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 23;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 68;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 123;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 900;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		x = 900;
		System.out.println("add " + x + ", " + treeSet);
		treeSet.add(x + "");

		for (String info : treeSet) {
			System.out.println(info);
		}
	}

}
