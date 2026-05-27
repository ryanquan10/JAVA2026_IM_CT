-- 列表
#sql("list")
	SELECT
		*
	FROM
		wx_user_withhold_item_local
	WHERE
		uid = #para(uid)
	order by id desc
#end
