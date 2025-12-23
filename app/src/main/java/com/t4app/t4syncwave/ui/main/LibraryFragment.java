package com.t4app.t4syncwave.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.adapter.MusicAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.databinding.FragmentLibraryBinding;
import com.t4app.t4syncwave.model.AudioUploadResponse;
import com.t4app.t4syncwave.model.MusicItem;
import com.t4app.t4syncwave.ui.AudioPlayerView;
import com.t4app.t4syncwave.ui.GlobalPlayerView;
import com.t4app.t4syncwave.viewmodel.GlobalMusicViewModel;
import com.t4app.t4syncwave.viewmodel.LibraryViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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

    private AudioPlayerView audioPlayerView;
    private GlobalPlayerView globalPlayerView;

    private LibraryViewModel libraryViewModel;
    private GlobalMusicViewModel globalMusicViewModel;

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

        audioPlayerView = binding.currentMusicPlay;
        globalPlayerView = requireActivity().findViewById(R.id.globalAudioPlayer);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        globalMusicViewModel = new ViewModelProvider(requireActivity()).get(GlobalMusicViewModel.class);

        globalPlayerView.setIamHost(true);
        adapter = new MusicAdapter(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onPlay(MusicItem item, int position) {}

            @Override
            public void onPause(MusicItem item) {
            }

            @Override
            public void onClick(MusicItem item, int position) {
                if (globalPlayerView.getVisibility() != View.VISIBLE){
                    globalPlayerView.setVisibility(View.VISIBLE);
                }

                if (Boolean.TRUE.equals(globalMusicViewModel.isPlaying().getValue())){
                    globalPlayerView.pauseLocal();
                }

                globalMusicViewModel.playSong(item);
                globalPlayerView.setTitle(item.getTitle());
                globalPlayerView.setArtist(item.getArtist());

                globalPlayerView.prepareAudio(item.getFileUrl());
            }
        });

        globalMusicViewModel.getMusicList().observe(getViewLifecycleOwner(), musicItems -> {
            if (musicItems != null && !musicItems.isEmpty()){
                adapter.updateList(musicItems);
            }
        });

        if (globalMusicViewModel.getMusicList().getValue() == null
                || globalMusicViewModel.getMusicList().getValue().isEmpty()) {
            libraryViewModel.getAllTracks();
        }

        if (globalMusicViewModel.getCurrentSong().getValue() != null){
            if (globalMusicViewModel.getCurrentPosition().getValue() != null){
                adapter.compareItem(globalMusicViewModel.getCurrentPosition().getValue(), globalMusicViewModel.getCurrentSong().getValue());
            }
        }

        libraryViewModel.getTracks().observe(getViewLifecycleOwner(), musicItems -> {
            if (musicItems != null && !musicItems.isEmpty()) {
//                adapter.updateList(musicItems);
                globalMusicViewModel.setMusicList(musicItems);
            }
        });


        binding.musicListRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.musicListRv.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.musicListRv.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(),
                R.drawable.recycler_divider)));
        binding.musicListRv.addItemDecoration(dividerItemDecoration);

        adapter.setClicksEnabled(true);

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

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");

        audioPickerLauncher.launch(intent);
    }
}