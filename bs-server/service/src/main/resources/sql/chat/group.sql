-- 群服务脚本专用
-- 用户群列表-通讯录
#sql("grouplist")
	SELECT
		wg.id groupid,
		wg.`name`,
		wg.avatar,
		wg.uid,
		wg.use_card,
		wg.vnum,
		gu.grouprole,
		wgm.joinnum
	FROM
		wx_group wg
	INNER JOIN (
		SELECT
			bizid,linkid
		FROM
			wx_chat_user_item
		WHERE
			uid = #para(uid)
		AND chatmode = #para(chatmode)
		AND linkflag = #para(linkflag)
	) AS groupindex ON groupindex.bizid = wg.id
	INNER JOIN wx_group_meta wgm ON wg.id = wgm.groupid
	INNER JOIN wx_group_user gu on gu.id = groupindex.linkid
	where
		wg.status = #para(status)
	#if(nick != null && nick != "")
		  and	wg.`name` like #para(nick)
	#end
	order by wg.id desc
#end

-- 群信息
#sql("groupInfo")
	SELECT
		g.*, 
		m.id metaid,
		m.joinnum,
		m.allactflag,
		m.forbiddenflag,
		m.allstartflag
	FROM
		wx_group g
	INNER JOIN wx_group_meta m ON m.groupid = g.id
	WHERE
		g.id = #para(groupid)
#end

-- 群信息
#sql("signInfo")
SELECT
    a.*,
    b.nick,
    b.avatar
FROM
    wx_group_sign a,
    user b
WHERE
    1=1
    and a.groupid = #para(groupid)
    and a.uid = b.id
    #if(uid != null && uid != "")
        and a.uid = #para(uid)
	#end
    #if(year != null && year != "")
        and year(a.signtime) = #para(year)
	#end
    #if(month != null && month != "")
        and month(a.signtime) = #para(month)
    #end
    #if(day != null && day != "")
        and day(a.signtime) = #para(day)
	#end
order by
    signtime desc
#end

-- 群信息
#sql("signRecords")
select
    a.grouprole as grouprole,
    b.id as uid,
    b.avatar as avatar,
    b.nick as nick,
    count(c.id) as signCount
from
    wx_group_user a,
    user b,
    wx_group_sign c
WHERE
    a.groupid = #para(groupid)
    and a.uid = b.id
    and a.groupid = c.groupid
    and a.uid = c.uid
    and year(c.signtime) = #para(year)
    and month(c.signtime) = #para(month)
group by
    c.uid
#end

-- 群用户列表-已调整
#sql("groupUserlist")
	SELECT
		wgu.id,
		wgu.uid,
		wgu.groupid,
		wgu.grouprole,
		wgu.srcnick,
		wgu.groupnick nick,
		wgu.forbiddenflag,
		wgu.forbiddenduration,
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
	#if(searchkey != null && searchkey != "")
		where  
				(
					wgu.autoflag = #para(yes)
					AND wgu.groupnick LIKE #para(searchkey)
				)
				OR (
					wgu.autoflag = #para(no)
					AND (
						wgu.srcnick LIKE #para(searchkey)
						OR wgu.groupnick LIKE #para(searchkey)
					)
				)	
	#end
	ORDER BY
		FIELD(wgu.grouprole,1,3,2),
		wgu.id
	#if(limitnum != null && limitnum != "")
		LIMIT 0,#para(limitnum)
	#end
#end

-- 群用户列表-已调整
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
	#if(searchkey != null && searchkey != "")
		and (
				(
					wgu.autoflag = #para(yes)
					AND wgu.groupnick LIKE #para(searchkey)
				)
				OR (
					wgu.autoflag = #para(no)
					AND (
						wgu.srcnick LIKE #para(searchkey)
						OR wgu.groupnick LIKE #para(searchkey)
					)
				)	
			)
	#end
	ORDER BY
		wgu.forbiddenflag,
		wgu.id
	#if(limitnum != null && limitnum != "")
		LIMIT 0,#para(limitnum)
	#end
#end

