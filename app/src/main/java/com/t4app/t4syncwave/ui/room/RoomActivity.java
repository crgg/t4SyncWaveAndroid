package com.t4app.t4syncwave.ui.room;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.FileUtils;
import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.adapter.MusicAdapter;
import com.t4app.t4syncwave.PermissionUtil;
import com.t4app.t4syncwave.ui.AudioPlayerView;
import com.t4app.t4syncwave.viewmodel.PlaybackManager;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.databinding.ActivityMainBinding;
import com.t4app.t4syncwave.events.PlaybackEvent;
import com.t4app.t4syncwave.events.PlaybackViewEvent;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;
import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModel;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModelFactory;

import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    private ActivityMainBinding binding;

    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private PlaybackState state;

    private PlaybackViewModel viewModel;
    private Room room;
    private MusicAdapter adapter;

    private String songListening;
    private boolean iAmHost = false;

    private AudioPlayerView audioPlayerView;

    private ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {

                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
//                        try {
////                            uploadAudio(audioUri);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        audioPlayerView = findViewById(R.id.actions_container);

        room = new Room();

        PlaybackManager playbackManager = new PlaybackManager(getApplicationContext());
        PermissionUtil permissionUtil = new PermissionUtil(this);

        PlaybackViewModelFactory factory = new PlaybackViewModelFactory(playbackManager, permissionUtil);

        viewModel = new ViewModelProvider(this, factory)
                .get(PlaybackViewModel.class);

        adapter = new MusicAdapter(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onPlay(MusicItem item, int position) {
                if (iAmHost){
                    if (item.getFileUrl().equalsIgnoreCase(songListening)){
                        audioPlayerView.startAudioPlayback();
                    }else{
                        audioPlayerView.prepareAudio(item.getFileUrl(), true);
                        state = new PlaybackState.Builder(
                                "playback-state",
                                room.getRoomName(),
                                room.getUserName(),
                                item.getDurationMs())
                                .setPlaying(true)
                                .setPosition((double) position)
                                .build();
                        viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
                    }
                }
            }

            @Override
            public void onPause(MusicItem item) {
                if (iAmHost){
                    audioPlayerView.pauseAudioPlayback();
                }
            }

            @Override
            public void onClick(MusicItem item) {

            }
        });

        binding.musicListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.musicListRv.setAdapter(adapter);

        observeEvents();
        getMusicList();

        binding.selectAudioBtn.setOnClickListener(view -> openAudioPicker());

        binding.disconnectRoom.setOnClickListener(view -> {
            viewModel.processInput(PlaybackViewEvent.Disconnect.INSTANCE);
        });

        binding.back.setOnClickListener(view -> finish());

        if (getIntent() != null){
            String roomName = getIntent().getStringExtra("roomName");
            String username = getIntent().getStringExtra("userName");

            binding.roomName.setText(roomName);
            viewModel.processInput(new PlaybackViewEvent.Connect(room));
        }

        audioPlayerView.setPlaybackActionListener(new ListenersUtils.PlaybackActionListener() {
            @Override
            public void onPlayRequested() {
                if (iAmHost){
                    PlaybackState updated = state.copy()
                            .setPlaying(true)
                            .build();
                    viewModel.processInput(new PlaybackViewEvent.ChangeState(updated));
                }
            }

            @Override
            public void onPauseRequested() {
                PlaybackState updated = state.copy()
                        .setPlaying(false)
                        .build();
                viewModel.processInput(new PlaybackViewEvent.ChangeState(updated));
            }

            @Override
            public void onChangeSeek(int progress) {
                PlaybackState updateState = state.copy()
                        .setTimestamp(progress)
                        .build();
                viewModel.processInput(new PlaybackViewEvent.ChangeState(updateState));
            }
        });


    }

    private void observeEvents(){
        viewModel.events.observe(this, playbackEvent -> {

            Log.d(TAG, "ENTRY IN OBSERVE EVENTS: ");
            if (playbackEvent instanceof PlaybackEvent.RemoteParticipantEvent){

                handleRemoteParticipantEvent((PlaybackEvent.RemoteParticipantEvent) playbackEvent);

            } else if (playbackEvent instanceof PlaybackEvent.Connected) {

                PlaybackEvent.Connected connected = (PlaybackEvent.Connected) playbackEvent;
                room = connected.getRoom();
                binding.disconnectRoom.setVisibility(View.VISIBLE);

            }else if (playbackEvent instanceof PlaybackEvent.UrlChanged) {

                PlaybackEvent.UrlChanged urlChanged = (PlaybackEvent.UrlChanged) playbackEvent;
//                setAudioUrl(urlChanged.getUrl());

            }else if (playbackEvent instanceof PlaybackEvent.Disconnected) {
                finish();
            }else if (playbackEvent instanceof PlaybackEvent.IAmHost) {
                iAmHost = true;
                audioPlayerView.setIamHost(true);
                binding.selectAudioBtn.setVisibility(View.VISIBLE);
                adapter.setClicksEnabled(true);
            }
        });
    }


    private void handleRemoteParticipantEvent(PlaybackEvent.RemoteParticipantEvent event){
        if (event instanceof PlaybackEvent.RemoteParticipantEvent.UserJoined){
            PlaybackEvent.RemoteParticipantEvent.UserJoined userJoined = (PlaybackEvent.RemoteParticipantEvent.UserJoined) event;

            if (iAmHost){
                viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
            }
        }else if (event instanceof PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState){
            PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState remoteState = (PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState) event;
            Log.d(TAG, "REMOTE PARTICIPANT EVENT");

            if (!iAmHost){
                if (state != null){
                    if (state.getTimestamp() != remoteState.getState().getTimestamp()){
                        audioPlayerView.setProgress((int) remoteState.getState().getTimestamp());
                        state = state.copy().
                                setTimestamp((int) remoteState.getState().getTimestamp()).
                                build();
                    }
                }else{
                    state = remoteState.getState();
                }
            }

            int pos = remoteState.getState().getPosition().intValue();
            if (audioPlayerView.getMediaPlayer() != null){
                if (!adapter.getSong(pos).getFileUrl().equalsIgnoreCase(songListening)){
                    audioPlayerView.prepareAudio(adapter.getSong(pos).getFileUrl(), remoteState.getState().isPlaying());
                }
                if (remoteState.getState().isPlaying()){
                    audioPlayerView.startAudioPlayback();
                }else{
                    audioPlayerView.pauseAudioPlayback();
                }

                Log.d(TAG, "SYNC PLAY PAUSE STATE: " + remoteState.getState().isPlaying());
                adapter.setRemotePlaying(pos, remoteState.getState().isPlaying());
            }else{
                audioPlayerView.prepareAudio(adapter.getSong(pos).getFileUrl(), remoteState.getState().isPlaying());
            }

        }
    }

    private void getMusicList(){
        ApiServices apiServices = AppController.getApiServices();
        Call<List<MusicItem>> call = apiServices.getAudioList();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<MusicItem>> call, @NonNull Response<List<MusicItem>> response) {
                if (response.isSuccessful()) {
                    List<MusicItem> body = response.body();
                    if (body != null) {
                        adapter.updateList(body);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MusicItem>> call, @NonNull Throwable throwable) {
                Log.e(TAG, "onFailure: VALIDATE DATA" + throwable.getMessage());

            }
        });
    }

    //AUDIO PLAYER
    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");

        audioPickerLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayerView.stopPlayback();
    }

}