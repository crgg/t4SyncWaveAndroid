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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.t4app.t4syncwave.R;
import com.t4app.t4syncwave.SessionManager;
import com.t4app.t4syncwave.adapter.MainPagerAdapter;
import com.t4app.t4syncwave.databinding.ActivityT4SyncWaveMainBinding;
import com.t4app.t4syncwave.ui.auth.LoginActivity;

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

        tabLayout = findViewById(R.id.tabLayout);

        if (savedInstanceState == null) {
            groupsFragment = new GroupsFragment();
            listenerFragment = new ListenerFragment();
            libraryFragment = new LibraryFragment();
            profileFragment = new ProfileFragment();

        }


        for (int i = 0; i < 4; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setCustomView(createCustomTabView(i));
            tabLayout.addTab(tab);
        }

        if (savedInstanceState == null) {
            updateTabIcons(0);
            loadMainFragment(0);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                updateTabIcons(position);
                loadMainFragment(position);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });


    }


    private View createCustomTabView(int position) {
        View customView = LayoutInflater.from(this)
                .inflate(R.layout.custom_tab_item, null);

        TextView tabText = customView.findViewById(R.id.tabText);
        ImageView tabIcon = customView.findViewById(R.id.tabIcon);

        switch (position) {
            case 0:
                tabText.setText("Groups");
                tabIcon.setImageResource(R.drawable.ic_group_outline_24);
                break;
            case 1:
                tabText.setText("Listener");
                tabIcon.setImageResource(R.drawable.ic_listener_outline_24);
                break;
            case 2:
                tabText.setText("Library");
                tabIcon.setImageResource(R.drawable.ic_library_outline_24);
                break;
            case 3:
                tabText.setText("Profile");
                tabIcon.setImageResource(R.drawable.ic_profile_outline_24);
                break;
        }

        return customView;
    }

    private void loadMainFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                switchMainFragment(groupsFragment);
                break;
            case 1:
                switchMainFragment(listenerFragment);
                break;
            case 2:
                switchMainFragment(libraryFragment);
                break;
            case 3:
                switchMainFragment(profileFragment);
                break;
        }

    }


    private void switchMainFragment(Fragment fragment) {
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        if (groupsFragment != null) transaction.hide(groupsFragment);
        if (listenerFragment != null) transaction.hide(listenerFragment);
        if (libraryFragment != null) transaction.hide(libraryFragment);
        if (profileFragment != null) transaction.hide(profileFragment);

        if (!fragment.isAdded()) {
            transaction.add(R.id.main_container, fragment);
        }

        transaction.show(fragment).commit();
    }



    private void updateTabIcons(int selectedPosition) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab == null || tab.getCustomView() == null) continue;

            ImageView icon = tab.getCustomView().findViewById(R.id.tabIcon);

            if (i == selectedPosition) {
                icon.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() ->
                        icon.animate().scaleX(1f).scaleY(1f).setDuration(100)
                );
                icon.setImageResource(ICON_FILLED[i]);
            } else {
                icon.setImageResource(ICON_OUTLINE[i]);
            }

        }
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(groupsFragment)
                .hide(listenerFragment)
                .hide(libraryFragment)
                .hide(profileFragment)
                .add(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit();
    }

}