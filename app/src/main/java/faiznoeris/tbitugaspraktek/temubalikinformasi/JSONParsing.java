package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
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
    String line = "",finalJson, content,id;

    public static int totalData = 0;

    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;
    Context context;
    View loadingView;
    View tampilanView;

    URL link;
    DBHelper db;
    HttpURLConnection conn;
    InputStream inputStream;
    BufferedReader bufferedReader;
    StringBuffer stringBuffer;
    JSONObject parentObject,e;
    JSONArray parentArray;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();

    public JSONParsing(FragmentStemmingStoplist_2 fragmentStoplistStemming_2, Context context, View loadingView, View tampilanView){
        this.context = context;
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;
        this.loadingView = loadingView;
        this.tampilanView = tampilanView;
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        try{
            db = new DBHelper(context);
            for(int i = 1; i <= 10; i++) {
                if (i > 1){
                    WEBSITE_ADDRESS = "http://www.hirupmotekar.com/page/"+i+"/?json=1";
                }
                //membuka koneksi
                link = new URL(WEBSITE_ADDRESS);
                conn = (HttpURLConnection) link.openConnection();
                try {
                    conn.connect();
                }catch (UnknownHostException e){
                    e.printStackTrace();
                    return null;
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

                finalJson = stringBuffer.toString();

                //rubah string json kedalam bentuk jsonobject
                parentObject = new JSONObject(finalJson);
                parentArray = parentObject.getJSONArray("posts");

                //pengambilan data
                for (int j = 0; j < parentArray.length(); j++) {
                    map = new HashMap<>(2);
                    e = parentArray.getJSONObject(j);
                    id = e.getString("id");
                    content = Html.fromHtml(e.getString("content")).toString(); //remove html entities
                    totalData++;
                    map.put("id", id);
                    map.put("content", content);
                    map.put("title", Html.fromHtml(e.getString("title")).toString());
                    data.add(map);
                    if(!(db.isDataUtamaExist(id))) {
                        if (db.addDataUtama(id, content, Html.fromHtml(e.getString("title")).toString())) {
                            //counter++;
                            Log.d(TAG_LOG_D, "Data JSON Inserted. (" + id + ", " + content + ")");
                        }
                    }
                    Log.d(TAG_LOG_D, "Data JSON added - " + id);
                }
            }

            //for specific link, like =
            /*JSONObject object = (JSONObject) new JSONTokener(finalJson).nextValue();

            content = object.getString("post");

            JSONObject object2 = (JSONObject) new JSONTokener(content).nextValue();

            content = object2.getString("content");
            String id = object2.getString("id");*/

            return data;
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }finally {
            conn.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> s) {
        showProgress(true);
        if (s != null) {
            Log.d(TAG_LOG_D, "Done - Total data = " + totalData);

            SimpleAdapter adapter = new SimpleAdapter(context, s,
                    R.layout.row,
                    new String[]{"id", "content","title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            fragmentStoplistStemming_2.setData(s, "");
            fragmentStoplistStemming_2.setAdapter(adapter, "utama");
        }else{
            showProgress(false);
            Toast.makeText(context, "Terjadi error saat pengambilan data!", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCancelled() {
        showProgress(true);
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

            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
