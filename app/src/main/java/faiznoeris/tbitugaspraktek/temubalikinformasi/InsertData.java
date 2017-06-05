package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Vellfire on 04/05/2017.
 */

public class InsertData extends AsyncTask<String, Void, List<Map<String, String>>> {

    private final String TAG_LOG_D = "InsertData";

    String str;
    int counter = 0;

    BufferedReader bufferedReader;
    AssetManager am;
    InputStream is;
    DBHelper db;

    Context context;

    ProgressBar loadingView;
    View tampilanView, loading;
    TextView info;

    long startTime, endTime;

    private Handler progressHandler = new Handler();

    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;

    List<Map<String, String>> data;

    String id,content,title,removedword;

    public InsertData(Context context, View loading,ProgressBar loadingView, View tampilanView, TextView info, List<Map<String, String>> data, FragmentStemmingStoplist_2 fragmentStoplistStemming_2) {
        this.context = context;
        this.loadingView = loadingView;
        this.tampilanView = tampilanView;
        this.info = info;
        this.data = data;
        this.loading = loading;
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;
    }

    @Override
    protected void onPreExecute() {
        if(loadingView != null) {
            tampilanView.setVisibility(View.GONE);
            loadingView.setMax(28524);
            loadingView.setVisibility(View.VISIBLE);
            info.setVisibility(View.VISIBLE);
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> maps) {
        if(loadingView != null) {
            stopLoading();

        }else if(loading != null){
            showProgress(true);
        }else if(fragmentStoplistStemming_2 != null){
            SimpleAdapter adapter = new SimpleAdapter(context, maps,
                    R.layout.row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            fragmentStoplistStemming_2.setData(data, "");
            fragmentStoplistStemming_2.setAdapter(adapter, "stoplist");
        }
        endTime= System.currentTimeMillis();
//        System.out.println(endTime-startTime); //Milli Secs
//        System.out.println((endTime-startTime)/1000); //Secs
        Toast.makeText(context, "Time spent adding data - " + (endTime-startTime)/1000 + " seconds", Toast.LENGTH_SHORT).show();
        Log.d(TAG_LOG_D, "Time spent adding data - " + (endTime-startTime)/1000 + " second");
    }

    @Override
    protected List<Map<String, String>> doInBackground(String... params) {


        if(params[0] == "insert_katadasar") {
            db = new DBHelper(context);
            am = context.getAssets();
            try {
                is = am.open("kata_dasar_indo.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            bufferedReader = new BufferedReader(new InputStreamReader(is));


            try {
                while ((str = bufferedReader.readLine()) != null) {
                    counter++;
                    if (!(db.isKataDasarExist(str))) {
                        if (db.addKataDasar(str)) {
                            //counter++;
                            progressHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadingView.setProgress(counter);
                                    info.setText("Current Progress = Adding data - " + counter + " / 28524");
                                }
                            });
                            Log.d(TAG_LOG_D, "Data Inserted. (" + counter + ")");// - progressbar(" + loadingView.getProgress() + ")");
                        }
                        //Log.d(TAG_LOG_D, "Data Already Inserted.");
                    } else {
                        progressHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setProgress(counter);
                                info.setText("Current Progress = Skipping existing data - " + counter);
                            }
                        });
                        Log.d(TAG_LOG_D, "Skipping existing Data");
                    }
                }

                Log.d(TAG_LOG_D, "Done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(params[0] == "insert_stoplist"){
            db = new DBHelper(context);
            Map<String, String> map = new HashMap<>(2);
            Map.Entry<String,String> entry;
            Iterator<Map.Entry<String,String>> it;

            /*for (Map<String, String> tempmap : data) {
                for (Map.Entry<String, String> tempentry : tempmap.entrySet()) {
                    map.put(tempentry.getKey(), tempentry.getValue());
                }
                data.add(map);
                if (!(db.isDataStoplistExist(id))) {
                    if (db.addDataStoplist(id, content, title, removedword)) {
                        //counter++;
                        Log.d(TAG_LOG_D, "Data Stoplist Inserted. (" + id + ", " + title + ")");
                    }
                }
                Log.d(TAG_LOG_D, "Data Stoplist added - " + map.get("id"));
            }*/
            Log.d(TAG_LOG_D, "Data = " + data.toString());
            /*it = map.entrySet().iterator();
            while (it.hasNext()) {
                entry = it.next();
                Log.d(TAG_LOG_D, "Visiting " + entry.getKey());
                if (entry.getKey().equals("id") && !(entry.getValue() == null)) {
                    id = entry.getValue();
                    map.put("id", id);
                } else if(entry.getKey().equals("content") && !(entry.getValue() == null)){
                    content = entry.getValue();
                    map.put("content", content);
                } else if(entry.getKey().equals("title") && !(entry.getValue() == null)){
                    title = entry.getValue();
                    map.put("title", title);
                } else if(entry.getKey().equals("removedword") && !(entry.getValue() == null)){
                    removedword = entry.getValue();
                    map.put("removedword", removedword);
                }

            }*/



        }
        return data;
    }


        /*try {
            while ((str = bufferedReader.readLine()) != null) {

                if (db.addKataDasar(str)) {
                    counter++;
                    Log.d(TAG_LOG_D, "Data Inserted. (" + counter + ")");
                }
            }
            Log.d(TAG_LOG_D, "Done");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //return null;





    private void stopLoading() {

        loadingView.setVisibility(View.GONE);
        info.setVisibility(View.GONE);
        tampilanView.setVisibility(View.VISIBLE);


    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
            tampilanView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loading.setVisibility(show ? View.VISIBLE : View.GONE);
            loading.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loading.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loading.setVisibility(show ? View.VISIBLE : View.GONE);
            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
