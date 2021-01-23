package com.example.pmis;

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

import com.example.pmis.Adapter.CancelledListAdapter;
import com.example.pmis.Adapter.DrugListAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientScheduleFacade;
import com.example.pmis.Model.Schedule;
import com.example.pmis.Model.Statistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class StatisticsFragment extends Fragment {
    private static final String TAG = "STATISTICS_FRAGMENT";
    private View view;
    private TextView tvPatientCount, tvTotalAppointments;
    private BarChart chartAllBarangay;
    private HorizontalBarChart chartAllAppointments;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference brgyRef, appointmentRef;
    private String userID;
    private List<AppointmentStatus> appointmentStatusList;
    private List<String> patientKeyList;
    private List<Statistics> statisticsList;
    private List<String> barangayList;
    private List<BarEntry> barEntryList;
    private List<Schedule> allAppointmentStatusList;

    private List<BarEntry> appointmentEntryList;
    private List<String> statusList;
    private List<String> appointmentCancelledList;
    private List<PatientScheduleFacade> patientScheduleFacadeList;
    private int patientCounter, appointmentCounter;
    private RecyclerView rvCancelled;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view  = inflater.inflate(R.layout.fragment_statistics, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        chartAllBarangay =  (BarChart) view.findViewById(R.id.chartAllBarangay);
        chartAllAppointments =  (HorizontalBarChart) view.findViewById(R.id.chartAllAppointments);
        tvPatientCount = view.findViewById(R.id.tvPatientCount);
        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments);
        rvCancelled = view.findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(getContext()));
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        patientKeyList = new ArrayList<>();
        statisticsList = new ArrayList<>();
        barangayList = new ArrayList<>();
        barEntryList = new ArrayList<>();
        statusList = new ArrayList<>();
        allAppointmentStatusList = new ArrayList<>();
        patientScheduleFacadeList = new ArrayList<>();
        appointmentCancelledList = new ArrayList<>();
        statusList.add("Pending");
        statusList.add("Completed");
        statusList.add("Cancelled");
        appointmentEntryList = new ArrayList<>();
        appointmentStatusList = new ArrayList<>();
        brgyRef = mFirebaseDatabase.getReference("Patient").child(userID);
        brgyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientCounter = 0;
                statisticsList.clear();
                barangayList.clear();
                barEntryList.clear();
                for(DataSnapshot patientSnapshot: snapshot.getChildren()){
                    String barangay = patientSnapshot.getValue(Patient.class).getBarangay();
                    barangayList.add(barangay);
                    patientCounter++;
                }
                countDuplicates(barangayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        appointmentRef = mFirebaseDatabase.getReference("AppointmentStatus").child(userID);
        appointmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             if(snapshot.hasChildren()){
                 appointmentCounter = 0;
                 appointmentStatusList.clear();
                 appointmentCancelledList.clear();
                 patientKeyList.clear();
                 int pendingCounter = 0;
                 int completedCounter = 0;
                 int cancelledCounter = 0;
                 appointmentEntryList.clear();
                 List<Integer> countList = new ArrayList<>();

                 for(DataSnapshot patientSnapshot: snapshot.getChildren()){
                     allAppointmentStatusList.add(patientSnapshot.getValue(Schedule.class));
                     appointmentCounter++;
                     if(patientSnapshot.getValue(AppointmentStatus.class).getStatus().equals("Pending")) {
                         pendingCounter++;
                     }else if (patientSnapshot.getValue(AppointmentStatus.class).getStatus().equals("Completed")) {
                         completedCounter++;
                     }else if (patientSnapshot.getValue(AppointmentStatus.class).getStatus().equals("Cancelled")) {
                             appointmentCancelledList.add(patientSnapshot.getValue(AppointmentStatus.class).getScheduleKey());
                             cancelledCounter++;
                     }
                 }
                 generateCancelledList(appointmentCancelledList);
                 countList.add(pendingCounter);
                 countList.add(completedCounter);
                 countList.add(cancelledCounter);
                 appointmentEntryList.add(new BarEntry(0, pendingCounter));
                 appointmentEntryList.add(new BarEntry(1, completedCounter));
                 appointmentEntryList.add(new BarEntry(2, cancelledCounter));

                 BarDataSet barDataSet = new BarDataSet(appointmentEntryList, "Pending, Completed, Cancelled");
                 barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                 Description description = new Description();
                 description.setText("Appointments");
                 chartAllAppointments.setDescription(description);
                 BarData barData = new BarData(barDataSet);
                 chartAllAppointments.setData(barData);
                 chartAllAppointments.getAxisLeft().setValueFormatter(new ValueFormatter() {
                     @Override
                     public String getFormattedValue(float value) {
                         return String.valueOf((int) Math.floor(value));
                     }
                 });
                 int max = Collections.max(countList);
                 chartAllAppointments.getAxisLeft().setLabelCount(max);
                 XAxis xAxis = chartAllAppointments.getXAxis();
                 xAxis.setValueFormatter(new IndexAxisValueFormatter(statusList));
                 xAxis.setLabelRotationAngle(270);
                 xAxis.setDrawGridLines(false);
                 xAxis.setDrawAxisLine(false);
                 xAxis.setGranularity(1f);
                 xAxis.setLabelCount(statusList.size());
                 chartAllAppointments.animateY(2000);
                 chartAllAppointments.invalidate();
                 tvTotalAppointments.setText(String.valueOf(appointmentCounter - 1));
             }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void generateCancelledList(List<String> appointmentCancelledList) {
        DatabaseReference schedRef = FirebaseDatabase.getInstance().getReference("Schedules").child(userID);
        schedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){

                    if(appointmentCancelledList.contains(ds.getValue(Schedule.class).getKey())){
                        PatientScheduleFacade patientScheduleFacade = new PatientScheduleFacade();
                        patientScheduleFacade.setDate(ds.getValue(Schedule.class).getDate());
                        patientScheduleFacade.setStartTime(ds.getValue(Schedule.class).getStartTime());
                        patientScheduleFacade.setEndTime(ds.getValue(Schedule.class).getEndTime());
                        patientScheduleFacade.setPatientKey(ds.getValue(Schedule.class).getPatientKey());
                        patientScheduleFacade.setStatus("Cancelled");
                        patientKeyList.add(ds.getValue(Schedule.class).getPatientKey());
                        patientScheduleFacadeList.add(patientScheduleFacade);

                     //   DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Patient").child(userID).child(patientScheduleFacade.getPatientKey());
//                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                String fullName = snapshot.getValue(Patient.class).getFirstName() + " " + snapshot.getValue(Patient.class).getMiddleName() + " " + snapshot.getValue(Patient.class).getLastName();
//                                patientScheduleFacade.setPatientName(fullName);
//
//                                Log.d(TAG, "patientScheduleFacade+ " + patientScheduleFacade.getDate(););
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
                    }

                }
                for(PatientScheduleFacade p: patientScheduleFacadeList){

                }
                HashSet<String> hashSet = new HashSet<String>();
                hashSet.addAll(patientKeyList);
                patientKeyList.clear();
                patientKeyList.addAll(hashSet);
                CancelledListAdapter cancelledListAdapter = new CancelledListAdapter(getContext(), patientKeyList,patientScheduleFacadeList);
                rvCancelled.setAdapter(cancelledListAdapter);




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void countDuplicates(List<String> barangayList) {
        tvPatientCount.setText(String.valueOf(patientCounter));
        List<String> barangayDistinct = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();
        HashSet<String> hashSet = new HashSet<String>(barangayList);
        for(String list: hashSet){
            int freq = Collections.frequency(barangayList, list);

            countList.add(freq);
            statisticsList.add(new Statistics(list, freq));

        }
        for(int i = 0; i < statisticsList.size();i++){
            String brgy = statisticsList.get(i).getBarangay();
            int count = statisticsList.get(i).getCount();
            barEntryList.add(new BarEntry(i, count));

        }
        BarDataSet barDataSet = new BarDataSet(barEntryList, "Total Patient Per Barangay");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        Description description = new Description();
        description.setText("Barangays");
        chartAllBarangay.setDescription(description);
        BarData barData = new BarData(barDataSet);
        chartAllBarangay.setData(barData);
        XAxis xAxis = chartAllBarangay.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(hashSet));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(hashSet.size());
        chartAllBarangay.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        int max = Collections.max(countList);
        chartAllBarangay.getAxisLeft().setLabelCount(max);
        chartAllAppointments.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        chartAllAppointments.getAxisLeft().setLabelCount(max);
//        xAxis.setLabelRotationAngle(270);
        chartAllBarangay.animateY(2000);
        chartAllBarangay.invalidate();

        chartAllBarangay.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "Entry : " + e.getData());
                Log.d(TAG, "Entry : " + e.getY());

            }

            @Override
            public void onNothingSelected() {

            }
        });



    }

}