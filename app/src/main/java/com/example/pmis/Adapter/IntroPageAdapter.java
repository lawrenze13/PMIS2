package com.example.pmis.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pmis.FullPaymentFragment;
import com.example.pmis.InstallmentFragment;
import com.example.pmis.Intro1Fragment;
import com.example.pmis.Intro2Fragment;
import com.example.pmis.Intro3Fragment;
import com.example.pmis.Intro4Fragment;
import com.example.pmis.Intro5Fragment;

public class IntroPageAdapter  extends FragmentStateAdapter {

    public IntroPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new Intro1Fragment();
                case 1:
                return new Intro2Fragment();
                case 2:
                return new Intro3Fragment();
                case 3:
                return new Intro4Fragment();

            default:
                return new Intro5Fragment();

        }
    }


    @Override
    public int getItemCount() {
        return 5;
    }
}



