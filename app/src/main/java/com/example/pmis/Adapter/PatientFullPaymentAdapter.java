package com.example.pmis.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.EditPatientPaymentActivity;
import com.example.pmis.Helpers.LoggedUserData;
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

public class PatientFullPaymentAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PAYMENT_ADAPTER";
    List<PatientPayment> fetchPatientPayment;
    public Context context;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;
    private StorageReference mStorageRef;
    private String patientKey, paymentKey;
    String  docName,  type,  method,  date,  total,  remarks,dateUpdated;

    public PatientFullPaymentAdapter(Context context, List<PatientPayment> fetchPatientPayment, String patientKey){
        this.fetchPatientPayment = fetchPatientPayment;
        this.context = context;
        this.patientKey = patientKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_full_payment_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientPayment patientPayment = fetchPatientPayment.get(position);
        paymentKey = patientPayment.getKey();

        docName = patientPayment.getDocName();
        type = patientPayment.getKey();
        method = patientPayment.getMethod();
        date = patientPayment.getDate();
        total = patientPayment.getTotal();
        remarks = patientPayment.getRemarks();
        dateUpdated = patientPayment.getDateUpdated();
        viewHolderClass.tvFPAmount.setText(total);
        viewHolderClass.tvFPDate.setText(date);
        viewHolderClass.tvFPDentist.setText(docName);
        viewHolderClass.tvFPLastUpdate.setText(dateUpdated);
        viewHolderClass.tvFPMethod.setText(method);
        viewHolderClass.ibPayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPatientPaymentActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("paymentKey", fetchPatientPayment.get(position).getKey());
                context.startActivity(intent);
            }
        });
        viewHolderClass.ibPayDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to Delete this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query deleteQuery = ref.child("Payments").child(patientKey).child("FULL PAYMENT").orderByChild("key").equalTo(paymentKey);
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
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();


            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        LoggedUserData userData = new LoggedUserData();
        DatabaseReference procedureRef = mFirebaseDatabase.getReference("Procedures").child(userData.userID()).child(patientPayment.getProcedureKey());
        procedureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewHolderClass.tvProcedure.setText(snapshot.getValue(Procedures.class).getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return fetchPatientPayment.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvFPLastUpdate, tvFPDentist, tvFPDate, tvFPMethod, tvFPAmount, tvProcedure;
        ImageButton ibPayEdit, ibPayDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvFPLastUpdate = itemView.findViewById(R.id.tvFPLastUpdate);
            tvFPDentist = itemView.findViewById(R.id.tvInsDentist);
            tvFPDate = itemView.findViewById(R.id.tvFPDate);
            tvFPMethod = itemView.findViewById(R.id.tvFPMethod);
            tvFPAmount = itemView.findViewById(R.id.tvFPAmount);
            ibPayEdit = itemView.findViewById(R.id.ibPayEdit);
            ibPayDelete = itemView.findViewById(R.id.ibPayDelete);
            tvProcedure = itemView.findViewById(R.id.tvProcedure);
        }
    }
}
