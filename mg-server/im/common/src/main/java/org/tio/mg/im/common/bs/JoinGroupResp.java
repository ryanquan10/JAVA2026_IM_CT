
/**
 *
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;
import java.util.Objects;



/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class JoinGroupResp implements Serializable {
	private static final long serialVersionUID = -1153163111359189882L;

	private String	g;
	/**
	 * @see org.tio.mg.im.common.bs.JoinGroupResp.JoinGroupResult
	 */
	private Short	result;

	public String getG() {
		return g;
	}

	public void setG(String g) {
		this.g = g;
	}

	public Short getResult() {
		return result;
	}

	public void setResult(Short result) {
		this.result = result;
	}

	public JoinGroupResp() {
	}
	
	
	/**
	 * 加入群组响应码
	 * 
	 */
	public enum JoinGroupResult {
		/**
		 * 进入成功
		 */
		OK((short) 1),

		/**
		 * 组不存在
		 */
		FAIL_GROUP_NOT_EXIST((short) 2),

		/**
		 * 组满
		 */
		FAIL_GROUP_FULL((short) 3),

		/**
		 * 你在黑名单中
		 */
		FAIL_IN_BLACK_LIST((short) 4),
		/**
		 * 被踢
		 */
		FAIL_KICKED((short) 5),
		/**
		 * 不允许游客进行
		 */
		FAIL_TOURIST_NOT_ALLOWED((short) 6),
		/**
		 * 其它原因
		 */
		FAIL_OTHER((short) 99);

		public static JoinGroupResult from(Short value) {
			JoinGroupResult[] values = JoinGroupResult.values();
			for (JoinGroupResult v : values) {
				if (Objects.equals(v.value, value)) {
					return v;
				}
			}
			return null;
		}

		Short value;

		private JoinGroupResult(Short value) {
			this.value = value;
		}

		public Short getValue() {
			return value;
		}

		public void setValue(Short value) {
			this.value = value;
		}
	}

}
