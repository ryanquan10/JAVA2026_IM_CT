### 钱包脚本
### 提现
#namespace("withhold5u")
	#include("withhold5u.sql")
#end

### 充值
#namespace("recharge5u")
	#include("recharge5u.sql")
#end

### 红包
#namespace("redpacket5u")
	#include("redpacket5u.sql")
#end

### 钱包
#namespace("wallet5u")
	#include("wallet5u.sql")
#end

### 本地钱包
#namespace("localwallet")
	#include("localwallet.sql")
#end

### 本地钱包提现
#namespace("localwalletwithhold")
	#include("localwalletwithhold.sql")
#end

### 本地钱包的红包
#namespace("redpacketLocal")
	#include("redpacketLocal.sql")
#end

### 本地钱包功能
#namespace("walletLocal")
	#include("walletLocal.sql")
#end