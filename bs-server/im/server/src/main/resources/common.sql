-- 要统计哪一天的
SET @statDay = '2018-01-17';


SET @stat_start = concat(@statDay, ' 00:00:00');
SET @stat_end = concat(@statDay, ' 23:59:59');
-- select @statDay;
-- select @stat_start;
-- select @stat_end;

--  天访问量 start
-- 查看某天的总访问量(含js,css,gif,jpg,html,api等)
SELECT SUM(requestCount) 总访问量 FROM `tio_token_path_access_stat` where path is null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end;
-- 查看某天的页面访问量
SELECT SUM(requestCount) 页面总访问量 FROM `tio_token_path_access_stat` where path is null and appType = 9 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end;
-- 查看某天的API访问量
SELECT SUM(requestCount) API总访问量 FROM `tio_token_path_access_stat` where path is null and appType = 8 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end;
-- 查看某天的首页访问量
SELECT SUM(requestCount) 首页总访问量 FROM `tio_token_path_access_stat` where path = '/index.html' and appType = 9 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end;
-- 查看某天的群组页面的访问量
SELECT SUM(requestCount) 群组页面访问量 FROM `tio_token_path_access_stat` where path = '/live/index.html' and appType = 9 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end;
--  天访问量 end




--  ip访问统计 start
-- 查看某天有多少个不同的ip访问了本系统
SELECT count(*) as ipCount from (SELECT count(*) counts, ip from tio_ip_path_access_stat where path is null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip) as t1; 
-- 查看某天有多少个不同的ip访问了首页
SELECT count(*) as ipCountForIndex from (SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and path = '/index.html' and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip) as t1;
-- 查看某天有多少个不同的ip访问了群组
SELECT count(*) as ipCountForLive from (SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and path = '/live/index.html' and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip) as t1;
-- 查看某天有多少个不同的ip访问了API
SELECT count(*) as ipCountForApi from (SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 8 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip) as t1;
-- 查看某天有多少个不同的ip访问了页面
SELECT count(*) as ipCountForPage from (SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip) as t1;


--  Api访问总次数(按ip分组统计)   ip    counts
SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 8 and path is null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip order by counts desc;
--  页面访问总次数(按ip分组统计)  ip    counts
SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and path is null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip order by counts desc;
--  首页访问总次数(按ip分组统计)   ip    counts
SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and path = '/index.html' and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip order by counts desc;
--  群组页面访问总次数(按ip分组统计)  ip    counts
SELECT count(*) counts, ip from tio_ip_path_access_stat where appType = 9 and path = '/live/index.html' and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip order by counts desc;


--  根据ip和path对访问次数进行分组统计     ip   path   counts
SELECT count(*) counts, ip, path from tio_ip_path_access_stat where path is not null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip, path order by counts desc;
--  根据ip和path对API访问次数进行分组统计   ip   path   counts
SELECT count(*) counts, ip, path from tio_ip_path_access_stat where appType = 8 and path is not null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip, path order by counts desc;
--  根据ip和path对页面访问次数进行分组统计   ip   path   counts
SELECT count(*) counts, ip, path from tio_ip_path_access_stat where appType = 9 and path is not null and firstAccessTime >= @stat_start and firstAccessTime <= @stat_end GROUP BY ip, path order by counts desc;

--  ip访问统计 end



-- 按ip统计点击量
SELECT
	SUM(s.requestCount) count,
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
	DATE_SUB(CURDATE(), INTERVAL 7 DAY) <= date(firstAccessTime)
AND s.path = '/index.html'
AND i.country = '中国'
AND i.province IS NOT NULL
GROUP BY
	ipid
ORDER BY
	count DESC;

-- 


SELECT
	SUM(count) count,
	province
FROM
	(
		SELECT
			SUM(s.requestCount) count,
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
			DATE_SUB(CURDATE(), INTERVAL 7 DAY) <= date(firstAccessTime)
		AND s.path = '/index.html'
		AND i.country = '中国'
		AND i.province IS NOT NULL
		GROUP BY
			ipid
	) AS t1
GROUP BY
	province
ORDER BY
	count DESC
