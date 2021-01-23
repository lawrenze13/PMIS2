package com.example.pmis;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pmis.Adapter.PaymentPageAdapter;
import com.example.pmis.Adapter.ReportPageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ReportBetaFragment extends Fragment {
    private static final String TAG = "PATIENT_PAYMENT";
    private View view;
    private TabLayout tabLayout;
    private ViewPager2 vpReport;
    private TabItem tabPayments, tabAppointments, tabProcedures, tabClinic;
    private String patientKey;
    private FloatingActionButton fabAddPayment;
    public PaymentPageAdapter paymentPageAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference fullRef,insRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_report_beta, container, false);
        return view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabPayments = view.findViewById(R.id.tabPayments);
        tabAppointments = view.findViewById(R.id.tabAppointment);
        tabProcedures = view.findViewById(R.id.tabProcedures);
        tabClinic = view.findViewById(R.id.tabClinic);
        vpReport = view.findViewById(R.id.vpReport);
        vpReport.setAdapter(new ReportPageAdapter(getActivity()));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabLayout, vpReport, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("Payment");
                        break;
                    }
                    case 1:{
                        tab.setText("Appointment");
                        break;
                    }
                    case 2:{
                        tab.setText("Procedures");
                        break;
                    }
                    case 3:{
                        tab.setText("Clinic");
                        break;
                    }
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }


}