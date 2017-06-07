package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Faiz Noeris on 5 Jun 2017.
 */

public class CheckData extends AsyncTask<Void, Void, List<Map<String, String>>> {
    private final String TAG_LOG_D = "CheckData";
    private String WEBSITE_ADDRESS = "http://www.hirupmotekar.com/?json=1";
    public static int totalDataNew = 0;
    int counter = 0;
    String line = "";

    String finalJson, id, str, judul, content;
    int size;
    long startTime, endTime;

    Handler progressHandler = new Handler();

    DBHelper db;

    URL link;
    HttpURLConnection conn;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuffer stringBuffer;
    JSONObject parentObject, e;
    JSONArray parentArray;

    FragmentStemmingStoplist_2 fragmentStemmingStoplist_2;
    Context context;

    ProgressBar loadingBarHorizontal;
    View mainView;
    TextView tvInfo;

    List<Map<String, String>> data_tocheck = new ArrayList<Map<String, String>>(), data_new = new ArrayList<Map<String, String>>();
    Map<String, String> map;
    String id_tocheck = "", id_new = "";

    public CheckData(Context context, ProgressBar loadingBarHorizontal, View mainView, TextView tvInfo, FragmentStemmingStoplist_2 fragmentStemmingStoplist_2) {
        this.context = context;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.loadingBarHorizontal = loadingBarHorizontal;
        this.fragmentStemmingStoplist_2 = fragmentStemmingStoplist_2;
    }

    @Override
    protected void onPreExecute() {
        db = new DBHelper(context);
        if (loadingBarHorizontal != null) {
            mainView.setVisibility(View.GONE);
            loadingBarHorizontal.setMax((db.getSizeDataUtama() * 3));
            loadingBarHorizontal.setVisibility(View.VISIBLE);
            tvInfo.setVisibility(View.VISIBLE);
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        totalDataNew = 0;
        db = new DBHelper(context);
        size = db.getSizeDataUtama();

        try {
            Log.d(TAG_LOG_D, "Acquiring new data from ");

            //NEW DATA ACQUIRED
            for (int i = 1; i <= 12; i++) {
                if (i > 1) {
                    WEBSITE_ADDRESS = "http://www.hirupmotekar.com/page/" + i + "/?json=1";
                }

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
                parentArray = parentObject.getJSONArray("posts");

                //pengambilan data_tocheck
                for (int j = 0; j < parentArray.length(); j++) {
                    map = new HashMap<>(2);
                    e = parentArray.getJSONObject(j);
                    id = e.getString("id");
                    content = Html.fromHtml(e.getString("content")).toString(); //remove html entities
                    totalDataNew++;
                    counter++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBarHorizontal.setProgress(counter);
                            tvInfo.setText("Current Progress = Getting new data");
                        }
                    });
                    map.put("id", id);
                    map.put("content", content);
                    map.put("title", Html.fromHtml(e.getString("title")).toString());
                    data_new.add(map);
                    Log.d(TAG_LOG_D, "New Data Acquired to data_new - " + id);

                }
            }

            Log.d(TAG_LOG_D, "Acquiring old data from db");

