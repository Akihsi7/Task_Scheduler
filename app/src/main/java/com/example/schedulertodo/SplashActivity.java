package com.example.schedulertodo;


import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulertodo.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerEvents();
    }

    private void registerEvents() {
        Intent SplashtoSignUp = new Intent(this, SignupActivity.class);
        Intent SplashtoLogin = new Intent(this, LoginActivity.class);
        binding.Loginbuttonhome.setOnClickListener(v -> startActivity(SplashtoLogin));
        binding.elseSignup.setOnClickListener(v -> startActivity(SplashtoSignUp));
    }
}