#sql("search")
    select
        a.*,
        b.id as applyId,
        b.status as applyStatus
    from
        circle a left join circle_member_apply b on a.id = b.circle_id and b.uid = #para(searchuid)
    where
        1 = 1
      and a.status = #para(status)
      and a.is_open = 1
      #if(searchkey != null && searchkey != "")
       and (a.show_id = #para(id) or a.name like #para(searchkey))
       #end
    order by name
#end

#sql("circlelist")
select
    a.*
from
    circle a,
    circle_member b
where
        a.id = b.circle_id
  and a.status = #para(status)
  and b.uid = #para(uid)
  order by a.name
#end

#sql("circleapplylist")
select * from circle_apply where uid = #para(uid) order by id desc
#end

#sql("recommendList")
select
    a.*,
    b.status as applyStatus
from
    circle a left join circle_member_apply b on a.id = b.circle_id and b.uid = #para(uid)
where
    a.is_recommend = 1
  and a.status = 1
  and a.is_open = 1
order by a.id desc
#end

#sql("addcircleapplylist")
select
    a.status as applyStatus,
    a.refuse_reason,
    b.*
from
    circle_member_apply a left join circle b on a.circle_id = b.id
where
    a.uid = #para(uid)
    and a.status != 1
order by id desc
#end

#sql("delUidCircleIdSetting")
DELETE
FROM
    circle_uid_circleid_setting
WHERE
        uid = #para(uid)
#end


#sql("delUidCityIdSetting")
DELETE
FROM
    circle_uid_cityid_setting
WHERE
        uid = #para(uid)
#end

#sql("readNewArticleMsg")
update circle_msg set read_flag = 1 where to_uid = #para(uid) and content_type = 1 and read_flag = 2
#end

#sql("readNewArticleCommentAndLikeMsg")
update circle_msg set read_flag = 1 where to_uid = #para(uid) and (content_type = 2 or content_type = 3) and read_flag = 2
#end

#sql("clearArticleMsgs")
delete from circle_msg where to_uid = #para(touid) and (`content_type` = 2 or `content_type` = 3)
#end

#sql("updateInviteCodeInvalid")
update circle_member_invite_code set is_invalid = 1 where use_uid = #para(uid) and circle_id = #para(circleId)
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


#sql("delinvitecode")
    delete from circle_member_invite_code where circle_id = #para(articleid) and create_uid = #para(uid) and is_use = 0 and is_invalid = 0
#end