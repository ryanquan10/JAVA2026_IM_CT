-- 列表
#sql("list")
SELECT
    *
FROM
    wx_user_recharge_item_local
WHERE
        uid = #para(uid)
order by id desc
    #end