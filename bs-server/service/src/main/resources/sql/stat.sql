-- 按天统计页面的访问量
---------------------------
-- /case/index.html	2018-04-16	24
-- /case/index.html	2018-04-17	109
-- /doc/index.html	    2018-04-16	18
-- /doc/index.html	    2018-04-17	97
---------------------------
#sql("requestCountByDay")
SELECT
	DATE_FORMAT(firstAccessTime, '%Y-%m-%d') d,
	path,
	sum(requestCount) count
FROM
	tio_ip_path_access_stat
WHERE
	(appType = 9 or appType = 11)
AND path IN (
	'/index.html',
	'/case/index.html',
	'/tx/index.html',
	'/index.html',
	'/blog/r/index.html',
	'/blog/r/blog-view-onlyhtml.html',
	'/stat/index.html'
)
AND DATE_SUB(CURDATE(), INTERVAL ? DAY) <= firstAccessTime
GROUP BY
	d, path  order by d
	
#end


-- 按天统计不同的ip数
---------------------------
--    d         |  ipCount
-- 2018-04-16	|  35
-- 2018-04-17	|  222
---------------------------
#sql("ipCountByDay")
SELECT
	d, count(*) count
FROM
	(
		SELECT
			count(*) counts,
			DATE_FORMAT(firstAccessTime, '%Y-%m-%d') d,
			ip
		FROM
			tio_ip_path_access_stat
		WHERE
			DATE_SUB(CURDATE(), INTERVAL ? DAY) <= firstAccessTime
		GROUP BY
			DATE_FORMAT(firstAccessTime, '%Y-%m-%d'),
			ip
	) AS t1 group by d order by d
#end


-- 按省统计点击量和ip量
---------------------------------------------
--  hitcount | ipcount      |  province
--  6565     |   545	    |  浙江省
--  5443     |   434	    |  湖南省
---------------------------------------------
#sql("statIpAndHitsByProvince")
SELECT
	count(count) ipcount,
	SUM(sum) hitcount,
	province
FROM
	(
		SELECT
			count(*) count,
			SUM(s.requestCount) sum,
			s.ipid,
			i.ip,
			i.country,
			i.area,
			i.province,
			i.city,
			i.operator
		FROM
			tio_ip_path_access_stat s
		LEFT JOIN tio_site_main.ip_info i ON i.id = s.ipid
		WHERE
			DATE_SUB(CURDATE(), INTERVAL ? DAY) <= firstAccessTime
		AND i.country = '中国'
		AND i.province IS NOT NULL
		GROUP BY
			ipid
	) AS t1
GROUP BY
	province
ORDER BY
	ipcount DESC
#end


#sql("ip")
SELECT
	#if(mergeRequest == null || !mergeRequest)
	tis.appType,
	#end
	sum(tis.decodeErrorCount) decodeErrorCount,
	sum(tis.sentBytes) sentBytes,
	sum(tis.sentPackets) sentPackets,
	sum(tis.requestCount) requestCount,
	sum(tis.activatedCount) activatedCount,
	sum(tis.handledBytes) handledBytes,
	sum(tis.handledPackets) handledPackets,
	sum(tis.receivedBytes) receivedBytes,
	sum(tis.receivedTcps) receivedTcps,
	sum(tis.receivedPackets) receivedPackets,
	ii.ip,
	ii.province,
	ii.city,
	ii.operator
FROM
	`tio_ip_stat` tis
LEFT JOIN tio_site_main.ip_info ii ON ii.id = tis.ipid
WHERE
	tis. START > #para(starttime)
AND tis. START < #para(endtime)
GROUP BY

	#if(mergeRequest == null || !mergeRequest)
	tis.appType, 
	#end
	tis.ipid
	
ORDER BY
	requestCount DESC
#end




