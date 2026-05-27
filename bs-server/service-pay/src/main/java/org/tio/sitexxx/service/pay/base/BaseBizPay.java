
package org.tio.sitexxx.service.pay.base;

import java.util.Map;

import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.vo.BindCardConfirmVo;
import org.tio.sitexxx.service.vo.BindCardVo;
import org.tio.sitexxx.service.vo.GrabRedpacketVo;
import org.tio.sitexxx.service.vo.OpenUserVo;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.RechargeQueryVo;
import org.tio.sitexxx.service.vo.RechargeVo;
import org.tio.sitexxx.service.vo.RedpacketQueryVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;
import org.tio.sitexxx.service.vo.UnBindCardVo;
import org.tio.sitexxx.service.vo.WalletVo;
import org.tio.sitexxx.service.vo.WithholdQueryVo;
import org.tio.sitexxx.service.vo.WithholdVo;

/**
 * 支付回调业务接口
 * @param <PayQuest>
 * @param <Resp>
 * @author lixinji
 * 2020年10月27日 上午11:20:10
 */
public interface BaseBizPay {

	/**
	 * 
	 * 开户
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:22:44
	 */
	Ret open(OpenUserVo openVo, HttpRequest request);

	/**
	 * 修改开户信息
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年11月12日 下午3:05:22
	 */
	Ret updateUser(Map<String, Object> resp, Boolean isAtom);

	/**
	 * 实名认证
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:23:55
	 */
	Ret authRealname(Map<String, Object> resp, Boolean isAtom);

	/**
	 * 绑定银行卡
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:26:10
	 */
	Ret bindBankCard(BasePayResp resp, BindCardVo cardVo, Boolean isAtom);

	/**
	 * 绑定银行卡
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:26:10
	 */
	Ret bindBankCardConfirm(BasePayResp resp, BindCardConfirmVo cardVo, Boolean isAtom);

	/**
	 * 解绑银行卡
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:27:00
	 */
	Ret removeBankCard(BasePayResp resp, UnBindCardVo cardVo, Boolean isAtom);

	/**
	 * 获取银行卡列表
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:27:18
	 */
	Ret getBankCards(Map<String, Object> resp);

	/**
	 * 用户余额
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:15:19
	 */
	Ret getWalletInfo(BasePayResp resp);

	/**
	 * 钱包明细
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Ret getCoinItems(Map<String, Object> resp);

	/**
	 * 明细信息
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:28:51
	 */
	Ret getCoinItemInfo(Map<String, Object> resp);

	/**
	 * 充值
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午7:37:00
	 */
	Ret recharge(BasePayResp resp, RechargeVo rechargeVo, Boolean isAtom);

	/**
	 * 充值确认
	 * @param resp
	 * @param rechargeVo
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月11日 上午11:44:51
	 */
	Ret rechargeConfirm(BasePayResp resp, RechargeConfirmVo rechargeVo, Boolean isAtom);

	/**
	 * @param resp
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月11日 下午12:02:05
	 */
	Ret rechargeCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom);

	/**
	 * 充值查询
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 上午10:42:04
	 */
	Ret rechargeQuery(HttpRequest request, BasePayResp resp, RechargeQueryVo rechargeVo, Boolean isAtom);

	/**
	 * 消费
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Ret cost(Map<String, Object> resp);

	/**
	 * 退款
	 * @param PayQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Ret back(Map<String, Object> resp);

	/**
	 * 红包初始化
	 * @param redpacketVo
	 * @return
	 * @author lixinji
	 * 2021年3月15日 下午6:00:09
	 */
	Ret initRedpacket(SendRedpacketVo redpacketVo, Boolean isAtom);

	/**
	 * 红包快捷支付
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月15日 下午7:37:00
	 */
	Ret quickRedpacket(BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom);

	/**
	 * 红包支付
	 * @param resp
	 * @param redpacketVo
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月16日 下午5:40:07
	 */
	Ret payRedpacket(HttpRequest request, BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom);

	/**
	 * 发红包
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月18日 下午6:08:30
	 */
	Ret sendRedpacket(Map<String, Object> resp);

	/**
	 * 红包查询
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年12月1日 下午1:47:29
	 */
	Ret redpacketQuery(HttpRequest request, BasePayResp resp, RedpacketQueryVo queryVo, Boolean isAtom);

	/**
	 * 抢红包
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月19日 上午10:08:07
	 */
	Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom);

	/**
	 * @param resp
	 * @param random
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月17日 上午11:18:53
	 */
	Ret grabRedpacketCallback(HttpRequest request, BasePayResp resp, GrabRedpacketVo grabVo, Boolean isAtom);

	/**
	 * 提现回调
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:11:15
	 */
	Ret withhold(BasePayResp resp, WithholdVo withholdVo, Boolean isAtom);

	/**
	 * 提现回调
	 * @param resp
	 * @param result
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月11日 下午4:00:28
	 */
	Ret withholdCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom);

	/**
	* @param resp
	* @param result
	* @param isAtom
	* @return
	* @author lixinji
	* 2021年3月16日 下午6:36:31
	*/
	Ret redpacketCallback(HttpRequest request, BasePayResp resp, Boolean result, Boolean isAtom);

	/**
	 * 提现查询
	 * @param PayQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 下午2:28:45
	 */
	Ret withholdQuery(HttpRequest request, BasePayResp resp, WithholdQueryVo withholdVo, Boolean isAtom);

	/**
	 * @param resp
	 * @param queryVo
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月24日 下午4:51:58
	 */
	Ret redpacketJobQuery(BasePayResp resp, RedpacketQueryVo queryVo, Boolean isAtom);

	/**
	 * @param redPacket
	 * @param isAtom
	 * @return
	 * @author lixinji
	 * 2021年3月24日 下午6:17:38
	 */
	public Ret redpacketTimeOut(WxWalletSendRedPacket redPacket, Boolean isAtom);

	/**
	 * 新增coin
	 * @param cny
	 * @param uid
	 * @param mode
	 * @param coinflag
	 * @param bizid
	 * @param resp
	 * @return
	 * @author lixinji
	 * 2021年3月19日 下午3:47:40
	 */
	Ret coinAdd(Long cny, Integer uid, Short mode, Short coinflag, String remark, Integer bizid, Short status, Map<String, Object> resultMap, String starttime, String endtime,
	        Boolean isAtom);

	/**
	 * 同步钱包
	 * @param resp
	 * @param walletVo
	 * @return
	 * @author lixinji
	 * 2021年3月19日 下午3:48:29
	 */
	Ret synWallet(Map<String, Object> resultMap, WalletVo walletVo, Boolean isAtom);

	/**
	 * @param rid
	 * @param lock
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:55:46
	 */
	WxWalletSendRedPacket getRedPacketLock(Integer rid, Boolean lock);

	/**
	 * @param redPacket
	 * @param status
	 * @param lock
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:55:48
	 */
	Ret updateRedPacketLock(WxWalletSendRedPacket redPacket, Short status, boolean lock);
}
