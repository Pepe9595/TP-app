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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class ScatterGraphFragment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SessionHandler session;
    private DrawerLayout drawer;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    //    private SimpleDateFormat sdf = new SimpleDateFormat("dd hh:mm");
    private Date midnight;
    private Date nextday;
    private ScatterChart chart;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private int userID;

    private List<Double> glukose_v;
    private List<Long> dates_v;

    private int dayB;
    private int monthB;
    private int yearB;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_scatter_graph);
        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();

        userID = Integer.parseInt(user.getId());

        Long time = new Date().getTime();
        midnight = new Date(time - time % (24 * 60 * 60 * 1000));
        nextday = new Date(midnight.getTime() + 24 * 60 * 60 * 1000);


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

        navigationView.setCheckedItem(R.id.nav_chart);

        TextView txtV = (TextView) findViewById(R.id.select_date_text);
        txtV.setTextColor(Color.rgb(255, 255, 255));

        selectDate();
//        displayGraph();


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
//        mDisplayDate.setText(Html.fromHtml("<u>"+ date_ini +"</u>"));
        mDisplayDate.setText(date_ini);

        mDisplayDate.setBackgroundColor(Color.rgb(255, 255, 255));
        mDisplayDate.setTextColor(Color.rgb(0, 0, 0));

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Calendar cal = Calendar.getInstance();
//                int year = cal.get(Calendar.YEAR);
//                int month = cal.get(Calendar.MONTH);
//                int day = cal.get(Calendar.DAY_OF_MONTH);
                int year = yearB;
                int month = monthB - 1;
                int day = dayB;

                DatePickerDialog dialog = new DatePickerDialog(
                        ScatterGraphFragment.this,
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


    public void displayGraph() {


        chart = (ScatterChart) findViewById(R.id.scatter_chart);

        chart.getDescription().setEnabled(false);

        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setMaxHighlightDistance(50f);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        chart.setMaxVisibleValueCount(200);
        chart.setPinchZoom(true);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXOffset(5f);
        chart.getLegend().setEnabled(false);


        YAxis yl = chart.getAxisLeft();
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.rgb(240, 240, 240));

        XAxis xl = chart.getXAxis();
        xl.setDrawGridLines(true);
        xl.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xl.setTextSize(10f);
        xl.setTextColor(Color.WHITE);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(true);
        xl.setTextColor(Color.rgb(255, 192, 56));
        xl.setCenterAxisLabels(true);
        xl.setGranularity(1f); // one hour
        xl.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });


        long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
        float to = now + 10;
        ArrayList<Entry> values1 = new ArrayList<>();

