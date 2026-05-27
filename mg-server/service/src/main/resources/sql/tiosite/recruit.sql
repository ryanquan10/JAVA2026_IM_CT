-- 招聘公司列表
#sql("cmplist")
	SELECT
		cmp.*, CASE
	WHEN rec.cmpid IS NULL THEN
		0
	ELSE
		rec.pubcount
	END pubcount,
	 CASE
	WHEN rec.cmpid IS NULL THEN
		0
	ELSE
		rec.subcount
	END subcount
	FROM
		tio_recruit_cmp cmp
	LEFT JOIN (
		SELECT
			cmpid,
			count(1) pubcount,
			sum(submitnum) subcount
		FROM
			tio_recruit
		WHERE
			STATUS = #para(status)
		GROUP BY
			cmpid
	) AS rec ON rec.cmpid = cmp.id
	where
		1 = 1
		#if(cmpid != null && cmpid != "")
			and cmp.id = #para(cmpid) 
		#end
		#if(cmpstatus != null && cmpstatus != "")
			and cmp.`status` = #para(cmpstatus) 
		#end
	ORDER BY
		cmpindex,
		id
#end

-- 招聘信息列表
#sql("recruitlist")
	SELECT
		tr.*, trc.cmpname,
		trc.cmpfullname,
		trc.`status` cmpstatus
	FROM
		tio_recruit tr
	INNER JOIN tio_recruit_cmp trc on tr.cmpid = trc.id
	where 
		1 = 1
		#if(cmpid != null && cmpid != "")
			and tr.cmpid = #para(cmpid) 
		#end
		#if(postname != null && postname != "")
			and tr.postname like  #para(postname) 
		#end
		#if(status != null && status != "")
			and tr.`status` = #para(status) 
		#end
	ORDER BY
		cmpindex,
		rindex,
		id DESC
#end

-- 投递信息列表
#sql("resumelist")
	SELECT
		trs.*, tr.postname,
		tr.posttype,
		trc.cmpfullname,
		trc.id cmpid,
		trc.cmpname,
		tr.postcity,
		tr.salarytype,
		tr.maxsalary,
		tr.minsalary
	FROM
		tio_resume trs
	INNER JOIN tio_recruit tr ON trs.rid = tr.id
	INNER JOIN tio_recruit_cmp trc ON trc.id = tr.cmpid
	WHERE
		1 = 1
		#if(cmpid != null && cmpid != "")
			and trc.id = #para(cmpid) 
		#end
		#if(postname != null && postname != "")
			and tr.postname like  #para(postname) 
		#end
	ORDER BY
		trc.cmpindex,
		tr.rindex,
		trs.id DESC
#end