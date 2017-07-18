package stbi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentSearchData;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 20/04/2017.
 */

public class Stoplist extends AsyncTask<Void, String, List<Map<String, String>>> {

    private final String[] stopwords_list = {"yang", "di", "dan", "itu", "dengan", "untuk", "tidak", "ini", "dari", "dalam", "akan", "pada", "juga", "saya", "ke", "karena", "tersebut", "bisa", "ada", "mereka", "lebih", "kata",
            "tahun", "sudah", "atau", "saat", "oleh", "menjadi", "orang", "ia", "telah", "adalah", "seperti", "sebagai", "bahwa", "dapat", "para", "harus", "namun", "kita", "dua", "satu", "masih", "hari",};
    private final String TAG_LOG_D = "Stoplisting";

    int counterLoadingBar = 0;
    String[] stoplisting;
    int countTotalWord = 0;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentShowData fragmentShowData;
    FragmentSearchData fragmentSearchData;
    Context context;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();
    ArrayList<String> id = new ArrayList<String>(), content = new ArrayList<String>(), removedword = new ArrayList<String>(), title = new ArrayList<String>();

    StringBuilder builder = new StringBuilder(), removedword_temp = new StringBuilder();
    long startTime, endTime;

    String str_id,str_content,str_title,str_removedword;

    private Handler progressHandler = new Handler();

    public Stoplist(List<Map<String, String>> data, Context context, FragmentShowData fragmentShowData, ProgressBar loadingBar, View mainView, TextView tvInfo, FragmentSearchData fragmentSearchData) {
        this.context = context;
        this.data = data;
        this.loadingBar = loadingBar;
        this.mainView = mainView;
        this.tvInfo = tvInfo;
        this.fragmentShowData = fragmentShowData;
        this.fragmentSearchData = fragmentSearchData;

    }

    @Override
    protected void onPreExecute() {
        db = new DBHelper(context);
        if(fragmentSearchData != null) {
            db.clearTbStop();
        }

        counterLoadingBar = 0;
        countTotalWord = 0;
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

        for (int i = 0; i < content.size(); i++) {
            stoplisting = content.get(i).split(" "); //pecah kalimat menjadi per kata
            countTotalWord += stoplisting.length;
        }

        loadingBar.setMax(countTotalWord);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        db = new DBHelper(context);



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
                counterLoadingBar++;
                progressHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingBar.setProgress(counterLoadingBar);
                        tvInfo.setText("Current Progress = Removing words | " + counterLoadingBar + " / " + countTotalWord);
                    }
                });
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
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);


        /*
        if (fragmentSearchData != null) {
            loadingBar.setProgress(0);
            if (!db.isTBDataStemmingEmpty()) {
                Indexing indexing = new Indexing(context, null, loadingBar, mainView, tvInfo, fragmentSearchData);
                indexing.execute();
            }
        }
        */
        /*
        if (fragmentSearchData != null) {
            loadingBar.setProgress(0);
            if (!db.isTBDataIndexEmpty()) {
                Bobot bobot = new Bobot(context, null, loadingBar, mainView, tvInfo, fragmentSearchData);
                bobot.execute();
            }
        }
        */
        /*
        if (fragmentSearchData != null) {
            loadingBar.setProgress(0);
            if (!db.isTBDataIndexEmpty()) {
                Vektor vektor = new Vektor(context, null, loadingBar, mainView, tvInfo);
                vektor.execute();
            }
        }
         */
        if(fragmentSearchData != null){
            loadingBar.setProgress(0);
            try {
                Stemming stemming = new Stemming(data, context, null, loadingBar, mainView, tvInfo, fragmentSearchData);
                stemming.execute();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            endTime = System.currentTimeMillis();
            Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
            SimpleAdapter adapter = new SimpleAdapter(context, data,
                    R.layout.listview_row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            Toast.makeText(context, "Task done in " + (endTime - startTime) / 1000 + " seconds", Toast.LENGTH_SHORT).show();

            fragmentShowData.setData(data, "");
            fragmentShowData.setAdapter(adapter, "stoplist");
        }
    }


    @Override
    protected void onCancelled() {
        loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }
}