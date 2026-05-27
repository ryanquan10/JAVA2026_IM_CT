-- 列表
#sql("list")
	SELECT
		*
	FROM
		wx_user_withhold_item
	WHERE
		`status` = #para(status)
	AND uid = #para(uid)
	order by id desc
#end
