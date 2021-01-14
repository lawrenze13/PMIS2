package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Adapter.DrugListAdapter;
import com.example.pmis.Adapter.ProcedureListAdapter;
import com.example.pmis.Model.Drugs;
import com.example.pmis.Model.Procedures;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProceduresActivity extends AppCompatActivity {
    private FloatingActionButton fabAddProcedures;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private RecyclerView rvProcedures;
    private ProcedureListAdapter procedureListAdapter;
    private List<Procedures> proceduresList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures);
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fabAddProcedures = findViewById(R.id.fabAddProcedures);
        fabAddProcedures.setOnClickListener(addProcedures);
        RecyclerView rvProcedures = (RecyclerView)findViewById(R.id.rvProcedures);
        rvProcedures.setLayoutManager(new LinearLayoutManager(this));
        proceduresList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                proceduresList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    Procedures procedures = ds.getValue(Procedures.class);
                    proceduresList.add(procedures);

                }
                procedureListAdapter = new ProcedureListAdapter(ProceduresActivity.this,proceduresList);
                rvProcedures.setAdapter(procedureListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public final View.OnClickListener addProcedures = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ProceduresActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.procedures_dialog, null);
            final TextView etPName = mView.findViewById(R.id.etPName);
            final TextView etPDescription = mView.findViewById(R.id.etPDescription);
            final TextView etPPrice = mView.findViewById(R.id.etPPrice);
            ImageButton btnPSubmit = (ImageButton) mView.findViewById(R.id.btnPSubmit);
            ImageButton btnPCancel = (ImageButton)mView.findViewById(R.id.btnPCancel);
            alert.setView(mView);
            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            btnPCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            btnPSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validate()){
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        userID = user.getUid();
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference("Procedures").child(userID);
                        String key = myRef.push().getKey();
                        String price = etPPrice.getText().toString().trim();
                        int fprice = Integer.parseInt(price);
                        Procedures procedures = new Procedures();
                        procedures.setName(etPName.getText().toString().trim());
                        procedures.setDescription(etPDescription.getText().toString().trim());
                        procedures.setPrice(fprice);
                        procedures.setKey(key);
                        myRef.child(key).setValue(procedures).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Procedure added successfully", Toast.LENGTH_LONG).show();
                                alertDialog.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Failed to add Procedure Item, Please Try again.", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
                private boolean validate() {
                    String procedureName = etPName.getText().toString().trim();
                    String procedureDescription = etPDescription.getText().toString().trim();
                    String procedurePrice = etPPrice.getText().toString().trim();
                    if(procedureName.isEmpty()){
                        etPName.setError("Procedure Name is required");
                        etPName.requestFocus();
                        return false;
                    }
                    if(procedureDescription.isEmpty()){
                        etPDescription.setError("Procedure Description is required");
                        etPDescription.requestFocus();
                        return false;
                    }
                    if(procedurePrice.isEmpty()){
                        etPPrice.setError("Price is required");
                        etPPrice.requestFocus();
                        return false;
                    }
                    return true;
                }
            });
            alertDialog.show();
        }


    };
}