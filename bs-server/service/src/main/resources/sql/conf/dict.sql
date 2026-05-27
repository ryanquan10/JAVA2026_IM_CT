 -- 获取字典name的字符串，每个名字用逗号隔开，形如"游戏,IM,物联网"
 #sql("getNameStrings")
	SELECT
	 pcode,
	 group_concat(NAME) split,
	 GROUP_CONCAT(`code`, '-', `name`) musplit
	FROM
	 `dict`
	WHERE
	 pcode = #(pcode)
	AND find_in_set(code, #(codes))
	GROUP BY
	 pcode
#end