-- 发红包列表
#sql("sendlist")
	SELECT
		id,
		uid,
		chatmode,
		chatbizid,
		reqid,
		agrno,
		merorderid,
		cny,
		`mode`,
		`status`,
		num,
		acceptnum,
		bless,
		paytype,
		starttime,
		backtime,
		endtime,
		bizcreattime
	FROM
		wx_wallet_send_red_packet
	WHERE
		`status` IN (#(statuses))
		AND uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND starttime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND starttime <= #para(endtime)
		#end
	order by id desc
#end

-- 发红包信息
#sql("redinfo")
	SELECT
		red.chatmode,
		red.uid,
		red.id,
		red.reqid,
		red.remark,
		red.num,
		red.starttime,
		red.endtime,
		red.backtime,
		red.acceptnum,
		red.bless,
		red.chatbizid,
		red.cny,
		u.nick,
		u.avatar,
		red.`status`,
		red.`mode`
	FROM
		wx_wallet_send_red_packet red
	INNER JOIN `user` u ON u.id = red.uid
	WHERE
		red.id = #para(rid)
#end

-- 红包信息-抢红包列表
#sql("redinfoGrablist")
	SELECT
		grab.uid,
		grab.rid,
		grab.id,
		grab.cny,
		grab.grabtime,
		grab.walletid,
		u.nick,
		u.avatar
	FROM
		wx_wallet_grab_red_item grab
	INNER JOIN `user` u ON u.id = grab.uid
	WHERE
		grab.rid = #para(rid)
		#if(uid != null && uid != "")
			AND and grab.uid = #para(uid)
		#end
	ORDER BY
		grab.id DESC
#end

-- 发红包统计
#sql("sendstat")
	SELECT
		sum(cny) cny,count(1) num
	FROM
		wx_wallet_send_red_packet
	WHERE
		`status` IN (#(statuses))
		AND uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND starttime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND starttime <= #para(endtime)
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
		wx_wallet_grab_red_item grab
	INNER JOIN wx_wallet_send_red_packet send ON grab.rid = send.id
	INNER JOIN `user` senduser ON senduser.id = send.uid
	WHERE
			grab.`status` = #para(status)
			AND grab.uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND grab.grabtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.grabtime <= #para(endtime)
		#end
	order by grab.id desc
#end


-- 抢红包统计
#sql("grabstat")
	SELECT
		sum(grab.cny) cny,count(1) num
	FROM
		wx_wallet_grab_red_item grab
	WHERE
			grab.`status` = #para(status)
			AND grab.uid = #para(uid)
		#if(starttime != null && starttime != "")
			AND grab.grabtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.grabtime <= #para(endtime)
		#end
#end


-- 发红包列表
#sql("sendlistLocal")
SELECT
    id,
    uid,
    chatmode,
    chatbizid,
    reqid,
    agrno,
    merorderid,
    cny,
    `mode`,
    `status`,
    num,
    acceptnum,
    bless,
    paytype,
    starttime,
    backtime,
    endtime,
    bizcreattime
FROM
    wx_wallet_send_red_packet_local
WHERE
        `status` IN (#(statuses))
  AND uid = #para(uid)
        #if(starttime != null && starttime != "")
			AND starttime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND starttime <= #para(endtime)
		#end
order by id desc
#end

-- 发红包信息
#sql("redinfoLocal")
SELECT
    red.chatmode,
    red.uid,
    red.id,
    red.reqid,
    red.remark,
    red.num,
    red.starttime,
    red.endtime,
    red.backtime,
    red.acceptnum,
    red.bless,
    red.chatbizid,
    red.cny,
    u.nick,
    u.avatar,
    red.`status`,
    red.`mode`
FROM
    wx_wallet_send_red_packet_local red
        INNER JOIN `user` u ON u.id = red.uid
WHERE
        red.id = #para(rid)
#end

-- 红包信息-抢红包列表
#sql("redinfoGrablistLocal")
SELECT
    grab.uid,
    grab.rid,
    grab.id,
    grab.cny,
    grab.grabtime,
    grab.walletid,
    u.nick,
    u.avatar
FROM
    wx_wallet_grab_red_item_local grab
        INNER JOIN `user` u ON u.id = grab.uid
WHERE
        grab.rid = #para(rid)
        #if(uid != null && uid != "")
			AND and grab.uid = #para(uid)
		#end
ORDER BY
    grab.id DESC
#end

-- 发红包统计
#sql("sendstatLocal")
SELECT
    sum(cny) cny,count(1) num
FROM
    wx_wallet_send_red_packet_local
WHERE
        `status` IN (#(statuses))
AND uid = #para(uid)
        #if(starttime != null && starttime != "")
			AND starttime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND starttime <= #para(endtime)
		#end
#end

-- 抢红包列表
#sql("grablistLocal")
SELECT
    grab.*, send.`mode`,
    send.`status` sendredstatus,
    senduser.nick,
    senduser.avatar
FROM
    wx_wallet_grab_red_item_local grab
        INNER JOIN wx_wallet_send_red_packet_local send ON grab.rid = send.id
        INNER JOIN `user` senduser ON senduser.id = send.uid
WHERE
        grab.`status` = #para(status)
  AND grab.uid = #para(uid)
        #if(starttime != null && starttime != "")
			AND grab.grabtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.grabtime <= #para(endtime)
		#end
order by grab.id desc
    #end


-- 抢红包统计
#sql("grabstatLocal")
SELECT
    sum(grab.cny) cny,count(1) num
FROM
    wx_wallet_grab_red_item_local grab
WHERE
        grab.`status` = #para(status)
  AND grab.uid = #para(uid)
         #if(starttime != null && starttime != "")
			AND grab.grabtime >= #para(starttime)
		#end
		#if(endtime != null && endtime != "")
			AND grab.grabtime <= #para(endtime)
		#end
#end