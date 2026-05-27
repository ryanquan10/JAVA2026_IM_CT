-- 聊天服务专用
-- 聊天列表-已调整
#sql("list")
	SELECT
		IF (items.chatmode = 2 ,- items.bizid, items.id) id,
		IF (items.chatmode = 2 ,- items.bizid, items.id) chatlinkid,
		 items.uid,
		 items.chatmode,
		 items.bizid,
		 linkid,
		 bizrole,
         items.avatar,
		 items.linkflag,
         items.`name`,
         items. `status`,
         items.joinnum,
		 toreadflag,
		 readflag,
		 viewflag,
		 notreadcount,
		 focusflag,
		 notreadstartmsgid,
		 atreadflag,
		 atnotreadcount,
		 atnotreadstartmsgid,
		 sysmsgkey,
		 msgfreeflag,
		 opernick,
		 tonicks,
		 topflag,
		 lastmsgid,
		 lastmsguid,
		 fromnick,
		 msgresume,
		 msgtype,
		 sysflag,
		 chatuptime,
		 sendtime,
		 i.ip,
		 i.city
	FROM
		wx_chat_items items
		INNER JOIN wx_chat_items_meta meta on items.id = meta.chatlinkid
	    LEFT JOIN user u ON items.uid = u.id
	    LEFT JOIN ip_info i ON u.ipid = i.id
	WHERE
		items.id IN (
			SELECT
				chatlinkid
			FROM
				wx_chat_user_item
			WHERE
				uid = #para(uid)
		)
	ORDER BY
		topflag,
		chatuptime desc
	LIMIT 0,#para(limit)
#end


-- 基础会话-已调整
#sql("baseInfo")
	SELECT
	IF (chatmode = 2 ,- bizid, items.id) chatlinkid,
	 items.*
	FROM
		wx_chat_items items
	WHERE
		items.id = #para(id)
#end

-- 基础会话-已调整
#sql("delGroupMsg")
delete from group_msg where uid = #para(uid)
#end

-- 会话详情-已调整
#sql("info")
	SELECT
		IF (items.chatmode = 2 ,- items.bizid, items.id) id,
		IF (items.chatmode = 2 ,- items.bizid, items.id) chatlinkid,
		meta.id chatlinkmetaid,
		 items.uid,
		 items.chatmode,
		 items.bizid,
		 linkid,
		 fidkey,
		 bizrole,
		 avatar,
		 items.linkflag,
		 `name`,
		 `status`,
		 msgfreeflag,
		 joinnum,
		 focusflag,
		 toreadflag,
		 readflag,
		 viewflag,
		 notreadcount,
		 notreadstartmsgid,
		 atreadflag,
		 atnotreadcount,
		 atnotreadstartmsgid,
		 sysmsgkey,
		 opernick,
		 tonicks,
		 topflag,
		 lastmsgid,
		 lastmsguid,
		 fromnick,
		 msgresume,
		 msgtype,
		 sysflag,
		 chatuptime,
		 sendtime,
		 items.createtime,
		 items.updatetime
	FROM
		wx_chat_items items
		INNER JOIN wx_chat_items_meta meta on items.id = meta.chatlinkid
	WHERE
		items.id = #para(id)
#end


-- 群更新会话显示-已调整
#sql("updateChatGroupview")
	UPDATE wx_chat_items_meta items
	INNER JOIN (
		SELECT
			chatlinkmetaid
		FROM
			wx_chat_group_item
		WHERE
			groupid = #para(groupid)
			and chatlinkid is not null
	) indextable ON items.id = indextable.chatlinkmetaid
	SET items.viewflag = #para(viewflag)
#end

-- 获取会话的隐藏列表-已调整
#sql("getHideGroupItems")
	select * from wx_chat_items_meta items
	INNER JOIN (
		SELECT
			chatlinkmetaid
		FROM
			wx_chat_group_item
		WHERE
			groupid = #para(groupid)
			and chatlinkid is not null
	) indextable ON items.id = indextable.chatlinkmetaid
	where items.viewflag = #para(viewflag)
#end
-----------------------------------------------调整-结束----------------------------------------------------

