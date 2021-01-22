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

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.AppointmentStatus;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientInfoFacade;
import com.example.pmis.Model.PaymentReportFacade;
import com.example.pmis.Model.ReportAppointment;
import com.example.pmis.Model.Schedule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;

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

public class ReportAppointmentFragment extends Fragment {
    private static final String TAG = "REPORT_APPOINTMENT";
    private Spinner spinnerFilterPayment, spinnerFilterType;
    private TextView tvReportPending, tvReportCancelled, tvReportCompleted;
    private Button btnPayment, btnSearch;
    private View view;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, paymentRef, fullRef, insRef;
    private List<String> filterList = new ArrayList<>();
    private List<AppointmentStatus> appointmentStatusList = new ArrayList<>();
    private List<ReportAppointment> reportAppointmentList = new ArrayList<>();
    private List<ReportAppointment> pdfAppointmentList = new ArrayList<>();
    private LoggedUserData loggedUserData = new LoggedUserData();
    String userID, clinicName, clinicAddress, docName, clinicContactNo, license, degree;
    private int cancelledCount, pendingCount, completedCount;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userID = loggedUserData.userID();
        tvReportPending = view.findViewById(R.id.tvReportPending);
        tvReportCancelled = view.findViewById(R.id.tvReportCancelled);
        tvReportCompleted = view.findViewById(R.id.tvReportCompleted);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnPayment = view.findViewById(R.id.btnPayment);
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF(pdfAppointmentList);
            }
        });
        btnPayment.setVisibility(View.GONE);
        spinnerFilterPayment = view.findViewById(R.id.spinnerFilterPayment);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        setAppointmentStatus();
        populatePatientKey();
        getClinicInfo();
        getDoctorName();
        buildSpinner();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPayment.setVisibility(View.VISIBLE);
                pendingCount = 0;
                cancelledCount = 0;
                completedCount = 0;
                pdfAppointmentList.clear();
                if(spinnerFilterPayment.getSelectedItemPosition() == 0){
                    for(ReportAppointment reportAppointment : reportAppointmentList){
                        if(reportAppointment.getStatus().equals("Pending")){
                            pendingCount++;
                            tvReportPending.setText(String.valueOf(pendingCount));
                        }else if(reportAppointment.getStatus().equals("Cancelled")){
                            cancelledCount++;
                            tvReportCancelled.setText(String.valueOf(cancelledCount));
                        }else if(reportAppointment.getStatus().equals("Completed")){
                            completedCount++;
                            tvReportCompleted.setText(String.valueOf(completedCount));
                        }
                    }
                    pdfAppointmentList.addAll(reportAppointmentList);
                }
                else if(spinnerFilterPayment.getSelectedItemPosition() == 1){
                    long currentDate = 0;
                    Date d = new Date();
                    DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                    Date newDateStr = null;
                    try {
                        newDateStr = format.parse(format.format(d));
                        currentDate = newDateStr.getTime();
                        for(ReportAppointment reportAppointment: reportAppointmentList){
                            if(reportAppointment.getTimeStamp() == currentDate){
                                if(reportAppointment.getStatus().equals("Pending")){
                                    pendingCount++;
                                    tvReportPending.setText(String.valueOf(pendingCount));
                                }else if(reportAppointment.getStatus().equals("Cancelled")){
                                    cancelledCount++;
                                    tvReportCancelled.setText(String.valueOf(cancelledCount));
                                }else if(reportAppointment.getStatus().equals("Completed")){
                                    completedCount++;
                                    tvReportCompleted.setText(String.valueOf(completedCount));
                                }
                                pdfAppointmentList.add(reportAppointment);
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    currentDate = newDateStr.getTime();
                }
                else if(spinnerFilterPayment.getSelectedItemPosition() == 2){
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
                    buildReport(start, end);
                }
                else if(spinnerFilterPayment.getSelectedItemPosition() == 3){
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
                    buildReport(start, end);
                }
                else if(spinnerFilterPayment.getSelectedItemPosition() == 4){
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
                    buildReport(start, end);
                }
            }
        });
    }

    private void generatePDF(List<ReportAppointment> pdfAppointmentList) {
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
        canvas.drawText("Time",  (float) (canvas.getWidth() * .60), 90, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(8.5f);
        canvas.drawText("Status",  (float) (canvas.getWidth() - 50 ), 90, paint);

        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 100, 575, 100, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(575, 80, 575, 100, solidLinePaint);

        currentY = 110;
        for (ReportAppointment report : pdfAppointmentList) {
            currentY = currentY + 10;
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(7f);
            canvas.drawText(report.getPatientName(), (float) (canvas.getWidth() * .1 ), currentY, paint);
            paint.setTextSize(7f);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(report.getDate(), (float) (canvas.getWidth() * .35), currentY, paint);
            paint.setTextSize(7f);
            String time = report.getStartTime() + " - " + report.getEndTime();
            canvas.drawText(time, (float) (canvas.getWidth() * .60), currentY, paint);
            canvas.drawText(report.getStatus(), (float) (canvas.getWidth() -50), currentY, paint);
            currentY = currentY + 10;
            solidLinePaint.setStyle(Paint.Style.STROKE);
            solidLinePaint.setStrokeWidth(1);
            canvas.drawLine(20, currentY, 575, currentY, solidLinePaint);
            currentY = currentY + 10;

            if(currentY > 800){
                myPdfDocument.finishPage(myPage);
                break;
            }else{

            }
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

    private void buildReport(long start, long end) {
        for(ReportAppointment reportAppointment: reportAppointmentList){
            if(reportAppointment.getTimeStamp()> start && reportAppointment.getTimeStamp() < end ){
                if(reportAppointment.getStatus().equals("Pending")){
                    pendingCount++;
                    tvReportPending.setText(String.valueOf(pendingCount));
                }else if(reportAppointment.getStatus().equals("Cancelled")){
                    cancelledCount++;
                    tvReportCancelled.setText(String.valueOf(cancelledCount));
                }else if(reportAppointment.getStatus().equals("Completed")){
                    completedCount++;
                    tvReportCompleted.setText(String.valueOf(completedCount));
                }
                pdfAppointmentList.add(reportAppointment);
            }
        }
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
    private void populatePatientKey() {
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PatientInfoFacade> patientInfoFacadeList = new ArrayList<>();
                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {

                    String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                    PatientInfoFacade patientInfoFacade = new PatientInfoFacade();
                    patientInfoFacade.setPatientName(patientName);
                    patientInfoFacade.setPatientKey(patientKey);
                    patientInfoFacadeList.add(patientInfoFacade);

                }
               populateAppointmentList(patientInfoFacadeList);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void populateAppointmentList(List<PatientInfoFacade> patientInfoFacadeList) {
        DatabaseReference appointmentRef = mFirebaseDatabase.getReference("Schedules").child(userID);
        appointmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot sched: snapshot.getChildren()){
                    for(PatientInfoFacade patientInfoFacade: patientInfoFacadeList){

                        String patientKey = patientInfoFacade.getPatientKey();
                        String patientName = patientInfoFacade.getPatientName();
                       if(patientKey.equals(sched.getValue(Schedule.class).getPatientKey())){
                           ReportAppointment reportAppointment = new ReportAppointment();
                        reportAppointment.setPatientName(patientName);
                        reportAppointment.setPatientKey(patientKey);
                           reportAppointment.setDate(sched.getValue(Schedule.class).getDate());
                           reportAppointment.setStartTime(sched.getValue(Schedule.class).getStartTime());
                           reportAppointment.setEndTime(sched.getValue(Schedule.class).getEndTime());
                           reportAppointment.setTimeStamp(sched.getValue(Schedule.class).getTimeStamp());
                           reportAppointment.setScheduleKey(sched.getValue(Schedule.class).getKey());
                           for(AppointmentStatus appointmentStatus: appointmentStatusList){
                               if(appointmentStatus.getScheduleKey().equals(reportAppointment.getScheduleKey())){
                                   reportAppointment.setStatus(appointmentStatus.getStatus());
                                   Log.d(TAG, "message: " + reportAppointment.getPatientName() + " " + appointmentStatus.getStatus());
                                   reportAppointmentList.add(reportAppointment);
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

    private void setAppointmentStatus() {
        Query query = mFirebaseDatabase.getReference("AppointmentStatus").child(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               for(DataSnapshot status: snapshot.getChildren()){
                   AppointmentStatus appointmentStatus = new AppointmentStatus();
                   appointmentStatus.setScheduleKey(status.getValue(AppointmentStatus.class).getScheduleKey());
                   appointmentStatus.setStatus(status.getValue(AppointmentStatus.class).getStatus());
                   appointmentStatusList.add(appointmentStatus);
               }
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
        view =  inflater.inflate(R.layout.fragment_report_appointment, container, false);
        return  view;
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
}