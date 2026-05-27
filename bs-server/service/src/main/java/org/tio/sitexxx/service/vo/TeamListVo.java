package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Date;

public class TeamListVo implements Serializable {
    private static final long serialVersionUID = 4144598704556724322L;

    private Integer uid;
    private String nike;
    private String invitecode;
    private Boolean parentinvitecode;
    private Date createtime;
    private Integer number;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getNike() {
        return nike;
    }

    public void setNike(String nike) {
        this.nike = nike;
    }

    public String getInvitecode() {
        return invitecode;
    }

    public void setInvitecode(String invitecode) {
        this.invitecode = invitecode;
    }

    public Boolean getParentinvitecode() {
        return parentinvitecode;
    }

    public void setParentinvitecode(Boolean parentinvitecode) {
        this.parentinvitecode = parentinvitecode;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
