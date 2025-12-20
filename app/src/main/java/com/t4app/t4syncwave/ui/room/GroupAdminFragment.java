package com.t4app.t4syncwave.ui.room;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.PermissionUtil;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.MemberAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.RetrofitClient;
import com.t4app.t4syncwave.conection.model.AddMemberResponse;
import com.t4app.t4syncwave.conection.model.GetGroupByIdResponse;
import com.t4app.t4syncwave.databinding.FragmentGroupAdminBinding;
import com.t4app.t4syncwave.events.PlaybackEvent;
import com.t4app.t4syncwave.events.PlaybackViewEvent;
import com.t4app.t4syncwave.model.Group;
import com.t4app.t4syncwave.model.MusicItem;
import com.t4app.t4syncwave.model.PlaybackState;
import com.t4app.t4syncwave.model.Room;
import com.t4app.t4syncwave.model.Track;
import com.t4app.t4syncwave.ui.AudioPlayerView;
import com.t4app.t4syncwave.viewmodel.PlaybackManager;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModel;
import com.t4app.t4syncwave.viewmodel.PlaybackViewModelFactory;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupAdminFragment extends Fragment {

    private static final String TAG = "GROUP_ADMIN_FRAG";
    private static final String ARG_GROUP = "groupId";
    private static final String ARG_OWNER = "isAdmin";

    private FragmentGroupAdminBinding binding;
    private MemberAdapter adapter;
    private SessionManager sessionManager;

    private String groupSelected;
    private Group globalGroup;
    private boolean iAmOwner;

    private PlaybackState state;
    private PlaybackViewModel viewModel;

    private AudioPlayerView audioPlayerView;

    private Track currentTrack;

    private Room room;

    public static GroupAdminFragment newInstance(String groupSelected, boolean owner) {
        GroupAdminFragment fragment = new GroupAdminFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP, groupSelected);
        args.putBoolean(ARG_OWNER, owner);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupAdminFragment() {}

    private ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {

                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
//                        uploadAudio(audioUri);

                    }
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentGroupAdminBinding.inflate(inflater, container, false);
       adapter = new MemberAdapter(requireActivity());
       return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GroupAdminFragmentArgs args = GroupAdminFragmentArgs.fromBundle(getArguments());

        groupSelected = args.getGroupId();
        iAmOwner = args.getIsAdmin();

        audioPlayerView = binding.audioPlayerView;

        room = new Room();

        PlaybackManager playbackManager = new PlaybackManager(requireActivity());
        PermissionUtil permissionUtil = new PermissionUtil(requireActivity());

        PlaybackViewModelFactory factory = new PlaybackViewModelFactory(playbackManager, permissionUtil);

        viewModel = new ViewModelProvider(this, factory)
                .get(PlaybackViewModel.class);

        observeEvents();

        binding.membersRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.membersRv.setAdapter(adapter);

        binding.btnBack.setOnClickListener(view1 ->{
            finish();
        });

        if (iAmOwner){
            binding.addMemberBtn.setVisibility(View.VISIBLE);
            binding.separation.setVisibility(View.VISIBLE);
        }else{
            binding.separation.setVisibility(View.GONE);
            binding.addMemberBtn.setVisibility(View.GONE);
        }

        audioPlayerView.setIamHost(iAmOwner);

        getParentFragmentManager()
                .setFragmentResultListener(
                        SelectSongBottomSheet.RESULT_KEY,
                        getViewLifecycleOwner(),
                        (key, bundle) -> {
                            MusicItem selected = (MusicItem) bundle.getSerializable(SelectSongBottomSheet.SONG_KEY);
                            if (state == null){
                                state = new PlaybackState.Builder(
                                        "playback-state",
                                        room.getRoomName(),
                                        room.getUserName(),
                                        0)
                                        .setPlaying(false)
                                        .setTrackUrl(selected.getFileUrl())
                                        .setPosition((double) 0)
                                        .build();

                                binding.containerNoMusic.setVisibility(View.GONE);
                                binding.audioPlayerView.setVisibility(View.VISIBLE);
                                audioPlayerView.prepareAudio(selected.getFileUrl(), false);

                                viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
                            }
                        }
                );


        audioPlayerView.setPlaybackActionListener(new ListenersUtils.PlaybackActionListener() {
            @Override
            public void onPlayRequested() {
                if (iAmOwner){
                    state = state.copy()
                            .setPlaying(true)
                            .build();
                    viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
                }
            }

            @Override
            public void onPauseRequested() {
                if (iAmOwner){
                    state = state.copy()
                            .setPlaying(false)
                            .build();
                    viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
                }
            }

            @Override
            public void onChangeSeek(int progress) {
                if (iAmOwner){
                    state = state.copy()
                            .setTimestamp(progress)
                            .build();

                    Log.d(TAG, "onChangeSeek: " + state.isPlaying());
                    viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
                }
            }
        });

        binding.btnAddMusic.setOnClickListener(view3 -> {
//            openAudioPicker();
            SelectSongBottomSheet sheet = new SelectSongBottomSheet();

            sheet.show(
                    getParentFragmentManager(),
                    "SelectSongBottomSheet"
            );


        });

        if (groupSelected != null){
            getGroupById(group -> {
                globalGroup = group;


                room.setRoomName(group.getId());
                room.setUserName(sessionManager.getName());
                room.setUserId(sessionManager.getUserId());
                room.setRole(iAmOwner ? "dj" : "member");

                binding.groupName.setText(group.getName());

                viewModel.processInput(new PlaybackViewEvent.Connect(room));

                if (group.getCurrentTrack() != null){
                    binding.containerNoMusic.setVisibility(View.GONE);
                    binding.audioPlayerView.setVisibility(View.VISIBLE);

                    currentTrack = group.getCurrentTrack();
                    audioPlayerView.setTitle(group.getName());
                    audioPlayerView.prepareAudio(currentTrack.getFileUrl(), iAmOwner);

                    if (iAmOwner){
                        state = new PlaybackState.Builder(
                                "playback-state",
                                room.getRoomName(),
                                room.getUserName(),
                                0)
                                .setPlaying(false)
                                .setTrackUrl(currentTrack.getFileUrl())
                                .setPosition((double) 0)
                                .build();
                    }
                }else{
                    binding.containerNoMusic.setVisibility(View.VISIBLE);
                    binding.audioPlayerView.setVisibility(View.GONE);
                }

                if (group.getMembers() != null && !group.getMembers().isEmpty()){
                    adapter.setMembers(group.getMembers());
                }

                binding.codeGroupValue.setText(group.getCode());
            });
        }

        binding.addMemberBtn.setOnClickListener(view2 -> {
            MessagesUtils.showAddMemberLayout(requireActivity(), email -> {
                addMember(email);
            });
        });
    }

    private void observeEvents(){
        viewModel.events.observe(getViewLifecycleOwner(), playbackEvent -> {

            Log.d(TAG, "ENTRY IN OBSERVE EVENTS: ");
            if (playbackEvent instanceof PlaybackEvent.RemoteParticipantEvent){

                handleRemoteParticipantEvent((PlaybackEvent.RemoteParticipantEvent) playbackEvent);

            } else if (playbackEvent instanceof PlaybackEvent.Connected) {

                PlaybackEvent.Connected connected = (PlaybackEvent.Connected) playbackEvent;
                room = connected.getRoom();

            }else if (playbackEvent instanceof PlaybackEvent.UrlChanged) {

                PlaybackEvent.UrlChanged urlChanged = (PlaybackEvent.UrlChanged) playbackEvent;
//                setAudioUrl(urlChanged.getUrl());

            }else if (playbackEvent instanceof PlaybackEvent.Disconnected) {
                finish();
            }else if (playbackEvent instanceof PlaybackEvent.IAmHost) {



            }
        });
    }


    private void handleRemoteParticipantEvent(PlaybackEvent.RemoteParticipantEvent event){
        if (event instanceof PlaybackEvent.RemoteParticipantEvent.UserJoined){
            PlaybackEvent.RemoteParticipantEvent.UserJoined userJoined = (PlaybackEvent.RemoteParticipantEvent.UserJoined) event;

            if (iAmOwner){
                viewModel.processInput(new PlaybackViewEvent.ChangeState(state));
            }
        }else if (event instanceof PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState){
            PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState remoteState =
                    (PlaybackEvent.RemoteParticipantEvent.ChangeRemoteState) event;

            PlaybackState remote = remoteState.getState();

            if (currentTrack == null && remote.getTrackUrl() != null){
                audioPlayerView.prepareAudio(remote.getTrackUrl(), iAmOwner);
                binding.containerNoMusic.setVisibility(View.GONE);
                binding.audioPlayerView.setVisibility(View.VISIBLE);

            }

            if (audioPlayerView.getMediaPlayer() == null || !audioPlayerView.isPrepared()) {
                return;
            }

            if (!iAmOwner) {
                if (state == null) {
                    state = remote;
                } else {
                    state = state.copy()
                            .setPlaying(remote.isPlaying())
                            .setTimestamp(remote.getTimestamp())
                            .setPosition(remote.getPosition())
                            .build();
                }

                audioPlayerView.setProgress((int) state.getTimestamp());
            }

            boolean shouldPlay = state.isPlaying();
            boolean isPlayingNow = audioPlayerView.isPlaying();

            if (shouldPlay != isPlayingNow) {
                if (shouldPlay) {
                    audioPlayerView.startLocal();
                } else {
                    audioPlayerView.pauseLocal();
                }
            }


            Log.d(TAG, "SYNC REMOTE -> playing=" + shouldPlay);


        }
    }


    public void finish(){
        viewModel.processInput(PlaybackViewEvent.Disconnect.INSTANCE);

        NavHostFragment.findNavController(this).popBackStack();
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");

        audioPickerLauncher.launch(intent);
    }

    private void addMember(String userEmail){
        ApiServices apiServices = AppController.getApiServices();

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupSelected);
        data.put("email", userEmail);
        data.put("role", "member");
        Call<AddMemberResponse> call = apiServices.addMember(data);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AddMemberResponse> call, Response<AddMemberResponse> response) {
                if (response.isSuccessful()) {
                    AddMemberResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getMember() != null) {
                            adapter.addMember(body.getMember());
                        } else {
                            if (body.getError() != null) {
                                MessagesUtils.showErrorDialog(requireActivity(), body.getError());
                            } else {
                                MessagesUtils.showErrorDialog(requireActivity(), "Unknow Error in add member");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AddMemberResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: ADD MEMBER" + t.getMessage());
                MessagesUtils.showErrorDialog(requireActivity(), ErrorUtils.parseError(t));
            }
        });
    }



    private void getGroupById(ListenersUtils.OnGetGroupListener listener){
        ApiServices apiServices = AppController.getApiServices();
        Call<GetGroupByIdResponse> call = apiServices.getGroupById(groupSelected);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetGroupByIdResponse> call, Response<GetGroupByIdResponse> response) {
                if (response.isSuccessful()) {
                    GetGroupByIdResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getGroup() != null){
                            listener.onSuccess(body.getGroup());
                        }else {
                            if (body.getError() != null){
                                MessagesUtils.showErrorDialog(requireActivity(), body.getError());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GetGroupByIdResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: GET GROUP" + t.getMessage() );
                MessagesUtils.showErrorDialog(requireActivity(), ErrorUtils.parseError(t));
            }
        });
    }

}