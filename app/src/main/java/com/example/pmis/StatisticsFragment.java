package com.example.pmis;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.Statistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private TextView tvPatientCount;
    private BarChart chartAllBarangay;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference brgyRef;
    private String userID;
    private List<Statistics> statisticsList;
    private List<String> barangayList;
    private List<BarEntry> barEntryList;
    private int patientCounter;
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
        tvPatientCount = view.findViewById(R.id.tvPatientCount);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        statisticsList = new ArrayList<>();
        barangayList = new ArrayList<>();
        barEntryList = new ArrayList<>();
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
            Log.d(TAG, "COLLECTIONS+ " + list + " " + freq);
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
//        xAxis.setLabelRotationAngle(270);
        chartAllBarangay.animateY(2000);
        chartAllBarangay.invalidate();



    }

}