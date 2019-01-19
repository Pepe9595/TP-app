package com.example.peter.myapplication;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SessionHandler session;
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();
        TextView welcomeText = findViewById(R.id.welcomeText);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.design_navigation_view);
        TextView txtProfileName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name_in_header);
        txtProfileName.setText(user.getFullName());
        txtProfileName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email_in_header);
        txtProfileName.setText(user.getEmail());
        welcomeText.setText("Login: " + user.getUsername() + "\nWelcome "+user.getFullName()+", your session will expire on "+user.getSessionExpiryDate()+"\nEmail: "+user.getEmail());
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(R.id.nav_home);



//        Button logoutBtn = findViewById(R.id.btnLogout);
//        logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                session.logoutUser();
//                Intent i = new Intent(DashboardActivity.this, LoginActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.nav_home:
//                intent = new Intent(this, DashboardActivity.class);
//                startActivity(intent);
                break;
            case R.id.nav_chart:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScatterGraphFragment()).commit();
                intent = new Intent(this, ScatterGraphFragment.class);
                startActivity(intent);
                break;
            case R.id.nav_values:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ValuesFragment()).commit();
                intent = new Intent(this, ValuesFragment.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                session.logoutUser();
                Intent i = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }

    }


}
