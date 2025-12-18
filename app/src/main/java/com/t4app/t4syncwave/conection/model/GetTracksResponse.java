package com.t4app.t4syncwave.conection.model;

import com.google.gson.annotations.SerializedName;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.List;

public class GetTracksResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("error")
    private String error;

    @SerializedName("audio")
    private List<MusicItem> audio;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<MusicItem> getAudio() {
        return audio;
    }

    public void setAudio(List<MusicItem> audio) {
        this.audio = audio;
    }
}
