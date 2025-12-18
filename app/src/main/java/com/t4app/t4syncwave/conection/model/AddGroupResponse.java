package com.t4app.t4syncwave.conection.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.model.Group;
import com.t4app.t4syncwave.model.GroupItem;
import com.t4app.t4syncwave.model.Member;

import java.io.Serializable;
import java.util.List;

public class AddGroupResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("group")
    private GroupItem group;

    @SerializedName("member")
    private List<Member> member;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public GroupItem getGroup() {
        return group;
    }

    public void setGroup(GroupItem group) {
        this.group = group;
    }

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }
}
