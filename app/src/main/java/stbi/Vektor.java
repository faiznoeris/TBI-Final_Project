package stbi;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 19/06/2017.
 */

public class Vektor extends AsyncTask<Void, String, Void> {
    private final String TAG_LOG_D = "Vektor";

    String[] term_split, term_split_temp;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, Nterm, id, n, sizestem;
    long startTime, endTime;

    double bobot, panjangvektor;

    StringBuilder builder = new StringBuilder();
    String str_id, str_term, str_count, str_removedword;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentShowData fragmentShowData;
    Context context;

    //Map<String, String> map;
    ArrayList<String> term_temp = new ArrayList<String>();

    private Handler progressHandler = new Handler();

    public Vektor(Context context, FragmentShowData fragmentShowData, ProgressBar loadingBar, View mainView, TextView tvInfo) {
        this.context = context;
        this.loadingBar = loadingBar;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.fragmentShowData = fragmentShowData;
    }

    @Override
    protected void onPreExecute() {
        counterLoadingBar = 0;
        countTotalWordToIndex = 0;

        db = new DBHelper(context);
        db.clearTbVektor();
        n = db.getTotalDataIndex();
        sizestem = db.getSizeDataStemming();


        loadingBar.setMax(sizestem);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor rs = db.getAllDataIndex();
        int id_before;
        boolean addtodb = true;
        //int counter = 0;



        if(rs.moveToFirst()){
            counterLoadingBar++;
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadingBar.setProgress(counterLoadingBar);
                    tvInfo.setText("Current Progress = Vektor | " + counterLoadingBar + " / " + sizestem + " dokumen");
                }
            });
            while(!rs.isAfterLast()){
                id_before = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));

                Cursor rs2 = db.getBobotFromIndex(id_before);
                //counter = 0;

                if(rs2.moveToFirst()) {
                    while (!rs2.isAfterLast()) {
                        bobot = rs2.getInt(rs2.getColumnIndex(DBHelper.DATA_COLUMN_BOBOT));

                        panjangvektor += bobot * bobot;
                        //counter++;
                        //Log.d(TAG_LOG_D, "PANJANGVEKTOR: " + panjangvektor + " | counter: " + counter);

                        rs2.moveToNext();
                    }
                    rs2.close();
                }

                panjangvektor = Math.sqrt(panjangvektor);

                if(addtodb){
                    if(db.addTbVektor(id_before, panjangvektor)){
                        Log.d(TAG_LOG_D, "ADD TO DB | ID: " + id_before + " PANJANGVEKTOR: " + panjangvektor);
                        addtodb = false;
                    }
                }

                if(id != id_before && id != 0) {
                    counterLoadingBar++;
                    if(db.addTbVektor(id_before, panjangvektor)){
                        Log.d(TAG_LOG_D, "ADD TO DB | ID: " + id_before + " PANJANGVEKTOR: " + panjangvektor);
                    }
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.setProgress(counterLoadingBar);
                            tvInfo.setText("Current Progress = Vektor | " + counterLoadingBar + " / " + sizestem + " dokumen");
                        }
                    });
                }

                id = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));

                rs.moveToNext();
            }
            rs.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
        Log.d(TAG_LOG_D, "Done, " + counterLoadingBar + " data dihitung panjang vektornya.");
        Toast.makeText(context, "Task done in " + (endTime-startTime)/1000 + " seconds", Toast.LENGTH_SHORT).show();
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCancelled() {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }

}
