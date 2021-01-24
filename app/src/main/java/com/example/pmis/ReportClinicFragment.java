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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pmis.Adapter.DrugListAdapter;
import com.example.pmis.Adapter.ProcedureListAdapter;
import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Clinic;
import com.example.pmis.Model.Drugs;
import com.example.pmis.Model.PatientProcedures;
import com.example.pmis.Model.Procedures;
import com.example.pmis.Model.ReportProcedures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportClinicFragment extends Fragment {
    private static final String TAG = "REPORT_APPOINTMENT";
    private Spinner spinnerFilterPayment;
    private TextView tvReportCount;
    private Button btnPayment, btnSearch;
    private View view;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, paymentRef, fullRef, insRef;
    private List<String> filterList = new ArrayList<>();
    private List<Procedures> proceduresList = new ArrayList<>();
    private List<Drugs> drugsList = new ArrayList<>();
    String userID, clinicName, clinicAddress, docName, clinicContactNo, license, degree;
    private LoggedUserData loggedUserData = new LoggedUserData();
    private int totalProcedure;
    private RecyclerView rvReportProcedures;

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

                if(spinnerFilterPayment.getSelectedItemPosition() == 0){
                    generateDrugsListPDF(drugsList);
                }else if(spinnerFilterPayment.getSelectedItemPosition() == 1){
                    generateProceduresListPDF(proceduresList);
                }
            }
        });
        btnPayment.setVisibility(View.GONE);
        proceduresList = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getClinicInfo();
        getDoctorName();
        buildSpinner();
        getProcedureList();
        getDrugsList();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPayment.setVisibility(View.VISIBLE);
                if(spinnerFilterPayment.getSelectedItemPosition() == 0){
                    tvReportCount.setText(String.valueOf(drugsList.size()));
                    DrugListAdapter drugListAdapter = new DrugListAdapter(getContext(),drugsList);
                    rvReportProcedures.setAdapter(drugListAdapter);
                }else if(spinnerFilterPayment.getSelectedItemPosition() == 1){
                    tvReportCount.setText(String.valueOf(proceduresList.size()));
                    ProcedureListAdapter procedureListAdapter = new ProcedureListAdapter(getContext(),proceduresList);
                    rvReportProcedures.setAdapter(procedureListAdapter);
                }
            }
        });
    }
    private void generateProceduresListPDF(List<Procedures> proceduresList) {
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
        paint.setTextSize(9f);
        canvas.drawText("Procedure", 40, 90, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8.5f);
        canvas.drawText("Description", (float) (canvas.getWidth() * .40), 90, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(9f);
        canvas.drawText("Price",  (float) (canvas.getWidth() * .85), 90, paint);


        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 100, 575, 100, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(575, 80, 575, 100, solidLinePaint);

        currentY = 110;
        for (Procedures report : proceduresList) {

            currentY = currentY + 10;
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(7f);
            canvas.drawText(report.getName(), 40, currentY, paint);
            paint.setTextSize(7f);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(report.getDescription(), (float) (canvas.getWidth() * .40), currentY, paint);
            paint.setTextSize(7f);
            canvas.drawText(String.valueOf(report.getPrice()), (float) (canvas.getWidth() * .85), currentY, paint);
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
    private void generateDrugsListPDF(List<Drugs> drugsList) {
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
        canvas.drawText("Brand", (float) (canvas.getWidth() * 2), 90, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.5f);
        canvas.drawText("Drug Name", (float) (canvas.getWidth() * .50), 90, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.5f);
        canvas.drawText("Dosage",  (float) (canvas.getWidth() * .75), 90, paint);


        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(20, 100, 575, 100, solidLinePaint);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1);
        canvas.drawLine(575, 80, 575, 100, solidLinePaint);

        currentY = 110;
        for (Drugs report : drugsList) {

            currentY = currentY + 10;
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(7f);
            canvas.drawText(report.getDrugBrand(), (float) (canvas.getWidth() * .25), currentY, paint);
            paint.setTextSize(7f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(report.getDrugName(), (float) (canvas.getWidth() * .50), currentY, paint);
            paint.setTextSize(7f);
            canvas.drawText(report.getDrugDosage(), (float) (canvas.getWidth() * .75), currentY, paint);
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

    private void getDrugsList() {
        DatabaseReference drugRef = mFirebaseDatabase.getReference("Drugs").child(userID);
        drugRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drugsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Drugs drugs = new Drugs();
                    drugs.setDrugBrand(ds.getValue(Drugs.class).getDrugBrand());
                    drugs.setDrugDosage(ds.getValue(Drugs.class).getDrugBrand());
                    drugs.setDrugName(ds.getValue(Drugs.class).getDrugBrand());
                    drugs.setKey(ds.getValue(Drugs.class).getKey());
                    drugsList.add(drugs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getProcedureList() {
        DatabaseReference procRef = mFirebaseDatabase.getReference("Procedures").child(userID);
        procRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                proceduresList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Procedures procedures = new Procedures();
                    procedures.setPrice(ds.getValue(Procedures.class).getPrice());
                    procedures.setDescription(ds.getValue(Procedures.class).getDescription());
                    procedures.setName(ds.getValue(Procedures.class).getName());
                    procedures.setKey(ds.getValue(Procedures.class).getKey());
                    procedures.setEquipments(ds.getValue(Procedures.class).getEquipments());
                    proceduresList.add(procedures);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildSpinner() {
        filterList.add("Drugs");
        filterList.add("Procedures");
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_report_clinic, container, false);
        return view;
    }
}