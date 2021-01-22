package com.example.pmis.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pmis.FullPaymentFragment;
import com.example.pmis.InstallmentFragment;
import com.example.pmis.ReportAppointmentFragment;
import com.example.pmis.ReportClinicFragment;
import com.example.pmis.ReportPaymentFragment;
import com.example.pmis.ReportProcedureFragment;

public class ReportPageAdapter  extends FragmentStateAdapter {
    public ReportPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ReportPaymentFragment();
            case 1:
                return  new ReportAppointmentFragment();
            case 2:
                return  new ReportProcedureFragment();
            default:
                return new ReportClinicFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
