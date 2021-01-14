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
import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddPatientPaymentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "ADD_PATIENT_PAYMENT";
    int price;
    private EditText  etPayDentist,etPayDate, etPayRemarks, etPayInitial,etPayInitialRemarks, etPayAmount, etPayPlan;
    private ConstraintLayout constraintInstallment;
    private Spinner spinnerPaymentType, spinnerPaymentMethod, spinnerProcedure;
    private Button btnPaySave;
    String procedureKey, patientProcedureKey, patientKey, procedureName;
    String userID, docName ;
    int paymentTypes;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference docRef, saveRef, saveInsRef;
    private LoggedUserData loggedUserData = new LoggedUserData();
    private  final List<String> paymentType = new ArrayList<String>();
    private  final List<String> paymentMethod = new ArrayList<String>();
    private final List<String> procedureList = new ArrayList<String>();
    private final List<String> priceList = new ArrayList<String>();
    final List<String> keyList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_add_payment);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewFinder();

        mFirebaseDatabase  = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        price = intent.getIntExtra("procedurePrice", 0);
        paymentTypes = intent.getIntExtra("type", 0);

        patientKey = intent.getStringExtra("patientKey");
        procedureName = intent.getStringExtra("procedureName");

        userID = loggedUserData.userID();
        Log.d(TAG, userID);
        buildDoctorName();
        buildPaymentTypeDropdown();
        buildPaymentMethodDropdown();
        buildProcedureDropdown();
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
        etPayAmount.setText(String.valueOf(price));
        spinnerPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    constraintInstallment.setVisibility(View.VISIBLE);
                }else{
                    constraintInstallment.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnPaySave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (validateFullPayment()) {
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
                        Log.d(TAG, "procedureKey: " + procedureKey);
                        DateUpdatedHelper dateUpdatedHelper = new DateUpdatedHelper();
                        String dateUpdated = dateUpdatedHelper.getDateUpdated();
                        saveRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child(type);
                        String key = saveRef.push().getKey();
                        PatientPayment patientPayment = new PatientPayment();
                        patientPayment.setDocName(docName);
                        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
                        try {
                            Date parseDate = format.parse(date);
                            long timeStamp = parseDate.getTime();
                            patientPayment.setTimeStamp(timeStamp);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        patientPayment.setDate(date);

                        patientPayment.setProcedureKey(procedureKey);
                        patientPayment.setMethod(method);
                        patientPayment.setTotal(total);
                        patientPayment.setRemarks(remarks);
                        patientPayment.setDateUpdated(dateUpdated);
                        patientPayment.setKey(key);

                        if(type.equals("INSTALLMENT")){
                            patientPayment.setInitialPayment(initialPayment);
                            patientPayment.setInitialRemarks(initialRemarks);
                            patientPayment.setPlanName(planName);
                            Installment installment = new Installment();
                            installment.setAmount(initialPayment);
                            installment.setRemarks(initialRemarks);
                            installment.setMethod(method);
                            installment.setDate(date);
                            try {
                                Date parseDate = format.parse(date);
                                long timeStamp = parseDate.getTime();
                                installment.setTimeStamp(timeStamp);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            installment.setDateUpdated(dateUpdated);
                            saveInsRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child(type).child(key);
                            saveInsRef.setValue(patientPayment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DatabaseReference savePayment = mFirebaseDatabase.getReference("Payments").child(patientKey).child(type).child(key).child("payment");
                                    String installmentKey = savePayment.push().getKey();
                                    installment.setKey(installmentKey);
                                    savePayment.child(installmentKey).setValue(installment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddPatientPaymentActivity.this, "Patient Payment has been added successfully", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddPatientPaymentActivity.this, "Submitting Failed. Please try again", Toast.LENGTH_LONG).show();

                                }
                            });
                        }else {
                            saveRef.child(key).setValue(patientPayment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddPatientPaymentActivity.this, "Patient Payment has been added successfully", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddPatientPaymentActivity.this, "Submitting Failed. Please try again", Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    }
                }
              
        });
        if(paymentTypes == 1){
            spinnerPaymentType.setSelection(1);
        }else{
            spinnerPaymentType.setSelection(0);
        }
        spinnerPaymentType.setEnabled(false);
    }

    private void buildProcedureDropdown() {
        List<Procedures> proceduresList = new ArrayList<>();
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
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, procedureList);
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



    private void buildPaymentMethodDropdown() {
        paymentMethod.add("Bank Transfer");
        paymentMethod.add("Cash");
        paymentMethod.add("Cheque");
        paymentMethod.add("Credit Card");
        paymentMethod.add("Insurance");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, paymentMethod);
        spinnerPaymentMethod.setAdapter(arrayAdapter);
    }

    private void buildPaymentTypeDropdown() {
        paymentType.add("FULL PAYMENT");
        paymentType.add("INSTALLMENT");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPatientPaymentActivity.this, android.R.layout.simple_spinner_dropdown_item, paymentType);
        spinnerPaymentType.setAdapter(arrayAdapter);
    }

    private void viewFinder() {
        etPayDentist = findViewById(R.id.etPayDentist);
        etPayDate = findViewById(R.id.etPayDate);
        etPayRemarks = findViewById(R.id.etPayRemarks);
        etPayInitial = findViewById(R.id.etPayInitial);
        etPayInitialRemarks = findViewById(R.id.etPayInitialRemarks);
        etPayPlan = findViewById(R.id.etPayPlan);
        etPayAmount = findViewById(R.id.etPayAmount);
        spinnerProcedure = findViewById(R.id.spinnerProcedure);
        constraintInstallment = findViewById(R.id.constraintInstallment);
        constraintInstallment.setVisibility(View.GONE);

        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        spinnerPaymentType = findViewById(R.id.spinnerPaymentType);

        btnPaySave = findViewById(R.id.btnPaySave);

    }

    private void buildDoctorName() {

        docRef = mFirebaseDatabase.getReference("Users").child(userID);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo userInfo = new UserInfo();
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                docName = "Dr. " +  firstName + ' ' + lastName + " D.M.D";
                etPayDentist.setText(docName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());
        etPayDate.setText(currentDateString);
    }
}