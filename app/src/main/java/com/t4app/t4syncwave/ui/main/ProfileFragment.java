package com.t4app.t4syncwave.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.databinding.FragmentLibraryBinding;
import com.t4app.t4syncwave.databinding.FragmentProfileBinding;
import com.t4app.t4syncwave.ui.auth.LoginActivity;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    public ProfileFragment() {
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
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
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = SessionManager.getInstance();

        binding.nameUser.setText(sessionManager.getName());
        binding.emailUser.setText(sessionManager.getUserEmail());

        binding.btnSignOut.setOnClickListener(view1 -> {
            sessionManager.clearSession();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            requireActivity().finish();
            startActivity(intent);
        });

    }
}