package com.founder.ark.ids.util;

import com.founder.ark.ids.bean.ConstantsLibrary;

/**
 * @author huyh (mailto:huyh@founder.com).
 */
public enum PollResult {
    USERNAME(ConstantsLibrary.StatusCode.USERNAME_OCCUPIED, "用户名被占用。"), EMAIL(ConstantsLibrary.StatusCode.EMAIL_OCCUPIED, "邮箱被占用。"), VACANT(0, ""),
    GROUPNAME(ConstantsLibrary.StatusCode.GROUPNAME_OCCUPIED, ConstantsLibrary.Message.GROUPNAME_OCCUPIED),
    MOBILE(ConstantsLibrary.StatusCode.MOBILE_OCCUPIED, "该手机号码已存在。"),
    CLIENTNAME(ConstantsLibrary.StatusCode.CLIENT_NAME_OCCUPIED, ConstantsLibrary.Message.CLIENT_NAME_OCCUPIED);
    private int status = -1;
    private String message;
    private boolean occupied;

    PollResult(int status, String message) {
        this.message = message;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public PollResult setOccupied(boolean occupied) {
        this.occupied = occupied;
        return this;
    }

    @Override
    public String toString() {
        return occupied ? message : "vacant";
    }
}