-- 群用户列表
#sql("atGroupUserlist")
	SELECT
		wgu.id,
		wgu.uid,
		wgu.groupid,
		wgu.grouprole,
		wgu.srcnick,
		wgu.groupnick nick,
		wf.remarkname,
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
	left join wx_friend wf on wf.uid = #para(uid) and wf.frienduid = wgu.uid
	#if(searchkey != null && searchkey != "")
		where  
				(
					wgu.autoflag = #para(yes)
					AND wgu.groupnick LIKE #para(searchkey)
				)
				OR (
					wgu.autoflag = #para(no)
					AND (
						wgu.srcnick LIKE #para(searchkey)
						OR wgu.groupnick LIKE #para(searchkey)
					)
				)
				OR (
					remarkname is not null and remarkname like #para(searchkey)
				)
	#end
	ORDER BY
		wgu.grouprole,
		wgu.id
	#if(limitnum != null && limitnum != "")
		LIMIT 0,#para(limitnum)
	#end
	
#end
-----------------------------------------------调整-结束----------------------------------------------------


-- 群用户列表
#sql("groupUserToPush")
	SELECT
		wgu.uid,
		jpush.regid
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
	INNER JOIN wx_jpush_user jpush on jpush.uid = wgu.uid
#end




-- 修改用户统计表
#sql("updateMeta")
	update wx_group_meta
		set 
		#if(transfercount != null && transfercount != "")
			transfercount = transfercount + #para(transfercount),
		#end
		#if(kickcount != null && kickcount != "")
			kickcount = kickcount + #para(kickcount),
		#end
		#if(leavecount != null && leavecount != "")
			leavecount = leavecount + #para(leavecount),
		#end
		#if(joinnum != null && joinnum != "")
			joinnum = joinnum + #para(joinnum),
		#end
		#if(allactflag != null && allactflag != "")
			allactflag = #para(allactflag),
		#end
		#if(allstartflag != null && allstartflag != "")
			allstartflag = #para(allstartflag),
		#end
		#if(forbiddenflag != null && forbiddenflag != "")
			forbiddenflag = #para(forbiddenflag),
		#end
		lastmsgid = null
		where groupid = #para(groupid)
	
#end

-- 同步自动用户的群用户的头像和昵称
#sql("synAutoGroupUserByUserUpdate")
	UPDATE wx_group_user
		SET 
			#if(setnick != null && setnick != "")
				srcnick = #para(nick),
				groupnick = #para(nick)
			#end
			#if(setavatar != null && setavatar != "")
				groupavator = #para(avatar)	
			#end
		WHERE
			uid = #para(uid)
#end

-- 群申请记录信息
#sql("applyinfo")
	SELECT
		wga.*, IFNULL(wgu.groupnick,u.nick) groupnick,
		u.nick srcnick,
		IFNULL(wgu.groupavator,u.avatar ) groupavator,
		wgu.grouprole
	FROM
		wx_group_apply wga
	INNER JOIN `user` u ON u.id = wga.operuid
	left JOIN wx_group_user wgu ON wga.groupid = wgu.groupid
	AND wga.operuid = wgu.uid
	WHERE
		wga.id = #para(aid)
#end

-- 群申请记录明细
#sql("applylist")
	SELECT
		wgai.*, u.nick,
		u.avatar,
		u.phone,
		u.email
	FROM
		wx_group_apply_items wgai
	INNER JOIN `user` u ON u.id = wgai.uid
	WHERE
		aid = #para(aid)
	ORDER BY
		wgai.id
#end

#sql("delGroupBak")
	INSERT INTO wx_group_bak SELECT
		*, now() baktime
	FROM
		wx_group
	WHERE
		id = #para(id) and `status` = 1
#end

#sql("delGroupMetaBak")
	INSERT INTO wx_group_meta_bak SELECT
		*, now() baktime
	FROM
		wx_group_meta
	WHERE
		groupid = #para(id) ; 
#end

#sql("delGroupUserBak")
	INSERT INTO wx_group_user_bak SELECT
		*, now() baktime
	FROM
		wx_group_user
	WHERE
		id = #para(id) and `status` = 1;
#end