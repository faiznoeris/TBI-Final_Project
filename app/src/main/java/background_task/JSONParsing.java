package background_task;

import android.content.Context;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 18/04/2017.
 */

public class JSONParsing extends AsyncTask<Void, String, List<Map<String, String>>> {

    private final String TAG_LOG_D = "Parsing";
    private String WEBSITE_ADDRESS = "http://www.hirupmotekar.com/?json=1";

    String line = "", content, id;
    int totalData;
    int totalPages = 2;
    int counterLoadingBar = 0;
    long startTime, endTime;

    Context context;
    FragmentShowData fragmentShowData;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    URL link;
    DBHelper db;
    HttpURLConnection conn;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuffer stringBuffer;
    JSONObject parentObject, getObject;
    JSONArray parentArray;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();

    Handler progressHandler = new Handler();

    public JSONParsing(FragmentShowData fragmentShowData, Context context, ProgressBar loadingBar, View mainView, TextView tvInfo, int totalPages, int totalData) {
        this.context = context;
        this.fragmentShowData = fragmentShowData;
        this.loadingBar = loadingBar;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.totalPages = totalPages;
        this.totalData = totalData;
    }

    @Override
    protected void onPreExecute() {
        loadingBar.setMax(totalData);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        try {
            db = new DBHelper(context);
            for (int i = 1; i <= totalPages; i++) {
                if (i > 1) {
                    WEBSITE_ADDRESS = "http://www.hirupmotekar.com/page/" + i + "/?json=1";
                    //membuka koneksi
                }
                link = new URL(WEBSITE_ADDRESS);
                conn = (HttpURLConnection) link.openConnection();
                try {
                    conn.connect();
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

                parentObject = new JSONObject(stringBuffer.toString());

                //totalPages = Integer.parseInt(parentObject.getString("pages"));
                //totalData = Integer.parseInt(parentObject.getString("count_total"));

                parentArray = parentObject.getJSONArray("posts");

                //pengambilan data
                for (int j = 0; j < parentArray.length(); j++) {
                    map = new HashMap<>(2);
                    getObject = parentArray.getJSONObject(j);
                    id = getObject.getString("id");
                    content = Html.fromHtml(getObject.getString("content")).toString(); //remove html entities
                    map.put("id", id);
                    map.put("content", content);
                    map.put("title", Html.fromHtml(getObject.getString("title")).toString());
                    map.put("url", Html.fromHtml(getObject.getString("url")).toString());
                    data.add(map);
                    if (!(db.isDataUtamaExist(id))) {
                        if (db.addDataUtama(id, content, Html.fromHtml(getObject.getString("title")).toString(), Html.fromHtml(getObject.getString("url")).toString())) {
                            Log.d(TAG_LOG_D, "Data JSON Inserted. (" + id + ", " + Html.fromHtml(getObject.getString("url")).toString() + ")");
                        }
                    }
                    counterLoadingBar++;
                    progressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.setProgress(counterLoadingBar);
                            tvInfo.setText("Current Progress = Adding data from web | " + counterLoadingBar + " / " + totalData);
                        }
                    });
                }

            }
            return data;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> s) {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
        if (s != null) {
            Log.d(TAG_LOG_D, "Done - Total data = " + totalData);

            SimpleAdapter adapter = new SimpleAdapter(context, s,
                    R.layout.listview_row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            fragmentShowData.setData(s, "");
            fragmentShowData.setAdapter(adapter, "utama");

            endTime = System.currentTimeMillis();
            Toast.makeText(context, "Time spent adding data from website - " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();
            Log.d(TAG_LOG_D, "Time spent adding data from website  - " + (endTime - startTime) / 1000 + " second");
        } else {
            loadingBar.setVisibility(View.GONE);
            tvInfo.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
            Toast.makeText(context, "Terjadi error saat pengambilan data_tocheck!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }
}
