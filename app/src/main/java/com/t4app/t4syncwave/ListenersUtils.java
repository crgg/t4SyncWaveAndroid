package com.t4app.t4syncwave;

import com.t4app.t4syncwave.model.Group;
import com.t4app.t4syncwave.model.GroupItem;

public class ListenersUtils {

    public interface OnGetGroupListener{
        void onSuccess(Group group);
    }

    public interface OnRoomClickListener {
        void onRoomClick(GroupItem room);
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
    }
}
