package com.example.pmis;

import androidx.annotation.NonNull;

import com.example.pmis.Model.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceUserInfo;
    private List<UserInfo> userInfos = new ArrayList<>();

    public FirebaseDatabaseHelper(){
        mDatabase = FirebaseDatabase.getInstance();
        mReferenceUserInfo = mDatabase.getReference("Users");
    }
    public interface DataStatus{
        void DataisLoaded(List<UserInfo> userInfos, List<String> keys);
    }
    public void readUserInfo(){
        mReferenceUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userInfos.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    UserInfo userinfo = keyNode.getValue(UserInfo.class);
                    userInfos.add(userinfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
