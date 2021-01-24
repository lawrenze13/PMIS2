package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Drugs;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.UserInfo;
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

public class PatientPrescriptionActivity extends AppCompatActivity {
    private static final String TAG = "PERSONALINFO: ";
    private FloatingActionButton fabAddPrescription;
    private RecyclerView rvPrescriptionList;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private String userID, patientKey, docName;
    private DatabaseReference userRef, presRef, docRef;
    private TextView tvPatientFullName, tvRecordCount;
    private List<DrugPrescriptionMain> drugPrescriptionMainList;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescription);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar14);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Prescription List");
        myToolbar.setTitleTextColor(getColor(R.color.white));
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        Intent intent = getIntent();
        tvRecordCount = findViewById(R.id.tvRecordCount);
        rvPrescriptionList = findViewById(R.id.rvPrescriptionList);
        rvPrescriptionList.setLayoutManager(new LinearLayoutManager(this));
        patientKey = intent.getStringExtra("key");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        drugPrescriptionMainList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        userRef.addValueEventListener(setPatientInfo);
        fabAddPrescription = findViewById(R.id.fabAddPrescription);
        fabAddPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientPrescriptionActivity.this, AddPrescriptionActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("action", "add");
                startActivity(intent);
            }
        });
        docRef = mFirebaseDatabase.getReference("Users").child(userID);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo userInfo = new UserInfo();
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                docName = "Dr. " +  firstName + ' ' + lastName + " D.M.D";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        presRef = mFirebaseDatabase.getReference("Prescription").child(patientKey);
        presRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drugPrescriptionMainList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    DrugPrescriptionMain drugPrescriptionMain = ds.getValue(DrugPrescriptionMain.class);
                    drugPrescriptionMainList.add(drugPrescriptionMain);
                }
                tvRecordCount.setText(drugPrescriptionMainList.size() + " prescription(s) found.");
                PatientPrescriptionAdapter patientPrescriptionAdapter = new PatientPrescriptionAdapter(PatientPrescriptionActivity.this, drugPrescriptionMainList, docName, patientKey);
                rvPrescriptionList.setAdapter(patientPrescriptionAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
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
                            .with(PatientPrescriptionActivity.this)
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