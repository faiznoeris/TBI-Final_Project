package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Vellfire on 18/04/2017.
 */

public class JSONParsing extends AsyncTask<Void, String, List<Map<String, String>>> {

    private final String TAG_LOG_D = "Parsing";
    private String WEBSITE_ADDRESS = "http://www.hirupmotekar.com/?json=1";
    String line = "", finalJson, content, id;

    int totalData;
    int totalPages;

    int counter = 0;
    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;
    Context context;
    ProgressBar loadingBarHorizontal;
    View mainView;
    TextView tvInfo;

    URL link;
    DBHelper db;
    HttpURLConnection conn;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuffer stringBuffer;
    JSONObject parentObject, e;
    JSONArray parentArray;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();

    Handler progressHandler = new Handler();

    long startTime, endTime;

    public JSONParsing(FragmentStemmingStoplist_2 fragmentStoplistStemming_2, Context context, ProgressBar loadingBarHorizontal, View mainView, TextView tvInfo, int totalPages, int totalData) {
        this.context = context;
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;
        this.loadingBarHorizontal = loadingBarHorizontal;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.totalPages = totalPages;
        this.totalData = totalData;
    }

    @Override
    protected void onPreExecute() {


        mainView.setVisibility(View.GONE);
        loadingBarHorizontal.setMax(totalData);
        loadingBarHorizontal.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        try {
            db = new DBHelper(context);
            link = new URL(WEBSITE_ADDRESS);
            conn = (HttpURLConnection) link.openConnection();
            try {
                conn.connect();
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            inputStream = conn.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuffer = new StringBuffer();

            //masukkan json kedalam stringbuffer
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();

            //finalJson = stringBuffer.toString();
            parentObject = new JSONObject(stringBuffer.toString());

            totalPages = Integer.parseInt(parentObject.getString("pages"));
            totalData = Integer.parseInt(parentObject.getString("count_total"));
            for (int i = 1; i <= totalPages; i++) {
                if (i > 1) {
                    WEBSITE_ADDRESS = "http://www.hirupmotekar.com/page/" + i + "/?json=1";
                    //membuka koneksi
                    link = new URL(WEBSITE_ADDRESS);
                    conn = (HttpURLConnection) link.openConnection();
                    try {
                        conn.connect();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }

                    inputStream = conn.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    stringBuffer = new StringBuffer();

                    //masukkan json kedalam stringbuffer
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    bufferedReader.close();

                    finalJson = stringBuffer.toString();

                    //rubah string json kedalam bentuk jsonobject
                    parentObject = new JSONObject(finalJson);

                }

                parentArray = parentObject.getJSONArray("posts");

                //pengambilan data_tocheck
                for (int j = 0; j < parentArray.length(); j++) {
                    map = new HashMap<>(2);
                    e = parentArray.getJSONObject(j);
                    id = e.getString("id");
                    content = Html.fromHtml(e.getString("content")).toString(); //remove html entities
                    //totalDataNew++;
                    map.put("id", id);
                    map.put("content", content);
                    map.put("title", Html.fromHtml(e.getString("title")).toString());
                    data.add(map);
                    if (!(db.isDataUtamaExist(id))) {
                        if (db.addDataUtama(id, content, Html.fromHtml(e.getString("title")).toString())) {
                            //counter++;
                            Log.d(TAG_LOG_D, "Data JSON Inserted. (" + id + ", " + content + ")");
                        }
                    }
                    Log.d(TAG_LOG_D, "Data JSON added - " + id);
                    counter++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBarHorizontal.setProgress(counter);
                            tvInfo.setText("Current Progress = Adding data from web - " + counter + " / " + totalData);
                        }
                    });
                }

            }

            //for specific link, like =
            /*JSONObject object = (JSONObject) new JSONTokener(finalJson).nextValue();

            content = object.getString("post");

            JSONObject object2 = (JSONObject) new JSONTokener(content).nextValue();

            content = object2.getString("content");
            String id = object2.getString("id");*/

            return data;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //conn.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> s) {
        stopLoading();
        //showProgress(true);
        if (s != null) {
            Log.d(TAG_LOG_D, "Done - Total data_tocheck = " + totalData);

            SimpleAdapter adapter = new SimpleAdapter(context, s,
                    R.layout.row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            fragmentStoplistStemming_2.setData(s, "");
            fragmentStoplistStemming_2.setAdapter(adapter, "utama");

            endTime = System.currentTimeMillis();
            Toast.makeText(context, "Time spent adding data utama - " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();
            Log.d(TAG_LOG_D, "Time spent adding data utama  - " + (endTime - startTime) / 1000 + " second");
        } else {
            //showProgress(false);
            stopLoading();
            Toast.makeText(context, "Terjadi error saat pengambilan data_tocheck!", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCancelled() {
        stopLoading();
        //showProgress(true);
    }

    private void stopLoading() {
        loadingBarHorizontal.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }

    /*@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            mainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loadingBarHorizontal.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingBarHorizontal.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingBarHorizontal.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadingBarHorizontal.setVisibility(show ? View.VISIBLE : View.GONE);
            mainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/
}
