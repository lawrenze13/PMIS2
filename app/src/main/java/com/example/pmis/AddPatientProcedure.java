package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Model.Drugs;
import com.example.pmis.Model.PatientProcedures;
import com.example.pmis.Model.Procedures;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class AddPatientProcedure extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    String patientKey, userID;
    private EditText etPProcDate, etPProcNotes;
    private Button btnSave;
    private Spinner spinnerProcedure;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference procedureRef, submitRef, presRef;
    private FirebaseAuth mAuth;
    final List<String> keyList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient_procedure);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveProcedure);
        etPProcNotes = findViewById(R.id.etPProcNotes);
        etPProcDate = findViewById(R.id.etPProcDate);
        etPProcDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                }
            }
        });
        spinnerProcedure = findViewById(R.id.spinnerProcedure);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        procedureRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        procedureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<String> procedureList = new ArrayList<String>();

                for(DataSnapshot ds: snapshot.getChildren()){

                    Procedures procedures = ds.getValue(Procedures.class);
                    String name = procedures.getName();
                    String description = procedures.getDescription();
                    String key = procedures.getKey();
                    int tempPrice = procedures.getPrice();
                    String price = String.valueOf(tempPrice);
                    keyList.add(key);
                    procedureList.add(name);

                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPatientProcedure.this, android.R.layout.simple_spinner_dropdown_item, procedureList);
                spinnerProcedure.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private final View.OnClickListener saveProcedure = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                PatientProcedures patientProcedures = new PatientProcedures();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                submitRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey);
                String key = submitRef.push().getKey();
                String date =etPProcDate.getText().toString().trim();
                String note = etPProcNotes.getText().toString().trim();
                String procedure = spinnerProcedure.getSelectedItem().toString().trim();
                int procedurePosition = spinnerProcedure.getSelectedItemPosition();
                String procedureKey = keyList.get(procedurePosition);
                String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                String dateUpdated = currentDate + ' ' + currentTime;
                patientProcedures.setDateUpdated(dateUpdated);
                patientProcedures.setDate(date);
                patientProcedures.setNote(note);
                patientProcedures.setProcedureKey(procedureKey);
                patientProcedures.setProcedure(procedure);
                patientProcedures.setKey(key);
                submitRef.child(key).setValue(patientProcedures).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddPatientProcedure.this,"Patient Procedure has been added succesfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPatientProcedure.this, "Submitting Failed. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    private boolean validate() {
        String date =etPProcDate.getText().toString().trim();

        String note = etPProcNotes.getText().toString().trim();
        if(date.isEmpty()) {
            etPProcDate.setError("Date is required");
            etPProcDate.requestFocus();
            return false;
        }
        if(note.isEmpty()){
            etPProcNotes.setError("Note is required");
            etPProcNotes.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());
        etPProcDate.setText(currentDateString);

    }
}