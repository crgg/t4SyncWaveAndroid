package com.t4app.t4syncwave;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.RetrofitClient;
import com.t4app.t4syncwave.databinding.ActivityMainBinding;
import com.t4app.t4syncwave.events.PlaybackEvent;
import com.t4app.t4syncwave.events.PlaybackViewEvent;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModel;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class T4SyncWaveMainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    private ActivityMainBinding binding;

    private ImageButton btnPlayPause;
    private SeekBar seekBarAudio;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private PlaybackState state;

    private PlaybackViewModel viewModel;
    private Room room;

    private ActivityResultLauncher<Intent> audioPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {

                            Uri audioUri = result.getData().getData();
                            if (audioUri != null) {
                                try {
                                    uploadAudio(audioUri);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
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

        btnPlayPause.setEnabled(false);
        seekBarAudio.setEnabled(false);

        setupListeners();

        observeEvents();

        binding.selectAudioBtn.setOnClickListener(view -> openAudioPicker());

        binding.connectBtn.setOnClickListener(view -> {
            String roomName  = binding.roomNameEt.getText().toString();
            String username  = binding.usernameEt.getText().toString();

            if (!roomName.isEmpty() && !username.isEmpty()){
                viewModel.processInput(new PlaybackViewEvent.Connect(roomName, username));
            }

        });

        binding.disconnectBtn.setOnClickListener(view -> {
            viewModel.processInput(PlaybackViewEvent.Disconnect.INSTANCE);
        });

    }

    private void observeEvents(){
        viewModel.events.observe(this, playbackEvent -> {
            if (playbackEvent instanceof PlaybackEvent.RemoteParticipantEvent){
                handleRemoteParticipantEvent((PlaybackEvent.RemoteParticipantEvent) playbackEvent);
            } else if (playbackEvent instanceof PlaybackEvent.Connected) {
                PlaybackEvent.Connected connected = (PlaybackEvent.Connected) playbackEvent;
                room = connected.getRoom();

                binding.connectBtn.setVisibility(View.GONE);
                binding.disconnectBtn.setVisibility(View.VISIBLE);
                binding.selectAudioBtn.setVisibility(View.VISIBLE);
            }else if (playbackEvent instanceof PlaybackEvent.UrlChanged) {
                PlaybackEvent.UrlChanged urlChanged = (PlaybackEvent.UrlChanged) playbackEvent;
                setAudioUrl(urlChanged.getUrl());
            }
        });
    }


    private void handleRemoteParticipantEvent(PlaybackEvent.RemoteParticipantEvent event){
        if (event instanceof PlaybackEvent.RemoteParticipantEvent.UserJoined){
            PlaybackEvent.RemoteParticipantEvent.UserJoined userJoined = (PlaybackEvent.RemoteParticipantEvent.UserJoined) event;
            binding.participants.setText("Remote Participant Joined " + userJoined.getName());
        }else if (event instanceof PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState){
            PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState remoteState =
                    (PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState) event;

            if (mediaPlayer != null){
                if (remoteState.getState().isPlaying()){
                    mediaPlayer.start();
                    isPlaying = true;
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    handler.postDelayed(updateProgress, 100);

                }else{
                    mediaPlayer.pause();
                    isPlaying = false;
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateProgress);
                }
            }

        }
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");

        audioPickerLauncher.launch(intent);
    }

    public void setAudioUrl(String url) {
        cleanupMediaPlayer();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                setupAudioPlayer();
                btnPlayPause.setEnabled(true);
                seekBarAudio.setEnabled(true);

                binding.selectAudioBtn.setVisibility(View.VISIBLE);
            });

            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                resetPlayer();
                return false;
            });

        } catch (IOException e) {
            Log.e(TAG, "setAudioUrl: ", e);
        }
    }

    private void uploadAudio(Uri uri) throws IOException {
        MultipartBody.Part audioPart = createAudioPart(this, uri, "file");

        ApiServices apiServices = RetrofitClient.getRetrofitClient().create(ApiServices.class);
        Call<AudioUploadResponse> call = apiServices.uploadFile(audioPart);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AudioUploadResponse> call, @NonNull Response<AudioUploadResponse> response) {
                if (response.isSuccessful()){
                    AudioUploadResponse body = response.body();
                    if (body != null){
                        if (body.isOk()){
                            viewModel.processInput(new PlaybackViewEvent.AudioChanged(body.getUrl()));
//                            setAudioUrl(body.getUrl());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AudioUploadResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "onFailure: VALIDATE DATA" + throwable.getMessage());

            }
        });
    }

    public static MultipartBody.Part createAudioPart(
            Context context,
            Uri audioUri,
            String formFieldName) throws IOException {

        File audioFile = audioFileFromUri(context, audioUri);

        String mimeType = context.getContentResolver().getType(audioUri);
        if (mimeType == null) {
            mimeType = "audio/*";
        }

        RequestBody requestBody =
                RequestBody.create(audioFile, MediaType.parse(mimeType));

        return MultipartBody.Part.createFormData(
                formFieldName,
                audioFile.getName(),
                requestBody
        );
    }


    public static File audioFileFromUri(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        String fileName = getFileName(resolver, uri);
        if (fileName == null) {
            fileName = "audio_" + System.currentTimeMillis();
        }

        File outFile = new File(context.getCacheDir(), fileName);

        try (InputStream in = resolver.openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        return outFile;
    }

    private static String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }



    private void setupAudioPlayer() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration() / 1000;
            seekBarAudio.setMax(mediaPlayer.getDuration());
            tvTotalTime.setText(formatTime(duration));
            tvCurrentTime.setText("0:00 / ");
            state = new PlaybackState.Builder(
                    "playback-state",
                    room.getRoomName(),
                    room.getUserName(),
                    duration
                    )
                    .setPlaying(false)
                    .setPosition(0.0)
                    .build();

//            viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
        }
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPrepared) {
                togglePlayPause();
            }
        });

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    tvCurrentTime.setText(formatTime(progress / 1000) + " / ");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateProgress);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    if (isPlaying) {
                        handler.postDelayed(updateProgress, 100);
                    }
                }
            }
        });
    }

    private void togglePlayPause() {
        if (!isPrepared || mediaPlayer == null) return;

        //TODO CHANGE AMBAS LOGICAS POR EVENTS AND CHANGE STATUS
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

    public String getFileName(Context context, Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null);

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (index != -1) {
                            result = cursor.getString(index);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result;
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