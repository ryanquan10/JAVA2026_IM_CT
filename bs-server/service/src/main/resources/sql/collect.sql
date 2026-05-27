#sql("collectList")
select * from collect
where uid = #para(uid)
        #if(category != null && category != "")
			and category = #para(category)
		#end
order by id desc
#end