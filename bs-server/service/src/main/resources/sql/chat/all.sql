### 新版本聊天数据库脚本
### 聊天服务
#namespace("chat")
	#include("chat.sql")
#end

#namespace("syn")
	#include("syn.sql")
#end


### 聊天索引
#namespace("chatindex")
	#include("chatindex.sql")
#end

### 聊天消息
#namespace("chatmsg")
	#include("chatmsg.sql")
#end

### 好友申请
#namespace("friendapply")
	#include("friendapply.sql")
#end

### 好友
#namespace("friend")
	#include("friend.sql")
#end

### 群组
#namespace("group")
	#include("group.sql")
#end