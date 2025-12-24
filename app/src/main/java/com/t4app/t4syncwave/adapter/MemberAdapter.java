package com.t4app.t4syncwave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<Member> members = new ArrayList<>();
    private final Context context;
    private SessionManager sessionManager;

    public MemberAdapter(Context context) {
        this.context = context;
        sessionManager = SessionManager.getInstance();
    }

    public void setMembers(List<Member> list) {
        members.clear();
        if (list != null) {
            members.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addMember(Member member) {
        if (member == null) return;
        members.add(member);
        notifyItemInserted(members.size() - 1);
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

        if (member.getName() == null && member.getUser() != null){
            holder.nameTv.setText(member.getUser().getName());
            member.setName(member.getUser().getName());
            member.setEmail(member.getUser().getEmail());
        }
        if (member.getName().equalsIgnoreCase(sessionManager.getName()) &&
                member.getEmail().equalsIgnoreCase(sessionManager.getUserEmail())){
            holder.nameTv.setText(R.string.you);
        }else{
            holder.nameTv.setText(member.getName());
        }

        holder.roleTv.setText(member.getRole().toUpperCase());



        holder.roleTv.setVisibility(
                "dj".equalsIgnoreCase(member.getRole()) ? View.VISIBLE : View.GONE
        );

        int color = member.isConnected() ? R.color.green_success : R.color.gray_hint;

        holder.statusOnlineIv.setImageTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), color));

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
        ImageView statusOnlineIv;
        TextView nameTv, roleTv;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            statusOnlineIv = itemView.findViewById(R.id.statusOnlineIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            roleTv = itemView.findViewById(R.id.roleTv);
        }
    }

    public List<Member> getMembers() {
        return members;
    }

    public void removeUserIfExists(String userId) {
        if (members.isEmpty()) return;
        for (int i = 0; i < members.size(); i++) {
            if (userId.equals(members.get(i).getUserId())) {
                members.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }


    public Member getUserConnected(String userName, boolean connected) {
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            if (m.getName() != null && m.getName().equalsIgnoreCase(userName)) {
                if (m.isConnected() != connected) {
                    return m;
                }
                break;
            }
        }
        return null;
    }

}

