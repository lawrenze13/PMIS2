package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pmis.Adapter.PatientMedicalHistoryAdapter;
import com.example.pmis.Adapter.PatientPhotoAdapter;
import com.example.pmis.Model.MedicalHistory;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPhotos;
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

public class PatientPhotoActivity extends AppCompatActivity {
    private String patientKey, userID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef,userRef;
    private StorageReference mStorageRef;
    private RecyclerView rvMedicalHistory;
    private TextView tvNoInfo, tvPatientFullName;
    private ImageView ivProfile;
    private List<PatientPhotos> patientPhotosList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_photo);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvNoInfo = findViewById(R.id.tvNoInfo);
        ivProfile = findViewById(R.id.ivProfile);
        rvMedicalHistory = findViewById(R.id.rvMedicalHistory);
        rvMedicalHistory.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fabAddPatientPhoto = findViewById(R.id.fabAddPatientPhoto);
        fabAddPatientPhoto.setOnClickListener(addMedicalHistory);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        patientPhotosList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        userRef.addValueEventListener(setPatientInfo);

        myRef = mFirebaseDatabase.getReference("PatientPhotos").child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientPhotosList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    PatientPhotos patientPhotos = ds.getValue(PatientPhotos.class);
                    patientPhotosList.add(patientPhotos);
                }
                int count = patientPhotosList.size();
                String textCount = count + " Patient Photo record(s) found";
                tvNoInfo.setText(textCount);
                PatientPhotoAdapter patientPhotoAdapter = new PatientPhotoAdapter(PatientPhotoActivity.this, patientPhotosList, patientKey);
                rvMedicalHistory.setAdapter(patientPhotoAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private final View.OnClickListener addMedicalHistory = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientPhotoActivity.this, AddPatientPhotoActivity.class);
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
                            .with(PatientPhotoActivity.this)
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