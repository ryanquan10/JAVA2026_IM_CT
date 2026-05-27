-- 群消息统计下的群模型列表
#sql("modeGroupStatList")
	SELECT
		g.id,
		g.avatar,
		g.`name`,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		m.updatetime,
		m.joinnum,
		t
	FROM
		wx_group g
	INNER JOIN (
		SELECT
			groupid,
			max(id) maxid,
			count(1),
			text t
		FROM
			wx_group_msg
		where 1 = 1
		#if(starttime != null && starttime != "")
			 and createtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			 and createtime <= #para(endtime)
		#end
        #if(searchkey != null && searchkey != "")
            AND text like #para(searchkey)
            AND sendbysys != 1
            AND contenttype = 1
        #end
		GROUP BY
			groupid
	) AS gstat ON gstat.groupid = g.id
	INNER JOIN wx_group_meta m ON m.groupid = g.id
	INNER JOIN `user` guser ON guser.id = g.uid
	where
			1 = 1
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
	ORDER BY
		gstat.maxid DESC
#end

-- 群消息统计下的群模型列表-备份
#sql("bakModeGroupStatList")
	SELECT
		g.id,
		g.avatar,
		g.`name`,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		m.updatetime,
		m.joinnum
	FROM
		wx_group g
	INNER JOIN (
		SELECT
			groupid,
			max(id) maxid,
			count(1)
		FROM
			wx_group_msg_bak
		where 1 = 1
		#if(starttime != null && starttime != "")
			 and createtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			 and createtime <= #para(endtime)
		#end
        #if(searchkey != null && searchkey != "")
            AND text like #para(searchkey)
            AND sendbysys != 1
            AND contenttype = 1
        #end
		GROUP BY
			groupid
	) AS gstat ON gstat.groupid = g.id
	INNER JOIN wx_group_meta m ON m.groupid = g.id
	INNER JOIN `user` guser ON guser.id = g.uid
	where
			1 = 1
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
	ORDER BY
		gstat.maxid DESC
#end


-- 群消息统计下的群模型列表-备份
#sql("delModeGroupStatList")
	SELECT
		g.id,
		g.avatar,
		g.`name`,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		m.updatetime,
		m.joinnum
	FROM
		wx_group_bak g
	INNER JOIN (
		SELECT
			groupid,
			max(id) maxid,
			count(1)
		FROM
			wx_group_msg_bak
		where 1 = 1
		#if(starttime != null && starttime != "")
			 and createtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			 and createtime <= #para(endtime)
		#end
        #if(searchkey != null && searchkey != "")
            AND text like #para(searchkey)
            AND sendbysys != 1
            AND contenttype = 1
        #end
		GROUP BY
			groupid
	) AS gstat ON gstat.groupid = g.id
	INNER JOIN wx_group_meta m ON m.groupid = g.id
	INNER JOIN `user` guser ON guser.id = g.uid
	where
			1 = 1
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
	ORDER BY
		gstat.maxid DESC
#end


