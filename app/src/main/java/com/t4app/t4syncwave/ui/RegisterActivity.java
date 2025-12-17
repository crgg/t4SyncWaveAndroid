package com.t4app.t4syncwave.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.LoginResponse;
import com.t4app.t4syncwave.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "REGISTER_ACTIVITY";
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.createAccountBtn.setOnClickListener(view -> {
            String name = binding.nameValue.getText().toString();
            String email = binding.emailValue.getText().toString();
            String password = binding.passwordValue.getText().toString();
            String validationPassword = binding.rePasswordValue.getText().toString();

            if (name.isEmpty()){
                binding.nameLayout.setError("Name is required");
                binding.nameValue.requestFocus();
            }else if (email.isEmpty()){
                binding.emailLayout.setError("Email is required");
                binding.emailValue.requestFocus();
            }else if (password.isEmpty()){
                binding.passwordLayout.setError("Password is required");
                binding.passwordLayout.setErrorIconDrawable(null);
                binding.passwordValue.requestFocus();
            }else if (!password.equalsIgnoreCase(validationPassword)){
                binding.rePasswordLayout.setError("Both passwords must be the same.");
                binding.rePasswordLayout.setErrorIconDrawable(null);
                binding.rePasswordValue.requestFocus();
            }else {
                register(name, email, password);
            }
        });

        binding.signIn.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        });
    }

    private void register(String name, String email, String password){
        ApiServices apiServices = AppController.getApiServices();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        Call<LoginResponse> call = apiServices.register(data);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    LoginResponse body = response.body();
                    if (body != null){
                        if (body.getMessage() != null && body.getMessage().contains("User registered successfully")) {
                            Intent intent = new Intent(RegisterActivity.this, T4SyncWaveMainActivity.class);
                            SessionManager sessionManager = SessionManager.getInstance();
                            sessionManager.saveUserDetails(
                                    body.getUser().getId(),
                                    body.getUser().getName(),
                                    body.getUser().getEmail(),
                                    body.getToken(),
                                    true);
                            startActivity(intent);
                            finish();
                        }else{
                            if (body.getError() != null) {
                                MessagesUtils.showErrorDialog(RegisterActivity.this,
                                        body.getError());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: LOGIN" + t.getMessage());
                MessagesUtils.showErrorDialog(RegisterActivity.this,
                        ErrorUtils.parseError(t));
            }
        });
    }

    private void login(String email, String password){
        ApiServices apiServices = AppController.getApiServices();
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        Call<LoginResponse> call = apiServices.login(data);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    LoginResponse body = response.body();
                    if (body != null){
                        if (body.getMessage() != null && body.getMessage().contains("Login successful")) {
                            Intent intent = new Intent(RegisterActivity.this, T4SyncWaveRoomActivity.class);
                            SessionManager sessionManager = SessionManager.getInstance();
                            sessionManager.saveUserDetails(
                                    body.getUser().getId(),
                                    body.getUser().getName(),
                                    body.getUser().getEmail(),
                                    body.getToken(),
                                    true);
                            startActivity(intent);
                            finish();
                        }else{
                            if (body.getError() != null) {
                                MessagesUtils.showErrorDialog(RegisterActivity.this,
                                        body.getError());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: LOGIN" + t.getMessage());
                MessagesUtils.showErrorDialog(RegisterActivity.this,
                        ErrorUtils.parseError(t));
            }
        });
    }


}