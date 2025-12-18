package com.t4app.t4syncwave.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GroupItem implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("created_by_name")
    private String createdByName;

    @SerializedName("created_by_avatar_url")
    private String createdByAvatarUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByAvatarUrl() {
        return createdByAvatarUrl;
    }

    public void setCreatedByAvatarUrl(String createdByAvatarUrl) {
        this.createdByAvatarUrl = createdByAvatarUrl;
    }
}
