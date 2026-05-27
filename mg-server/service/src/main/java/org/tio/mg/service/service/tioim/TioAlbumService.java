
package org.tio.mg.service.service.tioim;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.*;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.Const;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * im好友管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioAlbumService {
	
	@SuppressWarnings("unused")
	private static Logger	log	= LoggerFactory.getLogger(TioAlbumService.class);
	
	public static final TioAlbumService me	= new TioAlbumService();

	public Ret albumList(Integer pageNumber, Integer pageSize, String searchkey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("album.list", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		List<Record> list = records.getList();
		if (list != null && list.size()>0) {
			for (Record record : list) {
				int memberNum = CircleMember.dao.find("select * from circle_member where circle_id = ?", record.get("id").toString()).size();
				record.set("memberNum", memberNum);
				if (record.get("password") != null) {
					record.set("password", decryptor(record.get("password").toString()));
				}
			}
		}
		return RetUtils.okPage(records);
	}

	public String encipher(String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String key = "1234567891123456"; // 16 bytes key for AES-128
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public String decryptor(String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String key = "1234567891123456"; // 16 bytes key for AES-128
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(password));
		return new String(decryptedBytes);
	}
}
