package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmis.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private TextView register;
    private EditText txtFirstName, txtLastName, txtEmail, txtPassword;
    private Button btnSignup;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        register = findViewById(R.id.btnSignUp);
        mAuth =  FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
    }
    public void signup(View v) {
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        if(firstName.isEmpty()){
            txtFirstName.setError("First Name is required");
            txtFirstName.requestFocus();
            return;
        }
        if(lastName.isEmpty()){
            txtLastName.setError("Last Name is required");
            txtLastName.requestFocus();
            return;
        }
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
            txtPassword.requestFocus();
            return;

        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                   User user = new User(firstName,lastName,email);

                   FirebaseDatabase.getInstance().getReference("Users")
                           .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                           .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                               currentUser.sendEmailVerification();

                               Toast.makeText(SignUpActivity.this,"User has been registered successfully. Check your email for verification", Toast.LENGTH_LONG).show();
                               startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                           }else{
                               Toast.makeText(SignUpActivity.this,"Failed to register, try again", Toast.LENGTH_LONG).show();
                           }
                           progressBar.setVisibility(View.GONE);
                       }
                   });

                }else{
                    Toast.makeText(SignUpActivity.this,"Failed to login!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}