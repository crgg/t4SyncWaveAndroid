package com.t4app.t4syncwave.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.t4app.t4syncwave.PermissionUtil;
import com.t4app.t4syncwave.PlaybackManager;

public class PlaybackViewModelFactory implements ViewModelProvider.Factory {

    private final PlaybackManager playbackManager;
    private final PermissionUtil permissionUtil;

    public PlaybackViewModelFactory(PlaybackManager playbackManager, PermissionUtil permissionUtil) {
        this.playbackManager = playbackManager;
        this.permissionUtil = permissionUtil;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PlaybackViewModel.class)) {
            return (T) new PlaybackViewModel(playbackManager, permissionUtil);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
