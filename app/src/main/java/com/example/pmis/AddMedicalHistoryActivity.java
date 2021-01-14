package com.example.pmis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.pmis.Model.MedicalHistory;
import com.example.pmis.Model.Patient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddMedicalHistoryActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    private static final String TAG = "UPLOADING";
    private Button btnGallery,btnCamera,btnSave;
    public static final int PICK_IMAGE = 1;
    public static final int PICK_CAMERA = 2;
    private StorageReference mStorageRef;
    private boolean isPhotoEdited = false;
    String currentPhotoPath, patientKey,fileName;
    Uri contentUri;
    private EditText etCaption;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, editRef;
    private String userID, imageName, action, medicalHistoryKey, imageUrl;
    private Uri filepath;
    private ProgressBar pbUpload;
    private TextView tvUpload, textView20;
    private ImageView ivMedicalHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medical_history);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
         pbUpload = findViewById(R.id.pbUpload);
        textView20 = findViewById(R.id.textView20);
        etCaption = findViewById(R.id.etMedicalCaption);
         tvUpload = findViewById(R.id.tvUpload);
         ivMedicalHistory = findViewById(R.id.ivMedicalHistory);
        pbUpload.setVisibility(View.GONE);
        tvUpload.setVisibility(View.GONE);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        action = intent.getStringExtra("action");
        Log.d(TAG, "action: " + action);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(getFromGallery);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveMedicalHistory);
        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCameraPermission();
            }
        });

        if(action.equals("edit")){
            textView20.setText("Edit Medical History");
             medicalHistoryKey = intent.getStringExtra("medicalHistoryKey");
            Log.d(TAG,medicalHistoryKey);
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            editRef = mFirebaseDatabase.getReference("MedicalHistory").child(patientKey).child(medicalHistoryKey);
            editRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    MedicalHistory medicalHistory = new MedicalHistory();
                     imageUrl = snapshot.getValue(MedicalHistory.class).getImageUrl();
                    mStorageRef  = FirebaseStorage.getInstance().getReference().child("images/medicalHistory/" + patientKey + '/' + imageUrl);
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide
                                    .with(AddMedicalHistoryActivity.this)
                                    .asBitmap()
                                    .load(uri)
                                    .centerCrop()
                                    .into(ivMedicalHistory);
                        }
                    });
                    etCaption.setText(snapshot.getValue(MedicalHistory.class).getCaption());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getCameraPermission() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else{
            openCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       if(requestCode == CAMERA_PERM_CODE){
           if(grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               openCamera();
           }else{
               Toast.makeText(this, "Camera Permission is required to Use Camera", Toast.LENGTH_SHORT).show();

           }
       }

    }

    private void openCamera() {
        dispatchTakePictureIntent();

    }

    private final View.OnClickListener getFromGallery = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
            try {
                isPhotoEdited = true;
                contentUri = data.getData();
                File f = new File(String.valueOf(contentUri));
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                fileName = f.getName() + timeStamp;
                imageName =  f.getName()+ timeStamp;;
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivMedicalHistory.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        if(resultCode ==RESULT_OK && requestCode == PICK_CAMERA){
           File f = new File(currentPhotoPath);
           Log.d(TAG, currentPhotoPath);
           ivMedicalHistory.setImageURI(Uri.fromFile(f));

           Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
           contentUri = Uri.fromFile(f);
           mediaScanIntent.setData(contentUri);
           this.sendBroadcast(mediaScanIntent);
            imageName = f.getName();

        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp +"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return  image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pmis.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PICK_CAMERA);
            }
        }
    }

    private final View.OnClickListener saveMedicalHistory = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnSave.setEnabled(false);
            pbUpload.setVisibility(View.VISIBLE);
            tvUpload.setVisibility(View.VISIBLE);
            MedicalHistory medicalHistory = new MedicalHistory();
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            userID = user.getUid();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference("MedicalHistory").child(patientKey);
            String key = myRef.push().getKey();

                StorageReference image = mStorageRef.child("images/medicalHistory/" + patientKey + "/" + imageName);
                image.putFile(contentUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pbUpload.setProgress((int) progress);
                        tvUpload.setText(progress + " %");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        pbUpload.setProgress(0);
                        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        String dateUpdated = currentDate + ' ' + currentTime;
                        medicalHistory.setDate(dateUpdated);
                        medicalHistory.setKey(key);
                        medicalHistory.setImageUrl(imageName);
                        medicalHistory.setCaption(etCaption.getText().toString().trim());
                        myRef.child(key).setValue(medicalHistory).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AddMedicalHistoryActivity.this, "Medical History has been added", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                });


        }
    };

}