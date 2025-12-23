package com.t4app.t4syncwave.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.t4app.t4syncwave.AppController;
import com.t4app.t4syncwave.ErrorUtils;
import com.t4app.t4syncwave.MessagesUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.GroupAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.model.AddGroupResponse;
import com.t4app.t4syncwave.conection.model.ResponseGetGroups;
import com.t4app.t4syncwave.databinding.FragmentGroupsBinding;
import com.t4app.t4syncwave.viewmodel.GroupsViewModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsFragment extends Fragment {
    private static final String TAG = "GROUPS_FRAGMENT";

    private FragmentGroupsBinding binding;
    private GroupAdapter adapter;
    private SessionManager sessionManager;

    private GroupsViewModel viewModel;

    private boolean myGroups = true;

    public GroupsFragment() {
    }

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GroupsViewModel.class);

        viewModel.getFilter().observe(getViewLifecycleOwner(), filter -> {
            if (filter == GroupsViewModel.Filter.MY_GROUPS){
                binding.btnFilter.setText(R.string.created);
                myGroups = true;
                getMyGroups();
            }
        });

        adapter = new GroupAdapter(group -> {

            GroupsFragmentDirections.ActionGroupToGroupAdmin action =
                    GroupsFragmentDirections.actionGroupToGroupAdmin(group.getId(), myGroups);

            NavHostFragment.findNavController(this).navigate(action);

        });

        binding.roomsRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.roomsRv.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (myGroups){
                getMyGroups();
            }
        });

        binding.btnAdd.setOnClickListener(v -> MessagesUtils.showAddGroupLayout(requireActivity(), this::addGroup));

    }

    private void addGroup(String groupName){
        ApiServices apiServices = AppController.getApiServices();
        Map<String, Object> data = new HashMap<>();
        data.put("name", groupName);
        Call<AddGroupResponse> call = apiServices.addGroup(data);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AddGroupResponse> call, @NonNull Response<AddGroupResponse> response) {
                if (response.isSuccessful()) {
                    AddGroupResponse body = response.body();
                    if (body != null) {
                        if (body.isStatus() && body.getGroup() != null) {
                            adapter.addGroup(body.getGroup());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddGroupResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: add group", t);
                MessagesUtils.showErrorDialog(requireActivity(), ErrorUtils.parseError(t));
            }
        });
    }

    private void getMyGroups(){
        ApiServices apiServices = AppController.getApiServices();
        Call<ResponseGetGroups> call = apiServices.getGroupsList();
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