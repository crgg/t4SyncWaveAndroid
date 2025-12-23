package com.t4app.t4syncwave.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.t4app.t4syncwave.ListenersUtils;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.MainPagerAdapter;
import com.t4app.t4syncwave.databinding.ActivityT4SyncWaveMainBinding;
import com.t4app.t4syncwave.ui.GlobalPlayerView;
import com.t4app.t4syncwave.ui.auth.LoginActivity;
import com.t4app.t4syncwave.viewmodel.GlobalMusicViewModel;

import java.util.Objects;

public class T4SyncWaveMainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACT";
    private ActivityT4SyncWaveMainBinding binding;

    private GlobalPlayerView globalPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityT4SyncWaveMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.getRememberMe()){
            Intent intent = new Intent(T4SyncWaveMainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        globalPlayerView = binding.globalAudioPlayer;

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        globalPlayerView.findViewById(R.id.containerMusic).setOnClickListener(view -> {
            navController.navigate(R.id.musicPlayerFragment);
        });

        GlobalMusicViewModel viewModel = new ViewModelProvider(this).get(GlobalMusicViewModel.class);

        globalPlayerView.setPlaybackActionListener(new ListenersUtils.PlaybackActionListener() {
            @Override
            public void onPlayRequested() {
                viewModel.play();
            }

            @Override
            public void onPauseRequested() {
                viewModel.pause();
            }

            @Override
            public void onChangeSeek(int progress) {

            }
        });

    }

}