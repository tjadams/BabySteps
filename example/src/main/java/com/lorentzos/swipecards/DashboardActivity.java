package com.lorentzos.swipecards;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class DashboardActivity extends FragmentActivity {

    String username;
    DashboardTabAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = getIntent().getExtras().getString("username");
        setContentView(R.layout.activity_dashboard);
        if(savedInstanceState != null) {
            username = savedInstanceState.getString("username");
        }
        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        if (vpPager instanceof ColorViewPager) {
            ((ColorViewPager) vpPager).setActionBar(getActionBar());
            ((ColorViewPager) vpPager).setWindow(getWindow());
            getActionBar().setElevation(0);
        }
        adapterViewPager = new DashboardTabAdapter(getSupportFragmentManager());
        adapterViewPager.setUsername(username);
        vpPager.setAdapter(adapterViewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
