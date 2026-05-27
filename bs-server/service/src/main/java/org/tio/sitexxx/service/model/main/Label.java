package org.tio.sitexxx.service.model.main;


import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.model.main.base.BaseLabel;

import java.util.List;

public class Label extends BaseLabel<Label> {
    public static final Label dao = new Label().dao();

    private List<Record> friendList;
    private Integer num;

    public List<Record> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<Record> friendList) {
        this.friendList = friendList;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}

