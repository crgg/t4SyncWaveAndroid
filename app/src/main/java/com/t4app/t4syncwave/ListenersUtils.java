package com.t4app.t4syncwave;

import com.t4app.t4syncwave.model.Group;

public class ListenersUtils {

    public interface OnGetGroupListener{
        void onSuccess(Group group);
    }

    public interface OnRoomClickListener {
        void onRoomClick(Group room);
    }

    public interface AddGroupListener{
        void onAddGroup(String groupName);
    }

    public interface AddMemberListener{
        void onAddMember(String email);
    }

    public interface PlaybackActionListener {
        void onPlayRequested();
        void onPauseRequested();
        void onChangeSeek(int progress);
        void onChangePosition(int progress);
    }
}
