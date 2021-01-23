package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Adapter.PatientInstallmentBreakdownAdapter;
import com.example.pmis.Adapter.PatientPrescriptionAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.PatientPayment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InstallmentBreakdownActivity extends AppCompatActivity {
    private static final String TAG = "INSTALLMENT_BREAKDOWN";
    private FloatingActionButton fabAddPrescription;
    private RecyclerView rvInstallmentBreakdown;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private String userID, patientKey,paymentKey;
    private DatabaseReference paymentRef, presRef, docRef;
    private TextView tvPlanName, tvInsGrandTotal, tvInsBalance, tvInsAmount;
    private FloatingActionButton fabAddInstallment;
    private ImageButton ibPayDelete, ibPayEdit;
    private List<Installment> installmentList;
    private    double totalPaid = 0;
    private    double grandTotal = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_installment_breakdown);
        LoggedUserData loggedUserData = new LoggedUserData();
        userID = loggedUserData.userID();
        fabAddInstallment = findViewById(R.id.fabAddInstallment);
        tvPlanName = findViewById(R.id.tvPlanName);
        ibPayDelete = findViewById(R.id.ibPayDelete);
        ibPayDelete.setOnClickListener(deleteInstallment);
        ibPayEdit = findViewById(R.id.ibPayEdit);
        ibPayEdit.setOnClickListener(editInstallment);
        tvInsGrandTotal = findViewById(R.id.tvInsGrandTotal);
        tvInsAmount = findViewById(R.id.tvInsAmount);
        tvInsBalance = findViewById(R.id.tvInsBalance);
        rvInstallmentBreakdown = findViewById(R.id.rvInstallmentBreakdown);
        rvInstallmentBreakdown.setLayoutManager(new LinearLayoutManager(this));
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        paymentKey = intent.getStringExtra("paymentKey");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        installmentList = new ArrayList<>();
        Log.d(TAG, "patientKey: " + patientKey);
        Log.d(TAG, "paymentKey: " + paymentKey);
        paymentRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey).child(paymentKey);
        paymentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    totalPaid = 0;

                     installmentList.clear();
                    tvPlanName.setText(snapshot.getValue(PatientPayment.class).getPlanName());
                    tvInsGrandTotal.setText(snapshot.getValue(PatientPayment.class).getTotal());
                    for(DataSnapshot pay: snapshot.child("payment").getChildren()){
                        String paid = pay.getValue(Installment.class).getAmount();
                        totalPaid = totalPaid + Double.parseDouble(paid);
                        Installment installment = new Installment();
                        installment.setKey(pay.getValue(Installment.class).getKey());
                        installment.setDate(pay.getValue(Installment.class).getDate());
                        installment.setDateUpdated(pay.getValue(Installment.class).getDateUpdated());
                        installment.setMethod(pay.getValue(Installment.class).getMethod());
                        installment.setAmount(pay.getValue(Installment.class).getAmount());
                        installment.setRemarks(pay.getValue(Installment.class).getRemarks());
                        Log.d(TAG, "COUNTER: " + pay.getValue(Installment.class).getKey());
                        installmentList.add(installment);
                    }
                    grandTotal = Double.parseDouble(snapshot.getValue(PatientPayment.class).getTotal());
                    tvInsAmount.setText(String.valueOf(totalPaid));
                    tvInsBalance.setText(String.valueOf(Double.parseDouble(snapshot.getValue(PatientPayment.class).getTotal()) - totalPaid));
                    PatientInstallmentBreakdownAdapter patientInstallmentBreakdownAdapter = new PatientInstallmentBreakdownAdapter(InstallmentBreakdownActivity.this, installmentList, patientKey, paymentKey);
                    rvInstallmentBreakdown.setAdapter(patientInstallmentBreakdownAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fabAddInstallment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double balance = grandTotal - totalPaid;
                if(balance != 0) {
                    Intent intent = new Intent(InstallmentBreakdownActivity.this, AddPatientInstallmentBreakdownActivity.class);
                    intent.putExtra("patientKey", patientKey);
                    intent.putExtra("paymentKey", paymentKey);
                    intent.putExtra("balance", tvInsBalance.getText().toString().trim());
                    startActivity(intent);
                }else{
                    Toast.makeText(InstallmentBreakdownActivity.this,"Payment Completed. Cant add new Payment.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private final View.OnClickListener deleteInstallment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Query query = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey).orderByChild("key").equalTo(paymentKey);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(v.getContext(),"Item not deleted! please try again.", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };
    private final View.OnClickListener editInstallment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InstallmentBreakdownActivity.this, EditPaymentInstallmentActivity.class);
            intent.putExtra("patientKey", patientKey);
            intent.putExtra("paymentKey", paymentKey);
            startActivity(intent);
        }
    };
}