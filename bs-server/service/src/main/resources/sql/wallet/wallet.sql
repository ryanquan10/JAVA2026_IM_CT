-- 列表
#sql("items")
	SELECT
		*
	FROM
		wx_wallet_coin_item
	WHERE
		uid = #para(uid)
		#if(mode != null && mode != "")
			AND `mode` = #para(mode)
		#end
	order by id desc
#end

-- 银行卡列表
#sql("banklist")
	SELECT
		card.id,
		card.agrno,
		card.cardno,
		card.cardtype,
		card.phone,
		info.backcolor,
		info.banklogo,
		info.bankcode,
		info.bankname,
		info.bankwatermark
	FROM
		wx_wallet_bank_cards card
	LEFT JOIN wx_wallet_bank_info info ON card.bankcode = info.bankcode
	where 
		uid = #para(uid) and card.`status` = #para(status)
	order by id desc
#end