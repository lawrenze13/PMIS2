package com.example.pmis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.pmis.Adapter.IntroPageAdapter;
import com.example.pmis.Adapter.PaymentPageAdapter;

public class NewUserActivity extends AppCompatActivity {
    private ViewPager2 vpIntro;

//    @Override
//    public void onBackPressed() {
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        vpIntro = findViewById(R.id.vpIntro);
        vpIntro.setAdapter(new IntroPageAdapter(this));
    }
}