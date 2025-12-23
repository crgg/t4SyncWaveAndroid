package com.t4app.t4syncwave.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.conection.model.User;

import java.io.Serializable;

public class Member implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("role")
    private String role;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("user")
    private User user;

    @SerializedName("group")
    private Group group;

    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
