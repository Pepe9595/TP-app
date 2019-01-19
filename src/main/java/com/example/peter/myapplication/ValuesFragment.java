package com.example.peter.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//public class ValuesFragment extends Fragment {
public class ValuesFragment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_values, container, false);
//    }

    private SessionHandler session;
    private DrawerLayout drawer;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private int dayB;
    private int monthB;
    private int yearB;
    private int userID;

    private ListView listView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Glucose values from DB");

        setContentView(R.layout.fragment_values);
        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();

        userID = Integer.parseInt(user.getId());
//        Toast.makeText(getApplicationContext(), user.getId(), Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.design_navigation_view);

        TextView txtProfileName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name_in_header);
        txtProfileName.setText(user.getFullName());
        txtProfileName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email_in_header);
        txtProfileName.setText(user.getEmail());
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(R.id.nav_values);

        TextView txtV = (TextView) findViewById(R.id.select_date_text);
        txtV.setTextColor(Color.rgb(255, 255, 255));

        listView = (ListView) findViewById(R.id.listView);
        selectDate();
        getJSON("http://147.175.98.38/DB/getdata.php");
    }



    public void selectDate() {

        mDisplayDate = (TextView) findViewById(R.id.select_date);

        Calendar cal_ini = Calendar.getInstance();
        int year_ini = cal_ini.get(Calendar.YEAR);
        int month_ini = cal_ini.get(Calendar.MONTH);
        int day_ini = cal_ini.get(Calendar.DAY_OF_MONTH);

        dayB = day_ini;
        monthB = month_ini + 1;
        yearB = year_ini;

        String date_ini = month_ini + 1 + "/" + day_ini + "/" + year_ini;
        mDisplayDate.setText(date_ini);

        mDisplayDate.setBackgroundColor(Color.rgb(255, 255, 255));
        mDisplayDate.setTextColor(Color.rgb(0, 0, 0));

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = yearB;
                int month = monthB - 1;
                int day = dayB;

                DatePickerDialog dialog = new DatePickerDialog(
                        ValuesFragment.this,
//                        android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                dayB = dayOfMonth;
                monthB = month;
                yearB = year;

                String date = month + "/" + dayOfMonth + "/" + year;
                mDisplayDate.setText(date);

                getJSON("http://147.175.98.38/DB/getdata.php");
            }
        };
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(this, DashboardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_chart:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScatterGraphFragment()).commit();
                intent = new Intent(this, ScatterGraphFragment.class);
                startActivity(intent);
                break;
            case R.id.nav_values:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ValuesFragment()).commit();
//                intent = new Intent(this, ValuesFragment.class);
//                startActivity(intent);
                break;
            case R.id.nav_logout:
                session.logoutUser();
                Intent i = new Intent(ValuesFragment.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifi.isWifiEnabled()){
            GetJSON getJSON = new GetJSON();
            getJSON.execute();
        }else{
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<String> li = new ArrayList<>();
        String datum;
        String glukoza;
        String uID;


        ArrayList<String> dates_v = new ArrayList<>();
        ArrayList<String> glukose_v = new ArrayList<>();
        ArrayList<GlucoseListItem> arrayOfGlucose = new ArrayList<GlucoseListItem>();


        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            datum = obj.getString("datum");
            glukoza = obj.getString("glukoza");
            uID = obj.getString("user_id");

            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            try {
                number = format.parse(glukoza);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss"); // here set the pattern as you date in string was containing like date/month/year
                Date d = sdf.parse(datum);


                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int year = cal.get(Calendar.YEAR);
                int usID = Integer.parseInt(uID);

                double glukoza_mmolL = number.doubleValue();
                glukoza_mmolL = glukoza_mmolL/18;

                String gluko_str = String.format("%.2f", glukoza_mmolL);

                if (day == dayB && month == monthB && year == yearB && usID == userID) {

                    datum = datum.substring(10);
                    GlucoseListItem tmp = new GlucoseListItem(datum, gluko_str);
                    arrayOfGlucose.add(tmp);

                    dates_v.add(datum);
                    glukose_v.add(gluko_str);

                    li.add(datum + "\n" + "Glukoza: " + gluko_str + " mmol/L");
                }
                if (day > dayB && month >= monthB && year >= yearB) {
                    break;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        if(li.isEmpty()){
            Toast.makeText(this, "No data available for this Date", Toast.LENGTH_LONG).show();
        }

//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, li);

//        ArrayAdapter<String> arrayAdapter = (new ArrayAdapter<String>(this, R.layout.custom_listview, R.id.list_content, li));
//        listView.setAdapter(arrayAdapter);


        GlucoseListAdapter adapter = new GlucoseListAdapter(this, arrayOfGlucose);
        listView.setAdapter(adapter);

    }


}
