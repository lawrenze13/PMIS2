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

import com.example.pmis.EditPatientPaymentActivity;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.Procedures;
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

public class PatientInstallmentBreakdownAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PAYMENT_ADAPTER";
    List<Installment> fetchInstallment;
    public Context context;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;
    private StorageReference mStorageRef;
    private String patientKey, installmentKey, paymentKey;
    String  docName,  type,  method,  date,  amount,  remarks,dateUpdated;

    public PatientInstallmentBreakdownAdapter(Context context, List<Installment> fetchInstallment, String patientKey, String paymentKey){
        this.fetchInstallment = fetchInstallment;
        this.context = context;
        this.patientKey = patientKey;
        this.paymentKey = paymentKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.installment_breakdown_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        Installment installment = fetchInstallment.get(position);
        installmentKey = installment.getKey();
        method = installment.getMethod();
        date = installment.getDate();
        amount = installment.getAmount();
        remarks = installment.getRemarks();
        dateUpdated = installment.getDateUpdated();
        viewHolderClass.tvInsAmount.setText(amount);
        viewHolderClass.tvInsDate.setText(date);
        viewHolderClass.tvInsLastUpdate.setText(dateUpdated);
        viewHolderClass.tvInsMethod.setText(method);
        viewHolderClass.tvInsRemarks.setText(remarks);
        viewHolderClass.ibPayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPatientPaymentActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("paymentKey", fetchInstallment.get(position).getKey());
                Log.d(TAG, "installmentKey " + fetchInstallment.get(position).getKey());
                context.startActivity(intent);
            }
        });
        viewHolderClass.ibPayDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query deleteQuery = ref.child("Payments").child(patientKey).child("FULL PAYMENT").child(paymentKey).child("payment").orderByChild("key").equalTo(fetchInstallment.get(position).getKey());;
                deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
        return fetchInstallment.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvInsLastUpdate, tvInsDate, tvInsMethod, tvInsAmount, tvInsRemarks;
        ImageButton ibPayEdit, ibPayDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvInsLastUpdate = itemView.findViewById(R.id.tvInsLastUpdate);
            tvInsDate = itemView.findViewById(R.id.tvInsDate);
            tvInsMethod = itemView.findViewById(R.id.tvInsMethod);
            tvInsAmount = itemView.findViewById(R.id.tvInsAmount);
            tvInsRemarks = itemView.findViewById(R.id.tvInsRemarks);
            ibPayEdit = itemView.findViewById(R.id.ibPayEdit);
            ibPayDelete = itemView.findViewById(R.id.ibPayDelete);

        }
    }
}
