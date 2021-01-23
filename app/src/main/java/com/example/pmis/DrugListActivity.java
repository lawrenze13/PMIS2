package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.pmis.Adapter.DrugListAdapter;
import com.example.pmis.Model.Drugs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DrugListActivity extends AppCompatActivity {
    private FloatingActionButton fabAddDrug;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, drugRef;
    private String userID;
    private RecyclerView recyclerView;
    private DrugListAdapter drugListAdapter;
    private List<Drugs> drugsList;
    private SearchView searchDrugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar11);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Drug List");
        myToolbar.setTitleTextColor(getColor(R.color.white));
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchDrugs = (SearchView) findViewById(R.id.searchDrugs);
        fabAddDrug = (FloatingActionButton)findViewById(R.id.fabAddDrug);
        fabAddDrug.setOnClickListener(addDrug);
        recyclerView = (RecyclerView)findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        drugsList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        drugRef = mFirebaseDatabase.getReference("Drugs").child(userID);
        searchDrugs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == null || newText.length() == 0){
                    loadData("");
                }else{
                    loadData(newText);
                }
                return false;

            }
        });
        drugRef.keepSynced(true);
       loadData("");


    }

    private void loadData(String search) {
        String searchLower = search.toLowerCase();
        Query query = drugRef.orderByChild("sorter").startAt(searchLower).endAt(searchLower + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drugsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    Drugs drugs = ds.getValue(Drugs.class);
                    drugsList.add(drugs);

                }
                drugListAdapter = new DrugListAdapter(DrugListActivity.this,drugsList);
                recyclerView.setAdapter(drugListAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void cancel(View v){
        finish();
    }
    public final View.OnClickListener addDrug = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(DrugListActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.add_drug_dialog, null);
            final EditText etDrugName = (EditText)mView.findViewById(R.id.etDrugName);
            final EditText etDrugDosage = (EditText)mView.findViewById(R.id.etDrugDosage);
            final EditText etDrugBrand = (EditText)mView.findViewById(R.id.etDrugBrand);

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
                @Override
                public void onClick(View v) {
                    if(validate()){
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        userID = user.getUid();
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        myRef = mFirebaseDatabase.getReference("Drugs").child(userID);
                        String key = myRef.push().getKey();
                        Drugs drugs = new Drugs();
                        drugs.setDrugName(etDrugName.getText().toString().trim());
                        drugs.setDrugBrand(etDrugBrand.getText().toString().trim());
                        drugs.setDrugDosage(etDrugDosage.getText().toString().trim());
                        drugs.setSorter(etDrugName.getText().toString().trim().toLowerCase());
                        drugs.setKey(key);
                        myRef.child(key).setValue(drugs).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Drugs added successfully", Toast.LENGTH_LONG).show();
                                alertDialog.dismiss();
                                drugListAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Failed to add Drug Item, Please Try again.", Toast.LENGTH_LONG).show();
                            }
                        });
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

    };
    @Override
    public void onStart(){
        super.onStart();

    }
    @Override
    public void onStop(){
        super.onStop();

    }
}