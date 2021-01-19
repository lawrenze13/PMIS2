package com.example.pmis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnReset;
    private EditText txtEmail;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(resetEmail);
        txtEmail = findViewById(R.id.txtEmail);
    }
    private final View.OnClickListener resetEmail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                String email = txtEmail.getText().toString().trim();
                mAuth = FirebaseAuth.getInstance();
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ForgotPasswordActivity.this, "Please check your email for reset link.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this, "Cannot find the provided email on our database. Please try again.", Toast.LENGTH_LONG).show();

                    }
                });
            }
        }
    };

    private boolean validate() {
        String email = txtEmail.getText().toString().trim();
        if(email.isEmpty()){
            txtEmail.setError("Please provide an email.");
            txtEmail.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmail.setError("Please Provide a valid Email");
            txtEmail.requestFocus();
            return false;
        }
        return true;
    }
}