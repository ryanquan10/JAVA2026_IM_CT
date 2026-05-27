-- 用户统计列表
#sql("userstatlist")
	SELECT
		u.id,
		#if(email != null && email != "")
			u.loginname, 
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		u.nick,
		u.avatar,
		friend.count fcount,
		grouptable.count gcount,
		allgroup.count agcount,
		fromcall.count fcallcount,
		fromcall.callduration fduration,
		tocall.callduration toduration,
		tocall.count tcallcount
	FROM
		`user` u
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			wx_friend
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
			wx_group
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
			wx_group_user
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
			wx_call_item
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
			wx_call_item
		WHERE
			callduration > 0
		GROUP BY
			touid
	) AS tocall ON tocall.uid = u.id
	#if(searchkey != null && searchkey != "")
		where
		 (u.loginname like #para(searchkey) or u.nick like #para(searchkey))
	#end
	order by u.id desc
#end


-- 用户注册统计列表
#sql("userRegistList")
	SELECT
		urs.*
	FROM
		user_register_stat urs
	#if(ip != null && ip != "")
		 INNER JOIN tio_site_main.ip_info ri ON ri.id = urs.statbizid
	#end
	WHERE
			urs.type = #para(type)
		#if(starttime != null && starttime != "")
		 	AND urs.statbizstr >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
		 	AND urs.statbizstr <= #para(endtime)
		#end
		#if(searchip != null && searchip != "")
		 	AND urs.statbizstr = #para(searchip)
		#end
		#if(province != null && province != "")
		 	AND ri.province = #para(province)
		#end
		#if(city != null && city != "")
		 	AND ri.city = #para(city)
		#end
	order by 
		#if(time != null && time != "")
		 	urs.statbizstr desc
		#end
		#if(ip != null && ip != "")
		 	urs.#(order) desc
		#end
#end

-- 用户注册统计列表-ip小的时间维度
#sql("userIpTimeRegisterStat")
	SELECT
		*
	FROM
		user_register_stat
	WHERE
			type = #para(type)
		and statbizid =  #para(ipid)
	order by 
	 	#(order) desc
#end

-- 用户注册统计列表-ip小的时间维度
#sql("groupstat")
	SELECT
		*
	FROM
		group_stat
	WHERE
			type = #para(type)
		#if(starttime != null && starttime != "")
		 	AND dayperiod >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
		 	AND dayperiod <= #para(endtime)
		#end
	order by 
	 	dayperiod desc
#end