-- 有效群查询
#sql("grouplist")
	SELECT
		g.id,
		g.`name`,
		g.avatar,
		g.intro,
		g.notice,
		g.noticetime,
		g.createtime,
		g.applyflag,
		g.joinmode,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		CASE
	WHEN msg.count IS NULL THEN
		0
	ELSE
		msg.count
	END msgcount,
	 manager.mcount,
	 manager.count gusercount
	FROM
		wx_group g
	INNER JOIN `user` guser ON guser.id = g.uid
	LEFT JOIN (
		SELECT
			groupid,
			count(1) count
		FROM
			wx_group_msg
		GROUP BY
			groupid
	) AS msg ON msg.groupid = g.id
	LEFT JOIN (
		SELECT
			groupid,
			sum(grouprole = #(managerrole)) mcount,
			count(1) count
		FROM
			wx_group_user
		GROUP BY
			groupid
	) AS manager ON manager.groupid = g.id
	where
		 	 g.createtime >= #para(starttime)
		 	AND g.createtime <= #para(endtime)
		#if(searchkey != null && searchkey != "")
			 and (guser.loginname like #para(searchkey) or guser.nick like #para(searchkey))
		#end
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
	ORDER BY
		g.id DESC
#end

-- 群禁用用户列表-已调整
#sql("forbiddenUserList")
	SELECT
		wgu.id,
		wgu.uid,
		wgu.groupid,
		wgu.grouprole,
		wgu.srcnick,
		wgu.groupnick nick,
		wgu.forbiddenflag,
		wgu.forbiddenduration,
		wgu.cancelforbiddentime,
		wgu.groupavator avatar
	FROM
		wx_group_user wgu
	INNER JOIN (
		SELECT
			gpulinkid
		FROM
			wx_chat_group_item
		WHERE
			groupid = #para(groupid)
	) AS groupindex ON groupindex.gpulinkid = wgu.id
	where 
		wgu.forbiddenflag != #para(noflag)
	ORDER BY
		wgu.forbiddenflag,
		wgu.id
	
#end

-- 群管理员列表-已调整
#sql("managerUserList")
	SELECT
		wgu.id,
		wgu.uid,
		wgu.groupid,
		wgu.grouprole,
		wgu.srcnick,
		wgu.groupnick nick,
		wgu.setroletime,
		wgu.groupavator avatar
	FROM
		wx_group_user wgu
	INNER JOIN (
		SELECT
			gpulinkid
		FROM
			wx_chat_group_item
		WHERE
			groupid = #para(groupid)
	) AS groupindex ON groupindex.gpulinkid = wgu.id
	where 
		wgu.grouprole = #para(managerrole)
	ORDER BY
		wgu.id
#end

-- 群举报列表-已调整
#sql("reportList")
	SELECT
		re.*, u.nick,
		u.avatar,
		u.id
	FROM
		wx_user_report re
	INNER JOIN `user` u ON u.id = re.uid
	WHERE
		re.type = #para(type)
	AND groupid = #para(groupid)
	#if(status != null && status != "")
		AND re.`status` = #para(status)
	#end
#end


-- 群举报列表-已调整-New
#sql("reportListNew")
SELECT
    g.name as groupname,
    g.id as groupid,
    g.status as groupstatus,
    re.*, u.nick as reportUserNick,
    u.avatar as reportUserAvatar,
    u.id as reportUserId
FROM
    wx_user_report re,
    `user` u,
    wx_group g
WHERE
        re.type = #para(type)
  AND u.id = re.uid
  AND g.id = re.groupid
    #if(searchkey != null && searchkey != "")
		AND (g.`name` like #para(searchkey) or g.id = #para(searchkey))
	#end
    #if(status != null && status != "")
		AND re.`status` = #para(status)
	#end
	order by re.status, createtime desc
#end

-- 群封停操作列表-已调整
#sql("inblackOperList")
	SELECT
		*
	FROM
		wx_group_inblack
	WHERE
		groupid = #para(groupid)
#end


-- 群封停操作列表-已调整
#sql("getipblacklist")
SELECT
    *
FROM
    ip_black_list
where
    status = 1
order by
    createtime
desc
#end

-- 群管理查询
#sql("mangergrouplist")
	SELECT
		g.id,
		g.`name`,
		g.avatar,
		g.intro,
		g.notice,
		g.maximum,
		g.`status`,
		meta.joinnum,
		meta.forbiddenflag,
		g.friendflag,
		g.noticetime,
		g.createtime,
		g.applyflag,
		g.joinmode,
		g.vnum,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		CASE
	WHEN report.nodealreport IS NULL THEN
		0
	ELSE
		report.nodealreport
	END rdealcount,
	 CASE
	WHEN report.count IS NULL THEN
		0
	ELSE
		report.count
	END rcount,
    CASE
        WHEN dg.groupid IS NULL THEN
	    0
	ELSE
	    1
	END isdefault
	FROM
		wx_group g
	INNER JOIN `user` guser ON guser.id = g.uid
	INNER JOIN wx_group_meta meta ON meta.groupid = g.id
	LEFT JOIN (
		SELECT
			groupid,
			sum(`status` = 2) nodealreport,
			count(1) count
		FROM
			wx_user_report
		WHERE
			type = 2
		GROUP BY
			groupid
	) AS report ON report.groupid = g.id
	LEFT JOIN default_group dg on g.id = dg.groupid
	where
		 	1=1
		#if(searchkey != null && searchkey != "")
			 and (guser.loginname like #para(searchkey) or guser.nick like #para(searchkey) or guser.phone = #para(phone))
		#end
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
		#if(status != null && status != "")
			 and g.`status` = #para(status)
		#end
	ORDER BY
		g.id DESC
#end

-- 无效群查询
#sql("delgrouplist")
	SELECT
		g.id,
		g.`name`,
		g.avatar,
		g.intro,
		g.notice,
		g.noticetime,
		g.createtime,
		g.updatetime,
		g.applyflag,
		g.joinmode,
		guser.id uid,
		#if(email != null && email != "")
			guser.loginname loginname,
		#end
		#if(noemail != null && noemail != "")
			'******' loginname, 
		#end
		guser.nick usernick,
		guser.avatar useravatar,
		CASE 
			WHEN 
				msg.count IS NULL THEN 0
			ELSE
				msg.count 
		END msgcount,
		CASE
			WHEN manager.mcount IS NULL THEN
				0
			ELSE
				manager.mcount
		END mcount,
		
		CASE
			WHEN manager.count IS NULL THEN
				0
			ELSE
				manager.count
		END gusercount,
		CASE
			WHEN bakmsg.count IS NULL THEN
				0
			ELSE
				bakmsg.count
		END bakmsgcount,
		
		CASE
			WHEN bakmanager.mcount IS NULL THEN
				0
			ELSE
				bakmanager.mcount
		END bakmcount,
		
		CASE
			WHEN bakmanager.count IS NULL THEN
				0
			ELSE
				bakmanager.count
		END bakgusercount

	FROM
		wx_group_bak g
	INNER JOIN `user` guser ON guser.id = g.uid
	LEFT JOIN (
		SELECT
			groupid,
			count(1) count
		FROM
			wx_group_msg_bak
		GROUP BY
			groupid
	) AS bakmsg ON bakmsg.groupid = g.id
	LEFT JOIN (
			SELECT
				groupid,
				count(1) count
			FROM
				wx_group_msg
			GROUP BY
				groupid
		) AS msg ON msg.groupid = g.id
	LEFT JOIN (
		SELECT
			groupid,
			sum(grouprole = #(managerrole)) mcount,
			count(1) count
		FROM
			wx_group_user_bak
		GROUP BY
			groupid
	) AS bakmanager ON bakmanager.groupid = g.id
	LEFT JOIN (
			SELECT
				groupid,
				sum(grouprole = #(managerrole)) mcount,
				count(1) count
			FROM
				wx_group_user
			GROUP BY
				groupid
		) AS manager ON manager.groupid = g.id
	where
			 g.createtime >= #para(starttime)
		 	AND g.createtime <= #para(endtime)
		#if(searchkey != null && searchkey != "")
			 and (guser.loginname like #para(searchkey) or guser.nick like #para(searchkey))
		#end
		#if(groupkey != null && groupkey != "")
			 and (g.id = #para(gid)  or g.`name` like #para(groupkey))
		#end
	ORDER BY
		g.id DESC
#end