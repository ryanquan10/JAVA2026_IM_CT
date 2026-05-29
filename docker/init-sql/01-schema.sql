#namespace("stat")
	#include("stat.sql")
#end

#namespace("user")
	#include("user.sql")
#end

#namespace("collect")
	#include("collect.sql")
#end

#namespace("sign")
	#include("sign.sql")
#end

#namespace("circle")
	#include("circle.sql")
#end

#namespace("album")
	#include("album.sql")
#end

### 新版本聊天数据库脚本
#include("chat/all.sql")


### 钱包脚本
#include("wallet/all.sql")

### conf脚本
#include("conf/all.sql")

### 5u钱包脚本
#include("5uwallet/all.sql")