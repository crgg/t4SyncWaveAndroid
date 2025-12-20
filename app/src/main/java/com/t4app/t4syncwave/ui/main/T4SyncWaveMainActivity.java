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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.MainPagerAdapter;
import com.t4app.t4syncwave.databinding.ActivityT4SyncWaveMainBinding;
import com.t4app.t4syncwave.ui.auth.LoginActivity;

import java.util.Objects;

public class T4SyncWaveMainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACT";

    private Fragment groupsFragment;
    private Fragment listenerFragment;
    private Fragment libraryFragment;
    private Fragment profileFragment;


    private ActivityT4SyncWaveMainBinding binding;
    private TabLayout tabLayout;
    private final int[] ICON_OUTLINE = {
            R.drawable.ic_group_outline_24,
            R.drawable.ic_listener_outline_24,
            R.drawable.ic_library_outline_24,
            R.drawable.ic_profile_outline_24
    };

    private final int[] ICON_FILLED = {
            R.drawable.ic_group_fill_24,
            R.drawable.ic_listener_24,
            R.drawable.ic_library_fill_24,
            R.drawable.ic_profile_fill_24
    };


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

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);


    }

}