package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Model.Clinic;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditClinicActivity extends AppCompatActivity {
    private static final String TAG ="";
    private EditText etClinicName, etContactNo, etAddress;
    private TextView txtClinicName;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clinic);
        txtClinicName = (TextView)findViewById(R.id.txtClinicName);
        etClinicName = (EditText)findViewById(R.id.etClinicName);
        etAddress = (EditText)findViewById(R.id.etAddress);
        etContactNo = (EditText)findViewById(R.id.etContactNo);
        setClinicData();

    }

    private void setClinicData() {
        Intent intent = getIntent();
        String clinicName = intent.getStringExtra("clinicName");
        String address = intent.getStringExtra("address");
        String contactNo = intent.getStringExtra("contactNo");

        txtClinicName.setText(clinicName);
        etClinicName.setText(clinicName);
        etAddress.setText(address);
        etContactNo.setText(contactNo);
    }

    public boolean validateForm(){
        String clinicName = etClinicName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();
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
        return true;
    }
    public void cancel(View v){
        finish();
    }
    public void submit(View v){
        if(validateForm()){
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            userID = user.getUid();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference("Clinic").child(userID);
            Clinic clinic = new Clinic();
            clinic.setClinicName(etClinicName.getText().toString().trim());
            clinic.setAddress(etAddress.getText().toString().trim());
            clinic.setContactNo(etContactNo.getText().toString().trim());
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