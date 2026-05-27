
package org.tio.sitexxx.service.pay.base;

import java.util.Map;

/**
 * 响应格式化接口
 * 
 * @author lixinji
 * 2021年3月9日 下午3:39:33
 */
public interface BaseRespFormatPay {

	/**
	 * 获取merid
	 * @author lixinji
	 * 2021年3月10日 下午2:24:05
	 */
	String getMerid();

	/**
	 * 
	 * 开户
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:22:44
	 */
	Map<String, Object> openUser(Map<String, Object> resp, Integer uid);

	/**
	 * 修改开户信息
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年11月12日 下午3:05:22
	 */
	Map<String, Object> updateUser(Map<String, Object> resp, Integer uid);

	/**
	 * 绑定银行卡
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:26:10
	 */
	Map<String, Object> bindBankCard(Map<String, Object> resp, Integer uid);

	/**
	 * 确认绑卡
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午5:41:04
	 */
	Map<String, Object> bindBankCardConfirm(Map<String, Object> resp, Integer uid);

	/**
	 * 解绑银行卡
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:27:00
	 */
	Map<String, Object> removeBankCard(Map<String, Object> resp, Integer uid);

	/**
	 * 用户余额
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:15:19
	 */
	Map<String, Object> getWalletInfo(Map<String, Object> resp, Integer uid);

	/**
	 * 充值
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:18
	 */
	Map<String, Object> recharge(Map<String, Object> resp, Integer uid);

	/**
	 * 充值回调
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:18
	 */
	Map<String, Object> rechargeCallback(Map<String, Object> req, Integer uid);

	/**
	 * 充值确认接口
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 上午11:52:42
	 */
	Map<String, Object> rechargeConfirm(Map<String, Object> resp, Integer uid);

	/**
	 * 充值查询
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 上午10:42:04
	 */
	Map<String, Object> rechargeQuery(Map<String, Object> resp, Integer uid);

	/**
	 * 发红包
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Map<String, Object> sendRedpacket(Map<String, Object> resp, Integer uid);

	/**
	 * 红包查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年12月1日 下午1:47:29
	 */
	Map<String, Object> redpacketQuery(Map<String, Object> resp, Integer uid);

	/**
	 * 抢红包
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月19日 上午10:08:07
	 */
	Map<String, Object> grabRedpacket(Map<String, Object> resp, Integer uid);

	/**
	 * 提现
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:10:00
	 */
	Map<String, Object> withhold(Map<String, Object> resp, Integer uid);

	/**
	 * 提现回调
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:10:00
	 */
	Map<String, Object> withholdCallback(Map<String, Object> req, Integer uid);

	/**
	 * 提现查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 下午2:28:45
	 */
	Map<String, Object> withholdQuery(Map<String, Object> resp, Integer uid);

	/**
	 * 转账
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:10:00
	 */
	Map<String, Object> transfer(Map<String, Object> resp, Integer uid);

	/**
	 * 转账查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午3:07:36
	 */
	Map<String, Object> transferQuery(Map<String, Object> resp, Integer uid);

}
