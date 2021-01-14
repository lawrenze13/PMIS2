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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
    private TextView tvClinicName, tvClinicAddress;
    private ImageView imageView11;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference, viewPhotoReference;
    private DatabaseReference myRef;
    private String userID;
    private String mParam1;
    private String mParam2;

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
        cvClinicInfo = (CardView)view.findViewById(R.id.cvClinicInfo);
        cvClinicInfo.setOnClickListener(clinicInfo);
        cvProcedures = (CardView)view.findViewById(R.id.cvProcedures);
        cvProcedures.setOnClickListener(procedureList);
        cvDrugList = (CardView)view.findViewById(R.id.cvDrugList);
        cvDrugList.setOnClickListener(drugList);
        tvClinicName = view.findViewById(R.id.tvClinicName);
        imageView11 = view.findViewById(R.id.imageView11);
        tvClinicAddress = view.findViewById(R.id.tvClinicAddress);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvClinicName.setText(snapshot.child("clinicName").getValue(String.class));
                tvClinicAddress.setText(snapshot.child("address").getValue(String.class));
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cvPatientList = view.findViewById(R.id.cvPatientList);
        cvPatientList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),PatientActivity.class);
                startActivity(intent);
            }
        });
    }
    public final View.OnClickListener clinicInfo = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Navigation.findNavController(view).navigate(R.id.profileFragment);
        }
    };
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