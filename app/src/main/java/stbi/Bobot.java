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
 * Created by Vellfire on 18/06/2017.
 */

public class Bobot extends AsyncTask<Void, String, Void> {
    private final String TAG_LOG_D = "Bobot";

    String[] term_split, term_split_temp;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, Nterm, id, n;
    long startTime, endTime;

    double hasilbobot;

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

    public Bobot(Context context, FragmentShowData fragmentShowData, ProgressBar loadingBar, View mainView, TextView tvInfo) {
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
        n = db.getTotalDataIndex();

        loadingBar.setMax(n);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }


    @Override
    protected Void doInBackground(Void... params) {
        //db = new DBHelper(context);
        //n = db.getTotalDataIndex();

        Cursor rs = db.getAllDataIndex();

        if(rs.moveToFirst()){
            while(!rs.isAfterLast()){
                str_term = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_TERM));

                id = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_ID));
                index_count = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_COUNTINDEX));
                Nterm = db.getWordFreq(str_term);

                Log.d(TAG_LOG_D, "BEFORE COUNT: N: " + n + " | NTERM: " + Nterm + " | INDEX_COUNT: " + index_count + " | FOR TERM: " + str_term);

                hasilbobot = index_count * Math.log((n / Nterm));

                if(db.updateTbIndex_Bobot(hasilbobot, id)){
                    Log.d(TAG_LOG_D, "TERM: " + str_term + " | BOBOT: " + hasilbobot);
                }

                counterLoadingBar++;
                progressHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingBar.setProgress(counterLoadingBar);
                        tvInfo.setText("Current Progress = Bobot | " + counterLoadingBar + " / " + n);
                    }
                });

                rs.moveToNext();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
        Log.d(TAG_LOG_D, "Done, " + counterLoadingBar + " data dihitung bobotnya.");
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
