-- 聊天索引服务专用
------------------------------------------------------ query-begin --------------------------------------------
-- 未激活的有效群索引-已调整
#sql("notActGroupList")
	SELECT
		wcgi.*, wgu.msgfreeflag
	FROM
		wx_chat_group_item wcgi
	INNER JOIN wx_group_user wgu ON wgu.id = wcgi.gpulinkid
	WHERE
		wcgi.groupid = #para(groupid)
		AND wcgi.linkflag = #para(linkflag)
		AND wcgi.chatlinkid is null
#end

-- 群管理员列表
#sql("groupMangerList")
	SELECT
		*
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		AND grouprole = #para(grouprole)
		AND linkflag = #para(linkflag)
#end

-- 激活的有效群索引-已调整
#sql("actGroupList")
	SELECT
		*
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		AND linkflag = #para(linkflag)
		AND chatlinkid is not null
#end

-- 有效群索引-已调整
#sql("groupLinkList")
	SELECT
		*
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
		AND linkflag = #para(linkflag)
#end

--获取激活的用户索引-已调整
#sql("linkActUserList")
	SELECT
		*
	FROM
		wx_chat_user_item
	WHERE
		uid = #para(uid)
		#if(chatmode != null && chatmode != "")
			AND chatmode = #para(chatmode)
		#end
		AND linkflag = #para(linkflag)
		AND chatlinkid is not null
#end

--获取用户索引-已调整
#sql("linkUserList")
	SELECT
		*
	FROM
		wx_chat_user_item
	WHERE
		uid = #para(uid)
		AND linkflag = #para(linkflag)
#end

-- 未存在开始消息的group缓存-已调整
#sql("noStartMsgList")
	SELECT
		groupid,uid
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid)
	AND linkflag = #para(linkflag)
	AND chatlinkid is not null
	AND startmsgid is null
#end

-- 获取用户聊天索引-已调整
#sql("getuserindex")
	SELECT
		*
	FROM
		wx_chat_user_item
	WHERE
		uid = #para(uid) and chatmode = #para(chatmode) and bizid = #para(bizid)
#end

-- 获取群用户聊天索引-已调整
#sql("getgroupindex")
	SELECT
		*
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid) and uid = #para(uid)
#end


-- 获取群第一个用户-已调整
#sql("getFristGroupUser")
	SELECT
		*
	FROM
		wx_chat_group_item
	WHERE
		groupid = #para(groupid) AND linkflag = #para(linkflag) 
	ORDER BY gpulinkid 
	limit 0,1
#end
------------------------------------------------------ query-end --------------------------------------------

-- ----------------------------------------------------update-begin --------------------------------------------
-- 更新聊天索引-已调整
#sql("updateChatUserIndex")
	update wx_chat_user_item 
	set 
		#if(setnull != null && setnull != "")
			chatlinkid = null,
			chatlinkmetaid = null,
			startmsgid = null
		#end
		#if(sethide != null && sethide != "")
			chatlinkid = null,
			chatlinkmetaid = null
		#end
		#if(setfidnull != null && setfidnull != "")
			linkid = null,
			chatlinkid = null,
			chatlinkmetaid = null,
			startmsgid = null,
			linkflag = #para(linkflag)
		#end
		#if(tofidnull != null && tofidnull != "")
			linkflag = #para(linkflag),
			tochatlinkid = null,
			tochatlinkmetaid = null
		#end
	where uid = #para(uid) and chatmode = #para(chatmode) and bizid = #para(bizid) 
#end

-- 激活聊天索引-已调整
#sql("actChatUserIndex")
	update wx_chat_user_item 
	set 
		#if(startmsgid != null && startmsgid != "")
			startmsgid = #para(startmsgid),
		#end
		#if(tochatlinkid != null && tochatlinkid != "")
			tochatlinkid = #para(tochatlinkid),
			tochatlinkmetaid = #para(tochatlinkmetaid),
		#end
		#if(linkflag != null && linkflag != "")
			linkflag = #para(linkflag),
		#end
		-- 以下两个为互斥
		#if(chatlinkid != null && chatlinkid != "")
			chatlinkid = #para(chatlinkid),
			chatlinkmetaid = #para(chatlinkmetaid)
		#end
		#if(setnull != null && setnull != "")
			chatlinkid = null,
			chatlinkmetaid = null,
			startmsgid = null
		#end
	where uid = #para(uid) and chatmode = #para(chatmode) and bizid = #para(bizid) 
