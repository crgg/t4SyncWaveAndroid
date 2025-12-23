package com.t4app.t4syncwave.viewmodel;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.GetTracksResponse;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryViewModel extends ViewModel {
    private final MutableLiveData<List<MusicItem>> tracks = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<MusicItem>> getTracks(){
        return tracks;
    }

    public LiveData<String> getError(){
        return error;
    }

    public void getAllTracks(){
        ApiServices apiServices = AppController.getApiServices();
        Call<GetTracksResponse> call = apiServices.getUserTracks();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetTracksResponse> call, Response<GetTracksResponse> response) {
                if (response.isSuccessful()) {
                    GetTracksResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getAudio() != null) {
                            tracks.postValue(body.getAudio());
                        } else {
                            if (body.getError() != null && body.getError().contains("No audio found")) {
                                error.postValue(body.getError());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GetTracksResponse> call, Throwable t) {
                error.postValue(ErrorUtils.parseError(t));
            }
        });
    }
}
