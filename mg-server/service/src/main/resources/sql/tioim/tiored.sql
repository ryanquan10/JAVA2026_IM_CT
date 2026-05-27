-- 开户信息
#sql("openlist")
	SELECT
		w.id,
		w.uid,
		w.walletid,
		w.`status`,
		w.ip,
		w.device,
		w.appversion,
		u.nick,
		#if(email != null && email != "")
			u.phone, 
			winfo.mobile,
			winfo.cardno,
		#end
		#if(noemail != null && noemail != "")
			'******' phone, 
			'******' mobile,
			'******' cardno,
		#end
		u.avatar,
		winfo.`name`,
		winfo.`status`,
		wc.cny,
		w.createtime
	FROM
		wx_wallet w
	INNER JOIN `user` u ON u.id = w.uid
	INNER JOIN wx_wallet_info winfo ON winfo.uid = u.id
	INNER JOIN wx_wallet_coin wc ON wc.wid = w.id
	where
		1=1
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid) 
				or u.id = #para(searchid))
		#end
		#if(walletid != null && walletid != "")
			and w.walletid = #para(walletid) 
		#end
	ORDER BY
		w.id DESC
#end

-- 本地钱包开户信息
#sql("openlistlocal")
SELECT
    w.id,
    w.uid,
    w.walletid,
    w.`status`,
    w.ip,
    w.device,
    w.appversion,
    u.nick,
    #if(email != null && email != "")
    u.phone,
        winfo.mobile,
    winfo.cardno,
    #end
             #if(noemail != null && noemail != "")
			'******' phone,
        '******' mobile,
    '******' cardno,
    #end
    u.avatar,
        winfo.`name`,
    winfo.`status`,
    uc.cny,
    w.createtime
FROM
    wx_wallet_local w
        INNER JOIN `user` u ON u.id = w.uid
        INNER JOIN wx_wallet_info_local winfo ON winfo.uid = u.id
        INNER JOIN wx_wallet_coin_local wc ON wc.wid = w.id
        INNER JOIN wx_user_coin_local uc ON wc.uid = uc.uid
where
        1=1
    #if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid)
				or u.id = #para(searchid))
		#end
		#if(walletid != null && walletid != "")
			and w.walletid = #para(walletid)
		#end
ORDER BY
    w.id DESC
    #end
			
