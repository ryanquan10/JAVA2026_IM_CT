-- 后台系统脚本
-- 字典子节点查询
#sql("dictQuery")
	SELECT
		d.id,
		d. NAME,
		d. CODE,
		d.pcode,
		d.attribute,
		pd. NAME pname,
		d.orderby,
		d.status
	FROM
		dict d
	LEFT JOIN dict pd ON pd. CODE = d.pcode
	WHERE
		d.pcode = #para(pcode) 
		#if(name != null && name != "")
			and d.name like #para(name) 
		#end
		#if(status != null && status != "")
			and d.status != #para(status) 
		#end
	ORDER BY
		d.orderby,
		d.id
#end

-- 权限列表
#sql("authlist")
	select a.*,CASE when p.`name` is null then '/' else p.`name` end pname from mg_auth a
	left join  mg_auth p on a.pid = p.id
	where 
		#if(type != null && type != "")
			a.type = #para(type) 
		#end
		#if(defaulttype != null && defaulttype != "")
			a.type != #para(defaulttype) 
		#end
		#if(status != null && status != "")
			and a.`status` = #para(status)
		#end
		#if(name != null && name != "")
			and a.`name` like #para(name)
		#end
		
	order by a.deep,a.pid,a.aindex,a.type
#end

-- 操作权限列表
#sql("authOperList")
	select f2.* from mg_auth f2 
	INNER JOIN mg_auth pagema ON pagema.id = f2.pid
	where f2.type = #para(type) and f2.`status` = #para(status)
	#if(key != null && key != "")
			AND pagema.routekey = #para(key)
	#end
	
		ORDER BY f2.deep,f2.aindex
#end

-- 权限子树
#sql("authChildTree")
	WITH RECURSIVE delAuth AS (
		SELECT
			*
		FROM
			mg_auth loc
		WHERE
			loc.id = #para(id)
		UNION ALL
			SELECT
				chi.*
			FROM
				mg_auth chi
			INNER JOIN delAuth c ON chi.pid = c.id
	) SELECT
		id
	FROM
		delAuth
#end


-- 页面操作权限
#sql("pageoperlist")
	SELECT DISTINCT
		ma.id,
		ma.routekey,
		ma.authurl,
		ma.aindex,
		mra.`status`
	FROM
		mg_auth ma
	INNER JOIN mg_auth pagema ON pagema.id = ma.pid
	INNER JOIN mg_role_auth mra ON ma.id = mra.aid
	INNER JOIN mg_user_role mur ON mur.rid = mra.rid
	WHERE
		ma.type = #para(type)
	AND pagema.routekey = #para(routekey)
	AND mur.mguid = #para(mguid)
	AND ma.`status` = #para(status)
	AND mra.`status` = #para(status)
	ORDER BY
		ma.aindex
#end

-- 角色权限列表
#sql("roleAuthlist")
	SELECT
		ma.*, CASE when mra.id is null then -1 else mra.id end selid
	FROM
		mg_auth ma
	LEFT JOIN mg_role_auth mra ON ma.id = mra.aid
	AND mra.rid = #para(rid)
	AND mra.`status` = #para(status)
	where ma.`status` = #para(status)
	ORDER BY
		ma.deep,
		ma.pid,
		ma.aindex,
		ma.type
#end

-- 角色权限列表
#sql("roleAuthInit")
	INSERT IGNORE INTO mg_role_auth (rid, aid, `status`) SELECT
		#(rid) rid,
		id aid,
		#(status) `status`
	FROM
		mg_auth
	WHERE
		id IN #(aids)
#end


-- 角色列表
#sql("rolelist")
	SELECT
		outrole.*, CASE WHEN inrole.usercount is null then 0 else inrole.usercount end usercount
	FROM
		mg_role outrole
	LEFT JOIN (
		SELECT
			mr.id,
			count(mur.id) usercount
		FROM
			mg_role mr
		INNER JOIN mg_user_role mur ON mr.id = mur.rid
		AND mur.`status` = #para(status)
		GROUP BY
			mr.id
	) AS inrole ON inrole.id = outrole.id
	#if(rolestatus != null && rolestatus != "")
			WHERE
				outrole.`status` = #para(rolestatus)
	#end
	
#end

-- 角色的字典列表
#sql("roledict")
	SELECT
		id,name
	FROM
		mg_role 
		WHERE
			`status` = #para(status)
		order by rindex
	
#end

-- 系统参数配置列表
#sql("conflist")
	SELECT
		* 
	FROM
		conf
	where 
		1 = 1
	#if(type != null && type != "")
		and type = #para(type)
	#end
	#if(searchkey != null && searchkey != "")
		and (name like #para(searchkey) or title like #para(searchkey) )
	#end
	order by name
	
#end


-- 用户最近打开的记录
#sql("recentPageList")
	SELECT
		mrp.id,
		myauth. NAME,
		myauth.routekey,
		mrp.updatetime recenttime
	FROM
		mg_recent_path mrp
	INNER JOIN (
		SELECT
			*
		FROM
			mg_auth
		WHERE
			id IN (
				SELECT DISTINCT
					aid
				FROM
					mg_role_auth mra
				INNER JOIN mg_role mr ON mra.rid = mr.id
				AND mr.`status` = #para(status) 
				INNER JOIN mg_user_role mur ON mur.rid = mr.id
				AND mur.mguid = #para(mguid) 
				WHERE
					mra.`status` = #para(status) 
			)
		AND `status` = #para(status) 
		AND type = #para(type) 
	) AS myauth ON myauth.id = mrp.aid
	where mrp.mguid =  #para(mguid) 
	ORDER BY
		mrp.updatetime DESC
#end


-- 用户收藏的记录
#sql("favoritePageList")
	SELECT
		mrp.id,
		myauth. NAME,
		myauth.routekey
	FROM
		mg_favorite_path mrp
	INNER JOIN (
		SELECT
			*
		FROM
			mg_auth
		WHERE
			id IN (
				SELECT DISTINCT
					aid
				FROM
					mg_role_auth mra
				INNER JOIN mg_role mr ON mra.rid = mr.id
				AND mr.`status` = #para(status) 
				INNER JOIN mg_user_role mur ON mur.rid = mr.id
				AND mur.mguid = #para(mguid) 
				WHERE
					mra.`status` = #para(status) 
			)
		AND `status` = #para(status) 
		AND type = #para(type) 
	) AS myauth ON myauth.id = mrp.aid
	where mrp.mguid =  #para(mguid) 
	ORDER BY
		mrp.findex DESC,mrp.id DESC
#end

-- 查询用户组织
#sql("mgnnviteorguser")
    SELECT
        id,
        name,
        invitecode,
        mguid,
        createtime
    FROM
        mg_invite_org
    where
        1 = 1
        #if(searchkey != null && searchkey != "")
            and (name like #para(searchkey)  )
        #end
    ORDER BY
        createtime DESC
#end

-- 查询组织成员
#sql("listinviteuser")
    SELECT
        mio.id,
        mio.name,
        mio.invitecode,
        mio.mguid,
        mu.uid AS uid,
        mu.inviteorgid,
        mu.createtime,
        u.nick AS unick
    FROM
        mg_invite_org mio
    INNER JOIN mg_invite_user mu ON mio.id = mu.inviteorgid
    LEFT JOIN user u ON mu.uid = u.id

    #if(mguid != null)
    where
         mio.mguid = #para(mguid)
    #end
    ORDER BY mio.createtime desc
#end





