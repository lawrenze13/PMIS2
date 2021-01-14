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
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.pmis.Helpers.LoggedUserData;
import com.example.pmis.Model.Schedule;
import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddScheduleActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = "ADD_SCHEDULE";
    private EditText etSchedName, etSchedDoc, etSchedDate, etSchedStart, etSchedEnd, etSchedRemarks;
    private Button btnSchedSave;
    String fullName, patientKey, docName;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference docRef, saveRef;
    private final LoggedUserData loggedUserData = new LoggedUserData();
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);
        findView();
        ImageButton btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent patientIntent = getIntent();
        fullName = patientIntent.getStringExtra("fullName");
        patientKey = patientIntent.getStringExtra("patientKey");
        etSchedName.setText(fullName);
        userID  = loggedUserData.userID();
        Log.d(TAG, userID);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        buildDoctorName();
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
                    mTimePicker = new TimePickerDialog(AddScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                    mTimePicker = new TimePickerDialog(AddScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
        btnSchedSave.setOnClickListener(saveSchedule);
    }
    private final View.OnClickListener saveSchedule = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                DatabaseReference checkHourRef = mFirebaseDatabase.getReference("Schedules").child(patientKey);
                String startTime = etSchedStart.getText().toString().trim();
                String endTime = etSchedEnd.getText().toString().trim();
                String date = etSchedDate.getText().toString().trim();
                String remarks = etSchedRemarks.getText().toString().trim();
                            Schedule schedule = new Schedule();
                            schedule.setDocName(docName);
                            schedule.setPatientKey(patientKey);
                            schedule.setStartTime(startTime);
                            schedule.setDate(date);
                            String dateTime = date + " " + startTime;
                            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm");
                            try {
                                Date currentDate = format.parse(dateTime);
                                long timeStamp = currentDate.getTime();
                                schedule.setTimeStamp(timeStamp);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            schedule.setEndTime(endTime);
                            schedule.setRemarks(remarks);
                            mFirebaseDatabase = FirebaseDatabase.getInstance();
                            saveRef = mFirebaseDatabase.getReference("Schedules").child(patientKey);
                            String scheduleKey = saveRef.push().getKey();
                            schedule.setKey(scheduleKey);
                            saveRef.child(scheduleKey).setValue(schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(AddScheduleActivity.this, "Schedule added succesfully", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                String dateTimeStart = date + " " + startTime;
                String dateTimeEnd = date + " " + endTime;
//                SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm");
//                long timeStampStart = 0;
//                long timeStampEnd = 0;
//                try {
//                    Date currentDateStart = format.parse(dateTimeStart);
//                    Date currentDateEnd= format.parse(dateTimeEnd);
//                     timeStampStart = currentDateStart.getTime();
//                     timeStampEnd = currentDateEnd.getTime();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                Query query = checkHourRef.orderByChild("timeStamp").startAt(timeStampEnd).endAt(timeStampStart + "\uf8ff");
//                Log.d(TAG, "sTARTTIME ENDTIME " + timeStampStart + " " + timeStampEnd);
//                query.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.hasChildren()){
//
//                            for(DataSnapshot ds: snapshot.getChildren()){
//                                Log.d(TAG, "captured time " + ds.getValue(Schedule.class).getTimeStamp());
//                                Toast.makeText(AddScheduleActivity.this, ds.getValue(Schedule.class).getStartTime() + " to " + ds.getValue(Schedule.class).getEndTime() + " is unavailable. Please Change the start time and end time.", Toast.LENGTH_LONG).show();
//                            }
//                        }else{
//                            String remarks = etSchedRemarks.getText().toString().trim();
//                            Schedule schedule = new Schedule();
//                            schedule.setDocName(docName);
//                            schedule.setPatientKey(patientKey);
//                            schedule.setStartTime(startTime);
//                            schedule.setDate(date);
//                            String dateTime = date + " " + startTime;
//                            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm");
//                            try {
//                                Date currentDate = format.parse(dateTime);
//                                long timeStamp = currentDate.getTime();
//                                schedule.setTimeStamp(timeStamp);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                            schedule.setEndTime(endTime);
//                            schedule.setRemarks(remarks);
//                            mFirebaseDatabase = FirebaseDatabase.getInstance();
//                            saveRef = mFirebaseDatabase.getReference("Schedules").child(patientKey);
//                            String scheduleKey = saveRef.push().getKey();
//                            schedule.setKey(scheduleKey);
//                            saveRef.child(scheduleKey).setValue(schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Toast.makeText(AddScheduleActivity.this, "Schedule added succesfully", Toast.LENGTH_LONG).show();
//                                    finish();
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

            }
        }
    };

    private boolean validate() {
        String date = etSchedDate.getText().toString().trim();
        String startTime = etSchedStart.getText().toString().trim();
        String endTime = etSchedEnd.getText().toString().trim();
        String remarks = etSchedRemarks.getText().toString().trim();
        String hourStart[] = startTime.split(":");
        String hourEnd[] = endTime.split(":");
        if(Integer.parseInt(hourStart[0]) > Integer.parseInt(hourEnd[0])){
            Toast.makeText(AddScheduleActivity.this,"Start Time cannot be Higher than End Time.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(startTime == endTime ){
            Toast.makeText(AddScheduleActivity.this,"Start Time and End time cannot be equal", Toast.LENGTH_LONG).show();
            return false;
        }
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

    private void findView() {
        etSchedDate = findViewById(R.id.etSchedDate);
        etSchedName = findViewById(R.id.etSchedName);
        etSchedDoc = findViewById(R.id.etSchedDoc);
        etSchedStart = findViewById(R.id.etSchedStart);
        etSchedEnd = findViewById(R.id.etSchedEnd);
        etSchedRemarks = findViewById(R.id.etSchedRemarks);
        btnSchedSave = findViewById(R.id.btnSchedSave);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());
        etSchedDate.setText(currentDateString);
    }
}