-- 红包列表
#sql("redlist")
	SELECT
		send.id,
		send.reqid,
		send.merorderid,
		u.nick,
		u.avatar,
		send.uid,
		send.chatmode,
		touser.avatar touseravatar,
		touser.nick tousernick,
		touser.id touid,
		g.avatar groupavatar,
		g.`name` groupname,
		g.id groupid,
		#if(email != null && email != "")
			u.phone, 
		#end
		#if(noemail != null && noemail != "")
			'******' phone, 
		#end
		send.cny,
		send.`mode`,
		send.`status`,
		send.num,
		send.paytype,
		send.starttime,
		send.endtime
	FROM
		wx_wallet_send_red_packet send
	INNER JOIN `user` u ON u.id = send.uid
	LEFT JOIN `user` touser ON send.chatmode = 1
	AND send.chatbizid = touser.id
	LEFT JOIN wx_group g ON send.chatmode = 2
	AND send.chatbizid = g.id
	WHERE
		send.`status` IN (1, 5, 6, 7, 8)
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid) 
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (send.reqid = #para(orderno)  or send.merorderid = #para(orderno))
		#end
	order by send.id desc
#end

-- 抢红包列表
#sql("grabredlist")
SELECT
    send.id,
    send.reqid,
    send.merorderid,
    u.nick,
    u.avatar,
    send.uid,
    send.chatmode,
    touser.avatar touseravatar,
    touser.nick tousernick,
    touser.id touid,
    g.avatar groupavatar,
    g.`name` groupname,
    g.id groupid,
    #if(email != null && email != "")
    u.phone,
        #end
            #if(noemail != null && noemail != "")
			'******' phone,
        #end
    send.cny,
        send.`status`
FROM
    wx_wallet_send_red_packet_local send
        INNER JOIN `user` u ON u.id = send.uid
        LEFT JOIN `user` touser ON send.chatmode = 1
        AND send.chatbizid = touser.id
        LEFT JOIN wx_group g ON send.chatmode = 2
        AND send.chatbizid = g.id
#end

-- 本地钱包红包列表
#sql("redlistlocal")
SELECT
    send.id,
    send.reqid,
    send.merorderid,
    u.nick,
    u.avatar,
    send.uid,
    send.chatmode,
    touser.avatar touseravatar,
    touser.nick tousernick,
    touser.id touid,
    g.avatar groupavatar,
    g.`name` groupname,
    g.id groupid,
    #if(email != null && email != "")
    u.phone,
        #end
            #if(noemail != null && noemail != "")
			'******' phone,
        #end
    send.cny,
        send.`mode`,
    send.`status`,
    send.num,
    send.paytype,
    send.starttime,
    send.endtime
FROM
    wx_wallet_send_red_packet_local send
        INNER JOIN `user` u ON u.id = send.uid
        LEFT JOIN `user` touser ON send.chatmode = 1
        AND send.chatbizid = touser.id
        LEFT JOIN wx_group g ON send.chatmode = 2
        AND send.chatbizid = g.id
WHERE
        send.`status` IN (1, 5, 6, 7, 8)
    #if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid)
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (send.reqid = #para(orderno)  or send.merorderid = #para(orderno))
		#end
order by send.id desc
    #end

-- 提现列表
#sql("withholdlist")
	SELECT
		ww.id,
		ww.amount,
		ww.arrivalamount,
		ww.bizfee,
		ww.merfee,
		ww.reqid,
		ww.merorderid,
		u.nick,
		u.avatar,
		#if(email != null && email != "")
			u.phone, 
			wc.cardno,
			wc.agrno,
		#end
		#if(noemail != null && noemail != "")
			'******' phone,
			'******' cardno,
			'******' agrno,
		#end
		ww.uid,
		wc.bankcode,
		wi.bankname,
		ww.`status`,
		ww.resptime,
		ww.callbacktime
	FROM
		wx_wallet_withhold_items ww
	INNER JOIN wx_wallet_bank_cards wc ON ww.agrno = wc.agrno
	INNER JOIN wx_wallet_bank_info wi ON wi.bankcode = wc.bankcode
	INNER JOIN `user` u ON u.id = ww.uid
	WHERE
		1 = 1
		#if(status != null && status != "")
			and ww.`status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid) 
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (ww.reqid = #para(orderno)  or ww.merorderid = #para(orderno))
		#end
	ORDER BY
		ww.id DESC							
#end


-- 本地钱包提现列表
#sql("withholdlistlocal")
SELECT
    ww.id,
    ww.amount,
    ww.arrivalamount,
    ww.bizfee,
    ww.merfee,
    ww.reqid,
    ww.merorderid,
    u.nick,
    u.avatar,
    #if(email != null && email != "")
    u.phone,
--         wc.cardno,
--     wc.agrno,
    #end
             #if(noemail != null && noemail != "")
			'******' phone,
        '******' cardno,
    '******' agrno,
    #end
    ww.uid,
--         wc.bankcode,
--     wi.bankname,
    ww.`status`,
    ww.resptime,
    ww.callbacktime
FROM
    wx_wallet_withhold_items_local ww
--         INNER JOIN wx_wallet_bank_cards wc ON ww.agrno = wc.agrno
--         INNER JOIN wx_wallet_bank_info wi ON wi.bankcode = wc.bankcode
        INNER JOIN `user` u ON u.id = ww.uid
WHERE
        1 = 1
    #if(status != null && status != "")
			and ww.`status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid)
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (ww.reqid = #para(orderno)  or ww.merorderid = #para(orderno))
		#end
ORDER BY
    ww.id DESC
    #end


-- 充值列表
#sql("rechargelist")
	SELECT
		ww.id,
		ww.amount,
		ww.bizfee,
		ww.merfee,
		ww.reqid,
		ww.merorderid,
		u.nick,
		u.avatar,
		#if(email != null && email != "")
			u.phone, 
			wc.cardno,
			wc.agrno,
		#end
		#if(noemail != null && noemail != "")
			'******' phone,
			'******' cardno,
			'******' agrno,
		#end
		ww.uid,
		wc.bankcode,
		wi.bankname,
		ww.`status`,
		ww.bizcompletetime,
		ww.bizcreattime
	FROM
		wx_wallet_recharge_item ww
	INNER JOIN wx_wallet_bank_cards wc ON ww.agrno = wc.agrno
	INNER JOIN wx_wallet_bank_info wi ON wi.bankcode = wc.bankcode
	INNER JOIN `user` u ON u.id = ww.uid
	WHERE
		1 = 1
		#if(status != null && status != "")
			and ww.`status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid) 
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (ww.reqid = #para(orderno)  or ww.merorderid = #para(orderno))
		#end
	ORDER BY
		ww.id DESC						
#end

-- 本地钱包充值列表
#sql("rechargelistlocal")
SELECT
    ww.id,
    ww.amount,
    ww.bizfee,
    ww.merfee,
    ww.reqid,
    ww.merorderid,
    u.nick,
    u.avatar,
    #if(email != null && email != "")
    u.phone,
--         wc.cardno,
--     wc.agrno,
    #end
             #if(noemail != null && noemail != "")
			'******' phone,
        '******' cardno,
    '******' agrno,
    #end
    ww.uid,
--         wc.bankcode,
--     wi.bankname,
    ww.`status`,
    ww.bizcompletetime,
    ww.bizcreattime
FROM
    wx_wallet_recharge_item_local ww
--         INNER JOIN wx_wallet_bank_cards wc ON ww.agrno = wc.agrno
--         INNER JOIN wx_wallet_bank_info wi ON wi.bankcode = wc.bankcode
        INNER JOIN `user` u ON u.id = ww.uid
WHERE
        1 = 1
    #if(status != null && status != "")
			and ww.`status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (u.loginname like #para(searchkey) or u.nick like #para(searchkey)
				 or u.phone = #para(searchid)
				or u.id = #para(searchid))
		#end
		#if(orderno != null && orderno != "")
			and (ww.reqid = #para(orderno)  or ww.merorderid = #para(orderno))
		#end
ORDER BY
    ww.id DESC
    #end


-- 提现列表
#sql("coinlist")
	SELECT
		id,
		reqid,
		merorderid,
		`mode`,
		coinflag,
		`status`,
		cny,
		remark,
		bizcompletetime,
		bizcreattime
	FROM
		wx_wallet_coin_item
	WHERE
		uid = #para(uid)
	ORDER BY
		id DESC					
#end

-- 提现列表
#sql("coinlistlocal")
SELECT
    id,
    reqid,
    merorderid,
    `mode`,
    coinflag,
    `status`,
    cny,
    remark,
    bizcompletetime,
    bizcreattime
FROM
    wx_wallet_coin_item_local
WHERE
        uid = #para(uid)
ORDER BY
    id DESC
#end