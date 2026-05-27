-- 相册
#sql("list")
    select
        u.nick, a.*
    from
        album a left join
        user u on a.uid = u.id
    where
        1 = 1
		#if(searchkey != null && searchkey != "")
			and (a.name like #para(searchkey) or uid = #para(searchkey) or a.id = #para(searchkey))
		#end
    order by a.id desc
#end
