package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class ProfileInformationActivity extends AppCompatActivity {
    private TextView editFirstName, editLastName, editEmail, editSex, editAge, editClinicName, editAddress, editContactNumber;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);
        ivProfilePic =  findViewById(R.id.ivProfilePic3);
        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(editProfile);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        editFirstName = (TextView) findViewById(R.id.editFirstName);
        editLastName = (TextView) findViewById(R.id.editLastName);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar2);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Profile Information");
        myToolbar.setTitleTextColor(getColor(R.color.white));
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        editEmail = (TextView) findViewById(R.id.editEmail);
        editSex = (TextView) findViewById(R.id.editSex);
        editAge = (TextView) findViewById(R.id.editAge);

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
                                    .with(ProfileInformationActivity.this)
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private final View.OnClickListener editProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProfileInformationActivity.this,EditProfileActivity.class);
            String sex = editSex.getText().toString().trim();
            intent.putExtra("sex",sex);
            startActivity(intent);
        }
    };

}