package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditClinicActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    public static final int PICK_CAMERA = 2;
    public static final int CAMERA_PERM_CODE = 101;
    private static final String TAG = "EDIT_CLINIC" ;
    private EditText etClinicName, etContactNo, etAddress, etLicense, etDegree;
    private TextView txtClinicName;
    private Button btnUpload2;
    private ImageView ivProfilePic;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference storageReference,viewPhotoReference;
    private FirebaseStorage storage;
    private DatabaseReference myRef;
    private String userID, fileName;
    private  LoggedUserData loggedUserData;
    private Uri filepath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clinic);
         loggedUserData = new LoggedUserData();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        txtClinicName = (TextView)findViewById(R.id.txtClinicName);
        etLicense = (EditText) findViewById(R.id.etLicense);
        etDegree = (EditText) findViewById(R.id.etDegree);
        etClinicName = (EditText)findViewById(R.id.etClinicName);
        etAddress = (EditText)findViewById(R.id.etAddress);
        etContactNo = (EditText)findViewById(R.id.etContactNo);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        btnUpload2 = (Button) findViewById(R.id.btnUpload2);
        btnUpload2.setOnClickListener(uploadPhoto);
        setClinicData();

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
                        .with(EditClinicActivity.this)
                        .asBitmap()
                        .load(bitmap)
                        .centerCrop()
                        .into(ivProfilePic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }
    private void setClinicData() {
        Intent intent = getIntent();
        String clinicName = intent.getStringExtra("clinicName");
        String address = intent.getStringExtra("address");
        String contactNo = intent.getStringExtra("contactNo");
        String license = intent.getStringExtra("license");
        String degree = intent.getStringExtra("degree");
        if (clinicName == "NOT SET"){
            clinicName = " ";
        }
        if (address == "NOT SET"){
            address = " ";
        }
        if (contactNo == "NOT SET"){
            contactNo = " ";
        }
        txtClinicName.setText(clinicName);
        etClinicName.setText(clinicName);
        etAddress.setText(address);
        etContactNo.setText(contactNo);
        etLicense.setText(license);
        etDegree.setText(degree);
        viewPhotoReference = FirebaseStorage.getInstance().getReference().child("images/clinicPic/" + loggedUserData.userID());
        viewPhotoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide
                        .with(EditClinicActivity.this)
                        .asBitmap()
                        .load(uri)
                        .centerCrop()
                        .into(ivProfilePic);
            }
        });
    }

    public boolean validateForm(){
        String clinicName = etClinicName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();
        String license = etLicense.getText().toString().trim();
        String degree = etDegree.getText().toString().trim();
        if(clinicName.isEmpty()){
            etClinicName.setError("Clinic Name is required");
            etClinicName.requestFocus();
            return false;
        }
        if(address.isEmpty()){
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }
        if(contactNo.isEmpty()){
            etContactNo.setError("Contact No. is required");
            etContactNo.requestFocus();
            return false;
        }
        if(license.isEmpty()){
            etLicense.setError("License No. is required");
            etLicense.requestFocus();
            return false;
        }
        if(degree.isEmpty()){
            etDegree.setError("Degree  is required");
            etDegree.requestFocus();
            return false;
        }
        return true;
    }
    public void cancel(View v){
        finish();
    }
    public void submit(View v){
        if(validateForm()){
            if(filepath != null) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading..");
                progressDialog.show();

                StorageReference ref = storageReference.child("images/clinicPic/" + loggedUserData.userID());
                ref.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        userID = user.getUid();
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference("Clinic").child(userID);
                        Clinic clinic = new Clinic();
                        clinic.setClinicName(etClinicName.getText().toString().trim());
                        clinic.setAddress(etAddress.getText().toString().trim());
                        clinic.setContactNo(etContactNo.getText().toString().trim());
                        clinic.setDegree(etDegree.getText().toString().trim());
                        clinic.setLicense(etLicense.getText().toString().trim());
                        myRef.setValue(clinic).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditClinicActivity.this, "Update success!", Toast.LENGTH_LONG).show();
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditClinicActivity.this, "Failed to update. Please Try again!", Toast.LENGTH_LONG).show();

                            }
                        });

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditClinicActivity.this, "Failed to upload. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference("Clinic").child(userID);
                Clinic clinic = new Clinic();
                clinic.setClinicName(etClinicName.getText().toString().trim());
                clinic.setAddress(etAddress.getText().toString().trim());
                clinic.setContactNo(etContactNo.getText().toString().trim());
                clinic.setDegree(etDegree.getText().toString().trim());
                clinic.setLicense(etLicense.getText().toString().trim());
                myRef.setValue(clinic).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditClinicActivity.this, "Update success!", Toast.LENGTH_LONG).show();
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditClinicActivity.this, "Failed to update. Please Try again!", Toast.LENGTH_LONG).show();

                    }
                });
            }


        }
    }
}