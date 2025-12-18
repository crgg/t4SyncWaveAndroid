package com.t4app.t4syncwave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<Member> members = new ArrayList<>();
    private final Context context;

    public MemberAdapter(Context context) {
        this.context = context;
    }

    public void setMembers(List<Member> list) {
        members.clear();
        if (list != null) {
            members.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);

        holder.nameTv.setText(member.getName());
        holder.emailTv.setText(member.getEmail());
        holder.roleTv.setText(member.getRole().toUpperCase());

        holder.roleTv.setVisibility(
                "dj".equalsIgnoreCase(member.getRole()) ? View.VISIBLE : View.GONE
        );

        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile_fill_24)
                    .circleCrop()
                    .into(holder.avatarIv);
        } else {
            holder.avatarIv.setImageResource(R.drawable.ic_profile_fill_24);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;
        TextView nameTv, emailTv, roleTv;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            roleTv = itemView.findViewById(R.id.roleTv);
        }
    }
}

