package com.t4app.t4syncwave.viewmodel;

import android.content.Context;
import android.util.Log;

import com.t4app.t4syncwave.conection.ApiConfig;
import com.t4app.t4syncwave.events.PlaybackEvent;
import com.t4app.t4syncwave.events.PlaybackEventListener;
import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;

import org.json.JSONObject;

public class PlaybackManager implements SfuWebSocketClient.Callback{
    private static final String TAG = "PLAYBACK_MANAGER";

    private SfuWebSocketClient sfuWebSocketClient;
    private SfuWebSocketManager socketManager;
    private PlaybackEventListener listener;
    private Room room;

    public void setListener(PlaybackEventListener listener) {
        this.listener = listener;
    }

    public PlaybackManager(Context context) {
        sfuWebSocketClient = new SfuWebSocketClient(this);
        socketManager = new SfuWebSocketManager(sfuWebSocketClient);

        room = new Room();

    }

    public void connectRoom(Room roomState){
        if (sfuWebSocketClient != null){
            if (!sfuWebSocketClient.isConnected()){
                room.setRoomName(roomState.getRoomName());
                room.setUserName(roomState.getUserName());
                sfuWebSocketClient.connect(ApiConfig.WEB_SOCKET_URL);
            }
        }
    }

    public void sendPlaybackEvent(PlaybackEvent event){
        if (listener != null){
            listener.onCallEvent(event);
        }
    }

    public void handleWebSocketMessage(String text) {
        try {
            JSONObject json = new JSONObject(text);
            String type = json.optString("type");
            String roomName = json.optString("room", null);

            if (roomName != null && !roomName.equals(room.getRoomName())) {
                return;
            }

            switch (type) {
                case "joined":
                    handleUserJoined(json);
                    break;
                case "role":
                    handleUserRole(json);
                    break;
                case "playback-state":
                    handleChangeState(json);
                    break;
                case "left":
//                    handleUserLeave(json);
                    break;
                case "answer":
//                    JSONObject sdpObj = json.getJSONObject("sdp");
//                    handleAnswerFromServer(sdpObj);
                    break;
                case "ice-candidate":
//                    handleRemoteIceCandidate(json);
                    break;
                case "offer":
//                    JSONObject offerSdpObj = json.getJSONObject("sdp");
//                    handleOfferFromRemote(offerSdpObj);
                    break;

                default:
                    Log.d(TAG, "Tipo de mensaje no manejado: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando mensaje WS", e);
        }
    }

    public void handleUserJoined(JSONObject json){
        try {
            String remoteRoom = json.optString("room");
            String remoteUsername = json.optString("userName");
            if (remoteRoom.equalsIgnoreCase(room.getRoomName()) &&
                    !remoteUsername.equalsIgnoreCase(room.getUserName())){

                sendPlaybackEvent(new PlaybackEvent.RemoteParticipantEvent.UserJoined(remoteUsername, remoteRoom));
            }
        }catch (Exception e){
            Log.e(TAG, "handleUserJoined: ", e);
        }
    }

    public void handleUserRole(JSONObject json){
        try {
            String role = json.optString("role");
            if (role.equalsIgnoreCase("host")){
                sendPlaybackEvent(PlaybackEvent.IAmHost.INSTANCE);
            }
        }catch (Exception e){
            Log.e(TAG, "handleUserJoined: ", e);
        }
    }

    public void handleChangeState(JSONObject json) {
        try {
            String type = json.optString("type");
            String roomName = json.optString("room");
            String userName = json.optString("userName");

            Double position = json.has("position") && !json.isNull("position")
                    ? json.optDouble("position")
                    : null;

            boolean isPlaying = json.optBoolean("isPlaying", false);
            int timestamp = json.optInt("timestamp", 0);

            PlaybackState state =
                    new PlaybackState.Builder(type, roomName, userName, timestamp)
                            .setPosition(position)
                            .setPlaying(isPlaying)
                            .build();

            if (!state.getRoom().equalsIgnoreCase(room.getRoomName())) {
                return;
            }

            if (state.getUserName().equalsIgnoreCase(room.getUserName())) {
                return;
            }
            Log.d(TAG, "IN HANDLE CHANGE STATE: ");
            sendPlaybackEvent(new PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState(state));

        } catch (Exception e) {
            Log.e(TAG, "CHANGE STATE ERROR ", e);
        }
    }

    public void sendChangeState(PlaybackState state){
        socketManager.sendPlaybackState(state);
    }


    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected IN ROOM");
        socketManager.sendJoin(room);

        sendPlaybackEvent(new PlaybackEvent.Connected(room));
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMessage(String text) {
        Log.d(TAG, "MESSAGE RECEIVED: " + text);
        handleWebSocketMessage(text);
    }

    @Override
    public void onFailure(Throwable t) {

    }
}
