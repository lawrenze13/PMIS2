package com.example.pmis;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.pmis.Helpers.InputFilterMax;
import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    public static final int PICK_CAMERA = 2;
    public static final int CAMERA_PERM_CODE = 101;
    private static final String TAG = "EDIT_PROFILE" ;
    private ConstraintLayout  clLoading;
    private EditText etFirstName, etLastName, etAge, etEmail;
    private ProgressBar pbLoading;
    private TextView txtFullName;
    private Button btnUpload;
    private ImageView ivProfilePic;
    private RadioGroup rgSex;
    private RadioButton rMale, rFemale, rSex;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID,fileName;
    private Uri filepath;
    private StorageReference storageReference, viewProfileReference;
    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        txtFullName = (TextView)findViewById(R.id.txtFullName);
        etFirstName = (EditText)findViewById(R.id.etFirstName);
        etLastName = (EditText)findViewById(R.id.etLastName);
        etAge = (EditText)findViewById(R.id.etAge);
        etAge.setFilters(new InputFilter[]{ new InputFilterMax(1,150)});
        etEmail = (EditText)findViewById(R.id.etEmail);
        rgSex = (RadioGroup)findViewById(R.id.rgSex);
        int selectedID = rgSex.getCheckedRadioButtonId();
        rMale = (RadioButton)findViewById(R.id.rMale);
        rFemale = (RadioButton)findViewById(R.id.rFemale);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        clLoading = findViewById(R.id.clLoading);
        pbLoading = findViewById(R.id.pbLoading);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(uploadPhoto);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        myRef = mFirebaseDatabase.getReference().child("Users").child(userID);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                UserInfo uInfo = new UserInfo();
                    String photoUrl = datasnapshot.getValue(UserInfo.class).getPhotoUrl();
                    if(photoUrl != null) {
                        viewProfileReference = FirebaseStorage.getInstance().getReference().child("images/profilePics/" + userID);
                        viewProfileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide
                                        .with(EditProfileActivity.this)
                                        .asBitmap()
                                        .load(uri)
                                        .centerCrop()
                                        .into(ivProfilePic);
                            }
                        });
                    }
                    uInfo.setFirstName(datasnapshot.getValue(UserInfo.class).getFirstName());
                    uInfo.setLastName(datasnapshot.getValue(UserInfo.class).getLastName());
                    uInfo.setAge(datasnapshot.getValue(UserInfo.class).getAge());
                    uInfo.setEmail(datasnapshot.getValue(UserInfo.class).getEmail());
                    uInfo.setSex(datasnapshot.getValue(UserInfo.class).getSex());
                    rgSex.findViewById(R.id.rgSex);
                    etFirstName.setText(uInfo.firstName);
                    etLastName.setText(uInfo.lastName);
                    etEmail.setText(uInfo.email);
                    etAge.setText(uInfo.age);
                    String fullName = uInfo.firstName + " " + uInfo.lastName;
                    txtFullName.setText(fullName);
                    Intent intent = getIntent();
                    String sex = intent.getStringExtra("sex");
                    if(sex != null){
                    if(sex.equals("Male")){
                        rgSex.check(R.id.rMale);
                    }else{
                        rgSex.check(R.id.rFemale);
                    }
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public boolean validateForm(){
        String email = etEmail.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        if(email.isEmpty()){
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please Provide a valid Email");
            etEmail.requestFocus();
            return false;
        }
        if(firstName.isEmpty()){
            etFirstName.setError("First Name is required");
            etFirstName.requestFocus();
            return false;
        }
        if(lastName.isEmpty()){
            etLastName.setError("Last Name is required");
            etLastName.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please Provide a valid Email");
            etEmail.requestFocus();
            return false;
        }
        if(age.isEmpty()){
            etAge.setError("Age is required!");
            etAge.requestFocus();
            return false;
        }
        return true;
    }
    public void cancel(View v){
        finish();
    }
    public void submit(View v){
       // clLoading.setVisibility(View.VISIBLE);
        if(validateForm()){
            if(filepath != null){
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading..");
                progressDialog.show();
                StorageReference ref = storageReference.child("images/profilePics/" + userID);
                ref.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        userID = user.getUid();
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference("Users").child(userID);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setFirstName(etFirstName.getText().toString().trim());
                        userInfo.setLastName(etLastName.getText().toString().trim());
                        userInfo.setAge(etAge.getText().toString().trim());
                        userInfo.setEmail(etEmail.getText().toString().trim());
                        userInfo.setPhotoUrl(userID);
                        int selectedID = rgSex.getCheckedRadioButtonId();
                        rSex = (RadioButton)findViewById(selectedID);
                        userInfo.setSex(rSex.getText().toString().trim());
                        myRef.setValue(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Update success!", Toast.LENGTH_LONG).show();

                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Failed to update. Please Try again!", Toast.LENGTH_LONG).show();

                            }
                        });

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this,"Failed to upload. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference("Users").child(userID);
                UserInfo userInfo = new UserInfo();
                userInfo.setFirstName(etFirstName.getText().toString().trim());
                userInfo.setLastName(etLastName.getText().toString().trim());
                userInfo.setAge(etAge.getText().toString().trim());
                userInfo.setEmail(etEmail.getText().toString().trim());
                userInfo.setPhotoUrl(userID);
                int selectedID = rgSex.getCheckedRadioButtonId();
                rSex = (RadioButton)findViewById(selectedID);
                userInfo.setSex(rSex.getText().toString().trim());
                myRef.setValue(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfileActivity.this, "Update success!", Toast.LENGTH_LONG).show();

                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Failed to update. Please Try again!", Toast.LENGTH_LONG).show();

                    }
                });


            }


        }
    }
    private final View.OnClickListener uploadPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent,PICK_IMAGE);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1) {
             filepath = data.getData();
                File f = new File(String.valueOf(filepath));
                  String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                fileName = f.getName() + timeStamp;

                Log.d(TAG, "FILE PATH: " + fileName);
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Glide
                        .with(EditProfileActivity.this)
                        .asBitmap()
                        .load(bitmap)
                        .centerCrop()
                        .into(ivProfilePic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }
}