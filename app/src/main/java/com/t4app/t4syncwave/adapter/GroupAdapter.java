package com.t4app.t4syncwave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.RoomViewHolder> {

    private List<Group> rooms = new ArrayList<>();
    private final ListenersUtils.OnRoomClickListener listener;

    public void updateList(List<Group> list) {
        rooms.clear();
        rooms.addAll(list);
        notifyDataSetChanged();
    }


    public GroupAdapter(ListenersUtils.OnRoomClickListener listener) {
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
        Group room = rooms.get(position);

        holder.roomNameTv.setText(room.getName());

        if (room.getCode() != null){
            holder.roomCodeTv.setText(room.getCode());
        }

        holder.roomStatusTv.setText(room.isActive() ? holder.itemView.getContext().getString(R.string.active)
                : holder.itemView.getContext().getString(R.string.inactive));

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

    public void addGroup(Group group) {
        if (group == null) return;

        rooms.add(group);
        notifyItemInserted(rooms.size() - 1);
    }
}

