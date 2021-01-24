package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private static final String TAG = "PROCEDURE_ADAPTER";
    String patientKey, userID;
    private EditText etPProcDate, etPProcNotes,etPrice, etEquipment;
    private TextView tvPProcTitle;
    private Button btnSave;
    private Spinner spinnerProcedure;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference procedureRef, submitRef, editRef;
    private FirebaseAuth mAuth;
    private String action, patientProcedureKey;
    final List<String> keyList = new ArrayList<>();
    final List<Procedures> proceduresMasterList = new ArrayList<>();
    final List<String> procedureList = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient_procedure);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar7);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Add Patient Procedure");
        myToolbar.setTitleTextColor(getColor(R.color.white));
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent getProcedureIntent = getIntent();
        patientKey = getProcedureIntent.getStringExtra("patientKey");
        action = getProcedureIntent.getStringExtra("action");
        etEquipment = findViewById(R.id.etEquipment);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveProcedure);
        etPProcNotes = findViewById(R.id.etPProcNotes);
        etPProcDate = findViewById(R.id.etPProcDate);
        etPrice = findViewById(R.id.etPrice);

        etPProcDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();

                    datePicker.show(getSupportFragmentManager(), "date picker");
                    etPProcDate.clearFocus();

                }
            }
        });
        spinnerProcedure = findViewById(R.id.spinnerProcedure);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        procedureRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        procedureRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot ds: snapshot.getChildren()){

                    Procedures procedures = ds.getValue(Procedures.class);
                    String name = procedures.getName();
                    String description = procedures.getDescription();
                    String key = procedures.getKey();
                    int tempPrice = procedures.getPrice();
                    String price = String.valueOf(tempPrice);
                    keyList.add(key);
                    Procedures prod = new Procedures();
                    prod.setEquipments(ds.getValue(Procedures.class).getEquipments());
                    prod.setKey(ds.getValue(Procedures.class).getKey());
                    prod.setPrice(ds.getValue(Procedures.class).getPrice());
                    prod.setDescription(ds.getValue(Procedures.class).getDescription());
                    proceduresMasterList.add(prod);
                    procedureList.add(name);

                }
                arrayAdapter = new ArrayAdapter<>(AddPatientProcedure.this, android.R.layout.simple_spinner_dropdown_item, procedureList);
                spinnerProcedure.setAdapter(arrayAdapter);
                if(action.equals("edit")){
                    patientProcedureKey = getProcedureIntent.getStringExtra("patientProcedureKey");
                    getSupportActionBar().setTitle("Edit Patient Procedure");
                    Log.d(TAG, "procedureKey: " + patientProcedureKey);

                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    editRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey).child(patientProcedureKey);
                    editRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int position;
                            Log.d(TAG,"snapshot.getValue(PatientProcedures.class).getProcedure() " + snapshot.getValue(PatientProcedures.class).getProcedure());
                            etPProcDate.setText(snapshot.getValue(PatientProcedures.class).getDate());
                            spinnerProcedure.setSelection(arrayAdapter.getPosition(snapshot.getValue(PatientProcedures.class).getProcedure()));
                            etPProcNotes.setText(snapshot.getValue(PatientProcedures.class).getNote());
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


        spinnerProcedure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etPrice.setText(String.valueOf(proceduresMasterList.get(position).getPrice()));
                etEquipment.setText(String.valueOf(proceduresMasterList.get(position).getEquipments()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private final View.OnClickListener saveProcedure = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()) {

                if(action.equals("add")) {
                    PatientProcedures patientProcedures = new PatientProcedures();
                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    submitRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey);
                    String key = submitRef.push().getKey();
                    String date = etPProcDate.getText().toString().trim();
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
                            Toast.makeText(AddPatientProcedure.this, "Patient Procedure has been added successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddPatientProcedure.this, "Submitting Failed. Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                }if(action.equals("edit")){
                    PatientProcedures patientProcedures = new PatientProcedures();
                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    submitRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey).child(patientProcedureKey);
                    String date = etPProcDate.getText().toString().trim();
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
                    patientProcedures.setKey(patientProcedureKey);
                    submitRef.setValue(patientProcedures).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPatientProcedure.this, "Patient Procedure has been updated successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddPatientProcedure.this, "Patient Procedure update failed. Please try Again.", Toast.LENGTH_LONG).show();

                        }
                    });
                }
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
       // etPProcDate.setText(currentDateString);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
        etPProcDate.setText(format.format(cal.getTime()));

    }
}