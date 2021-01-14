package com.example.pmis.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pmis.FullPaymentFragment;
import com.example.pmis.InstallmentFragment;

public class PaymentPageAdapter extends FragmentStateAdapter {

    public PaymentPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new FullPaymentFragment();
            default:
                return  new InstallmentFragment();

        }
    }


    @Override
    public int getItemCount() {
        return 2;
    }
}
