package com.example.pmis.Helpers;

import androidx.annotation.NonNull;

import com.example.pmis.Model.Patient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PatientEmails {
    List<String> emailList = new ArrayList<>();
    private FirebaseDatabase mFirebaseDatabase;
    String userID;

    public PatientEmails(String userID){
        this.userID = userID;

    }
    public void patientEmail(){


    }

    public List<String> getEmailList() {
        return emailList;
    }
}
