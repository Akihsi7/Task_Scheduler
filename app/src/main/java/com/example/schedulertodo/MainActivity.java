package com.example.schedulertodo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private  static  int SPLASH_SCREEN = 3000;
    Animation topAnim, bottomAnim;
    ImageView image;
    ImageView logo;
    TextView slogan;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        // Use the token for sending notifications to this device
                        Log.d(TAG, "FCM Registration Token: " + token);
                    } else {
                        Log.w(TAG, "Failed to retrieve token: " + task.getException());
                    }
                });


        setContentView(R.layout.activity_main);
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        image = findViewById(R.id.imageView1);
        logo = findViewById(R.id.imageView3);
        slogan = findViewById(R.id.textView1);
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(MainActivity.this, SplashActivity.class);
                Intent intent2 = new Intent(MainActivity.this, Base.class);
                auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    startActivity(intent2);
                } else {
                    startActivity(intent1);
                }
                finish();
            }
        },SPLASH_SCREEN);

    }
}