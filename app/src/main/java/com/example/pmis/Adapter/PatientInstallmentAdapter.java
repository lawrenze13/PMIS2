package com.example.pmis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.EditPaymentInstallmentActivity;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.InstallmentBreakdownActivity;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PatientInstallmentAdapter extends  RecyclerView.Adapter{
    private static final String TAG = "INSTALLMENT_ADAPTER";
    List<PatientPayment> fetchPatientPayment;
//    List<Installment> fetchInstallment;
    public Context context;
    private DataSnapshot snapshot;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;
    private StorageReference mStorageRef;
    private String patientKey, paymentKey;
    private String userID;
    private LoggedUserData loggedUserData = new LoggedUserData();
    String  docName,  type,  method,  date,  total,  remarks,dateUpdated, planName;

    public PatientInstallmentAdapter(Context context, List<PatientPayment> fetchPatientPayment, String patientKey, DataSnapshot snapshot){
        this.fetchPatientPayment = fetchPatientPayment;
        this.snapshot = snapshot;
        this.context = context;
        this.patientKey = patientKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_installment_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        userID = loggedUserData.userID();
        PatientPayment patientPayment = fetchPatientPayment.get(position);
        paymentKey = patientPayment.getKey();
        docName = patientPayment.getDocName();
        type = patientPayment.getKey();
        method = patientPayment.getMethod();
        date = patientPayment.getDate();
        total = patientPayment.getTotal();
        remarks = patientPayment.getRemarks();
        dateUpdated = patientPayment.getDateUpdated();
        planName = patientPayment.getPlanName();
        double totalPayment = 0;
        int counter = 0;
        for(DataSnapshot ds: snapshot.getChildren()){
            if(paymentKey == ds.getValue(PatientPayment.class).getKey()){
                for(DataSnapshot pay: ds.child("payment").getChildren()){
                    totalPayment = totalPayment + Double.parseDouble(pay.getValue(Installment.class).getAmount());
                    counter++;
                }
            }
        }
        double balance = Double.parseDouble(total) - totalPayment;
        viewHolderClass.tvFPAmount.setText(total);
        viewHolderClass.tvFPDate.setText(date);
        viewHolderClass.tvFPDentist.setText(docName);
        viewHolderClass.tvFPLastUpdate.setText(dateUpdated);
        viewHolderClass.tvFPNoPayments.setText(String.valueOf(counter));
        viewHolderClass.tvFPBalance.setText(String.valueOf(balance));
        viewHolderClass.tvFPTotalPaid.setText(String.valueOf(totalPayment));
        viewHolderClass.tvPlanName.setText(planName);
        viewHolderClass.ibPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InstallmentBreakdownActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("paymentKey", fetchPatientPayment.get(position).getKey());
                Log.d(TAG, "paymentKey: " + fetchPatientPayment.get(position).getKey());
                Log.d(TAG, "position: " + position);
                context.startActivity(intent);
            }
        });
        viewHolderClass.ibPayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPaymentInstallmentActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("paymentKey", fetchPatientPayment.get(position).getKey());
                context.startActivity(intent);
            }
        });
        viewHolderClass.ibPayDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                Query query = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey).orderByChild("key").equalTo(paymentKey);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();

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
        });


    }

    @Override
    public int getItemCount() {
        return fetchPatientPayment.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvFPLastUpdate, tvFPDentist, tvFPDate, tvFPAmount, tvFPTotalPaid,tvFPBalance, tvFPNoPayments,tvPlanName;
        ImageButton ibPayView, ibPayDelete, ibPayEdit;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvFPLastUpdate = itemView.findViewById(R.id.tvFPLastUpdate);
            tvFPDentist = itemView.findViewById(R.id.tvInsDentist);
            tvFPDate = itemView.findViewById(R.id.tvFPDate);
            tvFPAmount = itemView.findViewById(R.id.tvFPAmount);
            ibPayDelete = itemView.findViewById(R.id.ibPayDelete);
            ibPayView = itemView.findViewById(R.id.ibPayView);
            ibPayEdit = itemView.findViewById(R.id.ibPayEdit);
            tvFPTotalPaid = itemView.findViewById(R.id.tvFPTotalPaid);
            tvFPBalance = itemView.findViewById(R.id.tvFPBalance);
            tvFPNoPayments = itemView.findViewById(R.id.tvFPNoPayments);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
        }
    }
}
