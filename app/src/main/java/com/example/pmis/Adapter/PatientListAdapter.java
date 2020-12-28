package com.example.pmis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Patient;
import com.example.pmis.PatientInformationActivity;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PatientListAdapter extends RecyclerView.Adapter {
    List<Patient> fetchPatientList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public PatientListAdapter(Context context,List<Patient> fetchPatientList){
        this.fetchPatientList = fetchPatientList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        Patient patient = fetchPatientList.get(position);
        String firstName = patient.getFirstName();
        String middleName = patient.getMiddleName();
        String lastName = patient.getLastName();
        String fullName = firstName + ' ' + middleName + ' ' + lastName;
        String birthDate = patient.getBirthDate();
        String key = patient.getKey();
        viewHolderClass.tvPFullName.setText(fullName);
        viewHolderClass.tvPAge.setText(birthDate);
        int currentPosition = position;
        viewHolderClass.btnPEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, PatientInformationActivity.class);
                intent.putExtra("key",key);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fetchPatientList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvPFullName, tvPAge;
        ImageButton btnPEdit, btnPCall;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPFullName = itemView.findViewById(R.id.tvPFullName);
            tvPAge = itemView.findViewById(R.id.tvPAge);
            btnPCall = itemView.findViewById(R.id.btnPCall);
            btnPEdit = itemView.findViewById( R.id.btnPEdit);

        }
    }
}
