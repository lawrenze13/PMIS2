package com.example.pmis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Patient;
import com.example.pmis.PatientInformationActivity;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PatientListAdapter extends RecyclerView.Adapter implements Filterable {
    private static final String TAG = "PATIENT_ADAPTER: " ;
    List<Patient> fetchPatientList;
    List<Patient> fetchAllPatientList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;
    public PatientListAdapter(Context context,List<Patient> fetchPatientList){
        this.fetchPatientList = fetchPatientList;
        this.context = context;
        this.fetchAllPatientList = fetchPatientList;
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
        String contactNo = patient.getContactNo();
        String fullName = firstName + ' ' + middleName + ' ' + lastName;
        String birthDate = patient.getBirthDate();
        String key = patient.getKey();
        viewHolderClass.tvPFullName.setText(fullName);
        viewHolderClass.tvPAge.setText(birthDate);
        int currentPosition = position;
        viewHolderClass.btnPEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:" + contactNo));
                context.startActivity(intent);
            }
        });
        viewHolderClass.btnPCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactNo));
                context.startActivity(callIntent);
            }
        });
        viewHolderClass.cvPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, PatientInformationActivity.class);
                intent.putExtra("key",key);
                context.startActivity(intent);
            }
        });
    }
//    public void filter(String queryText){
//        fetchPatientList.clear();
//        if(queryText.isEmpty()){
//            fetchPatientList.addAll(fetchAllPatientList);
//        }
//        else{
//            for(Patient patient: fetchAllPatientList){
//                if(patient.getFirstName().toLowerCase().contains(queryText.toLowerCase())){
//                    fetchPatientList.add(patient);
//                }
//            }
//        }
//        notifyDataSetChanged();
//    }
    @Override
    public int getItemCount() {
        return fetchPatientList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint ) {
           List<Patient> filteredList = new ArrayList<>();
           filteredList.clear();
           if(constraint == null || constraint.length() == 0){
               filteredList.addAll(fetchAllPatientList);
               for(int i = 0; i < fetchAllPatientList.size(); i++) {
                   Log.d(TAG, "fetchAllPatientList: " + fetchAllPatientList.get(i).getFirstName());
               }
           }else{
               String filterPattern = constraint.toString().toLowerCase().trim();
               for(Patient patient: fetchAllPatientList){
                   if(patient.getFirstName().toLowerCase().contains(filterPattern) || patient.getLastName().toLowerCase().contains(filterPattern)){
                       filteredList.add(patient);
                       Log.d(TAG, "patient: " + patient.getFirstName());
                   }
               }
           }
           FilterResults results = new FilterResults();
           results.values = filteredList;
           return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
                fetchPatientList.clear();
                fetchPatientList.addAll((Collection<? extends Patient>) results.values);
                notifyDataSetChanged();
        }
    };

    public class ViewHolderClass extends RecyclerView.ViewHolder {
        CardView cvPatient;
        TextView tvPFullName, tvPAge;
        ImageButton btnPEdit, btnPCall;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPFullName = itemView.findViewById(R.id.tvPFullName);
            tvPAge = itemView.findViewById(R.id.tvPAge);
            btnPCall = itemView.findViewById(R.id.btnPCall);
            btnPEdit = itemView.findViewById( R.id.btnPEdit);
            cvPatient = itemView.findViewById( R.id.cvPatient);

        }
    }
}
