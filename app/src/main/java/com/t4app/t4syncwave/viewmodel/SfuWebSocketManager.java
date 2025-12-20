package com.t4app.t4syncwave.viewmodel;

import android.util.Log;

import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;

import org.json.JSONObject;

public class SfuWebSocketManager {
    private static final String TAG = "WEB_SOCKET_MANAGER";

    private final SfuWebSocketClient sfuClient;

    public SfuWebSocketManager(SfuWebSocketClient sfuClient) {
        this.sfuClient = sfuClient;
    }

    public void sendJoin(Room room) {
        try {
            if (!canSend()) return;

            JSONObject joinJson = new JSONObject();
            joinJson.put("type", "join");
            joinJson.put("room", room.getRoomName());
            joinJson.put("userName", room.getUserName());
            joinJson.put("userId", room.getUserId());
            joinJson.put("role", room.getRole());

            Log.d(TAG, "JOIN enviado: " + joinJson);
            sfuClient.send(joinJson.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creando/enviando JOIN", e);
        }
    }

    public void sendLeft(Room room) {
        try {
            if (!canSend()) return;

            JSONObject json = new JSONObject();
            json.put("type", "left");
            json.put("room", room.getRoomName());
            json.put("userName", room.getUserName());

            sfuClient.send(json.toString());
            Log.d(TAG, "LEFT enviado: " + json);
        } catch (Exception e) {
            Log.e(TAG, "Error creando/enviando LEFT", e);
        }
    }

    public void sendPlaybackState(PlaybackState state) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", state.getType());
            json.put("room", state.getRoom());
            json.put("trackUrl", state.getTrackUrl());//TODO
            json.put("userName", state.getUserName());
            json.put("timestamp", state.getTimestamp());

            if (state.getPosition() != null) {
                json.put("position", state.getPosition());
            }

            json.put("isPlaying", state.isPlaying());
            sfuClient.send(json.toString());

        } catch (Exception e) {
            Log.e(TAG, "sendPlaybackState error", e);
        }
    }


    private boolean canSend() {
        return sfuClient != null && sfuClient.isConnected();
    }

}
