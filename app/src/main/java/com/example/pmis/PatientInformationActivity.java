package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.DrugPrescription;
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
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientInformationActivity extends AppCompatActivity {
    private static final String TAG ="PATIENT ACTIVITY";
    public static final int PICK_IMAGE = 1;
    public static final int PICK_CAMERA = 2;
    public static final int CAMERA_PERM_CODE = 101;

    private FirebaseStorage storage;
    private StorageReference storageReference, viewPhotoeReference;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private Button editInfo;
    private CardView cvPPrescription, cvMedicalHistory, cvProcedures,cvPayments, cvPPhotos;
    private ImageView ivPatientPic;
    private String userID, patientKey, contactNo , fullName;
    private ConstraintLayout clSched, clCall, clMessage, clDownload;
    private Patient patientInfo;
    private String  clinicName, clinicAddress, docName, clinicContactNo;
    private TextView tvPatientAddress2, tvDateAdded, tvPatientFullName, tvPatientEmail, tvPatientAddress, tvPatientContactNo, tvPatientBirthDate, tvPatientNotes, tvPatientGender, tvPatientAge;
    private List<MedicalHistory> medicalHistoryList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_information);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cvPPrescription = findViewById(R.id.cvPPrescription);
        ivPatientPic = findViewById(R.id.ivPatientPic);

        cvPPrescription.setOnClickListener(viewPrescription);
        cvPPhotos = findViewById(R.id.cvPPhotos);
        cvPPhotos.setOnClickListener(viewPatientPhotos);
        cvMedicalHistory = findViewById(R.id.cvMedicalHistory);
        cvMedicalHistory.setOnClickListener(viewMedicalHistory);
        cvProcedures = findViewById(R.id.cvProcedures);
        cvProcedures.setOnClickListener(viewProcedures);
        cvPayments = findViewById(R.id.cvPayments);
        cvPayments.setOnClickListener(viewPayments);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("key");
        Log.d(TAG, "patientKey: " + patientKey);
        mAuth = FirebaseAuth.getInstance();
        ViewFinder();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getClinicInfo();
        getDoctorName();
        myRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.getValue(Patient.class).getFirstName();
                String middleName = snapshot.getValue(Patient.class).getMiddleName();
                String lastName = snapshot.getValue(Patient.class).getLastName();
                String address2 = snapshot.getValue(Patient.class).getBarangay() + ", " + snapshot.getValue(Patient.class).getCity() + ", " + snapshot.getValue(Patient.class).getProvince();
                fullName = firstName + ' ' + middleName + ' ' + lastName;
                contactNo = snapshot.getValue(Patient.class).getContactNo();
                tvPatientAddress.setText(snapshot.getValue(Patient.class).getAddress());
                tvPatientAddress2.setText(address2);
                tvPatientBirthDate.setText(snapshot.getValue(Patient.class).getBirthDate());
                tvPatientContactNo.setText(snapshot.getValue(Patient.class).getContactNo());
                tvPatientEmail.setText(snapshot.getValue(Patient.class).getEmail());
                tvPatientGender.setText(snapshot.getValue(Patient.class).getSex());
                tvPatientNotes.setText(snapshot.getValue(Patient.class).getNotes());
                tvDateAdded.setText(snapshot.getValue(Patient.class).getDateAdded());
                contactNo = snapshot.getValue(Patient.class).getContactNo();
                patientInfo = new Patient();

                patientInfo.setAddress(snapshot.getValue(Patient.class).getAddress());
                patientInfo.setBirthDate(snapshot.getValue(Patient.class).getBirthDate());
                patientInfo.setContactNo(snapshot.getValue(Patient.class).getContactNo());
                patientInfo.setEmail(snapshot.getValue(Patient.class).getEmail());
                patientInfo.setSex(snapshot.getValue(Patient.class).getSex());
                patientInfo.setNotes(snapshot.getValue(Patient.class).getNotes());
                patientInfo.setDateAdded(snapshot.getValue(Patient.class).getDateAdded());
                patientInfo.setFirstName(snapshot.getValue(Patient.class).getFirstName());
                patientInfo.setMiddleName(snapshot.getValue(Patient.class).getMiddleName());
                patientInfo.setLastName(snapshot.getValue(Patient.class).getLastName());
                patientInfo.setKey(snapshot.getValue(Patient.class).getKey());

                tvPatientFullName.setText(fullName);

                viewPhotoeReference = FirebaseStorage.getInstance().getReference().child("images/patientPic/" + snapshot.getValue(Patient.class).getKey());
                viewPhotoeReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide
                                .with(PatientInformationActivity.this)
                                .load(uri)
                                .thumbnail(0.5f)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .centerCrop()
                                .into(ivPatientPic);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        editInfo = findViewById(R.id.editInfo);
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent schedIntent = new Intent(PatientInformationActivity.this,EditPatientInformationActivity.class);
                schedIntent.putExtra("patientKey", patientKey);
                startActivity(schedIntent);
            }
        });

        clDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
            }
        });
        ImageButton ibCall  = findViewById(R.id.ibCall);
        ibCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactNo));
                startActivity(callIntent);
            }
        });
        clCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactNo));
                startActivity(callIntent);
            }
        });
        ImageButton ibMessage = findViewById(R.id.ibMessage);
        ibMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:" + contactNo));
                startActivity(intent);
            }
        });
        clMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:" + contactNo));
                startActivity(intent);
            }
        });
        clSched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent schedIntent = new Intent(PatientInformationActivity.this,AddScheduleActivity.class);
                schedIntent.putExtra("patientKey", patientKey);
                schedIntent.putExtra("fullName", fullName);
                startActivity(schedIntent);
            }
        });
        ImageButton ibDownload = findViewById(R.id.ibDownload);
        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
            }
        });
        ImageButton ibSchedule = findViewById(R.id.ibSchedule);
        ibSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent schedIntent = new Intent(PatientInformationActivity.this,AddScheduleActivity.class);
                schedIntent.putExtra("patientKey", patientKey);
                schedIntent.putExtra("fullName", fullName);
                startActivity(schedIntent);
            }
        });

        DatabaseReference patientRef = mFirebaseDatabase.getReference("Patient").child(userID);
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot patientSnapshot: snapshot.getChildren()){
                    String patientKey = patientSnapshot.getValue(MedicalHistory.class).getKey();
                    DatabaseReference medicalHistoryRef = mFirebaseDatabase.getReference("MedicalHistory").child(patientKey);
                    medicalHistoryRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            medicalHistoryList.clear();
                             if(snapshot.exists()){
                                 for(DataSnapshot medicalHistorySnapshot: snapshot.getChildren()){
                                     MedicalHistory medicalHistory = new MedicalHistory();
                                     medicalHistory.setCaption(medicalHistorySnapshot.getValue(MedicalHistory.class).getCaption());
                                     medicalHistory.setImageUrl(medicalHistorySnapshot.getValue(MedicalHistory.class).getImageUrl());
                                     medicalHistory.setDate(medicalHistorySnapshot.getValue(MedicalHistory.class).getDate());
                                     medicalHistoryList.add(medicalHistory);
                                 }
                             }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void generatePDF() {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        String currentDate = format.format(new Date());
        PdfDocument myPdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint forLinePaint = new Paint();
        Paint solidLinePaint = new Paint();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 500, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();
        Typeface typeface = ResourcesCompat.getFont(PatientInformationActivity.this, R.font.montserrat);
        paint.setTypeface(typeface);
        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(clinicName, canvas.getWidth() / 2, 20, paint);
        paint.setTextSize(8f);
        canvas.drawText(clinicAddress, canvas.getWidth() / 2, 35, paint);
        canvas.drawText(clinicContactNo, canvas.getWidth() / 2, 44, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8.5f);
        canvas.drawText(docName, 20, 60, paint);
        paint.setTextSize(7f);
        canvas.drawText("General Dentist", 20, 68, paint);
        paint.setTextSize(7f);
        canvas.drawText("License No. 563242612", 20, 75, paint);

        paint.setTextSize(7f);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentDate, 575, 75, paint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 80, 280, 80, solidLinePaint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Full Name: ", 40, 110, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText(patientInfo.getFirstName() + " " + patientInfo.getMiddleName() + " " + patientInfo.getLastName(), 100, 110, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Birthdate: ", 40, 130, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getBirthDate(), 100, 130, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Gender: ", 40, 150, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getSex(), 100, 150, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Address: ", 40, 170, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getAddress(), 100, 170, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Contact No: ", 40, 190, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getContactNo(), 100, 190, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Email: ", 40, 210, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getEmail(), 100, 210, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8f);
        canvas.drawText("Notes: ", 40, 230, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9f);
        canvas.drawText( patientInfo.getNotes(), 100, 230, paint);
        myPdfDocument.finishPage(myPage);

//        PdfDocument.PageInfo myPageInfo2 = new PdfDocument.PageInfo.Builder(300, 500, 2).create();
//        PdfDocument.Page myPage2 = myPdfDocument.startPage(myPageInfo2);
//        Canvas canvas2 = myPage2.getCanvas();
//
//        paint.setTypeface(typeface);
//        paint.setTextSize(12f);
//        paint.setColor(Color.BLACK);
//        paint.setTextAlign(Paint.Align.CENTER);
//        canvas2.drawText(clinicName, canvas2.getWidth() / 2, 20, paint);
//        paint.setTextSize(8f);
//        canvas2.drawText(clinicAddress, canvas2.getWidth() / 2, 35, paint);
//        canvas2.drawText(clinicContactNo, canvas2.getWidth() / 2, 44, paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(8.5f);
//        canvas2.drawText(docName, 20, 60, paint);
//        paint.setTextSize(7f);
//        canvas2.drawText("General Dentist", 20, 68, paint);
//        paint.setTextSize(7f);
//        canvas2.drawText("License No. 563242612", 20, 75, paint);
//
//        paint.setTextSize(7f);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas2.drawText(currentDate, 575, 75, paint);
//        solidLinePaint.setStyle(Paint.Style.STROKE);
//        solidLinePaint.setStrokeWidth(1);
//        canvas2.drawLine(20, 80, 280, 80, solidLinePaint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(8f);
//        canvas2.drawText("Full Name: ", 40, 110, paint);
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(9f);
//        canvas2.drawText(patientInfo.getFirstName() + " " + patientInfo.getMiddleName() + " " + patientInfo.getLastName(), 100, 110, paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(8f);
//        canvas2.drawText("Birthdate: ", 40, 130, paint);
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(9f);
//        canvas2.drawText( patientInfo.getBirthDate(), 100, 130, paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(8f);
//        canvas2.drawText("Gender: ", 40, 150, paint);
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(9f);
//        canvas2.drawText( patientInfo.getSex(), 100, 150, paint);
//
//
//        myPdfDocument.finishPage(myPage2);


        String fileName = "test.pdf";

        File file = new File(getExternalFilesDir("/"),fileName);

        try{
            myPdfDocument.writeTo(new FileOutputStream(file));

        } catch (IOException e){
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        myPdfDocument.close();
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = Intent.createChooser(target, "Open File");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);

    }


    private final View.OnClickListener viewPrescription  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientPrescriptionActivity.class);
            intent.putExtra("key", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewPayments  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientPaymentActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewMedicalHistory = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientMedicalHistoryActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewProcedures = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientProceduresActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewPatientPhotos = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientInformationActivity.this, PatientPhotoActivity.class);
            intent.putExtra("patientKey", patientKey);
            startActivity(intent);
        }
    };
    private void ViewFinder() {
        tvPatientAddress2 = findViewById(R.id.tvPatientAddress2);
        tvPatientAddress = findViewById(R.id.tvPatientAddress);
        tvPatientBirthDate = findViewById(R.id.tvPatientBirthDate);
        tvPatientContactNo = findViewById(R.id.tvPatientContactNo);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientFullName = findViewById(R.id.tvPatientFullName);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientNotes = findViewById(R.id.tvPatientNotes);
        clSched = findViewById(R.id.clSched);
        clCall = findViewById(R.id.clCall);
        clMessage = findViewById(R.id.clMessage);
        clDownload = findViewById(R.id.clDownload);
        tvDateAdded = findViewById(R.id.tvDateAdded);


    }
    private void getDoctorName() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference  docRef = mFirebaseDatabase.getReference("Users").child(userID);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                docName = "Dr. " +  firstName + ' ' + lastName + " D.M.D";
                Log.d(TAG, "docName: " + docName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getClinicInfo() {
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clinicName = snapshot.getValue(Clinic.class).getClinicName();
                clinicAddress = snapshot.getValue(Clinic.class).getAddress();
                clinicContactNo = snapshot.getValue(Clinic.class).getContactNo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}