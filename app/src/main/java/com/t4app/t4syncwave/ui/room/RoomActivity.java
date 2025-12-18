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
import com.t4app.t4syncwave.adapter.MusicAdapter;
import com.t4app.t4syncwave.PermissionUtil;
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

    private ImageButton btnPlayPause;
    private SeekBar seekBarAudio;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;

    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private PlaybackState state;

    private PlaybackViewModel viewModel;
    private Room room;
    private MusicAdapter adapter;

    private String songListening;
    private boolean iAmHost = false;

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

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarAudio.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition / 1000) + " / ");
                handler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        room = new Room();

        PlaybackManager playbackManager = new PlaybackManager(getApplicationContext());
        PermissionUtil permissionUtil = new PermissionUtil(this);

        PlaybackViewModelFactory factory = new PlaybackViewModelFactory(playbackManager, permissionUtil);

        viewModel = new ViewModelProvider(this, factory)
                .get(PlaybackViewModel.class);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);

        adapter = new MusicAdapter(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onPlay(MusicItem item, int position) {
                if (iAmHost){
                    if (item.getFileUrl().equalsIgnoreCase(songListening)){
                        startAudioPlayback();
                    }else{
                        prepareAudio(item.getFileUrl(), true);
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
                    pauseAudioPlayback();
                }
            }
        });

        binding.musicListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.musicListRv.setAdapter(adapter);

        btnPlayPause.setEnabled(false);
        seekBarAudio.setEnabled(false);

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
            viewModel.processInput(new PlaybackViewEvent.Connect(roomName, username));
        }

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
                setupListeners();
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
                        if (mediaPlayer != null){
                            mediaPlayer.seekTo((int) remoteState.getState().getTimestamp());
                            if (isPlaying) {
                                handler.postDelayed(updateProgress, 100);
                            }
                        }
                        state = state.copy().
                                setTimestamp((int) remoteState.getState().getTimestamp()).
                                build();
                    }
                }else{
                    state = remoteState.getState();
                }
            }

            int pos = remoteState.getState().getPosition().intValue();
            if (mediaPlayer != null){
                if (!adapter.getSong(pos).getFileUrl().equalsIgnoreCase(songListening)){
                    prepareAudio(adapter.getSong(pos).getFileUrl(), remoteState.getState().isPlaying());
                }
                if (remoteState.getState().isPlaying()){
                    mediaPlayer.start();
                    isPlaying = true;
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    handler.postDelayed(updateProgress, 100);
                }else{
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = false;
                        btnPlayPause.setImageResource(R.drawable.ic_play);
                        handler.removeCallbacks(updateProgress);
                    }
                }

                Log.d(TAG, "SYNC PLAY PAUSE STATE: " + remoteState.getState().isPlaying());
                adapter.setRemotePlaying(pos, remoteState.getState().isPlaying());
            }else{
                prepareAudio(adapter.getSong(pos).getFileUrl(), remoteState.getState().isPlaying());
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


    public void prepareAudio(String url, boolean playing) {
        cleanupMediaPlayer();

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                setupAudioPlayer();
                if (iAmHost){
                    btnPlayPause.setEnabled(true);
                }
                seekBarAudio.setEnabled(true);
                songListening = url;
                togglePlayPause();
            });

            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                resetPlayer();
                return true;
            });

        } catch (IOException e) {
            Log.e(TAG, "prepareAudio: ", e);
        }
    }


    private void setupAudioPlayer() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration() / 1000;
            seekBarAudio.setMax(mediaPlayer.getDuration());
            tvTotalTime.setText(formatTime(duration));
            tvCurrentTime.setText("0:00 / ");
        }
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPrepared) {
                togglePlayPause();
                adapter.toggleCurrentPlayPause();
            }
        });

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: ");
                if (fromUser && mediaPlayer != null) {
                    tvCurrentTime.setText(formatTime(progress / 1000) + " / ");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch: ");
                handler.removeCallbacks(updateProgress);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: ");
                if (mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    PlaybackState updateState = state.copy()
                            .setTimestamp(seekBar.getProgress())
                            .build();
                    viewModel.processInput(new PlaybackViewEvent.ChangeState(updateState));
                    if (isPlaying) {
                        handler.postDelayed(updateProgress, 100);
                    }
                }
            }
        });
    }

    private void togglePlayPause() {
        if (!isPrepared || mediaPlayer == null) return;
        if (!isPlaying) {
            startAudioPlayback();
        } else {
            pauseAudioPlayback();
        }
    }

    private void startAudioPlayback() {
        mediaPlayer.start();
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        handler.postDelayed(updateProgress, 100);
        PlaybackState updated = state.copy()
                .setPlaying(true)
                .build();
        viewModel.processInput(new PlaybackViewEvent.ChangeState(updated));
    }

    private void pauseAudioPlayback() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updateProgress);
                PlaybackState updated = state.copy()
                        .setPlaying(false)
                        .build();
                viewModel.processInput(new PlaybackViewEvent.ChangeState(updated));
            }
        }
    }

    private void resetPlayer() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
        seekBarAudio.setProgress(0);
        tvCurrentTime.setText("0:00");
        handler.removeCallbacks(updateProgress);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateProgress);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPrepared = false;
        isPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayback();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlayback() {
        pauseAudioPlayback();
        resetPlayer();
    }
}