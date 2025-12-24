package com.t4app.t4syncwave.model;

public final class PlaybackState {

    private final String type;
    private final String room;
    private final String userName;
    private final Double position;
    private final boolean isPlaying;
    private final double timestamp;
    private final String trackUrl;

    private PlaybackState(Builder builder) {
        this.type = builder.type;
        this.room = builder.room;
        this.userName = builder.userName;
        this.position = builder.position;
        this.isPlaying = builder.isPlaying;
        this.timestamp = builder.timestamp;
        this.trackUrl = builder.trackUrl;
    }

    public static class Builder {

        private String type;
        private String room;
        private String userName;
        private Double position;
        private boolean isPlaying;
        private double timestamp;
        private String trackUrl;

        public Builder(String type, String room, String userName, double timestamp, double position) {
            this.type = type;
            this.room = room;
            this.userName = userName;
            this.timestamp = timestamp;
            this.position = position;
            this.isPlaying = false;
            this.trackUrl = null;
        }

        public Builder setType(String val) {
            this.type = val;
            return this;
        }

        public Builder setRoom(String val) {
            this.room = val;
            return this;
        }

        public Builder setUserName(String val) {
            this.userName = val;
            return this;
        }

        public Builder setPosition(Double val) {
            this.position = val;
            return this;
        }

        public Builder setPlaying(boolean val) {
            this.isPlaying = val;
            return this;
        }

        public Builder setTimestamp(double val) {
            this.timestamp = val;
            return this;
        }

        public Builder setTrackUrl(String val) {
            this.trackUrl = val;
            return this;
        }

        public PlaybackState build() {
            return new PlaybackState(this);
        }
    }

    public Builder copy() {
        return new Builder(this.type, this.room, this.userName, this.timestamp, this.position)
                .setPosition(this.position)
                .setPlaying(this.isPlaying)
                .setTimestamp(this.timestamp)
                .setTrackUrl(this.trackUrl);
    }

    public String getType() { return type; }
    public String getRoom() { return room; }
    public String getUserName() { return userName; }
    public Double getPosition() { return position; }
    public boolean isPlaying() { return isPlaying; }
    public double getTimestamp() { return timestamp; }
    public String getTrackUrl() { return trackUrl; }

    @Override
    public String toString() {
        return "PlaybackState{" +
                "type='" + type + '\'' +
                ", room='" + room + '\'' +
                ", userName='" + userName + '\'' +
                ", position=" + (position != null ? position : "null") +
                ", isPlaying=" + isPlaying +
                ", timestamp=" + timestamp +
                ", trackUrl='" + trackUrl + '\'' +
                '}';
    }

}
