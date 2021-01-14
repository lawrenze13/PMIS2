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

import com.example.pmis.Adapter.PatientListAdapter;
import com.example.pmis.Model.Patient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PatientFragment extends Fragment {
    private static final String TAG = "PATIENT_FRAGMENT: " ;
    private FloatingActionButton fabAddDrug2;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private RecyclerView rvPatient;
    private PatientListAdapter patientListAdapter;
    private List<Patient> patientList;
    private PatientViewModel mViewModel;
    private FloatingActionButton fabAddPatient;
    private View view;
    public static PatientFragment newInstance() {
        return new PatientFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.patient_fragment, container, false);
        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fabAddDrug2 = (FloatingActionButton)view.findViewById(R.id.fabAddDrug2);
        fabAddDrug2.setOnClickListener(addPatient);
        rvPatient = (RecyclerView)view.findViewById(R.id.rvPatient);
        rvPatient.setLayoutManager(new LinearLayoutManager(getContext()));
        patientList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Patient patient = ds.getValue(Patient.class);
                    patientList.add(patient);
                    Log.d(TAG, String.valueOf(patient));

                }
                patientListAdapter = new PatientListAdapter(getContext(),patientList);
                rvPatient.setAdapter(patientListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public final View.OnClickListener addPatient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), AddPatientActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
    };

}