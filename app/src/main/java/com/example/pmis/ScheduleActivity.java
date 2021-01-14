package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.Schedule;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private static final String TAG = "SCHEDULE";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private List<Schedule> scheduleList;
    private LoggedUserData loggedUserData;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        final CompactCalendarView compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        userID = loggedUserData.userID();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    Patient patient = ds.getValue(Patient.class);
//                    String patientKey = patient.getKey();
//                    Log.d(TAG, "patient_key: " + patientKey);
//                    DatabaseReference patientRef = mFirebaseDatabase.getReference("Schedule").child(patientKey);
//                    patientRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for(DataSnapshot ds: snapshot.getChildren()) {
//
//                                Log.d(TAG, "patient_key: " + ds.getValue(Schedule.class).getDate());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
                }
           // }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}