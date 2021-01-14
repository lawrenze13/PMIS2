package com.example.pmis;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.UserInfo;
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

public class ProfileFragment extends Fragment {
    private TextView editFirstName, editLastName, editEmail, editSex, editAge, editClinicName, editAddress, editContactNumber, tvDocName,tvDegree, tvLicense;
    private ProfileViewModel mViewModel;
    private Button btnEditClinic, btnEditProfile;
    private View view;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference userRef, clinicRef;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private String userID;
    private ImageView ivProfilePic;
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.profile_fragment, container, false);
        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ivProfilePic =  view.findViewById(R.id.ivProfilePic);
        btnEditClinic = (Button) view.findViewById(R.id.btnEditClinic);
        btnEditClinic.setOnClickListener(editClinic);
        btnEditProfile = (Button) view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(editProfile);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        editFirstName = (TextView) view.findViewById(R.id.editFirstName);
        editLastName = (TextView) view.findViewById(R.id.editLastName);
        tvLicense = (TextView) view.findViewById(R.id.tvLicense);
        tvDegree = (TextView) view.findViewById(R.id.tvDegree);
        editEmail = (TextView) view.findViewById(R.id.editEmail);
        editSex = (TextView) view.findViewById(R.id.editSex);
        editAge = (TextView) view.findViewById(R.id.editAge);
        tvDocName = (TextView) view.findViewById(R.id.tvDocName);
        editClinicName = (TextView) view.findViewById(R.id.editClinicName);
        editContactNumber = (TextView) view.findViewById(R.id.editContactNumber);
        editAddress = (TextView) view.findViewById(R.id.editAddress);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userRef = mFirebaseDatabase.getReference().child("Users").child(userID);
        clinicRef = mFirebaseDatabase.getReference().child("Clinic").child(userID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                UserInfo uInfo = new UserInfo();
                String photoUrl = datasnapshot.getValue(UserInfo.class).getPhotoUrl();
                if(photoUrl != null) {
                    storageReference = FirebaseStorage.getInstance().getReference().child("images/profilePics/" + userID);
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide
                                    .with(view)
                                    .asBitmap()
                                    .load(uri)
                                    .centerCrop()
                                    .into(ivProfilePic);
                        }
                    });
                }
                        uInfo.setFirstName(datasnapshot.child("firstName").getValue(String.class));
                        uInfo.setLastName(datasnapshot.child("lastName").getValue(String.class));
                        uInfo.setAge(datasnapshot.child("age").getValue(String.class));
                        uInfo.setEmail(datasnapshot.child("email").getValue(String.class));
                        uInfo.setSex(datasnapshot.child("sex").getValue(String.class));
                        editFirstName.setText(uInfo.firstName);
                        editLastName.setText(uInfo.lastName);
                        editEmail.setText(uInfo.email);
                        editSex.setText(uInfo.sex);
                        editAge.setText(uInfo.age);
                        String docName = "Dr. " + uInfo.firstName + ' ' + uInfo.lastName + " D.M.D";
                        tvDocName.setText(docName);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        clinicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                Clinic clinic = new Clinic();
                    if(datasnapshot.exists()) {
                        clinic.setClinicName(datasnapshot.getValue(Clinic.class).getClinicName());
                        clinic.setAddress(datasnapshot.getValue(Clinic.class).getAddress());
                        clinic.setContactNo(datasnapshot.getValue(Clinic.class).getContactNo());
                        clinic.setLicense(datasnapshot.getValue(Clinic.class).getLicense());
                        clinic.setDegree(datasnapshot.getValue(Clinic.class).getDegree());
                        editClinicName.setText(clinic.clinicName);
                        editContactNumber.setText(clinic.contactNo);
                        editAddress.setText(clinic.address);
                        tvDegree.setText(clinic.getDegree());
                        tvLicense.setText(clinic.getLicense());
                        clinic.setPhotoUrl(datasnapshot.getValue(Clinic.class).getPhotoUrl());
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private final View.OnClickListener editClinic = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String clinicName = editClinicName.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String contactNo = editContactNumber.getText().toString().trim();
            Intent intent = new Intent( getContext(),EditClinicActivity.class);

            intent.putExtra("clinicName",clinicName);
            intent.putExtra("address",address);
            intent.putExtra("contactNo",contactNo);
            startActivity(intent);
        }
    };
    private final View.OnClickListener editProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent( getContext(),EditProfileActivity.class);
            String sex = editSex.getText().toString().trim();
            intent.putExtra("sex",sex);
            startActivity(intent);
        }
    };

}