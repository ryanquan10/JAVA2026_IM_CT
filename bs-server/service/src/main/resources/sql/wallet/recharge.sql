-- 列表
#sql("list")
	SELECT
		recharge.id,
		recharge.uid,
		recharge.walletid,
		recharge.merid,
		recharge.reqid,
		recharge.merorderid,
		recharge.agrno,
		recharge.amount,
		recharge.bizcreattime,
		recharge.bizcompletetime,
		card.cardno,
		card.bankcode,
		info.bankname
	FROM
		wx_wallet_recharge_item recharge
	INNER JOIN wx_wallet_bank_cards card ON card.agrno = recharge.agrno
	LEFT JOIN wx_wallet_bank_info info ON info.bankcode = card.bankcode
	where
		recharge.`status` = #para(status)
	AND recharge.uid = #para(uid)
	order by recharge.id desc
#end
