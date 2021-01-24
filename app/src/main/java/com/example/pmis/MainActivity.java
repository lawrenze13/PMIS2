package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Helpers.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private TextView lblFullName, lblClinic;
    private EditText txtEmail, txtPassword;
    private Button btnSignup, btnForgot;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtEmail = findViewById(R.id.txtEmail);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        txtPassword = findViewById(R.id.txtPassword);
        btnForgot = findViewById(R.id.btnForgot);
        loadingDialog = new LoadingDialog(MainActivity.this);
        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        mAuth =  FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()){
          DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Clinic").child(user.getUid());
          dRef.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  if(!snapshot.exists()){
                      startActivity(new Intent(MainActivity.this, NewUserActivity.class));
                  }else{

                          startActivity(new Intent(MainActivity.this, HomeActivity.class));

                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
        }

    }
    public void login(View v){

        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        if(email.isEmpty()){
            txtEmail.setError("Email is required");
            txtEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            txtPassword.setError("Password is required");
            txtPassword.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmail.setError("Please Provide a valid Email");
            txtEmail.requestFocus();
            return;
        }
        if(password.length() < 6){
            txtPassword.setError("Min. password length is 6 Characters");
            txtPassword.requestFocus();
            return;
        }
        loadingDialog.startLoadingDialog();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()){
                        loadingDialog.dismissDialog();
//                        if(isNew){
//                            startActivity(new Intent(MainActivity.this, NewUserActivity.class));
//                        }else {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                     //   }
                    }else{
                        user.sendEmailVerification();
                        loadingDialog.dismissDialog();
                        Toast.makeText(MainActivity.this,"Check your email to verify your account!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismissDialog();
                Toast.makeText(MainActivity.this,"Incorrect Credentials. Please Try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void signUp(View v){
        Intent intent = new Intent(this, SignUpActivity.class);
       startActivity(intent);
    }
}