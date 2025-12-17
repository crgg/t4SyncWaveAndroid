package com.t4app.t4syncwave.events;

import com.t4app.t4syncwave.model.PlaybackState;

public class PlaybackViewEvent {

    public static final class Connect extends PlaybackViewEvent {
        private final String room;
        private final String username;
        public Connect(String room, String username){
            this.room = room;
            this.username = username;
        }

        public String getRoom() {
            return room;
        }

        public String getUsername() {
            return username;
        }
    }


    public static final class AudioChanged extends PlaybackViewEvent {
        private final String audioUrl;

        public AudioChanged(String audioUrl){
            this.audioUrl = audioUrl;
        }

        public String getAudioUrl() {
            return audioUrl;
        }
    }

    public static final class ChangeState extends PlaybackViewEvent{
        private final PlaybackState state;
        public ChangeState(PlaybackState state) {
            this.state = state;
        }

        public PlaybackState getState() {
            return state;
        }
    }

    public static final class Disconnect extends PlaybackViewEvent{
        public static final Disconnect INSTANCE = new Disconnect();
        public Disconnect() {}

    }
}
