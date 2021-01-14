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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmis.Helpers.GetDoctorName;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.DrugPrescriptionMain;
import com.example.pmis.Model.Installment;
import com.example.pmis.Model.Patient;
import com.example.pmis.Model.PatientPayment;
import com.example.pmis.Model.PaymentReportFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class ReportFragment extends Fragment {

   private Spinner spinnerFilterPayment, spinnerFilterAppointment;
   private TextView tvReportRevenue, tvReportBalance, tvReportCount;
   private Button btnPayment;
    private View view;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, paymentRef, fullPaymentRef;
    private List<String> filterList = new ArrayList<>();
    private LoggedUserData loggedUserData = new LoggedUserData();
    private double revenueTotal, fullPaymentTotal, balanceTotal;
    private static final String TAG = "REPORT_FRAGMENT";
    private int installmentCounter, fullpaymentCounter,counter;
    private List<PaymentReportFacade> fullPaymentList;
    private List<PaymentReportFacade> installmentList;
    private List<String> installmentPatientNameList;

    String userID, clinicName, clinicAddress, docName, clinicContactNo;
    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        fullPaymentList = new ArrayList<>();
        installmentPatientNameList = new ArrayList<String>();
        installmentList = new ArrayList<PaymentReportFacade>();
        spinnerFilterPayment = view.findViewById(R.id.spinnerFilterPayment);
        spinnerFilterAppointment = view.findViewById(R.id.spinnerFilterAppointment);
        btnPayment = view.findViewById(R.id.btnPayment);
        btnPayment.setOnClickListener(generatePaymentPDF);
        tvReportRevenue = view.findViewById(R.id.tvReportRevenue);
        tvReportBalance = view.findViewById(R.id.tvReportBalance);
        tvReportCount = view.findViewById(R.id.tvReportCount);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userID = loggedUserData.userID();
        getClinicInfo();
        getDoctorName();

        buildFilterDropdown();

        spinnerFilterPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                balanceTotal = 0;
                revenueTotal = 0;
                fullPaymentTotal = 0;
                installmentCounter = 0;
                fullpaymentCounter = 0;
                counter = 0;
                tvReportBalance.setText("P0.00");
                tvReportRevenue.setText("P0.00");
                tvReportCount.setText("0");
                installmentList.clear();
                fullPaymentList.clear();
                if (position == 0) {
                    myRef = mFirebaseDatabase.getReference("Patient").child(userID);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                                String patientKey = patientSnapshot.getValue(Patient.class).getKey();

                                paymentRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT");
                                paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
                                            double dbAmount = 0;
                                            PaymentReportFacade paymentReportFacade = new PaymentReportFacade();

                                            for (DataSnapshot installmentSnapshot : paymentSnapshot.child("payment").getChildren()) {
                                                String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                                paymentReportFacade.setPatientName(patientName);
                                                paymentReportFacade.setAmount(installmentSnapshot.getValue(Installment.class).getAmount());
                                                paymentReportFacade.setDate(installmentSnapshot.getValue(Installment.class).getDate());
                                                installmentList.add(paymentReportFacade);
                                                String amount = installmentSnapshot.getValue(Installment.class).getAmount();

                                                Log.d(TAG, "amount:" + amount);
                                                dbAmount = dbAmount + Double.parseDouble(amount);
                                                addRevenue(Double.parseDouble(amount));
                                                addCounter();
                                            }
                                            String total = paymentSnapshot.getValue(PatientPayment.class).getTotal();
                                            double installment = Double.parseDouble(total) - dbAmount;
                                            addBalance(installment);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                fullPaymentRef = mFirebaseDatabase.getReference("Payments").child(patientKey).child("FULL PAYMENT");
                                fullPaymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot fullPaymentSnapshot : snapshot.getChildren()) {
                                            String total = fullPaymentSnapshot.getValue(PatientPayment.class).getTotal();
                                            fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
                                            addRevenue(Double.parseDouble(total));
                                            addCounter();
                                            PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                                            String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                            paymentReportFacade.setPatientName(patientName);
                                            paymentReportFacade.setAmount(fullPaymentSnapshot.getValue(PatientPayment.class).getTotal());
                                            paymentReportFacade.setDate(fullPaymentSnapshot.getValue(Installment.class).getDate());
                                            fullPaymentList.add(paymentReportFacade);
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
                } else if (position == 1) {
                    long currentDate = 0;
                    Date d = new Date();
                    DateFormat format = new SimpleDateFormat("dd MMM yyyy");
                    try {
                        Date newDateStr = format.parse(format.format(d));
                        currentDate = newDateStr.getTime();
                        Log.d(TAG, "currentDate :" + currentDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    myRef = mFirebaseDatabase.getReference("Patient").child(userID);
                    long finalCurrentDate = currentDate;
                    Log.d(TAG, "finalCurrentDate :" + finalCurrentDate);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                                String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                                Query installmentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT").orderByChild("timeStamp").equalTo(finalCurrentDate);
                                installmentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {

                                                double dbAmount = 0;
                                                for (DataSnapshot installmentSnapshot : paymentSnapshot.child("payment").getChildren()) {
                                                    String amount = installmentSnapshot.getValue(Installment.class).getAmount();
                                                    Log.d(TAG, "amount:" + amount);
                                                    dbAmount = dbAmount + Double.parseDouble(amount);
                                                    addRevenue(Double.parseDouble(amount));
                                                    addCounter();
                                                    PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                                                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                                    paymentReportFacade.setPatientName(patientName);
                                                    paymentReportFacade.setAmount(installmentSnapshot.getValue(Installment.class).getAmount());
                                                    paymentReportFacade.setDate(installmentSnapshot.getValue(Installment.class).getDate());
                                                    installmentList.add(paymentReportFacade);

                                                }
                                                String total = paymentSnapshot.getValue(PatientPayment.class).getTotal();
                                                double installment = Double.parseDouble(total) - dbAmount;
                                                addBalance(installment);

                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                Query fullpaymentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("FULL PAYMENT").orderByChild("timeStamp").equalTo(finalCurrentDate);
                                fullpaymentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        for (DataSnapshot fullPaymentSnapshot : snapshot.getChildren()) {
                                            String total = fullPaymentSnapshot.getValue(PatientPayment.class).getTotal();
                                            fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
                                            addRevenue(Double.parseDouble(total));
                                            addCounter();
                                            PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                                            String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                            paymentReportFacade.setPatientName(patientName);
                                            paymentReportFacade.setAmount(fullPaymentSnapshot.getValue(PatientPayment.class).getTotal());
                                            paymentReportFacade.setDate(fullPaymentSnapshot.getValue(Installment.class).getDate());
                                            fullPaymentList.add(paymentReportFacade);

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
                else if(position == 2){
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
                     buildPaymentReport(start, end);
                }
                else if(position == 3){
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
                    buildPaymentReport(start, end);

                }
                else if(position == 4){
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
                    buildPaymentReport(start, end);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                Log.d(TAG, "docName: " + docName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getClinicInfo() {
        DatabaseReference clinicRef = mFirebaseDatabase.getReference("Clinic").child(userID);
        clinicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clinicName = snapshot.getValue(Clinic.class).getClinicName();
                clinicAddress = snapshot.getValue(Clinic.class).getAddress();
                clinicContactNo = snapshot.getValue(Clinic.class).getContactNo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildPaymentReport(long start, long end) {
        myRef = mFirebaseDatabase.getReference("Patient").child(userID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    String patientKey = patientSnapshot.getValue(Patient.class).getKey();
                    Query installmentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("INSTALLMENT").orderByChild("timeStamp").startAt(start).endAt(end);
                    installmentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {

                                double dbAmount = 0;
                                for (DataSnapshot installmentSnapshot : paymentSnapshot.child("payment").getChildren()) {
                                    String amount = installmentSnapshot.getValue(Installment.class).getAmount();
                                    Log.d(TAG, "amount:" + amount);
                                    dbAmount = dbAmount + Double.parseDouble(amount);
                                    addRevenue(Double.parseDouble(amount));
                                    addCounter();
                                    PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                                    String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                    paymentReportFacade.setPatientName(patientName);
                                    paymentReportFacade.setAmount(installmentSnapshot.getValue(Installment.class).getAmount());
                                    paymentReportFacade.setDate(installmentSnapshot.getValue(Installment.class).getDate());
                                    installmentList.add(paymentReportFacade);

                                }
                                String total = paymentSnapshot.getValue(PatientPayment.class).getTotal();
                                double installment = Double.parseDouble(total) - dbAmount;
                                addBalance(installment);

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Query fullpaymentQuery = mFirebaseDatabase.getReference("Payments").child(patientKey).child("FULL PAYMENT").orderByChild("timeStamp").startAt(start).endAt(end);
                    fullpaymentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot fullPaymentSnapshot : snapshot.getChildren()) {
                                String total = fullPaymentSnapshot.getValue(PatientPayment.class).getTotal();
                                fullPaymentTotal = fullPaymentTotal + Double.parseDouble(total);
                                addRevenue(Double.parseDouble(total));
                                addCounter();
                                PaymentReportFacade paymentReportFacade = new PaymentReportFacade();
                                String patientName = patientSnapshot.getValue(Patient.class).getFirstName() + " " + patientSnapshot.getValue(Patient.class).getLastName();
                                paymentReportFacade.setPatientName(patientName);
                                paymentReportFacade.setAmount(fullPaymentSnapshot.getValue(PatientPayment.class).getTotal());
                                paymentReportFacade.setDate(fullPaymentSnapshot.getValue(Installment.class).getDate());
                                fullPaymentList.add(paymentReportFacade);

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
    private final View.OnClickListener generatePaymentPDF = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

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
                canvas.drawText("General Dentist", 20, 68, paint);
                paint.setTextSize(7f);
                canvas.drawText("License No. 563242612", 20, 75, paint);

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
                for (PaymentReportFacade report : fullPaymentList) {
                    currentY = currentY + 10;
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getDate(), 120, currentY, paint);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getPatientName(), 230, currentY, paint);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getAmount(), 450, currentY, paint);
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
                for (PaymentReportFacade report : installmentList) {
                    currentY = currentY + 10;
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getDate(), 120, currentY, paint);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getPatientName(), 230, currentY, paint);
                    paint.setTextSize(7f);
                    canvas.drawText(report.getAmount(), 450, currentY, paint);
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
    };
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

    private void buildFilterDropdown() {
        filterList.add("All time");
        filterList.add("Today");
        filterList.add("This Week");
        filterList.add("This Month");
        filterList.add("This Year");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filterList);
        spinnerFilterPayment.setAdapter(arrayAdapter);
        spinnerFilterAppointment.setAdapter(arrayAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_report, container, false);
        return  view;
    }
}