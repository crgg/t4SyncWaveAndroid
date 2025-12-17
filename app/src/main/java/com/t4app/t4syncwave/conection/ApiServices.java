package com.t4app.t4syncwave.conection;

import com.google.gson.JsonObject;
import com.t4app.t4syncwave.conection.model.LoginResponse;
import com.t4app.t4syncwave.conection.model.ResponseGetGroups;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiServices {

    @POST(ApiConfig.LOGIN_URL)
    Call<LoginResponse> login(
            @Body Map<String, Object> body
    );

    @POST(ApiConfig.REGISTER_URL)
    Call<LoginResponse> register(
            @Body Map<String, Object> body
    );


    @POST(ApiConfig.UPLOAD_AUDIO)
    @Multipart
    Call<AudioUploadResponse> uploadFile(
            @Part MultipartBody.Part files
    );

    @GET(ApiConfig.GET_GROUPS)
    Call<ResponseGetGroups> getGroupsList();

    @GET(ApiConfig.GET_AUDIO_LIST)
    Call<List<MusicItem>> getAudioList();
}
