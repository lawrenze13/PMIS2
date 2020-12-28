package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etAge, etEmail;
    private TextView txtFullName;
    private RadioGroup rgSex;
    private RadioButton rMale, rFemale, rSex;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        txtFullName = (TextView)findViewById(R.id.txtFullName);
        etFirstName = (EditText)findViewById(R.id.etFirstName);
        etLastName = (EditText)findViewById(R.id.etLastName);
        etAge = (EditText)findViewById(R.id.etAge);
        etEmail = (EditText)findViewById(R.id.etEmail);
        rgSex = (RadioGroup)findViewById(R.id.rgSex);
        int selectedID = rgSex.getCheckedRadioButtonId();
        rMale = (RadioButton)findViewById(R.id.rMale);
        rFemale = (RadioButton)findViewById(R.id.rFemale);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        myRef = mFirebaseDatabase.getReference().child("Users").child(userID);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                UserInfo uInfo = new UserInfo();

                    uInfo.setFirstName(datasnapshot.getValue(UserInfo.class).getFirstName());
                    uInfo.setLastName(datasnapshot.getValue(UserInfo.class).getLastName());
                    uInfo.setAge(datasnapshot.getValue(UserInfo.class).getAge());
                    uInfo.setEmail(datasnapshot.getValue(UserInfo.class).getEmail());
                    uInfo.setSex(datasnapshot.getValue(UserInfo.class).getSex());
                    rgSex.findViewById(R.id.rgSex);
                    etFirstName.setText(uInfo.firstName);
                    etLastName.setText(uInfo.lastName);
                    etEmail.setText(uInfo.email);
                    etAge.setText(uInfo.age);
                    String fullName = uInfo.firstName + " " + uInfo.lastName;
                    txtFullName.setText(fullName);
                    Intent intent = getIntent();
                    String sex = intent.getStringExtra("sex");
                    if(sex != null){
                    if(sex.equals("Male")){
                        rgSex.check(R.id.rMale);
                    }else{
                        rgSex.check(R.id.rFemale);
                    }
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public boolean validateForm(){
        String email = etEmail.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        if(email.isEmpty()){
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if(firstName.isEmpty()){
            etFirstName.setError("First Name is required");
            etFirstName.requestFocus();
            return false;
        }
        if(lastName.isEmpty()){
            etLastName.setError("Last Name is required");
            etLastName.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please Provide a valid Email");
            etEmail.requestFocus();
            return false;
        }
        if(age.isEmpty()){
            etAge.setError("Age is required!");
            etAge.requestFocus();
            return false;
        }
        return true;
    }
    public void cancel(View v){
        finish();
    }
    public void submit(View v){
        if(validateForm()){
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            userID = user.getUid();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference("Users").child(userID);
            UserInfo userInfo = new UserInfo();
            userInfo.setFirstName(etFirstName.getText().toString().trim());
            userInfo.setLastName(etLastName.getText().toString().trim());
            userInfo.setAge(etAge.getText().toString().trim());
            userInfo.setEmail(etEmail.getText().toString().trim());
            int selectedID = rgSex.getCheckedRadioButtonId();
            rSex = (RadioButton)findViewById(selectedID);
            userInfo.setSex(rSex.getText().toString().trim());
           myRef.setValue(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Toast.makeText(EditProfileActivity.this, "Update success!", Toast.LENGTH_LONG).show();
                   finish();

               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(EditProfileActivity.this, "Failed to update. Please Try again!", Toast.LENGTH_LONG).show();

               }
           });


        }
    }
}