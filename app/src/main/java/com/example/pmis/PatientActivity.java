package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

public class PatientActivity extends AppCompatActivity {
    private static final String TAG = "FIREBASE: " ;
    private FloatingActionButton fabAddDrug2;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private RecyclerView rvPatient;
    private PatientListAdapter patientListAdapter;
    private List<Patient> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        fabAddDrug2 = (FloatingActionButton)findViewById(R.id.fabAddDrug2);
        fabAddDrug2.setOnClickListener(addPatient);
        rvPatient = (RecyclerView)findViewById(R.id.rvPatient);
        rvPatient.setLayoutManager(new LinearLayoutManager(this));
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
                patientListAdapter = new PatientListAdapter(PatientActivity.this,patientList);
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
            Intent intent = new Intent(PatientActivity.this, AddPatientActivity.class);
            startActivity(intent);
        }
    };
}