//        int minutes = 4;
//        long millis = minutes * 60 * 1000;

        for (float i = now; i < to; i++) {
            float val = (float) (Math.random() * 100) + 3;
            values1.add(new Entry(i, val));
        }


        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet(values1, "DS 1");
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        set1.setScatterShapeSize(12f);


        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);

        chart.setData(data);
        chart.invalidate();

    }

    public static Date toNearestWholeHour(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.MINUTE) >= 30)
            c.add(Calendar.HOUR, 1);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public static Date toNearestWholeMinute(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.SECOND) >= 30)
            c.add(Calendar.MINUTE, 1);

        c.set(Calendar.SECOND, 0);

        return c.getTime();
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
//                intent = new Intent(this, ScatterGraphFragment.class);
//                startActivity(intent);
                break;
            case R.id.nav_values:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ValuesFragment()).commit();
                intent = new Intent(this, ValuesFragment.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                session.logoutUser();
                Intent i = new Intent(ScatterGraphFragment.this, LoginActivity.class);
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

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        JSONArray jsonArray = new JSONArray(json);
        String datum;
        String glukoza;
        String uID;
        dates_v = new ArrayList<Long>();
        glukose_v = new ArrayList<>();
        ArrayList<String> id_v = new ArrayList<>();
        ArrayList<Date> dat = new ArrayList<>();


        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            datum = obj.getString("datum");
            glukoza = obj.getString("glukoza");
            uID = obj.getString("user_id");

            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            try {
                number = format.parse(glukoza);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            double glukoza_mmolL = number.doubleValue();
            glukoza_mmolL = glukoza_mmolL / 18;

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss"); // here set the pattern as you date in string was containing like date/month/year
                Date d = sdf.parse(datum);

                id_v.add(uID);
                dat.add(d);
                dates_v.add(d.getTime());
                glukose_v.add(Double.valueOf(twoDForm.format(glukoza_mmolL)));
//                glukose_v.add(glukoza_mmolL);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }


        chart = (ScatterChart) findViewById(R.id.scatter_chart);
        chart.fitScreen();
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
//        chart.setMaxHighlightDistance(50f);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
//        chart.setMaxVisibleValueCount(200);
        chart.setPinchZoom(true);
        chart.getLegend().setEnabled(false);

        YAxis yl = chart.getAxisLeft();
//        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.rgb(240, 240, 240));

        XAxis xl = chart.getXAxis();
        xl.setDrawGridLines(true);
        xl.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xl.setTextSize(10f);
        xl.setTextColor(Color.WHITE);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(true);
        xl.setTextColor(Color.rgb(0, 0, 0));
        xl.setCenterAxisLabels(true);
        xl.setGranularity(1f); // one hour
        xl.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                return mFormat.format(new Date((long) value));
            }
        });


        ArrayList<Entry> values1 = new ArrayList<>();
        double v = 0;
        float val = 0;

        for (int i = 0; i < dates_v.size(); i++) {
            v = glukose_v.get(i);
            val = (float) v;

            Date date = new Date(dates_v.get(i));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int year = cal.get(Calendar.YEAR);
            long startTime = cal.getTimeInMillis();

            int usID = Integer.parseInt(id_v.get(i));

            if (day == dayB && month == monthB && year == yearB && usID == userID) {
                values1.add(new Entry(startTime, val));
            }
            if (day > dayB && month >= monthB && year >= yearB) {
                break;
            }

        }



        if(values1.isEmpty()){
            Toast.makeText(this, "No data available for this Date", Toast.LENGTH_LONG).show();
        }

        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet(values1, "glucose");
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set1.setScatterShapeHoleColor(ColorTemplate.COLORFUL_COLORS[1]);
        set1.setScatterShapeHoleRadius(3f);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        set1.setScatterShapeSize(5f);
        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets
        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);
        chart.setData(data);
        // draw points over time
        chart.animateX(1500);
        chart.invalidate();





    }


//    public void graphView(){
//        // generate Dates
//        Calendar calendar = Calendar.getInstance();
//        Date d1 = calendar.getTime();
//        calendar.add(Calendar.DATE, 1);
//        Date d2 = calendar.getTime();
////        calendar.add(Calendar.DATE, 0.5);
////        Date d3 = calendar.getTime();
//
//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(new DataPoint[]{
//                new DataPoint(d1, 1),
//                new DataPoint(d2, 5),
////                new DataPoint(d3, 3)
//        });
//
//        // set date label formatter
////        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(ScatterGraphFragment.this));
//        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
//        {
//            @Override
//            public String formatLabel(double value, boolean isValueX) {
//                if(isValueX){
////                    Date now = toNearestWholeHour(new Date((long) value));
////                    return sdf.format(now);
//                    return sdf.format(new Date((long) value));
//                }
//                else{
//                    return super.formatLabel(value, isValueX);
//                }
//            }
//        });
//
//
//        graph.getGridLabelRenderer().setNumHorizontalLabels(6); // only 4 because of the space
//
//        // set manual x bounds to have nice steps
//        graph.getViewport().setMinX(d1.getTime());
//        graph.getViewport().setMaxX(d2.getTime());
//        graph.getViewport().setXAxisBoundsManual(true);
//
//        // as we use dates as labels, the human rounding to nice readable numbers
//        // is not necessary
//        graph.getGridLabelRenderer().setHumanRounding(false);
//        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
//        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
//        graph.addSeries(series);
//    }

}
