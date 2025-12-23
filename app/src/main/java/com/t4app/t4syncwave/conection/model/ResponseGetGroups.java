package com.t4app.t4syncwave.conection.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.model.Group;

import java.io.Serializable;
import java.util.List;

public class ResponseGetGroups implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("groups")
    private List<Group> groups;

    @SerializedName("error")
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
