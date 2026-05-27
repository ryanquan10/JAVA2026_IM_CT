-- 发票列表
#sql("invoicelist")
	SELECT
		i.*, u.nick,
		u.avatar
	FROM
		mg_invoice i
	INNER JOIN mg_user u ON u.id = i.mguid
	where
		1=1
		#if(paytype != null && paytype != "")
			and i.`paytype` = #para(paytype) 
		#end
		#if(type != null && type != "")
			and i.type = #para(type) 
		#end
		#if(status != null && status != "")
			and i.`status` = #para(status) 
		#end
		#if(developstatus != null && developstatus != "")
			and i.developstatus = #para(developstatus) 
		#end
		#if(mguid != null && mguid != "")
			and i.mguid = #para(mguid) 
		#end
	ORDER BY
		i.id desc
#end

-- 报销列表
#sql("reimburseList")
	SELECT
		mr.*, mu.nick,
		mu.loginname,
		mu.avatar
	FROM
		mg_invoice_reimbursement mr
	INNER JOIN mg_user mu ON mr.mguid = mu.id
	WHERE
		mr.`status` != #para(status) 
		#if(code != null && code != "")
			AND `code` LIKE #para(code) 
		#end
	
	ORDER BY
		mr.id DESC
#end

-- 报销信息
#sql("reimburseInfo")
	SELECT
		mr.*, mu.nick,
		mu.loginname,
		mu.avatar
	FROM
		mg_invoice_reimbursement mr
	INNER JOIN mg_user mu ON mr.mguid = mu.id
	WHERE
		mr.code = #para(code) 
#end

-- 非报销发票列表
#sql("invoiceOutReimburseList")
	SELECT
		i.*, u.nick,
		u.avatar
	FROM
		mg_invoice i
	INNER JOIN mg_user u ON u.id = i.mguid
	WHERE
		i.rcode = ''
	AND (
		developstatus = #para(developstatus) 
		OR developcode != ''
	)
	#if(mguid != null && mguid != "")
		and i.mguid = #para(mguid) 
	#end
	#if(starttime != null && starttime != "")
		and i.time >= #para(starttime) 
	#end
	#if(endtime != null && endtime != "")
		and i.time <= #para(endtime) 
	#end
	ORDER BY
		i.id DESC
#end

-- 报销发票列表
#sql("invoiceReimburseList")
	SELECT
		i.*, u.nick,
		u.avatar
	FROM
		mg_invoice i
	INNER JOIN mg_user u ON u.id = i.mguid
	WHERE
		i.rcode = #para(code) 
	ORDER BY
		i.id DESC
#end

-- 发票列表
#sql("userinvoicelist")
	SELECT
		i.*, u.nick,
		u.avatar
	FROM
		mg_invoice i
	INNER JOIN mg_user u ON u.id = i.mguid
	where
		i.mguid = #para(mguid) 
		#if(paytype != null && paytype != "")
			and i.`paytype` = #para(paytype) 
		#end
		#if(type != null && type != "")
			and i.type = #para(type) 
		#end
	ORDER BY
		i.id desc
#end

-- 发票金额
#sql("total")
	select sum(amount) from mg_invoice i
	where
		1=1
		#if(paytype != null && paytype != "")
			and i.`paytype` = #para(paytype) 
		#end
		#if(type != null && type != "")
			and i.type = #para(type) 
		#end
		#if(status != null && status != "")
			and i.`status` = #para(status) 
		#end
		#if(developstatus != null && developstatus != "")
			and i.developstatus = #para(developstatus) 
		#end
		#if(mguid != null && mguid != "")
			and i.mguid = #para(mguid) 
		#end
#end

-- 发票金额
#sql("usertotal")
	select sum(amount) from mg_invoice i
	where
		i.mguid = #para(mguid) 
		#if(paytype != null && paytype != "")
			and i.`paytype` = #para(paytype) 
		#end
		#if(type != null && type != "")
			and i.type = #para(type) 
		#end
#end