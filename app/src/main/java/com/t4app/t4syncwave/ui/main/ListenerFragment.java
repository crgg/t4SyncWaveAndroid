package com.t4app.t4syncwave.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.adapter.GroupAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.ResponseGetGroups;
import com.t4app.t4syncwave.databinding.FragmentListenerBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListenerFragment extends Fragment {
    private static final String TAG = "LISTENING_FRAGMENT";
    private FragmentListenerBinding binding;

    private GroupAdapter adapter;

    public ListenerFragment() {
    }

    public static ListenerFragment newInstance() {
        ListenerFragment fragment = new ListenerFragment();
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
        binding = FragmentListenerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GroupAdapter(group -> {

            GroupsFragmentDirections.ActionGroupToGroupAdmin action =
                    GroupsFragmentDirections.actionGroupToGroupAdmin(group.getId(), false);

            NavHostFragment.findNavController(this).navigate(action);

        });

        binding.roomsRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.roomsRv.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(view1 -> MessagesUtils.showJoinGroupByCode(requireActivity(),
                groupCode -> {
            //TODO:
        }));

        getAllGroups();

    }

    private void getAllGroups(){
        ApiServices apiServices = AppController.getApiServices();
        Call<ResponseGetGroups> call = apiServices.getAllGroupsList();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseGetGroups> call, @NonNull Response<ResponseGetGroups> response) {
                if (response.isSuccessful()) {
                    ResponseGetGroups body = response.body();
                    if (body != null) {
                        if (body.isStatus()){
                            if (body.getGroups() != null && !body.getGroups().isEmpty()){
                                binding.noGroupsTv.setVisibility(View.GONE);
                                binding.roomsRv.setVisibility(View.VISIBLE);
                                adapter.updateList(body.getGroups());
                            }
                        }else {
                            if (body.getError() != null && body.getError().contains("No groups found")){
                                binding.noGroupsTv.setVisibility(View.VISIBLE);
                                binding.roomsRv.setVisibility(View.GONE);
                            }
                        }
                    }
                    binding.swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseGetGroups> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: GET GROUPS" + t.getMessage() );
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }


}