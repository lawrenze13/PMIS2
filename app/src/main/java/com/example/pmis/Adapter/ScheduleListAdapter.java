package com.example.pmis.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pmis.AppointmentFragment;
import com.example.pmis.EditScheduleActivity;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.PatientInformationActivity;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleListAdapter extends RecyclerView.Adapter {
    private static final String TAG = "SCHEDULE_ADAPTER";
    List<PatientScheduleFacade> fetchPatientScheduleFacadeList;
    public Context context;
    private  String selectedItem = "";
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

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String currentDate = df.format(c);
        LoggedUserData loggedUserData = new LoggedUserData();
        String userID = loggedUserData.userID();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ViewHolderClass viewHolderClass = (ViewHolderClass)holder;
        PatientScheduleFacade patientScheduleFacade = fetchPatientScheduleFacadeList.get(position);
       // String fullName = patientScheduleFacade.getPatientName();
        String date = patientScheduleFacade.getDate();
        String startTime = patientScheduleFacade.getStartTime();
        String endTime = patientScheduleFacade.getEndTime();
        String note = patientScheduleFacade.getNote();
        String patientKey = patientScheduleFacade.getPatientKey();
        String scheduleKey = patientScheduleFacade.getScheduleKey();
        String status = patientScheduleFacade.getStatus();
        viewHolderClass.btnAction.setText(status);
        try {
            Date date1 = df.parse(date);
            Date date2 = df.parse(currentDate);
            Log.d(TAG, "COMPARE DATE : " + date1 + " " + date2);
            if(date1.before(date2) || date1.equals(date2)){
                viewHolderClass.btnAction.setVisibility(View.VISIBLE);
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("AppointmentStatus").child(fetchPatientScheduleFacadeList.get(position).getScheduleKey());
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            viewHolderClass.btnAction.setText(snapshot.getValue(AppointmentStatus.class).getStatus());
                            switch (snapshot.getValue(AppointmentStatus.class).getStatus()){
                                case "Completed":
                                    viewHolderClass.btnAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24,0,0,0);
                                    case "Pending":
                                    viewHolderClass.btnAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pending_actions_24,0,0,0);
                                    case "Canceled":
                                    viewHolderClass.btnAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_cancel_24,0,0,0);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else{
                viewHolderClass.btnAction.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Patient").child(userID).child(patientKey);
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.getValue(Patient.class).getFirstName() + " " + snapshot.getValue(Patient.class).getMiddleName() + " " + snapshot.getValue(Patient.class).getLastName();
                viewHolderClass.tvSCPatientName.setText(fullName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewHolderClass.tvSCPatientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PatientInformationActivity.class);
                intent.putExtra("key", fetchPatientScheduleFacadeList.get(position).getPatientKey());
                context.startActivity(intent);
            }
        });
        viewHolderClass.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentStatus = viewHolderClass.btnAction.getText().toString().trim();
                int statusPosition = 0;
                String[] status = {"Pending","Completed", "Cancelled"};
                for(int i = 0; i<status.length; i++){
                    if (status[i].equals(currentStatus)){
                        statusPosition = i;

                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Set Appointment Status");
                builder.setSingleChoiceItems(status,statusPosition , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem = status[which];

                    }
                });
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewHolderClass.btnAction.setText(selectedItem);
                        AppointmentStatus appointmentStatus = new AppointmentStatus();
                        appointmentStatus.setStatus(selectedItem);
                        appointmentStatus.setScheduleKey(fetchPatientScheduleFacadeList.get(position).getScheduleKey());
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("AppointmentStatus").child(userID).child(fetchPatientScheduleFacadeList.get(position).getScheduleKey());
                        myRef.setValue(appointmentStatus);
                        Toast.makeText(context,"Status changed Succesfully.", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to Delete this item?");
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query deleteQuery = ref.child("Schedules").child(userID).orderByChild("key").equalTo(scheduleKey);
                        deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Query delete = ref.child("AppointmentStatus").child(userID).orderByChild("scheduleKey").equalTo(scheduleKey);
                                            delete.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for(DataSnapshot status : snapshot.getChildren()){
                                                        status.getRef().removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            Toast.makeText(v.getContext(),"Item Deleted Successfully", Toast.LENGTH_LONG).show();
                                            fetchPatientScheduleFacadeList.remove(position);
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
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });
        viewHolderClass.ibSCEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditScheduleActivity.class);
                intent.putExtra("patientKey", patientKey);
                intent.putExtra("scheduleKey", scheduleKey);
                intent.putExtra("fullName", viewHolderClass.tvSCPatientName.getText().toString().trim());

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
        Button btnAction;
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
            btnAction = itemView.findViewById( R.id.btnAction);

        }
    }
}
