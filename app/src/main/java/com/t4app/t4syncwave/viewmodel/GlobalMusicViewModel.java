package com.t4app.t4syncwave.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.t4app.t4syncwave.model.MusicItem;

import java.util.ArrayList;
import java.util.List;

public class GlobalMusicViewModel extends ViewModel {

    private final MutableLiveData<List<MusicItem>> musicList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<MusicItem> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);


    public LiveData<List<MusicItem>> getMusicList() {
        return musicList;
    }

    public LiveData<MusicItem> getCurrentSong() {
        return currentSong;
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<Integer> getCurrentPosition(){
        return currentPosition;
    }

    private void setCurrentPosition(Integer pos){
        currentPosition.setValue(pos);
    }

    public void setMusicList(List<MusicItem> list) {
        musicList.setValue(list);
    }

    public void play(){
        isPlaying.setValue(true);
    }

    public void playSong(MusicItem item) {
        currentSong.setValue(item);
        isPlaying.setValue(true);
    }

    public void pause() {
        isPlaying.setValue(false);
    }

    public void stop() {
        isPlaying.setValue(false);
        currentSong.setValue(null);
    }
}

