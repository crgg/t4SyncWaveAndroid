package com.t4app.t4syncwave.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.model.RoomResponse;

import java.util.ArrayList;
import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RoomResponse room);
    }

    private List<RoomResponse> rooms = new ArrayList<>();
    private final OnRoomClickListener listener;

    public void updateList(List<RoomResponse> list) {
        rooms.clear();
        rooms.addAll(list);
        notifyDataSetChanged();
    }


    public RoomsAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomResponse room = rooms.get(position);

        holder.roomNameTv.setText(room.getName());
        holder.roomCodeTv.setText("Code: " + room.getCode());

        holder.roomStatusTv.setText(room.isActive() ? "Active" : "Inactive");
        holder.roomStatusTv.setTextColor(
                room.isActive() ? Color.GREEN : Color.GRAY
        );


        holder.playStatusIv.setImageResource(
                room.isPlaying()
                        ? R.drawable.ic_pause
                        : R.drawable.ic_play
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView roomNameTv, roomCodeTv, roomStatusTv;
        ImageView playStatusIv;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTv = itemView.findViewById(R.id.roomNameTv);
            roomCodeTv = itemView.findViewById(R.id.roomCodeTv);
            roomStatusTv = itemView.findViewById(R.id.roomStatusTv);
            playStatusIv = itemView.findViewById(R.id.playStatusIv);
        }
    }
}

