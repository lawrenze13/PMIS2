package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.PatientProcedures;
import com.example.pmis.Model.Procedures;
import com.example.pmis.Model.ReportAppointment;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReportProceduresAdapter extends RecyclerView.Adapter{
    private static final String TAG = "REPORT_PAYMENT_ADAPTER";
    List<PatientProcedures> fetchPatientProcedures;
    List<Procedures> proceduresList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public ReportProceduresAdapter(Context context,List<PatientProcedures> fetchPatientProcedures, List<Procedures> proceduresList){
        this.proceduresList = proceduresList;
        this.fetchPatientProcedures = fetchPatientProcedures;
        this.context = context;

    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_procedures_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientProcedures patientProcedures = fetchPatientProcedures.get(position);
        viewHolderClass.tvRPName.setText(patientProcedures.getKey());
        viewHolderClass.tvRPDate.setText(patientProcedures.getDate());
        viewHolderClass.tvRPProcedure.setText(patientProcedures.getProcedure());
        for(Procedures procedures: proceduresList){
            if(procedures.getKey().equals(patientProcedures.getProcedureKey())){
                viewHolderClass.tvRPAmount.setText(String.valueOf(procedures.getPrice()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return fetchPatientProcedures.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvRPName, tvRPDate, tvRPProcedure, tvRPAmount;

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvRPName = itemView.findViewById(R.id.tvRAName);
            tvRPDate = itemView.findViewById(R.id.tvRPDate);
            tvRPProcedure = itemView.findViewById(R.id.tvRPProcedure);
            tvRPAmount = itemView.findViewById(R.id.tvRPAmount);

        }
    }
}
