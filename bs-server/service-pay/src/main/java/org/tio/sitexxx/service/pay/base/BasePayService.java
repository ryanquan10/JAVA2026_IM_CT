
package org.tio.sitexxx.service.pay.base;

import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.vo.BindCardConfirmVo;
import org.tio.sitexxx.service.vo.BindCardVo;
import org.tio.sitexxx.service.vo.ClientTokenVo;
import org.tio.sitexxx.service.vo.GrabRedpacketVo;
import org.tio.sitexxx.service.vo.OpenUserVo;
import org.tio.sitexxx.service.vo.RechargeConfirmVo;
import org.tio.sitexxx.service.vo.RechargeQueryVo;
import org.tio.sitexxx.service.vo.RechargeVo;
import org.tio.sitexxx.service.vo.RedpacketQueryVo;
import org.tio.sitexxx.service.vo.SendRedpacketVo;
import org.tio.sitexxx.service.vo.UnBindCardVo;
import org.tio.sitexxx.service.vo.UpdateOpenVo;
import org.tio.sitexxx.service.vo.WalletVo;
import org.tio.sitexxx.service.vo.WithholdQueryVo;
import org.tio.sitexxx.service.vo.WithholdVo;

public interface BasePayService {

	/**
	 * 实名信息
	 * @param uid
	 * @return
	 * @author lixinji 2021年3月10日 下午5:44:39
	 */
	Ret realInfo(Integer uid);

	/**
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月9日 下午5:26:22
	 */

	/**
	 * 开户
	 * 
	 * @param openVo
	 * @return
	 * @author lixinji 2020年11月3日 下午5:57:12
	 */
	Ret openUser(OpenUserVo openVo, HttpRequest request);

	/**
	 * 银行卡列表
	 * 
	 * @param uid
	 * @return
	 * @author lixinji 2021年3月12日 上午11:13:29
	 */
	Ret bankcardList(Integer uid);

	/**
	 * 绑卡
	 * 
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月10日 下午5:51:44
	 */
	public Ret bindCard(BindCardVo cardVo, HttpRequest request);

	/**
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午9:59:15
	 */
	public Ret unBindCard(UnBindCardVo cardVo, HttpRequest request);

	/**
	 * 绑卡确定
	 * 
	 * @param cardVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月10日 下午6:14:54
	 */
	public Ret bindCardConfirm(BindCardConfirmVo cardVo, HttpRequest request);

	/**
	 * 修改开户信息
	 * 
	 * @param openVo
	 * @return
	 * @author lixinji 2020年11月12日 下午3:28:16
	 */
	public Ret updateOpenUser(UpdateOpenVo openVo, HttpRequest request);

	/**
	 * 钱包信息
	 * 
	 * @param uid
	 * @param walletid
	 * @return
	 * @author lixinji 2020年11月15日 下午5:56:34
	 */
	public Ret getWalletInfo(WalletVo walletVo, HttpRequest request);

	/**
	 * 发送红包
	 * 
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2020年11月18日 下午6:06:01
	 */
	public Ret sendRedpacket(SendRedpacketVo redpacketVo, HttpRequest request);

	public Ret transfer(SendRedpacketVo redpacketVo, HttpRequest request);

	/**
	 * @param redpacketVo
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:30
	 */
	public Ret initRedpacket(SendRedpacketVo redpacketVo);

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:31
	 */
	public Ret quickRedpacket(SendRedpacketVo redpacketVo, HttpRequest request);

	/**
	 * @param redpacketVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月15日 下午4:20:32
	 */
	public Ret payRedpacket(SendRedpacketVo redpacketVo, HttpRequest request, User user);

	/**
	 * 发送红包记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return 13805730416 郭 13905817500 张
	 * @author lixinji 2020年11月19日 上午11:32:00
	 */
	public Ret sendRedpacketlist(Integer uid, Integer pageNumber, String period);

	/**
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji 2020年11月27日 下午4:50:13
	 */
	public Ret sendRedpacketStat(Integer uid, String period);

	/**
	 * 抢红包
	 * 
	 * @param grabRedpacketVo
	 * @return
	 * @author lixinji 2020年11月19日 上午10:45:04
	 */
	public Ret grabRedpacket(GrabRedpacketVo grabRedpacketVo, User user, HttpRequest request);

	/**
	 * 抢红包记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:33:07
	 */
	public Ret grabRedpacketlist(Integer uid, Integer pageNumber, String period);

