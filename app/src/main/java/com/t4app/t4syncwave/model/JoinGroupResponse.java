package com.t4app.t4syncwave.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class JoinGroupResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("msg")
    private String msg;

    @SerializedName("group")
    private Group group;

    @SerializedName("member")
    private Member member;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
