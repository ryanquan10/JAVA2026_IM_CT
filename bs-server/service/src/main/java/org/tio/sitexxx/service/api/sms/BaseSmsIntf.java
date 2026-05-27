
package org.tio.sitexxx.service.api.sms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 基础短信业务接口
 * @author lixinji
 * 2020年12月10日 下午3:46:19
 */
public interface BaseSmsIntf<T extends BaseSmsVo, R extends BaseSmsResultVo> {

	/**
	 * 初始化vo
	 * @param mobile 移动端号码
	 * @param bizType 业务类型-见<BaseSmsVo.BaseSmsBizType>
	 * @param code 短信验证code
	 * @param extParams 扩展参数
	 * @return
	 * @author lixinji
	 * 2020年12月15日 下午10:23:13
	 */
	public T initSmsVo(String mobile, Short bizType, String code, Map<String, String> extParams);

	/**
	 * 验证码业务类型发送
	 * @param smsVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月15日 下午10:21:56
	 */
	public R sendCode(T smsVo, String code) throws Exception;

	/**
	 * 非验证码传入发送
	 * @param smsVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月15日 下午10:21:50
	 */
	public R send(T smsVo) throws Exception;

	/**
	 * 获取code
	 * @param smsVo
	 * @param isAdd:不存在是否生成：true:生成
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月15日 下午10:21:44
	 */
	public String getCode(T smsVo, boolean isAdd) throws Exception;

	/**
	 * 更新code
	 * @param smsVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月15日 下午10:21:38
	 */
	public R updateCode(T smsVo) throws Exception;

	/**
	 * 删除code
	 * @param smsVo
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月15日 下午10:21:35
	 */
	public R delCode(T smsVo) throws Exception;

	Boolean verifyCode(String mobile, String code) throws IOException;
}
