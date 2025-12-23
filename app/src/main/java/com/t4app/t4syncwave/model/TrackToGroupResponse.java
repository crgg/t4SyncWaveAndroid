package com.t4app.t4syncwave.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TrackToGroupResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("track")
    private MusicItem track;

    @SerializedName("error")
    private String error;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public MusicItem getTrack() {
        return track;
    }

    public void setTrack(MusicItem track) {
        this.track = track;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
