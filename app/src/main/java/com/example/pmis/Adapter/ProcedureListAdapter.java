package com.example.pmis.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.Model.Patient;
import com.example.pmis.Model.Procedures;
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

public class ProcedureListAdapter extends RecyclerView.Adapter {
    List<Procedures> fetchProcedureList;
    String name;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef, drugRef;
    private String userID;
    public Context context;

    public ProcedureListAdapter(Context context,List<Procedures> fetchProcedureList){
        this.fetchProcedureList = fetchProcedureList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.procedures_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        Procedures procedures = fetchProcedureList.get(position);
        String name = procedures.getName();
        String description = procedures.getDescription();
        int price = procedures.getPrice();
        String sprice = "P. " + Double.toString(price);
        String key = procedures.getKey();
        viewHolderClass.tvCName.setText(name);
        viewHolderClass.tvCDescription.setText(description);
        viewHolderClass.tvCPrice.setText(sprice);
        viewHolderClass.btnCDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = procedures.getKey();
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                Query deleteQuery = ref.child("Procedures").child(userID).orderByChild("key").equalTo(key);
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

    }

    @Override
    public int getItemCount() {
        return fetchProcedureList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        TextView tvCName, tvCDescription, tvCPrice;
        ImageButton btnCEdit, btnCDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvCName = itemView.findViewById(R.id.tvCName);
            tvCDescription = itemView.findViewById(R.id.tvCDescription);
            tvCPrice = itemView.findViewById(R.id.tvCPrice);
            btnCEdit = itemView.findViewById(R.id.btnCEdit);
            btnCDelete = itemView.findViewById( R.id.btnCDelete);

        }
    }
}
