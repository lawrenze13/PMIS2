package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.pmis.Adapter.PatientListAdapter;
import com.example.pmis.Model.Patient;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class PatientActivity extends AppCompatActivity {
    private static final String TAG = "FIREBASE: " ;
    private FloatingActionButton fabAddDrug2;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, dataRef;
    private String userID;
    private RecyclerView rvPatient;
    private PatientListAdapter patientListAdapter;
    private List<Patient> patientList;
    private Patient patient;
    private ImageButton btnCancel2;
    private SearchView searchPatient;
    private EditText etSearch;
    private FirebaseRecyclerAdapter<Patient, PatientViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter !=null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        fabAddDrug2 = (FloatingActionButton)findViewById(R.id.fabAddDrug2);
        fabAddDrug2.setOnClickListener(addPatient);
        searchPatient = findViewById(R.id.searchPatient);
        searchPatient.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rvPatient = (RecyclerView)findViewById(R.id.rvPatient);
        rvPatient.setLayoutManager(new LinearLayoutManager(this));
        patientList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        dataRef = mFirebaseDatabase.getReference("Patient").child(userID);
        loadData("");
       // getPatientData();
    }

    private void loadData(String search) {
        String searchLower = search.toLowerCase();
        String searchUpper = search.toLowerCase();
        Query query = dataRef.orderByChild("sorter").startAt(searchLower).endAt(searchLower + "\uf8ff");
        query.keepSynced(true);

        FirebaseRecyclerOptions<Patient> options =
                new FirebaseRecyclerOptions.Builder<Patient>().setQuery(query,Patient.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Patient, PatientViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PatientViewHolder holder, int position, @NonNull Patient model) {
                holder.tvPAge.setText(model.getSex());
                String fullName = model.getFirstName() + " " + model.getLastName();
                holder.tvPFullName.setText(fullName);
                holder.cvPatient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(getApplicationContext(), PatientInformationActivity.class);
                        intent.putExtra("key",model.getKey());
                        startActivity(intent);
                    }
                });
                holder.btnPEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("smsto:" + model.getContactNo()));
                        startActivity(intent);
                    }
                });
                holder.btnPCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + model.getContactNo()));
                        startActivity(callIntent);
                    }
                });
            }

            @NonNull
            @Override
            public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_card_view, parent, false);
                return new PatientViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        rvPatient.setAdapter(firebaseRecyclerAdapter);
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {
        CardView cvPatient;
        TextView tvPFullName, tvPAge;
        ImageButton btnPEdit, btnPCall;
        View view;
        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvPFullName = (TextView) itemView.findViewById(R.id.tvPFullName);
            tvPAge = itemView.findViewById(R.id.tvPAge);
            btnPCall = itemView.findViewById(R.id.btnPCall);
            btnPEdit = itemView.findViewById( R.id.btnPEdit);
            cvPatient = itemView.findViewById( R.id.cvPatient);
        }

    }
    private void getPatientData() {
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Patient patient = ds.getValue(Patient.class);
                    patientList.add(patient);
                    Log.d(TAG, String.valueOf(patient));

                }
                patientListAdapter = new PatientListAdapter(PatientActivity.this,patientList);
                rvPatient.setAdapter(patientListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public final View.OnClickListener addPatient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PatientActivity.this, AddPatientActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
    };
//    public final View.OnClickListener searchPatient = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String searchLower = etSearch.getText().toString().trim().toLowerCase();
//            String searchUpper = etSearch.getText().toString().trim().toUpperCase();
//            patientListAdapter.getFilter().filter(searchLower);
//        }
//    };
}