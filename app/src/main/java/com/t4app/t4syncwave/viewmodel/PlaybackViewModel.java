package com.t4app.t4syncwave.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.t4app.t4syncwave.PermissionUtil;
import com.t4app.t4syncwave.PlaybackManager;
import com.t4app.t4syncwave.events.PlaybackEvent;
import com.t4app.t4syncwave.events.PlaybackEventListener;
import com.t4app.t4syncwave.events.PlaybackViewEvent;
import com.t4app.t4syncwave.events.SingleLiveEvent;
import com.t4app.t4syncwave.model.Room;

public class PlaybackViewModel extends ViewModel implements PlaybackEventListener {
    private final SingleLiveEvent<PlaybackEvent> _event = new SingleLiveEvent<>();
    public LiveData<PlaybackEvent> events = _event;
    private PlaybackManager playbackManager;
    private PermissionUtil permissionUtil;

    private Room room;

    public PlaybackViewModel(PlaybackManager playbackManager, PermissionUtil permissionUtil) {
        this.playbackManager = playbackManager;
        this.permissionUtil = permissionUtil;
        this.playbackManager.setListener(this);
    }

    public void sendEvent(PlaybackEvent event){
        _event.postValue(event);
    }


    @Override
    public void onCallEvent(PlaybackEvent videoCallEvent) {
        sendEvent(videoCallEvent);
    }


    public void processInput(PlaybackViewEvent event){
        if (event instanceof PlaybackViewEvent.Connect){
            PlaybackViewEvent.Connect connect = (PlaybackViewEvent.Connect) event;

            room = new Room();

            room.setRoomName(connect.getRoom());
            room.setUserName(connect.getUsername());

            playbackManager.connectRoom(room);
        } else if (event instanceof PlaybackViewEvent.Disconnect) {

        }  else if (event instanceof PlaybackViewEvent.AudioAdded) {

            //TODO: NOT NOW
//            playbackManager.

        } else if (event instanceof PlaybackViewEvent.ChangeState) {
            PlaybackViewEvent.ChangeState changeState = (PlaybackViewEvent.ChangeState) event;
            playbackManager.sendChangeState(changeState.getState());
        }
    }

}
