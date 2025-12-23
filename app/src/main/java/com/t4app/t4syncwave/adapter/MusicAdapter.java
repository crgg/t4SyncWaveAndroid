package com.t4app.t4syncwave.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<MusicItem> musicList = new ArrayList<>();
    private boolean clicksEnabled = false;
    private int playingPosition = RecyclerView.NO_POSITION;

    public interface OnMusicActionListener {
        void onPlay(MusicItem item, int pos);
        void onPause(MusicItem item);
        void onClick(MusicItem item, int position);
    }

    private OnMusicActionListener listener;

    public MusicAdapter(OnMusicActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<MusicItem> newList) {
        musicList.clear();
        if (newList != null) musicList.addAll(newList);
        playingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void addSong(MusicItem song) {
        if (song == null) return;

        musicList.add(song);
        notifyItemInserted(musicList.size() - 1);
    }

    public MusicItem getSong(int pos){
        return musicList.get(pos);
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        MusicItem item = musicList.get(position);

        holder.tvTitle.setText(item.getTitle());

        holder.btnPlay.setImageResource(item.isPlaying() ? R.drawable.ic_pause_no_round :
                R.drawable.ic_play_no_round);

        if (item.getDurationMs() > 0){
            holder.tvDuration.setText(formatTime(item.getDurationMs()));
        }

        holder.itemView.setOnClickListener(view ->{
            handleItemClick(position);
        });

        holder.btnPlay.setOnClickListener(v -> {
            if (!clicksEnabled) return;
            handlePlayClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    private void handlePlayClick(int position) {
        MusicItem clicked = musicList.get(position);
        Log.d("TAG_ADAPTER", "handlePlayClick: ");
        if (position == playingPosition) {
            boolean newState = !clicked.isPlaying();
            clicked.setPlaying(newState);

            if (newState) listener.onPlay(clicked, position);
            else listener.onPause(clicked);

            notifyItemChanged(position);
            return;
        }

        if (playingPosition != RecyclerView.NO_POSITION) {
            MusicItem previous = musicList.get(playingPosition);
            previous.setPlaying(false);
            notifyItemChanged(playingPosition);
            listener.onPause(previous);
        }

        clicked.setPlaying(true);
        playingPosition = position;
        notifyItemChanged(position);
        if (position != RecyclerView.NO_POSITION){
            listener.onPlay(clicked, position);
        }

    }

    public void compareItem(int position, MusicItem musicItem) {
        MusicItem clicked = musicList.get(position);
        if (!Objects.equals(clicked.getId(), musicItem.getId())){
            return;
        }
        if (position == playingPosition) {
            boolean newState = !clicked.isPlaying();
            clicked.setPlaying(newState);
            notifyItemChanged(position);
            return;
        }

        if (playingPosition != RecyclerView.NO_POSITION) {
            MusicItem previous = musicList.get(playingPosition);
            previous.setPlaying(false);
            notifyItemChanged(playingPosition);
        }

        clicked.setPlaying(true);
        playingPosition = position;
        notifyItemChanged(position);
        listener.onClick(clicked, position);
    }

    public void handleItemClick(int position) {
        MusicItem clicked = musicList.get(position);
        if (position == playingPosition) {
            boolean newState = !clicked.isPlaying();
            clicked.setPlaying(newState);
            notifyItemChanged(position);
            return;
        }

        if (playingPosition != RecyclerView.NO_POSITION) {
            MusicItem previous = musicList.get(playingPosition);
            previous.setPlaying(false);
            notifyItemChanged(playingPosition);
        }

        clicked.setPlaying(true);
        playingPosition = position;
        notifyItemChanged(position);
        listener.onClick(clicked, position);
    }

    public void toggleCurrentPlayPause() {
        if (playingPosition == RecyclerView.NO_POSITION) return;

        MusicItem item = musicList.get(playingPosition);
        item.setPlaying(!item.isPlaying());
        notifyItemChanged(playingPosition);
    }

    public void setClicksEnabled(boolean enabled) {
        this.clicksEnabled = enabled;
        notifyDataSetChanged();
    }


    public void setRemotePlaying(int position, boolean isPlaying) {

        if (playingPosition != RecyclerView.NO_POSITION
                && playingPosition != position) {

            musicList.get(playingPosition).setPlaying(false);
            notifyItemChanged(playingPosition);
        }

        playingPosition = position;

        if (position != RecyclerView.NO_POSITION) {
            musicList.get(position).setPlaying(isPlaying);
            notifyItemChanged(position);
        }
    }

    public static String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }


    static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView btnPlay;
        TextView tvTitle;
        TextView tvDuration;
        LinearLayout container;

        MusicViewHolder(View itemView) {
            super(itemView);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            container = itemView.findViewById(R.id.containerItemMusic);
        }
    }
}

