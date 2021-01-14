package com.example.pmis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pmis.AddMedicalHistoryActivity;
import com.example.pmis.Model.MedicalHistory;
import com.example.pmis.Model.PatientPhotos;
import com.example.pmis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PatientPhotoAdapter extends RecyclerView.Adapter{
    private static final String TAG = "Patient Photos";
    List<PatientPhotos> fetchPatientPhotos;
    public Context context;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference presRef;
    private StorageReference mStorageRef;
    private String patientKey, medicalHistoryKey;

    public PatientPhotoAdapter(Context context, List<PatientPhotos> fetchPatientPhotos, String patientKey){
        this.fetchPatientPhotos = fetchPatientPhotos;
        this.context = context;
        this.patientKey = patientKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medical_history_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientPhotos patientPhotos = fetchPatientPhotos.get(position);
        medicalHistoryKey = patientPhotos.getKey();
        viewHolderClass.tvMedDate.setText(patientPhotos.getDate());
        viewHolderClass.tvMedCaption.setText(patientPhotos.getCaption());
        StorageReference medicalHistoryRef = mStorageRef.child("images/patientPhotos/"+ patientKey + '/' + patientPhotos.getImageUrl());

        medicalHistoryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "download url is: " + uri.toString());

                Glide
                        .with(context)
                        .asBitmap()
                        .load(uri)
                        .centerCrop()
                        .into(viewHolderClass.ivMedThumbnail);
            }
        });
        viewHolderClass.ibMedDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query deleteQuery = ref.child("PatientPhotos").child(patientKey).orderByChild("key").equalTo(patientPhotos.getKey());
                deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();
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
        viewHolderClass.ibMedEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddMedicalHistoryActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("action", "edit");

                intent.putExtra("medicalHistoryKey", fetchPatientPhotos.get(position).getKey());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fetchPatientPhotos.size();
    }
    public static class ViewHolderClass extends RecyclerView.ViewHolder {
        ImageView ivMedThumbnail;
        TextView tvMedCaption, tvMedDate;
        ImageButton ibMedEdit, ibMedDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            ivMedThumbnail = itemView.findViewById(R.id.ivMedThumbnail);
            tvMedCaption = itemView.findViewById(R.id.tvMedCaption);
            tvMedDate = itemView.findViewById(R.id.tvMedDate);
            ibMedDelete = itemView.findViewById(R.id.ibMedDelete);
            ibMedEdit = itemView.findViewById(R.id.ibMedEdit);
        }
    }
}
