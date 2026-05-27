
package org.tio.sitexxx.service.pay.base;

import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacketLocal;
import org.tio.sitexxx.service.vo.GrabRedpacketVo;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;

import java.util.Map;

/**
 * 支付接口
 * @param <payQuest>
 * @param <Resp>
 * @author lixinji
 * 2020年10月27日 上午11:20:10
 * @param <req>
 * @param <Resp>
 */
public interface BasePay<Req extends BasePayReq, Resp extends BasePayResp> {

	Ret grabRedpacket(GrabRedpacketVo grabVo, User user, Boolean isAtom);

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午5:02:35
	 */
	Map<String, Object> getConfParam();

	/**
	 * 
	 * 开户
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:22:44
	 */
	Resp openUser(Req payQuest, Integer uid);

	/**
	 * 修改开户信息
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年11月12日 下午3:05:22
	 */
	Resp updateUser(Req payQuest, Integer uid);

	/**
	 * 绑定银行卡
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:26:10
	 */
	Resp bindBankCard(Req payQuest, Integer uid);

	/**
	 * 确认绑卡
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月8日 下午5:41:04
	 */
	Resp bindBankCardConfirm(Req payQuest, Integer uid);

	/**
	 * 解绑银行卡
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:27:00
	 */
	Resp removeBankCard(Req payQuest, Integer uid);

	/**
	 * 用户余额
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:15:19
	 */
	Resp getWalletInfo(Req payQuest, Integer uid);

	/**
	 * 充值
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:18
	 */
	Resp recharge(Req payQuest, Integer uid);

	/**
	 * 充值确认接口
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 上午11:52:42
	 */
	Resp rechargeConfirm(RechargeConfirmVo rechargeVo, Integer uid, String cny);

	/**
	 * 充值查询
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 上午10:42:04
	 */
	Resp rechargeQuery(Req payQuest, Integer uid);




	/**
	 * 发红包
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Resp sendRedpacket(Req payQuest, Integer uid);

	/**
	 * 红包查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年12月1日 下午1:47:29
	 */
	Resp redpacketQuery(Req payQuest, Integer uid);

	/**
	 * 抢红包
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月19日 上午10:08:07
	 */
	Resp grabRedpacket(Req payQuest, Integer uid);

	/**
	 * 提现
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:10:00
	 */
	Resp withhold(Req payQuest, Integer uid);

	/**
	 * 提现查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月25日 下午2:28:45
	 */
	Resp withholdQuery(Req payQuest, Integer uid);

	BasePayResp rechargeConfirm(BasePayReq payQuest, Integer uid);

	/**
	 * 转账
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:10:00
	 */
	Resp transfer(Req payQuest, Integer uid);

	Resp transfer(Req payQuest, Integer uid, String cny);


	Ret payRedpacket(HttpRequest request, BasePayResp resp, SendRedpacketVo redpacketVo, Boolean isAtom);

	/**
	 * 转账查询
	 * @param payQuest
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2021年3月9日 下午3:07:36
	 */
	Resp transferQuery(Req payQuest, Integer uid);

	/**
	 * 客户端token
	 * @param payQuest
	 * @return
	 * @author lixinji
	 * 2020年10月27日 上午11:17:38
	 */
	Resp clientToken(Req payQuest, Integer uid);

	/**
	 * 手续费
	 * @param amount
	 * @return
	 * @author lixinji
	 * 2020年11月16日 下午2:06:05
	 */
	long commission(long amount);

	Ret initRedpacket(SendRedpacketVo redpacketVo, Boolean isAtom);

	WxWalletSendRedPacketLocal getRedPacketLockLocal(Integer rid, Boolean lock);

	Ret updateRedPacketLock(WxWalletSendRedPacketLocal redPacket, Short status, boolean lock);

	Ret grabRedpacketCallback(HttpRequest request, BasePayResp resp, GrabRedpacketVo grabVo, Boolean isAtom);
}
