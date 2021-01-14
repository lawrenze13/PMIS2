package com.example.pmis.Helpers;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetDoctorName {
    String docName;
    private DatabaseReference docRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private LoggedUserData loggedUserData;

    public String docName(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        docRef = mFirebaseDatabase.getReference("Users").child(userID);
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
        return docName;
    }
}
