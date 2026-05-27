-- 订单列表
#sql("orderlist")
	SELECT
		*
	FROM
		tio_order
	where
		1 = 1
		#if(bizname != null && bizname != "")
			and bizname like #para(bizname) 
		#end
		#if(cmpname != null && cmpname != "")
			and cmpname like #para(cmpname) 
		#end
		#if(productname != null && productname != "")
			and productname like #para(productname) 
		#end
		#if(status != null && status != "")
			and `status` = #para(status) 
		#end
		#if(starttime != null && starttime != "")
			and ordertime >= #para(starttime) 
		#end
		#if(endtime != null && endtime != "")
			and ordertime <= #para(endtime) 
		#end
	ORDER BY
		id desc
#end


-- 订单关联用户列表
#sql("orderUserlist")
	SELECT
		u.loginname,
		u.id uid,
		u.nick,
		u.avatar,
		tou.type,
		tou.`status`,
		tou.id,
		tou.oid,
		tou.createtime,
		ii.city,
		ii.province
	FROM
		tio_order_user tou
	INNER JOIN `user` u ON tou.uid = u.id
	LEFT JOIN ip_info ii ON ii.id = u.ipid
	AND u.`status` = #para(status) 
	WHERE
		tou.oid = #para(oid) 
	order by tou.id desc
#end

-- 订单售后列表
#sql("saleslist")
	SELECT
		*
	FROM
		tio_order_after_sales
	WHERE
		oid = #para(oid) and `status` = #para(status) 
	ORDER BY
		id DESC
#end