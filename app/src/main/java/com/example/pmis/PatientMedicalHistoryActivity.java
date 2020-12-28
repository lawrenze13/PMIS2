package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pmis.Adapter.PatientMedicalHistoryAdapter;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.MedicalHistory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PatientMedicalHistoryActivity extends AppCompatActivity {
    private String patientKey, userID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private RecyclerView rvMedicalHistory;
    private List<MedicalHistory> medicalHistoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medical_history);
        rvMedicalHistory = findViewById(R.id.rvMedicalHistory);
        rvMedicalHistory.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fabAddMedicalHistory = findViewById(R.id.fabAddMedicalHistory);
        fabAddMedicalHistory.setOnClickListener(addMedicalHistory);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        medicalHistoryList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("MedicalHistory").child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicalHistoryList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    MedicalHistory medicalHistory = ds.getValue(MedicalHistory.class);
                    medicalHistoryList.add(medicalHistory);
                }
                PatientMedicalHistoryAdapter patientMedicalHistoryAdapter = new PatientMedicalHistoryAdapter(PatientMedicalHistoryActivity.this, medicalHistoryList, patientKey);
                rvMedicalHistory.setAdapter(patientMedicalHistoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
    }

    private final View.OnClickListener addMedicalHistory = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientMedicalHistoryActivity.this, AddMedicalHistoryActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };




}