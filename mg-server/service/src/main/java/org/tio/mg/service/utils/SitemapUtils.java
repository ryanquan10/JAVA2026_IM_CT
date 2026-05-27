
/**
 * 
 */
package org.tio.mg.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.Threads;
import org.tio.utils.http.HttpUtils;
import org.tio.utils.thread.pool.AbstractSynRunnable;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author tanyaowu
 *
 */
public class SitemapUtils {

    private static Logger log = LoggerFactory.getLogger(SitemapUtils.class);

    /**
     * 
     */
    public SitemapUtils() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    public static String blogBody(Long blogid) {
	return new StringBuilder().append(Const.SITE).append("/").append(blogid).toString();
    }

    public static String tioCaseBody(Long cid) {
	return new StringBuilder().append(Const.SITE).append("/2/case/caseInfo.html?id=").append(cid).toString();
    }

    public static void push(String bodyString) throws Exception {
	notifyBaidu(Const.BaiduZiyuan.PUSH_URL, bodyString);
    }

    public static void update(String bodyString) throws Exception {
	notifyBaidu(Const.BaiduZiyuan.UPDATE_URL, bodyString);
    }

    public static void delete(String bodyString) throws Exception {
	notifyBaidu(Const.BaiduZiyuan.DELETE_URL, bodyString);
    }

    private static void notifyBaidu(String baiduUrl, String bodyString) {
//	int x = 6;
//	if (x > 1) {// 暂时不再往百度上面提交东西
//	    return;
//	}

	if (!Const.PRODUCT_SITE.equals(Const.SITE)) {
	    return;
	}
	Threads.getTioExecutor().execute(new AbstractSynRunnable(Threads.getTioExecutor()) {
	    @Override
	    public boolean isNeededExecute() {
		return false;
	    }

	    @Override
	    public void runTask() {
		try {
		    Response response = HttpUtils.post(baiduUrl, null, bodyString);

		    if (response != null) {
			ResponseBody responseBody = response.body();
			String respStr = "";
			if (responseBody != null) {
			    respStr = responseBody.string();
			}
			log.error("提交到百度统计\r\nurl\r\n{}\r\nbody\r\n{}\r\nresp\r\n{}", baiduUrl, bodyString, respStr);
		    }

		} catch (Exception e) {
		    log.error("", e);
		}
	    }
	});
    }

}
