package org.tio.sitexxx.service.service.atom;

public class ChatAtom {
    private String				c;											//聊天内容	//举例：大家好
    private String				at;											//艾特哪些用户。此值可为null	  //举例：[434343, 9898989]
    //	TODO:lixinji-删除G
    private Long	g;			//groupid	//举例：45454
    private Long	chatlinkid;	// 用户聊天会话
    private Long	cardid;		//名片id
    private Short	cardtype;	//名片类型
    private String mapjson;
    private String quotemsgcontent;
    private String quotemid;
    private String quotemsgtype;
    private String quotesrcnick;

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public Long getG() {
        return g;
    }

    public void setG(Long g) {
        this.g = g;
    }

    public Long getChatlinkid() {
        return chatlinkid;
    }

    public void setChatlinkid(Long chatlinkid) {
        this.chatlinkid = chatlinkid;
    }

    public Long getCardid() {
        return cardid;
    }

    public void setCardid(Long cardid) {
        this.cardid = cardid;
    }

    public Short getCardtype() {
        return cardtype;
    }

    public void setCardtype(Short cardtype) {
        this.cardtype = cardtype;
    }

    public String getMapjson() {
        return mapjson;
    }

    public void setMapjson(String mapjson) {
        this.mapjson = mapjson;
    }

    public String getQuotemsgcontent() {
        return quotemsgcontent;
    }

    public void setQuotemsgcontent(String quotemsgcontent) {
        this.quotemsgcontent = quotemsgcontent;
    }

    public String getQuotemid() {
        return quotemid;
    }

    public void setQuotemid(String quotemid) {
        this.quotemid = quotemid;
    }

    public String getQuotemsgtype() {
        return quotemsgtype;
    }

    public void setQuotemsgtype(String quotemsgtype) {
        this.quotemsgtype = quotemsgtype;
    }

    public String getQuotesrcnick() {
        return quotesrcnick;
    }

    public void setQuotesrcnick(String quotesrcnick) {
        this.quotesrcnick = quotesrcnick;
    }
}
