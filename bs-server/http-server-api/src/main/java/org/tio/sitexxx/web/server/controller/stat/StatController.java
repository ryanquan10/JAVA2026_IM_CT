
package org.tio.sitexxx.web.server.controller.stat;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.model.main.Role;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.base.UserRoleService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.stat.StatService;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.date.DateUtil;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/stat")
public class StatController {
	private static Logger log = LoggerFactory.getLogger(StatController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {
		log.info("");
	}

	/**
	 * -- 按天统计页面的访问量
		---------------------------
		--     d              path        count
		-- 2018-04-16   /case/index.html	 24
		-- 2018-04-17   /case/index.html	 109
		-- 2018-04-17   /doc/index.html	     18
		-- 2018-04-16   /doc/index.html	     97
		---------------------------
	 * @param days
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/requestCountByDay")
	public Resp requestCountByDay(Integer days, HttpRequest request) throws Exception {
		List<Record> list = StatService.me.requestCountByDay(days);

		return Resp.ok(list);
	}

	/**
	 * -- 按天统计不同的ip数
		---------------------------
		--    d         |  count
		-- 2018-04-16	|  35
		-- 2018-04-17	|  222
		---------------------------
	 * @param days
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/ipCountByDay")
	public Resp ipCountByDay(Integer days, HttpRequest request) throws Exception {
		List<Record> list = StatService.me.ipCountByDay(days);
		return Resp.ok(list);
	}

	/**
	 * -- 按省统计点击量
		---------------------------------------------
		--  hitcount | ipcount      |  province
		--  6565     |   545	    |  浙江省
		--  5443     |   434	    |  湖南省
		---------------------------------------------
	 * @param days
	 * @param request
	 * @return
	 */
	@RequestPath(value = "/statIpAndHitsByProvince")
	public Resp statIpAndHitsByProvince(Integer days, HttpRequest request) throws Exception {
		List<Record> list = StatService.me.statIpAndHitsByProvince(days);
		return Resp.ok(list);
	}

	@RequestPath(value = "/ip")
	public Resp ip(Boolean mergeRequest, String starttime, String endtime, Integer pageNumber, Integer pageSize, HttpRequest request) throws Exception {
		User curr = WebUtils.currUser(request);

		if (!UserService.isSuper(curr)) {
			Date startdate = DateUtil.parseDateTime(starttime);
			Date enddate = DateUtil.parseDateTime(endtime);
			long iv = enddate.getTime() - startdate.getTime();

			if (iv < (3600L * 1000L * 5L)) {
				return Resp.fail("<5小时的时间段数据，只有超管可以查询");
			}

			if (iv >= (10L * 3600L * 1000L * 24L)) {
				return Resp.fail(">=10天的时间段数据，只有超管可以查询");
			}

			if (iv >= (5L * 3600L * 1000L * 24L)) {
				if (!UserRoleService.hasRole(curr, Role.PAID_SITECODE)) {
					return Resp.fail(">=5天的时间段数据，只有官网源代码授权用户可以查询");
				}
			}

			if (iv >= (1L * 3600L * 1000L * 24L)) {//大于一天
				if (!UserRoleService.hasRole(curr, Role.PAID_DOC_CODE)) {
					return Resp.fail(">=1天的时间段数据，只有文档VIP授权用户可以查询");
				}
			}

		}

		Page<Record> page = StatService.me.ip(mergeRequest, starttime, endtime, pageNumber, pageSize);
		return Resp.ok(page);
	}

	/**
	 *
	 * @author tanyaowu
	 */
	public StatController() {
	}
}
