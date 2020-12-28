package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pmis.Model.Patient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddPatientActivity extends AppCompatActivity {
    private static final String TAG ="PATIENT ACTIVITY";
    private EditText etPatientBirthdate ,etPFirstName, etPMiddleName, etPLastName, etPEmail, etPContactNo, etPAddress, etPNotes;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        etPFirstName = findViewById(R.id.etPFirstName);
        etPMiddleName = findViewById(R.id.etPMiddleName);
        etPLastName = findViewById(R.id.etPLastName);
        etPEmail = findViewById(R.id.etPEmail);
        etPContactNo = findViewById(R.id.etPContactNo);
        etPAddress = findViewById(R.id.etPAddress);
        etPNotes = findViewById(R.id.etPNotes);
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Patient patient = new Patient();
//                if(validate()){
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    userID = user.getUid();
                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                     myRef = mFirebaseDatabase.getReference("Patient").child(userID);
                     String key = myRef.push().getKey();
                     patient.setFirstName(etPFirstName.getText().toString().trim());
                     patient.setMiddleName(etPMiddleName.getText().toString().trim());
                     patient.setLastName(etPLastName.getText().toString().trim());
                     patient.setEmail(etPEmail.getText().toString().trim());
                     patient.setContactNo(etPContactNo.getText().toString().trim());
                     patient.setAddress(etPAddress.getText().toString().trim());
                     patient.setNotes(etPNotes.getText().toString().trim());
                     patient.setBirthDate(etPatientBirthdate.getText().toString().trim());
                     patient.setKey(key);
                     myRef.child(key).setValue(patient).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             Toast.makeText(AddPatientActivity.this, "Patient Added Succesfully ", Toast.LENGTH_LONG).show();
                             finish();
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Toast.makeText(AddPatientActivity.this, "Failed to add Patient. Please Try Again", Toast.LENGTH_SHORT).show();
                         }
                     });
//                }
            }
        });
        etPatientBirthdate = findViewById(R.id.etPBirthDate);
        etPatientBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

}