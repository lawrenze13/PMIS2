package com.example.pmis;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.example.pmis.Helpers.LoadingDialog;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.PaymentReportFacade;
import com.example.pmis.Model.Schedule;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DASHBOARD_FRAG";
    private LoggedUserData loggedUserData;
    private TextView tvAppointmentToday,tvAppointmentUpcoming, tvTotalPatient,tvTotalRevenue, tvTotalBalance, tvTotalPatientRecall;
    private View view;
    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, keyRef;
    private ImageButton ibAppointmentsToday, ivSendAppointment, ibViewPatients,ibAddPatient, ibAddAppointment;
    private Button btnViewCalendar, btnExample, btnPat, btnStat, btnApp, btnCli;
    private int counter, upcomingCounter, patientCounter, recallCounter,revenueTotal;
    private double  fullPaymentTotal, balanceTotal;
    private int loadingCounter;
    private LoadingDialog loadingDialog;
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
        loadingDialog = new LoadingDialog(getActivity());

        counter = 0;

        upcomingCounter = 0;
        patientCounter = 0;
        balanceTotal = 0;
        revenueTotal = 0;
        fullPaymentTotal = 0;
        recallCounter = 0;
//        btnViewCalendar = view.findViewById(R.id.btnViewCalendar);
//        btnViewCalendar.setOnClickListener(viewDeviceCalendar);
//        ibAddPatient = view.findViewById(R.id.ibAddPatient);
//        ibAddPatient.setOnClickListener(addPatient);
        tvAppointmentToday = view.findViewById(R.id.tvAppointmentToday);
        tvAppointmentUpcoming = view.findViewById(R.id.tvAppointmentUpcoming);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
