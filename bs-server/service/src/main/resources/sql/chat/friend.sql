--新版好友数据库脚本
-- 查询我的好友
#sql("myFriends")
	SELECT
		wf.id,
		wf.remarkname,
		u.nick,
		u.avatar,
		wf.chatindex,
		u.status,
		u.id uid
	FROM
		wx_friend wf
	INNER JOIN (
		SELECT
			linkid
		FROM
			wx_chat_user_item
		WHERE
			uid = #para(uid)
		AND chatmode = #para(chatmode)
	) AS userindex ON userindex.linkid = wf.id
	INNER JOIN `user` u ON u.id = wf.frienduid 
###and u.status = #para(userstatus)
	
	#if(nick != null && nick != "")
		WHERE
			wf.remarkname like #para(nick) or u.nick like #para(nick) or u.beautiful_id like #para(nick)
	#end
		ORDER BY
		 wf.chatindex,wf.id
#end


-- 查询群的其他可以邀请成员
#sql("getOutGroupFd")
	SELECT
		wf.id,
		wf.remarkname,
		u.nick,
		u.avatar,
		wf.chatindex,
		u.id uid
	FROM
		wx_friend wf
	INNER JOIN (
		SELECT
			linkid
		FROM
			wx_chat_user_item
		WHERE
			uid = #para(uid)
			AND linkflag = #para(linkflag)
		AND chatmode = #para(chatmode)
	) AS userindex ON userindex.linkid = wf.id
	INNER JOIN `user` u ON u.id = wf.frienduid and u.status = #para(userstatus)
	where 
		not EXISTS (SELECT
				uid
			FROM
				wx_chat_group_item
			WHERE
				groupid = #para(groupid) and uid = wf.frienduid
				AND linkflag = #para(linkflag)
			)
		#if(nick != null && nick != "")
			and (wf.remarkname like #para(nick) or u.nick like #para(nick))
		#end
		ORDER BY
			 wf.chatindex,wf.id
#end

#sql("labelList")
select
    c.id as uid,
    c.nick,
    b.chatindex,
    b.remarkname,
    c.avatar,
    c.status
from
    label a,
    wx_friend b,
    user c
where
    a.uid = #para(uid)
    and a.id = #para(id)
    and b.labelids like #para(labelid)
    AND b.uid = a.uid
    and b.frienduid = c.id
    #if(searchkey != null && searchkey != "")
        and (b.remarkname like #para(searchkey) or c.nick like #para(searchkey) or b.frienduid = #para(fuid) or c.id = #para(fuid))
    #end
#end

#sql("searchGroup")
    select
        b.id,
        b.name,
        b.avatar
    from
        wx_group_user a,
        wx_group b
    where
        a.uid = #para(uid)
    and a.groupid = b.id
    #if(searchkey != null && searchkey != "")
        and (b.name like #para(searchkey)
        or b.id = #para(groupid))
    #end
#end


#sql("searchGroupFdList")
    select
        c.id as uid,
        c.nick,
        b.chatindex,
        b.remarkname,
        c.avatar,
        c.status
    from
        wx_group_user a,
        wx_friend b,
        user c
    where
        a.groupid = #para(groupid)
        and a.uid = b.frienduid
        and b.uid = #para(uid)
        and c.id != #para(uid)
        and b.frienduid = c.id
        #if(labelid != null && labelid != "")
            and (b.labelids is null or b.labelids not like #para(labelid))
        #end
#end

-----------------------------------------------调整-结束----------------------------------------------------



