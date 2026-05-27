-- 圈子申请列表
#sql("applylist")
    select
        *
    from
        circle_apply
    where
        1=1
        #if(status != null && status != "")
            and `status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (name like #para(searchkey) or uid like #para(searchkey)
		#end
    order by id desc
#end

-- 圈子列表
#sql("list")
    select
        *
    from
        circle
    where
        1=1
        #if(status != null && status != "")
            and `status` = #para(status)
		#end
		#if(searchkey != null && searchkey != "")
			and (name like #para(searchkey) or uid = #para(searchkey) or show_id = #para(searchkey))
		#end
    order by id desc
#end

-- 圈子文章列表
#sql("articlelist")
    select
        u.nick as nick, b.*, a.name as circleName, city.city
    from
        user u,
        circle a,
        circle_article b,
        city
    where
        b.uid = u.id
        and b.city_id = city.id
        and b.circle_id = a.id
		#if(searchkey != null && searchkey != "")
			and (a.name like #para(searchkey) or b.id = #para(searchkey) or b.uid = #para(searchkey))
		#end
    order by b.id desc
#end

#sql("commentlist")
    select
        u.nick as nick, c.*
    from
        user u,
        circle_article_comment c
    where
        c.uid = u.id
        and c.article_id = #para(articleId)
    order by c.id desc
#end

#sql("likelist")
    select
        u.nick as nick, c.*
    from
        user u,
        circle_article_like c
    where
        c.uid = u.id
        and c.article_id = #para(articleId)
    order by c.id desc
#end

#sql("delcirclemember")
    delete from circle_member where circle_id = #para(circleid)
#end

#sql("delcirclememberapply")
    delete from circle_member_apply where circle_id = #para(circleid)
#end

#sql("delarticle")
    delete from circle_article where circle_id = #para(circleid)
#end

#sql("delcomplaint")
delete from circle_complaint where circle_id = #para(circleid)
#end

#sql("dellog")
delete from circle_log where circle_id = #para(circleid)
#end

#sql("delcirclememberinvitecode")
    delete from circle_member_invite_code where circle_id = #para(circleid)
#end

#sql("delcirclesetting")
delete from circle_uid_circleid_setting where circle_id = #para(circleid)
#end

#sql("delcomment")
delete from circle_article_comment where article_id = #para(articleid)
#end

#sql("dellike")
delete from circle_article_like where article_id = #para(articleid)
#end

#sql("delarticlecomplaint")
delete from circle_article_complaint where article_id = #para(articleid)
#end