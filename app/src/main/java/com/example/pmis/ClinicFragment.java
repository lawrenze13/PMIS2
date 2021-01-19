package com.example.pmis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pmis.Model.Clinic;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ClinicFragment extends Fragment {
    private View view;
    private CardView cvDrugList, cvClinicInfo, cvPatientList,cvProcedures;
    private TextView editClinicName, editAddress, editContactNumber, tvDocName,tvDegree, tvLicense;
    private ImageView imageView11;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference, viewPhotoReference;
    private DatabaseReference myRef;
    private String userID;


    public ClinicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_clinic, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cvProcedures = (CardView)view.findViewById(R.id.cvProcedures);
        cvProcedures.setOnClickListener(procedureList);
        cvDrugList = (CardView)view.findViewById(R.id.cvDrugList);
        cvDrugList.setOnClickListener(drugList);
        editClinicName = (TextView) view.findViewById(R.id.editClinicName3);
        editContactNumber = (TextView) view.findViewById(R.id.editContactNumber3);
        editAddress = (TextView) view.findViewById(R.id.editAddress3);
        tvLicense = (TextView) view.findViewById(R.id.tvLicense3);
        tvDegree = (TextView) view.findViewById(R.id.tvDegree3);
        tvDocName = (TextView) view.findViewById(R.id.tvDocName3);
        imageView11 = view.findViewById(R.id.imageView11);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    editClinicName.setText(snapshot.getValue(Clinic.class).getClinicName());
                    editAddress.setText(snapshot.getValue(Clinic.class).getAddress());
                    editContactNumber.setText(snapshot.getValue(Clinic.class).getContactNo());
                    tvDegree.setText(snapshot.getValue(Clinic.class).getDegree());
                    tvLicense.setText(snapshot.getValue(Clinic.class).getLicense());
                    viewPhotoReference = FirebaseStorage.getInstance().getReference().child("images/clinicPic/" + userID);
                    viewPhotoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide
                                    .with(view)
                                    .asBitmap()
                                    .load(uri)
                                    .centerCrop()
                                    .into(imageView11);
                        }

                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Button btnEditClinic2 = (Button) view.findViewById(R.id.btnEditClinic2);
        btnEditClinic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clinicName = editClinicName.getText().toString().trim();
                String address = editAddress.getText().toString().trim();
                String contactNo = editContactNumber.getText().toString().trim();
                String license = tvLicense.getText().toString().trim();
                String degree = tvDegree.getText().toString().trim();
                Intent intent = new Intent( getContext(),EditClinicActivity.class);

                intent.putExtra("clinicName",clinicName);
                intent.putExtra("address",address);
                intent.putExtra("contactNo",contactNo);
                intent.putExtra("degree",degree);
                intent.putExtra("license",license);
                startActivity(intent);
            }
        });
    }

    public final View.OnClickListener drugList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(),DrugListActivity.class);
            startActivity(intent);
        }
    };
    public final View.OnClickListener procedureList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(),ProceduresActivity.class);
            startActivity(intent);
        }
    };
}