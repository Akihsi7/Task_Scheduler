package com.example.schedulertodo;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toolbar;

import com.example.schedulertodo.databinding.ActivityBaseBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;





public class Base extends AppCompatActivity {

    private ActivityBaseBinding binding;
    private static final String TAG = "Base";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set up the BottomNavigationView and NavController
        BottomNavigationView navView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);

        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
                            navController.navigate(R.id.action_global_navigation_home);
                        }
                        return true;

                    case R.id.nav_scheduler:
                        if (navController.getCurrentDestination().getId() != R.id.navigation_dashboard) {
                            navController.navigate(R.id.action_global_navigation_dashboard);
                        }
                        return true;

                    case R.id.nav_pending:
                        if (navController.getCurrentDestination().getId() != R.id.pendingFragment) {
                            navController.navigate(R.id.action_global_pendingFragment);
                        }
                        return true;

                    case R.id.nav_profile:
                        if (navController.getCurrentDestination().getId() != R.id.profileFragment) {
                            navController.navigate(R.id.action_global_profileFragment);
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }
}