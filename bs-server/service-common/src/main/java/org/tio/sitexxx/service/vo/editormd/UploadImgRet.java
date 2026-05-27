
/**
 * 
 */
package org.tio.sitexxx.service.vo.editormd;

import java.io.Serializable;

/**
 * @author tanyaowu
 *
 */
public class UploadImgRet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2955211594791449403L;

	public static UploadImgRet ok(String message, String url) {
		UploadImgRet ret = new UploadImgRet();
		ret.setSuccess(1);
		ret.setMessage(message);
		ret.setUrl(url);
		return ret;
	}

	public static UploadImgRet fail(String message) {
		UploadImgRet ret = new UploadImgRet();
		ret.setSuccess(0);
		ret.setMessage(message);
		return ret;
	}

	/**
	 * 0 | 1, //0表示上传失败;1表示上传成功
	 */
	private int		success	= 1;
	/**
	 * 提示的信息
	 */
	private String	message	= null;
	/**
	 * 上传成功时才返回
	 */
	private String	url		= null;

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 
	 */
	public UploadImgRet() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
