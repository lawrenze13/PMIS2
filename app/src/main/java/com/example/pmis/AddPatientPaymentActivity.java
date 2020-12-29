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
import android.widget.Spinner;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddPatientPaymentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "ADD_PATIENT_PAYMENT";
    int price;
    private EditText  etPayDentist,etPayDate, etPayRemarks, etPayInitial,etPayInitialRemarks, etPayAmount;
    private ConstraintLayout constraintInstallment;
    private Spinner spinnerPaymentType, spinnerPaymentMethod;
    private Button btnPaySave;
    String procedureKey, patientProcedureKey, patientKey, procedureName;
    String userID, docName;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference docRef;
    private LoggedUserData loggedUserData = new LoggedUserData();
    private  final List<String> paymentType = new ArrayList<String>();
    private  final List<String> paymentMethod = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_add_payment);
        viewFinder();

        mFirebaseDatabase  = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        price = intent.getIntExtra("procedurePrice", 0);
        procedureKey = intent.getStringExtra("procedureKey");
        patientProcedureKey = intent.getStringExtra("patientProcedureKey");
        patientKey = intent.getStringExtra("patientKey");
        procedureName = intent.getStringExtra("procedureName");

        userID = loggedUserData.userID();
        Log.d(TAG, userID);
        buildDoctorName();
        buildPaymentTypeDropdown();
        buildPaymentMethodDropdown();
        etPayDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
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
                if(spinnerPaymentType.getSelectedItemPosition() == 0) {
                    if (validateFullPayment()) {

                    }
                }
                else{
                    if(validateInstallment()){

                    }
                }
            }
        });

    }

    private boolean validateInstallment() {
        return true;
    }

    private boolean validateFullPayment() {
        String date = etPayDate.getText().toString().trim();
        String remarks = etPayRemarks.getText().toString().trim();
        String amount = etPayAmount.getText().toString().trim();
        if(Integer.parseInt(amount) > price){
            etPayAmount.setError("");
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
        etPayAmount = findViewById(R.id.etPayAmount);
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