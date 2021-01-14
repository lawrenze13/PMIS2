package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pmis.Model.Patient;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {
    private static final String TAG ="PATIENT_ACTIVITY";
    public static final int PICK_IMAGE = 1;
    public static final int PICK_CAMERA = 2;
    public static final int CAMERA_PERM_CODE = 101;
    private EditText etPatientBirthdate ,etPFirstName, etPMiddleName, etPLastName, etPEmail, etPContactNo, etPAddress, etPNotes;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Button btnUpload;
    private RadioGroup rgPSex;
    private RadioButton rSex;
    private ImageView ivPatientPic;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference, viewPhotoReference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, editRef;
    private String userID, fileName, action, patientKey;
    private Uri filepath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        etPFirstName = findViewById(R.id.etPFirstName);
        etPMiddleName = findViewById(R.id.etPMiddleName);
        etPLastName = findViewById(R.id.etPLastName);
        etPEmail = findViewById(R.id.etPEmail);
        etPContactNo = findViewById(R.id.etPContactNo);
        etPAddress = findViewById(R.id.etPAddress);
        etPNotes = findViewById(R.id.etPNotes);
        rgPSex = findViewById(R.id.rgPSex);
        etPatientBirthdate = findViewById(R.id.etPBirthDate);
        ivPatientPic = findViewById(R.id.ivPatientPic);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(uploadPhoto);
        Intent intent = getIntent();
        action = intent.getStringExtra("action" );
        Log.d(TAG, "action: " + action);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        if(action.equals("edit")) {
            Log.d(TAG, "action: " + action);
            patientKey = intent.getStringExtra("patientKey");
            Log.d(TAG, "patientKey: " + patientKey);
            editRef = FirebaseDatabase.getInstance().getReference("Patient").child(userID).child(patientKey);
            editRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    etPFirstName.setText(snapshot.getValue(Patient.class).getFirstName());
                    etPMiddleName.setText(snapshot.getValue(Patient.class).getMiddleName());
                    etPLastName.setText(snapshot.getValue(Patient.class).getLastName());
                    etPEmail.setText(snapshot.getValue(Patient.class).getEmail());
                    etPContactNo.setText(snapshot.getValue(Patient.class).getContactNo());
                    etPAddress.setText(snapshot.getValue(Patient.class).getAddress());
                    etPNotes.setText(snapshot.getValue(Patient.class).getNotes());
                    etPatientBirthdate.setText(snapshot.getValue(Patient.class).getBirthDate());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Patient patient = new Patient();

                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference("Patient").child(userID);
                String key;
                if(action.equals("edit")){
                    key = patientKey;
                }else {
                    key = myRef.push().getKey();
                }
                if(validate()) {
                        if (filepath != null) {
                            ProgressDialog progressDialog = new ProgressDialog(AddPatientActivity.this);
                            progressDialog.setTitle("Uploading..");
                            progressDialog.show();
                            StorageReference ref = storageReference.child("images/patientPic/" + key);
                            ref.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    int selectedID = rgPSex.getCheckedRadioButtonId();
                                    rSex = (RadioButton) findViewById(selectedID);
                                    patient.setSex(rSex.getText().toString());
                                    patient.setFirstName(etPFirstName.getText().toString().trim());
                                    patient.setMiddleName(etPMiddleName.getText().toString().trim());
                                    patient.setLastName(etPLastName.getText().toString().trim());
                                    patient.setEmail(etPEmail.getText().toString().trim());
                                    patient.setContactNo(etPContactNo.getText().toString().trim());
                                    patient.setSorter(etPFirstName.getText().toString().toLowerCase().trim() + ' ' + etPLastName.getText().toString().toLowerCase().trim());
                                    patient.setAddress(etPAddress.getText().toString().trim());
                                    patient.setNotes(etPNotes.getText().toString().trim());
                                    patient.setBirthDate(etPatientBirthdate.getText().toString().trim());
                                    String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                                    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                    String dateUpdated = currentDate + ' ' + currentTime;
                                    patient.setDateAdded(dateUpdated);
                                    patient.setKey(key);
                                    myRef.child(key).setValue(patient).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            String success;
                                            if(action.equals("edit")){
                                                success = "Patient Edited Succesfully ";
                                            }else {
                                                Log.d(TAG, "submitaction: " + action);
                                                success = "Patient Added Succesfully ";
                                            }
                                            Toast.makeText(AddPatientActivity.this, success, Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddPatientActivity.this, "Failed to add Patient. Please Try Again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    progressDialog.dismiss();
                                }


                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddPatientActivity.this, "Failed to upload. Please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(AddPatientActivity.this, "Upload Photo first", Toast.LENGTH_LONG).show();
                        }
                    }

                

            }
        });

        etPatientBirthdate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialog = new DatePickerDialog(AddPatientActivity.this,
                            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                            mDateSetListener,
                            year,month,day);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    etPatientBirthdate.clearFocus();
                }
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker datePicker, int year , int month, int day){
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/d/yy: " + month + '/' + day + '/' + year);
                String smonth = String.valueOf(month);
                String syear = String.valueOf(year);
                String sday = String.valueOf(day);
                String date = smonth + '/' + sday + '/' + syear;
                etPatientBirthdate.setText(date);
            }
        };
    }
    private boolean validate(){
        String firstName = etPFirstName.getText().toString().trim();
        String middleName = etPMiddleName.getText().toString().trim();
        String lastName = etPLastName.getText().toString().trim();
        String email = etPEmail.getText().toString().trim();
        String contactNo = etPContactNo.getText().toString().trim();
        String address = etPAddress.getText().toString().trim();
        String notes = etPNotes.getText().toString().trim();
        String birthDate = etPatientBirthdate.getText().toString().trim();
        if(firstName.equals("")){
            etPFirstName.setError("First Name is required.");
            etPFirstName.requestFocus();
            return false;
        }
//        if(middleName.equals("")){
//            etPMiddleName.setError("Middle Name is required.");
//            etPMiddleName.requestFocus();
//            return false;
//        }
        if(lastName.equals("")){
            etPLastName.setError("Last Name is required.");
            etPLastName.requestFocus();
            return false;
        }
        if(email.equals("")){
            etPEmail.setError("email is required.");
            etPEmail.requestFocus();
            return false;
        }
        if(contactNo.equals("")){
            etPContactNo.setError("Contact No. is required.");
            etPContactNo.requestFocus();
            return false;
        }
        if(address.equals("")){
            etPAddress.setError("Address is required.");
            etPAddress.requestFocus();
            return false;
        }
//        if(notes.equals("")){
//            etPNotes.setError("Notes is required.");
//            etPNotes.requestFocus();
//            return false;
//        }
        if(birthDate.equals("")){
            etPatientBirthdate.setError("Contact No. is required.");
            etPatientBirthdate.requestFocus();
            return false;
        }
        return true;
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
                        .with(AddPatientActivity.this)
                        .asBitmap()
                        .load(bitmap)
                        .centerCrop()
                        .into(ivPatientPic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

}