-- 初始化黑名单信息-已调整
#sql("blackInit")
	INSERT IGNORE INTO wx_user_black_items (
		uid,
		touid
	)
	VALUES
		(
			#para(uid),
			#para(touid)
		);
#end

-- 清空会话的消息记录-已调整
#sql("clearChatItemMsg")
	update wx_chat_items_meta
	set 
		lastmsgid = null,
		lastmsguid = null,
		msgresume = null,
		sendtime = null,
		msgcount = 0,
		sysmsgkey = '',
		chatuptime = now(),
		fromnick = null
	where id = #para(id) 
#end

-- 获取黑名单信息-已调整
#sql("blockitems")
	select * from wx_user_black_items where uid = #para(uid) and touid = #para(touid) and status = #para(status)
#end


#sql("userBlock")
select * from wx_user_black_items where uid = #para(uid) and status = #para(status)
#end


-- 群更新人数-已调整
#sql("chatItemUpdateJoinNum")
	INSERT INTO wx_chat_items (
		id,
		uid,
		chatmode,
		bizid,
		linkid,
		bizrole,
		linkflag
	)
	SELECT
		chatlinkid id,
		uid,
		2 chatmode,
		groupid bizid,
		gpulinkid linkid,
		grouprole bizrole,
		linkflag
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		and linkflag = #para(linkflag)
		and chatlinkid is not null
	ON DUPLICATE KEY 
	UPDATE 
	 	joinnum = joinnum + #para(joinnum)
#end

-- 群更新头像和名称-已调整
#sql("chatItemUpdateInfo")
	INSERT INTO wx_chat_items (
		id,
		uid,
		chatmode,
		bizid,
		linkid,
		bizrole,
		linkflag
	)
	SELECT
		chatlinkid id,
		uid,
		2 chatmode,
		groupid bizid,
		gpulinkid linkid,
		grouprole bizrole,
		linkflag
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		and linkflag = #para(linkflag)
		and chatlinkid is not null
	ON DUPLICATE KEY
	UPDATE 
		#if(name != null && name != "")
			name = #para(name)
		#end
	 	#if(avatar != null && avatar != "")
			avatar = #para(avatar)
		#end
#end

-- 群更新链接-已调整
#sql("chatItemUpdateActLinK")
	INSERT INTO wx_chat_items (
		id,
		uid,
		chatmode,
		bizid,
		linkid,
		bizrole,
		linkflag
	)
	SELECT
		chatlinkid id,
		uid,
		2 chatmode,
		groupid bizid,
		gpulinkid linkid,
		grouprole bizrole,
		linkflag
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		and chatlinkid is not null
	ON DUPLICATE KEY
	UPDATE 
	 	linkflag = #para(linkflag)
#end

-- 群更新链接-已调整
#sql("updateItemStatus")
	INSERT INTO wx_chat_items (
		id,
		uid,
		chatmode,
		bizid,
		linkid,
		bizrole,
		status
	)
	SELECT
		chatlinkid id,
		uid,
		2 chatmode,
		groupid bizid,
		gpulinkid linkid,
		grouprole bizrole,
		status
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		and linkflag = #para(linkflag)
		and chatlinkid is not null
	ON DUPLICATE KEY
	UPDATE 
	 	status = #para(status)
#end


#sql("hidechatjob")
	SELECT
		items.id
	FROM
		wx_chat_items items
		INNER JOIN wx_chat_items_meta meta on items.id = meta.chatlinkid
	WHERE
		items.id IN (
			SELECT
				chatlinkid
			FROM
				wx_chat_user_item
			WHERE
				uid = #para(uid)
		)
	ORDER BY
		topflag,
		chatuptime desc
	LIMIT #para(num),#para(limit)
#end


-- 
#sql("delChatBak")
	INSERT INTO wx_chat_del_item(uid,chatlinkid,chatmode,bizid) SELECT
		uid,id,chatmode,bizid
	FROM
		wx_chat_items
	WHERE
		id = #para(id)
#end


#sql("hidechatuid")
	select uid,count(1) num from wx_chat_user_item GROUP by uid HAVING count(1) > #(limit);
#end
