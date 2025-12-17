package com.t4app.t4syncwave;

import android.app.Application;

import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.RetrofitClient;

public class AppController extends Application {
    private static AppController instance;

    private static ApiServices apiServices = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.initialize(getApplicationContext());
    }

    public static ApiServices getApiServices(){
        if (apiServices == null){
            apiServices = RetrofitClient.getRetrofitClient().create(ApiServices.class);
        }
        return apiServices;
    }

}
