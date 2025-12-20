package com.t4app.t4syncwave.ui.room;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.adapter.MusicAdapter;
import com.t4app.t4syncwave.model.MusicItem;

public class SelectSongBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "select_song_result";
    public static final String SONG_KEY = "song";

    private SelectSongViewModel viewModel;
    private MusicAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.bottomsheet_select_song, container, false
        );

        viewModel = new ViewModelProvider(this).get(SelectSongViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.songsRecycler);
        ImageButton btnClose = view.findViewById(R.id.btnClose);

        adapter = new MusicAdapter(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onPlay(MusicItem item, int pos) {

            }

            @Override
            public void onPause(MusicItem item) {

            }

            @Override
            public void onClick(MusicItem item) {
                Bundle result = new Bundle();
                result.putSerializable(SONG_KEY, item);

                getParentFragmentManager()
                        .setFragmentResult(RESULT_KEY, result);

                dismiss();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        observeData();

        return view;
    }

    private void observeData() {
        viewModel.getSongs().observe(this, songs -> {
            adapter.updateList(songs);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel.getSongs().getValue() == null) {
            viewModel.getAllTracks();
        }
    }
}

