package com.example.pmis.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Drugs;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
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

public class CancelledListAdapter extends RecyclerView.Adapter{
    private static final String TAG = "CANCELLED_ADAPTER";
    List<PatientScheduleFacade> fetchPatientScheduleFacade;
    List<String> patientKeyList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private List<PatientScheduleFacade> appointmentList;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public CancelledListAdapter(Context context, List<String> patientKeyList,List<PatientScheduleFacade> fetchPatientScheduleFacade){
        this.fetchPatientScheduleFacade = fetchPatientScheduleFacade;
        this.context = context;
        this.patientKeyList = patientKeyList;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cancelled_card_view, parent,false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        String patientKey = patientKeyList.get(position);
        appointmentList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Patient").child(userID).child(patientKey);
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.getValue(Patient.class).getFirstName() + " " + snapshot.getValue(Patient.class).getLastName();
                viewHolderClass.tvName.setText(fullName);
                int c = 0;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        appointmentList.clear();
        for(int i = 0; i< fetchPatientScheduleFacade.size(); i++){
            PatientScheduleFacade p = fetchPatientScheduleFacade.get(i);
            if(patientKeyList.get(position).equals(p.getPatientKey())){
                appointmentList.add(p);
            }
        }
        Log.d(TAG,"appointmentList size: " +appointmentList.size());
        String msgCount = appointmentList.size() + " Cancelled Appointment(s)";
        viewHolderClass.tvCount.setText(msgCount);
        CancelledDatesAdapter cancelledDatesAdapter = new CancelledDatesAdapter(context, appointmentList);
        viewHolderClass.rvCancelledDates.setAdapter(cancelledDatesAdapter);
    }

    @Override
    public int getItemCount() {
        return patientKeyList.size();
    }
    private class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        RecyclerView rvCancelledDates;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCount = itemView.findViewById(R.id.tvCount);
            rvCancelledDates = itemView.findViewById(R.id.rvCancelledDates);
            rvCancelledDates.setLayoutManager(new LinearLayoutManager(context));


        }
    }
}
