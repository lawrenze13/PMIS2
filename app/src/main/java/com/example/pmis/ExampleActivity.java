package com.example.pmis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ExampleActivity extends AppCompatActivity {
    private Button btnAdd;
    private TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        tvName = findViewById(R.id.tvName);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvName.setText("My Name is Juan");
            }
        });

    }
}