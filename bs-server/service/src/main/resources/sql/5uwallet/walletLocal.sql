-- 列表
#sql("items")
SELECT
    *
FROM
    wx_wallet_coin_item_local
WHERE
        uid = #para(uid)
    #if(mode != null && mode != "")
			AND `mode` = #para(mode)
		#end
order by id desc
    #end