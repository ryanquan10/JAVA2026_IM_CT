
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.main.Album;
import org.tio.mg.service.model.main.AlbumPhoto;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.tioim.TioAlbumService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 相册管理
 * @author xinji
 * 2024年2月4日
 */
@RequestPath(value = "/album")
public class TioAlbumController {
	private static Logger log = LoggerFactory.getLogger(TioAlbumController.class);

	/**
	 * @param args
	 * @author xinji
	 * 2024年2月4日
	 */
	public static void main(String[] args) {}
	private TioAlbumService albumService = TioAlbumService.me;


	/**
	 * 相册列表
	 * @param request 请求
	 * @param searchKey 搜索关键字
	 * @param pageNumber 页码
	 * @param pageSize 大小
	 * @return Page<Record>
	 * @throws Exception
	 * @author xinji
	 * 2024年2月4日
	 */
	@RequestPath(value = "/albumList")
	public Resp albumList(HttpRequest request, String searchkey, Integer pageNumber, Integer pageSize) throws Exception {
		Ret ret = albumService.albumList(pageNumber, pageSize, searchkey);
		if(ret.isFail()) {
			return Resp.fail(ret.get("errorMsg").toString());
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * 删除相册
	 * @param request 请求
	 * @param albumIds 相册id 使用 , 分隔开
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年2月4日
	 */
	@RequestPath(value = "/circleDel")
	public Resp circleDel(HttpRequest request, String albumIds) throws Exception {
		if (albumIds == null || albumIds.isEmpty()) {
			return Resp.fail().msg("请选择需要删除的相册");
		}
		String[] ids = albumIds.split(",");
		List<String> delFail = new ArrayList<>();
		for (String id : ids) {
			Album album = Album.dao.findById(id);
			if (album == null) {
				delFail.add(id);
				continue;
			}
			boolean delete = album.delete();
			if (!delete) {
				delFail.add(id);
			}
		}
		if (delFail.size() > 0) {
			return Resp.fail().msg(delFail + " 以上id删除失败，请重试");
		}
		return Resp.ok();
	}

	/**
	 * 查看相册图片
	 * @param request 请求
	 * @param albumId 相册id
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年2月4日
	 */
	@RequestPath(value = "/photos")
	public Resp photos(HttpRequest request, Integer albumId) throws Exception {
		if (albumId == null) {
			return Resp.fail().msg("请选择需要查看的相册");
		}
		Album album = Album.dao.findById(albumId);
		if (album == null) {
			return Resp.fail().msg("相册不存在");
		}
		Map<String, Object> data = new HashMap<>();
		List<AlbumPhoto> albumPhotos = AlbumPhoto.dao.find("select * from album_photo where album_id = ?", albumId);
		User user = User.dao.findById(album.getUid());
		data.put("album", album);
		data.put("user", user);
		data.put("albumPhotos", albumPhotos);
		return Resp.ok(data);
	}

	/**
	 * 删除相册图片
	 * @param request 请求
	 * @param photoIds 图片ids
	 * @return boolean
	 * @throws Exception
	 * @author xinji
	 * 2024年2月4日
	 */
	@RequestPath(value = "/delPhotos")
	public Resp delPhotos(HttpRequest request, String photoIds) throws Exception {
		if (photoIds == null || photoIds.isEmpty()) {
			return Resp.fail().msg("请选择需要删除的图片");
		}
		String[] ids = photoIds.split(",");
		List<String> delFail = new ArrayList<>();
		for (String id : ids) {
			AlbumPhoto photo = AlbumPhoto.dao.findById(id);
			if (photo == null) {
				delFail.add(id);
				continue;
			}
			boolean delete = photo.delete();
			if (!delete) {
				delFail.add(id);
			}
		}
		if (delFail.size() > 0) {
			return Resp.fail().msg(delFail + " 以上id删除失败，请重试");
		}
		return Resp.ok();
	}
}
