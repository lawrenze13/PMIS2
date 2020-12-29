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

import com.example.pmis.AddPatientPaymentActivity;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientProcedures;
import com.example.pmis.Model.Procedures;
import com.example.pmis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PatientProceduresAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PROD_ADAPTER";
    public Context context;
    public List<PatientProcedures> fetchPatientProceduresList;
    private String patientKey, userID;
    public String procedureDate, dateUpdated, procedure, procedureKey, mainKey;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    public PatientProceduresAdapter(Context context, List<PatientProcedures> fetchPatientProceduresList, String patientKey){
        this.context = context;
        this.fetchPatientProceduresList = fetchPatientProceduresList;
        this.patientKey = patientKey;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_procedure_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return  viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ViewHolderClass viewHolderClass = (ViewHolderClass) holder;
        PatientProcedures patientProcedures = fetchPatientProceduresList.get(position);
        mainKey = patientProcedures.getKey();
        procedureKey = patientProcedures.getProcedureKey();
        Log.d(TAG,procedureKey);
        Log.d(TAG,mainKey);
        viewHolderClass.tvPProcDateUpdated.setText(patientProcedures.getDateUpdated());
        viewHolderClass.tvPProcDate.setText(patientProcedures.getDate());
        viewHolderClass.tvPProcProcedure.setText(patientProcedures.getProcedure());
        viewHolderClass.ibPProcDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query deleteQuery = ref.child("PatientProcedure").child(patientKey).orderByChild("key").equalTo(patientProcedures.getKey());
                deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),"Item Deleted Succesfully", Toast.LENGTH_LONG).show();
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
        viewHolderClass.ibPProcPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference("Procedures").child(userID).child(procedureKey);
                myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Procedures procedures = new Procedures();
                                String name = snapshot.getValue(Procedures.class).getName();
                                String description = snapshot.getValue(Procedures.class).getDescription();
                                int price = snapshot.getValue(Procedures.class).getPrice();
                                Intent intent = new Intent(context, AddPatientPaymentActivity.class);
                                intent.putExtra("procedureName", name);
                                intent.putExtra("procedureDesc",description);
                                intent.putExtra("procedurePrice",price);
                                intent.putExtra("patientKey",patientKey);
                                intent.putExtra("procedureKey",procedureKey);
                                intent.putExtra("patientProcedureKey",mainKey);
                                context.startActivity(intent);
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
        return fetchPatientProceduresList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvPProcProcedure, tvPProcDateUpdated, tvPProcDate;
        ImageButton ibPProcPayment, ibPProcEdit, ibPProcDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPProcProcedure = itemView.findViewById(R.id.tvPProcProcedure);
            tvPProcDateUpdated = itemView.findViewById(R.id.tvPProcDateUpdated);
            tvPProcDate = itemView.findViewById(R.id.tvPProcDate);
            ibPProcPayment = itemView.findViewById( R.id.ibPProcPayment);
            ibPProcEdit = itemView.findViewById( R.id.ibPProcEdit);
            ibPProcDelete = itemView.findViewById( R.id.ibPProcDelete);
        }
    }
}
