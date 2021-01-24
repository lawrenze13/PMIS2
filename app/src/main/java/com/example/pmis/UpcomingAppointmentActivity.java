package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.pmis.Adapter.PatientListAdapter;
import com.example.pmis.Adapter.ScheduleListAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.Model.Schedule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingAppointmentActivity extends AppCompatActivity {
    private static final String TAG = "UPCOMING";
    private RecyclerView rvUpcoming;
    private String userID;
    private LoggedUserData loggedUserData;
    private FirebaseDatabase mFirebaseDatabase ;
    private DatabaseReference myRef;
    private List<PatientScheduleFacade> scheduleList;
    private ScheduleListAdapter scheduleListAdapter;
    private FirebaseAuth mAuth;
    private   String patientName, schedDate, startTime, endTime, note, contactNo, patientKey, scheduleKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_appointment);
        rvUpcoming = findViewById(R.id.rvUpcoming);
        scheduleList = new ArrayList<PatientScheduleFacade>();
        rvUpcoming.setLayoutManager(new LinearLayoutManager(UpcomingAppointmentActivity.this));
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Date date = new Date(new Date().getTime() +  + 86400000);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
        String curDate = format.format(date);
        long timeStamp = 0;
        try {
            Date dateTime = format.parse(curDate);
            timeStamp = dateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> patientKeyList = new ArrayList<>();
                for(DataSnapshot patientSnapshot: snapshot.getChildren()) {
                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                     String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                    Log.d(TAG, "patientName: " + patientKey + " " + patientKey);
                    contactNo = patientSnapshot.getValue(Patient.class).getContactNo();
                   Query keyRef = mFirebaseDatabase.getReference("Schedules").child(userID).orderByChild("timeStamp");
                    keyRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot schedule : snapshot.getChildren()) {
                                    if (schedule.getValue(Schedule.class).getPatientKey() != null) {
                                        if (schedule.getValue(Schedule.class).getPatientKey().equals(patientKey)) {
                                            Date c = Calendar.getInstance().getTime();
                                            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                            String currentDate = df.format(c);
                                            String dbDate = schedule.getValue(Schedule.class).getDate();
                                            try {
                                                Date date1 = df.parse(dbDate);
                                                Date date2 = df.parse(currentDate);
                                                Log.d(TAG, "DATES  : " + date1 + " " + date2);
                                                if (date1.compareTo(date2) > 0) {
                                                    schedDate = schedule.getValue(Schedule.class).getDate();
                                                    startTime =  schedule.getValue(Schedule.class).getStartTime();
                                                    endTime =  schedule.getValue(Schedule.class).getEndTime();
                                                    note =  schedule.getValue(Schedule.class).getRemarks();
                                                    scheduleKey = schedule.getValue(Schedule.class).getKey();
                                                    PatientScheduleFacade patientScheduleFacade = new PatientScheduleFacade();
                                                    patientScheduleFacade.setPatientName(patientName);
                                                    patientScheduleFacade.setContactNo(contactNo);
                                                    patientScheduleFacade.setDate(schedDate);
                                                    patientScheduleFacade.setEndTime(endTime);
                                                    patientScheduleFacade.setStartTime(startTime);
                                                    patientScheduleFacade.setNote(note);
                                                    patientScheduleFacade.setPatientKey(patientKey);
                                                    patientScheduleFacade.setScheduleKey(scheduleKey);
                                                    DatabaseReference statusRef = mFirebaseDatabase.getReference("AppointmentStatus").child(userID).child(scheduleKey);
                                                    statusRef.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.hasChildren()) {
                                                                String status = snapshot.getValue(AppointmentStatus.class).getStatus();
                                                                patientScheduleFacade.setStatus(status);
                                                                scheduleList.add(patientScheduleFacade);

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }

                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            scheduleListAdapter = new ScheduleListAdapter(UpcomingAppointmentActivity.this,scheduleList);
                            rvUpcoming.setAdapter(scheduleListAdapter);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
//        long finalTimeStamp = timeStamp;
//        Log.d(TAG, "finalTimeStamp: " + finalTimeStamp);
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot patientSnapshot: snapshot.getChildren()){
//                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
//                    String patientKey = patientSnapshot.getValue(Patient.class).getKey();
//                    Log.d(TAG, "patientName: " + patientKey + " " + patientKey);
//                    contactNo = patientSnapshot.getValue(Patient.class).getContactNo();
//                    Query scheduleRef = mFirebaseDatabase.getReference("Schedules").child(patientKey).orderByChild("timeStamp").startAt(finalTimeStamp);
//                    scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            scheduleList.clear();
//                            for(DataSnapshot schedule: snapshot.getChildren()){
//                                schedDate = schedule.getValue(Schedule.class).getDate();
//                                startTime =  schedule.getValue(Schedule.class).getStartTime();
//                                endTime =  schedule.getValue(Schedule.class).getEndTime();
//                                note =  schedule.getValue(Schedule.class).getRemarks();
//                                scheduleKey = schedule.getKey();
//                                PatientScheduleFacade patientScheduleFacade = new PatientScheduleFacade();
//                                patientScheduleFacade.setPatientName(patientName);
//                                patientScheduleFacade.setContactNo(contactNo);
//                                patientScheduleFacade.setDate(schedDate);
//                                patientScheduleFacade.setEndTime(endTime);
//                                patientScheduleFacade.setStartTime(startTime);
//                                patientScheduleFacade.setNote(note);
//                                patientScheduleFacade.setPatientKey(patientKey);
//                                patientScheduleFacade.setScheduleKey(scheduleKey);
//                                scheduleList.add(patientScheduleFacade);
//                            }
//                            scheduleListAdapter = new ScheduleListAdapter(UpcomingAppointmentActivity.this,scheduleList);
//                            rvUpcoming.setAdapter(scheduleListAdapter);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


    }
}