-- 好友申请记录专用
-- 获取申请列表
#sql("applylist")
	SELECT
		ai.id,
		fromuid uid,
		greet,
		ai.`status`,
		replytime,
		autoflag,
		nick,
		avatar
	FROM
		wx_friend_apply_items ai
	INNER JOIN `user` u ON u.id = ai.fromuid
	WHERE
		ai.touid = #para(uid)
	ORDER BY
		ai.id DESC
#end

-- 获取申请信息
#sql("applyInfo")
	SELECT
		ai.id,
		ai.fromuid,
		ai.touid,
		ai.greet,
		ai.replytime,
		fromuser.nick,
		fromuser.avatar,
		i.province,
		i.city,
		ub.sign,
		ub.sex,
		i.country
	FROM
		wx_friend_apply_items ai
	INNER JOIN `user` fromuser ON fromuser.id = ai.fromuid
	INNER JOIN user_base ub ON fromuser.id = ub.uid
	INNER JOIN ip_info i ON i.id = fromuser.id
	WHERE 
		ai.id = #para(applyid)
#end
-----------------------------------------------调整-结束----------------------------------------------------
-- 初始化申请记录
#sql("applyinit")
	INSERT IGNORE INTO wx_friend_apply_items (
		fromuid,
		touid,
		greet,
		#if(autoflag != null && autoflag != "")
			autoflag,
		#end
		#if(replytime != null && replytime != "")
			replytime,
		#end
		#if(ipid != null && ipid != "")
			ipid,
		#end
		status
	)
	VALUES
		(
			#para(fromuid),
			#para(touid),
			#para(greet),
			#if(autoflag != null && autoflag != "")
				#para(autoflag),
			#end
			#if(replytime != null && replytime != "")
				#para(replytime),
			#end
			#if(ipid != null && ipid != "")
				#para(ipid),
			#end
			#para(status)
		);
#end

-- 获取好友申请记录
#sql("findApply")
	SELECT id,status FROM wx_friend_apply_items where fromuid = #para(fromuid) and touid = #para(touid)
#end

