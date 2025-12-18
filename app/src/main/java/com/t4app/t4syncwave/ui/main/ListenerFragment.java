package com.t4app.t4syncwave.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.t4app.t4syncwave.R;


public class ListenerFragment extends Fragment {

    public ListenerFragment() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.fragment_listener, container, false);
    }
}