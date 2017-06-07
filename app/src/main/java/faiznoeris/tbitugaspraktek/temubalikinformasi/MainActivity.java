package faiznoeris.tbitugaspraktek.temubalikinformasi;

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

import fragment.FragmentHitungKata;
import fragment.FragmentStemmingStoplist_1;
import fragment.FragmentStemmingStoplist_2;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DBHelper db;

    ProgressBar loadingBarHorizontal;
    TextView tvInfo;
    MenuItem menuGetData, menuSearchData;
    View mainView;

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

        mainView = findViewById(R.id.mainFrame);
        loadingBarHorizontal = (ProgressBar) findViewById(R.id.progress);
        tvInfo = (TextView) findViewById(R.id.progressinfo);



        //Toast.makeText(this, String.valueOf(obj.isTableExists("tbkatadasar")) + " " + String.valueOf(obj.isTBKataDasarEmpty()), Toast.LENGTH_SHORT).show();
        //!(obj.isTableExists(obj.KATADASAR_TABLE_NAME)) ||
        //check apakah tabel masih kosong, jika iya isi data_tocheck
        db = new DBHelper(this);
        //showProgress(true);

        navigationView.setCheckedItem(R.id.nav_drawer1);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, new FragmentHitungKata());
        ft.commit();

        /*if(db.isTBKataDasarEmpty()) {
            db = new DBHelper(this);
            //InsertData insertData = new InsertData(this, loadingBarHorizontal, mainView, tvInfo, null, null);
            //insertData.execute();
        }else {
            CheckData checkData = new CheckData(this, null, loadingBarHorizontal, mainView, tvInfo, null, null);
            checkData.execute();
        }*/

        // TODO check data_tocheck utama, jika data_tocheck utama kontennya berubah maka update data_tocheck
        // TODO berikan bobot saat pencarian (mungkin hitung keyword yang muncul dalam konten, paling banyak tampil paling atas

    }

    public void setActionBarTitle(String title) {
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menuGetData = menu.findItem(R.id.action_getdata);
        menuSearchData = menu.findItem(R.id.action_searchdata);
        menuGetData.setVisible(false);
        menuSearchData.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
}