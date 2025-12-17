package com.t4app.t4syncwave.conection.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.model.RoomResponse;

import java.io.Serializable;
import java.util.List;

public class ResponseGetGroups implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("groups")
    private List<RoomResponse> groups;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<RoomResponse> getGroups() {
        return groups;
    }

    public void setGroups(List<RoomResponse> groups) {
        this.groups = groups;
    }
}
