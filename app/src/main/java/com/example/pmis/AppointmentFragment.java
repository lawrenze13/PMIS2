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

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private TextView tvCalHeader, tvCounter;
    private FloatingActionButton fabAddSchedule;
    private RecyclerView rvSchedule;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef, schedRef, patientRef;
    private List<Schedule> scheduleList;
    private LoggedUserData loggedUserData;
    private String userID;
    private   String patientName, schedDate, startTime, endTime, note, contactNo, patientKey, scheduleKey;
    private ArrayList<String> keyList;
    private ArrayList<PatientScheduleFacade> patientScheduleFacadeArrayList;
    private ScheduleListAdapter scheduleListAdapter;
    private View view;
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
        patientScheduleFacadeArrayList = new ArrayList<>();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        String calHeader = month_name + ' ' + year;
        tvCalHeader.setText(calHeader);
        String TAG = "SCHEDULE";
        final CompactCalendarView compactCalendarView = (CompactCalendarView) view.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                Log.d(TAG, "Day was clicked: " + dateClicked + " with events " + events);
                patientScheduleFacadeArrayList.clear();
                scheduleListAdapter = new ScheduleListAdapter(getContext(),patientScheduleFacadeArrayList);
                rvSchedule.setAdapter(scheduleListAdapter);
                if( events.size() == 0){
                    tvCounter.setText("0 appointment record(s)") ;
                }
                for(int i = 0; i< events.size(); i++){
                    Event ev = events.get(i);
                    Log.d(TAG, "key: " + ev.getData().toString());
                    String patientKey = ev.getData().toString();
                    patientRef = mFirebaseDatabase.getReference("Patient").child(userID).child(patientKey);
                    patientRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            PatientScheduleFacade patientScheduleFacade = new PatientScheduleFacade();
                            patientName = snapshot.getValue(Patient.class).getFirstName() + ' ' + snapshot.getValue(Patient.class).getLastName();
                            contactNo = snapshot.getValue(Patient.class).getContactNo();

                            schedRef = mFirebaseDatabase.getReference("Schedules").child(patientKey);
                            schedRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    patientScheduleFacadeArrayList.clear();
                                    for(DataSnapshot ds: snapshot.getChildren()) {
                                        Schedule schedule = ds.getValue(Schedule.class);
                                        String cDate = schedule.getDate();
                                        String start = schedule.getStartTime();
                                        String startDate = cDate + ' ' + start;
                                        final String OLD_FORMAT = "EEE MMM d HH:mm:ss zzz yyyy";
                                        final String NEW_FORMAT = "d MMM yyy";
                                        Log.d(TAG, "startDATE :" + startDate);
                                        SimpleDateFormat formatter = new SimpleDateFormat(OLD_FORMAT);
                                        Date d = null;
                                        try {
                                            d = formatter.parse(String.valueOf(dateClicked));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        formatter.applyPattern(NEW_FORMAT);
                                        String newDateString = formatter.format(d);
                                        SimpleDateFormat oldFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
                                        Log.d(TAG, "COMPARE DATE: " + cDate + ' ' + newDateString);
                                        if (newDateString.equals(cDate)) {
                                            schedDate = schedule.getDate();
                                            startTime = schedule.getStartTime();
                                            endTime = schedule.getEndTime();
                                            note = schedule.getRemarks();
                                            scheduleKey = schedule.getKey();
                                            patientScheduleFacade.setPatientName(patientName);
                                            patientScheduleFacade.setContactNo(contactNo);
                                            patientScheduleFacade.setDate(schedDate);
                                            patientScheduleFacade.setEndTime(endTime);
                                            patientScheduleFacade.setStartTime(startTime);
                                            patientScheduleFacade.setNote(note);
                                            patientScheduleFacade.setPatientKey(patientKey);
                                            patientScheduleFacade.setScheduleKey(scheduleKey);
                                            patientScheduleFacadeArrayList.add(patientScheduleFacade);
                                            scheduleListAdapter.notifyDataSetChanged();
//                                                for(PatientScheduleFacade s:patientScheduleFacadeArrayList){
//                                                    Log.d(TAG, "data: " + s.getDate());
//                                                    Log.d(TAG, "data: " + s.getPatientName());
//                                                    Log.d(TAG, "data: " + s.getContactNo());
//                                                    Log.d(TAG, "data: " + s.getStartTime());
//                                                }
                                            tvCounter.setText(" "+ patientScheduleFacadeArrayList.size() + " appointment record(s)") ;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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
                for(DataSnapshot ds: snapshot.getChildren()){
                    Patient patient = ds.getValue(Patient.class);
                    String patientKey = patient.getKey();

                    DatabaseReference patientRef = mFirebaseDatabase.getReference("Schedules").child(patientKey);
                    patientRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds: snapshot.getChildren()) {

                                Schedule schedule = ds.getValue(Schedule.class);
                                String cDate = schedule.getDate();
                                String start = schedule.getStartTime();
                                String startDate = cDate + ' ' + start;
                                Log.d(TAG, "patient_key: " + patientKey);
                                Log.d(TAG, "schedule_date: " + startDate);
                                DateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm", Locale.getDefault());
                                DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH::mm:ss Z", Locale.getDefault());
                                try {
                                    Date date = (Date) formatter.parse(startDate);

                                    long timeStamp = date.getTime();
                                    Log.d(TAG, "timeStamp: " + timeStamp);
//                                    ArrayList<String> arrayList  = new ArrayList<>();
//                                    arrayList.add(schedule.getKey());
//                                    arrayList.add(schedule.getPatientKey());
                                    Event ev1 = new Event(Color.BLUE, timeStamp,schedule.getPatientKey() );
                                    compactCalendarView.addEvent(ev1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}