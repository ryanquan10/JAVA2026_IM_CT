-- 统计下的好友列表
#sql("realnamelist")
    select
        a.*,
        b.nick,
        b.phone
    from real_name_certification a
        left join user b
            on a.uid = b.id
    where
        1 = 1
        #if(uid != null && uid != "")
            and a.`uid` = #para(uid)
        #end
    ORDER BY
        a.create_time DESC
#end