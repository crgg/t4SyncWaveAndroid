package com.t4app.t4syncwave.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SfuWebSocketClient extends WebSocketListener {

    private static final String TAG = "WEB_SOCKET_CLIENT";
    private static SfuWebSocketClient instance;

    private WebSocket webSocket;
    private final OkHttpClient client;
    public boolean isConnected = false;

    private Callback callback;

    public interface Callback {
        void onConnected();
        void onDisconnected();
        void onMessage(String text);
        void onFailure(Throwable t);
    }

    public SfuWebSocketClient() {
        client = new OkHttpClient.Builder()
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }


    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void connect(String url) {
        if (webSocket != null) return;

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    public void send(String msg) {
        if (webSocket != null && isConnected) {
            Log.d(TAG, "send: MESSAGE " + msg);
            webSocket.send(msg);
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing");
            webSocket = null;
            isConnected = false;
        }
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        isConnected = true;
        if (callback != null) callback.onConnected();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        if (callback != null) callback.onMessage(text);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        isConnected = false;
        this.webSocket = null;
        if (callback != null) callback.onDisconnected();
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        isConnected = false;
        this.webSocket = null;
        if (callback != null) callback.onFailure(t);
    }
}

