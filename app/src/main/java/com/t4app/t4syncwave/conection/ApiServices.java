package com.t4app.t4syncwave.conection;

import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiServices {

    @POST(ApiConfig.UPLOAD_AUDIO)
    @Multipart
    Call<AudioUploadResponse> uploadFile(
            @Part MultipartBody.Part files
    );

    @GET(ApiConfig.GET_AUDIO_LIST)
    Call<List<MusicItem>> getAudioList();
}
