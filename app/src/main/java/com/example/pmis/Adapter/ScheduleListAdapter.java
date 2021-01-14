package com.example.pmis.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.AppointmentFragment;
import com.example.pmis.EditScheduleActivity;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.PatientInformationActivity;
import com.example.pmis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ScheduleListAdapter extends RecyclerView.Adapter {
    List<PatientScheduleFacade> fetchPatientScheduleFacadeList;
    public Context context;

    public ScheduleListAdapter(Context context, List<PatientScheduleFacade> fetchPatientScheduleFacadeList){
        this.fetchPatientScheduleFacadeList = fetchPatientScheduleFacadeList;
        this.context = context;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_card_view, parent, false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientScheduleFacade patientScheduleFacade = fetchPatientScheduleFacadeList.get(position);
        String fullName = patientScheduleFacade.getPatientName();
        String date = patientScheduleFacade.getDate();
        String startTime = patientScheduleFacade.getStartTime();
        String endTime = patientScheduleFacade.getEndTime();
        String note = patientScheduleFacade.getNote();
        String patientKey = patientScheduleFacade.getPatientKey();
        String scheduleKey = patientScheduleFacade.getScheduleKey();
        viewHolderClass.tvSCPatientName.setText(fullName);
        viewHolderClass.tvSCPatientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PatientInformationActivity.class);
                intent.putExtra("key", fetchPatientScheduleFacadeList.get(position).getPatientKey());
                context.startActivity(intent);
            }
        });
        viewHolderClass.tvSCDate.setText(date);
        viewHolderClass.tvSCStart.setText(startTime);
        viewHolderClass.tvSCEnd.setText(endTime);
        viewHolderClass.tvSCNote.setText(note);
        viewHolderClass.ibSCCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + patientScheduleFacade.getContactNo()));
                context.startActivity(callIntent);
            }
        });
        viewHolderClass.ibSCMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:" + patientScheduleFacade.getContactNo()));
                context.startActivity(intent);
            }
        });
        viewHolderClass.ibSCDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query deleteQuery = ref.child("Schedules").child(patientKey).orderByChild("key").equalTo(scheduleKey);
                deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();
                                 
                                   notifyItemRemoved(position);
                                   notifyItemRangeChanged(position, fetchPatientScheduleFacadeList.size());
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
        viewHolderClass.ibSCEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditScheduleActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("scheduleKey", scheduleKey);
                intent.putExtra("fullName", fullName);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fetchPatientScheduleFacadeList.size();
    }
    public class ViewHolderClass extends RecyclerView.ViewHolder {
        CardView cvPatient;
        TextView tvSCPatientName, tvSCDate, tvSCStart, tvSCEnd, tvSCNote;
        ImageButton ibSCCall, ibSCMessage, ibSCEdit, ibSCDelete;
        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            tvSCPatientName = itemView.findViewById(R.id.tvSCPatientName);
            tvSCDate = itemView.findViewById(R.id.tvSCDate);
            tvSCStart = itemView.findViewById(R.id.tvSCStart);
            tvSCEnd = itemView.findViewById( R.id.tvSCEnd);
            tvSCNote = itemView.findViewById( R.id.tvSCNote);

            ibSCCall = itemView.findViewById( R.id.ibSCCall);
            ibSCMessage = itemView.findViewById( R.id.ibSCMessage);
            ibSCEdit = itemView.findViewById( R.id.ibSCEdit);
            ibSCDelete = itemView.findViewById( R.id.ibSCDelete);

        }
    }
}
