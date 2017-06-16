package stemming_stoplist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.InsertData;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Vellfire on 20/04/2017.
 */

public class Stoplist extends AsyncTask<Void, String, List<Map<String, String>>> {

    private final String[] stopwords_list = {"yang", "di", "dan", "itu", "dengan", "untuk", "tidak", "ini", "dari", "dalam", "akan", "pada", "juga", "saya", "ke", "karena", "tersebut", "bisa", "ada", "mereka", "lebih", "kata",
            "tahun", "sudah", "atau", "saat", "oleh", "menjadi", "orang", "ia", "telah", "adalah", "seperti", "sebagai", "bahwa", "dapat", "para", "harus", "namun", "kita", "dua", "satu", "masih", "hari",};
    private final String TAG_LOG_D = "Stoplisting";

    String[] stoplisting;

    View loadingView;
    View tampilanView;

    DBHelper db;

    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;
    InsertData insertData;
    Context context;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();
    ArrayList<String> id = new ArrayList<String>(), content = new ArrayList<String>(), removedword = new ArrayList<String>(), title = new ArrayList<String>();

    StringBuilder builder = new StringBuilder(), removedword_temp = new StringBuilder();
    long startTime, endTime;

    String str_id,str_content,str_title,str_removedword;

    public Stoplist(List<Map<String, String>> data, Context context, FragmentStemmingStoplist_2 fragmentStoplistStemming_2, View loadingView, View tampilanView) {
        this.context = context;
        this.data = data;
        this.loadingView = loadingView;
        this.tampilanView = tampilanView;
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;

    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        db = new DBHelper(context);

        //memecah data
        for (Map<String, String> tempmap : data) {
            for (Map.Entry<String, String> entry : tempmap.entrySet()) {
                if (entry.getKey().equals("content") && !(entry.getValue() == null)) {
                    content.add(entry.getValue());
                } else if(entry.getKey().equals("id") && !(entry.getValue() == null)){
                    id.add(entry.getValue());
                } else if(entry.getKey().equals("title") && !(entry.getValue() == null)){
                    title.add(entry.getValue());
                }
                removedword.add("A");
            }
        }

        //proses stoplist
        for (int i = 0; i < content.size(); i++) {
            stoplisting = content.get(i).split(" "); //pecah kalimat menjadi per kata
            removedword_temp = new StringBuilder();
            builder = new StringBuilder();
            for (int j = 0; j < stoplisting.length; j++) {
                for (int k = 0; k < stopwords_list.length; k++) {
                    if (stoplisting[j].equalsIgnoreCase(stopwords_list[k])) {
                        if(!removedword_temp.toString().contains(stoplisting[j])) {
                            removedword_temp.append(stoplisting[j] + ", ");
                        }
                        stoplisting[j] = "";
                        Log.d(TAG_LOG_D, "Word removed - " + stopwords_list[k]);
                    }
                }
            }

            //penyatuan kata
            for (String s : stoplisting) {
                builder.append(s + " ");
            }
            content.set(i, builder.toString());
            removedword.set(i, removedword_temp.toString());
        }

        //pemasukan data kembali ke list
        data.clear();
        for (int i = 0; i < content.size(); i++) {
            map = new HashMap<>(2);
            str_id = id.get(i);
            str_content = content.get(i);
            str_title = title.get(i);
            str_removedword = removedword.get(i);
            map.put("id", str_id);
            map.put("content", str_content);
            map.put("title", str_title);
            map.put("removedword", str_removedword);
            data.add(map);
            if (!(db.isDataStoplistExist(str_id))) {
                if (db.addDataStoplist(str_id, str_content, str_title, str_removedword)) {
                    //counter++;
                    Log.d(TAG_LOG_D, "Data Stoplist Inserted. (" + str_id + ", " + str_title + ")");
                }
            }
            Log.d(TAG_LOG_D, "Data Stoplist added - " + str_id);
        }

        return data;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> data) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime-startTime)/1000 + " seconds");
        showProgress(true);
        SimpleAdapter adapter = new SimpleAdapter(context, data,
                R.layout.row,
                new String[]{"id", "content", "title"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});

        Toast.makeText(context, "Task done in " + (endTime-startTime)/1000 + " seconds", Toast.LENGTH_SHORT).show();

        fragmentStoplistStemming_2.setData(data, "");
        fragmentStoplistStemming_2.setAdapter(adapter, "stoplist");

        /*if(db.isTBKataDasarEmpty()) {
            InsertData insertData = new InsertData(context, loadingView, null, tampilanView, null, data, fragmentStoplistStemming_2);
            insertData.execute("insert_stoplist");
        }*/
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