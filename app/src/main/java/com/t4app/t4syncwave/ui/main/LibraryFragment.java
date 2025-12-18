package com.t4app.t4syncwave.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.t4app.t4syncwave.model.MusicItem;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {
    private static final String TAG = "LIBRARY_FRAGMENT";

    private FragmentLibraryBinding binding;
    private MusicAdapter adapter;

    public LibraryFragment() {
    }

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
        });

        binding.musicListRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.musicListRv.setAdapter(adapter);

        getAllTracks();

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
}