-- 聊天消息服务专用

-- 私聊的聊天列表
#sql("p2pMsgList")
	SELECT
		msg.*, u.nick,
		u.avatar
	FROM
		(
			SELECT
				id mid,
				uid,
				touid,
				sigleuid,
				sigleflag,
				text c,
				IF(contenttype = 4,text,'') ac,
				IF(contenttype = 2,text,'') bc,
				IF(contenttype = 3,text,'') fc,
				IF(contenttype = 6,text,'') ic,
				IF(contenttype = 5,text,'') vc,
				IF(contenttype = 9,text,'') cardc,
				IF(contenttype = 10 or contenttype = 11,text,'') `call`,
				IF(contenttype = 12,text,'') red,
				IF(contenttype = 88,text,'') temp,
				IF(contenttype = 14,text,'') position,
				readflag,
				readtime,
				time t,
				sendbysys,
				msgtype,
				contenttype ct,
                quotemid,
			    quotemsgcontent,
			    quotemsgtype,
			    quotesrcnick
			FROM
				wx_friend_msg
			WHERE
				twouid = #para(fidkey)
				#if(startmsgid != null && startmsgid != "")
					AND id >= #para(startmsgid)
				#end
				#if(limitmsgid != null && limitmsgid != "")
					AND id < #para(limitmsgid)
				#end
				#if(endmid != null && endmid != "")
					AND id > #para(endmid)
				#end
				AND `status` = #para(status)
				AND contenttype != 7 AND contenttype != 15 AND contenttype != 16 AND contenttype != 17
			ORDER BY
				id DESC
			LIMIT 0,
			#para(limit)
		) AS msg
	INNER JOIN `user` u ON msg.uid = u.id
	ORDER BY
		mid DESC
#end

