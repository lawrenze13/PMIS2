package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pmis.Adapter.PatientPrescriptionAdapter;
import com.example.pmis.Adapter.PatientProceduresAdapter;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.PatientProcedures;
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

public class PatientProceduresActivity extends AppCompatActivity {
    private static final String TAG = "PATIENT PROCEDURES";
    String patientKey;
    private FloatingActionButton fabAddPatientProcedure;
    private RecyclerView rvPatientProcedure;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    String userID, docName;
    private DatabaseReference procedureRef, presRef, docRef;
    private List<PatientProcedures> patientProceduresList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_procedures);
        patientProceduresList = new ArrayList<>();
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        fabAddPatientProcedure = findViewById(R.id.fabAddPatientProcedure);
        fabAddPatientProcedure.setOnClickListener(addPatientProcedure);
        rvPatientProcedure = findViewById(R.id.rvPatientProcedure);
        rvPatientProcedure.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        procedureRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey);
        procedureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientProceduresList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    PatientProcedures patientProcedures = ds.getValue(PatientProcedures.class);
                    patientProceduresList.add(patientProcedures);
                }

                PatientProceduresAdapter patientProceduresAdapter = new PatientProceduresAdapter(PatientProceduresActivity.this, patientProceduresList, patientKey);
                rvPatientProcedure.setAdapter(patientProceduresAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private final View.OnClickListener addPatientProcedure = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientProceduresActivity.this, AddPatientProcedure.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);

        }
    };
}