package com.example.pmis.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.AddPrescriptionActivity;
import com.example.pmis.Model.DrugPrescription;
import com.example.pmis.Model.Drugs;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DrugPrescriptionAdapter extends RecyclerView.Adapter {
    List<DrugPrescription> fetchDrugsPrescriptionList;
    private List<Drugs> drugsList;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public DrugPrescriptionAdapter(Context context,List<DrugPrescription> fetchDrugsPrescriptionList){
        this.fetchDrugsPrescriptionList = fetchDrugsPrescriptionList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drug_description_card_view, parent,false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
       ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        DrugPrescription drugPrescription = fetchDrugsPrescriptionList.get(position);
        viewHolderClass.tvDDescName.setText(drugPrescription.getDrugInfo());
        viewHolderClass.tvDDescDuration.setText(drugPrescription.getDuration());
        viewHolderClass.tvDDescFrequency.setText(drugPrescription.getFrequency());
        viewHolderClass.btnDDescDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDrugsPrescriptionList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,fetchDrugsPrescriptionList.size());
                Toast.makeText(context,"Item Deleted Successfully", Toast.LENGTH_LONG).show();
            }
        });
        viewHolderClass.btnDDescEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View mView = inflater.inflate(R.layout.add_drug_prescription_dialog, null);
                String dDescInfo = drugPrescription.getDrugInfo();
                String dDescFrequency = drugPrescription.getFrequency();
                String dDescDuration = drugPrescription.getDuration();
                final Spinner spinDrugInfo = (Spinner) mView.findViewById(R.id.spinDrugInfo);
                final EditText etFrequency = (EditText)mView.findViewById(R.id.etFrequency);
                final EditText etDuration = (EditText)mView.findViewById(R.id.etDuration);

                etFrequency.setText(dDescFrequency);
                etDuration.setText(dDescDuration);

                drugsList = new ArrayList<>();
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                drugRef = mFirebaseDatabase.getReference("Drugs").child(userID);
                drugRef.keepSynced(true);
                drugRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final List<String> drugsInfoList = new ArrayList<String>();

                        drugsList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){

                            Drugs drugs = ds.getValue(Drugs.class);
                            drugsList.add(drugs);
                            String drugName = drugs.getDrugName();
                            String drugBrand = drugs.getDrugBrand();
                            String drugDosage = drugs.getDrugDosage();
                            String drugInfo = drugName + " (" + drugBrand + ") " + drugDosage;
                            drugsInfoList.add(drugInfo);
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, drugsInfoList);
                        spinDrugInfo.setAdapter(arrayAdapter);
                        spinDrugInfo.setSelection(arrayAdapter.getPosition(dDescInfo));


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                ImageButton btnDrugSubmit = (ImageButton) mView.findViewById(R.id.btnDrugSubmit);
                ImageButton btnDrugCancel = (ImageButton)mView.findViewById(R.id.btnDrugCancel);

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
                    private static final String TAG = "weorowe";

                    @Override
                    public void onClick(View v) {
                        DrugPrescription newDrugPrescription = new DrugPrescription();

                        if(validate()){
                            String dDescInfo = spinDrugInfo.getSelectedItem().toString().trim();

                            String dDescFrequency = etFrequency.getText().toString().trim();
                            String dDescDuration = etDuration.getText().toString().trim();
                            newDrugPrescription.setDrugInfo(dDescInfo);

                            newDrugPrescription.setFrequency(dDescFrequency);
                            newDrugPrescription.setDuration(dDescDuration);
                            fetchDrugsPrescriptionList.set(position, newDrugPrescription);
                            notifyItemChanged(position);
                            alertDialog.dismiss();
                            Toast.makeText(context, "Item updated Successfully", Toast.LENGTH_LONG).show();


                        }
                    }
                    private boolean validate() {
                        String dDescInfo = spinDrugInfo.getSelectedItem().toString().trim();

                        String dDFrequency = etFrequency.getText().toString().trim();
                        String dDDuration = etDuration.getText().toString().trim();

                        if(dDFrequency.isEmpty()){
                            etFrequency.setError("Frequency and Dosage is required");
                            etFrequency.requestFocus();
                            return false;
                        }
                        if(dDDuration.isEmpty()){
                            etDuration.setError("Duration is required");
                            etDuration.requestFocus();
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
        return fetchDrugsPrescriptionList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvDDescName, tvDDescFrequency, tvDDescDuration;
        ImageButton btnDDescEdit, btnDDescDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvDDescName = itemView.findViewById(R.id.tvDDescName);
            tvDDescFrequency = itemView.findViewById(R.id.tvDDescFrequency);
            tvDDescDuration = itemView.findViewById(R.id.tvDDescDuration);
            btnDDescEdit = itemView.findViewById( R.id.btnDDescEdit);
            btnDDescDelete = itemView.findViewById( R.id.btnDDescDelete);

        }
    }
}
