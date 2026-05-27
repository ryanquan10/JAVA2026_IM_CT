-- 列表
#sql("items")
	SELECT
		*
	FROM
		wx_user_coin_item
	WHERE
		uid = #para(uid)
		#if(mode != null && mode != "")
			AND `mode` = #para(mode)
		#end
	order by id desc
#end
