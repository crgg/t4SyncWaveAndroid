package com.t4app.t4syncwave.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.adapter.MusicAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.GetTracksResponse;
import com.t4app.t4syncwave.databinding.FragmentLibraryBinding;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {
    private static final String TAG = "LIBRARY_FRAGMENT";

    private FragmentLibraryBinding binding;
    private MusicAdapter adapter;

    public LibraryFragment() {
    }

    private ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {

                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        uploadAudio(audioUri);
                    }
                }
            }
    );

    public static LibraryFragment newInstance() {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MusicAdapter(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onPlay(MusicItem item, int position) {

            }

            @Override
            public void onPause(MusicItem item) {

            }

            @Override
            public void onClick(MusicItem item) {

            }
        });

        binding.musicListRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.musicListRv.setAdapter(adapter);

        getAllTracks();


        binding.btnAdd.setOnClickListener(view1 -> {
            openAudioPicker();
        });
    }


    private void uploadAudio(Uri uri){
        ApiServices apiServices = AppController.getApiServices();

        RequestBody audioBody = createRequestBodyFromUri(requireContext(), uri);

        String fileName = getFileNameFromUri(requireContext(), uri);

        Log.d(TAG, "uploadAudio: " + fileName);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("file", fileName, audioBody);

        RequestBody groupIdBody = null;

        Call<AudioUploadResponse> call = apiServices.uploadAudio(audioPart, null);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AudioUploadResponse> call, Response<AudioUploadResponse> response) {
                if (response.isSuccessful()) {
                    AudioUploadResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getAudio() != null) {
                            adapter.addSong(body.getAudio());
                            binding.currentMusicPlay.setVisibility(View.VISIBLE);
                        } else {
                            if (body.getError() != null) {
                                MessagesUtils.showErrorDialog(requireActivity(), body.getError());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AudioUploadResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: add group", t);
                MessagesUtils.showErrorDialog(requireActivity(), ErrorUtils.parseError(t));
            }
        });
    }

    public static RequestBody createRequestBodyFromUri(Context context, Uri uri) {

        return new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse(context.getContentResolver().getType(uri));
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                InputStream inputStream =
                        context.getContentResolver().openInputStream(uri);

                if (inputStream == null) {
                    throw new IOException("Cannot open input stream from URI");
                }

                Source source = Okio.source(inputStream);
                sink.writeAll(source);
                source.close();
            }
        };
    }


    @Nullable
    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex =
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }


    private void getAllTracks(){
        ApiServices apiServices = AppController.getApiServices();
        Call<GetTracksResponse> call = apiServices.getUserTracks();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetTracksResponse> call, Response<GetTracksResponse> response) {
                if (response.isSuccessful()) {
                    GetTracksResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getAudio() != null) {
                            adapter.updateList(body.getAudio());
                            binding.currentMusicPlay.setVisibility(View.VISIBLE);
                        } else {
                            if (body.getError() != null && body.getError().contains("No audio found")) {
                                binding.noTracksTv.setVisibility(View.VISIBLE);
                                binding.currentMusicPlay.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GetTracksResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: add group", t);
                MessagesUtils.showErrorDialog(requireActivity(), ErrorUtils.parseError(t));
            }
        });
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");

        audioPickerLauncher.launch(intent);
    }
}