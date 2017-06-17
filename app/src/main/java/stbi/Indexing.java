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
    int countTerm, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count;
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
        db = new DBHelper(context);
        db.truncateTbIndexing();
        Cursor rs = db.getAllDataUtama();
        if (rs.moveToFirst()) {
            while (rs.isAfterLast() == false) {
                str_id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                str_content = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                str_title = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));

                term_split = str_content.split(" ");
                term_split.toString().replaceAll("^\\s|\n\\s|\\s$", "");
                term_split.toString().replaceAll("'", "");
                term_split.toString().replaceAll("-", "");
                term_split.toString().replaceAll("=", "");
                term_split.toString().replaceAll(".", "");
                term_split.toString().replaceAll(",", "");
                term_split.toString().replaceAll(":", "");
                term_split.toString().replaceAll(";", "");
                term_split.toString().replaceAll("!", "");
                term_split.toString().replaceAll("\\?", "");
                term_split.toString().replaceAll("\\)", "");
                term_split.toString().replaceAll("\\(", "");
                term_split.toString().replaceAll("\\\\", "");
                term_split.toString().replaceAll("\\/", "");

                for (int i = 0; i < term_split.length; i++) {
                    if (!term_temp.contains(term_split[i])) {
                        countTotalWordToIndex++;
                    }
                }
                rs.moveToNext();
            }
        }

        loadingBar.setMax(countTotalWordToIndex);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground(Void... params) {
        db = new DBHelper(context);
        db.truncateTbIndexing();
        Cursor rs = db.getAllDataUtama();
        if (rs.moveToFirst()) {
            while (rs.isAfterLast() == false) {
                str_id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                str_content = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                str_title = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));

                term_split = str_content.split(" ");
                term_split.toString().replaceAll("^\\s|\n\\s|\\s$", "");
                term_split.toString().replaceAll("'", "");
                term_split.toString().replaceAll("-", "");
                term_split.toString().replaceAll("=", "");
                term_split.toString().replaceAll(".", "");
                term_split.toString().replaceAll(",", "");
                term_split.toString().replaceAll(":", "");
                term_split.toString().replaceAll(";", "");
                term_split.toString().replaceAll("!", "");
                term_split.toString().replaceAll("\\?", "");
                term_split.toString().replaceAll("\\)", "");
                term_split.toString().replaceAll("\\(", "");
                term_split.toString().replaceAll("\\\\", "");
                term_split.toString().replaceAll("\\/", "");

                for (int i = 0; i < term_split.length; i++) {
                    if (!term_temp.contains(term_split[i])) {
                        term_temp.add(term_split[i].toLowerCase());
                    }
                }

                for (int i = 0; i < term_temp.size(); i++) {
                    index_count = db.getCountIndex(term_temp.get(i), str_id);
                    Log.d(TAG_LOG_D, term_temp.get(i) + " | " + index_count);
                    if (index_count > 0) {
                        index_count++;
                        if (db.updateTbIndex_Indexing(str_id, index_count, term_temp.get(i))) {

                        }
                    } else {
                        if (db.addTbIndex_Indexing(str_id, term_temp.get(i))) {

                        }
                    }
                    counterLoadingBar++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.setProgress(counterLoadingBar);
                            tvInfo.setText("Current Progress = Indexing | " + counterLoadingBar + " / " + countTotalWordToIndex);
                        }
                    });
                }

                rs.moveToNext();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
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
