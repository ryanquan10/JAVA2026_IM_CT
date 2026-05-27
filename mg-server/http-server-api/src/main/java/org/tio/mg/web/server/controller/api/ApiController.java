
package org.tio.mg.web.server.controller.api;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConst;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.mvc.Routes;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.conf.ClientConf;
import org.tio.mg.service.model.conf.Conf;
import org.tio.mg.service.model.conf.IpBlackList;
import org.tio.mg.service.model.main.*;
import org.tio.mg.service.model.mg.*;
import org.tio.mg.service.model.stat.TioIpPullblackLog;
import org.tio.mg.service.service.atom.AbsAtom;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.TioIpPullblackLogService;
import org.tio.mg.service.service.base.UserService;
import org.tio.mg.service.service.conf.IpBlackListService;
import org.tio.mg.service.service.mg.MgUserService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.web.server.controller.base.MgLoginController;
import org.tio.mg.web.server.utils.CloudflareR2Utils;
import org.tio.mg.web.server.utils.ImgUtils;
import org.tio.mg.web.server.utils.UploadUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.map.MapUtil;


import org.apache.http.client.methods.HttpPost;

/**
 * 外部api
 * @author xufei
 * 2021年4月21日 上午9:50:24
 */
@RequestPath(value = "/api")
public class ApiController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ApiController.class);
	private Integer payReqIdIndex = 1;

	/**
	 * @param args
	 * @author xufei
	 * 2021年4月22日 上午10:00:40
	 */
	public static void main(String[] args) {

	}

	/**
	 * 
	 */
	public ApiController() {
	}
	
	
	@RequestPath(value = "/alluser")
	public Resp alluser(HttpRequest request) throws Exception {
		List<Record> records = Db.use(Const.Db.TIO_MG).find("select loginname userName,phone regCellPhone,id userCode,CASE when `status` = 1 THEN 'T' ELSE 'F' END isValid from mg_user");
		return Resp.ok(records).code(0);
	}
	
	/**
	 * @param request
	 * @param sessionid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月22日 上午10:33:06
	 */
	@RequestPath(value = "/loginstat")
	public Resp loginstat(HttpRequest request,String sessionid) throws Exception {
		Integer userid = ImUtils.getUseridByToken(sessionid);
		if (userid != null) {
			MgUser user = MgUserService.ME.getById(userid);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userCode", user.getId());
			map.put("userName", user.getLoginname());
			map.put("regCellPhone", user.getPhone());
			map.put("isValid",Objects.equals(user.getStatus(), Const.Status.NORMAL) ? "T" : "F");
			return Resp.ok(map).code(0);
		}
		return Resp.fail().code(500);
	}
	
	/**
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年4月22日 上午10:38:14
	 */
	@SuppressWarnings("unchecked")
	@RequestPath(value = "/login")
	public Resp login(HttpRequest request) throws Exception {
		MgLoginController loginController = Routes.getController(MgLoginController.class);
		Map<String, Object> reqMap = Json.toBean(request.getBodyString(),Map.class);
		loginController.login(MapUtil.getStr(reqMap, "username"), MapUtil.getStr(reqMap, "password"), null, null,request);
		MgUser user = WebUtils.currUser(request);
		if(user != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userCode", user.getId());
			map.put("userName", user.getLoginname());
			map.put("regCellPhone", user.getPhone());
			map.put("isValid",Objects.equals(user.getStatus(), Const.Status.NORMAL) ? "T" : "F");
			return Resp.ok(map).code(0);
		}
		return Resp.fail().code(500);
	}

	@RequestPath(value = "/delDiscoveryPageInfo")
	public Resp delDiscoveryPageInfo(HttpRequest request, String id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		MgDiscoveryPage discoveryPage = MgDiscoveryPage.dao.findById(id);
		if(discoveryPage == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = discoveryPage.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	@RequestPath(value = "/updateDiscoveryPageInfo")
	public Resp updateDiscoveryPageInfo(HttpRequest request, Integer id, String name, String url, String logo) {
		if (id == null) {
			return Resp.fail("id不能为空");
		}

		MgDiscoveryPage discoveryPage = MgDiscoveryPage.dao.findById(id);
		if(discoveryPage == null) {
			return Resp.fail("该id不存在");
		}

		try {
			MgDiscoveryPage mgDiscoveryPage = new MgDiscoveryPage();
			mgDiscoveryPage.setId(id);
			mgDiscoveryPage.setName(name);
			mgDiscoveryPage.setUrl(url);
			mgDiscoveryPage.setLogo(logo);
			mgDiscoveryPage.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("自定义网站更新异常");
		}
		return Resp.fail().code(500);
	}

	@RequestPath(value = "/getDiscoveryPageInfo")
	public Resp getDiscoveryPageInfo(HttpRequest request) {
		List<MgDiscoveryPage> all = MgDiscoveryPage.dao.findAll();
		ClientConf conf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenCircle'");
		if (conf.getValue().equals(0)) {
			for (MgDiscoveryPage mgDiscoveryPage : all) {
				if (mgDiscoveryPage.getName().equals("帮办中心") || mgDiscoveryPage.getUrl().contains("circle")) {
					all.remove(mgDiscoveryPage);
				}
			}
		}

		return Resp.ok(all);
	}
	@RequestPath(value = "/setDiscoveryPageInfo")
	public Resp setDiscoveryPageInfo(HttpRequest request, String name, String url, String logo) {
		if(name == null || url == null || logo == null) {
			return Resp.fail("参数异常");
		}
		if (name.isEmpty()) {return Resp.fail("name不能为空");}
		if (url.isEmpty()) {return Resp.fail("url不能为空");}


		try {
			MgDiscoveryPage mgDiscoveryPage = new MgDiscoveryPage();
			mgDiscoveryPage.setName(name);
			mgDiscoveryPage.setUrl(url);
			mgDiscoveryPage.setLogo(logo);
			mgDiscoveryPage.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("自定义网站保存异常");
		}
		return Resp.fail().code(500);
	}

	@RequestPath("/uploadFile")
	public Resp uploadFile(HttpRequest request, UploadFile logo) {
		if (logo == null) {
			return Resp.fail("参数异常");
		}

		byte[] bs = logo.getData();
		String filename = logo.getName();
		String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

		try {
			String objectKey = "found/" + getUUID() + "." + extName;

			// 构建 Content-Type
			String contentType;
			switch (extName) {
				case "jpg":
				case "jpeg":
					contentType = "image/jpeg";
					break;
				case "png":
					contentType = "image/png";
					break;
				case "gif":
					contentType = "image/gif";
					break;
				case "mp4":
					contentType = "video/mp4";
					break;
				case "pdf":
					contentType = "application/pdf";
					break;
				default:
					contentType = "application/octet-stream";
			}

			// 上传文件到 R2
			InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//			CloudflareR2Utils.uploadFilePublic(
//					MgConst.CloudflareR2.R2_BUCKET_NAME,
//					objectKey,
//					inputStream,
//					bs.length,
//					contentType
//			);

			// 返回相对路径，前端拼接 base_url 获取完整 URL
			return Resp.ok("/" + objectKey);

		} catch (Exception e) {
			log.error("文件上传到 R2 异常", e);
			return Resp.fail().code(500).msg("文件上传失败");
		}
	}


	@RequestPath(value = "/addDefaultFriend")
	public Resp addDefaultFriend(HttpRequest request, String uid, String msg) {
		if(uid == null || msg == null) {
			return Resp.fail("参数异常");
		}
		if (uid.isEmpty()) {return Resp.fail("客服id不能为空");}

		User user = User.dao.findFirst("select * from user where id = ?", uid);
		if (user == null) {
			return Resp.fail("该user id不存在，请确认user的id是否正确");
		}

		try {
			DefaultFriends defaultFriends = new DefaultFriends();
			defaultFriends.setUid(uid);
			defaultFriends.setDefaultMsg(msg);
			defaultFriends.setWeight(0);
			defaultFriends.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("自定义网站保存异常");
		}
		return Resp.fail().code(500);
	}



	@RequestPath(value = "/delDefaultFriend")
	public Resp delDefaultFriend(HttpRequest request, String id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		DefaultFriends defaultFriends = DefaultFriends.dao.findById(id);
		if(defaultFriends == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = defaultFriends.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	@RequestPath(value = "/updateDefaultFriend")
	public Resp updateDefaultFriend(HttpRequest request, Integer id, String uid, String msg, Integer weight) {
		if(id == null || uid == null || weight == null) {
			return Resp.fail("参数异常");
		}
		if (uid.isEmpty()) {return Resp.fail("客服id不能为空");}

		User user = User.dao.findFirst("select * from user where id = ?", uid);
		if (user == null) {
			return Resp.fail("该user id不存在，请确认user的id是否正确");
		}


		try {
			DefaultFriends defaultFriends = new DefaultFriends();
			defaultFriends.setId(id);
			defaultFriends.setUid(uid);
			defaultFriends.setDefaultMsg(msg);
			defaultFriends.setWeight(weight);
			defaultFriends.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("自定义网站保存异常");
		}
		return Resp.fail().code(500);
	}

    @RequestPath(value = "/getDefaultFriend")
    public Resp getDefaultFriend(HttpRequest request) {
        List<DefaultFriends> all = DefaultFriends.dao.findAll();
        return Resp.ok(all);
    }

    @RequestPath(value = "/addDefaultGroup")
    public Resp addDefaultGroup(HttpRequest request, Long groupid) {

        if (groupid == null) {return Resp.fail("群组id不能为空");}

        WxGroup group = WxGroup.dao.findById(groupid);
        if (group == null) {
            return Resp.fail("该群组不存在, 请确认群组的id是否正确");
        }

        try {
            DefaultGroup defaultGroup = new DefaultGroup();
            defaultGroup.setGroupid(groupid);
            defaultGroup.setIsopen(1);
            defaultGroup.save();
            return Resp.ok();
        } catch (Exception e) {
            log.error("默认群组保存异常, 请重试,错误信息:" + e.getMessage());
        }
        return Resp.fail().code(500);
    }

	@RequestPath(value = "/updateDefaultGroup")
	public Resp updateDefaultGroup(HttpRequest request,  Long groupid, Integer isOpen) {
		if(groupid == null || isOpen == null) {
			return Resp.fail("参数异常");
		}
		if (groupid == null) {return Resp.fail("群组id不能为空");}

		WxGroup group = WxGroup.dao.findById(groupid);
		if (group == null) {
			return Resp.fail("该群组不存在, 请确认群组的id是否正确");
		}

		try {
			DefaultGroup defaultGroup = new DefaultGroup();
			defaultGroup.setGroupid(groupid);
			defaultGroup.setIsopen(isOpen);
			defaultGroup.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("修改异常，请重试，错误信息: " + e.getMessage());
		}
		return Resp.fail().code(500);
	}


	@RequestPath(value = "/getDefaultGroup")
	public Resp getDefaultGroup(HttpRequest request, Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("mguser.defaultgrouplist", params);
		Page<Record> userPage = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return Resp.ok(userPage);
	}

    @RequestPath(value = "/delDefaultGroup")
    public Resp delDefaultGroup(HttpRequest request, Integer groupid) {
        DefaultGroup defaultGroup = DefaultGroup.dao.findById(groupid);
        if (defaultGroup == null) {
            return Resp.fail("该默认群组不存在");
        }
        boolean delete = defaultGroup.deleteById(groupid);
        if (!delete) {
            return Resp.fail("删除失败，请重试");
        }
        return Resp.ok();
    }

	@RequestPath(value = "/getDefaultFriendConfig")
	public Resp getDefaultFriendConfig(HttpRequest request) {
		List<DefaultFriendsConfig> all = DefaultFriendsConfig.dao.findAll();
		return Resp.ok(all);
	}

	@RequestPath(value = "/updateDefaultFriendConfig")
	public Resp updateDefaultFriendConfig(HttpRequest request, Integer id, Integer isRotation, Integer point) {
		if(id == null || isRotation == null || point == null) {
			return Resp.fail("参数异常");
		}
		if (point <= 0) {
			return Resp.fail("轮询次数必须大于0");
		}

		try {
			DefaultFriendsConfig defaultFriendsConfig = new DefaultFriendsConfig();
			defaultFriendsConfig.setId(id);
			defaultFriendsConfig.setIsRotation(isRotation);
			defaultFriendsConfig.setPoint(point);
			defaultFriendsConfig.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("自定义网站保存异常");
		}
		return Resp.fail().code(500);
	}



	public static String getUUID(){

		UUID uuid=UUID.randomUUID();

		String str = uuid.toString();

		String uuidStr=str.replace("-", "");

		return uuidStr;

	}

	@RequestPath(value = "/setPayImg")
	public Resp setPayImg(HttpRequest request, String img, Integer type) {
		if(img == null) {
			return Resp.fail("参数异常");
		}
		List<PaymentImg> all = PaymentImg.dao.findAll();
		if (all != null) {
			for (PaymentImg item : all) {
				if (item.getType().equals(type)) {
					PaymentImg paymentImg = new PaymentImg();
					paymentImg.setId(item.getId());
					paymentImg.setPaymentImg(img);
					paymentImg.setType(item.getType());
					paymentImg.update();
					return Resp.ok();
				}
			}
		}

		try {
			PaymentImg paymentImg = new PaymentImg();
			paymentImg.setPaymentImg(img);
			paymentImg.setType(type);
			paymentImg.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("添加收款码失败");
			log.error(e.getMessage());
		}
		return Resp.fail().code(500);
	}

	@RequestPath(value = "/getPayImg")
	public Resp getPaymentImg(HttpRequest request) {
		List<PaymentImg> all = PaymentImg.dao.findAll();
		log.error("all: {}", all);
		if (all != null && all.size() > 0)
			return Resp.ok(all);
		return Resp.ok("");
	}

	@RequestPath(value = "/delPayImg")
	public Resp delPaymentImg(HttpRequest request, Integer id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		PaymentImg paymentImg = PaymentImg.dao.findById(id);
		if(paymentImg == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = paymentImg.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	@RequestPath(value = "/updatePayImg")
	public Resp updatePayImg(HttpRequest request, Integer id, String img, Integer type) {
		if (id == null) {
			return Resp.fail("id不能为空");
		}

		PaymentImg payImg = PaymentImg.dao.findById(id);
		if(payImg == null) {
			return Resp.fail("该id不存在");
		}

		try {
			PaymentImg paymentImg = new PaymentImg();
			paymentImg.setId(id);
			paymentImg.setPaymentImg(img);
			paymentImg.setType(type);
			paymentImg.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("更新收款码失败异常");
		}
		return Resp.fail().code(500);
	}

	@RequestPath(value = "/uploadPaymentImg")
	public Resp uploadPaymentImg(HttpRequest request, UploadFile logo) {
		if (logo == null) {
			return Resp.fail("参数异常");
		}

		byte[] bs = logo.getData();
		String filename = logo.getName();
		String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

		// 只允许特定格式的图片
		if (!"jpg jpeg png bmp".contains(extName)) {
			return Resp.fail("仅支持 jpg/jpeg/png/bmp 格式的图片上传");
		}

		try {
			String objectKey = "payment/" + getUUID() + "." + extName;

			// 构建 Content-Type
			String contentType;
			switch (extName) {
				case "jpg":
				case "jpeg":
					contentType = "image/jpeg";
					break;
				case "png":
					contentType = "image/png";
					break;
				case "bmp":
					contentType = "image/bmp";
					break;
				default:
					contentType = "application/octet-stream";
			}

			// 上传文件到 R2
			InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//			CloudflareR2Utils.uploadFilePublic(
//					MgConst.CloudflareR2.R2_BUCKET_NAME,
//					objectKey,
//					inputStream,
//					bs.length,
//					contentType
//			);

			// 返回相对路径，前端拼接 base_url 获取完整 URL
			return Resp.ok("/" + objectKey);

		} catch (Exception e) {
			log.error("文件上传到 R2 异常", e);
			return Resp.fail().code(500).msg("文件上传失败");
		}
	}

	@RequestPath(value = "/getPaymentItem")
	public Resp getPaymentItem(HttpRequest request) {
		List<WalletItemLocal> all = WalletItemLocal.dao.findAll();

		return Resp.ok().data(all);
	}

	@RequestPath(value = "/getRechargePaymentItem")
	public Resp getRechargePaymentItem(HttpRequest request) {
		List<WalletItemLocal> all = WalletItemLocal.dao.find("select * from wallet_item_local where rechargeOrWithhold = 1 order by createTime desc");

		return Resp.ok().data(all);
	}

	@RequestPath(value = "/getWithholdPaymentItem")
	public Resp getWithholdPaymentItem(HttpRequest request) {
		List<WalletItemLocal> all = WalletItemLocal.dao.find("select * from wallet_item_local where rechargeOrWithhold = 2 order by createTime desc");

		return Resp.ok().data(all);
	}

	@RequestPath(value = "/confirmationOfPayment")
	public Resp confirmationOfPayment(HttpRequest request, Integer id, Integer status) {

 		WalletItemLocal walletItem = WalletItemLocal.dao.findById(id);

		 if (walletItem == null) {
			 return Resp.fail("提现记录不存在");
		 }

		 if (!walletItem.getStatus().equals(0)) {
			 return Resp.fail("该记录已处理");
		 }

		WxWalletCoinItemLocal walletCoinItem = WxWalletCoinItemLocal.dao.findFirst("select * from wx_wallet_coin_item_local where merorderid = ?", walletItem.getSerialnumber());
		if (walletCoinItem == null) {
			return Resp.fail("提现记录不存在");
		}
		walletCoinItem.setStatus((short)3);
		boolean update1 = walletCoinItem.update();
		if (!update1)
			return Resp.fail("提现记录修改失败");

		if (walletItem.getRechargeOrWithhold().equals(1) && status.equals(1)) {

			String url_get = "http://" + MgConst.BS_IP + ":6060/paycallback/recharge" + "?uid=" + walletItem.getUid();

			try {
				log.error("充值回调开始");
				//创建一个获取连接客户端的工具
				CloseableHttpClient httpClient = HttpClients.createDefault();
				//创建Post请求
				HttpPost httpPost = new HttpPost(url_get);
				//添加请求头
				httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
				InetAddress address = InetAddress.getLocalHost();
				String ip=address.getHostAddress();//获得本机IP
				httpPost.addHeader("ip",ip);
				httpPost.addHeader("Cookie", walletItem.getToken());

				//封装请求参数，将map集合转换成json格式
				// 设置请求体
				//设置body内的参数，put到JSONObject中
				JSONObject param = new JSONObject();
				param.put("again", "confirm");
				param.put("orderStatus", "SUCCESS");
				param.put("orderErrorMessage", "");
				param.put("completeDateTime", new Date().toString());
				param.put("createDateTime", walletItem.getCreateTime());
				param.put("serialNumber", walletItem.getSerialnumber());
				//使用StringEntity转换成实体类型
				StringEntity entity = new StringEntity(param.toString());
//            entity.setContentEncoding("UTF-8");
//            entity.setContentType("application/json");//发送json数据需要设置contentType
				//将封装的参数添加到Post请求中
				httpPost.setEntity(entity);
				//执行请求
				CloseableHttpResponse response = httpClient.execute(httpPost);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					walletItem.setId(walletItem.getId());
					walletItem.setUid(walletItem.getUid());
					walletItem.setType(walletItem.getType());
					walletItem.setAmount(walletItem.getAmount());
					walletItem.setStatus(status);
					walletItem.setSerialnumber(walletItem.getSerialnumber());
					walletItem.setToken(walletItem.getToken());
					walletItem.setCreateTime(walletItem.getCreateTime());
					walletItem.setName(walletItem.getName());
					walletItem.setPaymentAccount(walletItem.getPaymentAccount());
					walletItem.setUpdateTime(new Date());
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				} else {
					walletItem.setStatus(3);
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				}
				response.close();
				httpClient.close();
				log.error("充值回调完成");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("提现回调失败");
			}


//			try {
//				log.error("充值回调开始");
//				URL url = new URL(url_get);    //把字符串转换为URL请求地址
//				HttpPost httpPost = new HttpPost(url_get);
//				CloseableHttpClient httpclient = HttpClients.createDefault();
//				HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开连接
//				connection.setDoInput(true);
//				connection.setDoOutput(true);
//				InetAddress address = InetAddress.getLocalHost();
//				String ip=address.getHostAddress();//获得本机IP
//				connection.setRequestProperty("ip",ip);  //请求来源IP
//				connection.setRequestProperty("Cookie", walletItem.getToken());
//
//
//				// 设置请求体
//				//设置body内的参数，put到JSONObject中
//				JSONObject param = new JSONObject();
//				param.put("again", "confirm");
//				param.put("orderStatus", "SUCCESS");
//				param.put("orderErrorMessage", "");
//				param.put("completeDateTime", new Date().toString());
//				param.put("createDateTime", walletItem.getCreateTime());
//				param.put("serialNumber", walletItem.getSerialnumber());
//
//				connection.connect();// 连接会话
//
//
//				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
//				writer.write(param.toString());
//				writer.flush();
//				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
//				String line;
//				StringBuilder sb = new StringBuilder();
//				while ((line = br.readLine()) != null) {// 循环读取流
//					sb.append(line);
//				}
//				br.close();// 关闭流
//				connection.disconnect();// 断开连接
//				log.error("充值回调完成");
//			} catch (Exception e) {
//				e.printStackTrace();
//                log.error("充值回调失败");
//			}
		} else if (walletItem.getRechargeOrWithhold().equals(2) && status.equals(1)) {
			String url_get = "http://" + MgConst.BS_IP + ":6060/paycallback/withhold" + "?uid=" + walletItem.getUid();

			try {


				log.error("充值回调开始");
				//创建一个获取连接客户端的工具
				CloseableHttpClient httpClient = HttpClients.createDefault();
				//创建Post请求
				HttpPost httpPost = new HttpPost(url_get);
				//添加请求头
				httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
				InetAddress address = InetAddress.getLocalHost();
				String ip=address.getHostAddress();//获得本机IP
				httpPost.addHeader("ip",ip);
				httpPost.addHeader("Cookie", walletItem.getToken());

				//封装请求参数，将map集合转换成json格式
				// 设置请求体
				//设置body内的参数，put到JSONObject中
				JSONObject param = new JSONObject();
				param.put("again", "confirm");
				param.put("orderStatus", "SUCCESS");
				param.put("orderErrorMessage", "");
				param.put("completeDateTime", new Date().toString());
				param.put("createDateTime", walletItem.getCreateTime());
				param.put("serialNumber", walletItem.getSerialnumber());
				//使用StringEntity转换成实体类型
				StringEntity entity = new StringEntity(param.toString());
//            entity.setContentEncoding("UTF-8");
//            entity.setContentType("application/json");//发送json数据需要设置contentType
				//将封装的参数添加到Post请求中
				httpPost.setEntity(entity);
				//执行请求
				CloseableHttpResponse response = httpClient.execute(httpPost);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					walletItem.setId(walletItem.getId());
					walletItem.setUid(walletItem.getUid());
					walletItem.setType(walletItem.getType());
					walletItem.setAmount(walletItem.getAmount());
					walletItem.setStatus(status);
					walletItem.setSerialnumber(walletItem.getSerialnumber());
					walletItem.setToken(walletItem.getToken());
					walletItem.setCreateTime(walletItem.getCreateTime());
					walletItem.setName(walletItem.getName());
					walletItem.setPaymentAccount(walletItem.getPaymentAccount());
					walletItem.setUpdateTime(new Date());
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				} else {
					walletItem.setStatus(3);
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				}
				response.close();
				httpClient.close();
				log.error("充值回调完成");

//				URL url = new URL(url_get);    //把字符串转换为URL请求地址
//				HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开连接
//				connection.setDoInput(true);
//				connection.setDoOutput(true);
//				InetAddress address = InetAddress.getLocalHost();
//				String ip=address.getHostAddress();//获得本机IP
//				connection.setRequestProperty("ip",ip);  //请求来源IP
//				connection.setRequestProperty("Cookie", walletItem.getToken());
//
//				// 设置请求体
//				//设置body内的参数，put到JSONObject中
//				JSONObject param = new JSONObject();
//				param.put("again", "confirm");
//				param.put("orderStatus", "SUCCESS");
//				param.put("orderErrorMessage", "");
//				param.put("completeDateTime", new Date().toString());
//				param.put("createDateTime", walletItem.getCreateTime());
//				param.put("serialNumber", walletItem.getSerialnumber());
//
//				connection.connect();// 连接会话
//
//				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
//				writer.write(param.toString());
//				writer.flush();
//				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
//				String line;
//				StringBuilder sb = new StringBuilder();
//				while ((line = br.readLine()) != null) {// 循环读取流
//					sb.append(line);
//				}
//				br.close();// 关闭流
//				connection.disconnect();// 断开连接
//				System.out.println(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
				log.error("提现回调失败");
			}
		}

		if (walletItem.getRechargeOrWithhold().equals(1) && status.equals(2)) {
			String url_get = "http://" + MgConst.BS_IP + ":6060/paycallback/recharge" + "?uid=" + walletItem.getUid();
			try {
				log.error("充值回调开始");
				//创建一个获取连接客户端的工具
				CloseableHttpClient httpClient = HttpClients.createDefault();
				//创建Post请求
				HttpPost httpPost = new HttpPost(url_get);
				//添加请求头
				httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
				InetAddress address = InetAddress.getLocalHost();
				String ip=address.getHostAddress();//获得本机IP
				httpPost.addHeader("ip",ip);
				httpPost.addHeader("Cookie", walletItem.getToken());

				//封装请求参数，将map集合转换成json格式
				// 设置请求体
				//设置body内的参数，put到JSONObject中
				JSONObject param = new JSONObject();
				param.put("again", "confirm");
				param.put("orderStatus", "FAIL");
				param.put("orderErrorMessage", "");
				param.put("completeDateTime", new Date().toString());
				param.put("createDateTime", walletItem.getCreateTime());
				param.put("serialNumber", walletItem.getSerialnumber());
				//使用StringEntity转换成实体类型
				StringEntity entity = new StringEntity(param.toString());
//            entity.setContentEncoding("UTF-8");
//            entity.setContentType("application/json");//发送json数据需要设置contentType
				//将封装的参数添加到Post请求中
				httpPost.setEntity(entity);
				//执行请求
				CloseableHttpResponse response = httpClient.execute(httpPost);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					walletItem.setId(walletItem.getId());
					walletItem.setUid(walletItem.getUid());
					walletItem.setType(walletItem.getType());
					walletItem.setAmount(walletItem.getAmount());
					walletItem.setStatus(status);
					walletItem.setSerialnumber(walletItem.getSerialnumber());
					walletItem.setToken(walletItem.getToken());
					walletItem.setCreateTime(walletItem.getCreateTime());
					walletItem.setName(walletItem.getName());
					walletItem.setPaymentAccount(walletItem.getPaymentAccount());
					walletItem.setUpdateTime(new Date());
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				} else {
					walletItem.setStatus(3);
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				}
				response.close();
				httpClient.close();
				log.error("充值回调完成");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("充值回调失败");
			}
		} else if (walletItem.getRechargeOrWithhold().equals(2) && status.equals(2)) {
			String url_get = "http://" + MgConst.BS_IP + ":6060/paycallback/withhold" + "?uid=" + walletItem.getUid();

			try {
				log.error("提现回调开始");
				//创建一个获取连接客户端的工具
				CloseableHttpClient httpClient = HttpClients.createDefault();
				//创建Post请求
				HttpPost httpPost = new HttpPost(url_get);
				//添加请求头
				httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
				InetAddress address = InetAddress.getLocalHost();
				String ip=address.getHostAddress();//获得本机IP
				httpPost.addHeader("ip",ip);
				httpPost.addHeader("Cookie", walletItem.getToken());

				//封装请求参数，将map集合转换成json格式
				// 设置请求体
				//设置body内的参数，put到JSONObject中
				JSONObject param = new JSONObject();
				param.put("again", "confirm");
				param.put("orderStatus", "FAIL");
				param.put("orderErrorMessage", "");
				param.put("completeDateTime", new Date().toString());
				param.put("createDateTime", walletItem.getCreateTime());
				param.put("serialNumber", walletItem.getSerialnumber());
				//使用StringEntity转换成实体类型
				StringEntity entity = new StringEntity(param.toString());
//            entity.setContentEncoding("UTF-8");
//            entity.setContentType("application/json");//发送json数据需要设置contentType
				//将封装的参数添加到Post请求中
				httpPost.setEntity(entity);
				//执行请求
				CloseableHttpResponse response = httpClient.execute(httpPost);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					walletItem.setId(walletItem.getId());
					walletItem.setUid(walletItem.getUid());
					walletItem.setType(walletItem.getType());
					walletItem.setAmount(walletItem.getAmount());
					walletItem.setStatus(status);
					walletItem.setSerialnumber(walletItem.getSerialnumber());
					walletItem.setToken(walletItem.getToken());
					walletItem.setCreateTime(walletItem.getCreateTime());
					walletItem.setName(walletItem.getName());
					walletItem.setPaymentAccount(walletItem.getPaymentAccount());
					walletItem.setUpdateTime(new Date());
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				} else {
					walletItem.setStatus(3);
					boolean update = walletItem.update();
					if (!update)
						return Resp.fail().code(500);
				}
				response.close();
				httpClient.close();
				log.error("充值回调完成");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("提现回调失败");
			}
		}

		return Resp.ok();
	}

	@RequestPath(value = "/getUserPayInfoAll")
	public Resp getUserPayInfo(HttpRequest request) {
		List<UserPaymentImg> all = UserPaymentImg.dao.findAll();
		return Resp.ok(all);
	}

	@RequestPath(value = "/getUserPayInfo")
	public Resp getUserPayInfo(HttpRequest request, Integer uid, Integer type) {
		UserPaymentImg userPaymentImg = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ? and type = ?", uid, type);
		return Resp.ok(userPaymentImg);
	}

	@RequestPath(value = "/getRealNameCertificationList")
	public Resp getRealNameCertificationList(HttpRequest request, Integer uid, Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null) {
			pageSize = 16;
		}

		Kv params = Kv.create();
		if(uid != null && uid != 0) {
			params.set("uid", uid);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("realname.realnamelist", params);
//		List<Record> records = Db.find("select a.*, b.nick, b.phone from real_name_certification a left join user b on a.uid = b.id");
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return Resp.ok(records);
	}

	@RequestPath(value = "/verifyUserReal")
	public Resp verifyRealName(HttpRequest request, Integer uid, Integer status, String mark) {

		RealNameCertification userRealRecord = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", uid);
		if (userRealRecord == null) {
			return Resp.fail().msg("该记录不存在");
		}
		if (!userRealRecord.getStatus().equals(0)) {
			return Resp.fail().msg("该记录已审核");
		}
		userRealRecord.setStatus(status);
		userRealRecord.setUpdatetime(new Date());
		userRealRecord.setMark(mark);

		if (status.equals(1)) {
			User user = User.dao.findById(uid);
			user.setRealnameflag((short)1);
			user.setUpdatetime(new Date());
			boolean update = user.update();
			if (!update) {
				return Resp.fail().msg("修改失败，请联系管理员");
			}
			IpInfo ipInfo = IpInfoService.ME.getById(user.getIpid());
			user.setIpInfo(ipInfo);
			boolean res = saveWallet(user, userRealRecord.getIdCardNumber(), userRealRecord.getRealName());
			if (!res) {
				return Resp.fail().msg("修改失败，请联系管理员");
			}
		}
		boolean update1 = userRealRecord.update();
		if (!update1) {
			return Resp.fail().msg("修改失败，请联系管理员");
		}


		return Resp.ok().msg("修改成功");
	}

	private boolean saveWallet(User user, String cardId, String name) {
		AbsAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				WxUserWalletLocal wallet = new WxUserWalletLocal();
				wallet.setUid(user.getId());
				wallet.setReqid(getReqId());
				wallet.setBizid(getMerchantid());
				wallet.setWalletid(getUUID());
				wallet.setIp(user.getIpInfo().getIp());
				wallet.setDevice(Short.valueOf("1"));
				wallet.setOperatorstatus(Short.valueOf("1") );
				wallet.setRealnamestatus(Short.valueOf("1"));
				wallet.setCoinsyn(Short.valueOf("3"));
				boolean save = wallet.save();
				if (!save) {
					log.error("钱包开户异常");
					return failRet("保存钱包逻辑失败");
				}
				user.setOpenflag(Const.YesOrNo.YES);
				user.setOpenid(wallet.getId());
				user.setIdcard(cardId);
				user.setName(name);
				boolean update = user.update();
				if (!update) {
					return failRet("修改用户钱包逻辑异常");
				}
				WxUserCoinLocal wxUserCoinLocal = new WxUserCoinLocal();
				wxUserCoinLocal.setCny(0L);
				wxUserCoinLocal.setCreatetime(new Date());
				wxUserCoinLocal.setUid(user.getId());
				wxUserCoinLocal.setWithdrawcny(0L);
				wxUserCoinLocal.setWalletid(wallet.getWalletid());
//					wxUserCoinLocal.setCostpwd(encrypt(userVo.getPaypwd()));
				wxUserCoinLocal.setUpdatetime(new Date());
				boolean save1 = wxUserCoinLocal.save();
				if (!save1) {
					return failRet("保存钱包逻辑失败");
				}
				WxWalletLocal wxWallet = new WxWalletLocal();
				wxWallet.setUid(user.getId());
				wxWallet.setReqid(wallet.getReqid());
				wxWallet.setWalletid(wallet.getWalletid());
				wxWallet.setStatus((short) 1);
				wxWallet.setAuthstatus("1");
				wxWallet.setAuditstatus("1");
				wxWallet.setMainflag((short) 1);
				wxWallet.setIp(user.getIpInfo().getIp());
				wxWallet.setDevice(wallet.getDevice());
				wxWallet.setAppversion(wallet.getAppversion());
				wxWallet.setCreatetime(new Date());
				wxWallet.setUpdatetime(new Date());
				boolean save2 = wxWallet.save();
				if (!save2) {
					return failRet("保存钱包逻辑失败");
				}

				WxWalletCoinLocal walletCoinLocal = new WxWalletCoinLocal();
				walletCoinLocal.setWid(wxWallet.getId());
				walletCoinLocal.setWalletid(wxWallet.getWalletid());
				walletCoinLocal.setUid(wxWallet.getUid());
				walletCoinLocal.setCny(0L);
				walletCoinLocal.setUnclearcny(0L);
				walletCoinLocal.setWithdrawcny(0L);
				walletCoinLocal.setAcceptredpacket(0L);
				walletCoinLocal.setSendpacket(0L);
				walletCoinLocal.setCreatetime(new Date());
				walletCoinLocal.setUpdatetime(new Date());
				boolean save3 = walletCoinLocal.save();
				if (!save3) {
					return failRet("保存钱包逻辑失败");
				}
				WxWalletInfoLocal wxWalletInfoLocal = new WxWalletInfoLocal();
				wxWalletInfoLocal.setUid(user.getId());
				wxWalletInfoLocal.setMobile(user.getPhone());
				wxWalletInfoLocal.setName(name);
				wxWalletInfoLocal.setCardno(cardId);
				wxWalletInfoLocal.setStatus((short) 1);
				wxWalletInfoLocal.setCreatetime(new Date());
				wxWalletInfoLocal.setUpdatetime(new Date());
				boolean save4 = wxWalletInfoLocal.save();
				if (!save4) {
					return failRet("保存钱包逻辑失败");
				}
				return true;
			}
		};
		boolean tx = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		return tx;
	}

	private synchronized String getReqId() {
		return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + payReqIdIndex + RandomUtil.randomNumbers(14);
	}

	private String getMerchantid() {
		return (String) getConfParam().get("merchantid");
	}

	public Map<String, Object> getConfParam() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put("merchantid", Const.WALLET_MERCHANTID);
		return conf;
	}


	/**
	 * 签到区
	 */

	/**
	 * 获取签到任务配置
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.04
	 */
	@RequestPath(value = "/getSignTask")
	public Resp getSignTask(HttpRequest request) {
		List<SignTask> all = SignTask.dao.findAll();
		return Resp.ok(all);
	}

	/**
	 * 添加签到任务
	 * @param request
	 * @param signDay
	 * @param rewardIntegral
	 * @param taskDescribe
	 * @return
	 * @author xinji
	 * 2023.09.04
	 */
	@RequestPath(value = "/addSignTask")
	public Resp addSignTask(HttpRequest request, Integer signDay, Long rewardIntegral, String taskDescribe) {
		if(signDay == null) {
			return Resp.fail("连续签到天数不能为空");
		}
		if (rewardIntegral == null) {return Resp.fail("奖励积分不能为空");}

		SignTask signTask = SignTask.dao.findFirst("select * from sign_task where sign_day = ?", signDay);
		if (signTask != null) {
			return Resp.fail("连续签到天数不能重复");
		}
		if (taskDescribe == null) {
			taskDescribe = "";
		}

		try {
			SignTask signTask1 = new SignTask();
			signTask1.setSignDay(signDay);
			signTask1.setRewardIntegral(rewardIntegral);
			signTask1.setTaskDescribe(taskDescribe);
			signTask1.setCreateTime(new Date());
			signTask1.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("任务添加失败");
		}
		return Resp.fail().code(500);
	}

	/**
	 *  删除签到任务
	 * @param request
	 * @param id
	 * @return
	 * @author xinji
	 * 2023.09.04
	 */
	@RequestPath(value = "/delSignTask")
	public Resp delSignTask(HttpRequest request, String id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		SignTask signTask = SignTask.dao.findById(id);
		if(signTask == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = signTask.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	/**
	 * 更新签到任务
	 * @param request
	 * @param id
	 * @param signDay
	 * @param rewardIntegral
	 * @param taskDescribe
	 * @return
	 * @author xinji
	 * 2023.09.04
	 */
	@RequestPath(value = "/updateSignTask")
	public Resp updateSignTask(HttpRequest request, Integer id, Integer signDay, Long rewardIntegral, String taskDescribe) {
		if (id == null) {
			return Resp.fail("id不能为空");
		}

		if (signDay == null) {
			return Resp.fail("签到天数不能为空");
		}

		if (rewardIntegral == null) {
			return Resp.fail("奖励积分不能为空");
		}

		if (taskDescribe == null) {
			taskDescribe = "";
		}

		SignTask signTask = SignTask.dao.findById(id);
		if (signTask == null) {
			return Resp.fail("该签到id不存在");
		}


		try {
			signTask.setSignDay(signDay);
			signTask.setRewardIntegral(rewardIntegral);
			signTask.setTaskDescribe(taskDescribe);
			signTask.setUpdateTime(new Date());
			signTask.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("任务更新失败");
		}
		return Resp.fail().code(500);
	}


	/**
	 * 公告区
	 */

	/**
	 * 获取公告列表
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.06
	 */
	@RequestPath(value = "/getNotice")
	public Resp getNotice(HttpRequest request) {
		List<Notice> all = Notice.dao.findAll();
		return Resp.ok(all);
	}

	/**
	 * 添加新的公告
	 * @param request
	 * @param title
	 * @param content
	 * @return
	 * @author xinji
	 * 2023.09.06
	 */
	@RequestPath(value = "/addNotice")
	public Resp addNotice(HttpRequest request, String title, String content) {
		if(title == null) {
			return Resp.fail("标题不能为空");
		}
		if (content == null) {return Resp.fail("内容不能为空");}

		try {
			Notice notice = new Notice();
			notice.setTitle(title);
			notice.setContent(content);
			notice.setReleaseTime(new Date());
			notice.save();
			return Resp.ok();
		} catch (Exception e) {
			log.error("公告添加失败");
		}
		return Resp.fail().code(500);
	}

	/**
	 *  删除公告
	 * @param request
	 * @param id
	 * @return
	 * @author xinji
	 * 2023.09.06
	 */
	@RequestPath(value = "/delNotice")
	public Resp delNotice(HttpRequest request, String id) {
		if(id == null) {
			return Resp.fail("id不能为空");
		}
		Notice notice = Notice.dao.findById(id);
		if(notice == null) {
			return Resp.fail("该id不存在");
		}

		boolean delete = notice.delete();
		return delete ? Resp.ok() : Resp.fail("删除失败");
	}

	/**
	 * 修改公告内容
	 * @param request
	 * @param id
	 * @param title
	 * @param content
	 * @return
	 * @author xinji
	 * 2023.09.06
	 */
	@RequestPath(value = "/updateNotice")
	public Resp updateNotice(HttpRequest request, Integer id, String title, String content) {
		if (id == null) {
			return Resp.fail("id不能为空");
		}

		if (title == null) {
			return Resp.fail("公告标题不能为空");
		}

		if (content == null) {
			return Resp.fail("公告内容不能为空");
		}


		Notice notice = Notice.dao.findById(id);
		if (notice == null) {
			return Resp.fail("该签到id不存在");
		}


		try {
			notice.setTitle(title);
			notice.setContent(content);
			notice.setReleaseTime(new Date());
			notice.update();
			return Resp.ok();
		} catch (Exception e) {
			log.error("任务更新失败");
		}
		return Resp.fail().code(500);
	}

	/**
	 * 修改公告内容
	 * @param request
	 * @param uid
	 * @param type
	 * @param money
	 * @return
	 * @author xinji
	 * 2023.09.16
	 */
	@RequestPath(value = "/updateMoney")
	public Resp updateMoney(HttpRequest request, Integer uid, Integer type, Long money) {
		if (uid == null) {
			return Resp.fail("uid不能为空");
		}

		if (type == null) {
			return Resp.fail("type不能为空");
		}

		if (money == null) {
			return Resp.fail("money不能为空");
		}

		User user = User.dao.findById(uid);
		if (Objects.equals(user.getOpenflag(), Const.YesOrNo.NO)) {
			return Resp.fail("用户未开户");
		}

		// 获取用户钱包信息
		WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", uid);
		if (userCoin == null) {
			return Resp.fail("用户钱包信息为空");
		}
		UserPaymentImg item = UserPaymentImg.dao.findFirst("select * from user_payment_img where uid = ?", uid);
//		"select * from wallet_item_local where rechargeOrWithhold = 1"
		// 修改用户余额
		if (type.equals(0)) {
			userCoin.setCny(userCoin.getCny() - money);
			userCoin.setUpdatetime(new Date());
			WalletItemLocal walletItem = new WalletItemLocal();
			walletItem.setType(5);
			walletItem.setRechargeOrWithhold(2);
			walletItem.setUid(uid);
			walletItem.setAmount(Double.valueOf(money));
			walletItem.setStatus(0);
			walletItem.setSerialnumber(getUUID());
			walletItem.setCreateTime(new Date());
			walletItem.setUpdateTime(new Date());
			walletItem.setToken(request.getHttpSession().getId());

			walletItem.setName("后台操作");
			walletItem.setPaymentAccount("后台操作");
			boolean save = walletItem.save();
			if (!save) {
				return Resp.fail("充值记录提交到后台失败，请联系客服");
			}
		} else {
			userCoin.setCny(userCoin.getCny() + money);
			userCoin.setUpdatetime(new Date());
			WalletItemLocal walletItem = new WalletItemLocal();
			walletItem.setType(5);
			walletItem.setRechargeOrWithhold(1);
			walletItem.setUid(uid);
			walletItem.setAmount(Double.valueOf(money));
			walletItem.setStatus(0);
			walletItem.setSerialnumber(getUUID());
			walletItem.setCreateTime(new Date());
			walletItem.setUpdateTime(new Date());
			walletItem.setToken(request.getHttpSession().getId());
			walletItem.setName("后台操作");
			walletItem.setPaymentAccount("后台操作");
			boolean save = walletItem.save();
			if (!save) {
				return Resp.fail("充值记录提交到后台失败，请联系客服");
			}
		}
		userCoin.update();
		return Resp.ok();
	}

	/**
	 * 提现/充值总记录
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.19
	 */
	@RequestPath(value = "/sysRecords")
	public Resp sysRecords(HttpRequest request) {
		Map data = new HashMap();

		int januaryRecharge = 0;
		int februaryRecharge = 0;
		int marchRecharge =0;
		int aprilRecharge = 0;
		int mayRecharge = 0;
		int juneRecharge = 0;
		int julyRecharge = 0;
		int augustRecharge = 0;
		int septemberRecharge = 0;
		int octoberRecharge = 0;
		int novemberRecharge = 0;
		int DecemberRecharge = 0;

		int januaryWithhold = 0;
		int februaryWithhold = 0;
		int marchWithhold =0;
		int aprilWithhold = 0;
		int mayWithhold = 0;
		int juneWithhold = 0;
		int julyWithhold = 0;
		int augustWithhold = 0;
		int septemberWithhold = 0;
		int octoberWithhold = 0;
		int novemberWithhold = 0;
		int DecemberWithhold = 0;
		List<WalletItemLocal> rechargeRecords = WalletItemLocal.dao.find("select * from wallet_item_local where rechargeOrWithhold = 1 and status = 1 order by createTime");
		List<WalletItemLocal> withholdRecords = WalletItemLocal.dao.find("select * from wallet_item_local where rechargeOrWithhold = 2 and status = 1 order by createTime");
		Double rechargeNum = 0.0;
		Double withholdNum = 0.0;
		Map<String, Integer> currTime = currTime();
		Integer currMonth = currTime.get("month");
		int[][] timeList = new int[12][12];
		switch (currMonth - 12) {
			case 0:
				timeList = new int[][]{{currTime.get("year"), 12}, {currTime.get("year"), 11}, {currTime.get("year"), 10}, {currTime.get("year"), 9}, {currTime.get("year"), 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -1:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year"), 11}, {currTime.get("year"), 10}, {currTime.get("year"), 9}, {currTime.get("year"), 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -2:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year"), 10}, {currTime.get("year"), 9}, {currTime.get("year"), 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -3:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year"), 9}, {currTime.get("year"), 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -4:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year"), 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -5:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year"), 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -6:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year"), 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -7:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year") - 1, 6}, {currTime.get("year"), 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -8:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year") - 1, 6}, {currTime.get("year") - 1, 5}, {currTime.get("year"), 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -9:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year") - 1, 6}, {currTime.get("year") - 1, 5}, {currTime.get("year") - 1, 4}, {currTime.get("year"), 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -10:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year") - 1, 6}, {currTime.get("year") - 1, 5}, {currTime.get("year") - 1, 4}, {currTime.get("year") - 1, 3}, {currTime.get("year"), 2}, {currTime.get("year"), 1}};
				break;
			case -11:
				timeList = new int[][]{{currTime.get("year") - 1, 12}, {currTime.get("year") - 1, 11}, {currTime.get("year") - 1, 10}, {currTime.get("year") - 1, 9}, {currTime.get("year") - 1, 8}, {currTime.get("year") - 1, 7}, {currTime.get("year") - 1, 6}, {currTime.get("year") - 1, 5}, {currTime.get("year") - 1, 4}, {currTime.get("year") - 1, 3}, {currTime.get("year") - 1, 2}, {currTime.get("year"), 1}};
				break;
		}
		if (rechargeRecords != null) {
			for (WalletItemLocal temp : rechargeRecords) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(temp.getCreateTime());
				for (int[] ints : timeList) {
					if (calendar.get(Calendar.YEAR) == ints[0] && calendar.get(Calendar.MONTH) + 1 == ints[1]) {
						switch (calendar.get(Calendar.MONTH) + 1) {
							case 1:
								januaryRecharge += temp.getAmount();
								break;
							case 2:
								februaryRecharge += temp.getAmount();
								break;
							case 3:
								marchRecharge += temp.getAmount();
								break;
							case 4:
								aprilRecharge += temp.getAmount();
								break;
							case 5:
								mayRecharge += temp.getAmount();
								break;
							case 6:
								juneRecharge += temp.getAmount();
								break;
							case 7:
								julyRecharge += temp.getAmount();
								break;
							case 8:
								augustRecharge += temp.getAmount();
								break;
							case 9:
								septemberRecharge += temp.getAmount();
								break;
							case 10:
								octoberRecharge += temp.getAmount();
								break;
							case 11:
								novemberRecharge += temp.getAmount();
								break;
							case 12:
								DecemberRecharge += temp.getAmount();
								break;
							default:
								break;
						}
					}
				}

				rechargeNum += temp.getAmount();
			}
		}
		if (withholdRecords != null) {
			for (WalletItemLocal temp : withholdRecords) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(temp.getCreateTime());
				for (int[] ints : timeList) {
					if (calendar.get(Calendar.YEAR) == ints[0] && calendar.get(Calendar.MONTH) + 1 == ints[1]) {
						switch (calendar.get(Calendar.MONTH) + 1) {
							case 1:
								januaryWithhold += temp.getAmount();
								break;
							case 2:
								februaryWithhold += temp.getAmount();
								break;
							case 3:
								marchWithhold += temp.getAmount();
								break;
							case 4:
								aprilWithhold += temp.getAmount();
								break;
							case 5:
								mayWithhold += temp.getAmount();
								break;
							case 6:
								juneWithhold += temp.getAmount();
								break;
							case 7:
								julyWithhold += temp.getAmount();
								break;
							case 8:
								augustWithhold += temp.getAmount();
								break;
							case 9:
								septemberWithhold += temp.getAmount();
								break;
							case 10:
								octoberWithhold += temp.getAmount();
								break;
							case 11:
								novemberWithhold += temp.getAmount();
								break;
							case 12:
								DecemberWithhold += temp.getAmount();
								break;
							default:
								break;
						}
					}
				}
				withholdNum += temp.getAmount();
			}
		}
		Double sysBalance = rechargeNum - withholdNum;
		List<Map<String, Integer>> resData = new ArrayList<Map<String, Integer>>();
		for (int[] ints : timeList) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("year", ints[0]);
			map.put("month", ints[1]);
			switch (ints[1]) {
				case 1:
					map.put("rechargeNum", januaryRecharge);
					map.put("withholdNum", januaryWithhold);
					break;
				case 2:
					map.put("rechargeNum", februaryRecharge);
					map.put("withholdNum", februaryWithhold);
					break;
				case 3:
					map.put("rechargeNum", marchRecharge);
					map.put("withholdNum", marchWithhold);
					break;
				case 4:
					map.put("rechargeNum", aprilRecharge);
					map.put("withholdNum", aprilWithhold);
					break;
				case 5:
					map.put("rechargeNum", mayRecharge);
					map.put("withholdNum", mayWithhold);
					break;
				case 6:
					map.put("rechargeNum", juneRecharge);
					map.put("withholdNum", juneWithhold);
					break;
				case 7:
					map.put("rechargeNum", julyRecharge);
					map.put("withholdNum", julyWithhold);
					break;
				case 8:
					map.put("rechargeNum", augustRecharge);
					map.put("withholdNum", augustWithhold);
					break;
				case 9:
					map.put("rechargeNum", septemberRecharge);
					map.put("withholdNum", septemberWithhold);
					break;
				case 10:
					map.put("rechargeNum", octoberRecharge);
					map.put("withholdNum", octoberWithhold);
					break;
				case 11:
					map.put("rechargeNum", novemberRecharge);
					map.put("withholdNum", novemberWithhold);
					break;
				case 12:
					map.put("rechargeNum", DecemberRecharge);
					map.put("withholdNum", DecemberWithhold);
					break;
				default:
					break;
			}
			resData.add(map);
		}

		data.put("rechargeNum",rechargeNum);
		data.put("withholdNum",withholdNum);
		data.put("sysBalance",sysBalance);
		data.put("res",resData);
		return Resp.ok(data);
	}

	public static Map<String, Integer> currTime() {
		Map<String, Integer> res = new HashMap<String, Integer>();
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		res.put("year", calendar.get(Calendar.YEAR));
		res.put("month", calendar.get(Calendar.MONTH) + 1);
		res.put("day", calendar.get(Calendar.DAY_OF_MONTH));
		return res;
	}

