package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Schedule;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "EDIT_SCHEDULE";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, docRef, saveRef;
    private Button btnSchedSave;
    private EditText etSchedName, etSchedDoc, etSchedDate, etSchedStart, etSchedEnd, etSchedRemarks;
    private final LoggedUserData loggedUserData = new LoggedUserData();
    private String userID, docName, fullName, patientKey, scheduleKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);
        viewFinder();
        userID  = loggedUserData.userID();
        Intent intent = getIntent();
        patientKey = intent.getStringExtra("patientKey");
        scheduleKey = intent.getStringExtra("scheduleKey");
        fullName = intent.getStringExtra("fullName");
        Log.d(TAG, "patientKey: " + patientKey);
        etSchedName.setText(fullName);
        etSchedDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                }
            }
        });
        etSchedStart.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    Calendar mCurrentTime = Calendar.getInstance();
                    int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mCurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(EditScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            etSchedStart.setText(hourOfDay + ":" + minute);
                        }
                    }, hour, minute, true);
                    mTimePicker.setTitle("Select Start Time");
                    mTimePicker.show();
                }
            }
        });
        etSchedEnd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    Calendar mCurrentTime = Calendar.getInstance();
                    int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mCurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(EditScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            etSchedEnd.setText(hourOfDay + ":" + minute);
                        }
                    }, hour, minute, true);
                    mTimePicker.setTitle("Select End Time");
                    mTimePicker.show();
                }
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        buildDoctorName();
        myRef = mFirebaseDatabase.getReference("Schedules").child(userID).child(scheduleKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                etSchedDate.setText(snapshot.getValue(Schedule.class).getDate());
                etSchedStart.setText(snapshot.getValue(Schedule.class).getStartTime());
                etSchedEnd.setText(snapshot.getValue(Schedule.class).getEndTime());
                etSchedRemarks.setText(snapshot.getValue(Schedule.class).getRemarks());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnSchedSave.setOnClickListener(saveSchedule);

    }
    private final View.OnClickListener saveSchedule = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                String date = etSchedDate.getText().toString().trim();
                String startTime = etSchedStart.getText().toString().trim();
                String endTime = etSchedEnd.getText().toString().trim();
                String remarks = etSchedRemarks.getText().toString().trim();
                Schedule schedule = new Schedule();
                schedule.setDocName(docName);
                schedule.setPatientKey(patientKey);
                schedule.setStartTime(startTime);
                schedule.setDate(date);
                schedule.setEndTime(endTime);
                schedule.setRemarks(remarks);
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                saveRef = mFirebaseDatabase.getReference("Schedules").child(userID).child(scheduleKey);
                schedule.setKey(scheduleKey);
                saveRef.setValue(schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditScheduleActivity.this, "Schedule Edited succesfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        }
    };

    private boolean validate() {
        String date = etSchedDate.getText().toString().trim();
        String startTime = etSchedStart.getText().toString().trim();
        String endTime = etSchedEnd.getText().toString().trim();
        String remarks = etSchedRemarks.getText().toString().trim();
        if(date.isEmpty()){
            etSchedDate.setError("Date is required");
            etSchedDate.requestFocus();
            return false;
        }
        if(startTime.isEmpty()){
            etSchedStart.setError("Start Time is required");
            etSchedStart.requestFocus();
            return false;
        } if(endTime.isEmpty()){
            etSchedEnd.setError("End Time is required");
            etSchedEnd.requestFocus();
            return false;
        } if(remarks.isEmpty()){
            etSchedRemarks.setError("Remarks is required");
            etSchedRemarks.requestFocus();
            return false;
        }
        return true;
    }
    private void viewFinder() {
        etSchedDate = findViewById(R.id.etSchedDate);
        etSchedDoc = findViewById(R.id.etSchedDoc);
        etSchedStart = findViewById(R.id.etSchedStart);
        etSchedName = findViewById(R.id.etSchedName);
        etSchedEnd = findViewById(R.id.etSchedEnd);
        etSchedRemarks = findViewById(R.id.etSchedRemarks);
        btnSchedSave = findViewById(R.id.btnSchedSave);
    }

    private void buildDoctorName() {
        docRef = mFirebaseDatabase.getReference("Users").child(userID);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);

                docName = "Dr. " +  firstName + ' ' + lastName + " D.M.D";
                etSchedDoc.setText(docName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());
       // etSchedDate.setText(currentDateString);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
        etSchedDate.setText(format.format(cal.getTime()));
    }
}