#end

-- 修改私聊的对方的聊天会话id-已调整
#sql("updateToChatlinkId")
	update wx_chat_user_item 
	set 
		#if(startmsgid != null && startmsgid != "")
			startmsgid = IFNULL(startmsgid, #para(startmsgid)),
		#end
		#if(setnull != null && setnull != "")
			tochatlinkid = null,
			tochatlinkmetaid = null
		#end
		#if(tochatlinkid != null && tochatlinkid != "")
			tochatlinkid = #para(tochatlinkid),
			tochatlinkmetaid = #para(tochatlinkmetaid)
		#end
		
	where uid = #para(uid) and chatmode = #para(chatmode) and bizid = #para(bizid) 
#end

-- 修改索引的起始消息-已调整
#sql("chatuserStartMsg")
	update wx_chat_user_item 
	set 
		#if(startmsgid != null && startmsgid != "")
			startmsgid =  #para(startmsgid)
		#end
		#if(setnull != null && setnull != "")
			startmsgid = null
		#end
	where uid = #para(uid) and chatmode = #para(chatmode) and bizid = #para(bizid) 
#end

-- 修改索引的起始消息-已调整
#sql("chatgroupStartMsg")
	update wx_chat_group_item 
	set 
		#if(startmsgid != null && startmsgid != "")
			startmsgid =  #para(startmsgid)
		#end
		#if(setnull != null && setnull != "")
			startmsgid = null,
			resetflag = #para(resetflag)
		#end
	where groupid = #para(groupid) and uid = #para(uid)
#end


-- 更新群用户聊天索引-已调整
#sql("updateChatGroupIndex")
	update wx_chat_group_item set
		#if(clearmsg != null && clearmsg != "")
			startmsgid = null,
		#end
		-- 以下两个条件互斥
		#if(setnull != null && setnull != "")
			chatlinkid = null,
			chatlinkmetaid = null,
			resetflag = #para(resetflag)
		#end
		#if(chatlinkid != null && chatlinkid != "")
			chatlinkid = #para(chatlinkid),
			chatlinkmetaid = #para(chatlinkmetaid)
		#end
	where groupid = #para(groupid) and uid = #para(uid)
#end

-- 修改群激活的索引-删除群-已调整
#sql("updateGroupLink")
	update
		wx_chat_group_item
	set 
		linkflag = #para(linkflag)
	WHERE
		groupid = #para(groupid) and chatlinkid is not null
#end

-- 激活群用户聊天索引-已调整
#sql("actGroupIndex")
	update wx_chat_group_item set
		#if(startmsgid != null && startmsgid != "")
			startmsgid = IFNULL(startmsgid, #para(startmsgid)),
		#end
		chatlinkid = #para(chatlinkid),
		chatlinkmetaid = #para(chatlinkmetaid)
	where groupid = #para(groupid) and uid = #para(uid)
#end

-- 激活群用户的用户聊天索引-已调整
#sql("actGroupToUserIndex")
	update wx_chat_user_item set
		#if(startmsgid != null && startmsgid != "")
			startmsgid = IFNULL(startmsgid, #para(startmsgid)),
		#end
		chatlinkid = #para(chatlinkid),
		chatlinkmetaid = #para(chatlinkmetaid)
	where  uid = #para(uid) and bizid = #para(groupid) and chatmode = #para(chatmode)
#end

-- 更新群组的起始消息id-已调整
#sql("chatGroupStartMsgUpdate")
	update wx_chat_group_item set
			startmsgid = #para(startmsgid)
	where  groupid = #para(groupid) 
	AND linkflag = #para(linkflag)
	AND chatlinkid is not null
	and startmsgid is null
#end

-- 焦点处理
#sql("focus")
	update wx_chat_group_item 
	set 
		focusflag = #para(focusflag)
	where groupid = #para(groupid) and uid = #para(uid)
#end
-- ---------------------------------------------------- update-end --------------------------------------------

-- ---------------------------------------------------- del-begin --------------------------------------------
-- 删除群未激活的索引-删除群-已调整
#sql("delGroupNoAct")
	DELETE
	FROM
		wx_chat_group_item
	WHERE
			groupid = #para(groupid)
		AND chatlinkid is null
#end

-- ---------------------------------------------------- update-end --------------------------------------------
