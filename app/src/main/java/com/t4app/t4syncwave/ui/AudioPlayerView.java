package com.t4app.t4syncwave.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
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

public class AudioPlayerView extends LinearLayout {
    private static final String TAG = "AUDIO_PLAYER_VIEW";

    private ImageButton btnPlayPause;
    private SeekBar seekBarAudio;
    private TextView tvCurrentTime;
    private TextView songName;
    private TextView tvTotalTime;

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
                tvCurrentTime.setText(formatTime(currentPosition / 1000) + " / ");
                handler.postDelayed(this, 100);
            }
        }
    };

    public AudioPlayerView(Context context) {
        super(context);
        init(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_audio_player, this, true);

        initViews(view);
        setupListeners();

        setEnabled(false);
    }

    private void initViews(View view) {
        songName = view.findViewById(R.id.songName);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        seekBarAudio = view.findViewById(R.id.seekBarAudio);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvTotalTime = view.findViewById(R.id.tvTotalTime);

        btnPlayPause.setEnabled(false);
        seekBarAudio.setEnabled(false);
    }

    public void setTitle(String text){
        songName.setText(text);
    }

    private void setupAudioPlayer() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration() / 1000;
            seekBarAudio.setMax(mediaPlayer.getDuration());
            tvTotalTime.setText(formatTime(duration));
            tvCurrentTime.setText("0:00 / ");
        }
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
//                togglePlayPause();
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
                if (fromUser && mediaPlayer != null) {
                    tvCurrentTime.setText(formatTime(progress / 1000)+ " / ");
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
                    listener.onChangeSeek(seekBar.getProgress());
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
        tvCurrentTime.setText(R.string._0_00);
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