	/**
	 * 
	 * @param uid
	 * @param period
	 * @return
	 * @author lixinji 2020年11月27日 下午4:52:51
	 */
	public Ret grabRedpacketStat(Integer uid, String period);

	/**
	 * 红包状态
	 * 
	 * @param uid
	 * @param serialNumber
	 * @return
	 * @author lixinji 2020年11月22日 下午10:30:43
	 */
	public Ret redStatus(User user, String serialNumber, Integer rid);

	/**
	 * 红包信息
	 * 
	 * @param serialNumber
	 * @return
	 * @author lixinji 2020年11月22日 下午10:50:11
	 */
	public Ret redInfo(HttpRequest request, String serialNumber, User user, Integer rid);

	/**
	 * 支付列表信息
	 * 
	 * @param user
	 * @param request
	 * @return
	 * @author lixinji 2021年3月17日 下午4:09:46
	 */
	public Ret payListInfo(User user, HttpRequest request);

	/**
	 * 客户端token
	 * 
	 * @param tokenVo
	 * @return
	 * @author lixinji 2020年11月15日 下午6:51:59
	 */
	public Ret getClientToken(ClientTokenVo tokenVo, HttpRequest request);

	/**
	 * 充值
	 * 
	 * @param rechargeVo
	 * @return
	 * @author lixinji 2020年11月15日 下午7:57:54
	 */
	public Ret recharge(RechargeVo rechargeVo, HttpRequest request);

	/**
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月11日 上午11:57:31
	 */
	public Ret rechargeConfirm(RechargeConfirmVo rechargeVo, HttpRequest request);

	/**
	 * 充值查询
	 * 
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2020年11月25日 下午1:59:12
	 */
	public Ret rechargeQuery(RechargeQueryVo rechargeVo, HttpRequest request);

	/**
	 * @param queryVo
	 * @param request
	 * @return
	 * @author lixinji 2021年3月18日 上午11:03:38
	 */
	public Ret redpacketPayQuery(RedpacketQueryVo queryVo, HttpRequest request);

	/**
	 * 充值记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:17:04
	 */
	public Ret rechargelist(Integer uid, Integer pageNumber);

	/**
	 * @param uid
	 * @param pageNumber
	 * @param mode
	 * @return
	 * @author lixinji 2020年11月26日 上午10:43:38
	 */
	public Ret getWalletItems(Integer uid, Integer pageNumber, Short mode);

	/**
	 * 提现
	 * 
	 * @param withholdVo
	 * @return
	 * @author lixinji 2020年11月16日 下午2:09:17
	 */
	public Ret withhold(WithholdVo withholdVo, HttpRequest request);

	/**
	 * 提现查询
	 * 
	 * @param rechargeVo
	 * @param request
	 * @return
	 * @author lixinji 2020年11月25日 下午1:59:12
	 */
	public Ret withholdQuery(WithholdQueryVo withholdQueryVo, HttpRequest request);

	/**
	 * 提现记录
	 * 
	 * @param uid
	 * @param pageNumber
	 * @return
	 * @author lixinji 2020年11月19日 上午11:16:33
	 */
	public Ret withholdlist(Integer uid, Integer pageNumber);

	/**
	 * 充值回调
	 * 
	 * @param request
	 * @return
	 * @author lixinji 2020年11月15日 下午7:59:04
	 */
	public Ret rechargeCallback(HttpRequest request, Integer uid);

	/**
	 * 提现回调
	 * 
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji 2020年11月16日 下午2:15:22
	 */
	public Ret withholdCallback(HttpRequest request, Integer uid);

	/**
	 * 发送红包回调
	 * 
	 * @param request
	 * @param uid
	 * @return
	 * @author lixinji 2020年11月18日 下午6:08:40
	 */
	public Ret redpacketCallback(HttpRequest request, Integer uid);

	/**
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:41:38
	 */
	public Ret rechargeJob() throws Exception;

	/**
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:41:39
	 */
	public Ret redpacketJob() throws Exception;

	/**
	 * @return
	 * @author lixinji
	 * 2021年4月9日 上午10:41:41
	 */
	public Ret withholdJob() throws Exception;

	/**
	 * 检测用户钱包是否可以注销
	 * @param user
	 * @return
	 * @author lixinji
	 * 2021年4月8日 下午4:53:31
	 */
	boolean walletCheckLogout(User user);
}
