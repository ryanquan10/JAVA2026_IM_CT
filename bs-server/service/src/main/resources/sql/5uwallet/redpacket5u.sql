-- 发红包列表
#sql("sendlist")
	SELECT
		*
	FROM
		wx_user_send_red_item
	WHERE
		`status` IN (#(statuses))
		AND uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND bizcreattime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND bizcreattime <= #para(endtime)
		#end
		
	order by id desc
#end

-- 发红包统计
#sql("sendstat")
	SELECT
		sum(amount) amount,count(1) num
	FROM
		wx_user_send_red_item
	WHERE
		`status` IN (#(statuses))
		AND uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND bizcreattime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND bizcreattime <= #para(endtime)
		#end
#end

-- 抢红包列表
#sql("grablist")
	SELECT
		grab.*, send.`mode`,
		send.`status` sendredstatus,
		senduser.nick,
		senduser.avatar
	FROM
		wx_user_grab_red_item grab
	INNER JOIN wx_user_send_red_item send ON grab.sendid = send.id
	INNER JOIN `user` senduser ON senduser.id = send.uid
	WHERE
			grab.`status` = #para(status)
			AND grab.uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND grab.bizcompletetime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.bizcompletetime <= #para(endtime)
		#end
	order by grab.id desc
#end


-- 抢红包统计
#sql("grabstat")
	SELECT
		sum(grab.amount) amount,count(1) num
	FROM
		wx_user_grab_red_item grab
	INNER JOIN wx_user_send_red_item send ON grab.sendid = send.id
	INNER JOIN `user` senduser ON senduser.id = send.uid
	WHERE
			grab.`status` = #para(status)
			AND grab.uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND grab.bizcompletetime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.bizcompletetime <= #para(endtime)
		#end
#end