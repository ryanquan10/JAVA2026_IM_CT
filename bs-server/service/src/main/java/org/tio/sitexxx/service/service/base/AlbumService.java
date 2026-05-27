package org.tio.sitexxx.service.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.model.main.Album;
import org.tio.sitexxx.service.model.main.AlbumPhoto;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class AlbumService {
    private static Logger log = LoggerFactory.getLogger(AlbumService.class);

    public static final AlbumService ME = new AlbumService();


    public Ret addAlbum(User user, String name, String cover, Integer permission, String password, Integer isTop) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Album album = new Album();
        if (isTop != null) {
            album.setIsTop(isTop);
        }
        album.setUid(user.getId());
        album.setName(name);
        album.setCover(cover);
        album.setPermission(permission);
        if (permission.equals(3)) {
            album.setPassword(encipher(password));
        }
        album.setCreateTime(new Date());
        boolean save = album.save();
        if (!save) {
            return Ret.fail().set("errorMsg", "添加相册失败，请重试");
        }
        return Ret.ok();
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

    public Ret albumList(User user, String searchkey, Integer pageNumber, Integer pageSize) {
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("uid",user.getId());
        if (searchkey != null && !searchkey.isEmpty()) {
            params.set("searchkey", "%" + searchkey + "%");
        }

        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("album.albumList", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return RetUtils.okPage(records);
    }

    public Ret albumUpdate(User user, Integer albumId, String name, String cover, Integer permission, String password, Integer isTop) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            return Ret.fail().set("errorMsg", "没有权限修改该相册");
        }
        album.setUid(user.getId());
        album.setName(name);
        album.setCover(cover);
        album.setPermission(permission);
        if (isTop != null) {
            album.setIsTop(isTop);
        }
        if (permission.equals(3)) {
            album.setPassword(encipher(password));
        }
        album.setUpdateTime(new Date());
        boolean update = album.update();
        if (!update) {
            return Ret.fail().set("errorMsg", "操作失败，请重试");
        }
        return Ret.ok();
    }

    public Ret addPhoto(User user, Integer albumId, String imgs, String videos) {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            return Ret.fail().set("errorMsg", "没有权限修改该相册");
        }
        if (imgs != null) {
          String[] imgList = imgs.split(",");
            if (imgList.length > 0) {
                for (String img : imgList) {
                    AlbumPhoto albumPhoto = new AlbumPhoto();
                    albumPhoto.setAlbumId(albumId);
                    albumPhoto.setImg(img);
                    albumPhoto.setType(1);
                    albumPhoto.setCreateTime(new Date());
                    albumPhoto.save();
                }
            }
            album.setPhotoNum(album.getPhotoNum() + imgList.length);
        }
        if (videos != null) {
            String[] videoList = videos.split(",");
            if (videoList.length > 0) {
                for (String video : videoList) {
                    AlbumPhoto albumPhoto = new AlbumPhoto();
                    albumPhoto.setAlbumId(albumId);
                    albumPhoto.setImg(video);
                    albumPhoto.setType(2);
                    albumPhoto.setCreateTime(new Date());
                    albumPhoto.save();
                }
            }
            album.setPhotoNum(album.getPhotoNum() + videoList.length);
        }
        album.setUpdateTime(new Date());
        album.update();
        return Ret.ok();

    }

    public Ret delPhoto(User user, Integer albumId, String ids) {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            return Ret.fail().set("errorMsg", "没有权限删除该相册的图片");
        }
        String[] idList = ids.split(",");
        for (String id : idList) {
            AlbumPhoto albumPhoto = AlbumPhoto.dao.findById(id);
            albumPhoto.delete();
        }
        album.setPhotoNum(album.getPhotoNum() - idList.length);
        album.setUpdateTime(new Date());
        album.update();
        return Ret.ok();
    }

    public Ret photoList(User user, Integer albumId, String password,  Integer pageNumber, Integer pageSize) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            if (album.getPermission().equals(2)) {
                return Ret.fail().set("errorMsg", "该相册为私有相册，没有权限访问");
            }
            if (album.getPermission().equals(3)) {
                if (password == null || password.isEmpty()) {
                    return Ret.fail().set("errorMsg", "请输入访问密码");
                }
                if (!album.getPassword().equals(encipher(password))) {
                    return Ret.fail().set("errorMsg", "密码错误");
                }
            }
        }
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("albumId",albumId);
        params.set("uid",user.getId());

        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("album.photoList", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return RetUtils.okPage(records);
    }

    public Ret albumTop(User user, Integer albumId) {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!album.getUid().equals(user.getId())) {
            return Ret.fail().set("errorMsg", "没有权限进行该操作");
        }
        album.setIsTop(1);
        album.setTopTime(new Date());
        album.setUpdateTime(new Date());
        boolean update = album.update();
        if (!update) {
            return Ret.fail().set("errorMsg", "操作失败，请重试");
        }
        return Ret.ok();
    }

    public Ret albumCancelTop(User user, Integer albumId) {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (!album.getUid().equals(user.getId())) {
            return Ret.fail().set("errorMsg", "没有权限进行该操作");
        }
        album.setIsTop(0);
        album.setTopTime(null);
        album.setUpdateTime(new Date());
        boolean update = album.update();
        if (!update) {
            return Ret.fail().set("errorMsg", "操作失败，请重试");
        }
        return Ret.ok();
    }

    public Ret getPhotoListNoLogin(Integer albumId, String password, Integer pageNumber, Integer pageSize) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Ret.fail().set("errorMsg", "相册不存在");
        }
        if (album.getPermission().equals(2)) {
            return Ret.fail().set("errorMsg", "该相册为私有相册，没有权限访问");
        }
        if (album.getPermission().equals(3)) {
            if (password == null || password.isEmpty()) {
                return Ret.fail().set("errorMsg", "请输入访问密码");
            }
            if (!album.getPassword().equals(encipher(password))) {
                return Ret.fail().set("errorMsg", "密码错误");
            }
        }
        if(pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        params.set("albumId",albumId);

        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("album.photoList", params);
        Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        return RetUtils.okPage(records);
    }

    public Ret albumListById(String ids) {
        List<Album> albums = new ArrayList<>();
        String[] idList = ids.split(",");
        for (String id : idList) {
            Album album = Album.dao.findFirst("select * from album where id = ? and permission != 2", id);
            if (album != null) {
                albums.add(album);
            }
        }

        return Ret.ok().set("records", albums);
    }
}