            //OLD DATA INTO MAP
            Cursor rs = db.getAllDataUtama();
            if (rs.moveToFirst()) {
                while (!rs.isAfterLast()) {
                    map = new HashMap<>(2);
                    id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    content = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    counter++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBarHorizontal.setProgress(counter);
                            tvInfo.setText("Current Progress = Getting old data");
                        }
                    });
                    map.put("id", id);
                    map.put("content", content);
                    map.put("title", judul);
                    data_tocheck.add(map);
                    rs.moveToNext();
                    Log.d(TAG_LOG_D, "Old Data added to data_tocheck - " + id);
                }
            }

            Log.d(TAG_LOG_D, "Checking old data and new data");

            // TODO FIX THIS SHIT


            //counter = 0;
            if (totalDataNew == size) {
                Log.d(TAG_LOG_D, "Size sama");
                for (Map<String, String> tempmap_1 : data_tocheck) {
                    counter++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBarHorizontal.setProgress(counter);
                            tvInfo.setText("Current Progress = Checking if theres any data to update - " + counter + " / " + (size * 3));
                        }
                    });
                    for (Map.Entry<String, String> entry_1 : tempmap_1.entrySet()) {
                        if (entry_1.getKey().equals("id")) {
                            id_tocheck = entry_1.getValue();
                        }
                        for (Map<String, String> tempmap_2 : data_new) {
                            for (Map.Entry<String, String> entry_2 : tempmap_2.entrySet()) {
                                if (entry_2.getKey().equals("id")) {
                                    id_new = entry_2.getValue();
                                }
                                if ((entry_1.getKey().equals("content") && entry_2.getKey().equals("content")) && id_tocheck.equalsIgnoreCase(id_new) && id_new != "" && id_tocheck != "") {
                                    if (entry_1.getValue().equalsIgnoreCase(entry_2.getValue())) {
                                        Log.d(TAG_LOG_D, "SAMA " + id_new + "  - lama > " + id_tocheck);
                                        if (db.updateDataUtama(id_new, "", entry_2.getValue())) {
                                            Log.d(TAG_LOG_D, "Data updated - " + id_tocheck);
                                            //return null;
                                        }
                                    }else{
                                        if (db.updateDataUtama(id_new, "", entry_2.getValue())) {
                                            Log.d(TAG_LOG_D, "Data updated - " + id_tocheck);
                                            //return null;
                                        }
                                        /*Log.d(TAG_LOG_D, "BEDA " + id_new + "  - lama > " + id_tocheck);
                                        Log.d(TAG_LOG_D, "BEDA " + entry_1.getValue() + "  - lama > " + entry_2.getValue());
                                        if (db.updateDataUtama(idupdate, "", entry_2.getValue())) {
                                            return null;
                                        }*/
                                    }
                                } else if ((entry_1.getKey().equals("title") && entry_2.getKey().equals("title")) && id_tocheck.equalsIgnoreCase(id_new) && id_new != "" && id_tocheck != "") {
                                    if (entry_1.getValue().equalsIgnoreCase(entry_2.getValue())) {
                                        //Log.d(TAG_LOG_D, "SAMA " + id_new + "  - lama > " + id_tocheck);
                                    }else{
                                        if (db.updateDataUtama(id_new, entry_2.getValue(), "")) {
                                            Log.d(TAG_LOG_D, "Data updated - " + id_tocheck);
                                            //return null;
                                        }
                                        /*Log.d(TAG_LOG_D, "BEDA " + id_new + "  - lama > " + id_tocheck);
                                        Log.d(TAG_LOG_D, "BEDA " + entry_1.getValue() + "  - lama > " + entry_2.getValue());
                                        if (db.updateDataUtama(idupdate, "", entry_2.getValue())) {
                                            return null;
                                        }*/
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                Log.d(TAG_LOG_D, "BEDA," + totalDataNew + " , " + size);
            }
            data_new.clear();
            data_tocheck.clear();
            /*for(int i = 0; i < size; i++){
                if (!(db.isKataDasarExist(str))) {
                    if (db.addKataDasar(str)) {
                        //counter++;
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
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG_LOG_D, "Done");
        return null;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> res) {
        stopLoading();

        /*SimpleAdapter adapter = new SimpleAdapter(context, res,
                R.layout.row,
                new String[]{"id", "content", "title"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});

//        fragmentStemmingStoplist_2.setData(res, "");
//        fragmentStemmingStoplist_2.setAdapter(adapter, "utama");
        */
        endTime = System.currentTimeMillis();
        Toast.makeText(context, "Time spent - " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();
        Log.d(TAG_LOG_D, "Time spent - " + (endTime - startTime) / 1000 + " second");
    }

    private void stopLoading() {
        loadingBarHorizontal.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }
}
