package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.example.pmis.Helpers.DateUpdatedHelper;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.Procedures;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditPatientPaymentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "EDIT_PAYMENT";
    private String paymentKey, patientKey;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private ConstraintLayout constraintInstallment;
    private Spinner spinnerPaymentType, spinnerProcedure,spinnerPaymentMethod;
    private Button btnPaySave;
    private EditText etPayDentist,etPayDate,etPayAmount,etPayRemarks,etPayPlan,etPayInitial,etPayInitialRemarks;
    private  final List<String> paymentType = new ArrayList<String>();
    private  final List<String> paymentMethod = new ArrayList<String>();
    private final List<String> procedureList = new ArrayList<String>();
    private final List<String> priceList = new ArrayList<String>();
    final List<String> keyList = new ArrayList<>();
    String userID;
    private LoggedUserData loggedUserData = new LoggedUserData();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_payment);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent paymentIntent = getIntent();
        paymentKey = paymentIntent.getStringExtra("paymentKey");
        patientKey = paymentIntent.getStringExtra("patientKey");
        Log.d(TAG, "payment_patient: " + paymentKey + " " + patientKey);
        userID = loggedUserData.userID();
        viewfinder();
        buildPaymentMethodDropdown();
        buildPaymentTypeDropdown();
        buildProcedureDropdown();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("FULL PAYMENT").child(patientKey).child(paymentKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PatientPayment patientPayment = new PatientPayment();
                  String type = "FULL PAYMENT";
                etPayDate.setText(snapshot.getValue(PatientPayment.class).getDate());
                etPayDentist.setText(snapshot.getValue(PatientPayment.class).getDocName());
                etPayAmount.setText(snapshot.getValue(PatientPayment.class).getTotal());
                etPayRemarks.setText(snapshot.getValue(PatientPayment.class).getRemarks());
                etPayDentist.setText(snapshot.getValue(PatientPayment.class).getDocName());
                etPayDentist.setText(snapshot.getValue(PatientPayment.class).getDocName());
                int index = keyList.indexOf(snapshot.getValue(PatientPayment.class).getProcedureKey());
                spinnerProcedure.setSelection(index);
                if(type == "FULL PAYMENT"){
                    constraintInstallment.setVisibility(View.GONE);
                }
                for(int i = 0; i<spinnerPaymentMethod.getCount();i++){
                    if(spinnerPaymentMethod.getItemAtPosition(i) == snapshot.getValue(PatientPayment.class).getMethod()){
                        spinnerPaymentMethod.setSelection(i);
                        break;
                    }

                }
                for(int i = 0; i<spinnerPaymentType.getCount();i++){
                    if(spinnerPaymentType.getItemAtPosition(i) == snapshot.getValue(PatientPayment.class).getType()){
                        spinnerPaymentType.setSelection(i);
                        break;
                    }

                }
                spinnerPaymentType.setEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnPaySave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFullPayment()){
                    String date = etPayDate.getText().toString().trim();
                    String total = etPayAmount.getText().toString().trim();
                    String remarks = etPayRemarks.getText().toString().trim();
                    String initialPayment = etPayInitial.getText().toString().trim();
                    String initialRemarks = etPayRemarks.getText().toString().trim();
                    String type = spinnerPaymentType.getSelectedItem().toString().trim();
                    String method = spinnerPaymentMethod.getSelectedItem().toString().trim();
                    String docName = etPayDentist.getText().toString().trim();
                    String planName = etPayPlan.getText().toString().trim();
                    String procedureKey = keyList.get(spinnerProcedure.getSelectedItemPosition());
                    DateUpdatedHelper dateUpdatedHelper = new DateUpdatedHelper();
                    String dateUpdated = dateUpdatedHelper.getDateUpdated();

                    PatientPayment patientPayment = new PatientPayment();
                    patientPayment.setDocName(docName);
                    patientPayment.setDate(date);
                    patientPayment.setProcedureKey(procedureKey);
                    patientPayment.setMethod(method);
                    patientPayment.setTotal(total);
                    patientPayment.setRemarks(remarks);
                    patientPayment.setDateUpdated(dateUpdated);
                    patientPayment.setKey(paymentKey);
                    DatabaseReference saveInsRef, saveRef;
                    saveRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child(type).child(patientKey).child(paymentKey);
                    saveRef.setValue(patientPayment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditPatientPaymentActivity.this, "Patient Payment has been edited successfully", Toast.LENGTH_LONG).show();
                                finish();
                            }
                    }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditPatientPaymentActivity.this, "Submitting Failed. Please try again", Toast.LENGTH_LONG).show();

                            }
                    });

                }
            }
        });
        etPayDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                    etPayDate.clearFocus();
                }
            }
        });
    }
    private boolean validateFullPayment() {
        String date = etPayDate.getText().toString().trim();
        String total = etPayAmount.getText().toString().trim();
        String remarks = etPayRemarks.getText().toString().trim();
        String initialPayment = etPayInitial.getText().toString().trim();
        String initialRemarks = etPayRemarks.getText().toString().trim();
        String type = spinnerPaymentType.getSelectedItem().toString().trim();
        String planName = etPayPlan.getText().toString().trim();
        Log.d(TAG, "installment: " + total + " " + initialPayment);
        if(date.isEmpty()){
            etPayDate.setError("Date is required.");
            return false;
        }
        if(total.isEmpty()){
            etPayAmount.setError("Total Amount is required.");
            etPayAmount.requestFocus();
            return  false;
        }
        if(remarks.isEmpty()){
            etPayRemarks.setError("Total Amount is required.");
            etPayRemarks.requestFocus();
            return false;
        }
        if(type.equals("INSTALLMENT")) {
            if (initialPayment.isEmpty()) {
                etPayInitial.setError("Total Amount is required.");
                etPayInitial.requestFocus();
                return false;
            }
            if (planName.isEmpty()) {
                etPayPlan.setError("Plan Name is required.");
                etPayPlan.requestFocus();
                return false;
            }
            if (initialRemarks.isEmpty()) {
                etPayInitialRemarks.setError("Total Amount is required.");
                etPayInitialRemarks.requestFocus();
                return false;
            }
            if(Double.parseDouble(total) < Double.parseDouble(initialPayment)){
                etPayInitial.setError("Initial Payment exceeds total.");
                etPayInitial.requestFocus();
                return false;
            }
        }
        return true;
    }
    private void buildProcedureDropdown() {
        List<Procedures> proceduresList = new ArrayList<>();
        LoggedUserData loggedUserData = new LoggedUserData();
        String userID = loggedUserData.userID();
        FirebaseDatabase mFirebaseDatabase =  FirebaseDatabase.getInstance();
        DatabaseReference procedureRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        procedureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Procedures procedures = ds.getValue(Procedures.class);
                    String name = procedures.getName();
                    String description = procedures.getDescription();
                    String key = procedures.getKey();
                    int tempPrice = procedures.getPrice();
                    String price = String.valueOf(tempPrice);
                    procedureList.add(name);
                    keyList.add(key);
                    priceList.add(price);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, procedureList);
                spinnerProcedure.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        spinnerProcedure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String price = priceList.get(position);
                etPayAmount.setText(price);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void viewfinder() {
        etPayDentist = findViewById(R.id.etPayDentist);
        spinnerProcedure = findViewById(R.id.spinnerProcedure);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        etPayDate = findViewById(R.id.etPayDate);
        etPayAmount = findViewById(R.id.etPayAmount);
        etPayRemarks = findViewById(R.id.etPayRemarks);
        etPayPlan = findViewById(R.id.etPayPlan);
        etPayInitial = findViewById(R.id.etPayInitial);
        etPayInitialRemarks = findViewById(R.id.etPayInitialRemarks);
        spinnerPaymentType = findViewById(R.id.spinnerPaymentType);
        constraintInstallment = findViewById(R.id.constraintInstallment);
        btnPaySave = findViewById(R.id.btnPaySave);
    }
    private void buildPaymentMethodDropdown() {
        paymentMethod.add("Bank Transfer");
        paymentMethod.add("Cash");
        paymentMethod.add("Cheque");
        paymentMethod.add("Credit Card");
        paymentMethod.add("Insurance");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, paymentMethod);
        spinnerPaymentMethod.setAdapter(arrayAdapter);
    }

    private void buildPaymentTypeDropdown() {
        paymentType.add("FULL PAYMENT");
        paymentType.add("INSTALLMENT");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, paymentType);
        spinnerPaymentType.setAdapter(arrayAdapter);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());
        //etPayDate.setText(currentDateString);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
        etPayDate.setText(format.format(cal.getTime()));
    }
}