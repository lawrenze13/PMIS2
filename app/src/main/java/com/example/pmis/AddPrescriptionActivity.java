package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Adapter.DrugListAdapter;
import com.example.pmis.Adapter.DrugPrescriptionAdapter;
import com.example.pmis.Model.DrugPrescription;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Drugs;
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
import java.util.List;
import java.util.Locale;

public class AddPrescriptionActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "patientKEY";
    private Button btnAddDrugPress, btnSave;
    private TextView tvTitle;
    private EditText etPresDate;
    private RecyclerView rvDrugPrescription;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, drugRef, editRef;
    private String userID, key, patientKey,action, prescriptionKey;
    private List<Drugs> drugsList;
    private List<DrugPrescription> prescriptionList;
    private List<DrugPrescription> drugPrescriptionList;
    private List<DrugPrescription> editDrugPrescriptionList;
    private DrugPrescriptionAdapter drugPrescriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prescription);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar8);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Add Patient Prescription");
        myToolbar.setTitleTextColor(getColor(R.color.white));
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent patientIntent = getIntent();
        patientKey = patientIntent.getStringExtra("patientKey");
        action = patientIntent.getStringExtra("action");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        drugsList = new ArrayList<>();

        rvDrugPrescription = findViewById(R.id.rvDrugPrescription);
        rvDrugPrescription.setLayoutManager(new LinearLayoutManager(this));
        drugPrescriptionList = new ArrayList<>();
        etPresDate = findViewById(R.id.etPresDate);
        etPresDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                    etPresDate.clearFocus();
                }
            }
        });
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveDrugPrescription);
        btnAddDrugPress = findViewById(R.id.btnAddDrugPres);
        btnAddDrugPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(AddPrescriptionActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.add_drug_prescription_dialog, null);
                final Spinner spinDrugInfo = (Spinner) mView.findViewById(R.id.spinDrugInfo);
                final EditText etFrequency = (EditText)mView.findViewById(R.id.etFrequency);
                final EditText etDuration = (EditText)mView.findViewById(R.id.etDuration);


                mFirebaseDatabase = FirebaseDatabase.getInstance();
                drugRef = mFirebaseDatabase.getReference("Drugs").child(userID);
                drugRef.keepSynced(true);
                drugRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final List<String> drugsInfoList = new ArrayList<String>();

                        drugsList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){

                            Drugs drugs = ds.getValue(Drugs.class);
                            drugsList.add(drugs);
                            String drugName = drugs.getDrugName();
                            String drugBrand = drugs.getDrugBrand();
                            String drugDosage = drugs.getDrugDosage();
                            String drugInfo = drugName + " (" + drugBrand + ") " + drugDosage;
                            drugsInfoList.add(drugInfo);
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPrescriptionActivity.this, android.R.layout.simple_spinner_dropdown_item, drugsInfoList);
                       spinDrugInfo.setAdapter(arrayAdapter);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                ImageButton btnDrugSubmit = (ImageButton) mView.findViewById(R.id.btnDrugSubmit);
                ImageButton btnDrugCancel = (ImageButton)mView.findViewById(R.id.btnDrugCancel);

                alert.setView(mView);
                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);

                btnDrugCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                btnDrugSubmit.setOnClickListener(new View.OnClickListener() {
                    private static final String TAG = "weorowe";

                    @Override
                    public void onClick(View v) {
                        DrugPrescription drugPrescription = new DrugPrescription();

                        if(validate()){
                            String dDescInfo = spinDrugInfo.getSelectedItem().toString().trim();
                            String dDescFrequency = etFrequency.getText().toString().trim();
                            String dDescDuration = etDuration.getText().toString().trim();
                            drugPrescription.setDrugInfo(dDescInfo);
                            drugPrescription.setFrequency(dDescFrequency);
                            drugPrescription.setDuration(dDescDuration);
                            DrugPrescription drugList = drugPrescription;
                            drugPrescriptionList.add(drugPrescription);
                            Log.d(TAG, String.valueOf(drugPrescriptionList.size()));

                                drugPrescriptionAdapter = new DrugPrescriptionAdapter(AddPrescriptionActivity.this, drugPrescriptionList);


                            rvDrugPrescription.setAdapter(drugPrescriptionAdapter);
                            drugPrescriptionAdapter.notifyItemChanged(drugPrescriptionList.size(),drugPrescriptionList);
                            alertDialog.dismiss();

                        }
                    }
                    private boolean validate() {
                        if(spinDrugInfo.getCount() != 0) {
                            String dDescInfo = spinDrugInfo.getSelectedItem().toString().trim();
                        }else{
                            Toast.makeText(AddPrescriptionActivity.this, "Add Clinic Drug First", Toast.LENGTH_LONG).show();

                            return false;
                        }
                        String dDFrequency = etFrequency.getText().toString().trim();
                        String dDDuration = etDuration.getText().toString().trim();

                        if(dDFrequency.isEmpty()){
                            etFrequency.setError("Frequency and Dosage is required");
                            etFrequency.requestFocus();
                            return false;
                        }
                        if(dDDuration.isEmpty()){
                            etDuration.setError("Duration is required");
                            etDuration.requestFocus();
                            return false;
                        }
                        return true;
                    }
                });

                alertDialog.show();

            }


        });



        if(action.equals("edit")){
            editDrugPrescriptionList = new ArrayList<>();
            getSupportActionBar().setTitle("Edit Patient Prescription");
            prescriptionKey = patientIntent.getStringExtra("prescriptionKey");

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            editRef = mFirebaseDatabase.getReference("Prescription").child(patientKey).child(prescriptionKey);
            editRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DrugPrescription drugPrescription = new DrugPrescription();
                    for(DataSnapshot ds: snapshot.child("drugList").getChildren()) {
                        drugPrescription.setDrugInfo(ds.getValue(DrugPrescription.class).getDrugInfo());
                        drugPrescription.setFrequency(ds.getValue(DrugPrescription.class).getFrequency());
                        drugPrescription.setDuration(ds.getValue(DrugPrescription.class).getDuration());
                        drugPrescriptionList.add(drugPrescription);
                    }
                    Log.d(TAG, String.valueOf(drugPrescriptionList.size()));
                    drugPrescriptionAdapter = new DrugPrescriptionAdapter(AddPrescriptionActivity.this,drugPrescriptionList);
                    rvDrugPrescription.setAdapter(drugPrescriptionAdapter);
                    drugPrescriptionAdapter.notifyItemChanged(drugPrescriptionList.size(),drugPrescriptionList);
                    etPresDate.setText(snapshot.getValue(DrugPrescriptionMain.class).getDate());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }



    private final View.OnClickListener saveDrugPrescription = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validateMain()){
                    DrugPrescriptionMain drugPrescriptionMain = new DrugPrescriptionMain();

                    drugPrescriptionMain.setDate(etPresDate.getText().toString().trim());
                    String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String dateUpdated = currentDate + ' ' + currentTime;
                    drugPrescriptionMain.setDateUpdated(dateUpdated);
                if(action.equals("add")) {
                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    myRef = mFirebaseDatabase.getReference("Prescription").child(patientKey);
                    String key = myRef.push().getKey();
                    drugPrescriptionMain.setKey(key);
                    myRef.child(key).setValue(drugPrescriptionMain).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            myRef.child(key).child("drugList").setValue(drugPrescriptionList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddPrescriptionActivity.this, "Presciption has been added succesfully", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }
                    });
                }
                if(action.equals("edit")){
                    drugPrescriptionMain.setKey(prescriptionKey);
                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                    myRef = mFirebaseDatabase.getReference("Prescription").child(patientKey).child(prescriptionKey);
                    myRef.setValue(drugPrescriptionMain).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            myRef.child("drugList").setValue(drugPrescriptionList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddPrescriptionActivity.this, "Prescription has been updated successfully", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }

        }
    };

    private boolean validateMain() {
        String presDate = etPresDate.getText().toString().trim();
        int drugPresCount = drugPrescriptionAdapter.getItemCount();
        if(presDate.isEmpty()){
            etPresDate.setError("Date is required");
            etPresDate.requestFocus();
            return false;
        }
        if(drugPresCount == 0){
            Toast.makeText(AddPrescriptionActivity.this, "Add at least one item on Drug Prescription First", Toast.LENGTH_LONG).show();
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
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
        etPresDate.setText(format.format(cal.getTime()));

    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(this, PatientPrescriptionActivity.class);
//        intent.putExtra("key", patientKey);
//        startActivity(intent);
//    }
}