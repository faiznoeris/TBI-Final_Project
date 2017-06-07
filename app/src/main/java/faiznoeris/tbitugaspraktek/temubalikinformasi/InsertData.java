package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Vellfire on 04/05/2017.
 */

public class InsertData extends AsyncTask<Void, Void, List<Map<String, String>>> {

    private final String TAG_LOG_D = "InsertData";

    int counter = 0;
    long startTime, endTime;
    String id, content, title, removedword, str;

    Handler progressHandler = new Handler();

    BufferedReader bufferedReader;
    AssetManager am;
    InputStream is;
    DBHelper db;

    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;
    Context context;

    ProgressBar loadingBarHorizontal;
    View mainView;
    TextView tvInfo;

    List<Map<String, String>> data;

    public InsertData(Context context, ProgressBar loadingBarHorizontal, View mainView, TextView tvInfo, List<Map<String, String>> data, FragmentStemmingStoplist_2 fragmentStoplistStemming_2) {
        this.context = context;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.data = data;
        this.loadingBarHorizontal = loadingBarHorizontal;
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;
    }

    @Override
    protected void onPreExecute() {
        mainView.setVisibility(View.GONE);
        loadingBarHorizontal.setMax(db.KATADASAR_MAX);
        loadingBarHorizontal.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.VISIBLE);
        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        db = new DBHelper(context);
        am = context.getAssets();
        try {
            is = am.open("kata_dasar_indo.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        bufferedReader = new BufferedReader(new InputStreamReader(is));

        try {
            while ((str = bufferedReader.readLine()) != null) {
                counter++;
                if (!(db.isKataDasarExist(str))) {
                    if (db.addKataDasar(str)) {
                        progressHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadingBarHorizontal.setProgress(counter);
                                tvInfo.setText("Current Progress = Adding data_tocheck - " + counter + " / 28524");
                            }
                        });
                        Log.d(TAG_LOG_D, "Data Inserted. (" + counter + ")");// - progressbar(" + loadingBarHorizontal.getProgress() + ")");
                    }
                    //Log.d(TAG_LOG_D, "Data Already Inserted.");
                } else {
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBarHorizontal.setProgress(counter);
                            tvInfo.setText("Current Progress = Skipping existing data_tocheck - " + counter);
                        }
                    });
                    Log.d(TAG_LOG_D, "Skipping existing Data");
                }
            }
            Log.d(TAG_LOG_D, "Done");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> maps) {
        stopLoading();

        SimpleAdapter adapter = new SimpleAdapter(context, maps,
                R.layout.row,
                new String[]{"id", "content", "title"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});

        fragmentStoplistStemming_2.setData(data, "");
        fragmentStoplistStemming_2.setAdapter(adapter, "stoplist");

        endTime = System.currentTimeMillis();
        Toast.makeText(context, "Time spent adding data_tocheck - " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();
        Log.d(TAG_LOG_D, "Time spent adding data_tocheck - " + (endTime - startTime) / 1000 + " second");
    }


    private void stopLoading() {
        loadingBarHorizontal.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }


}
