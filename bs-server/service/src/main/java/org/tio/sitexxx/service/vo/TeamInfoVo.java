package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Date;

public class TeamInfoVo implements Serializable {
    private static final long serialVersionUID = 4144598704556724322L;

    private Integer uid;
    private Integer teamNumber;
    private Double cny;
    private Boolean isOnline;
    private Date createTime;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(Integer teamNumber) {
        this.teamNumber = teamNumber;
    }

    public Double getCny() {
        return cny;
    }

    public void setCny(Double cny) {
        this.cny = cny;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
