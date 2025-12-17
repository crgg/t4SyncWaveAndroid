package com.t4app.t4syncwave.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.RoomsAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.ResponseGetGroups;
import com.t4app.t4syncwave.databinding.ActivityT4SyncWaveMainBinding;
import com.t4app.t4syncwave.model.RoomResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class T4SyncWaveMainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACT";
    private RoomsAdapter adapter;
    private ActivityT4SyncWaveMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityT4SyncWaveMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.getRememberMe()){
            Intent intent = new Intent(T4SyncWaveMainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        adapter = new RoomsAdapter(room -> {
            Intent intent = new Intent(T4SyncWaveMainActivity.this, T4SyncWaveRoomActivity.class);
            intent.putExtra("roomName", room.getName());
            intent.putExtra("userName", sessionManager.getName());
            startActivity(intent);
        });

        binding.roomsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.roomsRv.setAdapter(adapter);

        getGroups();
    }


    private void getGroups(){
        ApiServices apiServices = AppController.getApiServices();
        Call<ResponseGetGroups> call = apiServices.getGroupsList();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseGetGroups> call, Response<ResponseGetGroups> response) {
                if (response.isSuccessful()) {
                    ResponseGetGroups body = response.body();
                    if (body != null) {
                        if (body.isStatus()){
                            adapter.updateList(body.getGroups());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseGetGroups> call, Throwable t) {
                Log.e(TAG, "onFailure: GET GROUPS" + t.getMessage() );
            }
        });
    }

}