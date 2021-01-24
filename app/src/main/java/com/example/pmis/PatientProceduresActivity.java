package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pmis.Adapter.PatientPrescriptionAdapter;
import com.example.pmis.Adapter.PatientProceduresAdapter;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientProcedures;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PatientProceduresActivity extends AppCompatActivity {
    private static final String TAG = "PATIENT PROCEDURES";
    String patientKey;
    private FloatingActionButton fabAddPatientProcedure;
    private RecyclerView rvPatientProcedure;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    String userID, docName, procedureKey;
    private DatabaseReference procedureRef, presRef, docRef;
    private List<PatientProcedures> patientProceduresList;
    private TextView tvPatientFullName;
    private ImageView ivProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_procedures);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        patientProceduresList = new ArrayList<>();
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        userRef.addValueEventListener(setPatientInfo);
        fabAddPatientProcedure = findViewById(R.id.fabAddPatientProcedure);
        TextView tvNoInfo = findViewById(R.id.tvNoInfo);
        ivProfile = findViewById(R.id.ivProfile);
        fabAddPatientProcedure.setOnClickListener(addPatientProcedure);
        rvPatientProcedure = findViewById(R.id.rvPatientProcedure);
        rvPatientProcedure.setLayoutManager(new LinearLayoutManager(this));

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
                tvNoInfo.setText(patientProceduresList.size() + " Procedure(s) found");
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
            intent.putExtra("action", "add");

            startActivity(intent);

        }
    };
    public ValueEventListener setPatientInfo = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            tvPatientFullName = findViewById(R.id.tvPatientFullName);

            String firstName = snapshot.getValue(Patient.class).getFirstName();
            String middleName = snapshot.getValue(Patient.class).getMiddleName();
            String lastName = snapshot.getValue(Patient.class).getLastName();
            String fullName = firstName + ' ' + middleName + ' ' + lastName;
            tvPatientFullName.setText(fullName);
            StorageReference viewPhotoRef = FirebaseStorage.getInstance().getReference().child("images/patientPic/" + snapshot.getValue(Patient.class).getKey());
            viewPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide
                            .with(PatientProceduresActivity.this)
                            .load(uri)
                            .thumbnail(0.5f)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .centerCrop()
                            .into(ivProfile);
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
}