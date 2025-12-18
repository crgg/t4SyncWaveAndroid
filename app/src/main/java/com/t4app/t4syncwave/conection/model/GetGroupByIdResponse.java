package com.t4app.t4syncwave.conection.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.model.Group;

import java.io.Serializable;

public class GetGroupByIdResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("group")
    private Group group;

    @SerializedName("error")
    private String error;

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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
