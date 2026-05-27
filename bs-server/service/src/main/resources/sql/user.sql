#sql("searchByNick")
	select nick,id,avatar,beautiful_id from user
	where status = 1 and searchflag = 1
		and (	
			phone = #para(search) or id = #para(search) or (beautiful_id = #para(search) and now() < beautiful_id_expire_time )
		)
	order by id desc
#end


#sql("updateBase")
	update user_base 
	set
		#if(sex != null && sex != "")
			sex = #para(sex),
		#end
		sign = #para(sign)
	where 
		uid = #para(uid)
#end

#sql("searchByUid")
	select nick,id,avatar,beautiful_id from user where status = 1 and searchflag = 1 and (id = #para(uid) or (beautiful_id = #para(uid) and now() < beautiful_id_expire_time))
#end

#sql("searchByPhone")
select nick,id,avatar,beautiful_id from user where status = 1 and searchflag = 1 and phone = #para(uid)
    #end

#sql("searchByLoginname")
	select nick,id,avatar,beautiful_id from user where status = 1 and searchflag = 1 and loginname = #para(loginname)
#end

-- 查看某人的登录日志
#sql("pageLoginLog")
SELECT u.nick, u.id, u.loginname, ll.time,ll.deviceinfo,ll.devicetype,ii.ip, ii.province, ii.city, ii.operator, ua.agentName, ua.agentVersionMajor, ua.osName, ua.isMobile, ua.deviceClass
FROM `login_log` ll
LEFT JOIN user_agent ua ON ua.id = ll.uaid
LEFT JOIN user u ON u.id = ll.uid
LEFT JOIN ip_info ii ON ii.id = ll.ipid
WHERE u.id = #para(uid)
ORDER BY u.id, ll.time DESC
#end

-- 查看某人的访问日志
#sql("pageAccessLog")
SELECT u.id as uid, u.nick, u.loginname, ii.province, ii.city, ii.operator, ii.ip, cjl.groupid, cjl.jointime, cjl.leavetime, cjl.cost, cjl.`status` FROM `chatroom_join_leave` cjl 
LEFT JOIN user u on u.id = cjl.uid 
LEFT JOIN ip_info ii on ii.id = cjl.ipid 
where u.id = #para(uid)
order by cjl.id desc
#end



-- 
#sql("loginLastLog")
	INSERT INTO user_last_login_time (
		uid
	)
	(SELECT
		IFNULL(uid, #para(uid))
	FROM
		user_last_login_time
	WHERE
		uid = #para(uid)
    UNION
    SELECT #para(uid) AS uid
    LIMIT 1)
	ON DUPLICATE KEY 
	UPDATE 
	 	createtime = now()
#end