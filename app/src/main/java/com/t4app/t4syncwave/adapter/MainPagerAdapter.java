package com.t4app.t4syncwave.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.t4app.t4syncwave.ui.main.GroupsFragment;
import com.t4app.t4syncwave.ui.main.LibraryFragment;
import com.t4app.t4syncwave.ui.main.ListenerFragment;
import com.t4app.t4syncwave.ui.main.ProfileFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new GroupsFragment();
            case 1: return new ListenerFragment();
            case 2: return new LibraryFragment();
            case 3: return new ProfileFragment();
            default: return new GroupsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