//	public static String getUUID(){
//
//		UUID uuid=UUID.randomUUID();
//
//		String str = uuid.toString();
//
//		String uuidStr=str.replace("-", "");
//
//		return uuidStr;
//
//	}

	/**
	 *  更新签到规则
	 * @param request
	 * @param content
	 * @return
	 * @author xinji
	 * 2023.09.21
	 */
	@RequestPath(value = "/updateSignRole")
	public Resp updateSignRole(HttpRequest request, String content) {
		if (content == null || content.isEmpty()) {
			return Resp.fail().msg("签到规则内容不能为空");
		}
		List<SignRole> all = SignRole.dao.findAll();

		if (all == null) {
			SignRole signRole = new SignRole();
			signRole.setContent(content);
			signRole.setCreateTime(new Date());
			signRole.save();
			return Resp.ok();
		} else {
			SignRole signRole = all.get(0);
			signRole.setContent(content);
			signRole.setUpdateTime(new Date());
			signRole.update();
		}

		return Resp.ok();
	}

	/**
	 *  查看签到规则
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.21
	 */
	@RequestPath(value = "/getSignRole")
	public Resp getSignRole(HttpRequest request) {
		SignRole signRole = SignRole.dao.findFirst("select * from sign_role");

		return Resp.ok(signRole);
	}

	/**
	 * 客户端开关配置
	 */

	/**
	 *  添加客户端配置
	 * @param request
	 * @param name
	 * @param value
	 * @param describe
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/addClientConf")
	public Resp addClientConf(HttpRequest request, String name, Integer value, String describe) {
		if (name == null || name.isEmpty()) {
			return Resp.fail().msg("配置名称不能为空");
		}
		if (value == null) {
			return Resp.fail().msg("参数值不能为空");
		}
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", name);
		if (clientConf != null) {
			return Resp.fail().msg("该配置已存在");
		}
		ClientConf clientConf2 = new ClientConf();
		clientConf2.setName(name);
		clientConf2.setValue(value);
		clientConf2.setDescribe(describe);
		clientConf2.setCreatetime(new Date());
		clientConf2.save();
		return Resp.ok();
	}

	/**
	 *  修改客户端配置
	 * @param request
	 * @param name
	 * @param value
	 * @param describe
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/updateClientConf")
	public Resp updateClientConf(HttpRequest request, String name, Integer value, String describe) {
		if (name == null || name.isEmpty()) {
			return Resp.fail().msg("配置名称不能为空");
		}
		if (value == null) {
			return Resp.fail().msg("参数值不能为空");
		}
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", name);
		if (clientConf == null) {
			return Resp.fail().msg("该配置不存在");
		}
		if (value != null && !value.equals(clientConf.getValue())) {
			clientConf.setValue(value);
		}
		if (describe != null && !describe.isEmpty() && !describe.equals(clientConf.getDescribe())) {
			clientConf.setDescribe(describe);
		}

		clientConf.setUpdatetime(new Date());
		clientConf.update();
		return Resp.ok();
	}

	/**
	 *  查询指定客户端配置
	 * @param request
	 * @param name
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/getClientConfByName")
	public Resp updateClientConf(HttpRequest request, String name) {
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", name);
		if (clientConf == null) {
			return Resp.fail().msg("该配置不存在");
		}
		return Resp.ok(clientConf);
	}

	/**
	 *  查询所有客户端配置
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/getClientConf")
	public Resp getClientConf(HttpRequest request) {
		List<ClientConf> clientConfs = ClientConf.dao.findAll();
		return Resp.ok(clientConfs);
	}

    /**
     *  邀请码显示开关
     * @param request
     * @return
     * @author xinji
     * 2023.10.16
     */
    @RequestPath(value = "/isOpenInviteCodeShow")
    public Resp isOpenInviteCodeShow(HttpRequest request, String uids, Integer inviteshow) {
        if (uids == null) {
            return Resp.fail("uid不能为空");
        }
        if (!inviteshow.equals(0) && !inviteshow.equals(1)) {
            return Resp.fail("参数异常，请检查参数是否正确");
        }
		int successNum = 0;
        if (uids.equals("0")) {
            List<User> Users = User.dao.findAll();
            for (User user : Users) {
                user.setInviteshow(inviteshow);
                user.update();
				UserService.ME.notifyClearCache(user.getId());
				successNum++;
            }
        } else {
            String[] split = uids.split(",");
			for (String uid : split) {
				User user = User.dao.findById(uid);
				if (user != null) {
					user.setInviteshow(inviteshow);
					user.update();
					UserService.ME.notifyClearCache(user.getId());
					successNum++;
				}
			}
        }
        return Resp.ok("成功修改" + successNum + "位用户");
    }



	/**
	 *  添加黑名单
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/addIpBlack")
	public Resp addIpBlack(HttpRequest request, String ip, String remark) {
		Integer currId = WebUtils.currUserId(request);

		TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
		tioIpPullblackLog.setIp(ip);
		tioIpPullblackLog.setIpid(IpInfoService.ME.save(request.getClientIp()).getId());
		tioIpPullblackLog.setRemark(remark);
		tioIpPullblackLog.setServer(Const.MY_IP);
		tioIpPullblackLog.setServerport(request.getChannelContext().getServerNode().getPort());
		tioIpPullblackLog.setTime(new Date());
		tioIpPullblackLog.setType((short) 3);

		tioIpPullblackLog.setSessionid(request.getHttpSession().getId());
		tioIpPullblackLog.setCookie(request.getHeader(HttpConst.RequestHeaderKey.Cookie));
		tioIpPullblackLog.setInitpath(request.requestLine.getInitPath());
		tioIpPullblackLog.setPath(request.requestLine.getPath());
		tioIpPullblackLog.setRequestline(request.requestLine.toString());
		tioIpPullblackLog.setUid(currId);

		TioIpPullblackLogService.ME.addToBlack(tioIpPullblackLog);

		IpBlackListService.me.save(ip, remark);
		return Resp.ok();
	}


	/**
	 *  删除黑名单
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/deleteIpBlack")
	public Resp deleteIpBlack(HttpRequest request, String ip, String remark) {
		log.warn("ip:{}从黑名单中删除", ip);

		PullIpToBlackVo pullIpToBlackVo = new PullIpToBlackVo();
		pullIpToBlackVo.setIp(ip);
		pullIpToBlackVo.setRemark(remark);
		pullIpToBlackVo.setType(PullIpToBlackVo.Type.DELETE_IP_FROM_BLACK);
		RTopic pullIpToBlackTopic = RedisInit.get().getTopic(Const.Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.publish(pullIpToBlackVo);

		//		TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
		//		tioIpPullblackLog.setIp(ip);
		//		tioIpPullblackLog.setIpid(IpInfoService.ME.save(ip).getId());
		//		tioIpPullblackLog.setRemark(remark);
		//		tioIpPullblackLog.setServer(org.tio.mg.service.Const.SERVICE_HOST);
		//		tioIpPullblackLog.setServerport(serverport);
		//		tioIpPullblackLog.setTime(new Date());
		//		tioIpPullblackLog.setType(TioIpPullblackLog.Type.HTTP_REQUEST_TOO_FREQUENTLY);
		//
		//		TioIpPullblackLogService.ME.save(tioIpPullblackLog);

		IpBlackListService.me.delete(ip, remark);
		return Resp.ok();
	}


	/**
	 *  黑名单列表
	 * @param request
	 * @return
	 * @author xinji
	 * 2023.09.28
	 */
	@RequestPath(value = "/getIpBlackList")
	public Resp getIpBlackList(HttpRequest request, Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_CONF).getSqlPara("group.getipblacklist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_CONF).paginate(pageNumber, pageSize, sqlPara);
		Ret ret = RetUtils.okPage(records);
		if(ret.isFail()) {
			log.error("获取黑名单列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
}


