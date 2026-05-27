#sql("updateBase")
update user_base
set
    #if(sex != null && sex != "")
            sex = #para(sex),
    #end
            sign = #para(sign)
where
        uid = #para(uid)
#end

#sql("signtasklist")
select
    *
from
    sign_task
where
    1 = 1
    and sign_day = #para{signDay}
order by
    sign_day
#end

#sql("signitemlist")
select
    *
from
    sign_item
where
        1 = 1
  and uid = #para{uid}
order by
    create_time
desc
#end
