-- 登录日志列表
#sql("list")
	SELECT
		ll.id,
		u.nick,
		#if(email != null && email != "")
			u.loginname,
            u.phone,
	        u.email,
        #end
		#if(noemail != null && noemail != "")
			'******' loginname,
            '******' phone,
            '******' email,
        #end
		u.avatar,
		ll.uid,
		ll.ip,
		i.province,
		i.city,
		ll.devicetype,
		ll.deviceinfo,
		ll.time
	FROM
		login_log ll
	INNER JOIN `user` u ON u.id = ll.uid
	LEFT JOIN ip_info i ON i.id = ll.ipid
	where
		1 = 1
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey) or u.id = #para(searchid))
		#end
		#if(searchip != null && searchip != "")
			and ll.ip = #para(searchip)
		#end
		#if(starttime != null && starttime != "")
			AND ll.time >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND ll.time <= #para(endtime)
		#end
	order by ll.id desc
#end

-- 统计日志列表-天
#sql("statTimeList")
	select * from user_time_login_stat 
	where
		uid = -1
		#if(starttime != null && starttime != "")
			AND dayperiod >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND dayperiod <= #para(endtime)
		#end
	order by dayperiod desc
#end

-- 统计日志列表-用户
#sql("statTimeUserList")
	SELECT
		uls.*,
		#if(email != null && email != "")
			u.loginname, 
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		u.nick,
		u.avatar,
		#if(email != null && email != "")
			ub.email,
			ub.phone,
		#end
		#if(noemail != null && noemail != "")
			'******' email, 
			'******' phone,
		#end
		u.createtime,
		u.`status`,
		ub.sign,
		ub.sex,
		ub.validphone,
		ub.validemail,
		ri.ip,
		ri.province,
		ri.city,
		ll.createtime lastlogintime,
		friend.count fcount,
		grouptable.count gcount,
		allgroup.count agcount,
		fromcall.count fcallcount,
		fromcall.callduration fduration,
		tocall.callduration toduration,
		tocall.count tcallcount
	FROM
		user_time_login_stat uls
	INNER JOIN tio_site_main.`user` u ON uls.uid = u.id
	INNER JOIN tio_site_main.user_base ub ON u.id = ub.uid
	LEFT JOIN tio_site_main.ip_info ri ON ri.id = u.ipid
	LEFT JOIN tio_site_main.user_last_login_time ll ON ll.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_friend
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS friend ON friend.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_group
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS grouptable ON grouptable.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_group_user
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS allgroup ON allgroup.uid = u.id
	LEFT JOIN (
		SELECT
			fromuid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			tio_site_main.wx_call_item
		WHERE
			callduration > 0
		GROUP BY
			fromuid
	) AS fromcall ON fromcall.uid = u.id
	LEFT JOIN (
		SELECT
			touid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			tio_site_main.wx_call_item
		WHERE
			callduration > 0
		GROUP BY
			touid
	) AS tocall ON tocall.uid = u.id
	WHERE
		uls.dayperiod = #para(dayperiod)
	order by uls.totalcount desc,uls.updatetime desc
#end


-- 统计日志列表-日志
#sql("statTimeLoginList")
	SELECT
		ll.*,
		i.province,
		i.city
	FROM
		login_log ll
	LEFT JOIN ip_info i ON i.id = ll.ipid
	where 
			ll.dayperiod = #para(dayperiod)
		AND ll.uid = #para(uid)
	order by ll.id
#end


-- 统计日志列表-Ip
#sql("statIpList")
	SELECT
		*
	FROM
		user_ip_login_stat
	WHERE
			uid = - 1
		AND dayperiod = '-1'
	#if(ip != null && ip != "")
		AND ip = #para(ip)
	#end
	ORDER BY
		#(order) DESC
#end

-- 统计日志列表-ip-天
#sql("statIpDayList")
	SELECT
		*
	FROM
		user_ip_login_stat
	WHERE
		uid = - 1
	AND dayperiod != '-1'
	AND ip = #para(ip)
	ORDER BY
		#(order) DESC
#end

-- 统计日志列表-用户
#sql("statIpUserList")
	SELECT
		uls.*,
		#if(email != null && email != "")
			u.loginname, 
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		u.nick,
		u.avatar,
		#if(email != null && email != "")
			ub.email,
			ub.phone,
		#end
		#if(noemail != null && noemail != "")
			'******' email, 
			'******' phone,
		#end
		u.createtime,
		u.`status`,
		ub.sign,
		ub.sex,
		ub.validphone,
		ub.validemail,
		ri.ip,
		ri.province,
		ri.city,
		ll.createtime lastlogintime,
		friend.count fcount,
		grouptable.count gcount,
		allgroup.count agcount,
		fromcall.count fcallcount,
		fromcall.callduration fduration,
		tocall.callduration toduration,
		tocall.count tcallcount
	FROM
		user_ip_login_stat uls
	INNER JOIN tio_site_main.`user` u ON uls.uid = u.id
	INNER JOIN tio_site_main.user_base ub ON u.id = ub.uid
	LEFT JOIN tio_site_main.ip_info ri ON ri.id = u.ipid
	LEFT JOIN tio_site_main.user_last_login_time ll ON ll.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_friend
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS friend ON friend.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_group
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS grouptable ON grouptable.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			tio_site_main.wx_group_user
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS allgroup ON allgroup.uid = u.id
	LEFT JOIN (
		SELECT
			fromuid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			tio_site_main.wx_call_item
		WHERE
			callduration > 0
		GROUP BY
			fromuid
	) AS fromcall ON fromcall.uid = u.id
	LEFT JOIN (
		SELECT
			touid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			tio_site_main.wx_call_item
		WHERE
			callduration > 0
		GROUP BY
			touid
	) AS tocall ON tocall.uid = u.id
	WHERE
		uls.dayperiod = #para(dayperiod)
		AND uls.ip = #para(ip)
	order by uls.totalcount desc,uls.updatetime desc
#end


-- 统计日志列表-日志
#sql("statIpLoginList")
	SELECT
		ll.*,
		i.province,
		i.city
	FROM
		login_log ll
	LEFT JOIN ip_info i ON i.id = ll.ipid
	where 
			ll.dayperiod = #para(dayperiod)
		AND ll.uid = #para(uid)
		AND ll.ip = #para(ip)
	order by ll.id
#end