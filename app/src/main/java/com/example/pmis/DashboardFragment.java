package com.example.pmis;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pmis.Adapter.PatientListAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.PaymentReportFacade;
import com.example.pmis.Model.Schedule;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DASHBOARD_FRAG";
    private LoggedUserData loggedUserData;
    private TextView tvAppointmentToday,tvAppointmentUpcoming, tvTotalPatient,tvTotalRevenue, tvTotalBalance;
    private View view;
    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, keyRef;
    private ImageButton ibAppointmentsToday, ivSendAppointment, ibViewPatients,ibAddPatient;
    private Button btnViewCalendar, btnExample;
    private int counter, upcomingCounter, patientCounter;
    private double revenueTotal, fullPaymentTotal, balanceTotal;
    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_dashboard2, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        counter = 0;
        upcomingCounter = 0;
        patientCounter = 0;
        balanceTotal = 0;
        revenueTotal = 0;
        fullPaymentTotal = 0;
        btnViewCalendar = view.findViewById(R.id.btnViewCalendar);
        btnViewCalendar.setOnClickListener(viewDeviceCalendar);
        ibAddPatient = view.findViewById(R.id.ibAddPatient);
        ibAddPatient.setOnClickListener(addPatient);
        tvAppointmentToday = view.findViewById(R.id.tvAppointmentToday);
        tvAppointmentUpcoming = view.findViewById(R.id.tvAppointmentUpcoming);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        ibViewPatients = view.findViewById(R.id.ibViewPatients);
        ibViewPatients.setOnClickListener(viewPatients);
        ibAppointmentsToday = view.findViewById(R.id.ibAppointmentsToday);
        ibAppointmentsToday.setOnClickListener(viewAppointments);
        ivSendAppointment = view.findViewById(R.id.ivSendAppointment);
        ivSendAppointment.setOnClickListener(viewUpcomingAppointments);
        tvTotalPatient = view.findViewById(R.id.tvTotalPatient);
        loggedUserData = new LoggedUserData();
        userID = loggedUserData.userID();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               List<String> patientKeyList = new ArrayList<>();
                patientCounter = 0;
                for(DataSnapshot ds: snapshot.getChildren()) {
                    String patientKey = ds.getValue(Patient.class).getKey();
                    patientCounter = patientCounter + 1;
                    DatabaseReference  paymentRef = mFirebaseDatabase.getReference("Payments").child(patientKey);
                    paymentRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            balanceTotal = 0;
                            revenueTotal = 0;
                            fullPaymentTotal = 0;
                            double dbAmount = 0;
                            for(DataSnapshot installment: snapshot.child("INSTALLMENT").getChildren()){
                                for(DataSnapshot payment: installment.child("payment").getChildren()){
                                    String amount = payment.getValue(Installment.class).getAmount();
                                    dbAmount = dbAmount + Double.parseDouble(amount);
                                    Log.d(TAG, "amount:" + amount);
                                    addRevenue(Double.parseDouble(amount));

                                }
                                String total = installment.getValue(PatientPayment.class).getTotal();
                                double installmentBalance = Double.parseDouble(total) - dbAmount;
                                addBalance(installmentBalance);
                            }
                            for(DataSnapshot fullPayment: snapshot.child("FULL PAYMENT").getChildren()){
                                String total = fullPayment.getValue(PatientPayment.class).getTotal();
                                fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
                                Log.d(TAG, "total:" + total);
                                addRevenue(Double.parseDouble(total));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    keyRef = mFirebaseDatabase.getReference("Schedules").child(ds.getValue(Patient.class).getKey());
                    keyRef.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            counter = 0;
                            upcomingCounter = 0;


                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                            String currentDate = df.format(c);
                            Log.d(TAG, currentDate);
                            for(DataSnapshot ds: snapshot.getChildren()) {
                                String dbDate = ds.getValue(Schedule.class).getDate();
                                try {
                                    Date date1 = df.parse(dbDate);
                                    Date date2 = df.parse(currentDate);
                                    if(date1.compareTo(date2) > 0){
                                        upcomingCounter = upcomingCounter +1;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(dbDate.equals(currentDate)) {
                                    Log.d(TAG, "FIREBASE: " + ds.getValue(Schedule.class).getDate());
                                    counter = counter +1;
                                }
                            }
                            Log.d(TAG, "Upcoming : " + upcomingCounter);
                            tvAppointmentUpcoming.setText(String.valueOf(upcomingCounter));
                            Log.d(TAG, "FIREBASE counter: " + counter);
                            tvAppointmentToday.setText(String.valueOf(counter));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                Log.d(TAG, "PATIENT TOTAL: " + patientCounter);
                tvTotalPatient.setText(String.valueOf(patientCounter));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private final View.OnClickListener addPatient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), AddPatientActivity.class);
            intent.putExtra("action", "add");
            getContext().startActivity(intent);
        }
    };
    private final View.OnClickListener viewAppointments = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Navigation.findNavController(view).navigate(R.id.appointmentFragment);
        }
    };
    private final View.OnClickListener viewUpcomingAppointments = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), UpcomingAppointmentActivity.class);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewPatients = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), PatientActivity.class);
            startActivity(intent);
        }
    };
    private final View.OnClickListener viewDeviceCalendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            startActivity(intent);
        }
    };
    private void addBalance(double installment) {
        balanceTotal = balanceTotal + installment;
        Log.d(TAG, "balanceTotal:"  + balanceTotal);
        tvTotalBalance.setText("P"+String.valueOf(balanceTotal));
    }

    private void addRevenue(double parseDouble) {
        revenueTotal = revenueTotal + parseDouble;
        Log.d(TAG, "revenueTotal:"  + revenueTotal);
        tvTotalRevenue.setText("P"+String.valueOf(revenueTotal));
    }


}