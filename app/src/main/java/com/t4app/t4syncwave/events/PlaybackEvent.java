package com.t4app.t4syncwave.events;

import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;

public class PlaybackEvent {

    public static final class IAmHost extends PlaybackEvent{
        public static final IAmHost INSTANCE = new IAmHost();
        private IAmHost() {}
    }

    public static final class Disconnected extends PlaybackEvent{
        public static final Disconnected INSTANCE = new Disconnected();
        private Disconnected() {}
    }

    public static abstract class RemoteParticipantEvent extends PlaybackEvent{
        private RemoteParticipantEvent() {}
    }

    public static final class UserJoined extends RemoteParticipantEvent {
        private final String name;
        private final String room;

        public UserJoined(String name, String room) {
            this.name = name;
            this.room = room;
        }

        public String getName() {
            return name;
        }

        public String getRoom() {
            return room;
        }
    }

    public static final class RemoteUserLeave extends RemoteParticipantEvent {
        private final String name;
        private final String room;

        public RemoteUserLeave(String name, String room) {
            this.name = name;
            this.room = room;
        }

        public String getName() {
            return name;
        }

        public String getRoom() {
            return room;
        }
    }

    public static final class ChangeRemoteState extends RemoteParticipantEvent{
        private final PlaybackState state;
        public ChangeRemoteState(PlaybackState state) {
            this.state = state;
        }

        public PlaybackState getState() {
            return state;
        }
    }


    public static class Connected extends PlaybackEvent{
        private final Room room;

        public Connected(Room room) {
            this.room = room;
        }

        public Room getRoom() {
            return room;
        }
    }


    public static class UrlChanged extends PlaybackEvent{
        private final String url;

        public UrlChanged(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }



}
