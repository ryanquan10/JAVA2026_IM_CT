-- 后台用户列表
-- 权限列表
#sql("mguserauth")
	select * from mg_auth where 
		id in (
			SELECT DISTINCT
				aid
			FROM
				mg_role_auth mra
			INNER JOIN mg_role mr ON mra.rid = mr.id and mr.`status` = #para(status) 
			INNER JOIN mg_user_role mur ON mur.rid = mr.id
			AND mur.mguid = #para(mguid) 
			where mra.`status` = #para(status) 
			)
		AND `status` = #para(status) 
	ORDER BY deep,aindex
#end


-- 用户列表
#sql("mguserlist")
	SELECT
		u.*, mub.realname,mub.deptname,mub.phone,mub.position, roletable.rolename,
		roletable.rids
	FROM
		mg_user u
	INNER JOIN mg_user_base mub ON u.id = mub.mguid
	LEFT JOIN (
		SELECT
			mur.mguid,
			GROUP_CONCAT(
				mr.`name`
				ORDER BY
					rindex SEPARATOR ','
			) rolename,
			GROUP_CONCAT(
				mr.`id`
				ORDER BY
					rindex SEPARATOR ','
			) rids
		FROM
			mg_user_role mur
		INNER JOIN mg_role mr ON mur.rid = mr.id
		GROUP BY
			mur.mguid
	) AS roletable ON roletable.mguid = u.id
	WHERE
		#if(defaultstatus != null && defaultstatus != "")
			 u.`status` != #para(defaultstatus) 
		#end
		#if(status != null && status != "")
			 u.`status` = #para(status) 
		#end
		#if(searchkey != null && searchkey != "")
			 and (
					u.loginname LIKE #para(searchkey) 
					OR u.nick LIKE #para(searchkey) 
				)
		#end
		#if(rid != null && rid != "")
			AND FIND_IN_SET(#(rid) , rids)
		#end
	order by u.id desc
#end


-- 用户列表
#sql("userRoles")
	SELECT
		u.id ,roletable.rolename,roletable.rids
	FROM
		mg_user u
	LEFT JOIN (
		SELECT
			mur.mguid,
			GROUP_CONCAT(
				mr.`name`
				ORDER BY
					rindex SEPARATOR ','
			) rolename,
			GROUP_CONCAT(
				mr.`id`
				ORDER BY
					rindex SEPARATOR ','
			) rids
		FROM
			mg_user_role mur
		INNER JOIN mg_role mr ON mur.rid = mr.id
		GROUP BY
			mur.mguid
	) AS roletable ON roletable.mguid = u.id
	where u.id = #para(mguid) 
#end


-- 默认群组列表
#sql("defaultgrouplist")
SELECT
    wg.id as groupid,
    wg.name,
    wg.avatar,
    wg.status,
    dg.isopen
FROM
    default_group dg left join
    wx_group wg on dg.groupid = wg.id
#end
