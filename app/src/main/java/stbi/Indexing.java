package stbi;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentShowData;

/**
 * Created by Faiz Noeris on 17 Jun 2017.
 */

public class Indexing extends AsyncTask<Void, String, Void> {
    // get all data from tbstem, get konten and idkonten, split konten by space, loop that shit, and update / insert into tbindex

    private final String TAG_LOG_D = "Indexing";

    String[] term_split, term_split_temp;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, sizestem;
    long startTime, endTime;

    StringBuilder builder = new StringBuilder();
    String str_id, str_content, str_title, str_removedword;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentShowData fragmentShowData;
    Context context;

    //Map<String, String> map;
    ArrayList<String> term_temp = new ArrayList<String>();

    private Handler progressHandler = new Handler();

    public Indexing(Context context, FragmentShowData fragmentShowData, ProgressBar loadingBar, View mainView, TextView tvInfo) {
        this.context = context;
        this.loadingBar = loadingBar;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.fragmentShowData = fragmentShowData;
    }

    @Override
    protected void onPreExecute() {
        countTotalWordToIndex = 0;
        counterLoadingBar = 0;

        db = new DBHelper(context);
        db.clearTbIndexing();
        Cursor rs = db.getAllDataStemming();

        if (rs.moveToFirst()) {
            while (rs.isAfterLast() == false) {
                str_content = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));

                term_split = str_content.split(" ");

                for (int i = 0; i < term_split.length; i++) {
                    if (term_split[i] != "") {
                        term_split[i].replace("\n", "");
                        term_split[i].replace("'", "");
                        term_split[i].replace("-", "");
                        term_split[i].replace("=", "");
                        term_split[i].replace(".", "");
                        term_split[i].replace(",", "");
                        term_split[i].replace(":", "");
                        term_split[i].replace(";", "");
                        term_split[i].replace("!", "");
                        term_split[i].replace("\\?", "");
                        term_split[i].replace("\\)", "");
                        term_split[i].replace("\\(", "");
                        term_split[i].replace("\\\\", "");
                        term_split[i].replace("\\/", "");
                        term_split[i].replace("[^a-zA-Z ]", "");

                    }
                    term_temp.add(term_split[i].toLowerCase());
                }
                for (int j = 0; j < term_temp.size(); j++) {
                    countTotalWordToIndex++;
                }
                rs.moveToNext();
            }
        }

        sizestem = db.getSizeDataStemming();

        loadingBar.setMax(sizestem);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground(Void... params) {
        db = new DBHelper(context);
        db.clearTbIndexing();
        Cursor rs2 = db.getAllDataStemming();
        term_temp.clear();

        if (rs2.moveToFirst()) {
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadingBar.setProgress(counterLoadingBar);
                    tvInfo.setText("Current Progress = Indexing | " + counterLoadingBar + " / " + sizestem + " dokumen");
                }
            });
            while (rs2.isAfterLast() == false) {
                str_content = rs2.getString(rs2.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                str_id = rs2.getString(rs2.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));

                term_split = str_content.split(" ");

                for (int i = 0; i < term_split.length; i++) {
                    if (term_split[i] != "") {
                        term_split[i].replace("\n", "");
                        term_split[i].replace("'", "");
                        term_split[i].replace("-", "");
                        term_split[i].replace("=", "");
                        term_split[i].replace(".", "");
                        term_split[i].replace(",", "");
                        term_split[i].replace(":", "");
                        term_split[i].replace(";", "");
                        term_split[i].replace("!", "");
                        term_split[i].replace("\\?", "");
                        term_split[i].replace("\\)", "");
                        term_split[i].replace("\\(", "");
                        term_split[i].replace("\\\\", "");
                        term_split[i].replace("\\/", "");
                        term_split[i].replace("[^a-zA-Z ]", "");
                    }

                    //if (!term_temp.contains(term_split[i].toLowerCase())) {
                        term_temp.add(term_split[i].toLowerCase());
                    //}

                }
                for (int j = 0; j < term_temp.size(); j++) {
                    index_count = db.getCountIndex(term_temp.get(j), str_id);
                    Log.d(TAG_LOG_D, term_temp.get(j) + " | INDEX COUNT: " + index_count + " | ID: " + str_id);
                    if (index_count > 0) {
                        index_count++;
                        if (db.updateTbIndex_Indexing(index_count, term_temp.get(j), str_id)) {
                        }
                    } else {
                        if (db.addTbIndex_Indexing(term_temp.get(j), str_id)) {
                        }
                    }



                }

                counterLoadingBar++;
                progressHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingBar.setProgress(counterLoadingBar);
                        tvInfo.setText("Current Progress = Indexing | " + counterLoadingBar + " / " + sizestem + " dokumen");
                    }
                });
                rs2.moveToNext();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
        Log.d(TAG_LOG_D, "Done, " + counterLoadingBar + " data indexed.");
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