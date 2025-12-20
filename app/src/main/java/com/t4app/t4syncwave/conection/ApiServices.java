package com.t4app.t4syncwave.conection;

import com.t4app.t4syncwave.conection.model.AddGroupResponse;
import com.t4app.t4syncwave.conection.model.AddMemberResponse;
import com.t4app.t4syncwave.conection.model.GetGroupByIdResponse;
import com.t4app.t4syncwave.conection.model.GetTracksResponse;
import com.t4app.t4syncwave.conection.model.LoginResponse;
import com.t4app.t4syncwave.conection.model.ResponseGetGroups;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiServices {

    //AUTH
    @POST(ApiConfig.LOGIN_URL)
    Call<LoginResponse> login(
            @Body Map<String, Object> body
    );

    @POST(ApiConfig.REGISTER_URL)
    Call<LoginResponse> register(
            @Body Map<String, Object> body
    );


    //GROUP
    @GET(ApiConfig.GET_ALL_GROUPS)
    Call<ResponseGetGroups> getAllGroupsList();

    @GET(ApiConfig.GET_GROUPS)
    Call<ResponseGetGroups> getGroupsList();

    @POST(ApiConfig.ADD_MEMBER)
    Call<AddMemberResponse> addMember(
            @Body Map<String, Object> body
    );

    @POST(ApiConfig.ADD_GROUP)
    Call<AddGroupResponse> addGroup(
            @Body Map<String, Object> body
    );

    @GET(ApiConfig.GET_GROUP_BY_ID)
    Call<GetGroupByIdResponse> getGroupById(
            @Path("uuid") String id
    );


    //AUDIO
    @Multipart
    @POST(ApiConfig.UPLOAD_AUDIO)
    Call<AudioUploadResponse> uploadAudio(
            @Part MultipartBody.Part file,
            @Part("groupId") RequestBody groupId
    );

    @GET(ApiConfig.GET_AUDIO_LIST)
    Call<List<MusicItem>> getAudioList();

    @GET(ApiConfig.GET_USER_TRACKS)
    Call<GetTracksResponse> getUserTracks();
}
