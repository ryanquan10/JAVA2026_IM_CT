package org.tio.mg.service.vo;

import org.tio.mg.service.model.main.User;

/**
 * addUserVo
 *
 * @author <a href="https://github.com/Zakkoree">Zakkoree</a>
 * @date 2025/7/29
 */
public class AddUserVo extends User {
    private Integer num;

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
