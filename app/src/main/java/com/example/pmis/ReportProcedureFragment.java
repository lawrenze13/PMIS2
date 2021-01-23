package com.example.pmis;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmis.Adapter.ReportAppointmentAdapter;
import com.example.pmis.Adapter.ReportProceduresAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientProcedures;
import com.example.pmis.Model.Procedures;
import com.example.pmis.Model.ReportAppointment;
import com.example.pmis.Model.ReportProcedures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportProcedureFragment extends Fragment {
    private static final String TAG = "REPORT_APPOINTMENT";
    private Spinner spinnerFilterPayment;
    private TextView tvReportCount;
    private Button btnPayment, btnSearch;
    private View view;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, paymentRef, fullRef, insRef;
    private List<String> filterList = new ArrayList<>();
    private List<Procedures> proceduresPriceList = new ArrayList<>();
    private List<ReportProcedures> reportProceduresList = new ArrayList<>();
    String userID, clinicName, clinicAddress, docName, clinicContactNo, license, degree;
    private LoggedUserData loggedUserData = new LoggedUserData();
    private List<PatientProcedures> proceduresList;
    private int totalProcedure;
    private RecyclerView rvReportProcedures;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userID = loggedUserData.userID();
        spinnerFilterPayment = view.findViewById(R.id.spinnerFilterPayment);
        tvReportCount = view.findViewById(R.id.tvReportCount);
        rvReportProcedures = view.findViewById(R.id.rvReportProcedures);
        rvReportProcedures.setLayoutManager(new LinearLayoutManager(getContext()));
        btnSearch = view.findViewById(R.id.btnSearch);
        btnPayment = view.findViewById(R.id.btnPayment);
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF(proceduresList, proceduresPriceList);
            }
        });
        btnPayment.setVisibility(View.GONE);
        proceduresList = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getClinicInfo();
        getDoctorName();
        buildSpinner();
        getProcedurePrice();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPayment.setVisibility(View.VISIBLE);
                totalProcedure = 0;
                proceduresList.clear();
                reportProceduresList.clear();
                tvReportCount.setText("0");
                ReportProceduresAdapter reportProceduresAdapter = new ReportProceduresAdapter(getContext(), proceduresList, proceduresPriceList);
                rvReportProcedures.setAdapter(reportProceduresAdapter);
                int position = spinnerFilterPayment.getSelectedItemPosition();
                DatabaseReference patientRef = mFirebaseDatabase.getReference("Patient").child(userID);
                patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot patientSnapshot: snapshot.getChildren()) {
                            ReportProcedures reportProcedures = new ReportProcedures();
                            String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                            String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                            reportProcedures.setPatientName(patientName);
                           DatabaseReference procedureRef = mFirebaseDatabase.getReference("PatientProcedure").child(patientKey);
                            procedureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot procedureSnapshot: snapshot.getChildren()) {

                                        String procedureDate = procedureSnapshot.getValue(PatientProcedures.class).getDate();
                                        DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                                        Date d = new Date();
                                        long procedureLongDate = 0 ;
                                        long currentDate = 0;
                                        try {
                                            Date newDateStr = format.parse(procedureDate);
                                            Date todayDateStr = format.parse(format.format(d));
                                            currentDate = todayDateStr.getTime();
                                            procedureLongDate = newDateStr.getTime();
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        if(position == 0){
                                            totalProcedure++;
                                            PatientProcedures patientProcedures = new PatientProcedures();
                                            patientProcedures.setKey(patientName);
                                            patientProcedures.setProcedureKey(procedureSnapshot.getValue(PatientProcedures.class).getProcedureKey());
                                            patientProcedures.setProcedure(procedureSnapshot.getValue(PatientProcedures.class).getProcedure());
                                            patientProcedures.setDate(procedureSnapshot.getValue(PatientProcedures.class).getDate());
                                            proceduresList.add(patientProcedures);
                                            tvReportCount.setText(String.valueOf(totalProcedure));

                                        }else if (position ==1){
                                            if(procedureLongDate == currentDate){
                                                totalProcedure++;
                                                PatientProcedures patientProcedures = new PatientProcedures();
                                                patientProcedures.setKey(patientName);
                                                patientProcedures.setProcedureKey(procedureSnapshot.getValue(PatientProcedures.class).getProcedureKey());
                                                patientProcedures.setProcedure(procedureSnapshot.getValue(PatientProcedures.class).getProcedure());
                                                patientProcedures.setDate(procedureSnapshot.getValue(PatientProcedures.class).getDate());
                                                proceduresList.add(patientProcedures);
                                                tvReportCount.setText(String.valueOf(totalProcedure));
                                            }
                                        }else if(position == 2){
                                            Date date = new Date();
                                            Calendar c = Calendar.getInstance();
                                            c.setTime(date);
                                            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
                                            c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
                                            Date weekStart = c.getTime();
                                            long start = weekStart.getTime();
                                            Log.d(TAG, "start: " + start);
                                            c.add(Calendar.DAY_OF_MONTH, 6);
                                            Date weekEnd = c.getTime();
                                            long end = weekEnd.getTime();
                                            Log.d(TAG, "end: " + end);

                                            if(procedureLongDate > start && procedureLongDate < end){
                                                totalProcedure++;
                                                PatientProcedures patientProcedures = new PatientProcedures();
                                                patientProcedures.setKey(patientName);
                                                patientProcedures.setProcedureKey(procedureSnapshot.getValue(PatientProcedures.class).getProcedureKey());
                                                patientProcedures.setProcedure(procedureSnapshot.getValue(PatientProcedures.class).getProcedure());
                                                patientProcedures.setDate(procedureSnapshot.getValue(PatientProcedures.class).getDate());
                                                proceduresList.add(patientProcedures);
                                                tvReportCount.setText(String.valueOf(totalProcedure));
                                            }
                                        }else if(position == 3){
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.add(Calendar.MONTH, 0);
                                            calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                                            Date monthStart = calendar.getTime();
                                            long start  = monthStart.getTime();
                                            Log.d(TAG, "start: " + start);
                                            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                            Date monthEnd = calendar.getTime();
                                            long end = monthEnd.getTime();
                                            Log.d(TAG, "end: " + end);

                                            if(procedureLongDate > start && procedureLongDate < end){
                                                totalProcedure++;
                                                PatientProcedures patientProcedures = new PatientProcedures();
                                                patientProcedures.setKey(patientName);
                                                patientProcedures.setProcedureKey(procedureSnapshot.getValue(PatientProcedures.class).getProcedureKey());
                                                patientProcedures.setProcedure(procedureSnapshot.getValue(PatientProcedures.class).getProcedure());
                                                patientProcedures.setDate(procedureSnapshot.getValue(PatientProcedures.class).getDate());
                                                proceduresList.add(patientProcedures);
                                                tvReportCount.setText(String.valueOf(totalProcedure));
                                            }
                                        }else if(position == 4){
                                            Date date = new Date();
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(date);
                                            calendar.set(Calendar.DAY_OF_YEAR,1);
                                            Date startDate = calendar.getTime();
                                            long start = startDate.getTime();
                                            Log.d(TAG, "START: " + start);
                                            calendar.set(Calendar.MONTH, 11);
                                            calendar.set(Calendar.DAY_OF_MONTH, 31);
                                            Date endDate = calendar.getTime();
                                            long end = endDate.getTime();
                                            Log.d(TAG, "end: " + end);
                                            if(procedureLongDate > start && procedureLongDate < end){
                                                totalProcedure++;
                                                PatientProcedures patientProcedures = new PatientProcedures();
                                                patientProcedures.setKey(patientName);
                                                patientProcedures.setProcedureKey(procedureSnapshot.getValue(PatientProcedures.class).getProcedureKey());
                                                patientProcedures.setProcedure(procedureSnapshot.getValue(PatientProcedures.class).getProcedure());
                                                patientProcedures.setDate(procedureSnapshot.getValue(PatientProcedures.class).getDate());
                                                proceduresList.add(patientProcedures);
                                                tvReportCount.setText(String.valueOf(totalProcedure));
                                            }
                                        }
                                        reportProceduresAdapter.notifyDataSetChanged();

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
        });
    }

    private void generatePDF(List<PatientProcedures> proceduresList, List<Procedures> proceduresPriceList) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        String currentDate = format.format(new Date());
        PdfDocument myPdfDocument = new PdfDocument();
        int currentY = 0 ;
        currentY = 0 ;
        Paint paint = new Paint();
        Paint forLinePaint = new Paint();
        Paint solidLinePaint = new Paint();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.montserrat);
        paint.setTypeface(typeface);
        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(clinicName, canvas.getWidth() / 2, 20, paint);
        paint.setTextSize(8f);
        canvas.drawText(clinicAddress, canvas.getWidth() / 2, 35, paint);
        canvas.drawText(clinicContactNo, canvas.getWidth() / 2, 44, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8.5f);
        canvas.drawText(docName, 20, 60, paint);
        paint.setTextSize(7f);
        canvas.drawText(degree, 20, 68, paint);
        paint.setTextSize(7f);
        canvas.drawText(license, 20, 75, paint);

        paint.setTextSize(7f);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentDate, 575, 75, paint);


        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 80, 575, 80, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 80, 20, 100, solidLinePaint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8.5f);
        canvas.drawText("Patient Name", (float) (canvas.getWidth() * .1), 90, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(8.5f);
        canvas.drawText("Date", (float) (canvas.getWidth() * .35), 90, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(8.5f);
        canvas.drawText("Procedure",  (float) (canvas.getWidth() * .60), 90, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(8.5f);
        canvas.drawText("Amount",  (float) (canvas.getWidth() - 50 ), 90, paint);

        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 100, 575, 100, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(575, 80, 575, 100, solidLinePaint);

        currentY = 110;
        for (PatientProcedures report : proceduresList) {

                currentY = currentY + 10;
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(7f);
                canvas.drawText(report.getKey(), (float) (canvas.getWidth() * .1), currentY, paint);
                paint.setTextSize(7f);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(report.getDate(), (float) (canvas.getWidth() * .35), currentY, paint);
                paint.setTextSize(7f);
                canvas.drawText(report.getProcedure(), (float) (canvas.getWidth() * .60), currentY, paint);
                int amount= 0 ;
                for(Procedures procedures : proceduresPriceList){
                    if(report.getProcedureKey().equals(procedures.getKey())){
                        amount = procedures.getPrice();
                    }
                }
                canvas.drawText(String.valueOf(amount), (float) (canvas.getWidth() - 50), currentY, paint);
                currentY = currentY + 10;
                solidLinePaint.setStyle(Paint.Style.STROKE);
                solidLinePaint.setStrokeWidth(1);
                canvas.drawLine(20, currentY, 575, currentY, solidLinePaint);
                currentY = currentY + 10;


        }
        String fileName = "test.pdf";
        myPdfDocument.finishPage(myPage);
        File file = new File(getContext().getExternalFilesDir("/"),fileName);

        try{
            myPdfDocument.writeTo(new FileOutputStream(file));

        } catch (IOException e){
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        myPdfDocument.close();
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = Intent.createChooser(target, "Open File");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getContext().startActivity(intent);
    }

    private void getProcedurePrice() {

        DatabaseReference procRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        procRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                proceduresPriceList.clear();
                for(DataSnapshot p: snapshot.getChildren()) {
                    Procedures procedures = new Procedures();
                    procedures.setPrice(p.getValue(Procedures.class).getPrice());
                    procedures.setKey(p.getValue(Procedures.class).getKey());
                    proceduresPriceList.add(procedures);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildSpinner() {
        filterList.add("All time");
        filterList.add("Today");
        filterList.add("This Week");
        filterList.add("This Month");
        filterList.add("This Year");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filterList);
        spinnerFilterPayment.setAdapter(arrayAdapter);
    }
    private void getClinicInfo() {
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clinicName = snapshot.getValue(Clinic.class).getClinicName();
                clinicAddress = snapshot.getValue(Clinic.class).getAddress();
                clinicContactNo = snapshot.getValue(Clinic.class).getContactNo();
                degree = snapshot.getValue(Clinic.class).getDegree();
                license = snapshot.getValue(Clinic.class).getLicense();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getDoctorName() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference  docRef = mFirebaseDatabase.getReference("Users").child(userID);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                docName = "Dr. " +  firstName + ' ' + lastName + " D.M.D";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_report_procedure, container, false);
        return view;
    }
}