-- 群聊的聊天列表
#sql("groupMsgList")
	SELECT
		wgm.text as c,
		IF(wgm.contenttype = 4,wgm.text,'') ac,
		IF(wgm.contenttype = 2,wgm.text,'') bc,
		IF(wgm.contenttype = 3,wgm.text,'') fc,
		IF(wgm.contenttype = 6,wgm.text,'') ic,
		IF(wgm.contenttype = 5,wgm.text,'') vc,
		IF(wgm.contenttype = 9,wgm.text,'') cardc,
		IF(wgm.contenttype = 12,wgm.text,'') red,
		IF(wgm.contenttype = 88,wgm.text,'') temp,
		IF(wgm.contenttype = 13,wgm.text,'') apply,
		IF(wgm.contenttype = 14,wgm.text,'') position,
		wgm.contenttype as ct,
		wgm.id as mid,
		wgm.time as t,
		wgm.uid as f,
		wgm.groupid as g,
		wgm.sendbysys,
		wgm.device as d,
		wgm.sysmsgkey,
		wgm.opernick,
		wgm.tonicks,
		sigleuid,
		sigleflag,
		nick,
		avatar,
		whereflag,
		whereuid,
	    quotemid,
	    quotemsgcontent,
	    quotemsgtype,
	    quotesrcnick
	FROM
		wx_group_msg wgm
	WHERE
		groupid = #para(groupid)
		AND `status` = #para(status)
		#if(startmsgid != null && startmsgid != "")
			AND id >= #para(startmsgid)
		#end
		#if(limitmsgid != null && limitmsgid != "")
			AND id < #para(limitmsgid)
		#end
		#if(kickmsgid != null && kickmsgid != "")
			AND id <= #para(kickmsgid)
		#end
		#if(endmid != null && endmid != "")
			AND id > #para(endmid)
		#end
		#if(uid != null && uid != "")
			and 
				(sigleuid = -1 or sigleuid = #para(uid))
			and 
				(whereflag = 2 or LOCATE(#para(uidstr), whereuid) = 0)
		#end
	ORDER BY
		id DESC
	LIMIT 0,
	#para(limit)
#end
-----------------------------------------------调整-结束----------------------------------------------------


-- 更新消息发送后的聊天列表内容-已调整
#sql("sendP2PMsg")
	UPDATE wx_chat_items_meta
	SET 
		#if(readflag != null && readflag != "")
			readflag = #para(readflag),
		#end
		#if(readnull != null && readnull != "")
			notreadcount = 0,
	 		notreadstartmsgid = null,
		#end
		#if(toreadflag != null && toreadflag != "")
			toreadflag = #para(toreadflag),
		#end
		#if(notreadcount != null && notreadcount != "")
			notreadcount = notreadcount + #para(notreadcount),
	 		notreadstartmsgid = IFNULL(notreadstartmsgid, #para(notreadstartmsgid)),
		#end
		#if(viewflag != null && viewflag != "")
			viewflag = #para(viewflag),
		#end
	 	lastmsgid = #para(lastmsgid),
	 	lastmsguid = #para(lastmsguid),
	 	fromnick = #para(fromnick),
	 	msgresume = #para(msgresume),
	 	msgtype = #para(msgtype),
	 	sysflag = #para(sysflag),
	 	chatuptime = now(),
	 	sendtime = #para(sendtime)
	WHERE
		id = #para(id)
#end

-- 已读-已调整
#sql("chatRead")
	UPDATE wx_chat_items_meta
	SET 
		#if(atreadflag != null && atreadflag != "")
			atreadflag = #para(atreadflag),
			atnotreadcount = 0,
			atnotreadstartmsgid = null,
		#end
		#if(focusflag != null && focusflag != "") 
			focusflag = #para(focusflag),
		#end
		readflag = #para(readflag),
		notreadcount = 0,
	 	notreadstartmsgid = null
	WHERE
		id = #para(id)
#end

-- 对方已读-已调整
#sql("chatToRead")
	UPDATE wx_chat_items_meta
	SET 
		toreadflag = #para(toreadflag)
	WHERE
		id = #para(id)
#end

-- 私聊消息已读-已调整
#sql("p2pMsgRead")
	UPDATE wx_friend_msg
	SET 
		readflag = #para(readflag),
		readtime = now(),
		readipid = #para(readipid),
		readdevice = #para(readdevice)
	WHERE
		twouid = #para(twouid)
		and touid = #para(touid)
		and readflag = #para(noread)
		and contenttype != 15
		and contenttype != 16
        and contenttype != 17

#end

-- 群聊的聊天列表-已调整
#sql("firstGroupMsg")
	SELECT
		*
	FROM
		wx_group_msg wgm
	WHERE
		groupid = #para(groupid)
		AND `status` = #para(status)
		#if(startmsgid != null && startmsgid != "")
			AND id >= #para(startmsgid)
		#end
		#if(uid != null && uid != "")
			and 
				(sigleuid = -1 or sigleuid = #para(uid))
			and 
				(whereflag = 2 or LOCATE(#para(uidstr), whereuid) = 0)
		#end
	ORDER BY
		id DESC
	LIMIT 0,1
#end


-- 群聊处理-已调整
#sql("sendGroupMsg")
	UPDATE wx_chat_items_meta meta
	SET 
		#if(notreadcount != null && notreadcount != "")
			notreadcount = notreadcount + #para(notreadcount),
	 		notreadstartmsgid = IFNULL(notreadstartmsgid, #para(notreadstartmsgid)),
		#end
		#if(readflag != null && readflag != "")
			readflag = #para(readflag),
		#end
		#if(sysmsgkey != null && sysmsgkey != "")
			sysmsgkey = #para(sysmsgkey),
			opernick = #para(opernick),
			tonicks = #para(tonicks),
		#end
		#if(sysmsgkey == null || sysmsgkey == "")
			sysmsgkey = '',
		#end
		#if(viewflag != null && viewflag != "")
			viewflag = #para(viewflag),
		#end
		lastmsgid = #para(lastmsgid),
	 	lastmsguid = #para(lastmsguid),
	 	msgcount = msgcount + 1,
	 	fromnick = #para(fromnick),
	 	msgresume = #para(msgresume),
	 	msgtype = #para(msgtype),
	 	sysflag = #para(sysflag),
	 	chatuptime = now(),
	 	sendtime = #para(sendtime)
	 where
	 		bizid = #para(groupid)
	 	and chatmode = 2
		and linkflag = #para(linkflag)
		#if(focusflag != null && focusflag != "")
			and focusflag = #para(focusflag)
		#end
#end

-- 群聊at处理-已调整
#sql("sendGroupAtMsg")
	UPDATE wx_chat_items_meta meta
	SET 
		#if(viewflag != null && viewflag != "")
			viewflag = #para(viewflag),
		#end
		atreadflag = #para(atreadflag),
		atnotreadcount = atnotreadcount + #para(atnotreadcount),
 		atnotreadstartmsgid = #para(atnotreadstartmsgid)
 	where
	 		bizid = #para(groupid)
		and linkflag = #para(linkflag)
		and chatmode = 2
		#if(ats != null && ats != "")
			and uid in (#(ats))
		#end
		#if(focusflag != null && focusflag != "")
			and focusflag = #para(focusflag)
		#end
#end

-- 单通道群消息发送-已调整
#sql("sendGroupMsgById")
	UPDATE wx_chat_items_meta wi
	SET 
		#if(notreadcount != null && notreadcount != "")
			notreadcount = notreadcount + #para(notreadcount),
	 		notreadstartmsgid = IFNULL(notreadstartmsgid, #para(notreadstartmsgid)),
		#end
		#if(readflag != null && readflag != "")
			readflag = #para(readflag),
		#end
		#if(sysmsgkey != null && sysmsgkey != "")
			sysmsgkey = #para(sysmsgkey),
			opernick = #para(opernick),
			tonicks = #para(tonicks),
		#end
		#if(sysmsgkey == null || sysmsgkey == "")
			sysmsgkey = '',
		#end
		#if(viewflag != null && viewflag != "")
			viewflag = #para(viewflag),
		#end
		lastmsgid = #para(lastmsgid),
	 	lastmsguid = #para(lastmsguid),
	 	fromnick = #para(fromnick),
	 	msgresume = #para(msgresume),
	 	msgtype = #para(msgtype),
	 	msgcount = msgcount + 1,
	 	sysflag = #para(sysflag),
	 	chatuptime = now(),
	 	sendtime = #para(sendtime)
	 where wi.id = #para(id)
#end

-- 群聊删除处理 - 已调整
#sql("delGroupChatMsg")
	UPDATE wx_chat_items_meta meta
	SET 
		#if(notreadcount != null && notreadcount != "")
			notreadcount = notreadcount + #para(notreadcount),
	 		notreadstartmsgid = IFNULL(notreadstartmsgid, #para(notreadstartmsgid)),
		#end
		#if(readflag != null && readflag != "")
			readflag = #para(readflag),
		#end
		#if(sysmsgkey != null && sysmsgkey != "")
			sysmsgkey = #para(sysmsgkey),
			opernick = #para(opernick),
			tonicks = #para(tonicks),
		#end
		#if(sysmsgkey == null || sysmsgkey == "")
			sysmsgkey = '',
		#end
		#if(viewflag != null && viewflag != "")
			viewflag = #para(viewflag),
		#end
		lastmsgid = #para(lastmsgid),
	 	lastmsguid = #para(lastmsguid),
	 	fromnick = #para(fromnick),
	 	msgcount = msgcount + 1,
	 	chatuptime = now(),
	 	msgresume = #para(msgresume),
	 	msgtype = #para(msgtype),
	 	sysflag = #para(sysflag),
	 	sendtime = #para(sendtime)
	 where 
 			bizid = #para(groupid)
 			and chatmode = 2
			and linkflag = #para(linkflag)
		#if(focusflag != null && focusflag != "")
			and focusflag = #para(focusflag)
		#end
#end

-- 群聊消息删除-已调整
#sql("groupMsgDel")
	UPDATE wx_group_msg
	SET whereflag = #para(yes),whereuid = CONCAT(whereuid, #(uid), ',')
	WHERE
		groupid = #(groupid) 
			and 
		id IN (#(mids));
#end

--群聊重新加入群聊消息处理-已调整
#sql("rebindGroupMsgDeal")
	UPDATE wx_group_msg
	SET whereflag = #para(yes),whereuid = CONCAT(whereuid, #(uid), ',')
	WHERE
		groupid = #(groupid) 
			and 
		id > #para(startMid) 
#end

-- 同步自动用户的群消息头像和昵称
#sql("synAutoGroupMsgByUserUpdate")
	update  
		wx_group_msg
	set 
		#if(setnick != null && setnick != "")
			nick = #para(nick)
		#end
		#if(setavatar != null && setavatar != "")
			avatar = #para(avatar)
		#end
	WHERE
		groupid IN (
			SELECT
				g.id
			FROM
				wx_group g
			INNER JOIN wx_group_user wgu ON g.id = wgu.groupid
			AND wgu.uid = #para(uid)
			#if(yes != null && yes != "")
				AND wgu.autoflag = #para(yes)
			#end
			
		)
	and uid = #para(uid)
#end

-- 收藏列表
#sql("favoriteList")
	SELECT
		*
	FROM
		wx_user_favorites
	WHERE
		uid = #para(uid)
	#if(type != null && type != "")
		AND type = #para(type)
	#end
	ORDER BY
		id DESC
#end


#sql("delGroupMsgBak")
	INSERT INTO wx_group_msg_bak SELECT
		*, now() baktime
	FROM
		wx_group_msg
	WHERE
		id =  #para(id) and `status` = 1;
#end


#sql("delP2pMsgBak")
	INSERT INTO wx_friend_msg_bak SELECT
		*, now() baktime
	FROM
		wx_friend_msg
	WHERE
		id =  #para(id) and `status` = 1;
#end

#sql("clearMomentMsgs")
	delete from wx_friend_msg where touid = #para(touid) and (`contenttype` = 16 or `contenttype` = 17)
#end

#sql("updateMomentMsgs")
    update wx_friend_msg set readflag=1, readtime = #para(nowDate) where touid = #para(touid) and (`contenttype` = 16 or `contenttype` = 17)
#end
