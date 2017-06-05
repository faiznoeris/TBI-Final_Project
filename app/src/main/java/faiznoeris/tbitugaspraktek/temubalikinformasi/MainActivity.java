package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;

import fragment.FragmentHitungKata;
import fragment.FragmentStemmingStoplist_1;
import fragment.FragmentStemmingStoplist_2;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG_LOG_D = "Main_InsertTBKD";
    String str;

    DBHelper db;
    AssetManager am;
    InputStream is = null;
    BufferedReader bufferedReader;

    ProgressBar loadingView;
    TextView info;
    View tampilanView;

    int counter = 0; //counter penambahan kata dasar ke table

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //-------------------------------------------------------------------------------

        loadingView = (ProgressBar) findViewById(R.id.progress);
        tampilanView = findViewById(R.id.mainFrame);
        info = (TextView) findViewById(R.id.progressinfo);


        db = new DBHelper(this);

        //Toast.makeText(this, String.valueOf(obj.isTableExists("tbkatadasar")) + " " + String.valueOf(obj.isTBKataDasarEmpty()), Toast.LENGTH_SHORT).show();
        //!(obj.isTableExists(obj.KATADASAR_TABLE_NAME)) ||
        //check apakah tabel masih kosong, jika iya isi data

        //showProgress(true);
        navigationView.setCheckedItem(R.id.nav_drawer1);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, new FragmentHitungKata());
        ft.commit();

        if(db.isTBKataDasarEmpty()) {
            InsertData insertData = new InsertData(this, null, loadingView, tampilanView, info, null, null);
            insertData.execute("insert_katadasar");
        }else {
            CheckData checkData = new CheckData();
            checkData.execute();
        }

        // TODO check data utama, jika data utama kontennya berubah maka update data
        // TODO berikan bobot saat pencarian (mungkin hitung keyword yang muncul dalam konten, paling banyak tampil paling atas


    }

    public void setActionBarTitle(String title) {
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_getdata);
        MenuItem item2 = menu.findItem(R.id.action_searchdata);
        item.setVisible(false);
        item2.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_getdata) {
            //
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_drawer1) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentHitungKata());
            ft.commit();
        } else if (id == R.id.nav_drawer2) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentStemmingStoplist_1());
            ft.commit();
        } else if (id == R.id.nav_drawer3) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentStemmingStoplist_2());
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
            tampilanView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loadingView.setMax(28524);
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
            loadingView.setProgress(10000);
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}