package com.t4app.t4syncwave.ui.room;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.adapter.MemberAdapter;
import com.t4app.t4syncwave.conection.ApiServices;
import com.t4app.t4syncwave.conection.RetrofitClient;
import com.t4app.t4syncwave.conection.model.GetGroupByIdResponse;
import com.t4app.t4syncwave.databinding.FragmentGroupAdminBinding;
import com.t4app.t4syncwave.model.Group;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupAdminFragment extends Fragment {

    private static final String TAG = "GROUP_ADMIN_FRAG";
    private static final String ARG_GROUP = "group";

    private FragmentGroupAdminBinding binding;
    private MemberAdapter adapter;

    private String groupSelected;

    public static GroupAdminFragment newInstance(String groupSelected) {
        GroupAdminFragment fragment = new GroupAdminFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP, groupSelected);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupAdminFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupSelected = getArguments().getString(ARG_GROUP);
        }
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


        binding.membersRv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.membersRv.setAdapter(adapter);

        binding.btnBack.setOnClickListener(view1 ->{
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        if (groupSelected != null){
            getGroupById(group -> {
                binding.groupName.setText(group.getName());

                if (group.getCurrentTrack() != null){
                    binding.containerNoMusic.setVisibility(View.GONE);
                }

                if (group.getMembers() != null && !group.getMembers().isEmpty()){
                    adapter.setMembers(group.getMembers());
                }

//                binding.codeGroupValue.setText(group.get);
            });
        }
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