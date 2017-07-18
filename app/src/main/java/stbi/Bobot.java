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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import fragment.FragmentSearchData;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 18/06/2017.
 */

public class Bobot extends AsyncTask<Void, String, Void> {
    private final String TAG_LOG_D = "Bobot";

    String[] term_split, term_split_temp;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, id, loadingMax;
    long startTime, endTime;

    double hasilbobot, n, Nterm;

    StringBuilder builder = new StringBuilder();
    String str_id, str_term, str_count, str_removedword, idkonten;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentShowData fragmentShowData;
    FragmentSearchData fragmentSearchData;
    Context context;

    Cursor rs;

    Map<String, String> map;
    ArrayList<Integer> index_count_list = new ArrayList<Integer>();
    ArrayList<String> id_list = new ArrayList<String>();
    ArrayList<String> term_list = new ArrayList<String>();
    List<Map<String, String>> data = new ArrayList<>();


    private Handler progressHandler = new Handler();

    public Bobot(Context context, FragmentShowData fragmentShowData, ProgressBar loadingBar, View mainView, TextView tvInfo, FragmentSearchData fragmentSearchData) {
        this.context = context;
        this.loadingBar = loadingBar;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.fragmentShowData = fragmentShowData;
        this.fragmentSearchData = fragmentSearchData;
    }


    @Override
    protected void onPreExecute() {
        counterLoadingBar = 0;
        countTotalWordToIndex = 0;

        db = new DBHelper(context);
        n = db.getTotalDataIndex();
        loadingMax = db.getSizeDataIndex();

        loadingBar.setMax(loadingMax);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();

    }


    @Override
    protected Void doInBackground(Void... params) {
        //db = new DBHelper(context);
        //n = db.getTotalDataIndex();

        db = new DBHelper(context);
        rs = db.getAllDataIndex();

        if (!(db.isTBDataIndexEmpty())) {
            rs = db.getAllDataIndex();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    //Log.d("AS", "Judul" + judul);
                    map.put("count_index", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_COUNTINDEX)));
                    map.put("id", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN)));
                    map.put("term", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_TERM)));
                    //map.put("removedword", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_REMOVEDWORD)));
                    data.add(map);
                    rs.moveToNext();
                }
            }
            rs.close();
        }

        for (Map<String, String> tempmap : data) {
            for (Map.Entry<String, String> entry : tempmap.entrySet()) {
                if (entry.getKey().equals("count_index") && !(entry.getValue() == null)) {
                    index_count_list.add(Integer.parseInt(entry.getValue()));
                }else if (entry.getKey().equals("idkonten") && !(entry.getValue() == null)) {
                    id_list.add(entry.getValue());
                }else if (entry.getKey().equals("term") && !(entry.getValue() == null)) {
                    term_list.add(entry.getValue());
                }

            }
        }

        for (int i = 0; i < data.size(); i++) {
            index_count = index_count_list.get(i);
            Nterm = db.getWordFreq(term_list.get(i));

            Log.d(TAG_LOG_D, "BEFORE COUNT: N: " + n + " | NTERM: " + Nterm + " | INDEX_COUNT: " + index_count + " | LOG: " + String.valueOf(Math.log10((n / Nterm))) + " FOR TERM: " + term_list.get(i));

            hasilbobot = index_count * Math.log10((n / Nterm));

            if (db.updateTbIndex_Bobot(hasilbobot, id)) {
                Log.d(TAG_LOG_D, "TERM: " + term_list.get(i) + " | BOBOT: " + hasilbobot + " WORD KE: " + counterLoadingBar + " / " + loadingMax);
            }

            counterLoadingBar++;
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadingBar.setProgress(counterLoadingBar);
                    //Toast.makeText(context, String.valueOf(rs.getCount()), Toast.LENGTH_SHORT).show();
                    tvInfo.setText("Current Progress = Bobot | " + counterLoadingBar + " / " + loadingMax + " term");
                }
            });
        }

//        if (rs.moveToFirst()) {
//            while (!rs.isAfterLast()) {
//                //db.openDataBase();
//                str_term = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_TERM));
//
//                id = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_ID));
//                idkonten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
//                index_count = rs.getInt(rs.getColumnIndex(DBHelper.DATA_COLUMN_COUNTINDEX));
//                Nterm = db.getWordFreq(str_term);
//
//                /*if(idkonten == "116"){
//                    continue;
//                }*/
//
//                Log.d(TAG_LOG_D, "BEFORE COUNT: N: " + n + " | NTERM: " + Nterm + " | INDEX_COUNT: " + index_count + " | LOG: " + String.valueOf(Math.log10((n / Nterm))) + " FOR TERM: " + str_term);
//
//                hasilbobot = index_count * Math.log10((n / Nterm));
//
//                if (db.updateTbIndex_Bobot(hasilbobot, id)) {
//                    Log.d(TAG_LOG_D, "TERM: " + str_term + " | BOBOT: " + hasilbobot + " WORD KE: " + counterLoadingBar + " / " + loadingMax);
//                }
//
//                counterLoadingBar++;
//                progressHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingBar.setProgress(counterLoadingBar);
//                        //Toast.makeText(context, String.valueOf(rs.getCount()), Toast.LENGTH_SHORT).show();
//                        tvInfo.setText("Current Progress = Bobot | " + counterLoadingBar + " / " + loadingMax + " term");
//                    }
//                });
//
//                rs.moveToNext();
//            }
//        }
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);

        if (fragmentSearchData != null) {
            loadingBar.setProgress(0);
            if (!db.isTBDataIndexEmpty()) {
                Vektor vektor = new Vektor(context, null, loadingBar, mainView, tvInfo);
                vektor.execute();
            }
        } else {
            endTime = System.currentTimeMillis();
            Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
            Log.d(TAG_LOG_D, "Done, " + counterLoadingBar + " data dihitung bobotnya.");
            Toast.makeText(context, "Task done in " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCancelled() {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }


}
