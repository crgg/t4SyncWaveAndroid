package com.t4app.t4syncwave.conection;

public class ApiConfig {
    public static final String WEB_SOCKET_URL = "wss://t4videocall.t4ever.com/sfu-video/ws";
    public static final String BASE_URL = "https://t4videocall.t4ever.com/";

    public static final String LOGIN_URL = "api/auth/login";
    public static final String REGISTER_URL = "api/auth/register";

    //GROUPS
    public static final String GET_GROUPS = "api/groups/list";
    public static final String GET_ALL_GROUPS = "api/groups/groups-listens";
    public static final String GET_GROUP_BY_ID = "api/groups/get/{uuid}";
    public static final String ADD_GROUP = "api/groups/create";
    public static final String JOIN_GROUP = "api/groups/join";
    public static final String ADD_MEMBER = "api/groups/add-member";
    public static final String ADD_TRACK_TO_GROUP = "api/audio_test/add-track-to-group";



    public static final String UPLOAD_AUDIO = "api/audio_test/upload";
    public static final String GET_USER_TRACKS = "api/audio_test/list_all_by_user";
    public static final String GET_AUDIO_LIST = "api/audio/list";
}
