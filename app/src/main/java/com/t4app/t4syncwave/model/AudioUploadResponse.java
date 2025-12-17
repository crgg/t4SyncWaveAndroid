package com.t4app.t4syncwave.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AudioUploadResponse implements Serializable {

    @SerializedName("ok")
    private boolean ok;

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("duration")
    private int duration;

    @SerializedName("url")
    private String url;

    public AudioUploadResponse() {
    }

    public boolean isOk() {
        return ok;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public String getUrl() {
        return url;
    }
}

