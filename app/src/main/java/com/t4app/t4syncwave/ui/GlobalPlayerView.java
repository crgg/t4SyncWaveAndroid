package com.t4app.t4syncwave.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.R;

import java.io.IOException;
import java.util.Locale;

public class GlobalPlayerView extends LinearLayout {
    private static final String TAG = "AUDIO_PLAYER_VIEW";

    private ImageButton btnPlayPause;
    private SeekBar seekBarAudio;
    private TextView songName;
    private TextView artistName;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private ListenersUtils.PlaybackActionListener listener;

    public void setPlaybackActionListener(ListenersUtils.PlaybackActionListener listener) {
        this.listener = listener;
    }

    private boolean iAmHost;

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarAudio.setProgress(currentPosition);
                handler.postDelayed(this, 100);
            }
        }
    };

    public GlobalPlayerView(Context context) {
        super(context);
        init(context);
    }

    public GlobalPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_global_player, this, true);

        initViews(view);
        setupListeners();

        setEnabled(false);
    }

    private void initViews(View view) {
        songName = view.findViewById(R.id.songName);
        artistName = view.findViewById(R.id.artistName);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        seekBarAudio = view.findViewById(R.id.seekBarAudio);

        btnPlayPause.setEnabled(false);
        seekBarAudio.setEnabled(false);
    }

    public void setTitle(String text){
        songName.setText(text);
    }

    public void setArtist(String text){
        artistName.setText(text);
    }

    private void setupAudioPlayer() {
        if (mediaPlayer != null) {
            seekBarAudio.setMax(mediaPlayer.getDuration());
        }
    }

    public void prepareAudio(String url) {
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

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (isPrepared) {
                if (iAmHost){
                    togglePlayPause();
                }
            }
        });

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

                    if (listener != null){
                        listener.onChangeSeek(seekBar.getProgress());
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

    public void startAudioPlayback() {
        mediaPlayer.start();
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        handler.postDelayed(updateProgress, 100);
        if (listener != null) listener.onPlayRequested();
    }

    public void pauseAudioPlayback() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updateProgress);
                if (listener != null) listener.onPauseRequested();
            }
        }
    }


    public void startLocal() {
        mediaPlayer.start();
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        handler.postDelayed(updateProgress, 100);
    }

    public void pauseLocal() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updateProgress);
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
        handler.removeCallbacks(updateProgress);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
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

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlayback() {
        pauseAudioPlayback();
        resetPlayer();
    }

    public void setIamHost(boolean status){
        iAmHost = status;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setProgress(int progress){
        if (mediaPlayer != null){
            mediaPlayer.seekTo(progress);
            if (isPlaying) {
                handler.postDelayed(updateProgress, 100);
            }
        }
    }

    public boolean isPrepared() {
        return isPrepared;
    }
}
