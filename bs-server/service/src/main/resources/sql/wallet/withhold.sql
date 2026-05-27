-- 列表
#sql("list")
	SELECT
		withhold.id,
		withhold.uid,
		withhold.walletid,
		withhold.merorderid,
		withhold.reqid,
		withhold.agrno,
		withhold.amount,
		withhold.arrivalamount,
		withhold.`status`,
		withhold.bizfee,
		bizcompletetime,
		bizcreattime,
		card.bankcode,
		info.bankname,
		card.cardno
	FROM
		wx_wallet_withhold_items withhold
	INNER JOIN wx_wallet_bank_cards card ON card.agrno = withhold.agrno
	LEFT JOIN wx_wallet_bank_info info ON card.bankcode = info.bankcode
	WHERE
			withhold.`status` = #para(status)
		AND withhold.uid = #para(uid)
	order by withhold.id desc
#end


-- 列表
#sql("info")
	SELECT
		withhold.id,
		withhold.amount,
		withhold.arrivalamount,
		withhold.merorderid,
		withhold.reqid,
		withhold.`status`,
		withhold.walletid,
		withhold.uid,
		card.bankcode,
		info.bankname,
		info.banklogo,
		info.backcolor,
		info.bankwatermark,
		card.cardno
	FROM
		wx_wallet_withhold_items withhold
	INNER JOIN wx_wallet_bank_cards card ON card.agrno = withhold.agrno
	LEFT JOIN wx_wallet_bank_info info ON card.bankcode = info.bankcode
	WHERE
		withhold.id = #para(id)
#end