-- 列表
#sql("list")
	SELECT
		*
	FROM
		wx_user_recharge_item
	WHERE
		`status` = #para(status)
	AND uid = #para(uid)
	order by id desc
#end
