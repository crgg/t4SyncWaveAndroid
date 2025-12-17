package com.t4app.t4syncwave;

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

            Log.d(TAG, "JOIN enviado: " + joinJson);
            sfuClient.send(joinJson.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creando/enviando JOIN", e);
        }
    }

    public void sendPlaybackState(PlaybackState state) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", state.getType());
            json.put("room", state.getRoom());
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
