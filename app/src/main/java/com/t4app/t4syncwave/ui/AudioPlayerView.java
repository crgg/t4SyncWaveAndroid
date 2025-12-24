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

import androidx.constraintlayout.widget.ConstraintLayout;

import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SafeClickListener;

import java.io.IOException;
import java.util.Locale;

public class AudioPlayerView extends LinearLayout {
    private static final String TAG = "AUDIO_PLAYER_VIEW";
    private static final int SEEK_INTERVAL_MS = 15_000;

    private ImageButton btnPlayPause;
    private ImageButton btnRepeat;
    private ImageButton btnBackward15;
    private ImageButton btnForward15;
    private ImageButton btnSound;

    private ConstraintLayout containerActions;
    private LinearLayout containerMute;

    private SeekBar seekBarAudio;
    private TextView tvCurrentTime;
    private TextView songName;
    private TextView tvTotalTime;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private float lastVolume = 1f;
    private boolean isMuted = false;

    private boolean isUserSeeking = false;
    private boolean isPlaying = false;
    private boolean isPrepared = false;
    private boolean isRepeat = false;

    private int startPosition = 0;

    private ListenersUtils.PlaybackActionListener listener;

    public void setPlaybackActionListener(ListenersUtils.PlaybackActionListener listener) {
        this.listener = listener;
    }

    private boolean iAmHost;

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying && !isUserSeeking) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarAudio.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition / 1000) + " / ");
                listener.onChangePosition(currentPosition);

                Log.d(TAG, "run: UPDATE PROGRESS CURRENT POST IS " + currentPosition);
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
        btnRepeat = view.findViewById(R.id.repeatBtn);
        btnBackward15 = view.findViewById(R.id.backward15);
        btnForward15 = view.findViewById(R.id.forward15);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        seekBarAudio = view.findViewById(R.id.seekBarAudio);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvTotalTime = view.findViewById(R.id.tvTotalTime);

        containerActions = view.findViewById(R.id.containerActions);
        containerMute = view.findViewById(R.id.containerMute);
        btnSound = view.findViewById(R.id.btnMute);

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

    public void prepareAudio(String url) {
        cleanupMediaPlayer();

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                setupAudioPlayer();

                if (startPosition > 0){
                    mp.seekTo(startPosition);
                    seekBarAudio.setProgress(startPosition);
                    tvCurrentTime.setText(formatTime(startPosition / 1000) + " / ");
                }

                applyMuteState();

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

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    private void applyMuteState() {
        if (mediaPlayer == null) return;

        if (isMuted) {
            mediaPlayer.setVolume(0f, 0f);
            btnSound.setImageResource(R.drawable.ic_mute);
        } else {
            mediaPlayer.setVolume(lastVolume, lastVolume);
            btnSound.setImageResource(R.drawable.ic_sound);
        }
    }

    private void muteAudio() {
        if (mediaPlayer == null || isMuted) return;

        lastVolume = 1f;
        isMuted = true;
        applyMuteState();
    }

    private void unmuteAudio() {
        if (mediaPlayer == null) return;

        isMuted = false;
        applyMuteState();
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(new SafeClickListener() {
            @Override
            public void onSafeClick(View v) {
                if (isPrepared) {
                    if (iAmHost){
                        togglePlayPause();
                    }
                }
            }
        });

        btnSound.setOnClickListener(new SafeClickListener() {
            @Override
            public void onSafeClick(View v) {
                if (isPrepared){
                    if (!iAmHost){
                        if (isMuted){
                            unmuteAudio();
                        }else {
                            muteAudio();
                        }
                    }
                }
            }
        });

        if (iAmHost) {
            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null) {
                        tvCurrentTime.setText(formatTime(progress / 1000) + " / ");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isUserSeeking = true;
                    handler.removeCallbacks(updateProgress);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isUserSeeking = false;
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

        btnBackward15.setOnClickListener(new SafeClickListener() {
            @Override
            public void onSafeClick(View v) {
                if (iAmHost) {
                    seekBy(-SEEK_INTERVAL_MS);
                }
            }
        });

        btnForward15.setOnClickListener(new SafeClickListener() {
            @Override
            public void onSafeClick(View v) {
                if (iAmHost) {
                    seekBy(SEEK_INTERVAL_MS);
                }
            }
        });

        btnRepeat.setOnClickListener(view -> {
            if (iAmHost){
                if (mediaPlayer != null && isPrepared){
                    isRepeat = !isRepeat;
                    mediaPlayer.setLooping(isRepeat);
                    btnRepeat.setImageResource(isRepeat ? R.drawable.ic_true_repeat : R.drawable.ic_no_repeat);
                }
            }
        });
    }

    private void seekBy(int deltaMs) {
        if (mediaPlayer == null || !isPrepared) return;

        int current = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();

        int newPosition = current + deltaMs;

        if (newPosition < 0) newPosition = 0;
        if (newPosition > duration) newPosition = duration;

        mediaPlayer.seekTo(newPosition);
        seekBarAudio.setProgress(newPosition);
        tvCurrentTime.setText(formatTime(newPosition / 1000) + " / ");

        if (listener != null) {
            listener.onChangeSeek(newPosition);
        }
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
        if (!iAmHost){
            containerMute.setVisibility(View.VISIBLE);
            containerActions.setVisibility(View.GONE);
        }else{
            containerMute.setVisibility(View.GONE);
            containerActions.setVisibility(View.VISIBLE);
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setProgress(int progress){
        if (mediaPlayer != null){
            Log.d(TAG, "setProgress: NEW PROGRESS " + progress);
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
