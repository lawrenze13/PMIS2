package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Drugs;
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

public class DrugListAdapter extends RecyclerView.Adapter {
    List<Drugs> fetchDrugsList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public DrugListAdapter(Context context,List<Drugs> fetchDrugsList){
        this.fetchDrugsList = fetchDrugsList;
        this.context = context;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drug_list_cardview, parent,false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        Drugs drugs = fetchDrugsList.get(position);
        viewHolderClass.tvDrugName.setText(drugs.getDrugName());
        viewHolderClass.tvDrugBrand.setText(drugs.getDrugBrand());
        viewHolderClass.tvDrugDosage.setText(drugs.getDrugDosage());
        int currentPosition = position;

        viewHolderClass.btnDeleteDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = drugs.getKey();
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                Query deleteQuery = ref.child("Drugs").child(userID).orderByChild("key").equalTo(key);
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
        viewHolderClass.btnEditDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                 LayoutInflater inflater = LayoutInflater.from(context);
                View mView = inflater.inflate(R.layout.add_drug_dialog, null);
                String drugName = drugs.getDrugName();
                String drugBrand = drugs.getDrugBrand();
                String drugDosage = drugs.getDrugDosage();
                String key = drugs.getKey();

                final EditText etDrugName = (EditText)mView.findViewById(R.id.etDrugName);
                final EditText etDrugDosage = (EditText)mView.findViewById(R.id.etDrugDosage);
                final EditText etDrugBrand = (EditText)mView.findViewById(R.id.etDrugBrand);
                ImageButton btnDrugSubmit = (ImageButton) mView.findViewById(R.id.btnDrugSubmit);
                ImageButton btnDrugCancel = (ImageButton)mView.findViewById(R.id.btnDrugCancel);
                etDrugName.setText(drugName);
                etDrugBrand.setText(drugBrand);
                etDrugDosage.setText(drugDosage);
                alert.setView(mView);
                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);

                btnDrugCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                btnDrugSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(validate()){
                            mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            mFirebaseDatabase = FirebaseDatabase.getInstance();
                            myRef = mFirebaseDatabase.getReference("Drugs").child(userID).child(key);

                            Drugs drugs = new Drugs();
                            drugs.setDrugName(etDrugName.getText().toString().trim());
                            drugs.setDrugBrand(etDrugBrand.getText().toString().trim());
                            drugs.setDrugDosage(etDrugDosage.getText().toString().trim());
                            drugs.setKey(key);
                            myRef.child("drugName").setValue(drugs.drugName);
                            myRef.child("drugBrand").setValue(drugs.drugBrand);
                            myRef.child("drugDosage").setValue(drugs.drugDosage);
                            alertDialog.dismiss();
                            Toast.makeText(context,"Update successful", Toast.LENGTH_LONG).show();
                        }

                    }
                    private boolean validate() {
                        String drugName = etDrugName.getText().toString().trim();
                        String drugBrand = etDrugBrand.getText().toString().trim();
                        String drugDosage = etDrugDosage.getText().toString().trim();
                        if(drugName.isEmpty()){
                            etDrugName.setError("Drug Name is required");
                            etDrugName.requestFocus();
                            return false;
                        }
                        if(drugBrand.isEmpty()){
                            etDrugBrand.setError("Drug Brand is required");
                            etDrugBrand.requestFocus();
                            return false;
                        }
                        if(drugDosage.isEmpty()){
                            etDrugDosage.setError("Drug Dosage is required");
                            etDrugDosage.requestFocus();
                            return false;
                        }
                        return true;
                    }


                });
                alertDialog.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return fetchDrugsList.size();
    }

    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvDrugName, tvDrugBrand, tvDrugDosage;
        ImageButton btnEditDrug, btnDeleteDrug;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(R.id.tvDDescName);
            tvDrugBrand = itemView.findViewById(R.id.tvDrugBrand);
            tvDrugDosage = itemView.findViewById(R.id.tvDrugDosage);
            btnEditDrug = itemView.findViewById(R.id.btnEditDrug);
            btnDeleteDrug = itemView.findViewById( R.id.btnDeleteDrug);

        }
    }
}