//        ibAddAppointment = view.findViewById(R.id.ibAddAppointment);
//        ibAddAppointment.setOnClickListener(addAppointment);
        btnApp = view.findViewById(R.id.btnApp);
        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.appointmentFragment);
            }
        });
        btnCli = view.findViewById(R.id.btnCli);
        btnCli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.clinicFragment);
            }
        });
        btnPat = view.findViewById(R.id.btnPat);
        btnPat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.patientFragment);
            }
        });
        btnStat = view.findViewById(R.id.btnStat);
        btnStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.statisticsFragment);
            }
        });
        ibViewPatients = view.findViewById(R.id.ibViewPatients);
        ibViewPatients.setOnClickListener(viewPatients);
        ibAppointmentsToday = view.findViewById(R.id.ibAppointmentsToday);
        ibAppointmentsToday.setOnClickListener(viewAppointments);
        ivSendAppointment = view.findViewById(R.id.ivSendAppointment);
        ivSendAppointment.setOnClickListener(viewUpcomingAppointments);
        tvTotalPatient = view.findViewById(R.id.tvTotalPatient);
        tvTotalPatientRecall = view.findViewById(R.id.tvTotalPatientRecall);
        loggedUserData = new LoggedUserData();
        userID = loggedUserData.userID();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference paymentRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID);
        paymentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                balanceTotal = 0;
                revenueTotal = 0;
                fullPaymentTotal = 0;

                for (DataSnapshot installment : snapshot.child("INSTALLMENT").getChildren()) {
                    for(DataSnapshot patientInsSnapshot: installment.getChildren()) {
                        double dbAmount = 0;
                        Log.d(TAG, "installment: " + patientInsSnapshot.getValue(PatientPayment.class).getKey());
                        for (DataSnapshot payment : patientInsSnapshot.child("payment").getChildren()) {
                            String amount = payment.getValue(Installment.class).getAmount();
                            dbAmount = dbAmount +  Double.parseDouble(amount);
                            Log.d(TAG, "amount:" + amount);
                            addRevenue(Double.parseDouble(amount));
                        }
                        String total = patientInsSnapshot.getValue(PatientPayment.class).getTotal();
                            double installmentBalance = Double.parseDouble(total) - dbAmount;
                        Log.d(TAG, "BALANCE:" + Double.parseDouble(total) + " " +  dbAmount);
                            addBalance(installmentBalance);
                    }
                }
                for (DataSnapshot fullPayment : snapshot.child("FULL PAYMENT").getChildren()) {
                    for(DataSnapshot patientFullSnapshot: fullPayment.getChildren()) {
                        String total = patientFullSnapshot.getValue(PatientPayment.class).getTotal();
                        fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
                        Log.d(TAG, "total:" + total);
                        addRevenue(Double.parseDouble(total));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    List<String> patientKeyList = new ArrayList<>();
                    patientCounter = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (snapshot.exists()) {
                        String patientKey = ds.getValue(Patient.class).getKey();
                        patientCounter = patientCounter + 1;


                        keyRef = mFirebaseDatabase.getReference("Schedules").child(userID);
                        keyRef.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    for (DataSnapshot schedSnapshot : snapshot.getChildren()) {
                                        if (schedSnapshot.getValue(Schedule.class).getPatientKey() != null) {
                                            Log.d(TAG, "COMPARE: " + schedSnapshot.getValue(Schedule.class).getPatientKey() + " " + (patientKey));
                                            if (schedSnapshot.getValue(Schedule.class).getPatientKey().equals(patientKey)) {
                                                Date c = Calendar.getInstance().getTime();
                                                SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                                String currentDate = df.format(c);
                                                String dbDate = schedSnapshot.getValue(Schedule.class).getDate();
                                                try {
                                                    Date date1 = df.parse(dbDate);
                                                    Date date2 = df.parse(currentDate);
                                                    Date forRecall = Date.from(ZonedDateTime.now().minusMonths(3).toInstant());
                                                    Log.d(TAG, "DATES  : " + date1 + " " + date2);
                                                    if (date1.compareTo(date2) > 0) {
                                                        upcomingCounter = upcomingCounter + 1;
                                                    }
                                                    if (date1.equals(date2)) {
                                                        Log.d(TAG, "FIREBASE: " + ds.getValue(Schedule.class).getDate());
                                                        counter = counter + 1;
                                                    }else if(date1.before(forRecall)){
                                                        recallCounter++;

                                                    }
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }

                                                Log.d(TAG, "Upcoming : " + upcomingCounter);
                                                tvAppointmentUpcoming.setText(String.valueOf(upcomingCounter));
                                                Log.d(TAG, "FIREBASE counter: " + counter);
                                                tvAppointmentToday.setText(String.valueOf(counter));
                                                tvTotalPatientRecall.setText(String.valueOf(recallCounter));
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
                    Log.d(TAG, "PATIENT TOTAL: " + patientCounter);
                    tvTotalPatient.setText(String.valueOf(patientCounter));
                }

                }

                @Override
                public void onCancelled (@NonNull DatabaseError error){

                }
        });
    }

    private void addLoadingCounter() {
        loadingCounter++;
        if(loadingCounter == 2){
            loadingDialog.dismissDialog();
        }
    }

//    private final View.OnClickListener addPatient = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Intent intent = new Intent(getContext(), AddPatientActivity.class);
//            intent.putExtra("action", "add");
//            getContext().startActivity(intent);
//        }
//    };
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
//    private final View.OnClickListener viewDeviceCalendar = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
//            builder.appendPath("time");
//            ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
//            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
//            startActivity(intent);
//        }
//    };
    private final View.OnClickListener addAppointment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Navigation.findNavController(view).navigate(R.id.appointmentFragment);
        }
    };
    private void addBalance(double installment) {
        Log.d(TAG, "balanceTotal:"  + balanceTotal + " " + installment);
        balanceTotal = balanceTotal + installment;

        tvTotalBalance.setText("P"+String.valueOf(balanceTotal));
    }

    private void addRevenue(double parseDouble) {
        revenueTotal = (int) (revenueTotal + parseDouble);
        Log.d(TAG, "revenueTotal:"  + revenueTotal);
        tvTotalRevenue.setText("P"+String.valueOf(revenueTotal));
    }


}