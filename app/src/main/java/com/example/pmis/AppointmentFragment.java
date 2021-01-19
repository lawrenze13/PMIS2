package com.example.pmis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Adapter.PatientListAdapter;
import com.example.pmis.Adapter.ScheduleListAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.Model.Schedule;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AppointmentFragment extends Fragment {

    private static final String TAG = "APPOINTMENT_FRAGMENT";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    public TextView tvCalHeader, tvCounter;
    private FloatingActionButton fabAddSchedule;
    private RecyclerView rvSchedule;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, schedRef, patientRef;
    private List<Schedule> scheduleList;
    private LoggedUserData loggedUserData;
    private String userID;
    private   String patientName, schedDate, startTime, endTime, note, contactNo, patientKey, scheduleKey;
    private List<String> patientKeyList;
    private ArrayList<String> keyList;
    private ArrayList<PatientScheduleFacade> patientScheduleFacadeArrayList;
    private ScheduleListAdapter scheduleListAdapter;
    private View view;
    private Event  ev1;
    private CompactCalendarView compactCalendarView;
    public AppointmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_appointment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loggedUserData = new LoggedUserData();
        userID = loggedUserData.userID();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        fabAddSchedule = view.findViewById(R.id.fabAddSchedule);
        fabAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PatientActivity.class);
                startActivity(intent);
            }
        });
        rvSchedule = view.findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        tvCalHeader = view.findViewById(R.id.tvCalHeader);
        tvCounter = view.findViewById(R.id.tvCounter);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        keyList = new ArrayList<String>();
        patientKeyList = new ArrayList<>();
        patientScheduleFacadeArrayList = new ArrayList<>();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        String calHeader = month_name + ' ' + year;
        tvCalHeader.setText(calHeader);
        compactCalendarView = (CompactCalendarView) view.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                final int[] schedCounter = {0};
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                Log.d(TAG, "Day was clicked: " + dateClicked + " with events " + events);
                patientScheduleFacadeArrayList.clear();
                tvCounter.setText(String.valueOf(events.size()) + " appointment record(s)") ;
                scheduleListAdapter = new ScheduleListAdapter(getContext(),patientScheduleFacadeArrayList);
                rvSchedule.setAdapter(scheduleListAdapter);
                for(int i = 0; i< events.size(); i++){
                    Event ev = events.get(i);
                    PatientScheduleFacade sched = (PatientScheduleFacade) ev.getData();
                    Log.d(TAG, "EVENT DATA: " + sched.getPatientKey() + " " + sched.getScheduleKey());
                    String patientKey =  sched.getPatientKey();
                    String eventSchedKey= sched.getScheduleKey();
                    String patientName= sched.getPatientName();
                    String contactNo= sched.getContactNo();
                            schedRef = mFirebaseDatabase.getReference("Schedules").child(userID).child(eventSchedKey);
                            schedRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                       if(snapshot.exists()){
                                           Schedule schedule = snapshot.getValue(Schedule.class);
                                           schedDate = snapshot.getValue(Schedule.class).getDate();
                                           startTime = snapshot.getValue(Schedule.class).getStartTime();
                                           endTime = snapshot.getValue(Schedule.class).getEndTime();
                                           note = snapshot.getValue(Schedule.class).getRemarks();
                                           scheduleKey = snapshot.getValue(Schedule.class).getKey();
                                           PatientScheduleFacade patientScheduleFacade = new PatientScheduleFacade();
                                           patientScheduleFacade.setPatientName(patientName);
                                           patientScheduleFacade.setContactNo(contactNo);
                                           patientScheduleFacade.setDate(schedDate);
                                           patientScheduleFacade.setEndTime(endTime);
                                           patientScheduleFacade.setStartTime(startTime);
                                           patientScheduleFacade.setNote(note);
                                           patientScheduleFacade.setPatientKey(patientKey);
                                           patientScheduleFacade.setScheduleKey(scheduleKey);
                                           patientScheduleFacadeArrayList.add(patientScheduleFacade);
                                           schedCounter[0]++;
                                           Log.d(TAG, "sched counter: " +   schedCounter[0] + " " + events.size());
                                           if (schedCounter[0] == events.size()){
                                             scheduleListAdapter.notifyDataSetChanged();
                                           }
                                       }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }


            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                SimpleDateFormat year_date = new SimpleDateFormat("yyy");
                String year = year_date.format(firstDayOfNewMonth.getTime());
                SimpleDateFormat month_date = new SimpleDateFormat("MMM");
                String month_name = month_date.format(firstDayOfNewMonth.getTime());
                String calHeader = month_name + ' ' + year;
                tvCalHeader.setText(calHeader);
            }
        });
        compactCalendarView.performContextClick();

        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                compactCalendarView.removeAllEvents();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Patient patient = ds.getValue(Patient.class);
                    String patientKey = patient.getKey();
                    patientKeyList.add(patientKey);
                    patientName = ds.getValue(Patient.class).getFirstName() + ' ' + ds.getValue(Patient.class).getLastName();
                    contactNo = ds.getValue(Patient.class).getContactNo();
                }
                buildCalendarEvent(patientKeyList, patientName, contactNo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void buildCalendarEvent(List<String> patientKeyList, String patientName, String contactNo) {
            DatabaseReference patientRef = mFirebaseDatabase.getReference("Schedules").child(userID);
            patientRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    compactCalendarView.removeAllEvents();
                    if(snapshot.exists()){
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            if(ds.exists()) {
                                if (patientKeyList.contains(ds.getValue(Schedule.class).getPatientKey())) {
                                    Schedule schedule = ds.getValue(Schedule.class);
                                    String cDate = schedule.getDate();
                                    Log.d(TAG, "patient_key: " + patientKey);
                                    Log.d(TAG, "schedule_date: " + cDate);
                                    DateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                    DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH::mm:ss Z", Locale.getDefault());
                                    try {
                                        Date date = (Date) formatter.parse(cDate);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        Log.d(TAG, "timeStamp: " + cal.getTimeInMillis());
                                        PatientScheduleFacade scheduler = new PatientScheduleFacade();
                                        scheduler.setPatientKey(schedule.getPatientKey());
                                        scheduler.setScheduleKey(schedule.getKey());
                                        scheduler.setContactNo(contactNo);
                                        scheduler.setPatientName(patientName);
                                        ev1 = new Event(Color.BLUE, cal.getTimeInMillis(), scheduler);
                                        compactCalendarView.addEvent(ev1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

}