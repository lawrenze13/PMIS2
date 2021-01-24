package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pmis.Adapter.PaymentPageAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.Procedures;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PatientPaymentActivity extends AppCompatActivity {
    private static final String TAG = "PATIENT_PAYMENT";
    private TabLayout tabLayout;
    private ViewPager2 vpPayment;
    private TabItem tabFullPayment, tabInstallment;
    private TextView tvPayRevenue, tvPayBalance, tvPayRevenue2, tvGrandTotal,tvPatientFullName;
    private String patientKey;
    private FloatingActionButton fabAddPayment;
    public PaymentPageAdapter paymentPageAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference fullRef,insRef;
    private ImageView ivProfile;
    private  double grandTotal = 0;
    private  double revenue = 0;
    private double installmentTotal = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_payment);
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        FirebaseAuth  mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
        userRef.addValueEventListener(setPatientInfo);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivProfile = findViewById(R.id.ivProfile);
        fabAddPayment = findViewById(R.id.fabAddPayment);
        fabAddPayment.setOnClickListener(addPayment);
        tabLayout = findViewById(R.id.tabLayout);
        tabFullPayment = findViewById(R.id.tabFullPayment);
        tabInstallment = findViewById(R.id.tabInstallment);
        tvPayRevenue = findViewById(R.id.tvPayRevenue);
        tvPayRevenue2 = findViewById(R.id.tvPayRevenue2);
        tvPayBalance = findViewById(R.id.tvPayBalance);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        vpPayment = findViewById(R.id.vpPayment);

        vpPayment.setAdapter(new PaymentPageAdapter(this));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabLayout, vpPayment, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("Full Payment");

                        break;
                    }
                    case 1:{
                        tab.setText("Installment");

                        break;
                    }
                }
            }
        }
        );
        tabLayoutMediator.attach();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        fullRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("FULL PAYMENT").child(patientKey);
        fullRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                revenue = 0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    String total = ds.getValue(PatientPayment.class).getTotal();
                    double amount = Double.parseDouble(total);
                    revenue = revenue + amount;

                }
                tvPayRevenue.setText(String.valueOf(revenue));
                tvGrandTotal.setText("Total Revenue: " + (revenue + installmentTotal));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        insRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey);
        insRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double subTotal = 0;
                grandTotal = 0;
                installmentTotal = 0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    subTotal = Double.parseDouble(ds.getValue(PatientPayment.class).getTotal());
                    for(DataSnapshot pay: ds.child("payment").getChildren()) {
                        String total = pay.getValue(Installment.class).getAmount();
                        double amount = Double.parseDouble(total);
                        installmentTotal = installmentTotal + amount;

                    }
                    grandTotal = grandTotal + subTotal;
                }

                tvPayBalance.setText(String.valueOf(grandTotal - installmentTotal));
                tvPayRevenue2.setText(String.valueOf(installmentTotal));
                tvGrandTotal.setText("Total Revenue: " + (revenue + installmentTotal));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private final View.OnClickListener addPayment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(PatientPaymentActivity.this, AddPatientPaymentActivity.class);
            intent.putExtra("patientKey",patientKey);
            intent.putExtra("type", tabLayout.getSelectedTabPosition());
            startActivity(intent);
        }
    };
    public ValueEventListener setPatientInfo = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            tvPatientFullName = findViewById(R.id.tvPatientFullName);

            String firstName = snapshot.getValue(Patient.class).getFirstName();
            String middleName = snapshot.getValue(Patient.class).getMiddleName();
            String lastName = snapshot.getValue(Patient.class).getLastName();
            String fullName = firstName + ' ' + middleName + ' ' + lastName;
            tvPatientFullName.setText(fullName);
            StorageReference viewPhotoRef = FirebaseStorage.getInstance().getReference().child("images/patientPic/" + snapshot.getValue(Patient.class).getKey());
            viewPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide
                            .with(PatientPaymentActivity.this)
                            .load(uri)
                            .thumbnail(0.5f)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .centerCrop()
                            .into(ivProfile);
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    public String PatientKey(){
        return  patientKey;
    }
}