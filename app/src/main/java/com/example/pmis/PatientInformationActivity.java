package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.pmis.Model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientInformationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private CardView cvPPrescription, cvMedicalHistory, cvProcedures;
    private String userID, patientKey;
    private static final String TAG = "patientKey" ;
    private TextView tvPatientFullName, tvPatientEmail, tvPatientAddress, tvPatientContactNo, tvPatientBirthDate, tvPatientNotes, tvPatientGender, tvPatientAge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_information);
        cvPPrescription = findViewById(R.id.cvPPrescription);
        cvPPrescription.setOnClickListener(viewPrescription);
        cvMedicalHistory = findViewById(R.id.cvMedicalHistory);
        cvMedicalHistory.setOnClickListener(viewMedicalHistory);
        cvProcedures = findViewById(R.id.cvProcedures);
        cvProcedures.setOnClickListener(viewProcedures);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("key");
        mAuth = FirebaseAuth.getInstance();
        ViewFinder();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.getValue(Patient.class).getFirstName();
                String middleName = snapshot.getValue(Patient.class).getMiddleName();
                String lastName = snapshot.getValue(Patient.class).getLastName();
                String fullName = firstName + ' ' + middleName + ' ' + lastName;
                tvPatientAddress.setText(snapshot.getValue(Patient.class).getAddress());
                tvPatientBirthDate.setText(snapshot.getValue(Patient.class).getBirthDate());
                tvPatientContactNo.setText(snapshot.getValue(Patient.class).getContactNo());
                tvPatientEmail.setText(snapshot.getValue(Patient.class).getEmail());
                tvPatientGender.setText(snapshot.getValue(Patient.class).getSex());
                tvPatientNotes.setText(snapshot.getValue(Patient.class).getNotes());
                tvPatientFullName.setText(fullName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private final View.OnClickListener viewPrescription  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientPrescriptionActivity.class);
            intent.putExtra("key", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewMedicalHistory = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientMedicalHistoryActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewProcedures = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientProceduresActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private void ViewFinder() {
        tvPatientAddress = findViewById(R.id.tvPatientAddress);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvPatientBirthDate = findViewById(R.id.tvPatientBirthDate);
        tvPatientContactNo = findViewById(R.id.tvPatientContactNo);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientFullName = findViewById(R.id.tvPatientFullName);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientNotes = findViewById(R.id.tvPatientNotes);
    }
}