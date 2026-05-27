#sql("albumList")
select
    *
from
    album
where
    uid = #para(uid)
    #if(searchkey != null && searchkey != "")
       and name like #para(searchkey)
    #end
order by is_top desc, top_time desc, id
#end

#sql("photoList")
select
    *
from
    album_photo
where
    album_id = #para(albumId)
order by id desc
#end
