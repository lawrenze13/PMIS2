package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.DrugPrescription;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PatientPrescriptionAdapter extends RecyclerView.Adapter {
    List<DrugPrescriptionMain> fetchPrescriptionMainList;
    public Context context;
    public String docName;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;

    public PatientPrescriptionAdapter(Context context,List<DrugPrescriptionMain> fetchPrescriptionMainList, String docName){
        this.fetchPrescriptionMainList = fetchPrescriptionMainList;
        this.context = context;
        this.docName = docName;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prescription_card_view, parent, false);
       ViewHolderClass viewHolderClass = new ViewHolderClass(view);
       return  viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        DrugPrescriptionMain drugPrescriptionMain = fetchPrescriptionMainList.get(position);
        viewHolderClass.tvPPresAdded.setText(drugPrescriptionMain.getDate());
        viewHolderClass.tvPPresDentist.setText(docName);
        viewHolderClass.tvPPresUpdate.setText(drugPrescriptionMain.getDateUpdated());

    }

    @Override
    public int getItemCount() {
        return fetchPrescriptionMainList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvPPresUpdate, tvPPresDentist, tvPPresAdded;
        ImageButton ibPPresPDF, ibPPresEdit, ibPPresDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvPPresUpdate = itemView.findViewById(R.id.tvPPresUpdate);
            tvPPresDentist = itemView.findViewById(R.id.tvPPresDentist);
            tvPPresAdded = itemView.findViewById(R.id.tvPPresAdded);
            ibPPresPDF = itemView.findViewById( R.id.ibPPresPDF);
            ibPPresEdit = itemView.findViewById( R.id.ibPPresEdit);
            ibPPresDelete = itemView.findViewById( R.id.ibPPresDelete);
        }
    }
}
