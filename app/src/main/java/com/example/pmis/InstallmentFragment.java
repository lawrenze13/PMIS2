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
import com.example.pmis.Adapter.PatientInstallmentAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
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

public class InstallmentFragment extends Fragment {
    private static DecimalFormat df = new DecimalFormat("#.##");
    private static final String TAG = "INSTALLMENT_FRAGMENT";
    private View view;
    private RecyclerView rvInstallment;
    private String patientKey;
    private List<PatientPayment> patientPaymentList;
    private List<Installment> installmentList;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent intent = getActivity().getIntent();
        patientPaymentList = new ArrayList<>();
        installmentList = new ArrayList<>();
        patientKey = intent.getStringExtra("patientKey");
        Log.d(TAG, "patient key: "+ patientKey);
        LoggedUserData loggedUserData = new LoggedUserData();
        userID = loggedUserData.userID();
        rvInstallment = view.findViewById(R.id.rvInstallment);
        rvInstallment.setLayoutManager(new LinearLayoutManager(getContext()));
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientPaymentList.clear();
                installmentList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                        PatientPayment patientPayment = ds.getValue(PatientPayment.class);
                        patientPaymentList.add(patientPayment);

                }

                PatientInstallmentAdapter patientInstallmentAdapter = new PatientInstallmentAdapter(getContext(),patientPaymentList, patientKey, snapshot);
                rvInstallment.setAdapter(patientInstallmentAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_installment, container, false);
        return view;
    }
}