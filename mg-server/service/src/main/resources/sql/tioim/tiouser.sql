-- 用户列表
#sql("list")
	SELECT
		u.id,
		u.nick,
		u.avatar, 
		#if(email != null && email != "")
            u.loginname,
			u.email,
			u.phone,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname,
            '******' email,
			'******' phone,
		#end
		u.createtime,
		u.`status`,
	    u.inviteshow,
	    u.invitecode,
	    u.parentinvitecode,
		ub.sign,
		ub.sex,
		ub.validphone,
		ub.validemail,
		ri.ip,
		ri.province,
		u.realnameflag,
		ri.city,
		thirdtable.thirdtype,
		ll.createtime lastlogintime,
		col.cny,
        u.is_beautiful_id,
        u.beautiful_id,
		u.beautiful_id_create_time,
		u.beautiful_id_expire_time,
		u.source
	FROM
		`user` u
	INNER JOIN user_base ub ON u.id = ub.uid
	LEFT JOIN ip_info ri ON ri.id = u.ipid
    LEFT JOIN wx_user_coin_local col ON u.id = col.uid
	LEFT JOIN user_last_login_time ll ON ll.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			GROUP_CONCAT(type) thirdtype
		FROM
			user_third
		GROUP BY
			uid
	) AS thirdtable ON thirdtable.uid = u.id
	where
		#if(status != null && status != "")
			 u.`status` = #para(status) 
		#end
		#if(defaultstatus != null && defaultstatus != "")
			1=1
		#end
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid) 
				or u.id = #para(searchid) or (beautiful_id = #para(searchid) and now() < beautiful_id_expire_time))
		#end
	    and user_type != 2
    #if(orderby == 1)
        order by createtime
    #end
    #if(orderby == 2)
        order by lastlogintime
    #end
    #if(sort == 1)
        asc
    #end
    #if(sort == 2)
        desc
    #end
    #if(orderby == null || orderby == "")
        order by createtime desc
    #end
#end

-- 用户信息
#sql("info")
	SELECT
		u.id,
		u.nick,
		u.avatar,
		#if(email != null && email != "")
            u.loginname,
			u.email,
			u.phone,
		#end
		#if(noemail != null && noemail != "")
		    '******' loginname,
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
		`user` u
	INNER JOIN user_base ub ON u.id = ub.uid
	LEFT JOIN ip_info ri ON ri.id = u.ipid
	LEFT JOIN user_last_login_time ll ON ll.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			wx_friend
		WHERE
			 uid = #para(uid) and `status` = 1 
	) AS friend ON friend.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			wx_group
		WHERE
			uid = #para(uid) and `status` = 1 
	) AS grouptable ON grouptable.uid = u.id
	LEFT JOIN (
		SELECT
			uid,
			count(1) count
		FROM
			wx_group_user
		WHERE
			uid = #para(uid) and `status` = 1
	) AS allgroup ON allgroup.uid = u.id
	LEFT JOIN (
		SELECT
			fromuid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			wx_call_item
		WHERE
			fromuid = #para(uid) and callduration > 0
	) AS fromcall ON fromcall.uid = u.id
	LEFT JOIN (
		SELECT
			touid uid,
			count(1) count,
			sum(callduration) callduration
		FROM
			wx_call_item
		WHERE
			touid = #para(uid) and  callduration > 0
	) AS tocall ON tocall.uid = u.id
	where
		u.id = #para(uid) 
#end
			
-- 统计用户列表
#sql("statlist")
	SELECT
		u.id,
		#if(email != null && email != "")
			u.phone loginname, 
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		u.nick,
		u.avatar,
		#if(email != null && email != "")
			u.email,
			u.phone,
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
		friend.count fcount,
		allgroup.count agcount,
		ll.createtime lastlogintime
	FROM
		`user` u
	INNER JOIN user_base ub ON u.id = ub.uid
	LEFT JOIN ip_info ri ON ri.id = u.ipid
	LEFT JOIN user_last_login_time ll ON ll.uid = u.id
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
			wx_group_user
		WHERE
			`status` = 1
		GROUP BY
			uid
	) AS allgroup ON allgroup.uid = u.id
	where
		 u.createtime >= #para(starttime) 
		 and u.createtime <= #para(endtime) 
		#if(ipid != null && ipid != "")
		  	and u.ipid = #para(ipid) 
		#end
	order by u.id desc
#end

-- 群举报列表-已调整-New
#sql("reportListNew")
SELECT
    u1.nick as reportUserNick,
    u1.avatar as reportUserAvatar,
    u1.id as reportUserId,
    re.*,
    u2.nick as reportedUserNick,
    u2.avatar as reportedUserAvatar,
    u2.id as reportedUserId,
    u2.status as reportedUserStatus
FROM
    wx_user_report re,
    user u1,
    user u2
WHERE
    re.uid = u1.id
    AND re.touid = u2.id
    AND re.type = #para(type)
    #if(searchkey != null && searchkey != "")
		AND (u2.nick like #para(searchkey) or u2.id = #para(searchkey))
	#end
    #if(status != null && status != "")
		AND re.`status` = #para(status)
	#end
    order by re.status, createtime desc
#end