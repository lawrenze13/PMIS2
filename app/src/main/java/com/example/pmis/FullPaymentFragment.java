package com.example.pmis;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pmis.Adapter.PatientFullPaymentAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.MedicalHistory;
import com.example.pmis.Model.PatientPayment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FullPaymentFragment extends Fragment {
    private static DecimalFormat df = new DecimalFormat("#.##");
    private static final String TAG = "FULLPAYMENT_FRAGMENT";
    private View view;
    private TextView tvPayRevenue, tvPayBalance;
    private RecyclerView rvFullPayment;
    private String patientKey;
    private List<PatientPayment> patientPaymentList;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private LoggedUserData loggedUserData = new LoggedUserData();
    public FullPaymentFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_full_payment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userID = loggedUserData.userID();
        Intent intent = getActivity().getIntent();
        patientPaymentList = new ArrayList<>();
        patientKey = intent.getStringExtra("patientKey");
        Log.d(TAG, "patient key: "+ patientKey);
        tvPayRevenue = view.findViewById(R.id.tvPayRevenue);
        tvPayBalance = view.findViewById(R.id.tvPayBalance);
        rvFullPayment = view.findViewById(R.id.rvFullPayment);
        rvFullPayment.setLayoutManager(new LinearLayoutManager(getContext()));
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("FULL PAYMENT").child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientPaymentList.clear();
                double totalRevenue = 0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    PatientPayment patientPayment = ds.getValue(PatientPayment.class);
                    patientPaymentList.add(patientPayment);
                    double total = Double.parseDouble(ds.getValue(PatientPayment.class).getTotal());
                    totalRevenue = totalRevenue + total;

                }
                Log.d(TAG, "totalAmount:" + totalRevenue);

                PatientFullPaymentAdapter patientFullPaymentAdapter = new PatientFullPaymentAdapter(getContext(),patientPaymentList, patientKey);
                rvFullPayment.setAdapter(patientFullPaymentAdapter);
                patientFullPaymentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}