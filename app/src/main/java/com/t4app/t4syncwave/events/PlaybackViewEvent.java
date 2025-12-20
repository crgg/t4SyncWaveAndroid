package com.t4app.t4syncwave.events;

import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;

public class PlaybackViewEvent {

    public static final class Connect extends PlaybackViewEvent {
        private Room room;
        public Connect(Room room){
            this.room = room;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }
    }


    public static final class AudioAdded extends PlaybackViewEvent {
        public AudioAdded(){}
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
