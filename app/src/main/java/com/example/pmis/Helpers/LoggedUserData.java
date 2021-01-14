package com.example.pmis.Helpers;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoggedUserData {
    public LoggedUserData(){

    }
    String userID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;

    public String userID(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        return userID;
    }


}
