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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddPatientInstallmentBreakdownActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{
    private EditText etPayDate, etPayRemarks, etPayAmount;
    private Spinner spinnerPaymentMethod;
    private Button btnPaySave;
    private String patientKey, paymentKey, balance;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference paymentRef, saveRef, saveInsRef;
    private LoggedUserData loggedUserData = new LoggedUserData();
    private  final List<String> paymentMethod = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient_installment_breakdown);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etPayDate = findViewById(R.id.etPayDate);
        etPayRemarks = findViewById(R.id.etPayRemarks);
        etPayAmount = findViewById(R.id.etPayAmount);
        btnPaySave = findViewById(R.id.btnPaySave);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        paymentKey = intent.getStringExtra("paymentKey");
        balance = intent.getStringExtra("balance");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
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
        buildPaymentMethodDropdown();
        btnPaySave.setOnClickListener(saveInstallment);

//        paymentRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT").child(paymentKey);
//        paymentRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
    private final View.OnClickListener saveInstallment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                saveRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT").child(paymentKey).child("payment");
                String key = saveRef.push().getKey();
                DateUpdatedHelper dateUpdatedHelper = new DateUpdatedHelper();
                String currentDate = dateUpdatedHelper.getDateUpdated();
                Installment installment = new Installment();
                installment.setRemarks(etPayRemarks.getText().toString().trim());
                installment.setAmount(etPayAmount.getText().toString().trim());
                installment.setDate(etPayDate.getText().toString().trim());
                installment.setMethod(spinnerPaymentMethod.getSelectedItem().toString().trim());
                installment.setDateUpdated(currentDate);
                installment.setKey(key);
                saveRef.child(key).setValue(installment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddPatientInstallmentBreakdownActivity.this, "Installment has been added successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPatientInstallmentBreakdownActivity.this, "Submitting failed. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    private boolean validate() {
        String date = etPayDate.getText().toString().trim();
        String total = etPayAmount.getText().toString().trim();
        String remarks = etPayRemarks.getText().toString().trim();
        if(date.isEmpty()){
            etPayDate.setError("Date is required.");
            return false;
        }
        if(total.isEmpty()){
            etPayAmount.setError("Amount is required.");
            etPayAmount.requestFocus();
            return  false;
        }
        if(remarks.isEmpty()){
            etPayRemarks.setError("Remarks is required.");
            etPayRemarks.requestFocus();
            return false;
        }
        if(Double.parseDouble(total) > Double.parseDouble(balance)){
            etPayAmount.setError("Amount Exceed Total Payable. Total balance is " + balance);
            etPayAmount.requestFocus();
            return false;
        }

        return true;
    }

    private void buildPaymentMethodDropdown() {
        paymentMethod.add("Bank Transfer");
        paymentMethod.add("Cash");
        paymentMethod.add("Cheque");
        paymentMethod.add("Credit Card");
        paymentMethod.add("Insurance");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddPatientInstallmentBreakdownActivity.this, android.R.layout.simple_spinner_dropdown_item, paymentMethod);
        spinnerPaymentMethod.setAdapter(arrayAdapter);
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