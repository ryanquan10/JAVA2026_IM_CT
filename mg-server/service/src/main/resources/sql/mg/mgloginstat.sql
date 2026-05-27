-- 登录列表
#sql("loginlist")
	select 
		ll.id,
		ll.mguid,
		u.loginname,
		u.nick,
		u.avatar,
		roletable.rolename,
		ll.ip,
		i.province,
		i.city,
		ll.deviceinfo,
		ll.createtime
		from mg_user_login_log ll 
		INNER JOIN mg_user u on u.id = ll.mguid
		LEFT JOIN mg_ip_info i on i.id = ll.ipid
		LEFT JOIN (
				SELECT
					mur.mguid,
					GROUP_CONCAT(
						mr.`name`
						ORDER BY
							rindex SEPARATOR ','
					) rolename,
					GROUP_CONCAT(
						mr.`id`
						ORDER BY
							rindex SEPARATOR ','
					) rids
				FROM
					mg_user_role mur
				INNER JOIN mg_role mr ON mur.rid = mr.id
				GROUP BY
					mur.mguid
			) AS roletable ON roletable.mguid = u.id
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
				#if(rid != null && rid != "")
					AND FIND_IN_SET(#(rid) , roletable.rids)
				#end
			order by ll.id desc
#end


-- 统计日志列表-天
#sql("statTimeList")
	select * from mguser_login_stat 
	where
		mguid = -1
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
		uls.*, u.loginname,
		u.nick,
		u.avatar,
		roletable.rolename
	FROM
		mguser_login_stat uls
	INNER JOIN mg_user u on u.id = uls.mguid
	LEFT JOIN (
				SELECT
					mur.mguid,
					GROUP_CONCAT(
						mr.`name`
						ORDER BY
							rindex SEPARATOR ','
					) rolename,
					GROUP_CONCAT(
						mr.`id`
						ORDER BY
							rindex SEPARATOR ','
					) rids
				FROM
					mg_user_role mur
				INNER JOIN mg_role mr ON mur.rid = mr.id
				GROUP BY
					mur.mguid
			) AS roletable ON roletable.mguid = u.id
	WHERE
		uls.dayperiod = #para(dayperiod)
	order by uls.totalcount desc
#end

-- 统计日志列表-日志
#sql("statLoginList")
	SELECT
		ll.*,
		i.province,
		i.city
	FROM
		mg_user_login_log ll
	LEFT JOIN mg_ip_info i ON i.id = ll.ipid
	where 
			ll.dayperiod = #para(dayperiod)
		AND ll.mguid = #para(mguid)
	order by ll.id
#end


-- 统计日志列表-用户
#sql("statUserList")
	SELECT
		uls.*, u.loginname,
		u.nick,
		u.avatar,
		roletable.rolename
	FROM
		mguser_login_stat uls
	INNER JOIN mg_user u on u.id = uls.mguid
	LEFT JOIN (
				SELECT
					mur.mguid,
					GROUP_CONCAT(
						mr.`name`
						ORDER BY
							rindex SEPARATOR ','
					) rolename,
					GROUP_CONCAT(
						mr.`id`
						ORDER BY
							rindex SEPARATOR ','
					) rids
				FROM
					mg_user_role mur
				INNER JOIN mg_role mr ON mur.rid = mr.id
				GROUP BY
					mur.mguid
			) AS roletable ON roletable.mguid = u.id
	WHERE
		uls.dayperiod = '-1'  and uls.mguid != -1
		#if(searchkey != null && searchkey != "")
			 and (u.loginname like #para(searchkey) or u.nick like #para(searchkey) or u.id = #para(searchid))
		#end
	order by uls.totalcount desc
#end


-- 统计日志列表-用户
#sql("statUserDayList")
	SELECT
		uls.*
	FROM
		mguser_login_stat uls
	WHERE
		uls.dayperiod != '-1'
		 and uls.mguid = #para(mguid)
	order by uls.totalcount desc
#end