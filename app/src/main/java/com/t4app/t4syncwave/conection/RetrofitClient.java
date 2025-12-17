package com.t4app.t4syncwave.conection;

import android.util.Log;

import androidx.annotation.NonNull;

import com.t4app.t4syncwave.SessionManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RETROFIT-CLIENT";

    public static Retrofit getRetrofitClient() {
        String baseUrl = ApiConfig.BASE_URL;
        SessionManager sessionManager = SessionManager.getInstance();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                Log.d("RETRO", message)
        );
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + sessionManager.getTokenKey())
                    .addHeader("Accept", "application/json")
                    .build();
            return chain.proceed(request);
        };

        Interceptor errorInterceptor = chain -> {
            Response response = chain.proceed(chain.request());
            if (!response.isSuccessful()) {
                String body = response.body() != null
                        ? response.peekBody(Long.MAX_VALUE).string()
                        : "";
                Log.e(TAG, "HTTP Error: " + response.code() + " - " + body);
                throw new IOException("HTTP Error: " + response.code());
            }
            return response;
        };

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(errorInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(90, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .build();

            return new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
