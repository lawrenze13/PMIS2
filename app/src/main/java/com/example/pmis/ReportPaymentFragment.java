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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmis.Adapter.PatientPrescriptionAdapter;
import com.example.pmis.Adapter.ReportPaymentAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.PaymentReportFacade;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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


public class ReportPaymentFragment extends Fragment {

    private static final String TAG = "REPORT_PAYMENT";
    private Spinner spinnerFilterPayment, spinnerFilterType;
    private TextView tvReportRevenue, tvReportBalance, tvReportCount;
    private Button btnPayment, btnSearch;
    private View view;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, paymentRef, fullRef, insRef;
    private List<String> filterList = new ArrayList<>();
    private LoggedUserData loggedUserData = new LoggedUserData();
    private int installmentCounter, fullpaymentCounter,counter;
    private List<PaymentReportFacade> installmentMasterList;
    private List<PaymentReportFacade> fullpaymentMasterList;
    private List<PaymentReportFacade> pdfFullpaymentList;
    private List<PaymentReportFacade> pdfInstallmentList;
    private List<PaymentReportFacade> paymentReportFacadeList;
    private List<String> installmentPatientNameList;
    private RecyclerView rvReportPayment;
    private double revenueTotal, fullPaymentTotal, balanceTotal;
    String userID, clinicName, clinicAddress, docName, clinicContactNo, license, degree;
    private int count;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userID = loggedUserData.userID();
        pdfFullpaymentList = new ArrayList<>();
        installmentMasterList = new ArrayList<>();
        fullpaymentMasterList = new ArrayList<>();
        paymentReportFacadeList = new ArrayList<>();
        installmentPatientNameList = new ArrayList<String>();
        pdfInstallmentList = new ArrayList<PaymentReportFacade>();
        spinnerFilterPayment = view.findViewById(R.id.spinnerFilterPayment);
        spinnerFilterType = view.findViewById(R.id.spinnerFilterType);
        btnPayment = view.findViewById(R.id.btnPayment);
        btnSearch = view.findViewById(R.id.btnSearch);
        tvReportRevenue = view.findViewById(R.id.tvReportRevenue);
        tvReportBalance = view.findViewById(R.id.tvReportBalance);
        tvReportCount = view.findViewById(R.id.tvReportCount);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
         count = 0 ;
        paymentRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID);
        fullRef = paymentRef.child("INSTALLMENT");
        insRef = paymentRef.child("FULL PAYMENT");
        populatePatientKey();
        getClinicInfo();
        getDoctorName();
        buildSpinner();
        rvReportPayment = view.findViewById(R.id.rvReportPayment);
        rvReportPayment.setLayoutManager(new LinearLayoutManager(getContext()));
        btnPayment.setVisibility(View.GONE);
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spinnerFilterType.getSelectedItem().toString().trim() == "FULL PAYMENT"){
                    generateFullPaymentPDF(pdfFullpaymentList);
                }
                else if(spinnerFilterType.getSelectedItem().toString().trim() == "INSTALLMENT"){
                    generateInstallmentPDF(pdfInstallmentList);
                }
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPayment.setVisibility(View.VISIBLE);
                balanceTotal = 0;
                revenueTotal = 0;
                fullPaymentTotal = 0;
                installmentCounter = 0;
                fullpaymentCounter = 0;
                counter = 0;
                count = 0;
                tvReportBalance.setText("P0.00");
                tvReportRevenue.setText("P0.00");
                tvReportCount.setText("0");
                pdfFullpaymentList.clear();
                pdfInstallmentList.clear();
                if(spinnerFilterPayment.getSelectedItemPosition() == 0){
                    if(spinnerFilterType.getSelectedItem().toString().trim() == "FULL PAYMENT"){
                        for(PaymentReportFacade paymentReportFacade: fullpaymentMasterList){
                            count++;
                            Log.d(TAG, "count: " + count);
                            Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                            long currentDate = 0;
                            Date d = new Date();
                            DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                            double totalPayments = 0;
                            try {
                                Date newDateStr = format.parse(format.format(d));
                                currentDate = newDateStr.getTime();
                                Log.d(TAG, "currentDate :" + currentDate);
                                addRevenue(Double.parseDouble(paymentReportFacade.getTotal()));
                                addCounter();
                                PaymentReportFacade newRecord = new PaymentReportFacade();
                                newRecord.setDate(paymentReportFacade.getDate());
                                newRecord.setTotal(paymentReportFacade.getTotal());
                                newRecord.setPatientName(paymentReportFacade.getPatientName());
                                newRecord.setMethod(paymentReportFacade.getMethod());
                                pdfFullpaymentList.add(newRecord);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfFullpaymentList, "FULL PAYMENT");
                        rvReportPayment.setAdapter(reportPaymentAdapter);
                    }else if (spinnerFilterType.getSelectedItem().toString().trim() == "INSTALLMENT") {
                        for (PaymentReportFacade paymentReportFacade : installmentMasterList) {
                            count++;
                            Log.d(TAG, "count: " + count);
                            Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                            long currentDate = 0;
                            Date d = new Date();
                            DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                            double totalPayments = 0;
                            try {
                                Date newDateStr = format.parse(format.format(d));
                                currentDate = newDateStr.getTime();
                                Log.d(TAG, "currentDate :" + currentDate);
                                List<Installment> newInstallment = new ArrayList<>();
                                for (Installment installment : paymentReportFacade.getInstallmentList()) {
                                        Log.d(TAG, "AMOUNTNEW: " + installment.getAmount());
                                        addCounter();
                                        addRevenue(Double.parseDouble(installment.getAmount()));
                                        totalPayments = totalPayments + Double.parseDouble(installment.getAmount());

                                }

                                addBalance(Double.parseDouble(paymentReportFacade.getTotal()) - totalPayments);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        pdfInstallmentList.addAll(installmentMasterList);
                        ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfInstallmentList, "INSTALLMENT");
                        rvReportPayment.setAdapter(reportPaymentAdapter);
                    }
                }
               else if(spinnerFilterPayment.getSelectedItemPosition() == 1){
                    if(spinnerFilterType.getSelectedItem().toString().trim() == "FULL PAYMENT"){
                        for(PaymentReportFacade paymentReportFacade: fullpaymentMasterList){
                            count++;
                            Log.d(TAG, "count: " + count);
                            Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                            long currentDate = 0;
                            Date d = new Date();
                            DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                            double totalPayments = 0;
                            try {
                                Date newDateStr = format.parse(format.format(d));
                                currentDate = newDateStr.getTime();
                                Log.d(TAG, "currentDate :" + currentDate);
                                if(paymentReportFacade.getTimeStamp() == currentDate){
                                    addRevenue(Double.parseDouble(paymentReportFacade.getTotal()));
                                    addCounter();
                                    PaymentReportFacade newRecord = new PaymentReportFacade();
                                    newRecord.setDate(paymentReportFacade.getDate());
                                    newRecord.setTotal(paymentReportFacade.getTotal());
                                    newRecord.setPatientName(paymentReportFacade.getPatientName());
                                    newRecord.setMethod(paymentReportFacade.getMethod());
                                    pdfFullpaymentList.add(newRecord);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfFullpaymentList, "FULL PAYMENT");
                        rvReportPayment.setAdapter(reportPaymentAdapter);
                    }else if (spinnerFilterType.getSelectedItem().toString().trim() == "INSTALLMENT") {
                        for (PaymentReportFacade paymentReportFacade : installmentMasterList) {
                            count++;
                            Log.d(TAG, "count: " + count);
                            Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                            long currentDate = 0;
                            Date d = new Date();
                            DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                            double totalPayments = 0;
                            try {
                                Date newDateStr = format.parse(format.format(d));
                                currentDate = newDateStr.getTime();
                                Log.d(TAG, "currentDate :" + currentDate);
                                PaymentReportFacade newRecord = new PaymentReportFacade();
                                if (paymentReportFacade.getTimeStamp() == currentDate) {
                                     newRecord.setPatientName(paymentReportFacade.getPatientName());
                                     newRecord.setPlanName(paymentReportFacade.getPlanName());
                                     List<Installment> newInstallment = new ArrayList<>();
                                    for (Installment installment : paymentReportFacade.getInstallmentList()) {
                                            Log.d(TAG, "AMOUNTNEW: " + installment.getAmount());
                                            addCounter();
                                            addRevenue(Double.parseDouble(installment.getAmount()));
                                            totalPayments = totalPayments + Double.parseDouble(installment.getAmount());
                                            Installment i = new Installment();
                                            i.setMethod(installment.getMethod());
                                            i.setDate(installment.getDate());
                                            i.setAmount(installment.getAmount());
                                            newInstallment.add(i);
                                    }
                                    newRecord.setInstallmentList(newInstallment);
                                    pdfInstallmentList.add(newRecord);
                                }

                                addBalance(Double.parseDouble(paymentReportFacade.getTotal()) - totalPayments);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfInstallmentList, "INSTALLMENT");
                        rvReportPayment.setAdapter(reportPaymentAdapter);
                    }
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
        spinnerFilterPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

               if(position == 1){
                   Log.d(TAG, "spinner item: " + spinnerFilterPayment.getSelectedItem().toString().trim() );


               }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void buildReport(long start, long end) {
        if(spinnerFilterType.getSelectedItem().toString().trim() == "FULL PAYMENT"){
            for(PaymentReportFacade paymentReportFacade: fullpaymentMasterList){
                count++;
                Log.d(TAG, "count: " + count);
                Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                long currentDate = 0;
                Date d = new Date();
                DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                double totalPayments = 0;
                try {
                    Date newDateStr = format.parse(format.format(d));
                    currentDate = newDateStr.getTime();
                    Log.d(TAG, "currentDate :" + currentDate);
                    if(paymentReportFacade.getTimeStamp()> start && paymentReportFacade.getTimeStamp() < end ){
                        addRevenue(Double.parseDouble(paymentReportFacade.getTotal()));
                        addCounter();
                        PaymentReportFacade newRecord = new PaymentReportFacade();
                        newRecord.setDate(paymentReportFacade.getDate());
                        newRecord.setTotal(paymentReportFacade.getTotal());
                        newRecord.setPatientName(paymentReportFacade.getPatientName());
                        newRecord.setMethod(paymentReportFacade.getMethod());
                        pdfFullpaymentList.add(newRecord);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfFullpaymentList, "FULL PAYMENT");
            rvReportPayment.setAdapter(reportPaymentAdapter);
        }else if (spinnerFilterType.getSelectedItem().toString().trim() == "INSTALLMENT") {
            for (PaymentReportFacade paymentReportFacade : installmentMasterList) {
                count++;
                Log.d(TAG, "count: " + count);
                Log.d(TAG, "PatientName " + paymentReportFacade.getPatientName());
                long currentDate = 0;
                Date d = new Date();
                DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                double totalPayments = 0;
                try {
                    Date newDateStr = format.parse(format.format(d));
                    currentDate = newDateStr.getTime();
                    Log.d(TAG, "currentDate :" + currentDate);
                    PaymentReportFacade newRecord = new PaymentReportFacade();
                    if (paymentReportFacade.getTimeStamp()> start && paymentReportFacade.getTimeStamp() < end ) {
                        newRecord.setPatientName(paymentReportFacade.getPatientName());
                        newRecord.setPlanName(paymentReportFacade.getPlanName());
                        List<Installment> newInstallment = new ArrayList<>();
                        for (Installment installment : paymentReportFacade.getInstallmentList()) {
                            Log.d(TAG, "AMOUNTNEW: " + installment.getAmount());
                            addCounter();
                            addRevenue(Double.parseDouble(installment.getAmount()));
                            totalPayments = totalPayments + Double.parseDouble(installment.getAmount());
                            Installment i = new Installment();
                            i.setMethod(installment.getMethod());
                            i.setDate(installment.getDate());
                            i.setAmount(installment.getAmount());
                            newInstallment.add(i);
                        }
                        newRecord.setInstallmentList(newInstallment);
                        pdfInstallmentList.add(newRecord);
                    }

                    addBalance(Double.parseDouble(paymentReportFacade.getTotal()) - totalPayments);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            ReportPaymentAdapter reportPaymentAdapter = new ReportPaymentAdapter(getContext(), pdfInstallmentList, "INSTALLMENT");
            rvReportPayment.setAdapter(reportPaymentAdapter);
        }
    }

    private void generateInstallmentPDF(List<PaymentReportFacade> pdfInstallmentList) {
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
        canvas.drawText("Plan Name", (float) (canvas.getWidth() * .35), 90, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(8.5f);
        canvas.drawText("Date",  (float) (canvas.getWidth() * .60), 90, paint);
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
        for (PaymentReportFacade report : pdfInstallmentList) {
            currentY = currentY + 10;
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(7f);
            canvas.drawText(report.getPatientName(), (float) (canvas.getWidth() * .1 ), currentY, paint);
            paint.setTextSize(7f);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(report.getPlanName(), (float) (canvas.getWidth() * .35), currentY, paint);
            paint.setTextSize(7f);
            for(Installment installment: report.getInstallmentList()) {
                currentY = currentY + 10;
                canvas.drawText(installment.getDate(), (float) (canvas.getWidth() * .60), currentY, paint);
                canvas.drawText(installment.getAmount(), (float) (canvas.getWidth() -50), currentY, paint);
            }
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

    private void generateFullPaymentPDF(List<PaymentReportFacade> pdfFullpaymentList) {
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
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.5f);
        canvas.drawText("Date", 150, 90, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.5f);
        canvas.drawText("Patient Name", canvas.getWidth() / 2, 90, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.5f);
        canvas.drawText("Amount", 450, 90, paint);

        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 100, 575, 100, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(575, 80, 575, 100, solidLinePaint);

        currentY = 110;
        for (PaymentReportFacade report : pdfFullpaymentList) {
            currentY = currentY + 10;
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(7f);
            canvas.drawText(report.getDate(), 120, currentY, paint);
            paint.setTextSize(7f);
            canvas.drawText(report.getPatientName(), 230, currentY, paint);
            paint.setTextSize(7f);
            canvas.drawText(report.getTotal(), 450, currentY, paint);
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

    private void populatePatientKey() {
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {

                    String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                  PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                  paymentReportFacade.setPatientName(patientName);
                  paymentReportFacade.setPatientKey(patientKey);
                  paymentReportFacadeList.add(paymentReportFacade);

                }
                populatePaymentRecords(paymentReportFacadeList);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void populatePaymentRecords(List<PaymentReportFacade> paymentReportFacadeList) {
        for(PaymentReportFacade paymentReportFacade: paymentReportFacadeList) {
            String patientKey = paymentReportFacade.getPatientKey();
            String patientName = paymentReportFacade.getPatientName();
            DatabaseReference installmentRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("INSTALLMENT").child(patientKey);
            installmentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
                            PaymentReportFacade installmentFacade = new PaymentReportFacade();
                         installmentFacade.setPatientKey(patientKey);
                          installmentFacade.setPatientName(patientName);
                          installmentFacade.setPlanName(paymentSnapshot.getValue(PatientPayment.class).getPlanName());
                          installmentFacade.setDate(paymentSnapshot.getValue(PatientPayment.class).getDate());
                          installmentFacade.setTotal(paymentSnapshot.getValue(PatientPayment.class).getTotal());
                          installmentFacade.setRemarks(paymentSnapshot.getValue(PatientPayment.class).getTotal());
                          installmentFacade.setTimeStamp(paymentSnapshot.getValue(PatientPayment.class).getTimeStamp());
                          List<Installment> installmentList = new ArrayList<>();
                          for(DataSnapshot payments: paymentSnapshot.child("payment").getChildren()){
                              Installment installment = new Installment();
                              installment.setDate(payments.getValue(Installment.class).getDate());
                              installment.setTimeStamp(payments.getValue(Installment.class).getTimeStamp());
                              installment.setMethod(payments.getValue(Installment.class).getMethod());
                              installment.setAmount(payments.getValue(Installment.class).getAmount());
                              installment.setRemarks(payments.getValue(Installment.class).getRemarks());
                             installmentList.add(installment);
                          }
                        installmentFacade.setInstallmentList(installmentList);
                        installmentMasterList.add(installmentFacade);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            DatabaseReference fullpaymentRef = mFirebaseDatabase.getReference("PaymentsNew").child(userID).child("FULL PAYMENT").child(patientKey);
            fullpaymentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
                        PaymentReportFacade installmentFacade = new PaymentReportFacade();
                         installmentFacade.setPatientKey(patientKey);
                          installmentFacade.setPatientName(patientName);
                          installmentFacade.setTimeStamp(paymentSnapshot.getValue(PatientPayment.class).getTimeStamp());
                          installmentFacade.setDate(paymentSnapshot.getValue(PatientPayment.class).getDate());
                          installmentFacade.setMethod(paymentSnapshot.getValue(PatientPayment.class).getMethod());
                          installmentFacade.setTotal(paymentSnapshot.getValue(PatientPayment.class).getTotal());
                          installmentFacade.setRemarks(paymentSnapshot.getValue(PatientPayment.class).getTotal());

                          fullpaymentMasterList.add(installmentFacade);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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
        List<String> typeList = new ArrayList<>();
        typeList.add("FULL PAYMENT");
        typeList.add("INSTALLMENT");
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, typeList);
        spinnerFilterType.setAdapter(arrayAdapter1);
    }

    private void buildPaymentReport(long start, long end) {
//        myRef = mFirebaseDatabase.getReference("Patient").child(userID);
//
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
//                    String patientKey = patientSnapshot.getValue(Patient.class).getKey();
//                    Query installmentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT").orderByChild("timeStamp").startAt(start).endAt(end);
//                    installmentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                            for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
//
//                                double dbAmount = 0;
//                                for (DataSnapshot installmentSnapshot : paymentSnapshot.child("payment").getChildren()) {
//                                    String amount = installmentSnapshot.getValue(Installment.class).getAmount();
//                                    Log.d(TAG, "amount:" + amount);
//                                    dbAmount = dbAmount + Double.parseDouble(amount);
//                                    addRevenue(Double.parseDouble(amount));
//                                    addCounter();
//                                    PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
//                                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
//                                    paymentReportFacade.setPatientName(patientName);
//                                    paymentReportFacade.setAmount(installmentSnapshot.getValue(Installment.class).getAmount());
//                                    paymentReportFacade.setDate(installmentSnapshot.getValue(Installment.class).getDate());
//                                    installmentList.add(paymentReportFacade);
//
//                                }
//                                String total = paymentSnapshot.getValue(PatientPayment.class).getTotal();
//                                double installment = Double.parseDouble(total) - dbAmount;
//                                addBalance(installment);
//
//                            }
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    Query fullpaymentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("FULL PAYMENT").orderByChild("timeStamp").startAt(start).endAt(end);
//                    fullpaymentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                            for (DataSnapshot fullPaymentSnapshot : snapshot.getChildren()) {
//                                String total = fullPaymentSnapshot.getValue(PatientPayment.class).getTotal();
//                                fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
//                                addRevenue(Double.parseDouble(total));
//                                addCounter();
//                                PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
//                                String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
//                                paymentReportFacade.setPatientName(patientName);
//                                paymentReportFacade.setAmount(fullPaymentSnapshot.getValue(PatientPayment.class).getTotal());
//                                paymentReportFacade.setDate(fullPaymentSnapshot.getValue(Installment.class).getDate());
//                                fullPaymentList.add(paymentReportFacade);
//
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
    private void addBalance(double installment) {
        balanceTotal = balanceTotal + installment;
        Log.d(TAG, "balanceTotal:"  + balanceTotal);
        tvReportBalance.setText("P"+String.valueOf(balanceTotal));
    }

    private void addRevenue(double parseDouble) {
        revenueTotal = revenueTotal + parseDouble;
        Log.d(TAG, "revenueTotal:"  + revenueTotal);
        tvReportRevenue.setText("P"+String.valueOf(revenueTotal));
    }

    private void addCounter() {
        counter++;
        tvReportCount.setText(String.valueOf(counter));
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
    public ReportPaymentFragment() {
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
        view =  inflater.inflate(R.layout.fragment_report_payment, container, false);
        return view;
    }
}